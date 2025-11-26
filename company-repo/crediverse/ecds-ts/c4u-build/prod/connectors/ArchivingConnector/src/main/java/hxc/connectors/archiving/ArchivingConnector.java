package hxc.connectors.archiving;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.Config;
import hxc.configuration.IConfiguration;
import hxc.configuration.IServiceInfoConfig;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IConnector;
import hxc.connectors.archiving.archiver.Archiver;
import hxc.connectors.archiving.archiver.IArchiver;
import hxc.connectors.ctrl.ICtrlConnector;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.database.IDatabaseInfoConfig;
import hxc.connectors.snmp.ISnmpConnector;
import hxc.connectors.snmp.IncidentSeverity;
import hxc.servicebus.IServiceBus;
import hxc.services.logging.ILoggerInfoConfig;
import hxc.services.notification.INotifications;
import hxc.services.notification.IPhrase;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.security.SupplierOnly;
import hxc.services.transactions.ITransactionInfoConfig;
import hxc.utils.calendar.DateTime;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.instrumentation.Metric;
import hxc.utils.instrumentation.ValueType;
import hxc.utils.thread.ScheduledThread;
import hxc.utils.thread.TimedThread;

public class ArchivingConnector implements IConnector
{
	final static Logger logger = LoggerFactory.getLogger(ArchivingConnector.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private IDatabase database;
	private ITransactionInfoConfig cdr;
	private ILoggerInfoConfig log;
	private IDatabaseInfoConfig data;
	//private TimedThread archivingThread;
	private ISnmpConnector snmpConnector;
	protected ICtrlConnector controlConnector;


	private IArchiver archiver;
	private BackupThread archivingThread;
	private volatile boolean started = false;
	

	private final int LOG_SEQUENCE = 0;
	private final int CDR_SEQUENCE = 1;
	private final int DATABASE_SEQUENCE = 2;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Metric Data
	//
	// /////////////////////////////////

	private String backupName = "none";
	private Metric lastBackup = Metric.CreateSimple("Last Database Backup", "Time", ValueType.InstantaneousCount, 300000);

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

	public class BackupThread extends ScheduledThread
	{
		public BackupThread(String serviceName, ScheduledExecutorService scheduledExecutorService, ISnmpConnector snmpConnector)
		{
			super(serviceName, scheduledExecutorService, snmpConnector);
		}

		public BackupThread()
		{
			super(ArchivingConnector.this.getConfiguration().getName(IPhrase.ENG), esb.getScheduledThreadPool(), snmpConnector);
		}

		public void runActual() throws Throwable
		{
			ArchivingConnector.this.backup();
		}
	};

	@Override
	public boolean start(String[] args)
	{
		logger.info("{}({}).start: config = {}", this.getClass().getName(), Integer.toHexString(this.hashCode()), config);
		logger.info("ArchivingConnector.start: ...");

		this.database = esb.getFirstConnector(IDatabase.class);
		if (this.database == null)
		{
			logger.warn("Failed to get IDatabase.class");
			return false;
		}

		this.log = esb.getFirstService(ILoggerInfoConfig.class);
		if (this.log == null)
		{
			logger.warn("Failed to get ILoggerInfoConfig.class");
			return false;
		}

		cdr = esb.getFirstService(ITransactionInfoConfig.class);
		if (cdr == null)
		{
			logger.warn("Failed to get ITransactionInfoConfig.class");
			return false;
		}

		data = esb.getFirstConnector(IDatabaseInfoConfig.class);
		if (data == null)
		{
			logger.warn("Failed to get IDatabaseInfoConfig.class");
			return false;
		}

		snmpConnector = esb.getFirstConnector(ISnmpConnector.class);
		if (snmpConnector == null)
		{
			logger.warn("Failed to get ISnmpConnector.class");
			return false;
		}

		controlConnector = esb.getFirstConnector(ICtrlConnector.class);
		if (controlConnector == null)
		{
			logger.warn("Failed to get ICtrlConnector.class");
			return false;
		}

		// Used for compressing files
		archiver = new Archiver();

		this.archivingThread = new BackupThread();
		if ( config.archivingCheckTimeOfDay != null )
		{
			this.archivingThread.start(config.archivingCheckTimeOfDay,24 * 60 * 60, config.minInitialDelay);
		}
		else
		{
			this.archivingThread.start(null,config.archivingCheckIntervalSeconds, config.minInitialDelay);
		}
		/*
		// A thread that runs continously backing up files and archiving them, as well as cleanup
		archivingThread = new TimedThread("Archiving Connector Thread", config.archivingCheckIntervalSeconds * 1000L, TimedThreadType.INTERVAL)
		{
			@Override
			public void action()
			{
				try
				{
					backup();
				}
				catch (Throwable e)
				{
					logger.log(this, e);
				}
			}
		};
		logger.trace(this, "Starting archivingThread with {} interval", config.archivingCheckIntervalSeconds);
		archivingThread.start();
		*/

		logger.info("Archiving Connector has started.");

		this.started = true;
		return true;
	}

	@Override
	public void stop()
	{
		// Kill the archiving thread
		//archivingThread.kill();
		//archivingThread = null;
		this.archivingThread.stop();
		archiver = null;

		logger.info("Archiving Connector has stopped.");
		this.started = false;
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		logger.trace("{}({}).setConfiguration: config = {}", this.getClass().getName(), Integer.toHexString(this.hashCode()), config);
		this.config = (ArchivingConfiguration) config;

		stop();

		try
		{
			TimedThread.sleep(500);
		}
		catch (InterruptedException e)
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
		return archivingThread != null && archivingThread.isRunning() && archiver != null;
	}

	@Override
	public IConnection getConnection(String optionalConnectionString) throws IOException
	{
		return null;
	}

	@Override
	public IMetric[] getMetrics()
	{
		return null;
	}

	public static long parseTimeOfDay(String timeOfDayString, String fieldName) throws ValidationException
	{
		ValidationException.validate(timeOfDayString, "\\d\\d:\\d\\d:\\d\\d", fieldName);
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = null;
		long seconds = -1;
		try
		{
			date = dateFormat.parse(timeOfDayString);
			seconds = (date.getTime() - dateFormat.parse("00:00:00").getTime())/1000;
			if (seconds >= 24*60*60)
				throw ValidationException.createFieldValidationException(fieldName, String.format("Time of day %s cannot be later than 23:59:59 (seconds = %s)", timeOfDayString, seconds));
		}
		catch( ParseException exception )
		{
			throw ValidationException.createFieldValidationException(fieldName, String.format("Could not parse %s as HH:mm", timeOfDayString), exception);
		}
		return seconds;
		//return new DateTime(date).getSecondsSinceMidnight();
	}

	public static String formatTimeOfDay(long timeOfDay)
	{
		long hourOfDay = (timeOfDay / (60 * 60));
		long minute = (timeOfDay % (60 * 60)) / 60;
		long second = (timeOfDay % (60 * 60)) % 60;
		return String.format("%02d:%02d:%02d", hourOfDay, minute, second);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////
	@Perms(perms = { @Perm(name = "ViewArchivingParameters", description = "View Archiving Connector Parameters", category = "Archiving", supplier = true),
			@Perm(name = "ChangeArchivingParameters", implies = "ViewArchivingParameters", description = "Change Archiving Connector Parameters", category = "Archiving", supplier = true),
			@Perm(name = "PerformArchivingParameters", description = "Perform Archiving Connector Calls", category = "Archiving", supplier = true) })
	class ArchivingConfiguration extends ConfigurationBase
	{

		private long archivingCheckIntervalSeconds = 43200L;
		private String timeFormat = "yyyyMMdd'T'HHmmss";
		private int zipAfterDaysOld = 30;
		private String zipNameFormat = "%1$s-%2$s.zip"; // %1$s: File Type %2$s: Date
		private int deleteAfterDaysOld = 90;
		private Long archivingCheckTimeOfDay = null;
		private boolean incumbentOnly = false;
		private int minInitialDelay = 5 * 60;
		private String finalizationCommand = "";


		private ArchivingDestinationConfiguration destinations[] = { new ArchivingDestinationConfiguration("Log Files Destinations", LOG_SEQUENCE),
				new ArchivingDestinationConfiguration("CDR Files Destinations", CDR_SEQUENCE), new ArchivingDestinationConfiguration("Database Backup Destinations", DATABASE_SEQUENCE) };

		@Config(description = "Finalization Command", comment = "command to run after backup completes.")
		public String getFinalizationCommand()
		{
			check(esb, "ViewArchivingParameters");
			return this.finalizationCommand;
		}

		public void setFinalizationCommand( String finalizationCommand )
		{
			check(esb, "ChangeArchivingParameters");
			this.finalizationCommand = finalizationCommand;
		}

		@Config(description = "Minimum update Delay", comment = "For testing purposes only, to specify time (in seconds) from update to schedule execution.")
		public int getMinInitialDelay()
		{
			check(esb, "ViewArchivingParameters");
			return this.minInitialDelay;
		}

		@SupplierOnly
		public void setMinInitialDelay( int minInitialDelay )
		{
			check(esb, "ChangeArchivingParameters");
			this.minInitialDelay = minInitialDelay;
		}

		@Config(description = "Archive Interval", comment = "Value in seconds. Only applied if no Archive Start time specified")
		public long getArchivingCheckIntervalSeconds()
		{
			check(esb, "ViewArchivingParameters");
			return archivingCheckIntervalSeconds;
		}

		public void setArchivingCheckIntervalSeconds(long archivingCheckIntervalSeconds) throws ValidationException
		{
			logger.trace("{}({}).setArchivingCheckIntervalSeconds: entry ...", this.getClass().getName(), Integer.toHexString(this.hashCode()));
			check(esb, "ChangeArchivingParameters");

			ValidationException.min(10, archivingCheckIntervalSeconds);
			this.archivingCheckIntervalSeconds = archivingCheckIntervalSeconds;
		}

		public void setArchivingCheckTimeOfDay(String archivingCheckTimeOfDayString) throws ValidationException
		{
			check(esb, "ChangeArchivingParameters");
			if (archivingCheckTimeOfDayString.isEmpty())
			{
				this.archivingCheckTimeOfDay = null;
			}
			else
			{
				this.archivingCheckTimeOfDay = parseTimeOfDay(archivingCheckTimeOfDayString, "archivingCheckTimeOfDay");
			}
		}

		@Config(description = "Archive Start Time", comment = "Format HH:mm:ss. Archive Interval shall be 24 hours, when Archive Start Time is specified.")
		public String getArchivingCheckTimeOfDay()
		{
			check(esb, "ViewArchivingParameters");
			if ( this.archivingCheckTimeOfDay == null )
			{
				return "";
			}
			else
			{
				return formatTimeOfDay(this.archivingCheckTimeOfDay);
			}
		}

		public void setIncumbentOnly(boolean incumbentOnly)
		{
			check(esb, "ChangeArchivingParameters");
			this.incumbentOnly = incumbentOnly;
		}

		@Config(description = "Backup Incumbent Node only")
		public boolean getIncumbentOnly()
		{
			check(esb, "ViewArchivingParameters");
			return this.incumbentOnly;
		}

		@SupplierOnly
		public void setTimeFormat(String timeFormat) throws ValidationException
		{
			check(esb, "ChangeArchivingParameters");

			ValidationException.validateTimeFormat(timeFormat);
			this.timeFormat = timeFormat;
		}

		@SupplierOnly
		public String getTimeFormat()
		{
			check(esb, "ViewArchivingParameters");
			return timeFormat;
		}

		public void setZipAfterDaysOld(int zipAfterDaysOld) throws ValidationException
		{
			check(esb, "ChangeArchivingParameters");

			ValidationException.min(0, zipAfterDaysOld);
			this.zipAfterDaysOld = zipAfterDaysOld;
		}

		@Config(description = "Archive after Days Old")
		public int getZipAfterDaysOld()
		{
			check(esb, "ViewArchivingParameters");
			return zipAfterDaysOld;
		}

		@SupplierOnly
		public void setZipNameFormat(String zipNameFormat) throws ValidationException
		{
			check(esb, "ChangeArchivingParameters");

			ValidationException.validateFormat(zipNameFormat);
			this.zipNameFormat = zipNameFormat;
		}

		@SupplierOnly
		@Config(description = "Archive Name format")
		public String getZipNameFormat()
		{
			check(esb, "ViewArchivingParameters");
			return zipNameFormat;
		}

		public void setDeleteAfterDaysOld(int deleteAfterDaysOld) throws ValidationException
		{
			check(esb, "ChangeArchivingParameters");

			ValidationException.min(0, deleteAfterDaysOld);
			this.deleteAfterDaysOld = deleteAfterDaysOld;
		}

		public int getDeleteAfterDaysOld()
		{
			check(esb, "ViewArchivingParameters");
			return deleteAfterDaysOld;
		}

		@Override
		public String getPath(String languageCode)
		{
			return "Technical Settings";
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

		@Override
		public long getSerialVersionUID()
		{
			return -1489974181336850489L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "Archiving Connector";
		}

		@Override
		public void validate() throws ValidationException
		{

		}

		@Override
		public boolean load(IDatabaseConnection databaseConnection)
		{
			logger.info("{}({}).load: entry ...", this.getClass().getName(), Integer.toHexString(this.hashCode()));
			boolean result = super.load(databaseConnection);
			if ( ArchivingConnector.this.started )
			{
				logger.info("{}({}).load: running setConfiguration as started is set ...", this.getClass().getName(), Integer.toHexString(this.hashCode()));
				try
				{
					ArchivingConnector.this.setConfiguration(this);
				}
				catch( ValidationException exception )
				{
					logger.error("load: error while loading config {}", exception);
					snmpConnector.jobFailed(this.getName(IPhrase.ENG), IncidentSeverity.CRITICAL, String.format("Error while loading config %s", exception));
				}
			}
			else
			{
				logger.info("{}({}).load: not running setConfiguration as started is not set ...", this.getClass().getName(), Integer.toHexString(this.hashCode()));
			}
			return result;
		}

		@Override
		public Collection<IConfiguration> getConfigurations()
		{
			Collection<IConfiguration> configs = new ArrayList<IConfiguration>();
			for (int i = 0; i < destinations.length; i++)
			{
				configs.add(destinations[i]);
			}
			return configs;
		}

	}

	private ArchivingConfiguration config = new ArchivingConfiguration();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Archiving Destination Configuration
	//
	// /////////////////////////////////
	@Perms(perms = {
			@Perm(name = "ViewArchivingDestination", description = "View Archiving Destination Parameters", category = "ArchivingDestination", supplier = true),
			@Perm(name = "ChangeArchivingDestination", implies = "ViewArchivingDestination", description = "Change Archiving Destination Parameters", category = "ArchivingDestination", supplier = true) })
	class ArchivingDestinationConfiguration extends ConfigurationBase
	{

		private String name;
		private int sequence;

		private String transferCommand = "scp %1$s root@%2$s";
		private String destinationAddress1 = "";
		private String destinationAddress2 = "";

		private boolean deleteAfterTransfer = false;


		private String transferCommandArchived = "scp %1$s root@%2$s";
		private String archivedDestinationAddress1 = "";
		private String archivedDestinationAddress2 = "";
		private boolean deleteAfterArchivedTransfer = false;

		public boolean getDeleteAfterArchivedTransfer()
		{
			check(esb, "ViewArchivingDestination");
			return this.deleteAfterArchivedTransfer;
		}
		public void setDeleteAfterArchivedTransfer( boolean deleteAfterArchivedTransfer )
		{
			check(esb, "ChangeArchivingDestination");
			this.deleteAfterArchivedTransfer = deleteAfterArchivedTransfer;
		}

		public boolean getDeleteAfterTransfer()
		{
			check(esb, "ViewArchivingDestination");
			return this.deleteAfterTransfer;
		}

		public void setDeleteAfterTransfer( boolean deleteAfterTransfer )
		{
			check(esb, "ChangeArchivingDestination");
			this.deleteAfterTransfer = deleteAfterTransfer;
		}

		public ArchivingDestinationConfiguration()
		{
		}

		public ArchivingDestinationConfiguration(String name, int sequence)
		{
			this.name = name;
			this.sequence = sequence;
		}

		public void setTransferCommand(String transferCommand)
		{
			check(esb, "ChangeArchivingDestination");
			this.transferCommand = transferCommand;
		}

		public String getTransferCommand()
		{
			check(esb, "ViewArchivingDestination");
			return transferCommand;
		}

		public void setDestinationAddress1(String destinationAddress1) throws ValidationException
		{
			check(esb, "ChangeArchivingDestination");
			this.destinationAddress1 = destinationAddress1;
		}

		public String getDestinationAddress1()
		{
			check(esb, "ViewArchivingDestination");
			return destinationAddress1;
		}

		public void setDestinationAddress2(String destinationAddress2) throws ValidationException
		{
			check(esb, "ChangeArchivingDestination");
			this.destinationAddress2 = destinationAddress2;
		}

		public String getDestinationAddress2()
		{
			check(esb, "ViewArchivingDestination");
			return destinationAddress2;
		}

		public void setTransferCommandArchived(String transferCommandArchived)
		{
			check(esb, "ChangeArchivingDestination");
			this.transferCommandArchived = transferCommandArchived;
		}

		public String getTransferCommandArchived()
		{
			check(esb, "ViewArchivingDestination");
			return transferCommandArchived;
		}

		public void setArchivedDestinationAddress1(String archivedDestinationAddress1) throws ValidationException
		{
			check(esb, "ChangeArchivingDestination");
			this.archivedDestinationAddress1 = archivedDestinationAddress1;
		}

		public String getArchivedDestinationAddress1()
		{
			check(esb, "ViewArchivingDestination");
			return archivedDestinationAddress1;
		}

		public void setArchivedDestinationAddress2(String archivedDestinationAddress2) throws ValidationException
		{
			check(esb, "ChangeArchivingDestination");
			this.archivedDestinationAddress2 = archivedDestinationAddress2;
		}

		public String getArchivedDestinationAddress2()
		{
			check(esb, "ViewArchivingDestination");
			return archivedDestinationAddress2;
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
			return Long.parseLong("-62845260713683301" + ((7 + sequence) < 10 ? "0" : "") + (7 + sequence));
		}

		@Override
		public String getName(String languageCode)
		{
			return name;
		}

		@Override
		public void validate() throws ValidationException
		{

		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Archiving Thread Handling
	//
	// /////////////////////////////////

	private long lastModifiedLog = new Date().getTime();
	private long lastModifiedCDR = new Date().getTime();
	private long lastModifiedDBBackup = new Date().getTime();

	// Send, Archive, and Delete Files
	private void backup()
	{
		try
		{
			logger.debug("Archiving process started.");

			// Do backup
			if (config.incumbentOnly == false || controlConnector.isIncumbent(ICtrlConnector.DATABASE_ROLE))
			{
				logger.trace("Creating backup of database.");
				String newBackup = database.backup();
				if (newBackup != null)
				{
					if ( !backupName.equals(newBackup) )
					{
						lastBackup.report(esb, new Date());
					}
				}
				else
				{
					snmpConnector.jobFailed(this.config.getName(IPhrase.ENG), IncidentSeverity.CRITICAL, String.format("Failed to create database dump"));
				}
			}
			else
			{
				logger.trace("Not creating backup of database as database role is not incumbent");
			}

			// Sending Retreive
			retrieveAndSend(log, lastModifiedLog, LOG_SEQUENCE);
			lastModifiedLog = lastModifiedDate;
			retrieveAndSend(cdr, lastModifiedCDR, CDR_SEQUENCE);
			lastModifiedCDR = lastModifiedDate;
			retrieveAndSend(data, lastModifiedDBBackup, DATABASE_SEQUENCE);
			lastModifiedDBBackup = lastModifiedDate;

			// Archiving
			String logFile = archive(log);
			String cdrFile = archive(cdr);
			String dbFile = archive(data);

			// Sending
			if (logFile != null)
			{
				sendToDestination(logFile, LOG_SEQUENCE, true);
			}

			if (cdrFile != null)
			{
				sendToDestination(cdrFile, CDR_SEQUENCE, true);
			}

			if (dbFile != null)
			{
				sendToDestination(dbFile, DATABASE_SEQUENCE, true);
			}

			// Delete
			delete(log);
			delete(cdr);
			delete(data);

			finalization();

			logger.debug("Archiving process finished.");
		}
		catch (Exception exc)
		{
			snmpConnector.jobFailed(this.config.getName(IPhrase.ENG), IncidentSeverity.CRITICAL, String.format("Failed to backup %s", exc));
			logger.error("Failed to backup: {}", exc.toString());
		}
	}

	private long lastModifiedDate = 0;

	// Retrieve all the files that were modified after a certain time
	private File[] retrieve(final IServiceInfoConfig serviceInfo, final long lastModified)
	{
		lastModifiedDate = lastModified;
		File files[] = new File(serviceInfo.getDirectory()).listFiles(new FileFilter()
		{

			@Override
			public boolean accept(File pathname)
			{

				// Get the extension of the file
				String extension = serviceInfo.getRotatedFilename();

				if (extension != null)
					extension = extension.substring(extension.lastIndexOf('.'));
				else
					extension = "";

				// Check if file has that extension
				if (pathname.getName().indexOf(extension) <= 0)
				{
					return false;
				}

				// Check the last modified
				if (pathname.lastModified() > lastModified)
				{
					if (pathname.lastModified() > lastModifiedDate)
					{
						lastModifiedDate = pathname.lastModified();
					}
					return true;
				}

				return false;
			}

		});

		return files;
	}

	// Retrieves old files and sends it to the destinations
	private void retrieveAndSend(IServiceInfoConfig serviceInfo, long lastModified, int sequence)
	{
		if (archivingThread == null || archivingThread.isStopped())
		{
			logger.trace("retrieveAndSend: doing nothing as component is not running");
			return;
		}

		logger.trace("Retrieving {} files since {}.", serviceInfo.getDirectory(), new Date(lastModified));

		// Retrieve the files
		File files[] = retrieve(serviceInfo, lastModified);
		lastModified = lastModifiedDate;
		if (files != null && files.length > 0)
		{
			// Iterate through the files
			for (File file : files)
			{
				if (file == null)
				{
					return;
				}

				// Send the file to both destinations
				sendToDestination(file.getAbsolutePath(), sequence, false);
			}
		}
	}

	private void finalization()
	{
		if (archivingThread == null || archivingThread.isStopped())
		{
			logger.trace("finalization: doing nothing as component is not running");
			return;
		}
		if (config.finalizationCommand.isEmpty())
		{
			logger.trace("finalization: doing nothing as finalizationCommand is empty");
			return;
		}
		try
		{
			logger.trace("finalization: running {}", config.finalizationCommand);
			Process process = new ProcessBuilder( Arrays.asList( config.finalizationCommand.split( "\\s+" ) ) ).inheritIO().start();
			int rc = process.waitFor();
			logger.info("finalization: command completed with {} - command: {}", rc, config.finalizationCommand);
			if ( rc != 0 ) throw new RuntimeException(String.format("finalizationCommand returned non zero code %s", rc));
		}
		catch(Exception exception)
		{
			logger.error("finalization: failed to run finalization command {} : {}", config.finalizationCommand, exception);
			snmpConnector.jobFailed(this.config.getName(IPhrase.ENG), IncidentSeverity.CRITICAL, String.format("finalization: failed to run finalization command %s : %s", config.finalizationCommand, exception));
		}
	}

	// Compress the files that are older than config.zipAfterDaysOld
	private String archive(IServiceInfoConfig serviceInfo) throws Exception
	{
		if (archivingThread == null || archivingThread.isStopped())
		{
			logger.trace("archive: doing nothing as component is not running");
			return null;
		}

		int zipAfterDaysOld = ( serviceInfo.getArchiveAfterDays() != null ? serviceInfo.getArchiveAfterDays() : config.zipAfterDaysOld );

		if ( serviceInfo.getSkipArchival() )
		{
			logger.trace("Not zipping {} files that are {} days old (skipArchival == true).", serviceInfo.getDirectory(), zipAfterDaysOld);
			return null;
		}

		logger.trace("Zipping {} files that are {} days old.", serviceInfo.getDirectory(), zipAfterDaysOld);

		String interimfilename = serviceInfo.getInterimFilename();

		// Get the files that are a certain amount of days old
		File files[] = filesInDir(new File(serviceInfo.getDirectory()), zipAfterDaysOld, interimfilename);
		if (files == null || files.length == 0)
		{
			logger.trace("No files in {} files that are {} days old.", serviceInfo.getDirectory(), zipAfterDaysOld);
			return null;
		}

		// Get the name of the type of files
		if (interimfilename != null)
			interimfilename = interimfilename.substring(0, (interimfilename.lastIndexOf('.') > 0) ? interimfilename.lastIndexOf('.') : interimfilename.length());
		else
			interimfilename = serviceInfo.getClass().getSimpleName();

		// Generate a name for the compressed file
		String filename = serviceInfo.getDirectory() + "/" + String.format(config.zipNameFormat, interimfilename + "s", DateTime.getNow().toString(config.timeFormat));

		// Archive the file
		archiver.archive(files, filename);

		// Delete the files that have been backed up
		for (File file : files)
		{
			logger.trace("Deleting {}", file);
			file.delete();
		}

		// Return the file name if the archive was a success
		return filename;
	}

	// Delete old files that are older than config.deleteAfterDaysOld
	private void delete(IServiceInfoConfig serviceInfo)
	{
		logger.trace("delete: serviceInfo = {}", serviceInfo);
		if (archivingThread == null || archivingThread.isStopped())
		{
			logger.trace("delete: serviceInfo = {}: doing nothing as component is not running", serviceInfo);
			return;
		}

		int deleteAfterDaysOld = ( serviceInfo.getDeleteAfterDays() != null ? serviceInfo.getDeleteAfterDays() : config.deleteAfterDaysOld );

		logger.trace("Deleting {} files that are {} days old.", serviceInfo.getDirectory(), deleteAfterDaysOld);

		// Iterate through files
		for (File file : filesInDir(new File(serviceInfo.getDirectory()), deleteAfterDaysOld))
		{
			logger.trace("Checking {}", file);
			// Ensure the file is not being written to
			if (!file.getName().toLowerCase().contains(serviceInfo.getInterimFilename().toLowerCase()))
			{
				logger.trace("deleting {}", file);
				file.delete();
			}
			else
			{
				logger.trace("Not deleting {}", file);
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Method
	//
	// /////////////////////////////////

	// Retrieves files older than 'condition' days
	private File[] filesInDir(File folder, final int condition)
	{
		return filesInDir(folder, condition, null);
	}

	// Retrieves files older than 'condition' days that doesn't have a filename equal to notFilename
	private File[] filesInDir(File folder, final int condition, final String notFilename)
	{
		// List files with a filter
		File files[] = folder.listFiles(new FileFilter()
		{

			@Override
			public boolean accept(File pathname)
			{
				// Ensure it is not a zip file
				if (pathname.getName().substring(pathname.getName().lastIndexOf('.') + 1).equalsIgnoreCase("zip"))
				{
					return false;
				}

				// Ensure it doesn't have the filename 'notFilename'
				if (notFilename != null)
				{
					if (pathname.getName().equalsIgnoreCase(notFilename))
					{
						return false;
					}
				}

				// Get calendars for the file and for the current date
				Calendar then = Calendar.getInstance();
				then.setTimeZone(TimeZone.getTimeZone("UTC"));
				then.setTimeInMillis(pathname.lastModified());

				Calendar now = Calendar.getInstance();
				now.setTimeZone(TimeZone.getTimeZone("UTC"));
				now.setTime(new Date());

				// Check the amount of days old the file is
				int daysOld = now.get(Calendar.DAY_OF_YEAR) - then.get(Calendar.DAY_OF_YEAR);
				if (daysOld >= condition)
				{
					return true;
				}

				// Else leave it out of the list of files
				return false;
			}

		});
		return files;
	}

	// Sends the file to a destination using the transfer command
	private boolean sendFileToDest(String transfer, String file, String destination)
	{
		logger.trace("sendToDestination: transfer = {}, file = {}, destination = {}", transfer, file, destination);
		// Ensure all fields are present
		if (transfer == null || file == null || destination == null)
		{
			logger.info("sendToDestination: doing nothing as one of the following is null transfer = {}, file = {}, destination = {}", transfer, file, destination);
			return false;
		}

		// Send file
		if (!send(transfer, file, destination))
		{
			// Try send file again if it failed
			logger.warn("Failed to send {} to {}. Trying again.", file, destination);
			if (!send(transfer, file, destination))
			{
				logger.error("Failed to send {} to {}.", file, destination);
				return false;
			}
		}
		return true;
	}

	// Executes a terminal command
	private boolean executeCommand(String command)
	{
		logger.trace("Executing command: {}", command);
		try
		{
			// Execute the command
			Process process = Runtime.getRuntime().exec(command);
			int exit = process.waitFor();

			// Check if the command executed successfully
			if (exit == 0)
			{
				return true;
			}

			logger.warn("Copy operation returned an exit code of: {}", exit);
			snmpConnector.jobFailed(this.config.getName(IPhrase.ENG), IncidentSeverity.CRITICAL, String.format("executeCommand: command returned non zero code %s : %s", command, exit));
		}
		catch (Exception exc)
		{
			logger.error("Failed to execute copy command: {}", exc.getMessage());
			snmpConnector.jobFailed(this.config.getName(IPhrase.ENG), IncidentSeverity.CRITICAL, String.format("executeCommand: failed to run command %s : %s", command, exc));
		}

		return false;
	}

	// Executes the transfer command with the file and destination
	private boolean send(String transfer, String file, String destination)
	{
		return executeCommand(String.format(transfer, file, destination));
	}

	// Sends files to destinations
	private void sendToDestination(String file, int sequence, boolean archived)
	{
		ArchivingDestinationConfiguration destinationInfo = config.destinations[sequence];
		logger.trace("sendToDestination: file = {}, sequence = {}, archived = {}, destinationInfo.deleteAfterTransfer = {}, config.deleteAfterArchivedTransfer = {}",
			file, sequence, archived, destinationInfo.deleteAfterTransfer, destinationInfo.deleteAfterArchivedTransfer);
		// Check the sequence matches a valid destination in the config.destinations array
		if (config.destinations[sequence] == null || config.destinations[sequence].sequence != sequence)
		{
			return;
		}


		// Either use the transfer command for archived files or non archived files
		// Commands were separated to allow different destinations for the archived and non archived files
		String transfer = (archived) ? config.destinations[sequence].transferCommandArchived : config.destinations[sequence].transferCommand;

		// Get the destination
		boolean deleteAfter = (archived) ? destinationInfo.deleteAfterArchivedTransfer : destinationInfo.deleteAfterTransfer;
		String destination = (archived) ? config.destinations[sequence].archivedDestinationAddress1 : config.destinations[sequence].destinationAddress1;

		boolean sent = false;
	
		// Send the file to the destination
		if ((destination != null && destination.length() > 0) || !(transfer.contains("%2$s")))
		{
			sent = sent || sendFileToDest(transfer, file, destination);
		}

		// Get the second destination
		destination = (archived) ? config.destinations[sequence].archivedDestinationAddress2 : config.destinations[sequence].destinationAddress2;

		// Send the file to the destination
		if ((destination != null && destination.length() > 0) || !(transfer.contains("%2$s")))
		{
			sent = sent || sendFileToDest(transfer, file, destination);
		}
		logger.trace("sendToDestination: deleteAfter = {}, archived = {}, sent = {}", deleteAfter, archived, sent);
		if ( deleteAfter && sent )
		{
			logger.info("sendToDestination: deleting after trasnfer {}", file);
			try
			{
				new File(file).delete();
			}
			catch(Exception exception)
			{
				logger.error("sendToDestination: failed to delete transfered file {} : {}", file, exception);
				snmpConnector.jobFailed(this.config.getName(IPhrase.ENG), IncidentSeverity.CRITICAL, String.format("Failed to delete transferred file %s : %s", file, exception));
			}
		}
		else
		{
			logger.info("sendToDestination: NOT deleting (i.e. keeping) after trasnfer {}", file);
		}
	}
}
