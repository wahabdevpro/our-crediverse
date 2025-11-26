package hxc.connectors.sut;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
import hxc.connectors.IInteraction;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.database.IDatabaseInfoConfig;
import hxc.connectors.lifecycle.ISubscription;
import hxc.connectors.lifecycle.Membership;
import hxc.connectors.lifecycle.Subscription;
import hxc.connectors.sms.ISmsConnector;
import hxc.connectors.snmp.IAlarm;
import hxc.connectors.soap.ISubscriber;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.servicebus.Trigger;
import hxc.services.airsim.protocol.ICdr;
import hxc.services.airsim.protocol.IFilter;
import hxc.services.airsim.protocol.ILifecycle;
import hxc.services.airsim.protocol.ISmsHistory;
import hxc.services.airsim.protocol.ISystemUnderTest;
import hxc.services.airsim.protocol.ITemporalTrigger;
import hxc.services.airsim.protocol.IUssdResponse;
import hxc.services.notification.INotificationText;
import hxc.services.notification.INotifications;
import hxc.services.pin.Pin;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.security.SupplierOnly;
import hxc.services.transactions.ITransactionService;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.protocol.hux.HandleUSSDRequest;
import hxc.utils.protocol.hux.HandleUSSDRequestMembers;
import hxc.utils.protocol.hux.HandleUSSDResponse;
import hxc.utils.protocol.hux.HandleUSSDResponseMembers.Actions;
import hxc.utils.xmlrpc.XmlRpcClient;
import hxc.utils.xmlrpc.XmlRpcConnection;

