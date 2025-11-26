package hxc.connectors.hsx;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IConnector;
import hxc.connectors.smpp.SmppAddress;
import hxc.connectors.smpp.SmppNpi;
import hxc.connectors.smpp.SmppTon;
import hxc.connectors.smpp.SmsHistory;
import hxc.connectors.smpp.SmsRequest;
import hxc.connectors.smpp.SmsResponse;
import hxc.connectors.sms.ISmsConnector;
import hxc.connectors.sms.ISmsHistory;
import hxc.connectors.sms.ISmsResponse;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.INotificationText;
import hxc.services.notification.INotifications;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.notification.Notifications;
import hxc.utils.protocol.hsx.DeliverSMRequest;
import hxc.utils.protocol.hsx.DeliverSMResponse;
import hxc.utils.protocol.hsx.DeliverSMResponseMembers;
import hxc.utils.protocol.hsx.DeliverSMResponseParameters;
import hxc.utils.protocol.hsx.EncodingSelection;
import hxc.utils.protocol.hsx.PingRequest;
import hxc.utils.protocol.hsx.PingResponse;
import hxc.utils.protocol.hsx.RequestHeader;
import hxc.utils.protocol.hsx.ResponseHeader;
import hxc.utils.protocol.hsx.SubmitSMRequest;
import hxc.utils.protocol.hsx.SubmitSMRequestMembers;
import hxc.utils.protocol.hsx.SubmitSMRequestParameters;
import hxc.utils.protocol.hsx.SubmitSMResponse;
import hxc.utils.xmlrpc.XmlRpcClient;
import hxc.utils.xmlrpc.XmlRpcConnection;
import hxc.utils.xmlrpc.XmlRpcRequest;
import hxc.utils.xmlrpc.XmlRpcServer;

public class HsxConnector implements IConnector, ISmsConnector
{
	final static Logger logger = LoggerFactory.getLogger(HsxConnector.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private XmlRpcServer server;
	private XmlRpcClient client;
	private static int sequence = 0;
	private HsxConnector me = this;
	private int MAX_HISTORY = 2;
	private SmsHistory history[] = new SmsHistory[MAX_HISTORY];
	private int currentIndex = 0;

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
		// Create the server
		createServer();

		// Create Client
		client = new XmlRpcClient(config.gatewayUrl);

		// Start the server
		try
		{
			server.start(config.serverPort, config.serverPath);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}

		// Log Information
		// logger.info(this, "HsX Connector Started on :%d%s", config.serverPort, config.serverPath);

		return true;
	}

	/**
	 * Create server was extracted from start
	 */
	private void createServer()
	{
		server = new XmlRpcServer(DeliverSMRequest.class, PingRequest.class)
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
					catch (IOException exc)
					{

					}

					return;
				}

				// Dispatch
				DeliverSMRequest deliverSMRequest = (DeliverSMRequest) requestMethodCall;
				HsxConnection connection = new HsxConnection();

				SmsRequest hsxRequest = new SmsRequest( //
						deliverSMRequest.requestMembers.requestParameters.sourceNumber.addressDigits, //
						deliverSMRequest.requestMembers.requestParameters.destinationNumber.addressDigits, //
						deliverSMRequest.requestMembers.requestParameters.message, //
						deliverSMRequest.requestMembers.requestHeader.originTransactionId, //
						deliverSMRequest.requestMembers.requestHeader.originTimeStamp, //
						me);
				logger.trace("Received SM '{}' from {}", deliverSMRequest.requestMembers.requestParameters.message, hsxRequest.getMSISDN());

				int count = esb.dispatch(hsxRequest, connection);

				// Respond
				DeliverSMResponse deliverSMResponse = new DeliverSMResponse();
				deliverSMResponse.responseMembers = new DeliverSMResponseMembers();
				deliverSMResponse.responseMembers.responseHeader = new ResponseHeader();
				deliverSMResponse.responseMembers.responseHeader.originTransactionId = deliverSMRequest.requestMembers.requestHeader.originTransactionId;
				deliverSMResponse.responseMembers.responseHeader.originOperatorId = deliverSMRequest.requestMembers.requestHeader.originOperatorId;
				deliverSMResponse.responseMembers.responseHeader.setResponseCode(ResponseHeader.COMPLETE_SUCCESS, ResponseHeader.COMPONENT_BASE, ResponseHeader.REASON_SUCCESS);
				deliverSMResponse.responseMembers.responseHeader.responseMessage = "OK";
				deliverSMResponse.responseMembers.responseParameters = new DeliverSMResponseParameters();
				deliverSMResponse.responseMembers.responseParameters.messageId = deliverSMRequest.requestMembers.requestParameters.messageId;
				try
				{
					request.respond(deliverSMResponse);
				}
				catch (IOException e)
				{
					logger.info(e.getMessage(), e);
				}

