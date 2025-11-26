package hxc.connectors.hux;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.Number;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IConnector;
import hxc.connectors.soap.ISoapConnector;
import hxc.connectors.ussd.IPushUSSD;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.INotifications;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.utils.calendar.DateTime;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.notification.Notifications;
import hxc.utils.protocol.hsx.EncodingSelection;
import hxc.utils.protocol.hux.HandleUSSDRequest;
import hxc.utils.protocol.hux.HandleUSSDRequestMembers;
import hxc.utils.protocol.hux.HandleUSSDResponse;
import hxc.utils.protocol.hux.HandleUSSDResponseMembers;
import hxc.utils.protocol.hux.HandleUSSDResponseMembers.Actions;
import hxc.utils.protocol.hux.PingRequest;
import hxc.utils.protocol.hux.PingResponse;
import hxc.utils.protocol.hux.SendUSSDRequest;
import hxc.utils.protocol.hux.SendUSSDRequestMembers;
import hxc.utils.protocol.hux.SendUSSDResponse;
import hxc.utils.thread.TimedThread;
import hxc.utils.thread.TimedThread.TimedThreadType;
import hxc.utils.xmlrpc.XmlRpcClient;
import hxc.utils.xmlrpc.XmlRpcConnection;
import hxc.utils.xmlrpc.XmlRpcRequest;
import hxc.utils.xmlrpc.XmlRpcServer;

