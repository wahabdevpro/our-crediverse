/**
 *
 */
package hxc.services.logging;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.util.StatusPrinter;
import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.database.IDatabaseConnection;
import hxc.servicebus.HostInfo;
import hxc.servicebus.IServiceBus;
import hxc.services.IService;
import hxc.services.notification.INotifications;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.security.SupplierOnly;
import hxc.utils.calendar.DateTime;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;

/**
 * @author AndriesdB
 * @fixer  MartinC
 * 
 */
public class LoggerService implements ILogger, IService, ILoggerInfoConfig
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Private Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	protected boolean mustStop = true;
	protected boolean isRunning = false;
	private Date lastWriteTime = new Date();
	private int rotationCounter = 0;
	private Date nextRotationTime = new Date();
	
	private String hostname = "Unknown";

	private static Logger logger = LoggerFactory.getLogger(LoggerService.class);

	private String getInterimFilePath()
	{
		return config.directoryName + "/" + config.interimFileName;
	}
	
	private static LoggerService loggerService = new LoggerService();
	
	public static LoggerService getService()
	{
		return loggerService;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Per Thread Properties
	//
	// /////////////////////////////////
	private static final ThreadLocal<String> threadTransactionID = new ThreadLocal<String>()
	{
		@Override
		protected String initialValue()
		{
			return "";
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configurable Properties
	//
	// /////////////////////////////////
	@Perms(perms = { @Perm(name = "ViewLoggerParameters", description = "View Logger Parameters", category = "Logger", supplier = true),
			@Perm(name = "PerformLogfileRotate", description = "Perform Rotation of Logfile", category = "Logger", supplier = true),
			@Perm(name = "ChangeLoggerParameters", implies = "ViewLoggerParameters", description = "Change Logger Parameters", category = "Logger", supplier = true) })
	public class LoggerServiceConfig extends ConfigurationBase
	{
		private LoggingLevels loggingLevel = LoggingLevels.INFO;

		private int maxMilliSecondsToBlock = 250;
		private int rotationIntervalSeconds = 7200;
		private String timeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS";
		private String directoryName = "/var/opt/cs/c4u/log";
		private String interimFileName = "log.tmp";
		private String rotatedNameFormat = "%1$s%2$s.log";
		private int maxFileSizeInMegs = 1024;

		public LoggingLevels getLoggingLevel()
		{
			check(esb, "ViewLoggerParameters");
			return loggingLevel;
		}

		public void setLoggingLevel(LoggingLevels loggingLevel) throws ValidationException
		{
			this.loggingLevel = loggingLevel;

			//TODO root.setLevel(AdjustLogLevel2(loggingLevel));
		}

		@SupplierOnly
		public int getMaxMilliSecondsToBlock()
		{
			check(esb, "ViewLoggerParameters");
			return maxMilliSecondsToBlock;
		}

		@SupplierOnly
		public void setMaxMilliSecondsToBlock(int maxMilliSecondsToBlock) throws ValidationException
		{
			check(esb, "ChangeLoggerParameters");

			ValidationException.min(1, maxMilliSecondsToBlock);
			this.maxMilliSecondsToBlock = maxMilliSecondsToBlock;
		}

		public int getRotationIntervalSeconds()
		{
			check(esb, "ViewLoggerParameters");
			return rotationIntervalSeconds;
		}

		public void setRotationIntervalSeconds(int rotationIntervalSeconds) throws ValidationException
		{
			check(esb, "ChangeLoggerParameters");

			ValidationException.min(1, rotationIntervalSeconds);
			this.rotationIntervalSeconds = rotationIntervalSeconds;
		}

		@SupplierOnly
		public String getTimeFormat()
		{
			check(esb, "ViewLoggerParameters");
			return timeFormat;
		}

		@SupplierOnly
		public void setTimeFormat(String timeFormat) throws ValidationException
		{
			check(esb, "ChangeLoggerParameters");

			ValidationException.validateTimeFormat(timeFormat);
			this.timeFormat = timeFormat;
		}

		public String getDirectoryName()
		{
			check(esb, "ViewLoggerParameters");
			return directoryName;
		}

		public void setDirectoryName(String directoryName) throws ValidationException
		{
			check(esb, "ChangeLoggerParameters");
			this.directoryName = directoryName;
		}

		public String getInterimFileName()
		{
			check(esb, "ViewLoggerParameters");
			return interimFileName;
		}

		public void setInterimFileName(String interimFileName) throws ValidationException
		{
			check(esb, "ChangeLoggerParameters");
			this.interimFileName = interimFileName;
		}

		public int getMaxFileSizeInMegs()
		{
			check(esb, "ViewLoggerParameters");
			return maxFileSizeInMegs;
		}

		public void setMaxFileSizeInMegs(int maxFileSizeInMegs) throws ValidationException
		{
			check(esb, "ChangeLoggerParameters");

			ValidationException.min(1, maxFileSizeInMegs);
			this.maxFileSizeInMegs = maxFileSizeInMegs;
		}

		public String Rotate()
		{
			check(esb, "PerformLogfileRotate");
			logger.info("Performing a Manual Rotation");
			String result = LoggerService.this.rotate();
			setConfiguration(this);
			return result;
		}

		@Override
		public boolean load(IDatabaseConnection databaseConnection)
		{
			if (!super.load(databaseConnection))
			{
				return false;
			}

			return true;
		}

		@Override
		public void validate() throws ValidationException
		{

		}

		@Override
		public String getName(String languageCode)
		{
			return "Logger Service";
		}

		@Override
		public String getPath(String languageCode)
		{
			return "Technical Settings";
		}

		@Override
		public long getSerialVersionUID()
		{
			return 114958705L;
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

	}

	private LoggerServiceConfig config = new LoggerServiceConfig();
	private SimpleDateFormat simpleTimeFormat = new SimpleDateFormat(config.timeFormat);
	private static LogbackCircularAppender fifoAppender = new LogbackCircularAppender();

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
	
	private void setHostName()
	{
		try
		{
			hostname = System.getProperty("HOST_HOSTNAME");
			if (hostname == null || hostname.isBlank()) {
				InetAddress addr;
				addr = InetAddress.getLocalHost();
				hostname = addr.getHostName();
			}
		}
		catch (UnknownHostException ex)
		{
		    this.hostname = "localhost";
		}
	}

	@Override
	public boolean start(String[] args)
	{
		setHostName();
		fifoAppender.setTimeFormat(config.timeFormat);
		reconfigureLogger(config);

		mustStop = false;
		isRunning = true;
		logger.info("Logging Started");

		return true;
	}

	@Override
	public void stop()
	{
		mustStop = true;
		isRunning = false;
		logger.info("Logging Stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config)
	{
		this.config = (LoggerServiceConfig) config;
		simpleTimeFormat = new SimpleDateFormat(((LoggerServiceConfig) config).getTimeFormat());
		fifoAppender.setTimeFormat(this.config.timeFormat);

		synchronized (this)
		{
			stop();
			try
			{
				this.wait(100);
			}
			catch (Exception e)
			{
			}
			start(null);
		}
	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return false;
	}

	@Override
	public boolean isFit()
	{
		return isRunning;
	}

	@Override
	public IMetric[] getMetrics()
	{
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ILogger Implementation
	//
	// /////////////////////////////////
	
	private void addCommonContextData(boolean fatal, Object origin, String transactionID)
	{
		if (transactionID != null && transactionID.length() > 0)
		{
			logger.error("Wow, a real transaction ID "+transactionID);
		}
		MDC.put(LoggingConstants.CONST_LOG_ORIGIN, origin.toString());
		String localTid = (transactionID == null)?"":transactionID;
		localTid = localTid.trim();
		if (localTid.length() <= 0)
		{

			String threadTid = threadTransactionID.get();
			if (threadTid != null && threadTid.trim().length() > 0)
				localTid = threadTid.trim();
		}
		MDC.put(LoggingConstants.CONST_LOG_TRANSID, localTid);
		
		MDC.put(LoggingConstants.CONST_LOG_FATAL, fatal);
		MDC.put(LoggingConstants.CONST_LOG_TIME_FORMAT, config.timeFormat);
		
		MDC.put(LoggingConstants.CONST_LOG_HOSTNAME, this.hostname);
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ILoggerConfig Implementation
	//
	// /////////////////////////////////

	@Override
	public String getDirectory()
	{
		return config.directoryName;
	}

	@Override
	public String getTimeFormat()
	{
		return config.timeFormat;
	}

	@Override
	public String getRotatedFilename()
	{
		return config.rotatedNameFormat;
	}

	@Override
	public String getInterimFilename()
	{
		return config.interimFileName;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	

	private synchronized String rotate()
	{
		// Result
		String result = "No file to Rotate";

		// If the interim file exists
		File interimFile = new File(getInterimFilePath());
		if (interimFile.exists())
		{
			// Compose new name
			rotationCounter++;
			Date now = new Date();
			String rotatedFileName = String.format(config.rotatedNameFormat, HostInfo.getNameOrElseHxC(), simpleTimeFormat.format(lastWriteTime), simpleTimeFormat.format(now), rotationCounter);

			// Rename the file
			String rotationFileName = config.directoryName + "/" + rotatedFileName;
			File rotatedFile = new File(rotationFileName);
			interimFile.renameTo(rotatedFile);
			result = String.format("Rotated to %s", rotationFileName);

			lastWriteTime = now;
		}

		// Calculate next rotation time
		int rotationIntervalSeconds = config.rotationIntervalSeconds;
		DateTime next = DateTime.getNow().addSeconds(rotationIntervalSeconds);
		if (rotationIntervalSeconds > 3600)
		{
			long time = next.getTime();
			long fraction = time % 3600000L;
			if (fraction != 0)
				next = new DateTime(time - fraction);
		}
		nextRotationTime = next;
		result = String.format("%s\nNext Rotation at %s", result, simpleTimeFormat.format(nextRotationTime));
		//info(this, result);

		return result;
	}

    public boolean getSkipArchival()
	{
		return false;
	}

    public Integer getArchiveAfterDays()
	{
		return null;
	}

    public Integer getDeleteAfterDays()
	{
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	/*
	 * 	private LoggingLevels loggingLevel = LoggingLevels.INFO;

		// 1:time,2:level,3:transactionID,4:class,5:method,6:line,7:text
		private String lineFormat = "%1$s|%2$-5s|%3$-20s|%4$-" + MAX_ORIGIN_CLASS_LEN + "s|%5$-" + MAX_ORIGIN_METHOD_LEN + "s|%6$5d|%7$s";
	 */
	private static void reconfigureLogger(LoggerServiceConfig config)
	{
		String directory = config.getDirectoryName();
		if (new File(directory).mkdirs())
			System.setProperty("C4U_LOG_DIR", directory);
		else
			System.setProperty("C4U_LOG_DIR", "/var/opt/cs/c4u/log");
		
		System.setProperty("LOGFILENAME", config.getInterimFileName());
		System.setProperty("FILEROTATIONSIZE", String.valueOf(config.getMaxFileSizeInMegs()));
		System.setProperty("LOGLEVEL", config.getLoggingLevel().toString().toUpperCase());
		
		// assume SLF4J is bound to logback in the current environment
	    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
	    try
	    {
	      JoranConfigurator configurator = new JoranConfigurator();
	      ClassLoader classLoader = LoggerService.class.getClassLoader();
	      configurator.setContext(context);
	      // Call context.reset() to clear any previous configuration, e.g. default 
	      // configuration. For multi-step configuration, omit calling context.reset().
	      context.reset();
	      String loggingConfigFilename = "logback.xml";
	      /*if (CmdArgs.developEnabled) {
	    	  loggingConfigFilename = "etc/logback-config-devel.xml";
	      }*/
	      
	      InputStream loggingConfig = classLoader.getResourceAsStream(loggingConfigFilename);
	      if (loggingConfig != null) {
	    	  configurator.doConfigure(loggingConfig);
	      }
	      
	      fifoAppender.setName("FIFOAppender");
	      fifoAppender.setContext(context);
	      fifoAppender.setName("FIFOAppender");
	      fifoAppender.start();
	      
	      ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
	      root.addAppender(fifoAppender);
	    }
	    //catch (JoranException je)
	    catch (Exception je)
	    {
	      // StatusPrinter will handle this
	    }
	    StatusPrinter.printInCaseOfErrorsOrWarnings(context);
	}

	@Override
	public String getLogHistory()
	{
		return fifoAppender.getLogHistory();
	}
}
