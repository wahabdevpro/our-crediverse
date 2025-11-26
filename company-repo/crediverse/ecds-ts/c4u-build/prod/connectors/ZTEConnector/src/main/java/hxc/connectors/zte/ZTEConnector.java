package hxc.connectors.zte;

import java.math.BigDecimal;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.soap.ISubscriber;
import hxc.connectors.zte.proxy.Subscriber;
import hxc.servicebus.IServiceBus;
import hxc.services.logging.ILogger;
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

public class ZTEConnector implements IZTEConnector
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private ILogger logger;
	private List<ZTEConnection> zteConnections = null;

	private Metric zteCallsMetric = Metric.CreateBasedOnProtocol("ZTE Calls", 60000, "Call(s)", IZTEConnection.class, "getAddress", "close") //
			.prioritise("queryuserprofile", "queryaccountballance", "queryservice", "modcustomer", "deductfee", "setservice");
	private LinkedHashMap<String, AtomicLong> zteCallsCounter;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configurable Properties
	//
	// /////////////////////////////////
	@Perms(perms = { @Perm(name = "ViewZTEParameters", description = "View ZTE Parameters", category = "ZTE", supplier = true),
			@Perm(name = "ChangeZTEParameters", implies = "ViewZTEParameters", description = "Change ZTE Parameters", category = "ZTE", supplier = true) })
	public class ZTEConnectorConfig extends ConfigurationBase
	{
		private ZTEConnectionConfig[] zteConnectionConfigs = new ZTEConnectionConfig[] { //
				new ZTEConnectionConfig("ZTE1", 6546418764133218536L,"http://localhost:8089/services/","EconetWebService.EconetWebServiceHttpSoap12Endpoint","ConcurrentUSSD", "Econet@ConcurrentUSSD", 1000, 30000),
				new ZTEConnectionConfig("ZTE2", 3145454354865423484L),
				new ZTEConnectionConfig("ZTE3", -5185230540165405421L),
				new ZTEConnectionConfig("ZTE4", 2465651245651325449L),
		};

		private String defaultCurrency = "USD";
		private int currencyDecimalDigits = 2;
		private int defaultSubscriberNumberNAI = 1;

		public int getDefaultSubscriberNumberNAI()
		{
			check(esb, "ViewZTEParameters");
			return defaultSubscriberNumberNAI;
		}

		public void setDefaultSubscriberNumberNAI(int defaultSubscriberNumberNAI) throws ValidationException
		{
			check(esb, "ChangeZTEParameters");
			this.defaultSubscriberNumberNAI = defaultSubscriberNumberNAI;
		}

		public String getDefaultCurrency()
		{
			check(esb, "ViewZTEParameters");
			return defaultCurrency;
		}

		public void setDefaultCurrency(String defaultCurrency)
		{
			check(esb, "ChangeZTEParameters");
			this.defaultCurrency = defaultCurrency;
		}

		public int getCurrencyDecimalDigits()
		{
			check(esb, "ViewZTEParameters");
			return currencyDecimalDigits;
		}

		public void setCurrencyDecimalDigits(int currencyDecimalDigits) throws ValidationException
		{
			check(esb, "ChangeZTEParameters");
			ValidationException.inRange(0, currencyDecimalDigits, 6, "Invalid Currency Decimal Digits");
			this.currencyDecimalDigits = currencyDecimalDigits;
		}

		@Override
		public String getPath(String languageCode)
		{
			return "Technical Settings";
		}

		@Override
		public String getName(String languageCode)
		{
			return "ZTE Connector";
		}

		@Override
		public void validate() throws ValidationException
		{

		}

		public ZTEConnectorConfig()
		{

		}

		@Override
		public long getSerialVersionUID()
		{
			return 365481325L;
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
			for (IConfiguration config : zteConnectionConfigs)
			{
				result.add(config);
			}
			return result;
		}

	}

	@Perms(perms = { @Perm(name = "ViewZTEParameters", description = "View ZTE Parameters", category = "ZTE", supplier = true),
			@Perm(name = "ChangeZTEParameters", implies = "ViewZTEParameters", description = "Change ZTE Parameters", category = "ZTE", supplier = true) })
	public class ZTEConnectionConfig extends ConfigurationBase
	{
		private String name;
		private long serialVersionUID;
		private String startingURL = "";
		private String endPoint = "";
		private String userName = "hxcuser";
		private String password = "hxcuser";
		private int connectTimeout = 3000;
		private int readTimeout = 30000;
		private int maxConsecutiveErrors = 3;
		private int retryMinutesAfterFailure = 1;

		public ZTEConnectionConfig()
		{
		}

		private ZTEConnectionConfig(String name, long serialVersionUID)
		{
			this.name = name;
			this.serialVersionUID = serialVersionUID;
		}

		private ZTEConnectionConfig(String name, long serialVersionUID, String startingURL, String endPoint, //
				String userName, String password, int connectTimeout, int readTimeout)
		{
			this.name = name;
			this.serialVersionUID = serialVersionUID;
			this.startingURL = startingURL;
			this.endPoint = endPoint;
			this.userName = userName;
			this.password = password;
			this.connectTimeout = connectTimeout;
			this.readTimeout = readTimeout;
		}

		
		public String getEndPoint()
		{
			check(esb, "ViewZTEParameters");
			return endPoint;
		}

		public void setEndPoint(String endPoint) throws ValidationException
		{
			check(esb, "ChangeZTEParameters");

			//ValidationException.validateURL(endPoint, "URL");
			this.endPoint = endPoint;
		}
		
		public String getStartingURL()
		{
			check(esb, "ViewZTEParameters");
			return startingURL;
		}

		public void setStartingURL(String startingURL) throws ValidationException
		{
			check(esb, "ChangeZTEParameters");

			ValidationException.validateURL(startingURL, "URL");
			this.startingURL = startingURL;
		}

		public String getUserName()
		{
			check(esb, "ViewZTEParameters");
			return userName;
		}

		public void setUserName(String userName)
		{
			check(esb, "ChangeZTEParameters");
			this.userName = userName;
		}

		public String getPassword()
		{
			check(esb, "ViewZTEParameters");
			return password;
		}

		public void setPassword(String password)
		{
			check(esb, "ChangeZTEParameters");
			this.password = password;
		}

		public int getConnectTimeout()
		{
			check(esb, "ViewZTEParameters");
			return connectTimeout;
		}

		public void setConnectTimeout(int connectTimeout) throws ValidationException
		{
			check(esb, "ChangeZTEParameters");

			ValidationException.min(1, connectTimeout);
			this.connectTimeout = connectTimeout;
		}

		public int getReadTimeout()
		{
			check(esb, "ViewZTEParameters");
			return readTimeout;
		}

		public void setReadTimeout(int readTimeout) throws ValidationException
		{
			check(esb, "ChangeZTEParameters");

			ValidationException.min(1, readTimeout);
			this.readTimeout = readTimeout;
		}

		public int getMaxConsecutiveErrors()
		{
			check(esb, "ViewZTEParameters");
			return maxConsecutiveErrors;
		}

		public void setMaxConsecutiveErrors(int maxConsecutiveErrors) throws ValidationException
		{
			check(esb, "ChangeZTEParameters");

			ValidationException.min(1, maxConsecutiveErrors);
			this.maxConsecutiveErrors = maxConsecutiveErrors;
		}

		public int getRetryMinutesAfterFailure()
		{
			check(esb, "ViewZTEParameters");
			return retryMinutesAfterFailure;
		}

		public void setRetryMinutesAfterFailure(int retryMinutesAfterFailure) throws ValidationException
		{
			check(esb, "ChangeZTEParameters");

			ValidationException.min(1, retryMinutesAfterFailure);
			this.retryMinutesAfterFailure = retryMinutesAfterFailure;
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

	private ZTEConnectorConfig config = new ZTEConnectorConfig();

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
		// Get logger
		logger = esb.getFirstService(ILogger.class);
		if (logger == null)
			return false;

		logger.info(this, "Starting ZTE Connector at [" + config.zteConnectionConfigs[0].getStartingURL() + "]");

		zteCallsCounter = new LinkedHashMap<String, AtomicLong>();
		for (IDimension dimension : zteCallsMetric.getDimensions())
		{
			zteCallsCounter.put(dimension.getName().replaceAll(" ", "").toLowerCase(), new AtomicLong());
		}

		// Log Information
		logger.info(this, "ZTE Connector started at [" + config.zteConnectionConfigs[0].getStartingURL() + "]");

		return true;
	}

	@Override
	public void stop()
	{
		// Log Information
		logger.info(this, "ZTE Connector stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config)
	{
		this.config = (ZTEConnectorConfig) config;

		try
		{
			if (zteConnections != null)
				zteConnections.clear();
		}
		catch (UnsupportedOperationException e)
		{

		}
		zteConnections = null;
	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return false;
	}

	@Override
	public boolean isFit()
	{
		if (zteConnections == null)
			getConnection(null);

		boolean servicable = false;

		DateTime now = DateTime.getNow();
		for (ZTEConnection zteConnection : zteConnections)
		{
			if (zteConnection.isServicable(now))
				servicable = true;

			HashMap<String, AtomicLong> counter = zteConnection.getZTECallsCounter();
			for (String zteCall : counter.keySet())
			{
				if (zteCallsCounter.containsKey(zteCall.toLowerCase()))
				{
					zteCallsCounter.get(zteCall.toLowerCase()).addAndGet(counter.get(zteCall).longValue());
				}
			}
		}

		zteCallsMetric.report(esb, (Object[]) zteCallsCounter.values().toArray(new AtomicLong[zteCallsCounter.size()]));
		for (String key : zteCallsCounter.keySet())
		{
			zteCallsCounter.put(key, new AtomicLong());
		}

		return servicable;
	}

	@Override
	public IZTEConnection getConnection(String optionalConnectionString)
	{
		// Construct List of Connections
		if (zteConnections == null)
		{
			initConnectionList();
		}

		// Choose a connector based on 1) Least Pending Requests 2) Least Recently Used
		ZTEConnection connectionToUse = null;
		DateTime now = DateTime.getNow();
		for (ZTEConnection zteConnection : zteConnections)
		{
			if (zteConnection.isServicable(now))
			{
				if (connectionToUse == null || zteConnection.compareTo(connectionToUse) > 0)
					connectionToUse = zteConnection;
			}
		}

		return connectionToUse == null ? zteConnections.get(0) : connectionToUse;
	}

	private synchronized void initConnectionList()
	{
		if (zteConnections != null)
			return;

		ArrayList<ZTEConnection> tmp = new ArrayList<ZTEConnection>();

		Collection<IConfiguration> zteConfigs = config.getConfigurations();
		for (IConfiguration zteConfig : zteConfigs)
		{
			tmp.add(new ZTEConnection(logger, (ZTEConnectionConfig) zteConfig));
		}
		zteConnections = tmp;

	}

	@Override
	public IMetric[] getMetrics()
	{
		return new IMetric[] { zteCallsMetric };
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IZTEConnector Implementation
	//
	// /////////////////////////////////
	@Override
	public Subscriber getSubscriber(String subscriberNumber, ITransaction transaction)
	{
		return new Subscriber(subscriberNumber, this, logger, transaction);
	}

	@Override
	public String getNextTransactionID(int length)
	{
		String transactionID = esb.getNextTransactionNumber(length);

		if (logger != null)
			logger.setThreadTransactionID(transactionID);

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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

}
