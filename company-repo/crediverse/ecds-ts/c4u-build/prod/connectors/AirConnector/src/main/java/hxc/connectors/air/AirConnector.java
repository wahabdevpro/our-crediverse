package hxc.connectors.air;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.air.proxy.Subscriber;
import hxc.servicebus.IServiceBus;
import hxc.services.logging.LoggingConstants;
import hxc.services.notification.INotifications;
import hxc.services.numberplan.INumberPlan;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.transactions.ITransaction;
import hxc.utils.calendar.DateTime;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IDimension;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.instrumentation.Metric;

public class AirConnector implements IAirConnector
{
	final static Logger logger = LoggerFactory.getLogger(AirConnector.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private List<AirConnection> airConnections = null;

	private Metric airCallsMetric = Metric.CreateBasedOnProtocol("Air Calls", 60000, "Call(s)", IAirConnection.class, "getAddress", "close") //
			.prioritise("getaccountdetails", "getbalanceanddate", "getoffers", "updateaccountdetails", "updatebalanceanddate", "updateoffer");
	private LinkedHashMap<String, AtomicLong> airCallsCounter;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Language Map
	//
	// /////////////////////////////////
	private static Map<String, Locale> localeMap;

	static
	{
		String[] languages = Locale.getISOLanguages();
		localeMap = new HashMap<String, Locale>(languages.length);
		for (String language : languages)
		{
			Locale locale = new Locale(language);
			localeMap.put(locale.getISO3Language(), locale);
			
			// Historical
			if ("fr".equals(language))
				localeMap.put("fre", locale);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configurable Properties
	//
	// /////////////////////////////////
	@Perms(perms = { @Perm(name = "ViewAirParameters", description = "View Air Parameters", category = "Air", supplier = true),
			@Perm(name = "ChangeAirParameters", implies = "ViewAirParameters", description = "Change Air Parameters", category = "Air", supplier = true) })
	public class AirConnectorConfig extends ConfigurationBase
	{
		private AirConnectionConfig[] airConnectionConfigs = new AirConnectionConfig[] { //
				new AirConnectionConfig("AIR1", 6204927056154386309L, "http://127.0.0.1:10010/Air", "hxcuser/4.2/1.0", "hxcuser", "hxcuser", 1000, 30000), //
				new AirConnectionConfig("AIR2", 3963119645709750291L), //
				new AirConnectionConfig("AIR3", -5923903413150645870L), //
				new AirConnectionConfig("AIR4", 2080930020101758202L), //
		};

		private String defaultOriginNodeType = "EXT";
		private String defaultOriginOperatorID = "HXC";
		private String defaultCurrency = "USD";
		private int currencyDecimalDigits = 2;
		private int defaultSubscriberNumberNAI = 1;
		private boolean logRequests = true;
		private boolean logResponses = true;

		public String getDefaultOriginNodeType()
		{
			check(esb, "ViewAirParameters");
			return defaultOriginNodeType;
		}

		public void setDefaultOriginNodeType(String defaultOriginNodeType) throws ValidationException
		{
			check(esb, "ChangeAirParameters");
			this.defaultOriginNodeType = defaultOriginNodeType;
		}

		public String getDefaultOriginOperatorID()
		{
			check(esb, "ViewAirParameters");
			return defaultOriginOperatorID;
		}

		public void setDefaultOriginOperatorID(String defaultOriginOperatorID) throws ValidationException
		{
			check(esb, "ChangeAirParameters");
			this.defaultOriginOperatorID = defaultOriginOperatorID;
		}

		public int getDefaultSubscriberNumberNAI()
		{
			check(esb, "ViewAirParameters");
			return defaultSubscriberNumberNAI;
		}

		public void setDefaultSubscriberNumberNAI(int defaultSubscriberNumberNAI) throws ValidationException
		{
			check(esb, "ChangeAirParameters");
			this.defaultSubscriberNumberNAI = defaultSubscriberNumberNAI;
		}

		public String getDefaultCurrency()
		{
			check(esb, "ViewAirParameters");
			return defaultCurrency;
		}

		public void setDefaultCurrency(String defaultCurrency)
		{
			check(esb, "ChangeAirParameters");
			this.defaultCurrency = defaultCurrency;
		}

		public int getCurrencyDecimalDigits()
		{
			check(esb, "ViewAirParameters");
			return currencyDecimalDigits;
		}

		public void setCurrencyDecimalDigits(int currencyDecimalDigits) throws ValidationException
		{
			check(esb, "ChangeAirParameters");
			ValidationException.inRange(0, currencyDecimalDigits, 4, "Invalid Currency Decimal Digits");
			this.currencyDecimalDigits = currencyDecimalDigits;
		}
		
		public boolean getLogRequests()
		{
			check(esb, "ViewAirParameters");
			return this.logRequests;
		}
		
		public void setLogRequests(boolean logRequests)
		{
			check(esb, "ChangeAirParameters");
			this.logRequests = logRequests;
		}
		
		public boolean getLogResponses()
		{
			check(esb, "ViewAirParameters");
			return this.logResponses;
		}
		
		public void setLogResponses(boolean logResponses)
		{
			check(esb, "ChangeAirParameters");
			this.logResponses = logResponses;
		}

		@Override
		public String getPath(String languageCode)
		{
			return "Technical Settings";
		}

		@Override
		public String getName(String languageCode)
		{
			return "AIR Connector";
		}

		@Override
		public void validate() throws ValidationException
		{

		}

		public AirConnectorConfig()
		{

		}

		@Override
		public long getSerialVersionUID()
		{
			return 372557405L;
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

		@Override
		public Collection<IConfiguration> getConfigurations()
		{
			ArrayList<IConfiguration> result = new ArrayList<IConfiguration>();
			for (IConfiguration config : airConnectionConfigs)
			{
				result.add(config);
			}
			return result;
		}

	}

	@Perms(perms = { @Perm(name = "ViewAirParameters", description = "View Air Parameters", category = "Air", supplier = true),
			@Perm(name = "ChangeAirParameters", implies = "ViewAirParameters", description = "Change Air Parameters", category = "Air", supplier = true) })
	public class AirConnectionConfig extends ConfigurationBase
	{
		private String name;
		private long serialVersionUID;
		private String uri = "";
		private String userAgent = "hxcuser/4.2/1.0";
		private String userName = "hxcuser";
		private String password = "hxcuser";
		private int connectTimeout = 1000;
		private int readTimeout = 30000;
		private int maxConsecutiveErrors = 3;
		private int retrySecondsAfterFailure = 120;

		public AirConnectionConfig()
		{
		}

		private AirConnectionConfig(String name, long serialVersionUID)
		{
			this.name = name;
			this.serialVersionUID = serialVersionUID;
		}

		private AirConnectionConfig(String name, long serialVersionUID, String uri, String userAgent, //
				String userName, String password, int connectTimeout, int readTimeout)
		{
			this.name = name;
			this.serialVersionUID = serialVersionUID;
			this.uri = uri;
			this.userAgent = userAgent;
			this.userName = userName;
			this.password = password;
			this.connectTimeout = connectTimeout;
			this.readTimeout = readTimeout;

			// | 2069 | UCIP4 | slc.2 | 10.1.8.22 | 10010 | hxc | hxc123 | UCIP_HOSTNAME=USSDGW;UCIP_USERAGENT=UGw
			// Server/4.2/1.0;UCIP_NODETYPE=EXT;UCIP_NAI=2;UCIP_TIMEZONE=0;UCIP_RESPONSE_TIMEOUT=30000;

		}

		public String getUri()
		{
			check(esb, "ViewAirParameters");
			return uri;
		}

		public void setUri(String uri) throws ValidationException
		{
			check(esb, "ChangeAirParameters");

			ValidationException.validateURL(uri, "URI");
			this.uri = uri;
		}

		public String getUserAgent()
		{
			check(esb, "ViewAirParameters");
			return userAgent;
		}

		public void setUserAgent(String userAgent)
		{
			check(esb, "ChangeAirParameters");
			this.userAgent = userAgent;
		}

		public String getUserName()
		{
			check(esb, "ViewAirParameters");
			return userName;
		}

		public void setUserName(String userName)
		{
			check(esb, "ChangeAirParameters");
			this.userName = userName;
		}

		public String getPassword()
		{
			check(esb, "ViewAirParameters");
			return password;
		}

		public void setPassword(String password)
		{
			check(esb, "ChangeAirParameters");
			this.password = password;
		}

		public int getConnectTimeout()
		{
			check(esb, "ViewAirParameters");
			return connectTimeout;
		}

		public void setConnectTimeout(int connectTimeout) throws ValidationException
		{
			check(esb, "ChangeAirParameters");

			ValidationException.min(1, connectTimeout);
			this.connectTimeout = connectTimeout;
		}

		public int getReadTimeout()
		{
			check(esb, "ViewAirParameters");
			return readTimeout;
		}

		public void setReadTimeout(int readTimeout) throws ValidationException
		{
			check(esb, "ChangeAirParameters");

			ValidationException.min(1, readTimeout);
			this.readTimeout = readTimeout;
		}

		public int getMaxConsecutiveErrors()
		{
			check(esb, "ViewAirParameters");
			return maxConsecutiveErrors;
		}

		public void setMaxConsecutiveErrors(int maxConsecutiveErrors) throws ValidationException
		{
			check(esb, "ChangeAirParameters");

			ValidationException.min(1, maxConsecutiveErrors);
			this.maxConsecutiveErrors = maxConsecutiveErrors;
		}

		public int getRetrySecondsAfterFailure()
		{
			check(esb, "ViewAirParameters");
			return retrySecondsAfterFailure;
		}

		public void setRetrySecondsAfterFailure(int retrySecondsAfterFailure) throws ValidationException
		{
			check(esb, "ChangeAirParameters");

			ValidationException.min(1, retrySecondsAfterFailure);
			this.retrySecondsAfterFailure = retrySecondsAfterFailure;
		}

		@Override
		public String getPath(String languageCode)
		{
			return "";
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

		@Override
		public long getSerialVersionUID()
		{
			return serialVersionUID;
		}

		@Override
		public String getName(String languageCode)
		{
			return name;
		}

		@Override
		public void validate() throws ValidationException
		{
			try
			{
				ValidationException.min(connectTimeout, readTimeout);
			}
			catch (ValidationException exc)
			{
				throw new ValidationException("%s: %s", "ReadTimeout", exc.getMessage());
			}
		}
	}

	private AirConnectorConfig config = new AirConnectorConfig();

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
		logger.info("Starting Air Connector at [{}]", config.airConnectionConfigs[0].getUri());

		airCallsCounter = new LinkedHashMap<String, AtomicLong>();
		for (IDimension dimension : airCallsMetric.getDimensions())
		{
			airCallsCounter.put(dimension.getName().replaceAll(" ", "").toLowerCase(), new AtomicLong());
		}

		// Log Information
		logger.info("Air Connector started at [{}]", config.airConnectionConfigs[0].getUri());

		return true;
	}

	@Override
	public void stop()
	{
		// Log Information
		logger.info("Air Connector stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config)
	{
		this.config = (AirConnectorConfig) config;

		try
		{
			if (airConnections != null)
				airConnections.clear();
		}
		catch (UnsupportedOperationException e)
		{

		}
		airConnections = null;
	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return false;
	}

	@Override
	public boolean isFit()
	{
		if (airConnections == null)
			getConnection(null);

		boolean servicable = false;

		DateTime now = DateTime.getNow();
		for (AirConnection airConnection : airConnections)
		{
			if (airConnection.isServicable(now))
				servicable = true;

			HashMap<String, AtomicLong> counter = airConnection.getAirCallsCounter();
			for (String airCall : counter.keySet())
			{
				if (airCallsCounter.containsKey(airCall.toLowerCase()))
				{
					airCallsCounter.get(airCall.toLowerCase()).addAndGet(counter.get(airCall).longValue());
				}
			}
		}

		airCallsMetric.report(esb, (Object[]) airCallsCounter.values().toArray(new AtomicLong[airCallsCounter.size()]));
		for (String key : airCallsCounter.keySet())
		{
			airCallsCounter.put(key, new AtomicLong());
		}

		return servicable;
	}

	@Override
	public IAirConnection getConnection(String optionalConnectionString)
	{
		// Construct List of Connections
		if (airConnections == null)
		{
			initConnectionList();
		}

		// Choose a connector based on 1) Least Pending Requests 2) Least Recently Used
		AirConnection connectionToUse = null;
		DateTime now = DateTime.getNow();
		for (AirConnection airConnection : airConnections)
		{
			String host = airConnection.getHost();
			Integer port = airConnection.getPort();
			logger.debug("Checking AirConnection is servicable [{}] [{}]", host, port);
			if (airConnection.isServicable(now))
			{
				if (connectionToUse == null || airConnection.compareTo(connectionToUse) > 0)
					connectionToUse = airConnection;
			}
		}
		if(connectionToUse == null)
		{
			logger.error("All configured AIR servers are out of service. Using first one in list.");
			// TODO: Possibly good to have the option to configure a complete service outage until AIR comes back on line; testing periodically in a scheduled thread.
			for (AirConnection airConnection : airConnections)
			{
				if(airConnection.isConfigured())
				{
					if(connectionToUse == null || airConnection.compareTo(connectionToUse) > 0)
						connectionToUse = airConnection;
				}
			}
		}
		return connectionToUse;
	}

	private synchronized void initConnectionList()
	{
		if (airConnections != null)
			return;

		ArrayList<AirConnection> tmp = new ArrayList<AirConnection>();

		Collection<IConfiguration> airConfigs = config.getConfigurations();
		for (IConfiguration airConfig : airConfigs)
		{
			tmp.add(new AirConnection(config, (AirConnectionConfig) airConfig));
		}
		airConnections = tmp;

	}

	@Override
	public IMetric[] getMetrics()
	{
		return new IMetric[] { airCallsMetric };
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IAirConnector Implementation
	//
	// /////////////////////////////////
	@Override
	public Subscriber getSubscriber(String subscriberNumber, ITransaction transaction)
	{
		return new Subscriber(subscriberNumber, this, transaction);
	}

	@Override
	public String getNextTransactionID(int length)
	{
		String transactionID = esb.getNextTransactionNumber(length);
		MDC.put(LoggingConstants.CONST_LOG_TRANSID, transactionID);

		return transactionID;
	}

	@Override
	public INumberPlan getNumberPlan()
	{
		return esb.getFirstService(INumberPlan.class);
	}

	@Override
	public Long toLongAmount(BigDecimal amount)
	{
		if (amount == null)
			return null;

		return amount.movePointRight(config.currencyDecimalDigits).longValueExact();
	}

	@Override
	public BigDecimal fromLongAmount(Long amount)
	{
		if (amount == null)
			return null;
		return new BigDecimal(amount).movePointLeft(config.currencyDecimalDigits);
	}

	@Override
	public String getLanguageCode2(int languageID)
	{
		String languageCode3 = esb.getLocale().getLanguage(languageID);
		if (languageCode3 == null || languageCode3.isEmpty())
			return null;
		Locale locale = localeMap.get(languageCode3.toLowerCase());
		if (locale == null)
			return null;

		return locale.getLanguage();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

}
