package hxc.services.faf;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
//import hxc.connectors.air.IAirConnector;
//TODO choose between zte and air
//import hxc.connectors.zte.*;
import hxc.connectors.zte.proxy.Subscriber;
import hxc.connectors.zte.IZTEConnector;
import hxc.connectors.zte.ZTEException;

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
import hxc.services.faf.Variant;
import hxc.services.faf.ServiceClass;
import hxc.services.faf.FriendsAndFamilyServiceRecord;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.notification.Notifications;
import hxc.utils.protocol.zte.FafInformation;
import hxc.utils.protocol.zte.FafInformation.FaFOwners;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.RemoveMemberRequest;
import com.concurrent.hxc.RemoveMemberResponse;
import com.concurrent.hxc.ReplaceMemberRequest;
import com.concurrent.hxc.ReplaceMemberResponse;
import com.concurrent.hxc.ResponseHeader;
import com.concurrent.hxc.GetMembersRequest;
import com.concurrent.hxc.GetMembersResponse;
import com.concurrent.hxc.AddMemberRequest;
import com.concurrent.hxc.AddMemberResponse;
import com.concurrent.hxc.ServiceContext;
import com.concurrent.hxc.IServiceContext;
import java.io.IOException;

public class FriendsAndFamilyService extends VasService implements IService, IFriendsAndFamilyService
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
	// protected IAirConnector air;
	protected IZTEConnector zte;
	protected ISoapConnector soapConnector;

	private static final int FAF_SUB_OK = 1;
	private static final int FAF_SUB_NOT_ACTIVE = -1;
	private static final int FAF_SUB_NO_SERVICECLASS = -2;
	private static final int FAF_SUB_NO_SUBSCRIPTION = -3;
	private static final int FAF_SUB_NO_BALANCE = -4;
	private static final int FAF_SUB_TOO_MUCH_BALANCE = -5;

	private static final int FAF_RECORD_OK = 1;
	private static final int FAF_RECORD_FAIL_READ = -1;
	private static final int FAF_RECORD_EXCEED = -2;
	private static final int FAF_RECORD_FAIL_CHARGE = -3;

	private static final int FAF_ACTION_LIST = 0;
	private static final int FAF_ACTION_ADD = 1;
	private static final int FAF_ACTION_DELETE = 2;
	private static final int FAF_ACTION_REPLACE = 3;

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

		// Must have Air / ZTE
		// air = esb.getFirstConnector(IAirConnector.class);
		// if (air == null)
		// return false;
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
		logger.info(this, "Friends And Family Service Started");

		return true;
	}

	@Override
	public void stop()
	{
		// Log Information
		logger.info(this, "Friends And Family Service Stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		this.config = (FafServiceConfig) config;
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
	public GetMembersResponse getMembers(IServiceContext context, GetMembersRequest request)
	{
		// Prepare an Response
		GetMembersResponse response = super.getMembers(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(hxc.connectors.zte.proxy.Subscriber.TRANSACTION_ID_LENGTH));

		cdr.setA_MSISDN(request.getSubscriberNumber().toMSISDN());
		cdr.setServiceID(request.getServiceID());
		cdr.setProcessID("FAFgetMembers");
		// Update CDR
		cdr.setServiceID(getServiceID());

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = GetMembersRequest.validate(request);
				if (problem != null)
					return response.exitWith(logger, cdr, ReturnCodes.malformedRequest, problem);

				// Validate Variant
				Variant variant = getVariant(request.getVariantID());
				if (variant == null)
					return response.exitWith(logger, cdr, ReturnCodes.malformedRequest, "Invalid VariantID");

				// Update CDR
				cdr.setServiceID(request.getServiceID());
				cdr.setVariantID(request.getVariantID());

				// Get subscriberNumber Proxy
				Subscriber subscriber = getSubscriber(context, request.getSubscriberNumber(), transaction);
				FriendsAndFamilyServiceRecord[] fafRecord = new FriendsAndFamilyServiceRecord[1];// For pass by reference
				ServiceClass[] subscriberServiceClass = new ServiceClass[1];// For pass by reference
				long subcriptionCost[] = new long[1];// For pass by reference
				long usageCharge[] = new long[1];// For pass by reference
				String returnText = "Not Successful LIST";

				try
				{
					Properties properties = getProperties(context);
					// Check self add
					transaction.track(this, "getMembersStart");
					this.getProperties(context).setSubscriberNumber(subscriber.getInternationalNumber());
					subscriberServiceClass[0] = getServiceClass(subscriber);

					transaction.track(this, "CheckValidServiceSubscription");
					int checkSubscription = checkSubscriberValid(subscriber, transaction, subscriberServiceClass, subcriptionCost);
					if (checkSubscription == FAF_SUB_NOT_ACTIVE)
					{
						returnText = getNotificationText(subscriber, returnCodesInactiveAParty, properties);
						cdr.setAdditionalInformation("You cannot list FAF - Your account is inactive");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.inactiveAParty, returnText);
					}

					if (checkSubscription == FAF_SUB_NO_BALANCE)
					{
						returnText = getNotificationText(subscriber, returnCodesInsufficientBalance, properties);
						cdr.setAdditionalInformation("You have too little credit to subscribe to FAF");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.insufficientBalance, returnText);
					}
					if (checkSubscription == FAF_SUB_TOO_MUCH_BALANCE)
					{
						returnText = getNotificationText(subscriber, returnCodesExcessiveBalance, properties);
						cdr.setAdditionalInformation("You have too much Credit");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.excessiveBalance, returnText);
					}

					if (checkSubscription == FAF_SUB_NO_SERVICECLASS)
					{
						returnText = getNotificationText(subscriber, returnCodesNotEligible, properties);
						cdr.setAdditionalInformation("You may not list FAF");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.notEligible, returnText);
					}

					if (checkSubscription != FAF_SUB_OK)
					{
						returnText = getNotificationText(subscriber, returnCodesInvalidService, properties);
						cdr.setAdditionalInformation("You are not subscribed to use FAF");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.invalidService, returnText);
					}

					cdr.setChargeLevied((int) subcriptionCost[0]);
					response.setChargeLevied((int) subcriptionCost[0]);
					String chargeText = locale.formatCurrency(subcriptionCost[0]);
					properties.setCharge(chargeText);

					// Test LIST variant linked to the service class
					transaction.track(this, "CheckValidNumberPlansList");
					@SuppressWarnings("static-access")
					String variantMatchedID = variant.LIST_VARIANT;

					int checkRecord = getFriendsAndFamilyRecord(db, subscriber, subscriberServiceClass[0], variantMatchedID, FAF_ACTION_LIST, fafRecord, usageCharge);
					if (checkRecord == FAF_RECORD_FAIL_READ)
						return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, "Database problem");

					if (checkRecord == FAF_RECORD_EXCEED)
					{
						Date periodEndTime = new Date(fafRecord[0].getPeriodStartTime().getTime() + (long)1 * 60 * 60 * 1000);
						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String nextUsageDate = dateFormat.format(periodEndTime);
						this.getProperties(context).setNextUsageDate(nextUsageDate);
						returnText = getNotificationText(subscriber, returnCodesMaxCountExceeded, properties);
						cdr.setAdditionalInformation("You have used FAF too much please try again later");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.maxCountExceeded, returnText);
					}
					if (checkRecord == FAF_RECORD_FAIL_CHARGE)
					{
						returnText = getNotificationText(subscriber, returnCodesInsufficientBalance, properties);
						cdr.setAdditionalInformation("You have too little credit to use FAF");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.insufficientBalance, returnText);
					}

					long chargeTotal = subcriptionCost[0] + usageCharge[0];
					cdr.setChargeLevied((int) chargeTotal);
					response.setChargeLevied((int) chargeTotal);
					String chargeTotalText = locale.formatCurrency(chargeTotal);
					properties.setCharge(chargeTotalText);

					// get FAF list
					StringBuilder fafListSB = new StringBuilder();
					FafInformation[] fafInformation = subscriber.getFafList();

					if (fafInformation.length < 1)
						fafListSB.append("Empty");
					else
					{
						for (int i = 0; i < fafInformation.length; i++)
						{
							fafListSB.append(fafInformation[i].fafNumber);
							fafListSB.append('\n');
						}
					}
					properties.setFafList(fafListSB.toString());
					// Set the Result Text
					if (chargeTotal > 0L)
					{
						cdr.setAdditionalInformation("Your FAF List was Successful with charge");
						if (fafInformation.length < 1)
							returnText = getNotificationText(subscriber, subscriberFAFlistEmptyCharge, properties);
						else
							returnText = getNotificationText(subscriber, subscriberFAFlistCharge, properties);
					}
					else
					{
						cdr.setAdditionalInformation("Your FAF List was Successful free of charge");
						if (fafInformation.length < 1)
							returnText = getNotificationText(subscriber, subscriberFAFlistEmptyFree, properties);
						else
							returnText = getNotificationText(subscriber, subscriberFAFlistFree, properties);
					}
				}

				catch (SQLException e)
				{
					return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, e);
				}

				if (fafRecord[0].isNew)
					db.insert(fafRecord[0]);
				else
					db.update(fafRecord[0]);

				// Complete
				transaction.complete();
				context.setResultText(returnText);
				return response.exitWith(logger, cdr, ReturnCodes.success, returnText);
			}
		}
		catch (ZTEException ex)
		{
			if (ex.getResponseCode() == ZTEException.SUBSCRIBER_FAILURE || ex.getResponseCode() == ZTEException.AUTHORIZATION_FAILURE)
			{
				{
					String returnText = getNotificationText(null, returnCodesNotEligible, null);
					context.setResultText(returnText);
					cdr.setAdditionalInformation("You may not use CMBK");
					return response.exitWith(logger, cdr, ReturnCodes.notEligible, returnText);
				}
			}
			return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, ex);
		}
		catch (Throwable e)
		{
			return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, e);
		}
	}

	@Override
	public AddMemberResponse addMember(IServiceContext context, AddMemberRequest request)
	{
		// Prepare an Response
		AddMemberResponse response = super.addMember(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(hxc.connectors.air.proxy.Subscriber.TRANSACTION_ID_LENGTH));

		cdr.setA_MSISDN(request.getSubscriberNumber().toMSISDN());
		cdr.setB_MSISDN(request.getMemberNumber().toMSISDN());
		cdr.setServiceID(request.getServiceID());
		cdr.setProcessID("FAFaddMember");
		// Update CDR
		cdr.setServiceID(getServiceID());

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = AddMemberRequest.validate(request);
				if (problem != null)
					return response.exitWith(logger, cdr, ReturnCodes.malformedRequest, problem);

				// Validate Variant
				Variant variant = getVariant(request.getVariantID());
				if (variant == null)
					return response.exitWith(logger, cdr, ReturnCodes.malformedRequest, "Invalid VariantID");

				// Update CDR
				cdr.setServiceID(request.getServiceID());
				cdr.setVariantID(request.getVariantID());

				// Get subscriberNumber Proxy
				Subscriber subscriber = getSubscriber(context, request.getSubscriberNumber(), transaction);
				String languageCode = getLanguageCode(subscriber);
				FriendsAndFamilyServiceRecord[] fafRecord = new FriendsAndFamilyServiceRecord[1];// For pass by reference
				ServiceClass[] subscriberServiceClass = new ServiceClass[1];// For pass by reference
				long subcriptionCost[] = new long[1];// For pass by reference
				long usageCharge[] = new long[1];// For pass by reference
				String returnText = "Not Successful ADD";

				try
				{
					Properties properties = getProperties(context);
					this.getProperties(context).setSubscriberNumber(subscriber.getInternationalNumber());
					this.getProperties(context).setRecipientNumber(request.getMemberNumber().toString());
					String recipientMSISDN = request.getMemberNumber().toString();
					//remove leading zeroes 
					while (recipientMSISDN.indexOf("0") == 0)
					{
						recipientMSISDN = recipientMSISDN.substring(1, recipientMSISDN.length());
					}
					recipientMSISDN = numberPlan.getNationalFormat(recipientMSISDN);

					// Check self add
					transaction.track(this, "CheckSelfadd");
					if (isSameNumber(subscriber.getNationalNumber(), recipientMSISDN))
					{
						returnText = getNotificationText(subscriber, returnCodesCannotCallSelf, properties);
						cdr.setAdditionalInformation("You cannot add Yourself to your FAF list");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.cannotCallSelf, returnText);
					}

					subscriberServiceClass[0] = getServiceClass(subscriber);

					transaction.track(this, "CheckValidServiceSubscription");
					int checkSubscription = checkSubscriberValid(subscriber, transaction, subscriberServiceClass, subcriptionCost);
					if (checkSubscription == FAF_SUB_NOT_ACTIVE)
					{
						returnText = getNotificationText(subscriber, returnCodesInactiveAParty, properties);
						cdr.setAdditionalInformation("You cannot list FAF - Your account is inactive");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.inactiveAParty, returnText);
					}

					if (checkSubscription == FAF_SUB_NO_BALANCE)
					{
						returnText = getNotificationText(subscriber, returnCodesInsufficientBalance, properties);
						cdr.setAdditionalInformation("You have too little credit to subscribe to FAF");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.insufficientBalance, returnText);
					}
					if (checkSubscription == FAF_SUB_TOO_MUCH_BALANCE)
					{
						returnText = getNotificationText(subscriber, returnCodesExcessiveBalance, properties);
						cdr.setAdditionalInformation("You have too much Credit");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.excessiveBalance, returnText);
					}

					if (checkSubscription == FAF_SUB_NO_SERVICECLASS)
					{
						returnText = getNotificationText(subscriber, returnCodesNotEligible, properties);
						cdr.setAdditionalInformation("You may not list FAF");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.notEligible, returnText);
					}

					if (checkSubscription != FAF_SUB_OK)
					{
						returnText = getNotificationText(subscriber, returnCodesInvalidService, properties);
						cdr.setAdditionalInformation("You are not subscribed to use FAF");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.invalidService, returnText);
					}

					cdr.setChargeLevied((int) subcriptionCost[0]);
					response.setChargeLevied((int) subcriptionCost[0]);
					String chargeText = locale.formatCurrency(subcriptionCost[0]);
					properties.setCharge(chargeText);

					// get FAF list - see if the new number is already there
					FafInformation[] fafInformation = subscriber.getFafList();

					transaction.track(this, "CheckCurrentFaFList");
					if (fafInformation.length >= subscriberServiceClass[0].getMaxListSize())
					{
						returnText = getNotificationText(subscriber, returnCodesMaxMembersExceeded, properties);
						cdr.setAdditionalInformation("Your FAF list max members exceeded");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.maxMembersExceeded, returnText);
					}
					for (int i = 0; i < fafInformation.length; i++)
					{
						if (isSameNumber(recipientMSISDN, fafInformation[i].fafNumber))
						{
							returnText = getNotificationText(subscriber, returnCodesAlreadyMember, properties);
							cdr.setAdditionalInformation("New number already on your FAF list");
							context.setResultText(returnText);
							return response.exitWith(logger, cdr, ReturnCodes.alreadyMember, returnText);
						}
					}

					// Test recipientNumber is in one of the allowed variants
					// linked to the service class
					transaction.track(this, "CheckValidNumberPlans");
					String variantMatchedID = null;

					List<String> variantList = new ArrayList<String>();
					variantList = Arrays.asList(subscriberServiceClass[0].getVariantString().split(","));

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
						cdr.setAdditionalInformation("You cannot add that Number to your FAF list");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.memberNotEligible, returnText);
					}

					int checkRecord = getFriendsAndFamilyRecord(db, subscriber, subscriberServiceClass[0], variantMatchedID, FAF_ACTION_ADD, fafRecord, usageCharge);
					if (checkRecord == FAF_RECORD_FAIL_READ)
						return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, "Database problem");

					if (checkRecord == FAF_RECORD_EXCEED)
					{
						Date periodEndTime = new Date(fafRecord[0].getPeriodStartTime().getTime() + (long)28 * 24 * 60 * 60 * 1000);
						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						String nextUsageDate = dateFormat.format(periodEndTime);
						this.getProperties(context).setNextUsageDate(nextUsageDate);

						returnText = getNotificationText(subscriber, returnCodesMaxCountExceeded, properties);
						cdr.setAdditionalInformation("You have used FAF too much please try again later");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.maxCountExceeded, returnText);
					}
					if (checkRecord == FAF_RECORD_FAIL_CHARGE)
					{
						returnText = getNotificationText(subscriber, returnCodesInsufficientBalance, properties);
						cdr.setAdditionalInformation("You have too little credit to use FAF");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.insufficientBalance, returnText);
					}

					long chargeTotal = subcriptionCost[0] + usageCharge[0];
					cdr.setChargeLevied((int) chargeTotal);
					response.setChargeLevied((int) chargeTotal);
					String chargeTotalText = locale.formatCurrency(chargeTotal);
					properties.setCharge(chargeTotalText);

					FafInformation fafAddInfo = new FafInformation();
					fafAddInfo.fafNumber = recipientMSISDN;
					fafAddInfo.owner = FaFOwners.Subscriber;

					if (!subscriber.addFafList(fafAddInfo))
					{
						returnText = getNotificationText(subscriber, returnCodesMemberNotEligible, properties);
						cdr.setAdditionalInformation("Your FAF Add System Error Max List Size or Not allowed");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.memberNotEligible, returnText);
					}
						
					// Send a configurable SMS to the Recipient to inform him of
					// the successful recipient addition, with an invitation to
					if (config.smsToNewlyAddedFaf == true)
					{
						transaction.track(this, "SendRecipientSMS");

						if (variant.getVariantType().compareTo(Variant.LOCAL_ONNET_VARIANT) == 0)
						{
							INotificationText text = notifications.get(smsOnNetRecipientFAFadd, languageCode, locale, properties);
							if (text != null && text.getText() != null && text.getText().length() > 0)
								smsConnector.send(subscriber.getNationalNumber(), numberPlan.getInternationalFormat(recipientMSISDN), text);
						}

						else if (variant.getVariantType().compareTo(Variant.LOCAL_OFFNET_VARIANT) == 0)
						{
							INotificationText text = notifications.get(smsOffNetRecipientFAFadd, languageCode, locale, properties);
							if (text != null && text.getText() != null && text.getText().length() > 0)
								smsConnector.send(subscriber.getNationalNumber(), numberPlan.getInternationalFormat(recipientMSISDN), text);
						}

						else if (variant.getVariantType().compareTo(Variant.LOCAL_ALLNET_VARIANT) == 0)
						{
							INotificationText text = notifications.get(smsAllNetRecipientFAFadd, languageCode, locale, properties);
							if (text != null && text.getText() != null && text.getText().length() > 0)
								smsConnector.send(subscriber.getNationalNumber(), numberPlan.getInternationalFormat(recipientMSISDN), text);
						}

						else if (variant.getVariantType().compareTo(Variant.INTERNATIONAL_VARIANT) == 0)
						{
							INotificationText text = notifications.get(smsIntlRecipientFAFadd, languageCode, locale, properties);
							if (text != null && text.getText() != null && text.getText().length() > 0)
								smsConnector.send(subscriber.getNationalNumber(), numberPlan.getInternationalFormat(recipientMSISDN), text);
						}
						else
						{
							// INVALID TYPE
							return response.exitWith(logger, cdr, ReturnCodes.memberNotEligible, "Invalid Variant Type");
						}
					}

					// Set the Result Text
					if (chargeTotal > 0L)
					{
						cdr.setAdditionalInformation("Your FAF Add was Successful with charge");
						returnText = getNotificationText(subscriber, subscriberFAFaddCharge, properties);
					}
					else
					{
						cdr.setAdditionalInformation("Your FAF Add was Successful free of charge");
						returnText = getNotificationText(subscriber, subscriberFAFaddFree, properties);
					}
				}

				catch (SQLException e)
				{
					return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, e);
				}

				if (fafRecord[0].isNew)
					db.insert(fafRecord[0]);
				else
					db.update(fafRecord[0]);

				// Complete
				transaction.complete();
				context.setResultText(returnText);
				return response.exitWith(logger, cdr, ReturnCodes.success, returnText);
			}
		}
		catch (ZTEException ex)
		{
			if (ex.getResponseCode() == ZTEException.SUBSCRIBER_FAILURE || ex.getResponseCode() == ZTEException.AUTHORIZATION_FAILURE)
			{
				{
					String returnText = getNotificationText(null, returnCodesNotEligible, null);
					context.setResultText(returnText);
					cdr.setAdditionalInformation("You may not use CMBK");
					return response.exitWith(logger, cdr, ReturnCodes.notEligible, returnText);
				}
			}
			return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, ex);
		}
		catch (Throwable e)
		{
			return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, e);
		}
	}

	@Override
	public RemoveMemberResponse removeMember(IServiceContext context, RemoveMemberRequest request)
	{
		// Prepare an Response
		RemoveMemberResponse response = super.removeMember(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(hxc.connectors.air.proxy.Subscriber.TRANSACTION_ID_LENGTH));

		cdr.setA_MSISDN(request.getSubscriberNumber().toMSISDN());
		cdr.setB_MSISDN(request.getMemberNumber().toMSISDN());
		cdr.setServiceID(request.getServiceID());
		cdr.setProcessID("FAFremoveMember");
		// Update CDR
		cdr.setServiceID(getServiceID());

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = RemoveMemberRequest.validate(request);
				if (problem != null)
					return response.exitWith(logger, cdr, ReturnCodes.malformedRequest, problem);

				// Validate Variant
				Variant variant = getVariant(request.getVariantID());
				if (variant == null)
					return response.exitWith(logger, cdr, ReturnCodes.malformedRequest, "Invalid VariantID");

				// Update CDR
				cdr.setServiceID(request.getServiceID());
				cdr.setVariantID(request.getVariantID());

				// Get subscriberNumber Proxy
				Subscriber subscriber = getSubscriber(context, request.getSubscriberNumber(), transaction);
				FriendsAndFamilyServiceRecord[] fafRecord = new FriendsAndFamilyServiceRecord[1];// For pass by reference
				ServiceClass[] subscriberServiceClass = new ServiceClass[1];// For pass by reference
				long subcriptionCost[] = new long[1];// For pass by reference
				long usageCharge[] = new long[1];// For pass by reference
				String returnText = "Not Successful";

				try
				{
					Properties properties = getProperties(context);
					this.getProperties(context).setSubscriberNumber(subscriber.getInternationalNumber());
					this.getProperties(context).setRecipientNumber(request.getMemberNumber().toString());
					String recipientMSISDN = request.getMemberNumber().toString();
					//remove leading zeroes 
					while (recipientMSISDN.indexOf("0") == 0)
					{
						recipientMSISDN = recipientMSISDN.substring(1, recipientMSISDN.length());
					}
					recipientMSISDN = numberPlan.getNationalFormat(recipientMSISDN);

					subscriberServiceClass[0] = getServiceClass(subscriber);

					transaction.track(this, "CheckValidServiceSubscription");
					int checkSubscription = checkSubscriberValid(subscriber, transaction, subscriberServiceClass, subcriptionCost);
					if (checkSubscription == FAF_SUB_NOT_ACTIVE)
					{
						returnText = getNotificationText(subscriber, returnCodesInactiveAParty, properties);
						cdr.setAdditionalInformation("You cannot list FAF - Your account is inactive");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.inactiveAParty, returnText);
					}

					if (checkSubscription == FAF_SUB_NO_BALANCE)
					{
						returnText = getNotificationText(subscriber, returnCodesInsufficientBalance, properties);
						cdr.setAdditionalInformation("You have too little credit to subscribe to FAF");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.insufficientBalance, returnText);
					}
					if (checkSubscription == FAF_SUB_TOO_MUCH_BALANCE)
					{
						returnText = getNotificationText(subscriber, returnCodesExcessiveBalance, properties);
						cdr.setAdditionalInformation("You have too much Credit");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.excessiveBalance, returnText);
					}

					if (checkSubscription == FAF_SUB_NO_SERVICECLASS)
					{
						returnText = getNotificationText(subscriber, returnCodesNotEligible, properties);
						cdr.setAdditionalInformation("You may not list FAF");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.notEligible, returnText);
					}

					if (checkSubscription != FAF_SUB_OK)
					{
						returnText = getNotificationText(subscriber, returnCodesInvalidService, properties);
						cdr.setAdditionalInformation("You are not subscribed to use FAF");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.invalidService, returnText);
					}

					cdr.setChargeLevied((int) subcriptionCost[0]);
					response.setChargeLevied((int) subcriptionCost[0]);
					String chargeText = locale.formatCurrency(subcriptionCost[0]);
					properties.setCharge(chargeText);

					// Test recipientNumber is in the faflist
					FafInformation[] fafInformation = subscriber.getFafList();
					boolean haveFafMatch = false;

					for (int i = 0; i < fafInformation.length; i++)
					{
						if (isSameNumber(recipientMSISDN, fafInformation[i].fafNumber))
						{
							haveFafMatch = true; 
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(fafInformation[i].startDate);
							calendar.add(Calendar.DATE, config.allowDelDays);//The number of Days a number must STAY on FaF list
							if (config.allowDelDays > 0 && calendar.getTime().after(new Date()))
							{
								DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
								String nextUsageDate = dateFormat.format(calendar.getTime());
								this.getProperties(context).setNextUsageDate(nextUsageDate);

								returnText = getNotificationText(subscriber, returnCodesRecipientNotRemove, properties);
								cdr.setAdditionalInformation("You can only remove that number later");
								context.setResultText(returnText);
								return response.exitWith(logger, cdr, ReturnCodes.maxCountExceeded, returnText);
							}
						}
					}
					if (!haveFafMatch)
					{
						returnText = getNotificationText(subscriber, returnCodesNotMember, properties);
						cdr.setAdditionalInformation("Number to remove not on your FAF list");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.notMember, returnText);
					}

					String variantMatchedID = null;

					List<String> variantList = new ArrayList<String>();
					variantList = Arrays.asList(subscriberServiceClass[0].getVariantString().split(","));

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
						cdr.setAdditionalInformation("You cannot remove that Number to your FAF list");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.memberNotEligible, returnText);
					}

					int checkRecord = getFriendsAndFamilyRecord(db, subscriber, subscriberServiceClass[0], variantMatchedID, FAF_ACTION_DELETE, fafRecord, usageCharge);
					if (checkRecord == FAF_RECORD_FAIL_READ)
						return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, "Database problem");

					if (checkRecord == FAF_RECORD_EXCEED)
					{
						Date periodEndTime = new Date(fafRecord[0].getPeriodStartTime().getTime() + (long)28 * 24 * 60 * 60 * 1000);
						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						String nextUsageDate = dateFormat.format(periodEndTime);
						this.getProperties(context).setNextUsageDate(nextUsageDate);

						returnText = getNotificationText(subscriber, returnCodesMaxCountExceeded, properties);
						cdr.setAdditionalInformation("You have used FAF too much please try again later");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.maxCountExceeded, returnText);
					}
					if (checkRecord == FAF_RECORD_FAIL_CHARGE)
					{
						returnText = getNotificationText(subscriber, returnCodesInsufficientBalance, properties);
						cdr.setAdditionalInformation("You have too little credit to use FAF please top up");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.insufficientBalance, returnText);
					}

					long chargeTotal = subcriptionCost[0] + usageCharge[0];
					cdr.setChargeLevied((int) chargeTotal);
					response.setChargeLevied((int) chargeTotal);
					String chargeTotalText = locale.formatCurrency(chargeTotal);
					properties.setCharge(chargeTotalText);

					FafInformation fafDelInfo = new FafInformation();
					fafDelInfo.fafNumber = recipientMSISDN;
					fafDelInfo.owner = FaFOwners.Subscriber;

					if (!subscriber.delFafList(fafDelInfo))
					{
						returnText = getNotificationText(subscriber, returnCodesMemberNotEligible, properties);
						cdr.setAdditionalInformation("You cannot remove that Number to your FAF list");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.memberNotEligible, returnText);
					}

					if (chargeTotal > 0L)
					{
						cdr.setAdditionalInformation("Your FAF Del was Successful with charge");
						returnText = getNotificationText(subscriber, subscriberFAFdelCharge, properties);
					}
					else
					{
						cdr.setAdditionalInformation("Your FAF Del was Successful free of charge");
						returnText = getNotificationText(subscriber, subscriberFAFdelFree, properties);
					}
				}

				catch (SQLException e)
				{
					return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, e);
				}

				if (fafRecord[0].isNew)
					db.insert(fafRecord[0]);
				else
					db.update(fafRecord[0]);

				// Complete
				transaction.complete();
				context.setResultText(returnText);
				return response.exitWith(logger, cdr, ReturnCodes.success, returnText);
			}
		}
		catch (ZTEException ex)
		{
			if (ex.getResponseCode() == ZTEException.SUBSCRIBER_FAILURE || ex.getResponseCode() == ZTEException.AUTHORIZATION_FAILURE)
			{
				{
					String returnText = getNotificationText(null, returnCodesNotEligible, null);
					context.setResultText(returnText);
					cdr.setAdditionalInformation("You may not use CMBK");
					return response.exitWith(logger, cdr, ReturnCodes.notEligible, returnText);
				}
			}
			return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, ex);
		}
		catch (Throwable e)
		{
			return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, e);
		}
	}

	@Override
	public ReplaceMemberResponse replaceMember(IServiceContext context, ReplaceMemberRequest request)
	{
		// Prepare an Response
		ReplaceMemberResponse response = super.replaceMember(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(hxc.connectors.air.proxy.Subscriber.TRANSACTION_ID_LENGTH));

		cdr.setA_MSISDN(request.getSubscriberNumber().toMSISDN());
		cdr.setB_MSISDN(request.getNewMemberNumber().toMSISDN());
		cdr.setServiceID(request.getServiceID());
		cdr.setProcessID("FAFreplaceMember");
		// Update CDR
		cdr.setServiceID(getServiceID());

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = ReplaceMemberRequest.validate(request);
				if (problem != null)
					return response.exitWith(logger, cdr, ReturnCodes.malformedRequest, problem);

				// Validate Variant
				Variant variant = getVariant(request.getVariantID());
				if (variant == null)
					return response.exitWith(logger, cdr, ReturnCodes.malformedRequest, "Invalid VariantID");

				// Update CDR
				cdr.setServiceID(request.getServiceID());
				cdr.setVariantID(request.getVariantID());

				// Get subscriberNumber Proxy
				Subscriber subscriber = getSubscriber(context, request.getSubscriberNumber(), transaction);
				String languageCode = getLanguageCode(subscriber);
				FriendsAndFamilyServiceRecord[] fafDelRecord = new FriendsAndFamilyServiceRecord[1];// For pass by reference
				FriendsAndFamilyServiceRecord[] fafAddRecord = new FriendsAndFamilyServiceRecord[1];// For pass by reference
				long subcriptionCost[] = new long[1];// For pass by reference
				long usageCharge[] = new long[1];// For pass by reference
				ServiceClass[] subscriberServiceClass = new ServiceClass[1];// For pass by reference
				String returnText = "Not Successful";

				try
				{
					Properties properties = getProperties(context);
					this.getProperties(context).setSubscriberNumber(subscriber.getInternationalNumber());
					String newMemberNumber = request.getNewMemberNumber().toString();
					String oldMemberNumber = request.getOldMemberNumber().toString();
					this.getProperties(context).setRecipientNumber(newMemberNumber);
					this.getProperties(context).setModifyNumber(oldMemberNumber);
					subscriberServiceClass[0] = getServiceClass(subscriber);

					transaction.track(this, "CheckValidServiceSubscription");
					int checkSubscription = checkSubscriberValid(subscriber, transaction, subscriberServiceClass, subcriptionCost);
					if (checkSubscription == FAF_SUB_NOT_ACTIVE)
					{
						returnText = getNotificationText(subscriber, returnCodesInactiveAParty, properties);
						cdr.setAdditionalInformation("You cannot list FAF - Your account is inactive");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.inactiveAParty, returnText);
					}

					if (checkSubscription == FAF_SUB_NO_BALANCE)
					{
						returnText = getNotificationText(subscriber, returnCodesInsufficientBalance, properties);
						cdr.setAdditionalInformation("You have too little credit to subscribe to FAF");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.insufficientBalance, returnText);
					}
					if (checkSubscription == FAF_SUB_TOO_MUCH_BALANCE)
					{
						returnText = getNotificationText(subscriber, returnCodesExcessiveBalance, properties);
						cdr.setAdditionalInformation("You have too much Credit");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.excessiveBalance, returnText);
					}

					if (checkSubscription == FAF_SUB_NO_SERVICECLASS)
					{
						returnText = getNotificationText(subscriber, returnCodesNotEligible, properties);
						cdr.setAdditionalInformation("You may not list FAF");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.notEligible, returnText);
					}

					if (checkSubscription != FAF_SUB_OK)
					{
						returnText = getNotificationText(subscriber, returnCodesInvalidService, properties);
						cdr.setAdditionalInformation("You are not subscribed to use FAF");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.invalidService, returnText);
					}

					cdr.setChargeLevied((int) subcriptionCost[0]);
					response.setChargeLevied((int) subcriptionCost[0]);
					String chargeText = locale.formatCurrency(subcriptionCost[0]);
					properties.setCharge(chargeText);

					// Test getOldMemberNumber is in the faflist
					FafInformation[] fafInformation = subscriber.getFafList();
					boolean haveFafMatch = false;

					for (int i = 0; i < fafInformation.length; i++)
					{
						if (isSameNumber(oldMemberNumber, fafInformation[i].fafNumber))
							haveFafMatch = true;
					}
					if (!haveFafMatch)
					{
						returnText = getNotificationText(subscriber, returnCodesNotMember, properties);
						cdr.setAdditionalInformation("Number to remove not on your FAF list");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.notMember, returnText);
					}

					// get FAF list - see if the new number is already there
					for (int i = 0; i < fafInformation.length; i++)
					{
						if (isSameNumber(newMemberNumber, fafInformation[i].fafNumber))
						{
							returnText = getNotificationText(subscriber, returnCodesAlreadyMember, properties);
							cdr.setAdditionalInformation("New number already on your FAF list");
							context.setResultText(returnText);
							return response.exitWith(logger, cdr, ReturnCodes.alreadyMember, returnText);
						}
					}

					// Test getNewMemberNumber is in one of the allowed variants
					// linked to the service class
					transaction.track(this, "CheckValidNumberPlans");
					String variantMatchedID = null;

					// REPLACE has two parts, DELETE and ADD - check them separately for usage
					List<String> variantList = new ArrayList<String>();
					variantList = Arrays.asList(subscriberServiceClass[0].getVariantString().split(","));
					long chargeTotal = subcriptionCost[0];

					// REPLACE has two parts, DELETE and ADD - check DELETE 1st
					for (int i = 0; i < variantList.size(); i++)
					{
						variant = getVariant(variantList.get(i));
						if (matchNumberPlan(oldMemberNumber, variant))
						{
							variantMatchedID = variant.getVariantID();
							break; // we have a Match!!!
						}
					}
					if (variantMatchedID == null)
					{
						returnText = getNotificationText(subscriber, returnCodesMemberNotEligible, properties);
						cdr.setAdditionalInformation("You cannot remove that Number to your FAF list");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.memberNotEligible, returnText);
					}

					int checkRecord = getFriendsAndFamilyRecord(db, subscriber, subscriberServiceClass[0], variantMatchedID, FAF_ACTION_DELETE, fafDelRecord, usageCharge);
					if (checkRecord == FAF_RECORD_FAIL_READ)
						return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, "Database problem");

					if (checkRecord == FAF_RECORD_EXCEED)
					{
						Date periodEndTime = new Date(fafDelRecord[0].getPeriodStartTime().getTime() + (long)28 * 24 * 60 * 60 * 1000);
						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						String nextUsageDate = dateFormat.format(periodEndTime);
						this.getProperties(context).setNextUsageDate(nextUsageDate);

						returnText = getNotificationText(subscriber, returnCodesMaxCountExceeded, properties);
						cdr.setAdditionalInformation("You have used FAF too much please try again later");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.maxCountExceeded, returnText);
					}
					if (checkRecord == FAF_RECORD_FAIL_CHARGE)
					{
						returnText = getNotificationText(subscriber, returnCodesInsufficientBalance, properties);
						cdr.setAdditionalInformation("You have too little credit to use FAF");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.insufficientBalance, returnText);
					}
					chargeTotal += usageCharge[0];

					for (int i = 0; i < variantList.size(); i++)
					{
						variant = getVariant(variantList.get(i));
						if (matchNumberPlan(newMemberNumber, variant))
						{
							variantMatchedID = variant.getVariantID();
							break; // we have a Match!!!
						}
					}
					if (variantMatchedID == null)
					{
						returnText = getNotificationText(subscriber, returnCodesMemberNotEligible, properties);
						cdr.setAdditionalInformation("You cannot add that Number to your FAF list");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.memberNotEligible, returnText);
					}

					checkRecord = getFriendsAndFamilyRecord(db, subscriber, subscriberServiceClass[0], variantMatchedID, FAF_ACTION_ADD, fafAddRecord, usageCharge);
					if (checkRecord == FAF_RECORD_FAIL_READ)
						return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, "Database problem");

					if (checkRecord == FAF_RECORD_EXCEED)
					{
						Date periodEndTime = new Date(fafAddRecord[0].getPeriodStartTime().getTime() + (long)28 * 24 * 60 * 60 * 1000);
						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						String nextUsageDate = dateFormat.format(periodEndTime);
						this.getProperties(context).setNextUsageDate(nextUsageDate);

						returnText = getNotificationText(subscriber, returnCodesMaxCountExceeded, properties);
						cdr.setAdditionalInformation("You have used FAF too much please try again later");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.maxCountExceeded, returnText);
					}
					if (checkRecord == FAF_RECORD_FAIL_CHARGE)
					{
						returnText = getNotificationText(subscriber, returnCodesInsufficientBalance, properties);
						cdr.setAdditionalInformation("You have too little credit to use FAF");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.insufficientBalance, returnText);
					}
					chargeTotal += usageCharge[0];

					cdr.setChargeLevied((int) (chargeTotal));
					response.setChargeLevied((int) (chargeTotal));
					String chargeTotalText = locale.formatCurrency(chargeTotal);
					properties.setCharge(chargeTotalText);

					FafInformation fafAddInfo = new FafInformation();
					fafAddInfo.fafNumber = newMemberNumber;
					fafAddInfo.owner = FaFOwners.Subscriber;

					FafInformation fafDelInfo = new FafInformation();
					fafDelInfo.fafNumber = oldMemberNumber;
					fafDelInfo.owner = FaFOwners.Subscriber;

					if (!subscriber.replaceFafList(fafDelInfo, fafAddInfo))
					{
						returnText = getNotificationText(subscriber, returnCodesMemberNotEligible, properties);
						cdr.setAdditionalInformation("You cannot replace that Number to your FAF list");
						context.setResultText(returnText);
						return response.exitWith(logger, cdr, ReturnCodes.memberNotEligible, returnText);
					}
					
					// Send a configurable SMS to the Recipient to inform him of
					// the successful recipient addition, with an invitation to
					if (config.smsToNewlyAddedFaf == true)
					{
						transaction.track(this, "SendRecipientSMS");

						if (variant.getVariantType().compareTo(Variant.LOCAL_ONNET_VARIANT) == 0)
						{
							INotificationText text = notifications.get(smsOnNetRecipientFAFadd, languageCode, locale, properties);
							if (text != null && text.getText() != null && text.getText().length() > 0)
								smsConnector.send(subscriber.getNationalNumber(), numberPlan.getInternationalFormat(newMemberNumber), text);
						}

						else if (variant.getVariantType().compareTo(Variant.LOCAL_OFFNET_VARIANT) == 0)
						{
							INotificationText text = notifications.get(smsOffNetRecipientFAFadd, languageCode, locale, properties);
							if (text != null && text.getText() != null && text.getText().length() > 0)
								smsConnector.send(subscriber.getNationalNumber(), numberPlan.getInternationalFormat(newMemberNumber), text);
						}

						else if (variant.getVariantType().compareTo(Variant.LOCAL_ALLNET_VARIANT) == 0)
						{
							INotificationText text = notifications.get(smsAllNetRecipientFAFadd, languageCode, locale, properties);
							if (text != null && text.getText() != null && text.getText().length() > 0)
								smsConnector.send(subscriber.getNationalNumber(), numberPlan.getInternationalFormat(newMemberNumber), text);
						}

						else if (variant.getVariantType().compareTo(Variant.INTERNATIONAL_VARIANT) == 0)
						{
							INotificationText text = notifications.get(smsIntlRecipientFAFadd, languageCode, locale, properties);
							if (text != null && text.getText() != null && text.getText().length() > 0)
								smsConnector.send(subscriber.getNationalNumber(), numberPlan.getInternationalFormat(newMemberNumber), text);
						}
						else
						{
							// INVALID TYPE
							return response.exitWith(logger, cdr, ReturnCodes.memberNotEligible, "Invalid Variant Type");
						}
					}
					if (chargeTotal > 0L)
					{
						cdr.setAdditionalInformation("Your FAF Replace was Successful with charge");
						returnText = getNotificationText(subscriber, subscriberFAFreplaceCharge, properties);
					}
					else
					{
						cdr.setAdditionalInformation("Your FAF Replace was Successful free of charge");
						returnText = getNotificationText(subscriber, subscriberFAFreplaceFree, properties);
					}
				}

				catch (SQLException e)
				{
					return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, e);
				}

				if (fafDelRecord[0].isNew)
					db.insert(fafDelRecord[0]);
				else
					db.update(fafDelRecord[0]);

				if (fafAddRecord[0].isNew)
					db.insert(fafAddRecord[0]);
				else
					db.update(fafAddRecord[0]);

				// Complete
				transaction.complete();
				context.setResultText(returnText);
				return response.exitWith(logger, cdr, ReturnCodes.success, returnText);
			}
		}
		catch (ZTEException ex)
		{
			if (ex.getResponseCode() == ZTEException.SUBSCRIBER_FAILURE || ex.getResponseCode() == ZTEException.AUTHORIZATION_FAILURE)
			{
				{
					String returnText = getNotificationText(null, returnCodesNotEligible, null);
					context.setResultText(returnText);
					cdr.setAdditionalInformation("You may not use CMBK");
					return response.exitWith(logger, cdr, ReturnCodes.notEligible, returnText);
				}
			}
			return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, ex);
		}
		catch (Throwable e)
		{
			return response.exitWith(logger, cdr, ReturnCodes.technicalProblem, e);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public FriendsAndFamilyService()
	{
		super();
		super.commandParser = new VasCommandParser(this, "RecipientMSISDN", "ModifyMSISDN")
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
						arguments.memberNumber = arguments.newMemberNumber = arguments.recipientNumber = new Number(value);
						return true;
					case "ModifyMSISDN":
						arguments.oldMemberNumber = new Number(value);
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
	@Perms(perms = { @Perm(name = "ViewFafServiceParameters", description = "View FafService Parameters", category = "FafService", supplier = true),
			@Perm(name = "ChangeFafServiceParameters", implies = "ViewFafServiceParameters", description = "Change FafService Parameters", category = "FafService", supplier = true),
			@Perm(name = "ViewFafServiceNotifications", description = "View PIN Service Notifications", category = "FafService", supplier = true),
			@Perm(name = "ChangeFafServiceNotifications", implies = "ViewFafServiceNotifications", description = "Change PIN Service Notifications", category = "FafService", supplier = true) })

	public class FafServiceConfig extends ConfigurationBase
	{
		protected String shortCode = "301";
		protected String smsSourceAddress = "301";
		protected int serviceSubscriptionID = 16;
		protected int allowDelDays = 0;
		protected boolean serviceAutoSubscribe = true;
		protected boolean smsToNewlyAddedFaf = false;

		protected Phrase serviceName = Phrase.en("Friends And Family").fre("Amis et Famille");

		// Service Specific error Mapping
		protected ReturnCodeTexts[] returnCodesTexts = new ReturnCodeTexts[] { //
//				new ReturnCodeTexts(ReturnCodes.inactiveAParty, Phrase.en("Your Account is Inactive to use Friends & Family").fre("FR: Your Account is Inactive to use Friends & Family")),
//				new ReturnCodeTexts(ReturnCodes.insufficientBalance, Phrase.en("You do not have enough credit to use Friends & Family").fre("FR: You do not have enough credit to use Friends & Family")),
//				new ReturnCodeTexts(ReturnCodes.invalidService, Phrase.en("You are not subscribed to use Friends & Family").fre("FR: You are not subscribed to use Friends & Family")),
//				new ReturnCodeTexts(ReturnCodes.notEligible, Phrase.en("You May not use Friends & Family").fre("FR: You May not use Friends & Family")),
//				new ReturnCodeTexts(ReturnCodes.alreadyMember, Phrase.en("That number is already in your Friends & Family list").fre("FR: That number is already in your Friends & Family list")),
//				new ReturnCodeTexts(ReturnCodes.notMember, Phrase.en("The contact to remove is not found in your Friends & Family list").fre("FR: The contact to remove is not found in your Friends & Family list")),
//				new ReturnCodeTexts(ReturnCodes.memberNotEligible, Phrase.en("That number is invalid").fre("FR: That number is invalid")),
//				new ReturnCodeTexts(ReturnCodes.cannotCallSelf, Phrase.en("You cannot add yourself to your Friends & Family list").fre("FR: You cannot add yourself to your Friends & Family list")),
//				new ReturnCodeTexts(ReturnCodes.excessiveBalance, Phrase.en("You have too much credit to use Friends & Family").fre("FR: You have too much credit to use Friends & Family")),
//				new ReturnCodeTexts(ReturnCodes.maxCountExceeded, Phrase.en("You have reached the max number of operations allowed for this period, which is 3").fre("FR: You have reached the max number of operations allowed for this period, which is 3")),
//				new ReturnCodeTexts(ReturnCodes.success, Phrase.en("You have Successfully updated Friends & Family list").fre("FR: You have Successfully updated Friends & Family list")) 
				};

		public ReturnCodeTexts[] getReturnCodesTexts()
		{
			check(esb, "ViewFafServiceParameters");
			return returnCodesTexts;
		}

		public void setReturnCodesTexts(ReturnCodeTexts[] returnCodesTexts)
		{
			check(esb, "ChangeFafServiceParameters");
			this.returnCodesTexts = returnCodesTexts;
		}

		// Single Shot USSD and SMS Commands
		protected VasCommand[] commands = new VasCommand[] { //
				//
				new VasCommand(VasCommand.Processes.getMembers, "#"), //
				new VasCommand(VasCommand.Processes.addMember, "*1*{RecipientMSISDN}#"), //
				new VasCommand(VasCommand.Processes.removeMember, "*2*{RecipientMSISDN}#"), //
//				new VasCommand(VasCommand.Processes.replaceMember, "*3*{ModifyMSISDN}*{RecipientMSISDN}#"), //
		};

		// Variants
		protected Variant[] variants = new Variant[] { //
				new Variant("LCL_ONNET", Phrase.en("Local On Net"), Variant.LOCAL_ONNET_VARIANT, 3, 3, 3, 3, 0L, 0L, 3, "26377XXXXXXX,26378XXXXXXX,77XXXXXXX,78XXXXXXX"), //
//				new Variant("LCL_OFFNET", Phrase.en("Local Off Net"), Variant.LOCAL_OFFNET_VARIANT, 1, 1, 1, 1, 0L, 0L, 1, "26371XXXXXXX,26373XXXXXXX,71XXXXXXX,73XXXXXXX"), //
//				new Variant("LCL_ALLNET", Phrase.en("Local All net"), Variant.LOCAL_ALLNET_VARIANT, 5, 5, 3, 3, 0L, 0L, 5, "2637XXXXXXXX,7XXXXXXXX"),
//				new Variant("INTL", Phrase.en("International"), Variant.INTERNATIONAL_VARIANT, 0, 1, 0, 1, 10, 10, 1, "XXXXXXXXXXXxxx"), //
				new Variant("LIST", Phrase.en("List"), Variant.LIST_VARIANT, -1, -1, -1, -1, 10, 10, -1, "XXXXXXXXXXXxxx"), //
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
			check(esb, "ViewFafServiceParameters");
			return variants;
		}

		public void setVariants(Variant[] variants)
		{
			check(esb, "ChangeFafServiceParameters");
			this.variants = variants;
		}

		// Service Classes
		protected ServiceClass[] serviceClasses = new ServiceClass[] { //
				new ServiceClass(DEFAULT_SERVICE_CLASS_ID, Phrase.en("Default"), false, "LCL_ONNET,LIST", 0L, 0L, -1, -1, 3, 0, 0), //
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
			check(esb, "ViewFafServiceParameters");
			return serviceClasses;
		}

		public void setServiceClasses(ServiceClass[] serviceClasses)
		{
			check(esb, "ChangeFafServiceParameters");
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
			return -1086486634276858629L;
		}

		@Override
		public String getName(String languageCode)
		{
			return serviceName.getSafe(languageCode, "Friends And Family");
			// return getServiceName(languageCode);
		}

		public String getShortCode()
		{
			check(esb, "ViewFafServiceParameters");
			return shortCode;
		}

		public void setShortCode(String shortCode)
		{
			check(esb, "ChangeFafServiceParameters");
			this.shortCode = shortCode;
		}

		public String getSmsSourceAddress()
		{
			check(esb, "ViewFafServiceParameters");
			return smsSourceAddress;
		}

		public void setSmsSourceAddress(String smsSourceAddress)
		{
			check(esb, "ChangeFafServiceParameters");
			this.smsSourceAddress = smsSourceAddress;
		}

		public int getServiceSubscriptionID()
		{
			check(esb, "ViewFafServiceParameters");
			return serviceSubscriptionID;
		}

		public void setServiceSubscriptionID(int serviceSubscriptionID)
		{
			check(esb, "ChangeFafServiceParameters");
			this.serviceSubscriptionID = serviceSubscriptionID;
		}
		
//		public int getAllowDelDays()//HIDE from GUI 
//		{
//			check(esb, "ViewFafServiceParameters");
//			return allowDelDays;
//		}
//
//		public void setAllowDelDays(int allowDelDays)
//		{
//			check(esb, "ChangeFafServiceParameters");
//			this.allowDelDays = allowDelDays;
//		}

		public boolean isServiceAutoSubscribe()
		{
			check(esb, "ViewFafServiceParameters");
			return serviceAutoSubscribe;
		}

		public void setServiceAutoSubscribe(boolean serviceAutoSubscribe)
		{
			check(esb, "ChangeFafServiceParameters");
			this.serviceAutoSubscribe = serviceAutoSubscribe;
		}

		public boolean isSmsToNewlyAddedFaf()
		{
			check(esb, "ViewFafServiceParameters");
			return smsToNewlyAddedFaf;
		}

		public void setSmsToNewlyAddedFaf(boolean smsToNewlyAddedFaf)
		{
			check(esb, "ChangeFafServiceParameters");
			this.smsToNewlyAddedFaf = smsToNewlyAddedFaf;
		}

		@Override
		public void validate() throws ValidationException
		{
		}

		@Override
		public void performUpdateNotificationSecurityCheck()
		{
			check(esb, "ChangeFafServiceNotifications");
		}

		@Override
		public void performGetNotificationSecurityCheck()
		{
			check(esb, "ViewFafServiceNotifications");
		}

		public VasCommand[] getCommands()
		{
			check(esb, "ViewFafServiceParameters");
			return commands;
		}

		public void setCommands(VasCommand[] commands)
		{
			check(esb, "ChangeFafServiceParameters");
			this.commands = commands;
		}
	}

	FafServiceConfig config = new FafServiceConfig();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// VasService Implementation
	//
	// /////////////////////////////////

	@Override
	public String getServiceID()
	{
		return "FAF";
	}

	@Override
	public String getServiceName(String lanuageID)
	{
		return "Friends And Family Service";
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
	public boolean processFAF(ResponseHeader response, CdrBase cdr, IDatabaseConnection db, String variantID, String subscriberNumber, String recipientNumber)
	{
		Variant variant = getVariant(variantID);
		if (variant == null)
		{
			response.exitWith(logger, cdr, ReturnCodes.malformedRequest, "Invalid HGSFKJG VariantID");
			return false;
		}

		response.exitWith(logger, cdr, ReturnCodes.success, "[%s] Valid FAF to send to [%s].", subscriberNumber, recipientNumber);
		return true;
	}

	public boolean processFAF(String variantID, String subscriberNumber, String recipientNumber)
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
	private int checkSubscriberValid(Subscriber subscriber, Transaction<?> transaction, //
			ServiceClass[] subscriberServiceClass, long[] subcriptionCost) throws ZTEException, SQLException
	{
		boolean hasValidServiceSubscription = true;
		// Test if the subscriberNumber Account is in a valid state.
		transaction.track(this, "CheckAccountActive");
		if (!isSubscriberActive(subscriber))
			return FAF_SUB_NOT_ACTIVE;

		// Test if the subscriberNumber is in one of the allowed service classes
		transaction.track(this, "CheckValidServiceClass");
		if (subscriberServiceClass[0] == null)
			subscriberServiceClass[0] = getServiceClass(DEFAULT_SERVICE_CLASS_ID);

		// re-check after default check
		if (subscriberServiceClass[0] == null)
			return FAF_SUB_NO_SERVICECLASS;
		if (subscriberServiceClass[0].isPostPaid())
			return FAF_SUB_NO_SERVICECLASS;

		if (config.serviceSubscriptionID != 0)
			hasValidServiceSubscription = subscriber.hasAccountOffer(config.serviceSubscriptionID);

		if (config.serviceAutoSubscribe && !hasValidServiceSubscription)
		{
			transaction.track(this, "serviceAutoSubscribe");
			subcriptionCost[0] = subscriberServiceClass[0].getFafSubcriptionCost();
			if (subcriptionCost[0] != 0L && subscriber.getAccountBalance(0) < subscriberServiceClass[0].getFafSubcriptionCost())
				return FAF_SUB_NO_BALANCE;

			if (!chargeSubscriber(subscriber, subcriptionCost[0]))
				return FAF_SUB_NO_BALANCE;

			Date startDateTime = new Date();
			Date expiryDateTime = new Date();
			Calendar c = Calendar.getInstance();
			c.setTime(expiryDateTime);
			c.add(Calendar.DATE, subscriberServiceClass[0].getFafSubcriptionExpiry());
			expiryDateTime = c.getTime();
			subscriber.updateAccountOffer(config.serviceSubscriptionID, startDateTime, null, expiryDateTime, null);
			hasValidServiceSubscription = subscriber.hasAccountOffer(config.serviceSubscriptionID);
		}
		// Check MinBal and MaxBal after subscription
		if (subscriberServiceClass[0].getMinBal() != 0L && subscriber.getAccountBalance(0) < subscriberServiceClass[0].getMinBal())
			return FAF_SUB_NO_BALANCE;

		if (subscriberServiceClass[0].getMaxBal() != 0L && subscriber.getAccountBalance(0) > subscriberServiceClass[0].getMaxBal())
			return FAF_SUB_TOO_MUCH_BALANCE;
		if (hasValidServiceSubscription)
			return FAF_SUB_OK;
		// The final say...
		return FAF_SUB_NO_SUBSCRIPTION;
	}

	// Read and update the record
	private int getFriendsAndFamilyRecord(IDatabaseConnection db, Subscriber subscriber, ServiceClass subscriberServiceClass, String variantID, //
			int fafAction, FriendsAndFamilyServiceRecord[] fafRecord, long[] usageCharge)
	{
		String subscriberMsisdn = subscriber.getNationalNumber();
		StringBuilder variantActionSB = new StringBuilder();
		variantActionSB.append(variantID);
		switch (fafAction)
		{
			case FAF_ACTION_LIST:
				variantActionSB.append("_LIST");
				break;
			case FAF_ACTION_ADD:
				variantActionSB.append("_ADD");
				break;
			case FAF_ACTION_DELETE:
				variantActionSB.append("_DELETE");
				break;
			case FAF_ACTION_REPLACE:
				// REPLACE has two parts, DELETE and ADD - check them separately for usage
			default:
				return FAF_RECORD_FAIL_READ;
		}
		String variantAction = variantActionSB.toString();

		try
		{
			// load call me back record
			List<FriendsAndFamilyServiceRecord> friendsAndFamilyServiceRecords = getFriendsAndFamilyServiceRecords(db, getServiceID(), variantAction, subscriberMsisdn);
			fafRecord[0] = friendsAndFamilyServiceRecords.size() > 0 ? friendsAndFamilyServiceRecords.get(0) : null;

			// Create a new FriendsAndFamily record if it doesn't exist
			boolean isNew = fafRecord[0] == null;
			if (isNew)
			{
				fafRecord[0] = new FriendsAndFamilyServiceRecord(getServiceID(), variantAction, subscriberMsisdn, 0L, new Date(), 0);
			}
			else
				fafRecord[0].isNew = false;

			// check if limits are exceeded
			int newPeriodCount = fafRecord[0].getPeriodCount() + 1;

			Date now = new Date();

			float diffDayHours = (float) (now.getTime() - fafRecord[0].getPeriodStartTime().getTime()) / (60 * 60 * 1000);
			float diffMonthDays = (float) (now.getTime() - fafRecord[0].getPeriodStartTime().getTime()) / (24 * 60 * 60 * 1000);

			// Check free usage of variant - If over free limit, set charge
			Variant variant = getVariant(variantID);
			switch (fafAction)
			{
				case FAF_ACTION_LIST:
					// Update Daily usage
					if (diffDayHours >= 24)
					{
						newPeriodCount = 1;
						fafRecord[0].setPeriodStartTime(now);
					}
					if (subscriberServiceClass.getFreeDailyLists() > -1 && newPeriodCount > subscriberServiceClass.getFreeDailyLists())
						usageCharge[0] = subscriberServiceClass.getChargeList();
					// Check max usage of variant
					if (subscriberServiceClass.getMaxDailyLists() > -1 && newPeriodCount > subscriberServiceClass.getMaxDailyLists())
						return FAF_RECORD_EXCEED;
					break;
				case FAF_ACTION_ADD:
					// Update Monthly usage
					if (diffMonthDays >= 28)
					{
						newPeriodCount = 1;
						fafRecord[0].setPeriodStartTime(now);
					}

					if (variant.getMaxMonthlyAdd() > -1 && newPeriodCount > variant.getMaxMonthlyAdd())
						return FAF_RECORD_EXCEED;
					break;
				case FAF_ACTION_DELETE:
					// Update Monthly usage
					if (diffMonthDays >= 30)
					{
						newPeriodCount = 1;
						fafRecord[0].setPeriodStartTime(now);
					}
					if (variant.getMaxMonthlyDelete() > -1 && newPeriodCount > variant.getMaxMonthlyDelete())
						return FAF_RECORD_EXCEED;
					break;
				case FAF_ACTION_REPLACE:
					// REPLACE has two parts, DELETE and ADD - check them separately for usage
				default:
					return FAF_RECORD_FAIL_READ;
			}

			fafRecord[0].setPeriodCount(newPeriodCount);
			fafRecord[0].setTotalCount(fafRecord[0].getTotalCount() + 1);

			if (!chargeSubscriber(subscriber, usageCharge[0]))
				return FAF_RECORD_FAIL_CHARGE;
		}
		catch (Exception ex)
		{
			fafRecord[0] = null;
			return FAF_RECORD_FAIL_READ;
		}
		return FAF_RECORD_OK; // Carry on
	}

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

	private List<FriendsAndFamilyServiceRecord> getFriendsAndFamilyServiceRecords(IDatabaseConnection db, String serviceID, String variantAction, String subscriberMsisdn) throws SQLException
	{
		String where = "where serviceID = %s";
		int count = 0;
		Object[] params = new Object[3];
		params[count++] = serviceID;

		if (variantAction != null)
		{
			where += " and variantAction = %s";
			params[count++] = variantAction;
		}

		if (subscriberMsisdn != null)
		{
			where += " and subscriberMsisdn = %s";
			params[count++] = subscriberMsisdn;
		}

		return db.selectList(FriendsAndFamilyServiceRecord.class, where, java.util.Arrays.copyOf(params, count));
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
			result = new Subscriber(subscriberNumber.toMSISDN(), zte, logger, transaction);
			context.setSubscriberProxy(result);
			return result;
		}
		return result;
	}

	private ServiceClass getServiceClass(Subscriber subscriber) throws ZTEException // AirException
	{
		for (ServiceClass serviceClass : config.getServiceClasses())
		{
			if (subscriber.getServiceClass() == serviceClass.getServiceClassID())
				return serviceClass;
		}
		return null;
	}

	private ServiceClass getServiceClass(int serviceClassID) throws ZTEException // AirException
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

	private boolean chargeSubscriber(Subscriber donor, long charge) throws ZTEException, SQLException // AirException
	{
		if (charge == 0)
		{
			logger.trace(this, "Charge is %s, omitting UpdateBalanceAndDate", charge);
			return true;
		}

		String chargeText = locale.formatCurrency(charge);
		logger.trace(this, "Attempt to charge %s %s", donor.getInternationalNumber(), chargeText);

		donor.updateAccounts(-charge);

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
		protected String modifyNumber;
		protected String minBalance = "0";
		protected String maxBalance = "1000000";
		protected String charge = "0";
		protected String fafList;
		protected String nextUsageDate;

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

		public String getModifyNumber()
		{
			return modifyNumber;
		}

		public void setModifyNumber(String modifyNumber)
		{
			this.modifyNumber = modifyNumber;
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

		public String getFafList()
		{
			return fafList;
		}

		public void setFafList(String fafList)
		{
			this.fafList = fafList;
		}

		public String getNextUsageDate()
		{
			return nextUsageDate;
		}

		public void setNextUsageDate(String nextUsageDate)
		{
			this.nextUsageDate = nextUsageDate;
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

	protected int smsOnNetRecipientFAFadd = notifications.add("Recipient message OnNet FAF ADD", //
			"{subscriberNumber} Added you to their Friends & Family list to get beter On Net Rates", //
			"FR: {subscriberNumber} Added you to their Friends & Family list to get even beter On Net Rates", //
			null, null);

	protected int smsOffNetRecipientFAFadd = notifications.add("Recipient message OffNet FAF ADD", //
			"{subscriberNumber} Added you to their Friends & Family list to get beter Off Net Rates ", //
			"FR: {subscriberNumber} Added you to their Friends & Family list to get beter Off Net Rates", //
			null, null);

	protected int smsAllNetRecipientFAFadd = notifications.add("Recipient message AllNet FAF ADD", //
			"{subscriberNumber} Added you to their Friends & Family list to get beter call rates", //
			"FR: {subscriberNumber} Added you to their Friends & Family list to get beter call rates", //
			null, null);

	protected int smsIntlRecipientFAFadd = notifications.add("Recipient message International FAF ADD", //
			"{subscriberNumber} Added you to their Friends & Family list to get beter International Rates", //
			"FR: {subscriberNumber} Added you to their Friends & Family list to get beter International Rates", //
			null, null);

	protected int subscriberFAFaddFree = notifications.add("Subscriber response for FAF added Recipient for free", //
			"Number {recipientNumber} has been added to your Friends & Family list", //
			"FR: Number {recipientNumber} has been added to your Friends & Family list", //
			null, null);

	protected int subscriberFAFaddCharge = notifications.add("Subscriber response for FAF added Recipient with charge", //
			"Number {recipientNumber} has been added to your Friends & Family list, this was charged {charge}", //
			"FR: Number {recipientNumber} has been added to your Friends & Family list, this was charged {charge}", //
			null, null);

	protected int subscriberFAFdelFree = notifications.add("Subscriber response for FAF delete for free", //
			"{recipientNumber} has been successfully removed from your Friends & Family list", //
			"FR: {recipientNumber} has been successfully removed from your Friends & Family list", //
			null, null);

	protected int subscriberFAFdelCharge = notifications.add("Subscriber response for FAF delete with charge", //
			"{recipientNumber} has been successfully removed from your Friends & Family list, this was charged {charge}", //
			"FR: {recipientNumber} has been successfully removed from your Friends & Family list, this was charged {charge}", //
			null, null);

	protected int subscriberFAFreplaceFree = notifications.add("Subscriber response for FAF update for free", //
			"{modifyNumber} was raplaced with {recipientNumber} on your Friends & Family list", //
			"FR: {modifyNumber} was raplaced with {recipientNumber} on your Friends & Family list", //
			null, null);

	protected int subscriberFAFreplaceCharge = notifications.add("Subscriber response for FAF update with charge", //
			"{modifyNumber} was raplaced with {recipientNumber} on your Friends & Family list, this was charged {charge}", //
			"FR: {modifyNumber} was raplaced with {recipientNumber} on your Friends & Family list, this was charged {charge}", //
			null, null);

	protected int subscriberFAFlistFree = notifications.add("Subscriber response for FAF list for free", //
			" Your Friends & Family list has the following contacts - {fafList}", //
			"FR: Your Friends & Family list has the following contacts - {fafList}", //
			null, null);

	protected int subscriberFAFlistCharge = notifications.add("Subscriber response for FAF list with charge", //
			"Your Friends & Family list has the following contacts - {fafList}, this was charged {charge}", //
			"FR: Your Friends & Family list has the following contacts - {fafList}, this was charged {charge}", //
			null, null);

	protected int subscriberFAFlistEmptyFree = notifications.add("Subscriber response for Empty FAF list for free", //
			"Your Friends & Family list is empty", //
			"FR: Your Friends & Family list is empty", //
			null, null);

	protected int subscriberFAFlistEmptyCharge = notifications.add("Subscriber response for Empty FAF list with charge", //
			"Your Friends & Family list is empty, this was charged {charge}", //
			"FR: Your Friends & Family list is empty, this was charged {charge}", //
			null, null);
	
	protected int returnCodesInactiveAParty = notifications.add("returnCodesInactiveAParty",//
			"Your Account is Inactive to use Friends & Family",//
			"FR: Your Account is Inactive to use Friends & Family",//
			null,null);
	
	protected int returnCodesInsufficientBalance = notifications.add("returnCodesInsufficientBalance",//
			"You do not have enough credit to use Friends & Family",//
			"FR: You do not have enough credit to use Friends & Family",//
			null, null);
			
	protected int returnCodesInvalidService = notifications.add("returnCodesInvalidService",//
			"You are not subscribed to use Friends & Family",//
			"FR: You are not subscribed to use Friends & Family",//
			null, null);
			
	protected int returnCodesNotEligible = notifications.add("returnCodesNotEligible",//
			"You May not use Friends & Family",//
			"FR: You May not use Friends & Family",//
			null, null);

	protected int returnCodesAlreadyMember = notifications.add("returnCodesAlreadyMember",//
			"Number {RecipientNumber} is already in your Friends & Family list",//
			"FR: Number {RecipientNumber} is already in your Friends & Family list",//
			null, null);

	protected int returnCodesNotMember = notifications.add("returnCodesNotMember",//
			"The contact {RecipientNumber} is not found in your Friends & Family list",//
			"FR: The contact {RecipientNumber} is not found in your Friends & Family list",//
			null, null);

	protected int returnCodesMemberNotEligible = notifications.add("returnCodesMemberNotEligible",//
			"Number {RecipientNumber} is invalid",//
			"FR: Number {RecipientNumber} is invalid",//
			null, null);

	protected int returnCodesCannotCallSelf = notifications.add("returnCodesCannotCallSelf",//
			"You cannot add yourself to your Friends & Family list",//
			"FR: You cannot add yourself to your Friends & Family list",//
			null, null);

	protected int returnCodesExcessiveBalance = notifications.add("returnCodesExcessiveBalance",//
			"You have too much credit to use Friends & Family",//
			"FR: You have too much credit to use Friends & Family",//
			null, null);
	protected int returnCodesMaxMembersExceeded = notifications.add("returnCodesMaxMembersExceeded",//
			"You have reached the maximum number of contacts that can be added to your Friends & Family list, which is 3",//
			"FR: You have reached the maximum number of contacts that can be added to your Friends & Family list, which is 3",//
			null, null);
	
	protected int returnCodesMaxCountExceeded = notifications.add("returnCodesMaxCountExceeded",//
			"You cannot edit more numbers to your Friends & Family list until {NextUsageDate}‎‎",//
			"FR: You cannot edit more numbers to your Friends & Family list until {NextUsageDate}‎‎",//
			null, null);
	
	protected int returnCodesRecipientNotRemove = notifications.add("returnCodesMaxCountExceeded",//
			"You can only remove {RecipientNumber} from your Friends & Family list after {NextUsageDate}‎‎",//
			"FR: You can only remove {RecipientNumber} from your Friends & Family list after {NextUsageDate}‎‎",//
			null, null);
}
