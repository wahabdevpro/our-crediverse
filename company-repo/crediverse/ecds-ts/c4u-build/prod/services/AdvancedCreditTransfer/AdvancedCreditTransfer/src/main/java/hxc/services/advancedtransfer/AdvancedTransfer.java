package hxc.services.advancedtransfer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.AddCreditTransferRequest;
import com.concurrent.hxc.AddCreditTransferResponse;
import com.concurrent.hxc.AddMemberRequest;
import com.concurrent.hxc.AddMemberResponse;
import com.concurrent.hxc.CreditTransfer;
import com.concurrent.hxc.CreditTransferType;
import com.concurrent.hxc.GetCreditTransfersRequest;
import com.concurrent.hxc.GetCreditTransfersResponse;
import com.concurrent.hxc.GetMembersRequest;
import com.concurrent.hxc.GetMembersResponse;
import com.concurrent.hxc.GetOwnersRequest;
import com.concurrent.hxc.GetOwnersResponse;
import com.concurrent.hxc.IServiceContext;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.ProcessLifecycleEventRequest;
import com.concurrent.hxc.ProcessLifecycleEventResponse;
import com.concurrent.hxc.RemoveCreditTransfersRequest;
import com.concurrent.hxc.RemoveCreditTransfersResponse;
import com.concurrent.hxc.RemoveMemberRequest;
import com.concurrent.hxc.RemoveMemberResponse;
import com.concurrent.hxc.RemoveMembersRequest;
import com.concurrent.hxc.RemoveMembersResponse;
import com.concurrent.hxc.ResponseHeader;
import com.concurrent.hxc.ResumeCreditTransferRequest;
import com.concurrent.hxc.ResumeCreditTransferResponse;
import com.concurrent.hxc.ServiceContext;
import com.concurrent.hxc.SubscribeRequest;
import com.concurrent.hxc.SubscribeResponse;
import com.concurrent.hxc.SubscriptionState;
import com.concurrent.hxc.SuspendCreditTransferRequest;
import com.concurrent.hxc.SuspendCreditTransferResponse;
import com.concurrent.hxc.TransferRequest;
import com.concurrent.hxc.TransferResponse;
import com.concurrent.hxc.UnsubscribeRequest;
import com.concurrent.hxc.UnsubscribeResponse;
import com.concurrent.hxc.VasServiceInfo;

import hxc.connectors.Channels;
import hxc.connectors.IInteraction;
import hxc.connectors.air.AirException;
import hxc.connectors.air.proxy.AccountUpdate;
import hxc.connectors.air.proxy.Subscriber;
import hxc.connectors.air.proxy.UpdateParameters;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.lifecycle.ISubscription;
import hxc.connectors.lifecycle.ITemporalTrigger;
import hxc.connectors.soap.ISoapConnector;
import hxc.connectors.soap.ISubscriber;
import hxc.connectors.vas.VasCommand;
import hxc.connectors.vas.VasCommand.Processes;
import hxc.connectors.vas.VasCommandParser;
import hxc.servicebus.HostInfo;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.services.notification.INotificationText;
import hxc.services.pin.IPinService;
import hxc.services.transactions.CdrBase;
import hxc.services.transactions.CsvCdr;
import hxc.services.transactions.ICdr;
import hxc.services.transactions.ITransaction;
import hxc.services.transactions.Transaction;
import hxc.utils.calendar.DateTime;
import hxc.utils.protocol.sdp.ThresholdNotificationFileV2;
import hxc.utils.protocol.sdp.ThresholdNotificationFileV3;
import hxc.utils.string.StringUtils;

public class AdvancedTransfer extends AdvancedTransferBase
{
	final static Logger logger = LoggerFactory.getLogger(AdvancedTransfer.class);

	private static final String NO_PIN = "NO_PIN";
	private static final String VARIANT_ANY = "ANY";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// VAS Service Implementation
	//
	// /////////////////////////////////

	@Override
	public String getServiceID()
	{
		return "ACT";
	}

	@Override
	public String getServiceName(String languageCode)
	{
		return config.getName(languageCode);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public AdvancedTransfer()
	{
		super();
		super.commandParser = new VasCommandParser(this, "RecipientMSISDN", "TransferModeID", "Amount", "VariantID", "Pin", "TransferLimit")
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

					case "TransferModeID":
					{
						TransferMode transferMode = TransferMode.findByID(config.transferModes, value);
						if (transferMode == null)
							return false;
						arguments.transferMode = arguments.variantID = transferMode.getTransferModeID();
						return true;
					}

					case "Pin":
						arguments.pin = value;
						return true;

					case "VariantID":
						arguments.variantID = value;
						return true;

					case "Amount":
					{
						arguments.amount = Long.parseLong(value);
					}
						return true;

					case "TransferLimit":
						arguments.transferLimit = Long.parseLong(value);
						return true;
				}

				return false;
			}

			@Override
			protected ReturnCodes onPreExecute(IInteraction interaction, Processes process, ServiceContext context, CommandArguments arguments)
			{
				TransferMode transferMode = TransferMode.findByID(config.transferModes, arguments.transferMode);
				if (transferMode != null)
					arguments.amount = StringUtils.parseScaled(Long.toString(arguments.amount), transferMode.getRecipientUnitsDisplayConversion(), TransferMode.SCALE_DENOMINATOR);
				return ReturnCodes.success;
			}

			@Override
			protected ISubscriber getSubscriberProxy(String msisdn)
			{
				return new Subscriber(msisdn, air, null);
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
	// Get Service Info
	//
	// /////////////////////////////////

	@Override
	public VasServiceInfo[] getServiceInfo(IServiceContext context, Number subscriberNumber, String variantID, Integer languageID, boolean activeOnly, boolean suggested) throws Exception
	{
		List<VasServiceInfo> result = new ArrayList<VasServiceInfo>();
		String languageCode = esb.getLocale().getLanguage(languageID);

		Subscriber subscriber = null;
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
				else if (isDonor(db, subscriber, variant))
					serviceInfo.setState(SubscriptionState.active);
				else
					serviceInfo.setState(SubscriptionState.notActive);

				if (!activeOnly || serviceInfo.getState() != SubscriptionState.notActive)
					result.add(serviceInfo);
			}
		}
		catch (Exception e)
		{
			logger.error("Failed to connect to database", e);
			throw e;
		}

