package hxc.servicebus;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.management.UnixOperatingSystemMXBean;

import hxc.configuration.Config;
import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IConnector;
import hxc.connectors.ctrl.ICtrlConnector;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.diagnostic.IDiagnosticsTransmitter;
import hxc.connectors.snmp.ISnmpConnector;
import hxc.connectors.snmp.IncidentSeverity;
import hxc.connectors.snmp.IndicationState;
import hxc.services.IService;
import hxc.services.logging.ILogger;
import hxc.services.logging.ILoggerInfoConfig;
import hxc.services.notification.INotifications;
import hxc.services.notification.IPhrase;
import hxc.services.notification.Phrase;
import hxc.services.security.ISecurity;
import hxc.services.security.ISecurityCheck;
import hxc.services.security.IUser;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.security.SupplierOnly;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMeasurement;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.instrumentation.Metric;
import hxc.utils.instrumentation.ValueType;
import hxc.utils.plugins.C4UPluginLoader;
import hxc.utils.registration.IFacilityRegistration;
import hxc.utils.registration.IRegistration;
import hxc.utils.registration.Registration;
import hxc.utils.watcher.FileWatcher;
import hxc.utils.watcher.IFileWatcherProcessHandler;

@Perm(name = "ViewPerformance", description = "View Performance Parameters", category = "Performance")
public class ServiceBus implements IServiceBus, IService
{
	final static Logger logger = LoggerFactory.getLogger(ServiceBus.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private BlockingQueue<Runnable> requestQueue;
	private ThreadPoolExecutor threadPool;
	private ScheduledThreadPoolExecutor scheduledThreadPool;
	private ISecurityCheck security;
	private IServiceBus esb = this;
	private static final AtomicLong queueSequence = new AtomicLong(0);
	private C4UPluginLoader loader;
	private FileWatcher watcher = null;
	private IDiagnosticsTransmitter diagnosticsTransmitter;
	private List<String> pluginIdClashes = null;

	private Metric tpsMetric = Metric.CreateSimple("TPS", "transactions/s", ValueType.InstantaneousCount, 5000);
	private final Object startupLock = new Object();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Singleton Construction
	//
	// /////////////////////////////////
	private static ServiceBus instance = null;

	// Construction (Singleton approach)
	private ServiceBus()
	{
		services.add(this);
	}

	public static ServiceBus getInstance()
	{
		return instance == null ? getInstance(true) : instance;
	}

	public static synchronized ServiceBus getInstance(boolean makeNew)
	{
		if (instance != null && !makeNew)
			return instance;
		return instance = new ServiceBus();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configurable Properties
	//
	// /////////////////////////////////
	@Perms(perms = { @Perm(name = "ViewTuning", description = "View Tuning Parameters", category = "Service Bus", supplier = true),
			@Perm(name = "ChangeTuning", implies = "ViewTuning", description = "Change Tuning Parameters", category = "Service Bus", supplier = true) })
	public class ServiceBusConfig extends ConfigurationBase
	{
		final Logger logger = LoggerFactory.getLogger(ServiceBusConfig.class);
		
		private final String pluginDirectory = "/opt/cs/c4u/" + Version.major + "/hostprocess/plugins";
		private int maxTPS = 10;
		private int maxPeakTPS = 50;
		private int threadQueueCapacity = 5;
		private int maxThreadPoolSize = 10;
		private int maxSchedulerPoolSize = 10;
		private LocaleConfig localeConfig = new LocaleConfig();

		public String getPluginDirectory()
		{
			check(esb, "ViewTuning");
			return pluginDirectory;
		}

		@SupplierOnly
		public int getMaxTPS()
		{
			check(esb, "ViewTuning");
			return maxTPS;
		}

		@SupplierOnly
		public int getMaxPeakTPS()
		{
			check(esb, "ViewTuning");
			return maxPeakTPS;
		}

		@SupplierOnly
		public int getThreadQueueCapacity()
		{
			check(esb, "ViewTuning");
			return threadQueueCapacity;
		}

		@SupplierOnly
		public void setThreadQueueCapacity(int threadQueueCapacity) throws ValidationException
		{
			check(esb, "ChangeTuning");
			ValidationException.inRange(1, threadQueueCapacity, 100, "ThreadQueueCapacity");
			this.threadQueueCapacity = threadQueueCapacity;
		}

		@SupplierOnly
		public int getMaxThreadPoolSize()
		{
			check(esb, "ViewTuning");
			return maxThreadPoolSize;
		}

		@SupplierOnly
		public void setMaxThreadPoolSize(int maxThreadPoolSize) throws ValidationException
		{
			check(esb, "ChangeTuning");
			ValidationException.inRange(5, maxThreadPoolSize, 100, "MaxThreadPoolSize");
			this.maxThreadPoolSize = maxThreadPoolSize;
		}

		@SupplierOnly
		public int getMaxSchedulerPoolSize()
		{
			check(esb, "ViewTuning");
			return maxSchedulerPoolSize;
		}

		@SupplierOnly
		public void setMaxSchedulerPoolSize(int maxSchedulerPoolSize) throws ValidationException
		{
			check(esb, "ChangeTuning");
			ValidationException.inRange(1, maxSchedulerPoolSize, 10, "MaxSchedulerPoolSize");
			this.maxSchedulerPoolSize = maxSchedulerPoolSize;
		}

		@Override
		public String getPath(String languageCode)
		{
			return "Technical Settings";
		}

		@Override
		public String getName(String languageCode)
		{
			return "Service Bus";
		}

		@Override
		public void validate() throws ValidationException
		{

		}

		@Override
		public long getSerialVersionUID()
		{
			return 783078316L;
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

		@Override
		public Collection<IConfiguration> getConfigurations()
		{
			List<IConfiguration> result = new ArrayList<IConfiguration>();
			result.add(localeConfig);
			return result;
		}

	}

	private ServiceBusConfig config = new ServiceBusConfig();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configurable Locale Properties
	//
	// /////////////////////////////////
	@Perms(perms = { @Perm(name = "ViewLocale", description = "View Locale Parameters", category = "Locale", supplier = true),
			@Perm(name = "ChangeLocale", implies = "ViewLocale", description = "Change Locale Parameters", category = "Locale", supplier = true) })
	public class LocaleConfig extends ConfigurationBase implements ILocale
	{
		final Logger logger = LoggerFactory.getLogger(LocaleConfig.class);

		private int defaultLanguageId = 1;

		private String language1 = "eng";
		private String language1Name = "English";
		private String language1Alphabet = "latn";
		private String language1DateFormat = "d/MM/yy";
		private String language1TimeFormat = "HH:mm:ss";
		private EncodingScheme language1EncodingScheme = EncodingScheme.GSM7;
		private String language2 = "fre";
		private String language2Name = "Francais";
		private String language2Alphabet = "latn";
		private String language2DateFormat = "d/MM/yy";
		private String language2TimeFormat = "HH:mm:ss";
		private EncodingScheme language2EncodingScheme = EncodingScheme.GSM7;
		private String language3 = "";
		private String language3Name = "";
		private String language3Alphabet = "latn";
		private String language3DateFormat = "d/MM/yy";
		private String language3TimeFormat = "HH:mm:ss";
		private EncodingScheme language3EncodingScheme = EncodingScheme.GSM7;
		private String language4 = "";
		private String language4Name = "";
		private String language4Alphabet = "latn";
		private String language4DateFormat = "d/MM/yy";
		private String language4TimeFormat = "HH:mm:ss";
		private EncodingScheme language4EncodingScheme = EncodingScheme.GSM7;

		private String currencyCode = "USD";
		private int currencyDecimalDigits = 2;
		private int displayDecimalDigits = 2;

		@Override
		@Config(description = "Default Language Number", minValue = "1", maxValue = "4")
		public int getDefaultLanguageID()
		{
			return defaultLanguageId;
		}

		@Override
		public String getDefaultLanguageCode()
		{
			return getLanguage(getDefaultLanguageID());
		}

		public void setDefaultLanguageId(int defaultLanguageId) throws ValidationException
		{
			check(esb, "ChangeLocale");
			if (defaultLanguageId > IPhrase.MAX_LANGUAGES)
			{
				throw new ValidationException("cannot exceed %d", IPhrase.MAX_LANGUAGES);
			}
			this.defaultLanguageId = defaultLanguageId;
		}

		@Config(description = "4th Language Encoding Scheme")
		public EncodingScheme getLanguage4EncodingScheme()
		{
			return language4EncodingScheme;
		}

		public void setLanguage4EncodingScheme(EncodingScheme language4EncodingScheme)
		{
			this.language4EncodingScheme = language4EncodingScheme;
		}

		@Config(description = "4th Language Date Format")
		public String getLanguage4DateFormat()
		{
			return language4DateFormat;
		}

		public void setLanguage4DateFormat(String language4DateFormat)
		{
			this.language4DateFormat = language4DateFormat;
		}

		@Config(description = "4th Language Time Format")
		public String getLanguage4TimeFormat()
		{
			return language4TimeFormat;
		}

		public void setLanguage4TimeFormat(String language4TimeFormat)
		{
			this.language4TimeFormat = language4TimeFormat;
		}

		@Config(description = "4th Language ISO 15924 alphabet identifier")
		public String getLanguage4Alphabet()
		{
			return language4Alphabet;
		}

		public void setLanguage4Alphabet(String language4Alphabet)
		{
			check(esb, "ChangeLocale");
			this.language4Alphabet = language4Alphabet;
		}

		public String getLanguage4Name()
		{
			return language4Name;
		}

		public void setLanguage4Name(String language4Name)
		{
			this.language4Name = language4Name;
		}

		@Config(description = "4th Language ISO 639-2 language identifier")
		public String getLanguage4()
		{
			return language4;
		}

		public void setLanguage4(String language4)
		{
			check(esb, "ChangeLocale");
			this.language4 = language4;
		}

		@Config(description = "3rd Language Encoding Scheme")
		public EncodingScheme getLanguage3EncodingScheme()
		{
			return language3EncodingScheme;
		}

		public void setLanguage3EncodingScheme(EncodingScheme language3EncodingScheme)
		{
			this.language3EncodingScheme = language3EncodingScheme;
		}

		@Config(description = "3rd Language Date Format")
		public String getLanguage3DateFormat()
		{
			return language3DateFormat;
		}

		public void setLanguage3DateFormat(String language3DateFormat)
		{
			this.language3DateFormat = language3DateFormat;
		}

		@Config(description = "3rd Language Time Format")
		public String getLanguage3TimeFormat()
		{
			return language3TimeFormat;
		}

		public void setLanguage3TimeFormat(String language3TimeFormat)
		{
			this.language3TimeFormat = language3TimeFormat;
		}

		@Config(description = "3rd Language ISO 15924 alphabet identifier")
		public String getLanguage3Alphabet()
		{
			return language3Alphabet;
		}

		public void setLanguage3Alphabet(String language3Alphabet)
		{
			check(esb, "ChangeLocale");
			this.language3Alphabet = language3Alphabet;
		}

		public String getLanguage3Name()
		{
			return language3Name;
		}

		public void setLanguage3Name(String language3Name)
		{
			this.language3Name = language3Name;
		}

		public String getLanguage3()
		{
			return language3;
		}

		@Config(description = "3rd Language ISO 639-2 language identifier")
		public void setLanguage3(String language3)
		{
			check(esb, "ChangeLocale");
			this.language3 = language3;
		}

		@Config(description = "2nd Language Encoding Scheme")
		public EncodingScheme getLanguage2EncodingScheme()
		{
			return language2EncodingScheme;
		}

		public void setLanguage2EncodingScheme(EncodingScheme language2EncodingScheme)
		{
			this.language2EncodingScheme = language2EncodingScheme;
		}

		@Config(description = "2nd Language Date Format")
		public String getLanguage2DateFormat()
		{
			return language2DateFormat;
		}

		public void setLanguage2DateFormat(String language2DateFormat)
		{
			this.language2DateFormat = language2DateFormat;
		}

		@Config(description = "2nd Language Time Format")
		public String getLanguage2TimeFormat()
		{
			return language2TimeFormat;
		}

		public void setLanguage2TimeFormat(String language2TimeFormat)
		{
			this.language2TimeFormat = language2TimeFormat;
		}

		@Config(description = "2nd Language ISO 15924 alphabet identifier")
		public String getLanguage2Alphabet()
		{
			return language2Alphabet;
		}

		public void setLanguage2Alphabet(String language2Alphabet)
		{
			check(esb, "ChangeLocale");
			this.language2Alphabet = language2Alphabet;
		}

		public String getLanguage2Name()
		{
			return language2Name;
		}

		public void setLanguage2Name(String language2Name)
		{
			this.language2Name = language2Name;
		}

		@Config(description = "2nd Language ISO 639-2 language identifier")
		public String getLanguage2()
		{
			return language2;
		}

		public void setLanguage2(String language2)
		{
			check(esb, "ChangeLocale");
			this.language2 = language2;
		}

		@Config(description = "1st Language Encoding Scheme")
		public EncodingScheme getLanguage1EncodingScheme()
		{
			return language1EncodingScheme;
		}

		public void setLanguage1EncodingScheme(EncodingScheme language1EncodingScheme)
		{
			this.language1EncodingScheme = language1EncodingScheme;
		}

		@Config(description = "1st Language Date Format")
		public String getLanguage1DateFormat()
		{
			return language1DateFormat;
		}

		public void setLanguage1DateFormat(String language1DateFormat)
		{
			this.language1DateFormat = language1DateFormat;
		}

		@Config(description = "1st Language Time Format")
		public String getLanguage1TimeFormat()
		{
			return language1TimeFormat;
		}

		public void setLanguage1TimeFormat(String language1TimeFormat)
		{
			this.language1TimeFormat = language1TimeFormat;
		}

		@Config(description = "1st Language ISO 15924 alphabet identifier")
		public String getLanguage1Alphabet()
		{
			return language1Alphabet;
		}

		public void setLanguage1Alphabet(String language1Alphabet)
		{
			check(esb, "ChangeLocale");
			this.language1Alphabet = language1Alphabet;
		}

		public String getLanguage1Name()
		{
			return language1Name;
		}

		public void setLanguage1Name(String language1Name)
		{
			this.language1Name = language1Name;
		}

		@Config(description = "1st Language ISO 639-2 language identifier")
		public String getLanguage1()
		{
			return language1;
		}

		public void setLanguage1(String language1)
		{
			check(esb, "ChangeLocale");
			this.language1 = language1;
		}

		@Config(description = "ISO 4217 currency identifier")
		@Override
		public String getCurrencyCode()
		{
			check(esb, "ViewLocale");
			return currencyCode;
		}

		public void setCurrencyCode(String currency)
		{
			check(esb, "ChangeLocale");
			this.currencyCode = currency;
		}

		@SupplierOnly
		@Override
		public int getMaxLanguages()
		{
			return IPhrase.MAX_LANGUAGES;
		}

		@Override
		public String getLanguage(Integer languageID)
		{
			return getLanguageLocale(languageID).getLanguage();
		}

		@Override
		public int getLanguageID(Integer languageID)
		{
			return languageID == null || languageID < 1 || languageID > IPhrase.MAX_LANGUAGES ? IPhrase.DEFAULT_LANGUAGE_ID : languageID;
		}

		@Override
		public int getLanguageID(String languageCode)
		{
			for (int languageID = 1; languageID <= IPhrase.MAX_LANGUAGES; languageID++)
			{
				if (getLanguage(languageID).equals(languageCode))
					return languageID;
			}

			return IPhrase.DEFAULT_LANGUAGE_ID;
		}

		@Override
		public String getLanguageName(Integer languageID)
		{
			return getLanguageLocale(languageID).getLanguageName();
		}

		@Override
		public String getAlpabet(Integer languageID)
		{
			return getLanguageLocale(languageID).getAlpabet();
		}

		@Override
		public String getAlpabet(String languageCode)
		{
			for (int languageID = 1; languageID <= getMaxLanguages(); languageID++)
			{
				ILanguage locale = getLanguageLocale(languageID);
				if (locale.getLanguage().equalsIgnoreCase(languageCode))
					return locale.getAlpabet();
			}

			return null;
		}

		@Override
		public String getEncodingScheme(Integer languageID)
		{
			return getLanguageLocale(languageID).getEncodingScheme();
		}

		@Override
		public String getEncodingScheme(String languageCode)
		{
			return getEncodingScheme(getLanguageID(languageCode));
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
			return -7749345829926118652L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "Locale";
		}

		@Override
		public void validate() throws ValidationException
		{

		}

		@Override
		@Config(description = "Currency Decimal Digits")
		public int getCurrencyDecimalDigits()
		{
			check(esb, "ViewLocale");
			return currencyDecimalDigits;
		}

		public void setCurrencyDecimalDigits(int currencyDecimalDigits) throws ValidationException
		{
			if (currencyDecimalDigits < 0 || currencyDecimalDigits > 6)
				throw new ValidationException("Invalid Number of Decimal Digits");
			this.currencyDecimalDigits = currencyDecimalDigits;
		}

		
		@Config(description = "Display Decimal Digits")
		public int getDisplayDecimalDigits()
		{
			check(esb, "ViewLocale");
			return displayDecimalDigits;
		}

		public void setDisplayDecimalDigits(int displayDecimalDigits) throws ValidationException
		{
			if (displayDecimalDigits < 0 || displayDecimalDigits > 6)
				throw new ValidationException("Invalid Number of Decimal Digits");
			this.displayDecimalDigits = displayDecimalDigits;
		}

		@Override
		public String formatCurrency(long amount)
		{
			String sign = "";
			String value;
			if (amount < 0)
			{
				sign = "-";
				amount = -amount;
			}

			switch (this.currencyDecimalDigits)
			{
				case 0:
					value = String.format("%s%d", sign, amount);
					break;
				case 2:
					value = String.format("%s%d.%02d", sign, amount / 100, amount % 100);
					break;
				case 3:
					value = String.format("%s%d.%03d", sign, amount / 1000, amount % 1000);
					break;
				case 4:
					value = String.format("%s%d.%04d", sign, amount / 10000, amount % 10000);
					break;
				case 5:
					value = String.format("%s%d.%05d", sign, amount / 100000, amount % 100000);
					break;
				case 6:
					value = String.format("%s%d.%06d", sign, amount / 1000000, amount % 1000000);
					break;
				default:
				{
					return "??";
				}
			}
			if (this.displayDecimalDigits == this.currencyDecimalDigits)
			{
				return String.format("%s%s", sign, value);
			}
			if (this.displayDecimalDigits < this.currencyDecimalDigits)
			{
				return String.format("%s%s", sign, value.substring(0, value.length() - (this.currencyDecimalDigits - this.displayDecimalDigits)));
			}
			if (this.displayDecimalDigits > this.currencyDecimalDigits)
			{
				int padZeros = this.displayDecimalDigits - this.currencyDecimalDigits;
				String paddedValue = String.format("%1$-" + padZeros + "s", value).replace(' ', '0');
				return String.format("%s%s", sign, paddedValue);
			}
			return "??";
		}

		@Override
		public String formatDate(Date date, Integer languageID)
		{
			return (new SimpleDateFormat(getLanguageLocale(languageID).getDateFormat())).format(date);
		}

		@Override
		public String formatTime(Date time, Integer languageID)
		{
			return (new SimpleDateFormat(getLanguageLocale(languageID).getTimeFormat())).format(time);
		}

		@Override
		public String getDateFormat(Integer languageID)
		{
			return getLanguageLocale(languageID).getDateFormat();
		}

		@Override
		public ILanguage getLanguageLocale(Integer languageID)
		{
			String language;
			String languageName;
			String alpabet;
			String encodingScheme;
			String dateFormat;
			String timeFormat;

			if (languageID == null)
				languageID = getDefaultLanguageID();

			switch (languageID)
			{
				case 1:
					language = language1;
					languageName = language1Name;
					alpabet = language1Alphabet;
					encodingScheme = language1EncodingScheme.toString();
					dateFormat = language1DateFormat;
					timeFormat = language1TimeFormat;
					break;

				case 2:
					language = language2;
					languageName = language2Name;
					alpabet = language2Alphabet;
					encodingScheme = language2EncodingScheme.toString();
					dateFormat = language2DateFormat;
					timeFormat = language2TimeFormat;
					break;

				case 3:
					language = language3;
					languageName = language3Name;
					alpabet = language3Alphabet;
					encodingScheme = language3EncodingScheme.toString();
					dateFormat = language3DateFormat;
					timeFormat = language3TimeFormat;
					break;

				case 4:
					language = language4;
					languageName = language4Name;
					alpabet = language4Alphabet;
					encodingScheme = language4EncodingScheme.toString();
					dateFormat = language4DateFormat;
					timeFormat = language4TimeFormat;
					break;

				default:
					return getLanguageLocale(getDefaultLanguageID());
			}

			final String finalLanguage = language;
			final String finalLanguageName = languageName;
			final String finalAlpabet = alpabet;
			final String finalEncodingScheme = encodingScheme;
			final String finalDateFormat = dateFormat;
			final String finalTimeFormat = timeFormat;

			return new ILanguage()
			{

				@Override
				public String getLanguage()
				{
					return finalLanguage;
				}

				@Override
				public String getLanguageName()
				{
					return finalLanguageName;
				}

				@Override
				public String getAlpabet()
				{
					return finalAlpabet;
				}

				@Override
				public String getEncodingScheme()
				{
					return finalEncodingScheme;
				}

				@Override
				public String getDateFormat()
				{
					return finalDateFormat;
				}

				@Override
				public String getTimeFormat()
				{
					return finalTimeFormat;
				}

				@Override
				public int getCurrencyDecimalDigits()
				{
					return LocaleConfig.this.getCurrencyDecimalDigits();
				}

			};
		}

	}

	@Override
	public ILocale getLocale()
	{
		return config.localeConfig;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Connectors
	//
	// /////////////////////////////////

	// List of connectors
	private List<IConnector> connectors = new ArrayList<IConnector>();

	private boolean isPluginUnique(IPlugin plugin)
	{
		if (plugin.getConfiguration() == null)
			return true;

		for (IPlugin pn : connectors)
		{
			if ((pn.getConfiguration() != null) && (pn.getConfiguration().getSerialVersionUID() == plugin.getConfiguration().getSerialVersionUID()))
			{
				logger.info("Connector UID clash: [{}] [{}] [{}]", pn.getConfiguration().getClass().getName(), plugin.getConfiguration().getClass().getName(), pn.getConfiguration().getSerialVersionUID());
				return false;				
			}
		}
		for (IPlugin pn : services)
		{
			if ((pn.getConfiguration() != null) && (pn.getConfiguration().getSerialVersionUID() == plugin.getConfiguration().getSerialVersionUID()))
			{
				logger.info("Service UID clash: [{}] [{}] [{}]", pn.getConfiguration().getClass().getName(), plugin.getConfiguration().getClass().getName(), pn.getConfiguration().getSerialVersionUID());
				return false;
			}
		}
		return true;
	}

	public boolean verifyPluginRegistration(IPlugin plugin)
	{
		if (!isPluginUnique(plugin))
		{
			String errorMessage = String.format("Configuration '%s' SerialVersionUID ID '%d' NOT UNIQUE [%s]", plugin.getConfiguration().getName(Phrase.ENG),
					plugin.getConfiguration().getSerialVersionUID(), plugin.getClass().getSimpleName());
			if (pluginIdClashes == null)
			{
				pluginIdClashes = new ArrayList<String>();
			}
			pluginIdClashes.add(plugin.getClass().getSimpleName());
			logger.error(errorMessage);
			return false;
		}
		else if ((pluginIdClashes != null) && (pluginIdClashes.contains(plugin.getClass().getSimpleName())))
		{
			pluginIdClashes.remove(plugin.getClass().getSimpleName());
			if (pluginIdClashes.size() == 0)
				pluginIdClashes = null;
		}
		return true;
	}

	/**
	 * Register a connector
	 */
	@Override
	public void registerConnector(IConnector connector)
	{
		if (connector != null)
		{
			synchronized (connector)
			{
				if (!connectors.contains(connector) && verifyPluginRegistration(connector))
				{
					connectors.add(connector);
					connector.initialise(this);

					if (isRunning().get())
					{
						IDatabase database = getFirstConnector(IDatabase.class);
						if (database != null)
						{
							IConfiguration config = connector.getConfiguration();
							try (IDatabaseConnection connection = database.getConnection(null))
							{
								if (config != null && !config.load(connection))
									throw new Exception();
							}
							catch (Exception e)
							{
								logger.error("Failed to load Connector Configuration for '{}'", config != null ? config.getName(Phrase.ENG) : connector.getClass().getSimpleName());
							}
						}

						logger.trace("Starting IConnector '{}'", connector.getClass().toString());
						if (connector.start(null))
						{
							startedPlugins.push(connector);
						}
					}
				}
			}
		}
	}

	@Override
	public void unregisterConnector(IConnector connector)
	{
		if (connector != null)
		{
			synchronized (connector)
			{
				if (connectors.contains(connector))
				{
					connectors.remove(connector);

					if (isRunning().get())
					{
						connector.stop();
						startedPlugins.remove(connector);
						connector = null;
					}
				}
			}
		}
	}

	/**
	 * Obtain a list of connectors
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getConnectors(Class<T> cls)
	{
		List<T> result = new ArrayList<T>();
		for (IPlugin plugin : startedPlugins)
		{
			if (plugin instanceof IConnector && cls.isInstance(plugin))
			{
				result.add((T) plugin);
			}
		}
		return result;
	}

	/**
	 * Get the first connector of type
	 */

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getFirstConnector(Class<T> cls)
	{
		for (IPlugin plugin : startedPlugins)
		{
			if (plugin instanceof IConnector && cls.isInstance(plugin))
			{
				return (T) plugin;
			}
		}

		return null;
	}

	@Override
	public List<IPlugin> getRegisteredPlugins()
	{
		List<IPlugin> result = new ArrayList<IPlugin>();
		result.addAll(connectors);
		result.addAll(services);
		return result;
	}

	@Override
	public List<IPlugin> getStartedPlugins()
	{
		List<IPlugin> result = new ArrayList<IPlugin>();
		result.addAll(startedPlugins);
		result.add(this);
		return result;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Service
	//
	// /////////////////////////////////

	// Services
	private List<IService> services = new ArrayList<IService>();

	/**
	 * Register a Service
	 */
	@Override
	public void registerService(IService service)
	{
		if (service != null)
		{
			synchronized (service)
			{
				if (!services.contains(service) && verifyPluginRegistration(service))
				{
					service.initialise(this);
					services.add(service);

					if (isRunning().get())
					{
						IDatabase database = getFirstConnector(IDatabase.class);
						if (database != null)
						{
							IConfiguration config = service.getConfiguration();
							try (IDatabaseConnection connection = database.getConnection(null))
							{
								if (config != null && !config.load(connection))
									throw new Exception();
							}
							catch (Exception e)
							{
								logger.error("Failed to load Service Configuration for '{}'", config != null ? config.getName(Phrase.ENG) : service.getClass().getSimpleName());
							}
						}

						logger.trace("Starting IService '{}'", service.getClass().toString());
						if (service.start(null))
						{
							startedPlugins.push(service);
						}
					}
				}
			}
		}
	}

	@Override
	public void unregisterService(IService service)
	{
		if (service != null)
		{
			synchronized (service)
			{
				if (services.contains(service))
				{
					services.remove(service);

					if (isRunning().get())
					{
						service.stop();
						startedPlugins.remove(service);
						service = null;
					}
				}
			}
		}
	}

	/**
	 * Get a list of services of type
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getServices(Class<T> cls)
	{
		List<T> result = new ArrayList<T>();
		for (IPlugin plugin : startedPlugins)
		{
			if (plugin instanceof IService && cls.isInstance(plugin))
			{
				result.add((T) plugin);
			}
		}
		return result;
	}

	/**
	 * Get the first service of type
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getFirstService(Class<T> cls)
	{
		for (IPlugin plugin : startedPlugins)
		{
			if (plugin instanceof IService && cls.isInstance(plugin))
			{
				return (T) plugin;
			}
		}

		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Service
	//
	// /////////////////////////////////

	// Triggers
	List<ITrigger> triggers = new ArrayList<ITrigger>();
	Object lockTriggers = new Object();

	@Override
	public void addTrigger(ITrigger trigger)
	{
		synchronized(lockTriggers)
		{
			triggers.add(trigger);
		}
	}

	@Override
	public void removeTrigger(ITrigger trigger)
	{
		synchronized(lockTriggers)
		{
			triggers.remove(trigger);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Dispatching
	//
	// /////////////////////////////////

	@Override
	public int dispatch(final Object message, final IConnection connection)
	{
		int count = 0;
		synchronized(lockTriggers)
		{
			for (ITrigger trigger : triggers)
			{
				try
				{
					if (trigger.matches(message, connection))
					{
						long priority = queueSequence.getAndIncrement();
						if (trigger.getIsLowPriority(message))
							priority |= 0x40000000L;
						logger.trace("ServiceBus.dispatch():before execute message = {}, trigger = {}", message, trigger);
						threadPool.execute(new Action(trigger, message, connection, priority));
						logger.trace("ServiceBus.dispatch():after execute");
						count++;
					}
				}
				catch (Throwable e)
				{
					logger.error("Dispatch failed", e);
				}
			}
		}
		return count;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Services and Connectors Start & Stop
	//
	// /////////////////////////////////

	@Override
	public int getThreadQueueCapacity()
	{
		return config.threadQueueCapacity;
	}

	@Override
	public void setThreadQueueCapacity(int threadQueueCapacity)
	{
		try
		{
			config.setThreadQueueCapacity(threadQueueCapacity);
		}
		catch (ValidationException e)
		{
		}
	}

	@Override
	public int getMaxThreadPoolSize()
	{
		return config.maxThreadPoolSize;
	}

	@Override
	public void setMaxThreadPoolSize(int maxThreadPoolSize)
	{
		config.maxThreadPoolSize = maxThreadPoolSize;
	}

	// Started plug-ins (Connectors and Services)
	private Stack<IPlugin> startedPlugins = new Stack<IPlugin>();
	private AtomicBoolean running = new AtomicBoolean(false);

	/**
	 * Iterate through all connectors and services and call their starts
	 * 
	 * @throws ValidationException
	 */
	@Override
	public boolean start(String[] args)
	{
		boolean result = false;
		synchronized(startupLock)
		{	
			result = startPlugins(args);
			startupLock.notifyAll();
		}
		return result;
	}
	
	private boolean startPlugins(String[] args)
	{
		loader = new C4UPluginLoader();

		File dir = new File(config.pluginDirectory);
		if (!dir.exists())
			dir.mkdirs();

		// Register the directory to be watched
		if (watcher == null)
		{
			watcher = new FileWatcher();
		}
		watcher.registerDirectory(config.pluginDirectory);
		watcher.applyFilter(".*\\.(jar|lic)");

		// Create a file watch handler
		IFileWatcherProcessHandler handler = new IFileWatcherProcessHandler()
		{

			private HashMap<String, IPlugin> plugins = new HashMap<>();

			@Override
			public void processNewFile(File file)
			{
				if (file.getName().contains(".lic"))
				{
					logger.info("Loading Licence");
					Thread t = new Thread()
					{
						@Override
						public void run()
						{
							try
							{
								registerLicense(Registration.deserialize(config.pluginDirectory));
							}
							catch (Exception e)
							{
								logger.error("Error loading license: {}", e.getMessage());
							}
						}
					};
					t.start();
					return;
				}

				if (plugins.containsKey(file.getName()))
					return;

				Class<?> c;
				try
				{
					c = loader.loadMainClassFromJar(file);
				}
				catch (Exception exc)
				{
					return;
				}

				IPlugin plugin;
				try
				{
					plugin = (IPlugin) c.getDeclaredConstructor().newInstance();
				}
				catch (Exception exc)
				{
					return;
				}

				if (plugin instanceof IConnector)
				{
					esb.registerConnector((IConnector) plugin);
				}
				else if (plugin instanceof IService)
				{
					esb.registerService((IService) plugin);
				}
				else
				{
					return;
				}

				plugins.put(file.getName(), plugin);
			}

			@Override
			public void processDeletedFile(File file)
			{
				// Unloading not supported yet
			}

		};
		watcher.processEvents(handler);

		// Go through the plugin directory to check for plugins before start up
		File files[] = new File(config.pluginDirectory).listFiles(new FilenameFilter()
		{

			@Override
			public boolean accept(File dir, String name)
			{
				return name.contains(".jar") || name.contains(".lic");
			}

		});

		if (files != null && files.length > 0)
		{
			for (File file : files)
			{
				handler.processNewFile(file);
			}
		}

		// Essential to ensure that all classes will share the same class loader
		Thread.currentThread().setContextClassLoader(loader);

		// Initialise databases
		IDatabase database = null;
		for (IConnector connector : connectors)
		{
			if (connector instanceof IDatabase)
			{
				database = (IDatabase) connector;
				IConfiguration config = connector.getConfiguration();
				if (config != null)
					config.load(null);
			}
		}

		// Start Loggers
		for (IService service : services)
		{
			if (service instanceof ILogger)
			{
				logger.trace("Starting ILogger '{}'", service.getClass().toString());
				if (service.start(args))
				{
					startedPlugins.push(service);

					// Initialise loggers
					IConfiguration config = service.getConfiguration();
					if (config != null && database != null)
					{
						try (IDatabaseConnection connection = database.getConnection(null))
						{
							config.load(connection);
							service.setConfiguration(config);
						}
						catch (Exception e)
						{
							logger.error(e.getMessage(), e);
							System.exit(1);
						}
					}

				}
			}
		}


		// Start Databases
		for (IConnector connector : connectors)
		{
			if (connector instanceof IDatabase)
			{
				logger.trace("Starting IConnector '{}'", connector.getClass().toString());
				if (connector.start(args))
				{
					startedPlugins.push(connector);
				}
			}
		}

		// Start Control Connector
		for (IConnector connector : connectors)
		{
			if (connector instanceof ICtrlConnector)
			{
				control = (ICtrlConnector) connector;

				logger.trace("Starting IConnector '{}'", connector.getClass().toString());
				if (connector.start(args))
				{
					startedPlugins.push(connector);

					IConfiguration config = connector.getConfiguration();
					if (config != null && database != null)
					{
						try (IDatabaseConnection connection = database.getConnection(null))
						{
							config.load(connection);
							connector.setConfiguration(config);
						}
						catch (Exception e)
						{
							logger.error(e.getMessage(), e);
							System.exit(1);
						}
					}
				}

				break;
			}

		}

		// Create a database Connection
		IDatabaseConnection connection = null;
		try
		{
			try
			{
				if (logger != null && database != null)
					connection = database.getConnection(null);
			}
			catch (IOException e)
			{
			}

			// Read all configurations from database
			if (connection != null)
			{
				// check for Debug mode
				boolean debugMode = (args != null) && (Arrays.asList(args).contains("-debug") || Arrays.asList(args).contains("-d"));

				if (debugMode)
					logger.info("RUNNING IN DEBUG MODE");

				// Load Configuration into connectors and validate
				List<IConnector> failedConnectors = new ArrayList<IConnector>();
				for (IConnector connector : connectors)
				{
					if (connector instanceof IDatabase)
						continue;
					IConfiguration config = connector.getConfiguration();
					if (config != null && !config.load(connection))
					{
						if (!debugMode)
						{
							failedConnectors.add(connector);
							logger.error("Failed to load Connector Configuration for '{}' due to Validation Error", config.getName(Phrase.ENG));
						}
						else
							logger.error("Connector Configuration for '{}' Validation Failed (Running in Debug Mode) FIX!", config.getName(Phrase.ENG));

					}
				}
				for (IConnector failedConnector : failedConnectors)
				{
					connectors.remove(failedConnector);
				}

				// Load Configuration into services and validate
				List<IService> failedServices = new ArrayList<IService>();
				for (IService service : services)
				{
					if (service instanceof ILogger)
						continue;
					IConfiguration config = service.getConfiguration();
					if (config != null && !config.load(connection))
					{
						if (!debugMode)
						{
							failedServices.add(service);
							logger.error("Failed to load Service Configuration for '{}' due to Validation Error", config.getName(Phrase.ENG));
						}
						else
							logger.error("Service Configuration for '{}' Validation Failed (Running in Debug Mode) FIX!", config.getName(Phrase.ENG));

					}
				}
				for (IService failedService : failedServices)
				{
					services.remove(failedService);
				}

			}

			// Create the thread pool
			logger.trace("config.threadQueueCapacity = {}, config.maxThreadPoolSize = {}", config.threadQueueCapacity, config.maxThreadPoolSize);
			requestQueue = new PriorityBlockingQueue<Runnable>(config.threadQueueCapacity);
			threadPool = new ThreadPoolExecutor(config.maxThreadPoolSize, config.maxThreadPoolSize, 1, TimeUnit.HOURS, requestQueue, new ThreadFactory()
			{

				private int num = 0;

				@Override
				public Thread newThread(Runnable r)
				{
					try
					{
						logger.trace("ServiceBus...ThreadFactory.newThread:entry");
						return new Thread(r, "ServiceBusThreadPool-" + num++);
					}
					finally
					{
						logger.trace("ServiceBus...ThreadFactory.newThread:end");
					}
				}

			})
			{
				@Override
				protected void beforeExecute(Thread t, Runnable r)
				{
					logger.trace("ServiceBus...beforeExecute:entry");
					if (r instanceof Action && ((Action) r).isTransaction())
					{
						countTPS();
					}
				}
			};

			// Prevent Overflow
			threadPool.setRejectedExecutionHandler(new RejectedExecutionHandler()
			{

				@Override
				public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor)
				{
					logger.trace("ServiceBus...ThreadFactory.rejectedExecution:entry runnable = {}, executor = {}", runnable, executor);
					if (!executor.isShutdown() && runnable instanceof Action)
					{
						countTPS();
						runnable.run();
					}
				}
			});

			// Create the scheduler
			scheduledThreadPool = new ScheduledThreadPoolExecutor(config.maxSchedulerPoolSize, new ThreadFactory()
			{

				private int num = 0;

				@Override
				public Thread newThread(Runnable r)
				{
					return new Thread(r, "ScheduledServiceBusThreadPool-" + num++);
				}

			});

			boolean moreNeedToStart = true;
			boolean someStarted;
			IPlugin failed = null;

			// Continue till everything is started or a deadlock occurs
			while (moreNeedToStart)
			{
				moreNeedToStart = false;
				someStarted = false;

				for (IConnector connector : connectors)
				{
					if (!startedPlugins.contains(connector))
					{
						logger.trace("Starting IConnector '{}'", connector.getClass().toString());
						if (connector.start(args))
						{
							someStarted = true;
							startedPlugins.push(connector);
						}
						else
						{
							failed = connector;
							moreNeedToStart = true;
						}
					}
				}

				for (IService service : services)
				{
					if (service == this)
						continue;

					if (!startedPlugins.contains(service))
					{
						logger.trace("Starting IService '{}'", service.getClass().toString());
						if (service.start(args))
						{
							someStarted = true;
							startedPlugins.push(service);
						}
						else
						{
							failed = service;
							moreNeedToStart = true;
						}
					}
				}

				// Detect deadlock
				if (moreNeedToStart && !someStarted)
				{
					logger.error("Plugin '{}' Failed to start. Probably caused by one or dependencies which failed to start.", failed.toString());

					String name = failed.getClass().getSimpleName();
					if (failed.getConfiguration() != null)
						name = failed.getConfiguration().getName(Phrase.ENG);

					ISnmpConnector snmp = getFirstConnector(ISnmpConnector.class);
					if (snmp != null)
						snmp.elementManagementStatus(name, IndicationState.OUT_OF_SERVICE, IncidentSeverity.MAJOR, "Failed to start");
					return false;
				}
			}

			// Create TPS counter thread
			tokenBucket = new AtomicInteger(config.maxTPS);
			scheduledThreadPool.scheduleAtFixedRate(tpsMonitor, 0, 1000, TimeUnit.MILLISECONDS);

			// Get security if available
			security = getFirstService(ISecurityCheck.class);

		}
		finally
		{
			try
			{
				if (connection != null)
					connection.close();
			}
			catch (Exception e)
			{
				logger.error("DB connection failed", e);
			}
		}

		Runtime.getRuntime().addShutdownHook(new Thread()
		{

			@Override
			public void run()
			{
				// Shutdown all registered shutdown services
				for (IShutdown service : shutdowns)
				{
					{
						logger.trace("Doing shutdown for {}", service);
					}
					service.shutdown();
					{
						logger.trace("Shutdown completed for {}", service);
					}
				}

				logger.info("Shutting Down C4U");

				// Stop all Connectors and Services
				esb.stop();
			}

		});

		// Locate Diagnostic Transmitter if present
		diagnosticsTransmitter = getFirstConnector(IDiagnosticsTransmitter.class);
		
		//Print some useful commands to tail the log.
		logger.info("ServiceBus started...");
		ILoggerInfoConfig loggerInfoConfig = esb.getFirstService(ILoggerInfoConfig.class);
		if(loggerInfoConfig != null)
		{
			logger.info("tail -F {}/{} | grep -E \"(FATAL|ERROR|WARN)\"", loggerInfoConfig.getDirectory(), loggerInfoConfig.getInterimFilename());
			logger.info("tail -F {}/{} | grep -E \"(FATAL|ERROR|WARN)|$\"", loggerInfoConfig.getDirectory(), loggerInfoConfig.getInterimFilename());
		} else {
			logger.error("Could not obtain ILoggerInfoConfig from service bus");
			logger.info("tail -F /var/opt/cs/c4u/log/log.tmp | grep -E \"(FATAL|ERROR|WARN)\"");
			logger.info("tail -F /var/opt/cs/c4u/log/log.tmp | grep -E \"(FATAL|ERROR|WARN)|$\"");
		}
		setRunning(true);				
		return true;
	}

	@Override
	public void stop()
	{
		// Remove all Triggers first
		synchronized(lockTriggers)
		{
			triggers.clear();
		}

		// Shutdown the thread pool
		if (threadPool != null)
		{
			threadPool.shutdown();
			
			while (!threadPool.isShutdown())
			{
				try
				{
					Thread.sleep(100);
				}
				catch(Exception ex)
				{
					break;
				}
			}
		}
		threadPool = null;
		requestQueue = null;

		// Shutdown the scheduler
		if (scheduledThreadPool != null)
		{
			scheduledThreadPool.shutdown();
			
			while (!scheduledThreadPool.isShutdown())
			{
				try
				{
					Thread.sleep(100);
				}
				catch(Exception ex)
				{
					break;
				}
			}
		}
		scheduledThreadPool = null;

		// Then stop services in the reverse
		while (!startedPlugins.isEmpty())
		{
			IPlugin plugin = startedPlugins.pop();
			if (plugin != this)
			{
				try
				{
					logger.trace("Stopping {}", plugin);
					plugin.stop();
					logger.trace("Stopped {}", plugin);
				}
				catch (Throwable e)
				{
					logger.error(String.format("Failed to stop plugin %s", plugin.getClass().getName()), e);
				}
			}
		}
		setRunning(false);		
		connectors.clear();
		services.clear();
		startedPlugins.clear();
	}

	public int getPID()
	{
		java.lang.management.RuntimeMXBean runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();
		int myPid;

		try
		{
			// Updated to work with Java 11
			String name = runtime.getName(); // format: "pid@hostname"
			myPid = Integer.parseInt(name.substring(0, name.indexOf('@')));
			return myPid;
		}
		catch (IllegalArgumentException  | SecurityException e)
		{
		}
		return -1;
	}

	public void setRunning(boolean running)
	{
		this.running.set(running);			
	}
	
	public AtomicBoolean isRunning()
	{
		return this.running;		
	}
	
	public void waitForRunning() throws InterruptedException
	{
		synchronized(startupLock)
		{
			while(!isRunning().get())
				startupLock.wait();
		}
	}
	
	public void waitForRunning(long timeout) throws InterruptedException
	{
		synchronized(startupLock)
		{
			while(!isRunning().get())
				startupLock.wait(timeout);
		}
	}
	
	private List<IShutdown> shutdowns = new ArrayList<IShutdown>();

	@Override
	public void registerShutdown(IShutdown service)
	{
		if (service != null)
		{
			synchronized (service)
			{
				if (!shutdowns.contains(service))
				{
					shutdowns.add(service);
				}
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Transaction Numbering
	//
	// /////////////////////////////////

	@Override
	public String getNextTransactionNumber(int length)
	{
		return transactionMoniker.getNextTransactionNumber(length);
	}

	private final SimpleDateFormat transactionDateFormat = new SimpleDateFormat("yyMMddHHmmss");
	private static final String zeroes = "00000000000000000000000000000000000000000000000000000000000000000000000000000000000";
	private ICtrlConnector control = null;

	private class TransactionMoniker
	{
		private String prefix;
		private int counter = 0;

		private TransactionMoniker()
		{
			if (control == null)
				control = getFirstConnector(ICtrlConnector.class);
			String transactionNumberPrefix = control == null ? "00" : control.getThisTransactionNumberPrefix();
			prefix = transactionNumberPrefix + transactionDateFormat.format(new Date());
			counter = 0;
		}

		private TransactionMoniker create()
		{
			TransactionMoniker result = new TransactionMoniker();
			return result.prefix.equals(prefix) ? this : result;
		}

		public String getNextTransactionNumber(int length)
		{
			String suffix = Integer.toString(++counter);
			int padding = length - prefix.length() - suffix.length();
			if (padding < 0 || padding >= zeroes.length())
				return prefix + suffix;
			else
				return prefix + zeroes.substring(0, padding) + suffix;
		}

	}

	TransactionMoniker transactionMoniker = new TransactionMoniker();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// TPS Monitor
	//
	// /////////////////////////////////
	private TpsMonitor tpsMonitor = new TpsMonitor();
	private AtomicInteger tpsCounter = new AtomicInteger();
	private AtomicInteger tokenBucket = new AtomicInteger();
	private int currentTPS = 0;
	private int consecutiveLimiting = 0;
	private int aggregatedTps = 0;
	private long aggregatedTpsStarted = System.currentTimeMillis();;

	@Override
	public int getCurrentTPS()
	{
		check(this, "ViewPerformance");
		return currentTPS;
	}

	@Override
	public int getMaxTPS()
	{
		return config.maxTPS;
	}

	@Override
	public void setMaxTPS(int maxTPS)
	{
		check(ServiceBusConfig.class, "ChangeTuning");
		config.maxTPS = maxTPS;
	}

	@Override
	public int getMaxPeakTPS()
	{
		return config.maxPeakTPS;
	}

	@Override
	public void setMaxPeakTPS(int maxAccumulatedTokens)
	{
		config.maxPeakTPS = maxAccumulatedTokens;
	}

	@Override
	public int getConsecutiveLimiting()
	{
		return consecutiveLimiting;
	}

	private class TpsMonitor implements Runnable
	{
		@Override
		public void run()
		{
			currentTPS = tpsCounter.getAndSet(0);
			aggregatedTps += currentTPS;
			if (currentTPS >= config.maxTPS)
				consecutiveLimiting++;
			else
				consecutiveLimiting = 0;
			int tokensBeforeFull = config.maxPeakTPS - tokenBucket.get();
			int toGive = Math.min(config.maxTPS, tokensBeforeFull);
			tokenBucket.addAndGet(toGive);
			transactionMoniker = transactionMoniker.create();
			tpsMetric.report(esb, currentTPS);
		}
	}

	@Override
	public void countTPS()
	{
		// Throttle
		int token = tokenBucket.decrementAndGet();
		while (token < 0)
		{
			try
			{
				Thread.sleep(0, 100);
			}
			catch (InterruptedException e)
			{
				logger.error("countTPS", e);
			}
			token = tokenBucket.get();
		}

		// Increment the TPS counter
		tpsCounter.incrementAndGet();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IService Implementation
	//
	// /////////////////////////////////

	@Override
	public void initialise(IServiceBus esb)
	{
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		this.config = (ServiceBusConfig) config;
	};

	@Override
	public boolean canAssume(String serverRole)
	{
		return false;
	}

	@Override
	public boolean isFit()
	{
		// Monitor number of open FD per process.
		// Case: On high rate throughput HTTP servers might consume plenty of connections due to slow processing.
		// Normal value up to 200 open FD.
		// Suspicious above 2000
		// Default limit: 4096
		try
		{
			OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
			if (os instanceof UnixOperatingSystemMXBean)
				logger.info("Current count of open file descriptor: " + ((UnixOperatingSystemMXBean) os).getOpenFileDescriptorCount());
		}
		catch (Exception ex)
		{

		}

		// Calculate TPM
		long next = System.currentTimeMillis();
		long deltaMS = next - aggregatedTpsStarted;
		if (deltaMS > 30000)
		{
			double tps = (double) aggregatedTps / (deltaMS / 1000);
			aggregatedTpsStarted = next;
			aggregatedTps = 0;
			if (logger != null)
				logger.info(String.format("Average TPS = %.2f", tps));

			if (diagnosticsTransmitter != null)
			{
				diagnosticsTransmitter.set("ESB", "ATPS", (int) tps, 12);
			}
		}

		boolean isFit = (scheduledThreadPool != null) && !scheduledThreadPool.isShutdown() && !scheduledThreadPool.isTerminated() && (threadPool != null) && (!threadPool.isShutdown())
				&& (!threadPool.isTerminated()) && (pluginIdClashes == null);

		if (!isFit)
		{
			String pluginClash = (pluginIdClashes == null) ? "None" : pluginIdClashes.toString();

			logger.error(
					String.format("ScheduledThreadPool (Null:%b Shutdown:%b  Terminated:%b)", (scheduledThreadPool == null), scheduledThreadPool.isShutdown(), scheduledThreadPool.isTerminated()));
			logger.error(String.format("ThreadPool (Null:%b Shutdown:%b  Terminated:%b)", (threadPool == null), threadPool.isShutdown(), threadPool.isTerminated()));
			logger.error(String.format("pluginIdClashes:%s", pluginClash));
		}

		return isFit;
	}

	@Override
	public IMetric[] getMetrics()
	{
		return new IMetric[] { tpsMetric };
	}

	@Override
	public void sendMeasurement(IMeasurement measurement)
	{
		threadPool.execute(measurement);
	}

	@Override
	public ThreadPoolExecutor getThreadPool()
	{
		return threadPool;
	}

	@Override
	public ScheduledThreadPoolExecutor getScheduledThreadPool()
	{
		return scheduledThreadPool;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Resilience
	//
	// /////////////////////////////////
	@Override
	public IPlugin getCandidate(String serverRole)
	{
		for (IConnector connector : connectors)
		{
			if (connector.canAssume(serverRole))
				return connector;
		}

		for (IService service : services)
		{
			if (service.canAssume(serverRole))
				return service;
		}

		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Licensing
	//
	// /////////////////////////////////

	private HashMap<String, IFacilityRegistration> availableFacilities = new HashMap<String, IFacilityRegistration>();
	private IRegistration lastRegistration;

	private void registerLicense(IRegistration registration) throws Exception
	{	
		synchronized(startupLock)
		{
			while (!isRunning().get())
				startupLock.wait();
		}
		//while (!running)
		//	Thread.sleep(1000);
		

		lastRegistration = registration;
		IDatabase database = getFirstConnector(IDatabase.class);
		IDatabaseConnection connection = null;
		try
		{
			if (database != null)
				connection = database.getConnection(null);

			// Service Bus
			{
				config.maxTPS = registration.getMaxTPS();
				config.maxPeakTPS = registration.getMaxPeakTPS();
				tokenBucket.set(0);

				if (connection != null)
					config.save(connection, control);
			}

			// Security Service
			{
				ISecurity security = getFirstService(ISecurity.class);
				if (security != null)
				{
					IUser user = security.getUser("supplier");
					if (user != null)
					{
						byte publicKey[] = user.getPublicKey();
						byte[] passwordBytes = registration.getSupplierKey().getBytes();
						byte[] credentials = new byte[passwordBytes.length + publicKey.length];
						System.arraycopy(passwordBytes, 0, credentials, 0, passwordBytes.length);
						System.arraycopy(publicKey, 0, credentials, passwordBytes.length, publicKey.length);
						MessageDigest md = MessageDigest.getInstance("SHA-1");
						user.setPassword(md.digest(credentials));
						user.update();
					}
				}
			}

			// Control Connector
			{
				if (control != null)
				{
					control.setMaxNodes(registration.getMaxNodes());
				}
			}

			// Facilities
			{
				if (registration.getFacilities() != null && registration.getFacilities().length > 0)
				{
					for (IFacilityRegistration facility : registration.getFacilities())
					{
						availableFacilities.put(facility.getFacilityID().toUpperCase(), facility);
					}
				}
			}
		}
		finally
		{
			if (connection != null)
				connection.close();
		}
	}

	@Override
	public IFacilityRegistration hasFacility(String facilityID)
	{
		return availableFacilities.get(facilityID);
	}

	@Override
	public IRegistration getLastRegistration()
	{
		return lastRegistration;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	void check(Object object, String permissionId) throws SecurityException
	{
		if (security != null)
			security.check(object, permissionId);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Versioning
	//
	// /////////////////////////////////
	@Override
	public String getVersion()
	{
		Version.configure();
		return String.format("%s.%s", Version.major, Version.revision);
	}

	@Override
	public String getBaseDirectory()
	{
		Version.configure();
		return String.format("/opt/cs/c4u/%s/hostprocess", Version.major);
	}

}
