package hxc.services.credittransfer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.ChangePINRequest;
import com.concurrent.hxc.ChangePINResponse;
import com.concurrent.hxc.GetServicesRequest;
import com.concurrent.hxc.GetServicesResponse;
import com.concurrent.hxc.IHxC;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.Number.NumberPlan;
import com.concurrent.hxc.RequestHeader;
import com.concurrent.hxc.ResponseHeader;
import com.concurrent.hxc.TransferRequest;
import com.concurrent.hxc.TransferResponse;
import com.concurrent.hxc.VasServiceInfo;

import hxc.configuration.ValidationException;
import hxc.connectors.Channels;
import hxc.connectors.air.AirConnector;
import hxc.connectors.air.AirConnector.AirConnectionConfig;
import hxc.connectors.air.AirConnector.AirConnectorConfig;
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.ctrl.CtrlConnector.CtrlConfiguration;
import hxc.connectors.ctrl.IServerInfo;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.lifecycle.LifecycleConnector;
import hxc.connectors.lifecycle.LifecycleConnector.LifecycleConfiguration;
import hxc.connectors.lifecycle.TemporalTrigger;
import hxc.connectors.smpp.SmppConnector;
import hxc.connectors.soap.ISoapConnector;
import hxc.connectors.soap.SoapConnector;
import hxc.servicebus.HostInfo;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.servicebus.ServiceBus;
import hxc.services.airsim.AirSim;
import hxc.services.airsim.model.SubscriberEx;
import hxc.services.airsim.protocol.DedicatedAccount;
import hxc.services.airsim.protocol.IAirSim;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.services.credittransfer.CreditTransferBase.CreditTransferConfig;
import hxc.services.notification.Texts;
import hxc.services.numberplan.INumberPlan;
import hxc.services.numberplan.NumberPlanService;
import hxc.services.pin.Pin;
import hxc.services.pin.PinService;
import hxc.services.pin.PinService.PinServiceConfig;
import hxc.services.pin.PinServiceTest;
import hxc.services.reporting.ReportingService;
import hxc.services.security.SecurityService;
import hxc.services.transactions.TransactionService;
import hxc.testsuite.RunAllTestsBase;

@SuppressWarnings("unused")
public class CreditTransferTest extends RunAllTestsBase
{
	final static Logger logger = LoggerFactory.getLogger(CreditTransferTest.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fileds
	//
	// /////////////////////////////////

	private long donorBalanceBEFORE = 0;
	private long recipientBalanceBEFORE = 0;
	private long expectedDonorBalanceAFTER = 0;
	private long actualDonorBalanceAFTER = 0;
	private long expectedRecipientBalanceAFTER = 0;
	private long actualRecipientBalanceAFTER = 0;

	private static IServiceBus esb;
	private static IAirSim airSimulator;
	private static ISoapConnector soapConnector;
	private static IHxC vasConnector;
	private static MySqlConnector mysqlConnector = null;

	private static DedicatedAccount dedicatedAccountSMS = new DedicatedAccount();
	private static DedicatedAccount dedicatedAccountDATA = new DedicatedAccount();
	private static DedicatedAccount dedicatedAccountVOICE = new DedicatedAccount();

	private static final int smsDedicatedAccountID = 5;
	private static final int dataDedicatedAccountID = 7;
	private static final int voiceDedicatedAccountID = 17;

	// private static final String VARIANT_ID = "CrXfr";
	private static final String VARIANT_ID = "DEF";
	private static final String SERVICE_ID = "CrXfr";
	private static final String SERVICE_NAME = "Credit Transfer";
	private static int languageID = 1;
	private static final String A_PIN = "2222";

	private static SubscriberEx subscriberA;
	private static SubscriberEx subscriberB;
	private static SubscriberEx specialSubscriber;

	// private static final String A_NUMBER = "23776012000";
	// private static final String A_NUMBER = "23786012005";
	private static final String A_NUMBER = "76012008";
	// private static final String A_NUMBER = "23786012009";
	private static final String B_NUMBER = "76012001";
	private static final String SPECIAL_NUMBER = "112";

	private static final int A_NUMBER_SC = 76;
	private static final int B_NUMBER_SC = 76;

	private static CreditTransfer service = null;
	private static CreditTransferConfig config = null;
	private static PinService pinService = null;
	private static INumberPlan numberPlan = null;

	// Set up a new variant

	private static long chargeScalingFactor = CreditTransferBase.getChargeScalingfactor();
	private static int[] serviceClassIDs = new int[] { 1 };

	private static String[] donorQuotaList = { "Daily", "Weekly", "Monthly" };

	private static TransactionCharge transactionChargeBands[] = { new TransactionCharge(0 * chargeScalingFactor, 100 * chargeScalingFactor, 10 * chargeScalingFactor, 0, serviceClassIDs),
			new TransactionCharge(100 * chargeScalingFactor, 1000 * chargeScalingFactor, 10 * chargeScalingFactor, 1500, serviceClassIDs),
			new TransactionCharge(1000 * chargeScalingFactor, 10000 * chargeScalingFactor, 12 * chargeScalingFactor, 800, serviceClassIDs),
			new TransactionCharge(10000 * chargeScalingFactor, 9999999 * chargeScalingFactor, 14 * chargeScalingFactor, 500, serviceClassIDs) };

	private static CreditTransferVariant testVariant = new CreditTransferVariant("testVariant", // variantID
			0, // ussdID
			new Texts("Main to Main", "Main aux Main", "Main to Main"), // name
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
			new int[] { 1 }, // service classes
			new int[] { 1 }, // service classes
			donorQuotaList, // donor quota
			transactionChargeBands, new CumulativeLimits(1000, 10000, 100000), new CumulativeLimits(1000, 10000, 100000));

	// ///////////////////////////////////////////////////////////////////////////////
	//
	// Getters and Setters
	//
	// //////////////////////////////////////////

	@SuppressWarnings("serial")
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
		esb.registerService(new CreditTransfer());
		esb.registerService(new PinService());

		NumberPlanService numberPlanService = new NumberPlanService();
		esb.registerService(numberPlanService);

		AirConnector air = new AirConnector();
		AirConnectorConfig airConfig = (AirConnectorConfig) air.getConfiguration();
		AirConnectionConfig conConfig = (AirConnectionConfig) airConfig.getConfigurations().iterator().next();
		conConfig.setUri("http://127.0.0.1:10011/Air");
		esb.registerConnector(air);

		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerConnector(new SmppConnector());
		esb.registerService(new SecurityService());
		esb.registerConnector(new SoapConnector());
		CtrlConnector control = new CtrlConnector();
		CtrlConfiguration controlConfig = (CtrlConfiguration) control.getConfiguration();
		controlConfig.setWatchdogIntervalSeconds(5);
		control.setConfiguration(controlConfig);
		esb.registerConnector(control);
		LifecycleConnector lfcycle = new LifecycleConnector();
		LifecycleConfiguration configurationLife = (LifecycleConfiguration) lfcycle.getConfiguration();
		configurationLife.setPollingIntervalSeconds(5);
		lfcycle.setConfiguration(configurationLife);
		esb.registerConnector(lfcycle);
		esb.registerService(new ReportingService());

		boolean started = esb.start(null);
		assert (started);

		control.setServerList(new IServerInfo[] { new IServerInfo()
		{
			@Override
			public String getTransactionNumberPrefix()
			{
				return "00";
			}

			@Override
			public String getServerHost()
			{
				return HostInfo.getName();
			}

			@Override
			public String getPeerHost()
			{
				return HostInfo.getName();
			}
		} });

		// Get the VAS Soap Interface
		soapConnector = esb.getFirstConnector(ISoapConnector.class);
		Assert.assertNotNull("SOAP Connector is NULL!! Connector not starting !", soapConnector);
		vasConnector = soapConnector.getVasInterface();

		// Setup Config
		service = esb.getFirstService(CreditTransfer.class);
		Assert.assertNotNull("CreditTransfer service reference is NULL!!!, not starting !", service);
		config = (CreditTransferConfig) service.getConfiguration();

		// PinService
		pinService = esb.getFirstService(PinService.class);
		Assert.assertNotNull("PinService reference is NULL!!!, not starting !", pinService);

		// Setup Simulator
		numberPlan = esb.getFirstService(INumberPlan.class);
		Assert.assertNotNull("NumberPlan service reference is NULL!!!, not starting !", numberPlan);

		// Register PINs
		mysqlConnector = esb.getFirstConnector(MySqlConnector.class);
		String internationalNumber = numberPlan.getInternationalFormat(A_NUMBER);
		PinServiceTest.insertPin(new Pin(internationalNumber, "PIN", "DEF", "2222", 0, false, new Date()), mysqlConnector);
		// PinServiceTest.insertPin( new Pin(internationalNumber, "PIN", "CrXfr", "2222", 0, false, new Date(), logger), mysqlConnector );

		internationalNumber = numberPlan.getInternationalFormat(SPECIAL_NUMBER);
		PinServiceTest.insertPin(new Pin(internationalNumber, "PIN", "DEF", "2222", 0, false, new Date()), mysqlConnector);
		// PinServiceTest.insertPin( new Pin(internationalNumber, "PIN", "CrXfr", "2222", 0, false, new Date(), logger), mysqlConnector );

		// AIR simulator
		airSimulator = new AirSim(esb, 10011, "/Air", numberPlan, "CFR");
		Assert.assertTrue(airSimulator.start());

		// Create subscribers on AIR
		long initialDonorBalance = 50000L;
		long initialRecipientBalance = 200L;
		subscriberA = (SubscriberEx) airSimulator.addSubscriber(A_NUMBER, languageID, A_NUMBER_SC, initialDonorBalance, SubscriberState.active);
		subscriberA.setPinCode(A_PIN);
		subscriberB = (SubscriberEx) airSimulator.addSubscriber(B_NUMBER, languageID, B_NUMBER_SC, initialRecipientBalance, SubscriberState.active);
		specialSubscriber = (SubscriberEx) airSimulator.addSubscriber(SPECIAL_NUMBER, languageID, A_NUMBER_SC, initialDonorBalance, SubscriberState.active);

		// DA setup
		dedicatedAccountVOICE.setDedicatedAccountUnitType(0);// 0==time
		dedicatedAccountVOICE.setDedicatedAccountID(voiceDedicatedAccountID);

		dedicatedAccountSMS.setDedicatedAccountUnitType(6); // 6==volume
		dedicatedAccountSMS.setDedicatedAccountID(smsDedicatedAccountID);

		dedicatedAccountDATA.setDedicatedAccountUnitType(6); // 6==volume
		dedicatedAccountDATA.setDedicatedAccountID(dataDedicatedAccountID);

		// ////////////////////////////////////////////////////////////////////////////////////
		// Create new Variant for Credit Transfer to simulate a client-specific requirement
		// ////////////////////////////////////////////////////////////////////////////////////

		// PinServiceConfig pinServiceConfig = (PinServiceConfig)pinService.getConfiguration();
		// hxc.services.pin.Variant[] variants = pinServiceConfig.getVariants();
		// hxc.services.pin.Variant newVariant = new hxc.services.pin.Variant(
		// "CrXfr", "EN: Credit Transfer", "FR: Credit Transfer", null, null, 3, 4, 6, "1111", false);
		// hxc.services.pin.Variant[] newArray = new hxc.services.pin.Variant[variants.length + 1];
		// //concatenate two arrays
		// System.arraycopy(variants, 0, newArray, 0, variants.length);
		// System.arraycopy(new hxc.services.pin.Variant[]{newVariant}, 0, newArray, variants.length, 1);
		// pinServiceConfig.setVariants( newArray );

	}// setup()