public class C4UTestConnector implements IConnector, ISystemUnderTest
{
	final static Logger logger = LoggerFactory.getLogger(C4UTestConnector.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private hxc.connectors.lifecycle.ILifecycle lifecycleConnector;
	private IDatabase database;
	private ISmsConnector smsConnector;
	private int ussdTransactionID = 1000;
	private int ussdSessionID = 2000;
	private String ussdServiceCode;
	private static Pattern ussdPattern = Pattern.compile("^\\*(\\d+)(\\*\\d+)*\\#$");
	private String[] lastUssdLines = new String[0];
	private LinkedList<IAlarm> alarms;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

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
		lifecycleConnector = esb.getFirstConnector(hxc.connectors.lifecycle.ILifecycle.class);
		if (lifecycleConnector == null)
			return false;

		database = esb.getFirstConnector(IDatabase.class);
		if (database == null)
			return false;

		alarms = new LinkedList<>();
		Trigger<IAlarm> alarmsTrigger = new Trigger<IAlarm>(IAlarm.class)
		{
			@Override
			public boolean testCondition(IAlarm message)
			{
				return true;
			}

			@Override
			public void action(IAlarm message, IConnection connection)
			{
				alarms.addLast(message);
			}
		};
		esb.addTrigger(alarmsTrigger);

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
		this.config = (C4USuTConfiguration) config;
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
	@Perms(perms = { @Perm(name = "ViewC4USutParameters", description = "View Air Simulator Parameters", category = "C4U System Under Test", supplier = true),
			@Perm(name = "ChangeC4USutParameters", implies = "ViewC4USutParameters", description = "Change Air Simulator Parameters", category = "C4U System Under Test", supplier = true) })
	public class C4USuTConfiguration extends ConfigurationBase
	{

		//private String huxUrl = "http://localhost:14000/RPC2";
		private String huxUrl = "http://localhost:14400/ecds/ussd";
		private int alarmHistorySize = 4;
		private String defaultRestartScript = "sh /etc/init.d/supervisor restart";
		private int cdrHistorySize = 20;
		private String setupFile = "/var/opt/cs/c4u/test/setup.sh";

		public void setHuxUrl(String huxUrl)
		{
			check(esb, "ChangeC4USutParameters");
			this.huxUrl = huxUrl;
		}
		
		@SupplierOnly
		public String getHuxUrl()
		{
			check(esb, "ViewC4USutParameters");
			return huxUrl;
		}

		@SupplierOnly
		public int getAlarmHistorySize()
		{
			check(esb, "ViewC4USutParameters");
			return alarmHistorySize;
		}

		public void setAlarmHistorySize(int alarmHistorySize)
		{
			check(esb, "ChangeC4USutParameters");
			this.alarmHistorySize = alarmHistorySize;
		}

		@SupplierOnly
		public String getDefaultRestartScript()
		{
			check(esb, "ViewC4USutParameters");
			return defaultRestartScript;
		}

		public void setDefaultRestartScript(String defaultRestartScript)
		{
			check(esb, "ChangeC4USutParameters");
			this.defaultRestartScript = defaultRestartScript;
		}

		@SupplierOnly
		public int getCdrHistorySize()
		{
			check(esb, "ViewAirSimParameters");
			return cdrHistorySize;
		}

		public void setCdrHistorySize(int cdrHistorySize)
		{
			check(esb, "ChangeAirSimParameters");
			this.cdrHistorySize = cdrHistorySize;
		}
		
		@SupplierOnly
		public String getSetupFile() 
		{
			return setupFile;
		}
		
		public void setSetupFile(String setupFile)
		{
			check(esb, "ChangeAirSimParameters");
			this.setupFile = setupFile;
		}

		@Override
		@SupplierOnly
		public String getPath(String languageCode)
		{
			return "Testing";
		}

		@Override
		@SupplierOnly		
		public INotifications getNotifications()
		{
			return null;
		}

		@Override
		public long getSerialVersionUID()
		{
			return 5251826323893608281L;
		}

		@Override
		@SupplierOnly		
		public String getName(String languageCode)
		{
			return "C4U Sut Connector";
		}

		@Override
		public void validate() throws ValidationException
		{

		}

	}

	private C4USuTConfiguration config = new C4USuTConfiguration();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ISystemUnderTest Implementation
	//
	// /////////////////////////////////

	@Override
	public void setup()
	{
		logger.info("Running C4U SuT setup");
		try 
		{
			File c4u_config = new File(config.setupFile);
			if (c4u_config != null && c4u_config.exists())
			{
				Process proc = Runtime.getRuntime().exec(String.format("bash -c %s", config.setupFile));
				int exitValue = proc.waitFor();
				logger.info("script return code: {}", exitValue);
			}
			else
			{
				logger.error("C4U SuT setup could not find file (Configure in C4U Admin UI): {}", config.setupFile);
			}
		}
		catch(Exception e)
		{
			logger.error("Error thrown when running C4U SuT setup: {}", e.getMessage());
		}
	}

	@Override
	public ISmsHistory[] getSmsHistory()
	{
		ISmsConnector sms = esb.getFirstConnector(ISmsConnector.class);
		if (sms != null)
		{
			final hxc.connectors.sms.ISmsHistory[] smsHistory = sms.getSmsHistory();
			ArrayList<ISmsHistory> history = new ArrayList<>();

			for (final hxc.connectors.sms.ISmsHistory his : smsHistory)
			{
				if (his == null)
					continue;

				final IInteraction request = his.getRequest();
				if (request != null)
				{
					history.add(new ISmsHistory()
					{

						@Override
						public String getToMSISDN()
						{
							return request.getMSISDN();
						}

						@Override
						public Date getTime()
						{
							return his.getDate();
						}

						@Override
						public String getText()
						{
							return request.getMessage();
						}

						@Override
						public String getFromMSISDN()
						{
							return request.getShortCode();
						}

					});
				}

			}

			return history.toArray(new ISmsHistory[history.size()]);
		}

		return null;
	}

	@Override
	public void clearSmsHistory()
	{
		ISmsConnector sms = esb.getFirstConnector(ISmsConnector.class);
		if (sms != null)
		{
			sms.clearHistory();
		}
	}

	@Override
	public boolean restoreBackup(String backupFilename)
	{
		IDatabaseInfoConfig info = esb.getFirstConnector(IDatabaseInfoConfig.class);
		IDatabase database = (IDatabase) info;

		if (backupFilename == null || backupFilename.length() == 0)
		{
			File folder = new File(info.getDirectory());
			File backup = null;

			for (File f : folder.listFiles())
			{

				if (f == null)
					continue;

				if (!f.getName().contains(info.getRotatedFilename().substring(info.getRotatedFilename().lastIndexOf('.'))))
					continue;

				if (backup == null)
					backup = f;

				if (backup.lastModified() < f.lastModified())
					backup = f;
			}

			if (backup == null)
				return false;

			return database.restoreFromBackup(backup.getAbsolutePath());
		}

		File f = new File(info.getDirectory() + "/" + backupFilename);
		if (!f.exists())
			return false;

		return database.restoreFromBackup(backupFilename);
	}

	@Override
	public void injectMOSms(final String from, final String to, final String text)
	{
		if (smsConnector == null)
			smsConnector = esb.getFirstConnector(ISmsConnector.class);
		
		IInteraction sms = new IInteraction()
		{

			@Override
			public String getMSISDN()
			{
				return from;
			}

			@Override
			public String getShortCode()
			{
				return to;
			}

			@Override
			public String getMessage()
			{
				return text == null ? "" : text;
			}

			@Override
			public boolean reply(INotificationText notificationText)
			{
				return smsConnector.send(from, to, notificationText, true);
			}

			@Override
			public Channels getChannel()
			{
				return Channels.SMS;
			}

			@Override
			public String getInboundTransactionID()
			{
				return "1234";
			}

			@Override
			public String getInboundSessionID()
			{
				return "3";
			}

			@Override
			public String getIMSI()
			{
				return null;
			}
			
			@Override
			public void setRequest(boolean request)
			{
				// Not Required
			}

			@Override
			public Date getOriginTimeStamp()
			{
				return new Date();
			}
			public String getOriginInterface()
			{
				return "SMS";
			}

		};

		int count = esb.dispatch(sms, null);
		if (count == 0)
			logger.error("Sms '{}' to {} not processed", sms.getMessage(), sms.getShortCode());
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
		XmlRpcClient client = new XmlRpcClient(config.huxUrl);

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
			logger.error(e.getMessage(), e);
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
		ITransactionService transactionService = esb.getFirstService(ITransactionService.class);
		if (transactionService == null)
			return null;
		final hxc.services.transactions.ICdr cdr = transactionService.getLastCdr();
		if (cdr == null)
			return null;

		return new ICdr()
		{

			@Override
			public boolean isRolledBack()
			{
				return cdr.isRolledBack();
			}

			@Override
			public boolean isFollowUp()
			{
				return cdr.isFollowUp();
			}

			@Override
			public String getVariantID()
			{
				return cdr.getVariantID();
			}

			@Override
			public String getTransactionID()
			{
				return cdr.getTransactionID();
			}

			@Override
			public Date getStartTime()
			{
				return cdr.getStartTime();
			}

			@Override
			public String getServiceID()
			{
				return cdr.getServiceID();
			}

			@Override
			public ReturnCodes getReturnCode()
			{
				return cdr.getReturnCode();
			}

			@Override
			public RequestModes getRequestMode()
			{
				return cdr.getRequestMode();
			}

			@Override
			public String getProcessID()
			{
				return cdr.getProcessID();
			}

			@Override
			public int getLastExternalResultCode()
			{
				return cdr.getLastExternalResultCode();
			}

			@Override
			public String getLastActionID()
			{
				return cdr.getLastActionID();
			}

			@Override
			public String getInboundTransactionID()
			{
				return cdr.getInboundTransactionID();
			}

			@Override
			public String getInboundSessionID()
			{
				return cdr.getInboundSessionID();
			}

			@Override
			public String getHostName()
			{
				return cdr.getHostName();
			}

			@Override
			public int getChargeLevied()
			{
				return cdr.getChargeLevied();
			}

			@Override
			public Channels getChannel()
			{
				return cdr.getChannel();
			}

			@Override
			public String getCallerID()
			{
				return cdr.getCallerID();
			}

			@Override
			public String getB_MSISDN()
			{
				return cdr.getB_MSISDN();
			}

			@Override
			public String[] getAdditionalInformation()
			{
				return new String[] { cdr.getAdditionalInformation() };
			}

			@Override
			public String getA_MSISDN()
			{
				return cdr.getA_MSISDN();
			}

			@Override
			public String getParam1()
			{
				return cdr.getParam1();
			}

			@Override
			public String getParam2()
			{
				return cdr.getParam2();
			}
		};
	}

	@Override
	public ICdr[] getCdr(IFilter[] filters)
	{
		return null;
	}

	@Override
	public ICdr[] getCdrHistory() {
		return new ICdr[0];
	}

	@Override
	public void clearCdrHistory()
	{
	}

	@Override
	public ILifecycle getLifecycle(final String msisdn, final String serviceID, final String variantID)
	{
		try (IDatabaseConnection db = database.getConnection(null))
		{
			ISubscriber subscriber = getSubscriber(msisdn);

			final ISubscription subscription = lifecycleConnector.getSubscription(db, subscriber, serviceID, variantID);
			if (subscription == null)
				return null;

			return new ILifecycle()
			{
				@Override
				public String getMsisdn()
				{
					return msisdn;
				}

				@Override
				public String getServiceID()
				{
					return serviceID;
				}

				@Override
				public String getVariantID()
				{
					return variantID;
				}

				@Override
				public String getState()
				{
					return Integer.toString(subscription.getState());
				}

				@Override
				public Date getDateTime0()
				{
					return subscription.getNextDateTime();
				}

				@Override
				public Date getDateTime1()
				{
					return subscription.getDateTime1();
				}

				@Override
				public Date getDateTime2()
				{
					return subscription.getDateTime2();
				}

				@Override
				public Date getDateTime3()
				{
					return subscription.getDateTime3();
				}

				@Override
				public Date getDateTime4()
				{
					return subscription.getDateTime4();
				}

				@Override
				public String getMode()
				{
					return null;
				}

				@Override
				public boolean isBeingProcessed()
				{
					return subscription.isBeingProcessed();
				}

				@Override
				public boolean isCancelled()
				{
					return false;
				}

				@Override
				public String[] getAdditionalInformation()
				{
					return null;
				}

			};
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return null;
		}

	}

	@Override
	public ILifecycle[] getLifecycles(final String msisdn)
	{
		try (IDatabaseConnection db = database.getConnection(null))
		{
			ISubscriber subscriber = getSubscriber(msisdn);

			ISubscription subscriptions[] = lifecycleConnector.getSubscriptions(db, subscriber);
			if (subscriptions == null || subscriptions.length == 0)
				return null;

			List<ILifecycle> lifecycles = new ArrayList<>();

			for (final ISubscription subscription : subscriptions)
			{
				if (subscription == null)
					continue;

				lifecycles.add(new ILifecycle()
				{
					@Override
					public String getMsisdn()
					{
						return msisdn;
					}

					@Override
					public String getServiceID()
					{
						return subscription.getServiceID();
					}

					@Override
					public String getVariantID()
					{
						return subscription.getVariantID();
					}

					@Override
					public String getState()
					{
						return Integer.toString(subscription.getState());
					}

					@Override
					public Date getDateTime0()
					{
						return subscription.getNextDateTime();
					}

					@Override
					public Date getDateTime1()
					{
						return subscription.getDateTime1();
					}

					@Override
					public Date getDateTime2()
					{
						return subscription.getDateTime2();
					}

					@Override
					public Date getDateTime3()
					{
						return subscription.getDateTime3();
					}

					@Override
					public Date getDateTime4()
					{
						return subscription.getDateTime4();
					}

					@Override
					public String getMode()
					{
						return null;
					}

					@Override
					public boolean isBeingProcessed()
					{
						return subscription.isBeingProcessed();
					}

					@Override
					public boolean isCancelled()
					{
						return false;
					}

					@Override
					public String[] getAdditionalInformation()
					{
						return null;
					}

				});
			}

			return lifecycles.toArray(new ILifecycle[0]);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public boolean updateLifecycle(ILifecycle lifecycle)
	{
		try (IDatabaseConnection db = database.getConnection(null))
		{
			ISubscriber subscriber = getSubscriber(lifecycle.getMsisdn());

			final ISubscription subscription = lifecycleConnector.getSubscription(db, subscriber, lifecycle.getServiceID(), lifecycle.getVariantID());
			if (subscription == null)
				return false;

			if (lifecycle.getState() != null)
				subscription.setState(Integer.parseInt(lifecycle.getState()));

			if (lifecycle.getDateTime0() != null)
				subscription.setNextDateTime(lifecycle.getDateTime0());

			if (lifecycle.getDateTime1() != null)
				subscription.setDateTime1(lifecycle.getDateTime1());

			if (lifecycle.getDateTime2() != null)
				subscription.setDateTime2(lifecycle.getDateTime2());

			if (lifecycle.getDateTime3() != null)
				subscription.setDateTime3(lifecycle.getDateTime3());

			if (lifecycle.getDateTime4() != null)
				subscription.setDateTime4(lifecycle.getDateTime4());

			return lifecycleConnector.updateSubscription(db, subscription);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}

	}

	@Override
	public boolean deleteLifecycle(String msisdn, String serviceID, String variantID)
	{
		try (IDatabaseConnection db = database.getConnection(null))
		{
			ISubscriber subscriber = getSubscriber(msisdn);

			final ISubscription subscription = lifecycleConnector.getSubscription(db, subscriber, serviceID, variantID);
			if (subscription == null)
				return false;

			lifecycleConnector.removeMembers(db, subscriber, serviceID, variantID);
			lifecycleConnector.removeSubscription(db, subscriber, serviceID, variantID);

			return true;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean deleteLifecycles(String msisdn)
	{
		try (IDatabaseConnection db = database.getConnection(null))
		{
			ISubscriber subscriber = getSubscriber(msisdn);

			ISubscription subscriptions[] = lifecycleConnector.getSubscriptions(db, subscriber);
			if (subscriptions == null || subscriptions.length == 0)
				return false;

			for (ISubscription subscription : subscriptions)
			{
				lifecycleConnector.removeMembers(db, subscriber, subscription.getServiceID(), subscription.getVariantID());
				lifecycleConnector.removeSubscription(db, subscriber, subscription.getServiceID(), subscription.getVariantID());
			}

			return true;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean adjustLifecycle(String msisdn, String serviceID, String variantID, Boolean isBeingProcessed, Date timeStamp)
	{

		try (IDatabaseConnection db = database.getConnection(null))
		{
			ISubscriber subscriber = getSubscriber(msisdn);

			return lifecycleConnector.adjustLifecycle(db, subscriber, serviceID, variantID, isBeingProcessed, timeStamp);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}

	}

	private ISubscriber getSubscriber(final String msisdn)
	{
		ISubscriber subscriber = new ISubscriber()
		{

			@Override
			public String getInternationalNumber()
			{
				return msisdn;
			}

			@Override
			public String getNationalNumber()
			{
				return msisdn;
			}

			@Override
			public int getLanguageID()
			{
				return 1;
			}

			@Override
			public boolean isSameNumber(String msisdn)
			{
				return msisdn.equals(getInternationalNumber());
			}

			@Override
			public int getServiceClass()
			{
				return 1000;
			}

		};
		return subscriber;
	}

	@Override
	public boolean hasMemberLifecycle(String msisdn, String serviceID, String variantID, String memberMsisdn)
	{
		try (IDatabaseConnection db = database.getConnection(null))
		{
			ISubscriber owner = getSubscriber(msisdn);
			ISubscriber member = getSubscriber(memberMsisdn);

			return lifecycleConnector.isMember(db, owner, serviceID, member);

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}

	}

	@Override
	public String[] getMembersLifecycle(String msisdn, String serviceID, String variantID)
	{
		try (IDatabaseConnection db = database.getConnection(null))
		{
			ISubscriber owner = getSubscriber(msisdn);

			return lifecycleConnector.getMembers(db, owner, serviceID, variantID);

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public boolean addMemberLifecycle(String msisdn, String serviceID, String variantID, String memberMsisdn)
	{
		try (IDatabaseConnection db = database.getConnection(null))
		{
			ISubscriber owner = getSubscriber(msisdn);
			ISubscriber member = getSubscriber(memberMsisdn);

			return lifecycleConnector.addMember(db, owner, serviceID, variantID, member);
		}

		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean deleteMemberLifecycle(String msisdn, String serviceID, String variantID, String memberMsisdn)
	{
		try (IDatabaseConnection db = database.getConnection(null))
		{
			ISubscriber owner = getSubscriber(msisdn);
			ISubscriber member = getSubscriber(memberMsisdn);

			return lifecycleConnector.removeMember(db, owner, serviceID, variantID, member);
		}

		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public IAlarm getLastAlarm()
	{
		if (alarms.isEmpty())
			return null;

		return alarms.getLast();
	}

	@Override
	public IAlarm[] getAlarmHistory()
	{
		if (alarms.isEmpty())
			return null;

		IAlarm history[] = new IAlarm[config.alarmHistorySize];
		int index = 0;
		Iterator<IAlarm> iterator = alarms.descendingIterator();
		while (iterator.hasNext())
		{
			if (index == config.alarmHistorySize)
				break;

			history[index++] = iterator.next();
		}

		return history;
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
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	@Override
	public void tearDown()
	{
		try (IDatabaseConnection con = database.getConnection(null))
		{
			if (con.tableExists(Pin.class))
				con.delete(Pin.class, "");

			if (con.tableExists(Subscription.class))
				con.delete(Subscription.class, "");

			if (con.tableExists(Membership.class))
				con.delete(Membership.class, "");
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Temporal Triggers
	//
	// /////////////////////////////////

	@Override
	public ITemporalTrigger[] getTemporalTriggers(String serviceID, String variantID, String msisdnA, String msisdnB)
	{

		try (IDatabaseConnection db = database.getConnection(null))
		{
			ISubscriber subscriberA = msisdnA == null ? null : getSubscriber(msisdnA);
			ISubscriber subscriberB = msisdnB == null ? null : getSubscriber(msisdnB);
			hxc.connectors.lifecycle.ITemporalTrigger[] triggers = lifecycleConnector.getTemporalTriggers(db, serviceID, variantID, subscriberA, subscriberB, null);
			ITemporalTrigger[] result = new ITemporalTrigger[triggers.length];
			int index = 0;
			for (hxc.connectors.lifecycle.ITemporalTrigger trigger : triggers)
			{
				result[index++] = getTrigger(trigger.getServiceID(), trigger.getVariantID(), trigger.getMsisdnA(), trigger.getMsisdnB(), trigger.getNextDateTime(), trigger.isBeingProcessed(),
						trigger.getState());
			}

			return result;

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return null;
		}

	}

	@Override
	public boolean updateTemporalTrigger(ITemporalTrigger temporalTrigger)
	{
		try (IDatabaseConnection db = database.getConnection(null))
		{
			String msisdnA = temporalTrigger.getMsisdnA();
			String msisdnB = temporalTrigger.getMsisdnB();
			ISubscriber subscriberA = msisdnA == null ? null : getSubscriber(msisdnA);
			ISubscriber subscriberB = msisdnB == null ? null : getSubscriber(msisdnB);
			hxc.connectors.lifecycle.ITemporalTrigger[] triggers = //
			lifecycleConnector.getTemporalTriggers(db, temporalTrigger.getServiceID(), temporalTrigger.getVariantID(), subscriberA, subscriberB, temporalTrigger.getKeyValue());
			if (triggers.length != 1)
				return false;
			hxc.connectors.lifecycle.ITemporalTrigger trigger = triggers[0];

			trigger.setNextDateTime(temporalTrigger.getNextDateTime());
			trigger.setBeingProcessed(temporalTrigger.isBeingProcessed());

			return lifecycleConnector.updateTemporalTrigger(db, trigger);

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean deleteTemporalTrigger(ITemporalTrigger temporalTrigger)
	{
		try (IDatabaseConnection db = database.getConnection(null))
		{
			String msisdnA = temporalTrigger.getMsisdnA();
			String msisdnB = temporalTrigger.getMsisdnB();
			ISubscriber subscriberA = msisdnA == null ? null : getSubscriber(msisdnA);
			ISubscriber subscriberB = msisdnB == null ? null : getSubscriber(msisdnB);
			hxc.connectors.lifecycle.ITemporalTrigger[] triggers = lifecycleConnector.getTemporalTriggers(db, temporalTrigger.getServiceID(), temporalTrigger.getVariantID(), subscriberA, subscriberB,
					temporalTrigger.getKeyValue());
			if (triggers.length != 1)
				return false;
			hxc.connectors.lifecycle.ITemporalTrigger trigger = triggers[0];

			return lifecycleConnector.removeTemporalTrigger(db, trigger);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	private ITemporalTrigger getTrigger(final String serviceID, final String variantID, final String msisdnA, final String msisdnB, final Date nextDateTime, final boolean beingProcessed, final int state)
	{
		return new ITemporalTrigger()
		{

			@Override
			public String getServiceID()
			{
				return serviceID;
			}

			@Override
			public String getVariantID()
			{
				return variantID;
			}

			@Override
			public String getMsisdnA()
			{
				return msisdnA;
			}

			@Override
			public String getMsisdnB()
			{
				return msisdnB;
			}

			@Override
			public String getKeyValue()
			{
				return null;
			}

			@Override
			public Date getNextDateTime()
			{
				return nextDateTime;
			}

			@Override
			public boolean isBeingProcessed()
			{
				return beingProcessed;
			}

			@Override
			public int getState()
			{
				return state;
			}

		};
	}
	
	@Override
	public String nonQuery(String command)
	{
		IDatabase database = esb.getFirstConnector(IDatabase.class);
		try (IDatabaseConnection connection = database.getConnection(null))
		{
			int result = connection.executeNonQuery(command);
			return Integer.toString(result);
		}
		catch (Throwable tr)
		{
			return tr.getMessage();
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods ?
	//
	// /////////////////////////////////

}