		return result.toArray(new VasServiceInfo[0]);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Subscribe
	//
	// /////////////////////////////////
	@Override
	public SubscribeResponse subscribe(IServiceContext context, SubscribeRequest request)
	{
		// Prepare an Response
		SubscribeResponse response = super.subscribe(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = SubscribeRequest.validate(request);
				if (problem != null)
					return response.exitWith(cdr, ReturnCodes.malformedRequest, problem);

				// Validate Variant
				Variant variant = toVariant(request.getVariantID());
				if (variant == null)
					return response.exitWith(cdr, ReturnCodes.malformedRequest, "Invalid VariantID");

				// Update CDR
				cdr.setServiceID(request.getServiceID());
				cdr.setVariantID(request.getVariantID());
				cdr.setProcessID("Subscribe");

				// Get Donor Proxy
				Subscriber donor = getSubscriber(context, request.getSubscriberNumber(), transaction);

				try
				{
					// Set Properties
					Properties properties = getProperties(context);
					setDonorProperties(properties, donor, variant, cdr);
					DateTime expiryTime = DateTime.getNow().add(variant.getSafeValidityPeriod(), variant.getValidityPeriodUnit());

					// Perform GetAccountDetails UCIP call for the Donor.
					transaction.track(this, "GetAccountDetails");
					donor.getAccountDetails();
					setExpiryDateProperty(donor, properties, expiryTime);

					// Test if the Donor Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!isSubscriberActive(donor, null))
						return response.exitWith(cdr, ReturnCodes.notEligible, "Account not Active");

					// Test if the Donor is in one of the allowed service classes
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass donorServiceClass = getServiceClass(donor);
					if (donorServiceClass == null || !variant.isEligibleFor(donorServiceClass.getServiceClassID()))
						return response.exitWith( cdr, ReturnCodes.notEligible, "Wrong SC");

					// Test if the Donor is already subscribed to this Variant
					transaction.track(this, "CheckAlreadySubscribed");
					if (isDonor(db, donor, variant))
						return response.exitWith(cdr, ReturnCodes.alreadySubscribed, "Already Subscribed");

					// Charge the Donor with the subscription fee
					transaction.track(this, "ChargeDonor");
					long charge = variant.getSubscriptionCharge();
					if (request.getMode() == RequestModes.testOnly)
					{
						cdr.setChargeLevied((int) charge);
						response.setChargeLevied(charge);
						properties.setCharge(locale.formatCurrency(charge));
						transaction.complete();
						return response.exitWith(cdr, ReturnCodes.successfulTest, "Success");
					}
					if (!chargeDonor(null, donor, charge, donorServiceClass, properties, cdr, response))
						return response.exitWith(cdr, ReturnCodes.insufficientBalance, "Insufficient Balance");

					// Add Subscription to Lifecycle Store
					transaction.track(this, "AddSubscriptionLifecycle");
					addDonorLifecycle(db, donor, variant, expiryTime);

					// Set the Result Text
					context.setResultText(getResultText(donor, smsDonorSubscribed, properties));

				}
				catch (AirException e)
				{
					return response.exitWith(cdr, e.getReturnCode(), e);
				}
				catch (SQLException e)
				{
					return response.exitWith(cdr, ReturnCodes.technicalProblem, e);
				}

				// Complete
				transaction.complete();
				return response.exitWith(cdr, ReturnCodes.success, "Success");
			}
		}
		catch (Throwable e)
		{
			return response.exitWith(cdr, ReturnCodes.technicalProblem, e);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Un-Subscribe
	//
	// /////////////////////////////////

	@Override
	public UnsubscribeResponse unsubscribe(IServiceContext context, UnsubscribeRequest request)
	{
		// Create an Response
		UnsubscribeResponse response = super.unsubscribe(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{

			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = UnsubscribeRequest.validate(request);
				if (problem != null)
					return response.exitWith(cdr, ReturnCodes.malformedRequest, problem);

				// Validate Variant
				Variant variant = toVariant(request.getVariantID());
				if (variant == null)
					return response.exitWith(cdr, ReturnCodes.malformedRequest, "Invalid VariantID");

				// Update CDR
				cdr.setServiceID(request.getServiceID());
				cdr.setVariantID(request.getVariantID());
				cdr.setProcessID("Unsubscribe");

				// Get Subscriber Proxy
				Subscriber donor = getSubscriber(context, request.getSubscriberNumber(), transaction);
				try
				{
					// Perform GetAccountDetails UCIP call for the Donor.
					transaction.track(this, "GetAccountDetails");
					donor.getAccountDetails();

					// Get Properties
					Properties properties = getProperties(context);
					setDonorProperties(properties, donor, variant, cdr);

					// Test if the Donor Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!isSubscriberActive(donor, null))
						return response.exitWith(cdr, ReturnCodes.notEligible, "Donor Account not Active");

					// Test if the Donor is in one of the allowed service classes (S0571).
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass donorServiceClass = getServiceClass(donor);
					if (donorServiceClass == null)
						return response.exitWith(cdr, ReturnCodes.notEligible, "Wrong Donor SC");

					// Test if the Donor is subscribed to this variant of the Credit Sharing Service, by checking the presence of the Subscription OfferID (S0529) with a GetOffers UCIP call.
					transaction.track(this, "CheckIfSubscribed");
					if (!isDonor(db, donor, variant))
						return response.exitWith(cdr, ReturnCodes.notSubscribed, "Not Subscribed");

					// Charge the Donor with the Un-Subscription Fee
					transaction.track(this, "ChargeDonor");
					long charge = donorServiceClass.getUnsubscribeCharge();
					if (request.getMode() == RequestModes.testOnly)
					{
						cdr.setChargeLevied((int) charge);
						response.setChargeLevied(charge);
						properties.setCharge(locale.formatCurrency(charge));
						transaction.complete();
						return response.exitWith(cdr, ReturnCodes.successfulTest, "Success");
					}
					if (!chargeDonor(null, donor, charge, donorServiceClass, properties, cdr, response))
						return response.exitWith(cdr, ReturnCodes.insufficientBalance, "Insufficient Balance");

					// Common unsubscribe Procedure
					String[] recipientList = lifecycle.getMembers(db, donor, getServiceID(), variant.getVariantID());
					unsubscribe(db, donor, recipientList, variant, cdr, transaction, properties, context);

				}
				catch (AirException e)
				{
					return response.exitWith(cdr, e.getReturnCode(), e);
				}
				catch (SQLException e)
				{
					return response.exitWith(cdr, ReturnCodes.technicalProblem, e);
				}

				// Complete
				transaction.complete();
				return response.exitWith(cdr, ReturnCodes.success, "Success");
			}

		}
		catch (Throwable e)
		{
			return response.exitWith(cdr, ReturnCodes.technicalProblem, e);
		}

	}

	private void unsubscribe(IDatabaseConnection db, Subscriber donor, String[] recipientList, Variant variant, ICdr cdr, //
			Transaction<?> transaction, Properties properties, IServiceContext context) throws AirException, SQLException
	{
		// Delete all Transfer Records
		deleteTransferRecords(db, getServiceID(), variant != null ? variant.getVariantID() : null, donor, null, null);

		// Get All Active Credit Transfers
		ITemporalTrigger[] creditTransfers = lifecycle.getTemporalTriggers(db, getServiceID(), null, donor, null, null);

		// Remove Temporal Triggers
		for (ITemporalTrigger transfer : creditTransfers)
		{
			lifecycle.removeTemporalTrigger(db, transfer);
		}

		// Remove all the Recipients of the Donor
		// NOTE: This will also remove all the quotas recipients have.
		for (String recipientMSISDN : recipientList)
		{
			Subscriber recipient = new Subscriber(recipientMSISDN, air, transaction);
			setRecipientProperties(properties, recipient, cdr);
			removeRecipient(db, donor, recipient, transaction, variant, properties, smsDonorRemovedRecipient, context);
		}

		// Remove the subscription from the C4U Lifecycle Connector.
		lifecycle.removeSubscription(db, donor, getServiceID(), variant.getVariantID());

		// Set the Result Text
		context.setResultText(getResultText(donor, smsDonorUnsubscribed, properties));

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Add Recipient
	//
	// /////////////////////////////////

	@Override
	public AddMemberResponse addMember(IServiceContext context, AddMemberRequest request)
	{
		// Create Response
		AddMemberResponse response = super.addMember(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = AddMemberRequest.validate(request);
				if (problem != null)
					return response.exitWith(cdr, ReturnCodes.malformedRequest, problem);

				// Validate Variant
				Variant variant = toVariant(request.getVariantID());
				if (variant == null)
					return response.exitWith(cdr, ReturnCodes.invalidVariant, "Invalid Variant");

				// Update CDR
				cdr.setServiceID(request.getServiceID());
				cdr.setVariantID(request.getVariantID());
				cdr.setProcessID("AddMember");

				// Get Subscriber Proxy
				Subscriber donor = getSubscriber(context, request.getSubscriberNumber(), transaction);
				try
				{
					// Set Properties
					Properties properties = getProperties(context);
					setDonorProperties(properties, donor, variant, cdr);

					// Perform GetAccountDetails UCIP call for the Donor.
					transaction.track(this, "GetAccountDetails");
					donor.getAccountDetails();

					// Test if the Donor Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!isSubscriberActive(donor, null))
						return response.exitWith(cdr, ReturnCodes.notEligible, "Donor Account not Active");

					// Test if the Donor is in one of the allowed service classes (S0571).
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass donorServiceClass = getServiceClass(donor);
					if (donorServiceClass == null || !variant.isEligibleFor(donorServiceClass.getServiceClassID()))
						return response.exitWith(cdr, ReturnCodes.notEligible, "Wrong Donor SC");

					// Test if the Donor is subscribed to this variant of the Credit Sharing Service, by checking the presence of the Subscription OfferID (S0529) with a GetOffers UCIP call.
					// transaction.track(this, "CheckIfSubscribed");
					// if (!isDonor(db, donor, variant))
					// return response.exitWith( cdr, ReturnCodes.notSubscribed, "Not Subscribed");

					// Test if the Donor is not exceeding the maximum number of recipients for his service class (S0574)
					String[] memberMSISDNs = lifecycle.getMembers(db, donor, getServiceID(), variant.getVariantID());
					if (memberMSISDNs != null && memberMSISDNs.length >= donorServiceClass.getMaxRecipients())
						return response.exitWith(cdr, ReturnCodes.maxMembersExceeded, "Recipients Limit");

					// Perform GetAccountDetails UCIP call for the Recipient.
					Subscriber recipient = new Subscriber(request.getMemberNumber().toMSISDN(), air, transaction);
					setRecipientProperties(properties, recipient, cdr);
					recipient.getAccountDetails();

					// Test if already a member
					if (memberMSISDNs != null)
					{
						for (String memberMSISDN : memberMSISDNs)
						{
							if (recipient.isSameNumber(memberMSISDN))
								return response.exitWith(cdr, ReturnCodes.alreadyMember, "Already Added");
						}
					}

					// Test if the Recipient Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!isSubscriberActive(recipient, null))
						return response.exitWith(cdr, ReturnCodes.memberNotEligible, "Recipient Account not Active");

					// Test if the Recipient is in one of the allowed service classes
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass recipientServiceClass = getServiceClass(recipient);
					if (recipientServiceClass == null)
						return response.exitWith(cdr, ReturnCodes.memberNotEligible, "Wrong Recipient SC");

					// Charge the Donor with the AddRecipient Addition Fee
					transaction.track(this, "ChargeDonor");
					long charge = donorServiceClass.getAddRecipientCharge();
					if (request.getMode() == RequestModes.testOnly)
					{
						cdr.setChargeLevied((int) charge);
						response.setChargeLevied(charge);
						properties.setCharge(locale.formatCurrency(charge));
						transaction.complete();
						return response.exitWith(cdr, ReturnCodes.successfulTest, "Success");
					}
					if (!chargeDonor(null, donor, charge, donorServiceClass, properties, cdr, response))
						return response.exitWith(cdr, ReturnCodes.insufficientBalance, "Insufficient Balance");

					// The Recipient/Donor relationship is recorded in the C4U Lifecycle Connector.
					lifecycle.addMember(db, donor, getServiceID(), variant.getVariantID(), recipient);

					// Set the Result Text
					context.setResultText(getResultText(donor, smsDonorAddedRecipient, properties));

					// Send a configurable SMS to the Recipient to inform him of the successful recipient addition, with an invitation to decline by replying with "No" to the origin short code.
					transaction.track(this, "SendRecipientSMS");
					sendSubscriberSMS(recipient, smsRecipientAddedRecipient, properties);

				}
				catch (AirException e)
				{
					return response.exitWith(cdr, e.getReturnCode(), e);
				}
				catch (SQLException e)
				{
					return response.exitWith(cdr, ReturnCodes.technicalProblem, e);
				}

				// Terminate the process with a "Success" response Code.
				transaction.complete();
				return response.exitWith(cdr, ReturnCodes.success, "Success");
			}

		}
		catch (Throwable e)
		{
			return response.exitWith(cdr, ReturnCodes.technicalProblem, e);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Recipients
	//
	// /////////////////////////////////
	@Override
	public GetMembersResponse getMembers(IServiceContext context, GetMembersRequest request)
	{
		// Create Response
		GetMembersResponse response = super.getMembers(context, request);

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{

			// Validate Request
			String problem = GetMembersRequest.validate(request);
			if (problem != null)
				return response.exitWith(null, ReturnCodes.malformedRequest, problem);

			// Validate Variant
			Variant variant = toVariant(request.getVariantID());
			if (variant == null && request.getVariantID() != null)
				return response.exitWith(null, ReturnCodes.malformedRequest, "Invalid VariantID");

			// Get Subscriber Proxy
			Subscriber donor = getSubscriber(context, request.getSubscriberNumber(), null);
			try
			{
				// Get Properties
				Properties properties = getProperties(context);
				setDonorProperties(properties, donor, variant, null);

				// Perform GetAccountDetails UCIP call for the Donor.
				logger.debug("GetAccountDetails");
				donor.getAccountDetails();

				// Test if the Donor Account is in a valid state, i.e. all lifecycle dates are in the future.
				logger.debug("CheckIfActive");
				if (!isSubscriberActive(donor, null))
					return response.exitWith(null, ReturnCodes.notEligible, "Donor Account not Active");

				// Test if the Donor is in one of the allowed service classes (S0571).
				logger.debug("CheckServiceClass");
				ServiceClass donorServiceClass = getServiceClass(donor);
				if (donorServiceClass == null || variant != null && !variant.isEligibleFor(donorServiceClass.getServiceClassID()))
					return response.exitWith(null, ReturnCodes.notEligible, "Wrong Donor SC");

				// Update List of Members
				String[] members = lifecycle.getMembers(db, donor, getServiceID(), variant == null ? null : variant.getVariantID());
				response.setMembers(Number.fromString(numberPlan.getNationalFormat(members), Number.NumberType.UNKNOWN, Number.NumberPlan.UNKNOWN));

			}
			catch (AirException e)
			{
				return response.exitWith( null, e.getReturnCode(), e);
			}

			// Complete
			return response.exitWith( null, ReturnCodes.success, "Success");
		}
		catch (Throwable e)
		{
			return response.exitWith( null, ReturnCodes.technicalProblem, e);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Owners
	//
	// /////////////////////////////////

	@Override
	public GetOwnersResponse getOwners(IServiceContext context, GetOwnersRequest request)
	{
		// Create Response
		GetOwnersResponse response = super.getOwners(context, request);

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{

			// Validate Request
			String problem = GetOwnersRequest.validate(request);
			if (problem != null)
				return response.exitWith( null, ReturnCodes.malformedRequest, problem);

			// Empty Result
			response.setOwners(Number.fromString(new String[0], Number.NumberType.UNKNOWN, Number.NumberPlan.UNKNOWN));

			// Get Subscriber Proxy
			Subscriber recipient = getSubscriber(context, request.getMemberNumber(), null);

			// Set Properties
			Properties properties = getProperties(context);
			setRecipientProperties(properties, recipient, null);

			// Get Owners
			logger.debug("GetOwners");
			String[] ownerNumbers = lifecycle.getOwners(db, recipient, getServiceID());
			for (int index = 0; index < ownerNumbers.length; index++)
			{
				ownerNumbers[index] = numberPlan.getNationalFormat(ownerNumbers[index]);
			}
			response.setOwners(Number.fromString(ownerNumbers, Number.NumberType.UNKNOWN, Number.NumberPlan.UNKNOWN));

			// Complete
			return response.exitWith( null, ReturnCodes.success, "Success");
		}
		catch (Throwable e)
		{
			return response.exitWith( null, ReturnCodes.technicalProblem, e);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Remove Recipient
	//
	// /////////////////////////////////

	@Override
	public RemoveMemberResponse removeMember(IServiceContext context, RemoveMemberRequest request)
	{
		// Create Response
		RemoveMemberResponse response = super.removeMember(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{

			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = RemoveMemberRequest.validate(request);
				if (problem != null)
					return response.exitWith( cdr, ReturnCodes.malformedRequest, problem);

				// Get Subscriber Proxy's
				Subscriber donor = getSubscriber(context, request.getSubscriberNumber(), transaction);
				Subscriber recipient = new Subscriber(request.getMemberNumber().toMSISDN(), air, transaction);

				// Validate Variant, if any
				Variant variant = toVariant(request.getVariantID());

				// Update CDR
				cdr.setServiceID(request.getServiceID());
				cdr.setVariantID(request.getVariantID());
				cdr.setProcessID("RemoveMember");

				try
				{
					// Set Properties
					Properties properties = getProperties(context);
					setDonorProperties(properties, donor, variant, cdr);
					setRecipientProperties(properties, recipient, cdr);

					// Perform GetAccountDetails UCIP call for the Donor.
					transaction.track(this, "GetAccountDetails");
					donor.getAccountDetails();

					// Test if the Donor Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!isSubscriberActive(donor, null))
						return response.exitWith( cdr, ReturnCodes.notEligible, "Donor Account not Active");

					// Test if the Donor is in one of the allowed service classes (S0571).
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass donorServiceClass = getServiceClass(donor);
					if (donorServiceClass == null || variant != null && !variant.isEligibleFor(donorServiceClass.getServiceClassID()))
						return response.exitWith( cdr, ReturnCodes.notEligible, "Wrong Donor SC");

					// Test if the Donor is subscribed to this variant of the Credit Sharing Service, by checking the presence of the Subscription OfferID (S0529) with a GetOffers UCIP call.
					// transaction.track(this, "CheckIfSubscribed");
					// if (!isDonor(db, donor, variantx))
					// return response.exitWith( cdr, ReturnCodes.notSubscribed, "Not Subscribed");

					// Perform GetAccountDetails UCIP call for the Recipient.
					setRecipientProperties(properties, recipient, cdr);
					recipient.getAccountDetails();

					// Test if the Recipient Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!isSubscriberActive(recipient, null))
						return response.exitWith( cdr, ReturnCodes.memberNotEligible, "Recipient Account not Active");

					// Test if the Recipient is a recipient of the Donor, by checking the presence of the Recipient OfferID (S0530) with a GetOffers UCIP call and interrogation of the C4U Lifecycle
					// Membership store.
					transaction.track(this, "CheckIfRecipient");
					if (!isRecipient(db, donor, recipient, variant))
						return response.exitWith( cdr, ReturnCodes.notMember, "Not Donor's Recipient");

					// Get All Active Credit Transfers
					ITemporalTrigger[] creditTransfers = lifecycle.getTemporalTriggers(db, getServiceID(), null, donor, recipient, null);

					// Charge the Donor with Recipient Removal Fee
					transaction.track(this, "ChargeDonor");
					long charge = donorServiceClass.getRemoveRecipientCharge() + creditTransfers.length * donorServiceClass.getRemoveTransferCharge();
					if (request.getMode() == RequestModes.testOnly)
					{
						cdr.setChargeLevied((int) charge);
						response.setChargeLevied(charge);
						properties.setCharge(locale.formatCurrency(charge));
						transaction.complete();
						return response.exitWith( cdr, ReturnCodes.successfulTest, "Success");
					}
					if (!chargeDonor(null, donor, charge, donorServiceClass, properties, cdr, response))
						return response.exitWith( cdr, ReturnCodes.insufficientBalance, "Insufficient Balance");

					// Remove Transfer Records
					deleteTransferRecords(db, getServiceID(), request.getVariantID() == null ? null : request.getVariantID(), donor, recipient, null);

					// Remove Temporal Triggers
					for (ITemporalTrigger transfer : creditTransfers)
					{
						lifecycle.removeTemporalTrigger(db, transfer);
					}

					// Remove Recipient
					removeRecipient(db, donor, recipient, transaction, variant, properties, smsDonorRemovedRecipient, context);

				}
				catch (AirException e)
				{
					return response.exitWith( cdr, e.getReturnCode(), e);
				}

				// Complete
				transaction.complete();
				return response.exitWith( cdr, ReturnCodes.success, "Success");
			}

		}
		catch (Throwable e)
		{
			return response.exitWith( cdr, ReturnCodes.technicalProblem, e);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Remove Recipient
	//
	// /////////////////////////////////

	@Override
	public RemoveMembersResponse removeMembers(IServiceContext context, RemoveMembersRequest request)
	{
		// Create Response
		RemoveMembersResponse response = super.removeMembers(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{

			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = RemoveMembersRequest.validate(request);
				if (problem != null)
					return response.exitWith( cdr, ReturnCodes.malformedRequest, problem);

				// Validate Variant
				Variant variant = toVariant(request.getVariantID());
				if (variant == null)
					return response.exitWith( cdr, ReturnCodes.malformedRequest, "Invalid VariantID");

				// Update CDR
				cdr.setServiceID(request.getServiceID());
				cdr.setVariantID(request.getVariantID());
				cdr.setProcessID("RemoveMembers");

				// Get Donor Proxy
				Subscriber donor = getSubscriber(context, request.getSubscriberNumber(), transaction);
				try
				{
					// Set Properties
					Properties properties = getProperties(context);
					setDonorProperties(properties, donor, variant, cdr);

					// Perform GetAccountDetails UCIP call for the Donor.
					transaction.track(this, "GetAccountDetails");
					donor.getAccountDetails();

					// Test if the Donor Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!isSubscriberActive(donor, null))
						return response.exitWith( cdr, ReturnCodes.notEligible, "Donor Account not Active");

					// Test if the Donor is in one of the allowed service classes (S0571).
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass donorServiceClass = getServiceClass(donor);
					if (donorServiceClass == null || !variant.isEligibleFor(donorServiceClass.getServiceClassID()))
						return response.exitWith( cdr, ReturnCodes.notEligible, "Wrong Donor SC");

					// Test if the Donor is subscribed to this variant of the Credit Sharing Service, by checking the presence of the Subscription OfferID (S0529) with a GetOffers UCIP call.
					transaction.track(this, "CheckIfSubscribed");
					if (!isDonor(db, donor, variant))
						return response.exitWith( cdr, ReturnCodes.notSubscribed, "Not Subscribed");

					// Get List of Recipients
					String[] recipientList = lifecycle.getMembers(db, donor, getServiceID(), variant.getVariantID());

					// Get List of Active Credit Transfers
					ITemporalTrigger[] creditTransfers = lifecycle.getTemporalTriggers(db, getServiceID(), null, donor, null, null);

					// Charge the Donor with Recipient Removal Fee
					transaction.track(this, "ChargeDonor");
					long charge = donorServiceClass.getRemoveRecipientCharge() * recipientList.length + //
							donorServiceClass.getRemoveTransferCharge() * creditTransfers.length;
					if (request.getMode() == RequestModes.testOnly)
					{
						cdr.setChargeLevied((int) charge);
						response.setChargeLevied(charge);
						properties.setCharge(locale.formatCurrency(charge));
						transaction.complete();
						return response.exitWith( cdr, ReturnCodes.successfulTest, "Success");
					}
					if (!chargeDonor(null, donor, charge, donorServiceClass, properties, cdr, response))
						return response.exitWith( cdr, ReturnCodes.insufficientBalance, "Insufficient Balance");

					// Remove Transfer Records
					deleteTransferRecords(db, getServiceID(), null, donor, null, null);

					// Remove Active Credit Transfers
					for (ITemporalTrigger creditTransfer : creditTransfers)
					{
						lifecycle.removeTemporalTrigger(db, creditTransfer);
					}

					// Remove each Recipient
					for (String recipientMSISDN : recipientList)
					{
						Subscriber recipient = new Subscriber(recipientMSISDN, air, transaction);
						setRecipientProperties(properties, recipient, cdr);
						removeRecipient(db, donor, recipient, transaction, variant, properties, smsDonorRemovedRecipient, context);
					}

				}
				catch (AirException e)
				{
					return response.exitWith( cdr, e.getReturnCode(), e);
				}

				// Complete
				transaction.complete();
				return response.exitWith( cdr, ReturnCodes.success, "Success");
			}

		}
		catch (Throwable e)
		{
			return response.exitWith( cdr, ReturnCodes.technicalProblem, e);
		}

	}

	private void removeRecipient(IDatabaseConnection db, Subscriber donor, Subscriber recipient, Transaction<?> transaction, //
			Variant variant, Properties properties, int donorSmsID, IServiceContext context) throws AirException, SQLException
	{
		// Remove the Donor/Recipient Relationship for the C4U Lifecycle Connector
		lifecycle.removeMember(db, donor, getServiceID(), variant == null ? null : variant.getVariantID(), recipient);

		// Set the Result Text
		context.setResultText(getResultText(donor, donorSmsID, properties));

		// Send a configurable SMS to the Recipient to inform him of the successful recipient removal and the MSISDN of the Donor.
		transaction.track(this, "SendRecipientSMS");
		sendSubscriberSMS(recipient, smsRecipientRemovedRecipient, properties);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Credit Transfers
	//
	// /////////////////////////////////
	@Override
	public GetCreditTransfersResponse getCreditTransfers(IServiceContext serviceContext, GetCreditTransfersRequest request)
	{
		// Create Response
		GetCreditTransfersResponse response = super.getCreditTransfers(serviceContext, request);
		String languageCode = esb.getLocale().getLanguage(request.getLanguageID());

		// Create Donor Proxy
		Subscriber donor = request.getSubscriberNumber() == null ? null : new Subscriber(request.getSubscriberNumber().toMSISDN(), air, null);

		// Create Recipient Proxy
		Subscriber recipient = request.getMemberNumber() == null ? null : new Subscriber(request.getMemberNumber().toMSISDN(), air, null);

		// Create Properties
		Properties properties = getProperties(serviceContext);
		if (donor != null)
			properties.setDonorMSISDN(donor.getNationalNumber());
		if (recipient != null)
			properties.setRecipientMSISDN(recipient.getNationalNumber());

		// Get matching Transfer Mode
		TransferMode matchingMode = null;
		if (request.getTransferMode() != null)
		{
			matchingMode = toTransferMode(request.getTransferMode());
		}

		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Get Active Transfers
			Map<String, ITemporalTrigger> activeTransferModes = new HashMap<String, ITemporalTrigger>();
			if (request.isActiveOnly() || donor != null && recipient != null)
			{
				if (donor == null && recipient == null)
					return response.exitWith( null, ReturnCodes.malformedRequest, "Missing Donor/Recipient");

				ITemporalTrigger[] triggers = lifecycle.getTemporalTriggers(db, getServiceID(), null, donor, recipient, null);
				for (ITemporalTrigger trigger : triggers)
				{
					activeTransferModes.put(trigger.getKeyValue(), trigger);
				}
			}

			// Get each Transfer Mode
			List<CreditTransfer> transfers = new ArrayList<CreditTransfer>();
			for (TransferMode transferMode : config.getTransferModes())
			{
				if (request.isActiveOnly())
				{
					if (!activeTransferModes.containsKey(transferMode.getTransferModeID()))
						continue;
				}
				else
				{

					// Skip if Donor is not Eligible
					if (donor != null)
					{
						if (!transferMode.isValidDonorServiceClass(donor.getServiceClassCurrent()))
							continue;

						if (!request.isActiveOnly())
						{
							String variantID = getCorrespondingSubscription(db, donor, transferMode);
							if (variantID == null)
								continue;
						}
					}

					// Skip if Recipient is not Eligible
					if (recipient != null)
					{
						if (!transferMode.isValidRecipientServiceClass(recipient.getServiceClassCurrent()))
							continue;
					}
				}

				// Skip if mode doesn't match
				if (matchingMode != null && transferMode != matchingMode)
					continue;

				// Create Transfer
				CreditTransfer transfer = new CreditTransfer();
				transfer.setTransferModeID(transferMode.getTransferModeID());
				transfer.setName(transferMode.getName().get(languageCode));
				transfer.setTransferType(transferMode.getTransferType());
				transfer.setDonorAccountID(transferMode.getDonorAccountID());
				transfer.setDonorAccountType(transferMode.getDonorAccountType());
				transfer.setRecipientAccountID(transferMode.getRecipientAccountID());
				transfer.setRecipientAccountType(transferMode.getRecipientAccountType());
				transfer.setRecipientExpiryDays(transferMode.getRecipientExpiryDays());
				transfer.setUnits(transferMode.getUnits().get(languageCode));
				transfer.setConversionRate(transferMode.getConversionRate());
				transfer.setScaleNumerator(transferMode.getRecipientUnitsDisplayConversion());
				transfer.setScaleDenominator(TransferMode.SCALE_DENOMINATOR);
				transfer.setRequiresPIN(transferMode.getRequiresPIN());
				transfer.setRequiresSubscription(transferMode.getRequiredSubscriptionVariants().length > 0);

				ITemporalTrigger trigger = activeTransferModes.get(transferMode.getTransferModeID());
				if (trigger != null)
				{
					TransferRecord transferRecord = getTransferRecord(db, trigger);
					transfer.setActive(true);
					transfer.setDonorNumber(new Number(transferRecord.getDonorMsisdn()));
					transfer.setRecipientNumber(new Number(transferRecord.getRecipientMsisdn()));
					transfer.setAmount(transferRecord.getAmount());
					transfer.setInterval(transferMode.getInterval());
					transfer.setIntervalType(transferMode.getIntervalType().toString());
					transfer.setNextTransfer(trigger.getNextDateTime());
				}
				transfers.add(transfer);

				// Update Properties
				properties.setTransferMode(transferMode.getName());
				properties.setUnits(transferMode.getUnits());

			}

			response.setTransfers(transfers.toArray(new CreditTransfer[transfers.size()]));

			// Test if PIN is Critical
			if (donor != null && transfers.size() == 1 && transfers.get(0).getRequiresPIN())
			{
				CsvCdr cdr = new CsvCdr(request, "");
				if (!pinService.hasValidPIN(response, cdr, db, IPinService.DEFAULT_VARIANT_ID, donor.getInternationalNumber()))
					return response.exitWith( null, ReturnCodes.unregisteredPin, "PIN Required");
			}

		}
		catch (AirException e)
		{
			return response.exitWith( null, e.getReturnCode(), e);
		}
		catch (Throwable e)
		{
			return response.exitWith( null, ReturnCodes.technicalProblem, e);
		}

		return response.exitWith( null, ReturnCodes.success, "Success");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Add Credit Transfer
	//
	// /////////////////////////////////
	@Override
	public AddCreditTransferResponse addCreditTransfer(IServiceContext context, AddCreditTransferRequest request)
	{
		// Create Response
		AddCreditTransferResponse response = super.addCreditTransfer(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = AddCreditTransferRequest.validate(request);
				if (problem != null)
					return response.exitWith( cdr, ReturnCodes.malformedRequest, problem);

				// Validate Transfer Mode
				TransferMode transferMode = toTransferMode(request.getTransferMode());
				if (transferMode == null)
					return response.exitWith( cdr, ReturnCodes.malformedRequest, "Invalid Transfer Mode");

				// Validate the Transfer Type
				boolean isUponDepletion = transferMode.isUponDepletion();
				if (!isUponDepletion && !transferMode.isPeriodic())
					return response.exitWith( cdr, ReturnCodes.invalidVariant, "Invalid Transfer Mode");

				// Check Date
				Date nextTransferDate = request.getNextTransferDate();
				DateTime now = new DateTime();
				if (nextTransferDate == null)
					nextTransferDate = now.add(transferMode.getInterval(), transferMode.getIntervalType());
				if (!nextTransferDate.after(now))
					return response.exitWith( cdr, ReturnCodes.malformedRequest, "Invalid Next Date");

				// Get Properties
				Properties properties = getProperties(context);

				// Check Transfer Limit
				Long transferLimit = request.getTransferLimit();
				Long maxAmountPerPeriod = transferMode.getMaxAmountPerPeriod();
				if (transferLimit == null || maxAmountPerPeriod != null && transferLimit > maxAmountPerPeriod)
					transferLimit = maxAmountPerPeriod;

				if (isUponDepletion && (transferLimit == null || transferLimit <= 0))
				{
					properties.setMinQuantity(transferMode.formatRecipientQuantity(0));
					return response.exitWith( cdr, ReturnCodes.quantityTooSmall, "Invalid Transfer Limit");
				}

				if (transferLimit != null && transferLimit < request.getAmount())
				{
					properties.setMinQuantity(transferMode.formatRecipientQuantity(request.getAmount()));
					return response.exitWith( cdr, ReturnCodes.quantityTooSmall, "Invalid Transfer Limit");
				}

				// Update CDR
				cdr.setServiceID(request.getServiceID());
				cdr.setVariantID(transferMode.getTransferModeID());
				cdr.setProcessID("AddCreditTransfer");

				// Get Donor Proxy
				Subscriber donor = getSubscriber(context, request.getSubscriberNumber(), transaction);

				// Validate PIN
				boolean requiresPIN = transferMode.getRequiresPIN() && (!(request.getChannel() != null) && request.getChannel().equals(Channels.CRM));
				response.setRequiresPIN(requiresPIN);
				boolean testMode = request.getMode() == RequestModes.testOnly;

				if (!testMode)
				{
					if (requiresPIN && !pinService.validatePIN(response, cdr, db, IPinService.DEFAULT_VARIANT_ID, donor.getInternationalNumber(), request.getPin()))
						return response;
				}

				try
				{
					// Set Properties
					setDonorProperties(properties, donor, transferMode, cdr);

					// Perform GetAccountDetails UCIP call for the Donor.
					transaction.track(this, "GetAccountDetails");
					donor.getAccountDetails();

					// Test if the Donor Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!isSubscriberActive(donor, transferMode))
						return response.exitWith( cdr, ReturnCodes.notEligible, "Account not Active");

					// Test if the Donor is in one of the allowed service classes
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass donorServiceClass = getServiceClass(donor);
					if (donorServiceClass == null || !transferMode.isValidDonorServiceClass(donorServiceClass.getServiceClassID()))
						return response.exitWith( cdr, ReturnCodes.notEligible, "Wrong SC");

					// Check if the Donor has the required prerequisite subscription
					String variantID = getCorrespondingSubscription(db, donor, transferMode);
					if (variantID == null)
						return response.exitWith( cdr, ReturnCodes.notSubscribed, "No/Wrong Subscription");

					// Get Recipient Proxy
					Subscriber recipient = new Subscriber(request.getMemberNumber().toMSISDN(), air, transaction);
					setRecipientProperties(properties, recipient, cdr);

					// Perform GetAccountDetails UCIP call for the Donor.
					transaction.track(this, "GetAccountDetails");
					recipient.getAccountDetails();

					// Test if the Donor Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!isSubscriberActive(recipient, transferMode))
						return response.exitWith( cdr, ReturnCodes.notEligible, "Account not Active");

					// Test if the Donor is in one of the allowed service classes
					transaction.track(this, "CheckValidServiceClass");
					if (!transferMode.isValidRecipientServiceClass(recipient.getServiceClassCurrent()))
						return response.exitWith( cdr, ReturnCodes.notEligible, "Wrong SC");

					// Check if Already Added
					ITemporalTrigger[] triggers = lifecycle.getTemporalTriggers(db, getServiceID(), variantID, donor, recipient, transferMode.getTransferModeID());
					if (triggers != null && triggers.length > 0)
					{
						return response.exitWith( cdr, ReturnCodes.alreadyAdded, "Already Added");
					}

					// Add recipient automatically
					long addRecipientCharge = 0L;
					boolean mustAddRecipient = false;
					if (!isRecipient(db, recipient))
					{
						mustAddRecipient = true;
						addRecipientCharge = donorServiceClass.getAddRecipientCharge();
					}

					// Check if Amount not to Large or Small
					long amount = request.getAmount();
					setTransferProperties(properties, transferMode, amount);
					if (amount <= 0 || amount < transferMode.getMinAmount())
					{
						properties.setMinQuantity(transferMode.formatRecipientQuantity(transferMode.getMinAmount()));
						return response.exitWith( cdr, ReturnCodes.quantityTooSmall, "Too Little");
					}
					else if (amount > transferMode.getMaxAmount())
					{
						properties.setMaxQuantity(transferMode.formatRecipientQuantity(transferMode.getMaxAmount()));
						return response.exitWith( cdr, ReturnCodes.quantityTooBig, "Too Much");
					}

					// Test if Recipient already has the Transfer Mode
					ITemporalTrigger[] creditTransfers = lifecycle.getTemporalTriggers(db, getServiceID(), transferMode.getTransferModeID(), donor, recipient, transferMode.getTransferModeID());
					if (creditTransfers != null && creditTransfers.length > 0)
						return response.exitWith( cdr, ReturnCodes.alreadyAdded, "Already Added");

					// Charge the Donor with the Add Transfer fee
					transaction.track(this, "ChargeDonor");
					long addTransferCharge = donorServiceClass.getAddTransferCharge();
					long totalCharge = addRecipientCharge + addTransferCharge;
					properties.setCharge(locale.formatCurrency(totalCharge));
					setTransferProperties(properties, transferMode, amount);
					if (testMode)
					{
						cdr.setChargeLevied((int) totalCharge);
						response.setChargeLevied(totalCharge);
						transaction.complete();
						return response.exitWith( cdr, ReturnCodes.successfulTest, "Success");
					}
					if (!chargeDonor(null, donor, totalCharge, donorServiceClass, properties, cdr, response))
						return response.exitWith( cdr, ReturnCodes.insufficientBalance, "Insufficient Balance");

					// Add the Recipient
					if (mustAddRecipient)
					{
						lifecycle.addMember(db, donor, getServiceID(), variantID, recipient);
					}

					// Add Transfer Record and Temporal Trigger
					TransferRecord record = new TransferRecord(getServiceID(), variantID, transferMode.getTransferModeID(), //
							donor.getInternationalNumber(), recipient.getInternationalNumber(), //
							nextTransferDate, amount, transferLimit, request.getTransferThreshold());
					db.insert(record);
					lifecycle.addTemporalTrigger(db, record.toTrigger());

					// Set the Result Text
					context.setResultText(getResultText(donor, smsDonorAddedTransfer, properties));

					// Send a configurable SMS to the Recipient to inform him of the successful transfer addition, the fee which
					// has been charged
					transaction.track(this, "SendRecipientSMS");
					sendSubscriberSMS(recipient, smsRecipientAddedTransfer, properties);

				}
				catch (AirException e)
				{
					return response.exitWith( cdr, e.getReturnCode(), e);
				}
				catch (SQLException e)
				{
					return response.exitWith( cdr, ReturnCodes.technicalProblem, e);
				}

				// Complete
				transaction.complete();
				return response.exitWith( cdr, ReturnCodes.success, "Success");
			}
		}
		catch (Throwable e)
		{
			return response.exitWith( cdr, ReturnCodes.technicalProblem, e);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Remove Credit Transfers
	//
	// /////////////////////////////////

	@Override
	public RemoveCreditTransfersResponse removeCreditTransfers(IServiceContext context, RemoveCreditTransfersRequest request)
	{
		// Create Response
		RemoveCreditTransfersResponse response = super.removeCreditTransfers(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = RemoveCreditTransfersRequest.validate(request);
				if (problem != null)
					return response.exitWith( cdr, ReturnCodes.malformedRequest, problem);

				// Validate Transfer Mode
				TransferMode transferMode = toTransferMode(request.getTransferMode());
				if (transferMode == null)
					return response.exitWith( cdr, ReturnCodes.malformedRequest, "Invalid Transfer Mode");

				// Update CDR
				cdr.setServiceID(request.getServiceID());
				cdr.setVariantID(transferMode.getTransferModeID());
				cdr.setProcessID("RemoveCreditTransfers");

				// Get Donor Proxy
				Subscriber donor = getSubscriber(context, request.getSubscriberNumber(), transaction);

				try
				{
					// Set Properties
					Properties properties = getProperties(context);
					setDonorProperties(properties, donor, transferMode, cdr);

					// Perform GetAccountDetails UCIP call for the Donor.
					transaction.track(this, "GetAccountDetails");
					donor.getAccountDetails();

					// Test if the Donor Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!isSubscriberActive(donor, transferMode))
						return response.exitWith( cdr, ReturnCodes.notEligible, "Account not Active");

					// Test if the Donor is in one of the allowed service classes
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass donorServiceClass = getServiceClass(donor);
					if (donorServiceClass == null || !transferMode.isValidDonorServiceClass(donorServiceClass.getServiceClassID()))
						return response.exitWith( cdr, ReturnCodes.notEligible, "Wrong SC");

					// Get All Credit Transfers
					Subscriber recipient = request.getMemberNumber() == null ? null : new Subscriber(request.getMemberNumber().toMSISDN(), air, transaction);
					ITemporalTrigger[] creditTransfers = lifecycle.getTemporalTriggers(db, getServiceID(), request.getVariantID(), donor, recipient, transferMode.getTransferModeID());

					// Charge the Donor with the Add Transfer fee
					transaction.track(this, "ChargeDonor");
					long charge = creditTransfers.length * donorServiceClass.getRemoveTransferCharge();
					if (request.getMode() == RequestModes.testOnly)
					{
						cdr.setChargeLevied((int) charge);
						response.setChargeLevied(charge);
						properties.setCharge(locale.formatCurrency(charge));
						transaction.complete();
						return response.exitWith( cdr, ReturnCodes.successfulTest, "Success");
					}
					if (!chargeDonor(null, donor, charge, donorServiceClass, properties, cdr, response))
						return response.exitWith( cdr, ReturnCodes.insufficientBalance, "Insufficient Balance");

					// Remove Transfer Records
					deleteTransferRecords(db, getServiceID(), request.getVariantID(), donor, recipient, transferMode.getTransferModeID());

					// Remove Temporal Triggers
					for (ITemporalTrigger transfer : creditTransfers)
					{
						lifecycle.removeTemporalTrigger(db, transfer);
					}

					// Notify
					for (ITemporalTrigger transfer : creditTransfers)
					{
						if (recipient == null || !recipient.getInternationalNumber().equals(transfer.getMsisdnB()))
							recipient = new Subscriber(transfer.getMsisdnB(), air, transaction);
						properties.setRecipientMSISDN(recipient.getNationalNumber());
						setTransferProperties(properties, transferMode, 0);

						// Set the Result Text
						context.setResultText(getResultText(donor, smsDonorRemovedTransfer, properties));

						// Send a configurable SMS to the Recipient to inform him of the successful transfer addition, the fee which
						// has been charged
						transaction.track(this, "SendRecipientSMS");
						sendSubscriberSMS(recipient, smsRecipientRemovedTransfer, properties);
					}

				}
				catch (AirException e)
				{
					return response.exitWith( cdr, e.getReturnCode(), e);
				}
				catch (SQLException e)
				{
					return response.exitWith( cdr, ReturnCodes.technicalProblem, e);
				}

				// Complete
				transaction.complete();
				return response.exitWith( cdr, ReturnCodes.success, "Success");
			}
		}
		catch (Throwable e)
		{
			return response.exitWith( cdr, ReturnCodes.technicalProblem, e);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Suspend Credit Transfer
	//
	// /////////////////////////////////

	@Override
	public SuspendCreditTransferResponse suspendCreditTransfer(IServiceContext context, SuspendCreditTransferRequest request)
	{
		// Create Response
		SuspendCreditTransferResponse response = super.suspendCreditTransfer(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = SuspendCreditTransferRequest.validate(request);
				if (problem != null)
					return response.exitWith( cdr, ReturnCodes.malformedRequest, problem);

				// Validate Transfer Mode
				TransferMode transferMode = toTransferMode(request.getTransferMode());
				if (transferMode == null)
					return response.exitWith( cdr, ReturnCodes.malformedRequest, "Invalid Transfer Mode");

				// Update CDR
				cdr.setServiceID(getServiceID());
				cdr.setVariantID(transferMode.getTransferModeID());
				cdr.setProcessID("SuspendCreditTransfer");

				// Get Donor Proxy
				Subscriber donor = getSubscriber(context, request.getSubscriberNumber(), transaction);

				try
				{
					// Set Properties
					Properties properties = getProperties(context);
					setDonorProperties(properties, donor, transferMode, cdr);

					// Perform GetAccountDetails UCIP call for the Donor.
					transaction.track(this, "GetAccountDetails");
					donor.getAccountDetails();

					// Test if the Donor Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!isSubscriberActive(donor, transferMode))
						return response.exitWith( cdr, ReturnCodes.notEligible, "Account not Active");

					// Test if the Donor is in one of the allowed service classes
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass donorServiceClass = getServiceClass(donor);
					if (donorServiceClass == null || !transferMode.isValidDonorServiceClass(donorServiceClass.getServiceClassID()))
						return response.exitWith( cdr, ReturnCodes.notEligible, "Wrong SC");

					// Get All Credit Transfers
					Subscriber recipient = request.getMemberNumber() == null ? null : new Subscriber(request.getMemberNumber().toMSISDN(), air, transaction);
					List<TransferRecord> transfers = getTransferRecords(db, getServiceID(), request.getVariantID(), donor, recipient, transferMode.getTransferModeID());

					// Exit if none can be suspended
					int count = 0;
					for (TransferRecord transfer : transfers)
					{
						if (!transfer.isSuspended())
						{
							count++;
						}
					}
					if (count == 0)
						return response.exitWith( cdr, ReturnCodes.cannotBeSuspended, "None to Suspend");

					// Charge the Donor with the Suspend Transfer fee
					transaction.track(this, "ChargeDonor");
					long charge = count * donorServiceClass.getSuspendTransferCharge();
					if (request.getMode() == RequestModes.testOnly)
					{
						cdr.setChargeLevied((int) charge);
						response.setChargeLevied(charge);
						properties.setCharge(locale.formatCurrency(charge));
						transaction.complete();
						return response.exitWith( cdr, ReturnCodes.successfulTest, "Success");
					}
					if (!chargeDonor(null, donor, charge, donorServiceClass, properties, cdr, response))
						return response.exitWith( cdr, ReturnCodes.insufficientBalance, "Insufficient Balance");

					// Suspend Temporal Triggers
					for (TransferRecord transfer : transfers)
					{
						if (!transfer.isSuspended())
						{
							transfer.setSuspended(true);
							db.update(transfer);
						}
					}

					// Notify
					for (TransferRecord transfer : transfers)
					{
						if (recipient == null || !recipient.getInternationalNumber().equals(transfer.getRecipientMsisdn()))
							recipient = new Subscriber(transfer.getRecipientMsisdn(), air, transaction);
						properties.setRecipientMSISDN(recipient.getNationalNumber());
						setTransferProperties(properties, transferMode, transfer.getAmount());

						// Set the Result Text
						context.setResultText(getResultText(donor, smsDonorSuspendedTransfer, properties));

						// Send a configurable SMS to the Recipient to inform him of the successful transfer addition, the fee which
						// has been charged
						transaction.track(this, "SendRecipientSMS");
						sendSubscriberSMS(recipient, smsRecipientSuspendedTransfer, properties);
					}

				}
				catch (AirException e)
				{
					return response.exitWith( cdr, e.getReturnCode(), e);
				}
				catch (SQLException e)
				{
					return response.exitWith( cdr, ReturnCodes.technicalProblem, e);
				}

				// Complete
				transaction.complete();
				return response.exitWith( cdr, ReturnCodes.success, "Success");
			}
		}
		catch (Throwable e)
		{
			return response.exitWith( cdr, ReturnCodes.technicalProblem, e);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Resume Credit Transfer
	//
	// /////////////////////////////////

	@Override
	public ResumeCreditTransferResponse resumeCreditTransfer(IServiceContext context, ResumeCreditTransferRequest request)
	{
		// Create Response
		ResumeCreditTransferResponse response = super.resumeCreditTransfer(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = ResumeCreditTransferRequest.validate(request);
				if (problem != null)
					return response.exitWith( cdr, ReturnCodes.malformedRequest, problem);

				// Validate Transfer Mode
				TransferMode transferMode = toTransferMode(request.getTransferMode());
				if (transferMode == null)
					return response.exitWith( cdr, ReturnCodes.malformedRequest, "Invalid Transfer Mode");

				// Update CDR
				cdr.setServiceID(getServiceID());
				cdr.setVariantID(transferMode.getTransferModeID());
				cdr.setProcessID("ResumeCreditTransfer");

				// Get Donor Proxy
				Subscriber donor = getSubscriber(context, request.getSubscriberNumber(), transaction);

				try
				{
					// Set Properties
					Properties properties = getProperties(context);
					setDonorProperties(properties, donor, transferMode, cdr);

					// Perform GetAccountDetails UCIP call for the Donor.
					transaction.track(this, "GetAccountDetails");
					donor.getAccountDetails();

					// Test if the Donor Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!isSubscriberActive(donor, transferMode))
						return response.exitWith( cdr, ReturnCodes.notEligible, "Account not Active");

					// Test if the Donor is in one of the allowed service classes
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass donorServiceClass = getServiceClass(donor);
					if (donorServiceClass == null || !transferMode.isValidDonorServiceClass(donorServiceClass.getServiceClassID()))
						return response.exitWith( cdr, ReturnCodes.notEligible, "Wrong SC");

					// Get All Credit Transfers
					Subscriber recipient = request.getMemberNumber() == null ? null : new Subscriber(request.getMemberNumber().toMSISDN(), air, transaction);
					List<TransferRecord> transfers = getTransferRecords(db, getServiceID(), request.getVariantID(), donor, recipient, transferMode.getTransferModeID());

					// Exit if none can be suspended
					int count = 0;
					for (TransferRecord transfer : transfers)
					{
						if (transfer.isSuspended())
						{
							count++;
						}
					}
					if (count == 0)
						return response.exitWith( cdr, ReturnCodes.cannotBeResumed, "None to Resume");

					// Charge the Donor with the Suspend Transfer fee
					transaction.track(this, "ChargeDonor");
					long charge = count * donorServiceClass.getResumeTransferCharge();
					if (request.getMode() == RequestModes.testOnly)
					{
						cdr.setChargeLevied((int) charge);
						response.setChargeLevied(charge);
						properties.setCharge(locale.formatCurrency(charge));
						transaction.complete();
						return response.exitWith( cdr, ReturnCodes.successfulTest, "Success");
					}
					if (!chargeDonor(null, donor, charge, donorServiceClass, properties, cdr, response))
						return response.exitWith( cdr, ReturnCodes.insufficientBalance, "Insufficient Balance");

					// Suspend Temporal Triggers
					for (TransferRecord transfer : transfers)
					{
						if (transfer.isSuspended())
						{
							transfer.setSuspended(false);
							db.update(transfer);
						}
					}

					// Notify
					for (TransferRecord transfer : transfers)
					{
						if (recipient == null || !recipient.getInternationalNumber().equals(transfer.getRecipientMsisdn()))
							recipient = new Subscriber(transfer.getRecipientMsisdn(), air, transaction);
						properties.setRecipientMSISDN(recipient.getNationalNumber());
						setTransferProperties(properties, transferMode, transfer.getAmount());

						// Set the Result Text
						sendSubscriberSMS(donor, smsDonorResumedTransfer, properties);

						// Send a configurable SMS to the Recipient to inform him of the successful transfer addition, the fee which
						// has been charged
						transaction.track(this, "SendRecipientSMS");
						sendSubscriberSMS(recipient, smsRecipientResumedTransfer, properties);
					}

				}
				catch (AirException e)
				{
					return response.exitWith( cdr, e.getReturnCode(), e);
				}
				catch (SQLException e)
				{
					return response.exitWith( cdr, ReturnCodes.technicalProblem, e);
				}

				// Complete
				transaction.complete();
				return response.exitWith( cdr, ReturnCodes.success, "Success");
			}
		}
		catch (Throwable e)
		{
			return response.exitWith( cdr, ReturnCodes.technicalProblem, e);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Process Temporal Events
	//
	// /////////////////////////////////

	@Override
	protected void performPeriodicTransfer(ITemporalTrigger trigger)
	{
		// Create a CDR
		CdrBase cdr = new CsvCdr();
		cdr.setHostName(HostInfo.getName());
		cdr.setCallerID("LCYCL");
		cdr.setA_MSISDN(trigger.getMsisdnA());
		cdr.setB_MSISDN(trigger.getMsisdnB());
		cdr.setStartTime(trigger.getNextDateTime());
		cdr.setInboundTransactionID("0");
		cdr.setInboundSessionID("0");
		cdr.setChannel(Channels.INTERNAL);
		cdr.setRequestMode(RequestModes.normal);
		cdr.setTransactionID(esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));
		cdr.setServiceID(trigger.getServiceID());
		cdr.setVariantID(trigger.getVariantID());
		cdr.setProcessID("PerformPeriodicTransfer");

		// Create a response
		ResponseHeader response = new ResponseHeader(cdr.getInboundTransactionID(), cdr.getInboundSessionID());

		// Perform the Transfer
		response = performPeriodicTransfer(response, trigger, cdr);

		// Remove if Unsuccessful
		if (response.getReturnCode() == ReturnCodes.notEligible)
		{
			try (IDatabaseConnection db = database.getConnection(null))
			{
				lifecycle.removeTemporalTrigger(db, trigger);
			}
			catch (Exception e)
			{
				logger.error("Failed to remove temporal trigger", e);
			}
		}

	}

	private ResponseHeader performPeriodicTransfer(ResponseHeader response, ITemporalTrigger triggr, CdrBase cdr)
	{

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Wrap the Trigger
			TransferRecord transferRecord = getTransferRecord(db, triggr);

			// Get the Transfer Mode
			TransferMode transferMode = toTransferMode(transferRecord.getTransferModeID());
			if (transferMode == null)
				return response.exitWith( cdr, ReturnCodes.malformedRequest, "Invalid Transfer Mode");

			// Validate the Transfer Type
			boolean isUponDepletion = transferMode.isUponDepletion();
			if (!isUponDepletion && !transferMode.isPeriodic())
				return response.exitWith( cdr, ReturnCodes.malformedRequest, "Invalid Transfer Mode");

			// Update Trigger
			triggr.setBeingProcessed(false);
			Date lastTransferDate = transferRecord.getLastTransferDate();
			if (lastTransferDate == null)
				lastTransferDate = triggr.getNextDateTime();
			DateTime nextTime = new DateTime(lastTransferDate).add(transferMode.getInterval(), transferMode.getIntervalType());
			triggr.setNextDateTime(nextTime);
			transferRecord.setTotalTransferred(0L);
			lifecycle.updateTemporalTrigger(db, triggr);
			db.update(transferRecord);

			// Reset UponDepletion Total
			if (isUponDepletion && !transferRecord.isBeingRetried())
				return response.exitWith( cdr, ReturnCodes.success, "UponDepletion Total Reset");

			response = performCommonTransfer(db, transferRecord.getDonorMsisdn(), transferRecord.getRecipientMsisdn(), transferRecord.amount, //
					transferRecord, transferMode, cdr, new ServiceContext(), NO_PIN, response, false);

			// Update Trigger
			if (transferRecord.isChanged())
			{
				transferRecord.setChanged(false);
				db.update(transferRecord);
			}

			return response;
		}
		catch (Throwable e)
		{
			return response.exitWith( cdr, ReturnCodes.technicalProblem, e);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Upon Depletion Transfers
	//
	// /////////////////////////////////
	@Override
	protected boolean canPerformUponDepletionTransfer(ThresholdNotificationFileV2 message)
	{
		if (message.thresholdDirection != 'd')
			return false;

		for (TransferMode transferMode : config.getTransferModes())
		{
			if (transferMode.getTransferType() != CreditTransferType.UponDepletion)
				continue;

			if (transferMode.getThresholdID() == message.thresholdID //
					&& transferMode.isValidRecipientServiceClass(message.serviceClassID))
				return true;
		}
		return false;
	}

	@Override
	protected boolean canPerformUponDepletionTransfer(ThresholdNotificationFileV3 message)
	{
		if (message.thresholdDirection != 'd')
			return false;

		for (TransferMode transferMode : config.getTransferModes())
		{
			if (transferMode.getTransferType() != CreditTransferType.UponDepletion)
				continue;

			if (transferMode.getThresholdID() == message.thresholdID //
					&& transferMode.isValidRecipientServiceClass(message.serviceClassID))
				return true;
		}
		return false;
	}

	@Override
	protected void performUponDepletionTransfer(ThresholdNotificationFileV2 message)
	{
		// Create a CDR
		CdrBase cdr = new CsvCdr();
		cdr.setHostName(HostInfo.getName());
		cdr.setCallerID("LCYCL");
		cdr.setB_MSISDN(message.subscriberID);
		cdr.setInboundTransactionID("0");
		cdr.setInboundSessionID("0");
		cdr.setChannel(Channels.INTERNAL);
		cdr.setRequestMode(RequestModes.normal);
		cdr.setTransactionID(esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));
		cdr.setServiceID(getServiceID());
		cdr.setProcessID("performUponDepletionTransfer");

		// Create a response
		ResponseHeader response = new ResponseHeader(cdr.getInboundTransactionID(), cdr.getInboundSessionID());

		// Perform the Transfer
		response = performUponDepletionTransfer(response, message.subscriberID, message.thresholdID, cdr);

	}

	@Override
	protected void performUponDepletionTransfer(ThresholdNotificationFileV3 message)
	{
		// Create a CDR
		CdrBase cdr = new CsvCdr();
		cdr.setHostName(HostInfo.getName());
		cdr.setCallerID("LCYCL");
		cdr.setB_MSISDN(message.subscriberID);
		cdr.setInboundTransactionID("0");
		cdr.setInboundSessionID("0");
		cdr.setChannel(Channels.INTERNAL);
		cdr.setRequestMode(RequestModes.normal);
		cdr.setTransactionID(esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));
		cdr.setServiceID(getServiceID());
		cdr.setProcessID("performUponDepletionTransfer");

		// Create a response
		ResponseHeader response = new ResponseHeader(cdr.getInboundTransactionID(), cdr.getInboundSessionID());

		// Perform the Transfer
		response = performUponDepletionTransfer(response, message.subscriberID, message.thresholdID, cdr);

	}

	private ResponseHeader performUponDepletionTransfer(ResponseHeader response, String subscriberID, int thresholdID, CdrBase cdr)
	{

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Get Temporal Triggers for recipient
			Subscriber recipient = new Subscriber(subscriberID, air, null);
			List<TransferRecord> records = getTransferRecords(db, getServiceID(), null, null, recipient, null);

			// Find
			TransferMode transferMode = null;
			TransferRecord transferRecord = null;
			for (TransferRecord record : records)
			{
				transferMode = TransferMode.findByID(config.transferModes, record.getTransferModeID());
				if (transferMode != null && transferMode.getThresholdID() == thresholdID)
				{
					transferRecord = record;
					break;
				}
			}
			if (transferRecord == null)
				return response.exitWith( cdr, ReturnCodes.success, "ThresholdID %d no Action", thresholdID);

			response = performCommonTransfer(db, transferRecord.getDonorMsisdn(), transferRecord.getRecipientMsisdn(), transferRecord.getAmount(), //
					transferRecord, transferMode, cdr, new ServiceContext(), NO_PIN, response, false);

			// Update Trigger
			if (transferRecord.isChanged())
			{
				transferRecord.setChanged(false);
				db.update(transferRecord);
			}

			return response;

		}
		catch (Throwable e)
		{
			return response.exitWith( cdr, ReturnCodes.technicalProblem, e);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Add Hoc Transfer
	//
	// /////////////////////////////////
	@Override
	public TransferResponse transfer(IServiceContext context, TransferRequest request)
	{
		// Create Response
		TransferResponse response = super.transfer(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Update CDR
		cdr.setServiceID(request.getServiceID());
		cdr.setVariantID(request.getVariantID());
		cdr.setProcessID("Transfer");

		// Validate Request
		String problem = TransferRequest.validate(request);
		if (problem != null)
		{
			return response.exitWith( cdr, ReturnCodes.malformedRequest, problem);
		}

		// Get the TransferMode
		TransferMode transferMode = toTransferMode(request.getTransferModeID());
		if (transferMode == null || transferMode.getTransferType() != CreditTransferType.OnceOff)
			return response.exitWith( cdr, ReturnCodes.invalidVariant, "Invalid Transfer Mode");

		// Test if the Provider is attempting to send to Self in the same account.
		if (request.getSubscriberNumber().equals(request.getRecipientNumber()) && //
				transferMode.getDonorAccountID() == transferMode.getRecipientAccountID())
			return response.exitWith( cdr, ReturnCodes.cannotTransferToSelf, "Cannot Transfer Self");

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			return performCommonTransfer(db, request.getSubscriberNumber().toMSISDN(), request.getRecipientNumber().toMSISDN(), //
					request.getAmount(), null, transferMode, cdr, context, request.getPin(), response, request.getMode() == RequestModes.testOnly);
		}
		catch (Throwable e)
		{
			return response.exitWith( cdr, ReturnCodes.technicalProblem, e);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Common Transfer Mechanism
	//
	// /////////////////////////////////

	// Common Transfer procedure
	private <T extends ResponseHeader> T performCommonTransfer(IDatabaseConnection db, String donorMsisdn, String recipientMsisdn, long targetAmount, //
			TransferRecord transferRecord, TransferMode transferMode, CdrBase cdr, IServiceContext context, String pin, T response, //
			boolean testOnly) throws IOException
	{
		boolean isInteractive = cdr.getChannel() != Channels.INTERNAL;

		// Update CDR
		cdr.setServiceID(getServiceID());

		// Reset retry count and flag
		boolean beingRetried = false;
		int attemptNo = 1;
		if (transferRecord != null)
		{
			transferRecord.setChanged(false);
			beingRetried = transferRecord.isBeingRetried();
			if (beingRetried)
				transferRecord.setBeingRetried(false);
			attemptNo = transferRecord.getAttemptNo();
			if (attemptNo != 1)
				transferRecord.setAttemptNo(1);

			// Test if Number of Transfers exceed
			int newCount = transferRecord.getTransferCount() + 1;
			if (transferMode.getMaxCountPerPeriod() != null && newCount > transferMode.getMaxCountPerPeriod())
				return response.exitWith( cdr, ReturnCodes.maxCountExceeded, "Max Transfers Exceeded");
			transferRecord.setTransferCount(newCount);

		}

		// Transaction Reversal Scope
		try (Transaction<?> transaction = transactions.create(cdr, db))
		{
			// Create Donor Proxy
			Subscriber donor = new Subscriber(donorMsisdn, air, transaction);

			// Create Recipient Proxy
			Subscriber recipient = donorMsisdn.equals(recipientMsisdn) ? donor : new Subscriber(recipientMsisdn, air, transaction);

			// Test if transferring to self
			if (donor.getInternationalNumber().equals(recipient.getInternationalNumber()))
			{
				recipient = donor;
				if (transferMode.getDonorAccountID() == transferMode.getRecipientAccountID())
					return response.exitWith( cdr, ReturnCodes.cannotTransferToSelf, "Cannot Transfer to Self");
			}

			// Validate PIN
			if (transferMode.getRequiresPIN() && !NO_PIN.equals(pin) && //
					!pinService.validatePIN(response, cdr, db, IPinService.DEFAULT_VARIANT_ID, donor.getInternationalNumber(), pin))
				return response;

			try
			{
				// Update CDR
				cdr.setA_MSISDN(donor.getInternationalNumber());
				cdr.setB_MSISDN(recipient.getInternationalNumber());
				cdr.setVariantID(transferMode.getTransferModeID());

				// Set Properties
				Properties properties = new Properties();
				properties.setDonorMSISDN(donor.getNationalNumber());
				properties.setRecipientMSISDN(recipient.getNationalNumber());
				properties.setUnits(transferMode.getUnits());

				// Exit if suspended
				if (transferRecord != null && transferRecord.isSuspended())
					return response.exitWith( cdr, ReturnCodes.suspended, "Transfer Suspended");

				// Perform GetAccountDetails UCIP call for the Donor.
				transaction.track(this, "GetAccountDetails");
				donor.getAccountDetails();

				// Test if the Donor Account is in a valid state, i.e. all lifecycle dates are in the future.
				transaction.track(this, "CheckAccountActive");
				if (!isSubscriberActive(donor, transferMode))
					return response.exitWith( cdr, ReturnCodes.notEligible, "Account not Active");

				// Test if the Donor is in one of the allowed service classes
				transaction.track(this, "CheckValidServiceClass");
				ServiceClass donorServiceClass = getServiceClass(donor);
				if (donorServiceClass == null || !transferMode.isValidDonorServiceClass(donorServiceClass.getServiceClassID()))
					return response.exitWith( cdr, ReturnCodes.notEligible, "Wrong SC");

				// Perform GetAccountDetails UCIP call for the Recipient.
				transaction.track(this, "GetAccountDetails");
				recipient.getAccountDetails();

				// Test if the Recipient Account is in a valid state, i.e. all lifecycle dates are in the future.
				transaction.track(this, "CheckAccountActive");
				if (!isSubscriberActive(recipient, transferMode))
					return response.exitWith( cdr, ReturnCodes.notEligible, "Account not Active");

				// Test if the Recipient is in one of the allowed service classes
				transaction.track(this, "CheckValidServiceClass");
				if (!transferMode.isValidRecipientServiceClass(recipient.getServiceClassCurrent()))
					return response.exitWith( cdr, ReturnCodes.notEligible, "Wrong SC");

				// Exit if transfer is not required (Amount too small)
				if (targetAmount <= 0 || targetAmount < transferMode.getMinAmount())
				{
					// Inform the Donor
					transaction.track(this, "InformDonor");
					informDonor(donor, smsDonorTooSmall, isInteractive, context, properties);
					properties.setMinQuantity(transferMode.formatRecipientQuantity(transferMode.getMinAmount()));
					return response.exitWith( cdr, ReturnCodes.quantityTooSmall, "Too Small");
				}

				// Test if the Donor Balance is not to Low or High
				int donorAccountID = transferMode.getDonorAccountID();
				if (donorAccountID == 0 && donorServiceClass.isPostPaid())
					donorAccountID = config.getPostPaidAccountID();

				Long donorBalance = donor.getAccountBalance(donorAccountID);
				if (donorBalance == null)
				{
				}
				else if (donorBalance < transferMode.getDonorMinBalance())
				{
					// Inform the Donor
					transaction.track(this, "InformDonor");
					informDonor(donor, smsDonorDonorTooPoor, isInteractive, context, properties);

					// Send Recipient SMS
					transaction.track(this, "SendRecipientSMS");
					sendSubscriberSMS(recipient, smsRecipientDonorTooPoor, properties);

					// Attempt to Retry
					attemptToRetry(transferRecord, transferMode, attemptNo, transaction, donor, recipient, properties, false);

					return response.exitWith( cdr, ReturnCodes.insufficientBalance, "Donor Balance too low");
				}
				else if (donorBalance > transferMode.getDonorMaxBalance())
				{
					// Inform the Donor
					transaction.track(this, "InformDonor");
					informDonor(donor, smsDonorDonorTooRich, isInteractive, context, properties);

					// Send Recipient SMS
					transaction.track(this, "SendRecipientSMS");
					sendSubscriberSMS(recipient, smsRecipientDonorTooRich, properties);

					// Attempt to Retry
					attemptToRetry(transferRecord, transferMode, attemptNo, transaction, donor, recipient, properties, false);

					return response.exitWith( cdr, ReturnCodes.excessiveBalance, "Donor Balance too high");
				}

				// Test if the Recipient Balance is not to Low or High
				Long recipientBalance = recipient.getAccountBalance(transferMode.getRecipientAccountID());
				if (recipientBalance == null)
				{
					recipientBalance = 0L;
				}
				if (recipientBalance < transferMode.getRecipientMinBalance())
				{
					// Inform the Donor
					transaction.track(this, "InformDonor");
					informDonor(donor, smsDonorRecipientTooPoor, isInteractive, context, properties);

					// Send Recipient SMS
					transaction.track(this, "SendRecipientSMS");
					sendSubscriberSMS(recipient, smsRecipientRecipientTooPoor, properties);

					// Attempt to Retry
					attemptToRetry(transferRecord, transferMode, attemptNo, transaction, donor, recipient, properties, false);

					return response.exitWith( cdr, ReturnCodes.insufficientBalance, "Recipient Balance too low");
				}
				else if (recipientBalance > transferMode.getRecipientMaxBalance())
				{
					// Inform the Donor
					transaction.track(this, "InformDonor");
					informDonor(donor, smsDonorRecipientTooRich, isInteractive, context, properties);

					// Send Recipient SMS
					transaction.track(this, "SendRecipientSMS");
					sendSubscriberSMS(recipient, smsRecipientRecipientTooRich, properties);

					// Attempt to Retry
					attemptToRetry(transferRecord, transferMode, attemptNo, transaction, donor, recipient, properties, false);

					return response.exitWith( cdr, ReturnCodes.excessiveBalance, "Recipient Balance too high");
				}

				// Once-Off Transfers
				if (transferMode.getTransferType() == CreditTransferType.OnceOff)
				{
					try
					{
						// Load the Transfer Record
						List<TransferRecord> transferRecords = getTransferRecords(db, getServiceID(), VARIANT_ANY, donorMsisdn, "", transferMode.getTransferModeID());
						TransferRecord manualTransferRecord = transferRecords.size() > 0 ? transferRecords.get(0) : null;

						// Create Transfer Record if it doesn't extist
						boolean isNew = manualTransferRecord == null;
						if (isNew)
						{
							manualTransferRecord = new TransferRecord(
							/* serviceID */getServiceID(),
							/* variantID */VARIANT_ANY,
							/* transferModeID */transferMode.getTransferModeID(),
							/* donorMsisdn */donorMsisdn,
							/* recipientMsisdn */"",
							/* nextTransferDate */new Date(),
							/* amount */0L,
							/* transferLimit */transferMode.getMaxAmountPerPeriod(),
							/* transferThreshold */null);
						}

						// Reset Transfer record and expiry of period
						Date now = new Date();
						while (true)
						{
							DateTime endTime = new DateTime(manualTransferRecord.getNextTransferDate());
							endTime = endTime.add(transferMode.getInterval(), transferMode.getIntervalType());
							if (now.before(endTime))
								break;
							manualTransferRecord.setTotalTransferred(0L);
							manualTransferRecord.setTransferCount(0);
							manualTransferRecord.setNextTransferDate(endTime);
						}

						// Test if Number of Transfers exceed
						int newCount = manualTransferRecord.getTransferCount() + 1;
						if (transferMode.getMaxCountPerPeriod() != null && newCount > transferMode.getMaxCountPerPeriod())
							return response.exitWith( cdr, ReturnCodes.maxCountExceeded, "Max Transfers Exceeded");
						manualTransferRecord.setTransferCount(newCount);

						// Test if Total Amount is exceeded
						long newTotalTransferred = manualTransferRecord.getTotalTransferred() + targetAmount;
						if (transferMode.getMaxAmountPerPeriod() != null && newTotalTransferred > transferMode.getMaxAmountPerPeriod())
							return response.exitWith( cdr, ReturnCodes.maxAmountExceeded, "Max Amount Exceeded");
						manualTransferRecord.setTotalTransferred(newTotalTransferred);

						// Update Transfer Record but roll back if subsequent transaction fails
						if (!testOnly)
						{
							if (isNew)
								db.insert(manualTransferRecord);
							else
								db.update(manualTransferRecord);
						}
					}
					catch (Exception ex)
					{
						return response.exitWith( cdr, ReturnCodes.technicalProblem, ex);
					}
				}

				// Process TopUp if requested
				if (transferMode.isTopUpOnly())
				{
					targetAmount -= recipientBalance;
				}

				// Reduce amount to not exceed Transfer Limit
				if (transferRecord != null && transferRecord.getTransferLimit() != null && //
						transferRecord.getTotalTransferred() + targetAmount > transferRecord.getTransferLimit())
					targetAmount = transferRecord.getTransferLimit() - transferRecord.getTotalTransferred();

				// Exit if transfer is not required (Amount too small)
				if (targetAmount <= 0 || targetAmount < transferMode.getMinAmount())
				{
					// Inform the Donor
					transaction.track(this, "InformDonor");
					informDonor(donor, smsDonorNotRequired, isInteractive, context, properties);

					// Send Recipient SMS
					transaction.track(this, "SendRecipientSMS");
					sendSubscriberSMS(recipient, smsRecipientNotRequired, properties);

					return response.exitWith( cdr, ReturnCodes.success, "Not Required");
				}

				// Calculate the Charge
				setTransferProperties(properties, transferMode, targetAmount);
				long sourceAmount = TransferMode.unScale(targetAmount * transferMode.getConversionRate());
				long commission = transferMode.getCommissionAmount() + (sourceAmount * transferMode.getCommissionPercentage() + 5000) / 10000;
				long charge = commission;
				boolean isFromMain = transferMode.getDonorAccountID() == 0;
				if (isFromMain)
					charge += sourceAmount;
				cdr.setChargeLevied((int) charge);
				properties.setCharge(locale.formatCurrency(charge));

				// Charge the Donor with the price
				transaction.track(this, "ChargeDonor");
				UpdateParameters donorParams = new UpdateParameters(config.donorTransactionType, config.donorTransactionCode,//
						config.flagDonorPromotionNotification, config.flagDonorFirstIVRCallSet, config.flagDonorAccountActivation);
				if (!chargeDonor(donorParams, donor, charge, donorServiceClass, properties, cdr, response))
				{

					// Inform the Donor
					transaction.track(this, "InformDonor");
					informDonor(donor, smsDonorDonorTooPoor, isInteractive, context, properties);

					// Send Recipient SMS
					transaction.track(this, "SendRecipientSMS");
					sendSubscriberSMS(recipient, smsRecipientDonorTooPoor, properties);

					// Attempt to Retry
					attemptToRetry(transferRecord, transferMode, attemptNo, transaction, donor, recipient, properties, true);

					return response.exitWith( cdr, ReturnCodes.insufficientBalance, "Insufficient Balance for Transfer");
				}

				// Debit the Donor DA
				if (!isFromMain)
				{
					transaction.track(this, "Debit Donor");
					AccountUpdate daUpdate = new AccountUpdate(transferMode.getDonorAccountID(), transferMode.getDonorAccountType(), -sourceAmount, null, null, null, null, null);
					donor.updateAccounts(donorParams, null, daUpdate);
				}

				// Credit the Recipient
				UpdateParameters recipientParams = new UpdateParameters(config.recipientTransactionType, config.recipientTransactionCode,//
						config.flagRecipientPromotionNotification, config.flagRecipientFirstIVRCallSet, config.flagRecipientAccountActivation);
				transaction.track(this, "Credit Recipient");
				boolean isToMain = transferMode.getRecipientAccountID() == 0;
				if (isToMain)
				{
					recipient.updateAccounts(recipientParams, targetAmount);
				}
				else
				{
					AccountUpdate daUpdate = new AccountUpdate( //
							transferMode.getRecipientAccountID(), transferMode.getRecipientAccountType(), targetAmount, null, //
							transferMode.getRecipientExpiryDays(), null, null, null);
					recipient.updateAccounts(recipientParams, null, daUpdate);
				}

				// Update the Temporal Trigger
				if (transferRecord != null)
				{
					transferRecord.setTotalTransferred(transferRecord.getTotalTransferred() + targetAmount);
					transferRecord.setLastTransferDate(new Date());
				}

				// Inform the Donor
				transaction.track(this, "InformDonor");
				informDonor(donor, smsDonorTransferred, isInteractive, context, properties);

				// Send Recipient SMS
				transaction.track(this, "SendRecipientSMS");
				sendSubscriberSMS(recipient, smsRecipientReceived, properties);
			}
			catch (AirException e)
			{
				return response.exitWith( cdr, e.getReturnCode(), e);
			}

			// Complete
			transaction.complete();
			return response.exitWith( cdr, ReturnCodes.success, "Success");
		}
	}

	private boolean attemptToRetry(TransferRecord transferRecord, TransferMode transferMode, int attemptNo, Transaction<?> transaction, Subscriber donor, Subscriber recipient, Properties properties, boolean maySuspend)
	{
		// Test if we can Retry
		if (transferMode.isPeriodic() || transferMode.isUponDepletion())
		{
			if (attemptNo < transferMode.getMaxRetries())
			{
				// Update the Transfer Trigger
				transferRecord.setAttemptNo(attemptNo + 1);
				transferRecord.setBeingRetried(true);
				int minutes = transferMode.getRetryIntervalMinutes();
				Date nextAttemptTime = DateTime.getNow().addMinutes(minutes);
				transferRecord.setNextTransferDate(nextAttemptTime);

				// Send Donor SMS
				properties.setRetryInterval(Integer.toString(minutes));
				transaction.track(this, "SendDonorSMS");
				sendSubscriberSMS(donor, smsDonorRetry, properties);

				// Send Recipient SMS
				transaction.track(this, "SendRecipientSMS");
				sendSubscriberSMS(recipient, smsRecipientRetry, properties);

				return true;
			}
			else if (maySuspend && transferMode.isAutoSuspend())
			{
				transaction.track(this, "Suspend Transfer");
				transferRecord.setSuspended(true);

				// Inform the Donor
				transaction.track(this, "InformDonor");
				sendSubscriberSMS(donor, smsDonorSuspendedTransfer, properties);

				// Send Recipient SMS
				transaction.track(this, "SendRecipientSMS");
				sendSubscriberSMS(recipient, smsRecipientSuspendedTransfer, properties);
			}
		}

		return false;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Process Lifecycle Events
	//
	// /////////////////////////////////

	@Override
	public ProcessLifecycleEventResponse processLifecycleEvent(IServiceContext context, ProcessLifecycleEventRequest request)
	{
		// Create Response
		ProcessLifecycleEventResponse response = super.processLifecycleEvent(context, request);

		// Create Context
		if (context == null)
			context = new ServiceContext();

		// Cast Subscription
		ISubscription subscription = (ISubscription) request;

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{

			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = ProcessLifecycleEventRequest.validate(request);
				if (problem != null)
					return response.exitWith( cdr, ReturnCodes.malformedRequest, problem);

				// Validate Variant
				Variant variant = toVariant(request.getVariantID());
				if (variant == null)
					return response.exitWith( cdr, ReturnCodes.malformedRequest, "Invalid VariantID");

				// Update CDR
				cdr.setServiceID(request.getServiceID());
				cdr.setVariantID(request.getVariantID());
				cdr.setProcessID("ProcessLifecycleEvent");

				// Get Donor Proxy
				Subscriber donor = getSubscriber(context, new Number(subscription.getMsisdn()), transaction);
				try
				{
					// Get Properties
					Properties properties = getProperties(context);
					setDonorProperties(properties, donor, variant, cdr);

					// Perform GetAccountDetails UCIP call for the Donor.
					transaction.track(this, "GetAccountDetails");
					donor.getAccountDetails();

					// Test if the Donor Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!isSubscriberActive(donor, null))
						return response.exitWith( cdr, ReturnCodes.notEligible, "Donor Account not Active");

					// Test if the Donor is in one of the allowed service classes (S0571).
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass donorServiceClass = getServiceClass(donor);
					if (donorServiceClass == null || !variant.isEligibleFor(donorServiceClass.getServiceClassID()))
						return response.exitWith( cdr, ReturnCodes.notEligible, "Wrong Donor SC");

					// Test if the Donor is subscribed to this variant of the Credit Sharing Service, by checking the presence of the Subscription OfferID (S0529) with a GetOffers UCIP call.
					transaction.track(this, "CheckIfSubscribed");
					donor.getOffers(true, false, false, true, true);
					if (!lifecycle.isSubscribed(db, donor, getServiceID(), variant.getVariantID()))
						return response.exitWith( cdr, ReturnCodes.notSubscribed, "Not Subscribed");
					if (request.getDateTime3() == null)
						return response.exitWith( cdr, ReturnCodes.malformedRequest, "Invalid Date Time 3");
					DateTime renewalTime = new DateTime(request.getDateTime3());
					DateTime expiryDate = renewalTime.addSeconds(-1);
					setExpiryDateProperty(donor, properties, expiryDate);

					// Calculate hours before Renewal
					DateTime now = DateTime.getNow();
					long hoursBeforeExpiry = now.millisTo(renewalTime) / DateTime.MILLIS_PER_HOUR;
					properties.setHoursBeforeExpiry(Long.toString(hoursBeforeExpiry));

					// Switch on the State
					switch (subscription.getState())
					{
						case NEXT_SEND_FIRST_RENEW_WARNING:
							calculateRenewalCost(db, donor, variant, properties, cdr, transaction);
							transaction.track(this, "SendDonorSMS");
							sendSubscriberSMS(donor, smsDonorRenewalWarning, properties);
							if (variant.getSecondRenewalWarningHoursBefore() != 0)
							{
								subscription.setState(NEXT_SEND_SECOND_RENEW_WARNING);
								subscription.setNextDateTime(renewalTime.addHours(-variant.getSecondRenewalWarningHoursBefore()));
							}
							else
							{
								subscription.setState(NEXT_RENEW_SUBSCRIPTION);
								subscription.setNextDateTime(renewalTime);
							}
							subscription.setBeingProcessed(false);
							lifecycle.updateSubscription(db, subscription);
							break;

						case NEXT_SEND_SECOND_RENEW_WARNING:
							calculateRenewalCost(db, donor, variant, properties, cdr, transaction);
							transaction.track(this, "SendDonorSMS");
							sendSubscriberSMS(donor, smsDonorRenewalWarning, properties);
							subscription.setState(NEXT_RENEW_SUBSCRIPTION);
							subscription.setNextDateTime(renewalTime);
							subscription.setBeingProcessed(false);
							lifecycle.updateSubscription(db, subscription);
							break;

						case NEXT_RENEW_SUBSCRIPTION:
							renewSubscription(db, donor, variant, properties, cdr, transaction, donorServiceClass, response, expiryDate, context);
							if (response.getReturnCode() != ReturnCodes.success)
								return response;
							break;

						default:
							return response.exitWith( cdr, ReturnCodes.notEligible, "Invalid Lifecycle State");
					}

				}
				catch (AirException e)
				{
					return response.exitWith( cdr, e.getReturnCode(), e);
				}

				// Complete
				transaction.complete();
				return response.exitWith( cdr, ReturnCodes.success, "Success");
			}

		}

		catch (Throwable e)
		{
			return response.exitWith( cdr, ReturnCodes.technicalProblem, e);
		}

	}

	private ProcessLifecycleEventResponse renewSubscription(IDatabaseConnection db, Subscriber donor, Variant variant, //
			Properties properties, ICdr cdr, Transaction<?> transaction, ServiceClass donorServiceClass, //
			ProcessLifecycleEventResponse response, DateTime expiryDate, IServiceContext context) throws SQLException, AirException
	{
		long charge = variant.getRenewalCharge();
		DateTime newExpiryDate = expiryDate.add(variant.getSafeValidityPeriod(), variant.getValidityPeriodUnit());

		// Get List of Recipients
		String[] recipientList = lifecycle.getMembers(db, donor, getServiceID(), variant.getVariantID());

		// Charge the Donor with the total renewal charge
		transaction.track(this, "ChargeDonor");
		if (!chargeDonor(null, donor, charge, donorServiceClass, properties, cdr, response))
		{
			properties.setCharge(locale.formatCurrency(0L));
			unsubscribe(db, donor, recipientList, variant, cdr, transaction, properties, context);
			transaction.complete();
			return response.exitWith( cdr, ReturnCodes.insufficientBalance, "Unsubscribed due to Insufficient Balance");
		}

		// Update the Lifecycle Store
		addDonorLifecycle(db, donor, variant, newExpiryDate);

		// Set the Result Text
		properties.setNewExpiryDate(locale.formatDate(newExpiryDate, donor.getLanguageID()));
		properties.setNewExpiryTime(locale.formatTime(newExpiryDate, donor.getLanguageID()));
		String resultText = getResultText(donor, smsDonorRenewed, properties);
		context.setResultText(resultText);
		sendSubscriberSMS(donor, smsDonorRenewed, properties);

		return response.exitWith( cdr, ReturnCodes.success, "success");

	}

	private long calculateRenewalCost(IDatabaseConnection db, Subscriber donor, Variant variant, //
			Properties properties, ICdr cdr, Transaction<?> transaction) throws SQLException, AirException
	{
		long result = variant.getRenewalCharge();
		properties.setCharge(locale.formatCurrency(result));
		return result;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// SDP File Triggered Transfers
	//
	// /////////////////////////////////

	@Override
	protected boolean canPerformUponDepletionTransfer(String msisdn, int accountID, double balance)
	{
		// Create Account to Transfer Mode ID Map if Required
		Map<Integer, String[]> accountMap = super.sdpAccountMap;
		if (accountMap == null)
		{
			accountMap = new HashMap<Integer, String[]>();
			for (TransferMode mode : config.getTransferModes())
			{
				if (mode.getTransferType() == CreditTransferType.UponDepletion && mode.getRecipientAccountID() == accountID)
				{
					String[] accountModeIDs = accountMap.get(accountID);
					if (accountModeIDs == null)
						accountModeIDs = new String[1];
					else
						accountModeIDs = java.util.Arrays.copyOf(accountModeIDs, accountModeIDs.length + 1);
					accountModeIDs[accountModeIDs.length - 1] = mode.getTransferModeID();
					accountMap.put(accountID, accountModeIDs);
				}
			}
			super.sdpAccountMap = accountMap;
		}

		// Get candidate Transfer Mode IDs
		String[] transferModeIDs = accountMap.get(accountID);
		if (transferModeIDs == null || transferModeIDs.length == 0)
			return false;

		// Check Lifecycle Database if Transfer must be performed
		try (IDatabaseConnection db = database.getConnection(null))
		{
			Subscriber recipient = new Subscriber(msisdn, air, null);

			for (String transferModeID : transferModeIDs)
			{
				List<TransferRecord> records = getTransferRecords(db, getServiceID(), null, null, recipient, transferModeID);

				for (TransferRecord record : records)
				{
					Long transferThreshold = record.getTransferThreshold();
					if (transferThreshold != null && transferThreshold > balance)
						return true;
				}

			}
		}
		catch (Exception e)
		{
			logger.error("Failed to verify transfer threshold", e);
			return false;
		}

		return false;
	}

	@Override
	protected void performUponDepletionTransfer(String msisdn, int accountID, double balance)
	{
		// Create Account to Transfer Mode ID Map if Required
		Map<Integer, String[]> accountMap = super.sdpAccountMap;
		if (accountMap == null)
			return;

		// Get candidate Account Mode IDs
		String[] transferModeIDs = accountMap.get(accountID);
		if (transferModeIDs == null || transferModeIDs.length == 0)
			return;

		// Check Lifecycle Database which Transfer must be performed
		try (IDatabaseConnection db = database.getConnection(null))
		{
			Subscriber recipient = new Subscriber(msisdn, air, null);

			for (String transferModeID : transferModeIDs)
			{

				List<TransferRecord> records = getTransferRecords(db, getServiceID(), null, null, recipient, transferModeID);

				for (TransferRecord record : records)
				{
					Long transferThreshold = record.getTransferThreshold();
					if (transferThreshold != null && transferThreshold > balance)
					{
						// Create a CDR
						CdrBase cdr = new CsvCdr();
						cdr.setHostName(HostInfo.getName());
						cdr.setCallerID("FP");
						cdr.setB_MSISDN(recipient.getInternationalNumber());
						cdr.setInboundTransactionID("0");
						cdr.setInboundSessionID("0");
						cdr.setChannel(Channels.INTERNAL);
						cdr.setRequestMode(RequestModes.normal);
						cdr.setTransactionID(esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));
						cdr.setServiceID(getServiceID());
						cdr.setProcessID("performUponDepletionTransfer");

						// Create a response
						ResponseHeader response = new ResponseHeader(cdr.getInboundTransactionID(), cdr.getInboundSessionID());

						// Perform the Transfer
						TransferMode transferMode = TransferMode.findByID(config.getTransferModes(), transferModeID);
						response = performUponDepletionTransfer(response, transferMode, record, cdr);
						return;
					}
				}

			}
		}
		catch (Exception e)
		{
			logger.error("performUponDepletionTransfer failed", e);
		}

	}

	private ResponseHeader performUponDepletionTransfer(ResponseHeader response, TransferMode transferMode, TransferRecord transferRecord, CdrBase cdr)
	{

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			response = performCommonTransfer(db, transferRecord.getDonorMsisdn(), transferRecord.getRecipientMsisdn(), transferRecord.getAmount(), //
					transferRecord, transferMode, cdr, new ServiceContext(), NO_PIN, response, false);

			// Update Trigger
			if (transferRecord.isChanged())
			{
				transferRecord.setChanged(false);
				db.update(transferRecord);
			}

			return response;

		}
		catch (Throwable e)
		{
			return response.exitWith( cdr, ReturnCodes.technicalProblem, e);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
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
			result = new Subscriber(subscriberNumber.toMSISDN(), air, transaction);
			context.setSubscriberProxy(result);
		}

		return result;
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

	private boolean isDonor(IDatabaseConnection db, Subscriber donor, Variant variant) throws AirException, SQLException
	{
		return lifecycle.isSubscribed(db, donor, getServiceID(), variant == null ? null : variant.getVariantID());
	}

	private boolean isRecipient(IDatabaseConnection db, Subscriber recipient) throws SQLException, AirException
	{
		return lifecycle.isMember(db, getServiceID(), recipient);
	}

	private boolean isRecipient(IDatabaseConnection db, Subscriber donor, Subscriber recipient, Variant variant) throws SQLException, AirException
	{
		return lifecycle.isMember(db, donor, getServiceID(), variant == null ? null : variant.getVariantID(), recipient);
	}

	private boolean chargeDonor(UpdateParameters donorParams, Subscriber donor, long charge, ServiceClass donorServiceClass, //
			Properties properties, ICdr cdr, ResponseHeader response) throws AirException
	{
		if (charge == 0)
		{
			logger.debug("Charge is {},  omitting UpdateBalanceAndDate", charge);
			return true;
		}

		String chargeText = locale.formatCurrency(charge);
		logger.debug("Attempt to charge {} {}", donor.getInternationalNumber(), chargeText);

		try
		{
			if (donorServiceClass.isPostPaid() && config.postPaidAccountID > 0)
			{
				AccountUpdate postPaidDA = new AccountUpdate(config.postPaidAccountID, Subscriber.DATYPE_MONEY, -charge, null, null, null, null, null);
				donor.updateAccounts(donorParams, null, postPaidDA);
			}
			else
			{
				donor.updateAccounts(donorParams, -charge);
			}

			properties.setCharge(chargeText);
			cdr.setChargeLevied((int) charge);
			response.setChargeLevied((int) charge);

		}
		catch (AirException e)
		{
			if (e.getReturnCode() == ReturnCodes.insufficientBalance)
				return false;
			else
				throw e;
		}

		return true;
	}

	private Variant toVariant(String variantName)
	{
		if (variantName == null)
			return null;

		for (Variant variant : config.getVariants())
		{
			if (variant.equals(variantName))
				return variant;
		}

		return null;
	}

	private TransferMode toTransferMode(String transferModeID)
	{
		if (transferModeID == null)
			return null;

		for (TransferMode transferMode : config.getTransferModes())
		{
			if (transferMode.equals(transferModeID))
				return transferMode;
		}

		return null;
	}

	private ServiceClass getServiceClass(Subscriber donor) throws AirException
	{
		for (ServiceClass serviceClass : config.getServiceClasses())
		{
			if (donor.getServiceClassCurrent() == serviceClass.getServiceClassID())
				return serviceClass;
		}
		return null;
	}

	private void setDonorProperties(Properties properties, Subscriber donor, Variant variant, CdrBase cdr)
	{
		// Cdr
		if (cdr != null)
			cdr.setA_MSISDN(donor.getInternationalNumber());

		// Properties
		properties.setVariant(variant == null ? null : variant.getName());
		properties.setDonorMSISDN(donor.getNationalNumber());
	}

	private void setDonorProperties(Properties properties, Subscriber donor, TransferMode transferMode, CdrBase cdr)
	{
		// Cdr
		if (cdr != null)
			cdr.setA_MSISDN(donor.getInternationalNumber());

		// Properties
		properties.setTransferMode(transferMode.getName());
		properties.setDonorMSISDN(donor.getNationalNumber());
	}

	private void setRecipientProperties(Properties properties, Subscriber recipient, ICdr cdr)
	{
		// Cdr
		if (cdr != null)
		{
			cdr.setB_MSISDN(recipient.getInternationalNumber());
		}

		// Properties
		properties.setRecipientMSISDN(recipient.getNationalNumber());
	}

	private static final int NEXT_SEND_FIRST_RENEW_WARNING = 1;
	private static final int NEXT_SEND_SECOND_RENEW_WARNING = 2;
	private static final int NEXT_RENEW_SUBSCRIPTION = 3;

	private void addDonorLifecycle(IDatabaseConnection database, Subscriber donor, Variant variant, DateTime expiryDate) throws SQLException
	{
		DateTime renewalTime = expiryDate.addSeconds(1);
		DateTime firstRenewWarningTime = renewalTime.addHours(-variant.getFirstRenewalWarningHoursBefore());
		DateTime secondRenewWarningTime = renewalTime.addHours(-variant.getSecondRenewalWarningHoursBefore());
		int state;
		DateTime nextDateTime;
		if (variant.getFirstRenewalWarningHoursBefore() > 0)
		{
			state = NEXT_SEND_FIRST_RENEW_WARNING;
			nextDateTime = firstRenewWarningTime;
		}
		else if (variant.getSecondRenewalWarningHoursBefore() > 0)
		{
			state = NEXT_SEND_SECOND_RENEW_WARNING;
			nextDateTime = secondRenewWarningTime;
		}
		else
		{
			state = NEXT_RENEW_SUBSCRIPTION;
			nextDateTime = renewalTime;
		}

		lifecycle.addSubscription(database, donor, getServiceID(), variant.getVariantID(), //
				state, nextDateTime, new Date[] { firstRenewWarningTime, secondRenewWarningTime, renewalTime });

	}

	private void setExpiryDateProperty(Subscriber subscriber, Properties properties, Date expiryDate)
	{
		properties.setExpiryDate(locale.formatDate(expiryDate, subscriber.getLanguageID()));
		properties.setExpiryTime(locale.formatTime(expiryDate, subscriber.getLanguageID()));
	}

	private void sendSubscriberSMS(Subscriber subscriber, int notifcationID, Properties properties)
	{
		String languageCode = esb.getLocale().getLanguage(subscriber.getLanguageID());
		INotificationText text = notifications.get(notifcationID, languageCode, locale, properties);
		if (text != null && text.getText() != null && text.getText().length() > 0)
			smsConnector.send(config.smsSourceAddress, subscriber.getInternationalNumber(), text);
	}

	private void informDonor(Subscriber donor, int notification, boolean isInteractive, IServiceContext context, Properties properties)
	{
		if (!isInteractive)
			sendSubscriberSMS(donor, notification, properties);
		else
			context.setResultText(getResultText(donor, notification, properties));
	}

	private String getResultText(Subscriber subscriber, int notifcationID, Properties properties)
	{
		String languageCode = esb.getLocale().getLanguage(subscriber.getLanguageID());
		INotificationText text = notifications.get(notifcationID, languageCode, locale, properties);
		return text == null ? null : text.getText();
	}

	private String getCorrespondingSubscription(IDatabaseConnection db, Subscriber donor, TransferMode transferMode) throws AirException, SQLException
	{
		String[] requiredSubscriptions = transferMode.getRequiredSubscriptionVariants();
		if (requiredSubscriptions != null && requiredSubscriptions.length > 0)
		{
			for (String variantID : requiredSubscriptions)
			{
				Variant variant = toVariant(variantID);
				if (variant == null)
					continue;
				if (isDonor(db, donor, variant))
				{
					return variant.getVariantID();
				}
			}

			return null;
		}

		ISubscription[] subscriptions = lifecycle.getSubscriptions(db, donor, getServiceID());
		if (subscriptions == null || subscriptions.length == 0)
			return "";

		return subscriptions[0].getVariantID();
	}

	private void setTransferProperties(Properties properties, TransferMode transferMode, long quantity)
	{
		properties.setUnits(transferMode.getUnits());
		properties.setTransferMode(transferMode.getName());
		properties.setQuantity(transferMode.formatRecipientQuantity(quantity));
		properties.setTransferID(transferMode.getTransferModeID());
	}

	private TransferRecord getTransferRecord(IDatabaseConnection db, ITemporalTrigger trigger) throws SQLException
	{
		List<TransferRecord> list = getTransferRecords(db, trigger.getServiceID(), trigger.getVariantID(), trigger.getMsisdnA(), trigger.getMsisdnB(), trigger.getKeyValue());

		return list.size() == 0 ? null : list.get(0);
	}

	private List<TransferRecord> getTransferRecords(IDatabaseConnection db, String serviceID, String variantID, ISubscriber donor, ISubscriber recipient, String transferModeID) throws SQLException
	{
		return getTransferRecords(db, serviceID, variantID, donor == null ? null : donor.getInternationalNumber(), recipient == null ? null : recipient.getInternationalNumber(), transferModeID);
	}

	private List<TransferRecord> getTransferRecords(IDatabaseConnection db, String serviceID, String variantID, String donorMsisdn, String recipientMsisdn, String transferModeID) throws SQLException
	{
		String where = "where serviceID = %s";
		int count = 0;
		Object[] params = new Object[5];
		params[count++] = serviceID;

		if (variantID != null)
		{
			where += " and variantID = %s";
			params[count++] = variantID;
		}

		if (donorMsisdn != null)
		{
			where += " and donorMsisdn = %s";
			params[count++] = donorMsisdn;
		}

		if (recipientMsisdn != null)
		{
			where += " and recipientMsisdn = %s";
			params[count++] = recipientMsisdn;
		}

		if (transferModeID != null)
		{
			where += " and transferModeID = %s";
			params[count++] = transferModeID;
		}

		return db.selectList(TransferRecord.class, where, java.util.Arrays.copyOf(params, count));

	}

	private void deleteTransferRecords(IDatabaseConnection db, String serviceID, String variantID, ISubscriber donor, ISubscriber recipient, String transferModeID) throws SQLException
	{
		String where = "where serviceID = %s";
		int count = 0;
		Object[] params = new Object[5];
		params[count++] = serviceID;

		if (variantID != null)
		{
			where += " and variantID = %s";
			params[count++] = variantID;
		}

		where += " and donorMsisdn = %s";
		params[count++] = donor.getInternationalNumber();

		if (recipient != null)
		{
			where += " and recipientMsisdn = %s";
			params[count++] = recipient.getInternationalNumber();
		}

		if (transferModeID != null)
		{
			where += " and transferModeID = %s";
			params[count++] = transferModeID;
		}

		db.delete(TransferRecord.class, where, java.util.Arrays.copyOf(params, count));

	}

	private boolean isSubscriberActive(Subscriber subscriber, TransferMode transferMode) throws AirException
	{
		Date now = new Date();

		if (expired(subscriber.getSupervisionExpiryDate(), now))
			return false;

		if (expired(subscriber.getServiceFeeExpiryDate(), now))
			return false;

		if (expired(subscriber.getServiceRemovalDate(), now))
			return false;

		Boolean flag = subscriber.getAccountActivatedFlag();
		if (flag != null && !flag)
			return false;

		flag = subscriber.getTemporaryBlockedFlag();
		if (flag != null && flag)
			return false;

		Date date = subscriber.getRefillUnbarDateTime();
		if (date != null && date.after(now))
			return false;

		if (transferMode != null)
		{
			String blackListedPSOs = transferMode.getBlackListedPSOBits();
			if (blackListedPSOs != null && blackListedPSOs.length() > 0)
			{
				String[] psoList = blackListedPSOs.split("\\,");
				for (String pso : psoList)
				{
					if (subscriber.hasPSO(Integer.parseInt(pso)))
						return false;
				}
			}
		}

		return true;
	}

	private boolean expired(Date date, Date now)
	{
		return date != null && date.before(now);
	}

}
