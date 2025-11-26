package hxc.services.advancedtransfer;

import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.CreditTransferType;
import com.concurrent.hxc.IServiceContext;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.RequestHeader;
import com.concurrent.hxc.ServiceContext;
import com.concurrent.hxc.SubscribeRequest;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.Channels;
import hxc.connectors.IConnection;
import hxc.connectors.IInteraction;
import hxc.connectors.air.IAirConnector;
import hxc.connectors.air.proxy.Subscriber;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.hux.HuxConnection;
import hxc.connectors.hux.HuxProcessState;
import hxc.connectors.lifecycle.ILifecycle;
import hxc.connectors.lifecycle.ISubscription;
import hxc.connectors.lifecycle.ITemporalTrigger;
import hxc.connectors.sms.ISmsConnector;
import hxc.connectors.soap.ISoapConnector;
import hxc.connectors.vas.VasCommand;
import hxc.connectors.vas.VasService;
import hxc.processmodel.IProcess;
import hxc.servicebus.ILocale;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.servicebus.Trigger;
import hxc.services.IService;
import hxc.services.airsim.AirSim;
import hxc.services.airsim.protocol.IAirSim;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.services.notification.INotifications;
import hxc.services.notification.IPhrase;
import hxc.services.notification.ITexts;
import hxc.services.notification.Phrase;
import hxc.services.notification.ReturnCodeTexts;
import hxc.services.numberplan.INumberPlan;
import hxc.services.pin.IPinService;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.transactions.ITransactionService;
import hxc.utils.calendar.TimeUnits;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.notification.Notification;
import hxc.utils.notification.Notifications;
import hxc.utils.processmodel.Action;
import hxc.utils.processmodel.IMenuItem;
import hxc.utils.processmodel.Menu;
import hxc.utils.processmodel.MenuItem;
import hxc.utils.processmodel.MenuItems;
import hxc.utils.processmodel.Start;
import hxc.utils.protocol.sdp.DedicatedAccountsFileV3_3;
import hxc.utils.protocol.sdp.SubscriberFileV3_3;
import hxc.utils.protocol.sdp.ThresholdNotificationFileV2;
import hxc.utils.protocol.sdp.ThresholdNotificationFileV3;

public abstract class AdvancedTransferBase extends VasService implements IService
{
	final static Logger logger = LoggerFactory.getLogger(AdvancedTransferBase.class);
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
	protected IAirSim airSimulator;
	protected ISoapConnector soapConnector;
	protected IPinService pinService;
	protected Map<Integer, String[]> sdpAccountMap = null;

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

		// Must have Soap Connector
		soapConnector = esb.getFirstConnector(ISoapConnector.class);
		if (soapConnector == null)
			return false;

		// Must have PIN Service
		pinService = esb.getFirstService(IPinService.class);
		if (pinService == null)
			return false;

		// Get Locale
		this.locale = esb.getLocale();

		// Create a USSD Menu Trigger
		Trigger<HuxProcessState> menuTrigger = new Trigger<HuxProcessState>(HuxProcessState.class)
		{
			@Override
			public boolean testCondition(HuxProcessState state)
			{
				return state.getServiceCode().equals(config.shortCode) && "#".equals(state.getRequestString());
			}

			@Override
			public void action(HuxProcessState state, IConnection connection)
			{
				processMenu(state, (HuxConnection) connection);
			}
		};
		esb.addTrigger(menuTrigger);

		// Create an SMS/USSD Trigger
		Trigger<IInteraction> smsTrigger = new Trigger<IInteraction>(IInteraction.class)
		{
			@Override
			public boolean testCondition(IInteraction interaction)
			{
				return interaction.getShortCode().equals(config.shortCode) && commandParser != null && commandParser.canExecute(interaction.getMessage());
			}

			@Override
			public void action(IInteraction interaction, IConnection connection)
			{
				commandParser.execute(interaction, locale);
			}
		};
		esb.addTrigger(smsTrigger);

