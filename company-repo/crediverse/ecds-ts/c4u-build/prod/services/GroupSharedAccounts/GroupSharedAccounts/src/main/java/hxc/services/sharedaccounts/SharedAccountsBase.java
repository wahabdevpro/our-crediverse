package hxc.services.sharedaccounts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.IServiceContext;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.RequestHeader;
import com.concurrent.hxc.ServiceContext;
import com.concurrent.hxc.SubscribeRequest;
import com.google.gson.Gson;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.Channels;
import hxc.connectors.IConnection;
import hxc.connectors.IInteraction;
import hxc.connectors.air.IAirConnector;
import hxc.connectors.air.proxy.Subscriber;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.datawarehouse.IHistoryCleanup;
import hxc.connectors.hux.HuxConnection;
import hxc.connectors.hux.HuxProcessState;
import hxc.connectors.lifecycle.ILifecycle;
import hxc.connectors.lifecycle.ISubscription;
import hxc.connectors.sms.ISmsConnector;
import hxc.connectors.vas.VasService;
import hxc.processmodel.IProcess;
import hxc.servicebus.ILocale;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.servicebus.Trigger;
import hxc.services.IService;
import hxc.services.ServiceType;
import hxc.services.airsim.AirSim;
import hxc.services.airsim.protocol.IAirSim;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.services.notification.INotifications;
import hxc.services.notification.IPhrase;
import hxc.services.notification.ITexts;
import hxc.services.notification.Phrase;
import hxc.services.notification.ReturnCodeTexts;
import hxc.services.numberplan.INumberPlan;
import hxc.services.reporting.IReportingService;
import hxc.services.reporting.ReportParameters;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.sharedaccounts.reporting.SharedAccountsDetailedRevenueReport;
import hxc.services.sharedaccounts.reporting.SharedAccountsDetailedRevenueReportData;
import hxc.services.sharedaccounts.reporting.SharedAccountsDetailedRevenueReportParameters;
import hxc.services.transactions.ITransactionService;
import hxc.utils.calendar.DateRange;
import hxc.utils.calendar.DateRange.Periods;
import hxc.utils.calendar.DateTime;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.instrumentation.Metric;
import hxc.utils.notification.Notification;
import hxc.utils.notification.Notifications;
import hxc.utils.processmodel.Action;
import hxc.utils.processmodel.IMenuItem;
import hxc.utils.processmodel.Menu;
import hxc.utils.processmodel.MenuItem;
import hxc.utils.processmodel.MenuItems;
import hxc.utils.processmodel.Start;
import hxc.utils.protocol.sdp.DedicatedAccountsFileV3_3;
import hxc.utils.protocol.sdp.ThresholdNotificationFileV2;
import hxc.utils.protocol.sdp.ThresholdNotificationFileV3;

