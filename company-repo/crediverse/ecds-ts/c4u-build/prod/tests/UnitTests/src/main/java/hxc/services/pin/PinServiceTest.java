package hxc.services.pin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
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
import com.concurrent.hxc.ResetPINRequest;
import com.concurrent.hxc.ResetPINResponse;
import com.concurrent.hxc.ResponseHeader;
import com.concurrent.hxc.SubscriptionState;
import com.concurrent.hxc.ValidatePINRequest;
import com.concurrent.hxc.ValidatePINResponse;
import com.concurrent.hxc.VasServiceInfo;

import hxc.configuration.ValidationException;
import hxc.connectors.Channels;
import hxc.connectors.air.AirConnector;
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.mysql.MySqlConnection;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.lifecycle.LifecycleConnector;
import hxc.connectors.smpp.SmppConnector;
import hxc.connectors.soap.ISoapConnector;
import hxc.connectors.soap.SoapConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.servicebus.ServiceBus;
import hxc.services.notification.Phrase;
import hxc.services.numberplan.NumberPlanService;
import hxc.services.pin.PinService.PinServiceConfig;
import hxc.services.security.SecurityService;
import hxc.services.transactions.CdrBase;
import hxc.services.transactions.CsvCdr;
import hxc.services.transactions.TransactionService;
import hxc.testsuite.RunAllTestsBase;

