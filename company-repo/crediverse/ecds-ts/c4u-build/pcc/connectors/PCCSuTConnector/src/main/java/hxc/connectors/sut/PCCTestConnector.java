package hxc.connectors.sut;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.Channels;
import hxc.connectors.IConnection;
import hxc.connectors.IConnector;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.snmp.IAlarm;
import hxc.connectors.sut.database.LifeCycleRecord;
import hxc.connectors.sut.database.SmsqQueueRecord;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.services.airsim.protocol.ICdr;
import hxc.services.airsim.protocol.IFilter;
import hxc.services.airsim.protocol.ILifecycle;
import hxc.services.airsim.protocol.ISmsHistory;
import hxc.services.airsim.protocol.ISystemUnderTest;
import hxc.services.airsim.protocol.ITemporalTrigger;
import hxc.services.airsim.protocol.IUssdResponse;
import hxc.services.notification.INotifications;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.protocol.hux.HandleUSSDRequest;
import hxc.utils.protocol.hux.HandleUSSDRequestMembers;
import hxc.utils.protocol.hux.HandleUSSDResponse;
import hxc.utils.protocol.hux.HandleUSSDResponseMembers.Actions;
import hxc.utils.xmlrpc.XmlRpcClient;
import hxc.utils.xmlrpc.XmlRpcConnection;

public class PCCTestConnector implements IConnector, ISystemUnderTest
{
	final static Logger logger = LoggerFactory.getLogger(PCCTestConnector.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private IDatabase database;
	private File pcc_config;
	private File site_config;
	private int ussdTransactionID = 1000;
	private int ussdSessionID = 2000;
	private String ussdServiceCode;
	private static Pattern ussdPattern = Pattern.compile("^\\*(\\d+)(\\*\\d+)*\\#$");
	private String[] lastUssdLines = new String[0];
	private Pattern cdrFilePattern = Pattern.compile("pcc-\\d\\d-\\S+.csv$", Pattern.CASE_INSENSITIVE);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

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

		setup();

		return true;
	}

	@Override
	public void stop()
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
		this.config = (PCCSuTConfiguration) config;
		stop();
		try
		{
			Thread.sleep(500);
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
		return true;
	}

	@Override
	public IMetric[] getMetrics()
	{
		return null;
	}