public abstract class SharedAccountsBase extends VasService implements IService
{
	final static Logger logger = LoggerFactory.getLogger(SharedAccountsBase.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	protected IServiceBus esb;
	protected ITransactionService transactions;
	protected IAirConnector air;
	protected ILocale locale;
	protected ISmsConnector smsConnector;
	protected IDatabase database;
	protected ILifecycle lifecycle;
	protected INumberPlan numberPlan;
	private IReportingService reporting;
	protected final int expiryMarginDays = 1;
	protected IAirSim airSimulator = null;

	protected Metric serviceTypesMetric = Metric.CreateGraph("Service Types", 60000, "Units", (Object[]) ServiceType.values());
	protected AtomicLong serviceTypes[] = new AtomicLong[ServiceType.values().length];

	protected static final long UNLIMITED = 0x7FFFFFFFFFFFFFFFL;

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

	@Override
	public boolean start(String[] args)
	{

		logger.info("Starting SharedAccounts service...");

		// Must have Transaction Service
		transactions = esb.getFirstService(ITransactionService.class);
		if (transactions == null)
			return false;

		// Must have Air
		air = esb.getFirstConnector(IAirConnector.class);
		if (air == null)
			return false;

		// Must have SMS Connector
		smsConnector = esb.getFirstConnector(ISmsConnector.class);
		if (smsConnector == null)
			return false;

		// Must have Database
		database = esb.getFirstConnector(IDatabase.class);
		if (database == null)
			return false;

		// Must have Lifecycle
		lifecycle = esb.getFirstConnector(ILifecycle.class);
		if (lifecycle == null)
			return false;

		// Must have Number Plan
		numberPlan = esb.getFirstService(INumberPlan.class);
		if (numberPlan == null)
			return false;

		reporting = esb.getFirstService(IReportingService.class);
		if (reporting == null)
			return false;

		// Get Locale
		this.locale = esb.getLocale();

		// Create a USSD Menu Trigger
		Trigger<HuxProcessState> menuTrigger = new Trigger<HuxProcessState>(HuxProcessState.class)
		{
			@Override
			public boolean testCondition(HuxProcessState state)
			{
				return state.getServiceCode().equals(config.shortCode);
			}

			@Override
			public void action(HuxProcessState state, IConnection connection)
			{
				processMenu(state, (HuxConnection) connection);
			}
		};
		esb.addTrigger(menuTrigger);

		// Create a Simulator Trigger
		Trigger<HuxProcessState> simTrigger = new Trigger<HuxProcessState>(HuxProcessState.class)
		{
			@Override
			public boolean testCondition(HuxProcessState state)
			{
				return state.getServiceCode().equals("99E");
			}

			@Override
			public void action(HuxProcessState state, IConnection connection)
			{
				connectAirSim(state, (HuxConnection) connection);
			}
		};
		esb.addTrigger(simTrigger);

		// Create a Lifecycle Trigger
		Trigger<ISubscription> lcylTrigger = new Trigger<ISubscription>(ISubscription.class)
		{
			@Override
			public boolean testCondition(ISubscription subscription)
			{
				return subscription.getServiceID().equalsIgnoreCase(getServiceID());
			}

			@Override
			public void action(ISubscription subscription, IConnection connection)
			{
				processLifecycleEvent(null, subscription);
			}

			@Override
			public boolean isLowPriority(ISubscription message)
			{
				return true;
			}

		};
		esb.addTrigger(lcylTrigger);

		// Create a Decline via SMS Trigger
		Trigger<IInteraction> declineTrigger = new Trigger<IInteraction>(IInteraction.class)
		{
			@Override
			public boolean testCondition(IInteraction message)
			{
				return message.getShortCode().equals(config.smsSourceAddress) && config.declineKeyword.matches(message.getMessage());
			}

			@Override
			public void action(IInteraction message, IConnection connection)
			{
				onConsumerDeclined(message);
			}

		};
		esb.addTrigger(declineTrigger);

		// Create a Data Warehouse Cleanup Trigger
		Trigger<IHistoryCleanup> cleanupTrigger = new Trigger<IHistoryCleanup>(IHistoryCleanup.class)
		{
			@Override
			public boolean testCondition(IHistoryCleanup message)
			{
				return true;
			}

			@Override
			public void action(IHistoryCleanup message, IConnection connection)
			{
				cleanupDataWarehouse(message.getDaysToRetain());
			}

		};
		esb.addTrigger(cleanupTrigger);

		// Create TNP v2 trigger
		Trigger<ThresholdNotificationFileV2> tnp2Trigger = new Trigger<ThresholdNotificationFileV2>(ThresholdNotificationFileV2.class)
		{
			@Override
			public boolean testCondition(ThresholdNotificationFileV2 message)
			{
				// Retrieve VOICE quota
				Quota quota = null;
				for (Quota q : config.quotas)
				{
					if (q.getServiceType() == ServiceType.VOICE)
						quota = q;
				}

				if (quota == null)
					return false;

				return (
				// Direction must be "down"
				(message.thresholdDirection == 'd') &&
				// compare to pre-configured level
				(message.thresholdLimit <= quota.getWarningMargin()));
			}

			@Override
			public void action(ThresholdNotificationFileV2 message, IConnection connection)
			{
				onThresholdNotification(message);
			}

		};
		esb.addTrigger(tnp2Trigger);

		// Create Subscriber Data Trigger
		Trigger<DedicatedAccountsFileV3_3> daTrigger = new Trigger<DedicatedAccountsFileV3_3>(DedicatedAccountsFileV3_3.class)
		{
			@Override
			public boolean testCondition(DedicatedAccountsFileV3_3 record)
			{
				boolean result = true;// canPerformUponDepletionTransfer(record.accountID, record.dedicatedAccountID, record.dedicatedAccountUnitType == 1 ? record.dedicatedAccountBalance :
										// record.dedicatedAccountUnitBalance);
				return result;
			}

			@Override
			public void action(DedicatedAccountsFileV3_3 message, IConnection connection)
			{
				// performUponDepletionTransfer(record.accountID, record.dedicatedAccountID, record.dedicatedAccountUnitType == 1 ? record.dedicatedAccountBalance :
				// record.dedicatedAccountUnitBalance);
				onThresholdNotification(message);
			}

			@Override
			public boolean isLowPriority(DedicatedAccountsFileV3_3 record)
			{
				return true;
			}

		};
		esb.addTrigger(daTrigger);

		// Create TNP v3 trigger handler
		Trigger<ThresholdNotificationFileV3> tnp3Trigger = new Trigger<ThresholdNotificationFileV3>(ThresholdNotificationFileV3.class)
		{
			@Override
			public boolean testCondition(ThresholdNotificationFileV3 message)
			{
				return (message.thresholdDirection == 'd' ? true : false);
			}

			@Override
			public void action(ThresholdNotificationFileV3 message, IConnection connection)
			{
				onThresholdNotification(message);
			}

		};
		esb.addTrigger(tnp3Trigger);

		reporting.addReport(new SharedAccountsDetailedRevenueReport()
		{

			@Override
			public Collection<SharedAccountsDetailedRevenueReportData> getReportData(ReportParameters parameters)
			{
				try (IDatabaseConnection con = database.getConnection(null))
				{
					parameters.validate();
					DateRange dateRange = (parameters instanceof SharedAccountsDetailedRevenueReportParameters) ? ((SharedAccountsDetailedRevenueReportParameters) parameters).getPeriod() : DateRange
							.GetRange(Periods.ThisMonth);

					String query = "SELECT DATE(A.ts) AS SDATE " + //
							", IFNULL((SELECT SUM(CASE WHEN B.serviceType = 'VOICE' THEN B.amount ELSE 0 END) " + //
							"FROM hxc.sa_activity B " + //
								"WHERE B.amount > 0 && B.serviceID = %s && DATE(B.ts) = DATE(A.ts)), 0) AS VOICE_REV " + //
								", IFNULL((SELECT SUM(CASE WHEN B.serviceType = 'DATA' THEN B.amount ELSE 0 END) " + //
							"FROM hxc.sa_activity B " + //
								"WHERE B.amount > 0 && B.serviceID = %s && DATE(B.ts) = DATE(A.ts)), 0) AS DATA_REV " + //
								", IFNULL((SELECT SUM(CASE WHEN B.serviceType = 'SMS' THEN B.amount ELSE 0 END) " + //
							"FROM hxc.sa_activity B " + //
								"WHERE B.amount > 0 && B.serviceID = %s && DATE(B.ts) = DATE(A.ts)), 0) AS SMS_REV " + //
								",IFNULL((SELECT SUM(CASE WHEN B.serviceType = 'MMS' THEN B.amount ELSE 0 END) " + //
							"FROM hxc.sa_activity B " + //
								"WHERE B.amount > 0 && B.serviceID = %s && DATE(B.ts) = DATE(A.ts)), 0) AS MMS_REV " + //
								",IFNULL((SELECT SUM(CASE WHEN B.serviceType = 'AIRTIME' THEN B.amount ELSE 0 END) " + //
							"FROM hxc.sa_activity B " + //
								"WHERE B.amount > 0 && B.serviceID = %s && DATE(B.ts) = DATE(A.ts)), 0) AS AIRTIME_REV " + //
								",SUM(ABS(A.chargeLevied)) AS GLOBAL_REV " + //
							"FROM hxc.dw_transactions A " + //
								"WHERE A.serviceID = %s && A.ts >= %s && A.ts < %s " + //
							"GROUP BY SDATE,VOICE_REV,DATA_REV,SMS_REV,MMS_REV,AIRTIME_REV " + //
							"ORDER BY 1;";

					List<SharedAccountsDetailedRevenueReportData> data = con.selectList(SharedAccountsDetailedRevenueReportData.class, //
							query, getServiceID(), getServiceID(), getServiceID(), getServiceID(), getServiceID(), getServiceID(), //
							dateRange.getStartDate(), dateRange.getEndDateExclusive());
					
					if (data!= null)
					{
						//Correct Currency values
						for(SharedAccountsDetailedRevenueReportData report : data)
						{
							report.setAIRTIME_REV(report.getAIRTIME_REV().movePointLeft(locale.getCurrencyDecimalDigits()) );
							report.setDATA_REV(report.getDATA_REV().movePointLeft(locale.getCurrencyDecimalDigits()) );
							report.setGLOBAL_REV(report.getGLOBAL_REV().movePointLeft(locale.getCurrencyDecimalDigits()) );
							report.setMMS_REV(report.getMMS_REV().movePointLeft(locale.getCurrencyDecimalDigits()) );
							report.setSMS_REV(report.getSMS_REV().movePointLeft(locale.getCurrencyDecimalDigits()) );
							report.setVOICE_REV(report.getVOICE_REV().movePointLeft(locale.getCurrencyDecimalDigits()) );
						}
						
						if (logger.isTraceEnabled())
						{
							Gson gson = new Gson();
							logger.debug("SharedAccountsDetailedRevenueReport Data: " + gson.toJson( data ));
						}
					}
					else
					{
						// Print out query if problem occurs (NULL DATA)
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						logger.debug("SharedAccountsDetailedRevenueReportData returned NULL data");
						logger.trace(query, String.format("'%s'", getServiceID()), 
								String.format("'%s'", getServiceID()), String.format("'%s'", getServiceID()), 
								String.format("'%s'", getServiceID()), String.format("'%s'", getServiceID()), String.format("'%s'", getServiceID()), 
								String.format("'%s'", sdf.format(dateRange.getStartDate())), String.format("'%s'", sdf.format(dateRange.getEndDateExclusive())));
						
					}
					return data;
				}
				catch (Exception e)
				{
					logger.error("Problem Creating SharedAccountsDetailedRevenueReport Error:{}", e.getMessage());
 			  }
				return null;
			}

		});

		for (int i = 0; i < ServiceType.values().length; i++)
		{
			serviceTypes[i] = new AtomicLong();
		}

		// Log Information
		logger.info("Shared Accounts Service Started");

		return true;
	}

	@Override
	public void stop()
	{
		if (airSimulator != null)
			airSimulator.stop();
		
		// Log Information
		logger.info("Shared Accounts Service Stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		this.config = (SharedAccountsConfig) config;
	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return false;
	}

	@Override
	public boolean isFit()
	{
		serviceTypesMetric.report(esb, (Object[]) serviceTypes);
		return true;
	}

	@Override
	public IMetric[] getMetrics()
	{
		return new IMetric[] { serviceTypesMetric };
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configurable Parameters
	//
	// /////////////////////////////////
	@Perms(perms = {
			@Perm(name = "ViewSharedAccountsParameters", description = "View SharedAccounts Parameters", category = "SharedAccounts", supplier = true),
			@Perm(name = "ChangeSharedAccountsParameters", implies = "ViewSharedAccountsParameters", description = "Change SharedAccounts Parameters", category = "SharedAccounts", supplier = true),
			@Perm(name = "ViewSharedAccountsNotifications", description = "View Shared Accounts Notifications", category = "SharedAccounts", supplier = true),
			@Perm(name = "ChangeSharedAccountsNotifications", implies = "ViewSharedAccountsNotifications", description = "Change Shared Accounts Notifications", category = "SharedAccounts", supplier = true) })
	public class SharedAccountsConfig extends ConfigurationBase
	{
		protected Phrase serviceName = Phrase.en("Group Shared Accounts").fre("Group Shared Accounts");

		protected String shortCode = "193";

		protected String smsSourceAddress = "193";

		protected IProcess ussdProcess = SharedAccountsMenu.getMenuProcess(getServiceID());

		protected Phrase declineKeyword = Phrase.en("NO").fre("NON");

		protected Phrase mainAccountBalanceName = Phrase.en("Main").fre("Principal");

		// Renewal Enable Flag - set if subscriptions must be renewed at the end of their validity period.
		protected boolean autoRenew = true;

		// The Dedicated Account to use as Main Account for Post Paid Subscribers, or 0 if the MA is to be used for them as well
		// 1 - Use DA1 as "Main Account" for Post-Paid subscribers 0 - Use MA as "Main Account" for Post-Paid Subscribers
		protected int postPaidAccountID = 0;

		// The Dedicated Account which will contain the sharable Voice Amount for a Provider
		protected int voiceDedicatedAccountID = 1000;

		// The Dedicated Account which will contain the sharable SMS Amount for a Provider
		protected int smsDedicatedAccountID = 2000;

		// The Dedicated Account which will contain the sharable Data Amount for a Provider
		protected int dataDedicatedAccountID = 3000;

		// The Dedicated Account which will contain the sharable MMS Amount for a Provider
		protected int mmsDedicatedAccountID = 4000;

		// The Dedicated Account which will contain the sharable Airtime Amount for a Provider
		protected int airtimeDedicatedAccountID = 5000;

		// The Dedicated Account which accumulates the revenue earned from the Provider
		protected int revenueDedicatedAccountID = 9999;

		// Various aspects of each Variant of the Shared Accounts Service (Daily, Weekly or Monthly)
		// will be configurable through the WUI or CLI as well:

		protected Variant[] variants = new Variant[] {

		new Variant(
		/* variantID */"Daily",
		/* names */Phrase.en("Daily").fre("Quotidien"),
		/* validityPeriodDays */1,
		/* firstRenewalWarningHoursBefore */6,
		/* secondRenewalWarningHoursBefore */0,
		/* subscriptionCharge */0,
		/* renewalCharge */10,
		/* subscriptionOfferID */9997,
		/* consumerOfferID */999700), //

				new Variant(
				/* variantID */"Weekly",
				/* names */Phrase.en("Weekly").fre("Hebdomadaire"),
				/* validityPeriodDays */7,
				/* firstRenewalWarningHoursBefore */30,
				/* secondRenewalWarningHoursBefore */12,
				/* subscriptionCharge */0,
				/* renewalCharge */50,
				/* subscriptionOfferID */9998,
				/* consumerOfferID */999800), //

				new Variant(
				/* variantID */"Monthly",
				/* names */Phrase.en("Monthly").fre("Mensuel"),
				/* validityPeriodDays */30,
				/* firstRenewalWarningHoursBefore */78,
				/* secondRenewalWarningHoursBefore */30,
				/* subscriptionCharge */0,
				/* renewalCharge */200,
				/* subscriptionOfferID */9999,
				/* consumerOfferID */999900) };

		// Various aspects of each Quota type of the Shared Accounts Service
		// will be configurable through the WUI or CLI as well:

		protected Quota[] quotas = new Quota[] {

		new Quota(
		/* quotaID */"Internet_Wknd",
		/* name */Phrase.en("Internet_Wknd").fre("Internet_Wknd"),
		/* service */Phrase.en("Internet").fre("Internet"),
		/* serviceType */ServiceType.DATA,
		/* destination */Phrase.en("Internet").fre("Internet"),
		/* daysOfWeek */Phrase.en("Weekends").fre("Week-End"),
		/* timeOfDay */Phrase.en("Any Time").fre("24H/24"),
		/* unitName */Phrase.en("MB").fre("MB"),
		/* priceCents */2000,
		/* sponsorOfferID */3003,
		/* sponsorUsageCounterID */3000,
		/* beneficiaryOfferID */300300,
		/* beneficiaryUsageCounterID */3003,
		/* beneficiaryWarningUsageThresholdID */300301,
		/* beneficiaryTotalThresholdID */300302,
		/* warningMargin */500000,
		/* minUnits */1,
		/* maxUnits */10000,
		/* csUnitConversionFactor */1000000),

		new Quota(
		/* quotaID */"Calls_C4U",
		/* name */Phrase.en("Calls_C4U").fre("Appels_C4U"),
		/* service */Phrase.en("Calls").fre("Appels"),
		/* serviceType */ServiceType.VOICE,
		/* destination */Phrase.en("C4U to C4U").fre("C4U vers C4U"),
		/* daysOfWeek */Phrase.en("Any Day").fre("7J/7"),
		/* timeOfDay */Phrase.en("Any Time").fre("24H/24"),
		/* unitName */Phrase.en("Minutes").fre("Minutes"),
		/* priceCents */4500,
		/* sponsorOfferID */1001,
		/* sponsorUsageCounterID */1000,
		/* beneficiaryOfferID */100100,
		/* beneficiaryUsageCounterID */1001,
		/* beneficiaryWarningUsageThresholdID */100101,
		/* beneficiaryTotalThresholdID */100102,
		/* warningMargin */60,
		/* minUnits */2,
		/* maxUnits */1000,
		/* csUnitConversionFactor */60),

		new Quota(
		/* quotaID */"Airtime4U",
		/* name */Phrase.en("Airtime4U").fre("Airtime4U"),
		/* service */Phrase.en("Airtime").fre("Credit"),
		/* serviceType */ServiceType.AIRTIME,
		/* destination */Phrase.en("Any").fre("Any"),
		/* daysOfWeek */Phrase.en("Any Day").fre("7J/7"),
		/* timeOfDay */Phrase.en("Any Time").fre("24H/24"),
		/* unitName */Phrase.en("USD").fre("USD"),
		/* priceCents */100,
		/* sponsorOfferID */2002,
		/* sponsorUsageCounterID */2000,
		/* beneficiaryOfferID */200200,
		/* beneficiaryUsageCounterID */2002,
		/* beneficiaryWarningUsageThresholdID */200201,
		/* beneficiaryTotalThresholdID */200202,
		/* warningMargin */10,
		/* minUnits */2,
		/* maxUnits */100000,
		/* csUnitConversionFactor */1),

		};

		// Various aspects of each Service Class as it relates to the Shared Accounts Service
		// will be configurable through the WUI or CLI as well:

		// @formatter:on

		protected ServiceClass[] serviceClasses = new ServiceClass[] { new ServiceClass(
		/* serviceClassID */100,
		/* names */Phrase.en("C4U Silver").fre("C4U Silver"),
		/* eligibleForProvider */true,
		/* eligibleForConsumer */true,
		/* eligibleForProsumer */true,
		/* postPaid */false,
		/* maxConsumers */10,
		/* addConsumerCharge */0,
		/* removeConsumerCharge */0,
		/* unsubscribeCharge */0,
		/* removeQuotaCharge */0,
		/* providerBalanceEnquiryCharge */0,
		/* consumerBalanceEnquiryCharge */0), //

				new ServiceClass(
				/* serviceClassID */101,
				/* names */Phrase.en("C4U Gold").fre("C4U Gold"),
				/* eligibleForProvider */true,
				/* eligibleForConsumer */true,
				/* eligibleForProsumer */true,
				/* postPaid */false,
				/* maxConsumers */10,
				/* addConsumerCharge */0,
				/* removeConsumerCharge */0,
				/* unsubscribeCharge */0,
				/* removeQuotaCharge */0,
				/* providerBalanceEnquiryCharge */0,
				/* consumerBalanceEnquiryCharge */0), //
		};

		// @formatter:on

		// Service Specific error Mapping
		ReturnCodeTexts[] returnCodesTexts = new ReturnCodeTexts[] {

		new ReturnCodeTexts(ReturnCodes.alreadyAdded, //
				Phrase.en("This offer has already been added for the beneficiary") //
						.fre("Cette offre existe déjà pour le bénéfiaire")),

		new ReturnCodeTexts(ReturnCodes.alreadyMember, //
				Phrase.en("Beneficiary has already been added") //
						.fre("Le bénéficiaire a déjà été rajouté")),

		new ReturnCodeTexts(ReturnCodes.alreadySubscribed, //
				Phrase.en("You are already subscribed") //
						.fre("Vous etes déjà abonné")),

		new ReturnCodeTexts(ReturnCodes.authorizationFailure, //
				Phrase.en("Supplied user/password has not been authorized") //
						.fre("Utilisateur/Mot de passe fournis non autorisés")),

		new ReturnCodeTexts(ReturnCodes.cannotBeAdded, //
				Phrase.en("The offer cannot be added") //
						.fre("L'offre ne peux pas etre ajoutée")),

		new ReturnCodeTexts(ReturnCodes.incomplete, //
				Phrase.en("Request incomplete") //
						.fre("Requete incomplete")),

		new ReturnCodeTexts(ReturnCodes.insufficientBalance, //
				Phrase.en("Sorry, you do not have enough credit.  Please refill or dial *121# to borrow credit") //
						.fre("Désolé, crédit insuffisant.  Veuillez recharger ou tapez *121# pour emprunter du crédit")),

		new ReturnCodeTexts(ReturnCodes.invalidArguments, //
				Phrase.en("Invalid arguments have been supplied") //
						.fre("Paramètres fournis invalides")),

		new ReturnCodeTexts(ReturnCodes.invalidNumber, //
				Phrase.en("An invalid number has been supplied")//
						.fre("Numéro fourni invalide")),

		new ReturnCodeTexts(ReturnCodes.invalidQuota, //
				Phrase.en("An invalid offer has been specified") //
						.fre("Un offre invalide a été spécifiée")),

		new ReturnCodeTexts(ReturnCodes.malformedRequest, //
				Phrase.en("The request is malformed.  To access to Group Shared Accounts, dial *193#") //
						.fre("Requete malformée.  Pour accéder à Group Shared Accounts, composez *193#")),

		new ReturnCodeTexts(ReturnCodes.maxMembersExceeded, //
				Phrase.en("You already have the maximum number of Beneficiaries allowed.To change beneficiaries, dial *193#") //
						.fre("Vous avez déjà atteint le nombre max de bénéficiaires autorisé.  Pour changer des bénéficiaires, tapez *193#")),

		new ReturnCodeTexts(ReturnCodes.notEligible, //
				Phrase.en("You are not eligible for the service") //
						.fre("Vous n'etes pas élligible au service")),

		new ReturnCodeTexts(ReturnCodes.notMember, //
				Phrase.en("This beneficiary has not been previously added") //
						.fre("Ce bénéficiaire n'a pas été rajouté auparavant")),

		new ReturnCodeTexts(ReturnCodes.notSubscribed, //
				Phrase.en("The subscriber is not subscribed to the service") //
						.fre("Vous n'etes pas abonné au service")),

		new ReturnCodeTexts(ReturnCodes.notSupported, //
				Phrase.en("The operation is not supported for this service") //
						.fre("Opération non supportée pour ce service")),

		new ReturnCodeTexts(ReturnCodes.quotaNotSet, //
				Phrase.en("Reference to an offer which has not been set") //
						.fre("Référence à une offre non configurée")),

		new ReturnCodeTexts(ReturnCodes.technicalProblem, //
				Phrase.en("A Technical Problem has occurred.  Please try again later.") //
						.fre("Un problème technique est apparu.  Veuillez réessayer plus tard")),

		new ReturnCodeTexts(ReturnCodes.temporaryBlocked, //
				Phrase.en("Sorry, the Service is not currently available.  Try later") //
						.fre("Désolé, le Service n'est disponible pour l'instant, essayez plus tard")),

		new ReturnCodeTexts(ReturnCodes.timedOut, //
				Phrase.en("A Technical Problem has occurred.  Please try again later.") //
						.fre("Un problème technique est apparu.  Veuillez réessayer plus tard")),

		new ReturnCodeTexts(ReturnCodes.alreadyOtherMember, //
				Phrase.en("The subscriber is already registered as a beneficiary") //
						.fre("L'abonné est déjà inscrit comme bénéficiaire")),

		new ReturnCodeTexts(ReturnCodes.memberNotEligible, //
				Phrase.en("The number you have entered is not eligible for the service") //
						.fre("Le numero fourni n'etes pas élligible au service")),

		new ReturnCodeTexts(ReturnCodes.alreadyOwner, //
				Phrase.en("The subscriber is already a Provider and cannot be added as a beneficiary") //
						.fre("L'abonné est déjà un fournisseur et ne peux pas etre rajouté comme bénéficiaire")),

		};

		public Phrase getServiceName()
		{
			check(esb, "ChangeSharedAccountsParameters");
			return serviceName;
		}

		public void setShortCode(String shortCode)
		{
			check(esb, "ChangeSharedAccountsParameters");
			this.shortCode = shortCode;
		}

		public IProcess getUssdProcess()
		{
			check(esb, "ViewSharedAccountsParameters");
			return ussdProcess;
		}

		public String getSmsSourceAddress()
		{
			check(esb, "ChangeSharedAccountsParameters");
			return smsSourceAddress;
		}

		public void setSmsSourceAddress(String smsSourceAddress)
		{
			check(esb, "ViewSharedAccountsParameters");
			this.smsSourceAddress = smsSourceAddress;
		}

		public void setUssdProcess(IProcess ussdProcess) throws ValidationException
		{
			check(esb, "ChangeSharedAccountsParameters");

			try
			{
				// Validate USSD properties
				Start start = (Start) ussdProcess.getStart();
				validateAction(start);
			}
			catch (ValidationException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				throw new ValidationException("Could not validate USSD menu: %s", e.getMessage());
			}

			this.ussdProcess = ussdProcess;
		}

		public boolean isAutoRenew()
		{
			check(esb, "ViewSharedAccountsParameters");
			return autoRenew;
		}

		public void setAutoRenew(boolean autoRenew)
		{
			check(esb, "ChangeSharedAccountsParameters");
			this.autoRenew = autoRenew;
		}

		public int getPostPaidAccountID()
		{
			check(esb, "ViewSharedAccountsParameters");
			return postPaidAccountID;
		}

		public void setPostPaidAccountID(int postPaidAccountID)
		{
			check(esb, "ChangeSharedAccountsParameters");
			this.postPaidAccountID = postPaidAccountID;
		}

		public int getVoiceDedicatedAccountID()
		{
			check(esb, "ViewSharedAccountsParameters");
			return voiceDedicatedAccountID;
		}

		public void setVoiceDedicatedAccountID(int voiceDedicatedAccountID)
		{
			check(esb, "ChangeSharedAccountsParameters");
			this.voiceDedicatedAccountID = voiceDedicatedAccountID;
		}

		public int getSmsDedicatedAccountID()
		{
			check(esb, "ViewSharedAccountsParameters");
			return smsDedicatedAccountID;
		}

		public void setSmsDedicatedAccountID(int smsDedicatedAccountID)
		{
			check(esb, "ChangeSharedAccountsParameters");
			this.smsDedicatedAccountID = smsDedicatedAccountID;
		}

		public int getDataDedicatedAccountID()
		{
			check(esb, "ViewSharedAccountsParameters");
			return dataDedicatedAccountID;
		}

		public void setDataDedicatedAccountID(int dataDedicatedAccountID)
		{
			check(esb, "ChangeSharedAccountsParameters");
			this.dataDedicatedAccountID = dataDedicatedAccountID;
		}

		public int getMmsDedicatedAccountID()
		{
			check(esb, "ViewSharedAccountsParameters");
			return mmsDedicatedAccountID;
		}

		public void setMmsDedicatedAccountID(int mmsDedicatedAccountID)
		{
			check(esb, "ChangeSharedAccountsParameters");
			this.mmsDedicatedAccountID = mmsDedicatedAccountID;
		}

		public int getAirtimeDedicatedAccountID()
		{
			check(esb, "ViewSharedAccountsParameters");
			return airtimeDedicatedAccountID;
		}

		public void setAirtimeDedicatedAccountID(int airtimeDedicatedAccountID)
		{
			check(esb, "ChangeSharedAccountsParameters");
			this.airtimeDedicatedAccountID = airtimeDedicatedAccountID;
		}

		public int getRevenueDedicatedAccountID()
		{
			check(esb, "ViewSharedAccountsParameters");
			return revenueDedicatedAccountID;
		}

		public void setRevenueDedicatedAccountID(int revenueDedicatedAccountID)
		{
			check(esb, "ChangeSharedAccountsParameters");
			this.revenueDedicatedAccountID = revenueDedicatedAccountID;
		}

		public void setVariants(Variant[] variants)
		{
			check(esb, "ChangeSharedAccountsParameters");
			this.variants = variants;
		}

		public Variant[] getVariants()
		{
			check(esb, "ViewSharedAccountsParameters");
			return variants;
		}

		public String getShortCode()
		{
			check(esb, "ViewSharedAccountsParameters");
			return shortCode;
		}

		public Quota[] getQuotas()
		{
			check(esb, "ViewSharedAccountsParameters");
			return quotas;
		}

		public void setQuotas(Quota[] quotas)
		{
			check(esb, "ChangeSharedAccountsParameters");
			this.quotas = quotas;
		}

		public ServiceClass[] getServiceClasses()
		{
			check(esb, "ViewSharedAccountsParameters");
			return serviceClasses;
		}

		public void setServiceClasses(ServiceClass[] serviceClasses)
		{
			check(esb, "ChangeSharedAccountsParameters");
			this.serviceClasses = serviceClasses;
		}

		public IPhrase getDeclineKeyword()
		{
			check(esb, "ViewSharedAccountsParameters");
			return declineKeyword;
		}

		public void setDeclineKeyword(Phrase declineKeyword)
		{
			check(esb, "ChangeSharedAccountsParameters");
			this.declineKeyword = declineKeyword;
		}

		public Phrase getMainAccountBalanceName()
		{
			check(esb, "ViewSharedAccountsParameters");
			return mainAccountBalanceName;
		}

		public void setMainAccountBalanceName(Phrase mainAccountBalanceName)
		{
			check(esb, "ChangeSharedAccountsParameters");
			this.mainAccountBalanceName = mainAccountBalanceName;
		}

		public ReturnCodeTexts[] getReturnCodesTexts()
		{
			check(esb, "ViewSharedAccountsParameters");
			return returnCodesTexts;
		}

		public void setReturnCodesTexts(ReturnCodeTexts[] returnCodesTexts)
		{
			check(esb, "ChangeSharedAccountsParameters");
			this.returnCodesTexts = returnCodesTexts;
		}

		@Override
		public String getPath(String languageCode)
		{
			return "VAS Services";
		}

		@Override
		public INotifications getNotifications()
		{
			return notifications;
		}

		@Override
		public long getSerialVersionUID()
		{
			return -9213363036854775808L;
		}

		@Override
		public String getName(String languageCode)
		{
			return serviceName.getSafe(languageCode, "Group Shared Accounts");
		}

		@Override
		public void validate() throws ValidationException
		{
			for (Quota quota : quotas)
			{
				quota.validate(quotas);
			}
		}

		@Override
		public void performUpdateNotificationSecurityCheck()
		{
			check(esb, "ChangeSharedAccountsNotifications");
		}

		@Override
		public void performGetNotificationSecurityCheck()
		{
			check(esb, "ViewSharedAccountsNotifications");
		}

	} // SharedAccountConfig

	SharedAccountsConfig config = new SharedAccountsConfig();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Notifications
	//
	// /////////////////////////////////
	// protected Notifications notifications = new Notifications(Properties.class);
	public Notifications notifications = new Notifications(Properties.class);

	// Subscription ///
	protected int smsProviderSubscribed = notifications.add("Successfull Subscription", //
			"You have been subscribed to the {Variant} Group Shared Accounts available until {ExpiryDate}", //
			"Vous avez souscrit à un abonnement {Variant} à Group Shared Accounts valable jusqu'au {ExpiryDate}");

	// Added Consumer ///
	protected int smsProviderAddedConsumer = notifications.add("Added Consumer", //
			"", "");;

	protected int smsConsumerAddedConsumer = notifications.add("Being Added as Consumer", //
			"{ProviderMSISDN} wants to share credit with you.  Reply with {DeclineKeyword} to opt out", //
			"{ProviderMSISDN} veux partager du credit avec vous.  Repondez par {DeclineKeyword} pour décliner");

	// Added Quota ///
	protected int smsProviderAddedQuota = notifications.add("Added Quota", //
			"You have shared {SharedQuantity} {Units} {Destination} with {ConsumerMSISDN} {DaysOfWeek}, {TimeOfDay} for {Charge} USD", //
			"Vous avez partagé {SharedQuantity} {Units} {Destination} avec {ConsumerMSISDN} {DaysOfWeek}, {TimeOfDay} à {Charge} USD");

	protected int smsConsumerAddedQuota = notifications.add("Received Quota", //
			"{ProviderMSISDN} has shared {SharedQuantity} {Units} {Destination} with you until {ExpiryDate}.  Period: {DaysOfWeek} {TimeOfDay}.  To opt out, send {DeclineKeyword} to 193", //
			"{ProviderMSISDN} a partagé {SharedQuantity} {Units} {Destination} avec vous jusqu'au {ExpiryDate}.  Période: {DaysOfWeek}, {TimeOfDay}.Envoyer {DeclineKeyword} au 193 pour décliner");

	// Changed Quota
	protected int smsProviderChangedQuota = notifications.add("Change Quota", //
			"You have now shared {SharedQuantity} {Units} {Destination} with {ConsumerMSISDN} {DaysOfWeek}, {TimeOfDay} for {Charge} USD", //
			"Vous partagez maintenant {SharedQuantity} {Units} {Destination} avec {ConsumerMSISDN} {DaysOfWeek}, {TimeOfDay} à {Charge} USD");

	protected int smsConsumerChangedQuota = notifications
			.add("Received Changed Quota", //
					"{ProviderMSISDN} has now shared {SharedQuantity} {Units} {Destination} with you until {ExpiryDate}.  Period: {DaysOfWeek} {TimeOfDay}.  To opt out, send {DeclineKeyword} to 193", //
					"{ProviderMSISDN} partage maintenant {SharedQuantity} {Units} {Destination} avec vous jusqu'au {ExpiryDate}.  Période: {DaysOfWeek}, {TimeOfDay}.Envoyer {DeclineKeyword} au 193 pour décliner");

	// Removed Quota ///
	protected int smsProviderRemovedQuota = notifications.add("Removed Quota", //
			"You are no longer sharing {Service} ({Destination}) with {ConsumerMSISDN}.", //
			"Vous ne partagez plus le service {Service} ({Destination}) avec {ConsumerMSISDN}.");

	protected int smsConsumerRemovedQuota = notifications.add("Lost Quota", //
			"{ProviderMSISDN} is no longer sharing {Service} ({Destination}) with you.", //
			"{ProviderMSISDN} ne partage plus le service {Service} ({Destination}) avec vous.");

	// Removed Consumer ///
	protected int smsProviderRemovedConsumer = notifications.add("Removed Consumer", //
			"You have removed {ConsumerMSISDN} from your Shared Accounts for {Charge} USD", //
			"Vous avez enlevé {ConsumerMSISDN} de votre partage de crédit pour {Charge} USD");

	protected int smsConsumerRemovedConsumer = notifications.add("Being Removed as Consumer", //
			"You are no longer able to share {ProviderMSISDN}'s credit.", //
			"Vous ne partagez plus le credit de {ProviderMSISDN}.");

	// Un-subscribed ///
	protected int smsProviderUnsubscribed = notifications.add("Un-Subscribed", //
			"You have been un-subscribed from the {Variant} Group Shared Accounts at {Charge} USD", //
			"Vous avez annulé votre abonnement {Variant} à Group Shared Accounts à {Charge} USD");

	// Renewal Warnings ///
	protected int smsProviderRenewalWarning = notifications
			.add("Renewal Warning", //
					"Your {Variant} subscription to Group Shared Accounts will be renewed in {HoursBeforeExpiry} hours.  Please ensure that you have at least {Charge} USD .  To opt out dial *193# and unsubscribe", //
					"Votre abonnement {Variant} a Group Shared Accounts va etre renouvellé dans {HoursBeforeExpiry} heures.  Assurez vous d'avoir au moins {Charge} USD.  Pour annuler tapez *193# puis se désabonner");

	// Renewal ///
	protected int smsProviderRenewed = notifications.add("Provider Renewed", //
			"Your {Variant} subscription to Group Shared Accounts has been renewed until {NewExpiryDate} hours at {Charge} USD", //
			"Votre abonnement {Variant} à Group Shared Accounts a été renouvellé jusqu'au {NewExpiryDate} à {Charge} USD.");

	protected int smsConsumerRenewed = notifications.add("Provider Renewed Consumer", //
			"{ProviderMSISDN} has extended Shared Accounts with you until {NewExpiryDate}", //
			"{ProviderMSISDN} a étendu le partage de crédit avec vous jusqu'au {NewExpiryDate}");

	// Balances ///
	protected int smsProviderBalances = notifications.add("Provider Balances", //
			"Check your Group Shared Accounts balance: {BalanceList}", //
			"Consultez votre solde Group Shared Accounts: {BalanceList}");

	protected int smsConsumerBalances = notifications.add("Consumer Balances", //
			"Check your Group Shared Accounts Balance: {BalanceList}", //
			"Consultez votre solde Group Shared Accounts: {BalanceList}");

	// Thresholds ///
	protected int smsProviderThresholds = notifications.add("Provider Usage Thresholds", //
			"Provider account threshold reached, please topup: {NotificationThreshold} percent", //
			"Seuil de compte fournisseur atteint, s'il vous plaît compléter: {NotificationThreshold} percent");

	protected int smsProsumerThresholds = notifications.add("Prosumer Usage Thresholds", //
			"Prosumer account threshold reached, please topup: {NotificationThreshold} percent", //
			"Seuil de compte fournisseur atteint, s'il vous plaît compléter: {NotificationThreshold} percent");

	protected int smsConsumerThresholds = notifications.add("Consumer Usage Thresholds", //
			"Provider account threshold reached, use your own funds: {NotificationThreshold} percent", //
			"Seuil de compte fournisseur atteint, utiliser vos propres fonds: {NotificationThreshold} percent");

	public int getSmsConsumerThresholds()
	{
		return smsConsumerThresholds;
	}

	public int getSmsProviderThresholds()
	{
		return smsProviderThresholds;
	}

	// Decline
	protected int smsConsumerDeclineFailed = notifications.add("Decline Failed", //
			"Decline Failed.  Please try again later", //
			"Echec du rejet.  Veuillez réessayer plus tard");

	protected int smsConsumerDeclineNotOne = notifications.add("Decline not Consumer", //
			"You are not subscribed to Group Shared Accounts", //
			"Vous n'etes pas abonné à Group Shared Accounts");

	protected int smsProviderConsumerDeclined = notifications.add("Consumer Declined", //
			"{ConsumerMSISDN} declined your Shared Accounts Offer", //
			"{ConsumerMSISDN} a décliné votre offre de partage de crédit");

	protected int smsProviderBalanceFormat = notifications.add("Provider Balance Format", //
			"{SharedQuantity} USD for {Service}", //
			"{SharedQuantity} USD pour {Service}");

	protected int smsConsumerBalanceFormat = notifications.add("Consumer Balance Format", //
			"{SharedQuantity} {Units} for {QuotaName}", //
			"{SharedQuantity} {Units} pour {QuotaName}");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	class Properties
	{
		protected IPhrase variant;
		protected String charge = "0";
		protected String expiryDate;
		protected String providerMSISDN;
		protected String consumerMSISDN;
		protected String hoursBeforeExpiry;
		protected String newExpiryDate;
		protected String balanceList;

		protected Phrase quotaName;
		protected String sharedQuantity;
		protected Phrase destination;
		protected Phrase service;
		protected Phrase units;
		protected Phrase daysOfWeek;
		protected Phrase timeOfDay;

		protected String notificationThreshold = "40"; // 40%

		public String getVariant(String languageCode)
		{
			return variant.getSafe(languageCode, "Default");
		}

		public void setVariant(IPhrase variant)
		{
			this.variant = variant;
		}

		public String getCharge()
		{
			return charge;
		}

		public void setCharge(String charge)
		{
			this.charge = charge;
		}

		public String getExpiryDate()
		{
			return expiryDate;
		}

		public void setExpiryDate(String expiryDate)
		{
			this.expiryDate = expiryDate;
		}

		public String getDeclineKeyword(String languageCode)
		{
			return config.declineKeyword.getSafe(languageCode, "");
		}

		public String getProviderMSISDN()
		{
			return providerMSISDN;
		}

		public void setProviderMSISDN(String providerMSISDN)
		{
			this.providerMSISDN = providerMSISDN;
		}

		public String getConsumerMSISDN()
		{
			return consumerMSISDN;
		}

		public void setConsumerMSISDN(String consumerMSISDN)
		{
			this.consumerMSISDN = consumerMSISDN;
		}

		public String getHoursBeforeExpiry()
		{
			return hoursBeforeExpiry;
		}

		public void setHoursBeforeExpiry(String hoursBeforeExpiry)
		{
			this.hoursBeforeExpiry = hoursBeforeExpiry;
		}

		public String getQuotaName(String languageCode)
		{
			return quotaName.getSafe(languageCode, "");
		}

		public void setQuotaName(Phrase quotaName)
		{
			this.quotaName = quotaName;
		}

		public String getSharedQuantity()
		{
			return sharedQuantity;
		}

		public void setSharedQuantity(String sharedQuantity)
		{
			this.sharedQuantity = sharedQuantity;
		}

		public String getDestination(String languageCode)
		{
			return destination.getSafe(languageCode, "");
		}

		public void setDestination(Phrase destination)
		{
			this.destination = destination;
		}

		public String getService(String languageCode)
		{
			return service.getSafe(languageCode, "");
		}

		public void setService(Phrase service)
		{
			this.service = service;
		}

		public String getUnits(String languageCode)
		{
			return units.getSafe(languageCode, "");
		}

		public void setUnits(Phrase units)
		{
			this.units = units;
		}

		public String getDaysOfWeek(String languageCode)
		{
			return daysOfWeek.getSafe(languageCode, "");
		}

		public void setDaysOfWeek(Phrase daysOfWeek)
		{
			this.daysOfWeek = daysOfWeek;
		}

		public String getTimeOfDay(String languageCode)
		{
			return timeOfDay.getSafe(languageCode, "");
		}

		public void setTimeOfDay(Phrase timeOfDay)
		{
			this.timeOfDay = timeOfDay;
		}

		public String getNewExpiryDate()
		{
			return newExpiryDate;
		}

		public void setNewExpiryDate(String newExpiryDate)
		{
			this.newExpiryDate = newExpiryDate;
		}

		public String getBalanceList()
		{
			return balanceList;
		}

		public void setBalanceList(String balanceList)
		{
			this.balanceList = balanceList;
		}

		public void setNotificationThreshold(String notificationThreshold)
		{
			this.notificationThreshold = notificationThreshold;
		}

		public String getNotificationThreshold()
		{
			return this.notificationThreshold;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	private void processMenu(HuxProcessState state, HuxConnection connection)
	{

		// Plug-in Process Definition
		if (state.getCurrentAction() == null)
		{
			state.setCurrentAction((Action) config.ussdProcess);
			state.setVasService(this);
			state.setSubscriberProxy(new Subscriber(state.getSubscriberNumber().toString(), air, null));
			state.setNotifications(notifications);
		}

		// Execute Process
		state.setConnection(connection);
		state.execute();
	}

	private void connectAirSim(HuxProcessState state, HuxConnection connection)
	{
		airSimulator = new AirSim(esb, 10010, "/Air", numberPlan, "CFR");
		airSimulator.start();
		hxc.services.airsim.protocol.Subscriber subscriberA = airSimulator.addSubscriber("0824452655", 2, 101, 14000, SubscriberState.active);
		airSimulator.addSubscriber("0824452656", 2, 101, 14000, SubscriberState.active);
		airSimulator.addSubscriber("0824452657", 2, 101, 14000, SubscriberState.active);
		IServiceContext context = new ServiceContext();

		IDatabase database = esb.getFirstConnector(IDatabase.class);
		try (IDatabaseConnection db = database.getConnection(null))
		{
			ILifecycle lifecycle = esb.getFirstConnector(ILifecycle.class);
			lifecycle.removeSubscriptions(db, subscriberA, getServiceID());
			for (Variant variant : config.getVariants())
			{
				lifecycle.removeMembers(db, subscriberA, getServiceID(), variant.getVariantID());
			}
		}
		catch (Exception e)
		{
			logger.error("Air Mockery Failed", e);
			state.setCompleted(true);
			state.setOutput("Air Mockery Failed");
			connection.display(state);
			return;
		}

		// Subscribe
		{
			SubscribeRequest request = new SubscribeRequest();
			initialize(subscriberA.getInternationalNumber(), request);
			request.setServiceID(getServiceID());
			request.setVariantID("Weekly");
			request.setSubscriberNumber(new Number(subscriberA.getInternationalNumber()));
			subscribe(context, request);
		}

		state.setCompleted(true);
		state.setOutput("Air Mockery Started");
		connection.display(state);
	}

	private void initialize(String msisdn, RequestHeader request)
	{
		request.setCallerID(msisdn);
		request.setChannel(Channels.INTERNAL);
		request.setHostName("local");
		request.setTransactionID("00012");
		request.setSessionID("001");
		request.setVersion("1");
		request.setMode(RequestModes.normal);
		request.setLanguageID(2);
	}

	protected abstract void onConsumerDeclined(IInteraction message);

	protected abstract void onThresholdNotification(ThresholdNotificationFileV2 message);

	protected abstract void onThresholdNotification(ThresholdNotificationFileV3 message);

	protected abstract void onThresholdNotification(DedicatedAccountsFileV3_3 message);

	private ArrayList<String> menus = new ArrayList<>();

	private void validateAction(Action action) throws ValidationException
	{
		if (action == null)
			return;

		if (action instanceof Start)
			menus = new ArrayList<>();

		validateAction(action.getNextAction());

		if (action instanceof Menu)
		{
			Menu menu = (Menu) action;

			if (menus.contains(menu.getCaption().getModelText()))
				return;

			menus.add(menu.getCaption().getModelText());

			validateText(menu.getCaption());

			for (IMenuItem item : menu.getItems())
			{
				if (item instanceof MenuItem)
				{
					validateText(((MenuItem) item).getText());
				}
				else if (item instanceof Menu)
				{
					validateText(((Menu) item).getCaption());
				}
				else if (item instanceof MenuItems<?>)
				{
					validateText(((MenuItems<?>) item).getText());
					validateText(((MenuItems<?>) item).getEmptyText());
				}
				validateAction(((Action) item).getNextAction());
			}
		}
		else if (action instanceof MenuItem)
		{
			validateText(((MenuItem) action).getText());
		}
		else if (action instanceof MenuItems<?>)
		{
			validateText(((MenuItems<?>) action).getText());
			validateText(((MenuItems<?>) action).getEmptyText());
		}
	}

	private Notification not = new Notification(notifications);

	private void validateText(ITexts text) throws ValidationException
	{
		try
		{
			if (!text.getModelText().contains("{}"))
				not.setText(1, text.getModelText());
		}
		catch (IllegalArgumentException e)
		{
			throw new ValidationException("Invalid property %s for %s", e.getMessage(), text.getModelText());
		}
		catch (Exception e)
		{

		}

		for (int i = 0; i < IPhrase.MAX_LANGUAGES; i++)
		{
			String t = text.getText(i);
			try
			{
				if (!t.contains("{}"))
					not.setText(1, t);
			}
			catch (IllegalArgumentException e)
			{
				throw new ValidationException("Invalid property %s for %s", e.getMessage(), text.getModelText());
			}
			catch (Exception e)
			{

			}
		}
	}

	// Cleanup Data Warehouse
	private void cleanupDataWarehouse(int daysToRetain)
	{
		Date cutOffDate = DateTime.getToday().addDays(-daysToRetain);
		try (IDatabaseConnection db = database.getConnection(null))
		{
			db.delete(Activity.class, "where startTime < %s", cutOffDate);
		}
		catch (Exception e)
		{
			logger.error("cleanupDataWarehouse failed", e);
		}
	}

	@Override
	protected ReturnCodeTexts[] getReturnCodeTexts()
	{
		return config.getReturnCodesTexts();
	}

}
