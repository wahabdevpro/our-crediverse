package hxc.connectors.file;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IConnector;
import hxc.connectors.ctrl.ICtrlConnector;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.file.watcher.FileWatcherProcessHandler;
import hxc.connectors.file.watcher.IFileWatcherMediator;
import hxc.servicebus.HostInfo;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.INotifications;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.instrumentation.Metric;
import hxc.utils.thread.TimedThread;
import hxc.utils.thread.TimedThread.TimedThreadType;
import hxc.utils.watcher.FileWatcher;

public class FileConnector implements IConnector, IFileConnector, IFileWatcherMediator
{
	final static Logger logger = LoggerFactory.getLogger(FileConnector.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private FileWatcher fileWatchers[];
	private IDatabase database;
	private ICtrlConnector control;
	private TimedThread failSafe;
	private boolean skip;
	private final int updateTime = 60000;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Metric Data
	//
	// /////////////////////////////////
	private Metric fileInfoMetric = Metric.CreateGraph("File Info", updateTime * 10, "Records", "Successful Records", "Failed Records", "Captured Records");

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
		database = esb.getFirstConnector(IDatabase.class);
		if (database == null)
			return false;

		control = esb.getFirstConnector(ICtrlConnector.class);
		if (control == null)
			return false;

		try
		{
			// Initialise the file watchers
			initiateFileWatchers();
		}
		catch (Exception exc)
		{
			logger.error("Could not initiate File watchers. Error: {}", exc.getMessage());
			return false;
		}

		// Create a fail safe thread to check that the file records
		failSafe = new TimedThread("File Connector Fail Safe Thread", updateTime, TimedThreadType.INTERVAL)
		{

			@Override
			public void action()
			{
				logger.trace("Checking Server Roles for all File Types");

				if (skip || isInterrupted())
				{
					skip = false;
					return;
				}

				// Iterate through the config records
				for (ConfigRecord record : config.fileConfigs)
				{
					// Ensure that the record is not null
					if (record == null)
						continue;

					// Check if the record has an incumbent machine to process those files
					if (record.getServerRole() != null)
					{
						if (record.getServerRole().equalsIgnoreCase(HostInfo.getName()) || record.getServerRole().equalsIgnoreCase("localhost"))
						{
							continue;
						}
					}

					// Else check if the other machine has it as an incumbent machine
					control.checkFileServerRoleHasIncumbent(record.getInputDirectory(), record.getFileType());
				}
			}
		};
		failSafe.start();

		logger.info("File Connector Started.");

		return true;
	}

