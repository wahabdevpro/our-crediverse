package hxc.connectors.database.mysql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.Config;
import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnector;
import hxc.connectors.ctrl.ICtrlConnector;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.database.IDatabaseInfoConfig;
import hxc.connectors.snmp.ISnmpConnector;
import hxc.connectors.snmp.IncidentSeverity;
import hxc.connectors.snmp.IndicationState;
import hxc.servicebus.HostInfo;
import hxc.servicebus.IPlugin;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.INotifications;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.security.SupplierOnly;
import hxc.utils.calendar.DateTime;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.thread.FileSearchThread;

public class MySqlConnector implements IConnector, IDatabase, IDatabaseInfoConfig
{
	final static Logger logger = LoggerFactory.getLogger(MySqlConnector.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private String connectionString = null;
	private static Boolean databaseCreated = false;
	private int backupCounter;

	private static Map<String, String> forceDatabase; // used for unit tests
	private static final String mySqlDriver = "com.mysql.jdbc.Driver";
	private static final String mariaDBDriver = "org.mariadb.jdbc.Driver";

	// /////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////
	@Perms(perms = { @Perm(name = "ViewMySqlParameters", description = "View MySql Parameters", category = "MySql", supplier = true),
			@Perm(name = "ChangeMySqlParameters", implies = "ViewMySqlParameters", description = "Change MySql Parameters", category = "MySql", supplier = true) })
	public class MySqlConfiguration extends ConfigurationBase
	{
		private String driver = mySqlDriver;
		private String server = "localhost";
		private String databaseName = "hxc";
		private String user = "root";
		private String password = "ussdgw";
		private String backupDirectory = "/var/opt/cs/c4u/backup/database";
		private String backupNameFormat = "%1$s%2$s.bak";
		private int connectionPoolSize = 20;
		private String fitnessCheckCommand = "";
		private boolean skipArchival;
		private Integer archiveAfterDays;
		private Integer deleteAfterDays;
		private String additionalParameters = "--skip-lock-tables --single-transaction";
		private boolean usePipelineAuth = true;

		private static final String configFileName = "./MySqlConfig.xml";

		@Config(description = "Skip \"Archival\"", comment = "If this is set then files will not be compressed (e.g. zipped) and will stay uncompressed until removed.")
		public boolean getSkipArchival()
		{
			check(esb, "ViewMySqlParameters");
			return this.skipArchival;
		}

		public void setSkipArchival( boolean skipArchival )
		{
			check(esb, "ChangeMySqlParameters");
			this.skipArchival = skipArchival;
		}

		public String getAdditionalParameters()
		{
			check(esb, "ViewMySqlParameters");
			return this.additionalParameters;
		}

		public void setAdditionalParameters( String additionalParameters )
		{
			check(esb, "ChangeMySqlParameters");
			this.additionalParameters = additionalParameters;
		}


		@Config(description = "Archive After Days Old", comment = "Only specify if default value from Archive Connector is to be overwritten for Database.")
		public String getArchiveAfterDays()
		{
			check(esb, "ViewMySqlParameters");
			if ( this.archiveAfterDays == null ) return null;
			else return String.valueOf(this.archiveAfterDays);
		}

		public void setArchiveAfterDays( String archiveAfterDaysString ) throws ValidationException
		{
			check(esb, "ChangeMySqlParameters");
			try
			{
				if ( archiveAfterDaysString.isEmpty() ) this.archiveAfterDays = null;
				else this.archiveAfterDays = Integer.valueOf(archiveAfterDaysString);
			}
			catch( NumberFormatException exception )
			{
				throw ValidationException.createFieldValidationException("archiveAfterDays", String.format("Must be decimal number or blank: %s", archiveAfterDays), exception);
			}
		}

		@Config(description = "Delete After Days Old", comment = "Only specify if default value from Archive Connector is to be overwritten for Database.")
		public String getDeleteAfterDays()
		{
			check(esb, "ViewMySqlParameters");
			if ( this.deleteAfterDays == null ) return "";
			else return String.valueOf( this.deleteAfterDays );
		}

		public void setDeleteAfterDays( String deleteAfterDaysString ) throws ValidationException
		{
			check(esb, "ChangeMySqlParameters");
			try
			{
				if ( deleteAfterDaysString.isEmpty() ) this.deleteAfterDays = null;
				else this.deleteAfterDays = Integer.valueOf(deleteAfterDaysString);
			}
			catch( NumberFormatException exception )
			{
				throw ValidationException.createFieldValidationException("deleteAfterDays", String.format("Must be decimal number or blank: %s", deleteAfterDays), exception);
			}
		}

		@Config(description = "Database Driver")
		public String getDriver()
		{
			check(esb, "ViewMySqlParameters");
			return driver;
		}

		public void setDriver(String driver)
		{
			check(esb, "ChangeMySqlParameters");
			this.driver = driver;
		}

		@Config(description = "Database Server Machine")
		public String getServer()
		{
			check(esb, "ViewMySqlParameters");
			return server;
		}

		public void setServer(String server)
		{
			check(esb, "ChangeMySqlParameters");
			this.server = server;
		}

		@SupplierOnly
		@Config(description = "Database Catalog Name")
		public String getDatabaseName()
		{
			check(esb, "ViewMySqlParameters");
			return databaseName;
		}

		@SupplierOnly
		public void setDatabaseName(String databaseName)
		{
			check(esb, "ChangeMySqlParameters");
			this.databaseName = databaseName;
		}

		@SupplierOnly
		@Config(description = "Database User")
		public String getUser()
		{
			check(esb, "ViewMySqlParameters");
			return user;
		}

		@SupplierOnly
		public void setUser(String user)
		{
			check(esb, "ChangeMySqlParameters");
			this.user = user;
		}

		@SupplierOnly
		@Config(description = "Database Password", hidden = true)
		public String getPassword()
		{
			check(esb, "ViewMySqlParameters");
			return password;
		}

		@SupplierOnly
		public void setPassword(String password)
		{
			check(esb, "ChangeMySqlParameters");
			this.password = password;
		}

		@SupplierOnly
		public int getConnectionPoolSize()
		{
			check(esb, "ViewMySqlParameters");
			return connectionPoolSize;
		}

		@SupplierOnly
		public void setConnectionPoolSize(int connectionPoolSize)
		{
			check(esb, "ChangeMySqlParameters");
			this.connectionPoolSize = connectionPoolSize;
		}

		public String getBackupDirectory()
		{
			check(esb, "ViewMySqlParameters");
			return backupDirectory;
		}

		public void setBackupDirectory(String backupDirectory)
		{
			check(esb, "ChangeMySqlParameters");
			this.backupDirectory = backupDirectory;
		}

		@SupplierOnly
		public String getBackupNameFormat()
		{
			check(esb, "ViewMySqlParameters");
			return backupNameFormat;
		}

		@SupplierOnly
		public void setBackupNameFormat(String backupNameFormat) throws ValidationException
		{
			check(esb, "ChangeMySqlParameters");

			ValidationException.validateFormat(backupNameFormat);
			this.backupNameFormat = backupNameFormat;
		}

		@SupplierOnly
		public String getFitnessCheckCommand()
		{
			check(esb, "ChangeMySqlParameters");
			return fitnessCheckCommand;
		}

		@SupplierOnly
		public void setFitnessCheckCommand(String fitnessCheckCommand)
		{
			check(esb, "ChangeMySqlParameters");
			this.fitnessCheckCommand = fitnessCheckCommand;
		}

		@SupplierOnly
		public boolean getUsePipelineAuth()
		{
			check(esb, "ChangeMySqlParameters");
			return usePipelineAuth;
		}

		@SupplierOnly
		public void setUsePipelineAuth(boolean usePipelineAuth)
		{
			check(esb, "ChangeMySqlParameters");
			this.usePipelineAuth = usePipelineAuth;
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
			return -2414094378982580282L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "MySQL Database";
		}

		@Override
		public void validate() throws ValidationException
		{
			Connection connection;
			try
			{
				String connectionString = getLocalConnectionString();
				int connectionStringHash = connectionString.hashCode();
				connection = DriverManager.getConnection(connectionString);
				MySqlConnection result = new MySqlConnection(MySqlConnector.this, connection, connectionStringHash);
				result.close();
			}
			catch (SQLException | IOException e)
			{
				throw new ValidationException("Cannot Connect to the Database", e);
			}

		}

		@Override
		public boolean save(IDatabaseConnection database, ICtrlConnector control)
		{
			logger.trace("save: {}", getSerialVersionUID());
			Properties properties = new Properties();
			properties.setProperty("driver", driver);
			properties.setProperty("server", server);
			properties.setProperty("databaseName", databaseName);
			properties.setProperty("user", user);
			properties.setProperty("password", toBase64(password));
			properties.setProperty("poolsize", Integer.toString(connectionPoolSize));
			properties.setProperty("backupDirectory", backupDirectory);
			properties.setProperty("backupNameFormat", backupNameFormat);
			properties.setProperty("fitnessCheckCommand", fitnessCheckCommand);
			properties.setProperty("skipArchival", Boolean.toString(skipArchival));
			properties.setProperty("usePipelineAuth", Boolean.toString(usePipelineAuth));
			if ( archiveAfterDays != null ) properties.setProperty("archiveAfterDays", Integer.toString(archiveAfterDays));
			if ( deleteAfterDays != null ) properties.setProperty("deleteAfterDays", Integer.toString(deleteAfterDays));
			properties.setProperty("additionalParameters", additionalParameters);
			if (MySqlConnector.forceDatabase != null && MySqlConnector.forceDatabase.containsKey("hxc"))
				databaseName = MySqlConnector.forceDatabase.get("hxc");
			try (OutputStream os = new FileOutputStream(configFileName))
			{
				properties.storeToXML(os, "MySQL Connection Parameters");
			}
			catch (IOException e)
			{
				logger.error(e.getMessage(), e);
				return false;
			}
			//Not calling super.save(database, control) because config is not saved to database;
			return true;
		}

		@Override
		public boolean load(IDatabaseConnection databaseConnection)
		{
			logger.trace("load: {}", getSerialVersionUID());
			//Not calling super.load(databaseConnection) because load expects a valid databaseConnection - which this connector provides;		
			File configFile = new File(configFileName);
			if (!configFile.exists())
				return false;

			try (InputStream is = new FileInputStream(configFileName))
			{
				Properties properties = new Properties();
				properties.loadFromXML(is);
				driver = properties.getProperty("driver", driver);
				server = properties.getProperty("server", server);
				databaseName = properties.getProperty("databaseName", databaseName);
				user = properties.getProperty("user", user);
				password = fromBase64(properties.getProperty("password", toBase64(password)));
				connectionPoolSize = Integer.parseInt(properties.getProperty("poolsize", Integer.toString(connectionPoolSize)));
				backupDirectory = properties.getProperty("backupDirectory", backupDirectory);
				backupNameFormat = properties.getProperty("backupNameFormat", backupNameFormat);
				fitnessCheckCommand = properties.getProperty("fitnessCheckCommand", fitnessCheckCommand);
				skipArchival = Boolean.valueOf(properties.getProperty("skipArchival", Boolean.toString(skipArchival)));
				String archiveAfterDaysString = properties.getProperty("archiveAfterDays", ( archiveAfterDays != null ? Integer.toString(archiveAfterDays) : null ));
				archiveAfterDays = ( archiveAfterDaysString != null ? Integer.parseInt(archiveAfterDaysString) : null );
				String deleteAfterDaysString = properties.getProperty("deleteAfterDays", ( deleteAfterDays != null ? Integer.toString(deleteAfterDays) : null ));
				deleteAfterDays = ( deleteAfterDaysString != null ? Integer.parseInt(deleteAfterDaysString) : null );
				additionalParameters = properties.getProperty("additionalParameters", additionalParameters);
				usePipelineAuth = Boolean.valueOf(properties.getProperty("usePipelineAuth", Boolean.toString(usePipelineAuth)));
				if (MySqlConnector.forceDatabase != null && MySqlConnector.forceDatabase.containsKey("hxc"))
					databaseName = MySqlConnector.forceDatabase.get("hxc");
			}
			catch (IOException e)
			{
				logger.error("", e);
				return false;
			}

			return true;
		}

	};

	MySqlConfiguration config = new MySqlConfiguration();

	public MySqlConnector()
	{
		try
		{
			//Class.forName("com.mysql.jdbc.Driver");
			Class.forName(config.getDriver());
		}
		catch (ClassNotFoundException e)
		{

		}
		if (forceDatabase != null && forceDatabase.containsKey("hxc"))
			config.setDatabaseName(forceDatabase.get("hxc"));
	}

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
		try
		{
			//Class.forName("com.mysql.jdbc.Driver");
			Class.forName(config.getDriver());
		}
		catch (ClassNotFoundException e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}

		// Log
		logger.info("MySQL Connector Started");

		return true;
	}