	@AfterClass
	public static void teardown()
	{
		if (airSimulator != null)
			airSimulator.stop();
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Test Credit Transfer
	//
	// /////////////////////////////////
	@Test
	public void testServices() throws ValidationException, IOException, SQLException, InterruptedException
	{

		// Get All Services
		GetServicesRequest request = new GetServicesRequest();
		initialize(request);
		request.setActiveOnly(false);
		GetServicesResponse response = vasConnector.getServices(request);
		validate(ReturnCodes.success, request, response);
		VasServiceInfo[] info = response.getServiceInfo();
		// Assert.assertEquals(3, info.length);
		// Assert.assertEquals("CrXfr", info[0].getServiceID());
		// Assert.assertEquals("PIN", info[1].getServiceID());
	}

	// ================================================================================================//

	// @Ignore
	@Test
	public void testMainToMainCumulativeMonthlyQuotaReached()
	{
		long transferAmount = 200L;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("mainToMain");
		request.setAmount(transferAmount);

		// /////////////////////////////////////////////////////////////////////////////////////
		// DA setup (MAIN)
		long dedicatedAccountValue1 = 5000L, dedicatedAccountValue2 = dedicatedAccountValue1;

		// /////////////////////////////////////////////////////////////////////////////////////

		// specify donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setPin(A_PIN);

		// Ensure donor got enough money on AIR and is in right SC
		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// //////////////////////////////////////////////////////
		//
		// MANUAL calculations: Initial balances
		//
		// //////////////////////////////////////////////////////

		// Capture balances on AIR before transaction starts
		// These pre-balances will be needed to validate what the AUT is about to do (i.e. transfer)
		captureDonorBalanceBEFORE(subscriberA, 0);
		captureRecipientBalanceBEFORE(subscriberB, 0);

		// //////////////////////////////////////////////////////
		//
		// AUT Calculations: transfer process
		//
		// //////////////////////////////////////////////////////

		String variantID = request.getVariantID();
		CreditTransferVariant variant = service.getVariant(variantID);

		// Cumulative Donor Limit

		updateUsageCounters(A_NUMBER, 0, 0, 0);
		long monthlyLimit = variant.getCumulativeDonorLimits().getTotalMonthlyLimit();
		this.updateCumulativeCounters(A_NUMBER, 0, 0, monthlyLimit);

		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.quotaReached, request, response);

		// Reset Donor
		updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);

		// Cumulative Recipient Limit

		monthlyLimit = variant.getCumulativeRecipientLimits().getTotalMonthlyLimit();
		this.updateCumulativeCounters(B_NUMBER, 0, 0, monthlyLimit);
		TransferResponse response2 = vasConnector.transfer(request);
		validate(ReturnCodes.cannotReceiveCredit, request, response2);

		// Reset Recipient
		updateUsageCounters(B_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(B_NUMBER, 0, 0, 0);

	}// testMainToMainCumulativeMonthlyQuotaReached()

	// ================================================================================================//

	// @Ignore
	@Test
	public void testMainToMainCumulativeWeeklyQuotaReached()
	{
		long transferAmount = 200L;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("mainToMain");
		request.setAmount(transferAmount);

		// /////////////////////////////////////////////////////////////////////////////////////
		// DA setup (MAIN)
		long dedicatedAccountValue1 = 5000L, dedicatedAccountValue2 = dedicatedAccountValue1;

		// /////////////////////////////////////////////////////////////////////////////////////

		// specify donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setPin(A_PIN);

		// Ensure donor got enough money on AIR and is in right SC
		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// //////////////////////////////////////////////////////
		//
		// MANUAL calculations: Initial balances
		//
		// //////////////////////////////////////////////////////

		// Capture balances on AIR before transaction starts
		// These pre-balances will be needed to validate what the AUT is about to do (i.e. transfer)
		captureDonorBalanceBEFORE(subscriberA, 0);
		captureRecipientBalanceBEFORE(subscriberB, 0);

		// //////////////////////////////////////////////////////
		//
		// AUT Calculations: transfer process
		//
		// //////////////////////////////////////////////////////

		String variantID = request.getVariantID();
		CreditTransferVariant variant = service.getVariant(variantID);

		// Cumulative Donor Limit

		updateUsageCounters(A_NUMBER, 0, 0, 0);
		long weeklyLimit = variant.getCumulativeDonorLimits().getTotalWeeklyLimit();
		this.updateCumulativeCounters(A_NUMBER, 0, weeklyLimit, 0);

		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.quotaReached, request, response);

