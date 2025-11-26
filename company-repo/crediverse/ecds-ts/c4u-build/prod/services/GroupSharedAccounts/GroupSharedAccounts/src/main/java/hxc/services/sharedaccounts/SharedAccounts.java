package hxc.services.sharedaccounts;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.AddMemberRequest;
import com.concurrent.hxc.AddMemberResponse;
import com.concurrent.hxc.AddQuotaRequest;
import com.concurrent.hxc.AddQuotaResponse;
import com.concurrent.hxc.ChangeQuotaRequest;
import com.concurrent.hxc.ChangeQuotaResponse;
import com.concurrent.hxc.GetBalancesRequest;
import com.concurrent.hxc.GetBalancesResponse;
import com.concurrent.hxc.GetMembersRequest;
import com.concurrent.hxc.GetMembersResponse;
import com.concurrent.hxc.GetOwnersRequest;
import com.concurrent.hxc.GetOwnersResponse;
import com.concurrent.hxc.GetQuotasRequest;
import com.concurrent.hxc.GetQuotasResponse;
import com.concurrent.hxc.IServiceContext;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.ProcessLifecycleEventRequest;
import com.concurrent.hxc.ProcessLifecycleEventResponse;
import com.concurrent.hxc.RemoveMemberRequest;
import com.concurrent.hxc.RemoveMemberResponse;
import com.concurrent.hxc.RemoveMembersRequest;
import com.concurrent.hxc.RemoveMembersResponse;
import com.concurrent.hxc.RemoveQuotaRequest;
import com.concurrent.hxc.RemoveQuotaResponse;
import com.concurrent.hxc.ResponseHeader;
import com.concurrent.hxc.ServiceBalance;
import com.concurrent.hxc.ServiceContext;
import com.concurrent.hxc.ServiceQuota;
import com.concurrent.hxc.SubscribeRequest;
import com.concurrent.hxc.SubscribeResponse;
import com.concurrent.hxc.SubscriptionState;
import com.concurrent.hxc.UnsubscribeRequest;
import com.concurrent.hxc.UnsubscribeResponse;
import com.concurrent.hxc.VasServiceInfo;

import hxc.connectors.Channels;
import hxc.connectors.IInteraction;
import hxc.connectors.air.AirException;
import hxc.connectors.air.proxy.AccountUpdate;
import hxc.connectors.air.proxy.Subscriber;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.lifecycle.ILifecycle;
import hxc.connectors.lifecycle.ISubscription;
import hxc.servicebus.HostInfo;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.services.ServiceType;
import hxc.services.notification.INotificationText;
import hxc.services.notification.Phrase;
import hxc.services.transactions.CdrBase;
import hxc.services.transactions.CsvCdr;
import hxc.services.transactions.ICdr;
import hxc.services.transactions.ITransaction;
import hxc.services.transactions.Transaction;
import hxc.utils.calendar.DateTime;
import hxc.utils.instrumentation.Metric;
import hxc.utils.protocol.sdp.DedicatedAccountsFileV3_3;
import hxc.utils.protocol.sdp.ThresholdNotificationFileV2;
import hxc.utils.protocol.sdp.ThresholdNotificationFileV3;
import hxc.utils.protocol.ucip.DedicatedAccountInformation;
import hxc.utils.protocol.ucip.OfferInformation;
import hxc.utils.protocol.ucip.UsageCounterUsageThresholdInformation;
import hxc.utils.protocol.ucip.UsageThresholdInformation;

