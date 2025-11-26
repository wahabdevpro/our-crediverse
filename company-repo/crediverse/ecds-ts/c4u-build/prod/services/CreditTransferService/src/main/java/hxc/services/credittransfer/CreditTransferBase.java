package hxc.services.credittransfer;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.Config;
import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IInteraction;
import hxc.connectors.air.IAirConnector;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.hux.HuxConnection;
import hxc.connectors.hux.HuxProcessState;
import hxc.connectors.lifecycle.ILifecycle;
import hxc.connectors.lifecycle.ITemporalTrigger;
import hxc.connectors.sms.ISmsConnector;
import hxc.connectors.soap.ISoapConnector;
import hxc.connectors.vas.VasCommand;
import hxc.connectors.vas.VasService;
import hxc.servicebus.ILocale;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ReturnCodes;
import hxc.servicebus.Trigger;
import hxc.services.IService;
import hxc.services.airsim.AirSim;
import hxc.services.airsim.protocol.IAirSim;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.services.notification.INotifications;
import hxc.services.notification.Phrase;
import hxc.services.notification.ReturnCodeTexts;
import hxc.services.notification.Texts;
import hxc.services.numberplan.INumberPlan;
import hxc.services.pin.IPinService;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.transactions.ITransactionService;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.notification.Notifications;

public abstract class CreditTransferBase extends VasService implements IService
{
	final static Logger logger = LoggerFactory.getLogger(CreditTransferBase.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	protected IServiceBus esb;
	protected IAirConnector air;
	protected ILocale locale;
	protected ISmsConnector smsConnector;
	protected IDatabase database;
	protected ILifecycle lifecycle;
	protected INumberPlan numberPlan;
	protected IPinService pinService;
	protected ITransactionService transactions;
	protected ISoapConnector soapConnector;
	protected IAirSim airSimulator = null;

	public static final long chargeScalingFactor = 10000;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IService Implementation
	//
	// /////////////////////////////////

	public CreditTransferConfig getConfig()
	{
		return config;
	}

	public void setConfig(CreditTransferConfig config)
	{
		this.config = config;
	}

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

		// Must have PinService
		pinService = esb.getFirstService(IPinService.class);
		if (pinService == null)
			return false;

		// Get Locale
		this.locale = esb.getLocale();

		// Create a USSD Trigger
		/*
		 * Trigger<HuxProcessState> menuTrigger = new Trigger<HuxProcessState>(HuxProcessState.class) {
		 *
		 * @Override public boolean testCondition(HuxProcessState state) { return state.getServiceCode().equals(config.shortCode); }
		 *
		 * @Override public void action(HuxProcessState state, IConnection connection) { processUssd(state, (HuxConnection) connection); } }; esb.addTrigger(menuTrigger);
		 */

		// Create an SMS/USSD Trigger
		Trigger<IInteraction> ussdTrigger = new Trigger<IInteraction>(IInteraction.class)
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
		esb.addTrigger(ussdTrigger);

		// Create a Simulator Trigger
		Trigger<HuxProcessState> simTrigger = new Trigger<HuxProcessState>(HuxProcessState.class)
		{
			@Override
			public boolean testCondition(HuxProcessState state)
			{
				return state.getServiceCode().equals("99B");
			}

			@Override
			public void action(HuxProcessState state, IConnection connection)
			{
				connectAirSim(state, (HuxConnection) connection);
			}
		};
		esb.addTrigger(simTrigger);

		Trigger<ITemporalTrigger> counterTriggerHandler = new Trigger<ITemporalTrigger>(ITemporalTrigger.class)
		{
			@Override
			public boolean testCondition(ITemporalTrigger trigger)
			{
				return trigger.getServiceID().equals(getServiceID());
			}

			@Override
			public void action(ITemporalTrigger trigger, IConnection connection)
			{
				try (IDatabaseConnection dbCon = database.getConnection(null))
				{
					final long millisPerDay = 86400000;
					final long millisPerWeek = 7 * millisPerDay;
					final long millisPerMonth = 30 * millisPerDay;

					String msisdnA = trigger.getMsisdnA();
					String msisdnB = trigger.getMsisdnB();
					String serviceID = trigger.getServiceID();
					// Get usage record from db
					UsageCounter counterA = dbCon.select(UsageCounter.class, "where msisdn = %s and serviceId = %s", msisdnA, serviceID);
					UsageCounter counterB = dbCon.select(UsageCounter.class, "where msisdn = %s and serviceId = %s", msisdnB, serviceID);

					Date currentDate = new Date();
					Date weekBaseDate; // tracks weekly resets
					Date monthBaseDate; // tracks monthly resets
					long timeDiffWeek = 0L;
					long timeDiffMonth = 0L;

					if (counterA != null)
					{
						// Clear daily counters
						counterA.setDailyCounter(0);
						counterA.setDailySentAccumulator(0);
						counterA.setDailyReceivedAccumulator(0);

						// WEEKLY resets due??
						weekBaseDate = counterA.getWeekBaseDate();
						timeDiffWeek = currentDate.getTime() - weekBaseDate.getTime();

						if (timeDiffWeek / millisPerWeek >= 1)
						{
							counterA.setWeeklyCounter(0);
							counterA.setWeeklySentAccumulator(0);
							counterA.setWeeklyReceivedAccumulator(0);
							counterA.setWeekBaseDate(new Date()); // advance week start date to NOW
						}

						// --------------------------------------------------------
						// MONTHLY resets due??
						monthBaseDate = counterA.getMonthBaseDate();
						timeDiffMonth = currentDate.getTime() - monthBaseDate.getTime();
						if (timeDiffMonth / millisPerMonth >= 1)
						{
							counterA.setMonthlyCounter(0);
							counterA.setMonthlySentAccumulator(0);
							counterA.setMonthlyReceivedAccumulator(0);
							counterA.setMonthBaseDate(new Date()); // advance month start date to NOW
						}

						dbCon.update(counterA);
					}

					// ---------------------------------------------------------------------------

					if (counterB != null)
					{
						// Clear daily counters
						counterB.setDailyCounter(0);
						counterB.setDailySentAccumulator(0);
						counterB.setDailyReceivedAccumulator(0);

						// WEEKLY resets due??
						weekBaseDate = counterB.getWeekBaseDate();
						timeDiffWeek = currentDate.getTime() - weekBaseDate.getTime();
						if (timeDiffWeek / millisPerWeek >= 1)
						{
							counterB.setWeeklyCounter(0);
							counterB.setWeeklySentAccumulator(0);
							counterB.setWeeklyReceivedAccumulator(0);
							counterB.setWeekBaseDate(new Date()); // advance week start date to NOW
						}

						// MONTHLY resets due??
						monthBaseDate = counterB.getMonthBaseDate();
						timeDiffMonth = currentDate.getTime() - monthBaseDate.getTime();
						if (timeDiffMonth / millisPerMonth >= 1)
						{
							counterB.setMonthlyCounter(0);
							counterB.setMonthlySentAccumulator(0);
							counterB.setMonthlyReceivedAccumulator(0);
							counterB.setMonthBaseDate(new Date()); // advance month start date to NOW
						}

						dbCon.update(counterB);
					}

					// Now reschedule trigger to fire again a day later
					Quota dailyQuota = config.getDonorQuota(getVariant(trigger.getVariantID()).getDonorQuotas(), QuotaPeriodUnits.DAY);
					int numDays = dailyQuota.getNumberOfLimitPeriods();

					// Future time to fire trigger again
					currentDate.setTime(currentDate.getTime() + millisPerDay * numDays);
					trigger.setNextDateTime(currentDate);

					trigger.setBeingProcessed(false);
					lifecycle.updateTemporalTrigger(dbCon, trigger);
				}
				catch (Exception e)
				{
					logger.error("Failed to start", e);
				}
			}
		};
		esb.addTrigger(counterTriggerHandler);

		// Log Information
		logger.info("Credit Transfer Service Started");

		return true;
	}// start()