@SuppressWarnings("unused")
@RunWith(Parameterized.class)
public class PinServiceTest extends RunAllTestsBase
{
	final static Logger logger = LoggerFactory.getLogger(PinServiceTest.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private static ISoapConnector soapConnector;
	private static IHxC vasConnector;

	private static String SERVICE_ID = "PIN";
	private static String SERVICE_NAME = "Pin Service";
	private static int languageID = 1;
	private static String A_NEW_PIN = "2222";
	private static String A_OLD_PIN2 = "2222";
	private static String A_NEW_PIN2 = "3333";
	private static String A_INCORRECT_PIN = "1234";
	private static String A_LONG_PIN = "1234567";
	private static String A_SHORT_PIN = "123";
	private static String A_NUMBER = "23786012008";

	private static Variant defaultVariant = new Variant(PinService.getDefaultVariantID(), Phrase.en("Default"), 5, 4, 6, "1111", false);

	// Create variant that allows Default pin
	private static Variant testVariant = new Variant("TEST", Phrase.en("Default"), 5, 4, 6, "1111", true);

	private static int PIN_MAX_RETRIES = defaultVariant.getMaxRetries();
	private static int PIN_MAX_LENGTH = defaultVariant.getMaxLength();
	private static int PIN_MIN_LENGTH = defaultVariant.getMinLength();

	private static PinService service = null;
	private static PinServiceConfig config = null;
	private static MySqlConnector mysql = null;

	private static String DEF_VARIANT_ID = PinService.getDefaultVariantID();
	private static String INV_VARIANT_ID = "VAR";

	private String VARIANT_ID;

	public PinServiceTest(String variantID)
	{
		this.VARIANT_ID = variantID;
	}

	// Tests using VARIANT_ID will be run with null and default variant ID "DEF"
	@Parameters
	public static Collection<String[]> createInputValues()
	{
		return Arrays.asList(new String[][] { { DEF_VARIANT_ID }, { null } });
	}

	@Rule
	public TestRule watcher = new TestWatcher()
	{
		@Override
		protected void starting(Description description)
		{
			logger.trace(">>> Starting test [{}]",description.getMethodName());
		}

		@Override
		protected void finished(Description description)
		{
			logger.trace(">>> Ended test [{}]", description.getMethodName());
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Setup
	//
	// /////////////////////////////////
	@BeforeClass
	public static void setup() throws ValidationException
	{
		esb = ServiceBus.getInstance();
		esb.stop();
		
		configureLogging(esb);

		esb.registerService(new TransactionService());
		esb.registerService(new PinService());
		esb.registerService(new NumberPlanService());

		esb.registerConnector(new AirConnector());
		
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerConnector(new SmppConnector());
		esb.registerService(new SecurityService());
		esb.registerConnector(new SoapConnector());
		esb.registerConnector(new CtrlConnector());
		esb.registerConnector(new LifecycleConnector());
		boolean started = esb.start(null);
		assert (started);

		// Get the VAS Soap Interface
		soapConnector = esb.getFirstConnector(ISoapConnector.class);
		assertNotNull("SOAP Connector is NULL!! Connector not starting !", soapConnector);
		vasConnector = soapConnector.getVasInterface();

		// Setup Config
		service = esb.getFirstService(PinService.class);
		SERVICE_ID = service.getServiceID();
		SERVICE_NAME = service.getServiceName(null);

		config = (PinServiceConfig) service.getConfiguration();
		Variant[] variants = config.getVariants();

		for (Variant variant : variants)
		{
			if (variant.getVariantID() == PinService.getDefaultVariantID())
				defaultVariant = variant;
		}

		config.setVariants(new Variant[] { defaultVariant, testVariant });

		PIN_MAX_RETRIES = defaultVariant.getMaxRetries();
		PIN_MAX_LENGTH = defaultVariant.getMaxLength();
		PIN_MIN_LENGTH = defaultVariant.getMinLength();

		mysql = esb.getFirstConnector(MySqlConnector.class);

		logger.trace(">>>>>>>>>> Starting PinServiceTests <<<<<<<<<<");

	}

	// setup

	// ================================================================================================//
	/*
	 * 
	 * TEST LIST:
	 * 
	 * [0] VARIANT_ID = "DEF" [1] VARIANT_ID = null
	 * 
	 * >> Registration << 00 UNSUCCESSFUL Pin Registration - Invalid VariantID 00 SUCCESSFUL Pin Registration 00 SUCCESSFUL Pin Registration - Random pin 00 UNSUCCESSFUL Pin Registration - Invalid
	 * default pin 00 UNSUCCESSFUL Pin Registration - Invalid length 00 UNSUCCESSFUL Pin Registration - Malformed pin 00 UNSUCCESSFUL Pin Registration - Pin already registered
	 * 
	 * >> Change << 00 UNSUCCESSFUL Pin Change - Invalid VariantID 00 SUCCESSFUL Pin Change 00 SUCCESSFUL Pin Change - Multiple times 00 UNSUCCESSFUL Pin Change - Invalid old pin 00 SUCCESSFUL Pin
	 * Change - After failed attempts 00 UNSUCCESSFUL Pin Change - Max Retries + Blocked 00 UNSUCCESSFUL Pin Change - Blocked 00 UNSUCCESSFUL Pin Change - Not registered 00 UNSUCCESSFUL Pin Change -
	 * Invalid length 00 UNSUCCESSFUL Pin Change - Malformed pin 00 UNSUCCESSFUL Pin Change - Default pin 00 SUCCESSFUL Pin Change - Default pin allowed
	 * 
	 * >> Reset << 00 UNSUCCESSFUL Pin Reset - Invalid VariantID 00 SUCCESSFUL Pin Reset 00 UNSUCCESSFUL Pin Reset - Not registered
	 * 
	 * >> Validation << 00 UNSUCCESSFUL Pin Validation - Invalid VariantID 00 SUCCESSFUL Pin Validation 00 SUCCESSFUL Pin Validation - After failed attempts 00 UNSUCCESSFUL Pin Validation - Invalid
	 * pin 00 UNSUCCESSFUL Pin Validation - Invalid length 00 UNSUCCESSFUL Pin Validation - Malformed pin 00 UNSUCCESSFUL Pin Validation - Blocked 00 UNSUCCESSFUL Pin Validation - Max Retries +
	 * Blocked 00 UNSUCCESSFUL Pin Validation - Not registered
	 * 
	 * >> Has Valid Pin << 00 SUCCESSFUL Pin Valid Check 00 UNSUCCESSFUL Pin Valid Check - Not Registered 00 UNSUCCESSFUL Pin Valid Check - Blocked
	 * 
	 * >> Get Service Info << 00 GetServiceInfo Subscription Active 00 GetServiceInfo Subscription Unknown 00 GetServiceInfo Subscription Inactive
	 */

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Test Services
	//
	// /////////////////////////////////
	// @Ignore
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
		assertEquals(2, info.length);
		assertEquals(SERVICE_ID, info[0].getServiceID());
	}

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Registration - Invalid VariantID
	// @Ignore
	@Test
	public void testUnsuccessfulPinRegistrationInvalidVariant()
	{

		clearPin(A_NUMBER, DEF_VARIANT_ID);

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(INV_VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN(defaultVariant.getDefaultPin());
		request.setNewPIN(A_NEW_PIN);

		ChangePINResponse response = vasConnector.changePIN(request);
		validate(ReturnCodes.malformedRequest, request, response);

	}

	// testUnsuccessfulPinRegistrationInvalidVariant()

	// ================================================================================================//

	// 00 SUCCESSFUL Pin Registration
	// @Ignore
	@Test
	public void testSuccessfulPinRegistration()
	{
		clearPin(A_NUMBER, DEF_VARIANT_ID);

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN(defaultVariant.getDefaultPin());
		request.setNewPIN(A_NEW_PIN);

		ChangePINResponse response = vasConnector.changePIN(request);
		validate(ReturnCodes.success, request, response);

	}

	// testSuccessfulPinRegistration()

	// ================================================================================================//

	// 00 SUCCESSFUL Pin Registration - Random pin
	// //@Ignore
	@Test
	public void testSuccessfulPinRegistrationRandomPin()
	{
		clearPin(A_NUMBER, DEF_VARIANT_ID);

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN(defaultVariant.getDefaultPin());

		String newPin = null;
		for (int i = 0; i < 5; i++)
		{
			logger.trace("> Attempt [{}]", Integer.toString(i));
			Random randomGenerator = new Random();
			newPin = RandomString.getRandomString(randomGenerator.nextInt(PIN_MAX_LENGTH - PIN_MIN_LENGTH + 1) + PIN_MIN_LENGTH, RandomString.numbers);
			logger.trace("> Random pin [{}]", newPin);

			request.setNewPIN(newPin);
			ChangePINResponse response = vasConnector.changePIN(request);
			validate(ReturnCodes.success, request, response);
			clearPin(A_NUMBER, DEF_VARIANT_ID);
		}

	}

	// testSuccessfulPinRegistrationRandomPin()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Registration - Invalid default pin
	// @Ignore
	@Test
	public void testUnsuccessfulPinRegistrationInvalidDefaultPin()
	{
		clearPin(A_NUMBER, DEF_VARIANT_ID);

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN("0000");
		request.setNewPIN(A_NEW_PIN);

		ChangePINResponse response = vasConnector.changePIN(request);

		// If the user enters a non-default pin, assume it is a pin change request
		// and inform the user that they are unregistered
		validate(ReturnCodes.unregisteredPin, request, response);

	}

	// testUnsuccessfulPinRegistrationInvalidDefaultPin()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Registration - Invalid length
	// @Ignore
	@Test
	public void testUnsuccessfulPinRegistrationInvalidPinLength()
	{
		clearPin(A_NUMBER, DEF_VARIANT_ID);

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN(defaultVariant.getDefaultPin());

		// Pin too long
		logger.trace("> Pin too long attempt.");
		request.setNewPIN(A_LONG_PIN);
		ChangePINResponse response = vasConnector.changePIN(request);
		validate(ReturnCodes.invalidPin, request, response);

		// Pin too short
		logger.trace("> Pin too short attempt.");
		request.setNewPIN(A_SHORT_PIN);
		ChangePINResponse response2 = vasConnector.changePIN(request);
		validate(ReturnCodes.invalidPin, request, response2);

	}

	// testUnsuccessfulPinRegistrationInvalidPinLength()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Registration - Malformed Pin
	// @Ignore
	@Test
	public void testUnsuccessfulPinRegistrationMalformedPin()
	{
		clearPin(A_NUMBER, DEF_VARIANT_ID);

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN(defaultVariant.getDefaultPin());

		String newPin = null;

		// Make multiple registration attempts to a randomly generated pin containing invalid characters
		for (int i = 0; i < PIN_MAX_RETRIES; i++)
		{
			Random randomGenerator = new Random();
			newPin = RandomString.getRandomString(randomGenerator.nextInt(PIN_MAX_LENGTH - PIN_MIN_LENGTH + 1) + PIN_MIN_LENGTH, RandomString.characters);
			logger.trace("> Random pin [{}]", newPin);
			request.setNewPIN(newPin);
			ChangePINResponse response = vasConnector.changePIN(request);
			validate(ReturnCodes.invalidPin, request, response);
		}

	}

	// testUnsuccessfulPinRegistrationMalformedPin()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Registration - Pin already registered
	// @Ignore
	@Test
	public void testUnsuccessfulPinRegistrationPinAlreadyRegistered()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN(defaultVariant.getDefaultPin());
		request.setNewPIN(A_NEW_PIN);

		ChangePINResponse response = vasConnector.changePIN(request);
		validate(ReturnCodes.alreadyAdded, request, response);

	}

	// testUnsuccessfulPinRegistrationPinAlreadyRegistered()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Change - Invalid VariantID
	// @Ignore
	@Test
	public void testUnsuccessfulPinChangeInvalidVariant()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(INV_VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN(A_OLD_PIN2);
		request.setNewPIN(A_NEW_PIN2);

		ChangePINResponse response = vasConnector.changePIN(request);
		validate(ReturnCodes.malformedRequest, request, response);

	}

	// testUnsuccessfulPinChangeInvalidVariant()

	// ================================================================================================//

	// 00 SUCCESSFUL Pin Change
	// @Ignore
	@Test
	public void testSuccessfulPinChange()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN(A_OLD_PIN2);
		request.setNewPIN(A_NEW_PIN2);

		ChangePINResponse response = vasConnector.changePIN(request);
		validate(ReturnCodes.success, request, response);

	}

	// testSuccessfulPinChange()

	// ================================================================================================//

	// 00 SUCCESSFUL Pin Change - Multiple times
	// @Ignore
	@Test
	public void testSuccessfulPinChangeMultipleTimes()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN(A_OLD_PIN2);

		String newPin = null;
		for (int i = 0; i < 5; i++)
		{
			Random randomGenerator = new Random();
			newPin = RandomString.getRandomString(randomGenerator.nextInt(PIN_MAX_LENGTH - PIN_MIN_LENGTH + 1) + PIN_MIN_LENGTH, RandomString.numbers);
			logger.trace("> Random pin [{}]", newPin);

			request.setNewPIN(newPin);
			ChangePINResponse response = vasConnector.changePIN(request);
			validate(ReturnCodes.success, request, response);

			// Failure count should remain the same
			assertEquals(0, getFailureCount(A_NUMBER, DEF_VARIANT_ID));

			request.setOldPIN(newPin);
		}

	}

