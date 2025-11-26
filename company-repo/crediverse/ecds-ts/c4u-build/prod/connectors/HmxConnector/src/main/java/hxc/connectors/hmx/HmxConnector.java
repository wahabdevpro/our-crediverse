package hxc.connectors.hmx;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IConnector;
import hxc.connectors.hlr.IHlrConnector;
import hxc.connectors.hlr.IHlrInformation;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.INotifications;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.notification.Notifications;
import hxc.utils.protocol.hmx.CellGlobalId;
import hxc.utils.protocol.hmx.GetSubscriberInformationMembers;
import hxc.utils.protocol.hmx.GetSubscriberInformationParameters;
import hxc.utils.protocol.hmx.GetSubscriberInformationRequest;
import hxc.utils.protocol.hmx.GetSubscriberInformationResponse;
import hxc.utils.protocol.hmx.GetSubscriberInformationResponseParameters;
import hxc.utils.protocol.hsx.Number.NumberPlan;
import hxc.utils.protocol.hsx.Number.NumberType;
import hxc.utils.xmlrpc.XmlRpcClient;
import hxc.utils.xmlrpc.XmlRpcConnection;

public class HmxConnector implements IConnector, IHlrConnector
{
	final static Logger logger = LoggerFactory.getLogger(HmxConnector.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private static String hostName = null;
	private static AtomicInteger transactionId = new AtomicInteger(1000);

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
		// TODO AdB more initialisation

		logger.info("Hmx Connector Started");

		return true;
	}

