package hxc.services.sharedaccounts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.concurrent.hxc.AddMemberRequest;
import com.concurrent.hxc.AddMemberResponse;
import com.concurrent.hxc.AddQuotaRequest;
import com.concurrent.hxc.AddQuotaResponse;
import com.concurrent.hxc.ChangeQuotaRequest;
import com.concurrent.hxc.ChangeQuotaResponse;
import com.concurrent.hxc.GetBalancesRequest;
import com.concurrent.hxc.GetBalancesResponse;
import com.concurrent.hxc.GetHistoryRequest;
import com.concurrent.hxc.GetHistoryResponse;
import com.concurrent.hxc.GetMembersRequest;
import com.concurrent.hxc.GetMembersResponse;
import com.concurrent.hxc.GetOwnersRequest;
import com.concurrent.hxc.GetOwnersResponse;
import com.concurrent.hxc.GetQuotasRequest;
import com.concurrent.hxc.GetQuotasResponse;
import com.concurrent.hxc.GetReturnCodeTextRequest;
import com.concurrent.hxc.GetReturnCodeTextResponse;
import com.concurrent.hxc.GetServiceRequest;
import com.concurrent.hxc.GetServiceResponse;
import com.concurrent.hxc.GetServicesRequest;
import com.concurrent.hxc.GetServicesResponse;
import com.concurrent.hxc.IHxC;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.ProcessLifecycleEventRequest;
import com.concurrent.hxc.ProcessLifecycleEventResponse;
import com.concurrent.hxc.RemoveMemberRequest;
import com.concurrent.hxc.RemoveMemberResponse;
import com.concurrent.hxc.RemoveMembersRequest;
import com.concurrent.hxc.RemoveMembersResponse;
import com.concurrent.hxc.RemoveQuotaRequest;
import com.concurrent.hxc.RemoveQuotaResponse;
import com.concurrent.hxc.RequestHeader;
import com.concurrent.hxc.ResponseHeader;
import com.concurrent.hxc.ServiceQuota;
import com.concurrent.hxc.SubscribeRequest;
import com.concurrent.hxc.SubscribeResponse;
import com.concurrent.hxc.SubscriptionState;
import com.concurrent.hxc.UnsubscribeRequest;
import com.concurrent.hxc.UnsubscribeResponse;
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
import hxc.connectors.file.ConfigRecord;
import hxc.connectors.file.FileConnector;
import hxc.connectors.file.FileConnector.FileConnectorConfiguration;
import hxc.connectors.file.FileProcessorType;
import hxc.connectors.file.FileType;
import hxc.connectors.file.IFileConnector;
import hxc.connectors.lifecycle.ILifecycle;
import hxc.connectors.lifecycle.ISubscription;
import hxc.connectors.lifecycle.LifecycleConnector;
import hxc.connectors.lifecycle.LifecycleConnector.LifecycleConfiguration;
import hxc.connectors.smpp.SmppConnector;
import hxc.connectors.soap.ISoapConnector;
import hxc.connectors.soap.SoapConnector;
import hxc.connectors.sut.C4UTestConnector;
import hxc.servicebus.HostInfo;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.servicebus.ServiceBus;
import hxc.services.airsim.AirSim;
import hxc.services.airsim.model.OfferEx;
import hxc.services.airsim.model.SubscriberEx;
import hxc.services.airsim.protocol.CallHistory;
import hxc.services.airsim.protocol.Cdr;
import hxc.services.airsim.protocol.DedicatedAccount;
import hxc.services.airsim.protocol.IAirSim;
import hxc.services.airsim.protocol.SmsHistory;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.services.airsim.protocol.TnpThreshold;
import hxc.services.airsim.protocol.TnpThreshold.TnpTriggerTypes;
import hxc.services.airsim.protocol.UsageCounter;
import hxc.services.airsim.protocol.UsageThreshold;
import hxc.services.notification.INotification;
import hxc.services.notification.INotifications;
import hxc.services.numberplan.INumberPlan;
import hxc.services.numberplan.NumberPlanService;
import hxc.services.pin.PinService;
import hxc.services.reporting.ReportingService;
import hxc.services.security.SecurityService;
import hxc.services.sharedaccounts.SharedAccountsBase.SharedAccountsConfig;
import hxc.services.transactions.TransactionService;
import hxc.testsuite.RunAllTestsBase;