	@Override
	public void stop()
	{
		if (airSimulator != null)
			airSimulator.stop();
		
		// Log Information
		logger.info("Credit Transfer Service Stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		this.config = (CreditTransferConfig) config;
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

	public static long getChargeScalingfactor()
	{
		return chargeScalingFactor;
	}

	public enum UssdVars
	{
		UssdOption, MsisdnB, Amount, Pin, Anything
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configurable Parameters
	//
	// /////////////////////////////////

	@Perms(perms = {
			@Perm(name = "ViewCreditTransferParameters", description = "View CreditTransfer Parameters", category = "CreditTransfer", supplier = true),
			@Perm(name = "ChangeCreditTransferParameters", implies = "ViewCreditTransferParameters", description = "Change CreditTransfer Parameters", category = "CreditTransfer", supplier = true),
			@Perm(name = "ViewCreditTransferNotifications", description = "View Credit Transfer Notifications", category = "CreditTransfer", supplier = true),
			@Perm(name = "ChangeCreditTransferNotifications", implies = "ViewCreditTransferNotifications", description = "Change Credit Transfer Notifications", category = "CreditTransfer", supplier = true) })
	public class CreditTransferConfig extends ConfigurationBase
	{
		protected String shortCode = "153";

		protected String smsSourceAddress = "153";

		// @formatter:off
		protected UsageCounter usageCounter = new UsageCounter("",getServiceID(), 0, 0, 0);

		protected int allowedDonorServiceClassIds[] = { 1, 2, 3, 4, 76, 5, 6 };
		protected int allowedRecipientServiceClassIds[] = { 1, 2, 3, 4, 76, 5, 6 };

		protected TransactionCharge transactionChargeBands[] =
        {
            new TransactionCharge( 0*chargeScalingFactor, 100*chargeScalingFactor, 10*chargeScalingFactor, 0, getAllowedDonorServiceClassIds() ),
            new TransactionCharge( 100*chargeScalingFactor, 1000*chargeScalingFactor, 10*chargeScalingFactor, 1500, getAllowedDonorServiceClassIds() ),
            new TransactionCharge( 1000*chargeScalingFactor, 10000*chargeScalingFactor, 12*chargeScalingFactor, 800, getAllowedDonorServiceClassIds() ),
            new TransactionCharge( 10000*chargeScalingFactor, 9999999*chargeScalingFactor, 14*chargeScalingFactor, 500, getAllowedDonorServiceClassIds())
        };

		/*
		+------+-------+-----------+------+-----+---+
		|   USSD IDs   |         RECIPIENT          |
		+------+-------+-------+------+-----+-------+
		|  D   | *     | MAIN  | DATA | SMS | VOICE |
		|  O   | MAIN  | 0     | 1    | 2   | 3     |
		|  N   | DATA  | 4     | 5    | 6   | 7     |
		|  O   | SMS   | 8     | 9    | 10  | 11    |
		|  R   | VOICE | 12    | 13   | 14  | 15    |
		+------+-------+-------+------+-----+-------+
		*/

		Quota[] donorQuotas = new Quota[]
		{ 	new Quota("Daily",10000,1,QuotaPeriodUnits.DAY),
			new Quota("Weekly",10000,1,QuotaPeriodUnits.WEEK),
			new Quota("Monthly",10000,1,QuotaPeriodUnits.MONTH),
		};

		String[] donorQuotaList = {"Daily", "Weekly", "Monthly"};

		//Quota[] recipientQuotas = donorQuotas;

		// @formatter:off
		protected CreditTransferVariant variants[] =
		{
				new CreditTransferVariant("mainToMain", // variantID
                        0, // ussdID
                        new Texts("Main to Main","Main to Main","Main aux Main"), // name
                        0, // donorAccountID
                        DedicatedAccountUnitType.MONEY, // donorAccountType
                        500000, // donorMinBalance
                        1000000000, // donorMaxBalance
                        0, // recipientAccountID
                        DedicatedAccountUnitType.MONEY, // recipientAccountType
                        0, // recipientMinBalance
                        100000000, // recipientMaxBalance
                        Integer.valueOf(5), // recipientExpiryDays
                        new Texts("USD"), // donorUnits
                        new Texts("USD"), // recipientUnits
                        10000, // unitCostOfDonor
                        10000, // cost of 1 benefit (1*10^4==10000)
                        1000000, // minAmount
                        10000000, // maxAmount
                        allowedDonorServiceClassIds, // config.getDonorServiceClassIDs(), //validDonorServiceClasses
                        allowedRecipientServiceClassIds,
                        donorQuotaList, // donor quota
                        //defaultRecipientQuota, //recipient quota
                        transactionChargeBands,
                        new CumulativeLimits(1000,10000,100000),
                        new CumulativeLimits(1000,10000,100000)),

				new CreditTransferVariant("mainToData", // variantID
						1, // ussdID
						new Texts("Main to Data","Main to Data","Main aux Donnees"), // name
						0, // donorAccountID
						DedicatedAccountUnitType.MONEY, // donorAccountType
						500000, // donorMinBalance
						1000000000, // donorMaxBalance
						7, // recipientAccountID
						DedicatedAccountUnitType.VOLUME, // recipientAccountType
						0, // recipientMinBalance
						200000000, // recipientMaxBalance
						Integer.valueOf(5), // recipientExpiryDays
						new Texts("USD"), // donorUnits
						new Texts("MB"), // recipientUnits
						10000, // unitCostOfDonor
						4500, // unitCostPerBenefit (0.50*10^4==5000 per MB)
						1000000, // minAmount
						100000000, // maxAmount
						this.allowedDonorServiceClassIds, // validDonorServiceClasses
						this.allowedRecipientServiceClassIds, // validRecipientServiceClasses
						donorQuotaList, // donor quota
                        //defaultRecipientQuota, //recipient quota
                        transactionChargeBands,
                        new CumulativeLimits(1000,10000,100000),
                        new CumulativeLimits(1000,10000,100000)),

				new CreditTransferVariant("mainToSMS", // variantID
						2, // ussdID
						new Texts("Main to SMS","Main to SMS","Main aux SMS"), // name
						0, // donorAccountID
						DedicatedAccountUnitType.MONEY, // donorAccountType (1==money)
						500000, // donorMinBalance
						1000000000, // donorMaxBalance
						5, // recipientAccountID
						DedicatedAccountUnitType.VOLUME, // recipientAccountType (6==volume)
						0, // recipientMinBalance
						10000000, // recipientMaxBalance
						Integer.valueOf(5), // recipientExpiryDays
						new Texts("USD"), // donorUnits
						new Texts("SMS"), // recipientUnits
						10000, // unitCostOfDonor
						7500, // unitCostPerBenefit (0.65*10^4==6500 per SMS)
						1000000, // minAmount transferable
						50000000, // maxAmount transferable
						this.allowedDonorServiceClassIds, // validDonorServiceClasses
						this.allowedRecipientServiceClassIds, // validRecipientServiceClasses
						donorQuotaList, // donor quota
                        //defaultRecipientQuota, //recipient quota
                        transactionChargeBands,
                        new CumulativeLimits(1000,10000,100000),
                        new CumulativeLimits(1000,10000,100000)),

				new CreditTransferVariant("mainToVoice", // variantID
						3, // ussdID
						new Texts("Main to Voice","Main to Voice","Main aux Voix"), // name
						0, // donorAccountID
						DedicatedAccountUnitType.MONEY, // donorAccountType (1==money)
						500000, // donorMinBalance
						1000000, // donorMaxBalance
						17, // recipientAccountID
						DedicatedAccountUnitType.TIME, // recipientAccountType (0==time)
						0, // recipientMinBalance
						100000000, // recipientMaxBalance
						Integer.valueOf(5), // recipientExpiryDays
						new Texts("USD"), // donorUnits
						new Texts("min"), // recipientUnits
						10000, // unitCostOfDonor
						7500, // unitCostPerBenefit (0.65*10^4==6500 per SMS)
						100000, // minAmount transferable
						50000000, // maxAmount transferable
						this.allowedDonorServiceClassIds, // validDonorServiceClasses
						this.allowedRecipientServiceClassIds, // validRecipientServiceClasses
						donorQuotaList, // donor quota
                        //defaultRecipientQuota, //recipient quota
                        transactionChargeBands,
                        new CumulativeLimits(1000,10000,100000),
                        new CumulativeLimits(1000,10000,100000)),

                new CreditTransferVariant("dataToMain", // variantID
                        4, // ussdID
                        new Texts("Data to Main","Data to Main","Donnees aux Main"), // name
                        7, // donorAccountID
                        DedicatedAccountUnitType.VOLUME, // donorAccountType (6==volume)
                        500000, // donorMinBalance
                        100000000, // donorMaxBalance
                        0, // recipientAccountID
                        DedicatedAccountUnitType.MONEY, // recipientAccountType (1==money)
                        0, // recipientMinBalance
                        100000000, // recipientMaxBalance
                        Integer.valueOf(5), // recipientExpiryDays
                        new Texts("MB"), // donorUnits
                        new Texts("USD"), // recipientUnits
                        4500, // unitCostOfDonor
                        10000, // unitCostOfBenefit
                        100000, // minAmount transferable
                        50000000, // maxAmount transferable
                        this.allowedDonorServiceClassIds, // validDonorServiceClasses
                        this.allowedRecipientServiceClassIds, // validRecipientServiceClasses
                        donorQuotaList, // donor quota
                        //defaultRecipientQuota, //recipient quota
                        transactionChargeBands,
                        new CumulativeLimits(1000,10000,100000),
                        new CumulativeLimits(1000,10000,100000)),

                new CreditTransferVariant("dataToData", // variantID
                        5, // ussdID
                        new Texts("Data to Data","Data to Data","Donnees aux Donnees"), // name
                        7, // donorAccountID
                        DedicatedAccountUnitType.VOLUME, // donorAccountType (6==volume)
                        500000, // donorMinBalance
                        100000000, // donorMaxBalance
                        7, // recipientAccountID
                        DedicatedAccountUnitType.VOLUME, // recipientAccountType (6==volume)
                        0, // recipientMinBalance
                        100000000, // recipientMaxBalance
                        Integer.valueOf(5), // recipientExpiryDays
                        new Texts("MB"), // donorUnits
                        new Texts("MB"), // recipientUnits
                        4500, // unitCostOfDonor
                        4500, // unitCostOfBenefit
                        100000, // minAmount transferable
                        50000000, // maxAmount transferable
                        allowedDonorServiceClassIds, // validDonorServiceClasses
                        allowedRecipientServiceClassIds, // validRecipientServiceClasses
                        donorQuotaList, // donor quota
                        //defaultRecipientQuota, //recipient quota
                        transactionChargeBands,
                        new CumulativeLimits(1000,10000,100000),
                        new CumulativeLimits(1000,10000,100000)),

                new CreditTransferVariant("dataToSms", // variantID
                        6, // ussdID
                        new Texts("Data to SMS","Data to SMS","Donnees aux SMS"), // name
                        7, // donorAccountID
                        DedicatedAccountUnitType.VOLUME, // donorAccountType (6==volume)
                        500000, // donorMinBalance
                        100000000, // donorMaxBalance
                        5, // recipientAccountID
                        DedicatedAccountUnitType.VOLUME, // recipientAccountType (6==volume)
                        0, // recipientMinBalance
                        10000000, // recipientMaxBalance
                        Integer.valueOf(5), // recipientExpiryDays
                        new Texts("MB"), // donorUnits
                        new Texts("SMS"), // recipientUnits
                        4500, // unitCostOfDonor
                        7500, // unitCostOfBenefit
                        100000, // minAmount transferable
                        50000000, // maxAmount transferable
                        this.allowedDonorServiceClassIds, // validDonorServiceClasses
                        this.allowedRecipientServiceClassIds, // validRecipientServiceClasses
                        donorQuotaList, // donor quota
                        //defaultRecipientQuota, //recipient quota
                        transactionChargeBands,
                        new CumulativeLimits(1000,10000,100000),
                        new CumulativeLimits(1000,10000,100000)),

                new CreditTransferVariant("dataToVoice", // variantID
                        7, // ussdID
                        new Texts("Data to Voice", "Data to Voice","Donnes aux Voix"), // name
                        7, // donorAccountID
                        DedicatedAccountUnitType.VOLUME, // donorAccountType (6==volume)
                        500000, // donorMinBalance
                        100000000, // donorMaxBalance
                        17, // recipientAccountID
                        DedicatedAccountUnitType.TIME, // recipientAccountType
                        0, // recipientMinBalance
                        10000000, // recipientMaxBalance
                        Integer.valueOf(5), // recipientExpiryDays
                        new Texts("MB"), // donorUnits
                        new Texts("min"), // recipientUnits
                        4500, // unitCostOfDonor
                        7500, // unitCostOfBenefit
                        100000, // minAmount transferable
                        50000000, // maxAmount transferable
                        allowedDonorServiceClassIds, // validDonorServiceClasses
                        allowedRecipientServiceClassIds, // validRecipientServiceClasses
                        donorQuotaList, // donor quota
                        //defaultRecipientQuota, //recipient quota
                        transactionChargeBands,
                        new CumulativeLimits(1000,10000,100000),
                        new CumulativeLimits(1000,10000,100000)),

				new CreditTransferVariant("SmsToMain", // variantID
						8, // ussdID
						new Texts("SMS to Main","SMS to Main","SMS aux Main"), // name
						5, // donorAccountID
						DedicatedAccountUnitType.VOLUME, // donorAccountType (6==volume)
						50000, // donorMinBalance
						10000000, // donorMaxBalance
						0, // recipientAccountID
						DedicatedAccountUnitType.MONEY, // recipientAccountType (1==money)
						0, // recipientMinBalance
						100000000, // recipientMaxBalance
						Integer.valueOf(5), // recipientExpiryDays
						new Texts("SMS"), // donorUnits
						new Texts("USD"), // recipientUnits
						7500, // unitCostOfDonor
						10000, // unitCostOfBenefit
						100000, // minAmount transferable
						50000000, // maxAmount transferable
						this.allowedDonorServiceClassIds, // validDonorServiceClasses
						this.allowedRecipientServiceClassIds, // validRecipientServiceClasses
						donorQuotaList, // donor quota
                        //defaultRecipientQuota, //recipient quota
                        transactionChargeBands,
                        new CumulativeLimits(1000,10000,100000),
                        new CumulativeLimits(1000,10000,100000)),

				new CreditTransferVariant("SmsToData", // variantID
						9, // ussdID
						new Texts("SMS to Data","SMS to Data","SMS aux donnees"), // name
						5, // donorAccountID
						DedicatedAccountUnitType.VOLUME, // donorAccountType (6==volume)
						50000, // donorMinBalance
						10000000, // donorMaxBalance
						7, // recipientAccountID
						DedicatedAccountUnitType.VOLUME, // recipientAccountType (6==volume)
						0, // recipientMinBalance
						100000000, // recipientMaxBalance
						Integer.valueOf(5), // recipientExpiryDays
						new Texts("SMS"), // donorUnits
						new Texts("MB"), // recipientUnits
						7500, // unitCostOfDonor
						4500, // unitCostOfBenefit
						100000, // minAmount transferable
						50000000, // maxAmount transferable
						this.allowedDonorServiceClassIds, // validDonorServiceClasses
						this.allowedRecipientServiceClassIds, // validRecipientServiceClasses
						donorQuotaList, // donor quota
                        //defaultRecipientQuota, //recipient quota
                        transactionChargeBands,
                        new CumulativeLimits(1000,10000,100000),
                        new CumulativeLimits(1000,10000,100000)),

				new CreditTransferVariant("SmsToSms", // variantID
						10, // ussdID
						new Texts("SMS to SMS", "SMS to SMS", "SMS aux SMS"), // name
						5, // donorAccountID
						DedicatedAccountUnitType.VOLUME, // donorAccountType (6==volume)
						50000, // donorMinBalance
						10000000, // donorMaxBalance
						5, // recipientAccountID
						DedicatedAccountUnitType.VOLUME, // recipientAccountType (6==volume)
						0, // recipientMinBalance
						10000000, // recipientMaxBalance
						Integer.valueOf(5), // recipientExpiryDays
						new Texts("SMS"), // donorUnits
						new Texts("SMS"), // recipientUnits
						7500, // unitCostOfDonor
						7500, // unitCostOfBenefit
						100000, // minAmount transferable
						50000000, // maxAmount transferable
						this.allowedDonorServiceClassIds, // validDonorServiceClasses
						this.allowedRecipientServiceClassIds, // validRecipientServiceClasses
						donorQuotaList, // donor quota
                        //defaultRecipientQuota, //recipient quota
                        transactionChargeBands,
                        new CumulativeLimits(1000,10000,100000),
                        new CumulativeLimits(1000,10000,100000)),

				new CreditTransferVariant("SmsToVoice", // variantID
						11, // ussdID
						new Texts("Sms to Voice","SMS to Voice","SMS a la Voix"), // name
						5, // donorAccountID
						DedicatedAccountUnitType.VOLUME, // donorAccountType (6==volume)
						50000, // donorMinBalance
						10000000, // donorMaxBalance
						17, // recipientAccountID
						DedicatedAccountUnitType.TIME, // recipientAccountType (0==time)
						0, // recipientMinBalance
						10000000, // recipientMaxBalance
						Integer.valueOf(5), // recipientExpiryDays
						new Texts("SMS"), // donorUnits
						new Texts("min"), // recipientUnits
						7500, // unitCostOfDonor
						7500, // unitCostOfBenefit
						100000, // minAmount transferable
						50000000, // maxAmount transferable
						this.allowedDonorServiceClassIds, // validDonorServiceClasses
						this.allowedRecipientServiceClassIds, // validRecipientServiceClasses
						donorQuotaList, // donor quota
                        //defaultRecipientQuota, //recipient quota
                        transactionChargeBands,
                        new CumulativeLimits(1000,10000,100000),
                        new CumulativeLimits(1000,10000,100000)),


				new CreditTransferVariant("VoiceToMain", // variantID
						12, // ussdID
						new Texts("Voice to Main","Voice to Main","Voix aux Main"), // name
						17, // donorAccountID
						DedicatedAccountUnitType.TIME, // donorAccountType (0==minutes)
						50000, // donorMinBalance
						10000000, // donorMaxBalance
						0, // recipientAccountID
						DedicatedAccountUnitType.MONEY, // recipientAccountType (1==money)
						0, // recipientMinBalance
						10000000, // recipientMaxBalance
						Integer.valueOf(5), // recipientExpiryDays
						new Texts("min"), // donorUnits
						new Texts("USD"), // recipientUnits
						7500, // unitCostOfDonor
						10000, // unitCostOfBenefit
						100000, // minAmount transferable
						50000000, // maxAmount transferable
						this.allowedDonorServiceClassIds, // validDonorServiceClasses
						this.allowedRecipientServiceClassIds, // validRecipientServiceClasses
						donorQuotaList, // donor quota
                        //defaultRecipientQuota, //recipient quota
                        transactionChargeBands,
                        new CumulativeLimits(1000,10000,100000),
                        new CumulativeLimits(1000,10000,100000)),

		};// transfer variants

		// Single Shot USSD and SMS Commands
		protected VasCommand[] commands = new VasCommand[]
		{
				new VasCommand(VasCommand.Processes.transfer, "*{UssdOption}*{MsisdnB}*{Amount}*{Pin}#"), //
				new VasCommand(VasCommand.Processes.transfer, "*{Anything}#"), //
				// default i.e *SHORTCODE#
				new VasCommand(VasCommand.Processes.transfer, "#"), //
		};

		// Service Specific error Mapping
		// @formatter:off
		protected ReturnCodeTexts[] returnCodesTexts = new ReturnCodeTexts[]
		{
			/*
			new ReturnCodeTexts(ReturnCodes.alreadyAdded, //
				"This offer has already been added for the beneficiary", //
				"Cette offre existe déjà pour le bénéfiaire"),
			*/
			new ReturnCodeTexts(ReturnCodes.malformedRequest,
				Phrase.en("Welcome to the Credit Transfer Service. Dial *153*OPTION*NUMBER*AMOUNT*PIN# to transfer. Thank you.")
				.fre("Ceci est une erreur d'utilisation.  Veuillez réessayer SVP.")),

			new ReturnCodeTexts(ReturnCodes.success,
					Phrase.en("Your transfer was successfully completed.")
					.fre("Transfert effectue avec succes.")),

			new ReturnCodeTexts(ReturnCodes.invalidNumber,
					Phrase.en("The number you are transferring to is invalid")
					.fre("Le numéro de transfert n'est pas valide.")),

			new ReturnCodeTexts(ReturnCodes.cannotTransferToSelf,
					Phrase.en("You are not allowed to transfer to yourself")
					.fre("Vous n'etes pas autorisé à vous transférer du crédit.")),
			/*
			new ReturnCodeTexts(ReturnCodes.suspended,
					Phrase.en("You have expired. You can not use this service",
					.fre("Votre compte a expiré. Vous ne pouvez pas utiliser ce service."),
			*/
			new ReturnCodeTexts(ReturnCodes.inactiveAParty,
					Phrase.en("You are not active,  please activate before using Credit Transfer.")
					.fre("Votre compte n'est pas actif.")),
			/*
			new ReturnCodeTexts(ReturnCodes.temporaryBlocked,
					Phrase.en("You are not in an authorized service class",
					.fre("Désolé, vous n'etes pas autorisé à transférer du crédit."),
			*/
			new ReturnCodeTexts(ReturnCodes.quantityTooSmall,
					Phrase.en("Transfer amount too small.")
					.fre("Désolé, le montant à transférer est trop bas.")),

			new ReturnCodeTexts(ReturnCodes.quantityTooBig,
					Phrase.en("Transfer amount too big.")
					.fre("Désolé, le montant a transférer est trop élevé.")),

			new ReturnCodeTexts(ReturnCodes.insufficientBalance,
					Phrase.en("You have insufficient balance")
					.fre("Désolé, votre crédit est insuffisant pour effectuer cette opération.")),

			new ReturnCodeTexts(ReturnCodes.pinBlocked,
					Phrase.en("Your PIN is blocked, please contact customer care.")
					.fre("Votre code PIN est bloqué, se il vous plaît communiquer avec le service à la clientèle")),

			new ReturnCodeTexts(ReturnCodes.unregisteredPin,
					Phrase.en("Unregistered PIN, please change your default PIN.")
					.fre("PIN non enregistré, se il vous plaît changer vous PIN par défaut.")),

			new ReturnCodeTexts(ReturnCodes.invalidPin,
					Phrase.en("Your PIN is invalid.")
					.fre("Votre code de transfert n'est pas valide.")),

			new ReturnCodeTexts(ReturnCodes.quotaReached,
					Phrase.en("You have exceeded the maximum transfers.")
					.fre("Desole, vous avez depasse le nombre maximum d'operations de transfert")),

			new ReturnCodeTexts(ReturnCodes.cannotReceiveCredit,
					Phrase.en("The recipient cannot receive any more credit for the current period.")
					.fre("Le destinataire ne peut pas recevoir plus de cr�dit pour la p�riode actuelle.")),

			new ReturnCodeTexts(ReturnCodes.inactiveBParty,
					Phrase.en("The number you are sending to is not active, your friend must activate before using Credit Transfer.")
					.fre("Le numero de destination n'est pas actif.")),
			/*
			new ReturnCodeTexts(ReturnCodes.memberNotEligible,
					"Recipient unable to receive money.",
					.fre("Ce destinataire ne peut pas recevoir de crédit."),
			*/
			new ReturnCodeTexts(ReturnCodes.excessiveBalance,
					// "The number {recipientMsisdn} has too much money in their account. You can not send him more.",
					// "Ce destinataire {recipientMsisdn} a suffisamment de crédit. Vous ne pouvez pas lui en donner plus."),
					Phrase.en("The recipient number has too much money in their account. You can not send him more.")
					.fre("Ce destinataire a suffisamment de crédit. Vous ne pouvez pas lui en donner plus.")),

			new ReturnCodeTexts(ReturnCodes.technicalProblem, //
					Phrase.en("Technical error.") //
					.fre("Erreur technique.")),

			new ReturnCodeTexts(ReturnCodes.invalidVariant, //
					Phrase.en("Invalid variant") //
					.fre("Variant incorrecte")),

			new ReturnCodeTexts(ReturnCodes.notEligible, //
					Phrase.en("Not allowed to use this service") //
					.fre("Pas autorisé à utiliser ce service"))
		};// returnCodesTexts


		// -------------------------------------------------------------------------------------------------------//

		public void setShortCode(String shortCode)
		{
			check(esb, "ChangeCreditTransferParameters");
			this.shortCode = shortCode;
		}

		public String getShortCode()
		{
			check(esb, "ViewCreditTransferParameters");
			return shortCode;
		}

		public String getSmsSourceAddress()
		{
			check(esb, "ViewCreditTransferParameters");
			return smsSourceAddress;
		}

		public void setSmsSourceAddress(String smsSourceAddress)
		{
			check(esb, "ChangeCreditTransferParameters");
			this.smsSourceAddress = smsSourceAddress;
		}

		public ReturnCodeTexts[] getReturnCodesTexts()
		{
			check(esb, "ViewCreditTransferParameters");
			return returnCodesTexts;
		}

		public void setReturnCodesTexts(ReturnCodeTexts[] returnCodesTexts)
		{
			check(esb, "ChangeCreditTransferParameters");
			this.returnCodesTexts = returnCodesTexts;
		}

		public VasCommand[] getCommands()
		{
			check(esb, "ViewCreditTransferParameters");
			return commands;
		}

		public void setCommands(VasCommand[] commands) throws ValidationException
		{
			check(esb, "ChangeCreditTransferParameters");

			for (VasCommand command : commands)
			{
				try
				{
					if (!validateVasCommand(command.getCommand()))
						throw new ValidationException("Invalid command format.");
				}
				catch (ValidationException e)
				{
					throw new ValidationException(e.getLocalizedMessage() + " in command " + command.getCommand());
				}
			}

			this.commands = commands;
		}

		// allowedServiceClasses (getter/setter)
		@Config(description = "Allowed Donor Service Classes", hidden = true)
		public int[] getAllowedDonorServiceClassIds()
		{
			check(esb, "ViewCreditTransferParameters");
			return allowedDonorServiceClassIds;
		}

		public void setAllowedServiceClasses(int[] allowedServiceClasses) throws ValidationException
		{
			check(esb, "ChangeCreditTransferParameters");
			this.allowedDonorServiceClassIds = allowedServiceClasses;
		}
		/*
		@Config(description = "Transaction Charges", hidden = true)
		public TransactionCharge[] getTransactionCharges()
		{
			check(esb, "ViewCreditTransferParameters");
			return transactionChargeBands;
		}

		public void setTransactionCharges(TransactionCharge transactionCharges[]) throws ValidationException
		{
			check(esb, "ChangeCreditTransferParameters");

			CreditTransferVariant.validateTransactionCharges(transactionCharges);

			this.transactionChargeBands = transactionCharges;
		}
		*/
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
			return 52472278881652629L;
		}

		@Override
		public String getName(String languageCode)
		{
			return getServiceName(null);
		}

		@Override
		public void validate() throws ValidationException
		{
		}

		@Override
		public void performUpdateNotificationSecurityCheck()
		{
			check(esb, "ChangeCreditTransferNotifications");
		}

		@Override
		public void performGetNotificationSecurityCheck()
		{
			check(esb, "ViewCreditTransferNotifications");
		}

		// allowedRecipientServiceClasses
		@Config(description = "Allowed Recipient Service Classes", hidden = true)
		public int[] getAllowedRecipientServiceClasses()
		{
			check(esb, "ViewCreditTransferParameters");
			return allowedRecipientServiceClassIds;
		}

		public void setAllowedRecipientServiceClasses(int[] allowedRecipientServiceClasses)
		{
			check(esb, "ChangeCreditTransferParameters");
			this.allowedRecipientServiceClassIds = allowedRecipientServiceClasses;
		}

		public Quota[] getDonorQuotas()
		{
			check(esb, "ViewCreditTransferParameters");
			return donorQuotas;
		}

		public Quota getDonorQuota(String[] donorQuotaIDs, QuotaPeriodUnits units)
		{
			// Search for a matching ID and unit pair
			for (String s : donorQuotaIDs)
			{
				for (Quota q : this.donorQuotas)
				{
					if (s.equals(q.getQuotaID()))
						if (units.equals(q.getLimitPeriodUnits()))
							return q;
				}
			}

			// If quota is not defined for this period in this variant
			return null;
		}

		public void setDonorQuotas(Quota[] donorQuotas) throws ValidationException
		{
			check(esb, "ChangeCreditTransferParameters");
			validateDonorQuotas(donorQuotas);
			this.donorQuotas = donorQuotas;
		}

		@Config(description = "Variants", hidden = false)
		public CreditTransferVariant[] getVariants()
		{
			check(esb, "ViewCreditTransferParameters");
			return variants;
		}

		public void setVariants(CreditTransferVariant[] variants) throws ValidationException
		{
			check(esb, "ChangeCreditTransferParameters");

			ArrayList<Integer> ussdIDs = new ArrayList<Integer>();
			ArrayList<String> variantIDs = new ArrayList<String>();

			//ensure variants are valid
			for (CreditTransferVariant v : variants)
			{
				try
				{
					v.validate();

					validateQuotaIDs(v);

					if (!ussdIDs.contains(v.getUssdID()))
						ussdIDs.add(v.getUssdID());
					else
						throw new ValidationException("Duplicate USSD ID %s in variant %s", v.getUssdID(), v.getVariantID());

					if (!variantIDs.contains(v.getVariantID()))
						variantIDs.add(v.getVariantID());
					else
						throw new ValidationException("Duplicate variant ID: %s", v.getVariantID());

				}
				catch (ValidationException e)
				{
					throw e;
				}
			}

			this.variants = variants;
		}


		/**
		 * @return the usageCounter
		 */
		@Config(description = "Usage counter", hidden = true)
		public UsageCounter getUsageCounter()
		{
			check(esb, "ViewCreditTransferParameters");
			return usageCounter;
		}

		/**
		 * @param usageCounter
		 *            the usageCounter to set
		 */
		@Config(description = "Usage counter", hidden = true)
		public void setUsageCounter(UsageCounter usageCounter)
		{
			check(esb, "ChangeCreditTransferParameters");
			this.usageCounter = usageCounter;
		}

		/**
		 * @param allowedDonorServiceClasses
		 *            the allowedDonorServiceClasses to set
		 */
		public void setAllowedDonorServiceClasses(int[] allowedDonorServiceClasses)
		{
			check(esb, "ChangeCreditTransferParameters");
			this.allowedDonorServiceClassIds = allowedDonorServiceClasses;
		}

		/**
		 * @return the quota
		 */
		/*
		@Config(description = "Quota", hidden = true)
		public CreditTransferQuota getQuota()
		{
			check(esb, "ViewCreditTransferParameters");
			return quota;
		}
		 */
		/**
		 * @param quota
		 *            the quota to set
		 */
		/*
		public void setQuota(CreditTransferQuota quota)
		{
			check(esb, "ChangeCreditTransferParameters");
			this.quota = quota;
		}
		 */

		private void validateQuotaIDs(CreditTransferVariant v) throws ValidationException
		{
			String[] donorQuotas = v.getDonorQuotas();
			ArrayList<QuotaPeriodUnits> existingQuotaPeriods = new ArrayList<QuotaPeriodUnits>();

			for (String s : donorQuotas)
			{
				boolean found = false;
				for (Quota q : this.donorQuotas)
				{
					// Check that the quota ID is found in the list of defined quotas
					if (s.equals(q.getQuotaID()))
					{
						found = true;
						// Check that there are no duplicate limit periods
						if ( existingQuotaPeriods.contains(q.getLimitPeriodUnits()) )
							throw new ValidationException("Duplicate Quota Period %s for ID: %s", q.getLimitPeriodUnits().name(), s);
						existingQuotaPeriods.add(q.getLimitPeriodUnits());
						break;
					}
				}
				if (!found)
					throw new ValidationException("Invalid Quota ID: %s", s);
			}
		}

		private void validateDonorQuotas(Quota[] quotas) throws ValidationException
		{
			ArrayList<String> quotaIDs = new ArrayList<String>();
			for (Quota q : quotas)
			{
				if (!quotaIDs.contains(q.getQuotaID()))
					quotaIDs.add(q.getQuotaID());
				else
					throw new ValidationException("Duplicate quota ID: %s", q.getQuotaID());

			}
		}


	} // CreditTransferConfig (embedded class)


	protected CreditTransferConfig config = new CreditTransferConfig();


	//Given the configured array of ServiceClass objects, return an array of int ID's
//	public Integer[] getDonorServiceClassIDs()
//	{
//		ServiceClass[] serviceClasses = config.getAllowedDonorServiceClasses();
//		Integer scIDs[] = new Integer[serviceClasses.length];
//
//		int i = 0;
//		for (ServiceClass c : serviceClasses)
//		{
//			scIDs[i++] = c.getServiceClassID();
//		}
//
//		return scIDs;
//	}

	//Given the configured array of ServiceClass objects, return an array of int ID's
//	public Integer[] getRecipientServiceClassIDs()
//	{
//		ServiceClass[] serviceClasses = config.getAllowedRecipientServiceClasses();
//		Integer sc[] = new Integer[serviceClasses.length];
//
//		int i = 0;
//		for (ServiceClass c : serviceClasses)
//		{
//			sc[i++] = c.getServiceClassID();
//		}
//
//		return sc;
//	}

	public CreditTransferConfig getCreditTransferConfiguration()
	{
		if (config == null)
		{
			this.config = new CreditTransferConfig();
		}

		return this.config;
	}


	//Given a transferAmount, returns an applicable charge
//	public long getTransactionCharge(long transferAmount)
//	{
//		long transactionCharge = 0L;
//		long lowerBound = 0L;
//		long upperBound = 0L;
//
//		// search for the object whose range contains 'transferAmount'
//		TransactionCharge[] charges = config.getTransactionCharges();
//		for (TransactionCharge charge : charges)
//		{
//			lowerBound = charge.getAmountRange().getMinValue();
//			upperBound = charge.getAmountRange().getMaxValue();
//
//			if ((lowerBound <= transferAmount*chargeScalingFactor) && (transferAmount*chargeScalingFactor < upperBound))
//			{
//				long variableCharge = transferAmount * ( (long) (charge.getPercentageCharge() * chargeScalingFactor) )/ 100;
//				transactionCharge = (long) (charge.getFixedCharge() * chargeScalingFactor) + variableCharge;
//				break;
//			}
//		}
//
//		return transactionCharge;
//	}

	//Given a variantID, returns a variant from the list of configured variants
	public CreditTransferVariant getVariant(String variantID)
	{
		CreditTransferVariant variants[] = config.getVariants();
		for (CreditTransferVariant variant : variants)
		{
			if (variant.getVariantID().equalsIgnoreCase(variantID))
			{
				return variant;
			}
		}

		return null;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Notifications
	//
	// /////////////////////////////////
	protected Notifications notifications = new Notifications(Properties.class);

	// Notification IDs
	protected int smsDonorTransfered = notifications.add("Successfull Transfer SMS", //
			"Hello, you have transferred {NumberOfBenefits} {BenefitUnits} to {RecipientMsisdn} and been charged {Charge} {CurrencyUnits}. Account Balance: {donorBalanceAfter} {CurrencyUnits}.", //
			"Bonjour, vous avez souscrit à un abonnement pour {Charge} {CurrencyUnits}");

	protected int ussdDonorTransfered = notifications.add("Successfull Transfer USSD", //
			"Successfully transferred {NumberOfBenefits} {BenefitUnits} to {RecipientMsisdn} and been charged {Charge} {CurrencyUnits}.", //
			"Bonjour, vous avez souscrit à un abonnement pour {Charge} {CurrencyUnits}");

	protected int smsRecipientReceived = notifications.add("Successfull Reception", //
			"Hello, you have been topped up with {NumberOfBenefits} {BenefitUnits} until {ExpiryDate}", //
			"Bonjour, vous avez souscrit à un abonnement pour {NumberOfBenefits} {BenefitUnits} jusqu'au {ExpiryDate}");

	protected int mainRecipientToppedUp = notifications.add("Successful Main Topup", "Hello, you have been topped up with {NumberOfBenefits} {BenefitUnits}", //
			"Bonjour, vous avez souscrit à un abonnement pour {NumberOfBenefits} {BenefitUnits}");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	class Properties
	{
		protected String charge = "0";
		protected String expiryDate = "Never";
		protected String benefitUnits = "units";
		protected String currencyUnits = "USD";
		protected String numberOfBenefits = "0";
		protected String newDonorBalance = "0";
		protected String newRecipientBalance = "0";
		protected String dailyUsageLimit = "5";
		protected String donorMsisdn = "";
		protected String donorBalanceAfter = "0";
		protected String msisdnB = "";
		protected String ussdOption = "";
		protected String amount = "";
		protected String pin = "";

		protected String recipientMsisdn = "";

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

		public String getDonorMsisdn()
		{
			return donorMsisdn;
		}

		public void setDonorMsisdn(String donorMsisdn)
		{
			this.donorMsisdn = donorMsisdn;
		}

		public String getRecipientMsisdn()
		{
			return recipientMsisdn;
		}

		public void setRecipientMsisdn(String recipientMsisdn)
		{
			this.recipientMsisdn = recipientMsisdn;
		}

		public String getBenefitUnits()
		{
			return benefitUnits;
		}

		public void setBenefitUnits(String benefitUnits)
		{
			this.benefitUnits = benefitUnits;
		}

		public String getCurrencyUnits()
		{
			return currencyUnits;
		}

		public void setCurrencyUnits(String currenyUnits)
		{
			this.currencyUnits = currenyUnits;
		}

		public String getNumberOfBenefits()
		{
			return numberOfBenefits;
		}

		public void setNumberOfBenefits(long numberOfBenefits)
		{
			this.numberOfBenefits = Long.valueOf(numberOfBenefits).toString();
		}

		public String getNewDonorBalance()
		{
			return newDonorBalance;
		}

		public void setNewDonorBalance(String newDonorBalance)
		{
			this.newDonorBalance = newDonorBalance;
		}

		public String getNewRecipientBalance()
		{
			return newRecipientBalance;
		}

		public void setNewRecipientBalance(String newRecipientBalance)
		{
			this.newRecipientBalance = newRecipientBalance;
		}

		public String getDailyUsageLimit()
		{
			return dailyUsageLimit;
		}

		public void setDailyUsageLimit(String dailyUsageLimit)
		{
			this.dailyUsageLimit = dailyUsageLimit;
		}

		public void setNumberOfBenefits(String numberOfBenefits)
		{
			this.numberOfBenefits = numberOfBenefits;
		}

		public String getDonorBalanceAfter()
		{
			return donorBalanceAfter;
		}

		public void setDonorBalanceAfter(String donorBalanceAfter)
		{
			this.donorBalanceAfter = donorBalanceAfter;
		}

		public String getMsisdnB()
		{
			return msisdnB;
		}

		public String getUssdOption()
		{
			return ussdOption;
		}

		public String getAmount()
		{
			return amount;
		}

		public String getPin()
		{
			return pin;
		}

		public void setMsisdnB(String msisdnB)
		{
			this.msisdnB = msisdnB;
		}

		public void setUssdOption(String ussdOption)
		{
			this.ussdOption = ussdOption;
		}

		public void setAmount(String amount)
		{
			this.amount = amount;
		}

		public void setPin(String pin)
		{
			this.pin = pin;
		}


	}// Properties

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	@SuppressWarnings("unused")
	private String getVasCommand(UssdVars[] ussdVarArray)
	{
		String vasCommand = "";

		for (UssdVars var : ussdVarArray)
		{
			vasCommand = vasCommand.concat("*{" + var.name() + "}");
		}

		vasCommand = vasCommand.concat("#");

		return vasCommand;
	}

	private boolean validateVasCommand(String vasCommand) throws ValidationException
	{
		if (vasCommand.equals("#"))
			return true;

		ArrayList<UssdVars> vasCommandArray = new ArrayList<UssdVars>();

		// Split up parameters into a string array
		ArrayList<String> tempStringArray = extractUssdParameters(vasCommand);
		String[] ussdFormatString = new String[tempStringArray.size()];
		ussdFormatString = tempStringArray.toArray(ussdFormatString);

		if (ussdFormatString.length == 0)
			throw new ValidationException("Invalid command format");

		// Validate each parameter against UssdVars, and check for duplicates
		String currentParameter = "";
		try
		{
			for ( String keyword : ussdFormatString )
			{
				currentParameter = keyword;
				if ( vasCommandArray.contains(UssdVars.valueOf(keyword)) )
					return false;
				vasCommandArray.add(UssdVars.valueOf(keyword));
			}
		}
		catch (IllegalArgumentException e)
		{
			throw new ValidationException("Invalid parameter: " + currentParameter);
		}

		return true;
	}

	private ArrayList<String> extractUssdParameters(String vasCommand) throws ValidationException
	{
		ArrayList<String> ussdParameters = new ArrayList<String>();

		if (vasCommand.charAt(0) != '*')
			throw new ValidationException("Command must begin with * ");

		//regex: match *{PARAMETER} and extract PARAMETER
		Pattern regex = Pattern.compile("\\*\\{([^}]*)\\}");
		Matcher regexMatcher = regex.matcher(vasCommand);
		while (regexMatcher.find())
		{
			String parameter = regexMatcher.group(1);
			if (parameter.equals("") || parameter.length() == 0)
				throw new ValidationException("Invalid command format");
			ussdParameters.add(parameter);
		}

		return ussdParameters;
	}

	private void connectAirSim(HuxProcessState state, HuxConnection connection)
	{
		airSimulator = new AirSim(esb, 10010, "/Air", numberPlan, "USD");
		airSimulator.start();
		airSimulator.addSubscriber("0824452655", 2, 76, 14000, SubscriberState.active);
		airSimulator.addSubscriber("0824452656", 2, 76, 14000, SubscriberState.active);
		airSimulator.addSubscriber("0824452657", 2, 76, 14000, SubscriberState.active);
		//IServiceContext context = new ServiceContext();

		state.setCompleted(true);
		state.setOutput("Air Mockery Started");
		connection.display(state);
	}

	@Override
	protected ReturnCodeTexts[] getReturnCodeTexts()
	{
		return config.getReturnCodesTexts();
	}

}