	@Override
	public void stop()
	{
		closePooledConnections();
		TableInfo.clear();
		databaseCreated = false;
		// Log
		logger.info("MySQL Connector Stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		logger.error(String.format("%s(%s).setConfiguration: config = %s", this.getClass().getName(), Integer.toHexString(this.hashCode()), config));
		this.config = (MySqlConfiguration) config;
		connectionString = null;
	}

	@Override
	public MySqlConnection getConnection(String optionalConnectionString) throws IOException
	{
		Connection connection = null;
		boolean hasDatabase = false;
		MySqlConnection result = null;
		try
		{
			String connectionString = optionalConnectionString == null || optionalConnectionString.length() == 0 //
					? getLocalConnectionString() : optionalConnectionString;
			int connectionStringHash = connectionString.hashCode();
			result = getPooledConnection();
			if (result == null || result.getConnection().isClosed() || result.getConnectionHash() != connectionStringHash)
			{
				if (result != null && !result.getConnection().isClosed())
					result.getConnection().close();

				logger.info("MySqlConnection::getConnection: Connection String {}", connectionString);
				connection = DriverManager.getConnection(connectionString);
				result = new MySqlConnection(this, connection, connectionStringHash);
				hasDatabase = connection.getCatalog() != null && connection.getCatalog().length() > 0;

				if (!hasDatabase)
				{
					synchronized (databaseCreated)
					{
						if (!databaseCreated)
						{
							if (!result.databaseExists(config.databaseName))
								result.createDatabase(config.databaseName);
						}

						databaseCreated = true;
					}
				}

			}

			connection = result.getConnection();
			if (!hasDatabase && (connection.getCatalog() == null || connection.getCatalog().length() == 0))
				connection.setCatalog(config.databaseName);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			connection.setAutoCommit(true);
			return result;
		}
		catch (SQLException e)
		{
			if (connection != null)
			{
				try
				{
					connection.close();
				}
				catch (Exception ex)
				{
				}
			}

			String message = e.getMessage();
			if (e.getCause() != null)
			{
				message = message.replaceAll("\n", "\t");
				message += String.format(" Database connection failure cause: %s", e.getCause().getMessage());
			}
			logger.error(message, e);
			throw new IOException(message, e);
		}
	}

	@Override
	public boolean isAvailable(String server)
	{
		int portNumber = 3306;
		if (server != null)
		{
			int index = server.indexOf(':');
			if (index > 1)
			{
				String portString = server.substring(index + 1);
				server = server.substring(0, index);
				try
				{
					portNumber = Integer.parseInt(portString);
				}
				catch (Exception ex)
				{
					return false;
				}
			}
		}

		// Attempt to open a TcpConnection
		try (Socket socket = new Socket())
		{
			socket.connect(new InetSocketAddress(server, portNumber), 5000);
		}
		catch (IOException e)
		{
			logger.debug("Could not Open Connection to Database - {}", e);
			return false;
		}

		// Open connection and select a result
		String connectionString = String.format("jdbc:mysql://%s:%s?user=%s&password=%s&usePipelineAuth=%b&logger=com.mysql.jdbc.log.Slf4JLogger", server, portNumber, config.user, config.password, config.usePipelineAuth);
		if(mariaDBDriver.compareTo(config.getDriver()) == 0)
		{
			connectionString = String.format("jdbc:mariadb://%s:%s?user=%s&password=%s&usePipelineAuth=%b", server, portNumber, config.user, config.password, config.usePipelineAuth);
		}

		try (Connection connection = DriverManager.getConnection(connectionString))
		{
			String sql = String.format("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '%s';", config.databaseName);
			try (PreparedStatement statement = connection.prepareStatement(sql))
			{
				try (ResultSet resultSet = statement.executeQuery())
				{
					if (resultSet.next())
					{
						String result = resultSet.getString(1);
						return config.databaseName.equalsIgnoreCase(result);
					}
				}
			}
		}
		catch (SQLException e)
		{
			logger.debug("Database is not Available - {}", e);
			return false;
		}

		return false;
	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return isAvailable("localhost");
	}

	@Override
	public boolean isFit()
	{
		String command = config.getFitnessCheckCommand();
		if (command != null && !command.isEmpty())
		{
			try
			{
				Process process = Runtime.getRuntime().exec(command);
				int exit = process.waitFor();

				if (exit != 0)
				{
					logger.error("MySQL Connector fitness command {} returned {}", command, exit);
					return false;
				}
			}
			catch (Throwable tr)
			{
				logger.error(tr.getMessage(), tr);
				return false;
			}
		}

		return isAvailable(config.server);
	}

	@Override
	public IMetric[] getMetrics()
	{
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IDatabase Implementation
	//
	// /////////////////////////////////

	private String cachedMySqlDumpLocation = "mysqldump";
	private FileSearchThread fileSearchThread = new FileSearchThread("mysqldump")
	{
		@Override
		public void foundFile(String filename)
		{
			cachedMySqlDumpLocation = filename;
		}
	};

	private final String backupCommand = "%s --host=%s -u %s -p%s %s --databases %s -r %s";

	@Override
	public String backup() throws Exception
	{
		ISnmpConnector snmpConnector = esb.getFirstConnector(ISnmpConnector.class);
		if (cachedMySqlDumpLocation == null)
		{
			logger.error("Cannot find \"mysqldump\" file to make backup of database.");
			if ( snmpConnector != null ) snmpConnector.elementServiceStatus("MySql Backups", IndicationState.OUT_OF_SERVICE, IncidentSeverity.MAJOR, //
						"Cannot find \"mysqldump\" file to make backup of database.");
			return null;
		}

		File backupLocation = new File(config.backupDirectory);
		if (!backupLocation.exists())
		{
			backupLocation.mkdirs();
		}

		backupCounter++;

		String backup = config.backupDirectory + "/" + String.format(config.backupNameFormat, HostInfo.getNameOrElseHxC(), new DateTime().toString("yyyyMMdd'T'HHmmss.SSS"), backupCounter);
		String command = String.format(backupCommand, cachedMySqlDumpLocation, config.server, config.user, config.password, config.additionalParameters, config.databaseName, backup);
		logger.info("Making backup of {} database: {}", config.databaseName, command);

		try
		{
			boolean existsInPath = Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
			        .map(Paths::get)
			        .anyMatch(path -> Files.exists(path.resolve(cachedMySqlDumpLocation)));
			if(existsInPath)
			{
				Process process = Runtime.getRuntime().exec(command);
				int exit = process.waitFor();
				if (exit == 0)
				{
					logger.info("Successfully made {} backup of {} database.", backup, config.databaseName);
	
					return backup;
				}
				else
				{
					byte error[] = new byte[process.getErrorStream().available()];
					process.getErrorStream().read(error);
					logger.error("Backup was unsuccessful. Error: {}", new String(error));
					if ( snmpConnector != null )  snmpConnector.jobFailed("MySql Backups", IncidentSeverity.CRITICAL, "Failed to execute mysqldump command.");
				}
			} else {
				logger.warn("Could not make backup of database. Command {} does not exist.", command);
			}
		}
		catch (InterruptedException e)
		{
			logger.error("Backup could not be accomplished. Error", e);
			if ( snmpConnector != null ) snmpConnector.jobFailed("MySql Backups", IncidentSeverity.CRITICAL, "Failed to dump database (Interrupted).");
		}
		catch (Exception e)
		{
			logger.warn("Could not make backup of database. Attempting to locate \"mysqldump\" command and try again.", e);

			if (!fileSearchThread.isSearching())
				fileSearchThread.next();
			if ( snmpConnector != null ) snmpConnector.jobFailed("MySql Backups", IncidentSeverity.CRITICAL, String.format("Failed to dump database (Other): %s", e));
		}

		return null;
	}

	@Override
	public boolean restoreFromBackup(String backup)
	{

		if (backup == null)
			return false;

		try
		{
			Process process = Runtime.getRuntime()
					.exec(new String[] {
							cachedMySqlDumpLocation.substring(0, cachedMySqlDumpLocation.lastIndexOf("dump"))
									+ ((cachedMySqlDumpLocation.lastIndexOf('.') > cachedMySqlDumpLocation.lastIndexOf("dump"))
											? cachedMySqlDumpLocation.substring(cachedMySqlDumpLocation.lastIndexOf('.')) : ""),
							"-u" + config.user, "-p" + config.password, "-e", "source " + backup });

			try
			{
				int exit = process.waitFor();

				if (exit == 0)
				{
					logger.info("Successfully restored {} database from {}.", config.databaseName, backup);
				}
				else
				{
					byte error[] = new byte[process.getErrorStream().available()];
					process.getErrorStream().read(error);
					logger.info("Restore may have been unsuccessful. Message: {}", new String(error));
				}
			}
			catch (InterruptedException e)
			{
				logger.error("Restoration could not be accomplished. Error.", e);
				return false;
			}
		}
		catch (Exception e)
		{
			logger.error("Could not execute command", e);
			return false;
		}

		// Checking restore was successful
		List<IPlugin> plugins = esb.getRegisteredPlugins();
		for (IPlugin plugin : plugins)
		{

			try (IDatabaseConnection con = getConnection(null))
			{
				plugin.getConfiguration().load(con);
			}
			catch (Exception e)
			{
			}

		}

		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IDatabaseConfig Implementation
	//
	// /////////////////////////////////

	@Override
	public String getDirectory()
	{
		return config.backupDirectory;
	}

	@Override
	public String getTimeFormat()
	{
		return null;
	}

	@Override
	public String getRotatedFilename()
	{
		return config.backupNameFormat;
	}

	@Override
	public String getInterimFilename()
	{
		return config.databaseName;
	}

	@Override
	public boolean getSkipArchival()
	{
		return config.skipArchival;
	}

	@Override
	public Integer getArchiveAfterDays()
	{
		return config.archiveAfterDays;
	}

	@Override
	public Integer getDeleteAfterDays()
	{
		return config.deleteAfterDays;
	}


	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	private String toBase64(String text)
	{
		try
		{
			return DatatypeConverter.printBase64Binary(text.getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			return "";
		}
	}

	private String fromBase64(String text)
	{
		try
		{
			return new String(DatatypeConverter.parseBase64Binary(text), "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			return "";
		}
	}

	private String getLocalConnectionString()
	{
		if (connectionString == null)
		{
			if(mySqlDriver.equals(config.driver))
			{
				connectionString = String.format("jdbc:mysql://%s/%s?user=%s&password=%s&usePipelineAuth=%b&characterEncoding=UTF-8&logger=com.mysql.jdbc.log.Slf4JLogger", config.server, config.databaseName, config.user, config.password, config.usePipelineAuth);
			} else if(mariaDBDriver.equals(config.driver)) {
				connectionString = String.format("jdbc:mariadb://%s/%s?user=%s&password=%s&usePipelineAuth=%b&characterEncoding=UTF-8", config.server, config.databaseName, config.user, config.password, config.usePipelineAuth);
			}
		}
		return connectionString;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Connection Pooling
	//
	// /////////////////////////////////
	private Stack<MySqlConnection> connectionPool = new Stack<MySqlConnection>();
	private static Object poolSync = new Object();

	void returnConnection(MySqlConnection connection) throws SQLException
	{
		synchronized (poolSync)
		{
			if (connectionPool.size() < config.connectionPoolSize)
			{
				connectionPool.push(connection);
				return;
			}
		}
		connection.getConnection().close();
	}

	private MySqlConnection getPooledConnection()
	{
		synchronized (poolSync)
		{
			if (connectionPool.size() == 0)
				return null;
			return connectionPool.pop();
		}
	}

	private void closePooledConnections()
	{
		while (true)
		{
			MySqlConnection connection = getPooledConnection();
			if (connection == null)
				return;
			try
			{
				connection.getConnection().close();
			}
			catch (SQLException e)
			{
			}
		}
	}
	
	public static void overrideDb(Map<String, String> config)
	{
		forceDatabase = config;
	}
}