				if (count == 0)
				{
					send(hsxRequest.getShortCode(), hsxRequest.getMSISDN(), notifications.get(InvalidCommand, esb.getLocale().getDefaultLanguageCode(), esb.getLocale(), null));
					logger.trace("Invalid SMS Command '{}' on {}", deliverSMRequest.requestMembers.requestParameters.message, hsxRequest.getShortCode());
				}

			}
		};
	}

	@Override
	public void stop()
	{
		// Stop the Server
		server.stop();
		server = null;

		// Log Information
		logger.info("HsX Connector Stopped on :{}{}", config.serverPort, config.serverPath);
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		// Stop the Server (+ log)
		if (server != null)
		{
			stop();
		}

		// Log Information
		logger.info("HsX Connector Stopped on :{}{}", this.config.serverPort, this.config.serverPath);

		this.config = (HsxConfiguration) config;

		// Create Client
		client = new XmlRpcClient(this.config.gatewayUrl);

		// Start the server
		try
		{
			createServer();
			server.start(this.config.serverPort, this.config.serverPath);
		}
		catch (IOException e)
		{
			logger.error("Failed to start servers", e);
		}

		// Log Information
		logger.info("HsX Connector Started on :{}{}", this.config.serverPort, this.config.serverPath);

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
		if (server == null || client == null)
		{
			return false;
		}

		XmlRpcClient c = new XmlRpcClient(String.format("http://127.0.0.1:%s%s", config.serverPort, config.serverPath));
		try (XmlRpcConnection con = c.getConnection())
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
	@Perms(perms = { @Perm(name = "ViewHsxParameters", description = "View HsX Connector Parameters", category = "HsX", supplier = true),
			@Perm(name = "ChangeHsxParameters", implies = "ViewHsxParameters", description = "Change HsX Connector Parameters", category = "HsX", supplier = true),
			@Perm(name = "ViewHsxNotifications", description = "View HsX Connector Notifications", category = "HsX", supplier = true),
			@Perm(name = "ChangeHsxNotifications", implies = "ViewHsxNotifications", description = "Change HsX Connector Notifications", category = "HsX", supplier = true) })
	public class HsxConfiguration extends ConfigurationBase
	{

		private int serverPort = 12000;
		private String serverPath = "/RPC2";
		private String gatewayUrl = "http://127.0.0.1:21000/SMS";

		public int getServerPort()
		{
			check(esb, "ViewHsxParameters");
			return serverPort;
		}

		public void setServerPort(int serverPort) throws ValidationException
		{
			check(esb, "ChangeHsxParameters");

			ValidationException.port(serverPort, "serverPort");
			this.serverPort = serverPort;
		}

		public String getServerPath()
		{
			check(esb, "ViewHsxParameters");
			return serverPath;
		}

		public void setServerPath(String serverPath) throws ValidationException
		{
			check(esb, "ChangeHsxParameters");

			if (serverPath.indexOf('/') != 0)
				throw new ValidationException("Server path requires a '/' as first character.");

			this.serverPath = serverPath;
		}

		public String getGatewayUrl()
		{
			check(esb, "ViewHsxParameters");
			return gatewayUrl;
		}

		public void setGatewayUrl(String gatewayUrl) throws ValidationException
		{
			check(esb, "ChangeHsxParameters");

			ValidationException.validateURL(gatewayUrl, "GatewayUrl");
			this.gatewayUrl = gatewayUrl;
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
			return -7402508077777737740L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "HsX Connector";
		}

		@Override
		public void validate() throws ValidationException
		{

		}

		@Override
		public void performUpdateNotificationSecurityCheck()
		{
			check(esb, "ViewHsxNotifications");
		}

		@Override
		public void performGetNotificationSecurityCheck()
		{
			check(esb, "ChangeHsxNotifications");
		}

	};

	HsxConfiguration config = new HsxConfiguration();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Notifications
	//
	// /////////////////////////////////
	Notifications notifications = new Notifications(null);
	private final int InvalidCommand = notifications.add("Invalid Command", "Invalid Command");

	// Helpers

	private static hxc.utils.protocol.hsx.Number.NumberType convertType( SmppTon ton )
	{
		switch( ton )
		{
			case UNKNOWN:
				return hxc.utils.protocol.hsx.Number.NumberType.UNKNOWN;
			case INTERNATIONAL:
				return hxc.utils.protocol.hsx.Number.NumberType.INTERNATIONAL;
			case NATIONAL:
				return hxc.utils.protocol.hsx.Number.NumberType.NATIONAL;
			case NETWORK_SPECIFIC:
				return hxc.utils.protocol.hsx.Number.NumberType.NETWORKSPECIFIC;
			case SUBSCRIBER_NUMBER:
				return hxc.utils.protocol.hsx.Number.NumberType.SUBSCRIBER;
			case ALPHANUMERIC:
				return hxc.utils.protocol.hsx.Number.NumberType.ALPHANUMERIC;
			case ABBREVIATED:
				return hxc.utils.protocol.hsx.Number.NumberType.ABBREVIATED;
		}
		return hxc.utils.protocol.hsx.Number.NumberType.UNKNOWN;
	}

	private static hxc.utils.protocol.hsx.Number.NumberPlan convertPlan( SmppNpi npi )
	{
		switch( npi )
		{
			case UNKNOWN:
				return hxc.utils.protocol.hsx.Number.NumberPlan.UNKNOWN;
			case ISDN:
				return hxc.utils.protocol.hsx.Number.NumberPlan.ISDN;
			case DATA:
				return hxc.utils.protocol.hsx.Number.NumberPlan.DATA;
			case TELEX:
				return hxc.utils.protocol.hsx.Number.NumberPlan.TELEX;
			case LAND_MOBILE:
				return hxc.utils.protocol.hsx.Number.NumberPlan.UNKNOWN;
			case NATIONAL:
				return hxc.utils.protocol.hsx.Number.NumberPlan.NATIONAL;
			case PRIVATE:
				return hxc.utils.protocol.hsx.Number.NumberPlan.PRIVATE;
			case ERMES:
				return hxc.utils.protocol.hsx.Number.NumberPlan.ERMES;
			case INTERNET:
				return hxc.utils.protocol.hsx.Number.NumberPlan.UNKNOWN;
			case WAP:
				return hxc.utils.protocol.hsx.Number.NumberPlan.UNKNOWN;
		}
		return hxc.utils.protocol.hsx.Number.NumberPlan.UNKNOWN;
	}

	private static hxc.utils.protocol.hsx.Number convertAddress( SmppAddress address )
	{
		hxc.utils.protocol.hsx.Number number = new hxc.utils.protocol.hsx.Number();
		number.addressDigits = address.getAddress();
		number.numberType = ( address.getType() == null ? hxc.utils.protocol.hsx.Number.NumberType.UNKNOWN : convertType( address.getType() ) );
		number.numberPlan = ( address.getPlan() == null ? hxc.utils.protocol.hsx.Number.NumberPlan.UNKNOWN : convertPlan( address.getPlan() ) );
		return number;
	}

	private static SmppAddress createAddress( String msisdn )
	{
		return new SmppAddress( msisdn, null, null );
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@Override
	public void send(String fromMSISDN, String toMSISDN, INotificationText notificationText)
	{
		send(createAddress(fromMSISDN), createAddress(toMSISDN), notificationText, false);
	}

	@Override
	public boolean send(String fromMSISDN, String toMSISDN, INotificationText notificationText, boolean synchronise)
	{
		return send(createAddress(fromMSISDN), createAddress(toMSISDN), notificationText, synchronise);
	}

	@Override
	public ISmsResponse sendRequest(String fromMSISDN, String toMSISDN, INotificationText notificationText)
	{
		return sendRequest(createAddress(fromMSISDN), createAddress(toMSISDN), notificationText);
	}

	@Override
	public void send(final SmppAddress fromAddress, final SmppAddress toAddress, final INotificationText notificationText)
	{
		new Thread()
		{
			@Override
			public void run()
			{

				send(fromAddress, toAddress, notificationText, true);

			};
		}.start();
	}

	@Override
	public boolean send(SmppAddress fromAddress, SmppAddress toAddress, INotificationText notificationText, boolean synchronise)
	{
		if (synchronise)
		{
			return sendRequest(fromAddress, toAddress, notificationText) != null;
		}
		send(fromAddress, toAddress, notificationText);
		return true;
	}


	@Override
	public ISmsResponse sendRequest(SmppAddress fromAddress, SmppAddress toAddress, INotificationText notificationText)
	{
		// TODO Parameterise properly
		SubmitSMRequest request = new SubmitSMRequest();
		request.requestMembers = new SubmitSMRequestMembers();
		request.requestMembers.requestHeader = new RequestHeader();
		request.requestMembers.requestHeader.originSystemType = "hxc";
		request.requestMembers.requestHeader.originHostName = "adb1";
		request.requestMembers.requestHeader.originServiceName = "fundtx"; // ??
		request.requestMembers.requestHeader.originTransactionId = getTransactionID();
		request.requestMembers.requestHeader.originTimeStamp = new Date();
		request.requestMembers.requestHeader.originOperatorId = "750001";
		request.requestMembers.requestHeader.version = "1.0";
		request.requestMembers.requestParameters = new SubmitSMRequestParameters();
		request.requestMembers.requestParameters.destinationNumber = convertAddress( toAddress );
		request.requestMembers.requestParameters.sourceNumber = convertAddress( fromAddress );
		request.requestMembers.requestParameters.message = notificationText.getText();
		request.requestMembers.requestParameters.encodingSelection = new EncodingSelection[1];
		request.requestMembers.requestParameters.encodingSelection[0] = new EncodingSelection();
		request.requestMembers.requestParameters.encodingSelection[0].alphabet = esb.getLocale().getAlpabet(notificationText.getLanguageCode());
		request.requestMembers.requestParameters.encodingSelection[0].language = esb.getLocale().getEncodingScheme(notificationText.getLanguageCode());

		history[currentIndex] = new SmsHistory(new Date());
		history[currentIndex].setRequest( //
				new SmsRequest(fromAddress.getAddress(), //
						toAddress.getAddress(), //
						notificationText.getText(), //
						request.requestMembers.requestHeader.originTransactionId, //
						request.requestMembers.requestHeader.originTimeStamp, //
						me));

		SubmitSMResponse response = null;
		try (XmlRpcConnection connection = client.getConnection())
		{
			response = connection.call(request, SubmitSMResponse.class);
			logger.info("Sent SM from {} to {}: {}", fromAddress.getAddress(), toAddress.getAddress(), request.requestMembers.requestParameters.message);
		}
		catch (Exception e)
		{
			logger.info(e.getMessage(), e);
			return null;
		}

		ISmsResponse smsResponse = new SmsResponse();
		smsResponse.setMessageID(response.responseMembers.responseParameters.messageId);
		smsResponse.setResultMessage(response.responseMembers.responseHeader.responseMessage);
		try
		{
			smsResponse.setSequenceNumber(Integer.parseInt(response.responseMembers.responseHeader.originTransactionId));
		}
		catch (NumberFormatException exc)
		{
			smsResponse.setSequenceNumber(-1);
		}

		history[currentIndex].setResponse(smsResponse);

		if (++currentIndex >= MAX_HISTORY)
			currentIndex = 0;

		return smsResponse;
	}

	@Override
	public ISmsHistory[] getSmsHistory()
	{
		if (history.length > 1)
			Arrays.sort(history, new Comparator<SmsHistory>()
			{
				@Override
				public int compare(SmsHistory o1, SmsHistory o2)
				{
					if (o1 == null || o2 == null)
						return 1;
					return o1.getDate().compareTo(o2.getDate()) * -1;
				}
			});
		return history;
	}

	@Override
	public void clearHistory()
	{
		for (int i = 0; i < history.length; i++)
		{
			history[i] = null;
		}
		currentIndex = 0;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	private synchronized String getTransactionID()
	{
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		return String.format("%s%05d", sdf.format(new Date()), ++sequence);
	}

}
