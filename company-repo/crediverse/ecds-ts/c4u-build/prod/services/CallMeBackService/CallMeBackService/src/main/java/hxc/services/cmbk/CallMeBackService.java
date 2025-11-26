package hxc.services.cmbk;

import java.sql.SQLException;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.concurrent.hxc.IServiceContext;
import com.concurrent.hxc.SubscriptionState;
import com.concurrent.hxc.VasServiceInfo;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.hux.HuxConnection;
import hxc.connectors.hux.HuxProcessState;
import hxc.connectors.IConnection;
import hxc.connectors.IInteraction;
//import hxc.connectors.air.AirException;
//import hxc.connectors.air.proxy.Subscriber;
import hxc.connectors.air.IAirConnector;
import hxc.connectors.zte.*;
import hxc.connectors.zte.proxy.Subscriber;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.vas.VasService;
import hxc.connectors.vas.VasCommand.Processes;
import hxc.connectors.vas.VasCommand;
import hxc.connectors.vas.VasCommandParser;
import hxc.connectors.vas.VasCommandParser.CommandArguments;
import hxc.connectors.soap.ISoapConnector;
import hxc.connectors.soap.ISubscriber;
import hxc.connectors.sms.ISmsConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.servicebus.Trigger;
import hxc.servicebus.ILocale;
import hxc.services.IService;
import hxc.services.logging.ILogger;
import hxc.services.notification.INotificationText;
import hxc.services.notification.INotifications;
import hxc.services.notification.IPhrase;
import hxc.services.notification.Phrase;
import hxc.services.notification.ReturnCodeTexts;
import hxc.services.transactions.CdrBase;
import hxc.services.transactions.CsvCdr;
import hxc.services.transactions.ICdr;
import hxc.services.transactions.ITransaction;
import hxc.services.transactions.ITransactionService;
import hxc.services.transactions.Transaction;
import hxc.services.numberplan.INumberPlan;
import hxc.services.cmbk.Variant;
import hxc.services.cmbk.ServiceClass;
import hxc.services.cmbk.CallMeBackServiceRecord;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.notification.Notifications;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.ResponseHeader;
import com.concurrent.hxc.ProcessRequest;
import com.concurrent.hxc.ProcessResponse;
import com.concurrent.hxc.ServiceContext;
import com.concurrent.hxc.IServiceContext;
import java.io.IOException;

