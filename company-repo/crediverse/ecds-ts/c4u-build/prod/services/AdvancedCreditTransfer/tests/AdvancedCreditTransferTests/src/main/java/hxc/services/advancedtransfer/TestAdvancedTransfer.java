package hxc.services.advancedtransfer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.AddCreditTransferRequest;
import com.concurrent.hxc.AddCreditTransferResponse;
import com.concurrent.hxc.AddMemberRequest;
import com.concurrent.hxc.AddMemberResponse;
import com.concurrent.hxc.ChangePINRequest;
import com.concurrent.hxc.GetCreditTransfersRequest;
import com.concurrent.hxc.GetCreditTransfersResponse;
import com.concurrent.hxc.GetServicesRequest;
import com.concurrent.hxc.GetServicesResponse;
import com.concurrent.hxc.IHxC;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.ProcessLifecycleEventRequest;
import com.concurrent.hxc.ProcessLifecycleEventResponse;
import com.concurrent.hxc.RemoveCreditTransfersRequest;
import com.concurrent.hxc.RemoveCreditTransfersResponse;
import com.concurrent.hxc.RemoveMemberRequest;
import com.concurrent.hxc.RemoveMemberResponse;
import com.concurrent.hxc.RequestHeader;
import com.concurrent.hxc.ResponseHeader;
import com.concurrent.hxc.ResumeCreditTransferRequest;
import com.concurrent.hxc.ResumeCreditTransferResponse;
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
import hxc.connectors.lifecycle.ILifecycle;
import hxc.connectors.lifecycle.ISubscription;
import hxc.connectors.lifecycle.ITemporalTrigger;
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
import hxc.services.advancedtransfer.AdvancedTransferBase.AdvancedTransferConfig;
import hxc.services.airsim.AirSim;
import hxc.services.airsim.model.SubscriberEx;
import hxc.services.airsim.protocol.DedicatedAccount;
import hxc.services.airsim.protocol.IAirSim;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.services.airsim.protocol.TnpThreshold;
import hxc.services.airsim.protocol.TnpThreshold.TnpTriggerTypes;
import hxc.services.numberplan.INumberPlan;
import hxc.services.numberplan.NumberPlanService;
import hxc.services.pin.PinService;
import hxc.services.reporting.ReportingService;
import hxc.services.security.SecurityService;
import hxc.services.transactions.TransactionService;
import hxc.utils.calendar.DateTime;
import hxc.utils.protocol.ucip.ServiceOfferings;