	// testSuccessfulPinChangeMultipleTimes()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Change - invalid old pin
	// @Ignore
	@Test
	public void testUnsuccessfulPinChangeInvalidPin()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN(A_INCORRECT_PIN);
		request.setNewPIN(A_NEW_PIN2);

		// Attempt 1
		logger.trace("> Attempt 1");
		ChangePINResponse response = vasConnector.changePIN(request);
		validate(ReturnCodes.invalidPin, request, response);
		assertEquals(1, getFailureCount(A_NUMBER, DEF_VARIANT_ID));

		// Attempt 2
		logger.trace("> Attempt 2");
		ChangePINResponse response2 = vasConnector.changePIN(request);
		validate(ReturnCodes.invalidPin, request, response2);
		assertEquals(2, getFailureCount(A_NUMBER, DEF_VARIANT_ID));

	}

	// testUnsuccessfulPinChangeInvalidPin()

	// ================================================================================================//

	// 00 SUCCESSFUL Pin Change - After failed attempts
	// @Ignore
	@Test
	public void testSuccessfulPinChangeAfterFailure()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN(A_INCORRECT_PIN);
		request.setNewPIN(A_NEW_PIN2);

		// Attempt 1 - Fail
		logger.trace("> Attempt 1");
		ChangePINResponse response = vasConnector.changePIN(request);
		validate(ReturnCodes.invalidPin, request, response);
		assertEquals(1, getFailureCount(A_NUMBER, DEF_VARIANT_ID));

		// Failure count should increment
		assertEquals(1, getFailureCount(A_NUMBER, DEF_VARIANT_ID));

		// Set correct pin
		request.setOldPIN(A_OLD_PIN2);

		// Attempt 2 - Pass
		logger.trace("> Attempt 2");
		ChangePINResponse response2 = vasConnector.changePIN(request);
		validate(ReturnCodes.success, request, response2);

		// Failure count should be reset
		assertEquals(0, getFailureCount(A_NUMBER, DEF_VARIANT_ID));

	}

	// testSuccessfulPinChangeAfterFailure()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Change - Max Retries + Blocked
	// @Ignore
	@Test
	public void testUnsuccessfulPinChangeMaxRetriesBlocked()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN(A_INCORRECT_PIN);
		request.setNewPIN(A_NEW_PIN2);

		// Attempts 1 - Max retires+1
		for (int i = 0; i < PIN_MAX_RETRIES + 1; i++)
		{
			logger.trace("> Attempt {}", i + 1);
			ChangePINResponse response = vasConnector.changePIN(request);

			// Expect invalidPin response until max retries, after which pin will be blocked
			validate(i < PIN_MAX_RETRIES - 1 ? ReturnCodes.invalidPin : ReturnCodes.pinBlocked, request, response);

			// Expect retry count to increase until pin is blocked, after which it will stay the same
			assertEquals(i < PIN_MAX_RETRIES - 1 ? i + 1 : PIN_MAX_RETRIES, getFailureCount(A_NUMBER, DEF_VARIANT_ID));
		}

		// Set correct pin
		request.setOldPIN(A_OLD_PIN2);

		// Correct Attempt
		logger.trace("> Correct Attempt");
		ChangePINResponse response2 = vasConnector.changePIN(request);
		validate(ReturnCodes.pinBlocked, request, response2);

		// Failure count should remain the same
		assertEquals(PIN_MAX_RETRIES, getFailureCount(A_NUMBER, DEF_VARIANT_ID));

	}

	// testUnsuccessfulPinChangeMaxRetriesBlocked()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Change - Blocked
	// @Ignore
	@Test
	public void testUnsuccessfulPinChangeBlocked()
	{
		// Insert a blocked pin
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, PIN_MAX_RETRIES, true, new Date()));

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN(A_OLD_PIN2);
		request.setNewPIN(A_NEW_PIN2);

		ChangePINResponse response = vasConnector.changePIN(request);
		validate(ReturnCodes.pinBlocked, request, response);

		// Failure count should remain the same
		assertEquals(PIN_MAX_RETRIES, getFailureCount(A_NUMBER, DEF_VARIANT_ID));

	}

	// testUnsuccessfulPinChangeBlocked()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Change - Not registered
	// @Ignore
	@Test
	public void testUnsuccessfulPinNotRegistered()
	{
		clearPin(A_NUMBER, DEF_VARIANT_ID);

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN(A_OLD_PIN2);
		request.setNewPIN(A_NEW_PIN2);

		ChangePINResponse response = vasConnector.changePIN(request);
		validate(ReturnCodes.unregisteredPin, request, response);

	}

	// testUnsuccessfulPinNotRegistered()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Change - invalid length
	// @Ignore
	@Test
	public void testUnsuccessfulPinChangeInvalidLength()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN(A_OLD_PIN2);

		// Pin too long
		logger.trace("> Pin too long attempt.");
		request.setNewPIN(A_LONG_PIN);
		ChangePINResponse response = vasConnector.changePIN(request);
		validate(ReturnCodes.invalidPin, request, response);

		// Failure count should remain the same
		assertEquals(0, getFailureCount(A_NUMBER, DEF_VARIANT_ID));

		// Pin too short
		logger.trace("> Pin too short attempt.");
		request.setNewPIN(A_SHORT_PIN);
		ChangePINResponse response2 = vasConnector.changePIN(request);
		validate(ReturnCodes.invalidPin, request, response2);

		// Failure count should remain the same
		assertEquals(0, getFailureCount(A_NUMBER, DEF_VARIANT_ID));

	}

	// testUnsuccessfulPinChangeInvalidLength()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Change - Malformed pin
	// @Ignore
	@Test
	public void testUnsuccessfulPinChangeMalformedPin()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN(A_OLD_PIN2);

		String newPin = null;

		// Make multiple change attempts to a randomly generated pin containing invalid characters
		for (int i = 0; i < PIN_MAX_RETRIES; i++)
		{
			Random randomGenerator = new Random();
			newPin = RandomString.getRandomString(randomGenerator.nextInt(3) + 4, RandomString.characters);
			logger.trace("> Random pin [{}]", newPin);
			request.setNewPIN(newPin);
			ChangePINResponse response = vasConnector.changePIN(request);
			validate(ReturnCodes.invalidPin, request, response);

			// Failure count should remain the same
			assertEquals(0, getFailureCount(A_NUMBER, DEF_VARIANT_ID));
		}

	}

	// testUnsuccessfulPinChangeMalformedPin()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Change - Default pin
	// @Ignore
	@Test
	public void testUnsuccessfulPinChangeDefaultPin()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN(A_OLD_PIN2);
		request.setNewPIN(defaultVariant.getDefaultPin());

		ChangePINResponse response = vasConnector.changePIN(request);
		validate(ReturnCodes.invalidPin, request, response);
		assertEquals(0, getFailureCount(A_NUMBER, DEF_VARIANT_ID));

	}

	// testUnsuccessfulPinChangeDefaultPin()

	// ================================================================================================//

	// 00 SUCCESSFUL Pin Change - Default pin allowed
	// @Ignore
	@Test
	public void testSuccessfulPinChangeDefaultPin()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, testVariant.getVariantID(), A_OLD_PIN2, 0, false, new Date()));

		ChangePINRequest request = new ChangePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(testVariant.getVariantID());
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setOldPIN(A_OLD_PIN2);
		request.setNewPIN(testVariant.getDefaultPin());

		ChangePINResponse response = vasConnector.changePIN(request);
		validate(ReturnCodes.success, request, response);
		assertEquals(0, getFailureCount(A_NUMBER, testVariant.getVariantID()));

	}

	// testUnsuccessfulPinChangeDefaultPin()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Reset - Invalid VariantID
	// @Ignore
	@Test
	public void testUnsuccessfulPinResetInvalidVariant()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		ResetPINRequest request = new ResetPINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(INV_VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		ResetPINResponse response = vasConnector.resetPIN(request);
		validate(ReturnCodes.malformedRequest, request, response);

	}

	// testUnsuccessfulPinResetInvalidVariant()

	// ================================================================================================//

	// 00 SUCCESSFUL Pin Reset
	// @Ignore
	@Test
	public void testSuccessfulPinReset()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		ResetPINRequest request = new ResetPINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		ResetPINResponse response = vasConnector.resetPIN(request);
		validate(ReturnCodes.success, request, response);

	}

	// testSuccessfulPinReset()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Reset - Not registered
	// @Ignore
	@Test
	public void testUnsuccessfulPinResetNotRegistered()
	{
		clearPin(A_NUMBER, DEF_VARIANT_ID);

		ResetPINRequest request = new ResetPINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		ResetPINResponse response = vasConnector.resetPIN(request);
		validate(ReturnCodes.unregisteredPin, request, response);

	}

	// testUnsuccessfulPinResetNotRegistered()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Validation - Invalid VariantID
	// @Ignore
	@Test
	public void testUnsuccessfulPinValidationInvalidVariant()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		ValidatePINRequest request = new ValidatePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(INV_VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setPIN(A_OLD_PIN2);

		ValidatePINResponse response = vasConnector.validatePIN(request);
		validate(ReturnCodes.malformedRequest, request, response);

	}

	// testUnsuccessfulPinValidationInvalidVariant()

	// ================================================================================================//

	// 00 SUCCESSFUL Pin Validation
	// @Ignore
	@Test
	public void testSuccessfulPinValidation()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		ValidatePINRequest request = new ValidatePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setPIN(A_OLD_PIN2);

		ValidatePINResponse response = vasConnector.validatePIN(request);
		validate(ReturnCodes.success, request, response);

	}

	// testSuccessfulPinValidation()

	// ================================================================================================//

	// 00 SUCCESSFUL Pin Validation - After failed attempts
	// @Ignore
	@Test
	public void testSuccessfulPinValidationAfterFailure()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		ValidatePINRequest request = new ValidatePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setPIN(A_NEW_PIN2);

		// Attempt 1
		logger.trace("> Attempt 1");
		ValidatePINResponse response = vasConnector.validatePIN(request);
		validate(ReturnCodes.invalidPin, request, response);
		assertEquals(1, getFailureCount(A_NUMBER, DEF_VARIANT_ID));

		// Failure count should increment
		assertEquals(1, getFailureCount(A_NUMBER, DEF_VARIANT_ID));

		// Set correct pin
		request.setPIN(A_OLD_PIN2);

		// Attempt 2
		logger.trace("> Attempt 2");
		ValidatePINResponse response2 = vasConnector.validatePIN(request);
		validate(ReturnCodes.success, request, response2);

		// Failure count should be reset
		assertEquals(0, getFailureCount(A_NUMBER, DEF_VARIANT_ID));

	}

	// testSuccessfulPinValidationAfterFailure()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Validation - Invalid pin
	// @Ignore
	@Test
	public void testUnsuccessfulPinValidationInvalidPin()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		ValidatePINRequest request = new ValidatePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setPIN(A_NEW_PIN2);

		// Attempt 1
		logger.trace("> Attempt 1");
		ValidatePINResponse response = vasConnector.validatePIN(request);
		validate(ReturnCodes.invalidPin, request, response);
		assertEquals(1, getFailureCount(A_NUMBER, DEF_VARIANT_ID));

		// Attempt 2
		logger.trace("> Attempt 2");
		ValidatePINResponse response2 = vasConnector.validatePIN(request);
		validate(ReturnCodes.invalidPin, request, response2);
		assertEquals(2, getFailureCount(A_NUMBER, DEF_VARIANT_ID));

	}

	// testUnsuccessfulPinValidationInvalidPin()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Validation - Invalid length
	// @Ignore
	@Test
	public void testUnsuccessfulPinValidationInvalidLength()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		ValidatePINRequest request = new ValidatePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		// Pin too long
		logger.trace("> Pin too long attempt.");
		request.setPIN(A_LONG_PIN);
		ValidatePINResponse response = vasConnector.validatePIN(request);
		validate(ReturnCodes.invalidPin, request, response);

		// Pin too short
		logger.trace("> Pin too short attempt.");
		request.setPIN(A_SHORT_PIN);
		ValidatePINResponse response2 = vasConnector.validatePIN(request);
		validate(ReturnCodes.invalidPin, request, response2);

		// Failure count should increment
		assertEquals(2, getFailureCount(A_NUMBER, DEF_VARIANT_ID));

	}

	// testUnsuccessfulPinValidationInvalidLength()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Validation - Malformed pin
	// @Ignore
	@Test
	public void testUnsuccessfulPinValidationMalformedPin()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		ValidatePINRequest request = new ValidatePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);

		String pin = null;

		// Make multiple validation attempts to a randomly generated pin containing invalid characters
		for (int i = 0; i < PIN_MAX_RETRIES - 1; i++)
		{
			Random randomGenerator = new Random();
			pin = RandomString.getRandomString(randomGenerator.nextInt(PIN_MAX_LENGTH - PIN_MIN_LENGTH + 1) + PIN_MIN_LENGTH, RandomString.characters);
			logger.trace("> Random pin [{}]", pin);
			request.setPIN(pin);
			ValidatePINResponse response = vasConnector.validatePIN(request);
			validate(ReturnCodes.invalidPin, request, response);

			// Failure count should increment
			assertEquals(i + 1, getFailureCount(A_NUMBER, DEF_VARIANT_ID));
		}

	}

	// testUnsuccessfulPinValidationMalformedPin()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Validation - Blocked
	// @Ignore
	@Test
	public void testUnsuccessfulPinValidationBlocked()
	{
		// Create blocked pin
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, PIN_MAX_RETRIES, true, new Date()));

		ValidatePINRequest request = new ValidatePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setPIN(A_OLD_PIN2);

		ValidatePINResponse response = vasConnector.validatePIN(request);
		validate(ReturnCodes.pinBlocked, request, response);
		assertEquals(PIN_MAX_RETRIES, getFailureCount(A_NUMBER, DEF_VARIANT_ID));

	}

	// testUnsuccessfulPinValidationBlocked()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Validation - Max Retries + Blocked
	// @Ignore
	@Test
	public void testUnsuccessfulPinValidationMaxRetriesBlocked()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		ValidatePINRequest request = new ValidatePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setPIN(A_NEW_PIN2);

		// Attempts 1 - 5
		for (int i = 0; i < PIN_MAX_RETRIES + 1; i++)
		{
			logger.trace("> Attempt {}", i + 1);
			ValidatePINResponse response = vasConnector.validatePIN(request);

			// Expect invalidPin response until max retries, after which pin will be blocked
			validate(i < PIN_MAX_RETRIES - 1 ? ReturnCodes.invalidPin : ReturnCodes.pinBlocked, request, response);

			// Expect retry count to increase until pin is blocked, after which it will stay the same
			assertEquals(i < PIN_MAX_RETRIES - 1 ? i + 1 : PIN_MAX_RETRIES, getFailureCount(A_NUMBER, DEF_VARIANT_ID));
		}

		// Set correct pin
		request.setPIN(A_OLD_PIN2);

		// Correct Attempt
		logger.trace("> Correct Attempt");
		ValidatePINResponse response2 = vasConnector.validatePIN(request);
		validate(ReturnCodes.pinBlocked, request, response2);

		// Failure count should remain the same
		assertEquals(PIN_MAX_RETRIES, getFailureCount(A_NUMBER, DEF_VARIANT_ID));

	}

	// testUnsuccessfulPinValidationMaxRetriesBlocked()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Validation - Not Registered
	// @Ignore
	@Test
	public void testUnsuccessfulPinValidationNotRegistered()
	{
		clearPin(A_NUMBER, DEF_VARIANT_ID);

		ValidatePINRequest request = new ValidatePINRequest();
		initialize(request);
		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		Number A_number = new Number(A_NUMBER);
		A_number.setNumberPlan(NumberPlan.NATIONAL);
		request.setSubscriberNumber(A_number);
		request.setPIN(A_OLD_PIN2);

		ValidatePINResponse response = vasConnector.validatePIN(request);
		validate(ReturnCodes.unregisteredPin, request, response);

	}

	// testUnsuccessfulPinValidation()

	// ================================================================================================//

	// 00 SUCCESSFUL Pin Valid Check
	// @Ignore
	@Test
	public void testSuccessfulPinValidCheck()
	{
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		try (MySqlConnection db = mysql.getConnection(null))
		{
			ValidatePINRequest request = new ValidatePINRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(VARIANT_ID);
			Number A_number = new Number(A_NUMBER);
			A_number.setNumberPlan(NumberPlan.NATIONAL);
			request.setSubscriberNumber(A_number);
			request.setPIN(A_OLD_PIN2);

			ValidatePINResponse response = vasConnector.validatePIN(request);
			validate(ReturnCodes.success, request, response);
			CdrBase cdr = new CsvCdr();
			assertTrue(service.hasValidPIN(response, cdr, db, DEF_VARIANT_ID, A_NUMBER));
		}
		catch (Throwable sqle)
		{
			logger.error(sqle.getLocalizedMessage());
			fail("Mysql Exception: " + sqle.getLocalizedMessage());
		}

	}

	// testSuccessfulPinValidCheck()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Valid Check - Not Registered
	// @Ignore
	@Test
	public void testUnsuccessfulPinValidCheckNotRegistered()
	{
		clearPin(A_NUMBER, DEF_VARIANT_ID);

		try (MySqlConnection db = mysql.getConnection(null))
		{
			ValidatePINRequest request = new ValidatePINRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(VARIANT_ID);
			Number A_number = new Number(A_NUMBER);
			A_number.setNumberPlan(NumberPlan.NATIONAL);
			request.setSubscriberNumber(A_number);
			request.setPIN(A_OLD_PIN2);

			ValidatePINResponse response = vasConnector.validatePIN(request);
			validate(ReturnCodes.unregisteredPin, request, response);
			CdrBase cdr = new CsvCdr();
			assertFalse(service.hasValidPIN(response, cdr, db, DEF_VARIANT_ID, A_NUMBER));
		}
		catch (Throwable sqle)
		{
			logger.error(sqle.getLocalizedMessage());
			fail("Mysql Exception: " + sqle.getLocalizedMessage());
		}

	}

	// testUnsuccessfulPinValidCheckNotRegistered()

	// ================================================================================================//

	// 00 UNSUCCESSFUL Pin Valid Check - Blocked
	// @Ignore
	@Test
	public void testUnsuccessfulPinValidCheckBlocked()
	{
		// Insert blocked PIN
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, true, new Date()));

		try (MySqlConnection db = mysql.getConnection(null))
		{
			ValidatePINRequest request = new ValidatePINRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(VARIANT_ID);
			Number A_number = new Number(A_NUMBER);
			A_number.setNumberPlan(NumberPlan.NATIONAL);
			request.setSubscriberNumber(A_number);
			request.setPIN(A_OLD_PIN2);

			ValidatePINResponse response = vasConnector.validatePIN(request);
			validate(ReturnCodes.pinBlocked, request, response);
			CdrBase cdr = new CsvCdr();
			assertFalse(service.hasValidPIN(response, cdr, db, DEF_VARIANT_ID, A_NUMBER));
		}
		catch (Throwable sqle)
		{
			logger.error(sqle.getLocalizedMessage());
			fail("Mysql Exception: " + sqle.getLocalizedMessage());
		}

	}

	// testUnsuccessfulPinValidCheckNotRegistered()

	// ================================================================================================//

	// 00 GetServiceInfo Subscription Active
	// @Ignore
	@Test
	public void testGetServiceInfoSubscriptionActive()
	{
		// Insert PIN
		insertPin(new Pin(A_NUMBER, SERVICE_ID, DEF_VARIANT_ID, A_OLD_PIN2, 0, false, new Date()));

		try (MySqlConnection db = mysql.getConnection(null))
		{
			GetServicesRequest request = new GetServicesRequest();
			initialize(request);
			request.setActiveOnly(false);
			request.setSubscriberNumber(new Number(A_NUMBER));

			GetServicesResponse response = vasConnector.getServices(request);
			validate(ReturnCodes.success, request, response);
			VasServiceInfo[] info = response.getServiceInfo();

			assertEquals(SubscriptionState.active, info[0].getState());
			assertEquals(SubscriptionState.active, info[1].getState());
		}
		catch (Throwable sqle)
		{
			logger.error(sqle.getLocalizedMessage());
			fail("Mysql Exception: " + sqle.getLocalizedMessage());
		}

	}

	// testGetServiceInfoSubscriptionActive()

	// ================================================================================================//

	// 00 GetServiceInfo Subscription Unknown
	// @Ignore
	@Test
	public void testGetServiceInfoSubscriptionUnknown()
	{
		// Clear PIN
		clearPin(A_NUMBER, DEF_VARIANT_ID);

		try (MySqlConnection db = mysql.getConnection(null))
		{
			// Test with null subscriber number

			GetServicesRequest request = new GetServicesRequest();
			initialize(request);
			request.setActiveOnly(false);

			GetServicesResponse response = vasConnector.getServices(request);
			validate(ReturnCodes.success, request, response);
			VasServiceInfo[] info = response.getServiceInfo();

			assertEquals(SubscriptionState.unknown, info[0].getState());
			assertEquals(SubscriptionState.unknown, info[1].getState());

			// Test with empty subscriber number
			GetServicesRequest request2 = new GetServicesRequest();
			initialize(request2);
			request2.setActiveOnly(false);
			request2.setSubscriberNumber(new Number(""));

			GetServicesResponse response2 = vasConnector.getServices(request2);
			validate(ReturnCodes.success, request2, response2);
			VasServiceInfo[] info2 = response2.getServiceInfo();

			assertEquals(SubscriptionState.unknown, info2[0].getState());
			assertEquals(SubscriptionState.unknown, info2[1].getState());

		}
		catch (Throwable sqle)
		{
			logger.error(sqle.getLocalizedMessage());
			fail("Mysql Exception: " + sqle.getLocalizedMessage());
		}

	}

	// testGetServiceInfoSubscriptionUnknown()

	// ================================================================================================//

	// 00 GetServiceInfo Subscription Inactive
	// @Ignore
	@Test
	public void testGetServiceInfoSubscriptionInactive()
	{
		// Clear PIN
		clearPin(A_NUMBER, DEF_VARIANT_ID);

		try (MySqlConnection db = mysql.getConnection(null))
		{

			GetServicesRequest request = new GetServicesRequest();
			initialize(request);
			request.setActiveOnly(false);
			request.setSubscriberNumber(new Number(A_NUMBER));

			GetServicesResponse response = vasConnector.getServices(request);
			validate(ReturnCodes.success, request, response);
			VasServiceInfo[] info = response.getServiceInfo();

			assertEquals(SubscriptionState.notActive, info[0].getState());
			assertEquals(SubscriptionState.notActive, info[1].getState());

		}
		catch (Throwable sqle)
		{
			logger.error(sqle.getLocalizedMessage());
			fail("Mysql Exception: " + sqle.getLocalizedMessage());
		}

	}

	// testGetServiceInfoSubscriptionInactive()

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
		assertEquals(code, response.getReturnCode());
		assertEquals(request.getTransactionID(), response.getTransactionId());
		assertEquals(request.getSessionID(), response.getSessionId());
	}

	// //////////////////////////////////
	// Pin Management
	// ////////////////

	// Removes the PIN from the DB for specific msisdn
	public static void clearPin(String msisdn, String variantID, MySqlConnector mysql)
	{

		Pin dbPin = null;

		// Get pin from DB
		try (MySqlConnection db = mysql.getConnection(null))
		{
			dbPin = db.select(Pin.class, "where msisdn = %s and serviceId = %s and variantID = %s", msisdn, SERVICE_ID, variantID);
			if (dbPin != null)
				db.delete(dbPin);
		}
		catch (Throwable sqle)
		{
			logger.error(sqle.getLocalizedMessage());
		}
	}

	// Creates a pin on the db specified by Pin
	public static void insertPin(Pin dbPin, MySqlConnector mysql)
	{

		dbPin.setServiceID(SERVICE_ID);

		// Clear existing PIN
		clearPin(dbPin.getMsisdn(), dbPin.getVariantID(), mysql);

		// insert PIN into DB
		try (MySqlConnection db = mysql.getConnection(null))
		{
			db.insert(dbPin);
		}
		catch (Throwable sqle)
		{
			logger.error(sqle.getLocalizedMessage());
		}
	}

	// Removes the PIN from the DB for specific msisdn
	public static void clearPin(String msisdn, String variantID)
	{

		Pin dbPin = null;

		// Get pin from DB
		try (MySqlConnection db = mysql.getConnection(null))
		{
			dbPin = db.select(Pin.class, "where msisdn = %s and serviceId = %s and variantID = %s", msisdn, SERVICE_ID, variantID);
			if (dbPin != null)
				db.delete(dbPin);
		}
		catch (Throwable sqle)
		{
			logger.error(sqle.getLocalizedMessage());
		}
	}

	// Creates a pin on the db specified by Pin
	private static void insertPin(Pin dbPin)
	{

		dbPin.setServiceID(SERVICE_ID);

		// Clear existing PIN
		clearPin(dbPin.getMsisdn(), dbPin.getVariantID());

		// insert PIN into DB
		try (MySqlConnection db = mysql.getConnection(null))
		{
			db.insert(dbPin);
		}
		catch (Throwable sqle)
		{
			logger.error(sqle.getLocalizedMessage());
		}
	}

	// Gets the PIN failed attempt count
	private static int getFailureCount(String msisdn, String variantID)
	{

		int failureCount = -1;

		Pin dbPin = null;

		try (MySqlConnection db = mysql.getConnection(null))
		{
			dbPin = db.select(Pin.class, "where msisdn = %s and serviceId = %s and variantID = %s", msisdn, SERVICE_ID, variantID);
			if (dbPin != null)
				failureCount = dbPin.getFailedCount();
		}
		catch (Throwable sqle)
		{
			logger.error(sqle.getLocalizedMessage());
		}

		return failureCount;
	}

	// Generates random strings for PIN format testing
	protected static class RandomString
	{
		private static String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ -`~!@#$%^&*()_+=-,<.>/?;:'\"|\\[{]}";
		private static String numbers = "0123456789";

		protected static String getRandomString(int length, String inputString)
		{
			String randomString = "";
			Random randomGenerator = new Random();

			for (int i = 0; i < length; i++)
			{
				int randomInt = randomGenerator.nextInt(inputString.length());
				randomString = randomString + inputString.charAt(randomInt);
			}
			return randomString;
		}
	}

}
