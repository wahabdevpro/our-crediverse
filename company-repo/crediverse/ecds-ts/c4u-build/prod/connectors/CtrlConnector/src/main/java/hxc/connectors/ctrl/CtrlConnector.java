package hxc.connectors.ctrl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.Config;
import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IConnector;
import hxc.connectors.ctrl.protocol.ConfigNotificationRequest;
import hxc.connectors.ctrl.protocol.ConfigNotificationResponse;
import hxc.connectors.ctrl.protocol.DistributeFileRequest;
import hxc.connectors.ctrl.protocol.DistributeFileResponse;
import hxc.connectors.ctrl.protocol.ElectionRequest;
import hxc.connectors.ctrl.protocol.ElectionResponse;
import hxc.connectors.ctrl.protocol.ElectionResultRequest;
import hxc.connectors.ctrl.protocol.ElectionResultResponse;
import hxc.connectors.ctrl.protocol.FileServerRoleRequest;
import hxc.connectors.ctrl.protocol.FileServerRoleResponse;
import hxc.connectors.ctrl.protocol.PingRequest;
import hxc.connectors.ctrl.protocol.PingResponse;
import hxc.connectors.ctrl.protocol.PollServerRoleRequest;
import hxc.connectors.ctrl.protocol.PollServerRoleResponse;
import hxc.connectors.ctrl.protocol.ProcessedFileNotificationRequest;
import hxc.connectors.ctrl.protocol.ProcessedFileNotificationResponse;
import hxc.connectors.ctrl.protocol.ServerInfo;
import hxc.connectors.ctrl.protocol.ServerRequest;
import hxc.connectors.ctrl.protocol.ServerResponse;
import hxc.connectors.ctrl.protocol.ServerRole;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.file.FileRecord;
import hxc.connectors.file.FileType;
import hxc.connectors.file.IFileConnector;
import hxc.connectors.snmp.ISnmpConnector;
import hxc.connectors.snmp.IncidentSeverity;
import hxc.connectors.snmp.IndicationState;
import hxc.servicebus.HostInfo;
import hxc.servicebus.IPlugin;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.IShutdown;
import hxc.services.notification.INotifications;
import hxc.services.notification.Phrase;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.security.SupplierOnly;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.instrumentation.Metric;
import hxc.utils.instrumentation.ValueType;
import hxc.utils.thread.TimedThread;
import hxc.utils.thread.TimedThread.TimedThreadType;

public class CtrlConnector implements IConnector, ICtrlConnector
{
	final static Logger logger = LoggerFactory.getLogger(CtrlConnector.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////

	private IServiceBus esb;
	private TimedThread watchdogThread = null;
	private TimedThread listenerThread = null;
	private TimedThread healthCheckerThread = null;
	private IDatabase database;
	private ServerSocket serverSocket = null;
	private static int requestSequenceNo = 1;
	private static String hostName;
	private static int maxNodes = 1;

	private Map<String, ServerRole> serverRoles = new HashMap<String, ServerRole>();
	private Map<String, ServerInfo> servers = new HashMap<String, ServerInfo>();

	private Metric memoryUsage = Metric.CreateSimple("General Memory Usage", "MB", ValueType.InstantaneousCount, 60000);
	private Metric heapMemory = Metric.CreateGraph("Heap/NonHeap Memory Usage", 60000, "MB", "Heap Memory Used", "Non Heap Memory Used");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public CtrlConnector()
	{
	}

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
		// Get Database
		database = esb.getFirstConnector(IDatabase.class);
		if (database == null)
		{
			logger.error("Failed to get IDatabase from the \"Service Bus\"");
			return false;
		}

		// Get Host Name
		// hostName = HostInfo.getName();
		hostName = HostInfo.getName(esb);
		if (hostName == null)
		{
			String name = this.getClass().getName();
			logger.error(name + ": UnknownHostException");
			return false;
		}

		// Create the Listener Thread
		listenerThread = new TimedThread("Listener Worker Thread", 1000L, TimedThreadType.INTERVAL)
		{
			@Override
			public synchronized void start()
			{
				super.start();
				logger.trace("Started Listener Worker Thread of Control Connector");
			}

			@Override
			public void action()
			{
				try
				{
					if (serverSocket == null)
						serverSocket = new ServerSocket(config.serverPort);

					final Socket socket = serverSocket.accept();
					Thread serverThread = new Thread()
					{
						@Override
						public void run()
						{
							try
							{
								serverWorker(socket);
							}
							catch (Exception e)
							{
								logger.error(e.getMessage(), e);
							}
						}

					};
					serverThread.start();
				}
				catch (Exception e)
				{
					logger.error(e.getMessage(), e);
				}
			}

			@Override
			public synchronized void kill()
			{
				super.kill();
				logger.trace("Stopped Listener Worker Thread of Control Connector");
			}
		};
		listenerThread.start();

		// Create the Watchdog Thread
		watchdogThread = new TimedThread("Watchdog Worker Thread", config.watchdogIntervalSeconds * 1000L, TimedThreadType.INTERVAL)
		{
			@Override
			public synchronized void start()
			{
				super.start();
				logger.trace("Started Watchdog Worker Thread of Control Connector");
			}

			@Override
			public void action()
			{
				try
				{
					checkPeerServerRoles();
				}
				catch (Exception e)
				{
					logger.error(e.getMessage(), e);
				}
			}

			@Override
			public synchronized void kill()
			{
				super.kill();
				logger.trace("Stopped Watchdog Worker Thread of Control Connector");
			}
		};
		watchdogThread.start();

		// Create the HealthChecker Thread
		healthCheckerThread = new TimedThread("Health Checker Thread", config.healthCheckIntervalSeconds * 1000L, TimedThreadType.INTERVAL)
		{
			@Override
			public synchronized void start()
			{
				super.start();
				logger.trace("Started Health Checker Thread of Control Connector");
			}

			@Override
			public void action()
			{
				try
				{
					healthCheck();
				}
				catch (Exception e)
				{
					logger.error(e.getMessage(), e);
				}
			}

			@Override
			public synchronized void kill()
			{
				super.kill();
				logger.trace("Stopped Health Checker Thread of Control Connector");
			}
		};
		healthCheckerThread.start();

		TimedThread onceOffHealth = new TimedThread("Once Off Health Check", 30000L, TimedThreadType.EXECUTE_ONCE)
		{
			@Override
			public void action()
			{
				healthCheck();
			}
		};
		onceOffHealth.start();

		esb.registerShutdown(new IShutdown()
		{

			@Override
			public void shutdown()
			{
				// Relinquish Attached Resources
				relinquishAttachedResources();
			}

		});

		return true;
	}