	@Override
	public void stop()
	{
		// Stop all the file watchers
		if (fileWatchers != null)
		{
			for (FileWatcher watcher : fileWatchers)
			{
				if (watcher != null)
				{
					watcher.destroy();
					watcher = null;
				}
			}
		}

		// Kill the fail safe
		skip = true;
		failSafe.kill();

		logger.info("File Connector Stopped.");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		this.config = (FileConnectorConfiguration) config;

		try
		{
			stop();
		}
		catch (Exception e)
		{
			logger.error("Problem stopping File connector with error: " + e.getMessage());
		}

		try
		{
			if (failSafe != null)
			{
				failSafe.setWaitTime(1000);
				failSafe.sleep();
			}
		}
		catch (Exception e)
		{
		}

		try
		{
			start(null);
		}
		catch (Exception e)
		{
			logger.error("Problem starting File connector with error: " + e.getMessage());
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
		for (FileWatcher watcher : fileWatchers)
		{
			if (watcher == null)
			{
				logger.error("File watcher is null.");
				return false;
			}
		}

		if (failSafe == null)
		{
			logger.error("Fail Safe thread has stopped working.");
		}

		return failSafe != null && failSafe.isAlive();
	}

	@Override
	public IConnection getConnection(String optionalConnectionString) throws IOException
	{
		return null;
	}

	@Override
	public IMetric[] getMetrics()
	{
		return new IMetric[] { fileInfoMetric };
	}

	// Initialise the file watchers for each file record
	public void initiateFileWatchers() throws Exception
	{
		logger.trace("Initiating File Watchers.");

		// Get a unique list of directories to watch and create that amount of file watchers
		fileWatchers = new FileWatcher[uniqueDirectories()];

		// Ensure the length at least one or more
		if (fileWatchers == null || fileWatchers.length == 0)
			return;

		// Gets a hash map of the records and directory
		HashMap<String, ArrayList<ConfigRecord>> paths = getUniqueDirRecords();

		// Counter
		int i = 0;

		// Iterate through the keyset of the hash map
		for (final String directory : paths.keySet())
		{
			// Keep a reference to the regex's from the records
			String regex = "";

			// Keep track of the records
			HashMap<String, ConfigRecord> records = new HashMap<>();

			// Keep track of the type of file it is, at the moment only CSV is implemented
			FileProcessorType type = FileProcessorType.CSV;

			// Iterate through the records
			for (ConfigRecord record : paths.get(directory))
			{
				// Ensure the record is not null
				if (record == null)
					continue;

				// Get the server role and ensure this machine will process it
				String serverRole = record.getServerRole();
				if (serverRole == null || serverRole.equalsIgnoreCase("localhost") || serverRole.equalsIgnoreCase(HostInfo.getName()))
				{
					// Add the regex to string
					regex += convertWildcardToRegex(record.getFilenameFilter()) + "|";

					// Add the record to the records variable
					records.put(convertWildcardToRegex(record.getFilenameFilter()), record);
				}
			}

			// Check if directory exists and try to create it if it does not
			try
			{
				File folder = new File(directory);
				if (!folder.exists() && !folder.mkdirs())
				{
					logger.error("Directory {} is not writable or you have invalid permission.", directory);
					continue;
				}
			}
			catch (Exception ex)
			{
				logger.error(ex.getMessage(), ex);
				continue;
			}

			// Check that there is regex
			if (regex == null || regex.trim().length() == 0)
				continue;

			// Remove the last '|' from the regex
			regex = regex.substring(0, regex.lastIndexOf('|'));

			// Create the file watcher
			fileWatchers[i] = new FileWatcher();
			fileWatchers[i].registerDirectory(directory);
			fileWatchers[i].applyFilter(regex);
			fileWatchers[i].setIncludeModifed(false);

			// Create a file watcher process handler to process the files
			final FileWatcherProcessHandler fileWatcherProcessHandler = new FileWatcherProcessHandler(type, records, //
					this, new FileProcessHandler());
			fileWatchers[i++].processEvents(fileWatcherProcessHandler);

			// Process any existing files in the directory
			final String finalRegex = regex;
			new TimedThread("File Watcher " + i + " Initialiser Thread", 500)
			{
				@Override
				public void action()
				{
					File input = new File(directory);
					for (File f : input.listFiles(new FilenameFilter()
					{
						@Override
						public boolean accept(File dir, String name)
						{
							return name.matches(finalRegex);
						}
					}))
					{
						fileWatcherProcessHandler.processNewFile(f);
					}
				}
			}.start();
		}

		logger.trace("Finished Initiating File Watchers.");
	}

	// Register the progress of the current file
	private synchronized void registerDTO(FileDTO dto)
	{
		// Ensure it is not null
		if (dto == null)
			return;

		// Create the progress record to keep track
		ProgressRecord progress = new ProgressRecord();
		progress.filename = dto.filename;
		progress.recordNo = dto.recordNo;

		try (IDatabaseConnection connection = database.getConnection(null))
		{

			// Create the table if it does not exist
			if (!connection.tableExists(ProgressRecord.class))
			{
				connection.createTable(ProgressRecord.class);
			}

			// Update or insert the record into the database
			connection.upsert(progress);

		}
		catch (Exception e)
		{
			logger.error("Could not update the progress of the file.");
		}
	}

	// Register what file has been completed or still being processed
	private synchronized void registerFile(File file, boolean completed)
	{
		try (IDatabaseConnection connection = database.getConnection(null))
		{

			// Create the table if it does not exist
			if (!connection.tableExists(FileRecord.class))
			{
				connection.createTable(FileRecord.class);
			}

			// Check if the record already exists, if not then add the details
			FileRecord record = connection.select(FileRecord.class, " where filename = %s", file.getName());
			if (record == null)
			{
				logger.trace("Registering file {} on to database.", file.getName().substring(file.getName().indexOf('/') + 1));

				// Create the file record
				record = new FileRecord();
				record.filename = file.getName();
				record.firstReceived = new Date();
				record.inputDirectory = file.getParent();
			}

			// Set whether the file is completed
			record.completed = completed;

			if (completed)
				logger.trace("Registering Completed Processing {}.", file.getName().substring(file.getName().indexOf('/') + 1));

			// Insert the record into the database
			connection.upsert(record);

		}
		catch (Exception e)
		{
			logger.error("Could not register file record into database. Error: {}", e);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////
	@Perms(perms = { @Perm(name = "ViewFileParameters", description = "View File Parameters", category = "File", supplier = true),
			@Perm(name = "ChangeFileParameters", implies = "ViewFileParameters", description = "Change File Parameters", category = "File", supplier = true) })
	public class FileConnectorConfiguration extends ConfigurationBase
	{
		// List of Configuration Records
		public ConfigRecord[] fileConfigs = new ConfigRecord[] { //
		new ConfigRecord(1, "*v3.0.TNP", "/tmp/c4u", "/tmp/c4u/done", FileProcessorType.CSV, FileType.ThresholdNotificationFileV3, HostInfo.getName(), false) };

		public FileConnectorConfiguration()
		{
		}

		public ConfigRecord[] getFileConfigs()
		{
			check(esb, "ViewFileParameters");
			return fileConfigs;
		}

		public void setFileConfigs(ConfigRecord[] fileConfigs)
		{
			check(esb, "ChangeFileParameters");
			this.fileConfigs = fileConfigs;
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
			return -2276295713134351271L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "File Connector";
		}

		@Override
		public void validate() throws ValidationException
		{
			try
			{
				if (fileConfigs.length > 1)
				{
					for (int i = 1; i < fileConfigs.length; i++)
					{
						// Validate that the other file configs cannot have the same input directory and filename filter
						for (int j = 0; j < i; j++)
						{
							if (fileConfigs[i].getInputDirectory().equalsIgnoreCase(fileConfigs[j].getInputDirectory())
									&& fileConfigs[i].getFilenameFilter().equals(fileConfigs[j].getFilenameFilter()))
							{
								throw new ValidationException("Cannot have the same filename filter for file records in the same input directory.");
							}
						}

						// Directory Creation
						File dir = null;

						// Checks if the directory exists, if not, then create it
						if (fileConfigs[i].getInputDirectory() != null)
						{
							dir = new File(fileConfigs[i].getInputDirectory());
							if (dir != null && !dir.exists())
							{
								if (!dir.mkdirs())
								{
									throw new ValidationException("Cannot create input directory (%s) for %s", fileConfigs[i].getInputDirectory(), fileConfigs[i].getFileType().toString());
								}
							}
						}

						// Check if the output directory exists, if not, then create it
						if (fileConfigs[i].getOutputDirectory() != null || fileConfigs[i].getOutputDirectory().trim().length() == 0)
						{
							dir = new File(fileConfigs[i].getOutputDirectory());
							if (dir != null && !dir.exists())
							{
								if (!dir.mkdirs())
								{
									throw new ValidationException("Cannot create output directory (%s) for %s", fileConfigs[i].getOutputDirectory(), fileConfigs[i].getFileType().toString());
								}
							}
						}

						// Validate the wildcard or regex
						try
						{
							Pattern.compile(convertWildcardToRegex(fileConfigs[i].getFilenameFilter()));
						}
						catch (Exception exc)
						{
							throw new ValidationException("Invalid expression. Use Wildcards or Reqular Expressions");
						}

					}
				}
			}
			catch (Exception e)
			{
				throw new ValidationException("Error validating File Records: %s", e.getMessage());
			}
		}

		@Override
		public boolean save(IDatabaseConnection database, ICtrlConnector control)
		{
			try
			{
				// Check if the table exists
				if (!database.tableExists(ConfigRecord.class))
				{
					database.createTable(ConfigRecord.class);
				}

				// Delete all records from the table
				database.delete(ConfigRecord.class, "");
			}
			catch (SQLException exc)
			{
				return false;
			}

			// Iterate through the configs and insert them into the database
			for (ConfigRecord record : fileConfigs)
			{
				try
				{
					database.upsert(record);
				}
				catch (SQLException e)
				{
					return false;
				}
			}

			return super.save(database, control);
		}

		@Override
		public boolean load(IDatabaseConnection databaseConnection)
		{

			try
			{
				// First check if the table exists
				if (databaseConnection.tableExists(ConfigRecord.class))
				{

					// Get all the records from the database
					List<ConfigRecord> records = databaseConnection.selectList(ConfigRecord.class, "");
					fileConfigs = new ConfigRecord[records.size()];
					for (int i = 0; i < fileConfigs.length; i++)
					{
						fileConfigs[i] = records.get(i);

						// Create input directory if it doesn't exist
						File dir = new File(fileConfigs[i].getInputDirectory());
						if (dir != null && !dir.exists())
						{
							dir.mkdirs();
						}

						// Create output directory if it doesn't exist
						dir = new File(fileConfigs[i].getOutputDirectory());
						if (dir != null && !dir.exists())
						{
							dir.mkdirs();
						}
					}

				}
			}
			catch (SQLException e)
			{
			}

			return super.load(databaseConnection);
		}

		@Override
		public String toString()
		{
			return "FileConfig";
		}

	};

	private FileConnectorConfiguration config = new FileConnectorConfiguration();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IFile Connector Implementation
	//
	// /////////////////////////////////

	// Checks if the input directory and filetype have a server role assigned to it.
	@Override
	public boolean hasIncumbent(String inputDirectory, FileType fileType)
	{
		// Iterate through the file records
		for (ConfigRecord record : config.fileConfigs)
		{
			if (record == null)
				continue;

			// Check if the input directory and file types equal
			if (record.getInputDirectory().equalsIgnoreCase(inputDirectory) && record.getFileType() == fileType)
			{
				return record.getServerRole() != null && (record.getServerRole().equals(HostInfo.getName()) || record.getServerRole().equals("localhost"));
			}
		}
		return true;
	}

	// Sets the server role for the file config and re-initiates the file watchers
	@Override
	public void setIncumbent(String inputDirectory, FileType fileType, String serverRole)
	{
		// Iterate through the file records
		for (ConfigRecord record : config.fileConfigs)
		{
			if (record == null)
				continue;

			// Check if the input directory and file types equal
			if (record.getInputDirectory().equalsIgnoreCase(inputDirectory) && record.getFileType() == fileType)
			{
				// Set the server role and persist to the database
				logger.trace("Setting Incumbent of {} file type to {}", fileType.toString(), HostInfo.getName());
				record.setServerRole(serverRole);
				try (IDatabaseConnection connection = database.getConnection(null))
				{
					config.save(connection, control);
				}
				catch (Exception ex)
				{
					logger.error(ex.getMessage(), ex);
				}

				// Re-initiate the file watchers
				try
				{
					initiateFileWatchers();
				}
				catch (Exception ex)
				{
					logger.error(ex.getMessage(), ex);
				}

				return;
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// File Watcher Handler Mediator Implementation
	//
	// /////////////////////////////////

	// Responsible for distributing the file to the other nodes
	@Override
	public void distribute(File file, String copyCommand)
	{
		int count = 0;
		try (IDatabaseConnection connection = database.getConnection(null))
		{
			// Check the File Records table exists
			if (!connection.tableExists(FileRecord.class))
			{
				connection.createTable(FileRecord.class);
			}

			// Get the file record from the database
			FileRecord fileRecord = connection.select(FileRecord.class, "where filename = %s", file.getName());
			if (fileRecord == null)
			{
				// If it does not exist, register it in the database
				registerFile(file, false);
				if (control != null)
				{
					// Distribute the file to the other nodes
					logger.trace("Distributing {} to other servers.", file.getName().substring(file.getName().indexOf('/') + 1));
					boolean distributed = control.distributeFile(file.getParent(), file.getName());
					while (!distributed)
					{
						// If it failed to distribute the file, try again
						distributed = control.distributeFile(file.getParent(), file.getName());
						Thread.sleep(5000);

						// Check if it has tried 3 or more times
						if (++count >= 3)
						{
							logger.trace("Failed to get response that the file has been distributed. Assuming other HostProcesses are down.");
							break;
						}
					}

					// If successfully distributed, then update the record in the database
					if (distributed)
					{
						fileRecord = connection.select(FileRecord.class, "where filename = %s", file.getName());
						fileRecord.distributed = true;
						connection.update(fileRecord);
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Error distributing file to other nodes. {}", e.getMessage());
		}
	}

	// Get the last record for a file that has not been completed processing
	@Override
	public long lastRecord(File file)
	{
		try (IDatabaseConnection connection = database.getConnection(null))
		{
			// Select the record from the database
			ProgressRecord record = connection.select(ProgressRecord.class, "where filename = %s", file.getName());
			return (record == null) ? 0 : record.recordNo;
		}
		catch (Exception e)
		{
			logger.error("Error retrieving last record of {}. {}", file.getName(), e.getMessage());
			return 0;
		}
	}

	// Checks the database if the file is completed
	@Override
	public boolean isCompleted(File file)
	{
		try (IDatabaseConnection connection = database.getConnection(null))
		{
			// Query the database for the file record
			FileRecord record = connection.select(FileRecord.class, "where filename = %s", file.getName());
			if (record != null)
			{
				return record.completed;
			}
		}
		catch (Exception e)
		{
			logger.error("Error checking is {} is completed. {}", file.getName(), e.getMessage());
		}
		return false;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// File Process Handler Implementation
	//
	// /////////////////////////////////

	// Handles what happens to the file
	public class FileProcessHandler implements IFileProcessorHandler
	{

		private String outputDir;
		private FailSafeProcessThread processingThread;
		private FileStat stats;

		// Initialise
		public FileProcessHandler()
		{
			processingThread = new FailSafeProcessThread();
			stats = new FileStat();

			// Add the fail safe thread in the shutdown thread
			Runtime.getRuntime().addShutdownHook(processingThread);
		}

		// Gets executed once a record is parsed into a DTO
		@Override
		public void dispatchDTO(FileDTO dto)
		{
			// Increase success statistics
			stats.success();

			// Dispatch the DTO through the ESB
			int count = esb.dispatch(dto, null);

			// Remove the process thread from the shutdown thread
			Runtime.getRuntime().removeShutdownHook(processingThread);

			// Initialise and set the current DTO to the fail dafe thread
			processingThread = new FailSafeProcessThread();
			processingThread.setCurrentDTO(dto);
			Runtime.getRuntime().addShutdownHook(processingThread);

			// Check if something caught the DTO from the ESB
			if (count > 0)
			{
				registerDTO(dto);
				stats.caught();
			}
		}

		// Executed once the file has been completed processing
		@Override
		public void completedDTOs(File file)
		{
			// Report the statistics for metrics
			fileInfoMetric.report(esb, stats.successfulRecords(), stats.failedRecords(), stats.caughtRecords());

			// Reset the statistics
			stats.reset();

			logger.info("Finished Processing {}.", file.getName().substring(file.getName().indexOf('/') + 1));

			if (outputDir != null && outputDir.trim().length() > 0)
			{
				// Check if the output directory exists
				File dir = new File(outputDir);
				if (!dir.exists())
					dir.mkdirs();

				// Move the file to the output directory
				if (!file.renameTo(new File(outputDir, file.getName())))
				{
					// Free memory for Windows
					System.gc();

					try
					{
						// Move the file if the first move failed
						Files.move(file.toPath(), new File(outputDir, file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
					}
					catch (IOException e)
					{
						logger.error("Failed to move {} to {}.", file.getName(), outputDir);
					}
				}
			}

			// Register the file in the database that it has been completed
			registerFile(file, true);

			// Remove the fail safe thread from the shutdown thread
			Runtime.getRuntime().removeShutdownHook(processingThread);

			// Notify the other nodes to move the file
			if (control != null)
			{
				control.notifyFileProcessed(file, outputDir);
			}
		}

		@Override
		public void dispatchError(Exception exc)
		{
			logger.error("Error when dispatching the file: {}", exc.getLocalizedMessage());
			stats.fail();
		}

		@Override
		public void malformedRecord(FileDTO dto, Exception exc)
		{
			logger.warn("Error while processing file: {}: {}", dto.filename, exc.getLocalizedMessage());
			stats.fail();
		}

		@Override
		public void processingFailed(FileDTO dto)
		{
			registerDTO(dto);
		}

		public void setOutputDir(String value)
		{
			outputDir = (String) value;
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// File Process Object Implementation
	//
	// /////////////////////////////////

	// Used for metrics
	class FileStat
	{
		private long records = 0;
		private long successfulRecords = 0;
		private long caughtRecords = 0;
		private long failedRecords = 0;

		public void reset()
		{
			records = 0;
			successfulRecords = 0;
			failedRecords = 0;
			caughtRecords = 0;
		}

		public void success()
		{
			records++;
			successfulRecords++;
		}

		public void fail()
		{
			records++;
			failedRecords++;
		}

		public void caught()
		{
			caughtRecords++;
		}

		public long records()
		{
			return records;
		}

		public long successfulRecords()
		{
			return successfulRecords;
		}

		public long failedRecords()
		{
			return failedRecords;
		}

		public long caughtRecords()
		{
			return caughtRecords;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	// Gets a hash map with the directory being the key and an array list of config records that listen in that directory
	private HashMap<String, ArrayList<ConfigRecord>> getUniqueDirRecords()
	{
		// Initialise the hash map
		HashMap<String, ArrayList<ConfigRecord>> paths = new HashMap<String, ArrayList<ConfigRecord>>();

		// Iterate through the config records
		for (ConfigRecord conf : config.fileConfigs)
		{
			// Ensure the record is not null
			if (conf == null)
				continue;

			// Check if the input directory already exists as key
			if (paths.containsKey(conf.getInputDirectory()))
			{
				// Add the config record to the array list
				paths.get(conf.getInputDirectory()).add(conf);
				continue;
			}

			// Else create the array list for the directory
			paths.put(conf.getInputDirectory(), new ArrayList<ConfigRecord>());

			// Add the config record to the array list
			paths.get(conf.getInputDirectory()).add(conf);
		}

		// Return populated map
		return paths;
	}

	// Stores the last DTO it was busy with just in case something goes wrong
	private class FailSafeProcessThread extends Thread
	{

		private FileDTO currentDTO;

		public void setCurrentDTO(FileDTO currentDTO)
		{
			this.currentDTO = currentDTO;
		}

		@Override
		public void run()
		{
			registerDTO(currentDTO);
		}
	}

	// Gets the number of unique directories from the config records
	private int uniqueDirectories()
	{
		int unique = 0;
		ArrayList<String> dirs = new ArrayList<String>();

		// Iterate through the config records
		for (ConfigRecord record : config.fileConfigs)
		{
			// Check the record directory doesn't already exist and that the server machine is the incumbent machine for processing this file
			if (record.getInputDirectory() == null || dirs.contains(record.getInputDirectory()) || //
					record.getServerRole() == null || (!record.getServerRole().equalsIgnoreCase("localhost") && !record.getServerRole().equalsIgnoreCase(HostInfo.getName())))
			{
				continue;
			}

			// Increment the counter
			unique++;
			dirs.add(record.getInputDirectory());
		}
		return unique;
	}

	// Converts Wildcard to Regex
	private String convertWildcardToRegex(String wildcard)
	{
		StringBuffer s = new StringBuffer(wildcard.length());
		for (int i = 0, is = wildcard.length(); i < is; i++)
		{
			char c = wildcard.charAt(i);
			switch (c)
			{
				case '*':
					if ((i > 0) && (wildcard.charAt(i - 1) != '.' && wildcard.charAt(i - 1) != '['))
						s.append(".*");
					else
						// s.append("[a-zA-z0-9_]*");
						s.append(".*");
					break;
				case '?':
					if ((i > 0) && wildcard.charAt(i - 1) != '[')
						s.append(".");
					break;
				case '(':
				case ')':
				case '[':
				case ']':
				case '$':
				case '^':
				case '.':
				case '{':
				case '}':
				case '|':
				case '\\':
					if (i > 0 && wildcard.charAt(i - 1) == '\\')
						break;
					s.append("\\");
					s.append(c);
					break;
				default:
					s.append(c);
					break;
			}
		}

		return s.toString();
	}
}
