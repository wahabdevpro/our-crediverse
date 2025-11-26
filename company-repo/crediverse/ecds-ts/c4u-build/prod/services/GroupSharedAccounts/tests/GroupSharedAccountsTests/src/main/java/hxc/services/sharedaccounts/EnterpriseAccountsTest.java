package hxc.services.sharedaccounts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
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
import com.concurrent.hxc.GetQuotasRequest;
import com.concurrent.hxc.GetQuotasResponse;
import com.concurrent.hxc.GetServiceRequest;
import com.concurrent.hxc.GetServiceResponse;
import com.concurrent.hxc.GetServicesRequest;
import com.concurrent.hxc.GetServicesResponse;
import com.concurrent.hxc.IHxC;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.RequestHeader;
import com.concurrent.hxc.ResponseHeader;
import com.concurrent.hxc.ServiceBalance;
import com.concurrent.hxc.ServiceQuota;
import com.concurrent.hxc.SubscribeRequest;
import com.concurrent.hxc.SubscribeResponse;
import com.concurrent.hxc.SubscriptionState;
import com.concurrent.hxc.VasServiceInfo;

import hxc.configuration.ValidationException;
import hxc.connectors.Channels;
import hxc.connectors.air.AirConnector;
import hxc.connectors.air.AirConnector.AirConnectionConfig;
import hxc.connectors.air.AirConnector.AirConnectorConfig;
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.lifecycle.ILifecycle;
import hxc.connectors.lifecycle.LifecycleConnector;
import hxc.connectors.smpp.SmppConnector;
import hxc.connectors.soap.ISoapConnector;
import hxc.connectors.soap.SoapConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.servicebus.ServiceBus;
import hxc.services.airsim.AirSim;
import hxc.services.airsim.model.OfferEx;
import hxc.services.airsim.model.SubscriberEx;
import hxc.services.airsim.protocol.DedicatedAccount;
import hxc.services.airsim.protocol.IAirSim;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.services.airsim.protocol.UsageCounter;
import hxc.services.airsim.protocol.UsageThreshold;
import hxc.services.numberplan.INumberPlan;
import hxc.services.numberplan.NumberPlanService;
import hxc.services.reporting.ReportingService;
import hxc.services.security.SecurityService;
import hxc.services.sharedaccounts.SharedAccountsBase.SharedAccountsConfig;
import hxc.services.transactions.TransactionService;
import hxc.testsuite.RunAllTestsBase;

