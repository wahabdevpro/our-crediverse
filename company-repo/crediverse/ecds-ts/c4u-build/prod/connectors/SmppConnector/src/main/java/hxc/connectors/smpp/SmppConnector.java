package hxc.connectors.smpp;

import java.io.IOException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.net.URL;
import java.net.MalformedURLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudhopper.commons.charset.Charset;
import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.commons.util.windowing.DuplicateKeyException;
import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.BaseBindResp;
import com.cloudhopper.smpp.pdu.BindReceiver;
import com.cloudhopper.smpp.pdu.BindTransceiver;
import com.cloudhopper.smpp.pdu.BindTransmitter;
import com.cloudhopper.smpp.pdu.DataSm;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.pdu.EnquireLinkResp;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.tlv.Tlv;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.SmppBindException;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppInvalidArgumentException;

import hxc.configuration.Config;
import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IConnector;
import hxc.connectors.IInteraction;
import hxc.connectors.ctrl.ICtrlConnector;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.smpp.client.SmppClient;
import hxc.connectors.smpp.session.SmppSession;
import hxc.connectors.smpp.session.SmppSessionHandler;
import hxc.connectors.sms.ISmsConnector;
import hxc.connectors.sms.ISmsHistory;
import hxc.connectors.sms.ISmsResponse;
import hxc.servicebus.EncodingScheme;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.INotificationText;
import hxc.services.notification.INotifications;
import hxc.services.notification.Phrase;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.instrumentation.Metric;
import hxc.utils.notification.Notifications;
import hxc.utils.thread.TimedThread;
import hxc.utils.thread.TimedThread.TimedThreadType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SmppConnector implements IConnector, ISmsConnector
{
	private final static Logger logger = LoggerFactory.getLogger(SmppConnector.class);
	public static final int HOURS_24 = 1000 * 60 * 60 * 24;
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private ICtrlConnector control;
	private SmppConnector me = this;
	private SmsqConnector smsq;
	private String smsqSMSC;

	private static int sequence = 0;
	private ScheduledThreadPoolExecutor monitor;

	private SmppClient client;
	private ConcurrentMap<Long, SmppSession> sessions = new ConcurrentHashMap<>();
	private Map<String, RoundRobinList<SMSCConfig>> uniqueSMSCs;
	private BlockingDeque<SMSCConfig> failedSessions = new LinkedBlockingDeque<>();
	private ConcurrentLinkedDeque<SmppFailedMessage> failedMessageCache = new ConcurrentLinkedDeque<SmppFailedMessage>();

	private TimedThread keepAliveThread;
	private TimedThread metricsThread;
	private TimedThread retryFailedSmsThread;

	private static final int MAX_HISTORY = 10;
	private static SmsHistory history[] = new SmsHistory[MAX_HISTORY];

	private static final int MAX_SEND_ATTEMPTS = 5; //Unused
	private static final int MAX_RETRY_TPS = 50; 

	private static int currentIndex = 0;
	private final ReentrantReadWriteLock connectorLock = new ReentrantReadWriteLock();
	//ReadLock for where sessions are used to send Smpp messages
	private final ReadLock connectorReadLock = connectorLock.readLock();
	//WriteLock for where sessions are changed, unbound, disconnected, reconnected
	private final WriteLock connectorWriteLock = connectorLock.writeLock();

	private Thread smppSessionsReconfigure = null;
	private final Object reconfigureLock = new Object();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Metric Data
	//
	// /////////////////////////////////
	private Metric requestCounterMetric = Metric.CreateGraph("SMPP Request Counter", 10000, "Requests", "Enquire Link Requests", "Submit Sm Requests", "Deliver Sm Requests", "Data Sm Requests");
	private Metric responseCounterMetric = Metric.CreateGraph("SMPP Response Counter", 10000, "Responses", "Enquire Link Responses", "Submit Sm Responses", "Deliver Sm Responses",
			"Data Sm Responses");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IConnector implementation
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
		logger.trace("SmppConnector.start: entry ...");

		// Get Control Connector
		control = esb.getFirstConnector(ICtrlConnector.class);
		if (control == null)
		{
			logger.error("Failed to get ICtrlConnector from the \"Service Bus\"");
			return false;
		}

		// Create a new Thread Pool Executor
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

		// Create a Scheduled Thread Pool Executor for SMPP
		monitor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, new ThreadFactory()
		{

			private int nextNum = 0;

			@Override
			public Thread newThread(Runnable runnable)
			{
				// Creates a thread with a specific name
				Thread thread = new Thread(runnable);
				thread.setName("C4USmppClientSession-" + ++nextNum);
				return thread;
			}

		});

		// Initialise the client
		client = new SmppClient(executor, 4, monitor);

		logger.trace("Smpp Connector Started.");

		// Create the SMPP sessions if node is Incumbent
		if (config.getRequireDatabaseRole() == false || control.isIncumbent(ICtrlConnector.DATABASE_ROLE))
		{
			logger.trace("SmppConnector.start: createSessions ...");
			createSessions(getSMSCConfigurations());
			logger.trace("SmppConnector.start: createSessions completed");
		}
		else
		{
			logger.info("SMPP connector not bound to SMS-C - Not incumbent Database Role");
		}

		keepAliveThread = new TimedThread("Smpp Keep Alive Thread", config.keepAliveIntervalMilliseconds, TimedThreadType.INTERVAL)
		{
			@Override
			public void action()
			{
				logger.info("SMPP KeepAlive: starting ...");
				logger.info("SMPP KeepAlive: Sessions size: {}. There are {} failed sessions.", sessions.size(), failedSessions.size());
				try {
					doKeepAlive();
				} catch (Throwable tr) {
					logger.error("SMPP KeepAlive: █████ Error: ", tr);
				}
				logger.info("SMPP KeepAlive: done.");
			}
		};
		// Start the keep alive thread
		keepAliveThread.start();

		retryFailedSmsThread = new TimedThread("Smpp Retry Failed SMS Thread", 
		config.retryFailedSmsIntervalMilliseconds, TimedThreadType.INTERVAL)
		{
			@Override
			public void action()
			{
				logger.debug("SMPP RetryFailedSMS: starting ...");
				try {
					resendFailedMessages();
				} catch (Throwable tr) {
					logger.error("SMPP RetryFailedSMS: Error: ", tr);
				}
				logger.debug("SMPP RetryFailedSMS: done.");
			}
		};
		// Start the retry failed SMS thread

		retryFailedSmsThread.start();

		// Create thread for SMPP metric
		metricsThread = new TimedThread("Smpp Metric Thread", config.keepAliveIntervalMilliseconds * 2L, TimedThreadType.INTERVAL)
		{
			@Override
			public void action()
			{
				logger.info("Smpp Metric Thread: Executing...");
				// Keep counter of the various requests and responses
				int totalEnquireRequests = 0;
				int totalSubmitRequests = 0;
				int totalDeliverRequests = 0;
				int totalDataRequests = 0;
				int totalEnquireResponses = 0;
				int totalSubmitResponses = 0;
				int totalDeliverResponses = 0;
				int totalDataResponses = 0;

				try
				{
					// Iterate through the sessions
					for (SmppSession session : sessions.values())
					{
						// Ensure the session is valid
						if (session == null)
							continue;
						// Increment the various request counters
						totalEnquireRequests += session.getTxEnquireLink();
						totalSubmitRequests += session.getTxSubmitSm();
						totalDeliverRequests += session.getTxDeliverSm();
						totalDataRequests += session.getTxDataSm();

						// Increment the various response counters
						totalEnquireResponses += session.getRxEnquireLink();
						totalSubmitResponses += session.getRxSubmitSm();
						totalDeliverResponses += session.getRxDeliverSm();
						totalDataResponses += session.getRxDataSm();
					}

					// Report the results
					requestCounterMetric.report(esb, totalEnquireRequests, totalSubmitRequests, totalDeliverRequests, totalDataRequests);
					responseCounterMetric.report(esb, totalEnquireResponses, totalSubmitResponses, totalDeliverResponses, totalDataResponses);
				}
				catch (Exception exception)
				{
					logger.error("Exception in metric processing: {}", exception);
					logger.error(exception.getMessage(), exception);
				} 
			}
		};

		// Start the metric thread
		metricsThread.start();

		logger.trace("SmppConnector.start: end: ...");
		return true;
	}

	private synchronized void doKeepAlive() {
		Instant beforeLock = null;
		Instant afterLock = null;
		Duration lockDiff = null;
		HashSet<SMSCConfig> recreateSet = new HashSet<SMSCConfig>();
		HashMap<Long, SmppSession> rebindSet = new HashMap<Long, SmppSession>();
		// If this node is the Incumbent then we check for valid sessions and try to correct them
		if (config.getRequireDatabaseRole() == false || control.isIncumbent(ICtrlConnector.DATABASE_ROLE))
		{
			StringBuilder logDetail = new StringBuilder();
			// Iterate through the sessions to check for any failed sessions 
			// that are not in the failedSessions list
			
			for (long key : sessions.keySet())
			{
				// Get SMSC
				SMSCConfig smsc = getSMSCConfiguration(key);

				// Ensure the binding is not NONE
				if (smsc.getSmscBinding() == SmsCBinding.NONE)
				{
					logDetail.append("[skipping smsc; binding: none; " + smsc.getName(Phrase.ENG) + "] " );
					continue;
				}
				// Get session
				SmppSession session = sessions.get(key);

				// Check session is valid
				if (session == null || session.isClosed() || !session.isBound())
				{
					try
					{
						connectorWriteLock.lock();
						logger.info("SMPP KeepAlive: Updating failed sessions list.");
						sessions.remove(smsc.getSerialVersionUID());
						if(!failedSessions.contains(smsc) && !isFailedSession(key))						
							failedSessions.push(smsc);
					}
					finally{
						connectorWriteLock.unlock();
					}
				}
			}
			

			// Iterate through any failed sessions
			Instant failedSessionsStart = Instant.now();
			logDetail = new StringBuilder();
			
			if (!failedSessions.isEmpty()) {
				logger.info("SMPP KeepAlive: Reconnecting failed sessions.");
			}

			for (SMSCConfig smscConfig : failedSessions) {
				logger.info("SMPP KeepAlive: failed session to be reconnected: {}", smscConfig.getName(Phrase.ENG) );
			}

			try {
				for (SMSCConfig smsc : failedSessions)
				{
					//SMSCConfig smsc = failedSessions.pop();
					logger.debug("SMPP KeepAlive: Reconnecting failed session for: {}", smsc.getName(Phrase.ENG));
					SmppSession session = createSession(smsc);
					// Add the session to the map
					if(session != null)
					{	
						try{				
							connectorWriteLock.lock();
							failedSessions.remove(smsc);
							sessions.put(smsc.getSerialVersionUID(), session);
						}
						finally{
							connectorWriteLock.unlock();
						}

					} else {
						logger.error("SMPP KeepAlive: Could not recreate connection to SMSC {}, host {}, port {}, system ID {} ", smsc.getName(Phrase.ENG), smsc.getSmscUrl(), smsc.getPort(), smsc.getSystemID());
					}
				}
			} catch (RuntimeException e) {
				// try-catch is used only for logging purposes.
				logger.error("SMPP KeepAlive: Error while iterating failedSessions", e);
				throw e;
			}

			logger.info("SMPP KeepAlive: Current failed sessions size {} after recreate connection attempts. Sessions size: {}",
						failedSessions.size(),
						sessions.size());

			Instant failedSessionsEnd = Instant.now();
			Duration diff = Duration.between(failedSessionsStart, failedSessionsEnd);
			logger.info("SMPP KeepAlive: Recreated failed sessions [{}] {}ms", logDetail.toString(), diff.toMillis());
			Instant enquireSessionsStart = Instant.now();
			logDetail = new StringBuilder();
			// Iterate through the sessions
			for (long key : sessions.keySet())
			{
				// Get SMSC
				SMSCConfig smsc = getSMSCConfiguration(key);
				// Ensure the binding is not NONE
				if (smsc.getSmscBinding() == SmsCBinding.NONE)
				{
					logger.trace("SMPP KeepAlive: {} binding is {} - skiping", smsc.getName(Phrase.ENG), smsc.getSmscBinding());
					logDetail.append("[skipping smsc; binding: none; " + smsc.getName(Phrase.ENG) + "] ");
					continue;
				}
				// Get session
				SmppSession session = sessions.get(key);	
				if(session != null)
				{					
					try
					{
						// Enquire link to keep the connection alive
						logDetail.append("[enquireLink: smsc:" + smsc.getName(Phrase.ENG) + ", session" + session.getName() + "] ");
						enquireLink(session);
					}
					catch (Throwable throwable)
					{
						logger.error("SMPP KeepAlive: {} Got exception when doing enquire link during keepalive - will unbind: {}", smsc.getName(Phrase.ENG), throwable);
						unbind(session);
						try{				
							connectorWriteLock.lock();
							if(session == null || sessions.containsValue(session))
							{
								sessions.remove(smsc.getSerialVersionUID());
								if(!failedSessions.contains(smsc) && !isFailedSession(key))
									failedSessions.push(smsc);
							}
								
						}
						finally{
							connectorWriteLock.unlock();
						}
					}
					// Cancel all expired window requests
					session.getSendWindow().cancelAllExpired();
				}
			}
			Instant enquireSessionsEnd = Instant.now();
			diff = Duration.between(enquireSessionsStart, enquireSessionsEnd);
			logger.info("SMPP KeepAlive: Enquire Link on sessions [{}] {}ms", logDetail.toString(), diff.toMillis());

		}
		//Apparently this code is dead...
		// Else check if there are sessions that haven't been closed or are still bound
		else if (sessions.size() > 0)
		{
			logger.warn("SMPP KeepAlive: debug: dead code is being executed.");
			// Iterate through the remaining sessions
			for (SmppSession session : sessions.values())
			{
				// Check if session is valid
				if (session == null)
				{
					logger.debug("SMPP KeepAlive: got null session while trying to do cleanup ... ignoring.");
					continue;
				}
				// Check if session is still bound
				if (session.isBound())
				{
					logger.debug("SMPP KeepAlive: Unbinding session {} for other node.", session.getName());
					try
					{
						// Unbind the session
						unbind(session);
					}
					catch (Throwable throwable)
					{
						logger.error("SMPP KeepAlive: Got exception when unbind while doing keepalive", throwable);
						continue;
					}
				}

				// Destroy the session
				session.destroy();
			}
		}
	}

	@Override
	public void stop()
	{
		logger.info("SmppConnector.stop: entry: ...");
		
		// Unbind all the sessions
		for (SmppSession session : sessions.values())
		{
			// Ensure session is valid
			if (session == null)
				continue;

			// Unbind the session
			unbind(session);
		}

		// Clean up the SMPP Components
		client.destroy();
		monitor.shutdownNow();
		client = null;

		// Stop the threads
		logger.info("SMPP KeepAlive: Killing keepAliveThread...");
		keepAliveThread.kill();
		logger.info("SMPP KeepAlive: keepAliveThread killed.");
		metricsThread.kill();
		retryFailedSmsThread.kill();

		logger.info("Smpp Connector Stopped.");
		
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	public SmppConfiguration createConfiguration()
	{
		return new SmppConfiguration();
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		try {
			logger.trace("SmppConnector.setConfiguration: entry ...");
			this.config = (SmppConfiguration) config;

			// Change the timed threads waiting time
			if (keepAliveThread != null)
				keepAliveThread.setWaitTime(this.config.keepAliveIntervalMilliseconds);

			if (metricsThread != null)
				metricsThread.setWaitTime(this.config.keepAliveIntervalMilliseconds);

			if (retryFailedSmsThread != null)
				retryFailedSmsThread.setWaitTime(this.config.retryFailedSmsIntervalMilliseconds);

			// Try prevent a trillion new threads from potentially being spawned by repeatedly calling setConfiguration.
			synchronized(reconfigureLock)
			{
				smppSessionsReconfigure = new Thread("Smpp Sessions Setup")
				{
					@Override
					public void run()
					{
						long threadId = Thread.currentThread().getId();
						logger.trace("SmppConnector.setConfiguration: starting thread {} to createSessions ...", threadId);
						// FIXME this does not check control.isIncumbent(ICtrlConnector.DATABASE_ROLE) ... while start does ... why !?
						// 10/10 will be inconsistent again ...
						// Create the sessions again
						logger.trace("SmppConnector.setConfiguration: thread {} createSessions ...", threadId);
						createSessions(getSMSCConfigurations());
						logger.trace("SmppConnector.setConfiguration: thread {} sessions recreated.", threadId);
					};

				};
				smppSessionsReconfigure.start();
			}
		} catch (Exception e) {
			logger.trace("SmppConnector.setConfiguration: exception", e);
		}
	}

	@Override
	public IConnection getConnection(String optionalConnectionString) throws IOException
	{
		return new SmppConnection(this);
	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return false;
	}

	@Override
	public boolean isFit()
	{
		logger.trace("SmppConnector.isFit: Starting...");
		// Check whether it is Incumbent or not
		if (config.getRequireDatabaseRole() == false || control.isIncumbent(ICtrlConnector.DATABASE_ROLE))
		{
			int prospectiveSessionCount = countProspectiveSessions(getSMSCConfigurations());
			int sessionCount = 0;
			int fitSessionCount = 0;
			
			for (Long key: sessions.keySet())
			{
				SmppSession session = sessions.get(key);
				SMSCConfig smsc = getSMSCConfiguration(key);
				// Check if session is valid
				if (session == null)
				{
					continue;
				}
				sessionCount++;
				// Keep reference to fitness
				boolean isFit = true;
				String status = "Fit";

				try
				{
					// Check if session is closed
					if (session.isClosed())
					{
						status = "Closed";
						isFit = false;
					}

					// Check if session is bound
					if (!session.isBound())
					{
						status = "Unbound";
						isFit = false;
					}

					// Enquire link
					if (!enquireLink(session))
					{
						status = "Unable to Enquire Link";
						unbind(session);
						isFit = false;
					}
					if(isFit == false)
					{
						try{
							connectorWriteLock.lock();
							if(session == null || (!session.getStale() && sessions.containsValue(session)))
							{
								sessions.remove(smsc.getSerialVersionUID());
								if( !failedSessions.contains(smsc) 
								&& !isFailedSession(key) && !session.getStale())
									failedSessions.push(smsc);
							}
						}
						finally{
							connectorWriteLock.unlock();
						}
					}
				}
				catch (Throwable throwable)
				{
					logger.error("Got exception while checking fitness with enquirelink", throwable);
					status = throwable.toString();
					isFit = false;
				}
				finally
				{
					// Log the status
					logger.trace("{} is Currently {} (isFit={})", session.getName(), status, isFit);
					if ( isFit ){
						fitSessionCount++;
					} else {
						// if we ever find a session is not fit, ensure we unbind it properly
						unbind(session);
						//add to failed sessions if not already added
						try{
							connectorWriteLock.lock();
							if(session == null || (!session.getStale() && sessions.containsValue(session)))
							{
								sessions.remove(smsc.getSerialVersionUID());
								if(!isFailedSession(key) && !failedSessions.contains(smsc) && !session.getStale()) {
									failedSessions.push(smsc);
								}
							}
						}
						finally{
							connectorWriteLock.unlock();
						}
					}
				}
			}
			logger.info("prospectiveSessionCount = {}, sessionCount = {}, fitSessionCount = {}, failedSessionCount = {}", prospectiveSessionCount, sessionCount, fitSessionCount, failedSessions.size());

//			if (fitSessionCount == 0) {
//				try {
//					logger.info("All SMPP sessions are unfit. Trying to reinitialize SMPP configuration.");
//					setConfiguration(config);    // Reinitialize
//					logger.info("SMPP configuration successfully reinitialized.");
//				} catch (ValidationException e) {
//					logger.error("Error during SMPP configuration reinitialization.", e);
//				}
//			}

			 //resendFailedMessages();

			return ( prospectiveSessionCount == 0 || fitSessionCount > 0 );
		}
		return true;
	}

	private void resendFailedMessages() {
			// Send any messages that have failed
			// FIXME - this is very unsafe
			int messageCacheSize = failedMessageCache.size();
			if (messageCacheSize > 0)
			{
				logger.debug("Processing {} failed messages out of {}", messageCacheSize>MAX_RETRY_TPS? MAX_RETRY_TPS:messageCacheSize, messageCacheSize);
				Date now = new Date();

				// Iterate through the message caches
				for (int i = 0; i < messageCacheSize && i < MAX_RETRY_TPS; i++)
				{
					// Get a valid message
					SmppFailedMessage message = failedMessageCache.pop();
					if (message == null)
					{
						logger.warn("message on messageCache was null ... skipping");
						continue;
					}
					// Calculate the amount of time since the failure
					long startTime = message.getDateFailed().getTime();
					long endTime = now.getTime();
					long timeDiff = endTime - startTime;
					long days = timeDiff / HOURS_24;

					// If message is more than a day old, remove it
					if (days == 0)
					{
						sendSubmitSmRequest(message.getMessage(), false, null);
					}
				}
				logger.debug("Processing done for failed messages - remaining {}", failedMessageCache.size());
			}
			else
			{
				logger.debug("No failed messages to process");
			}
	}

	@Override
	public IMetric[] getMetrics()
	{
		return new IMetric[] { requestCounterMetric, responseCounterMetric };
	}


	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// SmsqConnector
	//
	// /////////////////////////////////

	private class SmsqConnector
	{
		private String host;
		private int port;
		private String username;
		private String password;
		private String database;
		private String table;
		private String version;
		private long seqNo = 0;
		private int dcsEncoding = -1;
		private int ttl = 300;
		private int priority = 0;

		private int dpExpLen = 0;
		private int dpDropLen = 0;
		private String dpAddPrefix = null;

		private Connection connection = null;

		SmsqConnector( String fullUrl )
		{
			if( !fullUrl.startsWith("smsq:") )
			{
				logger.error("SMSQ invalid URL [{}] must start with [smsq:]", fullUrl);
				return;
			}

			try
			{
				// Pathetic Java cannot parse URLs with protocols not known to it ...
				String smsqUrl = fullUrl;
				smsqUrl = smsqUrl.replaceFirst("^smsq:", "http:");

				URL url = new URL(smsqUrl);
				String query = url.getQuery();

				// Pathetic Java URL class doesn't support native query parameter parsing, so here we are ...
				Map<String, String> parameters = new HashMap<>();
			    if( query != null ) {
					String[] pairs = query.split("&");
			        for(String pair : pairs) {
			            int idx = pair.indexOf("=");
			            parameters.put(pair.substring(0, idx), pair.substring(idx + 1));
			        }
			    }

				version = parameters.get("version");
				database = parameters.get("database");
				table = parameters.get("table");
				host = url.getHost();
				port = url.getPort();
				if( port <= 0 ) port = 3306;
				String userInfo = url.getUserInfo();
				String[] credentials = userInfo.split(":");
				username = credentials[0];
				password = credentials[1];

				dcsEncoding = Integer.parseInt(parameters.getOrDefault("dcs", String.valueOf(dcsEncoding)));
				ttl = Integer.parseInt(parameters.getOrDefault("ttl", String.valueOf(ttl)));
				priority = Integer.parseInt(parameters.getOrDefault("priority", String.valueOf(priority)));
				
				dpExpLen = Integer.parseInt(parameters.getOrDefault("dpexp", String.valueOf(dpExpLen)));
				dpDropLen = Integer.parseInt(parameters.getOrDefault("dpdrop", String.valueOf(dpDropLen)));
				dpAddPrefix = parameters.getOrDefault("dpadd", "");

				logger.info("Using SMSQ {} db host [{}] port [{}] user [{}] db [{}] table [{}] ttl [{}] dcs [{}] priority [{}]", version, host, port, username, database, table, ttl, dcsEncoding, priority );
			}
			catch( MalformedURLException ex )
			{
				logger.info("SMSQ configuration: malformed URL: {} [{}]", fullUrl, ex.getMessage());
			}
		}

		SmsqConnector( String host, int port, String username, String password, String database, String table, String version )
		{
			this.host = host;
			this.port = port;
			this.username = username;
			this.password = password;
			this.database = database;
			this.table = table;
			this.version = version;

			if( port <= 0 ) port = 3306;
		}

		public void destroy()
		{
			if (connection != null) {
				try {
					connection.close();
					connection = null;
                } catch (SQLException e2) {
					logger.error("SMSQ: failed to close connection: {}", e2.getMessage());
				}
			}
		}

		private void connect()
		{
			if( connection != null ) return;

			logger.info("SMSQ: connecting to the database: jdbc:mysql://{}:{}/{}", host, port, database);

			try {

				Class.forName("com.mysql.jdbc.Driver");

				connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);

			} catch (SQLException | ClassNotFoundException e) {

				logger.error("SMSQ: connecting to the database failed with: {}", e.getMessage());

				if (connection != null) {
					try {
						connection.close();
						connection = null;
	                } catch (SQLException e2) {
						logger.error("SMSQ: failed to close connection after a failure: {}", e2.getMessage());
					}
				}
			}
		}

		private String normalizeMsisdn( String msisdn )
		{
			if( dpExpLen > 0 )
			{
				if( msisdn.length() == dpExpLen )
				{
					if( dpDropLen > 0 )
						msisdn = msisdn.substring(dpDropLen);
					if( dpAddPrefix.length() > 0 )
						msisdn = dpAddPrefix + msisdn;
				}
			}

			return msisdn;
		}

		public boolean sendSMS( String source, String dest, String msg, int dcs, int retries )
		{
			connect();

			if( connection == null ) return false;

			PreparedStatement statement = null;

			String nsource = normalizeMsisdn( source );
			String ndest = normalizeMsisdn( dest );

			try {
				logger.trace("SMSQ: inserting SMS into the SMSQ database, src [{}] dest [{}]", source, dest);

				if( dcsEncoding >= 0 )
					dcs = dcsEncoding;

				if( version.equals( "1.7" ) )
				{
					statement = connection.prepareStatement("INSERT INTO " + table +
						" (transaction_id, application, source_msisdn, destination_msisdn, dcs_encoding, message, kvpinfo, insertion_date, ttl) " +
						"VALUES (?, 'CREDIVERSE', ?, ?, ?, ?, '', NOW(), ?)");
					statement.setLong(1, ++seqNo);
					statement.setString(2, nsource);
					statement.setString(3, ndest);
					statement.setInt(4, dcs);
					statement.setString(5, msg);
					statement.setInt(6, ttl);
					statement.executeUpdate();
				}
				else
				{
					statement = connection.prepareStatement("INSERT INTO " + table +
						" (transaction_id, application, priority, source_msisdn, destination_msisdn, message, insertion_date, attempt_no, attempt_last, last_update) " +
						"VALUES (?, 'CREDIVERSE', ?, ?, ?, ?, NOW(), 0, NOW(), NOW())");
					statement.setLong(1, ++seqNo);
					statement.setInt(2, priority);
					statement.setString(3, nsource);
					statement.setString(4, ndest);
					statement.setString(5, msg);
					statement.executeUpdate();
				}

				logger.info("SMSQ: inserted SMS into the SMSQ database, src [{}] dest [{}] seq [{}] dcs [{}] ttl [{}] priority [{}]", nsource, ndest, seqNo, dcs, ttl, priority);

				return true;

			} catch (SQLException e) {

				logger.error("SMSQ: inserting into the database failed with: {}", e.getMessage());

				if (statement != null) {
					try {
						statement.close();
						statement = null;
	                } catch (SQLException e2) {
						logger.error("SMSQ: failed to close statement after a failure: {}", e2.getMessage());
					}
				}

				if (connection != null) {
					try {
						connection.close();
						connection = null;
	                } catch (SQLException e2) {
						logger.error("SMSQ: failed to close connection after a failure: {}", e2.getMessage());
					}
				}
			}

			if( (--retries) > 0 )
				sendSMS( source, dest, msg, dcs, retries );

			return false;
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////
	@Perms(perms = { @Perm(name = "ViewSmppParameters", description = "View SMPP Connector Parameters", category = "SMPP", supplier = true),
			@Perm(name = "ChangeSmppParameters", implies = "ViewSmppParameters", description = "Change SMPP Connector Parameters", category = "SMPP", supplier = true),
			@Perm(name = "ViewSmppNotifications", description = "View SMPP Connector Notifications", category = "SMPP", supplier = true),
			@Perm(name = "ChangeSmppNotifications", implies = "ViewSmppNotifications", description = "Change SMPP Connector Notifications", category = "SMPP", supplier = true) })
	public class SmppConfiguration extends ConfigurationBase
	{

		private long connectTimeoutMilliseconds = 10000;
		private long sendWindowTimeout = 10;
		private long responseTimeout = 10000;
		private long enquireLinkTimeout = 10000;
		private long requestExpiryTimeoutMilliseconds = 30000;
		private long monitorIntervalSeconds = 15;
		private int sendWindowSize = 10;
		private int failedMessageCacheSize = 10000;
		private int maximumMessageSize = 134;
		private long keepAliveIntervalMilliseconds = 30000;
		private long retryFailedSmsIntervalMilliseconds = 1000;
		private boolean messagesDiacritical = false;
		private SMSCConfig smscs[] = new SMSCConfig[] { new SMSCConfig(1), new SMSCConfig(2), new SMSCConfig(3), new SMSCConfig(4) };
		private SmppTon defaultSourceTypeOfNumber = SmppTon.UNKNOWN;
		private SmppNpi defaultSourceNumberPlanIndicator = SmppNpi.ISDN;
		private SmppTon defaultDestinationTypeOfNumber = SmppTon.INTERNATIONAL;
		private SmppNpi defaultDestinationNumberPlanIndicator = SmppNpi.ISDN;
		private boolean requireDatabaseRole = true;

		public void setConnectTimeoutMilliseconds(long connectTimeoutMilliseconds) throws ValidationException
		{
			check(esb, "ChangeSmppParameters");

			ValidationException.min(1, connectTimeoutMilliseconds);
			this.connectTimeoutMilliseconds = connectTimeoutMilliseconds;
		}

		public long getConnectTimeoutMilliseconds()
		{
			check(esb, "ViewSmppParameters");
			return connectTimeoutMilliseconds;
		}

		public void setSendWindowTimeout( long sendWindowTimeout ) throws ValidationException
		{
			check(esb, "ChangeSmppParameters");
			ValidationException.min(0, sendWindowTimeout);
			this.sendWindowTimeout = sendWindowTimeout;
		}

		@Config(description = "Send window timeout", comment = "milliseconds")
		public long getSendWindowTimeout()
		{
			check(esb, "ViewSmppParameters");
			return this.sendWindowTimeout;
		}

		public void setResponseTimeout( long responseTimeout ) throws ValidationException
		{
			check(esb, "ChangeSmppParameters");
			ValidationException.min(0, responseTimeout);
			this.responseTimeout = responseTimeout;
		}

		@Config(description = "Response Timeout", comment = "milliseconds")
		public long getResponseTimeout()
		{
			check(esb, "ViewSmppParameters");
			return this.responseTimeout;
		}

		public void setEnquireLinkTimeout( long enquireLinkTimeout ) throws ValidationException
		{
			check(esb, "ChangeSmppParameters");
			ValidationException.min(0, enquireLinkTimeout);
			this.enquireLinkTimeout = enquireLinkTimeout;
		}

		@Config(description = "EnquireLink Timeout", comment = "milliseconds")
		public long getEnquireLinkTimeout()
		{
			check(esb, "ViewSmppParameters");
			return this.enquireLinkTimeout;
		}

		public void setRequestExpiryTimeoutMilliseconds(long requestExpiryTimeoutMilliseconds) throws ValidationException
		{
			check(esb, "ChangeSmppParameters");

			ValidationException.min(1, requestExpiryTimeoutMilliseconds);
			this.requestExpiryTimeoutMilliseconds = requestExpiryTimeoutMilliseconds;
		}

		public long getRequestExpiryTimeoutMilliseconds()
		{
			check(esb, "ViewSmppParameters");
			return requestExpiryTimeoutMilliseconds;
		}

		public void setMonitorIntervalSeconds(long monitorIntervalSeconds) throws ValidationException
		{
			check(esb, "ChangeSmppParameters");

			ValidationException.min(1, monitorIntervalSeconds);
			this.monitorIntervalSeconds = monitorIntervalSeconds;
		}

		public long getMonitorIntervalSeconds()
		{
			check(esb, "ViewSmppParameters");
			return monitorIntervalSeconds;
		}

		public void setSendWindowSize( int sendWindowSize ) throws ValidationException
		{
			check(esb, "ChangeSmppParameters");
			ValidationException.min(1, sendWindowSize);
			this.sendWindowSize = sendWindowSize;
		}

		@Config(description = "Send Window Size", comment = "the amount of requests that can be waiting for responses")
		public int getSendWindowSize()
		{
			check(esb, "ViewSmppParameters");
			return this.sendWindowSize;
		}

		public void setFailedMessageCacheSize( int failedMessageCacheSize ) throws ValidationException
		{
			check(esb, "ChangeSmppParameters");
			ValidationException.min(1, failedMessageCacheSize);
			this.failedMessageCacheSize = failedMessageCacheSize;
		}

		@Config(description = "Failed Message Cache Size", comment = "the amount of requests that will be retried if the initial request fails")
		public int getFailedMessageCacheSize()
		{
			check(esb, "ViewSmppParameters");
			return this.failedMessageCacheSize;
		}
		
		public void setMaximumMessageSize(int maximumMessageSize) throws ValidationException
		{
			check(esb, "ChangeSmppParameters");

			ValidationException.min(1, maximumMessageSize);
			this.maximumMessageSize = maximumMessageSize;
		}

		public int getMaximumMessageSize()
		{
			check(esb, "ViewSmppParameters");
			return maximumMessageSize;
		}

		public void setKeepAliveIntervalMilliseconds(long keepAliveIntervalMilliseconds) throws ValidationException
		{
			check(esb, "ChangeSmppParameters");

			ValidationException.min(1, keepAliveIntervalMilliseconds);
			this.keepAliveIntervalMilliseconds = keepAliveIntervalMilliseconds;
		}

		public long getKeepAliveIntervalMilliseconds()
		{
			check(esb, "ViewSmppParameters");
			return keepAliveIntervalMilliseconds;
		}

		public void setRetryFailedSmsIntervalMilliseconds(long retryFailedSmsIntervalMilliseconds) throws ValidationException
		{
			check(esb, "ChangeSmppParameters");

			ValidationException.min(1, retryFailedSmsIntervalMilliseconds);
			this.retryFailedSmsIntervalMilliseconds = retryFailedSmsIntervalMilliseconds;
		}

		public long getRetryFailedSmsIntervalMilliseconds()
		{
			check(esb, "ViewSmppParameters");
			return retryFailedSmsIntervalMilliseconds;
		}

		@Config(description = "Convert Messages to Plain Text")
		public void setConvertMessagesToPlainText(boolean messagesDiacritical)
		{
			check(esb, "ChangeSmppParameters");
			this.messagesDiacritical = messagesDiacritical;
		}

		@Config(description = "Convert Messages to Plain Text")
		public boolean getConvertMessagesToPlainText()
		{
			check(esb, "ViewSmppParameters");
			return messagesDiacritical;
		}

		public void setDefaultSourceTypeOfNumber(SmppTon defaultSourceTypeOfNumber)
		{
			check(esb, "ChangeSmppParameters");
			this.defaultSourceTypeOfNumber = defaultSourceTypeOfNumber;
		}

		public SmppTon getDefaultSourceTypeOfNumber()
		{
			check(esb, "ViewSmppParameters");
			return this.defaultSourceTypeOfNumber;
		}

		public void setDefaultSourceNumberPlanIndicator(SmppNpi defaultSourceNumberPlanIndicator)
		{
			check(esb, "ChangeSmppParameters");
			this.defaultSourceNumberPlanIndicator = defaultSourceNumberPlanIndicator;
		}

		public SmppNpi getDefaultSourceNumberPlanIndicator()
		{
			check(esb, "ViewSmppParameters");
			return this.defaultSourceNumberPlanIndicator;
		}

		public void setDefaultDestinationTypeOfNumber(SmppTon defaultDestinationTypeOfNumber)
		{
			check(esb, "ChangeSmppParameters");
			this.defaultDestinationTypeOfNumber = defaultDestinationTypeOfNumber;
		}

		public SmppTon getDefaultDestinationTypeOfNumber()
		{
			check(esb, "ViewSmppParameters");
			return this.defaultDestinationTypeOfNumber;
		}

		public void setDefaultDestinationNumberPlanIndicator(SmppNpi defaultDestinationNumberPlanIndicator)
		{
			check(esb, "ChangeSmppParameters");
			this.defaultDestinationNumberPlanIndicator = defaultDestinationNumberPlanIndicator;
		}

		public SmppNpi getDefaultDestinationNumberPlanIndicator()
		{
			check(esb, "ViewSmppParameters");
			return this.defaultDestinationNumberPlanIndicator;
		}

		public void setRequireDatabaseRole(boolean requireDatabaseRole)
		{
			check(esb, "ChangeSmppParameters");
			this.requireDatabaseRole = requireDatabaseRole;
		}

		public boolean getRequireDatabaseRole()
		{
			check(esb, "ViewSmppParameters");
			return this.requireDatabaseRole;
		}

		@Override
		public String getPath(String languageCode)
		{
			return "Technical Settings";
		}

		@Override
		public INotifications getNotifications()
		{
			return notifications;
		}

		@Override
		public long getSerialVersionUID()
		{
			return -3662775870768294396L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "SMPP Connector";
		}

		@Override
		public Collection<IConfiguration> getConfigurations()
		{
			Collection<IConfiguration> configs = new ArrayList<IConfiguration>();
			for (int i = 0; i < smscs.length; i++)
			{
				configs.add(smscs[i]);
			}
			return configs;
		}

		@Override
		public void validate() throws ValidationException
		{

		}

		@Override
		public void performUpdateNotificationSecurityCheck()
		{
			check(esb, "ViewSmppNotifications");
		}

		@Override
		public void performGetNotificationSecurityCheck()
		{
			check(esb, "ChangeSmppNotifications");
		}
		
		@Override
		public boolean load(IDatabaseConnection databaseConnection)
		{
			logger.trace("load: {}", getSerialVersionUID());
			if(!super.load(databaseConnection))
				return false;
			//Trigger configuration to reload and re-init the connector's sessions.
			try {
				setConfiguration(this);
			} catch (ValidationException e) {
				logger.error("Invalid configuration values caused Validation Exception in Smpp Connector", e);
				return false;
			}
			return true;
		}

	};

	SmppConfiguration config = new SmppConfiguration();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// SMSC Config
	//
	// /////////////////////////////////

	@Perms(perms = { @Perm(name = "ViewSMSC", description = "View SMSC Parameters", category = "SMSC", supplier = true),
			@Perm(name = "ChangeSMSC", implies = "ViewSMSC", description = "Change SMSC Parameters", category = "SMSC", supplier = true) })
	public class SMSCConfig extends ConfigurationBase
	{

		private int smsc = 0;

		private String smscName = "SMSC";
		private String smscUrl = "127.0.0.1";
		private int port = 2775;
		private SmsCBinding smscBinding = SmsCBinding.NONE;
		private String password = "password";
		private String systemID = "hxc";
		private String systemType = "hxc";
		private String bindAddressRange = "";
		private SmppNpi bindAddressRangeNumberPlanIndicator = SmppNpi.UNKNOWN;
		private SmppTon bindAddressRangeTypeOfNumber = SmppTon.UNKNOWN;

		public String describe(String extra)
		{
			return String.format("%s@%s(smsc = %s, smscName = %s, smscUrl = %s, port = %s, smscBinding = %s, password = %s, systemID = %s, systemType = %s, bindAddressRange = %s, bindAddressRangeNumberPlanIndicator = %s, bindAddressRangeTypeOfNumber = %s%s%s)",
					this.getClass().getName(), Integer.toHexString(this.hashCode()),
					smsc, smscName, smscUrl, port, smscBinding, password, systemID, systemType,
					bindAddressRange, bindAddressRangeNumberPlanIndicator, bindAddressRangeTypeOfNumber, 
					(extra.isEmpty() ? "" : ", "), extra);
		}

		public String describe()
		{
			return this.describe("");
		}

		public String toString()
		{
			return this.describe();
		}

		public SMSCConfig(int smsc)
		{
			this.smsc = smsc;
		}

		public SMSCConfig()
		{
		}

		public void setSmscName(String smscName)
		{
			check(esb, "ChangeSMSC");
			this.smscName = smscName;
		}

		public String getSmscName()
		{
			check(esb, "ViewSMSC");
			return smscName;
		}

		public void setSmscUrl(String smscUrl)
		{
			check(esb, "ChangeSMSC");
			this.smscUrl = smscUrl;
		}

		public String getSmscUrl()
		{
			check(esb, "ViewSMSC");
			return smscUrl;
		}

		public void setPort(int port) throws ValidationException
		{
			check(esb, "ChangeSMSC");

			ValidationException.port(port, "Port");
			this.port = port;
		}

		public int getPort()
		{
			check(esb, "ViewSMSC");
			return port;
		}

		public void setSmscBinding(SmsCBinding smscBinding) throws ValidationException
		{
			check(esb, "ChangeSMSC");
			this.smscBinding = smscBinding;
		}

		public SmsCBinding getSmscBinding()
		{
			check(esb, "ViewSMSC");
			return smscBinding;
		}

		public void setPassword(String password)
		{
			check(esb, "ChangeSMSC");
			this.password = password;
		}

		public String getPassword()
		{
			check(esb, "ViewSMSC");
			return password;
		}

		public void setSystemID(String systemID)
		{
			check(esb, "ChangeSMSC");
			this.systemID = systemID;
		}

		public String getSystemID()
		{
			check(esb, "ViewSMSC");
			return systemID;
		}

		public void setSystemType(String systemType)
		{
			check(esb, "ChangeSMSC");
			this.systemType = systemType;
		}

		public String getSystemType()
		{
			check(esb, "ViewSMSC");
			return systemType;
		}

		public void setBindAddressRange(String bindAddressRange) throws ValidationException
		{
			check(esb, "ChangeSMSC");
			ValidationException.validate(bindAddressRange, "^.{0,41}$", "Bind Address Range");
			this.bindAddressRange = bindAddressRange;
		}

		public String getBindAddressRange()
		{
			check(esb, "ViewSMSC");
			return this.bindAddressRange;
		}

		public void setBindAddressRangeNumberPlanIndicator(SmppNpi bindAddressRangeNumberPlanIndicator)
		{
			check(esb, "ChangeSMSC");
			this.bindAddressRangeNumberPlanIndicator = bindAddressRangeNumberPlanIndicator;
		}

		public SmppNpi getBindAddressRangeNumberPlanIndicator()
		{
			check(esb, "ViewSMSC");
			return this.bindAddressRangeNumberPlanIndicator;
		}

		public void setBindAddressRangeTypeOfNumber(SmppTon bindAddressRangeTypeOfNumber)
		{
			check(esb, "ChangeSMSC");
			this.bindAddressRangeTypeOfNumber = bindAddressRangeTypeOfNumber;
		}

		public SmppTon getBindAddressRangeTypeOfNumber()
		{
			check(esb, "ViewSMSC");
			return this.bindAddressRangeTypeOfNumber;
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

		@Override
		public long getSerialVersionUID()
		{
			return Long.parseLong("536142002719613793" + (smsc + 5));
		}

		@Override
		public String getPath(String languageCode)
		{
			return "";
		}

		@Override
		public String getName(String languageCode)
		{
			return "SMSC " + smsc;
		}

		@Override
		public void validate() throws ValidationException
		{

		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Notifications
	//
	// /////////////////////////////////
	Notifications notifications = new Notifications(null);
	private final int InvalidCommand = notifications.add("Invalid Command", "Invalid Command");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Sending SM
	//
	// /////////////////////////////////

	// Sends an SM asynchronously
	@Override
	public void send(String fromMSISDN, String toMSISDN, INotificationText notificationText)
	{
		send(fromMSISDN, toMSISDN, notificationText, false);
	}

	// Sends an SM either synchronously or asynchronously
	@Override
	public boolean send(String fromMSISDN, String toMSISDN, INotificationText notificationText, boolean synchronise)
	{
		ISmsResponse response = internalSend(fromMSISDN, toMSISDN, notificationText, synchronise, null);
		logger.debug("SMS SENDING RESPONSE: {} {}", response.getMessageID(), response.getResultMessage());
		return response != null && !response.getMessageID().equals("ERROR");
	}

	// Sends a SubmitSm Request and returns a SubmitSm Response
	@Override
	public ISmsResponse sendRequest(String fromMSISDN, String toMSISDN, INotificationText notificationText)
	{
		return internalSend(fromMSISDN, toMSISDN, notificationText, true, null);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Sending SM with Address
	//
	// /////////////////////////////////

	// Sends an SM asynchronously
	@Override
	public void send(SmppAddress fromAddress, SmppAddress toAddress, INotificationText notificationText)
	{
		send(fromAddress, toAddress, notificationText, false);
	}

	// Sends an SM either synchronously or asynchronously
	@Override
	public boolean send(SmppAddress fromAddress, SmppAddress toAddress, INotificationText notificationText, boolean synchronise)
	{
		ISmsResponse response = internalSend(fromAddress, toAddress, notificationText, synchronise, null);
		return response != null && !response.getMessageID().equals("ERROR");
	}

	// Sends a SubmitSm Request and returns a SubmitSm Response
	@Override
	public ISmsResponse sendRequest(SmppAddress fromAddress, SmppAddress toAddress, INotificationText notificationText)
	{
		return internalSend(fromAddress, toAddress, notificationText, true, null);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Receiving SM
	//
	// ///////////////////////////////

	// When the SMPP receives a Deliver SM from an external source
	private void onSMReceived(DeliverSm deliverSm, String source)
	{
		// Converts the DeliverSm to a SmsRequest
		Date originTimeStamp = new Date();
		SmsRequest hsxRequest = new SmsRequest(deliverSm.getSourceAddress().getAddress(), deliverSm.getDestAddress().getAddress(), new String(deliverSm.getShortMessage()),
				"" + deliverSm.getSequenceNumber(), originTimeStamp, me);

		logger.trace("DeliverSM: Received SM '{}' from {}", new String(deliverSm.getShortMessage()), hsxRequest.getMSISDN());

		// Dispatch a SmsRequest through the ESB
		int count = esb.dispatch(hsxRequest, new SmppConnection(this));

		// If nothing listened in for that SM request then send it back with an Invalid Command message
		if (count == 0)
		{
			internalSend(hsxRequest.getShortCode(), hsxRequest.getMSISDN(), notifications.get(InvalidCommand, esb.getLocale().getDefaultLanguageCode(), esb.getLocale(), null), false, source);
			logger.trace("DeliverSM: Invalid SMS Command '{}' on {}", new String(deliverSm.getShortMessage()), hsxRequest.getShortCode());
		}
	}

	// When the SMPP receives a Data SM from an external source
	private void onSMReceived(DataSm dataSm, String source)
	{
		String message = null;
		// FIXME ... yes ... this is wrong - have to decode using DCS sent ... but it was wrong before I made this change also and I'm in a rush ... get over it ... this is no worse than the
		// rest of the code ... may even be slightly better ...
		// ... I'm just making the code slightly better ... not here to fix all problems at once
		if (dataSm.hasOptionalParameter(SmppConstants.TAG_MESSAGE_PAYLOAD))
		{
			Tlv messagePayload = dataSm.getOptionalParameter(SmppConstants.TAG_MESSAGE_PAYLOAD);
			message = new String(messagePayload.getValue());
		}
		else
		{
			message = new String(dataSm.getShortMessage());
		}
		// Converts the DeliverSm to a SmsRequest
		Date originTimeStamp = new Date();
		SmsRequest hsxRequest = new SmsRequest(dataSm.getSourceAddress().getAddress(), dataSm.getDestAddress().getAddress(), message, "" + dataSm.getSequenceNumber(), originTimeStamp, me);

		logger.trace("DataSM: Received SM '{}' from {}", message, hsxRequest.getMSISDN());

		// Dispatch a SmsRequest through the ESB
		int count = esb.dispatch(hsxRequest, new SmppConnection(this));

		// If nothing listened in for that SM request then send it back with an Invalid Command message
		if (count == 0)
		{
			internalSend(hsxRequest.getShortCode(), hsxRequest.getMSISDN(), notifications.get(InvalidCommand, esb.getLocale().getDefaultLanguageCode(), esb.getLocale(), null), false, source);
			logger.trace("DataSM: Invalid SMS Command '{}' on {}", message, hsxRequest.getShortCode());
		}
	}

	// Helpers

	private SmppAddress applyDefaultsToSource(SmppAddress address)
	{
		if (address.getType() == null)
		{
			address.setType(config.getDefaultSourceTypeOfNumber());
		}
		if (address.getPlan() == null)
		{
			address.setPlan(config.getDefaultSourceNumberPlanIndicator());
		}
		return address;
	}

	private SmppAddress applyDefaultsToDestination(SmppAddress address)
	{
		if (address.getType() == null)
		{
			address.setType(config.getDefaultDestinationTypeOfNumber());
		}
		if (address.getPlan() == null)
		{
			address.setPlan(config.getDefaultDestinationNumberPlanIndicator());
		}
		return address;
	}

	private byte convertNpi(SmppNpi npi)
	{
		switch (npi)
		{
		case UNKNOWN:
			return SmppConstants.NPI_UNKNOWN;
		case ISDN:
			return SmppConstants.NPI_E164;
		case DATA:
			return SmppConstants.NPI_X121;
		case TELEX:
			return SmppConstants.NPI_TELEX;
		case LAND_MOBILE:
			return SmppConstants.NPI_LAND_MOBILE;
		case NATIONAL:
			return SmppConstants.NPI_NATIONAL;
		case PRIVATE:
			return SmppConstants.NPI_PRIVATE;
		case ERMES:
			return SmppConstants.NPI_ERMES;
		case INTERNET:
			return SmppConstants.NPI_INTERNET;
		case WAP:
			return SmppConstants.NPI_WAP_CLIENT_ID;
		}
		return SmppConstants.NPI_UNKNOWN;
	}

	private byte convertTon(SmppTon ton)
	{
		switch (ton)
		{
		case UNKNOWN:
			return SmppConstants.TON_UNKNOWN;
		case INTERNATIONAL:
			return SmppConstants.TON_INTERNATIONAL;
		case NATIONAL:
			return SmppConstants.TON_NATIONAL;
		case NETWORK_SPECIFIC:
			return SmppConstants.TON_NETWORK;
		case SUBSCRIBER_NUMBER:
			return SmppConstants.TON_SUBSCRIBER;
		case ALPHANUMERIC:
			return SmppConstants.TON_ALPHANUMERIC;
		case ABBREVIATED:
			return SmppConstants.TON_ABBREVIATED;
		}
		return SmppConstants.TON_UNKNOWN;
	}

	private Address convertAddress(SmppAddress address)
	{
		return new Address(convertTon(address.getType()), convertNpi(address.getPlan()), address.getAddress());
	}

	private SmppAddress createAddress(String msisdn)
	{
		return new SmppAddress(msisdn, null, null);
	}

	private ISmsResponse internalSend(String fromMSISDN, String toMSISDN, INotificationText notificationText, boolean synchronise, String source)
	{
		return internalSend(createAddress(fromMSISDN), createAddress(toMSISDN), notificationText, synchronise, source);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal SM Implementation
	//
	// ///////////////////////////////

	// Creates an SM request to be sent
	private ISmsResponse internalSend(SmppAddress fromAddress, SmppAddress toAddress, INotificationText notificationText, boolean synchronise, String source)
	{
		// Date created
		Date created = new Date();

		// Get the message
		String message = config.messagesDiacritical ? deAccent(notificationText.getText()) : notificationText.getText();

		// Get the data coding
		EncodingScheme scheme = EncodingScheme.valueOf(esb.getLocale().getEncodingScheme(notificationText.getLanguageCode()));
		byte dataCoding = dataCodingForEncodingScheme(scheme);
		Charset charset = charsetForEncodingScheme(scheme);

		logger.debug("internalSend: languageCode = {}, scheme = {}, dataCoding = {}, charset = {}", notificationText.getLanguageCode(), scheme, dataCoding, charset);

		// Normalise the message
		byte normalisedMessage[] = CharsetUtil.encode(CharsetUtil.normalize(message, charset), charset);

		if( smsq != null )
		{
			ISmsResponse response = new SmsResponse(SmsResponse.ERROR, "Technical problem has occurred.", sequence++);

			boolean result =
				smsq.sendSMS( convertAddress(applyDefaultsToSource(fromAddress)).getAddress(),
					convertAddress(applyDefaultsToDestination(toAddress)).getAddress(), message, dataCoding, 2 );

			if( result == true )
			{
				response.setMessageID(SmsResponse.ASYNCHRONOUS);
				response.setResultMessage("Message successfully sent asynchronously.");
				response.setSequenceNumber(sequence - 1);
			}

			return response;
		}

		// Split the message into segments
		byte messageSegments[][] = splitLongMessage(normalisedMessage, config.maximumMessageSize);

		// Set up generic failed response
		ISmsResponse response = new SmsResponse(SmsResponse.ERROR, "Technical problem has occurred.", sequence++);

		for (int i = 0; i < messageSegments.length; i++)
		{
			// Set the Short Message
			SubmitSm sm = new SubmitSm();
			sm.setDataCoding(dataCoding);
			sm.setEsmClass(SmppConstants.ESM_CLASS_UDHI_MASK);
			// sm.setRegisteredDelivery(SmppConstants.REGISTERED_DELIVERY_SMSC_RECEIPT_REQUESTED);
			sm.setSourceAddress(convertAddress(applyDefaultsToSource(fromAddress)));
			sm.setDestAddress(convertAddress(applyDefaultsToDestination(toAddress)));

			try
			{
				sm.setShortMessage(messageSegments[i]);
			}
			catch (SmppInvalidArgumentException exception)
			{
				logger.error("Cannot set short message", exception);
				response.setResultMessage("Technical problem has occurred. " + exception.toString());
				break;
			}

			// Send the actual SM
			response = sendSubmitSmRequest(sm, synchronise, source);
		}

		// Add to history
		Date originTimeStamp = new Date();
		addHistory(created, new SmsRequest(fromAddress.getAddress(), toAddress.getAddress(), message, getTransactionID(), originTimeStamp, me), response);

		return response;
	}

	// Send the SubmitSM to the SMSCs
	private ISmsResponse sendSubmitSmRequest(SubmitSm sm, boolean synchronous, String source)
	{
		logger.trace("SmppConnector.sendSubmitSmRequest: sm = ({}){}, synchronous = {}, source = {}", sm.hashCode(), sm, synchronous, source);
		// Create the response
		ISmsResponse response = new SmsResponse(SmsResponse.ERROR, "Failed to submit sm.", sequence - 1);

		// A set with the failed addresses that the messages failed to send to
		//Set<String> failedAddresses = new HashSet<String>();
		boolean sent = false;

		// Iterate through the SMSCs
		SMSCConfig[] smscs = getUniqueSMSCConfigurations(source);
		if (smscs.length < 1)
		{
			logger.debug("No SMPP servers available from configuration (sm={}, source={}, smscs.length={})", sm.hashCode(), source, smscs.length);
		    response.setMessageID(SmsResponse.WARN);
			response.setResultMessage("No SMPP servers available from configuration.");
		}


		for (SMSCConfig smsc : smscs)
		{
			// Ensure the SMSC is valid
			if (smsc == null)
			{
				logger.warn("got null smsc ... trying next (sm={}, source={}, smscs.length={})", sm.hashCode(), source, smscs.length);
				continue;
			}
			// DO NOT SEND SMS TO ALL SMSCS !!!
			if ( sent ) break;

			logger.trace("SmppConnector.sendSubmitSmRequest: Sending SMS");
			
			// Get the session
			SmppSession session = sessions.get(smsc.getSerialVersionUID());
			// Ensure the session is valid and still bound
			
			if (session == null || !session.isBound())
			{				
					// If not, add it to the failed addresses
					logger.warn("smscs {} does not exist or is not bound (sm={}, source={}, smscs.length={})", smsc.getName(Phrase.ENG), sm.hashCode(), source, smscs.length);
					
					if(session == null || !session.getStale())
					{
						try{
							connectorWriteLock.lock();
							if(session == null || sessions.containsValue(session))
							{
								sessions.remove(smsc.getSerialVersionUID());
								if (!failedSessions.contains(smsc)) {
									failedSessions.push(smsc);
								}	
							}
						}
						finally{
							connectorWriteLock.unlock();
						}
					}		
				

				// AB: This seems unnecessary. Can be removed
				/*if(session != null){
					unbind(session);
				}*/
				continue;
			}
			

			// Create future for messages
			@SuppressWarnings("rawtypes")
			WindowFuture<Integer, PduRequest, PduResponse> future = null;
			try
			{
				// Send requests
				logger.trace("SmppConnector.sendSubmitSmRequest: sending sms with smsc {} (sm={}, source={}, smscs.length={})", smsc, sm.hashCode(), source, smscs.length);
				future = session.sendRequestPdu(sm, config.sendWindowTimeout, synchronous);
				sent = true;
			}
			catch (Throwable throwable)
			{
				// Log and set the message
				logger.error("Failed to send sm: {} for {} (may try next smsc) (sm={}, source={}, smscs.length={})", throwable, smsc.getName(Phrase.ENG), sm.hashCode(), source, smscs.length);
				logger.error("Failed to send sm");
				response.setResultMessage("Failed to send sm: " + throwable.getMessage());

				// Add to failed addresses
				try{					
					logger.debug("Unbinding session {} due to failed SMS.", session.getName());
					unbind(session);
					connectorWriteLock.lock();
					if(session == null || (!session.getStale() && sessions.containsValue(session)))
					{
						sessions.remove(smsc.getSerialVersionUID());
						if (!failedSessions.contains(smsc) && !session.getStale()) {
							failedSessions.push(smsc);
						}
					}
				}
				finally{
					connectorWriteLock.unlock();
				}
				continue;
			}

			// If the call is synchronous wait for the response
			if (synchronous)
			{

				try
				{
					// Wait for the response from the SMSC
					future.await(config.responseTimeout);
				}
				catch (InterruptedException exception)
				{
					logger.error("Interrupted while waiting for response pdu ({}) (sm={}, source={}, smscs.length={})", exception, sm.hashCode(), source, smscs.length);
					logger.error(exception.getMessage(), exception);
				}

				// Get the response
				PduResponse pduResp = future.getResponse();

				// Ensure the response is valid
				if (pduResp != null && pduResp instanceof SubmitSmResp)
				{
					// Set the response message
					SubmitSmResp smResponse = (SubmitSmResp) pduResp;
					response.setMessageID(smResponse.getMessageId());
					response.setResultMessage(smResponse.getResultMessage());
					response.setSequenceNumber(smResponse.getSequenceNumber());
				}
			}
			// Else the call is asynchronous
			else
			{
				// Set a message saying it is asynchronous
				response.setMessageID(SmsResponse.ASYNCHRONOUS);
				response.setResultMessage("Message successfully sent asynchronously.");
				response.setSequenceNumber(sequence - 1);
			}
		}

		if (sent == false)
		{
			// Cache the message that failed to send
			
			SmppFailedMessage message = new SmppFailedMessage();
			message.setMessage(sm);

			// Add the message to the cache (back of queue - Deque.addLast()) 
			failedMessageCache.addLast(message);
			if (failedMessageCache.size() > config.getFailedMessageCacheSize())
			{
				SmppFailedMessage discarded = failedMessageCache.pop();
				logger.warn("messageCache full {} - discarded ({}){}", failedMessageCache.size(), discarded.getMessage().hashCode(), discarded.getMessage());
			}
		}

		// Log the failure
		if (response.getMessageID().equals(SmsResponse.ERROR))
		{
			logger.error(response.getResultMessage());
		}

		// Return the response
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ISms Connector Implementation - SMS History
	//
	// /////////////////////////////////

	// Gets the history of last sent SMSs
	@Override
	public ISmsHistory[] getSmsHistory()
	{
		SmsHistory[] temp;
		synchronized (SmppConnector.history) 
		{
			temp = history.clone();
		}	

		// If there is more than one SM sent, then sort according to newest-oldest
		if (temp.length > 1)
		{
			Arrays.sort(temp, new Comparator<SmsHistory>()
			{
				@Override
				public int compare(SmsHistory o1, SmsHistory o2)
				{
					if (o1 == null || o2 == null)
						return 1;
					return o1.getDate().compareTo(o2.getDate()) * -1;
				}
			});
		}

		return temp;
	}

	// Clears the history buffer
	@Override
	public void clearHistory()
	{
		synchronized (SmppConnector.history) 
		{
			for (int i = 0; i < history.length; i++)
			{
				history[i] = null;
			}
			currentIndex = 0;
		}	
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Session Handler
	//
	// /////////////////////////////////

	// Handles any SMPP related methods
	class SmppSmHandler extends SmppSessionHandler
	{

		// A reference to the SMSC
		private SMSCConfig smsc;

		public SmppSmHandler(SMSCConfig smsc)
		{
			super(smsc.getName(Phrase.ENG));
			this.smsc = smsc;
		}

		// When the SMPP receives a PDU Request
		@Override
		public PduResponse firePduRequestReceived(@SuppressWarnings("rawtypes") PduRequest pduRequest)
		{
			// Create the response
			PduResponse response = pduRequest.createResponse();

			// Asynchronously check if the request is a DeliverSM and send it through to onSMReceived method
			final PduRequest<?> request = pduRequest;
			new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					// Check the command ID of the request
					if (request.getCommandId() == SmppConstants.CMD_ID_DELIVER_SM)
					{
						onSMReceived((DeliverSm) request, smsc.smscUrl);
					}
					else if (request.getCommandId() == SmppConstants.CMD_ID_DATA_SM)
					{
						onSMReceived((DataSm) request, smsc.smscUrl);
					}
				}

			}).start();

			return response;
		}

		// If the channel unexpectedly closes to the SMSC
		@Override
		public void fireChannelUnexpectedlyClosed()
		{
			super.fireChannelUnexpectedlyClosed();
			logger.warn("SmppConnector.fireChannelUnexpectedlyClosed. Channel closed. SMSC {}, URL {}, Port {}, System ID {}", smsc.getSmscName(), smsc.getSmscUrl(), smsc.getPort(), smsc.getSystemID());
			
			// Remove the session from the sessions map and get the session
			SmppSession session = sessions.get(smsc.getSerialVersionUID());//sessions.get(smsc.getSerialVersionUID());
			
			// Add to failed sessions
			try{
				connectorWriteLock.lock();
				if(session == null || (!session.getStale() &&sessions.containsValue(session)))
				{
					sessions.remove(smsc.getSerialVersionUID());
					if (!failedSessions.contains(smsc) && (session == null || 
					(session != null && !session.getStale()))) {
						failedSessions.push(smsc);
						logger.info("Failed sesssions tracking adding smsc: {}", smsc.getName(Phrase.ENG));
					}
				}
			}
			finally{
				connectorWriteLock.unlock();
			}
			// Close the session and destroy it
			Objects.requireNonNull(session, "session may not be null");
			logger.info("Closing and destroying  session name: {}, channel id: {}, bound duration: {}",
					session.getConfiguration().getName(),
					session.getChannel().getId(),
					session.getBoundDuration());
			session.close();
			session.destroy();
			logger.info("Destroyed session name: {}, channel id: {} ", session.getConfiguration().getName(),
					session.getChannel().getId());				

			
			
			
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	// Convert the encoding scheme to a byte
	private byte dataCodingForEncodingScheme(EncodingScheme scheme)
	{
		switch (scheme)
		{
		case GSM:
		case GSM7:
		case GSM8:
		case PACKED_GSM:
		case AIRWIDE_GSM:
		case VFD2_GSM:
		case VFTR_GSM:
		case TMOBILE_NL_GSM:
			return SmppConstants.DATA_CODING_GSM;

		case UCS_2:
			return SmppConstants.DATA_CODING_UCS2;

		case AIRWIDE_IA5:
		case ISO_8859_1:
		case ISO_8859_15:
		case MODIFIED_UTF8:
		case UTF_8:
		default:
			return SmppConstants.DATA_CODING_UCS2;
		}
	}

	private Charset charsetForEncodingScheme(EncodingScheme scheme)
	{
		switch (scheme)
		{
		case GSM:
		case GSM7:
		case GSM8:
		case PACKED_GSM:
		case AIRWIDE_GSM:
		case VFD2_GSM:
		case VFTR_GSM:
		case TMOBILE_NL_GSM:
			return CharsetUtil.CHARSET_GSM;

		case UCS_2:
			return CharsetUtil.CHARSET_UCS_2;

		case AIRWIDE_IA5:
		case ISO_8859_1:
		case ISO_8859_15:
		case MODIFIED_UTF8:
		case UTF_8:
		default:
			return CharsetUtil.CHARSET_UCS_2;
		}
	}

	private int countProspectiveSessions(SMSCConfig smscs[])
	{
		// Check for nulls
		int count = 0;

		for (SMSCConfig smsc : smscs)
		{
			if ( smsc == null ) continue;
			switch (smsc.smscBinding)
			{
			case RECEIVER:
				count++;
				break;
			case TRANSCEIVER:
				count++;
				break;
			case TRANSMITTER:
				count++;
				break;
			case NONE:
			default:
				break;
			}
		}
		return count;
	}

	// Creates the sessions to connect to the SMSC
	private synchronized void createSessions(SMSCConfig smscs[])
	{
		logger.info("SmppConnector.createSessions: creating sessions for (smscs.length = {}) smscs = {}", smscs.length, smscs);
		
		smsqSMSC = null;
		if( smsq != null )
		{
			smsq.destroy();
			smsq = null;
		}

		// Copy the sessions to a temporary map and clear the original one
		ConcurrentMap<Long, SmppSession> tempSessions = new ConcurrentHashMap<Long, SmppSession>(sessions);

		sessions.clear();

		// Unbind all the sessions and mark them stale
		for (SmppSession session : tempSessions.values())
		{
			// Ensure session is valid
			if (session == null)
				continue;

			// Unbind the session
			session.setStale(true);
			logger.trace("unbinding session ({}) as it is not null ... ?", session);
			unbind(session);
		}

		try{
			connectorWriteLock.lock();
			// Clear the failed sessions stack
			logger.trace("Clearing the failed sessions list");
			failedSessions.clear();
		}
		finally{
			connectorWriteLock.unlock();
		}

		// Iterate through the sessions and make sure they are destroyed
		for (SmppSession session : tempSessions.values())
		{
			// Check if session is valid
			if (session == null)
				continue;
			try
			{
				// Destroy the session
				session.destroy();
			}
			catch (Exception exception)
			{
				logger.error("Failed to destroy session", exception);
				logger.error(exception.getMessage(), exception);
			}
		}
	
		// Clear the map
		tempSessions.clear();
		uniqueSMSCs = null;
		// Iterate through the SMSCs
		for (SMSCConfig smsc : smscs)
		{
			// Create each session (uses connectionWriteLock)
			SmppSession session = createSession(smsc);
			try{
				connectorWriteLock.lock();
				if(session != null)
				{
					sessions.put(smsc.getSerialVersionUID(), session);
				}
				// AB: Not needed since null sessions are already added to failedSessions
				// in the createSession method
				/*else
				{
					if (!isFailedSession(smsc.getSerialVersionUID()) && !failedSessions.contains(smsc))
						failedSessions.put(smsc);
				}*/
			}
			finally{
				connectorWriteLock.unlock();
			}
		}
		
		logger.info("SmppConnector::createSessions: created sessions.size={}", sessions.size());
	}

	// Creates a session from the configuration
	private SmppSession createSession(SMSCConfig smsc)
	{
		// Check for nulls
		if (smsc == null || client == null)
			return null;

		// Get the bind type
		SmppBindType bindType = null;
		switch (smsc.smscBinding)
		{
		case RECEIVER:
			bindType = SmppBindType.RECEIVER;
			break;
		case TRANSCEIVER:
			bindType = SmppBindType.TRANSCEIVER;
			break;
		case TRANSMITTER:
			bindType = SmppBindType.TRANSMITTER;
			break;
		case NONE:
			if( smsc.smscUrl.startsWith("smsq:") )
			{
				logger.info("SMSC configuration for [{}] is for SMSQ", smsc.getSmscName());
				smsqSMSC = smsc.getSmscName();
				smsq = new SmsqConnector( smsc.smscUrl );
			}
			return null;
		default:
			// Return null if the bind type is invalid
			return null;
		}

		// Load the configuration into the SmppSessionConfiguration
		SmppSessionConfiguration sessionConfig = new SmppSessionConfiguration();
		sessionConfig.setName(smsc.getName(Phrase.ENG));
		sessionConfig.setType(bindType);
		sessionConfig.setHost(smsc.smscUrl);
		sessionConfig.setPort(smsc.port);
		sessionConfig.setSystemId(smsc.systemID);
		sessionConfig.setSystemType(smsc.systemType);
		sessionConfig.setPassword(smsc.password);
		sessionConfig.getLoggingOptions().setLogBytes(true);
		sessionConfig.setConnectTimeout(config.connectTimeoutMilliseconds);
		sessionConfig.setRequestExpiryTimeout(config.requestExpiryTimeoutMilliseconds);
		sessionConfig.setWindowMonitorInterval(config.monitorIntervalSeconds * 1000);
		sessionConfig.setWindowSize(config.sendWindowSize);
		sessionConfig.setCountersEnabled(true);
		sessionConfig.setAddressRange(
				convertAddress(new SmppAddress((smsc.bindAddressRange.length() > 0 ? smsc.bindAddressRange : null), smsc.bindAddressRangeTypeOfNumber, smsc.bindAddressRangeNumberPlanIndicator)));

		// Create a handler for the session
		SmppSmHandler handler = new SmppSmHandler(smsc);
		SmppSession session = null;
		try
		{
			// Create the session
			session = client.bind(sessionConfig, handler);
			logger.info("New session created and bound session name: {}, channel id: {}", session.getConfiguration().getName(), session.getChannel().getId());
			
			try
			{
				// Enquire the link
				enquireLink(session);
			}
			catch (Throwable throwable)
			{
				// Unbind before adding the session to the failed sessions stack
				logger.trace("unbinding session ({}) exception occured in enquireLink ... ({})", session, throwable);
				unbind(session);

				throw throwable;
			}
		}
		catch (Throwable throwable)
		{
			logger.error("Exception when creating session", throwable);

			// Add the failed session
			try{
				connectorWriteLock.lock();				
				if (!isFailedSession(smsc.getSerialVersionUID()) && !failedSessions.contains(smsc))
					failedSessions.addFirst(smsc);
			}
			finally{
				connectorWriteLock.unlock();
			}

			return null;
		}
		return session;
	}

	// Rebinds a session to the SMSC
	private boolean bind(SmppSession session) throws Throwable
	{
		// Ensure the session is valid and not bound to the SMSC already
		if (session == null || session.isBound())
			return true;

		// Get the bind request type
		BaseBind<?> bind = null;
		switch (session.getBindType())
		{
		case RECEIVER:
			bind = new BindReceiver();
			break;
		case TRANSCEIVER:
			bind = new BindTransceiver();
			break;
		case TRANSMITTER:
			bind = new BindTransmitter();
			break;
		default:
			return true;
		}

		// Set the various information for the bind request
		bind.setSystemId(session.getSystemId());
		bind.setPassword(session.getPassword());
		bind.setSystemType(session.getSystemType());
		bind.setInterfaceVersion(session.getInterfaceVersion());
		bind.setAddressRange(session.getAddressRange());

		try
		{
			// Try bind to the SMSC and get a response back
			BaseBindResp resp = session.bind(bind, config.responseTimeout);

			// Return true if the response is valid
			return resp != null && resp.isResponse();
		}
		catch (SmppBindException exception)
		{
			// Handle any errors that may have occurred with the binding
			handleBindStatus(exception.getBindResponse().getCommandStatus(), session);
			throw exception;
		}
		catch (SmppChannelException exception)
		{
			throw exception;
		}
		catch (Throwable throwable)
		{
			logger.error("Failed to (re-)bind {}: {}", session.getName(), throwable);
			throw throwable;
		}
	}

	// Unbind and destroy the session
	private void unbind(SmppSession session)
	{
		if (session == null || !session.isBound())
			return;

		logger.info("Unbinding session name: {}, channel id: {}, bound duration: {}",
				session.getConfiguration().getName(),
				session.getChannel().getId(),
				session.getBoundDuration());
		session.unbind(config.responseTimeout);
		session.close();
		session.destroy();
		logger.info("unbound and destroyed session name: {}, channel id: {}, bound duration: {}",
				session.getConfiguration().getName(),
				session.getChannel().getId());
	}

	// Sends a EnquireLink request to the SMSC for a keep alive
	private boolean enquireLink(SmppSession session) throws Throwable
	{
		// Ensure the session is valid and bound to an SMSC
		if (session == null || !session.isBound())
			return false;

		try
		{
			// Send the enquire link request
			EnquireLinkResp resp = session.enquireLink(new EnquireLink(), config.enquireLinkTimeout);

			// Check if the response is valid
			return resp != null && resp.isResponse();
		}
		catch (SmppBindException e)
		{
			// Handle any issues when enquiring link
			handleBindStatus(e.getBindResponse().getCommandStatus(), session);
			throw e;
		}
		catch (Throwable throwable)
		{
			logger.error("Could not enquire link: {}", throwable);
			throw throwable;
		}
	}

	// Helper to get the SMSC configurations as an Array
	private SMSCConfig[] getSMSCConfigurations()
	{
		return config.getConfigurations().toArray(new SMSCConfig[config.getConfigurations().size()]);
	}

	// Get a specific SMSC configuration
	private SMSCConfig getSMSCConfiguration(long serialVersionUID)
	{
		// Iterate through the configurations
		for (IConfiguration smsc : config.getConfigurations())
		{
			if (smsc.getSerialVersionUID() == serialVersionUID)
			{
				return (SMSCConfig) smsc;
			}
		}

		return null;
	}

	// Easy way of doing a round robin list
	private  class RoundRobinList<T> extends LinkedList<T>
	{
		private static final long serialVersionUID = -6569158521395641834L;
		private Iterator<T> iterator;
		private final Object lock = new Object();
		private int iteratorPos;

		public synchronized T next()
		{
			synchronized (lock){
				if (iterator == null || !iterator.hasNext()) {
					iterator = iterator();
					iteratorPos = 0;
				}
				iteratorPos ++;
				return iterator.next();
			}

		}

		public synchronized boolean add(T t) {

			synchronized (lock){

				boolean addOutcome = super.add(t);

				/*
				  a new iterator is started each time an item is added. We then set the position
				  by finding the current position and setting this position in the new iterator
				 */
				iterator = iterator();
				// int currentIteratorPos = iteratorPos;
				for (int i=0 ; i < iteratorPos; i++){
                    // TODO - Pointless much? - shouldn't we uncomment the logger or completely get rid of this?
					iterator.next();
					//logger.info("Thread " + Thread.currentThread().getName() + " calling [[RESET]] CURR POS : (" + i + ") ands value: " + next);
				}

				return addOutcome;
			}
		}
	}


	// Gets unique SMSC configurations for sending SM's
	public SMSCConfig[] getUniqueSMSCConfigurations(String url)
	{
		// FIXME: This is not thread safe ...
		// Check that uniqueSMSCs map is initialised
		if (uniqueSMSCs == null)
		{
			logger.trace("SmppConnector.getUniqueSMSCConfigurations: initializing uniqueSMSCs");
			// Initialise the map
			uniqueSMSCs = new HashMap<String, SmppConnector.RoundRobinList<SMSCConfig>>();

			// Iterate through the SMSCs
			for (SMSCConfig smsc : getSMSCConfigurations())
			{
				// Ensure it is valid
				if (smsc == null)
				{
					logger.error("smsc is null");
					continue;
				}

				// Check the binding type
				if (smsc.smscBinding == SmsCBinding.NONE)
				{
					logger.trace("smscBinding is NONE");
					continue;
				}

				/*
				// SMPP connection may bind later ... only check this before using it.
				// Ensure the session is bound
				SmppSession session = sessions.get(smsc.getSerialVersionUID());
				if (session == null || !session.isBound())
				{
					this.log(LoggingLevels.TRACE, this, "session is not bound");
					continue;
				}
				 */

				// Check if it already contains the SMSC url
				if (uniqueSMSCs.containsKey(smsc.smscUrl))
				{
					logger.trace("SmppConnector.getUniqueSMSCConfigurations: Adding SMSC ({}) to existing url {}", smsc, smsc.smscUrl);
					uniqueSMSCs.get(smsc.smscUrl).add(smsc);

					continue;
				}

				// Create the round robin list for the url
				uniqueSMSCs.put(smsc.smscUrl, new RoundRobinList<SmppConnector.SMSCConfig>());

				// Add the smsc to the list
				logger.trace("SmppConnector.getUniqueSMSCConfigurations: Adding SMSC ({}) to new url {}", smsc, smsc.smscUrl);
				uniqueSMSCs.get(smsc.smscUrl).add(smsc);
			}
			logger.trace("SmppConnector.getUniqueSMSCConfigurations: uniqueSMSCs.length = {}", uniqueSMSCs.size());
		}

		// Check if url is found in the map
		if (url != null)
		{
			if (uniqueSMSCs.containsKey(url))
				return new SMSCConfig[] { uniqueSMSCs.get(url).next() };
			else
				return new SMSCConfig[] {};
		}

		// Create array
		SMSCConfig smscs[] = new SMSCConfig[uniqueSMSCs.size()];

		int index = 0;

		// Iterate through the unique URLs
		for (String key : uniqueSMSCs.keySet())
		{
			if (logger != null) logger.trace("SmppConnector.getUniqueSMSCConfigurations: getting next unique smsc for key {}", key);
			smscs[index++] = uniqueSMSCs.get(key).next();
			//this.log(LoggingLevels.TRACE, this, "SmppConnector.getUniqueSMSCConfigurations: got smsc = {}", smscs[index-1]);
		}

		// Return the list of unique addresses
		//this.log(LoggingLevels.TRACE, this, "SmppConnector.getUniqueSMSCConfigurations: smscs = {}", smscs);
		return smscs;
	}

	// Cancels a duplicate key from the session
	private void cancelSessionKey(SmppSession session, DuplicateKeyException duplicateException)
	{
		// FIXME ... this makes little sense and should not be done.
		try
		{
			// Get the key
			String duplicate = duplicateException.getMessage();
			int key = Integer.parseInt(duplicate.substring(duplicate.indexOf('[') + 1, duplicate.indexOf(']')));

			// Cancel the key from the window
			session.getSendWindow().cancel(key);
		}
		catch (Exception exception)
		{
			// Else cancel all expired
			logger.error("Failed to cancel session key on {} ({})", duplicateException, exception);
			logger.error(exception.getMessage(), exception);
			session.getSendWindow().cancelAllExpired();
		}
	}

	// Check if the failed sessions contains a particular SMSC configuration
	private boolean isFailedSession(long serialVersionUID)
	{
		for(SMSCConfig smsc: failedSessions)
		{
			if (smsc.getSerialVersionUID() == serialVersionUID)
				return true;
		}
		return false;
	}

	private static Random random = new Random(new Date().getTime());

	// Splits the message into multiple segments for Multi-Messaging
	private static byte[][] splitLongMessage(byte[] longMessage, int maximumSegmentSize)
	{
		final byte UDHIE_HEADER_LENGTH = 0x05;
		final byte UDHIE_IDENTIFIER_SAR = 0x00;
		final byte UDHIE_SAR_LENGTH = 0x03;

		// Determine number of segments
		int numSegments = longMessage.length / maximumSegmentSize;
		int messageLength = longMessage.length;
		if (numSegments > 255)
		{
			numSegments = 255;
			messageLength = numSegments * maximumSegmentSize;
		}
		if ((messageLength % maximumSegmentSize) > 0)
		{
			numSegments++;
		}
		byte[][] segments = new byte[numSegments][];

		int lengthOfData;

		// Generate a reference number
		byte[] referenceNumber = new byte[1];
		random.nextBytes(referenceNumber);

		for (int i = 0; i < numSegments; i++)
		{
			if (numSegments - i == 1)
			{
				lengthOfData = messageLength - i * maximumSegmentSize;
			}
			else
			{
				lengthOfData = maximumSegmentSize;
			}
			segments[i] = new byte[6 + lengthOfData];

			// UDH Header
			// Header Length
			segments[i][0] = UDHIE_HEADER_LENGTH;
			// SAR Identifier
			segments[i][1] = UDHIE_IDENTIFIER_SAR;
			// SAR Length
			segments[i][2] = UDHIE_SAR_LENGTH;
			// Reference Number (same for all messages)
			segments[i][3] = referenceNumber[0];
			// Total Number of Segments
			segments[i][4] = (byte) numSegments;
			// Segment Number
			segments[i][5] = (byte) (i + 1);
			// Copy the Data into the Array
			System.arraycopy(longMessage, (i * maximumSegmentSize), segments[i], 6, lengthOfData);
		}
		return segments;
	}

	// Handles the bind status
	private void handleBindStatus(int status, SmppSession session)
	{
		switch (status)
		{
		// Already bound to SMSC
		case SmppConstants.STATUS_ALYBND:

			logger.trace("Session ({}) is already bound to SMSC.", session);
			// Set the session to be bound
			session.setBound();

			break;

			// Invalid bind status
		case SmppConstants.STATUS_INVBNDSTS:

			// Unbind the session
			logger.trace("Session ({}) bind status is STATUS_INVBNDSTS - unbinding ...", session);
			unbind(session);
			break;

		default:
			break;
		}
	}

	// Add the requests and responses to the history buffer
	private void addHistory(Date created, IInteraction request, ISmsResponse response)
	{
		// Create the history object
		SmsHistory hist = new SmsHistory(created);
		hist.setRequest(request);
		hist.setResponse(response);

		// Add to the buffer
		synchronized (SmppConnector.history) 
		{
			history[currentIndex++] = hist;
			if (currentIndex > MAX_HISTORY - 1)
				currentIndex = 0;
		}	
	}

	// Helper method to get the next transaction ID
	private synchronized String getTransactionID()
	{
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		return String.format("%s%05d", sdf.format(new Date()), ++sequence);
	}

	// Removes funny characters from a message
	private String deAccent(String str)
	{
		String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}
}
