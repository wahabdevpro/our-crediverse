package hxc.services.airsim;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.Endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.servicebus.IServiceBus;
import hxc.services.IService;
import hxc.services.airsim.protocol.AirCalls;
import hxc.services.airsim.protocol.IAirSim;
import hxc.services.airsim.protocol.IAirSimService;
import hxc.services.airsim.protocol.ICdr;
import hxc.services.airsim.protocol.ISmsHistory;
import hxc.services.airsim.protocol.IUssdResponse;
import hxc.services.notification.INotifications;
import hxc.services.numberplan.INumberPlan;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.security.SupplierOnly;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.thread.TimedThread;
import hxc.utils.thread.TimedThread.TimedThreadType;

public class AirSimService implements IService, IAirSimService, IAirSimProvider
{
	final static Logger logger = LoggerFactory.getLogger(AirSimService.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private Endpoint endpoint;
	private HttpServer server = null;
	private AirSim airsim = null;
	private BlockingQueue<Runnable> requestQueue = null;
	private ThreadPoolExecutor threadPool = null;
	private INumberPlan numberPlan = null;
	private TimedThread autoSaveThread = null;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IConnector Implementation
	//
	// /////////////////////////////////

	@Override
	public void initialise(IServiceBus esb)
	{
		this.esb = esb;
	}

	@Override
	public boolean start(String[] args)
	{
		// Get Number Plan
		numberPlan = esb.getFirstService(INumberPlan.class);
		if (numberPlan == null)
			return false;

		// Publish
		logger.info("Starting Soap Connector on {}", config.getSoapURL());
		try
		{
			server = HttpServer.create(new InetSocketAddress(config.soapPort), config.maxBacklog);
			server.start();
			HttpContext context = server.createContext(config.soapPath);

			airsim = new AirSim(esb, config.airPort, config.airPath, numberPlan, config.currency, config.stateFilename);
			endpoint = Endpoint.create(airsim);

			// Create the thread pool
			requestQueue = new ArrayBlockingQueue<Runnable>(config.threadQueueCapacity);
			threadPool = new ThreadPoolExecutor(1, config.maxThreadPoolSize, 1, TimeUnit.HOURS, requestQueue, new ThreadFactory()
			{

				private int num = 0;

				@Override
				public Thread newThread(Runnable r)
				{
					return new Thread(r, "AirSimThreadPool-" + num++);
				}

			})
			{
				@Override
				public void execute(Runnable command)
				{
					try
					{
						super.execute(command);
					}
					catch (Throwable e)
					{
						logger.error("Failed to execute command", e);
					}

				}

				@Override
				protected void afterExecute(Runnable r, Throwable t)
				{
					super.afterExecute(r, t);

					if (t != null)
						logger.error("afterExecute failed", t);
				}

				@Override
				public Future<?> submit(Runnable task)
				{
					// Catch the exception that was thrown if any
					Future<?> future = super.submit(task);
					if (future != null)
					{
						try
						{
							future.get();
						}
						catch (InterruptedException e)
						{
							logger.error("Future interupted", e);
						}
						catch (ExecutionException e)
						{
							logger.error("Execution exception", e);
						}
					}

					return future;
				}
			};

			// Prevent Overflow
			threadPool.setRejectedExecutionHandler(new RejectedExecutionHandler()
			{
				@Override
				public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
				{
					if (!executor.isShutdown())
					{
						r.run();
					}
				}
			});
			endpoint.setExecutor(threadPool);

			endpoint.publish(context);
		}
		catch (Exception ex)
		{
			logger.error("AirSimService failed to start", ex);
			return false;
		}

		// Start Auto Save thread
		if (this.config.autoStateSaveIntervalMinutes != null)
		{
			autoSaveThread = new TimedThread("AirSim Auto Save", this.config.autoStateSaveIntervalMinutes * 60000L, TimedThreadType.INTERVAL)
			{
				@Override
				public void action()
				{
					if (airsim != null) {
						try 
						{
							airsim.saveState();
						} 
						catch(Exception e) {}
					}
						
				}
			};
			autoSaveThread.start();
		}

		// Log Information
		logger.info("AIR Simulator Started on {}", config.getSoapURL());

		return true;
	}

	@Override
	public void stop()
	{
		logger.info("Stopping AIR Simulator on {}", config.getSoapURL());

		// Kill the Auto Save Thread
		if (autoSaveThread != null)
		{
			autoSaveThread.kill();
			autoSaveThread = null;
		}

		// Save one last time
		if (this.config.autoStateSaveIntervalMinutes != null && airsim != null)
		{
			try 
			{
				airsim.saveState();
			} 
			catch(Exception e) {}
		}

		// Stop
		if (endpoint != null && endpoint.isPublished())
			endpoint.stop();

		// Shutdown the thread pool
		if (threadPool != null)
			threadPool.shutdown();
		threadPool = null;
		requestQueue = null;

		// Stop Server
		if (server != null)
		{
			try
			{
				server.stop(0);
				synchronized (this)
				{
					Thread.sleep(50);
				}
			}
			catch (InterruptedException e)
			{
			}
			server = null;
		}

		// Log Information
		logger.info("AIR Simulator Stopped on {}", config.getSoapURL());

	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		// Save the new Configuration
		this.config = (AirSimConfiguration) config;

		// Stop the Service
		stop();

		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException exc)
		{

		}

		// Re-Start the Service
		start(null);

	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return false;
	}