public class EnterpriseAccountsTest extends RunAllTestsBase
{
	final static Logger logger = LoggerFactory.getLogger(EnterpriseAccountsTest.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private String P_NUMBER = "0824452655"; // Provider
	private String I_NUMBER = "0824452656"; // Prosumer
	private String C_NUMBER = "0824452657"; // Consumer
	private String SERVICE_ID = "GSA";
	private String SERVICE_NAME_FR = "Group Shared Accounts";
	private String MONTHLY = "Monthly";
	private String WEEKLY = "Weekly";
	private String DAILY = "Daily";
	private String QUOTA_ID = "Airtime4U";
	private String SERVICE_TYPE = "Airtime";
	private String DESTINATION = "Any";
	private String DAYS_OF_WEEK = "Any Day";
	private String TIME_OF_DAY = "Any Time";
	private static IAirSim airSimulator = null;
	private int languageID = 1;
	private Variant monthly;
	private Quota calls;
	private IHxC vasConnector;

	protected static final long UNLIMITED = 0x7FFFFFFFFFFFFFFFL;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Setup
	//
	// /////////////////////////////////
	@BeforeClass
	public static void setup() throws ValidationException
	{
		// Create Transaction Service
		esb = ServiceBus.getInstance();
		esb.stop();
		configureLogging(esb);
		
		esb.registerService(new TransactionService());
		esb.registerService(new SharedAccounts());
		esb.registerService(new NumberPlanService());
		AirConnector air = new AirConnector();

		AirConnectorConfig airConfig = (AirConnectorConfig) air.getConfiguration();
		AirConnectionConfig conConfig = (AirConnectionConfig) airConfig.getConfigurations().iterator().next();
		conConfig.setUri("http://127.0.0.1:10011/Air");

		esb.registerConnector(air);

		esb.registerConnector(new MySqlConnector()); // ??
		esb.registerConnector(new SmppConnector());
		esb.registerService(new SecurityService());
		esb.registerConnector(new SoapConnector());

		esb.registerConnector(new CtrlConnector()); // ??
		esb.registerConnector(new LifecycleConnector());
		esb.registerService(new ReportingService());

		boolean started = esb.start(null);
		assertTrue(started);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Test Enterprise Accounts
	//
	// /////////////////////////////////
	@Test
	public void testEnterpriseAccounts() throws ValidationException, IOException, SQLException, InterruptedException
	{
		long providerBalance = 9400L;
		long prosumerBalance = 200L;
		long consumerBalance = 200L;

		Long providerDA = null;
		Long prosumerDA = null;

		final int CHARGE_ADD = 0;

		// Get the VAS Soap Interface
		ISoapConnector soapConnector = esb.getFirstConnector(ISoapConnector.class);
		assertNotNull("SOAP Connector is NULL!! Connector not starting !", soapConnector);
		vasConnector = soapConnector.getVasInterface();

		// Get Lifecycle
		Party pParty = new Party(P_NUMBER, languageID);
		Party iParty = new Party(I_NUMBER, languageID);
		Party cParty = new Party(C_NUMBER, languageID);
		IDatabase database = esb.getFirstConnector(IDatabase.class);
		IDatabaseConnection db = database.getConnection(null);
		ILifecycle lifecycle = esb.getFirstConnector(ILifecycle.class);
		lifecycle.removeSubscriptions(db, pParty, SERVICE_ID);
		lifecycle.removeMembers(db, pParty, SERVICE_ID, DAILY);
		lifecycle.removeMembers(db, pParty, SERVICE_ID, WEEKLY);
		lifecycle.removeMembers(db, pParty, SERVICE_ID, MONTHLY);

		// Setup Simulator
		INumberPlan numberPlan = esb.getFirstService(INumberPlan.class);
		airSimulator = new AirSim(esb, 10011, "/Air", numberPlan, "CFR");
		assertTrue(airSimulator.start());
		SubscriberEx provider = (SubscriberEx) airSimulator.addSubscriber(P_NUMBER, languageID, 101, providerBalance, SubscriberState.active);
		SubscriberEx prosumer = (SubscriberEx) airSimulator.addSubscriber(I_NUMBER, languageID, 101, prosumerBalance, SubscriberState.active);
		SubscriberEx consumer = (SubscriberEx) airSimulator.addSubscriber(C_NUMBER, languageID, 101, consumerBalance, SubscriberState.active);

		// Setup Config
		SharedAccounts service = esb.getFirstService(SharedAccounts.class);
		SharedAccountsConfig config = (SharedAccountsConfig) service.getConfiguration();
		Variant[] variants = config.getVariants();
		monthly = variants[2];
		assertEquals(MONTHLY, monthly.getVariantID());

		// Get the Quota to test with
		calls = config.getQuotas()[2];
		assertEquals(QUOTA_ID, calls.getQuotaID());

		// Create Usage Counters
		for (Quota quota : config.getQuotas())
		{
			if (quota.getQuotaID().equalsIgnoreCase(QUOTA_ID))
			{
				{
					int beneficiaryTotalThresholdID = quota.getBeneficiaryTotalThresholdID();
					UsageThreshold usageThreshold = new UsageThreshold();
					usageThreshold.setUsageThresholdID(beneficiaryTotalThresholdID);
					airSimulator.updateUsageThreshold(I_NUMBER, usageThreshold);
					airSimulator.updateUsageThreshold(C_NUMBER, usageThreshold);
				}

				{
					int beneficiaryWarningUsageThresholdID = quota.getBeneficiaryWarningUsageThresholdID();
					UsageThreshold usageThreshold = new UsageThreshold();
					usageThreshold.setUsageThresholdID(beneficiaryWarningUsageThresholdID);
					airSimulator.updateUsageThreshold(I_NUMBER, usageThreshold);
					airSimulator.updateUsageThreshold(C_NUMBER, usageThreshold);
				}

				{
					int beneficiaryUsageCounterID = quota.getBeneficiaryUsageCounterID();
					UsageCounter usageCounter = new UsageCounter();
					usageCounter.setUsageCounterID(beneficiaryUsageCounterID);
					airSimulator.updateUsageCounter(P_NUMBER, usageCounter);
					airSimulator.updateUsageCounter(I_NUMBER, usageCounter);
					airSimulator.updateUsageCounter(C_NUMBER, usageCounter);
				}

				break;
			}
		}

		// Check Conditions - ma, isP, isB, airtimeDA, pFlag, pCounter, bFlag, bCounter, bLimit, bWarning
		check(provider, providerBalance, false, false, providerDA, false, null, false, null, null, null);
		check(prosumer, prosumerBalance, false, false, prosumerDA, false, null, false, null, null, null);
		check(consumer, consumerBalance, false, false, null, false, null, false, null, null, null);

		// Get All Services
		{
			GetServicesRequest request = new GetServicesRequest();
			initialize(request);
			request.setActiveOnly(false);
			GetServicesResponse response = vasConnector.getServices(request);
			validate(request, response);
			VasServiceInfo[] info = response.getServiceInfo();
			assertEquals(3, info.length);
			assertEquals(SERVICE_ID, info[0].getServiceID());
			assertEquals(DAILY, info[0].getVariantID());
			assertEquals(SERVICE_NAME_FR, info[0].getServiceName());
			assertEquals(SubscriptionState.unknown, info[0].getState());
			assertEquals(SERVICE_ID, info[1].getServiceID());
			assertEquals(WEEKLY, info[1].getVariantID());
			assertEquals(SERVICE_NAME_FR, info[1].getServiceName());
			assertEquals(SubscriptionState.unknown, info[1].getState());
			assertEquals(SERVICE_ID, info[2].getServiceID());
			assertEquals(MONTHLY, info[2].getVariantID());
			assertEquals(SERVICE_NAME_FR, info[2].getServiceName());
			assertEquals(SubscriptionState.unknown, info[2].getState());
		}

		// Get Active Services
		{
			GetServicesRequest request = new GetServicesRequest();
			initialize(request);
			request.setActiveOnly(true);
			request.setSubscriberNumber(new Number(P_NUMBER));
			GetServicesResponse response = vasConnector.getServices(request);
			validate(request, response);
			VasServiceInfo[] info = response.getServiceInfo();
			assertEquals(0, info.length);
		}

		// Get Service
		{
			GetServiceRequest request = new GetServiceRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setSubscriberNumber(new Number(P_NUMBER));
			request.setActiveOnly(true);
			GetServiceResponse response = vasConnector.getService(request);
			validate(request, response);
			assertEquals(0, response.getServiceInfo().length);

		}

		// Check Conditions - ma, isP, isB, airtimeDA, pFlag, pCounter, bFlag, bCounter, bLimit, bWarning
		check(provider, providerBalance, false, false, providerDA, false, null, false, null, null, null);
		check(prosumer, prosumerBalance, false, false, prosumerDA, false, null, false, null, null, null);
		check(consumer, consumerBalance, false, false, null, false, null, false, null, null, null);

		// Subscribe
		assertEquals(providerBalance, (long) provider.getAccountValue1());
		{
			SubscribeRequest subscribeRequest = new SubscribeRequest();
			initialize(subscribeRequest);
			subscribeRequest.setServiceID(SERVICE_ID);
			subscribeRequest.setVariantID(MONTHLY);
			subscribeRequest.setSubscriberNumber(new Number(P_NUMBER));
			SubscribeResponse subscribeResponse = vasConnector.subscribe(subscribeRequest);
			validate(subscribeRequest, subscribeResponse);
		}
		assertTrue(lifecycle.isSubscribed(db, pParty, SERVICE_ID));
		providerBalance -= monthly.getSubscriptionCharge();

		// Check Conditions - ma, isP, isB, airtimeDA, pFlag, pCounter, bFlag, bCounter, bLimit, bWarning
		check(provider, providerBalance, true, false, providerDA, false, null, false, null, null, null);
		check(prosumer, prosumerBalance, false, false, prosumerDA, false, null, false, null, null, null);
		check(consumer, consumerBalance, false, false, null, false, null, false, null, null, null);

		// Check Balances
		check(provider, 0L, null);
		check(prosumer, null, null);
		check(consumer, null, null);

		// Attempt to Subscribe Twice
		{
			SubscribeRequest subscribeRequest = new SubscribeRequest();
			initialize(subscribeRequest);
			subscribeRequest.setServiceID(SERVICE_ID);
			subscribeRequest.setVariantID(MONTHLY);
			subscribeRequest.setSubscriberNumber(new Number(P_NUMBER));
			SubscribeResponse subscribeResponse = vasConnector.subscribe(subscribeRequest);
			assertEquals(ReturnCodes.alreadySubscribed, subscribeResponse.getReturnCode());
		}

		// Get Active Services
		{
			GetServicesRequest request = new GetServicesRequest();
			initialize(request);
			request.setActiveOnly(true);
			request.setSubscriberNumber(new Number(P_NUMBER));
			GetServicesResponse response = vasConnector.getServices(request);
			validate(request, response);
			VasServiceInfo[] info = response.getServiceInfo();
			assertEquals(1, info.length);
			assertEquals(SERVICE_ID, info[0].getServiceID());
			assertEquals(MONTHLY, info[0].getVariantID());
			assertEquals(SERVICE_NAME_FR, info[0].getServiceName());
			assertEquals(SubscriptionState.active, info[0].getState());
		}
		assertEquals(providerBalance, (long) provider.getAccountValue1());

		// Get Service
		{
			GetServiceRequest request = new GetServiceRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setSubscriberNumber(new Number(P_NUMBER));
			request.setActiveOnly(true);
			GetServiceResponse response = vasConnector.getService(request);
			validate(request, response);
			assertEquals(MONTHLY, response.getServiceInfo()[0].getVariantID());
			assertEquals(SubscriptionState.active, response.getServiceInfo()[0].getState());
		}
		assertEquals(providerBalance, (long) provider.getAccountValue1());

		// Get Service
		{
			GetServiceRequest request = new GetServiceRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(P_NUMBER));
			request.setActiveOnly(true);
			GetServiceResponse response = vasConnector.getService(request);
			validate(request, response);
			assertEquals(SubscriptionState.active, response.getServiceInfo()[0].getState());
		}
		assertEquals(providerBalance, (long) provider.getAccountValue1());

		// Add Prosumer
		{
			AddMemberRequest request = new AddMemberRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(P_NUMBER));
			request.setMemberNumber(new Number(I_NUMBER));
			request.setMemberType("Pros");
			AddMemberResponse response = vasConnector.addMember(request);
			validate(request, response);
		}
		providerBalance -= CHARGE_ADD;
		assertTrue(lifecycle.isMember(db, pParty, SERVICE_ID, iParty));
		assertFalse(lifecycle.isMember(db, pParty, SERVICE_ID, cParty));

		// Check Conditions - ma, isP, isB, airtimeDA, pFlag, pCounter, bFlag, bCounter, bLimit, bWarning
		check(provider, providerBalance, true, false, providerDA, false, null, false, null, null, null);
		check(prosumer, prosumerBalance, true, true, prosumerDA, false, null, false, null, null, null);
		check(consumer, consumerBalance, false, false, null, false, null, false, null, null, null);

		// Add Consumer
		{
			AddMemberRequest request = new AddMemberRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(I_NUMBER));
			request.setMemberNumber(new Number(C_NUMBER));
			AddMemberResponse response = vasConnector.addMember(request);
			validate(request, response);
		}
		providerBalance -= CHARGE_ADD;
		assertTrue(lifecycle.isMember(db, pParty, SERVICE_ID, iParty));
		assertTrue(lifecycle.isMember(db, iParty, SERVICE_ID, cParty));

