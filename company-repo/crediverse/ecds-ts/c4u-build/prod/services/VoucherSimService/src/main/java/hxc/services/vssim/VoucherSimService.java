package hxc.services.vssim;

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

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import hxc.configuration.Config;
import hxc.configuration.IConfiguration;
import hxc.configuration.Rendering;
import hxc.configuration.ValidationException;
import hxc.servicebus.IServiceBus;
import hxc.services.IService;
import hxc.services.notification.INotifications;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.security.SupplierOnly;
import hxc.services.vssim.engine.ChangeVoucherState;
import hxc.services.vssim.engine.DeleteChangeVoucherStateTask;
import hxc.services.vssim.engine.DeleteGenerateVoucherTask;
import hxc.services.vssim.engine.DeleteLoadVoucherBatchTask;
import hxc.services.vssim.engine.DeletePurgeVoucherTask;
import hxc.services.vssim.engine.DeleteVoucherDetailsReportTask;
import hxc.services.vssim.engine.DeleteVoucherDistributionReportTask;
import hxc.services.vssim.engine.DeleteVoucherUsageReportTask;
import hxc.services.vssim.engine.EndReservation;
import hxc.services.vssim.engine.GenerateVoucher;
import hxc.services.vssim.engine.GenerateVoucherDetailsReport;
import hxc.services.vssim.engine.GenerateVoucherDistributionReport;
import hxc.services.vssim.engine.GenerateVoucherUsageReport;
import hxc.services.vssim.engine.GetChangeVoucherStateTaskInfo;
import hxc.services.vssim.engine.GetGenerateVoucherDetailsReportTaskInfo;
import hxc.services.vssim.engine.GetGenerateVoucherDistributionReportTaskInfo;
import hxc.services.vssim.engine.GetGenerateVoucherTaskInfo;
import hxc.services.vssim.engine.GetGenerateVoucherUsageReportTaskInfo;
import hxc.services.vssim.engine.GetLoadVoucherBatchFileTaskInfo;
import hxc.services.vssim.engine.GetPurgeVouchersTaskInfo;
import hxc.services.vssim.engine.GetVoucherBatchFilesList;
import hxc.services.vssim.engine.GetVoucherDetails;
import hxc.services.vssim.engine.GetVoucherHistory;
import hxc.services.vssim.engine.LoadVoucherBatchFile;
import hxc.services.vssim.engine.LoadVoucherCheck;
import hxc.services.vssim.engine.PurgeVouchers;
import hxc.services.vssim.engine.ReserveVoucher;
import hxc.services.vssim.engine.UpdateVoucherState;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.protocol.vsip.ChangeVoucherStateCallRequest;
import hxc.utils.protocol.vsip.DeleteChangeVoucherStateTaskCallRequest;
import hxc.utils.protocol.vsip.DeleteGenerateVoucherTaskCallRequest;
import hxc.utils.protocol.vsip.DeleteLoadVoucherBatchTaskCallRequest;
import hxc.utils.protocol.vsip.DeletePurgeVoucherTaskCallRequest;
import hxc.utils.protocol.vsip.DeleteVoucherDetailsReportTaskCallRequest;
import hxc.utils.protocol.vsip.DeleteVoucherDistributionReportTaskCallRequest;
import hxc.utils.protocol.vsip.DeleteVoucherUsageReportTaskCallRequest;
import hxc.utils.protocol.vsip.EndReservationCallRequest;
import hxc.utils.protocol.vsip.GenerateVoucherCallRequest;
import hxc.utils.protocol.vsip.GenerateVoucherDetailsReportCallRequest;
import hxc.utils.protocol.vsip.GenerateVoucherDistributionReportCallRequest;
import hxc.utils.protocol.vsip.GenerateVoucherUsageReportCallRequest;
import hxc.utils.protocol.vsip.GetChangeVoucherStateTaskInfoCallRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherDetailsReportTaskInfoCallRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherDistributionReportTaskInfoCallRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherTaskInfoCallRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherUsageReportTaskInfoCallRequest;
import hxc.utils.protocol.vsip.GetLoadVoucherBatchFileTaskInfoCallRequest;
import hxc.utils.protocol.vsip.GetPurgeVouchersTaskInfoCallRequest;
import hxc.utils.protocol.vsip.GetVoucherBatchFilesListCallRequest;
import hxc.utils.protocol.vsip.GetVoucherDetailsCallRequest;
import hxc.utils.protocol.vsip.GetVoucherHistoryCallRequest;
import hxc.utils.protocol.vsip.IValidationContext;
import hxc.utils.protocol.vsip.LoadVoucherBatchFileCallRequest;
import hxc.utils.protocol.vsip.LoadVoucherCheckCallRequest;
import hxc.utils.protocol.vsip.PurgeVouchersCallRequest;
import hxc.utils.protocol.vsip.ReserveVoucherCallRequest;
import hxc.utils.protocol.vsip.UpdateVoucherStateCallRequest;
import hxc.utils.xmlrpc.XmlRpcRequest;
import hxc.utils.xmlrpc.XmlRpcServer;