	@Override
	public void stop()
	{
		listenerThread.kill();
		watchdogThread.kill();
		healthCheckerThread.kill();
		
		

		// Stop the Server Socket
		if (serverSocket != null && !serverSocket.isClosed())
		{
			try
			{
				logger.trace("Closing Server Socket for Control Service");
				serverSocket.close();
			}
			catch (IOException e)
			{
			}

			serverSocket = null;
		}
		
		try
		{
			listenerThread.join();
		}
		catch(Exception ex)
		{
			
		}
		
		try
		{
			watchdogThread.join();
		}
		catch(Exception ex)
		{
			
		}
		
		try
		{
			healthCheckerThread.join();
		}
		catch(Exception ex)
		{
			
		}

		// Log Information
		logger.info("Control Connector Stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		// Stop the Server
		// server.stop();
		if (logger != null)
			logger.info("Control Connector Stopped");

		this.config = (CtrlConfiguration) config;

		if (watchdogThread != null)
			watchdogThread.setWaitTime(this.config.watchdogIntervalSeconds * 1000L);

		if (healthCheckerThread != null)
			healthCheckerThread.setWaitTime(this.config.healthCheckIntervalSeconds * 1000L);

		// Log Information
		if (logger != null)
			logger.info("Control Connector Re-Started");

	}

	@Override
	public IConnection getConnection(String optionalConnectionString) throws IOException
	{
		return null;
	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return false;
	}

	@Override
	public boolean isFit()
	{
		if (watchdogThread == null || !watchdogThread.isAlive())
		{
			logger.error("Watch Dog Thread is dead. status: {}", (watchdogThread == null?"NULL":"NOT ALIVE"));
			return false;
		}
		if (listenerThread == null || !listenerThread.isAlive())
		{
			logger.error("Listener Thread is dead. status: {}", (watchdogThread == null?"NULL":"NOT ALIVE"));
			return false;
		}
		if (healthCheckerThread == null || !healthCheckerThread.isAlive())
		{
			logger.error("Health Checker Thread is dead. Thread status: {}", (watchdogThread == null?"NULL":"NOT ALIVE"));
			return false;
		}

		if (servers.size() > 0)
		{
			PingRequest request = new PingRequest();
			request.setSeq(99);
			PingResponse response = sendPeerRequest(hostName, request);
			if (response == null || response.getSeq() != 100)
			{
				logger.error("Ping hostname {} failed. Response status: {}", hostName, (response == null?"NULL":response.getSeq()));
				return false;
			}
		}
		return true;
	}

	@Override
	public IMetric[] getMetrics()
	{
		return new IMetric[] { memoryUsage, heapMemory };
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////

	@Perms(perms = { @Perm(name = "ViewCtrlParameters", description = "View Control Connector Parameters", category = "Control Connector", supplier = true),
			@Perm(name = "ChangeCtrlParameters", implies = "ViewCtrlParameters", description = "Change Control Connector Parameters", category = "Control Connector", supplier = true),
			@Perm(name = "ViewCtrlNotifications", description = "View Control Connector Notifications", category = "Control Connector", supplier = true),
			@Perm(name = "ViewCtrlParameters", implies = "ViewCtrlNotifications", description = "Change Control Connector Notifications", category = "Control Connector", supplier = true) })
	public class CtrlConfiguration extends ConfigurationBase
	{
		private int serverPort = 14200;
		private int fileTransferPort = 15251;
		private long watchdogIntervalSeconds = 60L;
		private long healthCheckIntervalSeconds = 60L;
		private long compulsoryHealthCheckIntervalSeconds = 300L;
		private long consecutiveTPSLimit = 0;
		private int connectTimeoutSeconds = 5;
		private int readTimeoutSeconds = 5;
		private String healthScript = "";
		private int alarmMemoryRatio = 5;
		private boolean enableGarbageCollection = false;
		
		@SupplierOnly
		@Config(description = "Manage Garbage Collection")
		public boolean getEnableGarbageCollection() {
			return enableGarbageCollection;
		}

		public void setEnableGarbageCollection(boolean manageGarbageCollection) {
			this.enableGarbageCollection = manageGarbageCollection;
		}

		public int getGarbageCollectionRatio() {
			return garbageCollectionRatio;
		}

		public void setGarbageCollectionRatio(int garbageCollectionRatio) {
			this.garbageCollectionRatio = garbageCollectionRatio;
		}

		private int garbageCollectionRatio = 40;

		public long getWatchdogIntervalSeconds()
		{
			check(esb, "ViewCtrlParameters");
			return watchdogIntervalSeconds;
		}

		public void setWatchdogIntervalSeconds(long watchdogInterval) throws ValidationException
		{
			check(esb, "ChangeCtrlParameters");

			ValidationException.min(1, watchdogInterval);
			this.watchdogIntervalSeconds = watchdogInterval;
		}

		public long getHealthCheckIntervalSeconds()
		{
			check(esb, "ViewCtrlParameters");
			return healthCheckIntervalSeconds;
		}

		public void setHealthCheckIntervalSeconds(long healthCheckIntervalSeconds) throws ValidationException
		{
			check(esb, "ChangeCtrlParameters");

			ValidationException.min(1, healthCheckIntervalSeconds);
			this.healthCheckIntervalSeconds = healthCheckIntervalSeconds;
		}

		public long getCompulsoryHealthCheckIntervalSeconds()
		{
			check(esb, "ViewCtrlParameters");
			return compulsoryHealthCheckIntervalSeconds;
		}

		public void setCompulsoryHealthCheckIntervalSeconds(long compulsoryHealthCheckIntervalSeconds) throws ValidationException
		{
			check(esb, "ChangeCtrlParameters");

			ValidationException.min(1, compulsoryHealthCheckIntervalSeconds);
			this.compulsoryHealthCheckIntervalSeconds = compulsoryHealthCheckIntervalSeconds;
		}

		public long getConsecutiveTPSLimit()
		{
			check(esb, "ViewCtrlParameters");
			return consecutiveTPSLimit;
		}

		public void setConsecutiveTPSLimit(long consecutiveTPSLimit) throws ValidationException
		{
			check(esb, "ChangeCtrlParameters");

			ValidationException.min(0, consecutiveTPSLimit);
			this.consecutiveTPSLimit = consecutiveTPSLimit;
		}

		@SupplierOnly
		public int getConnectTimeoutSeconds()
		{
			check(esb, "ViewCtrlParameters");
			return connectTimeoutSeconds;
		}

		@SupplierOnly
		public void setConnectTimeoutSeconds(int connectTimeoutSeconds) throws ValidationException
		{
			check(esb, "ChangeCtrlParameters");

			ValidationException.min(1, connectTimeoutSeconds);
			this.connectTimeoutSeconds = connectTimeoutSeconds;
		}

		@SupplierOnly
		public int getReadTimeoutSeconds()
		{
			check(esb, "ViewCtrlParameters");
			return readTimeoutSeconds;
		}

		@SupplierOnly
		public void setReadTimeoutSeconds(int readTimeoutSeconds) throws ValidationException
		{
			check(esb, "ChangeCtrlParameters");

			ValidationException.min(1, readTimeoutSeconds);
			this.readTimeoutSeconds = readTimeoutSeconds;
		}

		public int getServerPort()
		{
			check(esb, "ViewCtrlParameters");
			return serverPort;
		}

		public void setServerPort(int serverPort) throws ValidationException
		{
			check(esb, "ChangeCtrlParameters");

			ValidationException.port(serverPort);
			this.serverPort = serverPort;
		}

		public int getFileTransferPort()
		{
			check(esb, "ViewCtrlParameters");
			return fileTransferPort;
		}

		public void setFileTransferPort(int fileTransferPort) throws ValidationException
		{
			check(esb, "ChangeCtrlParameters");

			ValidationException.port(fileTransferPort);
			this.fileTransferPort = fileTransferPort;
		}

		@SupplierOnly
		public int getAlarmMemoryRatio()
		{
			check(esb, "ViewCtrlParameters");
			return alarmMemoryRatio;
		}

		@SupplierOnly
		public void setAlarmMemoryRatio(int alarmMemoryRatio) throws ValidationException
		{
			check(esb, "ChangeCtrlParameters");
			this.alarmMemoryRatio = alarmMemoryRatio;
		}

		public String getHealthScript()
		{
			check(esb, "ViewCtrlParameters");
			return healthScript;
		}

		public void setHealthScript(String healthScript) throws ValidationException
		{
			check(esb, "ChangeCtrlParameters");
			this.healthScript = healthScript;
		}
		
		@Override
		public String getPath(String languageCode)
		{
			return "Technical Settings";
		}

		@Override
		public long getSerialVersionUID()
		{
			return -1411687343402146265L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "Control Connector";
		}

		@Override
		public void validate() throws ValidationException
		{
			try
			{
				ValidationException.min(connectTimeoutSeconds, readTimeoutSeconds);
			}
			catch (ValidationException exc)
			{
				throw new ValidationException("%s:%s", "ReadTimeoutSeconds", exc.getMessage());
			}
		}

		@Override
		public boolean save(IDatabaseConnection database, ICtrlConnector control)
		{

			// Save Server Roles and Owners
			if (!saveServerRoles(database))
				return false;

			// Save Server List
			if (!saveServerList(database))
				return false;

			for (IServerInfo server : servers.values())
			{
				logger.trace("Loaded Server '{}' with Peer '{}'", server.getServerHost(), server.getPeerHost());
			}

			// This section overides settings (including role/server settings)
			if (!super.save(database, control))
				return false;

			return true;

		}

		@Override
		public boolean load(IDatabaseConnection databaseConnection)
		{
			if (!super.load(databaseConnection))
				return false;

			// Read Server Roles and Owners
			if (!readServerRoles(databaseConnection))
				return false;

			// Read Server List
			if (!readServerList(databaseConnection))
				return false;

			// Create Database Role
			ServerRole databaseRole = serverRoles.get(DATABASE_ROLE.toLowerCase());
			if (databaseRole == null)
			{
				try
				{
					databaseRole = new ServerRole();
					databaseRole.setServerRoleName(DATABASE_ROLE);
					databaseRole.setExclusive(true);
					serverRoles.put(DATABASE_ROLE.toLowerCase(), databaseRole);
					databaseConnection.upsert(databaseRole);
				}
				catch (SQLException | ValidationException e)
				{
					logger.error(e.getMessage(), e);
					return false;
				}

			}

			for (IServerRole role : serverRoles.values())
			{
				logger.trace("Loaded Server Role {} ({}) Attach: '{}' Detach: '{}'", role.getServerRoleName(), role.isExclusive() ? "Exclusive" : "Non-Exclusive", role.getAttachCommand(),
						role.getDetachCommand());
			}

			return true;
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

	};

	CtrlConfiguration config = new CtrlConfiguration();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Watchdog Thread
	//
	// /////////////////////////////////
	// private void watchdogWorker() throws InterruptedException
	// {
	//
	// while (!Thread.currentThread().isInterrupted() && stopSignal != null)
	// {
	// // Wait for next interval
	// synchronized (stopSignal)
	// {
	// stopSignal.wait(config.watchdogIntervalSeconds * 1000L);
	// }
	//
	// // Check Server Roles held by Peer
	// CheckPeerServerRoles();
	//
	// }
	//
	// }

	// Check Server Roles held by Peer
	private void checkPeerServerRoles()
	{
		logger.trace("Checking Peer Server Roles...");

		// Get ServerInfo
		IServerInfo serverInfo = servers.get(hostName);
		if (serverInfo == null)
		{
			logger.debug("Aborted Checking Peer Server Roles - Not local host info is available");
			return;
		}

		String peerHost = serverInfo.getPeerHost();
		if (peerHost == null || peerHost.length() == 0)
		{
			logger.debug("Aborted Checking Peer Server Roles - Not peer host info is available");
			return;
		}

		// Poll all Roles held by Peer
		for (IServerRole role : serverRoles.values().toArray(new ServerRole[0]))
		{
			if (!role.isExclusive())
				continue;

			if (peerHost.equalsIgnoreCase(role.getOwner()))
			{
				// Send a Poll Request
				logger.trace("Sending Poll Request for Role '{}' to '{}'", role.getServerRoleName(), peerHost);
				PollServerRoleRequest rolePoll = new PollServerRoleRequest();
				rolePoll.setServerRole(role.getServerRoleName());
				PollServerRoleResponse response = sendPeerRequest(peerHost, rolePoll);

				// Start an Election if it is not fit or doesn't respond
				if (response == null || !response.isFit())
				{
					logger.trace("Peer {} didn't responded to Poll Request for Role '{}' or is Unfit", peerHost, role.getServerRoleName());
					startElection(role);
				}
				else
				{
					logger.trace("Peer {} responded to Poll Request for Role '{}' and is Fit", peerHost, role.getServerRoleName());
				}
			}

			// Or re-elect if role held by non-existent server
			else if (!servers.containsKey(role.getOwner()))
			{
				logger.trace("Server Role {} doesn't have an owner. Starting and Election...", role.getServerRoleName());
				startElection(role);
			}

		}
	}

	// Start an Election for a particular role
	private boolean startElection(IServerRole role)
	{
		// Create Election request
		logger.trace("Starting an Election for Role '{}'", role.getServerRoleName());

		ElectionRequest request = new ElectionRequest();
		request.setRequestingHost(hostName);
		request.setServerRole(role.getServerRoleName());
		request.setHopsToLive(servers.size() * 2);

		return continueElection(request);
	}

	private boolean continueElection(ElectionRequest request)
	{
		// Try to send it to downstream peers
		IServerInfo serverInfo = servers.get(hostName);
		request.setHopsToLive(request.getHopsToLive() - 1);
		while (true)
		{
			String peerHost = serverInfo.getPeerHost().toLowerCase();
			ElectionResponse response = sendPeerRequest(peerHost, request);
			if (response != null)
			{
				logger.info("Sent Election Request for {} Role to {}", request.getServerRole(), peerHost);
				return true;
			}
			else
			{
				logger.error("Failed to send Election Request for {} Role to {}", request.getServerRole(), peerHost);
			}

			// Try next downstream peer
			serverInfo = servers.get(peerHost);
			if (hostName.equalsIgnoreCase(serverInfo.getServerHost()))
			{
				logger.error("Failed to send Election Request for {} Role to any Peer", request.getServerRole());
				return false;
			}

		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Listener Thread
	//
	// /////////////////////////////////
	// private void listenerWorker()
	// {
	// // Create the server Socket
	// try
	// {
	// serverSocket = new ServerSocket(config.serverPort);
	//
	// while (!Thread.currentThread().isInterrupted() && stopSignal != null)
	// {
	// final Socket socket = serverSocket.accept();
	// Thread serverThread = new Thread()
	// {
	// @Override
	// public void run()
	// {
	// try
	// {
	// serverWorker(socket);
	// }
	// catch (Exception e)
	// {
	// logger.error(e.getMessage(), e);
	// }
	// }
	//
	// };
	// serverThread.start();
	//
	// }
	// }
	// catch (IOException e1)
	// {
	// logger.log(this, e1);
	// }
	//
	// }

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Server Worker Thread
	//
	// /////////////////////////////////
	private void serverWorker(Socket socket)
	{
		try (InputStream inputStream = socket.getInputStream())
		{
			try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream))
			{
				ServerRequest request = (ServerRequest) objectInputStream.readObject();

				try (OutputStream outputStream = socket.getOutputStream())
				{
					try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream))
					{
						ServerResponse response = null;
						String simpleName = request.getClass().getSimpleName();
						switch (simpleName)
						{
							case "PollServerRoleRequest":
								logger.trace("Received a Poll Server Request from {}", request.getSourceHost());
								response = processPollServerRoleRequest((PollServerRoleRequest) request);
								break;

							case "ElectionRequest":
								logger.trace("Received an Election Request from {}", request.getSourceHost());
								response = processElectionRequest((ElectionRequest) request);
								break;

							case "ElectionResultRequest":
								logger.trace("Received an Election Result Request from {}", request.getSourceHost());
								response = processElectionResultRequest((ElectionResultRequest) request);
								break;

							case "ConfigNotificationRequest":
								logger.trace("Received a Config Notification Request from {}", request.getSourceHost());
								response = processConfigNotificationRequest((ConfigNotificationRequest) request);
								break;

							// case "DistributeFileNotificationRequest":
							// logger.trace("Received a Distribute File Notification Request from {}", request.getSourceHost());
							// response = distributeFileNotification((DistributeFileNotificationRequest) request);
							// break;

							case "DistributeFileRequest":
								logger.trace("Recieved a Distribute File Request from {}", request.getSourceHost());
								response = distributeFile((DistributeFileRequest) request);
								break;

							case "ProcessedFileNotificationRequest":
								logger.trace("Recieved a Processed File Notification Request from {}", request.getSourceHost());
								response = processedFileNotification((ProcessedFileNotificationRequest) request);
								break;

							case "FileServerRoleRequest":
								logger.trace("Recieved a File Server Role Request from {}", request.getSourceHost());
								response = fileServerRoleCheck((FileServerRoleRequest) request);
								break;

							case "PingRequest":
								response = new PingResponse((PingRequest) request);
								break;

							default:
								logger.error("Invalid server Request '{}'", simpleName);
								break;
						}

						if (response != null)
							objectOutputStream.writeObject(response);
					}
					catch (IOException e)
					{
						logger.error(e.getMessage(), e);
						return;
					}

				}
				catch (IOException e)
				{
					logger.error(e.getMessage(), e);
					return;
				}
			}
			catch (IOException | ClassNotFoundException e)
			{
				logger.error(e.getMessage(), e);
				return;
			}

		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
			return;
		}
		finally
		{
			try
			{
				if (!socket.isClosed())
					socket.close();
			}
			catch (IOException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

	}

	// Process Poll Server Role Requests
	private PollServerRoleResponse processPollServerRoleRequest(PollServerRoleRequest request)
	{
		logger.trace("Assessing Fitness for Server Role {}...", request.getServerRole());
		PollServerRoleResponse response = new PollServerRoleResponse(request);

		IPlugin plugin = esb.getCandidate(request.getServerRole());
		logger.trace("Detected Server Role {} {} fit", request.getServerRole(), plugin != null ? "is" : "isn't");

		response.setFit(plugin != null);

		return response;
	}

	// Process Election Request
	private ElectionResponse processElectionRequest(final ElectionRequest request)
	{
		// Conduct the Election
		Thread electionThread = new Thread()
		{
			@Override
			public void run()
			{
				conductElection(request);
			}
		};
		electionThread.run();

		ElectionResponse response = new ElectionResponse(request);

		return response;
	}

	// Conduct a collection
	private void conductElection(ElectionRequest request)
	{
		// Nominate Self
		IPlugin plugin = esb.getCandidate(request.getServerRole());
		boolean isFit = plugin != null;
		logger.trace("Considering nominating self for {} Server Role... - {} fit", request.getServerRole(), isFit ? "Is" : "Isn't");
		if (isFit)
		{
			int rating = 1;
			if (isIncumbent(request.getServerRole()))
				rating *= 2;
			logger.trace("Considering nominating self for {} Server Role... - Rating = {}, Incumnent = {}", request.getServerRole(), rating, request.getCandidateRating());
			if (request.getCandidateHost() == null || rating > request.getCandidateRating())
			{
				logger.info("Nominating self for {} Server Role with Rating of {}", request.getServerRole(), rating);
				request.setCandidateRating(rating);
				request.setCandidateHost(hostName);
			}
		}

		// Stop if the Election came full circle
		if (hostName.equalsIgnoreCase(request.getRequestingHost()) || request.getHopsToLive() <= 0)
		{
			logger.info("Election for {} Server Role has completed - The Winner is {}", request.getServerRole(), request.getCandidateHost());
			ElectionResultRequest result = new ElectionResultRequest();
			result.setRequestingHost(hostName);
			result.setElectedHost(request.getCandidateHost());
			result.setServerRole(request.getServerRole());
			result.setHopsToLive(servers.size() * 2);
			logger.trace("Staring Announcing Election Result for {} Server Role ({})", request.getServerRole(), request.getCandidateHost());
			announceElectionResult(result);
		}
		else
		{
			logger.info("Election for {} Server Role is continuing", request.getServerRole());
			continueElection(request);
		}

	}

	private void announceElectionResult(ElectionResultRequest request)
	{
		// Try to send it to downstream peers
		IServerInfo serverInfo = servers.get(hostName);
		request.setHopsToLive(request.getHopsToLive() - 1);
		while (true)
		{
			String peerHost = serverInfo.getPeerHost().toLowerCase();
			logger.trace("Sending Election Results for {} Role to {}", request.getServerRole(), peerHost);
			ElectionResultResponse response = sendPeerRequest(peerHost, request);
			if (response != null)
			{
				logger.info("Sent Election Results for {} Role to {}", request.getServerRole(), peerHost);
				break;
			}
			else
			{
				logger.error("Failed to send Election Results for {} Role to {}", request.getServerRole(), peerHost);
			}

			// Try next downstream peer
			serverInfo = servers.get(peerHost);
			if (hostName.equalsIgnoreCase(serverInfo.getServerHost()))
			{
				logger.error("Failed to send Election Results for {} Role to any Peer", request.getServerRole());
				break;
			}

		}

	}

	// Process Election Result Request
	private ElectionResultResponse processElectionResultRequest(final ElectionResultRequest request)
	{
		// Process Result asynchronously
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				// Continue to Announce
				boolean announcementCompleted = hostName.equalsIgnoreCase(request.getRequestingHost()) || request.getHopsToLive() < 0;
				if (!announcementCompleted)
				{
					logger.trace("Continuing Announcing Election Result for {} Server Role ({})", request.getServerRole(), request.getElectedHost());
					announceElectionResult(request);
				}
				else
				{
					logger.trace("Announcement of Election Result for {} Server Role ({}) completed", request.getServerRole(), request.getElectedHost());
				}

				// Update local Copy of Server Role
				ServerRole serverRole = serverRoles.get(request.getServerRole().toLowerCase());
				String oldOwner = serverRole.getOwner();
				String newOwner = request.getElectedHost();
				logger.trace("Server Role {} - New owner {} (was {})", request.getServerRole(), newOwner, oldOwner);
				if (newOwner != null && !newOwner.equalsIgnoreCase(oldOwner))
				{
					try
					{
						logger.info("Replacing {} with {} as owner of {} server role", oldOwner, newOwner, serverRole.getServerRoleName());
						serverRole.setOwner(newOwner);

						// Detach Role
						String detachCommand = serverRole.getDetachCommand();
						if (hostName.equalsIgnoreCase(oldOwner) && detachCommand != null && detachCommand.length() > 0)
						{
							logger.trace("Executing Detach Command {}", detachCommand);
							ExecuteCommand(detachCommand, oldOwner);
						}

						// Attach Role
						String attachCommand = serverRole.getAttachCommand();
						if (hostName.equalsIgnoreCase(newOwner) && attachCommand != null && attachCommand.length() > 0)
						{
							logger.trace("Executing Attach Command {}", attachCommand);
							ExecuteCommand(attachCommand, newOwner);
						}

						// Save to database
						logger.trace("Saving new owner of {} server role to database", serverRole.getServerRoleName());
						try (IDatabaseConnection connection = database.getConnection(null))
						{
							connection.upsert(serverRole);
						}
						catch (Exception e)
						{
							logger.error(e.getMessage(), e);
						}

						// Send Alarm
						String alarm;
						if (oldOwner == null)
							alarm = String.format("Server role '%s' assigned to %s.", request.getServerRole(), newOwner);
						else
							alarm = String.format("Server role '%s' failed over from %s to %s.", request.getServerRole(), oldOwner, newOwner);
						ISnmpConnector snmp = esb.getFirstConnector(ISnmpConnector.class);
						if (snmp == null)
							logger.trace(alarm);
						else if (oldOwner == null)
							snmp.elementManagementStatus(request.getServerRole(), IndicationState.MANAGED, IncidentSeverity.CLEAR, alarm);
						else
							snmp.elementManagementStatus(request.getServerRole(), IndicationState.FAILED_OVER, IncidentSeverity.MINOR, alarm);

					}
					catch (Exception e)
					{
						logger.error(e.getMessage(), e);
					}
				}

			}

		};
		thread.run();

		// Return Response
		ElectionResultResponse response = new ElectionResultResponse(request);
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Health Thread
	//
	// /////////////////////////////////

	private int healthCounter = (int) (config.compulsoryHealthCheckIntervalSeconds / config.healthCheckIntervalSeconds * 100);
	private boolean consecutiveTPSLimitActive = false;
	private List<IPlugin> recentHealthChanges = new ArrayList<>();
	private List<String> recentRoleHealthChanges = new ArrayList<>();

	private void healthCheck()
	{
		checkHealthOS();
		checkHealthComponenets();
		checkHealthScript();
	}

	private void checkHealthComponenets()
	{
		if (healthCounter >= config.compulsoryHealthCheckIntervalSeconds / config.healthCheckIntervalSeconds)
		{
			fitnessCheck(null, null);
			healthCounter = -1;
			limitCheck(true);
		}
		else
		{
			fitnessCheck(recentHealthChanges, recentRoleHealthChanges);
			limitCheck(false);
		}
		healthCounter++;
	}

	private void fitnessCheck(List<IPlugin> changes, List<String> roleChanges)
	{
		ISnmpConnector snmp = esb.getFirstConnector(ISnmpConnector.class);
		if (snmp == null)
		{
			if (healthCounter >= config.compulsoryHealthCheckIntervalSeconds / config.healthCheckIntervalSeconds)
				logger.error("Snmp Connector is not found. Cannot perform the Health Check.");
			return;
		}

		logger.info("Checking Health for the connectors and services.");

		List<IPlugin> plugins = esb.getStartedPlugins();
		for (IPlugin plugin : plugins)
		{
			if (plugin == null)
				continue;

			String name = plugin.getClass().getSimpleName();
			boolean fit;
			try
			{
				fit = plugin.isFit();
			}
			catch (Exception exc)
			{
				logger.error("Could not check fitness for {},  reason: {}", name, exc.getMessage());
				continue;
			}

			if (!fit && ((changes != null) ? !changes.contains(plugin) : true))
			{
				if (plugin.getConfiguration() != null)
					name = plugin.getConfiguration().getName(Phrase.ENG);

				snmp.elementServiceStatus(name, IndicationState.OUT_OF_SERVICE, IncidentSeverity.MAJOR, "Unfit");

				if (changes != null)
					changes.add(plugin);
			}

			if (fit && ((changes != null) ? changes.contains(plugin) : true))
			{
				if (plugin.getConfiguration() != null)
					name = plugin.getConfiguration().getName(Phrase.ENG);

				snmp.elementServiceStatus(name, IndicationState.IN_SERVICE, IncidentSeverity.CLEAR, "Fit");

				if (changes != null)
					changes.remove(plugin);
			}
		}

		if (plugins.size() > 0)
		{
			logger.info("Checking Roles are assigned to a plugin.");
	
			for (String role : serverRoles.keySet())
			{
				IPlugin plugin = esb.getCandidate(role);
				if (plugin == null && ((roleChanges != null) ? !roleChanges.contains(role) : true))
				{
					snmp.elementManagementStatus(role, IndicationState.IMPAIRED, IncidentSeverity.MINOR, "No plugin can assume the role.");
	
					if (roleChanges != null)
						roleChanges.add(role);
				}
	
				if (plugin != null && ((roleChanges != null) ? roleChanges.contains(role) : true))
				{
					snmp.elementManagementStatus(role, IndicationState.MANAGED, IncidentSeverity.CLEAR, "Role has an owner.");
	
					if (roleChanges != null)
						roleChanges.remove(role);
				}
			}
		}
	}

	public void limitCheck(boolean ignoreLimit)
	{
		if ((ignoreLimit || !consecutiveTPSLimitActive) && esb.getConsecutiveLimiting() > config.consecutiveTPSLimit)
		{
			consecutiveTPSLimitActive = true;
			ISnmpConnector snmp = esb.getFirstConnector(ISnmpConnector.class);
			if (snmp == null)
				return;

			snmp.thresholdStatus("TPS", IndicationState.SOFT_LIMIT_BREACHED, IncidentSeverity.MINOR, "Limit has been reached.");
		}
		else if ((ignoreLimit || consecutiveTPSLimitActive) && esb.getConsecutiveLimiting() <= config.consecutiveTPSLimit)
		{
			consecutiveTPSLimitActive = false;
			ISnmpConnector snmp = esb.getFirstConnector(ISnmpConnector.class);
			if (snmp == null)
				return;

			snmp.thresholdStatus("TPS", IndicationState.WITHIN_LIMITS, IncidentSeverity.CLEAR, "Within limits.");
		}
	}

	private boolean limitedSpace = false;
	private boolean memoryLow = false;
	private boolean diskSpaceLow = false;

	private void checkHealthOS()
	{
		Runtime runtime = Runtime.getRuntime();
		float memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / 1048576;
		memoryUsage.report(esb, memoryUsed);
		float maxMemory = ((float) runtime.maxMemory() / 1048576);
		float currentAllocMemory = ((float) runtime.totalMemory() / 1048576);
		float usedMemoryRatio = ((memoryUsed / maxMemory) * 100);
		float availMemoryRatio = 100 - usedMemoryRatio; 
		logger.info("Memory Used: [{}]MB; Memory Currently Allocated to JVM: [{}]MB; Total Memory: [{}]MB; Used Memory Ratio: [%.2f]%%", memoryUsed, currentAllocMemory, maxMemory, usedMemoryRatio);
		if (availMemoryRatio < config.alarmMemoryRatio)
		{
			memoryLow = true;
			logger.warn(String.format("Memory is running low. Available Memory vs Max Memory ratio is [%.2f]%%. Max memory: [%.2f]MB; Used Memory [%.2f]MB", availMemoryRatio, maxMemory, memoryUsed));
			ISnmpConnector snmp = esb.getFirstConnector(ISnmpConnector.class);
			if (snmp != null)
			{
				snmp.elementManagementStatus("Memory Usage", IndicationState.IMPAIRED, IncidentSeverity.MAJOR, "Memory low on " + hostName + ". Memory used: " + memoryUsed + " MB");
			}
		}
		else
		{
			if (memoryLow || healthCounter >= config.compulsoryHealthCheckIntervalSeconds / config.healthCheckIntervalSeconds)
			{
				memoryLow = false;
				logger.info("Memory usage is normal.");
				ISnmpConnector snmp = esb.getFirstConnector(ISnmpConnector.class);
				if (snmp != null)
				{
					snmp.elementManagementStatus("Memory Usage", IndicationState.MANAGED, IncidentSeverity.CLEAR, "Memory usage is normal on " + hostName + ". Memory used: " + memoryUsed + " MB");
				}
			}
		}

		MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		MemoryUsage non = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
		heapMemory.report(esb, heap.getUsed() / 1048576, non.getUsed() / 1048576);
		logger.trace("Heap Memory Usage: {}/{} MB", heap.getUsed() / 1048576, heap.getMax() / 1048576);
		logger.trace("Non Heap Memory Usage: {}/{} MB", non.getUsed() / 1048576, non.getMax() / 1048576);
		
		if (config.getEnableGarbageCollection())
		{

			long ratio = (long) (((float) heap.getUsed() / heap.getMax()) * 100);
			if (ratio > config.getAlarmMemoryRatio())
			{
				logger.trace("Calling Garbage Collector. Used Heap Memory too High.");
				try
				{
					System.gc();
				}
				catch (Exception exc)
				{
					logger.error("Garbage Collector failed: {}", exc.getMessage());
				}
			}
		}

		File roots[] = File.listRoots();
		for (File root : roots)
		{
			long usable = root.getUsableSpace();
			limitedSpace = (((float) usable / (float) root.getTotalSpace()) * 100) < 10;
			if (limitedSpace && healthCounter >= config.compulsoryHealthCheckIntervalSeconds / config.healthCheckIntervalSeconds)
			{
				diskSpaceLow = true;
				logger.warn("Storage space limited ({} MB free space left) on {}. Please free up some space.", usable / 1048576, root.getName().length() > 0 ? root.getName() : hostName);
				ISnmpConnector snmp = esb.getFirstConnector(ISnmpConnector.class);
				if (snmp != null)
				{
					snmp.elementManagementStatus("Storage Disk", IndicationState.IMPAIRED, IncidentSeverity.MAJOR,
							"Storage space limited (" + usable / 1048576 + " MB free space left) on " + (root.getName().length() > 0 ? root.getName() : hostName));
				}
			}
			else
			{
				if (diskSpaceLow || healthCounter >= config.compulsoryHealthCheckIntervalSeconds / config.healthCheckIntervalSeconds)
				{
					diskSpaceLow = false;
					logger.info("Storage space normal ({} MB free space left) on {}.", usable / 1048576, root.getName().length() > 0 ? root.getName() : hostName);
					ISnmpConnector snmp = esb.getFirstConnector(ISnmpConnector.class);
					if (snmp != null)
					{
						snmp.elementManagementStatus("Storage Disk", IndicationState.MANAGED, IncidentSeverity.CLEAR,
								"Storage space normal (" + usable / 1048576 + " MB free space left) on " + (root.getName().length() > 0 ? root.getName() : hostName));
					}
				}
			}
		}
	}

	private void checkHealthScript()
	{
		if (healthCounter < config.compulsoryHealthCheckIntervalSeconds / config.healthCheckIntervalSeconds)
			return;

		String script = config.healthScript;
		if (script == null || script.trim().length() == 0)
		{
			ISnmpConnector snmp = esb.getFirstConnector(ISnmpConnector.class);
			if (snmp != null)
			{
				snmp.taskExecutionStatus("Health Script", IndicationState.STARTED, IncidentSeverity.CLEAR, "Passed");
			}
			return;
		}

		Process proc;
		int exitValue;
		try
		{
			proc = Runtime.getRuntime().exec(String.format(script, servers.get(hostName) != null ? servers.get(hostName).getPeerHost() : "", isIncumbent(DATABASE_ROLE) ? "incumbent" : ""));
			proc.waitFor();

			exitValue = proc.exitValue();
		}
		catch (Exception exc)
		{
			logger.error("Failed to execute health script: {}", exc.getMessage());
			return;
		}

		if (exitValue != 0)
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			try
			{
				String line = null;
				while ((line = reader.readLine()) != null)
				{
					if (line.toLowerCase().startsWith("error") || line.toLowerCase().startsWith("fatal"))
					{
						logger.error("Health Script: {}", line);
						ISnmpConnector snmp = esb.getFirstConnector(ISnmpConnector.class);
						if (snmp != null)
						{
							snmp.taskExecutionStatus("Health Script", IndicationState.FAILED, IncidentSeverity.MAJOR, line);
						}
					}
					else
					{
						if (logger.isInfoEnabled())
						logger.info("Health Script: {}", line);
					}
				}
			}
			catch (Exception exc)
			{
				logger.error("Error reading input stream from health script. {}", exc.getMessage());
			}
		}
		else
		{

			ISnmpConnector snmp = esb.getFirstConnector(ISnmpConnector.class);
			if (snmp != null)
			{
				snmp.taskExecutionStatus("Health Script", IndicationState.STARTED, IncidentSeverity.CLEAR, "Passed");
			}

		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ICtrlConnector Implementation
	//
	// /////////////////////////////////

	// Test if host is Incumbent custodian of the Server Role
	@Override
	public boolean isIncumbent(String serverRole)
	{
		logger.trace("isIncumbent: serverRole = '{}'", serverRole);
		if (serverRole == null || serverRole.length() == 0)
			return true;

		IServerRole role = serverRoles.get(serverRole.toLowerCase());
		logger.trace("isIncumbent: role = '{}'", role);
		if (role == null)
			return false;

		logger.trace("isIncumbent: role.isExclusive() = '{}'", role.isExclusive());
		if (!role.isExclusive())
			return true;

		logger.trace("isIncumbent: hostName = '{}', role.getOwner() = '{}'", hostName, role.getOwner());
		return hostName.equalsIgnoreCase(role.getOwner());
	}

	@Override
	public String getThisTransactionNumberPrefix()
	{
		// Defensive
		if (hostName == null || servers == null)
			return "00";

		ServerInfo server = servers.get(hostName);

		return server == null ? "00" : server.getTransactionNumberPrefix();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ICtrlConnector Implementation
	//
	// /////////////////////////////////
	@Override
	public IServerInfo[] getServerList()
	{
		logger.trace("CtrlConnector.getServerList ...");
		return servers.values().toArray(new IServerInfo[0]);
	}

	@Override
	public void setServerList(IServerInfo[] servers) throws ValidationException
	{
		logger.trace("CtrlConnector.setServerList ...");
		String key;

		if (servers == null || servers.length == 0)
			throw new ValidationException("Invalid Arguments");

		if (servers.length > maxNodes)
			throw new ValidationException("Max Nodes Reached. Only a Max of %s Nodes Allowed", maxNodes);

		Map<String, ServerInfo> map = new HashMap<String, ServerInfo>();
		for (IServerInfo server : servers)
		{
			ServerInfo serverInfo = new ServerInfo();
			serverInfo.setServerHost(server.getServerHost());
			serverInfo.setPeerHost(server.getPeerHost());
			serverInfo.setTransactionNumberPrefix(server.getTransactionNumberPrefix());
			key = serverInfo.getServerHost().toLowerCase();
			if (map.containsKey(key))
				throw new ValidationException("Duplicate Server Name '%s'", key);
			map.put(key, serverInfo);
		}

		// Test if servers form a single loop
		Map<String, ServerInfo> map2 = new HashMap<String, ServerInfo>();
		String firstKey = key = servers[0].getServerHost().toLowerCase();
		while (map2.size() < servers.length)
		{
			if (map2.containsKey(key))
				throw new ValidationException("Server Chain at %s not Circular", key);

			ServerInfo server = map.get(key);
			map2.put(key, server);

			key = server.getPeerHost().toLowerCase();
			if (!map.containsKey(key))
				throw new ValidationException("Peer Host '%s' not configured under 'Server' (possible non-circular path in Server cluster configuration)", key);
		}
		if (!key.equalsIgnoreCase(firstKey))
			throw new ValidationException("Server Chain not Circular", key);
		this.servers = map;
	}

	@Override
	public IServerRole[] getServerRoleList()
	{
		logger.trace("CtrlConnector.getServerRoleList ...");
		return serverRoles.values().toArray(new IServerRole[0]);
	}

	@Override
	public void setServerRoleList(IServerRole[] serverRoles) throws ValidationException
	{
		logger.trace("CtrlConnector.setServerRoleList: entry");
		if (serverRoles == null || serverRoles.length == 0)
			throw new ValidationException("Invalid Arguments");
		Map<String, ServerRole> map = new HashMap<String, ServerRole>();
		for (IServerRole serverRole : serverRoles)
		{
			logger.trace("CtrlConnector.setServerRoleList: name = {}, exclusive = {}, attachCommand = {}, detachCommand = {}, owner = {}", serverRole.getServerRoleName(),
					serverRole.isExclusive(), serverRole.getAttachCommand(), serverRole.getDetachCommand(), serverRole.getOwner());
			ServerRole role = new ServerRole();
			role.setServerRoleName(serverRole.getServerRoleName());
			role.setExclusive(serverRole.isExclusive());
			role.setAttachCommand(serverRole.getAttachCommand());
			role.setDetachCommand(serverRole.getDetachCommand());
			String key = serverRole.getServerRoleName().toLowerCase();
			if (map.containsKey(key))
				throw new ValidationException("Duplicate Server Role Name '%s'", key);
			map.put(key, role);
		}

		this.serverRoles = map;
		logger.trace("CtrlConnector.setServerRoleList: end");
	}

	@Override
	public void moveRole(String serverRole, String hostServer) throws ValidationException
	{
		// TODO Auto-generated method stub
		// ??
	}

	@Override
	public void setMaxNodes(int maxNodes)
	{
		CtrlConnector.maxNodes = maxNodes;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Config Change Notification
	//
	// /////////////////////////////////
	@Override
	public void notifyConfigChange(long configSerialVersionUID)
	{
		// Create Election request
		logger.trace("Starting Configuration Change Notification for {}", configSerialVersionUID);

		ConfigNotificationRequest request = new ConfigNotificationRequest();
		request.setRequestingHost(hostName);
		request.setConfigSerialVersionUID(configSerialVersionUID);
		request.setHopsToLive(servers.size() * 2);

		continueConfigNotification(request);

	}

	private boolean continueConfigNotification(ConfigNotificationRequest request)
	{
		// Try to send it to downstream peers
		IServerInfo serverInfo = servers.get(hostName);
		if (serverInfo == null)
			return true;

		request.setHopsToLive(request.getHopsToLive() - 1);
		while (true)
		{
			String peerHost = serverInfo.getPeerHost().toLowerCase();
			ConfigNotificationResponse response = sendPeerRequest(peerHost, request);
			if (response != null)
			{
				logger.info("Sent Configuration Change Notification for {} to {}", request.getConfigSerialVersionUID(), peerHost);
				return true;
			}
			else
			{
				logger.error("Failed to send Configuration Change Notification for {} to {}", request.getConfigSerialVersionUID(), peerHost);
			}

			// Try next downstream peer
			serverInfo = servers.get(peerHost);
			if (hostName.equalsIgnoreCase(serverInfo.getServerHost()))
			{
				logger.error("Failed to send Configuration Change Notification for {} to any peer", request.getConfigSerialVersionUID());
				return false;
			}

		}

	}

	private ConfigNotificationResponse processConfigNotificationRequest(final ConfigNotificationRequest request)
	{
		// Process Result asynchronously
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				// Continue to Notify
				boolean notificationCompleted = hostName.equalsIgnoreCase(request.getRequestingHost()) || request.getHopsToLive() < 0;
				if (!notificationCompleted)
				{
					logger.trace("Continuing Config Notification for {}", request.getConfigSerialVersionUID());
					continueConfigNotification(request);
				}
				else
				{
					logger.trace("Config Notification for {} completed", request.getConfigSerialVersionUID());
				}

				// Re-Read Config
				List<IPlugin> plugins = esb.getRegisteredPlugins();
				for (IPlugin plugin : plugins)
				{
					if (processConfigNotificationRequest(plugin, request))
						break;
				}

			}

		};
		thread.run();

		// Return Response
		ConfigNotificationResponse response = new ConfigNotificationResponse(request);
		return response;

	}

	private boolean processConfigNotificationRequest(IPlugin plugin, ConfigNotificationRequest request)
	{
		// Get the Plugin's config
		IConfiguration config = plugin.getConfiguration();
		if (config == null)
			return false;

		// Update Parent
		return processConfigNotificationRequest(config, request);
	}

	private boolean processConfigNotificationRequest(IConfiguration config, ConfigNotificationRequest request)
	{
		// Same Serial Version UID
		if (config.getSerialVersionUID() == request.getConfigSerialVersionUID())
		{
			logger.info("Reloading {} config after receiving config change notification for plugin: {} from requesting host: {}", config.getName(Phrase.ENG), request.getConfigSerialVersionUID(), request.getRequestingHost());

			boolean succeeded = false;
			try (IDatabaseConnection databaseConnection = database.getConnection(null))
			{
				succeeded = config.load(databaseConnection);
			}
			catch (Exception e)
			{
				succeeded = false;
			}

			if (!succeeded)
				logger.error("Failed to reload {} config after receiving notification for {}", config.getName(Phrase.ENG), request.getConfigSerialVersionUID());

			return true;
		}

		// Else try child Configs
		Collection<IConfiguration> children = config.getConfigurations();
		if (children == null)
			return false;

		for (IConfiguration childConfig : children)
		{
			if (processConfigNotificationRequest(childConfig, request))
				return true;
		}

		return false;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// File Handling Notifications
	//
	// /////////////////////////////////

	@Override
	public boolean distributeFile(final String directory, final String filename)
	{
		IServerInfo info = servers.get(hostName);
		if (info == null || info.getPeerHost().equalsIgnoreCase(hostName))
			return true;

		int availablePort = config.fileTransferPort;
		for (int i = availablePort; i < 65000; i++)
		{
			try (ServerSocket s = new ServerSocket(i))
			{
				availablePort = i;
				break;
			}
			catch (Exception e)
			{
				continue;
			}
		}

		final int port = availablePort;
		Thread fileTransferServer = new Thread()
		{

			@Override
			public void run()
			{
				File transfer = new File(String.format("%s/%s", directory, filename));
				if (transfer.length() > Integer.MAX_VALUE)
					return; // File to large to send

				try (ServerSocket server = new ServerSocket(port))
				{
					server.setSoTimeout(config.connectTimeoutSeconds);
					try (Socket sock = server.accept())
					{
						byte bytearr[] = new byte[(int) transfer.length()];
						try (BufferedInputStream bin = new BufferedInputStream(new FileInputStream(transfer)))
						{
							bin.read(bytearr, 0, bytearr.length);
							try (OutputStream output = sock.getOutputStream())
							{
								output.write(bytearr, 0, bytearr.length);
								output.flush();
								logger.trace("{} has been transfered.", filename);
							}
						}
					}
				}
				catch (Exception e)
				{
					logger.error("File transfer server failed: {}", e.toString());
				}
			}

		};
		fileTransferServer.start();

		// Create request object
		DistributeFileRequest request = new DistributeFileRequest();

		// Set the fields of the object
		request.setDirectory(directory);
		request.setFilename(filename);
		request.setRequestingHost(hostName);
		request.setPort(port);
		request.setHopsToLive(servers.size() * 2);

		return continueDistributingFile(request);
	}

	private boolean continueDistributingFile(DistributeFileRequest request)
	{

		IServerInfo info = servers.get(hostName);

		request.setHopsToLive(request.getHopsToLive() - 1);
		while (true)
		{

			String peerHost = info.getPeerHost();

			DistributeFileResponse response = sendPeerRequest(peerHost, request);

			if (response != null)
			{
				logger.debug("{} has been distributed to {}.", request.getFilename(), peerHost);
				return true;
			}
			else
			{
				logger.error("Failed to send {} to {}.", request.getFilename(), peerHost);
			}

			info = servers.get(peerHost);
			if (hostName.equalsIgnoreCase(info.getServerHost()))
			{
				logger.error("Failed to send {} to any peer host.", request.getFilename());
				return false;
			}
		}
	}

	private DistributeFileResponse distributeFile(DistributeFileRequest request)
	{

		// Check to see whether the request object made its way back to the creater of the object
		boolean distributed = hostName.equalsIgnoreCase(request.getRequestingHost()) || request.getHopsToLive() == 0;
		if (!distributed)
		{

			// Create destination directory if it doesn't exist
			File directory = new File(request.getDirectory());
			if (!directory.exists())
			{
				directory.mkdirs();
			}

			// Create a client socket for file transfer
			try (Socket socket = new Socket(request.getRequestingHost(), request.getPort()))
			{
				try (InputStream in = socket.getInputStream())
				{
					String filename = String.format("%s/%s", request.getDirectory(), request.getFilename());
					try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, false)))
					{
						int c;
						while ((c = in.read()) != -1)
							writer.append((char) c);
						writer.flush();
					}
				}
			}
			catch (IOException e)
			{
				logger.error("Failed to transfer {}: {}", request.getFilename(), e.toString());
			}

			continueDistributingFile(request);

		}

		DistributeFileResponse response = new DistributeFileResponse(request);
		return response;
	}

	@Override
	public boolean notifyFileProcessed(File file, String outputDir)
	{

		// Create the processed file request
		ProcessedFileNotificationRequest request = new ProcessedFileNotificationRequest();

		// Set the request
		request.setFile(file);
		request.setOutputDir(outputDir);
		request.setRequestingHost(hostName);
		request.setHopsToLive(servers.size() * 2);

		return continueNotifyingProcessed(request);
	}

	private boolean continueNotifyingProcessed(ProcessedFileNotificationRequest request)
	{

		// Check to see if there is more than one server
		IServerInfo info = servers.get(hostName);
		if (info == null)
		{
			// If not, then write to database that file has been processed
			try (IDatabaseConnection connection = database.getConnection(null))
			{
				FileRecord record = connection.select(FileRecord.class, "where filename = %s", request.getFilename());
				if (record != null)
				{
					record.completed = true;
					connection.update(record);
				}
			}
			catch (Exception exc)
			{
				logger.error("Failed to update the database for {} being completed", request.getFilename());
			}
			return true;
		}

		// Decrement the number of times the request needs to be sent
		request.setHopsToLive(request.getHopsToLive() - 1);
		while (true)
		{

			String peerHost = info.getPeerHost().toLowerCase();

			// Send processed notification request
			ProcessedFileNotificationResponse response = sendPeerRequest(peerHost, request);

			// Validate that the response is not null
			if (response != null)
			{
				logger.info("Sent Processed File Notification for {} to {}", request.getFilename(), peerHost);
				return true;
			}
			else
			{
				logger.error("Failed to send Processed File Notification for {} to {}", request.getFilename(), peerHost);
			}

			info = servers.get(peerHost);
			if (hostName.equalsIgnoreCase(info.getServerHost()))
			{
				logger.error("Failed to notify {} completed to any of the peers", request.getFilename());
				return false;
			}
		}
	}

	private ProcessedFileNotificationResponse processedFileNotification(final ProcessedFileNotificationRequest request)
	{

		Thread thread = new Thread()
		{

			@Override
			public void run()
			{
				// Check that the notification has been sent to all servers
				boolean processed = hostName.equalsIgnoreCase(request.getRequestingHost()) || request.getHopsToLive() == 0;
				if (processed)
				{
					// If so, then update the record
					logger.trace("Completed Sending Processed File Notification of {} file to every peer", request.getFilename());
					try (IDatabaseConnection connection = database.getConnection(null))
					{
						FileRecord record = connection.select(FileRecord.class, "where filename = %s", request.getFilename());
						if (record != null)
						{
							record.completed = true;
							connection.update(record);
						}
					}
					catch (Exception exc)
					{
						logger.error("Failed to update the database for {} being processed", request.getFilename());
					}
				}
				else
				{
					// Else, move the file to the output directory
					File file = request.getFile();
					if (file.exists())
					{
						File outputDir = new File(request.getOutputDir());
						if (!outputDir.exists())
						{
							outputDir.mkdirs();
						}

						file.renameTo(new File(request.getOutputDir() + "/" + file.getName()));
					}
					logger.trace("Continuing to send Processed File Notification for {} to peers", request.getFilename());
					continueNotifyingProcessed(request);
				}
			}

		};
		thread.run();

		ProcessedFileNotificationResponse response = new ProcessedFileNotificationResponse(request);
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// File Fail Over Mechanism
	//
	// /////////////////////////////////

	@Override
	public boolean checkFileServerRoleHasIncumbent(String inputDirectory, FileType fileType)
	{

		// Create request
		FileServerRoleRequest request = new FileServerRoleRequest();

		// Set the request
		request.setInputDirectory(inputDirectory);
		request.setFileType(fileType);
		request.setHasIncumbent(false);
		request.setHopsToLive(servers.size() * 2);
		request.setRequestingHost(hostName);

		return checkPeerFileServerRoles(request);
	}

	private boolean checkPeerFileServerRoles(FileServerRoleRequest request)
	{

		// Check to see if there are more than two servers
		IServerInfo info = servers.get(hostName);
		if (info == null)
		{
			// If not, then make that server the incumbent for that particular filetype
			IFileConnector file = esb.getFirstConnector(IFileConnector.class);
			if (file != null)
			{
				file.setIncumbent(request.getInputDirectory(), request.getFileType(), hostName);
			}
			return true;
		}

		// Decrement the number of times it needs to be sent
		request.setHopsToLive(request.getHopsToLive() - 1);
		while (true)
		{

			String peerHost = info.getPeerHost().toLowerCase();

			// Send the request to peerHost
			FileServerRoleResponse response = sendPeerRequest(peerHost, request);

			if (response != null)
			{
				logger.info("Sent File Server Role Request to {}", peerHost);
				return true;
			}
			else
			{
				logger.error("Failed to send File Server Role Request to {}", peerHost);
			}

			info = servers.get(peerHost);
			if (hostName.equalsIgnoreCase(info.getServerHost()))
			{
				logger.error("Failed to send File Server Role Request to any of the peers");
				return false;
			}
		}
	}

	private FileServerRoleResponse fileServerRoleCheck(final FileServerRoleRequest request)
	{

		Thread thread = new Thread()
		{

			@Override
			public void run()
			{
				// Check if the request has been sent to all the servers
				boolean sent = hostName.equalsIgnoreCase(request.getRequestingHost()) || request.getHopsToLive() == 0;
				if (sent)
				{
					logger.trace("Completed Sending File Server Role Request to every peer");
					// Check to see if one of the servers is an incumbent of that particular filetype
					if (!request.hasIncumbent())
					{
						// If not, then set this server as the incumbent
						IFileConnector file = esb.getFirstConnector(IFileConnector.class);
						if (file != null)
						{
							file.setIncumbent(request.getInputDirectory(), request.getFileType(), hostName);
						}

					}
				}
				else
				{
					// If not, check to see whether this server is the incumbent of the filetype
					IFileConnector file = esb.getFirstConnector(IFileConnector.class);
					if (file != null && !request.hasIncumbent())
					{
						boolean hasIncumbent = file.hasIncumbent(request.getInputDirectory(), request.getFileType());
						request.setHasIncumbent(hasIncumbent);
					}

					checkPeerFileServerRoles(request);
				}
			}

		};
		thread.run();

		FileServerRoleResponse response = new FileServerRoleResponse(request);
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	// Execute an Attach/Detach command
	private boolean ExecuteCommand(String command, String owner)
	{
		if (command == null || command.length() == 0)
			return false;

		try
		{
			Process process = Runtime.getRuntime().exec(command);
			int returnCode = process.waitFor();
			if (returnCode == 0)
			{
				logger.info("Successfully Executed '{}'", command);
				return true;
			}
			else
			{
				logger.error("Execution of '{}' returned '{}'", command, returnCode);
				return false;
			}
		}
		catch (IOException | InterruptedException e)
		{
			logger.error("Execution of '{}' failed: '{}'", command, e.getMessage());
			return false;
		}

	}

	// Send Request to a Peer
	@SuppressWarnings("unchecked")
	private <Tres extends ServerResponse, Treq extends ServerRequest> Tres sendPeerRequest(String peerHost, Treq req)
	{
		req.setSequenceNo(requestSequenceNo++);
		req.setSourceHost(hostName);
		req.setTargetHost(peerHost);

		try (Socket socket = new Socket())
		{
			socket.setSoTimeout(config.readTimeoutSeconds * 1000);
			socket.connect(new InetSocketAddress(peerHost, config.serverPort), config.connectTimeoutSeconds * 1000);

			try (OutputStream outputStream = socket.getOutputStream())
			{
				try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream))
				{
					objectOutputStream.writeObject(req);
					objectOutputStream.flush();

					try (InputStream inputStream = socket.getInputStream())
					{
						try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream))
						{
							return (Tres) objectInputStream.readObject();
						}
						catch (ClassNotFoundException | IOException e)
						{
							logger.error("SendPeerRequest reading peer feedback (peer:{} port:{}) Msg:{} Trace:{ {} }", peerHost, config.serverPort, e.getMessage(),
									e.getStackTrace()[0]);
						}

					}
					catch (IOException e)
					{
						logger.error("SendPeerRequest accessing peer input stream(peer:{} port:{}) Msg:{} Trace:{ {} }", peerHost, config.serverPort, e.getMessage(),
								e.getStackTrace()[0]);
					}

				}
				catch (IOException e)
				{
					logger.error("SendPeerRequest writing to peer (peer:{} port:{}) Msg:{} Trace:{ {} }", peerHost, config.serverPort, e.getMessage(), e.getStackTrace()[0]);
				}

			}
			catch (IOException e)
			{
				logger.error("SendPeerRequest accessing output stream (peer:{} port:{}) Msg:{} Trace:{ {} }", peerHost, config.serverPort, e.getMessage(),
						e.getStackTrace()[0]);
			}
		}
		catch (IOException e)
		{
			logger.error("SendPeerRequest connecting to peer (peer:{} port:{}) Msg:{} Trace:{ {} }", peerHost, config.serverPort, e.getMessage(), e.getStackTrace()[0]);
		}