public class SharedAccountsTest extends RunAllTestsBase
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	//private static String INPUT_DIRECTORY = "/var/opt/cs/c4u/input";
	private static String INPUT_DIRECTORY = "/tmp/c4u";
	private static String DONE_DIRECTORY = String.format("%s%s", INPUT_DIRECTORY, "/done");

	private String A_NUMBER = "08244526545";
	private String B_NUMBER1 = "0823751482";
	private String B_NUMBER2 = "0823751483";

	private String SERVICE_ID = "GSA";
	private String SERVICE_NAME_FR = "Group Shared Accounts";

	private String MONTHLY = "Monthly";
	private String WEEKLY = "Weekly";
	private String DAILY = "Daily";

	private int VOICE_DA_ID = 1000;

	private String QUOTA_ID = "Calls_C4U";
	private String SERVICE_TYPE = "Calls";
	private String DESTINATION = "C4U to C4U";
	private String DAYS_OF_WEEK = "Any Day";
	private String TIME_OF_DAY = "Any Time";
	private static IAirSim airSimulator = null;
	private int languageID = 1;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Setup
	//
	// /////////////////////////////////
	@BeforeClass
	public static void setup() throws ValidationException
	{

		// //////////////////////////////////////////////////////////////////
		//

		// Delete old TNP Files
		File folder = new File(INPUT_DIRECTORY);
		if (folder.exists())
		{
			// Retrieve ".tnp" files in the specified directory
			File[] files = folder.listFiles(new FilenameFilter()
			{

				@Override
				public boolean accept(File dir, String name)
				{
					int length = name.length();
					if (length <= 4)
						return false;
					String extension = name.substring(length - 4);

					boolean result = extension.equalsIgnoreCase(".tnp");
					return result;
				}

			});

			// Delete the above-retrieved ".tnp" files
			for (File file : files)
			{
				if (file.exists())
					file.delete();
			}
		}
		//
		// //////////////////////////////////////////////////////////////////

		// Create Transaction Service
		esb = ServiceBus.getInstance();
		esb.stop();
		configureLogging(esb);

		esb.registerService(new TransactionService());
		esb.registerService(new SharedAccounts());
		esb.registerService(new NumberPlanService()); // ??
		esb.registerService(new SecurityService());
		esb.registerService(new PinService());
		esb.registerService(new ReportingService());

		AirConnector air = new AirConnector();
		AirConnectorConfig airConfig = (AirConnectorConfig) air.getConfiguration();
		AirConnectionConfig conConfig = (AirConnectionConfig) airConfig.getConfigurations().iterator().next();
		conConfig.setUri("http://127.0.0.1:10011/Air");

		esb.registerConnector(air);
		esb.registerConnector(new MySqlConnector()); // ??
		esb.registerConnector(new SmppConnector());
		esb.registerConnector(new SoapConnector());
		esb.registerConnector(new CtrlConnector()); // ??
		esb.registerConnector(new C4UTestConnector());

		// ///////////////////////////////////////////////////////////////////
		//

		// Add File Connector for TNP v3
		FileConnector fileConnector = new FileConnector();
		FileConnectorConfiguration fileConnectorConfig = (FileConnectorConfiguration) fileConnector.getConfiguration();

		ConfigRecord cr1 = new ConfigRecord();
		cr1.setSequence(1);
		cr1.setFilenameFilter("*v2.0.TNP");
		cr1.setInputDirectory(INPUT_DIRECTORY);
		cr1.setOutputDirectory(DONE_DIRECTORY);
		cr1.setFileProcessorType(FileProcessorType.CSV);
		cr1.setFileType(FileType.ThresholdNotificationFileV2);
		cr1.setServerRole(HostInfo.getName());
		cr1.setStrictlySequential(false);

		ConfigRecord cr2 = new ConfigRecord();
		cr2.setFilenameFilter("*v3.0.TNP");
		cr2.setInputDirectory(INPUT_DIRECTORY);
		cr2.setOutputDirectory(DONE_DIRECTORY);
		cr2.setFileProcessorType(FileProcessorType.CSV);
		cr2.setFileType(FileType.ThresholdNotificationFileV3);
		cr2.setServerRole(HostInfo.getName());
		cr2.setStrictlySequential(false);

		fileConnectorConfig.setFileConfigs(new ConfigRecord[] { cr1, cr2 });
		esb.registerConnector(fileConnector);

		// Add Lifecycle Connector
		LifecycleConnector lifecycle = new LifecycleConnector();
		esb.registerConnector(lifecycle);
		LifecycleConfiguration lifecycleConfig = (LifecycleConfiguration) lifecycle.getConfiguration();
		lifecycleConfig.setPollingIntervalSeconds(10);

		//
		// ///////////////////////////////////////////////////////////////////

		boolean started = esb.start(null);
		assert (started);

	} // setup()

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Test Shared Accounts
	//
	// /////////////////////////////////
	@Test
	public void testSharedAccounts() throws ValidationException, IOException, SQLException, InterruptedException
	{
		long balance = 4400L;
		final int CHARGE_SUB = 0; // 200;
		final int CHARGE_UNS = 0;
		final int CHARGE_ADD = 0;
		final int CHARGE_REM = 0; // 50;
		final int CHARGE_QTA = 45;
		final int CHARGE_DEL = 0;
		final int CHARGE_REN = 200;
		final int CHARGE_BEQ = 0;

		// Get the VAS Soap Interface
		ISoapConnector soapConnector = esb.getFirstConnector(ISoapConnector.class);
		assertNotNull("SOAP Connector is NULL!! Connector not starting !", soapConnector);
		IHxC vasConnector = soapConnector.getVasInterface();

		// Get Lifecycle
		Party aParty = new Party(A_NUMBER, languageID);
		Party bParty1 = new Party(B_NUMBER1, languageID);
		Party bParty2 = new Party(B_NUMBER2, languageID);
		IDatabase database = esb.getFirstConnector(IDatabase.class);
		IDatabaseConnection db = database.getConnection(null);
		ILifecycle lifecycle = esb.getFirstConnector(ILifecycle.class);
		lifecycle.removeSubscriptions(db, aParty, SERVICE_ID);
		lifecycle.removeMembers(db, aParty, SERVICE_ID, DAILY);
		lifecycle.removeMembers(db, aParty, SERVICE_ID, WEEKLY);
		lifecycle.removeMembers(db, aParty, SERVICE_ID, MONTHLY);

		// Get Services w/o AIR
		{
			// GetServicesRequest request = new GetServicesRequest();
			// initialize(request);
			// request.setSubscriberNumber(new Number(A_NUMBER));
			// request.setActiveOnly(true);
			// GetServicesResponse response = vasConnector.getServices(request);
			// validate(request, response);
			// VasServiceInfo[] info = response.getServiceInfo();
			// assertEquals(0, info.length);
		}

		// Setup Simulator
		INumberPlan numberPlan = esb.getFirstService(INumberPlan.class);
		airSimulator = new AirSim(esb, 10011, "/Air", numberPlan, "CFR");
		assertTrue(airSimulator.start());
		SubscriberEx subscriberA = (SubscriberEx) airSimulator.addSubscriber(A_NUMBER, languageID, 101, balance, SubscriberState.active);
		airSimulator.addSubscriber(B_NUMBER1, languageID, 101, 200, SubscriberState.active);
		airSimulator.addSubscriber(B_NUMBER2, languageID, 101, 200, SubscriberState.active);

		// Provision subscriberA with voice DA
		DedicatedAccount da = new DedicatedAccount();
		da.setDedicatedAccountID(VOICE_DA_ID);
		da.setDedicatedAccountUnitType(0);
		da.setDedicatedAccountValue1(200L);
		// airSimulator.updateDedicatedAccount(subscriberA.getInternationalNumber(), da);

		//airSimulator.injectResponse(AirCalls.DeleteOffer, 100, 0);
		//airSimulator.injectResponse(AirCalls.DeleteOffer, 0, 0);

		// Setup Config
		SharedAccounts service = esb.getFirstService(SharedAccounts.class);
		SharedAccountsConfig config = (SharedAccountsConfig) service.getConfiguration();
		Variant[] variants = config.getVariants();
		Variant monthly = variants[2];
		assertEquals(MONTHLY, monthly.getVariantID());

		// Create Usage Counters
		for (Quota quota : config.getQuotas())
		{
			if (quota.getQuotaID().equalsIgnoreCase(QUOTA_ID))
			{
				{
					int beneficiaryTotalThresholdID = quota.getBeneficiaryTotalThresholdID();
					UsageThreshold usageThreshold = new UsageThreshold();
					usageThreshold.setUsageThresholdID(beneficiaryTotalThresholdID);
					airSimulator.updateUsageThreshold(B_NUMBER1, usageThreshold);
					airSimulator.updateUsageThreshold(B_NUMBER2, usageThreshold);
				}

				{
					int beneficiaryWarningUsageThresholdID = quota.getBeneficiaryWarningUsageThresholdID();
					UsageThreshold usageThreshold = new UsageThreshold();
					usageThreshold.setUsageThresholdID(beneficiaryWarningUsageThresholdID);
					airSimulator.updateUsageThreshold(B_NUMBER1, usageThreshold);
					airSimulator.updateUsageThreshold(B_NUMBER2, usageThreshold);
				}

				{
					int beneficiaryUsageCounterID = quota.getBeneficiaryUsageCounterID();
					UsageCounter usageCounter = new UsageCounter();
					usageCounter.setUsageCounterID(beneficiaryUsageCounterID);
					airSimulator.updateUsageCounter(A_NUMBER, usageCounter);
					airSimulator.updateUsageCounter(B_NUMBER1, usageCounter);
					airSimulator.updateUsageCounter(B_NUMBER2, usageCounter);
				}

				break;
			}
		}

		
		// Get Return Code Text for insufficientBalance
		{
			GetReturnCodeTextRequest request = new GetReturnCodeTextRequest();
			initialize(request);
			request.setReturnCode(ReturnCodes.insufficientBalance);
			request.setLanguageID(2);
			request.setServiceID(SERVICE_ID);
			GetReturnCodeTextResponse response = vasConnector.getReturnCodeText(request);
			validate(request, response);
		}

		// Get Return Code Text for invalidPin
		{
			GetReturnCodeTextRequest request = new GetReturnCodeTextRequest();
			initialize(request);
			request.setReturnCode(ReturnCodes.invalidPin);
			request.setLanguageID(2);
			request.setServiceID(SERVICE_ID);
			GetReturnCodeTextResponse response = vasConnector.getReturnCodeText(request);
			validate(request, response);
		}

		// Get Return Code Text for timedOut
		{
			GetReturnCodeTextRequest request = new GetReturnCodeTextRequest();
			initialize(request);
			request.setReturnCode(ReturnCodes.timedOut);
			request.setLanguageID(1);
			request.setServiceID(SERVICE_ID);
			GetReturnCodeTextResponse response = vasConnector.getReturnCodeText(request);
			validate(request, response);
		}

		// Get All Services
		{
			GetServicesRequest request = new GetServicesRequest();
			initialize(request);
			request.setActiveOnly(false);
			GetServicesResponse response = vasConnector.getServices(request);
			validate(request, response);
			VasServiceInfo[] info = response.getServiceInfo();
			// assertEquals(3, info.length);
			assertEquals(4, info.length);
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
			request.setSubscriberNumber(new Number(A_NUMBER));
			GetServicesResponse response = vasConnector.getServices(request);
			validate(request, response);
			VasServiceInfo[] info = response.getServiceInfo();
			// assertEquals(0, info.length);
			assertEquals(1, info.length);
		}

		// Get Service
		{
			GetServiceRequest request = new GetServiceRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setActiveOnly(true);
			GetServiceResponse response = vasConnector.getService(request);
			validate(request, response);
			assertEquals(0, response.getServiceInfo().length);

		}

		// Subscribe but delete offer
		{
			SubscribeRequest subscribeRequest = new SubscribeRequest();
			initialize(subscribeRequest);
			subscribeRequest.setServiceID(SERVICE_ID);
			subscribeRequest.setVariantID(MONTHLY);
			subscribeRequest.setSubscriberNumber(new Number(A_NUMBER));
			SubscribeResponse subscribeResponse = vasConnector.subscribe(subscribeRequest);
			validate(subscribeRequest, subscribeResponse);
		}
		balance -= CHARGE_SUB;
		Map<Integer, OfferEx> offerz = subscriberA.getOffers();
		offerz.remove(9999);
		// subscriberA.getOffers().remove(9999);
		assert (lifecycle.isSubscribed(db, aParty, SERVICE_ID));

		// Get Services and Expect no result
		{
			GetServiceRequest request = new GetServiceRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setActiveOnly(true);
			GetServiceResponse response = vasConnector.getService(request);
			validate(request, response);
			assertEquals(0, response.getServiceInfo().length);

		}
		assert (!lifecycle.isSubscribed(db, aParty, SERVICE_ID));

		// Subscribe
		assertEquals(balance, (long) subscriberA.getAccountValue1());
		{
			SubscribeRequest subscribeRequest = new SubscribeRequest();
			initialize(subscribeRequest);
			subscribeRequest.setServiceID(SERVICE_ID);
			subscribeRequest.setVariantID(MONTHLY);
			subscribeRequest.setSubscriberNumber(new Number(A_NUMBER));
			SubscribeResponse subscribeResponse = vasConnector.subscribe(subscribeRequest);
			validate(subscribeRequest, subscribeResponse);
		}
		balance -= CHARGE_SUB;
		assertEquals(balance, (long) subscriberA.getAccountValue1());
		assertTrue(lifecycle.isSubscribed(db, aParty, SERVICE_ID));

		// Deliberately remove subscription lifecycle record
		ISubscription subscription1 = lifecycle.getSubscription(db, aParty, SERVICE_ID, MONTHLY);
		// lifecycle.removeSubscriptions(db, aParty, SERVICE_ID);

		// Attempt to Subscribe Twice
		{
			SubscribeRequest subscribeRequest = new SubscribeRequest();
			initialize(subscribeRequest);
			subscribeRequest.setServiceID(SERVICE_ID);
			subscribeRequest.setVariantID(MONTHLY);
			subscribeRequest.setSubscriberNumber(new Number(A_NUMBER));
			SubscribeResponse subscribeResponse = vasConnector.subscribe(subscribeRequest);
			assertEquals(ReturnCodes.alreadySubscribed, subscribeResponse.getReturnCode());
		}

		// Test Sim History
		{
			CallHistory[] history = airSimulator.getCallHistory();
			assert (history.length > 2);
			airSimulator.clearCallHistory();
			history = airSimulator.getCallHistory();
			assertEquals(0, history.length);
		}

		// Get Active Services
		{
			GetServicesRequest request = new GetServicesRequest();
			initialize(request);
			request.setActiveOnly(true);
			request.setSubscriberNumber(new Number(A_NUMBER));
			GetServicesResponse response = vasConnector.getServices(request);
			validate(request, response);
			VasServiceInfo[] info = response.getServiceInfo();
			// assertEquals(1, info.length);
			assertEquals(2, info.length);
			assertEquals(SERVICE_ID, info[0].getServiceID());
			assertEquals(MONTHLY, info[0].getVariantID());
			assertEquals(SERVICE_NAME_FR, info[0].getServiceName());
			assertEquals(SubscriptionState.active, info[0].getState());
		}
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Check Recovered Lifecycle
		ISubscription subscription2 = lifecycle.getSubscription(db, aParty, SERVICE_ID, MONTHLY);
		assertEquals(subscription1.getDateTime1(), subscription2.getDateTime1());
		assertEquals(subscription1.getDateTime2(), subscription2.getDateTime2());
		assertEquals(subscription1.getDateTime3(), subscription2.getDateTime3());
		assertEquals(subscription1.getMsisdn(), subscription2.getMsisdn());
		assertEquals(subscription1.getNextDateTime(), subscription2.getNextDateTime());
		assertEquals(subscription1.getState(), subscription2.getState());
		assertEquals(subscription1.getVariantID(), subscription2.getVariantID());
		assertEquals(subscription1.isBeingProcessed(), subscription2.isBeingProcessed());
		assertEquals(subscription1.getServiceID(), subscription2.getServiceID());

		// Get Service
		{
			GetServiceRequest request = new GetServiceRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setActiveOnly(true);
			GetServiceResponse response = vasConnector.getService(request);
			validate(request, response);
			assertEquals(MONTHLY, response.getServiceInfo()[0].getVariantID());
			assertEquals(SubscriptionState.active, response.getServiceInfo()[0].getState());
		}
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Get Service
		{
			GetServiceRequest request = new GetServiceRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setActiveOnly(true);
			GetServiceResponse response = vasConnector.getService(request);
			validate(request, response);
			assertEquals(SubscriptionState.active, response.getServiceInfo()[0].getState());
		}
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Add Bogus membership
		lifecycle.addMember(db, aParty, SERVICE_ID, MONTHLY, bParty1);

		// Add Member 1
		{
			AddMemberRequest request = new AddMemberRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER1));
			AddMemberResponse response = vasConnector.addMember(request);
			validate(request, response);
		}
		balance -= CHARGE_ADD;
		assertEquals(balance, (long) subscriberA.getAccountValue1());
		assertTrue(lifecycle.isMember(db, aParty, SERVICE_ID, bParty1));
		assertFalse(lifecycle.isMember(db, aParty, SERVICE_ID, bParty2));

		// Add Member 2
		{
			AddMemberRequest request = new AddMemberRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER2));
			AddMemberResponse response = vasConnector.addMember(request);
			validate(request, response);
		}
		balance -= CHARGE_ADD;
		assertEquals(balance, (long) subscriberA.getAccountValue1());
		assertTrue(lifecycle.isMember(db, aParty, SERVICE_ID, bParty1));
		assertTrue(lifecycle.isMember(db, aParty, SERVICE_ID, bParty2));

		// Attempt to Add Member 2 again
		{
			AddMemberRequest request = new AddMemberRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER2));
			AddMemberResponse response = vasConnector.addMember(request);
			assertEquals(ReturnCodes.alreadyOtherMember, response.getReturnCode());
		}
		assertEquals(balance, (long) subscriberA.getAccountValue1());
		assertTrue(lifecycle.isMember(db, aParty, SERVICE_ID, bParty1));
		assertTrue(lifecycle.isMember(db, aParty, SERVICE_ID, bParty2));

		// Deliberately Delete Membership for Member 2
		lifecycle.removeMember(db, aParty, SERVICE_ID, MONTHLY, bParty2);
		assert (!lifecycle.isMember(db, aParty, SERVICE_ID, MONTHLY, bParty2));

		// Get All possible Quotas for B_NUMBER2
		{
			GetQuotasRequest request = new GetQuotasRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER2));
			request.setActiveOnly(false);
			GetQuotasResponse response = vasConnector.getQuotas(request);
			validate(request, response);
			// assertEquals(2, response.getServiceQuotas().length);
			assertEquals(3, response.getServiceQuotas().length);
		}
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Get All Active Quotas for B_NUMBER2
		{
			GetQuotasRequest request = new GetQuotasRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER2));
			request.setActiveOnly(true);
			GetQuotasResponse response = vasConnector.getQuotas(request);
			validate(request, response);
			assertEquals(0, response.getServiceQuotas().length);

		}
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Add Quota for B_NUMBER2
		{
			AddQuotaRequest request = new AddQuotaRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER2));

			ServiceQuota quota = new ServiceQuota();
			quota.setService(SERVICE_TYPE);
			quota.setDestination(DESTINATION);
			quota.setDaysOfWeek(DAYS_OF_WEEK);
			quota.setTimeOfDay(TIME_OF_DAY);

			quota.setQuantity(5L);
			request.setQuota(quota);

			AddQuotaResponse response = vasConnector.addQuota(request);
			validate(request, response);
			assertNotNull(response.getQuota());
			assertEquals(QUOTA_ID, response.getQuota().getQuotaID());
		}
		balance -= CHARGE_QTA * 5;
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Membership has to be restored for Member 2
		assert (lifecycle.isMember(db, aParty, SERVICE_ID, MONTHLY, bParty2));

		// Try to Add Quota for B_NUMBER2 again
		{
			AddQuotaRequest request = new AddQuotaRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER2));

			ServiceQuota quota = new ServiceQuota();
			quota.setService(SERVICE_TYPE);
			quota.setDestination(DESTINATION);
			quota.setDaysOfWeek(DAYS_OF_WEEK);
			quota.setTimeOfDay(TIME_OF_DAY);

			quota.setQuantity(5L);
			request.setQuota(quota);

			AddQuotaResponse response = vasConnector.addQuota(request);
			assertEquals(ReturnCodes.alreadyAdded, response.getReturnCode());

		}

		// Change Quota for B_NUMBER2
		{
			ChangeQuotaRequest request = new ChangeQuotaRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER2));

			ServiceQuota oldQuota = new ServiceQuota();
			oldQuota.setService(SERVICE_TYPE);
			oldQuota.setDestination(DESTINATION);
			oldQuota.setDaysOfWeek(DAYS_OF_WEEK);
			oldQuota.setTimeOfDay(TIME_OF_DAY);
			request.setOldQuota(oldQuota);

			ServiceQuota newQuota = new ServiceQuota();
			newQuota.setService(SERVICE_TYPE);
			newQuota.setDestination(DESTINATION);
			newQuota.setDaysOfWeek(DAYS_OF_WEEK);
			newQuota.setTimeOfDay(TIME_OF_DAY);
			request.setNewQuota(newQuota);
			newQuota.setQuantity(6L);

			ChangeQuotaResponse response = vasConnector.changeQuota(request);
			validate(request, response);

			//assert (subscriberA.hasOffer(200400));
			//assert (subscriberA.hasOffer(2004));
			//assert (subscriberB2.hasOffer(2004));

		}
		balance -= CHARGE_QTA;
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Get Balance for Consumer
		{
			GetBalancesRequest request = new GetBalancesRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(B_NUMBER2));
			request.setRequestSMS(true);

			GetBalancesResponse response = vasConnector.getBalances(request);
			validate(request, response);
		}

		// Get All Active Quotas for B_NUMBER2
		{
			GetQuotasRequest request = new GetQuotasRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER2));
			request.setActiveOnly(true);
			GetQuotasResponse response = vasConnector.getQuotas(request);
			validate(request, response);
			assertEquals(1, response.getServiceQuotas().length);

		}
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Remove Quota for B_NUMBER2
		{
			RemoveQuotaRequest request = new RemoveQuotaRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER2));

			ServiceQuota quota = new ServiceQuota();
			quota.setQuotaID(QUOTA_ID);
			request.setQuota(quota);

			RemoveQuotaResponse response = vasConnector.removeQuota(request);
			validate(request, response);

		}
		balance -= CHARGE_DEL;
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Get All Active Quotas for B_NUMBER2
		{
			GetQuotasRequest request = new GetQuotasRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER2));
			request.setActiveOnly(true);
			GetQuotasResponse response = vasConnector.getQuotas(request);
			validate(request, response);
			assertEquals(0, response.getServiceQuotas().length);

		}
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Get Members
		{
			GetMembersRequest request = new GetMembersRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			GetMembersResponse response = vasConnector.getMembers(request);
			validate(request, response);
			assertEquals(2, response.getMembers().length);
		}
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Get Owners
		{
			GetOwnersRequest request = new GetOwnersRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setMemberNumber(new Number(B_NUMBER2));
			GetOwnersResponse response = vasConnector.getOwners(request);
			validate(request, response);
			assertEquals(1, response.getOwners().length);
			assertEquals(A_NUMBER, response.getOwners()[0].toMSISDN());
		}
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Delete Member
		{
			RemoveMemberRequest request = new RemoveMemberRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER1));
			RemoveMemberResponse response = vasConnector.removeMember(request);
			validate(request, response);
		}
		balance -= CHARGE_REM;
		assertEquals(balance, (long) subscriberA.getAccountValue1());
		assertFalse(lifecycle.isMember(db, aParty, SERVICE_ID, bParty1));
		assertTrue(lifecycle.isMember(db, aParty, SERVICE_ID, bParty2));

		// Add Member 1 again
		{
			AddMemberRequest request = new AddMemberRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER1));
			AddMemberResponse response = vasConnector.addMember(request);
			validate(request, response);
		}
		balance -= CHARGE_ADD;
		assertEquals(balance, (long) subscriberA.getAccountValue1());
		assertTrue(lifecycle.isMember(db, aParty, SERVICE_ID, bParty1));

		// Add Quota for B_NUMBER1
		{
			AddQuotaRequest request = new AddQuotaRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER1));

			ServiceQuota quota = new ServiceQuota();
			quota.setService(SERVICE_TYPE);
			quota.setDestination(DESTINATION);
			quota.setDaysOfWeek(DAYS_OF_WEEK);
			quota.setTimeOfDay(TIME_OF_DAY);

			quota.setQuantity(10L);
			request.setQuota(quota);

			AddQuotaResponse response = vasConnector.addQuota(request);
			validate(request, response);

		}
		balance -= CHARGE_QTA * 10;
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Add Quota for B_NUMBER2
		{
			AddQuotaRequest request = new AddQuotaRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER2));

			ServiceQuota quota = new ServiceQuota();
			quota.setService(SERVICE_TYPE);
			quota.setDestination(DESTINATION);
			quota.setDaysOfWeek(DAYS_OF_WEEK);
			quota.setTimeOfDay(TIME_OF_DAY);

			quota.setQuantity(5L);
			request.setQuota(quota);

			AddQuotaResponse response = vasConnector.addQuota(request);
			validate(request, response);

		}
		balance -= CHARGE_QTA * 5;
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Get Balance for Provider
		{
			GetBalancesRequest request = new GetBalancesRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setRequestSMS(true);

			GetBalancesResponse response = vasConnector.getBalances(request);
			validate(request, response);
		}
		balance -= CHARGE_BEQ;
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Get Balance for Consumer
		{
			GetBalancesRequest request = new GetBalancesRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(B_NUMBER1));
			request.setRequestSMS(true);

			GetBalancesResponse response = vasConnector.getBalances(request);
			validate(request, response);
		}

		// Delete Member1
		{
			RemoveMemberRequest request = new RemoveMemberRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER1));
			RemoveMemberResponse response = vasConnector.removeMember(request);
			validate(request, response);
		}
		balance -= CHARGE_REM;
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Get Members
		{
			GetMembersRequest request = new GetMembersRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			GetMembersResponse response = vasConnector.getMembers(request);
			validate(request, response);
			assertEquals(1, response.getMembers().length);
			assertEquals(B_NUMBER2, response.getMembers()[0].getAddressDigits());
		}
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Get Members
		{
			GetMembersRequest request = new GetMembersRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			GetMembersResponse response = vasConnector.getMembers(request);
			validate(request, response);
			assertEquals(1, response.getMembers().length);
			assertEquals(B_NUMBER2, response.getMembers()[0].getAddressDigits());
		}
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Send First Warning
		{

			ISubscription lcy = lifecycle.getSubscription(db, aParty, SERVICE_ID, MONTHLY);
			assertNotNull(lcy);

			ProcessLifecycleEventRequest request = createRequest(lcy);

			ProcessLifecycleEventResponse response = vasConnector.processLifecycleEvent(request);
			validate(request, response);
		}
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Send Second Warning
		{
			ISubscription lcy = lifecycle.getSubscription(db, aParty, SERVICE_ID, MONTHLY);
			assertNotNull(lcy);
			ProcessLifecycleEventRequest request = createRequest(lcy);

			ProcessLifecycleEventResponse response = vasConnector.processLifecycleEvent(request);
			validate(request, response);
		}
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Renew Subscription
		{
			ISubscription lcy = lifecycle.getSubscription(db, aParty, SERVICE_ID, MONTHLY);
			assertNotNull(lcy);

			ProcessLifecycleEventRequest request = createRequest(lcy);

			ProcessLifecycleEventResponse response = vasConnector.processLifecycleEvent(request);
			validate(request, response);
		}
		balance -= CHARGE_REN + CHARGE_QTA * 5;
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Delete Members
		{
			RemoveMembersRequest request = new RemoveMembersRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			RemoveMembersResponse response = vasConnector.removeMembers(request);
			validate(request, response);
		}
		balance -= CHARGE_REM;
		assertEquals(balance, (long) subscriberA.getAccountValue1());
		assertFalse(lifecycle.isMember(db, aParty, SERVICE_ID, bParty1));
		assertFalse(lifecycle.isMember(db, aParty, SERVICE_ID, bParty2));

		// Get Members
		{
			GetMembersRequest request = new GetMembersRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			GetMembersResponse response = vasConnector.getMembers(request);
			validate(request, response);
			assertEquals(0, response.getMembers().length);
		}
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Send First Warning
		{

			ISubscription lcy = lifecycle.getSubscription(db, aParty, SERVICE_ID, MONTHLY);
			assertNotNull(lcy);

			ProcessLifecycleEventRequest request = createRequest(lcy);

			ProcessLifecycleEventResponse response = vasConnector.processLifecycleEvent(request);
			validate(request, response);
		}
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Send Second Warning
		{

			ISubscription lcy = lifecycle.getSubscription(db, aParty, SERVICE_ID, MONTHLY);
			assertNotNull(lcy);

			ProcessLifecycleEventRequest request = createRequest(lcy);

			ProcessLifecycleEventResponse response = vasConnector.processLifecycleEvent(request);
			validate(request, response);
		}
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Renew Subscription
		{

			ISubscription lcy = lifecycle.getSubscription(db, aParty, SERVICE_ID, MONTHLY);
			assertNotNull(lcy);

			ProcessLifecycleEventRequest request = new ProcessLifecycleEventRequest(lcy);
			initialize(request);

			ProcessLifecycleEventResponse response = vasConnector.processLifecycleEvent(request);
			validate(request, response);
		}
		balance -= CHARGE_REN;
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Un-Subscribe
		{
			UnsubscribeRequest unsubscribeRequest = new UnsubscribeRequest();
			initialize(unsubscribeRequest);
			unsubscribeRequest.setServiceID(SERVICE_ID);
			unsubscribeRequest.setVariantID(MONTHLY);
			unsubscribeRequest.setSubscriberNumber(new Number(A_NUMBER));
			UnsubscribeResponse unsubscribeResponse = vasConnector.unsubscribe(unsubscribeRequest);
			validate(unsubscribeRequest, unsubscribeResponse);
		}
		balance -= CHARGE_UNS;
		assertEquals(balance, (long) subscriberA.getAccountValue1());

		// Get History
		{
			GetHistoryRequest request = new GetHistoryRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setRowLimit(3);
			request.setInReverse(true);
//			GetHistoryResponse response = vasConnector.getHistory(request);
			GetHistoryResponse response = null;
			validate(request, response);
		}
		

		// Create a Dedicated Account Dump Trigger Transfer for subscriber B
		{

			// /////////////////////////////////////////////////////////////////////////////////////////
			//
			//

			// Deliberately Delete Membership for Member 2
			lifecycle.removeMember(db, aParty, SERVICE_ID, MONTHLY, bParty2);
			assert (!lifecycle.isMember(db, aParty, SERVICE_ID, MONTHLY, bParty2));

			// Subscribe
			assertEquals(balance, (long) subscriberA.getAccountValue1());
			{
				SubscribeRequest subscribeRequest = new SubscribeRequest();
				initialize(subscribeRequest);
				subscribeRequest.setServiceID(SERVICE_ID);
				subscribeRequest.setVariantID(MONTHLY);
				subscribeRequest.setSubscriberNumber(new Number(A_NUMBER));
				SubscribeResponse subscribeResponse = vasConnector.subscribe(subscribeRequest);
				validate(subscribeRequest, subscribeResponse);
			}
			balance -= CHARGE_SUB;
			assertEquals(balance, (long) subscriberA.getAccountValue1());
			assertTrue(lifecycle.isSubscribed(db, aParty, SERVICE_ID));

			// Add Member 1
			{
				AddMemberRequest request = new AddMemberRequest();
				initialize(request);
				request.setServiceID(SERVICE_ID);
				request.setVariantID(MONTHLY);
				request.setSubscriberNumber(new Number(A_NUMBER));
				request.setMemberNumber(new Number(B_NUMBER1));
				AddMemberResponse response = vasConnector.addMember(request);
				validate(request, response);
			}
			balance -= CHARGE_ADD;
			assertEquals(balance, (long) subscriberA.getAccountValue1());
			assertTrue(lifecycle.isMember(db, aParty, SERVICE_ID, bParty1));
			assertFalse(lifecycle.isMember(db, aParty, SERVICE_ID, bParty2));

			// Add Member 2
			{
				AddMemberRequest request = new AddMemberRequest();
				initialize(request);
				request.setServiceID(SERVICE_ID);
				request.setVariantID(MONTHLY);
				request.setSubscriberNumber(new Number(A_NUMBER));
				request.setMemberNumber(new Number(B_NUMBER2));
				AddMemberResponse response = vasConnector.addMember(request);
				validate(request, response);
			}
			balance -= CHARGE_ADD;
			assertEquals(balance, (long) subscriberA.getAccountValue1());
			assertTrue(lifecycle.isMember(db, aParty, SERVICE_ID, bParty1));
			assertTrue(lifecycle.isMember(db, aParty, SERVICE_ID, bParty2));

			lifecycle.addMember(db, subscriberA, SERVICE_ID, MONTHLY, bParty1);
			lifecycle.addMember(db, subscriberA, SERVICE_ID, MONTHLY, bParty2);
			String[] beneficiaries = lifecycle.getMembers(db, subscriberA, SERVICE_ID, MONTHLY);

			//
			//
			// /////////////////////////////////////////////////////////////////////////////////////

			// Provision subscriberA with voice DA
			DedicatedAccount d = new DedicatedAccount();
			d.setDedicatedAccountID(VOICE_DA_ID);
			d.setDedicatedAccountUnitType(0);
			d.setDedicatedAccountValue1(200L);
			airSimulator.updateDedicatedAccount(subscriberA.getInternationalNumber(), d);

			SharedAccounts gsa = (SharedAccounts) (esb.getFirstService(SharedAccounts.class));
			SharedAccountsConfig gsaConfig = (SharedAccountsConfig) gsa.getConfiguration();
			int voiceDAid = gsaConfig.getVoiceDedicatedAccountID();

			// Simulate TNP3 record
			TnpThreshold threshold = new TnpThreshold();
			threshold.setServiceClass(subscriberA.getServiceClassCurrent());
			threshold.setThresholdID(7);
			
			//Tell SDP simulator where to dump TNP files
			FileConnector fconn = (FileConnector)esb.getFirstConnector(IFileConnector.class);
			if (fconn == null)
			{
				assertFalse(true);
			}
			FileConnectorConfiguration fconnConfig = (FileConnectorConfiguration)fconn.getConfiguration();
			ConfigRecord[] configs = fconnConfig.getFileConfigs();

			ConfigRecord confRec = null;
			for (ConfigRecord rec : configs)
			{
				if (rec.getFileType() == FileType.ThresholdNotificationFileV3)
				{
					confRec = rec;
					break;
				}
			}
			if (confRec == null)
			{
				assertFalse(true);
			}
			String tnpDumpDirectory = confRec.getInputDirectory();
			threshold.setDirectory(tnpDumpDirectory);

				
			threshold.setVersion("3.0");
			threshold.setSenderID("sdp3");
			threshold.setReceiverID("hxc5");
			threshold.setAccountID(voiceDAid); // Voice DA = 1000
			threshold.setLevel(60); // warning level set at 60
			threshold.setUpwards(false); // we depleting the acc
			threshold.setTriggerType(TnpTriggerTypes.BATCH);
			threshold.setAccountGroupID(12);
			boolean ok = airSimulator.addTnpThreshold(threshold);
			assertTrue(ok);

			Map<Integer, DedicatedAccount> daMap = subscriberA.getDedicatedAccounts();
			DedicatedAccount dAcc = (DedicatedAccount) daMap.get(voiceDAid);
			long oldDaValue = dAcc.getDedicatedAccountValue1();// should be above threshold
			double thresholdLevel = threshold.getLevel();
			assertTrue(oldDaValue > thresholdLevel);

			long newDaValue = (long) thresholdLevel - 1;// ensure new DA value is below threshold
			assertTrue(newDaValue <= thresholdLevel);

			// Trigger dump of TNP file
			//
			dAcc.setDedicatedAccountValue1(newDaValue);
			try
			{
				subscriberA.triggerValueChange(VOICE_DA_ID, oldDaValue, newDaValue);
			}
			catch (Exception e)
			{
				// we do nothing here, test case will just fail anyway
				e.printStackTrace(System.out);
			}

			// Wait for callback magic to happen
			Thread.sleep(10000L * 1);

			// provider stuff
			int smsConsumerThresholds = gsa.getSmsConsumerThresholds();
			INotifications notifs = gsaConfig.getNotifications();
			INotification notification = notifs.getNotification(smsConsumerThresholds);
			String expectedConsumerSMS = notification.getText(languageID);
			expectedConsumerSMS = expectedConsumerSMS.substring(0, expectedConsumerSMS.lastIndexOf(':') + 1);

			// consumer stuff
			int smsProviderThresholds = gsa.getSmsProviderThresholds();
			notification = notifs.getNotification(smsProviderThresholds);
			String expectedProviderSMS = notification.getText(languageID);
			expectedProviderSMS = expectedProviderSMS.substring(0, expectedProviderSMS.lastIndexOf(':') + 1);

			// Search for the expected provider SMS in the history log
			boolean providerSmsWasSent = false;
			SmsHistory smsHistory[] = airSimulator.getSmsHistory();

			for (SmsHistory sms : smsHistory)
			{
				String currentSMS = sms.getText();

				if (currentSMS.startsWith(expectedProviderSMS))
				{
					providerSmsWasSent = true;
				}
			}
			assertTrue(providerSmsWasSent);

			// Search for the expected consumer SMS in the history log
			int smsCount = 0;
			boolean consumerSmsWasSent = false;
			for (SmsHistory sms : smsHistory)
			{
				String currentSMS = sms.getText();

				if (currentSMS.startsWith(expectedConsumerSMS))
				{
					smsCount += 1;
					if (smsCount == beneficiaries.length)
					{
						consumerSmsWasSent = true;
						break;
					}
				}
			}
			assertTrue(consumerSmsWasSent);

			//==========================  CDR  =========================
			//Basic CDR Validation
			Cdr cdr = airSimulator.getLastCdr();

			String cdrServiceID = cdr.getServiceID();
			assertTrue(cdrServiceID.equals(SERVICE_ID));

			String cdrCallerID = cdr.getCallerID();
			assertTrue(cdrCallerID.equals(A_NUMBER));
			
			String cdrProcessID = cdr.getProcessID();
			assertTrue(cdrProcessID.equals("onThresholdNotificationV3"));

			ReturnCodes cdrReturnCode = cdr.getReturnCode();
			assertTrue(cdrReturnCode == ReturnCodes.success);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	private ProcessLifecycleEventRequest createRequest(ISubscription subscription)
	{
		ProcessLifecycleEventRequest request = new ProcessLifecycleEventRequest(subscription);
		initialize(request);

		return request;
	}

	private void initialize(RequestHeader request)
	{
		request.setCallerID(A_NUMBER);
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