public class HuxConnector implements IConnector, IHuxConnector, IPushUSSD
{
	final static Logger logger = LoggerFactory.getLogger(HuxConnector.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////

	private IServiceBus esb;
	private XmlRpcServer server;
	private ISoapConnector soapConnector;
	private HuxConnector me = this;
	private Map<String, HuxProcessState> menuStates = new HashMap<String, HuxProcessState>();
	private TimedThread cleanupWorkerThread = null;

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
		// Get Soap Connector
		soapConnector = esb.getFirstConnector(ISoapConnector.class);
		if (soapConnector == null)
			return false;

		// Create the Server
		server = new XmlRpcServer(HandleUSSDRequest.class, PingRequest.class)
		{
			@Override
			protected void uponXmlRpcRequest(XmlRpcRequest request)
			{
				Object requestMethodCall = request.getMethodCall();

				if (requestMethodCall instanceof PingRequest)
				{
					PingRequest ping = (PingRequest) requestMethodCall;
					PingResponse response = new PingResponse();
					response.seq = ++ping.seq;

					try
					{
						request.respond(response);
					}
					catch (IOException e)
					{
						logger.error("Response exception: {}", e.getMessage());
					}

					return;
				}

				// De-serialise the Request
				HandleUSSDRequest handleUSSDRequest = (HandleUSSDRequest) requestMethodCall;

				HandleUSSDRequestMembers members = handleUSSDRequest.members;
				logger.debug("HuX Req From: {} *{}{} Seq: {} Session: {} Queue Size: {}", //
						members.MSISDN, //
						members.USSDServiceCode, //
						members.USSDRequestString, //
						members.Sequence == null ? "" : members.Sequence, //
						members.SessionId == null ? "" : members.SessionId,//
								menuStates.size());

				HuxConnection connection = new HuxConnection(me, request);

				// Test if this is a Request or a Reply
				boolean isReply = handleUSSDRequest.members.response != null && handleUSSDRequest.members.response;

				// This is a new Request
				HuxProcessState processState = null;
				if (!isReply)
				{
					// Create menu state
					processState = new HuxProcessState(esb.getLocale());
					processState.setSubscriberNumber(new Number(handleUSSDRequest.members.MSISDN));
					processState.setTransactionID(handleUSSDRequest.members.TransactionId);
					processState.setTransactionTime(handleUSSDRequest.members.TransactionTime);
					processState.setSessionID(handleUSSDRequest.members.SessionId == null ? 0 : handleUSSDRequest.members.SessionId);
					processState.setServiceCode(handleUSSDRequest.members.USSDServiceCode);
					processState.setRequestString(handleUSSDRequest.members.USSDRequestString);
					processState.setInput(handleUSSDRequest.members.USSDRequestString);
					processState.setIMSI(handleUSSDRequest.members.IMSI);
					processState.setVasInterface(soapConnector.getVasInterface());
					processState.setMaxOutputLength(config.maxMenuLength);
					processState.setSessionKey(getSessionKey(handleUSSDRequest.members));
					processState.setConnection(connection);
					String sessionKey = processState.getSessionKey();
					menuStates.put(sessionKey, processState);
					logger.debug("Session added to Session Store [{}]", sessionKey);
				}
				// Else, this is a response
				else
				{
					// Retrieve state
					String sessionKey = getSessionKey(handleUSSDRequest.members);
					processState = menuStates.get(sessionKey);

					// Handle non-existent session
					if (processState == null)
					{
						logger.error("Invalid/Expired USSD Session ID: {}", sessionKey);

						try
						{

							HandleUSSDResponse response = new HandleUSSDResponse();
							response.members = new HandleUSSDResponseMembers();
							response.members.TransactionId = handleUSSDRequest.members.TransactionId;
							response.members.TransactionTime = handleUSSDRequest.members.TransactionTime;
							response.members.ResponseCode = 0;
							response.members.USSDResponseString = notifications.get(invalidSession, esb.getLocale().getDefaultLanguageCode(), esb.getLocale(), null).getText();
							response.members.action = Actions.end;
							response.members.encodingSelection = new EncodingSelection[1];
							int languageID = esb.getLocale().getDefaultLanguageID();
							response.members.encodingSelection[0] = new EncodingSelection();
							response.members.encodingSelection[0].alphabet = esb.getLocale().getAlpabet(languageID);
							response.members.encodingSelection[0].language = esb.getLocale().getLanguage(languageID);

							request.respond(response);

						}
						catch (IOException e)
						{
							int stateSessionID = processState == null ? 0 : processState.getSessionID();
							logger.error("IOException on Session[{}] - cleanup", stateSessionID);
							menuStates.remove(Integer.toString(stateSessionID));
							logger.error("Session error", e);
							if (connection != null)
							{
								try
								{
									connection.close();
								}
								catch (IOException e1)
								{
									logger.error("Could not close connection[{}]", connection.toString());								
								}
							}
						}
						finally
						{
							try
							{
								if (connection != null)
								{
									logger.trace("close connection[{}]", connection.toString());
									connection.close();
								}
							}
							catch (IOException e)
							{
								int stateSessionID = processState.getSessionID();
								logger.error("IOException on Session[{}] - cleanup", stateSessionID);
								menuStates.remove(Integer.toString(stateSessionID));
							}
						}
						return;
					}

					// Update Menu State
					processState.setTransactionID(handleUSSDRequest.members.TransactionId);
					processState.setTransactionTime(handleUSSDRequest.members.TransactionTime);
					processState.setInput(handleUSSDRequest.members.USSDRequestString);

				}

				// Dispatch the Request
				logger.trace("Dispatching ussd request ...");
				int count = esb.dispatch(processState, connection);
				logger.trace("Dispatched ussd request ...");
				if (count == 0)
				{
					processState.reply(notifications.get(invalidCommand, esb.getLocale().getDefaultLanguageCode(), esb.getLocale(), null));
					logger.debug("Unhandled USSD Request {}{}'", handleUSSDRequest.members.USSDServiceCode, handleUSSDRequest.members.USSDRequestString);
				}

			}
		};

		// Start the Server
		try
		{
			server.start(config.serverPort, config.serverPath);
		}
		catch (IOException e)
		{
			logger.error("Hux Server cannot me started on path:{} port:{}", config.serverPath, config.serverPort);
			logger.error("Hux Server failed", e);
			return false;
		}

		// Create a State Cleanup Worker Thread
		cleanupWorkerThread = new TimedThread("HuX Cleanup Worker Thread", 10000, TimedThreadType.INTERVAL)
		{
			@Override
			public void action()
			{
				try
				{
					cleanupWorker();
				}
				catch (Exception e)
				{
					logger.error("HuX Cleanup Worker Thread", e);
				}
			}
		};
		cleanupWorkerThread.start();

		// Log Information
		logger.info("HuX Connector Started on :{}{}", config.serverPort, config.serverPath);

		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void stop()
	{
		// Stop the Server
		server.stop();
		server = null;

		// Stop the Worker Thread
		if (cleanupWorkerThread != null && cleanupWorkerThread.isAlive())
		{
			cleanupWorkerThread.kill();
			try
			{
				cleanupWorkerThread.join(1000);
			}
			catch (InterruptedException e)
			{
				//TODO: Fix use of deprecated stop function!
				cleanupWorkerThread.stop();
			}
			cleanupWorkerThread = null;
		}

		// Log Information
		logger.info("HuX Connector Stopped on :{}{}", config.serverPort, config.serverPath);
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config)
	{
		// Stop the Server
		server.stop();
		logger.info("HuX Connector Stopped on :{}{}", this.config.serverPort, this.config.serverPath);

		this.config = (HuxConfiguration) config;

		// Start the Server
		try
		{
			server.start(this.config.serverPort, this.config.serverPath);
		}
		catch (IOException e)
		{
			logger.error("Failed to start HUX", e);
		}

		// Log Information
		logger.info("HuX Connector Re-Started on :{}{}", this.config.serverPort, this.config.serverPath);

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
		if (server == null)
			return false;

		XmlRpcClient client = new XmlRpcClient(String.format("http://127.0.0.1:%d%s", config.serverPort, config.serverPath));
		try (XmlRpcConnection con = client.getConnection())
		{
			if (!con.isConnected())
				con.connect();

			PingRequest request = new PingRequest();
			request.seq = 99;

			PingResponse response = con.call(request, PingResponse.class);

			if (response == null || response.seq != 100)
				return false;

		}
		catch (Exception exc)
		{
			return false;
		}

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
	@Perms(perms = { @Perm(name = "ViewHuxParameters", description = "View HuX Connector Parameters", category = "HuX", supplier = true),
			@Perm(name = "ChangeHuxParameters", implies = "ViewHuxParameters", description = "Change HuX Connector Parameters", category = "HuX", supplier = true),
			@Perm(name = "ViewHuxNotifications", description = "View HuX Connector Notifications", category = "HuX", supplier = true),
			@Perm(name = "ChangeHuxNotifications", implies = "ViewHuxNotifications", description = "Change HuX Connector Notifications", category = "HuX", supplier = true), })
	public class HuxConfiguration extends ConfigurationBase
	{
		private int serverPort = 14000;
		private String serverPath = "/RPC2";
		private int sessionTimeOut_minutes = 3;
		private int maxMenuLength = 160;
		private String huxServerAddress = "http://localhost:4010/";

		public int getServerPort()
		{
			check(esb, "ViewHuxParameters");
			return serverPort;
		}

		public void setServerPort(int serverPort) throws ValidationException
		{
			check(esb, "ChangeHuxParameters");

			ValidationException.port(serverPort, "serverPort");
			this.serverPort = serverPort;
		}

		public String getServerPath()
		{
			check(esb, "ViewHuxParameters");
			return serverPath;
		}

		public void setServerPath(String serverPath) throws ValidationException
		{
			check(esb, "ChangeHuxParameters");

			if (serverPath.indexOf('/') != 0)
				throw new ValidationException("Server path requires a '/' as first character.");

			this.serverPath = serverPath;
		}

		public int getSessionTimeOut_minutes()
		{
			check(esb, "ViewHuxParameters");
			return sessionTimeOut_minutes;
		}

		public void setSessionTimeOut_minutes(int sessionTimeOut_minutes) throws ValidationException
		{
			check(esb, "ChangeHuxParameters");
			ValidationException.inRange(1, sessionTimeOut_minutes, 30, "sessionTimeOutMinutes");
			this.sessionTimeOut_minutes = sessionTimeOut_minutes;
		}

		public int getMaxMenuLength()
		{
			check(esb, "ViewHuxParameters");
			return maxMenuLength;
		}

		public void setMaxMenuLength(int maxMenuLength) throws ValidationException
		{
			check(esb, "ChangeHuxParameters");
			ValidationException.inRange(80, maxMenuLength, 182, "MaxMenuLength");
			this.maxMenuLength = maxMenuLength;
		}

		public String getHuxServerAddress()
		{
			check(esb, "ViewHuxParameters");
			return huxServerAddress;
		}

		public void setHuxServerAddress(String huxServerAddress)
		{
			check(esb, "ChangeHuxParameters");
			this.huxServerAddress = huxServerAddress;
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
			return 4738008229488107656L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "HuX Service";
		}

		@Override
		public void validate() throws ValidationException
		{

		}

		@Override
		public void performUpdateNotificationSecurityCheck()
		{
			check(esb, "ChangeHuxNotifications");
		}

		@Override
		public void performGetNotificationSecurityCheck()
		{
			check(esb, "ViewHuxNotifications");
		}

	};

	HuxConfiguration config = new HuxConfiguration();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Notifications
	//
	// /////////////////////////////////
	Notifications notifications = new Notifications(null);

	private final int invalidCommand = notifications.add("Invalid Command", "Invalid Command", "FR: Invalid Command");

	private final int invalidSession = notifications.add("Invalid/Expired Session ID", "Invalid/Expired Session ID", "FR: Invalid/Expired");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Cleanup Thread
	//
	// /////////////////////////////////
	private void cleanupWorker()
	{
		try
		{
			DateTime cutOffTime = DateTime.getNow().addMinutes(-config.sessionTimeOut_minutes);
			HuxProcessState[] states = menuStates.values().toArray(new HuxProcessState[menuStates.size()]);
			for (HuxProcessState state : states)
			{
				if (state.getLastInteractionTime().before(cutOffTime))
				{
					SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");					
					String lastInteractionTime = dt.format(state.getLastInteractionTime()); 
					String formattedCutoffTime = cutOffTime.toString("yyyy-MM-dd HH:mm:ss.SSS");
					String sessionID = Integer.toString(state.getSessionID());
					logger.debug("Removing expired session [{}], cutoff time [{}], last interaction time [{}], MSISDN [{}]", sessionID, formattedCutoffTime, lastInteractionTime, state.getMSISDN());
					menuStates.remove(sessionID);
				}
			}
		}
		catch (Exception e)
		{
			return;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IPushUSSD
	//
	// /////////////////////////////////
	@Override
	public String pushUSSD(String transactionID, String msisdn, String serviceCode, String languageCode, String text, boolean notifyOnly) throws Exception
	{
		// Create HuX/USSD SendMessage Request
		SendUSSDRequest req = new SendUSSDRequest();
		req.members = new SendUSSDRequestMembers();
		req.members.TransactionId = transactionID;
		req.members.MSISDN = msisdn;
		req.members.USSDServiceCode = serviceCode;
		req.members.USSDRequestString = text;
		req.members.encodingSelection = null;
		if (languageCode != null && languageCode.length() > 0)
		{
			req.members.encodingSelection = new EncodingSelection[1];
			req.members.encodingSelection[0] = new EncodingSelection();
			req.members.encodingSelection[0].alphabet = esb.getLocale().getAlpabet(languageCode);
			req.members.encodingSelection[0].language = languageCode;
		}
		req.members.action = notifyOnly ? SendUSSDRequestMembers.ACTION_NOTIFY : SendUSSDRequestMembers.ACTION_REQUEST;
		req.members.notifyResponseMode = null;

		// Perform the Push USSD Call
		XmlRpcClient client = new XmlRpcClient(config.huxServerAddress);

		// Prompt the user until he gives a valid USSD response
		try (XmlRpcConnection connection = client.getConnection())
		{
			SendUSSDResponse ussdResp = connection.call(req, SendUSSDResponse.class);
			return ussdResp.members.USSDResponseString;
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public void display(XmlRpcRequest rpcRequest, HuxProcessState huxProcessState)
	{
		// Defensive
		if (huxProcessState == null)
		{
			logger.error("HuxConnector::display - got Null menu");
			return;
		}

		// Construct a reply
		HandleUSSDResponse response = new HandleUSSDResponse();
		response.members = new HandleUSSDResponseMembers();
		response.members.TransactionId = huxProcessState.getTransactionID();
		response.members.TransactionTime = huxProcessState.getOriginTimeStamp();
		response.members.ResponseCode = 0;
		response.members.USSDResponseString = huxProcessState.getOutput();
		response.members.action = huxProcessState.isCompleted() ? Actions.end : Actions.request;
		response.members.encodingSelection = new EncodingSelection[1];
		int languageID = huxProcessState.getLanguageID();
		response.members.encodingSelection[0] = new EncodingSelection();
		response.members.encodingSelection[0].alphabet = huxProcessState.getLocale().getAlpabet(languageID);
		response.members.encodingSelection[0].language = huxProcessState.getLocale().getLanguage(languageID);
		try
		{
			rpcRequest.respond(response);
		}
		catch (IOException e)
		{
			int stateSessionID = huxProcessState.getSessionID();
			logger.error("IOException on Session[{}] - clean up", stateSessionID);
			menuStates.remove(Integer.toString(stateSessionID));
			try
			{
				huxProcessState.getConnection().close();
			}
			catch (IOException e1)
			{
					logger.error("Could not close connection");								
			}
		}

		// Remove State
		if (huxProcessState.isCompleted())
		{
			logger.info("Removing USSD session [{}], MSISDN [{}]", huxProcessState.getSessionKey(), huxProcessState.getMSISDN());
			menuStates.remove(huxProcessState.getSessionKey());			
		} else {
			huxProcessState.setLastInteractionTime(new Date());
		}

	}

	protected String getSessionKey(HandleUSSDRequestMembers members)
	{
		String result = members.MSISDN;
		if (members.SessionId != null)
			result += members.SessionId.toString();
		return result;
	}

}