		return null;
	}

	// Read Server Roles and Owners
	private boolean readServerRoles(IDatabaseConnection connection)
	{
		try
		{
			// Retrieve Server Roles from Database
			List<ServerRole> roles = connection.selectList(ServerRole.class, "");
			serverRoles.clear();
			for (ServerRole role : roles)
			{
				serverRoles.put(role.getServerRoleName().toLowerCase(), role);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

	// Read Server List
	private boolean readServerList(IDatabaseConnection connection)
	{
		try
		{
			// List of Peers
			List<ServerInfo> serverList = connection.selectList(ServerInfo.class, "");
			servers.clear();
			for (ServerInfo server : serverList)
			{
				servers.put(server.getServerHost().toLowerCase(), server);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

	// Save Server Roles and Owners
	private boolean saveServerRoles(IDatabaseConnection connection)
	{
		try
		{
			// Save List to Database
			logger.trace("Saving Server Roles to Database");
			connection.delete(ServerRole.class, "");
			List<ServerRole> roles = new ArrayList<ServerRole>(serverRoles.values());
			for (IServerRole role : roles)
			{
				connection.upsert(role);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

	// Save Server List
	private boolean saveServerList(IDatabaseConnection connection)
	{
		try
		{
			// Save List of Peers
			logger.trace("Saving Server Info to Database");
			connection.delete(ServerInfo.class, "");
			List<ServerInfo> serverList = new ArrayList<ServerInfo>(servers.values());
			for (ServerInfo server : serverList)
			{
				connection.upsert(server);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

	// Relinquish Attached Resources
	private void relinquishAttachedResources()
	{
		for (ServerRole role : serverRoles.values())
		{
			if (hostName.equalsIgnoreCase(role.getOwner()))
			{
				ExecuteCommand(role.getDetachCommand(), hostName);
			}
		}

	}

}