		// Create a Simulator Trigger
		Trigger<HuxProcessState> simTrigger = new Trigger<HuxProcessState>(HuxProcessState.class)
		{
			@Override
			public boolean testCondition(HuxProcessState state)
			{
				return state.getServiceCode().equalsIgnoreCase("99C");
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

		// Create TNP v2 trigger
		Trigger<ThresholdNotificationFileV2> tnp2Trigger = new Trigger<ThresholdNotificationFileV2>(ThresholdNotificationFileV2.class)
		{
			@Override
			public boolean testCondition(ThresholdNotificationFileV2 message)
			{
				return canPerformUponDepletionTransfer(message);
			}

			@Override
			public void action(ThresholdNotificationFileV2 message, IConnection connection)
			{
				performUponDepletionTransfer(message);
			}

		};
		esb.addTrigger(tnp2Trigger);

		// Create TNP v3 trigger
		Trigger<ThresholdNotificationFileV3> tnp3Trigger = new Trigger<ThresholdNotificationFileV3>(ThresholdNotificationFileV3.class)
		{
			@Override
			public boolean testCondition(ThresholdNotificationFileV3 message)
			{
				return canPerformUponDepletionTransfer(message);
			}

			@Override
			public void action(ThresholdNotificationFileV3 message, IConnection connection)
			{
				performUponDepletionTransfer(message);
			}

		};
		esb.addTrigger(tnp3Trigger);

		// Create Subscriber Data Trigger
		Trigger<SubscriberFileV3_3> subscriberTrigger = new Trigger<SubscriberFileV3_3>(SubscriberFileV3_3.class)
		{
			@Override
			public boolean testCondition(SubscriberFileV3_3 record)
			{
				return canPerformUponDepletionTransfer(record.accountID, 0, record.accountBalance);
			}

			@Override
			public void action(SubscriberFileV3_3 record, IConnection connection)
			{
				performUponDepletionTransfer(record.accountID, 0, record.accountBalance);
			}

			@Override
			public boolean isLowPriority(SubscriberFileV3_3 record)
			{
				return true;
			}

		};
		esb.addTrigger(subscriberTrigger);

		// Create Subscriber Data Trigger
		Trigger<DedicatedAccountsFileV3_3> daTrigger = new Trigger<DedicatedAccountsFileV3_3>(DedicatedAccountsFileV3_3.class)
		{
			@Override
			public boolean testCondition(DedicatedAccountsFileV3_3 record)
			{
				boolean result = canPerformUponDepletionTransfer(record.accountID, record.dedicatedAccountID, record.dedicatedAccountUnitType == 1 ? record.dedicatedAccountBalance
						: record.dedicatedAccountUnitBalance);
				return result;
			}

			@Override
			public void action(DedicatedAccountsFileV3_3 record, IConnection connection)
			{
				performUponDepletionTransfer(record.accountID, record.dedicatedAccountID, record.dedicatedAccountUnitType == 1 ? record.dedicatedAccountBalance : record.dedicatedAccountUnitBalance);
			}

			@Override
			public boolean isLowPriority(DedicatedAccountsFileV3_3 record)
			{
				return true;
			}

		};
		esb.addTrigger(daTrigger);

		// Create a Temporal Event Trigger
		Trigger<ITemporalTrigger> timeTrigger = new Trigger<ITemporalTrigger>(ITemporalTrigger.class)
		{
			@Override
			public boolean testCondition(ITemporalTrigger trigger)
			{
				return trigger.getServiceID().equalsIgnoreCase(getServiceID());
			}

			@Override
			public void action(ITemporalTrigger trigger, IConnection connection)
			{
				performPeriodicTransfer(trigger);
			}

			@Override
			public boolean isLowPriority(ITemporalTrigger trigger)
			{
				return true;
			}

		};
		esb.addTrigger(timeTrigger);

		// Log Information
		logger.info("Advanced Credit Transfer Service Started");

		return true;
	}

	@Override
	public void stop()
	{
		if (airSimulator != null)
			airSimulator.stop();
		
		// Log Information
		logger.info("Advanced Credit Transfer Service Stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		config.validate();
		sdpAccountMap = null;
		this.config = (AdvancedTransferConfig) config;
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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configurable Parameters
	//
	// /////////////////////////////////
	@Perms(perms = {
			@Perm(name = "ViewAdvancedTransferParameters", description = "View AdvancedTransfer Parameters", category = "AdvancedTransfer", supplier = true),
			@Perm(name = "ChangeAdvancedTransferParameters", implies = "ViewAdvancedTransferParameters", description = "Change AdvancedTransfer Parameters", category = "AdvancedTransfer", supplier = true),
			@Perm(name = "ViewAdvancedTransferNotifications", description = "View Advanced Credit Transfer Notifications", category = "AdvancedTransfer", supplier = true),
			@Perm(name = "ChangeAdvancedTransferNotifications", implies = "ViewAdvancedTransferNotifications", description = "Change Advanced Credit Transfer Notifications", category = "AdvancedTransfer", supplier = true) })
	public class AdvancedTransferConfig extends ConfigurationBase
	{
		final Logger logger = LoggerFactory.getLogger(AdvancedTransferConfig.class);
		
		protected String shortCode = "143";
		protected String smsSourceAddress = "143";

		protected Phrase serviceName = Phrase.en("Advanced Credit Transfer").fre("Credit Transfer automatique");

		// The Dedicated Account to use as Main Account for Post Paid Subscribers, or 0 if the MA is to be used for them as well
		// 1 - Use DA1 as "Main Account" for Post-Paid subscribers 0 - Use MA as "Main Account" for Post-Paid Subscribers
		protected int postPaidAccountID = 0;

		// Donor Parameters
		protected String donorTransactionType = "";
		protected String donorTransactionCode = "";
		protected boolean flagDonorPromotionNotification = false;
		protected boolean flagDonorFirstIVRCallSet = false;
		protected boolean flagDonorAccountActivation = false;

		// Recipient Parameters
		protected String recipientTransactionType = "";
		protected String recipientTransactionCode = "";
		protected boolean flagRecipientPromotionNotification = false;
		protected boolean flagRecipientFirstIVRCallSet = false;
		protected boolean flagRecipientAccountActivation = false;

		// NOTE: No {Tokens} allowed
		protected ReturnCodeTexts[] returnCodesTexts = new ReturnCodeTexts[] { //
				new ReturnCodeTexts(ReturnCodes.quantityTooSmall, Phrase.en("The Quantity is too small").fre("FR: The Quantity is too small")),
				new ReturnCodeTexts(ReturnCodes.quantityTooBig, Phrase.en("The Quantity is too big").fre("FR: The Quantity is too big")),
				new ReturnCodeTexts(ReturnCodes.unregisteredPin, Phrase.en("This Service Requires you to have a PIN. Please Contact Customer Care on 123").fre(
						"FR: This Service Requires you to have a PIN. Please Contact Customer Care on 123")) };

		protected IProcess ussdProcess = AdvancedTransferMenu.getMenuProcess(getServiceID());

		// Various aspects of each Variant of the Advanced Credit Transfer Service
		// will be configurable through the WUI or CLI as well:
		protected Variant[] variants = new Variant[] {

				// new Variant(nameLanguage1, nameLanguage2, nameLanguage3, nameLanguage4,
				// validityPeriod, validityPeriodUnit,
				// firstRenewalWarningHoursBefore, secondRenewalWarningHoursBefore,
				// subscriptionCharge, renewalCharge,
				// subscriptionOfferID, recipientOfferID)

				new Variant("Daily", Phrase.en("Daily").fre("Quotidien"), 1, TimeUnits.Days, 4, 0, 10, 10, true, new Integer[] { 100 }), //
				new Variant("Weekly", Phrase.en("Weekly").fre("Hebdomadaire"), 1, TimeUnits.Weeks, 12, 4, 70, 70, true, new Integer[] { 100 }), //
				new Variant("Monthly", Phrase.en("Monthly").fre("Mensuel"), 1, TimeUnits.Months, 12, 4, 300, 300, true, new Integer[] { 100 }), //
				new Variant("OneMonth", Phrase.en("One Month").fre("One Month"), 1, TimeUnits.Months, 12, 4, 300, 0, false, new Integer[] { 100 }), //
		};

		// Various aspects of each Transfer Type of the Advanced Credit Transfer Service
		// will be configurable through the WUI or CLI as well:
		protected TransferMode[] transferModes = new TransferMode[] {

		new TransferMode( //
				/* transferModeID */"MMD",
				/* transferType */CreditTransferType.Periodic,
				/* topUpOnly */true,
				/* requiresPIN */false,
				/* name */Phrase.en("Daily Credit Gift TopUp Donation"),
				/* unitLanguage */Phrase.en("USD"),
				/* donorAccountID */0,
				/* donorAccountType */1,
				/* donorMinBalance */5,
				/* donorMaxBalance */7000,
				/* recipientAccountID */0,
				/* recipientAccountType */1,
				/* recipientMinBalance */0,
				/* recipientMaxBalance */10000,
				/* recipientExpiryDays */null,
				/* interval */1,
				/* intervalType */TimeUnits.Days,
				/* donorUnitsDisplayConversion */6400,
				/* recipientUnitsDisplayConversion */6400,
				/* conversionRate */640000,
				/* minAmount */1,
				/* maxAmount */300,
				/* commissionAmount */10,
				/* commissionPercentage */100,
				/* thresholdID */0,
				/* validDonorServiceClasses */new Integer[] { 100 },
				/* validRecipientServiceClasses */new Integer[] { 100 },
				/* requiredSubscriptionVariants */new String[] { "Daily", "Weekly", "Monthly", "OneMonth" },
				/* blackListedPSOBits */"",
				/* maxAmountPerPeriod */null,
				/* maxCountPerPeriod */null),

		new TransferMode( //
				/* transferModeID */"MSD",
				/* transferType */CreditTransferType.Periodic,
				/* topUpOnly */false,
				/* requiresPIN */false,
				/* name */Phrase.en("Weekly SMS Gift Donation"),
				/* unitLanguage */Phrase.en("SMS"),
				/* donorAccountID */0,
				/* donorAccountType */1,
				/* donorMinBalance */3,
				/* donorMaxBalance */5000,
				/* recipientAccountID */1,
				/* recipientAccountType */5,
				/* recipientMinBalance */8,
				/* recipientMaxBalance */2000,
				/* recipientExpiryDays */30,
				/* interval */1,
				/* intervalType */TimeUnits.Weeks,
				/* donorUnitsDisplayConversion */640000,
				/* recipientUnitsDisplayConversion */640000,
				/* conversionRate */320000,
				/* minAmount */1,
				/* maxAmount */123,
				/* commissionAmount */20,
				/* commissionPercentage */0,
				/* thresholdID */0,
				/* validDonorServiceClasses */new Integer[] { 100 },
				/* validRecipientServiceClasses */new Integer[] { 100 },
				/* requiredSubscriptionVariants */new String[] { "OneMonth" },
				/* blackListedPSOBits */"",
				/* maxAmountPerPeriod */null,
				/* maxCountPerPeriod */null),

		new TransferMode( //
				/* transferModeID */"MDR",
				/* transferType */CreditTransferType.UponDepletion,
				/* topUpOnly */true,
				/* requiresPIN */false,
				/* name */Phrase.en("Monthly Data Gift TopUp Replenish"),
				/* unitLanguage */Phrase.en("MB"),
				/* donorAccountID */0,
				/* donorAccountType */1,
				/* donorMinBalance */4,
				/* donorMaxBalance */3500,
				/* recipientAccountID */2,
				/* recipientAccountType */6,
				/* recipientMinBalance */0,
				/* recipientMaxBalance */4400,
				/* recipientExpiryDays */30,
				/* interval */1,
				/* intervalType */TimeUnits.Months,
				/* donorUnitsDisplayConversion */64000,
				/* recipientUnitsDisplayConversion */64000,
				/* conversionRate */64000,
				/* minAmount */100,
				/* maxAmount */456456,
				/* commissionAmount */0,
				/* commissionPercentage */200,
				/* thresholdID */5,
				/* validDonorServiceClasses */new Integer[] { 100 },
				/* validRecipientServiceClasses */new Integer[] { 100 },
				/* requiredSubscriptionVariants */new String[] { "Weekly", "Monthly", "OneMonth" },
				/* blackListedPSOBits */"",
				/* maxAmountPerPeriod */null,
				/* maxCountPerPeriod */null),

		new TransferMode( //
				/* transferModeID */"MVR",
				/* transferType */CreditTransferType.UponDepletion,
				/* topUpOnly */false,
				/* requiresPIN */false,
				/* name */Phrase.en("Daily Voice Gift Replenish"),
				/* unitLanguage */Phrase.en("Mins"),
				/* donorAccountID */0,
				/* donorAccountType */1,
				/* donorMinBalance */5,
				/* donorMaxBalance */1000,
				/* recipientAccountID */3,
				/* recipientAccountType */0,
				/* recipientMinBalance */9,
				/* recipientMaxBalance */5680,
				/* recipientExpiryDays */7,
				/* interval */1,
				/* intervalType */TimeUnits.Days,
				/* donorUnitsDisplayConversion */640000,
				/* recipientUnitsDisplayConversion */640000,
				/* conversionRate */480000,
				/* minAmount */12,
				/* maxAmount */1258,
				/* commissionAmount */10,
				/* commissionPercentage */0,
				/* thresholdID */6,
				/* validDonorServiceClasses */new Integer[] { 100 },
				/* validRecipientServiceClasses */new Integer[] { 100 },
				/* requiredSubscriptionVariants */new String[] { "Monthly", "OneMonth" },
				/* blackListedPSOBits */"",
				/* maxAmountPerPeriod */null,
				/* maxCountPerPeriod */null),

		new TransferMode( //
				/* transferModeID */"SSD",
				/* transferType */CreditTransferType.Periodic,
				/* topUpOnly */true,
				/* requiresPIN */false,
				/* name */Phrase.en("Weekly SMS Transfer TopUp Donation"),
				/* unitLanguage */Phrase.en("SMS"),
				/* donorAccountID */1,
				/* donorAccountType */5,
				/* donorMinBalance */1,
				/* donorMaxBalance */1000,
				/* recipientAccountID */1,
				/* recipientAccountType */5,
				/* recipientMinBalance */12,
				/* recipientMaxBalance */9900,
				/* recipientExpiryDays */7,
				/* interval */1,
				/* intervalType */TimeUnits.Weeks,
				/* donorUnitsDisplayConversion */640000,
				/* recipientUnitsDisplayConversion */640000,
				/* conversionRate */640000,
				/* minAmount */16,
				/* maxAmount */123,
				/* commissionAmount */20,
				/* commissionPercentage */100,
				/* thresholdID */0,
				/* validDonorServiceClasses */new Integer[] { 100 },
				/* validRecipientServiceClasses */new Integer[] { 100 },
				/* requiredSubscriptionVariants */new String[] { "Daily", "Weekly", "Monthly", "OneMonth" },
				/* blackListedPSOBits */"",
				/* maxAmountPerPeriod */null,
				/* maxCountPerPeriod */null),

		new TransferMode( //
				/* transferModeID */"SSR",
				/* transferType */CreditTransferType.UponDepletion,
				/* topUpOnly */false,
				/* requiresPIN */false,
				/* name */Phrase.en("Monthly Data Transfer Replenish"),
				/* unitLanguage */Phrase.en("MB"),
				/* donorAccountID */2,
				/* donorAccountType */6,
				/* donorMinBalance */0,
				/* donorMaxBalance */2000,
				/* recipientAccountID */2,
				/* recipientAccountType */6,
				/* recipientMinBalance */15,
				/* recipientMaxBalance */7300,
				/* recipientExpiryDays */14,
				/* interval */1,
				/* intervalType */TimeUnits.Months,
				/* donorUnitsDisplayConversion */64000,
				/* recipientUnitsDisplayConversion */64000,
				/* conversionRate */640000,
				/* minAmount */123,
				/* maxAmount */56756,
				/* commissionAmount */0,
				/* commissionPercentage */0,
				/* thresholdID */5,
				/* validDonorServiceClasses */new Integer[] { 100 },
				/* validRecipientServiceClasses */new Integer[] { 100 },
				/* requiredSubscriptionVariants */new String[] { "Daily", "Weekly" },
				/* blackListedPSOBits */"",
				/* maxAmountPerPeriod */null,
				/* maxCountPerPeriod */null),

		new TransferMode( //
				/* transferModeID */"VVD",
				/* transferType */CreditTransferType.Periodic,
				/* topUpOnly */false,
				/* requiresPIN */true,
				/* name */Phrase.en("Daily Voice Transfer TopUp Donation"),
				/* unitLanguage */Phrase.en("Mins"),
				/* donorAccountID */3,
				/* donorAccountType */0,
				/* donorMinBalance */10,
				/* donorMaxBalance */2000,
				/* recipientAccountID */3,
				/* recipientAccountType */0,
				/* recipientMinBalance */0,
				/* recipientMaxBalance */6100,
				/* recipientExpiryDays */30,
				/* interval */1,
				/* intervalType */TimeUnits.Days,
				/* donorUnitsDisplayConversion */640000,
				/* recipientUnitsDisplayConversion */640000,
				/* conversionRate */640000,
				/* minAmount */100,
				/* maxAmount */123123,
				/* commissionAmount */30,
				/* commissionPercentage */100,
				/* thresholdID */0,
				/* validDonorServiceClasses */new Integer[] { 100 },
				/* validRecipientServiceClasses */new Integer[] { 100 },
				/* requiredSubscriptionVariants */new String[] {},
				/* blackListedPSOBits */"",
				/* maxAmountPerPeriod */null,
				/* maxCountPerPeriod */null),

		new TransferMode( //
				/* transferModeID */"OOT",
				/* transferType */CreditTransferType.OnceOff,
				/* topUpOnly */false,
				/* requiresPIN */false,
				/* name */Phrase.en("Direct Transfer"),
				/* unitLanguage */Phrase.en("USD"),
				/* donorAccountID */0,
				/* donorAccountType */1,
				/* donorMinBalance */5,
				/* donorMaxBalance */70000,
				/* recipientAccountID */0,
				/* recipientAccountType */1,
				/* recipientMinBalance */0,
				/* recipientMaxBalance */100000,
				/* recipientExpiryDays */null,
				/* interval */1,
				/* intervalType */TimeUnits.Days,
				/* donorUnitsDisplayConversion */640000,
				/* recipientUnitsDisplayConversion */640000,
				/* conversionRate */640000,
				/* minAmount */2,
				/* maxAmount */300,
				/* commissionAmount */10,
				/* commissionPercentage */100,
				/* thresholdID */0,
				/* validDonorServiceClasses */new Integer[] { 100 },
				/* validRecipientServiceClasses */new Integer[] { 100 },
				/* requiredSubscriptionVariants */new String[] {},
				/* blackListedPSOBits */"3",
				/* maxAmountPerPeriod */300L,
				/* maxCountPerPeriod */1),

		new TransferMode( //
				/* transferModeID */"NI",
				/* transferType */CreditTransferType.OnceOff,
				/* topUpOnly */false,
				/* requiresPIN */false,
				/* name */Phrase.en("ESA"),
				/* unitLanguage */Phrase.en("USD"),
				/* donorAccountID */0,
				/* donorAccountType */Subscriber.DATYPE_MONEY,
				/* donorMinBalance */0,
				/* donorMaxBalance */100000,
				/* recipientAccountID */1000,
				/* recipientAccountType */Subscriber.DATYPE_MONEY,
				/* recipientMinBalance */0,
				/* recipientMaxBalance */100000,
				/* recipientExpiryDays */30,
				/* interval */1,
				/* intervalType */TimeUnits.Days,
				/* donorUnitsDisplayConversion */640000,
				/* recipientUnitsDisplayConversion */640000,
				/* conversionRate */640000,
				/* minAmount */1,
				/* maxAmount */100000,
				/* commissionAmount */0,
				/* commissionPercentage */0,
				/* thresholdID */0,
				/* validDonorServiceClasses */new Integer[] { 100 },
				/* validRecipientServiceClasses */new Integer[] { 100 },
				/* requiredSubscriptionVariants */new String[0],
				/* blackListedPSOBits */"",
				/* maxAmountPerPeriod */null,
				/* maxCountPerPeriod */null),

		};

		// Various aspects of each Service Class as it relates to the Advanced Credit Transfer Service
		// will be configurable through the WUI or CLI as well:
		protected ServiceClass[] serviceClasses = new ServiceClass[] {

		// serviceClassID, englishName, frenchNname, postPaid, maxConsumers
		// addConsumerCharge, removeConsumerCharge, unsubscribeCharge,
		// removeTransferCharge, providerBalanceEnquiryCharge, consumerBalanceEnquiryCharge

		new ServiceClass(100, Phrase.en("Gold"), false, 10, 1, 2, 3, 4, 5, 6, 7), //

		};

		// Single Shot USSD and SMS Commands
		protected VasCommand[] commands = new VasCommand[] { //
		//
				new VasCommand(VasCommand.Processes.transfer, "*{RecipientMSISDN}*{Amount}*{Pin}#{TransferModeID=oot}"), //
				new VasCommand(VasCommand.Processes.transfer, "oot{TransferModeID=oot} {RecipientMSISDN} {Pin} {Amount}"), //
		};

		public Phrase getServiceName()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return serviceName;
		}

		public String getShortCode()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return shortCode;
		}

		public void setShortCode(String shortCode)
		{
			check(esb, "ChangeAdvancedTransferParameters");
			this.shortCode = shortCode;
		}

		public String getSmsSourceAddress()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return smsSourceAddress;
		}

		public void setSmsSourceAddress(String smsSourceAddress)
		{
			check(esb, "ChangeAdvancedTransferParameters");
			this.smsSourceAddress = smsSourceAddress;
		}

		public int getPostPaidAccountID()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return postPaidAccountID;
		}