	@Override
	public boolean isFit()
	{
		return config.isFit && server != null && endpoint != null && endpoint.isPublished();
	}

	@Override
	public IMetric[] getMetrics()
	{
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////
	@Perms(perms = { @Perm(name = "ViewAirSimParameters", description = "View Air Simulator Parameters", category = "Air Simulator", supplier = true),
			@Perm(name = "ChangeAirSimParameters", implies = "ViewAirSimParameters", description = "Change Air Simulator Parameters", category = "Air Simulator", supplier = true) })
	public class AirSimConfiguration extends ConfigurationBase
	{
		private int soapPort = 10012;
		private String soapPath = "/Air";
		private int airPort = 10011;
		private String airPath = "/Air";
		private int threadQueueCapacity = 5;
		private int maxThreadPoolSize = 5;
		private int maxBacklog = 2;
		private String currency = "CFR";
		private String stateFilename = "/tmp/C4U/air_sim_state.json";
		private Integer autoStateSaveIntervalMinutes = null;
		private boolean isFit = true;

		public int getAirPort()
		{
			check(esb, "ViewAirSimParameters");
			return airPort;
		}

		public void setAirPort(int airPort) throws ValidationException
		{
			check(esb, "ChangeAirSimParameters");

			ValidationException.port(airPort, "AirPort");
			this.airPort = airPort;
		}

		public String getAirPath()
		{
			check(esb, "ViewAirSimParameters");
			return airPath;
		}

		public void setAirPath(String airPath)
		{
			check(esb, "ChangeAirSimParameters");
			this.airPath = airPath;
		}

		public String getSoapPath()
		{
			check(esb, "ViewAirSimParameters");
			return soapPath;
		}

		public void setSoapPath(String soapPath)
		{
			check(esb, "ChangeAirSimParameters");
			this.soapPath = soapPath;
		}

		public String getCurrency()
		{
			check(esb, "ViewAirSimParameters");
			return currency;
		}

		public void setCurrency(String currency)
		{
			check(esb, "ChangeAirSimParameters");
			this.currency = currency;
		}

		public AirSimConfiguration()
		{
		}

		public String getSoapURL()
		{
			check(esb, "ViewAirSimParameters");
			return String.format("http://%s:%d%s", "0.0.0.0", soapPort, soapPath);
		}

		public int getSoapPort()
		{
			check(esb, "ViewAirSimParameters");
			return soapPort;
		}

		public void setSoapPort(int httpPort) throws ValidationException
		{
			check(esb, "ChangeAirSimParameters");

			ValidationException.port(httpPort, "SOAPPort");
			this.soapPort = httpPort;
		}

		public int getThreadQueueCapacity()
		{
			check(esb, "ViewAirSimParameters");
			return threadQueueCapacity;
		}

		@SupplierOnly
		public void setThreadQueueCapacity(int threadQueueCapacity) throws ValidationException
		{
			check(esb, "ChangeAirSimParameters");
			ValidationException.inRange(1, threadQueueCapacity, 100, "ThreadQueueCapacity");
			this.threadQueueCapacity = threadQueueCapacity;
		}

		public int getMaxThreadPoolSize()
		{
			check(esb, "ViewAirSimParameters");
			return maxThreadPoolSize;
		}

		@SupplierOnly
		public void setMaxThreadPoolSize(int maxThreadPoolSize) throws ValidationException
		{
			check(esb, "ChangeAirSimParameters");
			ValidationException.inRange(5, maxThreadPoolSize, 1000, "MaxThreadPoolSize");
			this.maxThreadPoolSize = maxThreadPoolSize;
		}

		public int getMaxBacklog()
		{
			check(esb, "ViewAirSimParameters");
			return maxBacklog;
		}

		@SupplierOnly
		public void setMaxBacklog(int maxBacklog) throws ValidationException
		{
			check(esb, "ChangeAirSimParameters");
			ValidationException.inRange(2, maxBacklog, 100, "MaxBacklog");
			this.maxBacklog = maxBacklog;
		}

		public String getStateFilename()
		{
			check(esb, "ViewAirSimParameters");
			return stateFilename;
		}

		public void setStateFilename(String stateFilename)
		{
			check(esb, "ChangeAirSimParameters");
			this.stateFilename = stateFilename;
		}

		public Integer getAutoStateSaveIntervalMinutes()
		{
			check(esb, "ChangeAirSimParameters");
			return autoStateSaveIntervalMinutes;
		}

		public void setAutoStateSaveIntervalMinutes(Integer autoStateSaveIntervalMinutes)
		{
			check(esb, "ChangeAirSimParameters");
			this.autoStateSaveIntervalMinutes = autoStateSaveIntervalMinutes;
		}

		public String ClearState() throws Exception
		{
			if (airsim != null)
				airsim.reset();

			return SaveState();
		}

		public boolean isFit()
		{
			check(esb, "ViewAirSimParameters");
			return isFit;
		}
		
		@SupplierOnly
		public void setFit(boolean isFit)
		{
			check(esb, "ChangeAirSimParameters");
			this.isFit = isFit;
		}

		public String SaveState()
		{
			check(esb, "ChangeAirSimParameters");
			if (airsim == null)
				return "Failed";

			boolean ok = airsim.saveState();
			
			if (!ok)
				throw new AssertionError("A problem prevented AirSIM state from being saved. Please check C4U logs for details");
			else
				return "Success";

		}

		public String RestoreState() {
			check(esb, "ChangeAirSimParameters");
			if (airsim == null) {
				return "Failed";
			}

			boolean ok = airsim.restoreState();
			if (!ok) {
				logger.warn("A problem prevented AirSIM state from being restored. Please check C4U logs for details");
				return "Failure";
			} else {
				return "Success";
			}
		}
		
		public String StartSimulator()
		{
			check(esb, "ChangeAirSimParameters");
			if (airsim == null)
				return "Failed";

			boolean ok = airsim.start();
			if (!ok)
				throw new AssertionError("A problem prevented AirSIM from starting. Please check C4U logs for details");
			else
				return "Success";
		}
		
		public String StopSimulator()
		{
			check(esb, "ChangeAirSimParameters");
			if (airsim == null)
				return "Failed";

			airsim.stop();
			return "Success";
		}


		@Override
		public String getPath(String languageCode)
		{
			return "Testing";
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

		@Override
		public long getSerialVersionUID()
		{
			return 4155099346172906269L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "Air Simulator";
		}

		@Override
		public void validate() throws ValidationException
		{
			if (autoStateSaveIntervalMinutes != null)
				ValidationException.inRange(5, autoStateSaveIntervalMinutes, 10080, "autoStateSaveIntervalMinutes", //
						"The Auto State Save Interval must be between 5 and 10080 minutes (or empty)");
		}

	}

	AirSimConfiguration config = new AirSimConfiguration();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IAirsimService Implementation
	//
	// /////////////////////////////////

	@Override
	public ICdr[] getCdrHistory()
	{
		return airsim.getCdrHistory();
	}

	@Override
	public ISmsHistory[] getSmsHistory()
	{
		return airsim.getSmsHistory();
	}
	
	@Override
	public void clearSmsHistory()
	{
		airsim.clearSmsHistory();
	}
	
	@Override
	public IUssdResponse injectMOUssd(String from, String text, String imsi)
	{
		return airsim.injectMOUssd(from, text, imsi);
	}
	
	@Override
	public void injectMOSms(String from, String to, String text)
	{
		airsim.injectMOSms(from, to, text);
	}
	
	@Override
	public void injectAirResponse(String airCallName, String responseCode, String delay)
	{
		AirCalls airCall = AirCalls.valueOf(airCallName);
		airsim.injectResponse(airCall, Integer.valueOf(responseCode), Integer.valueOf(delay));
	}
	
	@Override
	public void resetInjectedAirResponse(String airCallName)
	{
		AirCalls airCall = AirCalls.valueOf(airCallName);
		airsim.resetInjectedResponse(airCall);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IAirSimProvider Implementation
	//
	// /////////////////////////////////

	@Override
	public IAirSim getAirSim()
	{
		return airsim;
	}

}