public class TestAdvancedTransfer
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	final static Logger logger = LoggerFactory.getLogger(TestAdvancedTransfer.class);
	private static IServiceBus esb;
	private final String A_NUMBER = "0824452655";
	private final String B_NUMBER1 = "0823751482";
	private final String SERVICE_ID = "ACT";
	private final String SERVICE_NAME = "Advanced Credit Transfer";
	private final String MONTHLY = "Monthly";
	private final String ONEMONTH = "OneMonth";
	private final String WEEKLY = "Weekly";
	private final String DAILY = "Daily";
	private static IAirSim airSimulator = null;
	private int languageID = 1;
	private final long TRANSFER1 = 200;
	private final long TRANSFER2 = 300;
	private final static String DIRECTORY = "/tmp";
	private final static String PREFIX = "TST";

	private final String PERIODIC_MODE = "MMD";
	private final String THRESHOLD_MODE = "MDR";
	private final String ONCEOFF_MODE = "OOT";
	private final String PIN = "1234";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Setup
	//
	// /////////////////////////////////
	@BeforeClass
	public static void setup() throws ValidationException
	{
		// Delete old TNP Files
		File folder = new File(DIRECTORY);
		if (folder.exists())
		{
			File[] files = folder.listFiles(new FilenameFilter()
			{

				@Override
				public boolean accept(File dir, String name)
				{
					int length = name.length();
					if (length <= 4)
						return false;
					String extension = name.substring(length - 4);
					return extension.equalsIgnoreCase(".tnp");
				}

			});

			for (File file : files)
			{
				if (file.exists())
					file.delete();
			}
		}

		// Create Transaction Service
		esb = ServiceBus.getInstance();
		esb.stop();
		esb.registerService(new TransactionService());
		esb.registerService(new AdvancedTransfer());
		esb.registerService(new NumberPlanService());

		AirConnector air = new AirConnector();
		AirConnectorConfig airConfig = (AirConnectorConfig) air.getConfiguration();
		AirConnectionConfig conConfig = (AirConnectionConfig) airConfig.getConfigurations().iterator().next();
		conConfig.setUri("http://127.0.0.1:10011/Air");

		esb.registerConnector(air);
		esb.registerConnector(new MySqlConnector());
		esb.registerConnector(new SmppConnector());
		esb.registerService(new SecurityService());
		esb.registerConnector(new SoapConnector());
		esb.registerConnector(new CtrlConnector());
		esb.registerService(new PinService());
		esb.registerConnector(new C4UTestConnector());

		// Add File Connector for TNP v2
		FileConnector fc = new FileConnector();
		FileConnectorConfiguration fcc = (FileConnectorConfiguration) fc.getConfiguration();
		ConfigRecord cr1 = new ConfigRecord();
		cr1.setSequence(1);
		cr1.setFilenameFilter("*v2.0.TNP");
		cr1.setInputDirectory(DIRECTORY);
		cr1.setOutputDirectory("/tmp/done");
		cr1.setFileProcessorType(FileProcessorType.CSV);
		cr1.setFileType(FileType.ThresholdNotificationFileV2);
		cr1.setServerRole(HostInfo.getName());
		cr1.setStrictlySequential(false);

		// Add File Connector for DA Dumps
		ConfigRecord cr2 = new ConfigRecord();
		cr2.setSequence(2);
		cr2.setFilenameFilter("*dedicatedaccount.v3.csv");
		cr2.setInputDirectory(DIRECTORY);
		cr2.setOutputDirectory("/tmp/done");
		cr2.setFileProcessorType(FileProcessorType.CSV);
		cr2.setFileType(FileType.DedicatedAccountsFileV3_3);
		cr2.setServerRole(HostInfo.getName());
		cr2.setStrictlySequential(false);
		fcc.setFileConfigs(new ConfigRecord[] { cr1, cr2 });
		esb.registerConnector(fc);

		LifecycleConnector lc = new LifecycleConnector();
		esb.registerConnector(lc);
		LifecycleConfiguration lcc = (LifecycleConfiguration) lc.getConfiguration();
		lcc.setPollingIntervalSeconds(2);
		esb.registerService(new ReportingService());

		boolean started = esb.start(null);
		assertTrue(started);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Test Credit Sharing
	//
	// /////////////////////////////////
	@Test
	public void testAdvancedTransfer() throws ValidationException, IOException, SQLException, InterruptedException
	{
		long balanceA = 4400L;
		long balanceB = 50L;

		// Get the VAS Soap Interface
		ISoapConnector soapConnector = esb.getFirstConnector(ISoapConnector.class);
		assertNotNull("SOAP Connector is NULL!! Connector not starting !", soapConnector);
		IHxC vasConnector = soapConnector.getVasInterface();

		// Get Lifecycle
		Party aParty = new Party(A_NUMBER, languageID);
		IDatabase database = esb.getFirstConnector(IDatabase.class);
		IDatabaseConnection db = database.getConnection(null);
		ILifecycle lifecycle = esb.getFirstConnector(ILifecycle.class);

		lifecycle.removeSubscriptions(db, aParty, SERVICE_ID);
		lifecycle.removeMembers(db, aParty, SERVICE_ID, DAILY);
		lifecycle.removeMembers(db, aParty, SERVICE_ID, WEEKLY);
		lifecycle.removeMembers(db, aParty, SERVICE_ID, MONTHLY);
		lifecycle.removeMembers(db, aParty, SERVICE_ID, ONEMONTH);
		ITemporalTrigger[] triggers = lifecycle.getTemporalTriggers(db, SERVICE_ID, null, aParty, null, null);
		for (ITemporalTrigger trigger : triggers)
		{
			lifecycle.removeTemporalTrigger(db, trigger);
		}

		// Delete all Transfer Records
		db.delete(TransferRecord.class, "where donorMsisdn = %s and serviceID = %s", A_NUMBER, SERVICE_ID);

		// Setup Simulator
		INumberPlan numberPlan = esb.getFirstService(INumberPlan.class);
		airSimulator = new AirSim(esb, 10011, "/Air", numberPlan, "CFR");
		assertTrue(airSimulator.start());
		SubscriberEx subscriberA = (SubscriberEx) airSimulator.addSubscriber(A_NUMBER, languageID, 100, balanceA, SubscriberState.active);
		SubscriberEx subscriberB1 = (SubscriberEx) airSimulator.addSubscriber(B_NUMBER1, languageID, 100, balanceB, SubscriberState.active);

		// Setup Config
		AdvancedTransfer service = esb.getFirstService(AdvancedTransfer.class);
		AdvancedTransferConfig config = (AdvancedTransferConfig) service.getConfiguration();
		config.validate();
		Variant[] variants = config.getVariants();
		Variant monthly = variants[2];
		assertEquals(MONTHLY, monthly.getVariantID());

		ServiceClass serviceClass = config.getServiceClasses()[0];
		final long CHARGE_SUBSCRIBE = monthly.getSubscriptionCharge();
		final long CHARGE_USUBSCRIBE = serviceClass.getUnsubscribeCharge();
		final long CHARGE_ADD_RECIPIENT = serviceClass.getAddRecipientCharge();
		final long CHARGE_RENEW = monthly.getRenewalCharge();
		final long CHARGE_REMOVE_TRANSFER = serviceClass.getRemoveTransferCharge();
		final long CHARGE_ADD_TRANSFER = serviceClass.getAddTransferCharge();
		final long CHARGE_SUSPEND_TRANSFER = serviceClass.getSuspendTransferCharge();
		final long CHARGE_RESUME_TRANSFER = serviceClass.getResumeTransferCharge();

		TransferMode mmd = TransferMode.findByID(config.getTransferModes(), PERIODIC_MODE);
		assertNotNull(mmd);
		assertEquals(PERIODIC_MODE, mmd.getTransferModeID());
		final long abCommissionAmount = mmd.getCommissionAmount();
		final long abCommissionPercent = mmd.getCommissionPercentage();

		TransferMode mdr = TransferMode.findByID(config.getTransferModes(), THRESHOLD_MODE);
		assertNotNull(mdr);
		assertEquals(THRESHOLD_MODE, mdr.getTransferModeID());
		final long mdrCommissionAmount = mdr.getCommissionAmount();
		final long mdrCommissionPercent = mdr.getCommissionPercentage();
		final long mdrConversionRate = mdr.getConversionRate();

		// Verify 4 Variants are available
		{
			GetServicesRequest request = new GetServicesRequest();
			initialize(request);
			request.setActiveOnly(false);
			GetServicesResponse response = vasConnector.getServices(request);
			validate(request, response);
			VasServiceInfo[] info = filter(response.getServiceInfo());
			assertEquals(4, info.length);
			assertEquals(SERVICE_ID, info[0].getServiceID());
			assertEquals(DAILY, info[0].getVariantID());
			assertEquals(SERVICE_NAME, info[0].getServiceName());
			assertEquals(SubscriptionState.unknown, info[0].getState());
			assertEquals(SERVICE_ID, info[1].getServiceID());
			assertEquals(WEEKLY, info[1].getVariantID());
			assertEquals(SERVICE_NAME, info[1].getServiceName());
			assertEquals(SubscriptionState.unknown, info[1].getState());
			assertEquals(SERVICE_ID, info[2].getServiceID());
			assertEquals(MONTHLY, info[2].getVariantID());
			assertEquals(SERVICE_NAME, info[2].getServiceName());
			assertEquals(SubscriptionState.unknown, info[2].getState());

			assertEquals(SERVICE_ID, info[3].getServiceID());
			assertEquals(ONEMONTH, info[3].getVariantID());
			assertEquals(SERVICE_NAME, info[3].getServiceName());
			assertEquals(SubscriptionState.unknown, info[3].getState());
		}

		// Verify MSISDN A is not subscribed to Any
		{
			GetServicesRequest request = new GetServicesRequest();
			initialize(request);
			request.setActiveOnly(true);
			request.setSubscriberNumber(new Number(A_NUMBER));
			GetServicesResponse response = vasConnector.getServices(request);
			validate(request, response);
			VasServiceInfo[] info = filter(response.getServiceInfo());
			assertEquals(0, info.length);
		}

		// Attempt to unsubscribe MSISDN A even though he is not subscribed
		{
			UnsubscribeRequest request = new UnsubscribeRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			UnsubscribeResponse response = vasConnector.unsubscribe(request);
			assertEquals(ReturnCodes.notSubscribed, response.getReturnCode());
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Subscribe MSISDN A
		{
			SubscribeRequest request = new SubscribeRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			SubscribeResponse response = vasConnector.subscribe(request);
			validate(request, response);
			balanceA -= CHARGE_SUBSCRIBE;
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Verify MSISDN is Subscribed to ONEMONTH
		{
			GetServicesRequest request = new GetServicesRequest();
			initialize(request);
			request.setActiveOnly(true);
			request.setSubscriberNumber(new Number(A_NUMBER));
			GetServicesResponse response = vasConnector.getServices(request);
			validate(request, response);
			VasServiceInfo[] info = filter(response.getServiceInfo());
			assertEquals(1, info.length);
			assertEquals(SERVICE_ID, info[0].getServiceID());
			assertEquals(MONTHLY, info[0].getVariantID());
			assertEquals(SERVICE_NAME, info[0].getServiceName());
			assertEquals(SubscriptionState.active, info[0].getState());
		}

		// Attempt To Subscribe MSISDN A again
		{
			SubscribeRequest request = new SubscribeRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			SubscribeResponse response = vasConnector.subscribe(request);
			assertEquals(ReturnCodes.alreadySubscribed, response.getReturnCode());
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Unsubscribe MSISDN A
		{
			UnsubscribeRequest request = new UnsubscribeRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			UnsubscribeResponse response = vasConnector.unsubscribe(request);
			validate(request, response);
			balanceA -= CHARGE_USUBSCRIBE;
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Verify MSISDN A is not subscribed to Any
		{
			GetServicesRequest request = new GetServicesRequest();
			initialize(request);
			request.setActiveOnly(true);
			request.setSubscriberNumber(new Number(A_NUMBER));
			GetServicesResponse response = vasConnector.getServices(request);
			validate(request, response);
			VasServiceInfo[] info = filter(response.getServiceInfo());
			assertEquals(0, info.length);
		}

		// Subscribe MSISDN A Again
		{
			SubscribeRequest request = new SubscribeRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			SubscribeResponse response = vasConnector.subscribe(request);
			validate(request, response);
			balanceA -= CHARGE_SUBSCRIBE;
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Attempt to remove recipient MSISDN B1 even though it has not been added
		{
			RemoveMemberRequest request = new RemoveMemberRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER1));
			RemoveMemberResponse response = vasConnector.removeMember(request);
			assertEquals(ReturnCodes.notMember, response.getReturnCode());
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Add recipient MSISDN B1
		{
			AddMemberRequest request = new AddMemberRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(MONTHLY);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER1));
			AddMemberResponse response = vasConnector.addMember(request);
			validate(request, response);
			balanceA -= CHARGE_ADD_RECIPIENT;
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Get Available Transfers of MSISDN A/B1 - Expect 7
		{
			GetCreditTransfersRequest request = new GetCreditTransfersRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER1));
			request.setActiveOnly(false);
			GetCreditTransfersResponse response = vasConnector.getCreditTransfers(request);
			validate(request, response);
			assertEquals(7, response.getTransfers().length);
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Get Active Transfers of MSISDN B1 - Expect 0
		{
			GetCreditTransfersRequest request = new GetCreditTransfersRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER1));
			request.setActiveOnly(true);
			GetCreditTransfersResponse response = vasConnector.getCreditTransfers(request);
			validate(request, response);
			assertEquals(0, response.getTransfers().length);
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Register PIN
		{
			ChangePINRequest request = new ChangePINRequest();
			initialize(request);
			request.setServiceID("PIN");
			request.setVariantID("DEF");
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setOldPIN("1111");
			request.setNewPIN(PIN);
			vasConnector.changePIN(request);
		}

		// Eligibility Tests
		{
			testEligibility(vasConnector, subscriberA, subscriberA, balanceA, balanceB, subscriberB1);
			testEligibility(vasConnector, subscriberB1, subscriberA, balanceA, balanceB, subscriberB1);

		}

		// Test Transfer to Self
		{
			TransferRequest request = new TransferRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setTransferModeID(ONCEOFF_MODE);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setRecipientNumber(new Number(A_NUMBER));
			request.setAmount(50L);
			request.setPin(PIN);

			TransferResponse response = vasConnector.transfer(request);
			assertEquals(ReturnCodes.cannotTransferToSelf, response.getReturnCode());
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());

		}

		// Test once-off Transfer to B1
		{
			TransferRequest request = new TransferRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setTransferModeID(ONCEOFF_MODE);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setRecipientNumber(new Number(B_NUMBER1));
			request.setAmount(50L);
			request.setPin(PIN);

			TransferResponse response = vasConnector.transfer(request);
			validate(request, response);
			balanceA -= 61;
			balanceB += 50;
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Test once-off Transfer to B1 - Trigger Once per day limit
		{
			TransferRequest request = new TransferRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setTransferModeID(ONCEOFF_MODE);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setRecipientNumber(new Number(B_NUMBER1));
			request.setAmount(50L);
			request.setPin(PIN);

			TransferResponse response = vasConnector.transfer(request);
			assertEquals(ReturnCodes.maxCountExceeded, response.getReturnCode());
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Add Monthly Transfer for MSISDN B1
		{
			AddCreditTransferRequest request = new AddCreditTransferRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER1));
			request.setTransferMode(PERIODIC_MODE);
			request.setAmount(TRANSFER1);
			request.setTransferLimit(TRANSFER1);

			request.setPin(PIN);
			request.setNextTransferDate(DateTime.getNow().addSeconds(5));
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
			AddCreditTransferResponse response = vasConnector.addCreditTransfer(request);
			validate(request, response);
			balanceA -= CHARGE_ADD_TRANSFER;
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Get Active Transfers of MSISDN B1 Expect 1
		{
			GetCreditTransfersRequest request = new GetCreditTransfersRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER1));
			request.setActiveOnly(true);
			GetCreditTransfersResponse response = vasConnector.getCreditTransfers(request);
			validate(request, response);
			assertEquals(1, response.getTransfers().length);
			assertEquals(balanceB, (long) subscriberB1.getAccountValue1());
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Attempt to Add Monthly Transfer for MSISDN B1 again
		{
			AddCreditTransferRequest request = new AddCreditTransferRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER1));
			request.setTransferMode(PERIODIC_MODE);
			request.setAmount(100);
			request.setNextTransferDate(DateTime.getNow().addSeconds(5));
			request.setTransferLimit(100L);
			request.setPin(PIN);
			AddCreditTransferResponse response = vasConnector.addCreditTransfer(request);
			assertEquals(ReturnCodes.alreadyAdded, response.getReturnCode());
			assertEquals(balanceB, (long) subscriberB1.getAccountValue1());
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Perform Monthly Transfer for MSISDN B1
		{
			// Wait for Lifecycle to run
			long before = subscriberB1.getAccountValue1();
			long amount = TRANSFER1 - before;
			Thread.sleep(10000 * 1);
			balanceB += amount;
			balanceA -= amount + abCommissionAmount + (abCommissionPercent * amount + 5000) / 10000;
			assertEquals(balanceB, (long) subscriberB1.getAccountValue1());
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());

		}

		// Send First Warning
		{
			ISubscription lcy = lifecycle.getSubscription(db, aParty, SERVICE_ID, MONTHLY);
			assertNotNull(lcy);
			ProcessLifecycleEventRequest request = new ProcessLifecycleEventRequest(lcy);
			initialize(request);

			ProcessLifecycleEventResponse response = vasConnector.processLifecycleEvent(request);
			validate(request, response);
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Send Second Warning
		{
			ISubscription lcy = lifecycle.getSubscription(db, aParty, SERVICE_ID, MONTHLY);
			assertNotNull(lcy);
			ProcessLifecycleEventRequest request = new ProcessLifecycleEventRequest(lcy);
			initialize(request);

			ProcessLifecycleEventResponse response = vasConnector.processLifecycleEvent(request);
			validate(request, response);
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Renew Subscription
		{
			ISubscription lcy = lifecycle.getSubscription(db, aParty, SERVICE_ID, MONTHLY);
			assertNotNull(lcy);

			ProcessLifecycleEventRequest request = new ProcessLifecycleEventRequest(lcy);
			initialize(request);

			ProcessLifecycleEventResponse response = vasConnector.processLifecycleEvent(request);
			validate(request, response);
			balanceA -= CHARGE_RENEW;
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Remove Monthly Transfer for MSISDN B1
		{
			RemoveCreditTransfersRequest request = new RemoveCreditTransfersRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER1));
			request.setTransferMode(PERIODIC_MODE);
			RemoveCreditTransfersResponse response = vasConnector.removeCreditTransfers(request);
			validate(request, response);
			balanceA -= CHARGE_REMOVE_TRANSFER;
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Get Active Transfers of MSISDN B1 - Expect 0
		{
			GetCreditTransfersRequest request = new GetCreditTransfersRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER1));
			request.setActiveOnly(true);
			GetCreditTransfersResponse response = vasConnector.getCreditTransfers(request);
			validate(request, response);
			assertEquals(0, response.getTransfers().length);
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Add Threshold based transfer to Subscriber B1
		{
			AddCreditTransferRequest request = new AddCreditTransferRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER1));
			request.setTransferMode(THRESHOLD_MODE);
			request.setAmount(TRANSFER2);
			request.setTransferLimit(5000L);
			request.setTransferThreshold(12L);
			request.setPin(PIN);
			AddCreditTransferResponse response = vasConnector.addCreditTransfer(request);
			validate(request, response);
			balanceA -= CHARGE_ADD_TRANSFER;
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Suspend Threshold based transfer to Subscriber B1
		{
			SuspendCreditTransferRequest request = new SuspendCreditTransferRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER1));
			request.setTransferMode(THRESHOLD_MODE);
			SuspendCreditTransferResponse response = vasConnector.suspendCreditTransfer(request);
			validate(request, response);
			balanceA -= CHARGE_SUSPEND_TRANSFER;
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Resume Threshold based transfer to Subscriber B1
		{
			ResumeCreditTransferRequest request = new ResumeCreditTransferRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setSubscriberNumber(new Number(A_NUMBER));
			request.setMemberNumber(new Number(B_NUMBER1));
			request.setTransferMode(THRESHOLD_MODE);
			ResumeCreditTransferResponse response = vasConnector.resumeCreditTransfer(request);
			validate(request, response);
			balanceA -= CHARGE_RESUME_TRANSFER;
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		}

		// Create a Dedicated Account Dump Trigger Transfer for subscriber B
		{
			// Create DA2 for Subscriber B
			long daValue = 10L;
			DedicatedAccount da = new DedicatedAccount();
			da.setDedicatedAccountID(2);
			da.setDedicatedAccountUnitType(6);
			da.setDedicatedAccountValue1(daValue);
			airSimulator.updateDedicatedAccount(subscriberB1.getInternationalNumber(), da);

			// Create D/A Dump
			airSimulator.produceDedicatedAccountFile(DIRECTORY, PREFIX, "3.3");

			// Wait for transfer to complete
			Thread.sleep(10000L * 1);

			DedicatedAccount dataDA = subscriberB1.getDedicatedAccounts().get(2);
			assertEquals(TRANSFER2, (long) dataDA.getDedicatedAccountValue1());

			// Verify Result
			long amount = TRANSFER2 - daValue;
			long sourceAmount = (amount * mdrConversionRate + 320000L) / 640000L;
			balanceA -= sourceAmount + mdrCommissionAmount + (mdrCommissionPercent * sourceAmount + 5000) / 10000;

			assertEquals(balanceB, (long) subscriberB1.getAccountValue1());
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());

			da.setDedicatedAccountValue1(0L);
			airSimulator.updateDedicatedAccount(subscriberB1.getInternationalNumber(), da);

			Thread.sleep(2000 * 1);
		}

		// Trigger a Downwards TNP for subscriber B
		{
			balanceB = 5;
			long amount = TRANSFER2;

			// Simulate TNP
			TnpThreshold threshold = new TnpThreshold();
			threshold.setServiceClass(subscriberB1.getServiceClassCurrent());
			threshold.setThresholdID(5);
			threshold.setDirectory(DIRECTORY);
			threshold.setVersion("2.0");
			threshold.setSenderID("sdp1");
			threshold.setReceiverID("hxc5");
			threshold.setAccountID(0);
			threshold.setLevel(10);
			threshold.setUpwards(false);
			threshold.setTriggerType(TnpTriggerTypes.TRAFFIC);
			threshold.setAccountGroupID(12);
			boolean ok = airSimulator.addTnpThreshold(threshold);
			assertTrue(ok);

			// Trigger TNP
			airSimulator.setBalance(subscriberB1.getInternationalNumber(), balanceB);

			// Wait for transfer to complete
			Thread.sleep(10000L * 1);

			// Verify Result
			long sourceAmount = (amount * mdrConversionRate + 320000L) / 640000L;
			balanceA -= sourceAmount + mdrCommissionAmount + (mdrCommissionPercent * sourceAmount + 5000) / 10000;

			DedicatedAccount dataDA = subscriberB1.getDedicatedAccounts().get(2);
			assertEquals(amount, (long) dataDA.getDedicatedAccountValue1());

			assertEquals(balanceB, (long) subscriberB1.getAccountValue1());
			assertEquals(balanceA, (long) subscriberA.getAccountValue1());

			Thread.sleep(2000 * 1);
		}

	}

	private VasServiceInfo[] filter(VasServiceInfo[] serviceInfo)
	{
		if (serviceInfo == null || serviceInfo.length == 0)
			return serviceInfo;

		int toIndex = 0;

		for (int fromIndex = 0; fromIndex < serviceInfo.length; fromIndex++)
		{
			VasServiceInfo info = serviceInfo[fromIndex];
			if (info.getServiceID().equalsIgnoreCase(SERVICE_ID))
				serviceInfo[toIndex++] = info;
		}

		return toIndex == serviceInfo.length ? serviceInfo : java.util.Arrays.copyOf(serviceInfo, toIndex);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
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

	private void testEligibility(IHxC vasConnector, SubscriberEx subscriber, SubscriberEx subscriberA, long balanceA, long balanceB, SubscriberEx subscriberB1)
	{
		DateTime now = DateTime.getNow();
		DateTime yesterday = now.addDays(-1);
		DateTime tomorrow = now.addDays(1);

		// Test Supervision Expiry Date
		Date previous = subscriber.getSupervisionExpiryDate();
		subscriber.setSupervisionExpiryDate(yesterday);
		testNotEligible(vasConnector, balanceA, subscriberA, balanceB, subscriberB1);
		subscriber.setSupervisionExpiryDate(previous);

		// Test Service Fee Expiry Date
		previous = subscriber.getServiceFeeExpiryDate();
		subscriber.setServiceFeeExpiryDate(yesterday);
		testNotEligible(vasConnector, balanceA, subscriberA, balanceB, subscriberB1);
		subscriber.setServiceFeeExpiryDate(previous);

		// Test Service Removal Date
		previous = subscriber.getServiceRemovalDate();
		subscriber.setServiceRemovalDate(yesterday);
		testNotEligible(vasConnector, balanceA, subscriberA, balanceB, subscriberB1);
		subscriber.setServiceRemovalDate(previous);

		// Test Account Activated Flag
		Boolean flag = subscriber.getAccountActivatedFlag();
		subscriber.setAccountActivatedFlag(false);
		testNotEligible(vasConnector, balanceA, subscriberA, balanceB, subscriberB1);
		subscriber.setAccountActivatedFlag(flag);

		// Test Temporary Blocked Flag
		flag = subscriber.getTemporaryBlockedFlag();
		subscriber.setTemporaryBlockedFlag(true);
		testNotEligible(vasConnector, balanceA, subscriberA, balanceB, subscriberB1);
		subscriber.setTemporaryBlockedFlag(flag);

		// Test Refill Unbar Date
		previous = subscriber.getRefillUnbarDateTime();
		subscriber.setRefillUnbarDateTime(tomorrow);
		testNotEligible(vasConnector, balanceA, subscriberA, balanceB, subscriberB1);
		subscriber.setRefillUnbarDateTime(previous);

		// PSO Bit
		Map<Integer, ServiceOfferings> offerings = subscriber.getServiceOfferings();
		ServiceOfferings pso = new ServiceOfferings();
		pso.serviceOfferingActiveFlag = true;
		pso.serviceOfferingID = 3;
		offerings.put(pso.serviceOfferingID, pso);
		testNotEligible(vasConnector, balanceA, subscriberA, balanceB, subscriberB1);
		offerings.remove(pso.serviceOfferingID);
	}

	// Eligible
	private void testNotEligible(IHxC vasConnector, long balanceA, SubscriberEx subscriberA, long balanceB, SubscriberEx subscriberB1)
	{
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setTransferModeID(ONCEOFF_MODE);
		request.setSubscriberNumber(new Number(A_NUMBER));
		request.setRecipientNumber(new Number(B_NUMBER1));
		request.setAmount(50L);
		request.setPin(PIN);

		TransferResponse response = vasConnector.transfer(request);
		assertEquals(ReturnCodes.notEligible, response.getReturnCode());
		assertEquals(balanceA, (long) subscriberA.getAccountValue1());
		assertEquals(balanceB, (long) subscriberB1.getAccountValue1());
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
