package hxc.services.caisim;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
import hxc.services.notification.INotifications;
import hxc.services.numberplan.INumberPlan;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;

public class CaiSimService implements IService
{
	final static Logger logger = LoggerFactory.getLogger(CaiSimService.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	private IServiceBus esb;
	private INumberPlan numberPlan;
	private Endpoint endpoint;
	private HttpServer server;
	private BlockingQueue<Runnable> requestQueue = null;
	private ThreadPoolExecutor threadPool = null;
	private ICaiSim caiSimulator;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public ICaiSim getCaiSimulator()
	{
		return caiSimulator;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IService Implementation
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
		// Get the number plan service
		numberPlan = esb.getFirstService(INumberPlan.class);
		if (numberPlan == null)
			return false;

		logger.info("Starting CAI Simulator: soapPort = {}.", config.soapPort);

		try
		{
			// Create the HTTP server
			logger.info("going to start on port {} with backlog {}", config.soapPort, config.maxBacklog);
			server = HttpServer.create(new InetSocketAddress(config.soapPort), config.maxBacklog);

			// Start the server
			server.start();

			// Create the context
			HttpContext context = server.createContext(config.soapPath);

			// Create CAI simulator
			caiSimulator = new CaiSim(esb, numberPlan, config.maxThreadPoolSize, config.caiPort);

			// Add the authentication to the sim
			caiSimulator.addAuthentication(config.userId, config.password);

			// Create the end point with the CAI simulator
			endpoint = Endpoint.create(caiSimulator);

			// Create the thread pool
			requestQueue = new ArrayBlockingQueue<Runnable>(config.threadQueueCapacity);
			threadPool = new ThreadPoolExecutor(1, config.maxThreadPoolSize, 1, TimeUnit.HOURS, requestQueue, new ThreadFactory()
			{

				private int num = 0;

				// Name the threads that are created
				@Override
				public Thread newThread(Runnable r)
				{
					return new Thread(r, "CaiSimThreadPool-" + num++);
				}

			})
			{
				// Capture what happens when executing a request
				@Override
				public void execute(Runnable command)
				{
					try
					{
						super.execute(command);
					}
					catch (Throwable e)
					{
						logger.error("Execute error", e);
					}

				}

				@Override
				protected void afterExecute(Runnable r, Throwable t)
				{
					super.afterExecute(r, t);

					if (t != null)
						logger.info("afterExecute", t);
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
						catch (Throwable e)
						{
							logger.info("submit", e);
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

			// Set the executor for the endpoint
			endpoint.setExecutor(threadPool);

			// Publish the wsdl and soap service
			endpoint.publish(context);

		}
		catch (Throwable e)
		{
			logger.error("Failed to start", e);
			return false;
		}

		logger.info("CAI Simulator Started.");

		return true;
	}

	@Override
	public void stop()
	{
		logger.info("Stopping CAI Simulator");

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

		logger.info("CAI Simulator Stopped.");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		this.config = (CaiSimConfiguration) config;
	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return false;
	}

	@Override
	public boolean isFit()
	{
		if (server == null || endpoint == null || !endpoint.isPublished())
			return false;

		// TODO: Test Soap Protocol
		return true;
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
	@Perms(perms = { @Perm(category = "CaiSim", description = "CAI Simulator Permissions", name = "CAI Simulator") })
	class CaiSimConfiguration extends ConfigurationBase
	{

		private int caiPort = 3300;
		private int soapPort = 3301;
		private String soapPath = "/Cai";
		private int maxBacklog = 10;
		private int threadQueueCapacity = 10;
		private int maxThreadPoolSize = 10;
		private String userId = "sogadm";
		private String password = "sogadm";

		public int getCaiPort()
		{
			return caiPort;
		}

		public void setCaiPort(int caiPort)
		{
			this.caiPort = caiPort;
		}

		public int getSoapPort()
		{
			return soapPort;
		}

		public void setSoapPort(int soapPort)
		{
			this.soapPort = soapPort;
		}

		public String getSoapPath()
		{
			return soapPath;
		}

		public void setSoapPath(String soapPath)
		{
			this.soapPath = soapPath;
		}

		public int getMaxBacklog()
		{
			return maxBacklog;
		}

		public void setMaxBacklog(int maxBacklog)
		{
			this.maxBacklog = maxBacklog;
		}

		public int getThreadQueueCapacity()
		{
			return threadQueueCapacity;
		}

		public void setThreadQueueCapacity(int threadQueueCapacity)
		{
			this.threadQueueCapacity = threadQueueCapacity;
		}

		public int getMaxThreadPoolSize()
		{
			return maxThreadPoolSize;
		}

		public void setMaxThreadPoolSize(int maxThreadPoolSize)
		{
			this.maxThreadPoolSize = maxThreadPoolSize;
		}

		public String getUserId()
		{
			return userId;
		}

		public void setUserId(String userId)
		{
			this.userId = userId;
		}

		public String getPassword()
		{
			return password;
		}

		public void setPassword(String password)
		{
			this.password = password;
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
			return -3179878276095990445L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "CAI Simulator";
		}

		@Override
		public void validate() throws ValidationException
		{

		}

	}

	private CaiSimConfiguration config = new CaiSimConfiguration();

}