		// Check Conditions - ma, isP, isB, airtimeDA, pFlag, pCounter, bFlag, bCounter, bLimit, bWarning
		check(provider, providerBalance, true, false, providerDA, false, null, false, null, null, null);
		check(prosumer, prosumerBalance, true, true, prosumerDA, false, null, false, null, null, null);
		check(consumer, consumerBalance, false, true, null, false, null, false, null, null, null);

		// Attempt to Add Consumer again
		{
			AddMemberRequest request = new AddMemberRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(I_NUMBER));
			request.setMemberNumber(new Number(C_NUMBER));
			AddMemberResponse response = vasConnector.addMember(request);
			assertEquals(ReturnCodes.alreadyOtherMember, response.getReturnCode());
		}
		assertEquals(providerBalance, (long) provider.getAccountValue1());
		assertTrue(lifecycle.isMember(db, pParty, SERVICE_ID, iParty));
		assertTrue(lifecycle.isMember(db, iParty, SERVICE_ID, cParty));

		// Get All possible Quotas for Prosumer
		{
			GetQuotasRequest request = new GetQuotasRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(P_NUMBER));
			request.setMemberNumber(new Number(I_NUMBER));
			request.setActiveOnly(false);
			GetQuotasResponse response = vasConnector.getQuotas(request);
			validate(request, response);
			assertEquals(3, response.getServiceQuotas().length);
		}
		assertEquals(providerBalance, (long) provider.getAccountValue1());

		// Get All Active Quotas for Prosumer
		{
			GetQuotasRequest request = new GetQuotasRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(P_NUMBER));
			request.setMemberNumber(new Number(I_NUMBER));
			request.setActiveOnly(true);
			GetQuotasResponse response = vasConnector.getQuotas(request);
			validate(request, response);
			assertEquals(0, response.getServiceQuotas().length);
		}
		assertEquals(providerBalance, (long) provider.getAccountValue1());

		// Add Quota for Prosumer
		long initialQuantity = 100L;
		{
			AddQuotaRequest request = new AddQuotaRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(P_NUMBER));
			request.setMemberNumber(new Number(I_NUMBER));

			ServiceQuota quota = new ServiceQuota();
			quota.setService(SERVICE_TYPE);
			quota.setDestination(DESTINATION);
			quota.setDaysOfWeek(DAYS_OF_WEEK);
			quota.setTimeOfDay(TIME_OF_DAY);

			quota.setQuantity(initialQuantity);
			request.setQuota(quota);

			AddQuotaResponse response = vasConnector.addQuota(request);
			validate(request, response);
			assertNotNull(response.getQuota());
			assertEquals(QUOTA_ID, response.getQuota().getQuotaID());
		}
		long price = (calls.getPriceCents() * initialQuantity + 50) / 100;
		long units = initialQuantity * calls.getUnitConversionFactor();
		long warn = units - calls.getWarningMargin();
		providerBalance -= price;
		prosumerDA = price;

		// Check Conditions - ma, isP, isB, airtimeDA, pFlag, pCounter, bFlag, bCounter, bLimit, bWarning
		check(provider, providerBalance, true, false, providerDA, true, null, false, null, null, null);
		check(prosumer, prosumerBalance, true, true, prosumerDA, false, null, true, 0L, units, warn);
		check(consumer, consumerBalance, false, true, null, false, null, false, null, null, null);

		// Check Balances
		check(provider, null, null);
		check(prosumer, price, "USD");
		check(consumer, null, null);

		// Try to Add Quota for Prosumer again
		{
			AddQuotaRequest request = new AddQuotaRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(P_NUMBER));
			request.setMemberNumber(new Number(I_NUMBER));

			ServiceQuota quota = new ServiceQuota();
			quota.setService(SERVICE_TYPE);
			quota.setDestination(DESTINATION);
			quota.setDaysOfWeek(DAYS_OF_WEEK);
			quota.setTimeOfDay(TIME_OF_DAY);

			quota.setQuantity(100L);
			request.setQuota(quota);

			AddQuotaResponse response = vasConnector.addQuota(request);
			assertEquals(ReturnCodes.alreadyAdded, response.getReturnCode());

		}

		// Add Unlimited Quota for Consumer
		{
			AddQuotaRequest request = new AddQuotaRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(I_NUMBER));
			request.setMemberNumber(new Number(C_NUMBER));

			ServiceQuota quota = new ServiceQuota();
			quota.setService(SERVICE_TYPE);
			quota.setDestination(DESTINATION);
			quota.setDaysOfWeek(DAYS_OF_WEEK);
			quota.setTimeOfDay(TIME_OF_DAY);

			quota.setQuantity(-1L);
			request.setQuota(quota);

			AddQuotaResponse response = vasConnector.addQuota(request);
			validate(request, response);
			assertNotNull(response.getQuota());
			assertEquals(QUOTA_ID, response.getQuota().getQuotaID());

		}
		assertEquals(providerBalance, (long) provider.getAccountValue1());

		// Check Conditions - ma, isP, isB, airtimeDA, pFlag, pCounter, bFlag, bCounter, bLimit, bWarning
		check(provider, providerBalance, true, false, providerDA, true, null, false, null, null, null);
		check(prosumer, prosumerBalance, true, true, prosumerDA, true, null, true, 0L, units, warn);
		check(consumer, consumerBalance, false, true, null, false, null, true, 0L, UNLIMITED, UNLIMITED - calls.getWarningMargin());

		// Check Balances
		check(provider, null, null);
		check(prosumer, price, "USD");
		check(consumer, initialQuantity, "USD");

		// Update Quota for Prosumer
		long newQuantity = 150L;
		{
			ChangeQuotaRequest request = new ChangeQuotaRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(P_NUMBER));
			request.setMemberNumber(new Number(I_NUMBER));

			ServiceQuota oldQuota = new ServiceQuota();
			oldQuota.setService(SERVICE_TYPE);
			oldQuota.setDestination(DESTINATION);
			oldQuota.setDaysOfWeek(DAYS_OF_WEEK);
			oldQuota.setTimeOfDay(TIME_OF_DAY);
			oldQuota.setQuantity(initialQuantity);
			request.setOldQuota(oldQuota);

			ServiceQuota newQuota = new ServiceQuota();
			newQuota.setService(SERVICE_TYPE);
			newQuota.setDestination(DESTINATION);
			newQuota.setDaysOfWeek(DAYS_OF_WEEK);
			newQuota.setTimeOfDay(TIME_OF_DAY);
			newQuota.setQuantity(newQuantity);
			request.setNewQuota(newQuota);

			ChangeQuotaResponse response = vasConnector.changeQuota(request);
			validate(request, response);
		}

		providerBalance += price;
		price = (calls.getPriceCents() * newQuantity + 50) / 100;
		units = newQuantity * calls.getUnitConversionFactor();
		warn = units - calls.getWarningMargin();
		providerBalance -= price;
		prosumerDA = price;

		// Check Conditions - ma, isP, isB, airtimeDA, pFlag, pCounter, bFlag, bCounter, bLimit, bWarning
		check(provider, providerBalance, true, false, providerDA, true, null, false, null, null, null);
		check(prosumer, prosumerBalance, true, true, prosumerDA, true, null, true, 0L, units, warn);
		check(consumer, consumerBalance, false, true, null, false, null, true, 0L, UNLIMITED, UNLIMITED - calls.getWarningMargin());

		// Check Balances
		check(provider, null, null);
		check(prosumer, price, "USD");
		check(consumer, newQuantity, "USD");

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	private void initialize(RequestHeader request)
	{
		request.setCallerID(P_NUMBER);
		request.setChannel(Channels.INTERNAL);
		request.setHostName("local");
		request.setTransactionID("00012");
		request.setSessionID("001");
		request.setVersion("1");
		request.setMode(RequestModes.normal);
		request.setLanguageID(languageID);
	}

	private void validate(RequestHeader request, ResponseHeader response)
	{
		assertEquals(ReturnCodes.success, response.getReturnCode());
		assertEquals(request.getTransactionID(), response.getTransactionId());
		assertEquals(request.getSessionID(), response.getSessionId());
	}

	private void check(SubscriberEx subscriber, long ma, boolean isP, boolean isC, Long airtimeDA, boolean pFlag, Long pCounter, boolean bFlag, Long bCounter, Long bLimit, Long bWarning)
	{
		// Main Account
		assertEquals(ma, (long) subscriber.getAccountValue1());

		// airtimeDA
		DedicatedAccount da = subscriber.getDedicatedAccounts().get(5000);
		Long actual = da == null ? null : da.getDedicatedAccountValue1();
		assertTrue(actual == null && airtimeDA == null || actual != null && airtimeDA != null && (long) actual == (long) airtimeDA);

		// isP
		OfferEx offer = subscriber.getOffers().get(monthly.getSubscriptionOfferID());
		assertTrue(offer == null && !isP || offer != null && isP);

		// isC
		offer = subscriber.getOffers().get(monthly.getConsumerOfferID());
		assertTrue(offer == null && !isC || offer != null && isC);

		// boolean pFlag
		offer = subscriber.getOffers().get(calls.getSponsorOfferID());
		assertTrue(offer == null && !pFlag || offer != null && pFlag);

		// Long pCounter
		UsageCounter counter = subscriber.getUsageCounters().get(calls.getSponsorUsageCounterID());
		actual = counter == null ? null : counter.getUsageCounterValue();
		assertTrue(actual == null && pCounter == null || actual != null && pCounter != null && (long) actual == (long) pCounter);

		// boolean bFlag
		offer = subscriber.getOffers().get(calls.getBeneficiaryOfferID());
		assertTrue(offer == null && !bFlag || offer != null && bFlag);

		// Long bCounter
		counter = subscriber.getUsageCounters().get(calls.getBeneficiaryUsageCounterID());
		actual = counter == null ? null : counter.getUsageCounterValue();
		assertTrue(actual == null && bCounter == null || actual != null && bCounter != null && (long) actual == (long) bCounter);

		// Long bLimit
		UsageThreshold threshold = subscriber.getUsageThresholds().get(calls.getBeneficiaryTotalThresholdID());
		actual = threshold == null ? null : threshold.getUsageThresholdValue();
		assertTrue(actual == null && bLimit == null || actual != null && bLimit != null && (long) actual == (long) bLimit);

		// Long bWarning
		threshold = subscriber.getUsageThresholds().get(calls.getBeneficiaryWarningUsageThresholdID());
		actual = threshold == null ? null : threshold.getUsageThresholdValue();
		assertTrue(actual == null && bWarning == null || actual != null && bWarning != null && (long) actual == (long) bWarning);

	}

	// Check Balances
	private void check(SubscriberEx subscriber, Long balance, String units)
	{
		GetBalancesRequest request = new GetBalancesRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(MONTHLY);
		request.setSubscriberNumber(new Number(subscriber.getInternationalNumber()));
		request.setRequestSMS(false);
		GetBalancesResponse response = vasConnector.getBalances(request);
		assertTrue(ReturnCodes.success == response.getReturnCode() || balance == null);

		ServiceBalance[] balances = response.getBalances();
		if (balances == null)
			balances = new ServiceBalance[0];
		assertTrue(balances.length == 1 || balances.length == 2 || balance == null || balance == 0L);
		if (balances.length <= 1)
			return;
		assertEquals(units, balances[1].getUnit());
		assertEquals((long) balance, balances[1].getValue());
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Teardown
	//
	// /////////////////////////////////
	@AfterClass
	public static void teardown()
	{
		if (airSimulator != null)
			airSimulator.stop();
		esb.stop();
	}

}