public class SharedAccounts extends SharedAccountsBase
{
	final static Logger logger = LoggerFactory.getLogger(SharedAccounts.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private static final String PROSUMER = "PROSUMER";
	private Metric notificationMetric = Metric.CreateGraph("Notifications Info",
			5000, "Notifications", "Successful Notifications", "Failed Notifications");
	private long successfulNotifications = 0;
	private long failedNotifications = 0;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// VAS Service Implementation
	//
	// /////////////////////////////////

	@Override
	public String getServiceID()
	{
		return "GSA";
	}

	@Override
	public String getServiceName(String languageCode)
	{
		return config.getName(languageCode);
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
				String languageCode = esb.getLocale().getLanguage(languageID);
				serviceInfo.setVariantName(variant.toString(languageCode));

				if (subscriber == null)
					serviceInfo.setState(SubscriptionState.unknown);
				else if (isProvider(db, subscriber, variant))
					serviceInfo.setState(SubscriptionState.active);
				else
					serviceInfo.setState(SubscriptionState.notActive);

				if (!activeOnly || serviceInfo.getState() != SubscriptionState.notActive)
					result.add(serviceInfo);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
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
				cdr.setA_MSISDN(request.getSubscriberNumber().getAddressDigits());
				cdr.setServiceID(getServiceID());
				cdr.setVariantID(variant == null ? request.getVariantID() : variant.getVariantID());
				cdr.setProcessID("Subscribe");

				// Get Provider Proxy
				Subscriber provider = getSubscriber(context, request.getSubscriberNumber(), transaction);

				try
				{
					// Set Properties
					Properties properties = getProperties(context);
					setProviderProperties(properties, provider, variant, cdr);
					DateTime expiryDate = DateTime.getToday().addDays(variant.getSafeValidityPeriodDays());
					DateTime safeExpiryDate = expiryDate.addDays(expiryMarginDays);

					// Perform GetAccountDetails UCIP call for the Provider.
					transaction.track(this, "GetAccountDetails");
					provider.getAccountDetails();
					setExpiryDateProperty(provider, properties, expiryDate);

					// Test if the Provider Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!provider.isActive())
						return response.exitWith(cdr, ReturnCodes.notEligible, "Account not Active");

					// Test if the Provider is in one of the allowed service classes
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass providerServiceClass = getServiceClass(provider);
					if (providerServiceClass == null || !providerServiceClass.isEligibleForProvider())
						return response.exitWith(cdr, ReturnCodes.notEligible, "Wrong SC");

					// Test if the Provider is already subscribed to this or another variant of the Shared Accounts Service,
					// by checking the presence of the Subscription OfferID with a GetOffers UCIP call.
					transaction.track(this, "CheckAlreadySubscribed");
					provider.getOffers(true, false, false, true, true);
					if (isProvider(db, provider))
						return response.exitWith(cdr, ReturnCodes.alreadySubscribed, "Already Subscribed");

					// Test if the Provider is already a Consumer of another Provider.
					transaction.track(this, "CheckAlreadyConsumer");
					if (isConsumer(db, provider))
						return response.exitWith(cdr, ReturnCodes.alreadyMember, "Already Consumer");

					// Charge the Provider with the subscription fee for the selected variant via a negative relative
					// adjustment to his Main Account (Pre-Paid) or DA[] (Post-Paid) using an UpdateBalanceAndDate UCIP Call.
					// This same UpdateBalanceAndDate UCIP call will also be used to increment the Revenue DA with
					// an amount equal to the subscription fee.
					transaction.track(this, "ChargeProvider");
					int charge = variant.getSubscriptionCharge();
					if (request.getMode() == RequestModes.testOnly)
					{
						cdr.setChargeLevied(charge);
						response.setChargeLevied(charge);
						properties.setCharge(locale.formatCurrency(charge));
						transaction.complete();
						return response.exitWith(cdr, ReturnCodes.successfulTest, "Success");
					}
					if (!chargeProvider(provider, charge, providerServiceClass, properties, cdr, response))
						return response.exitWith(cdr, ReturnCodes.insufficientBalance, "Insufficient Balance");

					// Set the Subscription OfferID for the Provider with an UpdateOffer UCIP call and set the
					// Offer Expiry Date according to the validity period of the selected variant , i.e. 1, 7 or 30 days.
					// NOTE 1: For the initial subscription period, the Subscription OfferID's validity will be extended
					// to give him one full validity period.
					// NOTE 2: All Subscription Offers expires at midnight at the end of the validity period.
					transaction.track(this, "SetProviderOfferID");
					provider.updateSharedOffer(variant.getSubscriptionOfferID(), null, null, safeExpiryDate, null);

					// Add Subscription to Lifecycle Store
					transaction.track(this, "AddSubscriptionLifecycle");
					addProviderLifecycle(db, provider, variant, expiryDate);

					// Send a configurable SMS to the Provider to inform him of the successful subscription, the fee which
					// has been charged and the expiry/renewal date of his subscription.
					transaction.track(this, "SendProviderSMS");
					sendSubscriberSMS(provider, smsProviderSubscribed, properties);

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
				cdr.setA_MSISDN(request.getSubscriberNumber().getAddressDigits());				
				cdr.setServiceID(getServiceID());
				cdr.setVariantID(variant == null ? request.getVariantID() : variant.getVariantID());
				cdr.setProcessID("Unsubscribe");

				// Get Subscriber Proxy
				Subscriber provider = getSubscriber(context, request.getSubscriberNumber(), transaction);
				try
				{
					// Perform GetAccountDetails UCIP call for the Provider.
					transaction.track(this, "GetAccountDetails");
					provider.getAccountDetails();

					// Get Properties
					Properties properties = getProperties(context);
					setProviderProperties(properties, provider, variant, cdr);

					// Test if the Provider Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!provider.isActive() && !request.isForced())
						return response.exitWith(cdr, ReturnCodes.notEligible, "Provider Account not Active");

					// Test if the Provider is in one of the allowed service classes.
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass providerServiceClass = getServiceClass(provider);
					if (providerServiceClass == null || !providerServiceClass.isEligibleForProvider() && !request.isForced())
						return response.exitWith(cdr, ReturnCodes.notEligible, "Wrong Provider SC");

					// Test if the Provider is subscribed to this variant of the Shared Accounts Service, by checking the presence of the Subscription OfferID with a GetOffers UCIP call.
					transaction.track(this, "CheckIfSubscribed");
					provider.getOffers(true, false, false, true, true);
					if (!isProvider(db, provider, variant) && !request.isForced())
						return response.exitWith(cdr, ReturnCodes.notSubscribed, "Not Subscribed");

					// Charge the Provider with the Un-Subscription Fee for the selected variant via a negative relative adjustment to his Main Account (Pre-Paid) or DA[] (Post-Paid)
					// using an UpdateBalanceAndDate UCIP Call.
					// This same UpdateBalanceAndDate UCIP call will also be used to increment the Revenue DA with an amount equal to the un-subscription fee.
					// NOTE: If the Un-Subscription Fee is configured to be 0.00 USD, the UpdateBalanceAndDate UCIP call will be omitted altogether.
					transaction.track(this, "ChargeProvider");
					int charge = providerServiceClass.getUnsubscribeCharge();
					if (request.getMode() == RequestModes.testOnly)
					{
						cdr.setChargeLevied((int) charge);
						response.setChargeLevied(charge);
						properties.setCharge(locale.formatCurrency(charge));
						transaction.complete();
						return response.exitWith(cdr, ReturnCodes.successfulTest, "Success");
					}
					if (!chargeProvider(provider, charge, providerServiceClass, properties, cdr, response))
						return response.exitWith(cdr, ReturnCodes.insufficientBalance, "Insufficient Balance");

					// Common unsubscribe Procedure
					String[] consumerList = lifecycle.getMembers(db, provider, getServiceID(), variant.getVariantID());
					unsubscribe(db, provider, consumerList, variant, cdr, transaction, properties);

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

	private void unsubscribe(IDatabaseConnection db, Subscriber provider, String[] consumerList, Variant variant, ICdr cdr, Transaction<?> transaction, Properties properties) throws AirException,
			SQLException
	{
		// Remove all the Consumers of the Provider as per process ‎(7) Remove Consumer.
		// NOTE: This will also remove all the quotas consumers have.
		for (String consumerMSISDN : consumerList)
		{
			Subscriber consumer = new Subscriber(consumerMSISDN, air, transaction);
			setConsumerProperties(properties, consumer, cdr);
			consumer.getOffers(true, false, false, true, true);
			removeConsumer(db, provider, consumer, transaction, variant, properties, smsProviderRemovedConsumer, cdr);
		}

		// Delete the Subscription OfferID for the Provider with an DeleteOffer ACIP call.
		if (provider.hasSharedOffer(variant.getSubscriptionOfferID()))
			provider.deleteSharedOffer(variant.getSubscriptionOfferID());

		// Delete all the Provider Quota OfferIDs for the Provider with a DeleteOffer ACIP call.
		for (Quota quota : config.quotas)
		{
			if (provider.hasProviderOffer(quota.getBeneficiaryOfferID()))
				provider.deleteProviderOffer(quota.getBeneficiaryOfferID());

			if (provider.hasSharedOffer(quota.getSponsorOfferID()))
				provider.deleteSharedOffer(quota.getSponsorOfferID());
		}

		// Clear Dedicated Accounts
		List<AccountUpdate> accountUpdates = new ArrayList<AccountUpdate>();

		if (provider.hasDecicatedAccount(config.getSmsDedicatedAccountID()))
			accountUpdates.add(new AccountUpdate(config.getSmsDedicatedAccountID(), Subscriber.DATYPE_MONEY, null, 0L, null, null, null, null));

		if (provider.hasDecicatedAccount(config.getMmsDedicatedAccountID()))
			accountUpdates.add(new AccountUpdate(config.getMmsDedicatedAccountID(), Subscriber.DATYPE_MONEY, null, 0L, null, null, null, null));

		if (provider.hasDecicatedAccount(config.getDataDedicatedAccountID()))
			accountUpdates.add(new AccountUpdate(config.getDataDedicatedAccountID(), Subscriber.DATYPE_MONEY, null, 0L, null, null, null, null));

		if (provider.hasDecicatedAccount(config.getVoiceDedicatedAccountID()))
			accountUpdates.add(new AccountUpdate(config.getVoiceDedicatedAccountID(), Subscriber.DATYPE_MONEY, null, 0L, null, null, null, null));

		if (accountUpdates.size() > 0)
			provider.updateAccounts(null, accountUpdates.toArray(new AccountUpdate[accountUpdates.size()]));

		// Remove the subscription from the C4U Lifecycle Connector.
		lifecycle.removeSubscription(db, provider, getServiceID(), variant.getVariantID());

		// Send a configurable SMS to the Provider to inform him of the successful un-subscription and the fee which has been charged.
		transaction.track(this, "SendProviderSMS");
		sendSubscriberSMS(provider, smsProviderUnsubscribed, properties);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Add Consumer / Prosumer
	//
	// /////////////////////////////////

	@Override
	public AddMemberResponse addMember(IServiceContext context, AddMemberRequest request)
	{
		// Create Response
		AddMemberResponse response = super.addMember(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Test if Prosumer
		String type = request.getMemberType();
		boolean asProsumer = type != null && type.length() > 0 && PROSUMER.startsWith(type.toUpperCase());

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Test if the Provider is attempting to add himself as a Consumer.
				if (request.getSubscriberNumber().equals(request.getMemberNumber()))
					return response.exitWith(cdr, ReturnCodes.cannotAddSelf, "Cannot add Self");

				// Validate Request
				String problem = AddMemberRequest.validate(request);
				if (problem != null)
					return response.exitWith(cdr, ReturnCodes.malformedRequest, problem);

				// Validate Variant
				Variant variant = toVariant(request.getVariantID());
				if (variant == null)
					return response.exitWith(cdr, ReturnCodes.malformedRequest, "Invalid VariantID");

				// Update CDR
				cdr.setServiceID(getServiceID());
				cdr.setVariantID(variant == null ? request.getVariantID() : variant.getVariantID());
				cdr.setProcessID("AddMember");

				// Get Subscriber Proxy
				Subscriber provider = getSubscriber(context, request.getSubscriberNumber(), transaction);
				try
				{
					// Set Properties
					Properties properties = getProperties(context);
					setProviderProperties(properties, provider, variant, cdr);

					// Perform GetAccountDetails UCIP call for the Provider.
					transaction.track(this, "GetAccountDetails");
					provider.getAccountDetails();

					// Test if the Provider Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!provider.isActive())
						return response.exitWith(cdr, ReturnCodes.notEligible, "Provider Account not Active");

					// Test if the Provider is in one of the allowed service classes
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass providerServiceClass = getServiceClass(provider);
					if (providerServiceClass == null || !providerServiceClass.isEligibleForProvider())
						return response.exitWith(cdr, ReturnCodes.notEligible, "Wrong Provider SC");

					// Test if the Provider is subscribed to this variant of the Shared Accounts Service, by checking the presence of the Subscription OfferID with a GetOffers UCIP call.
					transaction.track(this, "CheckIfSubscribed");
					provider.getOffers(true, false, false, true, true);
					if (!isProvider(db, provider, variant))
						return response.exitWith(cdr, ReturnCodes.notSubscribed, "Not Subscribed");
					OfferInformation subscriptionOffer = provider.getSharedOffer(variant.getSubscriptionOfferID());
					Date safeExpiryDate = subscriptionOffer.expiryDate;

					// Test if the Provider is not exceeding the maximum number of consumers for his service class
					String[] memberMSISDNs = lifecycle.getMembers(db, provider, getServiceID(), variant.getVariantID());
					if (memberMSISDNs != null && memberMSISDNs.length >= providerServiceClass.getMaxConsumers())
						return response.exitWith(cdr, ReturnCodes.maxMembersExceeded, "Consumers Limit");

					// Perform GetAccountDetails UCIP call for the Consumer.
					Subscriber consumer = new Subscriber(request.getMemberNumber().toMSISDN(), air, transaction);
					setConsumerProperties(properties, consumer, cdr);
					consumer.getAccountDetails();

					// Test if the Consumer Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!consumer.isActive())
						return response.exitWith(cdr, ReturnCodes.memberNotEligible, "Consumer Account not Active");

					// Test if the Consumer is in one of the allowed service classes
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass consumerServiceClass = getServiceClass(consumer);
					if (consumerServiceClass == null || !consumerServiceClass.isEligibleForConsumer())
						return response.exitWith(cdr, ReturnCodes.memberNotEligible, "Wrong Consumer SC");

					// Test if the Consumer is not already a Provider, by checking the presence of the Subscription OfferID with a GetOffers UCIP call.
					transaction.track(this, "CheckConsumerIsProvider");
					consumer.getOffers(true, false, false, true, true);
					if (isProvider(db, consumer))
						return response.exitWith(cdr, ReturnCodes.alreadyOwner, "Already Provider");

					// Test if the Consumer is not already a Consumer of another Provider
					transaction.track(this, "CheckAlreadyConsumer");
					if (isConsumer(db, consumer))
						return response.exitWith(cdr, ReturnCodes.alreadyOtherMember, "Already Consumer");

					// Charge the Provider with the Consumer Addition Fee for the selected variant via a negative relative adjustment to his Main Account (Pre-Paid) or DA[] (Post-Paid)
					// using an UpdateBalanceAndDate UCIP Call.
					// This same UpdateBalanceAndDate UCIP call will also be used to increment the Revenue DA with an amount equal to the consumer addition fee.
					// NOTE: If the Consumer Addition fee is configured to be 0.00 USD, the UpdateBalanceAndDate UCIP call will be omitted altogether
					transaction.track(this, "ChargeProvider");
					int charge = providerServiceClass.getAddConsumerCharge();
					if (request.getMode() == RequestModes.testOnly)
					{
						cdr.setChargeLevied((int) charge);
						response.setChargeLevied(charge);
						properties.setCharge(locale.formatCurrency(charge));
						transaction.complete();
						return response.exitWith(cdr, ReturnCodes.successfulTest, "Success");
					}
					if (!chargeProvider(provider, charge, providerServiceClass, properties, cdr, response))
						return response.exitWith(cdr, ReturnCodes.insufficientBalance, "Insufficient Balance");

					// Set the Consumer Subscription OfferID for the Consumer with an UpdateOffer UCIP call and set the Offer Expiry Date to match the Expiry Date of the Provider's
					// Subscription OfferID.
					consumer.updateProviderOffer(variant.getConsumerOfferID(), null, null, safeExpiryDate, null, provider);

					// The Consumer/Provider relationship is recorded in the C4U Lifecycle Connector.
					lifecycle.addMember(db, provider, getServiceID(), variant.getVariantID(), consumer);

					// Also set the Subscription flag for a Prosumer
					if (asProsumer)
					{
						transaction.track(this, "SetProviderOfferID");
						consumer.updateSharedOffer(variant.getSubscriptionOfferID(), null, null, safeExpiryDate, null);
					}

					// Send a configurable SMS to the Provider to inform him of the successful consumer addition, the fee which has been charged and the MSISDN of the added consumer.
					transaction.track(this, "SendProviderSMS");
					sendSubscriberSMS(provider, smsProviderAddedConsumer, properties);

					// Send a configurable SMS to the Consumer to inform him of the successful consumer addition, with an invitation to decline by replying with "No" to the origin short code.
					transaction.track(this, "SendConsumerSMS");
					sendSubscriberSMS(consumer, smsConsumerAddedConsumer, properties);

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
	// Get Consumers
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
			if (variant == null)
				return response.exitWith(null, ReturnCodes.malformedRequest, "Invalid VariantID");

			// Get Subscriber Proxy
			Subscriber provider = getSubscriber(context, request.getSubscriberNumber(), null);
			try
			{
				// Get Properties
				Properties properties = getProperties(context);
				setProviderProperties(properties, provider, variant, null);

				// Perform GetAccountDetails UCIP call for the Provider.
				logger.debug("GetAccountDetails");
				provider.getAccountDetails();

				// Test if the Provider Account is in a valid state, i.e. all lifecycle dates are in the future.
				logger.debug("CheckIfActive");
				if (!provider.isActive())
					return response.exitWith(null, ReturnCodes.notEligible, "Provider Account not Active");

				// Test if the Provider is in one of the allowed service classes
				logger.debug("CheckServiceClass");
				ServiceClass providerServiceClass = getServiceClass(provider);
				if (providerServiceClass == null || !providerServiceClass.isEligibleForProvider())
					return response.exitWith(null, ReturnCodes.notEligible, "Wrong Provider SC");

				// Test if the Provider is subscribed to this variant of the Shared Accounts Service, by checking the presence of the Subscription OfferID with a GetOffers UCIP call.
				logger.debug("GetOffers");
				provider.getOffers(true, false, false, true, true);
				if (!isProvider(db, provider, variant))
					return response.exitWith(null, ReturnCodes.notSubscribed, "Not Subscribed");
				OfferInformation subscriptionOffer = provider.getSharedOffer(variant.getSubscriptionOfferID());
				DateTime safeExpiryDate = new DateTime(subscriptionOffer.expiryDate);
				DateTime expiryDate = safeExpiryDate.addDays(-expiryMarginDays);

				setExpiryDateProperty(provider, properties, expiryDate);

				// Update List of Members
				String[] members = lifecycle.getMembers(db, provider, getServiceID(), variant.getVariantID());
				response.setMembers(Number.fromString(numberPlan.getNationalFormat(members), Number.NumberType.UNKNOWN, Number.NumberPlan.UNKNOWN));
			}
			catch (AirException e)
			{
				return response.exitWith(null, e.getReturnCode(), e);
			}

			// Complete
			return response.exitWith(null, ReturnCodes.success, "Success");
		}
		catch (Throwable e)
		{
			return response.exitWith(null, ReturnCodes.technicalProblem, e);
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
				return response.exitWith(null, ReturnCodes.malformedRequest, problem);

			// Validate Variant
			Variant variant = toVariant(request.getVariantID());

			// Empty Result
			response.setOwners(Number.fromString(new String[0], Number.NumberType.UNKNOWN, Number.NumberPlan.UNKNOWN));

			// Get Subscriber Proxy
			Subscriber consumer = getSubscriber(context, request.getMemberNumber(), null);
			try
			{
				// Set Properties
				Properties properties = getProperties(context);

				// Get Offers
				logger.debug("GetOffers");
				consumer.getOffers(true, false, false, true, true);

				Subscriber provider = null;
				for (Variant var : config.getVariants())
				{
					// Skip if Variant doesn't match optional supplied variantID
					if (variant != null && !var.getVariantID().equalsIgnoreCase(variant.getVariantID()))
						continue;

					OfferInformation offer = consumer.getProviderOffer(var.getConsumerOfferID());
					if (offer != null)
					{
						provider = new Subscriber(offer.offerProviderID, air, null);
						if (isConsumer(db, provider, consumer, var))
						{
							variant = var;
							break;
						}
					}
				}
				if (provider == null)
					return response.exitWith(null, ReturnCodes.success, "Success");

				// Perform GetAccountDetails UCIP call for the Provider.
				logger.debug("GetAccountDetails");
				provider.getAccountDetails();

				// Test if the Provider Account is in a valid state, i.e. all lifecycle dates are in the future.
				logger.debug("CheckIfActive");
				if (!provider.isActive())
					return response.exitWith(null, ReturnCodes.success, "Success");

				// Test if the Provider is in one of the allowed service classes .
				logger.debug("CheckServiceClass");
				ServiceClass providerServiceClass = getServiceClass(provider);
				if (providerServiceClass == null || !providerServiceClass.isEligibleForProvider())
					return response.exitWith(null, ReturnCodes.success, "Success");

				// Test if the Provider is subscribed to this variant of the Shared Accounts Service, by checking the presence of the Subscription OfferID with a GetOffers UCIP call.
				logger.debug("GetOffers");
				provider.getOffers(true, false, false, true, true);
				if (!isProvider(db, provider, variant))
					return response.exitWith(null, ReturnCodes.success, "Success");

				// Update List of Owners
				properties.setProviderMSISDN(provider.getNationalNumber());
				String[] members = new String[] { provider.getNationalNumber() };
				response.setOwners(Number.fromString(members, Number.NumberType.UNKNOWN, Number.NumberPlan.UNKNOWN));
			}
			catch (AirException e)
			{
				return response.exitWith(null, e.getReturnCode(), e);
			}

			// Complete
			return response.exitWith(null, ReturnCodes.success, "Success");
		}
		catch (Throwable e)
		{
			return response.exitWith(null, ReturnCodes.technicalProblem, e);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Remove Consumer
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
					return response.exitWith(cdr, ReturnCodes.malformedRequest, problem);

				// Get Subscriber Proxy's
				Subscriber provider = getSubscriber(context, request.getSubscriberNumber(), transaction);
				Subscriber consumer = new Subscriber(request.getMemberNumber().toMSISDN(), air, transaction);

				// Validate Variant
				Variant variant = toVariant(request.getVariantID());

				if (variant == null)
				{
					for (Variant var : config.getVariants())
					{
						if (lifecycle.isMember(db, provider, getServiceID(), var.getVariantID(), consumer))
						{
							variant = var;
							break;
						}
					}
				}

				if (variant == null)
					return response.exitWith(cdr, ReturnCodes.malformedRequest, "Invalid VariantID");

				// Update CDR
				cdr.setServiceID(getServiceID());
				cdr.setVariantID(variant == null ? request.getVariantID() : variant.getVariantID());
				cdr.setProcessID("RemoveMember");

				try
				{
					// Set Properties
					Properties properties = getProperties(context);
					setProviderProperties(properties, provider, variant, cdr);

					// Perform GetAccountDetails UCIP call for the Provider.
					transaction.track(this, "GetAccountDetails");
					provider.getAccountDetails();

					// Test if the Provider Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!provider.isActive() && !request.isForced())
						return response.exitWith(cdr, ReturnCodes.notEligible, "Provider Account not Active");

					// Test if the Provider is in one of the allowed service classes.
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass providerServiceClass = getServiceClass(provider);
					if (providerServiceClass == null || !providerServiceClass.isEligibleForProvider() && !request.isForced())
						return response.exitWith(cdr, ReturnCodes.notEligible, "Wrong Provider SC");

					// Test if the Provider is subscribed to this variant of the Shared Accounts Service, by checking the presence of the Subscription OfferID with a GetOffers UCIP call.
					transaction.track(this, "CheckIfSubscribed");
					provider.getOffers(true, false, false, true, true);
					if (!isProvider(db, provider, variant) && !request.isForced())
						return response.exitWith(cdr, ReturnCodes.notSubscribed, "Not Subscribed");
					OfferInformation subscriptionOffer = provider.getSharedOffer(variant.getSubscriptionOfferID());
					DateTime safeExpiryDate = new DateTime(subscriptionOffer.expiryDate);
					DateTime expiryDate = safeExpiryDate.addDays(-expiryMarginDays);
					setExpiryDateProperty(provider, properties, expiryDate);

					// Perform GetAccountDetails UCIP call for the Consumer.
					setConsumerProperties(properties, consumer, cdr);
					consumer.getAccountDetails();

					// Test if the Consumer Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!consumer.isActive() && !request.isForced())
						return response.exitWith(cdr, ReturnCodes.memberNotEligible, "Consumer Account not Active");

					// Test if the Consumer is in one of the allowed service classes
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass consumerServiceClass = getServiceClass(consumer);
					if (consumerServiceClass == null || !consumerServiceClass.isEligibleForConsumer() && !request.isForced())
						return response.exitWith(cdr, ReturnCodes.memberNotEligible, "Wrong Consumer SC");

					// Test if the Consumer is a consumer of the Provider, by checking the presence of the Consumer OfferID with a GetOffers UCIP call and interrogation of the C4U Lifecycle
					// Membership store.
					transaction.track(this, "CheckIfConsumer");
					consumer.getOffers(true, false, false, true, true);
					if (!isConsumer(db, provider, consumer, variant))
						return response.exitWith(cdr, ReturnCodes.notSubscribed, "Not Provider's Consumer");

					// Charge the Provider with Consumer Removal Fee via a negative relative adjustment to his Main Account (Pre-Paid) or DA[] (Post-Paid) using an UpdateBalanceAndDate
					// UCIP Call.
					// This same UpdateBalanceAndDate UCIP call will also be used to increment the Revenue DA with an amount equal to the consumer removal fee.
					// NOTE: If the consumer removal fee is configured to be 0.00 USD, the UpdateBalanceAndDate UCIP call will be omitted altogether.
					transaction.track(this, "ChargeProvider");
					int charge = providerServiceClass.getRemoveConsumerCharge();
					if (request.getMode() == RequestModes.testOnly)
					{
						cdr.setChargeLevied((int) charge);
						response.setChargeLevied(charge);
						properties.setCharge(locale.formatCurrency(charge));
						transaction.complete();
						return response.exitWith(cdr, ReturnCodes.successfulTest, "Success");
					}
					if (!chargeProvider(provider, charge, providerServiceClass, properties, cdr, response))
						return response.exitWith(cdr, ReturnCodes.insufficientBalance, "Insufficient Balance");

					// Remove Consumer
					boolean isDecline = request.getCallerID().equals(request.getMemberNumber().toString());
					int smsToSendToProvider = isDecline ? smsProviderConsumerDeclined : smsProviderRemovedConsumer;
					removeConsumer(db, provider, consumer, transaction, variant, properties, smsToSendToProvider, cdr);

				}
				catch (AirException e)
				{
					return response.exitWith(cdr, e.getReturnCode(), e);
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
	// Remove Consumer
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
					return response.exitWith(cdr, ReturnCodes.malformedRequest, problem);

				// Validate Variant
				Variant variant = toVariant(request.getVariantID());
				if (variant == null)
					return response.exitWith(cdr, ReturnCodes.malformedRequest, "Invalid VariantID");

				// Update CDR
				cdr.setServiceID(getServiceID());
				cdr.setVariantID(variant == null ? request.getVariantID() : variant.getVariantID());
				cdr.setProcessID("RemoveMembers");

				// Get Provider Proxy
				Subscriber provider = getSubscriber(context, request.getSubscriberNumber(), transaction);
				try
				{
					// Set Properties
					Properties properties = getProperties(context);
					setProviderProperties(properties, provider, variant, cdr);

					// Perform GetAccountDetails UCIP call for the Provider.
					transaction.track(this, "GetAccountDetails");
					provider.getAccountDetails();

					// Test if the Provider Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!provider.isActive())
						return response.exitWith(cdr, ReturnCodes.notEligible, "Provider Account not Active");

					// Test if the Provider is in one of the allowed service classes
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass providerServiceClass = getServiceClass(provider);
					if (providerServiceClass == null || !providerServiceClass.isEligibleForProvider())
						return response.exitWith(cdr, ReturnCodes.notEligible, "Wrong Provider SC");

					// Test if the Provider is subscribed to this variant of the Shared Accounts Service, by checking the presence of the Subscription OfferID with a GetOffers UCIP call.
					transaction.track(this, "CheckIfSubscribed");
					provider.getOffers(true, false, false, true, true);
					if (!isProvider(db, provider, variant))
						return response.exitWith(cdr, ReturnCodes.notSubscribed, "Not Subscribed");
					OfferInformation subscriptionOffer = provider.getSharedOffer(variant.getSubscriptionOfferID());
					DateTime safeExpiryDate = new DateTime(subscriptionOffer.expiryDate);
					DateTime expiryDate = safeExpiryDate.addDays(-expiryMarginDays);
					setExpiryDateProperty(provider, properties, expiryDate);

					// Get List of Consumers
					String[] consumerList = lifecycle.getMembers(db, provider, getServiceID(), variant.getVariantID());

					// Charge the Provider with Consumer Removal Fee via a negative relative adjustment to his Main Account (Pre-Paid) or DA[] (Post-Paid) using an UpdateBalanceAndDate
					// UCIP Call.
					// This same UpdateBalanceAndDate UCIP call will also be used to increment the Revenue DA with an amount equal to the consumer removal fee.
					// NOTE: If the consumer removal fee is configured to be 0.00 USD, the UpdateBalanceAndDate UCIP call will be omitted altogether.
					transaction.track(this, "ChargeProvider");
					int charge = providerServiceClass.getRemoveConsumerCharge() * consumerList.length;
					if (request.getMode() == RequestModes.testOnly)
					{
						cdr.setChargeLevied((int) charge);
						response.setChargeLevied(charge);
						properties.setCharge(locale.formatCurrency(charge));
						transaction.complete();
						return response.exitWith(cdr, ReturnCodes.successfulTest, "Success");
					}
					if (!chargeProvider(provider, charge, providerServiceClass, properties, cdr, response))
						return response.exitWith(cdr, ReturnCodes.insufficientBalance, "Insufficient Balance");

					// Remove each Consumer
					for (String consumerMSISDN : consumerList)
					{
						Subscriber consumer = new Subscriber(consumerMSISDN, air, transaction);
						setConsumerProperties(properties, consumer, cdr);
						consumer.getOffers(true, false, false, true, true);
						removeConsumer(db, provider, consumer, transaction, variant, properties, smsProviderRemovedConsumer, cdr);
					}

				}
				catch (AirException e)
				{
					return response.exitWith(cdr, e.getReturnCode(), e);
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

	private void removeConsumer(IDatabaseConnection db, Subscriber provider, Subscriber consumer, Transaction<?> transaction, //
			Variant variant, Properties properties, int providerSmsID, ICdr cdr) throws AirException, SQLException
	{
		// Remove all Quotas which the Consumer has, as per Process ‎6) Remove Quota.
		transaction.track(this, "DeleteQuotaOfferIDs");
		Integer[] thresholdsToDelete = new Integer[config.quotas.length * 2];
		Integer[] countersToClear = new Integer[config.quotas.length];
		int thresholdDeleteCount = 0;
		int counterDeleteCount = 0;
		for (Quota quota : config.quotas)
		{
			// Continue if Consumer doesn't have the quota
			if (!consumer.hasProviderOffer(quota.getBeneficiaryOfferID()))
				continue;

			consumer.deleteProviderOffer(quota.getBeneficiaryOfferID());
			int tid = quota.getBeneficiaryTotalThresholdID();
			int cid = quota.getBeneficiaryUsageCounterID();
			int wid = quota.getBeneficiaryWarningUsageThresholdID();
			thresholdsToDelete[thresholdDeleteCount++] = tid;
			thresholdsToDelete[thresholdDeleteCount++] = wid;
			countersToClear[counterDeleteCount++] = cid;

			// Log Activity
			UsageThresholdInformation thresholdInfo = consumer.getUsageThreshold(tid);
			if (thresholdInfo != null)
			{
				Long thresholdValue = thresholdInfo.usageThresholdValue;
				if (thresholdValue != null)
				{
					long quantity = thresholdValue / quota.getUnitConversionFactor();
					logActivity(db, cdr, quota, -quantity);
				}
			}

		}

		if (thresholdDeleteCount > 0)
		{
			transaction.track(this, "DeleteUsageThresholds");
			thresholdsToDelete = java.util.Arrays.copyOf(thresholdsToDelete, thresholdDeleteCount);
			consumer.deleteUsageThresholds(null, thresholdsToDelete);
		}

		if (counterDeleteCount > 0)
		{
			transaction.track(this, "ClearUsageCounters");
			countersToClear = java.util.Arrays.copyOf(countersToClear, counterDeleteCount);
			consumer.clearUsageCounters(null, countersToClear);
		}

		// Delete the Consumer OfferID for the Consumer with a DeleteOffer ACIP call.
		transaction.track(this, "DeleteConsumerOfferID");
		if (consumer.hasProviderOffer(variant.getConsumerOfferID()))
			consumer.deleteProviderOffer(variant.getConsumerOfferID());

		// Remove the Provider/Consumer Relationship for the C4U Lifecycle Connector
		lifecycle.removeMember(db, provider, getServiceID(), variant.getVariantID(), consumer);

		// Send a configurable SMS to the Provider to inform him of the successful consumer removal, the fee which has been charged and the MSISDN of the Consumer.
		transaction.track(this, "SendProviderSMS");
		sendSubscriberSMS(provider, providerSmsID, properties);

		// Send a configurable SMS to the Consumer to inform him of the successful consumer removal and the MSISDN of the Provider.
		transaction.track(this, "SendConsumerSMS");
		sendSubscriberSMS(consumer, smsConsumerRemovedConsumer, properties);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Add Quota
	//
	// /////////////////////////////////

	@Override
	public AddQuotaResponse addQuota(IServiceContext context, AddQuotaRequest request)
	{
		// Create Response
		AddQuotaResponse response = super.addQuota(context, request);

		// Language
		String languageCode = esb.getLocale().getLanguage(request.getLanguageID());

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{

			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = AddQuotaRequest.validate(request);
				if (problem != null)
					return response.exitWith(cdr, ReturnCodes.malformedRequest, problem);

				// Validate Variant
				Variant variant = toVariant(request.getVariantID());
				if (variant == null)
					return response.exitWith(cdr, ReturnCodes.malformedRequest, "Invalid VariantID");

				// Update CDR
				cdr.setServiceID(getServiceID());
				cdr.setVariantID(variant == null ? request.getVariantID() : variant.getVariantID());
				cdr.setProcessID("AddQuota");

				// Get Provider Proxy
				Subscriber provider = getSubscriber(context, request.getSubscriberNumber(), transaction);
				try
				{
					// Get Properties
					Properties properties = getProperties(context);
					setProviderProperties(properties, provider, variant, cdr);

					// Perform GetAccountDetails UCIP call for the Provider.
					transaction.track(this, "GetAccountDetails");
					provider.getAccountDetails();

					// Test if the Provider Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!provider.isActive())
						return response.exitWith(cdr, ReturnCodes.notEligible, "Provider Account not Active");

					// Test if the Provider is in one of the allowed service classes
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass providerServiceClass = getServiceClass(provider);
					if (providerServiceClass == null || !providerServiceClass.isEligibleForProvider())
						return response.exitWith(cdr, ReturnCodes.notEligible, "Wrong Provider SC");

					// Test if the Provider is subscribed to this variant of the Shared Accounts Service, by checking the presence of the Subscription OfferID with a GetOffers UCIP call.
					transaction.track(this, "CheckIfSubscribed");
					provider.getOffers(true, false, false, true, true);
					if (!isProvider(db, provider, variant))
						return response.exitWith(cdr, ReturnCodes.notSubscribed, "Not Subscribed");
					OfferInformation subscriptionOffer = provider.getSharedOffer(variant.getSubscriptionOfferID());
					DateTime safeExpiryDate = new DateTime(subscriptionOffer.expiryDate);
					DateTime expiryDate = safeExpiryDate.addDays(-expiryMarginDays);
					setExpiryDateProperty(provider, properties, expiryDate);
					boolean fromProsumer = isProsumer(provider, variant);

					// Perform GetAccountDetails UCIP call for the Consumer.
					Subscriber consumer = new Subscriber(request.getMemberNumber().toMSISDN(), air, transaction);
					setConsumerProperties(properties, consumer, cdr);
					consumer.getAccountDetails();
					boolean forProsumer = isProsumer(consumer, variant);

					// Test if the Consumer Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!consumer.isActive())
						return response.exitWith(cdr, ReturnCodes.memberNotEligible, "Consumer Account not Active");

					// Test if the Consumer is in one of the allowed service classes
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass consumerServiceClass = getServiceClass(consumer);
					if (consumerServiceClass == null || !consumerServiceClass.isEligibleForConsumer())
						return response.exitWith(cdr, ReturnCodes.memberNotEligible, "Wrong Consumer SC");

					// Test if the Consumer is a consumer of the Provider, by checking the presence of the Consumer OfferID with a GetOffers UCIP call and interrogation of the C4U Lifecycle
					// Membership store.
					transaction.track(this, "CheckIfConsumer");
					long addConsumerCharge = 0L;
					boolean mustAddConsumer = false;
					consumer.getOffers(true, false, false, true, true);
					if (!isConsumer(db, consumer))
					{
						addConsumerCharge = providerServiceClass.getAddConsumerCharge();
						mustAddConsumer = true;
					}
					else if (!isConsumer(db, provider, consumer, variant))
						return response.exitWith(cdr, ReturnCodes.notSubscribed, "Not Provider's Consumer");

					// Test if the selected combination is allowed.
					// Example: Voice/Weekends/PeakHours/OffNet may not be supported.
					Quota quota = Quota.find(config.quotas, request.getQuota());
					if (quota == null)
						return response.exitWith(cdr, ReturnCodes.invalidQuota, "Invalid Quota");
					cdr.setParam1(quota.getQuotaID());
					cdr.setParam2(quota.getServiceType().toString());
					long quantity = request.getQuota().getQuantity();
					setQuotaProperties(properties, quota);
					response.setQuota(quota.toServiceQuota(languageCode));
					response.getQuota().setQuantity(quantity);

					// Test if unlimited
					boolean unlimited = quantity == -1;
					if (unlimited && !fromProsumer)
						return response.exitWith(cdr, ReturnCodes.quantityTooBig, "Quantity too big");

					// Test Min/Max units
					if (!unlimited && quantity < quota.getMinUnits())
						return response.exitWith(cdr, ReturnCodes.quantityTooSmall, "Quantity too small");
					else if (!forProsumer && quantity > quota.getMaxUnits())
						return response.exitWith(cdr, ReturnCodes.quantityTooBig, "Quantity too big");

					// Test if the Consumer already has the Quota
					if (consumer.hasProviderOffer(quota.getBeneficiaryOfferID()))
						return response.exitWith(cdr, ReturnCodes.alreadyAdded, "Already has Quota");

					// Calculate the quota price of the selected number of units from a configurable lookup table provided by C4U.
					long quotaPrice = unlimited ? 0L : (quota.getPriceCents() * quantity + 50) / 100;
					long sharedQuantity = quantity;
					if (fromProsumer)
					{
						int usageThresholdID = quota.getBeneficiaryTotalThresholdID();
						UsageThresholdInformation usageThresold = provider.getUsageThreshold(usageThresholdID);
						if (usageThresold != null)
						{
							Long value = usageThresold.usageThresholdValue;
							if (value != null)
								sharedQuantity = value / quota.getUnitConversionFactor();
						}
					}
					properties.setSharedQuantity(Long.toString(sharedQuantity));
					long totalCharge = quotaPrice + addConsumerCharge;
					properties.setCharge(locale.formatCurrency(totalCharge));

					// Exit if test only
					if (request.getMode() == RequestModes.testOnly)
					{
						cdr.setChargeLevied((int) totalCharge);
						response.setChargeLevied(totalCharge);
						transaction.complete();
						return response.exitWith(cdr, ReturnCodes.successfulTest, "Success");
					}

					// Charge the Provider with the calculated price of the quota via a negative relative adjustment to his Main Account (Pre-Paid) or DA[] (Post-Paid) using an
					// UpdateBalanceAndDate UCIP Call.
					// This same UpdateBalanceAndDate UCIP call will also be used to increment the Revenue DA with an amount equal to the quota price.
					// NOTE: In the unlikely event of the quota price being 0.00 USD, the UpdateBalanceAndDate UCIP call will be omitted altogether.
					transaction.track(this, "ChargeProvider");
					if (!chargeProvider(provider, totalCharge, providerServiceClass, properties, cdr, response))
						return response.exitWith(cdr, ReturnCodes.insufficientBalance, "Insufficient Balance");

					// Add Consumer
					if (mustAddConsumer)
					{
						// Set the Consumer Subscription OfferID for the Consumer with an UpdateOffer UCIP call and set the Offer Expiry Date to match the Expiry Date of the Provider's
						// Subscription OfferID.
						consumer.updateProviderOffer(variant.getConsumerOfferID(), null, null, safeExpiryDate, null, provider);

						// The Consumer/Provider relationship is recorded in the C4U Lifecycle Connector.
						// Refer to ‎4.2.8 for additional information relating to the C4U Lifecycle Store
						lifecycle.addMember(db, provider, getServiceID(), variant.getVariantID(), consumer);
					}

					// Increment the Service Balance DAs with the price.
					if (!unlimited)
					{
						int daID = getDedicatedAccountID(quota);
						AccountUpdate daUpdate = new AccountUpdate(daID, Subscriber.DATYPE_MONEY, quotaPrice, null, null, safeExpiryDate, null, null);
						if (forProsumer)
							consumer.updateAccounts(null, daUpdate);
						else
							provider.updateAccounts(null, daUpdate);
					}

					// If the Provider doesn't have the Provider Quota OfferID yet, set the Provider Quota OfferID
					// for the Provider with an UpdateOffer UCIP call and set the Offer Expiry Date to match the Expiry Date of the
					if (!provider.hasSharedOffer(quota.getSponsorOfferID()))
						provider.updateSharedOffer(quota.getSponsorOfferID(), null, null, safeExpiryDate, null);

					// Set the Consumer Quota OfferID for the Consumer with an UpdateOffer UCIP call and set the Offer Expiry Date to match the Expiry Date of the Provider's Subscription
					// OfferID.
					consumer.updateProviderOffer(quota.getBeneficiaryOfferID(), null, null, safeExpiryDate, null, provider);

					// Set/Increment the corresponding Quota Usage Threshold with the number of Quota Units for the Consumer and clear the consumer's corresponding Quota Usage Counter
					// with an UpdateUsageThresholdsAndCounters UCIP call.
					// The Quota Usage Threshold will be incremented if the Provider shares an additional amount of service units with his consumer, e.g. when the consumer has depleted his allotted
					// number of service units.
					long csQuantity = unlimited ? UNLIMITED : quantity * quota.getUnitConversionFactor();
					consumer.clearUsageCounters(null, quota.getBeneficiaryUsageCounterID());
					consumer.setUsageThreshold(quota.getBeneficiaryTotalThresholdID(), csQuantity, null, null);
					long warningLevel = csQuantity > quota.getWarningMargin() ? csQuantity - quota.getWarningMargin() : csQuantity;
					consumer.setUsageThreshold(quota.getBeneficiaryWarningUsageThresholdID(), warningLevel, null, null);

					// Log the Activity for Reporting Purposes
					logActivity(db, cdr, quota, quantity);
					serviceTypes[quota.getServiceType().ordinal()].incrementAndGet();

					// Send Consumer Addition Notifications
					if (mustAddConsumer)
					{
						// Send a configurable SMS to the Provider to inform him of the successful consumer addition, the fee which has been charged and the MSISDN of the added consumer.
						transaction.track(this, "SendProviderSMS");
						sendSubscriberSMS(provider, smsProviderAddedConsumer, properties);

						// Send a configurable SMS to the Consumer to inform him of the successful consumer addition, with an invitation to decline by replying with "No" to the origin short code.
						transaction.track(this, "SendConsumerSMS");
						sendSubscriberSMS(consumer, smsConsumerAddedConsumer, properties);
					}

					// Send a configurable SMS to the Provider to inform him of the successful quota provisioning, the restrictions and amount of service units, the expiry date, the price which
					// has been charged and the MSISDN of the Consumer.
					transaction.track(this, "SendProviderSMS");
					sendSubscriberSMS(provider, smsProviderAddedQuota, properties);

					// Send a configurable SMS to the Consumer to inform him of the successful quota provisioning, the restrictions and amount of service units, the expiry date and the MSISDN of
					// the Provider.
					transaction.track(this, "SendConsumerSMS");
					sendSubscriberSMS(consumer, smsConsumerAddedQuota, properties);

				}
				catch (AirException e)
				{
					return response.exitWith(cdr, e.getReturnCode(), e);
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
	// Change Quota
	//
	// /////////////////////////////////

	@Override
	public ChangeQuotaResponse changeQuota(IServiceContext context, ChangeQuotaRequest request)
	{
		// Create Response
		ChangeQuotaResponse response = super.changeQuota(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{

			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = ChangeQuotaRequest.validate(request);
				if (problem != null)
					return response.exitWith(cdr, ReturnCodes.malformedRequest, problem);

				// Validate Variant
				Variant variant = toVariant(request.getVariantID());
				if (variant == null)
					return response.exitWith(cdr, ReturnCodes.malformedRequest, "Invalid VariantID");

				// Update CDR
				cdr.setServiceID(getServiceID());
				cdr.setVariantID(variant == null ? request.getVariantID() : variant.getVariantID());
				cdr.setProcessID("AddQuota");

				// Get Provider Proxy
				Subscriber provider = getSubscriber(context, request.getSubscriberNumber(), transaction);
				try
				{
					// Get Properties
					Properties properties = getProperties(context);
					setProviderProperties(properties, provider, variant, cdr);

					// Perform GetAccountDetails UCIP call for the Provider.
					transaction.track(this, "GetAccountDetails");
					provider.getAccountDetails();

					// Test if the Provider Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!provider.isActive())
						return response.exitWith(cdr, ReturnCodes.notEligible, "Provider Account not Active");

					// Test if the Provider is in one of the allowed service classes
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass providerServiceClass = getServiceClass(provider);
					if (providerServiceClass == null || !providerServiceClass.isEligibleForProvider())
						return response.exitWith(cdr, ReturnCodes.notEligible, "Wrong Provider SC");

					// Test if the Provider is subscribed to this variant of the Shared Accounts Service, by checking the presence of the Subscription OfferID with a GetOffers UCIP call.
					transaction.track(this, "CheckIfSubscribed");
					provider.getOffers(true, false, false, true, true);
					if (!isProvider(db, provider, variant))
						return response.exitWith(cdr, ReturnCodes.notSubscribed, "Not Subscribed");
					OfferInformation subscriptionOffer = provider.getSharedOffer(variant.getSubscriptionOfferID());
					DateTime safeExpiryDate = new DateTime(subscriptionOffer.expiryDate);
					DateTime expiryDate = safeExpiryDate.addDays(-expiryMarginDays);
					setExpiryDateProperty(provider, properties, expiryDate);
					boolean fromProsumer = isProsumer(provider, variant);

					// Perform GetAccountDetails UCIP call for the Consumer.
					Subscriber consumer = new Subscriber(request.getMemberNumber().toMSISDN(), air, transaction);
					properties.setConsumerMSISDN(consumer.getNationalNumber());
					consumer.getAccountDetails();

					// Test if the Consumer Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!consumer.isActive())
						return response.exitWith(cdr, ReturnCodes.memberNotEligible, "Consumer Account not Active");

					// Test if the Consumer is in one of the allowed service classes
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass consumerServiceClass = getServiceClass(consumer);
					if (consumerServiceClass == null || !consumerServiceClass.isEligibleForConsumer())
						return response.exitWith(cdr, ReturnCodes.memberNotEligible, "Wrong Consumer SC");

					// Test if the Consumer is a consumer of the Provider, by checking the presence of the Consumer OfferID with a GetOffers UCIP call and interrogation of the C4U Lifecycle
					// Membership store.
					transaction.track(this, "CheckIfConsumer");
					consumer.getOffers(true, false, false, true, true);
					if (!isConsumer(db, provider, consumer, variant))
						return response.exitWith(cdr, ReturnCodes.notSubscribed, "Not Provider's Consumer");
					boolean toProsumer = isProsumer(consumer, variant);

					// Test if the selected combination of is allowed.
					// Example: Voice/Weekends/PeakHours/OffNet may not be supported.
					Quota oldQuota = Quota.find(config.quotas, request.getOldQuota());
					if (oldQuota == null)
						return response.exitWith(cdr, ReturnCodes.invalidQuota, "Invalid Quota");

					// Test if the selected combination of is allowed.
					// Example: Voice/Weekends/PeakHours/OffNet may not be supported.
					Quota newQuota = Quota.find(config.quotas, request.getNewQuota());
					if (newQuota == null)
						return response.exitWith(cdr, ReturnCodes.invalidQuota, "Invalid Quota");
					long newQuantity = request.getNewQuota().getQuantity();
					setQuotaProperties(properties, newQuota);
					cdr.setParam1(newQuota.getQuotaID());
					cdr.setParam2(newQuota.getServiceType().toString());

					// Only the Quantity may change
					if (!oldQuota.getServiceType().equals(newQuota.getServiceType())//
							|| !oldQuota.getDestination().equals(newQuota.getDestination())//
							|| !oldQuota.getDaysOfWeek().equals(newQuota.getDaysOfWeek())//
							|| !oldQuota.getTimeOfDay().equals(newQuota.getTimeOfDay()))
					{
						return response.exitWith(cdr, ReturnCodes.invalidQuota, "Not Same");
					}

					// Test if the Consumer has the Quota
					if (!consumer.hasProviderOffer(newQuota.getBeneficiaryOfferID()))
						return response.exitWith(cdr, ReturnCodes.invalidQuota, "Not Provisioned");

					// Get the Old Quality from the Usage Threshold
					UsageThresholdInformation thresholdInfo = consumer.getUsageThreshold(oldQuota.getBeneficiaryTotalThresholdID());
					if (thresholdInfo == null)
						return response.exitWith(cdr, ReturnCodes.technicalProblem, "UT %d not set", oldQuota.getBeneficiaryTotalThresholdID());
					Long thresholdValue = thresholdInfo.usageThresholdValue;
					long oldQuantity = (thresholdValue == null ? 0L : thresholdValue) / oldQuota.getUnitConversionFactor();

					// Test if unlimited
					boolean unlimited = newQuantity == -1;
					if (unlimited && !fromProsumer)
						return response.exitWith(cdr, ReturnCodes.quantityTooBig, "Quantity too big");

					// Test Min/Max units
					if (newQuantity < newQuota.getMinUnits() || newQuantity <= oldQuantity)
						return response.exitWith(cdr, ReturnCodes.quantityTooSmall, "Quantity too small");
					else if (!toProsumer && newQuantity > newQuota.getMaxUnits())
						return response.exitWith(cdr, ReturnCodes.quantityTooBig, "Quantity too big");

					// Calculate the quota price of the selected number of units from a configurable lookup table provided by C4U.
					long deltaQuantity = newQuantity - oldQuantity;
					long price = (newQuota.getPriceCents() * deltaQuantity + 50) / 100;

					long sharedQuantity = newQuantity;

					properties.setSharedQuantity(Long.toString(sharedQuantity));
					properties.setCharge(locale.formatCurrency(price));

					// Exit if test only
					if (request.getMode() == RequestModes.testOnly)
					{
						cdr.setChargeLevied((int) price);
						response.setChargeLevied(price);
						transaction.complete();
						return response.exitWith(cdr, ReturnCodes.successfulTest, "Success");
					}

					// Charge the Provider with the calculated price of the quota via a negative relative adjustment to his Main Account (Pre-Paid) or DA[] (Post-Paid) using an
					// UpdateBalanceAndDate UCIP Call.
					// This same UpdateBalanceAndDate UCIP call will also be used to increment the Revenue DA with an amount equal to the quota price.
					// NOTE: In the unlikely event of the quota price being 0.00 USD, the UpdateBalanceAndDate UCIP call will be omitted altogether.
					transaction.track(this, "ChargeProvider");
					if (!chargeProvider(provider, price, providerServiceClass, properties, cdr, response))
						return response.exitWith(cdr, ReturnCodes.insufficientBalance, "Insufficient Balance");

					// Increment the Service Balance DAs with the price.
					int daID = getDedicatedAccountID(newQuota);
					AccountUpdate daUpdate = new AccountUpdate(daID, Subscriber.DATYPE_MONEY, price, null, null, safeExpiryDate, null, null);
					if (toProsumer)
						consumer.updateAccounts(null, daUpdate);
					else
						provider.updateAccounts(null, daUpdate);

					// Set/Increment the corresponding Quota Usage Threshold with the number of Quota Units for the Consumer and clear the consumer's corresponding Quota Usage Counter
					// with an UpdateUsageThresholdsAndCounters UCIP call.
					// The Quota Usage Threshold will be incremented if the Provider shares an additional amount of service units with his consumer, e.g. when the consumer has depleted his allotted
					// number of service units.
					long newCSQuantity = unlimited ? UNLIMITED : newQuantity * newQuota.getUnitConversionFactor();
					consumer.setUsageThreshold(newQuota.getBeneficiaryTotalThresholdID(), newCSQuantity, null, null);
					long warningCSLevel = newCSQuantity > newQuota.getWarningMargin() ? newCSQuantity - newQuota.getWarningMargin() : newCSQuantity;
					consumer.setUsageThreshold(newQuota.getBeneficiaryWarningUsageThresholdID(), warningCSLevel, null, null);

					// Log the Activity for Reporting Purposes
					logActivity(db, cdr, newQuota, newQuantity - oldQuantity);

					// Send a configurable SMS to the Provider to inform him of the successful quota provisioning, the restrictions and amount of service units, the expiry date, the price which
					// has been charged and the MSISDN of the Consumer.
					transaction.track(this, "SendProviderSMS");
					sendSubscriberSMS(provider, smsProviderChangedQuota, properties);

					// Send a configurable SMS to the Consumer to inform him of the successful quota provisioning, the restrictions and amount of service units, the expiry date and the MSISDN of
					// the Provider.
					transaction.track(this, "SendConsumerSMS");
					sendSubscriberSMS(consumer, smsConsumerChangedQuota, properties);

				}
				catch (AirException e)
				{
					return response.exitWith(cdr, e.getReturnCode(), e);
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
	// Get Quotas
	//
	// /////////////////////////////////
	@Override
	public GetQuotasResponse getQuotas(IServiceContext context, GetQuotasRequest request)
	{
		// Create Response
		GetQuotasResponse response = super.getQuotas(context, request);

		// Language
		String languageCode = esb.getLocale().getLanguage(request.getLanguageID());

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{

			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = GetQuotasRequest.validate(request);
				if (problem != null)
					return response.exitWith(cdr, ReturnCodes.malformedRequest, problem);

				// Validate Variant
				Variant variant = toVariant(request.getVariantID());
				if (variant == null)
					return response.exitWith(cdr, ReturnCodes.malformedRequest, "Invalid VariantID");

				// Update CDR
				cdr.setServiceID(getServiceID());
				cdr.setVariantID(variant == null ? request.getVariantID() : variant.getVariantID());
				cdr.setProcessID("GetQuotas");

				// Get Provider/Consumer Proxy
				Subscriber provider = getSubscriber(context, request.getSubscriberNumber(), transaction);
				Subscriber consumer = null;
				boolean activeOnly = request.isActiveOnly();

				try
				{
					// Set Provider Properties
					Properties properties = getProperties(context);
					setProviderProperties(properties, provider, variant, cdr);

					// Check if subscribed
					transaction.track(this, "CheckIsSubscribed");
					provider.getOffers(true, false, false, true, true);
					if (!isProvider(db, provider, variant))
						return response.exitWith(cdr, ReturnCodes.notSubscribed, variant.toString());

					// Get Consumer Proxy
					if (activeOnly)
					{
						consumer = new Subscriber(request.getMemberNumber().toMSISDN(), air, transaction);
						setConsumerProperties(properties, consumer, cdr);
					}

					// Compile Quota List
					transaction.track(this, "CompileQuotaList");
					List<ServiceQuota> list = new ArrayList<ServiceQuota>();
					for (Quota quota : config.quotas)
					{
						if (!isApplicable(request.getService(), quota.getService()))
							continue;

						if (!isApplicable(request.getDestination(), quota.getDestination()))
							continue;

						if (!isApplicable(request.getDaysOfWeek(), quota.getDaysOfWeek()))
							continue;

						if (!isApplicable(request.getTimeOfDay(), quota.getTimeOfDay()))
							continue;

						if (request.getQuotaID() != null && !request.getQuotaID().equalsIgnoreCase(quota.getQuotaID()))
							continue;

						if (!activeOnly || consumer.hasProviderOffer(quota.getBeneficiaryOfferID()))
						{
							ServiceQuota serviceQuota = quota.toServiceQuota(languageCode);
							if (activeOnly && consumer != null)
							{
								UsageThresholdInformation thresholdInfo = consumer.getUsageThreshold(quota.getBeneficiaryTotalThresholdID());
								if (thresholdInfo == null)
									return response.exitWith(cdr, ReturnCodes.technicalProblem, "UT %d not set", quota.getBeneficiaryTotalThresholdID());
								Long thresholdValue = thresholdInfo.usageThresholdValue;
								long quantity = (thresholdValue == null ? 0L : thresholdValue) / quota.getUnitConversionFactor();
								serviceQuota.setQuantity(quantity);
							}
							list.add(serviceQuota);
							setQuotaProperties(properties, quota);
						}
					}

					response.setServiceQuotas(list.toArray(new ServiceQuota[list.size()]));

				}
				catch (AirException e)
				{
					return response.exitWith(cdr, e.getReturnCode(), e);
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
	// Remove Quotas
	//
	// /////////////////////////////////

	@Override
	public RemoveQuotaResponse removeQuota(IServiceContext context, RemoveQuotaRequest request)
	{
		RemoveQuotaResponse response = super.removeQuota(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{

			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = RemoveQuotaRequest.validate(request);
				if (problem != null)
					return response.exitWith(cdr, ReturnCodes.malformedRequest, problem);

				// Validate Variant
				Variant variant = toVariant(request.getVariantID());
				if (variant == null)
					return response.exitWith(cdr, ReturnCodes.malformedRequest, "Invalid VariantID");

				// Update CDR
				cdr.setServiceID(getServiceID());
				cdr.setVariantID(variant == null ? request.getVariantID() : variant.getVariantID());
				cdr.setProcessID("RemoveQuota");

				// Get Subscriber Proxy
				Subscriber provider = getSubscriber(context, request.getSubscriberNumber(), transaction);
				try
				{
					// Get Properties
					Properties properties = getProperties(context);
					setProviderProperties(properties, provider, variant, cdr);

					// Perform GetAccountDetails UCIP call for the Provider.
					transaction.track(this, "GetAccountDetails");
					provider.getAccountDetails();

					// Test if the Provider Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!provider.isActive())
						return response.exitWith(cdr, ReturnCodes.notEligible, "Provider Account not Active");

					// Test if the Provider is in one of the allowed service classes
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass providerServiceClass = getServiceClass(provider);
					if (providerServiceClass == null || !providerServiceClass.isEligibleForProvider())
						return response.exitWith(cdr, ReturnCodes.notEligible, "Wrong Provider SC");

					// Test if the Provider is subscribed to this variant of the Shared Accounts Service, by checking the presence of the Subscription OfferID with a GetOffers UCIP call.
					transaction.track(this, "CheckIfSubscribed");
					provider.getOffers(true, false, false, true, true);
					if (!isProvider(db, provider, variant))
						return response.exitWith(cdr, ReturnCodes.notSubscribed, "Not Subscribed");
					OfferInformation subscriptionOffer = provider.getSharedOffer(variant.getSubscriptionOfferID());
					DateTime safeExpiryDate = new DateTime(subscriptionOffer.expiryDate);
					DateTime expiryDate = safeExpiryDate.addDays(-expiryMarginDays);
					setExpiryDateProperty(provider, properties, expiryDate);

					// Perform GetAccountDetails UCIP call for the Consumer.
					Subscriber consumer = new Subscriber(request.getMemberNumber().toMSISDN(), air, transaction);
					setConsumerProperties(properties, consumer, cdr);
					consumer.getAccountDetails();

					// Test if the Consumer Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!consumer.isActive())
						return response.exitWith(cdr, ReturnCodes.memberNotEligible, "Consumer Account not Active");

					// Test if the Consumer is in one of the allowed service classes
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass consumerServiceClass = getServiceClass(consumer);
					if (consumerServiceClass == null || !consumerServiceClass.isEligibleForConsumer())
						return response.exitWith(cdr, ReturnCodes.memberNotEligible, "Wrong Consumer SC");

					// Test if the Consumer is a consumer of the Provider, by checking the presence of the Consumer OfferID with a GetOffers UCIP call and interrogation of the C4U Lifecycle
					// Membership store.
					transaction.track(this, "CheckIfConsumer");
					consumer.getOffers(true, false, false, true, true);
					if (!isConsumer(db, provider, consumer, variant))
						return response.exitWith(cdr, ReturnCodes.notSubscribed, "Not Provider's Consumer");

					// 9. Test if the Consumer has the selected Quota by checking if the corresponding Consumer Quota OfferID is set.
					Quota quota = Quota.find(config.quotas, request.getQuota());
					if (quota == null)
						return response.exitWith(cdr, ReturnCodes.invalidQuota, "Invalid Quota");
					cdr.setParam1(quota.getQuotaID());
					cdr.setParam2(quota.getServiceType().toString());
					setQuotaProperties(properties, quota);
					if (!consumer.hasProviderOffer(quota.getBeneficiaryOfferID()))
						return response.exitWith(cdr, ReturnCodes.quotaNotSet, "No such Quota");

					// Charge the Provider with quota removal fee via a negative relative adjustment to his Main Account (Pre-Paid) or DA (Post-Paid) using an UpdateBalanceAndDate
					// UCIP Call.
					// This same UpdateBalanceAndDate UCIP call will also be used to increment the Revenue DA with an amount equal to the quota price.
					// NOTE: If the quota removal fee is configured to be 0.00 USD, the UpdateBalanceAndDate UCIP call will be omitted altogether.
					transaction.track(this, "ChargeProvider");
					int charge = providerServiceClass.getRemoveQuotaCharge();
					if (request.getMode() == RequestModes.testOnly)
					{
						properties.setCharge(locale.formatCurrency(charge));
						cdr.setChargeLevied((int) charge);
						response.setChargeLevied(charge);
						transaction.complete();
						return response.exitWith(cdr, ReturnCodes.successfulTest, "Success");
					}
					if (!chargeProvider(provider, charge, providerServiceClass, properties, cdr, response))
						return response.exitWith(cdr, ReturnCodes.insufficientBalance, "Insufficient Balance");

					// Get the number of Units
					long quantity = 0;
					UsageThresholdInformation thresholdInfo = consumer.getUsageThreshold(quota.getBeneficiaryTotalThresholdID());
					if (thresholdInfo != null)
					{
						Long thresholdValue = thresholdInfo.usageThresholdValue;
						if (thresholdValue != null)
						{
							quantity = thresholdValue / quota.getUnitConversionFactor();
						}
					}

					// Delete the Consumer Quota OfferID for the Consumer with a DeleteOffer ACIP call.
					consumer.deleteProviderOffer(quota.getBeneficiaryOfferID());

					// Delete the corresponding Quota Usage Threshold for the Consumer with a DeleteUsageThresholds ACIP call.
					consumer.deleteUsageThresholds(null, quota.getBeneficiaryTotalThresholdID(), quota.getBeneficiaryWarningUsageThresholdID());

					// Clear the consumer's corresponding Quota Usage Counter with an UpdateUsageThresholdsAndCounters UCIP call.
					consumer.clearUsageCounters(null, quota.getBeneficiaryUsageCounterID());

					// Log the Activity for Reporting Purposes
					logActivity(db, cdr, quota, -quantity);

					// Send a configurable SMS to the Provider to inform him of the successful quota removal, the fee which has been charged and the MSISDN of the Consumer.
					transaction.track(this, "SendProviderSMS");
					sendSubscriberSMS(provider, smsProviderRemovedQuota, properties);

					// Send a configurable SMS to the Consumer to inform him of the successful quota removal and the MSISDN of the Provider.
					transaction.track(this, "SendConsumerSMS");
					sendSubscriberSMS(consumer, smsConsumerRemovedQuota, properties);

				}
				catch (AirException e)
				{
					return response.exitWith(cdr, e.getReturnCode(), e);
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
	// Process Lifecycle Events
	//
	// /////////////////////////////////

	@Override
	public ProcessLifecycleEventResponse processLifecycleEvent(IServiceContext context, ProcessLifecycleEventRequest request)
	{
		final int SUBSCRIBER_NOT_FOUND = 102;

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
					return response.exitWith(cdr, ReturnCodes.malformedRequest, problem);

				// Validate Variant
				Variant variant = toVariant(request.getVariantID());
				if (variant == null)
					return response.exitWith(cdr, ReturnCodes.malformedRequest, "Invalid VariantID");

				// Update CDR
				cdr.setServiceID(getServiceID());
				cdr.setVariantID(variant == null ? request.getVariantID() : variant.getVariantID());
				cdr.setProcessID("ProcessLifecycleEvent");

				// Get Provider Proxy
				Subscriber provider = getSubscriber(context, new Number(subscription.getMsisdn()), transaction);
				try
				{
					// Get Properties
					Properties properties = getProperties(context);
					setProviderProperties(properties, provider, variant, cdr);

					// Perform GetAccountDetails UCIP call for the Provider.
					try
					{
						transaction.track(this, "GetAccountDetails");
						provider.getAccountDetails();
					}
					catch (AirException ae)
					{
						if (ae.getResponseCode() == SUBSCRIBER_NOT_FOUND)
						{
							String[] consumerList = lifecycle.getMembers(db, provider, getServiceID(), variant.getVariantID());
							for (String consumerMSISDN : consumerList)
							{
								Subscriber consumer = new Subscriber(consumerMSISDN, air, transaction);
								setConsumerProperties(properties, consumer, cdr);
								consumer.getOffers(true, false, false, true, true);
								removeConsumer(db, provider, consumer, transaction, variant, properties, smsProviderRemovedConsumer, cdr);
							}

							lifecycle.removeSubscription(db, provider, request.getServiceID(), request.getVariantID());
							transaction.complete();
							return response.exitWith(cdr, ae.getReturnCode(), "Subscriber not Found");
						}

						throw ae;
					}

					// Test if the Provider Account is in a valid state, i.e. all lifecycle dates are in the future.
					transaction.track(this, "CheckAccountActive");
					if (!provider.isActive())
					{
						forceUnsubscribe(cdr, db, transaction, variant, provider, properties);
						return response.exitWith(cdr, ReturnCodes.notEligible, "Provider Account not Active");
					}

					// Test if the Provider is in one of the allowed service classes
					transaction.track(this, "CheckValidServiceClass");
					ServiceClass providerServiceClass = getServiceClass(provider);
					if (providerServiceClass == null || !providerServiceClass.isEligibleForProvider())
					{
						forceUnsubscribe(cdr, db, transaction, variant, provider, properties);
						return response.exitWith(cdr, ReturnCodes.notEligible, "Wrong Provider SC");
					}

					// Test if the Provider is subscribed to this variant of the Shared Accounts Service, by checking the presence of the Subscription OfferID with a GetOffers UCIP call.
					transaction.track(this, "CheckIfSubscribed");
					provider.getOffers(true, false, false, true, true);

					if (!lifecycle.isSubscribed(db, provider, getServiceID(), variant.getVariantID()))
						return response.exitWith(cdr, ReturnCodes.notSubscribed, "Not Subscribed");
					if (request.getDateTime3() == null)
						return response.exitWith(cdr, ReturnCodes.malformedRequest, "Invalid Date Time 3");
					DateTime renewalTime = new DateTime(request.getDateTime3());
					DateTime expiryDate = renewalTime.addDays(-1);
					setExpiryDateProperty(provider, properties, expiryDate);

					// Test if subscribed on AIR
					OfferInformation subscriptionOffer = provider.getSharedOffer(variant.getSubscriptionOfferID());
					if (subscriptionOffer == null)
					{
						forceUnsubscribe(cdr, db, transaction, variant, provider, properties);
						return response.exitWith(cdr, ReturnCodes.notSubscribed, "Not Subscribed on AIR");
					}

					// Test if Renewals are enabled
					if (!config.isAutoRenew())
					{
						forceUnsubscribe(cdr, db, transaction, variant, provider, properties);
						return response.exitWith(cdr, ReturnCodes.success, "Not Renewable");
					}

					// Calculate hours before Renewal
					DateTime now = DateTime.getNow();
					long hoursBeforeExpiry = now.millisTo(renewalTime) / DateTime.MILLIS_PER_HOUR;
					properties.setHoursBeforeExpiry(Long.toString(hoursBeforeExpiry));

					// Switch on the State
					switch (subscription.getState())
					{
						case NEXT_SEND_FIRST_RENEW_WARNING:
							calculateRenewalCost(db, provider, variant, properties, cdr, transaction);
							transaction.track(this, "SendProviderSMS");
							sendSubscriberSMS(provider, smsProviderRenewalWarning, properties);
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
							calculateRenewalCost(db, provider, variant, properties, cdr, transaction);
							transaction.track(this, "SendProviderSMS");
							sendSubscriberSMS(provider, smsProviderRenewalWarning, properties);
							subscription.setState(NEXT_RENEW_SUBSCRIPTION);
							subscription.setNextDateTime(renewalTime);
							subscription.setBeingProcessed(false);
							lifecycle.updateSubscription(db, subscription);
							break;

						case NEXT_RENEW_SUBSCRIPTION:
							renewSubscription(db, provider, variant, properties, cdr, transaction, providerServiceClass, response, expiryDate);
							if (response.getReturnCode() != ReturnCodes.success)
								return response;
							break;

						case NEXT_SEND_THRESHOLD_NOTIFICATION:
							transaction.track(this, "SendThresholdSMS");
							sendSubscriberSMS(provider, smsProviderRenewalWarning, properties);
							subscription.setBeingProcessed(false);
							break;

						default:
							return response.exitWith(cdr, ReturnCodes.notEligible, "Invalid Lifecycle State");
					}

				}
				catch (AirException e)
				{
					return response.exitWith(cdr, e.getReturnCode(), e);
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

	private void forceUnsubscribe(CdrBase cdr, IDatabaseConnection db, Transaction<?> transaction, Variant variant, Subscriber provider, Properties properties) throws SQLException, AirException
	{
		properties.setCharge(locale.formatCurrency(0L));
		String[] consumerList = lifecycle.getMembers(db, provider, getServiceID(), variant.getVariantID());
		unsubscribe(db, provider, consumerList, variant, cdr, transaction, properties);
		transaction.complete();
	}

	private ProcessLifecycleEventResponse renewSubscription(IDatabaseConnection db, Subscriber provider, Variant variant, //
			Properties properties, ICdr cdr, Transaction<?> transaction, ServiceClass providerServiceClass, //
			ProcessLifecycleEventResponse response, DateTime expiryDate) throws SQLException, AirException
	{
		long charge = variant.getRenewalCharge();
		List<RenewInformation> renewInformationList = new ArrayList<RenewInformation>();
		DateTime newExpiryDate = expiryDate.addDays(variant.getSafeValidityPeriodDays());
		DateTime newSafeExpiryDate = newExpiryDate.addDays(expiryMarginDays);
		List<Quota> activeQuotas = new ArrayList<Quota>();

		long newVoiceDAValue = 0L;
		long newDataDAValue = 0L;
		long newMMSDAValue = 0L;
		long newSMSDAValue = 0L;
		long newAirtimeDAValue = 0L;

		// Get List of Consumers
		String[] consumerList = lifecycle.getMembers(db, provider, getServiceID(), variant.getVariantID());
		for (String consumerMSISDN : consumerList)
		{
			try
			{
				// Perform GetAccountDetails UCIP call for the Consumer.
				Subscriber consumer = new Subscriber(consumerMSISDN, air, transaction);
				setConsumerProperties(properties, consumer, cdr);
				consumer.getAccountDetails();

				// Test if the Consumer Account is in a valid state, i.e. all lifecycle dates are in the future.
				transaction.track(this, "CheckAccountActive");
				if (!consumer.isActive())
				{
					removeConsumer(db, provider, consumer, transaction, variant, properties, smsProviderRemovedConsumer, cdr);
					continue;
				}

				// Test if the Consumer is in one of the allowed service classes
				transaction.track(this, "CheckValidServiceClass");
				ServiceClass consumerServiceClass = getServiceClass(consumer);
				if (consumerServiceClass == null || !consumerServiceClass.isEligibleForConsumer())
				{
					removeConsumer(db, provider, consumer, transaction, variant, properties, smsProviderRemovedConsumer, cdr);
					continue;
				}

				// Create Renew Information
				RenewInformation renewInformation = new RenewInformation();
				renewInformation.consumer = consumer;
				renewInformationList.add(renewInformation);

				// For Each Quota
				for (Quota quota : config.getQuotas())
				{
					// Test if the consumer has this quota
					UsageThresholdInformation ut = consumer.getUsageThreshold(quota.getBeneficiaryTotalThresholdID());
					if (ut == null || ut.usageThresholdValue == null || ut.usageThresholdValue == 0)
						continue;

					// Record Quota as being Active
					if (!activeQuotas.contains(quota))
						activeQuotas.add(quota);

					// Add to result
					long price = (quota.getPriceCents() * ut.usageThresholdValue / quota.getUnitConversionFactor() + 50) / 100;
					charge += price;
					switch (quota.getServiceType())
					{
						case VOICE:
							newVoiceDAValue += price;
							break;

						case DATA:
							newDataDAValue += price;
							break;

						case SMS:
							newSMSDAValue += price;
							break;

						case MMS:
							newMMSDAValue += price;
							break;

						case AIRTIME:
							newAirtimeDAValue += price;
							break;

						default:
							throw new AirException(999);
					}
					renewInformation.ucToClear.add(quota.getBeneficiaryUsageCounterID());
					renewInformation.providerOffersToExtend.add(quota.getBeneficiaryOfferID());

				}
			}
			catch (AirException ex)
			{
				continue;
			}

		} // foreach Consumer

		// Charge the Provider with the total renewal charge (fee + quotas) for the selected variant via a negative relative
		// adjustment to his Main Account (Pre-Paid) or DA[] (Post-Paid) using an UpdateBalanceAndDate UCIP Call.
		transaction.track(this, "ChargeProvider");
		if (!chargeProvider(provider, charge, providerServiceClass, properties, cdr, response))
		{
			properties.setCharge(locale.formatCurrency(0L));
			unsubscribe(db, provider, consumerList, variant, cdr, transaction, properties);
			transaction.complete();
			return response.exitWith(cdr, ReturnCodes.insufficientBalance, "Insufficient Balance");
		}

		// Load the new DA Values
		List<AccountUpdate> accountUpdates = new ArrayList<AccountUpdate>();

		if (newSMSDAValue != 0)
			accountUpdates.add(new AccountUpdate(config.getSmsDedicatedAccountID(), Subscriber.DATYPE_MONEY, null, newSMSDAValue, null, newSafeExpiryDate, null, null));

		if (newMMSDAValue != 0)
			accountUpdates.add(new AccountUpdate(config.getMmsDedicatedAccountID(), Subscriber.DATYPE_MONEY, null, newMMSDAValue, null, newSafeExpiryDate, null, null));

		if (newVoiceDAValue != 0)
			accountUpdates.add(new AccountUpdate(config.getVoiceDedicatedAccountID(), Subscriber.DATYPE_MONEY, null, newVoiceDAValue, null, newSafeExpiryDate, null, null));

		if (newAirtimeDAValue != 0)
			accountUpdates.add(new AccountUpdate(config.getAirtimeDedicatedAccountID(), Subscriber.DATYPE_MONEY, null, newAirtimeDAValue, null, newSafeExpiryDate, null, null));

		if (newDataDAValue != 0)
			accountUpdates.add(new AccountUpdate(config.getDataDedicatedAccountID(), Subscriber.DATYPE_MONEY, null, newDataDAValue, null, newSafeExpiryDate, null, null));

		if (accountUpdates.size() > 0)
			provider.updateAccounts(null, accountUpdates.toArray(new AccountUpdate[accountUpdates.size()]));

		// Extend the offers for each Consumer and clear their UCs
		for (RenewInformation renewInformation : renewInformationList)
		{
			// Extend Offers
			for (Integer offerID : renewInformation.providerOffersToExtend)
			{
				renewInformation.consumer.updateProviderOffer(offerID, null, null, newSafeExpiryDate, null, provider);
			}

			// Extend Beneficiary Offer ID
			renewInformation.consumer.updateProviderOffer(variant.getConsumerOfferID(), null, null, newSafeExpiryDate, null, provider);

			// Clear UCs
			renewInformation.consumer.clearUsageCounters(null, renewInformation.ucToClear.toArray(new Integer[0]));
		}

		// Extend the offers of the Provider
		for (Quota quota : activeQuotas)
		{
			provider.updateSharedOffer(quota.getSponsorOfferID(), null, null, newSafeExpiryDate, null);
			provider.updateProviderOffer(quota.getBeneficiaryOfferID(), null, null, newSafeExpiryDate, null, provider);
		}

		// Update the Subsription Offer ID
		provider.updateSharedOffer(variant.getSubscriptionOfferID(), null, null, newSafeExpiryDate, null);

		// Update the Lifecycle Store
		addProviderLifecycle(db, provider, variant, newExpiryDate);

		// Send Notification to Provider
		properties.setNewExpiryDate(locale.formatDate(newExpiryDate, provider.getLanguageID()));
		sendSubscriberSMS(provider, smsProviderRenewed, properties);

		// Send Notification to Consumers
		for (RenewInformation renewInformation : renewInformationList)
		{
			Subscriber consumer = renewInformation.consumer;
			properties.setNewExpiryDate(locale.formatDate(newExpiryDate, consumer.getLanguageID()));
			sendSubscriberSMS(consumer, smsConsumerRenewed, properties);
		}

		return response.exitWith(cdr, ReturnCodes.success, "success");

	}

	private class RenewInformation
	{
		Subscriber consumer;
		List<Integer> ucToClear = new ArrayList<Integer>();
		List<Integer> providerOffersToExtend = new ArrayList<Integer>();
	}

	private long calculateRenewalCost(IDatabaseConnection db, Subscriber provider, Variant variant, //
			Properties properties, ICdr cdr, Transaction<?> transaction) throws SQLException, AirException
	{
		long result = variant.getRenewalCharge();

		// Get List of Consumers
		String[] consumerList = lifecycle.getMembers(db, provider, getServiceID(), variant.getVariantID());
		for (String consumerMSISDN : consumerList)
		{
			try
			{
				// Perform GetAccountDetails UCIP call for the Consumer.
				Subscriber consumer = new Subscriber(consumerMSISDN, air, transaction);
				setConsumerProperties(properties, consumer, cdr);
				consumer.getAccountDetails();

				// Test if the Consumer Account is in a valid state, i.e. all lifecycle dates are in the future.
				transaction.track(this, "CheckAccountActive");
				if (!consumer.isActive())
					continue;

				// Test if the Consumer is in one of the allowed service classes
				transaction.track(this, "CheckValidServiceClass");
				ServiceClass consumerServiceClass = getServiceClass(consumer);
				if (consumerServiceClass == null || !consumerServiceClass.isEligibleForConsumer())
					continue;

				// Test if the Consumer is a consumer of the Provider, by checking the presence of the Consumer OfferID with a GetOffers UCIP call
				transaction.track(this, "CheckIfConsumer");
				consumer.getOffers(true, false, false, true, true);
				if (!isConsumer(db, provider, consumer, variant))
					continue;

				// For Each Quota
				for (Quota quota : config.getQuotas())
				{
					// Test if the consumer has this quota
					if (!consumer.hasProviderOffer(quota.getBeneficiaryOfferID()))
						continue;

					// Add to result
					UsageThresholdInformation ut = consumer.getUsageThreshold(quota.getBeneficiaryTotalThresholdID());
					if (ut != null && ut.usageThresholdValue != null)
					{
						result += (quota.getPriceCents() * ut.usageThresholdValue / quota.getUnitConversionFactor() + 50) / 100;
					}
				}

			}
			catch (AirException ex)
			{
				continue;
			}

		}

		properties.setCharge(Long.toString(result));
		return result;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Balances
	//
	// /////////////////////////////////

	@Override
	public GetBalancesResponse getBalances(IServiceContext context, GetBalancesRequest request)
	{
		DateTime expiryDate = DateTime.getNow();
		int languageID = esb.getLocale().getLanguageID(request.getLanguageID());
		String languageCode = esb.getLocale().getLanguage(request.getLanguageID());

		// Create Response
		GetBalancesResponse response = super.getBalances(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = GetBalancesRequest.validate(request);
				if (problem != null)
					return response.exitWith(cdr, ReturnCodes.malformedRequest, problem);

				// Get the Variant, if Specified
				Variant variant = toVariant(request.getVariantID());

				// Update CDR
				cdr.setServiceID(getServiceID());
				cdr.setVariantID(variant == null ? request.getVariantID() : variant.getVariantID());
				cdr.setProcessID("GetBalances");

				// Get Provider/Consumer Proxy
				Subscriber subscriber = getSubscriber(context, request.getSubscriberNumber(), transaction);
				try
				{
					// Get Offers
					subscriber.getOffers(true, false, false, true, true);

					// Get Properties
					Properties properties = getProperties(context);
					cdr.setA_MSISDN(subscriber.getInternationalNumber());

					// Get Balance and Date
					transaction.track(this, "GetBalanceAndDate");
					subscriber.getBalanceAndDate();

					// Get Offer Flags for Subscriber
					subscriber.getOffers(true, false, false, true, true);

					// Get Service Class
					ServiceClass subscriberServiceClass = getServiceClass(subscriber);
					if (subscriberServiceClass == null)
						return response.exitWith(cdr, ReturnCodes.notEligible, "Wrong SC");

					// Test if Subscribed
					transaction.track(this, "CheckIfProviderOrConsumer");
					boolean isProvider = false;
					boolean isConsumer = false;
					Subscriber provider = null;
					for (Variant var : config.getVariants())
					{
						// Skip if Variant doesn't match optional supplied variantID
						if (variant != null && !var.getVariantID().equalsIgnoreCase(variant.getVariantID()))
							continue;

						// Test if this is a Provider
						OfferInformation offer = subscriber.getSharedOffer(var.getSubscriptionOfferID());
						if (isProvider(db, subscriber, var))
						{
							isProvider = true;
							provider = subscriber;
							variant = var;
							properties.setVariant(variant.getNames());
							properties.setProviderMSISDN(subscriber.getNationalNumber());
							DateTime safeExpiryDate = new DateTime(offer.expiryDate);
							expiryDate = safeExpiryDate.addDays(-expiryMarginDays);
							properties.setExpiryDate(locale.formatDate(expiryDate, languageID));
							break;
						}

						else
						{
							// Test if this is a Consumer
							offer = subscriber.getProviderOffer(var.getConsumerOfferID());
							if (offer != null)
							{
								provider = new Subscriber(offer.offerProviderID, air, transaction);
								if (isConsumer(db, provider, subscriber, var))
								{
									isConsumer = true;
									variant = var;
									properties.setVariant(variant.getNames());
									properties.setProviderMSISDN(provider.getNationalNumber());
									properties.setConsumerMSISDN(subscriber.getNationalNumber());
									DateTime safeExpiryDate = new DateTime(offer.expiryDate);
									expiryDate = safeExpiryDate.addDays(-expiryMarginDays);
									properties.setExpiryDate(locale.formatDate(expiryDate, languageID));
									break;
								}
							}
						}

					}

					// Exit if not subscribed
					if (!isProvider && !isConsumer)
						return response.exitWith(cdr, ReturnCodes.notSubscribed, "Not Subscribed");

					// Update Properties

					// Compile Balances for Provider
					ServiceBalance[] balances = null;
					if (isProvider)
					{
						response.setBalances(getProviderBalances(subscriber, languageCode, transaction, expiryDate));
						if (request.getRequestSMS())
						{
							balances = subscriber.getLanguageID() == languageID ? response.getBalances() //
									: getProviderBalances(subscriber, esb.getLocale().getLanguage(subscriber.getLanguageID()), transaction, expiryDate);
						}
					}

					// Compile Balances for Consumer
					else if (isConsumer)
					{
						response.setBalances(getConsumerBalances(provider, subscriber, languageCode, transaction, expiryDate));
						if (request.getRequestSMS())
						{
							balances = subscriber.getLanguageID() == languageID ? response.getBalances() //
									: getConsumerBalances(provider, subscriber, esb.getLocale().getLanguage(subscriber.getLanguageID()), transaction, expiryDate);
						}
					}

					// Charge the Provider/Consumer for the Balance Enquiry of the selected variant via a negative relative adjustment to his
					// Main Account (Pre-Paid) or DA[] (Post-Paid) using an UpdateBalanceAndDate UCIP Call.
					// This same UpdateBalanceAndDate UCIP call will also be used to increment the Revenue DA with an amount equal to the consumer addition fee.
					// NOTE: If the Consumer Addition fee is configured to be 0.00 USD, the UpdateBalanceAndDate UCIP call will be omitted altogether
					transaction.track(this, "ChargeProvider");
					int charge = isProvider ? subscriberServiceClass.getProviderBalanceEnquiryCharge() : subscriberServiceClass.getConsumerBalanceEnquiryCharge();
					if (request.getMode() == RequestModes.testOnly)
					{
						cdr.setChargeLevied((int) charge);
						properties.setCharge(locale.formatCurrency(charge));
						response.setChargeLevied(charge);
						transaction.complete();
						return response.exitWith(cdr, ReturnCodes.successfulTest, "Success");
					}
					ServiceClass serviceClass = getServiceClass(subscriber);
					if (!chargeProvider(subscriber, charge, serviceClass, properties, cdr, response))
						return response.exitWith(cdr, ReturnCodes.insufficientBalance, "Insufficient Balance");

					// Send SMS if Required
					if (request.getRequestSMS())
					{
						Integer language = subscriber.getLanguageID();
						String lang = esb.getLocale().getLanguage(language);
						transaction.track(this, "SendBalanceSMS");
						StringBuilder sb = new StringBuilder();
						boolean first = true;
						for (ServiceBalance balance : balances)
						{
							properties.setSharedQuantity(Long.toString(balance.getValue()));
							properties.setService(new Phrase(lang, balance.getName()));
							properties.setUnits(new Phrase(lang, balance.getUnit()));
							properties.setQuotaName(new Phrase(lang, balance.getName()));

							int smsBalanceFormat = isProvider ? smsProviderBalanceFormat : smsConsumerBalanceFormat;
							INotificationText format = notifications.get(smsBalanceFormat, lang, locale, properties);

							if (first)
								first = false;
							else
								sb.append(", ");
							sb.append(format.getText());
							first = false;
						}
						properties.setBalanceList(sb.toString());

						if (isProvider)
							sendSubscriberSMS(subscriber, smsProviderBalances, properties);
						else if (isConsumer)
							sendSubscriberSMS(subscriber, smsConsumerBalances, properties);
					}

				}
				catch (AirException e)
				{
					return response.exitWith(cdr, e.getReturnCode(), e);
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
	// Consumer Declined
	//
	// /////////////////////////////////

	@Override
	protected void onConsumerDeclined(IInteraction message)
	{
		// Create a CDR
		CdrBase cdr = new CsvCdr(message, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Create Properties
		int errorMessageID = super.smsConsumerDeclineFailed;
		Subscriber consumer = null;
		Properties properties = new Properties();

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Update CDR
				cdr.setServiceID(getServiceID());
				cdr.setProcessID("ConsumerDeclined");

				// Create Consumer Proxy
				consumer = new Subscriber(message.getMSISDN(), air, transaction);

				// Test if this is a consumer
				Variant variant = null;
				OfferInformation offer = null;
				Subscriber provider = null;
				for (Variant var : config.getVariants())
				{
					// Test if this is a Consumer
					offer = consumer.getProviderOffer(var.getConsumerOfferID());
					if (offer != null)
					{
						provider = new Subscriber(offer.offerProviderID, air, transaction);
						if (isConsumer(db, provider, consumer, var))
						{
							variant = var;
							properties.setVariant(variant.getNames());
							properties.setProviderMSISDN(offer.offerProviderID);
							properties.setConsumerMSISDN(consumer.getNationalNumber());
							properties.setExpiryDate(locale.formatDate(offer.expiryDate, consumer.getLanguageID()));
							break;
						}

					}
				}

				if (variant == null || provider == null)
				{
					cdr.setReturnCode(ReturnCodes.notMember);
					errorMessageID = super.smsConsumerDeclineNotOne;
					return;
				}
				cdr.setVariantID(variant.getVariantID());

				// Create Provider Proxy
				cdr.setB_MSISDN(provider.getInternationalNumber());

				// Remove Consumer
				removeConsumer(db, provider, consumer, transaction, variant, properties, smsProviderConsumerDeclined, cdr);

				// Complete
				transaction.complete();
				cdr.setReturnCode(ReturnCodes.success);
				errorMessageID = 0;
				return;

			}
			catch (AirException e)
			{
				logger.error(e.getMessage(), e);
				cdr.setReturnCode(e.getReturnCode());
				cdr.setAdditionalInformation(e.getMessage());
				return;
			}

		}
		catch (Throwable e)
		{
			logger.error(e.getMessage(), e);
			cdr.setReturnCode(ReturnCodes.technicalProblem);
			cdr.setAdditionalInformation(e.getMessage());
			return;
		}
		finally
		{
			if (errorMessageID != 0 && consumer != null)
				sendSubscriberSMS(consumer, errorMessageID, properties);
		}

	} // onConsumerDeclined()

	@Override
	protected void onThresholdNotification(ThresholdNotificationFileV2 message)
	{
		CdrBase cdr = new CsvCdr();

		Subscriber subscriber = new Subscriber(message.subscriberID, air, null);

		// Create Properties
		Properties properties = new Properties();
		properties.setNotificationThreshold(Double.toString(message.thresholdLimit));

		sendSubscriberSMS(subscriber, super.smsConsumerThresholds, properties);

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Update CDR
				cdr.setServiceID(getServiceID());
				cdr.setProcessID("onThresholdNotificationV2");

				// Test if this is a consumer
				Variant variant = null;
				OfferInformation offer = null;
				Subscriber provider = null;
				for (Variant v : config.getVariants())
				{
					// Test if this is a Consumer
					offer = subscriber.getProviderOffer(v.getConsumerOfferID());
					if (offer != null)
					{
						provider = new Subscriber(offer.offerProviderID, air, transaction);
						if (isConsumer(db, provider, subscriber, v))
						{
							variant = v;
							properties.setVariant(variant.getNames());
							properties.setProviderMSISDN(offer.offerProviderID);
							properties.setConsumerMSISDN(subscriber.getNationalNumber());
							properties.setExpiryDate(locale.formatDate(offer.expiryDate, subscriber.getLanguageID()));
							break;
						}

					}
				}

				if (variant == null || provider == null)
				{
					cdr.setReturnCode(ReturnCodes.notMember);
					return;
				}
				cdr.setVariantID(variant.getVariantID());

				// Complete
				transaction.complete();
				cdr.setReturnCode(ReturnCodes.success);
				return;

			}
			catch (AirException e)
			{
				logger.error(e.getMessage(), e);
				cdr.setReturnCode(e.getReturnCode());
				cdr.setAdditionalInformation(e.getMessage());
				return;
			}

		}
		catch (Throwable e)
		{
			logger.error(e.getMessage(), e);
			cdr.setReturnCode(ReturnCodes.technicalProblem);
			cdr.setAdditionalInformation(e.getMessage());
			return;
		}

	}

	@Override
	protected void onThresholdNotification(DedicatedAccountsFileV3_3 message)
	{

	}

	@Override
	protected void onThresholdNotification(ThresholdNotificationFileV3 message)
	{
		// Create a CDR
		String transactionNumber = esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH);

		// Create Properties
		Properties properties = new Properties();
		properties.setNotificationThreshold(Double.toString(message.thresholdLimit));

		GetMembersRequest request = new GetMembersRequest();
		CdrBase cdr = new CsvCdr(request, transactionNumber);

		request.setServiceID(this.getServiceID());
		request.setSubscriberNumber(new Number(message.subscriberID));
		request.setCallerID(message.subscriberID);
		request.setTransactionID(transactionNumber);

		IServiceContext context = new ServiceContext();
		context.setProperties(properties);

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Update CDR
				cdr.setServiceID(getServiceID());
				cdr.setProcessID("onThresholdNotificationV3");
				cdr.setCallerID(message.subscriberID);
				cdr.setTransactionID(transactionNumber);
				cdr.setChannel(Channels.BATCH);// file processing

				Subscriber prosumer = air.getSubscriber(message.subscriberID, transaction);
				// Test if this is a consumer
				Variant variant = null;
				Variant variants[] = config.getVariants();
				boolean isProsumer = false;
				boolean isProvider = false;
				boolean isConsumer = false;

				// Pick the Variant for which this MSISDN is a designated prosumer/provider
				for (Variant v : variants)
				{
					isProsumer = isProsumer(prosumer, v);
					String logMsg;
					if (isProsumer)
					{
						logMsg = "isProsumer [true]" + prosumer.getInternationalNumber();
						logger.info(logMsg);
					}

					isProvider = isProvider(db, prosumer, v);
					if (isProvider)
					{
						logMsg = "isProvider [true]" + prosumer.getInternationalNumber();
						logger.info(logMsg);
					}

					if (isProsumer || isProvider)
					{
						variant = v;
						properties.setVariant(variant.getNames());
						setProviderProperties(properties, prosumer, variant, cdr);

						break;
					}

				}// for(..)

				if (variant == null || isProvider == false)
				{
					cdr.setReturnCode(ReturnCodes.notMember);
					failedNotifications++;
					notificationMetric.report(esb, successfulNotifications, failedNotifications);
					return;
				}

				String variantID = variant.getVariantID();
				request.setVariantID(variantID);// which variant???
				cdr.setVariantID(variantID);

				// Send threshold SMS notifications to provider/prosumer
				if (isProvider)
				{
					transaction.track(this, "SendProviderThresholdSMS");
					sendSubscriberSMS(prosumer, super.smsProviderThresholds, properties);
				}
				else if (isProsumer)
				{
					transaction.track(this, "SendProsumerThresholdSMS");
					sendSubscriberSMS(prosumer, super.smsProsumerThresholds, properties);
				}

				// Send threshold SMS to all beneficiaries
				ILifecycle lifecycle = esb.getFirstConnector(ILifecycle.class);
				String[] memberMSISDNs = lifecycle.getMembers(db, prosumer, getServiceID(), variantID);

				Subscriber smsRecipient = null;
				for (String msisdn : memberMSISDNs)
				{
					smsRecipient = new Subscriber(msisdn, air, null);
					isConsumer = isConsumer(db, prosumer, smsRecipient, variant);
					if (isConsumer)
					{
						setConsumerProperties(properties, smsRecipient, cdr);
						transaction.track(this, "SendConsumerThresholdSMS");
						sendSubscriberSMS(smsRecipient, super.smsConsumerThresholds, properties);
					}
					else
					{
						logger.info(String.format("{} is not a consumer, notification SMS not sent!", msisdn));
					}
				}

				for (Quota quota : config.quotas)
				{
					if (quota.getServiceType() == ServiceType.VOICE)
					{
						cdr.setParam1(quota.getQuotaID());
						cdr.setParam2(quota.getServiceType().toString());
						break;
					}
				}

				cdr.setReturnCode(ReturnCodes.success);
				cdr.setAdditionalInformation("Success");

				// Complete
				transaction.complete();

				successfulNotifications++;
				notificationMetric.report(esb, successfulNotifications, failedNotifications);
				return;
			}
			// catch (AirException e)
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
				cdr.setReturnCode(ReturnCodes.technicalProblem);
				cdr.setAdditionalInformation(e.getMessage());

				failedNotifications++;
				notificationMetric.report(esb, successfulNotifications, failedNotifications);
				return;
			}

		}
		catch (Throwable e)
		{
			logger.error(e.getMessage(), e);
			cdr.setReturnCode(ReturnCodes.technicalProblem);
			cdr.setAdditionalInformation(e.getMessage());

			failedNotifications++;
			notificationMetric.report(esb, successfulNotifications, failedNotifications);
			return;
		}

	} // onThresholdNotification()

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	private ServiceBalance[] getProviderBalances(Subscriber provider, String languageCode, Transaction<?> transaction, Date expiryDate) throws AirException
	{
		ServiceBalance[] result = new ServiceBalance[10];
		int index = 0;

		// Main
		{
			ServiceBalance main = new ServiceBalance();
			Long mainAccountValue = provider.getAccountValue1();
			if (mainAccountValue == null)
				mainAccountValue = 0L;
			main.setValue(mainAccountValue);
			main.setName(config.mainAccountBalanceName.getSafe(languageCode, "main"));
			main.setUnit(esb.getLocale().getCurrencyCode());
			main.setExpiryDate(provider.getSupervisionExpiryDate());
			result[index++] = main;
		}

		// Voice
		transaction.track(this, "Query Voice");
		ServiceBalance balance = getProviderBalance(ServiceType.VOICE, provider, config.getVoiceDedicatedAccountID(), languageCode, expiryDate);
		if (balance != null)
			result[index++] = balance;

		// SMS
		transaction.track(this, "Query SMS");
		balance = getProviderBalance(ServiceType.SMS, provider, config.getSmsDedicatedAccountID(), languageCode, expiryDate);
		if (balance != null)
			result[index++] = balance;

		// Data
		transaction.track(this, "Query Data");
		balance = getProviderBalance(ServiceType.DATA, provider, config.getDataDedicatedAccountID(), languageCode, expiryDate);
		if (balance != null)
			result[index++] = balance;

		// MMS
		transaction.track(this, "Query MMS");
		balance = getProviderBalance(ServiceType.MMS, provider, config.getMmsDedicatedAccountID(), languageCode, expiryDate);
		if (balance != null)
			result[index++] = balance;

		// Voice
		transaction.track(this, "Query Airtime");
		balance = getProviderBalance(ServiceType.AIRTIME, provider, config.getAirtimeDedicatedAccountID(), languageCode, expiryDate);
		if (balance != null)
			result[index++] = balance;

		return Arrays.copyOf(result, index);

	}

	private ServiceBalance getProviderBalance(ServiceType serviceType, Subscriber provider, int dedicatedAccountID, String languageCode, Date expiryDate) throws AirException
	{
		for (Quota quota : config.getQuotas())
		{
			if (quota.getServiceType() == serviceType)
			{
				// Get the Corresponding DA
				DedicatedAccountInformation dedicatedAccount = provider.getDedicatedAccount(dedicatedAccountID);
				if (dedicatedAccount == null)
					return null;

				// Create Balance
				ServiceBalance balance = new ServiceBalance();
				balance.setName(quota.getService().getSafe(languageCode, ""));
				balance.setValue(dedicatedAccount.dedicatedAccountValue1 == null ? 0L : dedicatedAccount.dedicatedAccountValue1);
				balance.setExpiryDate(expiryDate);
				balance.setUnit(esb.getLocale().getCurrencyCode());
				return balance;
			}
		}

		return null;
	}

	private ServiceBalance[] getConsumerBalances(Subscriber provider, Subscriber consumer, String languageCode, Transaction<?> transaction, Date expiryDate) throws AirException
	{
		ServiceBalance[] result = new ServiceBalance[config.getQuotas().length + 1];
		int index = 0;

		// Main
		{
			ServiceBalance main = new ServiceBalance();
			main.setValue(consumer.getAccountValue1());
			main.setName(config.mainAccountBalanceName.getSafe(languageCode, "main"));
			main.setUnit(esb.getLocale().getCurrencyCode());
			main.setExpiryDate(consumer.getSupervisionExpiryDate());
			result[index++] = main;
		}

		// Get Usage Counters
		transaction.track(this, "GetUsageCountersAndThresholds");
		consumer.getUsageThresholdsAndCounters();

		for (Quota quota : config.getQuotas())
		{
			// Skip if consumer does not have this quota
			OfferInformation offer = consumer.getProviderOffer(quota.getBeneficiaryOfferID());
			if (offer == null)
				continue;

			// Skip if provider doesn't have DA
			DedicatedAccountInformation da = provider.getDedicatedAccount(getDedicatedAccountID(quota));
			if (da == null || da.dedicatedAccountValue1 == null)
				continue;

			// Get the UT
			UsageThresholdInformation ut = consumer.getUsageThreshold(quota.getBeneficiaryTotalThresholdID());

			// Get the UC
			UsageCounterUsageThresholdInformation uc = consumer.getUsageCounter(quota.getBeneficiaryUsageCounterID());

			// Skip if either not set
			if (uc == null || uc.usageCounterValue == null //
					|| ut == null || ut.usageThresholdValue == null)
				continue;

			// Create Balance
			ServiceBalance balance = new ServiceBalance();
			balance.setName(quota.getName().getSafe(languageCode, ""));

			long providerLeft = da.dedicatedAccountValue1 * 100 / quota.getPriceCents();
			long consumerLeft = (ut.usageThresholdValue - uc.usageCounterValue) / quota.getUnitConversionFactor();
			balance.setValue(providerLeft < consumerLeft ? providerLeft : consumerLeft);
			balance.setExpiryDate(expiryDate);
			balance.setUnit(quota.getUnitName().getSafe(languageCode, ""));
			result[index++] = balance;

		}

		return Arrays.copyOf(result, index);
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

	private void setProviderProperties(Properties properties, Subscriber provider, Variant variant, CdrBase cdr)
	{
		// Cdr
		if (cdr != null)
			cdr.setA_MSISDN(provider.getInternationalNumber());

		// Properties
		properties.setVariant(variant.getNames());
		properties.setProviderMSISDN(provider.getNationalNumber());
	}

	private void setConsumerProperties(Properties properties, Subscriber consumer, ICdr cdr)
	{
		// Cdr
		cdr.setB_MSISDN(consumer.getInternationalNumber());

		// Properties
		properties.setConsumerMSISDN(consumer.getNationalNumber());
	}

	private void setQuotaProperties(Properties properties, Quota quota)
	{
		properties.setQuotaName(quota.getName());
		properties.setDestination(quota.getDestination());
		properties.setService(quota.getService());
		properties.setUnits(quota.getUnitName());
		properties.setDaysOfWeek(quota.getDaysOfWeek());
		properties.setTimeOfDay(quota.getTimeOfDay());
	}

	private ServiceClass getServiceClass(Subscriber provider) throws AirException
	{
		for (ServiceClass serviceClass : config.getServiceClasses())
		{
			if (provider.getServiceClassCurrent() == serviceClass.getServiceClassID() && serviceClass.isEligibleForProvider())
				return serviceClass;
		}
		return null;
	}

	private boolean isProvider(IDatabaseConnection db, Subscriber provider) throws AirException, SQLException
	{
		for (Variant variant : config.getVariants())
		{
			if (isProvider(db, provider, variant))
				return true;
		}
		return false;
	}

	private boolean isProvider(IDatabaseConnection db, Subscriber provider, Variant variant) throws AirException, SQLException
	{
		// Get Subscription Offer
		OfferInformation offer = provider.getSharedOffer(variant.getSubscriptionOfferID());
		boolean hasOffer = offer != null;

		// Is this a Prosumer ?
		if (isProsumer(provider, variant))
			return true;

		// Get Subscriptions
		ISubscription[] subscriptions = lifecycle.getSubscriptions(db, provider, getServiceID());
		int count = subscriptions.length;

		// Multiples not allowed
		if (count > 1)
		{
			logger.error("Deleting multiple {} subscriptions for {}", getServiceName(null), provider.getInternationalNumber());
			lifecycle.removeSubscriptions(db, provider, getServiceID());
			subscriptions = new ISubscription[0];
		}

		// Doesn't have offer and there are no subscriptions
		if (!hasOffer && count == 0)
			return false;

		// Has offer, and there is exactly one subscription
		if (hasOffer && count == 1)
			return subscriptions[0].getVariantID().equalsIgnoreCase(variant.getVariantID());

		// Doesn't have the offer, although there is one subscription record!
		if (!hasOffer && count == 1)
		{
			if (subscriptions[0].getVariantID().equalsIgnoreCase(variant.getVariantID()))
			{
				logger.error("Deleting bogus {}/{} subscription for {}", getServiceName(null), variant.getVariantID(), provider.getInternationalNumber());

				// Create a CDR
				CdrBase cdr = new CsvCdr();
				cdr.setTransactionID(esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));
				cdr.setHostName(HostInfo.getNameOrElseHxC());
				cdr.setCallerID(provider.getNationalNumber());
				cdr.setChannel(Channels.INTERNAL);
				cdr.setRequestMode(RequestModes.force);

				// Database Connection Scope
				try (IDatabaseConnection db2 = database.getConnection(null))
				{

					// Transaction Reversal Scope
					try (Transaction<?> transaction = transactions.create(cdr, db2))
					{
						Properties properties = new Properties();
						forceUnsubscribe(cdr, db2, transaction, variant, provider, properties);
					}
					catch (IOException e)
					{
						logger.error(e.getMessage(), e);
						return false;
					}
				}
				catch (Exception e)
				{
					logger.error(e.getMessage(), e);
					return false;
				}

			}
			return false;
		}

		// Has the offer, but no subscription record!
		if (hasOffer && count == 0)
		{
			DateTime today = DateTime.getToday();

			// Add a lifecycle record
			for (Variant var : config.getVariants())
			{
				DateTime expiryDate = today.addDays(var.getSafeValidityPeriodDays() + 1);
				if (offer.expiryDate.before(expiryDate))
				{
					addProviderLifecycle(db, provider, var, new DateTime(offer.expiryDate));
					logger.error("Added missing {}/{} subscription for {}", getServiceName(null), var.getVariantID(), provider.getInternationalNumber());
					return variant.getVariantID().equalsIgnoreCase(var.getVariantID());
				}
			}
			logger.error("Unable to add missing {} subscription for {}", getServiceName(null), provider.getInternationalNumber());
		}

		return hasOffer;
	}

	private boolean isConsumer(IDatabaseConnection db, Subscriber consumer) throws SQLException, AirException
	{
		for (Variant variant : config.getVariants())
		{
			OfferInformation offer = consumer.getProviderOffer(variant.getConsumerOfferID());
			if (offer == null || offer.offerProviderID == null)
				continue;
			Subscriber provider = new Subscriber(offer.offerProviderID, air, null);
			if (isConsumer(db, provider, consumer, variant))
				return true;
		}

		String[] owners = lifecycle.getOwners(db, consumer, getServiceID());
		for (String owner : owners)
		{
			Subscriber provider = new Subscriber(owner, air, null);
			for (Variant variant : config.getVariants())
			{
				if (lifecycle.removeMember(db, provider, getServiceID(), variant.getVariantID(), consumer))
					logger.error("Deleting bogus {}/{} membership of {} for {} ", getServiceName(null), variant.getVariantID(), consumer.getInternationalNumber(),
							provider.getInternationalNumber());
			}
		}

		return false;
	}

	private boolean isConsumer(IDatabaseConnection db, Subscriber provider, Subscriber consumer, Variant variant) throws SQLException, AirException
	{
		// Test if is Member of this Variant
		boolean isMember = lifecycle.isMember(db, provider, getServiceID(), variant.getVariantID(), consumer);

		// Return false if the subscriber is not subscribed to this variant
		if (!isProvider(db, provider, variant))
		{
			// Remove Bogus Membership
			if (isMember)
			{
				logger.error("Deleting bogus{}/{} membership of {} for {} ", getServiceName(null), variant.getVariantID(), consumer.getInternationalNumber(), provider.getInternationalNumber());
				lifecycle.removeMember(db, provider, getServiceID(), variant.getVariantID(), consumer);
			}
			return false;
		}

		// Get the Shared Offer
		OfferInformation offer = consumer.getProviderOffer(variant.getConsumerOfferID());
		boolean isConsumerOfProvider = offer != null && provider.getNaiNumber().equals(offer.offerProviderID);

		// Handle Inconsistencies
		if (isConsumerOfProvider)
		{
			if (isMember)
				return true;
			else
			{
				logger.error("Adding missing {}/{} membership of {} for {} ", getServiceName(null), variant.getVariantID(), consumer.getInternationalNumber(), provider.getInternationalNumber());
				lifecycle.addMember(db, provider, getServiceID(), variant.getVariantID(), consumer);
				return true;
			}
		}
		else
		{
			if (!isMember)
				return false;
			else
			{
				logger.error("Deleting bogus {}/{} membership of {} for {} ", getServiceName(null), variant.getVariantID(), consumer.getInternationalNumber(), provider.getInternationalNumber());
				lifecycle.removeMember(db, provider, getServiceID(), variant.getVariantID(), consumer);
				return false;
			}

		}

	}

	private boolean isProsumer(Subscriber prosumer, Variant variant) throws AirException, SQLException
	{
		// Get Subscription Offer
		OfferInformation offer1 = prosumer.getSharedOffer(variant.getSubscriptionOfferID());

		// Get Provider Offer
		OfferInformation offer2 = prosumer.getProviderOffer(variant.getConsumerOfferID());

		return offer1 != null && offer2 != null;
	}

	private boolean chargeProvider(Subscriber provider, long charge, ServiceClass providerServiceClass, //
			Properties properties, ICdr cdr, ResponseHeader response) throws AirException
	{
		if (charge == 0)
		{
			logger.debug("Charge is {},  omitting UpdateBalanceAndDate", charge);
			return true;
		}

		String chargeText = locale.formatCurrency(charge);
		logger.debug("Attempt to charge {} {}", provider.getInternationalNumber(), chargeText);

		try
		{
			AccountUpdate updateRevenueDA = new AccountUpdate(config.revenueDedicatedAccountID, Subscriber.DATYPE_MONEY, charge, null, null, null, null, null);

			if (providerServiceClass.isPostPaid() && config.postPaidAccountID > 0)
			{
				AccountUpdate postPaidDA = new AccountUpdate(config.postPaidAccountID, Subscriber.DATYPE_MONEY, -charge, null, null, null, null, null);
				provider.updateAccounts(null, postPaidDA, updateRevenueDA);
			}
			else
			{
				provider.updateAccounts(-charge, updateRevenueDA);
			}

			properties.setCharge(chargeText);
			cdr.setChargeLevied((int) charge);
			response.setChargeLevied((int) charge);

		}
		catch (AirException e)
		{
			if (e.getReturnCode() == ReturnCodes.insufficientBalance || e.getResponseCode() == 106)
				return false;
			else
				throw e;
		}

		return true;
	}

	private int getDedicatedAccountID(Quota quota)
	{
		switch (quota.getServiceType())
		{
			case VOICE:
				return config.getVoiceDedicatedAccountID();

			case DATA:
				return config.getDataDedicatedAccountID();

			case SMS:
				return config.getSmsDedicatedAccountID();

			case MMS:
				return config.getMmsDedicatedAccountID();

			case AIRTIME:
				return config.getAirtimeDedicatedAccountID();

			default:
				return -1;
		}
	}

	private static final int NEXT_SEND_FIRST_RENEW_WARNING = 1;
	private static final int NEXT_SEND_SECOND_RENEW_WARNING = 2;
	private static final int NEXT_RENEW_SUBSCRIPTION = 3;
	private static final int NEXT_SEND_THRESHOLD_NOTIFICATION = 4;

	private void addProviderLifecycle(IDatabaseConnection database, Subscriber provider, Variant variant, DateTime expiryDate) throws SQLException
	{
		DateTime renewalTime = expiryDate.addDays(1);
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

		lifecycle.addSubscription(database, provider, getServiceID(), variant.getVariantID(), //
				state, nextDateTime, new Date[] { firstRenewWarningTime, secondRenewWarningTime, renewalTime });

	}

	private void setExpiryDateProperty(Subscriber provider, Properties properties, Date expiryDate)
	{
		properties.setExpiryDate(locale.formatDate(expiryDate, provider.getLanguageID()));
	}

	private void sendSubscriberSMS(Subscriber subscriber, int notifcationID, Properties properties)
	{
		String languageCode = esb.getLocale().getLanguage(subscriber.getLanguageID());
		INotificationText text = notifications.get(notifcationID, languageCode, locale, properties);
		if (text != null && text.getText() != null && text.getText().length() > 0)
			smsConnector.send(config.smsSourceAddress, subscriber.getInternationalNumber(), text);
	}

	private boolean isApplicable(String restriction, Phrase phrase)
	{
		if (restriction == null || restriction == "")
			return true;

		return phrase.matches(restriction);
	}

	private static AtomicInteger sequenceNo = new AtomicInteger(0);

	private void logActivity(IDatabaseConnection db, ICdr cdr, Quota quota, Long quantity)
	{
		Activity activity = new Activity();
		activity.setStartTime(cdr.getStartTime());
		activity.setSequenceNo(SharedAccounts.sequenceNo.incrementAndGet());
		activity.setA_MSISDN(cdr.getA_MSISDN());
		activity.setB_MSISDN(cdr.getB_MSISDN());
		activity.setServiceID(cdr.getServiceID());
		activity.setVariantID(cdr.getVariantID());
		activity.setProcessID(cdr.getProcessID());
		activity.setQuotaName(quota.getQuotaID());
		activity.setServiceType(quota.getServiceType().toString());
		activity.setQuantity(quantity);
		activity.setAmount((quota.getPriceCents() * quantity + 50) / 100);

		try
		{
			db.insert(activity);
		}
		catch (SQLException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

}