	@Override
	public IConnection getConnection(String optionalConnectionString) throws IOException
	{
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////
	@Perms(perms = { @Perm(name = "ViewPCCSuTParameters", description = "View PCC Test Connector Parameters", category = "PCC Test Connector", supplier = true),
			@Perm(name = "ChangePCCSuTParameters", implies = "ViewPCCSuTParameters", description = "Change PCC Test Connector Parameters", category = "PCC Test Connector", supplier = true) })
	public class PCCSuTConfiguration extends ConfigurationBase
	{

		private static final long serialVersionUID = -1255988940741403107L;

		//private String huxUrl = "http://localhost:14000/RPC2";
		private String huxUrl = "http://localhost:14400/ecds/ussd";
		private String smsqDatabaseConnection = "jdbc:mysql://localhost/dbhxc?user=root&password=ussdgw";
		private int smsHistorySize = 2;
		private String pccConfigLocation = "/opt/pcc/conf/pcc-config.xml";
		private String siteConfigLocation = "/opt/pcc/conf/site-config.xml";
		private String restoreScript = "/opt/pcc/bin/pcc-reload.sh";
		private String cdrLocation = "/opt/pcc/cdr";
		private String cdrLayout = "<timestamp>,<transaction id>,<channel>,<shortcode>,<request string>,<customer care id>,"
				+ "<initiator>,<gift subscription>,<msisdna>,<msisdnb>,<subscription activation date>,<subscription expiry date>,"
				+ "<last renewal Date>,<transaction type>,<subscription tags before>,<subscription tag requested>,<period purchased>,"
				+ "<pending cancellation>,<autorenewal>,<charge>,<service class MSISDNA>,<service class MSISDNB>,<PCC result code>,<last return code external>";
		private int cdrHistorySize = 4;
		private String lifecycleDatabaseConnection = "jdbc:mysql://localhost/dbpcc?user=root&password=ussdgw";
		private String defaultRestartScript = "sh /opt/pcc/bin/pccctl.sh start";
		private int connectTimeout_ms;
		private int readTimeout_ms;

		public void setHuxUrl(String huxUrl)
		{
			check(esb, "ChangePCCSuTParameters");
			this.huxUrl = huxUrl;
		}

		public String getHuxUrl()
		{
			check(esb, "ViewPCCSuTParameters");
			return huxUrl;
		}

		public void setSmsqDatabaseConnection(String smsqDatabaseConnection)
		{
			check(esb, "ChangePCCSuTParameters");
			this.smsqDatabaseConnection = smsqDatabaseConnection;
		}

		public String getSmsqDatabaseConnection()
		{
			check(esb, "ViewPCCSuTParameters");
			return smsqDatabaseConnection;
		}

		public void setSmsHistorySize(int smsHistorySize)
		{
			check(esb, "ChangePCCSuTParameters");
			this.smsHistorySize = smsHistorySize;
		}

		public int getSmsHistorySize()
		{
			check(esb, "ViewPCCSuTParameters");
			return smsHistorySize;
		}

		public void setPccConfigLocation(String pccConfigLocation)
		{
			check(esb, "ChangePCCSuTParameters");
			this.pccConfigLocation = pccConfigLocation;
		}

		public String getPccConfigLocation()
		{
			check(esb, "ViewPCCSuTParameters");
			return pccConfigLocation;
		}

		public void setSiteConfigLocation(String siteConfigLocation)
		{
			check(esb, "ChangePCCSuTParameters");
			this.siteConfigLocation = siteConfigLocation;
		}

		public String getSiteConfigLocation()
		{
			check(esb, "ViewPCCSuTParameters");
			return siteConfigLocation;
		}

		public void setRestoreScript(String restoreScript)
		{
			check(esb, "ChangePCCSuTParameters");
			this.restoreScript = restoreScript;
		}

		public String getRestoreScript()
		{
			check(esb, "ViewPCCSuTParameters");
			return restoreScript;
		}

		public void setCdrLocationx(String cdrLocation)
		{
			check(esb, "ChangePCCSuTParameters");
			this.cdrLocation = cdrLocation;
		}

		public String getCdrLocationx()
		{
			check(esb, "ViewPCCSuTParameters");
			return cdrLocation;
		}

		public void setCdrLayout(String cdrLayout)
		{
			check(esb, "ChangePCCSuTParameters");
			this.cdrLayout = cdrLayout;
		}

		public String getCdrLayout()
		{
			check(esb, "ViewPCCSuTParameters");
			return cdrLayout;
		}

		public void setCdrHistorySize(int cdrHistorySize)
		{
			check(esb, "ChangePCCSuTParameters");
			this.cdrHistorySize = cdrHistorySize;
		}

		public int getCdrHistorySize()
		{
			check(esb, "ViewPCCSuTParameters");
			return cdrHistorySize;
		}

		public void setLifecycleDatabaseConnection(String lifecycleDatabaseConnection)
		{
			check(esb, "ChangePCCSuTParameters");
			this.lifecycleDatabaseConnection = lifecycleDatabaseConnection;
		}

		public String getLifecycleDatabaseConnection()
		{
			check(esb, "ViewPCCSuTParameters");
			return lifecycleDatabaseConnection;
		}

		public void setDefaultRestartScript(String defaultRestartScript)
		{
			check(esb, "ChangePCCSuTParameters");
			this.defaultRestartScript = defaultRestartScript;
		}

		public String getDefaultRestartScript()
		{
			check(esb, "ViewPCCSuTParameters");
			return defaultRestartScript;
		}

		public int getConnectTimeout_ms()
		{
			check(esb, "ViewPCCSuTParameters");
			return connectTimeout_ms;
		}

		public void setConnectTimeout_ms(int connectTimeout_ms)
		{
			check(esb, "ChangePCCSuTParameters");
			this.connectTimeout_ms = connectTimeout_ms;
		}

		public int getReadTimeout_ms()
		{
			check(esb, "ViewPCCSuTParameters");
			return readTimeout_ms;
		}

		public void setReadTimeout_ms(int readTimeout_ms)
		{
			check(esb, "ChangePCCSuTParameters");
			this.readTimeout_ms = readTimeout_ms;
		}

		@Override
		public String getPath(String languageCode)
		{
			return "Testing";
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
			return "PCC SuT Connector";
		}

		@Override
		public void validate() throws ValidationException
		{
		}
	}

	private PCCSuTConfiguration config = new PCCSuTConfiguration();
	
	class CdrHistoryContainer
	{
		private int historySize = 4;
		private LinkedList<String> list = new LinkedList<String>();
		
		CdrHistoryContainer(int historySize)
		{
			if (historySize > 0)
				this.historySize = historySize;
		}
		
		public void add(String s)
		{
			if (list.size() < historySize)
				list.add(s);
		}
		
		public String[] getHistory()
		{
			return list.toArray(new String[0]);
		}
		
		public boolean isHistoryPopulated()
		{
			return historySize == list.size();
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ISystemUnderTest Implementation
	//
	// /////////////////////////////////

	@Override
	public void setup()
	{
		logger.info("Setting up PCC SuT");

		// Check the pcc_config.xml file exists
		pcc_config = new File(config.pccConfigLocation);
		pcc_config = pcc_config.exists() ? pcc_config : null;
		if (pcc_config != null)
		{
			try
			{
				// Copy the pcc_config into a tmp directory
				File dest = new File("/tmp/pcc_config.xml.tmp");
				try {
					Files.copy(pcc_config.toPath(), dest.toPath());
				} catch (FileAlreadyExistsException ignored) {}
				
				if (dest.exists())
					pcc_config = dest;
			}
			catch (IOException e)
			{
				pcc_config = null;
				logger.error(e.getMessage(), e);
			}
		}

		// Check the site_config.xml file exists
		site_config = new File(config.siteConfigLocation);
		site_config = site_config.exists() ? site_config : null;
		if (site_config != null)
		{
			try
			{
				// Copy the site config to a tmp directory
				File dest = new File("/tmp/site_config.xml.tmp");
				try {
					Files.copy(site_config.toPath(), dest.toPath());
				} catch (FileAlreadyExistsException ignored) {}
				
				if (dest.exists())
					site_config = dest;
			}
			catch (IOException e)
			{
				site_config = null;
				logger.error(e.getMessage(), e);
			}
		}		
	}//setup()

	@Override
	public ISmsHistory[] getSmsHistory()
	{
		if (database == null)
			return new ISmsHistory[0];

		// Get the SMS history
		ArrayList<ISmsHistory> history = new ArrayList<>();

		// Connect to the database
		try (IDatabaseConnection connection = database.getConnection(config.smsqDatabaseConnection))
		{

			// Query the list of records
			List<SmsqQueueRecord> smsq_queue = connection.selectList(SmsqQueueRecord.class, " order by insertion_date limit %s", config.smsHistorySize);

			// Iterate through them and add it to the history
			for (SmsqQueueRecord queue : smsq_queue)
			{
				final SmsqQueueRecord q = queue;
				history.add(new ISmsHistory()
				{

					@Override
					public String getToMSISDN()
					{
						return q.destination_msisdn;
					}

					@Override
					public Date getTime()
					{
						return q.insertion_date;
					}

					@Override
					public String getText()
					{
						return q.message;
					}

					@Override
					public String getFromMSISDN()
					{
						return q.source_msisdn;
					}
				});
			}
			logger.info("Successfully retrieved sms history.");

		}
		catch (Exception e)
		{
			logger.error("Failed to get sms history: {}", e.getMessage());
		}

		return history.toArray(new ISmsHistory[0]);
	}

	@Override
	public void clearSmsHistory()
	{
		if (database == null)
			return;

		// Clear the database table
		try (IDatabaseConnection connection = database.getConnection(config.smsqDatabaseConnection))
		{

			connection.delete(SmsqQueueRecord.class, "");
			logger.info("Cleared sms history.");
		}
		catch (Exception e)
		{
			logger.error("Failed to clear sms history: {}", e.getMessage());
		}

	}

	@Override
	public boolean restoreBackup(String backupFilename)
	{
		// Check the config files are referenced
		if ((pcc_config == null || site_config == null) && (backupFilename == null || backupFilename.length() == 0))
			return false;

		// If the backup filename is specified, then use that file for pcc_config.xml
		if (backupFilename != null && backupFilename.length() > 0)
			pcc_config = new File(backupFilename);

		// If it does not exist then return
		if (!pcc_config.exists())
		{
			logger.error("{} does not exist. Ensure setup is called or provide filename.", pcc_config.getName());
			return false;
		}

		try
		{
			// Copy the pcc_config.xml file to the config location
			File dest = new File(config.pccConfigLocation);
			Files.copy(pcc_config.toPath(), dest.toPath());
		}
		catch (IOException exc)
		{
			logger.error("Could not copy pcc config: {}", exc.getMessage());
		}

		try
		{
			// Copy the site_config.xml file to the config location
			File dest = new File(config.siteConfigLocation);
			Files.copy(site_config.toPath(), dest.toPath());
		}
		catch (IOException exc)
		{
			logger.error("Could not copy site config: {}", exc.getMessage());
		}

		try
		{
			// Execute the restore script
			Process proc = Runtime.getRuntime().exec(String.format("sh %s -c %s %s", config.restoreScript, config.pccConfigLocation, config.siteConfigLocation));
			int exitValue = proc.waitFor();
			if (exitValue == 0)
				return true;
		}
		catch (IOException | InterruptedException e)
		{
			logger.error("Process could not execute successfully: {}", e.getMessage());
		}

		return false;
	}

	@Override
	public void injectMOSms(String from, String to, String text)
	{
		// Create SMS record
		SmsqQueueRecord sms = new SmsqQueueRecord();
		sms.destination_msisdn = to;
		sms.insertion_date = new Date();
		sms.message = text;
		sms.source_msisdn = from;

		// Insert the record into the database
		try (IDatabaseConnection connection = database.getConnection(config.smsqDatabaseConnection))
		{
			connection.insert(sms);
		}
		catch (Exception e)
		{
			logger.error("Failed to inject Sms: {}", e.getMessage());
		}
	}

	@Override
	public IUssdResponse injectMOUssd(String from, String text, String imsi)
	{
		boolean isLast = true;
		String response = "Bad USSD String";

		// Construct a HandleUSSDRequest
		HandleUSSDRequest ussdRequest = new HandleUSSDRequest();
		ussdRequest.members = new HandleUSSDRequestMembers();
		ussdRequest.members.MSISDN = from;
		ussdRequest.members.TransactionId = Integer.toString(ussdTransactionID++);
		ussdRequest.members.TransactionTime = new Date();
		ussdRequest.members.IMSI = imsi;

		// Parse the USSD String
		Matcher match = ussdPattern.matcher(text);

		if (match.matches())
		{
			ussdRequest.members.USSDServiceCode = ussdServiceCode = match.group(1);
			ussdRequest.members.USSDRequestString = text.substring(ussdRequest.members.USSDServiceCode.length() + 1);
			ussdRequest.members.response = false;
			ussdRequest.members.SessionId = ++ussdSessionID;
		}
		else
		{

			ussdRequest.members.USSDServiceCode = ussdServiceCode;
			ussdRequest.members.USSDRequestString = text;
			ussdRequest.members.response = true;
			ussdRequest.members.SessionId = ussdSessionID;
		}

		// Create an XmlRpcClient
		XmlRpcClient client = new XmlRpcClient(config.huxUrl, config.connectTimeout_ms, config.readTimeout_ms);

		// Send Request
		try (XmlRpcConnection connection = client.getConnection())
		{
			HandleUSSDResponse ussdResponse = connection.call(ussdRequest, HandleUSSDResponse.class);
			isLast = ussdResponse.members.action == Actions.end;
			response = ussdResponse.members.USSDResponseString;
			lastUssdLines = response.split("[\\r\\n]+");
		}
		catch (Exception e)
		{
			response = "Exception: " + e.getMessage();
			isLast = true;
		}

		// Return Result
		final String finalResponse = response;
		final boolean finalIsLast = isLast;
		return new IUssdResponse()
		{
			@Override
			public String getText()
			{
				return finalResponse;
			}

			@Override
			public boolean isLast()
			{
				return finalIsLast;
			}

		};
	}

	@Override
	public String getUssdMenuLine(int lineNumber)
	{
		if (lastUssdLines == null || lineNumber < 1 || lineNumber > lastUssdLines.length)
			return null;
		return lastUssdLines[lineNumber - 1];
	}

	@Override
	public ICdr getLastCdr()
	{
		ICdr[] history = getCdrHistory();
		if (history.length != 0)
			return history[0];
		
		return null;
	}

	@Override
	public ICdr[] getCdr(IFilter[] filters)
	{
		ICdr[] history = getCdrHistory();

		ArrayList<ICdr> cdrs = new ArrayList<>();

		// Iterate through the buffer
		cdrFor: for (ICdr cdr : history)
		{

			boolean match = true;

			// Iterate through the filters
			for (IFilter filter : filters)
			{
				for (String additionalInformation : cdr.getAdditionalInformation())
				{
					if (additionalInformation.toLowerCase().startsWith(filter.getField().toLowerCase()))
					{
						switch (filter.getComparator())
						{
							case CONTAINS:
								match = match ? additionalInformation.contains(filter.getValue().toString()) : false;
								break;

							case EQUALTO:
								match = match ? additionalInformation.substring(additionalInformation.indexOf(':') + 1).trim().equalsIgnoreCase(filter.getValue().toString()) : false;
								break;

							case GREATER_THAN:
								try
								{
									double num = Double.parseDouble(additionalInformation.substring(additionalInformation.indexOf(':') + 1).trim());
									match = match ? Double.parseDouble(filter.getValue()) < num : false;
								}
								catch (Exception exc)
								{
									match = false;
								}
								break;

							case LESS_THAN:
								try
								{
									double num = Double.parseDouble(additionalInformation.substring(additionalInformation.indexOf(':') + 1).trim());
									match = match ? Double.parseDouble(filter.getValue()) > num : false;
								}
								catch (Exception exc)
								{
									match = false;
								}
								break;

							default:
								break;
						}

						if (!match)
							continue cdrFor;
					}
				}
			}

			if (match)
			{
				cdrs.add(cdr);
			}

		}//cdrFor:

		return cdrs.toArray(new ICdr[0]);
	}

	@Override
	public ICdr[] getCdrHistory()
	{	
		CdrHistoryContainer cdrContainer = new CdrHistoryContainer(config.cdrHistorySize);
		
		ArrayList<File> pccCdrFiles = new ArrayList<File>();
		try
		{
			Process lsProcess = Runtime.getRuntime().exec("ls -1t " + config.cdrLocation);
			lsProcess.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(lsProcess.getInputStream()));
			
			String fileName = new String();
			while((fileName = reader.readLine()) != null)
			{
				if (cdrFilePattern.matcher(fileName).matches())
					pccCdrFiles.add(new File(config.cdrLocation + "/" + fileName));
			}
		}
		catch (Exception e)
		{
			logger.error("Failed get last modified files", e);
		}
		
		// read lines from the back and add the to the CDR container
		try
		{
			for (File file : pccCdrFiles)
			{
				RandomAccessFile rf = new RandomAccessFile(file, "r");
				final long len = rf.length();
				String line = new String();
				for(long i = len - 1; i >= 0; --i)
				{
					rf.seek(i);
					final char ch = (char) rf.read();
					if (ch != '\n' || i == 0)
						line = ch + line;
					else if (!line.isEmpty())
					{
						cdrContainer.add(line);
						line = new String();
					}
					
					if (cdrContainer.isHistoryPopulated())
						break;
				}
				
				if (cdrContainer.isHistoryPopulated())
					break;
				
				if (!line.isEmpty())
					cdrContainer.add(line);

				rf.close();
			}
		}
		catch(Exception e)
		{
			logger.error("Failed to get CDR history", e);
		}
		
		// create a list of ICdr from the CDR file lines
		String[] historyStr = cdrContainer.getHistory();
		ICdr history[] = new ICdr[historyStr.length];
		for (int i = 0; i < historyStr.length; ++i)
		{
			history[i] = createCdr(historyStr[i]);
		}
		
		return history;
	}

	@Override
	public void clearCdrHistory()
	{
	}

	@Override
	public void tearDown()
	{
		// Remove reference of the pcc_config and site_config
		logger.info("Tearing down PCC SuT.");
		pcc_config = null;
		site_config = null;
	}

	@Override
	public ILifecycle getLifecycle(String msisdn, String serviceID, String variantID)
	{
		// Get information from the database
		try (IDatabaseConnection connection = database.getConnection(config.lifecycleDatabaseConnection))
		{
			final LifeCycleRecord record = connection.select(LifeCycleRecord.class, " where msisdn = %s AND tag = %s", msisdn, String.format("%s-%s", serviceID, variantID));

			if (record != null)
			{
				return new ILifecycle()
				{

					@Override
					public boolean isBeingProcessed()
					{
						return false;
					}

					@Override
					public boolean isCancelled()
					{
						return false;
					}

					@Override
					public String getVariantID()
					{
						return record.tag.substring(record.tag.indexOf('-') + 1);
					}

					@Override
					public String getState()
					{
						return record.status;
					}

					@Override
					public String getServiceID()
					{
						return record.tag.substring(0, record.tag.indexOf('-'));
					}

					@Override
					public String getMsisdn()
					{
						return record.msisdn;
					}

					@Override
					public String getMode()
					{
						return null;
					}

					@Override
					public Date getDateTime4()
					{
						return record.time_last_renew;
					}

					@Override
					public Date getDateTime3()
					{
						return record.time_removal;
					}

					@Override
					public Date getDateTime2()
					{
						return record.time_subscribe;
					}

					@Override
					public Date getDateTime1()
					{
						return record.time_created;
					}

					@Override
					public Date getDateTime0()
					{
						return record.time_expiry;
					}

					@Override
					public String[] getAdditionalInformation()
					{
						return new String[] { record.user_data };
					}

				};
			}
		}
		catch (Exception e)
		{
			logger.error("Getting lifecycle record failed: {}", e.getMessage());
		}

		return null;
	}

	@Override
	public ILifecycle[] getLifecycles(String msisdn)
	{
		// Get more than one record from the database
		try (IDatabaseConnection connection = database.getConnection(config.lifecycleDatabaseConnection))
		{
			List<LifeCycleRecord> records = connection.selectList(LifeCycleRecord.class, " where msisdn = %s", msisdn);

			if (records != null && records.size() > 0)
			{
				List<ILifecycle> lifecycles = new ArrayList<>();

				for (final LifeCycleRecord record : records)
				{
					lifecycles.add(new ILifecycle()
					{

						@Override
						public boolean isBeingProcessed()
						{
							return false;
						}

						@Override
						public boolean isCancelled()
						{
							return false;
						}

						@Override
						public String getVariantID()
						{
							return record.tag.substring(record.tag.indexOf('-') + 1);
						}

						@Override
						public String getState()
						{
							return record.status;
						}

						@Override
						public String getServiceID()
						{
							return record.tag.substring(0, record.tag.indexOf('-'));
						}

						@Override
						public String getMsisdn()
						{
							return record.msisdn;
						}

						@Override
						public String getMode()
						{
							return null;
						}

						@Override
						public Date getDateTime4()
						{
							return record.time_last_renew;
						}

						@Override
						public Date getDateTime3()
						{
							return record.time_removal;
						}

						@Override
						public Date getDateTime2()
						{
							return record.time_subscribe;
						}

						@Override
						public Date getDateTime1()
						{
							return record.time_created;
						}

						@Override
						public Date getDateTime0()
						{
							return record.time_expiry;
						}

						@Override
						public String[] getAdditionalInformation()
						{
							return new String[] { record.user_data };
						}
					});
				}

				return lifecycles.toArray(new ILifecycle[0]);
			}
		}
		catch (Exception e)
		{
			logger.error("Getting lifecycle records failed: {}", e.getMessage());
		}

		return null;
	}

	@Override
	public boolean updateLifecycle(ILifecycle lifecycle)
	{
		// Connect to the database
		try (IDatabaseConnection connection = database.getConnection(config.lifecycleDatabaseConnection))
		{
			// Get the lifecycle record
			LifeCycleRecord record = null;

			if (lifecycle.getAdditionalInformation() != null && lifecycle.getAdditionalInformation().length > 0)
			{
				record = connection.select(LifeCycleRecord.class, " where msisdn = %s AND tag = %s AND instance = %s", lifecycle.getMsisdn(),
						String.format("%s-%s", lifecycle.getServiceID(), lifecycle.getVariantID()), lifecycle.getAdditionalInformation()[1]);
			}
			else
			{
				record = connection.select(LifeCycleRecord.class, " where msisdn = %s AND tag = %s", lifecycle.getMsisdn(), String.format("%s-%s", lifecycle.getServiceID(), lifecycle.getVariantID()));
			}

			if (record == null)
				return false;

			// Update the fields
			record.status = lifecycle.getState() != null ? lifecycle.getState() : record.status;
			record.time_expiry = lifecycle.getDateTime0() != null ? lifecycle.getDateTime0() : record.time_expiry;
			record.next_attempt_renewal = lifecycle.getDateTime1() != null ? lifecycle.getDateTime1() : record.next_attempt_renewal;
			record.time_subscribe = lifecycle.getDateTime2() != null ? lifecycle.getDateTime2() : record.time_subscribe;
			record.time_removal = lifecycle.getDateTime3() != null ? lifecycle.getDateTime3() : record.time_removal;
			record.time_last_renew = lifecycle.getDateTime4() != null ? lifecycle.getDateTime4() : record.time_last_renew;
			record.user_data = (lifecycle.getAdditionalInformation() != null && lifecycle.getAdditionalInformation().length > 0) ? lifecycle.getAdditionalInformation()[0] : record.user_data;
			record.instance = (lifecycle.getAdditionalInformation() != null && lifecycle.getAdditionalInformation().length > 1) ? lifecycle.getAdditionalInformation()[1] : record.instance;

			return connection.update(record) > 0;
		}
		catch (Exception e)
		{
			logger.error("Update lifecycle record failed: {}", e.getMessage());
		}

		return false;
	}

	@Override
	public boolean deleteLifecycle(String msisdn, String serviceID, String variantID)
	{
		try (IDatabaseConnection connection = database.getConnection(config.lifecycleDatabaseConnection))
		{
			return connection.delete(LifeCycleRecord.class, " where msisdn = %s AND tag = %s", msisdn, String.format("%s-%s", serviceID, variantID));
		}
		catch (Exception e)
		{
			logger.error("Delete lifecycle record failed: {}", e.getMessage());
		}

		return false;
	}

	@Override
	public boolean deleteLifecycles(String msisdn)
	{
		try (IDatabaseConnection connection = database.getConnection(config.lifecycleDatabaseConnection))
		{
			return connection.delete(LifeCycleRecord.class, " where msisdn = %s", msisdn);
		}
		catch (Exception e)
		{
			logger.error("Delete lifecycle records failed: {}", e.getMessage());
		}

		return false;
	}

	@Override
	public boolean adjustLifecycle(String msisdn, String serviceID, String variantID, Boolean isBeingProcessed, Date timeStamp)
	{
		return false;
	}

	@Override
	public boolean hasMemberLifecycle(String msisdn, String serviceID, String variantID, String memberMsisdn)
	{
		logger.debug("Not implemented.");
		return false;
	}

	@Override
	public String[] getMembersLifecycle(String msisdn, String serviceID, String variantID)
	{
		logger.debug("Not implemented.");
		return null;
	}

	@Override
	public boolean addMemberLifecycle(String msisdn, String serviceID, String variantID, String memberMsisdn)
	{
		logger.debug("Not implemented.");
		return false;
	}

	@Override
	public boolean deleteMemberLifecycle(String msisdn, String serviceID, String variantID, String memberMsisdn)
	{
		logger.debug("Not implemented.");
		return false;
	}

	@Override
	public IAlarm getLastAlarm()
	{
		logger.debug("Not implemented.");
		return null;
	}

	@Override
	public IAlarm[] getAlarmHistory()
	{
		logger.debug("Not implemented.");
		return null;
	}

	@Override
	public boolean restart(String optionalCommand)
	{
		try
		{
			Process process = null;
			if (optionalCommand == null)
				process = Runtime.getRuntime().exec(config.defaultRestartScript);
			else
				process = Runtime.getRuntime().exec(optionalCommand);

			int exit = process.waitFor();

			if (exit == 0)
				return true;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
		}
		return false;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Temporal Triggers
	//
	// /////////////////////////////////
	@Override
	public ITemporalTrigger[] getTemporalTriggers(String serviceID, String variantID, String msisdnA, String msisdnB)
	{
		logger.debug("Not implemented.");
		return null;
	}

	@Override
	public boolean updateTemporalTrigger(ITemporalTrigger temporalTrigger)
	{
		logger.debug("Not implemented.");
		return false;
	}

	@Override
	public boolean deleteTemporalTrigger(ITemporalTrigger temporalTrigger)
	{
		logger.debug("Not implemented.");
		return false;
	}
	
	@Override
	public String nonQuery(String command)
	{
		logger.debug("Not implemented.");
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	private ICdr createCdr(String line)
	{

		// Reads the CDR line and splits it into an array
		final String fields[] = line.split(",");
		if (fields == null || fields.length <= 1)
			return null;

		//return new ICdr()
		ICdr cdr = new ICdr()
		{
			@Override
			public boolean isRolledBack()
			{
				return false;
			}

			@Override
			public boolean isFollowUp()
			{
				return false;
			}

			@Override
			public String getVariantID()
			{
				return null;
			}

			@Override
			public String getTransactionID()
			{
				return field(fields, "transaction id", false);
			}

			@Override
			public Date getStartTime()
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss'.'SSS");
				try
				{
					String dateStamp = field(fields, "timestamp", false);
					dateStamp = dateStamp.substring(0, dateStamp.length() - 3);
					Date date = sdf.parse(dateStamp);
					return date;
				}
				catch (ParseException e)
				{
					return null;
				}
			}

			@Override
			public String getServiceID()
			{
				return null;
			}

			@Override
			public ReturnCodes getReturnCode()
			{
				return null;
			}

			@Override
			public RequestModes getRequestMode()
			{
				return null;
			}

			@Override
			public String getProcessID()
			{
				return null;
			}

			@Override
			public int getLastExternalResultCode()
			{
				try
				{
					return Integer.parseInt(field(fields, "last return code external", false));
				}
				catch (Exception exc)
				{
					return -1;
				}
			}

			@Override
			public String getLastActionID()
			{
				return null;
			}

			@Override
			public String getInboundTransactionID()
			{
				return null;
			}

			@Override
			public String getInboundSessionID()
			{
				return null;
			}

			@Override
			public String getHostName()
			{
				return null;
			}

			@Override
			public int getChargeLevied()
			{
				try
				{
					return Integer.parseInt(field(fields, "charge", false));
				}
				catch (Exception exc)
				{
					return -1;
				}
			}

			@Override
			public Channels getChannel()
			{
				return null;
			}

			@Override
			public String getCallerID()
			{
				return null;
			}

			@Override
			public String getB_MSISDN()
			{
				return field(fields, "msisdnb", false);
			}

			@Override
			public String[] getAdditionalInformation()
			{
				return leftOvers(fields, true, "timestamp", "transaction id", "msisdna", "msisdnb", "charge", "last return code external");
			}

			@Override
			public String getA_MSISDN()
			{
				return field(fields, "msisdna", false);
			}

			@Override
			public String getParam1()
			{
				return null;
			}

			@Override
			public String getParam2()
			{
				return null;
			}
		};
		
		return cdr;
	}

	// Gets the value from an array of strings according to the field name
	private String field(String fields[], String nameOfField, boolean verbose)
	{
		if (fields == null)
			return verbose ? "" : null;

		String cdr[] = config.cdrLayout.replaceAll("<|>", "").split(",");

		int index = -1;
		for (index = 0; index < cdr.length + 1; index++)
		{
			if (index >= cdr.length || index >= fields.length)
				return verbose ? "" : null;

			if (cdr[index] != null && cdr[index].toLowerCase().contains(nameOfField.toLowerCase()))
			{
				break;
			}
		}

		return verbose ? nameOfField + ": " + fields[index] : fields[index];
	}

	// Gets the left overs and excludes the exceptions
	private String[] leftOvers(String fields[], boolean verbose, String... exceptions)
	{
		if (fields == null)
			return verbose ? new String[] { "" } : null;

		String cdr[] = config.cdrLayout.replaceAll("<|>", "").split(",");

		if (exceptions != null && exceptions.length > 0)
		{
			for (int i = 0; i < cdr.length; i++)
			{
				for (int j = 0; j < exceptions.length; j++)
				{
					if (cdr[i] != null && cdr[i].toLowerCase().contains(exceptions[j].toLowerCase()))
					{
						cdr[i] = "";
					}
				}
			}

			ArrayList<String> leftOvers = new ArrayList<>();
			for (int i = 0; i < cdr.length; i++)
			{
				if (cdr[i] != null && cdr[i].length() > 0 && fields.length > i)
					leftOvers.add(verbose ? cdr[i] + ": " + fields[i] : fields[i]);
			}
			return leftOvers.toArray(new String[0]);
		}
		return verbose ? new String[] { "" } : null;
	}
}