		public void setPostPaidAccountID(int postPaidAccountID)
		{
			check(esb, "ChangeAdvancedTransferParameters");
			this.postPaidAccountID = postPaidAccountID;
		}

		public String getDonorTransactionType()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return donorTransactionType;
		}

		public void setDonorTransactionType(String donorTransactionType)
		{
			check(esb, "ChangeAdvancedTransferParameters");
			this.donorTransactionType = donorTransactionType;
		}

		public String getDonorTransactionCode()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return donorTransactionCode;
		}

		public void setDonorTransactionCode(String donorTransactionCode)
		{
			check(esb, "ChangeAdvancedTransferParameters");
			this.donorTransactionCode = donorTransactionCode;
		}

		public boolean isFlagDonorPromotionNotification()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return flagDonorPromotionNotification;
		}

		public void setFlagDonorPromotionNotification(boolean flagDonorPromotionNotification)
		{
			check(esb, "ChangeAdvancedTransferParameters");
			this.flagDonorPromotionNotification = flagDonorPromotionNotification;
		}

		public boolean isFlagDonorFirstIVRCallSet()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return flagDonorFirstIVRCallSet;
		}

		public void setFlagDonorFirstIVRCallSet(boolean flagDonorFirstIVRCallSet)
		{
			check(esb, "ChangeAdvancedTransferParameters");
			this.flagDonorFirstIVRCallSet = flagDonorFirstIVRCallSet;
		}

		public boolean isFlagDonorAccountActivation()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return flagDonorAccountActivation;
		}

		public void setFlagDonorAccountActivation(boolean flagDonorAccountActivation)
		{
			check(esb, "ChangeAdvancedTransferParameters");
			this.flagDonorAccountActivation = flagDonorAccountActivation;
		}

		public String getRecipientTransactionType()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return recipientTransactionType;
		}

		public void setRecipientTransactionType(String recipientTransactionType)
		{
			check(esb, "ChangeAdvancedTransferParameters");
			this.recipientTransactionType = recipientTransactionType;
		}

		public String getRecipientTransactionCode()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return recipientTransactionCode;
		}

		public void setRecipientTransactionCode(String recipientTransactionCode)
		{
			check(esb, "ChangeAdvancedTransferParameters");
			this.recipientTransactionCode = recipientTransactionCode;
		}

		public boolean isFlagRecipientPromotionNotification()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return flagRecipientPromotionNotification;
		}

		public void setFlagRecipientPromotionNotification(boolean flagRecipientPromotionNotification)
		{
			check(esb, "ChangeAdvancedTransferParameters");
			this.flagRecipientPromotionNotification = flagRecipientPromotionNotification;
		}

		public boolean isFlagRecipientFirstIVRCallSet()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return flagRecipientFirstIVRCallSet;
		}

		public void setFlagRecipientFirstIVRCallSet(boolean flagRecipientFirstIVRCallSet)
		{
			check(esb, "ChangeAdvancedTransferParameters");
			this.flagRecipientFirstIVRCallSet = flagRecipientFirstIVRCallSet;
		}

		public boolean isFlagRecipientAccountActivation()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return flagRecipientAccountActivation;
		}

		public void setFlagRecipientAccountActivation(boolean flagRecipientAccountActivation)
		{
			check(esb, "ChangeAdvancedTransferParameters");
			this.flagRecipientAccountActivation = flagRecipientAccountActivation;
		}

		public Variant[] getVariants()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return variants;
		}

		public void setVariants(Variant[] variants)
		{
			check(esb, "ChangeAdvancedTransferParameters");
			this.variants = variants;
		}

		public TransferMode[] getTransferModes()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return transferModes;
		}

		public void setTransferModes(TransferMode[] transferModes)
		{
			check(esb, "ChangeAdvancedTransferParameters");
			this.transferModes = transferModes;
		}

		public ServiceClass[] getServiceClasses()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return serviceClasses;
		}

		public void setServiceClasses(ServiceClass[] serviceClasses)
		{
			check(esb, "ChangeAdvancedTransferParameters");
			this.serviceClasses = serviceClasses;
		}

		public ReturnCodeTexts[] getReturnCodesTexts()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return returnCodesTexts;
		}

		public void setReturnCodesTexts(ReturnCodeTexts[] returnCodesTexts)
		{
			check(esb, "ChangeAdvancedTransferParameters");
			this.returnCodesTexts = returnCodesTexts;
		}

		public IProcess getUssdProcess()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return ussdProcess;
		}

		public void setUssdProcess(IProcess ussdProcess) throws ValidationException
		{
			check(esb, "ChangeAdvancedTransferParameters");

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

		public VasCommand[] getCommands()
		{
			check(esb, "ViewAdvancedTransferParameters");
			return commands;
		}

		public void setCommands(VasCommand[] commands)
		{
			check(esb, "ChangeAdvancedTransferParameters");
			this.commands = commands;
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
			return -1103158008836733663L;
		}

		@Override
		public String getName(String languageCode)
		{
			return serviceName.getSafe(languageCode, "Advanced Credit Transfer");
		}

		@Override
		public void validate() throws ValidationException
		{
			ValidationException.notEmpty(shortCode, "shortCode", "Short Code not Specified");
			ValidationException.isNumeric(shortCode, "shortCode", "Short Code must be Numeric");
			ValidationException.notEmpty(smsSourceAddress, "smsSourceAddress", "SMS Source Address not Specified");
			ValidationException.isNumeric(smsSourceAddress, "smsSourceAddress", "SMS Source Address must be Numeric");

			int[] serviceClassIDs = ServiceClass.getServiceClassIDs(serviceClasses);
			String[] variantIDs = Variant.getVariantIDs(variants);

			ValidationException.notNull(variants, "variants", "Variants cannot be NULL");

			for (int i = 0; i < variants.length; i++)
			{
				try
				{
					ValidationException.notNull(variants[i], "Variants cannot be NULL");
					variants[i].validate(variants, serviceClassIDs, locale);
				}
				catch (ValidationException ve)
				{
					throw ValidationException.createFieldValidationException(String.format("variants[%d].%s", i, (ve.getField() == null ? "" : ve.getField())), ve.getMessage());
				}
			}

			ValidationException.notNull(transferModes, "Transfer Modes cannot be NULL");
			for (int i = 0; i < transferModes.length; i++)
			{
				try
				{
					ValidationException.notNull(transferModes[i], "Transfer Modes cannot be NULL");
					transferModes[i].validate(transferModes, serviceClassIDs, variantIDs, locale);
				}
				catch (ValidationException ve)
				{
					throw ValidationException.createFieldValidationException(String.format("transferModes[%d].%s", i, (ve.getField() == null ? "" : ve.getField())), ve.getMessage());
				}
			}

			ValidationException.notNull(serviceClasses, "Service Classes cannot be NULL");
			for (int i = 0; i < serviceClasses.length; i++)
			{
				try
				{
					ValidationException.notNull(serviceClasses[i], "Service Classes cannot be NULL");
					serviceClasses[i].validate(serviceClasses, locale);
				}
				catch (ValidationException ve)
				{
					throw ValidationException.createFieldValidationException(String.format("serviceClasses[%d].%s", i, (ve.getField() == null ? "" : ve.getField())), ve.getMessage());
				}
			}

			if (commandParser != null)
				commandParser.validate(commands);

		}

		@Override
		public void performUpdateNotificationSecurityCheck()
		{
			check(esb, "ChangeAdvancedTransferNotifications");
		}

		@Override
		public void performGetNotificationSecurityCheck()
		{
			check(esb, "ViewAdvancedTransferNotifications");
		}

	}

	AdvancedTransferConfig config = new AdvancedTransferConfig();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Notifications
	//
	// /////////////////////////////////
	protected Notifications notifications = new Notifications(Properties.class);

	// Subscription
	protected int smsDonorSubscribed = notifications.add("Successful Subscription sent to Donor", //
			"You have been subscribed to the {Variant} Advanced Credit Transfer Service for {Charge} {Currency} until {ExpiryDate} {ExpiryTime}", //
			"FR: You have been subscribed to the {Variant} Advanced Credit Transfer Service for {Charge} {Currency} until {ExpiryDate} {ExpiryTime}", //
			null, null);

	// Unsubscription
	protected int smsDonorUnsubscribed = notifications.add("Successful Unsubscription sent to Donor", //
			"You have been unsubscribed from the {Variant} Advanced Credit Transfer Service for {Charge} {Currency}", //
			"FR: You have been unsubscribed from the {Variant} Advanced Credit Transfer Service for {Charge} {Currency}", //
			null, null);

	// Added Recipient
	protected int smsDonorAddedRecipient = notifications.add("Added Recipient sent to Donor", //
			"You have added {RecipientMSISDN} as a recipient to your {Variant} Advanced Credit Transfer Service for {Charge} {Currency}", //
			"FR: You have added {RecipientMSISDN} as a recipient to your {Variant} Advanced Credit Transfer Service for {Charge} {Currency}", //
			null, null);

	protected int smsRecipientAddedRecipient = notifications.add("Added as Recipient sent to Recipient", //
			"You have added as a recipient by {DonorMSISDN} for the {Variant} Advanced Credit Transfer Service", //
			"FR: You have added as a recipient by {DonorMSISDN} for the {Variant} Advanced Credit Transfer Service", //
			null, null);

	// Recipient Removal
	protected int smsDonorRemovedRecipient = notifications.add("Successful Recipient Removal sent to Donor", //
			"You have removed {RecipientMSISDN} from your {Variant} Advanced Credit Transfer Service for {Charge} {Currency}", //
			"FR: You have removed {RecipientMSISDN} from your {Variant} Advanced Credit Transfer Service for {Charge} {Currency}", //
			null, null);

	protected int smsRecipientRemovedRecipient = notifications.add("Successful Removal as Recipient sent to Recipient", //
			"You have been removed from {DonorMSISDN}'s {Variant} Advanced Credit Transfer Service", //
			"FR: You have been removed from {DonorMSISDN}'s {Variant} Advanced Credit Transfer Service", //
			null, null);

	// Transfer Addition
	protected int smsDonorAddedTransfer = notifications.add("Successful Transfer Addition sent to Donor", //
			"You have added an Advanced Transfer of {Quantity} {Units} {TransferMode} for {RecipientMSISDN} for {Charge} {Currency}", //
			"FR: You have added an Advanced Transfer of {Quantity} {Units} {TransferMode} for {RecipientMSISDN} for {Charge} {Currency}", //
			null, null);

	protected int smsRecipientAddedTransfer = notifications.add("Transfer Reception sent to Recipient", //
			"You will receive {TransferMode} Transfer of {Quantity} {Units} {TransferMode} from {DonorMSISDN}", //
			"FR: You will receive {TransferMode} Transfer of {Quantity} {Units} {TransferMode} from {DonorMSISDN}", //
			null, null);

	// Transfer Removal
	protected int smsDonorRemovedTransfer = notifications.add("Successful Transfer Removal sent to Donor", //
			"You have removed Advanced Transfer of {Quantity} {Units} {TransferMode} to {RecipientMSISDN} for {Charge} {Currency}", //
			"FR: You have removed Advanced Transfer of {Quantity} {Units} {TransferMode} to {RecipientMSISDN} for {Charge} {Currency}", //
			null, null);

	protected int smsRecipientRemovedTransfer = notifications.add("Lost Transfer sent to Recipient", //
			"{DonorMSISDN} removed your Advanced Transfer of {Quantity} {Units} {TransferMode}", //
			"FR: {DonorMSISDN} removed your Advanced Transfer of {Quantity} {Units} {TransferMode}", //
			null, null);

	// Transfer Suspension
	protected int smsDonorSuspendedTransfer = notifications.add("Successful Transfer Suspension sent to Donor", //
			"Your Advanced Transfer of {Quantity} {Units} {TransferMode} to {RecipientMSISDN} has been suspended for {Charge} {Currency}", //
			"FR: Your Advanced Transfer of {Quantity} {Units} {TransferMode} to {RecipientMSISDN} has been suspended for {Charge} {Currency}", //
			null, null);

	protected int smsRecipientSuspendedTransfer = notifications.add("Transfer Suspended sent to Recipient", //
			"{DonorMSISDN} suspended your Advanced Transfer of {Quantity} {Units} {TransferMode}", //
			"FR: {DonorMSISDN} suspended your Advanced Transfer of {Quantity} {Units} {TransferMode}", //
			null, null);

	// Transfer Resumption
	protected int smsDonorResumedTransfer = notifications.add("Successful Transfer Resumption sent to Donor", //
			"You have resumed Advanced Transfer of {Quantity} {Units} {TransferMode} to {RecipientMSISDN} for {Charge} {Currency}", //
			"FR: You have resumed Advanced Transfer of {Quantity} {Units} {TransferMode} to {RecipientMSISDN} for {Charge} {Currency}", //
			null, null);

	protected int smsRecipientResumedTransfer = notifications.add("Transfer Resumed sent to Recipient", //
			"{DonorMSISDN} resumed your Advanced Transfer of {Quantity} {Units} {TransferMode}", //
			"FR: {DonorMSISDN} resumed your Advanced Transfer of {Quantity} {Units} {TransferMode}", //
			null, null);

	// Transfer Change
	protected int smsDonorChangedTransfer = notifications.add("Successful Transfer Change sent to Donor", //
			"You have added Advanced Transfer of {Quantity} {Units} {TransferMode} to {RecipientMSISDN} for {Charge} {Currency}", //
			"FR: You have added Advanced Transfer of {Quantity} {Units} {TransferMode} to {RecipientMSISDN} for {Charge} {Currency}", //
			null, null);

	protected int smsRecipientChangedTransfer = notifications.add("Received Transfer Change sent to Recipient", //
			"You will now receive Advanced Transfer of {Quantity} {Units} {TransferMode} from {DonorMSISDN}", //
			"FR: You will now receive Advanced Transfer of {Quantity} {Units} {TransferMode} from {DonorMSISDN}", //
			null, null);

	// Successful Transfer
	protected int smsDonorTransferred = notifications.add("Successful Transfer sent to Donor", //
			"You have transferred {Quantity} {Units} to {RecipientMSISDN} for {Charge} {Currency}", //
			"FR: You have transferred {Quantity} {Units} to {RecipientMSISDN} for {Charge} {Currency}", //
			null, null);

	protected int smsRecipientReceived = notifications.add("Received Successfuly sent to Recipient", //
			"You have received {Quantity} {Units} from {DonorMSISDN}", //
			"FR: You have received {Quantity} {Units} from {DonorMSISDN}", //
			null, null);

	// Transfer Amount too small smsDonorTooSmall
	protected int smsDonorTooSmall = notifications.add("Transfer Amount too small", //
			"The amount you which to send to {RecipientMSISDN} is too small", //
			"FR: The amount you which to send to {RecipientMSISDN} is too small", null, null);

	// Donor Balance too low
	protected int smsDonorDonorTooPoor = notifications.add("Donor's Balance too low sent to Donor", //
			"Your Balance is too low to Transfer Credit to {RecipientMSISDN}", //
			"FR: Your Balance is too low to Transfer Credit to {RecipientMSISDN}", null, null);

	protected int smsRecipientDonorTooPoor = notifications.add("Donor's Balance too low sent to Recipient", //
			"{DonorMSISDN} can currently not Transfer Credit to you", //
			"FR: {DonorMSISDN} can currently not Transfer Credit to you", null, null);

	// Donor Balance too high
	protected int smsDonorDonorTooRich = notifications.add("Donor's Balance too high sent to Donor", //
			"Your Balance is too high to Transfer Credit to {RecipientMSISDN}", //
			"FR: Your Balance is too high to Transfer Credit to {RecipientMSISDN}", null, null);

	protected int smsRecipientDonorTooRich = notifications.add("Donor's Balance too low sent to Recipient", //
			"{DonorMSISDN} can currently not Transfer Credit to you", //
			"FR: {DonorMSISDN} can currently not Transfer Credit to you", null, null);

	// Recipient Balance too low
	protected int smsDonorRecipientTooPoor = notifications.add("Recipients's Balance too low sent to Donor", //
			"{RecipientMSISDN}'s Balance is too low for Credit Transfer", //
			"FR: {RecipientMSISDN}'s Balance is too low for Credit Transfer", null, null);

	protected int smsRecipientRecipientTooPoor = notifications.add("Recipients's Balance too low sent to Recipient", //
			"Your Balance is too low to receive Credit from {DonorMSISDN}", //
			"FR: Your Balance is too low to receive Credit from {DonorMSISDN}", null, null);

	// Recipient Balance too high
	protected int smsDonorRecipientTooRich = notifications.add("Recipients's Balance too high sent to Donor", //
			"{RecipientMSISDN}'s Balance is too high for Credit Transfer", //
			"FR: {RecipientMSISDN}'s Balance is too high for Credit Transfer", null, null);

	protected int smsRecipientRecipientTooRich = notifications.add("Recipients's Balance too high sent to Recipient", //
			"Your Balance is too high to receive Credit from {DonorMSISDN}", //
			"FR: Your Balance is too high to receive Credit from {DonorMSISDN}", null, null);

	// Not Required
	protected int smsDonorNotRequired = notifications.add("No Transfer Required sent to Donor", //
			"Credit Transfer to {RecipientMSISDN} not required - has sufficient Credit", //
			"FR: Credit Transfer to {RecipientMSISDN} not required - has sufficient Credit", null, null);

	protected int smsRecipientNotRequired = notifications.add("No Transfer Required sent to Recipient", //
			"Credit Transfer from {DonorMSISDN} not required - you have sufficient Credit", //
			"FR: Credit Transfer from {DonorMSISDN} not required - you have sufficient Credit", null, null);

	// Failed, Will retry
	protected int smsDonorRetry = notifications.add("Will retry low sent to Donor", //
			"Credit Transfer to {RecipientMSISDN} failed. It will be retried in {RetryInterval} minutes", //
			"FR: Credit Transfer to {RecipientMSISDN} failed. It will be retried in {RetryInterval} minutes", null, null);

	protected int smsRecipientRetry = notifications.add("Will Retry sent to Recipient", //
			"{DonorMSISDN}'s attempt to Transfer Credit to you was unsuccessful. Will be retried in {RetryInterval} minutes", //
			"FR: {DonorMSISDN}'s attempt to Transfer Credit to you was unsuccessful. Will be retried in {RetryInterval} minutes", null, null);

	// Renewal Warnings
	protected int smsDonorRenewalWarning = notifications.add("Renewal Warning sent to Donor", //
			"Your {Variant} subscription will be renewed in {HoursBeforeExpiry} hours. Please ensure that you have at least {Charge} {Currency} in your Account.", //
			"FR: Your {Variant} subscription will be renewed in {HoursBeforeExpiry} hours. Please ensure that you have at least {Charge} {Currency} in your Account.");

	// Renewal
	protected int smsDonorRenewed = notifications.add("Donor Renewed sent to Donor", //
			"Your {Variant} subscription has been renewed until {NewExpiryDate} hours for {Charge} {Currency}", //
			"FR: Your {Variant} subscription has been renewed until {NewExpiryDate} hours for {Charge} {Currency}");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	class Properties
	{
		protected IPhrase variant;
		protected IPhrase units;
		protected String charge = "0";
		protected String expiryDate;
		protected String expiryTime;
		protected String newExpiryDate;
		protected String newExpiryTime;
		protected String donorMSISDN;
		protected String recipientMSISDN;
		protected IPhrase transferMode;
		protected String transferID;
		protected String quantity;
		protected String hoursBeforeExpiry;
		protected String minQuantity;
		protected String maxQuantity;
		protected String retryInterval;

		public String getDonorMSISDN()
		{
			return donorMSISDN;
		}

		public void setDonorMSISDN(String donorMSISDN)
		{
			this.donorMSISDN = donorMSISDN;
		}

		public String getRecipientMSISDN()
		{
			return recipientMSISDN;
		}

		public void setRecipientMSISDN(String recipientMSISDN)
		{
			this.recipientMSISDN = recipientMSISDN;
		}

		public String getVariant(String languageCode)
		{
			return variant.get(languageCode);
		}

		public void setVariant(IPhrase variant)
		{
			this.variant = variant;
		}

		public String getUnits(String languageCode)
		{
			return units.get(languageCode);
		}

		public void setUnits(IPhrase units)
		{
			this.units = units;
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

		public String getExpiryTime()
		{
			return expiryTime;
		}

		public void setExpiryTime(String expiryTime)
		{
			this.expiryTime = expiryTime;
		}

		public String getNewExpiryDate()
		{
			return newExpiryDate;
		}

		public void setNewExpiryDate(String newExpiryDate)
		{
			this.newExpiryDate = newExpiryDate;
		}

		public String getNewExpiryTime()
		{
			return newExpiryTime;
		}

		public void setNewExpiryTime(String newExpiryTime)
		{
			this.newExpiryTime = newExpiryTime;
		}

		public String getTransferMode(String languageCode)
		{
			return transferMode.get(languageCode);
		}

		public void setTransferMode(IPhrase transferMode)
		{
			this.transferMode = transferMode;
		}

		public String getTransferID()
		{
			return transferID;
		}

		public void setTransferID(String transferID)
		{
			this.transferID = transferID;
		}

		public String getQuantity()
		{
			return quantity;
		}

		public void setQuantity(String quantity)
		{
			this.quantity = quantity;
		}

		public String getHoursBeforeExpiry()
		{
			return hoursBeforeExpiry;
		}

		public void setHoursBeforeExpiry(String hoursBeforeExpiry)
		{
			this.hoursBeforeExpiry = hoursBeforeExpiry;
		}

		public String getMinQuantity()
		{
			return minQuantity;
		}

		public void setMinQuantity(String minQuantity)
		{
			this.minQuantity = minQuantity;
		}

		public String getMaxQuantity()
		{
			return maxQuantity;
		}

		public void setMaxQuantity(String maxQuantity)
		{
			this.maxQuantity = maxQuantity;
		}

		public String getRetryInterval()
		{
			return retryInterval;
		}

		public void setRetryInterval(String retryInterval)
		{
			this.retryInterval = retryInterval;
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
			state.setSubscriberProxy(new Subscriber(state.getSubscriberNumber().toMSISDN(), air, null));
			state.setNotifications(notifications);
		}

		// Execute Process
		state.setConnection(connection);
		state.execute();
	}

	protected abstract void performPeriodicTransfer(ITemporalTrigger trigger);

	protected abstract boolean canPerformUponDepletionTransfer(ThresholdNotificationFileV2 message);

	protected abstract boolean canPerformUponDepletionTransfer(ThresholdNotificationFileV3 message);

	protected abstract boolean canPerformUponDepletionTransfer(String msisdn, int accountID, double balance);

	protected abstract void performUponDepletionTransfer(ThresholdNotificationFileV2 message);

	protected abstract void performUponDepletionTransfer(ThresholdNotificationFileV3 message);

	protected abstract void performUponDepletionTransfer(String msisdn, int accountID, double balance);

	private void connectAirSim(HuxProcessState state, HuxConnection connection)
	{
		airSimulator = new AirSim(esb, 10010, "/Air", numberPlan, "USD");
		airSimulator.start();
		hxc.services.airsim.protocol.Subscriber subscriberA = airSimulator.addSubscriber("0824452655", 1, 100, 14000, SubscriberState.active);
		airSimulator.addSubscriber("0824452656", 1, 100, 14000, SubscriberState.active);
		airSimulator.addSubscriber("0824452657", 1, 100, 14000, SubscriberState.active);
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

			ITemporalTrigger[] triggers = lifecycle.getTemporalTriggers(db, getServiceID(), null, subscriberA, null, null);
			for (ITemporalTrigger trigger : triggers)
			{
				lifecycle.removeTemporalTrigger(db, trigger);
			}

		}
		catch (Exception e)
		{
			logger.error("connectAirSim failed", e);
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

		// Add Member
		{
			// AddMemberRequest request = new AddMemberRequest();
			// initialize(subscriberA.getInternationalNumber(), request);
			// request.setServiceID(getServiceID());
			// request.setVariantID("Weekly");
			// request.setSubscriberNumber(new Number(subscriberA.getInternationalNumber()));
			// request.setMemberNumber(new Number(subscriberB.getInternationalNumber()));
			// AddMemberResponse response = addMember(context, request);
		}

		// Add Transfer
		{
			// AddCreditTransferRequest request = new AddCreditTransferRequest();
			// initialize(subscriberA.getInternationalNumber(), request);
			// request.setServiceID(getServiceID());
			// request.setTransferMode("AB");
			// request.setSubscriberNumber(new Number(subscriberA.getInternationalNumber()));
			// request.setMemberNumber(new Number(subscriberB.getInternationalNumber()));
			// request.setAmount(100);
			// AddCreditTransferResponse response = addCreditTransfer(context, request);

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

	@Override
	protected ReturnCodeTexts[] getReturnCodeTexts()
	{
		return config.getReturnCodesTexts();
	}

}