public class VoucherSimService implements IService, IVoucherSimService, IValidationContext
{
	final static Logger logger = LoggerFactory.getLogger(VoucherSimService.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private Endpoint endpoint;
	private HttpServer server = null;
	private BlockingQueue<Runnable> requestQueue = null;
	private ThreadPoolExecutor threadPool = null;
	private VoucherSim voucherSimulator = null;

	private Class<?>[] supportedCalls = new Class<?>[] { GetVoucherDetailsCallRequest.class, GetVoucherHistoryCallRequest.class, UpdateVoucherStateCallRequest.class,
			LoadVoucherCheckCallRequest.class, GenerateVoucherCallRequest.class, GetGenerateVoucherTaskInfoCallRequest.class, LoadVoucherBatchFileCallRequest.class,
			GetLoadVoucherBatchFileTaskInfoCallRequest.class, GetVoucherBatchFilesListCallRequest.class, ChangeVoucherStateCallRequest.class, GetChangeVoucherStateTaskInfoCallRequest.class,
			PurgeVouchersCallRequest.class, GetPurgeVouchersTaskInfoCallRequest.class, GenerateVoucherDetailsReportCallRequest.class, GetGenerateVoucherDetailsReportTaskInfoCallRequest.class,
			GenerateVoucherDistributionReportCallRequest.class, GetGenerateVoucherDistributionReportTaskInfoCallRequest.class, GenerateVoucherUsageReportCallRequest.class,
			GetGenerateVoucherUsageReportTaskInfoCallRequest.class, DeleteGenerateVoucherTaskCallRequest.class, DeleteLoadVoucherBatchTaskCallRequest.class,
			DeleteChangeVoucherStateTaskCallRequest.class, DeletePurgeVoucherTaskCallRequest.class, DeleteVoucherDetailsReportTaskCallRequest.class,
			DeleteVoucherDistributionReportTaskCallRequest.class, DeleteVoucherUsageReportTaskCallRequest.class, ReserveVoucherCallRequest.class, EndReservationCallRequest.class };

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
		// Publish
		logger.info("Starting Soap Connector on {}", config.getSoapURL());
		try
		{
			server = HttpServer.create(new InetSocketAddress(config.soapPort), config.maxBacklog);
			server.start();
			HttpContext context = server.createContext(config.soapPath);

			BasicAuthenticator soapAuthenticator = new BasicAuthenticator("HxC")
			{
				@Override
				public boolean checkCredentials(String arg0, String arg1)
				{
					if (VoucherSimService.this.checkCredentials(arg0, arg1))
						return true;
					else
					{
						logger.warn("SOAP Authentication failed for {}", arg0);
						return false;
					}
				}
			};
			context.setAuthenticator(soapAuthenticator);

			voucherSimulator = new VoucherSim(esb, this, config.maxScheduledTasks);
			endpoint = Endpoint.create(voucherSimulator);

			// Create the thread pool
			requestQueue = new ArrayBlockingQueue<Runnable>(config.threadQueueCapacity);
			threadPool = new ThreadPoolExecutor(1, config.maxThreadPoolSize, 1, TimeUnit.HOURS, requestQueue, new ThreadFactory()
			{

				private int num = 0;

				@Override
				public Thread newThread(Runnable r)
				{
					return new Thread(r, "VoucherSimThreadPool-" + num++);
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
						logger.error("Execute failed", e);
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
						catch (InterruptedException | ExecutionException e)
						{
							logger.error("submit failed", e);
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

			XmlRpcServer server = new XmlRpcServer(supportedCalls)
			{
				@Override
				protected void uponXmlRpcRequest(XmlRpcRequest rpc)
				{
					try
					{
						String operatorID = config.getOperatorID();

						String typeName = rpc.getTypeName();
						switch (typeName)
						{

							case "GetVoucherDetailsCallRequest":
								rpc.respond(GetVoucherDetails.call(voucherSimulator, (GetVoucherDetailsCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "GetVoucherHistoryCallRequest":
								rpc.respond(GetVoucherHistory.call(voucherSimulator, (GetVoucherHistoryCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "UpdateVoucherStateCallRequest":
								rpc.respond(UpdateVoucherState.call(voucherSimulator, (UpdateVoucherStateCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "LoadVoucherCheckCallRequest":
								rpc.respond(LoadVoucherCheck.call(voucherSimulator, (LoadVoucherCheckCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "GenerateVoucherCallRequest":
								rpc.respond(GenerateVoucher.call(voucherSimulator, (GenerateVoucherCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "GetGenerateVoucherTaskInfoCallRequest":
								rpc.respond(GetGenerateVoucherTaskInfo.call(voucherSimulator, (GetGenerateVoucherTaskInfoCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "LoadVoucherBatchFileCallRequest":
								rpc.respond(LoadVoucherBatchFile.call(voucherSimulator, (LoadVoucherBatchFileCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "GetLoadVoucherBatchFileTaskInfoCallRequest":
								rpc.respond(GetLoadVoucherBatchFileTaskInfo.call(voucherSimulator, (GetLoadVoucherBatchFileTaskInfoCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "GetVoucherBatchFilesListCallRequest":
								rpc.respond(GetVoucherBatchFilesList.call(voucherSimulator, (GetVoucherBatchFilesListCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "ChangeVoucherStateCallRequest":
								rpc.respond(ChangeVoucherState.call(voucherSimulator, (ChangeVoucherStateCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "GetChangeVoucherStateTaskInfoCallRequest":
								rpc.respond(GetChangeVoucherStateTaskInfo.call(voucherSimulator, (GetChangeVoucherStateTaskInfoCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "PurgeVouchersCallRequest":
								rpc.respond(PurgeVouchers.call(voucherSimulator, (PurgeVouchersCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "GetPurgeVouchersTaskInfoCallRequest":
								rpc.respond(GetPurgeVouchersTaskInfo.call(voucherSimulator, (GetPurgeVouchersTaskInfoCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "GenerateVoucherDetailsReportCallRequest":
								rpc.respond(GenerateVoucherDetailsReport.call(voucherSimulator, (GenerateVoucherDetailsReportCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "GetGenerateVoucherDetailsReportTaskInfoCallRequest":
								rpc.respond(GetGenerateVoucherDetailsReportTaskInfo.call(voucherSimulator, (GetGenerateVoucherDetailsReportTaskInfoCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "GenerateVoucherDistributionReportCallRequest":
								rpc.respond(GenerateVoucherDistributionReport.call(voucherSimulator, (GenerateVoucherDistributionReportCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "GetGenerateVoucherDistributionReportTaskInfoCallRequest":
								rpc.respond(GetGenerateVoucherDistributionReportTaskInfo.call(voucherSimulator, (GetGenerateVoucherDistributionReportTaskInfoCallRequest) rpc.getMethodCall(),
										operatorID));
								break;

							case "GenerateVoucherUsageReportCallRequest":
								rpc.respond(GenerateVoucherUsageReport.call(voucherSimulator, (GenerateVoucherUsageReportCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "GetGenerateVoucherUsageReportTaskInfoCallRequest":
								rpc.respond(GetGenerateVoucherUsageReportTaskInfo.call(voucherSimulator, (GetGenerateVoucherUsageReportTaskInfoCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "DeleteGenerateVoucherTaskCallRequest":
								rpc.respond(DeleteGenerateVoucherTask.call(voucherSimulator, (DeleteGenerateVoucherTaskCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "DeleteLoadVoucherBatchTaskCallRequest":
								rpc.respond(DeleteLoadVoucherBatchTask.call(voucherSimulator, (DeleteLoadVoucherBatchTaskCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "DeleteChangeVoucherStateTaskCallRequest":
								rpc.respond(DeleteChangeVoucherStateTask.call(voucherSimulator, (DeleteChangeVoucherStateTaskCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "DeletePurgeVoucherTaskCallRequest":
								rpc.respond(DeletePurgeVoucherTask.call(voucherSimulator, (DeletePurgeVoucherTaskCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "DeleteVoucherDetailsReportTaskCallRequest":
								rpc.respond(DeleteVoucherDetailsReportTask.call(voucherSimulator, (DeleteVoucherDetailsReportTaskCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "DeleteVoucherDistributionReportTaskCallRequest":
								rpc.respond(DeleteVoucherDistributionReportTask.call(voucherSimulator, (DeleteVoucherDistributionReportTaskCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "DeleteVoucherUsageReportTaskCallRequest":
								rpc.respond(DeleteVoucherUsageReportTask.call(voucherSimulator, (DeleteVoucherUsageReportTaskCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "ReserveVoucherCallRequest":
								rpc.respond(ReserveVoucher.call(voucherSimulator, (ReserveVoucherCallRequest) rpc.getMethodCall(), operatorID));
								break;

							case "EndReservationCallRequest":
								rpc.respond(EndReservation.call(voucherSimulator, (EndReservationCallRequest) rpc.getMethodCall(), operatorID));
								break;

							default:
								throw new Exception("Unsupported VSIP Call: " + typeName);

						}

					}
					catch (Exception e)
					{
						logger.error("failed to start", e);
					}
				}

			};

			server.start(config.voucherPort, config.voucherPath);

			BasicAuthenticator xmlrpcAuthenticator = new BasicAuthenticator("HxC")
			{
				@Override
				public boolean checkCredentials(String arg0, String arg1)
				{
					if (VoucherSimService.this.checkCredentials(arg0, arg1))
						return true;
					else
					{
						logger.warn("SOAP Authentication failed for {}", arg0);
						return false;
					}
				}
			};
			server.setAuthenticator(xmlrpcAuthenticator);
		}
		catch (Exception ex)
		{
			logger.error("failed to start", ex);
			return false;
		}

		// Log Information
		logger.info("Voucher Simulator Started on {}", config.getSoapURL());

		return true;
	}

	@Override
	public void stop()
	{
		logger.info("Stopping Voucher Simulator on {}", config.getSoapURL());

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
		logger.info("Voucher Simulator Stopped on {}", config.getSoapURL());

	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		this.config = (VoucherSimConfiguration) config;
		stop();
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException exc)
		{

		}
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
		return server != null && endpoint != null && endpoint.isPublished();
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
	@Perms(perms = { @Perm(name = "ViewVoucherSimParameters", description = "View Voucher Simulator Parameters", category = "Voucher Simulator", supplier = true),
			@Perm(name = "ChangeVoucherSimParameters", implies = "ViewVoucherSimParameters", description = "Change Voucher Simulator Parameters", category = "Voucher Simulator", supplier = true) })
	public class VoucherSimConfiguration extends ConfigurationBase
	{
		private String operatorID = "c4u";
		private String password = "c4u";
		private int maxScheduledTasks = 10;
		private int soapPort = 10022;
		private String soapPath = "/RPC2";
		private int voucherPort = 10021;
		private String voucherPath = "/RPC2";
		private int threadQueueCapacity = 5;
		private int maxThreadPoolSize = 5;
		private int maxBacklog = 2;
		private boolean isMultiOperator = false;

		public String getOperatorID()
		{
			check(esb, "ViewVoucherSimParameters");
			return operatorID;
		}

		public void setOperatorID(String operatorID)
		{
			check(esb, "ChangeVoucherSimParameters");
			this.operatorID = operatorID;
		}

		@Config(description = "Password", renderAs = Rendering.PASSWORD)
		public String getPassword()
		{
			check(esb, "ViewVoucherSimParameters");
			return password;
		}

		public void setPassword(String password)
		{
			check(esb, "ChangeVoucherSimParameters");
			this.password = password;
		}

		public boolean getIsMultiOperator()
		{
			check(esb, "ViewVoucherSimParameters");
			return isMultiOperator;
		}

		public void setIsMultiOperator(boolean isMultiOperator)
		{
			check(esb, "ChangeVoucherSimParameters");
			this.isMultiOperator = isMultiOperator;
		}

		public int getVoucherPort()
		{
			check(esb, "ViewVoucherSimParameters");
			return voucherPort;
		}

		public void setVoucherPort(int voucherPort) throws ValidationException
		{
			check(esb, "ChangeVoucherSimParameters");

			ValidationException.port(voucherPort, "VoucherPort");
			this.voucherPort = voucherPort;
		}

		public String getVoucherPath()
		{
			check(esb, "ViewVoucherSimParameters");
			return voucherPath;
		}

		public void setVoucherPath(String voucherPath)
		{
			check(esb, "ChangeVoucherSimParameters");
			this.voucherPath = voucherPath;
		}

		public String getSoapPath()
		{
			check(esb, "ViewVoucherSimParameters");
			return soapPath;
		}

		public void setSoapPath(String soapPath)
		{
			check(esb, "ChangeVoucherSimParameters");
			this.soapPath = soapPath;
		}

		public String getSoapURL()
		{
			check(esb, "ViewVoucherSimParameters");
			return String.format("http://%s:%d%s", "localhost", soapPort, soapPath);
		}

		public int getSoapPort()
		{
			check(esb, "ViewVoucherSimParameters");
			return soapPort;
		}

		public void setSoapPort(int httpPort) throws ValidationException
		{
			check(esb, "ChangeVoucherSimParameters");

			ValidationException.port(httpPort, "SoapPort");
			this.soapPort = httpPort;
		}

		public int getThreadQueueCapacity()
		{
			check(esb, "ViewVoucherSimParameters");
			return threadQueueCapacity;
		}

		@SupplierOnly
		public void setThreadQueueCapacity(int threadQueueCapacity) throws ValidationException
		{
			check(esb, "ChangeVoucherSimParameters");
			ValidationException.inRange(1, threadQueueCapacity, 100, "ThreadQueueCapacity");
			this.threadQueueCapacity = threadQueueCapacity;
		}

		public int getMaxThreadPoolSize()
		{
			check(esb, "ViewVoucherSimParameters");
			return maxThreadPoolSize;
		}

		@SupplierOnly
		public void setMaxThreadPoolSize(int maxThreadPoolSize) throws ValidationException
		{
			check(esb, "ChangeVoucherSimParameters");
			ValidationException.inRange(5, maxThreadPoolSize, 1000, "MaxThreadPoolSize");
			this.maxThreadPoolSize = maxThreadPoolSize;
		}

		public int getMaxBacklog()
		{
			check(esb, "ViewVoucherSimParameters");
			return maxBacklog;
		}

		@SupplierOnly
		public void setMaxBacklog(int maxBacklog) throws ValidationException
		{
			check(esb, "ChangeVoucherSimParameters");
			ValidationException.inRange(5, maxBacklog, 100, "MaxBacklog");
			this.maxBacklog = maxBacklog;
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
			return -932372036854775808L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "Voucher Simulator";
		}

		@Override
		public void validate() throws ValidationException
		{

		}

	}

	VoucherSimConfiguration config = new VoucherSimConfiguration();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IVoucherSim
	//
	// /////////////////////////////////

	@Override
	public IVoucherSim getVoucherSimulator()
	{
		return voucherSimulator;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IValidationContext
	//
	// /////////////////////////////////
	@Override
	public boolean getIsMultiOperator()
	{
		return config.getIsMultiOperator();
	}

	@Override
	public String getOperatorID()
	{
		return config.getOperatorID();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	protected boolean checkCredentials(String username, String password)
	{
		return username != null && username.equals(config.getOperatorID()) && password != null && password.equals(config.getPassword());
	}
}