	@Override
	public void stop()
	{
		// Log Information
		logger.info("Hmx Connector Stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{

		this.config = (HmxConfiguration) config;

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
		// GG Implement Fitness Test
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
	@Perms(perms = { @Perm(name = "ViewHmxParameters", description = "View Hmx Connector Parameters", category = "Hmx", supplier = true),
			@Perm(name = "ChangeHmxParameters", implies = "ViewHmxParameters", description = "Change Hmx Connector Parameters", category = "Hmx", supplier = true),
			@Perm(name = "ViewHmxNotifications", description = "View Hmx Connector Notifications", category = "Hmx", supplier = true),
			@Perm(name = "ChangeHmxNotifications", implies = "ViewHmxNotifications", description = "Change Hmx Connector Notifications", category = "Hmx", supplier = true) })
	public class HmxConfiguration extends ConfigurationBase
	{

		private String serverPath = "http://localhost:10010/Air";

		private boolean requestState = true;
		private NumberType numberType = NumberType.INTERNATIONAL;
		private NumberPlan numberPlan = NumberPlan.ISDN;
		private int connectTimeout_ms = 10000;
		private int readTimeout_ms = 10000;

		public String getServerPath()
		{
			check(esb, "ViewHmxParameters");
			return serverPath;
		}

		public void setServerPath(String serverPath) throws ValidationException
		{
			check(esb, "ChangeHmxParameters");
			this.serverPath = serverPath;
		}

		public boolean getRequestState()
		{
			check(esb, "ViewHmxParameters");
			return this.requestState;
		}

		public void setRequestState( boolean requestState )
		{
			check(esb, "ChangeHmxParameters");
			this.requestState = requestState;
		}

		public NumberType getNumberType()
		{
			check(esb, "ViewHmxParameters");
			return this.numberType;
		}

		public void setNumberType( NumberType numberType )
		{
			check(esb, "ChangeHmxParameters");
			this.numberType = numberType;
		}

		public NumberPlan getNumberPlan()
		{
			check(esb, "ViewHmxParameters");
			return this.numberPlan;
		}

		public void setNumberPlan( NumberPlan numberPlan )
		{
			check(esb, "ChangeHmxParameters");
			this.numberPlan = numberPlan;
		}
				
		public int getConnectTimeout_ms()
		{
			check(esb, "ViewHmxParameters");
			return connectTimeout_ms;
		}

		public void setConnectTimeout_ms(int connectTimeout_ms)
		{
			check(esb, "ChangeHmxParameters");
			this.connectTimeout_ms = connectTimeout_ms;
		}

		public int getReadTimeout_ms()
		{
			check(esb, "ViewHmxParameters");
			return readTimeout_ms;
		}

		public void setReadTimeout_ms(int readTimeout_ms)
		{
			check(esb, "ChangeHmxParameters");
			this.readTimeout_ms = readTimeout_ms;
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
			return 9149607749808891088L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "Hmx Connector";
		}

		@Override
		public void validate() throws ValidationException
		{

		}

		@Override
		public void performUpdateNotificationSecurityCheck()
		{
		}

		@Override
		public void performGetNotificationSecurityCheck()
		{
		}

	};

	HmxConfiguration config = new HmxConfiguration();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Notifications
	//
	// /////////////////////////////////
	Notifications notifications = new Notifications(null);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IHlrConnector
	//
	// /////////////////////////////////
	@Override
	public IHlrInformation getInformation(String msisdn, boolean needLocation, boolean needMnp, boolean needImsi)
	{
        long start = System.nanoTime();
		logger.info("Enter HmxConnector.getInformation() method.");
		GetSubscriberInformationRequest req = new GetSubscriberInformationRequest();
		GetSubscriberInformationMembers member = req.member = new GetSubscriberInformationMembers();

		hxc.utils.protocol.hsx.RequestHeader header = member.requestHeader = new hxc.utils.protocol.hsx.RequestHeader();
		header.requestingUser = "HxC";
		header.originSystemType = "HxC";
		
		if (hostName == null)
		{
			try
			{
				hostName = InetAddress.getLocalHost().getHostName().toLowerCase();
				int firstDotPosition = hostName.indexOf('.');
				if (firstDotPosition > 0)
				{
					hostName = hostName.substring(0, firstDotPosition);
				}
			}
			catch (UnknownHostException e)
			{
				hostName = "localhost";
			}
		}
		
		
		header.originHostName = hostName; 
		header.originServiceName = "ECDS";
		header.originTransactionId = String.format("%08d", transactionId.getAndIncrement());
		header.originTimeStamp = new Date();
		header.originOperatorId = "HxC";
		header.sessionId = null;
		header.mode = null;
		header.version = "1.0";

		GetSubscriberInformationParameters params = member.requestParameters = new GetSubscriberInformationParameters();
		hxc.utils.protocol.hsx.Number number = params.subscriberNumber = new hxc.utils.protocol.hsx.Number();
		number.addressDigits = msisdn;
		
		number.numberType = config.getNumberType();
		number.numberPlan = config.getNumberPlan();
		params.domain = 0;
		params.requestState = config.getRequestState();
		
		 // If true it will require ATI. Not applicable in case only IMSI is requested
		if (needLocation || needMnp || needImsi)
			params.requestState = true;
		
		params.requestBasicLocation = needLocation;
		params.requestMnpStatus = needMnp;
		params.requestImsi = needImsi;

		XmlRpcClient client = new XmlRpcClient(config.serverPath, config.connectTimeout_ms, config.readTimeout_ms);
		try (XmlRpcConnection connection = client.getConnection())
		{
			GetSubscriberInformationResponse response = connection.call(req, GetSubscriberInformationResponse.class);
			if (response != null)
			{
				GetSubscriberInformationResponseParameters parms = response.members.responseParameters;
				if (parms != null)
				{
					HlrInformation info = new HlrInformation();
					if (needMnp && parms.mnp != null)
						info.setMnpStatus(parms.mnp.mnpStatusId);
					if (needImsi && parms.imsi != null)
						info.setIMSI(parms.imsi.imsi);
					if (needLocation && parms.basicLocation != null && parms.basicLocation.cellGlobalId != null)
					{
						CellGlobalId cell = parms.basicLocation.cellGlobalId;
						info.setCellIdentity(cell.cellIdentity);
						info.setLocationAreaCode(cell.locationAreaCode);
						info.setMobileCountryCode(cell.mobileCountryCode);
						info.setMobileNetworkCode(cell.mobileNetworkCode);
					}
					logger.info("Exit HmxConnector.getInformation() with some info, " + executionTime(start));
					return info;
				}
			}
			}
		catch (Throwable e) 	
		{
				logger.error("getInformation failed", e);
		}
	

		logger.info("Exit HmxConnector.getInformation() returning null, " + executionTime(start));
		return null;

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

    public static String executionTime(long start) {
        long time = System.nanoTime() - start;
        long seconds = time / 1_000_000_000 ;
        long ms = Math.round((time - (seconds * 1_000_000_000)) / 1_000_000);
        return "execution time: " + (seconds == 0 ? "" : seconds + " s. ") + ms + " ms.";
    }
}