public class CallMeBackService extends VasService implements IService, ICallMeBackService
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	protected IServiceBus esb;
	protected ILocale locale;
	protected ILogger logger;
	protected IDatabase database;
	protected ITransactionService transactions;
	protected INumberPlan numberPlan;
	protected ISmsConnector smsConnector;
	protected IAirConnector air;
	protected IZTEConnector zte;
	protected ISoapConnector soapConnector;

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
		// Must have Logger
		logger = esb.getFirstService(ILogger.class);
		if (logger == null)
			return false;

		// Must have Database
		database = esb.getFirstConnector(IDatabase.class);
		if (database == null)
			return false;
		// Must have SMS Connector
		
		smsConnector = esb.getFirstConnector(ISmsConnector.class);
		if (smsConnector == null)
			return false;

		// Must have Number Plan Service
		numberPlan = esb.getFirstService(INumberPlan.class);
		if (numberPlan == null)
			return false;

		// Must have Soap Connector
		soapConnector = esb.getFirstConnector(ISoapConnector.class);
		if (soapConnector == null)
			return false;

		// Must have Transaction Service
		transactions = esb.getFirstService(ITransactionService.class);
		if (transactions == null)
			return false;

		// Must have Air
		air = esb.getFirstConnector(IAirConnector.class);
		if (air == null)
			return false;

		/*
		 * Must have ZTE zte = esb.getFirstConnector(IZTEConnector.class); if (zte == null) return false;
		 */
		zte = esb.getFirstConnector(IZTEConnector.class); 
		if (zte == null) 
			return false;
		
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
				commandParser.execute(interaction, esb.getLocale());
			}
		};
		esb.addTrigger(smsTrigger);

		// Get Locale
		this.locale = esb.getLocale();

		// Log Information
		logger.info(this, "Call Me Back Service Started");

		return true;
	}

	@Override
	public void stop()
	{
		// Log Information
		logger.info(this, "Call Me Back Service Stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		this.config = (CmbkServiceConfig) config;
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
	public ProcessResponse process(IServiceContext context, ProcessRequest request)
	{
		// Prepare an Response
		ProcessResponse response = super.process(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(hxc.connectors.zte.proxy.Subscriber.TRANSACTION_ID_LENGTH));

		cdr.setA_MSISDN(request.getSubscriberNumber().toMSISDN());
		cdr.setB_MSISDN(request.getMemberNumber().toMSISDN());
		cdr.setServiceID(request.getServiceID());
		cdr.setProcessID("CMBK");
		// Update CDR
		cdr.setServiceID(getServiceID());

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = ProcessRequest.validate(request);
				if (problem != null)
					return response.exitWith(logger, cdr, ReturnCodes.malformedRequest, problem);

				// Validate SOAP incoming Variant, NOT USSD variant
				Variant variant = getVariant(request.getVariantID());
				if (request.getVariantID() != null && variant == null)
				{
					context.setResultText("Invalid VariantID");
					cdr.setAdditionalInformation("Invalid VariantID");
					return response.exitWith(logger, cdr, ReturnCodes.malformedRequest, "Invalid VariantID");
				}

				// Update CDR
				cdr.setServiceID(request.getServiceID());
				cdr.setVariantID(request.getVariantID());
				cdr.setProcessID("ProcessCMBK");

				// Get subscriberNumber Proxy
				CallMeBackServiceRecord cmbkRecord = null;
				String returnText = "Not Successful";
				try
				{
					Subscriber subscriber = getSubscriber(context, request.getSubscriberNumber(), transaction);
				
					String recipientMSISDN = request.getMemberNumber().toString();
					//remove leading zeroes 
					while (recipientMSISDN.indexOf("0") == 0)
					{
						recipientMSISDN = recipientMSISDN.substring(1, recipientMSISDN.length());
					}
					recipientMSISDN = numberPlan.getNationalFormat(recipientMSISDN);

					Properties properties = getProperties(context);
					// Check self send
					transaction.track(this, "CheckSelfSend");
					this.getProperties(context).setSubscriberNumber(subscriber.getInternationalNumber());
					this.getProperties(context).setRecipientNumber(numberPlan.getNationalFormat(recipientMSISDN));
					if (isSameNumber(subscriber.getNationalNumber(), recipientMSISDN))
					{
						returnText = getNotificationText(subscriber, returnCodesCannotCallSelf, properties);
						context.setResultText(returnText);
						cdr.setAdditionalInformation("You cannot send CMBK to Yourself");
						return response.exitWith(logger, cdr, ReturnCodes.cannotCallSelf, returnText);
					}

					// Test if the subscriberNumber Account is in a valid state.
					transaction.track(this, "CheckAccountActive");
					if (!isSubscriberActive(subscriber))
					{
						returnText = getNotificationText(subscriber, returnCodesInactiveAParty, properties);
						context.setResultText(returnText);
						cdr.setAdditionalInformation("You cannot send CMBK Your account is inactive");
						return response.exitWith(logger, cdr, ReturnCodes.inactiveAParty, returnText);
					}

					// Test if the subscriberNumber is in one of the allowed
					// service classes
					transaction.track(this, "CheckValidServiceClass");
					if (subscriber.getPostPaidFlag())
					{
						returnText = getNotificationText(subscriber, returnCodesOnlyPrepaid, properties);
						context.setResultText(returnText);
						cdr.setAdditionalInformation("Only Prepaid may use CMBK");
						return response.exitWith(logger, cdr, ReturnCodes.notEligible, returnText);
					}
					
					ServiceClass subscriberServiceClass = getServiceClass(subscriber);
					if (subscriberServiceClass == null)
					{
						subscriberServiceClass = getServiceClass(DEFAULT_SERVICE_CLASS_ID);
						if (subscriberServiceClass == null) // we have tried
						{
							returnText = getNotificationText(subscriber, returnCodesNotEligible, properties);
							context.setResultText(returnText);
							cdr.setAdditionalInformation("You may not use CMBK");
							return response.exitWith(logger, cdr, ReturnCodes.notEligible, returnText);
						}
					}
					// Check min and MaxBal
					if(subscriberServiceClass.getMinBal() != 0L && subscriber.getAccountBalance(0) < subscriberServiceClass.getMinBal())
					{
						returnText = getNotificationText(subscriber, returnCodesInsufficientBalance, properties);
						context.setResultText(returnText);
						cdr.setAdditionalInformation("You do not have enough Credit");
						return response.exitWith(logger, cdr, ReturnCodes.insufficientBalance, returnText);
					}
					
					if(subscriberServiceClass.getMaxBal() != 0L && subscriber.getAccountBalance(0) > subscriberServiceClass.getMaxBal())
					{
						returnText = getNotificationText(subscriber, returnCodesExcessiveBalance, properties);
						context.setResultText(returnText);
						cdr.setAdditionalInformation("You have too much Credit");
						return response.exitWith(logger, cdr, ReturnCodes.excessiveBalance, returnText);
					}
					
					// Test recipientNumber is in one of the allowed variants
					// linked to the service class
					transaction.track(this, "CheckValidNumberPlans");
					String variantMatchedID = null;

					List<String> variantList = new ArrayList<String>();
					variantList = Arrays.asList(subscriberServiceClass.getVariantString().split(","));

					for (int i = 0; i < variantList.size(); i++)
					{
						variant = getVariant(variantList.get(i));
						if (matchNumberPlan(recipientMSISDN, variant))
						{
							variantMatchedID = variant.getVariantID();
							break; // we have a Match!!!
						}
					}
					if (variantMatchedID == null)
					{
						returnText = getNotificationText(subscriber, returnCodesMemberNotEligible, properties);
						context.setResultText(returnText);
						cdr.setAdditionalInformation("You cannot set send CMBK to that Number");
						return response.exitWith(logger, cdr, ReturnCodes.memberNotEligible, returnText);
					}

					cmbkRecord = getCallMeBackRecord(db, request.getSubscriberNumber().toMSISDN(), variantMatchedID);
					if (cmbkRecord == null)
						return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, "Database problem");

					// check if limits are exceeded
					int newDayCount = cmbkRecord.getDayCount() + 1;
					int newWeekCount = cmbkRecord.getWeekCount() + 1;
					int newMonthCount = cmbkRecord.getMonthCount() + 1;
					long newTotalCount = cmbkRecord.getTotalCount() + 1;

					Date now = new Date();

					float diffDayHours = (float) (now.getTime() - cmbkRecord.getDayStartTime().getTime()) / (60 * 60 * 1000);
					float diffWeekDays = (float) (now.getTime() - cmbkRecord.getWeekStartTime().getTime()) / (24 * 60 * 60 * 1000);
					float diffMonthDays = (float) (now.getTime() - cmbkRecord.getMonthStartTime().getTime()) / (24 * 60 * 60 * 1000);

					// Update Daily usage
					if (diffDayHours >= 24)
					{
						newDayCount = 1;
						cmbkRecord.setDayStartTime(now);
					}

					// Update Weekly usage
					if (diffWeekDays >= 7)
					{
						newWeekCount = 1;
						cmbkRecord.setWeekStartTime(now);
					}

					// Update Monthly usage
					if (diffMonthDays >= 30)
					{
						newMonthCount = 1;
						cmbkRecord.setMonthStartTime(now);
					}
					// Check free usage of variant - If over free limit, set
					// charge
					long charge = 0;
					if (variant.getFreeDailyRequests() > -1 && newDayCount > variant.getFreeDailyRequests())
						charge = variant.getCharge();
					if (variant.getFreeWeeklyRequests() > -1 && newWeekCount > variant.getFreeWeeklyRequests())
						charge = variant.getCharge();
					if (variant.getFreeMonthlyRequests() > -1 && newMonthCount > variant.getFreeMonthlyRequests())
						charge = variant.getCharge();

					// Check max usage of variant
					if (variant.getMaxDailyRequests() > -1 && newDayCount > variant.getMaxDailyRequests())
					{
						returnText = getNotificationText(subscriber, returnCodesMaxCountExceeded, properties);
						context.setResultText(returnText);
						cdr.setAdditionalInformation("You have used CMBK too much please try again tomorrow");
						return response.exitWith(logger, cdr, ReturnCodes.maxCountExceeded, returnText);
					}
					if (variant.getMaxWeeklyRequests() > -1 && newWeekCount > variant.getMaxWeeklyRequests())
					{
						returnText = getNotificationText(subscriber, returnCodesMaxCountExceeded, properties);
						context.setResultText(returnText);
						cdr.setAdditionalInformation("You have used CMBK too much please try again tomorrow");
						return response.exitWith(logger, cdr, ReturnCodes.maxCountExceeded, returnText);
					}
					if (variant.getMaxMonthlyRequests() > -1 && newMonthCount > variant.getMaxMonthlyRequests())
					{
						returnText = getNotificationText(subscriber, returnCodesMaxCountExceeded, properties);
						context.setResultText(returnText);
						cdr.setAdditionalInformation("You have used CMBK too much please try again tomorrow");
						return response.exitWith(logger, cdr, ReturnCodes.maxCountExceeded, returnText);
					}

					cmbkRecord.setDayCount(newDayCount);
					cmbkRecord.setWeekCount(newWeekCount);
					cmbkRecord.setMonthCount(newMonthCount);
					cmbkRecord.setTotalCount(newTotalCount);

					// Charge if needed
					if (charge>0)
						transaction.track(this, "ChargeIntl");
					if (request.getMode() == RequestModes.testOnly)
					{
						cdr.setChargeLevied((int) charge);
						response.setChargeLevied(charge);
						properties.setCharge(locale.formatCurrency(charge));
						transaction.complete();
						cdr.setAdditionalInformation("Successful test"); 
						return response.exitWith(logger, cdr, ReturnCodes.successfulTest, "Success test");
					}		
					if (!chargeSubscriber(subscriber, charge, subscriberServiceClass, properties, cdr, response))
					{
						returnText = getNotificationText(subscriber, returnCodesInsufficientBalance, properties);
						context.setResultText(returnText);
						cdr.setAdditionalInformation("You have too little credit to use CMBK");
						return response.exitWith(logger, cdr, ReturnCodes.insufficientBalance, returnText);
					}

					// Send a configurable SMS to the Recipient to inform him of
					// the successful recipient addition, with an invitation to
					// decline by replying with "No" to the origin short code.
					transaction.track(this, "SendRecipientSMS");

					String languageCode = getLanguageCode(subscriber);
					if (variant.getVariantType().compareTo(Variant.LOCAL_ONNET_VARIANT) == 0)
					{
						INotificationText text = notifications.get(smsOnNetRecipientCMBK, languageCode, locale, properties);
						if (text != null && text.getText() != null && text.getText().length() > 0)
							smsConnector.send(subscriber.getNationalNumber(), numberPlan.getInternationalFormat(recipientMSISDN), text);
						// Set the Result Text
						returnText = getNotificationText(subscriber, onNetSubscriberCMBK, properties);
					}

					else if (variant.getVariantType().compareTo(Variant.LOCAL_OFFNET_VARIANT) == 0)
					{
						INotificationText text = notifications.get(smsOffNetRecipientCMBK, languageCode, locale, properties);
						if (text != null && text.getText() != null && text.getText().length() > 0)
							smsConnector.send(subscriber.getNationalNumber(), numberPlan.getInternationalFormat(recipientMSISDN), text);
						// Set the Result Text
						returnText = getNotificationText(subscriber, offNetSubscriberCMBK, properties);
					}

					else if (variant.getVariantType().compareTo(Variant.LOCAL_ALLNET_VARIANT) == 0)
					{
						INotificationText text = notifications.get(smsAllNetRecipientCMBK, languageCode, locale, properties);
						if (text != null && text.getText() != null && text.getText().length() > 0)
							smsConnector.send(subscriber.getNationalNumber(), numberPlan.getInternationalFormat(recipientMSISDN), text);
						// Set the Result Text
						returnText = getNotificationText(subscriber, allNetSubscriberCMBK, properties);
					}
					
					else if (variant.getVariantType().compareTo(Variant.INTERNATIONAL_VARIANT) == 0)
					{
						INotificationText text = notifications.get(smsIntlRecipientCMBK, languageCode, locale, properties);
						if (text != null && text.getText() != null && text.getText().length() > 0)
							smsConnector.send(subscriber.getNationalNumber(), numberPlan.getInternationalFormat(recipientMSISDN), text);
						// Set the Result Text
						returnText = getNotificationText(subscriber, intlSubscriberCMBK, properties);
					}
					else
					{
						returnText = getNotificationText(subscriber, returnCodesMemberNotEligible, properties);
						context.setResultText(returnText);
						cdr.setAdditionalInformation("Member not eligible for Call Me Back");
						return response.exitWith(logger, cdr, ReturnCodes.memberNotEligible, returnText);
					}
				}

				catch (SQLException e)
				{
					cdr.setAdditionalInformation("Technical problem occurred");
					return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, e);
				}
				catch (ZTEException ex)
				{
					if (ex.getResponseCode() == ZTEException.SUBSCRIBER_FAILURE || ex.getResponseCode() == ZTEException.AUTHORIZATION_FAILURE)
					{
						{
							returnText = getNotificationText(null, returnCodesNotEligible, null);
							context.setResultText(returnText);
							cdr.setAdditionalInformation("Your Account is not found or is not active");
							return response.exitWith(logger, cdr, ReturnCodes.notEligible, returnText);
						}
					}
					cdr.setAdditionalInformation("Technical problem occurred");
					returnText = getNotificationText(null, returnCodesGenericError, null);
					context.setResultText(returnText);
					if (ex.getMessage() != null && ex.getMessage().length() != 0)
						logger.warn(this, "Rewording exception [%s] to [%s]", ex.getMessage(), returnText);
					else
						logger.warn(this, "Rewording exception [%s] to [%s]", ex.toString(), returnText);

					return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, returnText);
				}

				if (cmbkRecord.isNew)
					db.insert(cmbkRecord);
				else
					db.update(cmbkRecord);

				// Complete
				transaction.complete();
				context.setResultText(returnText);
				cdr.setAdditionalInformation("Successful Call Me Back");
				return response.exitWith(logger, cdr, ReturnCodes.success, returnText);
			}
		}
		catch (Throwable ex)
		{
			cdr.setAdditionalInformation("Technical problem occurred");
			String returnText = getNotificationText(null, returnCodesGenericError, null);
			context.setResultText(returnText);
			if (ex.getMessage() != null && ex.getMessage().length() != 0)
				logger.warn(this, "Rewording exception [%s] to [%s]", ex.getMessage(), returnText);
			else
				logger.warn(this, "Rewording exception [%s] to [%s]", ex.toString(), returnText);

			return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, returnText);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////
	//
	// Record Mechanisms
	//
	////////////////////////////////////
	// Read the record
	private CallMeBackServiceRecord getCallMeBackRecord(IDatabaseConnection db, String subscriberMsisdn, String variantID)
	{
		CallMeBackServiceRecord cmbkRecord = null;
		try
		{
			// load call me back record
			List<CallMeBackServiceRecord> callMeBackServiceRecords = getCallMeBackServiceRecords(db, getServiceID(), variantID, subscriberMsisdn);
			cmbkRecord = callMeBackServiceRecords.size() > 0 ? callMeBackServiceRecords.get(0) : null;

			// Create a new CallMeBack record if it doesn't exist
			boolean isNew = cmbkRecord == null;
			if (isNew)
			{
				cmbkRecord = new CallMeBackServiceRecord(/* serviceID */getServiceID(), /* variantID */variantID, /* subscriberMsisdn */subscriberMsisdn, /* TotalCMBK's */0L,
						/* dayStartTime */new Date(), /* dayCount */ 0, /* weekStartTime */new Date(), /* weekCount */ 0, /* monthStartTime */new Date(), /* monthCount */ 0);
			}
			else
				cmbkRecord.isNew = false;
			return cmbkRecord;
		}
		catch (Exception ex)
		{
			cmbkRecord = null;
			return cmbkRecord;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public CallMeBackService()
	{
		super();
		super.commandParser = new VasCommandParser(this, "RecipientMSISDN")
		{
			@Override
			protected VasCommand[] getCommands()
			{
				return config.commands;
			}

			@Override
			public boolean parseCommandVariable(VasCommand.Processes process, String commandVariable, String value, CommandArguments arguments)
			{
				switch (commandVariable)
				{
					case "RecipientMSISDN":
						arguments.memberNumber = arguments.recipientNumber = new Number(value);
						return true;
				}

				return false;
			}

			@Override
			protected ReturnCodes onPreExecute(IInteraction interaction, Processes process, ServiceContext context, CommandArguments arguments)
			{

				return ReturnCodes.success;
			}

			@Override
			protected ISubscriber getSubscriberProxy(String msisdn)
			{
				return new hxc.connectors.zte.proxy.Subscriber(msisdn, zte, logger, null);
			}

			@Override
			protected ISoapConnector getSoapConnector()
			{
				return soapConnector;
			}
		};
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configurable Parameters
	//
	// /////////////////////////////////
	@Perms(perms = { @Perm(name = "ViewCmbkServiceParameters", description = "View CmbkService Parameters", category = "CmbkService", supplier = true),
			@Perm(name = "ChangeCmbkServiceParameters", implies = "ViewCmbkServiceParameters", description = "Change CmbkService Parameters", category = "CmbkService", supplier = true),
			@Perm(name = "ViewCmbkServiceNotifications", description = "View PIN Service Notifications", category = "CmbkService", supplier = true),
			@Perm(name = "ChangeCmbkServiceNotifications", implies = "ViewCmbkServiceNotifications", description = "Change PIN Service Notifications", category = "CmbkService", supplier = true) })
	public class CmbkServiceConfig extends ConfigurationBase
	{
		protected String shortCode = "159";
		//protected String billingConnectorOption = "zte"; //air
		protected String smsSourceAddress = "159";

		protected Phrase serviceName = Phrase.en("Call Me BacK").fre("Bip Me : Rapelle Moi");

		// Service Specific error Mapping
		protected ReturnCodeTexts[] returnCodesTexts = new ReturnCodeTexts[] { //
//				new ReturnCodeTexts(ReturnCodes.insufficientBalance, Phrase.en("You do not have enough Credit").fre("FR: You do not have enough Credit")),
//				new ReturnCodeTexts(ReturnCodes.notEligible, Phrase.en("You May not use CMBK").fre("FR: You May not use CMBK")),
//				new ReturnCodeTexts(ReturnCodes.memberNotEligible, Phrase.en("You May not send CMBK to that number").fre("FR: You May not send CMBK to that number")),
//				new ReturnCodeTexts(ReturnCodes.cannotCallSelf, Phrase.en("You cannot send CMBK to yourself").fre("FR: You cannot send CMBK to yourself")),
//				new ReturnCodeTexts(ReturnCodes.excessiveBalance, Phrase.en("You have too much Credit").fre("FR: You have too much Credit")),
//				new ReturnCodeTexts(ReturnCodes.maxCountExceeded, Phrase.en("You have used CMBK to many times, please try tomorrow").fre("FR: You have used CMBK to many times, please try tomorrow")),
//				new ReturnCodeTexts(ReturnCodes.success, Phrase.en("You have Successfully sent CMBK to that number").fre("FR: You have Successfully sent CMBK to that number"))
		};

		public ReturnCodeTexts[] getReturnCodesTexts()
		{
			check(esb, "ViewCmbkServiceParameters");
			return returnCodesTexts;
		}

		
		public void setReturnCodesTexts(ReturnCodeTexts[] returnCodesTexts)
		{
			check(esb, "ChangeCmbkServiceParameters");
			this.returnCodesTexts = returnCodesTexts;
		}

		// Single Shot USSD and SMS Commands
		protected VasCommand[] commands = new VasCommand[] { //
				new VasCommand(VasCommand.Processes.process, "*{RecipientMSISDN}#"), //
				new VasCommand(VasCommand.Processes.process, "{RecipientMSISDN}"), //
		};

		// Variants
		protected Variant[] variants = new Variant[] { //
//				new Variant("LCL_ONNET", Phrase.en("Local On Net"), Variant.LOCAL_ONNET_VARIANT, 3, 3, -1, -1, -1, -1, 0, "26377XXXXXXX,26378XXXXXXX,77XXXXXXX,78XXXXXXX"), //
//				new Variant("LCL_OFFNET", Phrase.en("Local Off Net"), Variant.LOCAL_OFFNET_VARIANT, 1, 3, -1, -1, -1, -1, 1, "26371XXXXXXX,26373XXXXXXX,71XXXXXXX,73XXXXXXX"), //
				new Variant("LCL_ALLNET", Phrase.en("Local All net"), Variant.LOCAL_ALLNET_VARIANT, 5, 5, -1, -1, -1, -1, 0, "2637XXXXXXXX,7XXXXXXXX"),
				new Variant("INTL", Phrase.en("International"), Variant.INTERNATIONAL_VARIANT, 0, -1, -1, -1, -1, -1, 10, "XXXXXXXXXXXxxx"), //
		};

		protected Variant getVariant(String variantID)
		{
			if (variantID == null || variantID.length() == 0)
				variantID = DEFAULT_VARIANT_ID;

			for (Variant variant : config.getVariants())
			{
				if (variantID.equalsIgnoreCase(variant.getVariantID()))
					return variant;
			}

			return null;
		}

		public Variant[] getVariants()
		{
			check(esb, "ViewCmbkServiceParameters");
			return variants;
		}

		public void setVariants(Variant[] variants)
		{
			check(esb, "ChangeCmbkServiceParameters");
			this.variants = variants;
		}

		// Service Classes
		protected ServiceClass[] serviceClasses = new ServiceClass[] { //
				new ServiceClass(DEFAULT_SERVICE_CLASS_ID, Phrase.en("Default"), false, "LCL_ALLNET,INTL",0L,0L), //
		};

		protected ServiceClass getServiceClass(int getClassID)
		{
			if (getClassID < 0)
				getClassID = 0;

			for (ServiceClass serviceClass : config.getServiceClasses())
			{
				if (getClassID == serviceClass.getServiceClassID())
					return serviceClass;
			}

			return null;
		}

		public ServiceClass[] getServiceClasses()
		{
			check(esb, "ViewCmbkServiceParameters");
			return serviceClasses;
		}

		public void setServiceClasses(ServiceClass[] serviceClasses)
		{
			check(esb, "ChangeCmbkServiceParameters");
			this.serviceClasses = serviceClasses;
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
			return -5036486632276658623L;
		}

		@Override
		public String getName(String languageCode)
		{
			return serviceName.getSafe(languageCode, "Call Me Back");
		}

		public String getShortCode()
		{
			check(esb, "ViewCmbkServiceParameters");
			return shortCode;
		}

		public void setShortCode(String shortCode)
		{
			check(esb, "ChangeCmbkServiceParameters");
			this.shortCode = shortCode;
		}

		public String getSmsSourceAddress()
		{
			check(esb, "ViewCmbkServiceParameters");
			return smsSourceAddress;
		}

		public void setSmsSourceAddress(String smsSourceAddress)
		{
			check(esb, "ChangeCmbkServiceParameters");
			this.smsSourceAddress = smsSourceAddress;
		}

		@Override
		public void validate() throws ValidationException
		{
		}

		@Override
		public void performUpdateNotificationSecurityCheck()
		{
			check(esb, "ChangeCmbkServiceNotifications");
		}

		@Override
		public void performGetNotificationSecurityCheck()
		{
			check(esb, "ViewCmbkServiceNotifications");
		}

		public VasCommand[] getCommands()
		{
			check(esb, "ViewCmbkServiceParameters");
			return commands;
		}

		public void setCommands(VasCommand[] commands)
		{
			check(esb, "ChangeCmbkServiceParameters");
			this.commands = commands;
		}
	}

	CmbkServiceConfig config = new CmbkServiceConfig();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// VasService Implementation
	//
	// /////////////////////////////////

	@Override
	public String getServiceID()
	{
		return "CMBK";
	}

	@Override
	public String getServiceName(String lanuageID)
	{
		return "Call Me Back Service";
	}

	public static String getDefaultVariantID()
	{
		return "Default";
	}

	@Override
	public VasServiceInfo[] getServiceInfo(IServiceContext context, Number subscriberNumber, String variantID, Integer languageID, boolean activeOnly, boolean suggested) throws Exception
	{

		List<VasServiceInfo> result = new ArrayList<VasServiceInfo>();
		String languageCode = esb.getLocale().getLanguage(languageID);

		ISubscriber subscriber = null;
		if (subscriberNumber != null)
		{
			subscriber = getSubscriber(context, subscriberNumber, null);
		}

		try (IDatabaseConnection db = database.getConnection(null))
		{
			for (Variant variant : config.getVariants())
			{
				if (variantID != null && !variantID.equals("") && !variantID.equalsIgnoreCase(variant.getVariantID()))
					continue;

				VasServiceInfo serviceInfo = new VasServiceInfo();
				serviceInfo.setServiceID(getServiceID());
				serviceInfo.setServiceName(getServiceName(esb.getLocale().getLanguage(languageID)));
				serviceInfo.setVariantID(variant.getVariantID());
				serviceInfo.setVariantName(variant.toString(languageCode));

				if (subscriber == null)
					serviceInfo.setState(SubscriptionState.unknown);
				else if (isSubscriberActive(subscriber))
					serviceInfo.setState(SubscriptionState.active);
				else
					serviceInfo.setState(SubscriptionState.notActive);

				if (!activeOnly || serviceInfo.getState() != SubscriptionState.notActive)
					result.add(serviceInfo);
			}
		}
		catch (Exception e)
		{
			logger.log(this, e);
			throw e;
		}

		return result.toArray(new VasServiceInfo[0]);
	}

	@Override
	public boolean processCMBK(ResponseHeader response, CdrBase cdr, IDatabaseConnection db, String variantID, String subscriberNumber, String recipientNumber)
	{
		Variant variant = getVariant(variantID);
		if (variant == null)
		{
			response.exitWith(logger, cdr, ReturnCodes.malformedRequest, "Invalid HGSFKJG VariantID");
			return false;
		}

		response.exitWith(logger, cdr, ReturnCodes.success, "[%s] Valid CMBK to send to [%s].", subscriberNumber, recipientNumber);
		return true;
	}

	public boolean processCMBK(String variantID, String subscriberNumber, String recipientNumber)
	{
		Variant variant = getVariant(variantID);
		if (variant == null)
			return false;
		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	private boolean isSameNumber(String subscriberNumber, String recipientNumber)
	{
		while (subscriberNumber.indexOf("0") == 0)
		{
			subscriberNumber = subscriberNumber.substring(1, subscriberNumber.length());
		}

		while (recipientNumber.indexOf("0") == 0)
		{
			recipientNumber = recipientNumber.substring(1, recipientNumber.length());
		}

		if (subscriberNumber.length() >= recipientNumber.length())
		{
			subscriberNumber = subscriberNumber.substring(subscriberNumber.length() - recipientNumber.length(), subscriberNumber.length());
			if (recipientNumber.equals(subscriberNumber))
			{
				return true;
			}
		}
		else if (subscriberNumber.length() < recipientNumber.length())
		{
			recipientNumber = recipientNumber.substring(recipientNumber.length() - subscriberNumber.length(), recipientNumber.length());
			if (subscriberNumber.equals(recipientNumber))
			{
				return true;
			}
		}
		return false;

	}

	private boolean matchNumberPlan(String recipientNumber, Variant variant)
	{
		String testNumberPlan;
		int mandatoryLength;
		int optionalLength;
		int prefixLength;

		List<String> numberPlanList = new ArrayList<String>();
		numberPlanList = Arrays.asList(variant.getNumberPlanString().split(","));

		for (int i = 0; i < numberPlanList.size(); i++)
		{
			testNumberPlan = numberPlanList.get(i);
			// Get prefix
			prefixLength = testNumberPlan.indexOf('X');

			// Get Mandatory length (prefix + XXXXX)
			if (testNumberPlan.contains("X"))
				mandatoryLength = testNumberPlan.lastIndexOf('X') + 1;
			else
				mandatoryLength = testNumberPlan.length();

			// Get Mandatory length (prefix + XXXXX + xxx)
			if (testNumberPlan.contains("x"))
				optionalLength = testNumberPlan.lastIndexOf('x') + 1;
			else
				optionalLength = mandatoryLength;

			if (recipientNumber.length() >= mandatoryLength && recipientNumber.length() <= optionalLength
					&& testNumberPlan.substring(0, prefixLength).equals(recipientNumber.substring(0, prefixLength)))
				return true;
		}
		return false;
	}

	private List<CallMeBackServiceRecord> getCallMeBackServiceRecords(IDatabaseConnection db, String serviceID, String variantID, String subscriberMsisdn) throws SQLException
	{
		String where = "where serviceID = %s";
		int count = 0;
		Object[] params = new Object[3];
		params[count++] = serviceID;

		if (variantID != null)
		{
			where += " and variantID = %s";
			params[count++] = variantID;
		}

		if (subscriberMsisdn != null)
		{
			where += " and subscriberMsisdn = %s";
			params[count++] = subscriberMsisdn;
		}

		return db.selectList(CallMeBackServiceRecord.class, where, java.util.Arrays.copyOf(params, count));
	}

	private boolean isSubscriberActive(ISubscriber subscriber) throws ZTEException// 
	{
		hxc.connectors.zte.proxy.Subscriber sub = (hxc.connectors.zte.proxy.Subscriber) subscriber;
		Date now = new Date();

			if (expired(sub.getSupervisionExpiryDate(), now))
				return false;
	
			if (expired(sub.getServiceFeeExpiryDate(), now))
				return false;
	
			if (expired(sub.getServiceRemovalDate(), now))
				return false;
	
			Boolean flag = sub.getAccountActivatedFlag();
			if (flag != null && !flag)
				return false;
	
			flag = sub.getTemporaryBlockedFlag();
			if (flag != null && flag)
				return false;
	
			return true;
	}

	private boolean expired(Date date, Date now)
	{
		return date != null && date.before(now);
	}

	private Subscriber getSubscriber(IServiceContext context, Number subscriberNumber, ITransaction transaction)
	{
			Subscriber result;
			if (context.getSubscriberProxy() != null && context.getSubscriberProxy() instanceof Subscriber //
					&& context.getSubscriberProxy().isSameNumber(subscriberNumber.toMSISDN()))
			{
				result = (Subscriber) context.getSubscriberProxy();
				result.setTransaction(transaction);
			}
			else
			{
				result = new Subscriber(subscriberNumber.toMSISDN(), zte, logger, transaction);// zte
				context.setSubscriberProxy(result);
				return result;
			}
			return result;
	}

	private ServiceClass getServiceClass(Subscriber subscriber) throws ZTEException // ZTEException
	{
		for (ServiceClass serviceClass : config.getServiceClasses())
		{
			if (subscriber.getServiceClass() == serviceClass.getServiceClassID())
				return serviceClass;
		}
		return null;
	}

	private ServiceClass getServiceClass(int serviceClassID) throws ZTEException // ZTEException
	{
		for (ServiceClass serviceClass : config.getServiceClasses())
		{
			if (serviceClassID == serviceClass.getServiceClassID())
				return serviceClass;
		}
		return null;
	}

	private Properties getProperties(IServiceContext context)
	{
		Object properties = context.getProperties();
		if (properties == null || !(properties instanceof Properties))
		{
			properties = new Properties();
			context.setProperties(properties);
		}

		return (Properties) properties;
	}

	private String getLanguageCode(ISubscriber subscriber)
	{
		String languageCode = null;
		try
		{
			languageCode = esb.getLocale().getLanguage(subscriber.getLanguageID());
		}
		catch (Exception e)
		{
			languageCode = esb.getLocale().getDefaultLanguageCode();
			throw e;
		}
		return languageCode;
	}

	private String getNotificationText(ISubscriber subscriber, int notifcationID, Properties properties)
	{
		String languageCode = null;
		try
		{
			languageCode = esb.getLocale().getLanguage(subscriber.getLanguageID());
		}
		catch (Exception e)
		{
			languageCode = esb.getLocale().getDefaultLanguageCode();
		}
		INotificationText text = notifications.get(notifcationID, languageCode, locale, properties);
		return text == null ? null : text.getText();
	}

	private boolean chargeSubscriber(Subscriber subscriber, long charge, ServiceClass donorServiceClass, //
			Properties properties, ICdr cdr, ResponseHeader response) throws ZTEException, SQLException // ZTEException
	{
		if (charge <= 0)
		{
			logger.trace(this, "Charge is %d, omitting UpdateBalanceAndDate", charge);
			return true;
		}
		
		if (subscriber.getAccountBalance(0) < charge)
		{
			logger.trace(this, "Charge (%d) is more than Balance(%d), omitting UpdateBalanceAndDate", charge, subscriber.getAccountBalance(0));
			return false;
		}

		String chargeText = locale.formatCurrency(charge);
		logger.trace(this, "Attempt to charge %s %s", subscriber.getInternationalNumber(), chargeText);

		if (donorServiceClass.isPostPaid())
		{
			response.setReturnCode(ReturnCodes.notEligible);
		}
		else
		{
			try
			{
				subscriber.updateAccounts(-charge);
			}
			catch (ZTEException e)
			{
				logger.log(this, e);
				return false;
			}
		}

		properties.setCharge(chargeText);
		cdr.setChargeLevied((int) charge);
		response.setChargeLevied((int) charge);

		return true;
	}

	@Override
	protected ReturnCodeTexts[] getReturnCodeTexts()
	{
		return config.getReturnCodesTexts();
	}

	class Properties
	{
		protected String subscriberNumber;
		protected String subscriberClass = "0";
		protected String recipientNumber;
		protected String minBalance = "0";
		protected String maxBalance = "1000000";
		protected String charge = "0";

		public String getSubscriberNumber()
		{
			return subscriberNumber;
		}

		public void setSubscriberNumber(String subscriberNumber)
		{
			this.subscriberNumber = subscriberNumber;
		}

		public String getSubscriberClass()
		{
			return subscriberClass;
		}

		public void setSubscriberClass(String subscriberClass)
		{
			this.subscriberClass = subscriberClass;
		}

		public String getRecipientNumber()
		{
			return recipientNumber;
		}

		public void setRecipientNumber(String recipientNumber)
		{
			this.recipientNumber = recipientNumber;
		}

		public String getMinBalance()
		{
			return minBalance;
		}

		public void setMinBalance(String minBalance)
		{
			this.minBalance = minBalance;
		}

		public String getMaxBalance()
		{
			return maxBalance;
		}

		public void setMaxBalance(String maxBalance)
		{
			this.maxBalance = maxBalance;
		}

		public String getCharge()
		{
			return charge;
		}

		public void setCharge(String charge)
		{
			this.charge = charge;
		}

	}

	protected Variant getVariant(String variantID)
	{
		if (variantID == null || variantID.length() == 0)
			variantID = DEFAULT_VARIANT_ID;

		for (Variant variant : config.getVariants())
		{
			if (variantID.equalsIgnoreCase(variant.getVariantID()))
				return variant;
		}

		return null;

	}

	protected static boolean isNumeric(String pin)
	{
		String numbers = "0123456789";
		StringCharacterIterator iter = new StringCharacterIterator(pin);

		for (char ch = iter.first(); ch != StringCharacterIterator.DONE; ch = iter.next())
		{
			if (!numbers.contains(Character.toString(ch)))
			{
				return false;
			}
		}

		return true;
	}

	protected Notifications notifications = new Notifications(Properties.class);

	protected int smsOnNetRecipientCMBK = notifications.add("Recipient CMBK message to OnNet", //
			"Please call {subscriberNumber} On Net Rates apply", //
			"FR: Please call {subscriberNumber} On Net Rates apply", //
			null, null);

	protected int smsOffNetRecipientCMBK = notifications.add("Recipient CMBK message to OffNet", //
			"Please call {subscriberNumber} Off Net Rates apply", //
			"FR: Please call {subscriberNumber} Off Net Rates apply", //
			null, null);

	protected int smsAllNetRecipientCMBK = notifications.add("Recipient CMBK message to AllNet", //
			"Please call {subscriberNumber}", //
			"FR: Please call {subscriberNumber}", //
			null, null);
	
	protected int smsIntlRecipientCMBK = notifications.add("Recipient CMBK message to International", //
			"Please call {subscriberNumber} International Rates apply", //
			"FR: Please call {subscriberNumber} International Rates apply", //
			null, null);

	protected int onNetSubscriberCMBK = notifications.add("Subscriber response for CMBK to OnNet Recipient", //
			"Please call me sent to {recipientNumber} this was a On Net call", //
			"FR: Please call me sent to {recipientNumber} this was a On Net call", //
			null, null);

	protected int offNetSubscriberCMBK = notifications.add("Subscriber response for CMBK to OffNet Recipient", //
			"Please call me sent to {recipientNumber} this was a Off Net call", //
			"FR: Please call me sent to {recipientNumber} this was a Off Net call", //
			null, null);

	protected int allNetSubscriberCMBK = notifications.add("Subscriber response for CMBK to AllNet Recipient", //
			"Call Me Back SMS to {RecipientNumber} was successful.\nNo airtime, no worries. Dial *179# to get 30c, 50c or 75c Buddie airtime credit.", //
			"FR: Please call me sent to {recipientNumber}", //
			null, null);
	
	protected int intlSubscriberCMBK = notifications.add("Subscriber response for CMBK to International Recipient", //
			"Call Me Back SMS to {RecipientNumber} was successful.\nNo airtime, no worries. Dial *179# to get 30c, 50c or 75c Buddie airtime credit.", //
			"FR: Call Me Back SMS to {RecipientNumber} was successful.\nNo airtime, no worries. Dial *179# to get 30c, 50c or 75c Buddie airtime credit.", //
			null, null);
	
	protected int returnCodesInactiveAParty= notifications.add("returnCodesInactiveAParty",//
			"You are not active, please activate your service before using",//
			"FR: You are not active, please activate your service before using",//
			null, null);

	protected int returnCodesInsufficientBalance = notifications.add("returnCodesInsufficientBalance",//
			"You do not have enough Credit",//
			"FR: You do not have enough Credit",//
			null, null);
	
	protected int returnCodesNotEligible = notifications.add("returnCodesNotEligible",//
			"Your Account is not found or is not active, you may not use CMBK",//
			"FR: Your Account is not found or is not active, you may not use CMBK",//
			null, null);
	
	protected int returnCodesGenericError = notifications.add("returnCodesGenericError",//
			"Technical problem occurred",//
			"FR: Technical problem occurred",//
			null, null);
	
	protected int returnCodesOnlyPrepaid = notifications.add("returnCodesOnlyPrepaid",//
			"Only Prepaid customers may use CMBK",//
			"FR: Only Prepaid customers may use CMBK",//
			null, null);

	protected int returnCodesMemberNotEligible = notifications.add("returnCodesMemberNotEligible",//
			"You May not send CMBK to that number",//
			"FR: You May not send CMBK to that number",//
			null, null);

	protected int returnCodesCannotCallSelf = notifications.add("returnCodesCannotCallSelf",//
			"You cannot send call-me-back to yourself",//
			"FR: You cannot send call-me-back to yourself",//
			null, null);

	protected int returnCodesExcessiveBalance = notifications.add("returnCodesExcessiveBalance",//
			"You have too much Credit",//
			"FR: You have too much Credit",//
			null, null);

	protected int returnCodesMaxCountExceeded = notifications.add("returnCodesMaxCountExceeded",//
			"You have exceeded the maximum number of Call me back requests for today",//
			"FR: You have exceeded the maximum number of Call me back requests for today",//
			null, null);
}