		// Reset Donor
		updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);

		// Cumulative Recipient Limit

		updateUsageCounters(B_NUMBER, 0, 0, 0);
		weeklyLimit = variant.getCumulativeRecipientLimits().getTotalWeeklyLimit();
		this.updateCumulativeCounters(B_NUMBER, 0, weeklyLimit, 0);

		TransferResponse response2 = vasConnector.transfer(request);
		validate(ReturnCodes.cannotReceiveCredit, request, response2);

		// Reset Recipient
		updateUsageCounters(B_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(B_NUMBER, 0, 0, 0);

	}// testMainToMainCumulativeWeeklyQuotaReached()

	// ================================================================================================//

	// @Ignore
	@Test
	public void testMainToMainCumulativeDailyQuotaReached()
	{
		long transferAmount = 200L;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("mainToMain");
		request.setAmount(transferAmount);

		// /////////////////////////////////////////////////////////////////////////////////////
		// DA setup (MAIN)
		long dedicatedAccountValue1 = 5000L, dedicatedAccountValue2 = dedicatedAccountValue1;

		// /////////////////////////////////////////////////////////////////////////////////////

		// specify donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setPin(A_PIN);

		// Ensure donor got enough money on AIR and is in right SC
		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// //////////////////////////////////////////////////////
		//
		// MANUAL calculations: Initial balances
		//
		// //////////////////////////////////////////////////////

		// Capture balances on AIR before transaction starts
		// These pre-balances will be needed to validate what the AUT is about to do (i.e. transfer)
		captureDonorBalanceBEFORE(subscriberA, 0);
		captureRecipientBalanceBEFORE(subscriberB, 0);

		// //////////////////////////////////////////////////////
		//
		// AUT Calculations: transfer process
		//
		// //////////////////////////////////////////////////////

		String variantID = request.getVariantID();
		CreditTransferVariant variant = service.getVariant(variantID);

		// Cumulative Donor Limit

		updateUsageCounters(A_NUMBER, 0, 0, 0);
		long dailyLimit = variant.getCumulativeDonorLimits().getTotalDailyLimit();
		this.updateCumulativeCounters(A_NUMBER, dailyLimit, 0, 0);

		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.quotaReached, request, response);

		// Reset Donor
		updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);

		// Cumulative Recipient Limit
		updateUsageCounters(B_NUMBER, 0, 0, 0);
		dailyLimit = variant.getCumulativeRecipientLimits().getTotalDailyLimit();
		this.updateCumulativeCounters(B_NUMBER, dailyLimit, 0, 0);

		TransferResponse response2 = vasConnector.transfer(request);
		validate(ReturnCodes.cannotReceiveCredit, request, response2);

		// Reset Recipient
		updateUsageCounters(B_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(B_NUMBER, 0, 0, 0);

	}// testMainToMainCumulativeQuotaReached()

	// ================================================================================================//

	// SUCCESSFUL Credit Transfer (main to main acct)
	// @Ignore
	@Test
	public void testSuccessfulMainToMainTopup()
	{
		long transferAmount = 200L;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("mainToMain");
		request.setAmount(transferAmount);

		// /////////////////////////////////////////////////////////////////////////////////////
		// DA setup (MAIN)
		long dedicatedAccountValue1 = 5000L, dedicatedAccountValue2 = dedicatedAccountValue1;

		// /////////////////////////////////////////////////////////////////////////////////////

		// specify donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setPin(A_PIN);

		// Ensure donor got enough money on AIR and is in right SC
		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// //////////////////////////////////////////////////////
		//
		// MANUAL calculations: Initial balances
		//
		// //////////////////////////////////////////////////////

		// Capture balances on AIR before transaction starts
		// These pre-balances will be needed to validate what the AUT is about to do (i.e. transfer)
		captureDonorBalanceBEFORE(subscriberA, 0);
		captureRecipientBalanceBEFORE(subscriberB, 0);

		// //////////////////////////////////////////////////////
		//
		// AUT Calculations: transfer process
		//
		// //////////////////////////////////////////////////////

		updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.success, request, response);

		// //////////////////////////////////////////////////////
		//
		// MANUAL calculations and verification: AFTER
		//
		// //////////////////////////////////////////////////////
		// Following method simulates transaction by calculating EXPECTED donor and recipient balances
		// CreditTransferVariant variant = config.super().getVariant( request.getVariantID() );
		CreditTransferVariant variant = service.getVariant(request.getVariantID());
		simulateDebitsAndCredits(transferAmount, variant, 76);

		Assert.assertEquals(expectedDonorBalanceAFTER, subscriberA.getAccountValue1().longValue());
		Assert.assertEquals(expectedRecipientBalanceAFTER, subscriberB.getAccountValue1().longValue());

	}// testSuccessfulMain2MainTopup()

	// ================================================================================================//

	// SUCCESSFUL Credit Transfer (main to main acct)
	// @Ignore
	@Test
	public void testInvalidMsisdnA()
	{
		String INVALID_A_NUMBER = "-" + A_NUMBER;
		long transferAmount = 200L;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("mainToMain");
		request.setAmount(transferAmount);

		// /////////////////////////////////////////////////////////////////////////////////////
		// DA setup (MAIN)
		long dedicatedAccountValue1 = 5000L, dedicatedAccountValue2 = dedicatedAccountValue1;

		// /////////////////////////////////////////////////////////////////////////////////////

		// specify donor MSISDN
		Number A_number = new Number(INVALID_A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setPin(A_PIN);

		// Ensure donor got enough money on AIR and is in right SC
		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// capture initial conditions
		long donorBalanceBEFORE = subscriberA.getAccountValue1();
		long recipientBalanceBEFORE = subscriberB.getAccountValue1();

		// //////////////////////////////////////////////////////
		// Activate transfer

		updateUsageCounters(INVALID_A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(INVALID_A_NUMBER, 0, 0, 0);
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.invalidNumber, request, response);

		// //////////////////////////////////////////////////////

	}// testInvalidMsisdnA()

	// ================================================================================================//

	// SUCCESSFUL Credit Transfer (main to main acct)
	// @Ignore
	@Test
	public void testInvalidMsisdnB()
	{
		String INVALID_B_NUMBER = "x" + B_NUMBER;
		long transferAmount = 200L;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("mainToMain");
		request.setAmount(transferAmount);

		// /////////////////////////////////////////////////////////////////////////////////////
		// DA setup (MAIN)
		long dedicatedAccountValue1 = 5000L, dedicatedAccountValue2 = dedicatedAccountValue1;

		// /////////////////////////////////////////////////////////////////////////////////////

		// specify donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(INVALID_B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setPin(A_PIN);

		// Ensure donor got enough money on AIR and is in right SC
		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// capture initial conditions
		long donorBalanceBEFORE = subscriberA.getAccountValue1();
		long recipientBalanceBEFORE = subscriberB.getAccountValue1();

		// //////////////////////////////////////////////////////
		// Activate transfer

		this.updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.invalidNumber, request, response);

		// //////////////////////////////////////////////////////

	}// testInvalidMsisdnB()

	// ================================================================================================//

	// @Ignore
	@Test
	public void testSuccessfulMainToSMS()
	{
		long transferAmount = 100;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("mainToSms");
		request.setAmount(transferAmount);

		// /////////////////////////////////////////////////////////////////////////////////////
		// MAIN account setup
		long donorAccountValue1 = 5000L, donorAccountValue2 = donorAccountValue1;
		try
		{
			subscriberA.setAccountValue1(donorAccountValue1);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		// /////////////////////////////////////////////////////////////////////////////////////
		// DA setup (B_NUMBER SMS)
		long smsDedicatedAccountValue1 = 100;
		dedicatedAccountSMS.setDedicatedAccountID(smsDedicatedAccountID);
		dedicatedAccountSMS.setDedicatedAccountValue1(smsDedicatedAccountValue1);
		dedicatedAccountSMS.setDedicatedAccountValue2(smsDedicatedAccountValue1);
		airSimulator.updateDedicatedAccount(B_NUMBER, dedicatedAccountSMS);

		// /////////////////////////////////////////////////////////////////////////////////////

		// specify donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setPin(A_PIN);

		// Set up service classes
		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// //////////////////////////////////////////////////////
		//
		// MANUAL calculations: Initial balances
		//
		// //////////////////////////////////////////////////////

		// Capture balances on AIR before transaction starts
		// These pre-balances will be needed to validate what the AUT is about to do (i.e. transfer)
		captureDonorBalanceBEFORE(subscriberA, 0);
		captureRecipientBalanceBEFORE(subscriberB, smsDedicatedAccountID);

		// //////////////////////////////////////////////////////
		//
		// AUT Calculations: transfer process
		//
		// //////////////////////////////////////////////////////

		updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.success, request, response);

		// //////////////////////////////////////////////////////
		//
		// MANUAL calculations and verification: AFTER
		//
		// //////////////////////////////////////////////////////

		// Following method simulates transaction by calculating EXPECTED donor and recipient balances
		CreditTransferVariant variant = service.getVariant(request.getVariantID());
		simulateDebitsAndCredits(transferAmount, variant, 76);

		// Validate DEBIT (from MAIN account)
		this.actualDonorBalanceAFTER = subscriberA.getAccountValue1().longValue();
		Assert.assertEquals(expectedDonorBalanceAFTER, actualDonorBalanceAFTER);

		// Validate CREDIT (into the SMS DA)
		this.actualRecipientBalanceAFTER = subscriberB.getDedicatedAccounts().get(Integer.valueOf(smsDedicatedAccountID)).getDedicatedAccountValue1();
		Assert.assertEquals(expectedRecipientBalanceAFTER, actualRecipientBalanceAFTER);

	}// testSuccessfulMainToSMS()

	// ================================================================================================//

	// @Ignore
	@Test
	public void quotaReached()
	{
		long transferAmount = 100;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("mainToSms");
		request.setAmount(transferAmount);

		// /////////////////////////////////////////////////////////////////////////////////////
		// MAIN account setup
		long donorAccountValue1 = 5000L, donorAccountValue2 = donorAccountValue1;
		try
		{
			subscriberA.setAccountValue1(donorAccountValue1);
		}
		catch (Exception e1)
		{
			logger.error(e1.getMessage(), e1);
		}

		// /////////////////////////////////////////////////////////////////////////////////////
		// DA setup (B_NUMBER SMS)
		long smsDedicatedAccountValue1 = 100;
		dedicatedAccountSMS.setDedicatedAccountID(smsDedicatedAccountID);
		dedicatedAccountSMS.setDedicatedAccountValue1(smsDedicatedAccountValue1);
		dedicatedAccountSMS.setDedicatedAccountValue2(smsDedicatedAccountValue1);
		airSimulator.updateDedicatedAccount(B_NUMBER, dedicatedAccountSMS);

		// /////////////////////////////////////////////////////////////////////////////////////

		// specify donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		// Make sure PIN is OK (valid and not blocked)
		request.setPin(A_PIN);

		String serviceID = "PIN";
		String internationalMSISDN = numberPlan.getInternationalFormat(A_NUMBER);

		try (IDatabaseConnection dbConnection = mysqlConnector.getConnection(null))
		{
			Pin pin = dbConnection.select(Pin.class, "where msisdn = %s and serviceId = %s and variantID = %s", internationalMSISDN, serviceID, VARIANT_ID);
			if (pin != null)
			{
				pin.setFailedCount(0);
				pin.setBlocked(false);
			}
			else
			{
				throw new SQLException("Pin not found for MSISDN [" + internationalMSISDN + "] serviceID [" + serviceID + "] variantID [" + VARIANT_ID + "]");
			}

			dbConnection.update(pin);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		// Set up service classes
		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// //////////////////////////////////////////////////////
		// Activate transfer
		/*
		 * //TODO: Should get these values from the variant int dailyLimit = config.getQuota().getDailyQuota().getLimit(); int weeklyLimit = config.getQuota().getWeeklyQuota().getLimit(); int
		 * monthlyLimit = config.getQuota().getMonthlyQuota().getLimit();
		 */
		CreditTransferVariant variant = service.getVariant(request.getVariantID());

		try
		{
			Quota dailyQuota = config.getDonorQuota(variant.getDonorQuotas(), QuotaPeriodUnits.DAY);
			int dailyLimit = dailyQuota.getLimit();

			// Set daily limit and test quota reached

			updateUsageCounters(A_NUMBER, dailyLimit, 0, 0);
			// this.updateCumulativeCounters(A_NUMBER, , 0, 0);

			TransferResponse response = vasConnector.transfer(request);
			validate(ReturnCodes.quotaReached, request, response);

			Quota weeklyQuota = config.getDonorQuota(variant.getDonorQuotas(), QuotaPeriodUnits.WEEK);
			int weeklyLimit = weeklyQuota.getLimit();

			// Set weekly limit and test quota reached

			updateUsageCounters(A_NUMBER, 0, weeklyLimit, 0);

			TransferResponse response2 = vasConnector.transfer(request);
			validate(ReturnCodes.quotaReached, request, response2);

			Quota monthlyQuota = config.getDonorQuota(variant.getDonorQuotas(), QuotaPeriodUnits.MONTH);
			int monthlyLimit = monthlyQuota.getLimit();

			// Set monthly limit and test quota reached

			updateUsageCounters(A_NUMBER, 0, 0, monthlyLimit);

			TransferResponse response3 = vasConnector.transfer(request);
			validate(ReturnCodes.quotaReached, request, response3);

		}
		catch (Exception e)
		{
			// Catches null pointer exception thrown if quota is not found for the specified period
		}

	}// testQuotaReached()

	// ================================================================================================//

	// @Ignore
	@Test
	public void testTransferToSelf()
	{
		long transferAmount = 100;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("mainToSms");
		request.setAmount(transferAmount);

		// /////////////////////////////////////////////////////////////////////////////////////
		// MAIN account setup
		long donorAccountValue1 = 5000L, donorAccountValue2 = donorAccountValue1;
		try
		{
			subscriberA.setAccountValue1(donorAccountValue1);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		// /////////////////////////////////////////////////////////////////////////////////////
		// DA setup (B_NUMBER SMS)
		long smsDedicatedAccountValue1 = 100;
		dedicatedAccountSMS.setDedicatedAccountID(smsDedicatedAccountID);
		dedicatedAccountSMS.setDedicatedAccountValue1(smsDedicatedAccountValue1);
		dedicatedAccountSMS.setDedicatedAccountValue2(smsDedicatedAccountValue1);
		airSimulator.updateDedicatedAccount(B_NUMBER, dedicatedAccountSMS);

		// /////////////////////////////////////////////////////////////////////////////////////

		// specify donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(A_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setPin(A_PIN);

		// Set up service classes
		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// Capture initial balances for donor and recipient
		long donorBalanceBEFORE = subscriberA.getAccountValue1();
		DedicatedAccount recipientDA = subscriberB.getDedicatedAccounts().get(Integer.valueOf(smsDedicatedAccountID));
		long recipientBalanceBEFORE = recipientDA.getDedicatedAccountValue1();

		// //////////////////////////////////////////////////////
		// Activate transfer

		this.updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.cannotTransferToSelf, request, response);

		// //////////////////////////////////////////////////////

		// Validate debit (MAIN acct)
		long donorBalanceAFTER = subscriberA.getAccountValue1();
		long expectedDonorBalanceAFTER = donorBalanceBEFORE;
		Assert.assertEquals(expectedDonorBalanceAFTER, donorBalanceAFTER);

		// Validate CREDIT (DA)
		long expectedRecipientBalanceAFTER = recipientBalanceBEFORE;
		long actualRecipientBalanceAFTER = recipientDA.getDedicatedAccountValue1();
		Assert.assertEquals(expectedRecipientBalanceAFTER, actualRecipientBalanceAFTER);
	}// testTransferToSelf()

	// ================================================================================================//

	// @Ignore
	@Test
	public void testTransferAmountTooSmall()
	{
		long transferAmount = 1;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("mainToSms");
		request.setAmount(transferAmount);

		// /////////////////////////////////////////////////////////////////////////////////////
		// MAIN account setup
		long donorAccountValue1 = 5000L, donorAccountValue2 = donorAccountValue1;
		try
		{
			subscriberA.setAccountValue1(donorAccountValue1);
		}
		catch (Exception e1)
		{
			logger.error(e1.getMessage(), e1);
		}

		// /////////////////////////////////////////////////////////////////////////////////////
		// DA setup (B_NUMBER SMS)
		long smsDedicatedAccountValue1 = 100;
		dedicatedAccountSMS.setDedicatedAccountID(smsDedicatedAccountID);
		dedicatedAccountSMS.setDedicatedAccountValue1(smsDedicatedAccountValue1);
		dedicatedAccountSMS.setDedicatedAccountValue2(smsDedicatedAccountValue1);
		airSimulator.updateDedicatedAccount(B_NUMBER, dedicatedAccountSMS);

		// /////////////////////////////////////////////////////////////////////////////////////

		// Donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// Recipient MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		// Sort out PIN
		request.setPin(A_PIN);
		String internationalMSISDN = numberPlan.getInternationalFormat(A_NUMBER);

		try (IDatabaseConnection dbConnection = mysqlConnector.getConnection(null))
		{
			Pin pin = dbConnection.select(Pin.class, "where msisdn = %s and serviceId = %s and variantID = %s", internationalMSISDN, "PIN", VARIANT_ID);
			if (pin != null)
			{
				pin.setFailedCount(0);
				pin.setBlocked(false);
			}
			else
			{
				throw new SQLException("Pin not found for MSISDN [%s]", internationalMSISDN);
			}

			dbConnection.update(pin);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		// Set up service classes
		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// Capture initial balances for donor and recipient
		long donorBalanceBEFORE = subscriberA.getAccountValue1();
		DedicatedAccount recipientDA = subscriberB.getDedicatedAccounts().get(Integer.valueOf(smsDedicatedAccountID));
		long recipientBalanceBEFORE = recipientDA.getDedicatedAccountValue1();

		// //////////////////////////////////////////////////////
		// Activate transfer

		this.updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.quantityTooSmall, request, response);

		// //////////////////////////////////////////////////////

		// Validate debit (MAIN acct)
		long donorBalanceAFTER = subscriberA.getAccountValue1();
		long expectedDonorBalanceAFTER = donorBalanceBEFORE;
		Assert.assertEquals(expectedDonorBalanceAFTER, donorBalanceAFTER);

		// Validate CREDIT (DA)
		long expectedRecipientBalanceAFTER = recipientBalanceBEFORE;
		long actualRecipientBalanceAFTER = recipientDA.getDedicatedAccountValue1();
		Assert.assertEquals(expectedRecipientBalanceAFTER, actualRecipientBalanceAFTER);
	}// testTransferAmountTooSmall()

	// ================================================================================================//

	// @Ignore
	@Test
	public void testTransferAmountTooBig()
	{
		long transferAmount = 99999999;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("mainToSms");
		request.setAmount(transferAmount);

		// /////////////////////////////////////////////////////////////////////////////////////
		// MAIN account setup
		long donorAccountValue1 = 5000L, donorAccountValue2 = donorAccountValue1;
		try
		{
			subscriberA.setAccountValue1(donorAccountValue1);
		}
		catch (Exception e1)
		{
			logger.error(e1.getMessage(), e1);
		}

		// /////////////////////////////////////////////////////////////////////////////////////
		// DA setup (B_NUMBER SMS)
		long smsDedicatedAccountValue1 = 100;
		dedicatedAccountSMS.setDedicatedAccountID(smsDedicatedAccountID);
		dedicatedAccountSMS.setDedicatedAccountValue1(smsDedicatedAccountValue1);
		dedicatedAccountSMS.setDedicatedAccountValue2(smsDedicatedAccountValue1);
		airSimulator.updateDedicatedAccount(B_NUMBER, dedicatedAccountSMS);

		// /////////////////////////////////////////////////////////////////////////////////////

		// Donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// Recipient MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setPin(A_PIN);
		String internationalMSISDN = numberPlan.getInternationalFormat(A_NUMBER);

		try (IDatabaseConnection dbConnection = mysqlConnector.getConnection(null))
		{
			Pin pin = dbConnection.select(Pin.class, "where msisdn = %s and serviceId = %s and variantID = %s", internationalMSISDN, "PIN", VARIANT_ID);
			if (pin != null)
			{
				pin.setFailedCount(0);
				pin.setBlocked(false);
			}
			else
			{
				throw new SQLException("Pin not found for MSISDN [%s]", internationalMSISDN);
			}

			dbConnection.update(pin);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		// Set up service classes
		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// Capture initial balances for donor and recipient
		long donorBalanceBEFORE = subscriberA.getAccountValue1();
		DedicatedAccount recipientDA = subscriberB.getDedicatedAccounts().get(Integer.valueOf(smsDedicatedAccountID));
		long recipientBalanceBEFORE = recipientDA.getDedicatedAccountValue1();

		// //////////////////////////////////////////////////////
		// Activate transfer

		this.updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.quantityTooBig, request, response);

		// //////////////////////////////////////////////////////

		// Validate debit (MAIN acct)
		long donorBalanceAFTER = subscriberA.getAccountValue1();
		long expectedDonorBalanceAFTER = donorBalanceBEFORE;
		Assert.assertEquals(expectedDonorBalanceAFTER, donorBalanceAFTER);

		// Validate CREDIT (DA)
		long expectedRecipientBalanceAFTER = recipientBalanceBEFORE;
		long actualRecipientBalanceAFTER = recipientDA.getDedicatedAccountValue1();
		Assert.assertEquals(expectedRecipientBalanceAFTER, actualRecipientBalanceAFTER);
	}// testTransferAmountTooBig()

	// ================================================================================================//

	// @Ignore
	@Test
	public void testDAnotFound()
	{
		long transferAmount = 200;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("mainToSms");
		request.setAmount(transferAmount);

		// /////////////////////////////////////////////////////////////////////////////////////
		// MAIN account setup
		long donorAccountValue1 = 5000L, donorAccountValue2 = donorAccountValue1;
		try
		{
			subscriberA.setAccountValue1(donorAccountValue1);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		// /////////////////////////////////////////////////////////////////////////////////////
		// DA setup (B_NUMBER SMS)
		long smsDedicatedAccountValue1 = 100;
		dedicatedAccountSMS.setDedicatedAccountID(smsDedicatedAccountID);
		dedicatedAccountSMS.setDedicatedAccountValue1(smsDedicatedAccountValue1);
		dedicatedAccountSMS.setDedicatedAccountValue2(smsDedicatedAccountValue1);
		airSimulator.updateDedicatedAccount(B_NUMBER, dedicatedAccountSMS);

		airSimulator.deleteDedicatedAccount(B_NUMBER, smsDedicatedAccountID);

		// /////////////////////////////////////////////////////////////////////////////////////

		// Donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// Recipient MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setPin(A_PIN);

		// Set up service classes
		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// Capture initial balances for donor and recipient
		long donorBalanceBEFORE = subscriberA.getAccountValue1();
		DedicatedAccount recipientDA = subscriberB.getDedicatedAccounts().get(Integer.valueOf(smsDedicatedAccountID));
		long recipientBalanceBEFORE = 0L;
		if (null != recipientDA)
		{
			recipientBalanceBEFORE = recipientDA.getDedicatedAccountValue1();
		}

		// //////////////////////////////////////////////////////
		// Activate transfer

		this.updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.technicalProblem, request, response);
	}// testDAnotFound()

	// ================================================================================================//

	// SUCCESSFUL Credit Transfer (voice acct to main acct)
	// @Ignore
	@Test
	public void testSuccessfulMainToVoiceTopup()
	{
		int mainAccountID = 0;
		long transferAmount = 500;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("MainToVoice");
		request.setAmount(transferAmount);

		// Setup DAs
		// VOICE
		long dedicatedAccountValue1 = 0L;
		long dedicatedAccountValue2 = dedicatedAccountValue1;
		dedicatedAccountVOICE.setDedicatedAccountID(voiceDedicatedAccountID);
		dedicatedAccountVOICE.setDedicatedAccountValue1(dedicatedAccountValue1);
		dedicatedAccountVOICE.setDedicatedAccountValue2(dedicatedAccountValue2);
		airSimulator.updateDedicatedAccount(B_NUMBER, dedicatedAccountVOICE);

		// specify donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setPin(A_PIN);

		// ensure A has enough money
		long donorBalanceBEFORE = 100000L;
		try
		{
			subscriberA.setAccountValue1(donorBalanceBEFORE);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// get recipient balance BEFORE
		captureDonorBalanceBEFORE(subscriberA, mainAccountID);
		captureRecipientBalanceBEFORE(subscriberB, voiceDedicatedAccountID);

		// try transaction
		this.updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.success, request, response);

		// //////////////////////////////////////////////////////
		//
		// MANUAL calculations and verification: AFTER
		//
		// //////////////////////////////////////////////////////

		// Following method simulates transaction by calculating EXPECTED donor and recipient balances
		CreditTransferVariant variant = service.getVariant(request.getVariantID());
		simulateDebitsAndCredits(transferAmount, variant, 76);

		// Validate DEBIT (from MAIN account)
		this.actualDonorBalanceAFTER = subscriberA.getAccountValue1().longValue();
		Assert.assertEquals(expectedDonorBalanceAFTER, actualDonorBalanceAFTER);

		// Validate CREDIT (into the VOICE DA)
		this.actualRecipientBalanceAFTER = subscriberB.getDedicatedAccounts().get(Integer.valueOf(voiceDedicatedAccountID)).getDedicatedAccountValue1();
		Assert.assertEquals(expectedRecipientBalanceAFTER, actualRecipientBalanceAFTER);

	}// testSuccessfulMainToVoiceTopup()

	// ================================================================================================//

	// FAILED Credit Transfer (insufficient funds, main acct to voice)
	// @Ignore
	@Test
	public void testInsufficientFunds()
	{
		long transferAmount = 120;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("mainToVoice");
		request.setAmount(transferAmount);

		// Sort out PIN
		request.setPin(A_PIN);
		String internationalMSISDN = numberPlan.getInternationalFormat(A_NUMBER);

		try (IDatabaseConnection dbConnection = mysqlConnector.getConnection(null))
		{
			Pin pin = dbConnection.select(Pin.class, "where msisdn = %s and serviceId = %s and variantID = %s", internationalMSISDN, "PIN", VARIANT_ID);
			if (pin != null)
			{
				pin.setFailedCount(0);
				pin.setBlocked(false);
			}
			else
			{
				throw new SQLException("Pin not found for MSISDN [%s]", internationalMSISDN);
			}

			dbConnection.update(pin);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		// DA setup
		// VOICE
		long dedicatedAccountValue1 = 0L, dedicatedAccountValue2 = dedicatedAccountValue1;
		dedicatedAccountVOICE.setDedicatedAccountID(voiceDedicatedAccountID);
		dedicatedAccountVOICE.setDedicatedAccountValue1(dedicatedAccountValue1);
		dedicatedAccountVOICE.setDedicatedAccountValue2(dedicatedAccountValue2);
		airSimulator.updateDedicatedAccount(B_NUMBER, dedicatedAccountVOICE);

		// initialize donor balance to 1
		try
		{
			subscriberA.setAccountValue1(1L);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		subscriberA.setServiceClassCurrent(76);

		// specify donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		// retrieve balances BEFORE transaction
		long receipientBalanceBEFORE = dedicatedAccountVOICE.getDedicatedAccountValue1();
		long donorBalanceBEFORE = subscriberA.getAccountValue1().longValue();

		// try transaction
		this.updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.insufficientBalance, request, response);

		// retrieve balances AFTER transaction
		long donorBalanceAFTER = subscriberA.getAccountValue1().longValue();
		long receipientBalanceAFTER = dedicatedAccountVOICE.getDedicatedAccountValue1();

		// validate balances (should be unchanged)
		Assert.assertEquals(donorBalanceBEFORE, donorBalanceAFTER);
		Assert.assertEquals(receipientBalanceBEFORE, receipientBalanceAFTER);
	}// testInsufficientFunds()

	// ================================================================================================//

	// FAILED Credit Transfer (invalid service class, main acct to main acct)
	// @Ignore
	@Test
	public void testInvalidDonorServiceClass()
	{
		long transferAmount = 120;
		int serviceClassCurrent = 999;

		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("MainToVoice");
		request.setAmount(transferAmount);

		// DA setup
		// VOICE
		long dedicatedAccountValue1 = 0L, dedicatedAccountValue2 = dedicatedAccountValue1;
		dedicatedAccountVOICE.setDedicatedAccountID(voiceDedicatedAccountID);
		dedicatedAccountVOICE.setDedicatedAccountValue1(dedicatedAccountValue1);
		dedicatedAccountVOICE.setDedicatedAccountValue2(dedicatedAccountValue2);
		airSimulator.updateDedicatedAccount(B_NUMBER, dedicatedAccountVOICE);

		// specify donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setAmount(transferAmount);

		request.setPin(A_PIN);
		String internationalMSISDN = numberPlan.getInternationalFormat(A_NUMBER);

		try (IDatabaseConnection dbConnection = mysqlConnector.getConnection(null))
		{
			Pin pin = dbConnection.select(Pin.class, "where msisdn = %s and serviceId = %s and variantID = %s", internationalMSISDN, "PIN", VARIANT_ID);
			if (pin != null)
			{
				pin.setFailedCount(0);
				pin.setBlocked(false);
			}
			else
			{
				throw new SQLException("Pin not found for MSISDN [%s]", internationalMSISDN);
			}

			dbConnection.update(pin);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		// initialize service class
		subscriberA.setServiceClassCurrent(serviceClassCurrent);

		// retrieve balances BEFORE transaction
		long receipientBalanceBEFORE = subscriberB.getAccountValue1();
		long donorBalanceBEFORE = subscriberA.getAccountValue1().longValue();

		// try transaction
		this.updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.notEligible, request, response);

		// retrieve balances AFTER transaction
		long donorBalanceAFTER = subscriberA.getAccountValue1().longValue();
		long receipientBalanceAFTER = subscriberB.getAccountValue1().longValue();

		// validate balances (should be unchanged)
		Assert.assertEquals(donorBalanceBEFORE, donorBalanceAFTER);
		Assert.assertEquals(receipientBalanceBEFORE, receipientBalanceAFTER);
	}// testInvalidDonorServiceClass()

	// ================================================================================================//

	// FAILED Credit Transfer (too much B-party balance)
	// @Ignore
	@Test
	public void testExcessiveVoiceBalance()
	{
		long transferAmount = 1000;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("mainToVoice");
		request.setAmount(transferAmount);

		// Donor balance setup
		try
		{
			subscriberA.setAccountValue1(100000L);
		}
		catch (Exception e1)
		{
			logger.error(e1.getMessage(), e1);
		}

		// VOICE DA setup
		CreditTransferVariant variant = service.getVariant(request.getVariantID());
		long maxRecipientBalance = variant.getRecipientMaxBalance();

		dedicatedAccountVOICE.setDedicatedAccountID(voiceDedicatedAccountID);
		dedicatedAccountVOICE.setDedicatedAccountValue1(maxRecipientBalance - 1);
		dedicatedAccountVOICE.setDedicatedAccountValue2(dedicatedAccountVOICE.getDedicatedAccountValue1());
		airSimulator.updateDedicatedAccount(B_NUMBER, dedicatedAccountVOICE);

		// specify donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setAmount(transferAmount);

		// Sort out PIN
		request.setPin(A_PIN);
		String internationalMSISDN = numberPlan.getInternationalFormat(A_NUMBER);

		try (IDatabaseConnection dbConnection = mysqlConnector.getConnection(null))
		{
			Pin pin = dbConnection.select(Pin.class, "where msisdn = %s and serviceId = %s and variantID = %s", internationalMSISDN, "PIN", VARIANT_ID);
			if (pin != null)
			{
				pin.setFailedCount(0);
				pin.setBlocked(false);
			}
			else
			{
				throw new SQLException("Pin not found for MSISDN [%s]", internationalMSISDN);
			}

			dbConnection.update(pin);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		// Service classes
		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(76);

		// retrieve balances BEFORE transaction
		long donorBalanceBEFORE = subscriberA.getAccountValue1().longValue();
		DedicatedAccount recipientDA = subscriberB.getDedicatedAccounts().get(Integer.valueOf(voiceDedicatedAccountID));
		long receipientBalanceBEFORE = recipientDA.getDedicatedAccountValue1();

		// try transaction
		this.updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.excessiveBalance, request, response);

		// validate A balances (should be unchanged)
		long donorBalanceAFTER = subscriberA.getAccountValue1().longValue();
		Assert.assertEquals(donorBalanceBEFORE, donorBalanceAFTER);

		// validate B balances (should be unchanged)
		long receipientBalanceAFTER = recipientDA.getDedicatedAccountValue1().longValue();
		Assert.assertEquals(receipientBalanceBEFORE, receipientBalanceAFTER);
	}// testExcessiveVoiceBalance()

	// ================================================================================================//

	// FAILED Credit Transfer (invalid service class for B-party)
	// @Ignore
	@Test
	public void testInvalidRecipientServiceClass()
	{
		long transferAmount = 50;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("mainToVoice");
		request.setAmount(transferAmount);

		// DA setup
		// Main account
		long donorMainAccountValue1 = 0L;
		try
		{
			subscriberA.setAccountValue1(donorMainAccountValue1);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		// VOICE account
		long dedicatedAccountValue1 = 0L;
		long dedicatedAccountValue2 = dedicatedAccountValue1;
		dedicatedAccountVOICE.setDedicatedAccountID(voiceDedicatedAccountID);
		dedicatedAccountVOICE.setDedicatedAccountValue1(dedicatedAccountValue1);
		dedicatedAccountVOICE.setDedicatedAccountValue2(dedicatedAccountValue2);
		airSimulator.updateDedicatedAccount(B_NUMBER, dedicatedAccountVOICE);

		// Specify donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// Specify receiver MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setAmount(transferAmount);
		request.setPin(A_PIN);

		try
		{
			subscriberA.setAccountValue1(100000L);
			subscriberB.setServiceClassCurrent(776);
			subscriberB.setAccountValue1(200L);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		// Retrieve balances BEFORE transaction
		long donorBalanceBEFORE = subscriberA.getAccountValue1().longValue();
		long receipientBalanceBEFORE = subscriberB.getAccountValue1();

		// Try transaction
		this.updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.notEligible, request, response);

		// Validate balances (should be unchanged)
		long donorBalanceAFTER = subscriberA.getAccountValue1().longValue();
		Assert.assertEquals(donorBalanceBEFORE, donorBalanceAFTER);
		long receipientBalanceAFTER = subscriberB.getAccountValue1().longValue();
		Assert.assertEquals(receipientBalanceBEFORE, receipientBalanceAFTER);
	}// testInvalidServiceClass()

	// ================================================================================================//

	// FAILED Credit Transfer (recipient DA doesn't exist, main acct to DA)
	// @Ignore
	@Test
	public void testInvalidVariant()
	{
		long transferAmount = 120;
		transferAmount = 500;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("Voice");
		request.setAmount(transferAmount);
		request.setPin(A_PIN);

		// DA setup
		airSimulator.deleteDedicatedAccount(B_NUMBER, voiceDedicatedAccountID);
		// VOICE
		long dedicatedAccountValue1 = 0L, dedicatedAccountValue2 = dedicatedAccountValue1;
		int invalidDedicatedAccountID = 1117; // invalid DA id
		dedicatedAccountVOICE.setDedicatedAccountID(invalidDedicatedAccountID);
		dedicatedAccountVOICE.setDedicatedAccountValue1(dedicatedAccountValue1);
		dedicatedAccountVOICE.setDedicatedAccountValue2(dedicatedAccountValue2);
		airSimulator.updateDedicatedAccount(B_NUMBER, dedicatedAccountVOICE);

		// initialize donor balance to 1
		try
		{
			subscriberA.setAccountValue1(100000L);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(76);

		// specify donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		// retrieve balances BEFORE transaction
		long receipientBalanceBEFORE = subscriberB.getAccountValue1();
		long donorBalanceBEFORE = subscriberA.getAccountValue1().longValue();

		// try transaction
		this.updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.invalidVariant, request, response);

		// retrieve balances AFTER transaction
		long donorBalanceAFTER = subscriberA.getAccountValue1().longValue();
		long receipientBalanceAFTER = subscriberB.getAccountValue1().longValue();

		// validate balances (should be unchanged)
		Assert.assertEquals(donorBalanceBEFORE, donorBalanceAFTER);
		Assert.assertEquals(receipientBalanceBEFORE, receipientBalanceAFTER);

		airSimulator.deleteDedicatedAccount(B_NUMBER, invalidDedicatedAccountID);
	}// testInvalidRecipientDA()

	// ================================================================================================//

	// SUCCESSFUL DATA topup (main acct to DATA acct)
	// @Ignore
	@Test
	public void testSuccessfulMainToDataTopup()
	{
		int mainAccountID = 0;
		long transferAmount = 120;

		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("mainToData");
		request.setAmount(transferAmount);

		// /////////////////////////////////////////////////////////////////////////////////////
		// MAIN account setup
		long donorAccountValue1 = 1000L, donorAccountValue2 = donorAccountValue1;
		try
		{
			subscriberA.setAccountValue1(donorAccountValue1);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		// /////////////////////////////////////////////////////////////////////////////////////
		// DA setup (B_NUMBER SMS)
		int dataDedicatedAccountID = 7;
		long dataDedicatedAccountValue1 = 500;
		dedicatedAccountDATA.setDedicatedAccountID(dataDedicatedAccountID);
		dedicatedAccountDATA.setDedicatedAccountValue1(dataDedicatedAccountValue1);
		dedicatedAccountDATA.setDedicatedAccountValue2(dataDedicatedAccountValue1);
		airSimulator.updateDedicatedAccount(B_NUMBER, dedicatedAccountDATA);

		// /////////////////////////////////////////////////////////////////////////////////////

		// specify donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setPin(A_PIN);

		// Set up service classes
		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// Capture initial balances for donor and recipient
		captureDonorBalanceBEFORE(subscriberA, mainAccountID);
		captureRecipientBalanceBEFORE(subscriberB, dataDedicatedAccountID);

		// //////////////////////////////////////////////////////
		// Activate transfer

		this.updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.success, request, response);

		// //////////////////////////////////////////////////////

		CreditTransferVariant variant = service.getVariant(request.getVariantID());
		simulateDebitsAndCredits(transferAmount, variant, 76);

		// Validate DEBIT (from MAIN account)
		this.actualDonorBalanceAFTER = subscriberA.getAccountValue1().longValue();
		Assert.assertEquals(expectedDonorBalanceAFTER, actualDonorBalanceAFTER);

		// Validate CREDIT (into the DATA DA)
		this.actualRecipientBalanceAFTER = subscriberB.getDedicatedAccounts().get(Integer.valueOf(dataDedicatedAccountID)).getDedicatedAccountValue1();
		Assert.assertEquals(expectedRecipientBalanceAFTER, actualRecipientBalanceAFTER);

	}// testSuccessfulMainToDataTopup()

	// ================================================================================================//

	// SUCCESSFUL SMS to DATA topup
	// Strategy: Successively send wrong PIN until 'maxRetries' is exceeded. This will
	// trigger pinService to block the user in the db. The value of 'maxRetries' is maintained
	// by the PinService in the respective variant.
	// @Ignore
	@Test
	public void testPinBlocked()
	{
		long transferAmount = 120;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("smsToData");
		request.setAmount(transferAmount);

		// /////////////////////////////////////////////////////////////////////////////////////
		// MAIN account setup
		long donorAccountValue1 = 1000L, donorAccountValue2 = donorAccountValue1;
		try
		{
			subscriberA.setAccountValue1(donorAccountValue1);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		// /////////////////////////////////////////////////////////////////////////////////////
		// DA setup (A_NUMBER SMS)
		int smsDedicatedAccountID = 5;
		long smsDedicatedAccountValue1 = 200;
		dedicatedAccountSMS.setDedicatedAccountID(smsDedicatedAccountID);
		dedicatedAccountSMS.setDedicatedAccountValue1(smsDedicatedAccountValue1);
		dedicatedAccountSMS.setDedicatedAccountValue2(smsDedicatedAccountValue1);
		airSimulator.updateDedicatedAccount(A_NUMBER, dedicatedAccountSMS);

		// DA setup (B_NUMBER SMS)
		int dataDedicatedAccountID = 7;
		long dataDedicatedAccountValue1 = 500;
		dedicatedAccountDATA.setDedicatedAccountID(dataDedicatedAccountID);
		dedicatedAccountDATA.setDedicatedAccountValue1(dataDedicatedAccountValue1);
		dedicatedAccountDATA.setDedicatedAccountValue2(dataDedicatedAccountValue1);
		airSimulator.updateDedicatedAccount(B_NUMBER, dedicatedAccountDATA);

		// /////////////////////////////////////////////////////////////////////////////////////

		// specify donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setPin("wrongPin");

		// Set up service classes
		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// Capture initial balances for donor and recipient
		captureDonorBalanceBEFORE(subscriberA, smsDedicatedAccountID);
		captureRecipientBalanceBEFORE(subscriberB, dataDedicatedAccountID);

		// //////////////////////////////////////////////////////
		//
		// Activate transfer
		//
		// //////////////////////////////////////////////////////

		// Firstly find the variant we are testing against (i.e. CrXfr). Once found,
		// get configured allowed PIN retries from it.
		// Send as many pin retries as the variant specifies
		// Note if PinService blocks the user once allowed retries have been exhausted
		hxc.services.pin.Variant[] variants = ((PinServiceConfig) (pinService.getConfiguration())).getVariants();
		int index = -1;
		boolean found = false;

		for (hxc.services.pin.Variant v : variants)
		{
			index += 1;
			// if (v.getVariantID().equalsIgnoreCase("CrXfr"))
			if (v.getVariantID().equalsIgnoreCase("DEF"))
			{
				found = !found;
				break;
			}
		}
		assert (found == true);

		int pinRetryLimit = variants[index].getMaxRetries();
		updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);

		TransferResponse response = null;
		for (int i = 0; i <= pinRetryLimit; i++)
		{
			response = vasConnector.transfer(request);
		}

		validate(ReturnCodes.pinBlocked, request, response);

	}// testPinBlocked()
		// ================================================================================================//

	// SUCCESSFUL SMS to DATA topup
	// @Ignore
	@Test
	public void testSuccessfulSmsToDataTopup()
	{
		long transferAmount = 120;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("smsToData");
		request.setAmount(transferAmount);

		// /////////////////////////////////////////////////////////////////////////////////////
		// MAIN account setup
		long donorAccountValue1 = 1000L, donorAccountValue2 = donorAccountValue1;
		try
		{
			subscriberA.setAccountValue1(donorAccountValue1);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		// /////////////////////////////////////////////////////////////////////////////////////
		// DA setup (A_NUMBER SMS)
		int smsDedicatedAccountID = 5;
		long smsDedicatedAccountValue1 = 200;
		dedicatedAccountSMS.setDedicatedAccountID(smsDedicatedAccountID);
		dedicatedAccountSMS.setDedicatedAccountValue1(smsDedicatedAccountValue1);
		dedicatedAccountSMS.setDedicatedAccountValue2(smsDedicatedAccountValue1);
		airSimulator.updateDedicatedAccount(A_NUMBER, dedicatedAccountSMS);

		// DA setup (B_NUMBER SMS)
		int dataDedicatedAccountID = 7;
		long dataDedicatedAccountValue1 = 500;
		dedicatedAccountDATA.setDedicatedAccountID(dataDedicatedAccountID);
		dedicatedAccountDATA.setDedicatedAccountValue1(dataDedicatedAccountValue1);
		dedicatedAccountDATA.setDedicatedAccountValue2(dataDedicatedAccountValue1);
		airSimulator.updateDedicatedAccount(B_NUMBER, dedicatedAccountDATA);

		// /////////////////////////////////////////////////////////////////////////////////////

		// specify donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setPin(A_PIN);

		// Set up service classes
		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// Capture initial balances for donor and recipient
		captureDonorBalanceBEFORE(subscriberA, smsDedicatedAccountID);
		captureRecipientBalanceBEFORE(subscriberB, dataDedicatedAccountID);

		// //////////////////////////////////////////////////////
		//
		// Activate transfer
		//
		// //////////////////////////////////////////////////////

		this.updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(B_NUMBER, 0, 0, 0);
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.success, request, response);

		// //////////////////////////////////////////////////////
		//
		// MANUAL calculations and verification: AFTER
		//
		// //////////////////////////////////////////////////////
		CreditTransferVariant variant = service.getVariant(request.getVariantID());
		simulateDebitsAndCredits(transferAmount, variant, 76);

		// Validate DEBIT (SMS DA)
		this.actualDonorBalanceAFTER = subscriberA.getDedicatedAccounts().get(Integer.valueOf(smsDedicatedAccountID)).getDedicatedAccountValue1();
		Assert.assertEquals(expectedDonorBalanceAFTER, this.actualDonorBalanceAFTER);

		// Validate CREDIT (DATA DA)
		this.actualRecipientBalanceAFTER = subscriberB.getDedicatedAccounts().get(Integer.valueOf(dataDedicatedAccountID)).getDedicatedAccountValue1();
		Assert.assertEquals(expectedRecipientBalanceAFTER, this.actualRecipientBalanceAFTER);
	}// testSuccessfulSmsToDataTopup()

	// ================================================================================================//

	// Register PIN
	// @Ignore
	@Ignore
	@Test
	public void testSuccessfulPinRegistration()
	{
		// clearPin(A_NUMBER);

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID("PIN");
		// request.setVariantID("DEF");
		request.setVariantID("CrXfr");
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN("1111");
		request.setNewPIN("2222");
		this.updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);
		ChangePINResponse response = vasConnector.changePIN(request);
		validate(ReturnCodes.success, request, response);

		Number S_number = new Number(SPECIAL_NUMBER);
		S_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(S_number);
		request.setOldPIN("1111");
		request.setNewPIN("2222");
		this.updateUsageCounters(SPECIAL_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(SPECIAL_NUMBER, 0, 0, 0);
		response = vasConnector.changePIN(request);
		validate(ReturnCodes.success, request, response);

	}// testSuccessfulPinRegistration()

	// ================================================================================================//

	// Special MSISDN_A
	// @Ignore
	@Test
	public void testSpecialMsisdnA()
	{
		long transferAmount = 120;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("smsToData");
		request.setAmount(transferAmount);

		// /////////////////////////////////////////////////////////////////////////////////////
		// MAIN account setup
		long donorAccountValue1 = 1000L, donorAccountValue2 = donorAccountValue1;
		try
		{
			subscriberA.setAccountValue1(donorAccountValue1);
		}
		catch (Exception e1)
		{
			logger.error(e1.getMessage(), e1);
		}

		// /////////////////////////////////////////////////////////////////////////////////////
		// DA setup (A_NUMBER SMS)
		int smsDedicatedAccountID = 5;
		long smsDedicatedAccountValue1 = 200;
		dedicatedAccountSMS.setDedicatedAccountID(smsDedicatedAccountID);
		dedicatedAccountSMS.setDedicatedAccountValue1(smsDedicatedAccountValue1);
		dedicatedAccountSMS.setDedicatedAccountValue2(smsDedicatedAccountValue1);
		airSimulator.updateDedicatedAccount(SPECIAL_NUMBER, dedicatedAccountSMS);

		// DA setup (B_NUMBER SMS)
		int dataDedicatedAccountID = 7;
		long dataDedicatedAccountValue1 = 500;
		dedicatedAccountDATA.setDedicatedAccountID(dataDedicatedAccountID);
		dedicatedAccountDATA.setDedicatedAccountValue1(dataDedicatedAccountValue1);
		dedicatedAccountDATA.setDedicatedAccountValue2(dataDedicatedAccountValue1);
		airSimulator.updateDedicatedAccount(B_NUMBER, dedicatedAccountDATA);

		// /////////////////////////////////////////////////////////////////////////////////////

		// specify donor MSISDN
		Number A_number = new Number(SPECIAL_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setPin(A_PIN);

		// Set up service classes
		specialSubscriber.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// Capture initial balances for donor and recipient
		DedicatedAccount donorDA = specialSubscriber.getDedicatedAccounts().get(Integer.valueOf(smsDedicatedAccountID));
		long donorBalanceBEFORE = donorDA.getDedicatedAccountValue1();

		DedicatedAccount recipientDA = subscriberB.getDedicatedAccounts().get(Integer.valueOf(dataDedicatedAccountID));
		long recipientBalanceBEFORE = recipientDA.getDedicatedAccountValue1();

		// //////////////////////////////////////////////////////
		// Activate transfer

		this.updateUsageCounters(SPECIAL_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(SPECIAL_NUMBER, 0, 0, 0);
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.success, request, response);

		// //////////////////////////////////////////////////////

		// Validate debit (MAIN acct)
		String variantID = request.getVariantID();
		CreditTransferVariant variant = service.getVariant(variantID);

		/*
		 * long numberOfBenefits = (transferAmount * 10000) / variant.getUnitCostPerBenefit(); long costOfBenefits = numberOfBenefits * variant.getUnitCostPerBenefit(); //10^4 times
		 *
		 * long transactionCharge = config.getTransactionCharge(transferAmount); long totalDebit = (costOfBenefits + transactionCharge + 5000) / 10000;
		 */

		// DebitsAndCredits transferCalc = DebitsAndCredits.calculateDebitsAndCredits(transferAmount, variant);
		DebitsAndCredits d = new DebitsAndCredits();
		// DebitsAndCreditsResponse transferCalc = d.calculateDebitsAndCredits(transferAmount, service.getTransactionCharge(transferAmount), variant);
		try
		{
			DebitsAndCreditsResponse transferCalc = d.calculateDebitsAndCredits(transferAmount, variant.getTransactionCharge(transferAmount, 76), variant);

			// Validate DEBIT
			long donorBalanceAFTER = donorDA.getDedicatedAccountValue1();
			Assert.assertEquals(donorBalanceBEFORE - transferCalc.getDonorDADebit(), donorBalanceAFTER);

			// Validate CREDIT
			long recipientBalanceAFTER = recipientDA.getDedicatedAccountValue1();
			Assert.assertEquals(recipientBalanceBEFORE + transferCalc.getRecipientDACredit(), recipientBalanceAFTER);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}// testSpecialMsisdnA()

	// ================================================================================================//

	// Special MSISDN_B
	// @Ignore
	@Test
	public void testSpecialMsisdnB()
	{
		long transferAmount = 120;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("smsToData");
		request.setAmount(transferAmount);

		// /////////////////////////////////////////////////////////////////////////////////////
		// MAIN account setup
		long donorAccountValue1 = 1000L, donorAccountValue2 = donorAccountValue1;
		try
		{
			subscriberA.setAccountValue1(donorAccountValue1);
		}
		catch (Exception e1)
		{
			logger.error(e1.getMessage(), e1);
		}

		// /////////////////////////////////////////////////////////////////////////////////////
		// DA setup (A_NUMBER SMS)
		int smsDedicatedAccountID = 5;
		long smsDedicatedAccountValue1 = 500;
		dedicatedAccountSMS.setDedicatedAccountID(smsDedicatedAccountID);
		dedicatedAccountSMS.setDedicatedAccountValue1(smsDedicatedAccountValue1);
		dedicatedAccountSMS.setDedicatedAccountValue2(smsDedicatedAccountValue1);
		airSimulator.updateDedicatedAccount(A_NUMBER, dedicatedAccountSMS);

		// DA setup (B_NUMBER DATA)
		int dataDedicatedAccountID = 7;
		long dataDedicatedAccountValue1 = 200;
		dedicatedAccountDATA.setDedicatedAccountID(dataDedicatedAccountID);
		dedicatedAccountDATA.setDedicatedAccountValue1(dataDedicatedAccountValue1);
		dedicatedAccountDATA.setDedicatedAccountValue2(dataDedicatedAccountValue1);
		airSimulator.updateDedicatedAccount(SPECIAL_NUMBER, dedicatedAccountDATA);

		// /////////////////////////////////////////////////////////////////////////////////////

		// specify donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(SPECIAL_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setPin(A_PIN);

		// Set up service classes
		specialSubscriber.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// Capture initial balances for donor and recipient
		DedicatedAccount donorDA = subscriberA.getDedicatedAccounts().get(Integer.valueOf(smsDedicatedAccountID));
		long donorBalanceBEFORE = donorDA.getDedicatedAccountValue1();

		DedicatedAccount recipientDA = specialSubscriber.getDedicatedAccounts().get(Integer.valueOf(dataDedicatedAccountID));
		long recipientBalanceBEFORE = recipientDA.getDedicatedAccountValue1();

		// //////////////////////////////////////////////////////
		// Activate transfer

		this.updateUsageCounters(SPECIAL_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(SPECIAL_NUMBER, 0, 0, 0);
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.success, request, response);

		// //////////////////////////////////////////////////////

		// Validate debit (MAIN acct)
		String variantID = request.getVariantID();
		CreditTransferVariant variant = service.getVariant(variantID);

		/*
		 *
		 * long numberOfBenefits = (transferAmount * 10000) / variant.getUnitCostPerBenefit(); long costOfBenefits = numberOfBenefits * variant.getUnitCostPerBenefit(); //10^4 times
		 *
		 * long transactionCharge = config.getTransactionCharge(transferAmount); long totalDebit = (costOfBenefits + transactionCharge + 5000) / 10000;
		 */

		// DebitsAndCredits transferCalc = DebitsAndCredits.calculateDebitsAndCredits(transferAmount, variant);
		DebitsAndCredits d = new DebitsAndCredits();
		// DebitsAndCreditsResponse transferCalc = d.calculateDebitsAndCredits(transferAmount, service.getTransactionCharge(transferAmount), variant);
		try
		{
			DebitsAndCreditsResponse transferCalc = d.calculateDebitsAndCredits(transferAmount, variant.getTransactionCharge(transferAmount, 76), variant);

			// Validate DEBIT
			long donorBalanceAFTER = donorDA.getDedicatedAccountValue1();
			Assert.assertEquals(donorBalanceBEFORE - transferCalc.getDonorDADebit(), donorBalanceAFTER);

			// Validate CREDIT
			long recipientBalanceAFTER = recipientDA.getDedicatedAccountValue1();
			Assert.assertEquals(recipientBalanceBEFORE + transferCalc.getRecipientDACredit(), recipientBalanceAFTER);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}// testSpecialMsisdnB()

	// ================================================================================================//

	// Triggers
	// @Ignore
	@Test
	public void testTriggers()
	{

		// / Clear tables

		clearLifecycleTimeTrigger(A_NUMBER, B_NUMBER, "mainToMain");
		this.updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(B_NUMBER, 0, 0, 0);

		// / Do Transfer

		long transferAmount = 200L;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("mainToMain");
		request.setAmount(transferAmount);

		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setPin(A_PIN);

		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// transfer
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.success, request, response);

		// / Validate Tables Post Transfer

		Assert.assertTrue(this.validateCumulativeSentAccumulators(A_NUMBER, transferAmount, transferAmount, transferAmount));
		Assert.assertTrue(this.validateCumulativeSentAccumulators(B_NUMBER, 0, 0, 0));
		Assert.assertTrue(this.validateCumulativeReceivedAccumulators(B_NUMBER, transferAmount, transferAmount, transferAmount));
		Assert.assertTrue(this.validateCumulativeReceivedAccumulators(A_NUMBER, 0, 0, 0));
		Assert.assertTrue(this.validateUsageCounters(A_NUMBER, 1, 1, 1));
		Assert.assertTrue(this.validateUsageCounters(B_NUMBER, 0, 0, 0));

		// / Force trigger and daily expiry

		final long millisInDay = 86400000;
		Date currentDate = new Date();

		updateLifecycleTimeTrigger(A_NUMBER, B_NUMBER, currentDate, "mainToMain");
		Date yesterday = new Date();
		yesterday.setTime(yesterday.getTime() - millisInDay);
		this.updateUsageCounterDates(A_NUMBER, yesterday, yesterday, yesterday);
		this.updateUsageCounterDates(B_NUMBER, yesterday, yesterday, yesterday);

		// / Wait

		long delay = 10000;

		try
		{
			Thread.sleep(delay);
		}
		catch (InterruptedException ex)
		{
			Thread.currentThread().interrupt();
		}

		// / Validate Daily Clear

		Assert.assertEquals(true, this.validateCumulativeSentAccumulators(A_NUMBER, 0, transferAmount, transferAmount));
		Assert.assertEquals(true, this.validateCumulativeSentAccumulators(B_NUMBER, 0, 0, 0));
		Assert.assertEquals(true, this.validateCumulativeReceivedAccumulators(B_NUMBER, 0, transferAmount, transferAmount));
		Assert.assertEquals(true, this.validateCumulativeReceivedAccumulators(A_NUMBER, 0, 0, 0));
		Assert.assertEquals(true, this.validateUsageCounters(A_NUMBER, 0, 1, 1));
		Assert.assertEquals(true, this.validateUsageCounters(B_NUMBER, 0, 0, 0));

		// / Force trigger and weekly expiry

		currentDate = new Date();
		updateLifecycleTimeTrigger(A_NUMBER, B_NUMBER, currentDate, "mainToMain");
		Date lastWeek = new Date();
		lastWeek.setTime(lastWeek.getTime() - (millisInDay * 7));
		this.updateUsageCounterDates(A_NUMBER, lastWeek, lastWeek, lastWeek);
		this.updateUsageCounterDates(B_NUMBER, lastWeek, lastWeek, lastWeek);

		// / Wait

		try
		{
			Thread.sleep(delay);
		}
		catch (InterruptedException ex)
		{
			Thread.currentThread().interrupt();
		}

		// / Validate Weekly Clear

		Assert.assertEquals(true, this.validateCumulativeSentAccumulators(A_NUMBER, 0, 0, transferAmount));
		Assert.assertEquals(true, this.validateCumulativeSentAccumulators(B_NUMBER, 0, 0, 0));
		Assert.assertEquals(true, this.validateCumulativeReceivedAccumulators(B_NUMBER, 0, 0, transferAmount));
		Assert.assertEquals(true, this.validateCumulativeReceivedAccumulators(A_NUMBER, 0, 0, 0));
		Assert.assertEquals(true, this.validateUsageCounters(A_NUMBER, 0, 0, 1));
		Assert.assertEquals(true, this.validateUsageCounters(B_NUMBER, 0, 0, 0));

		// / Force trigger and monthly expiry

		currentDate = new Date();
		updateLifecycleTimeTrigger(A_NUMBER, B_NUMBER, currentDate, "mainToMain");
		Date lastMonth = new Date();
		lastMonth.setTime(lastMonth.getTime() - (millisInDay * 30));
		this.updateUsageCounterDates(A_NUMBER, lastMonth, lastMonth, lastMonth);
		this.updateUsageCounterDates(B_NUMBER, lastMonth, lastMonth, lastMonth);

		// / Wait

		try
		{
			Thread.sleep(delay);
		}
		catch (InterruptedException ex)
		{
			Thread.currentThread().interrupt();
		}

		// / Validate Monthly Clear

		Assert.assertEquals(true, this.validateCumulativeSentAccumulators(A_NUMBER, 0, 0, 0));
		Assert.assertEquals(true, this.validateCumulativeSentAccumulators(B_NUMBER, 0, 0, 0));
		Assert.assertEquals(true, this.validateCumulativeReceivedAccumulators(B_NUMBER, 0, 0, 0));
		Assert.assertEquals(true, this.validateCumulativeReceivedAccumulators(A_NUMBER, 0, 0, 0));
		Assert.assertEquals(true, this.validateUsageCounters(A_NUMBER, 0, 0, 0));
		Assert.assertEquals(true, this.validateUsageCounters(B_NUMBER, 0, 0, 0));

	}// testTriggers()

	// ================================================================================================//

	// Invalid Quota ID
	// @Ignore
	@Test
	public void testInvalidQuotaID()
	{
		// Backup existing quotas and variants (to be restored after test)

		Quota[] originalQuotas = config.getDonorQuotas();
		CreditTransferVariant[] originalVariants = config.getVariants();

		// Set quotas, exclude monthly

		Quota[] donorQuotas = new Quota[] { new Quota("Daily", 5, 1, QuotaPeriodUnits.DAY), new Quota("Weekly", 15, 1, QuotaPeriodUnits.WEEK),
		// new Quota("Monthly",30,1,QuotaPeriodUnits.MONTH),
		};

		try
		{
			config.setDonorQuotas(donorQuotas);
		}
		catch (Exception e)
		{
			Assert.fail("Could not set quotas. Reason: " + e.getLocalizedMessage());
		}

		// Set test variant quota list, include "Monthly"

		testVariant.setDonorQuotas(new String[] { "Daily", "Weekly", "Monthly" });

		// Create new variant array

		CreditTransferVariant[] variants = { testVariant };

		// Expect exception thrown for invalid quota ID monthly
		try
		{
			config.setVariants(variants);
			Assert.fail("No Exception thrown");
		}
		catch (ValidationException e)
		{
			Assert.assertTrue("Exception message should contain", e.getLocalizedMessage().toLowerCase().contains("quota"));
			Assert.assertTrue("Exception message should contain", e.getLocalizedMessage().toLowerCase().contains("invalid"));
			Assert.assertTrue("Exception message should contain", e.getLocalizedMessage().toLowerCase().contains("monthly"));
		}
		catch (Exception e)
		{
			Assert.fail("ValidationException not thrown. Exception: " + e.getLocalizedMessage());
		}

		// Restore existing quotas and variants for subsequent tests

		try
		{
			config.setDonorQuotas(originalQuotas);
			config.setVariants(originalVariants);
		}
		catch (Exception e)
		{
			Assert.fail("Could not restore original variants. Reason: " + e.getLocalizedMessage());
		}

	}// testInvalidQuotaID()

	// ================================================================================================//

	// Duplicate Quota Unit
	// @Ignore
	@Test
	public void testDuplicateQuotaUnit()
	{
		// Backup existing quotas and variants (to be restored after test)

		Quota[] originalQuotas = config.getDonorQuotas();
		CreditTransferVariant[] originalVariants = config.getVariants();

		// Set quotas

		Quota[] donorQuotas = new Quota[] { new Quota("DailyPrepaid", 5, 1, QuotaPeriodUnits.DAY), new Quota("WeeklyPrepaid", 15, 1, QuotaPeriodUnits.WEEK),
				new Quota("MonthlyPrepaid", 30, 1, QuotaPeriodUnits.MONTH), new Quota("DailyPostpaid", 10, 1, QuotaPeriodUnits.DAY), new Quota("WeeklyPostpaid", 30, 1, QuotaPeriodUnits.WEEK),
				new Quota("MonthlyPostpaid", 60, 1, QuotaPeriodUnits.MONTH) };

		try
		{
			config.setDonorQuotas(donorQuotas);
		}
		catch (Exception e)
		{
			Assert.fail("Could not set quotas. Reason: " + e.getLocalizedMessage());
		}

		// Set donor quota with valid IDs

		testVariant.setDonorQuotas(new String[] { "DailyPrepaid", "WeeklyPrepaid", "MonthlyPrepaid" });

		// Create new variant array

		CreditTransferVariant[] variants = { testVariant };

		// Expect NO exception thrown when setting
		try
		{
			config.setVariants(variants);
		}
		catch (Exception e)
		{
			Assert.fail("Exception thrown: " + e.getLocalizedMessage());
		}

		// Set quota with duplicate unit type (day)

		testVariant.setDonorQuotas(new String[] { "DailyPrepaid", "WeeklyPrepaid", "DailyPostpaid" });

		// Update variant array

		variants = new CreditTransferVariant[] { testVariant };

		// Expect exception thrown for duplicate quota period
		try
		{
			config.setVariants(variants);
			Assert.fail("No Exception thrown");
		}
		catch (ValidationException e)
		{
			Assert.assertTrue(e.getLocalizedMessage().toLowerCase().contains("quota"));
			Assert.assertTrue(e.getLocalizedMessage().toLowerCase().contains("duplicate"));
			Assert.assertTrue(e.getLocalizedMessage().toLowerCase().contains("day"));
		}
		catch (Exception e)
		{
			Assert.fail("ValidationException not thrown. Exception: " + e.getLocalizedMessage());
		}

		// Restore existing quotas and variants for subsequent tests

		try
		{
			config.setDonorQuotas(originalQuotas);
			config.setVariants(originalVariants);
		}
		catch (Exception e)
		{
			Assert.fail("Could not restore original variants. Reason: " + e.getLocalizedMessage());
		}

	}// testDuplicateQuotaUnit()

	// ================================================================================================//

	// Sucesful Quota Update
	// @Ignore
	@Test
	public void testSuccessfulQuotaUpdate()
	{
		// Backup existing quotas and variants (to be restored after test)

		Quota[] originalQuotas = config.getDonorQuotas();
		CreditTransferVariant[] originalVariants = config.getVariants();

		// Set quotas

		Quota[] donorQuotas = new Quota[] { new Quota("DailyPrepaid", 5, 1, QuotaPeriodUnits.DAY), new Quota("WeeklyPrepaid", 15, 1, QuotaPeriodUnits.WEEK),
				new Quota("MonthlyPrepaid", 30, 1, QuotaPeriodUnits.MONTH), new Quota("DailyPostpaid", 10, 1, QuotaPeriodUnits.DAY), new Quota("WeeklyPostpaid", 30, 1, QuotaPeriodUnits.WEEK),
				new Quota("MonthlyPostpaid", 60, 1, QuotaPeriodUnits.MONTH) };

		try
		{
			config.setDonorQuotas(donorQuotas);
		}
		catch (Exception e)
		{
			Assert.fail("Could not set quotas. Reason: " + e.getLocalizedMessage());
		}

		// Set donor quota with valid IDs

		testVariant.setDonorQuotas(new String[] { "DailyPrepaid", "WeeklyPrepaid" });

		// Create new variant array

		CreditTransferVariant[] variants = { testVariant };

		// Expect NO exception thrown when setting
		try
		{
			config.setVariants(variants);
		}
		catch (Exception e)
		{
			Assert.fail("Exception thrown: " + e.getLocalizedMessage());
		}

		// Restore existing quotas and variants for subsequent tests

		try
		{
			config.setDonorQuotas(originalQuotas);
			config.setVariants(originalVariants);
		}
		catch (Exception e)
		{
			Assert.fail("Could not restore original variants. Reason: " + e.getLocalizedMessage());
		}

	}// testSuccessfulQuotaUpdate

	// ================================================================================================//

	// Invalid PIN
	// @Ignore
	@Test
	public void testInvalidPIN()
	{
		final String INVALID_A_PIN = "111";
		long transferAmount = 120;
		TransferRequest request = new TransferRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID("smsToData");
		request.setAmount(transferAmount);

		// /////////////////////////////////////////////////////////////////////////////////////
		// MAIN account setup
		long donorAccountValue1 = 1000L, donorAccountValue2 = donorAccountValue1;
		try
		{
			subscriberA.setAccountValue1(donorAccountValue1);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		// /////////////////////////////////////////////////////////////////////////////////////
		// DA setup (A_NUMBER SMS)
		int smsDedicatedAccountID = 5;
		long smsDedicatedAccountValue1 = 200;
		dedicatedAccountSMS.setDedicatedAccountID(smsDedicatedAccountID);
		dedicatedAccountSMS.setDedicatedAccountValue1(smsDedicatedAccountValue1);
		dedicatedAccountSMS.setDedicatedAccountValue2(smsDedicatedAccountValue1);
		airSimulator.updateDedicatedAccount(A_NUMBER, dedicatedAccountSMS);

		// DA setup (B_NUMBER SMS)
		int dataDedicatedAccountID = 7;
		long dataDedicatedAccountValue1 = 500;
		dedicatedAccountDATA.setDedicatedAccountID(dataDedicatedAccountID);
		dedicatedAccountDATA.setDedicatedAccountValue1(dataDedicatedAccountValue1);
		dedicatedAccountDATA.setDedicatedAccountValue2(dataDedicatedAccountValue1);
		airSimulator.updateDedicatedAccount(B_NUMBER, dedicatedAccountDATA);

		// /////////////////////////////////////////////////////////////////////////////////////

		// specify donor MSISDN
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// specify receiver MSISDN
		Number B_number = new Number(B_NUMBER);
		B_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setRecipientNumber(B_number);

		request.setPin(INVALID_A_PIN);

		// Set up service classes
		subscriberA.setServiceClassCurrent(76);
		subscriberB.setServiceClassCurrent(5);

		// Capture initial balances for donor and recipient
		DedicatedAccount donorDA = subscriberA.getDedicatedAccounts().get(Integer.valueOf(smsDedicatedAccountID));
		long donorBalanceBEFORE = donorDA.getDedicatedAccountValue1();

		DedicatedAccount recipientDA = subscriberB.getDedicatedAccounts().get(Integer.valueOf(dataDedicatedAccountID));
		long recipientBalanceBEFORE = recipientDA.getDedicatedAccountValue1();

		// //////////////////////////////////////////////////////
		// Activate transfer

		this.updateUsageCounters(A_NUMBER, 0, 0, 0);
		this.updateCumulativeCounters(A_NUMBER, 0, 0, 0);
		TransferResponse response = vasConnector.transfer(request);
		validate(ReturnCodes.invalidPin, request, response);

		// //////////////////////////////////////////////////////

		// Validate DEBIT
		long donorBalanceAFTER = donorDA.getDedicatedAccountValue1();
		Assert.assertEquals(donorBalanceBEFORE, donorBalanceAFTER);

		// Validate CREDIT
		long recipientBalanceAFTER = recipientDA.getDedicatedAccountValue1();
		Assert.assertEquals(recipientBalanceBEFORE, recipientBalanceAFTER);
	}// testInvalidPIN()

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

	private void validate(ReturnCodes code, RequestHeader request, ResponseHeader response)
	{
		Assert.assertEquals(code, response.getReturnCode());
		Assert.assertEquals(request.getTransactionID(), response.getTransactionId());
		Assert.assertEquals(request.getSessionID(), response.getSessionId());
	}

	private boolean updateUsageCounters(String msisdn, int dailyCount, int weeklyCount, int monthlyCount)
	{
		try (IDatabaseConnection dbConnection = mysqlConnector.getConnection(null))
		{
			if (!numberPlan.isValid(msisdn))
			{
				return false;
			}

			// Internationalize MSISDN if local
			msisdn = numberPlan.getInternationalFormat(msisdn);

			// Retrieve record from db
			UsageCounter counter = dbConnection.select(UsageCounter.class, "where msisdn=%s AND serviceID=%s", msisdn, SERVICE_ID);

			// Not found - then insert new record
			if (counter == null)
			{
				counter = new UsageCounter(msisdn, "CrXfr", 0, 0, 0);
				dbConnection.insert(counter);
			}
			else
			// set counter as requested
			{
				counter.setDailyCounter(dailyCount);
				counter.setWeeklyCounter(weeklyCount);
				counter.setMonthlyCounter(monthlyCount);
				dbConnection.update(counter);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

	private boolean validateUsageCounters(String msisdn, int dailyCount, int weeklyCount, int monthlyCount)
	{
		try (IDatabaseConnection dbConnection = mysqlConnector.getConnection(null))
		{
			msisdn = numberPlan.getInternationalFormat(msisdn);

			UsageCounter counter = dbConnection.select(UsageCounter.class, "where msisdn=%s AND serviceID=%s", msisdn, SERVICE_ID);
			if (counter == null)
				return false;

			if (counter.getDailyCounter() != dailyCount)
				return false;

			if (counter.getWeeklyCounter() != weeklyCount)
				return false;

			if (counter.getMonthlyCounter() != monthlyCount)
				return false;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	private boolean updateCumulativeCounters(String msisdn, long dailyCount, long weeklyCount, long monthlyCount)
	{
		try (IDatabaseConnection dbConnection = mysqlConnector.getConnection(null))
		{
			if (!numberPlan.isValid(msisdn))
			{
				return false;
			}

			// Internationalize MSISDN if local
			msisdn = numberPlan.getInternationalFormat(msisdn);

			// Retrieve record from db
			UsageCounter counter = dbConnection.select(UsageCounter.class, "where msisdn=%s AND serviceID=%s", msisdn, SERVICE_ID);

			// Not found - then insert new record
			if (counter == null)
			{
				counter = new UsageCounter(msisdn, "CrXfr", 0, 0, 0);
				dbConnection.insert(counter);
			}
			else
			// set counter as requested
			{
				// Donor
				counter.setDailySentAccumulator(dailyCount);
				counter.setWeeklySentAccumulator(weeklyCount);
				counter.setMonthlySentAccumulator(monthlyCount);
				// Recipient
				counter.setDailyReceivedAccumulator(dailyCount);
				counter.setWeeklyReceivedAccumulator(weeklyCount);
				counter.setMonthlyReceivedAccumulator(monthlyCount);

				dbConnection.update(counter);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

	private boolean validateCumulativeSentAccumulators(String msisdn, long dailyAmount, long weeklyAmount, long monthlyAmount)
	{
		try (IDatabaseConnection dbConnection = mysqlConnector.getConnection(null))
		{
			msisdn = numberPlan.getInternationalFormat(msisdn);

			UsageCounter counter = dbConnection.select(UsageCounter.class, "where msisdn=%s AND serviceID=%s", msisdn, SERVICE_ID);
			if (counter == null)
				return false;

			if (counter.getDailySentAccumulator() != dailyAmount)
				return false;

			if (counter.getWeeklySentAccumulator() != weeklyAmount)
				return false;

			if (counter.getMonthlySentAccumulator() != monthlyAmount)
				return false;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	private boolean validateCumulativeReceivedAccumulators(String msisdn, long dailyAmount, long weeklyAmount, long monthlyAmount)
	{
		try (IDatabaseConnection dbConnection = mysqlConnector.getConnection(null))
		{
			msisdn = numberPlan.getInternationalFormat(msisdn);

			UsageCounter counter = dbConnection.select(UsageCounter.class, "where msisdn=%s AND serviceID=%s", msisdn, SERVICE_ID);
			if (counter == null)
				return false;

			if (counter.getDailyReceivedAccumulator() != dailyAmount)
				return false;

			if (counter.getWeeklyReceivedAccumulator() != weeklyAmount)
				return false;

			if (counter.getMonthlyReceivedAccumulator() != monthlyAmount)
				return false;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	private boolean updateUsageCounterDates(String msisdn, Date ts, Date weekBaseDate, Date monthBaseDate)
	{
		try (IDatabaseConnection dbConnection = mysqlConnector.getConnection(null))
		{
			msisdn = numberPlan.getInternationalFormat(msisdn);

			UsageCounter counter = dbConnection.select(UsageCounter.class, "where msisdn=%s AND serviceID=%s", msisdn, SERVICE_ID);

			if (counter == null)
				return false;
			else
			{
				counter.setFirstInserted(ts);
				counter.setWeekBaseDate(weekBaseDate);
				counter.setMonthBaseDate(monthBaseDate);
				dbConnection.update(counter);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

	private boolean updateLifecycleTimeTrigger(String msisdnA, String msisdnB, Date nextDateTime, String variantID)
	{
		try (IDatabaseConnection dbConnection = mysqlConnector.getConnection(null))
		{
			msisdnA = numberPlan.getInternationalFormat(msisdnA);
			msisdnB = numberPlan.getInternationalFormat(msisdnB);
			TemporalTrigger trigger = dbConnection.select(TemporalTrigger.class, "where msisdnA=%s AND msisdnB=%s AND variantID=%s AND serviceID=%s", msisdnA, msisdnB, variantID, SERVICE_ID);

			if (trigger == null)
			{
				trigger = new TemporalTrigger();
				trigger.setBeingProcessed(false);
				trigger.setMsisdnA(msisdnA);
				trigger.setMsisdnB(msisdnB);
				trigger.setNextDateTime(nextDateTime);
				trigger.setServiceID(SERVICE_ID);
				trigger.setVariantID(variantID);
				trigger.setState(0);
				dbConnection.insert(trigger);
			}
			else
			{
				trigger.setNextDateTime(nextDateTime);
				trigger.setState(0);
				dbConnection.update(trigger);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	private boolean clearLifecycleTimeTrigger(String msisdnA, String msisdnB, String variantID)
	{
		try (IDatabaseConnection dbConnection = mysqlConnector.getConnection(null))
		{
			msisdnA = numberPlan.getInternationalFormat(msisdnA);
			msisdnB = numberPlan.getInternationalFormat(msisdnB);
			dbConnection.delete(TemporalTrigger.class, "where msisdnA=%s AND msisdnB=%s AND variantID=%s AND serviceID=%s", msisdnA, msisdnB, variantID, SERVICE_ID);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	private void captureDonorBalanceBEFORE(SubscriberEx subscriber, int accountID)
	{
		if (accountID == 0)
		{
			this.donorBalanceBEFORE = subscriber.getAccountValue1();
		}
		else
		{
			DedicatedAccount da = airSimulator.getDedicatedAccount(subscriber.getInternationalNumber(), accountID);
			this.donorBalanceBEFORE = da.getDedicatedAccountValue1();
		}
	}

	private void captureRecipientBalanceBEFORE(SubscriberEx subscriber, int accountID)
	{
		if (accountID == 0)
		{
			this.recipientBalanceBEFORE = subscriber.getAccountValue1();
		}
		else
		{
			DedicatedAccount da = airSimulator.getDedicatedAccount(subscriber.getInternationalNumber(), accountID);
			this.recipientBalanceBEFORE = da.getDedicatedAccountValue1();
		}
	}

	private long getDonorBalanceBEFORE()
	{
		return donorBalanceBEFORE;
	}

	private void setDonorBalanceBEFORE(long donorBalanceBEFORE)
	{
		this.donorBalanceBEFORE = donorBalanceBEFORE;
	}

	private long getRecipientBalanceBEFORE()
	{
		return recipientBalanceBEFORE;
	}

	private void setRecipientBalanceBEFORE(long recipientBalanceBEFORE)
	{
		this.recipientBalanceBEFORE = recipientBalanceBEFORE;
	}

	private long getExpectedDonorBalanceAFTER()
	{
		return expectedDonorBalanceAFTER;
	}

	private void setExpectedDonorBalanceAFTER(long expectedDonorBalanceAFTER)
	{
		this.expectedDonorBalanceAFTER = expectedDonorBalanceAFTER;
	}

	private long getActualDonorBalanceAFTER()
	{
		return actualDonorBalanceAFTER;
	}

	private void setActualDonorBalanceAFTER(long actualDonorBalanceAFTER)
	{
		this.actualDonorBalanceAFTER = actualDonorBalanceAFTER;
	}

	private long getExpectedRecipientBalanceAFTER()
	{
		return expectedRecipientBalanceAFTER;
	}

	private void setExpectedRecipientBalanceAFTER(long expectedRecipientBalanceAFTER)
	{
		this.expectedRecipientBalanceAFTER = expectedRecipientBalanceAFTER;
	}

	private long getActualRecipientBalanceAFTER()
	{
		return actualRecipientBalanceAFTER;
	}

	private void setActualRecipientBalanceAFTER(long actualRecipientBalanceAFTER)
	{
		this.actualRecipientBalanceAFTER = actualRecipientBalanceAFTER;
	}

	// Determines EXPECTED and ACTUAL values of subscriber balances following a transfer transaction
	private void simulateDebitsAndCredits(long transferAmount, CreditTransferVariant variant, int serviceClass)
	{
		try
		{
			// Simulate DONOR DEBIT
			long transactionCharge = variant.getTransactionCharge(transferAmount, serviceClass);
			long unitCostPerDonor = variant.getUnitCostPerDonation();
			long numberOfDebitsWithoutCharge = transferAmount * 10000 / unitCostPerDonor;
			long numberOfDebitsWithCharge = (transferAmount * 10000 + transactionCharge) / unitCostPerDonor;
			expectedDonorBalanceAFTER = donorBalanceBEFORE - numberOfDebitsWithCharge;

			// Simulate RECIPIENT CREDIT
			long unitCostPerBenefit = variant.getUnitCostPerBenefit();
			long numberOfCredits = (numberOfDebitsWithoutCharge * unitCostPerDonor) / unitCostPerBenefit;
			expectedRecipientBalanceAFTER = recipientBalanceBEFORE + numberOfCredits;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

}
