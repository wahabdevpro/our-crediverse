package hxc.services.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.concurrent.hxc.IHxC;
import com.concurrent.hxc.MigrateRequest;
import com.concurrent.hxc.MigrateResponse;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.RequestHeader;

import hxc.configuration.ValidationException;
import hxc.connectors.Channels;
import hxc.connectors.air.AirConnector;
import hxc.connectors.air.AirConnector.AirConnectionConfig;
import hxc.connectors.air.AirConnector.AirConnectorConfig;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.soap.ISoapConnector;
import hxc.connectors.soap.SoapConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.servicebus.ServiceBus;
import hxc.services.airsim.AirSim;
import hxc.services.airsim.protocol.IAirSim;
import hxc.services.airsim.protocol.Subscriber;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.services.language.LanguageService.LanguageChangeConfig;
import hxc.services.numberplan.INumberPlan;
import hxc.services.numberplan.NumberPlanService;
import hxc.services.security.SecurityService;
import hxc.services.transactions.TransactionService;
import hxc.testsuite.RunAllTestsBase;

public class LanguageChangeTest extends RunAllTestsBase
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private String A_NUMBER = "08244526545";
	private String SERVICE_ID = "LangCh";
	private String FRENCH = "1";
	private String ENGLISH = "2";

	private static IAirSim airSimulator = null;

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
		esb.registerService(new LanguageService());
		AirConnector air = new AirConnector();

		AirConnectorConfig airConfig = (AirConnectorConfig) air.getConfiguration();
		AirConnectionConfig conConfig = (AirConnectionConfig) airConfig.getConfigurations().iterator().next();
		conConfig.setUri("http://127.0.0.1:10011/Air");

		esb.registerConnector(air);
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerService(new SecurityService());
		esb.registerConnector(new SoapConnector());
		esb.registerService(new NumberPlanService());
		boolean started = esb.start(null);
		assert (started);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Test Credit Sharing
	//
	// /////////////////////////////////
	@Test
	public void testCreditSharing() throws ValidationException, IOException, SQLException
	{
		long balance = 1200L;
		final int CHARGE_MIG = 10;

		// Setup Simulator
		INumberPlan numberPlan = esb.getFirstService(INumberPlan.class);
		airSimulator = new AirSim(esb, 10011, "/Air", numberPlan, "CFR");
		assertTrue(airSimulator.start());
		Subscriber subscriberA = airSimulator.addSubscriber(A_NUMBER, 1, 102, balance, SubscriberState.active);

		// Setup Config
		LanguageService service = esb.getFirstService(LanguageService.class);
		LanguageChangeConfig config = (LanguageChangeConfig) service.getConfiguration();
		config.setMigrationCharge(CHARGE_MIG);

		// Get the VAS Soap Interface
		ISoapConnector soapConnector = esb.getFirstConnector(ISoapConnector.class);
		assertNotNull("SOAP Connector is NULL!! Connector not starting !", soapConnector);
		IHxC vasConnector = soapConnector.getVasInterface();

		// Migrate French to Invalid
		{
			MigrateRequest request = new MigrateRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(FRENCH);
			request.setNewServiceID(SERVICE_ID);
			request.setNewVariantID("23");
			request.setSubscriberNumber(new Number(A_NUMBER));
			MigrateResponse response = vasConnector.migrate(request);
			assertEquals(ReturnCodes.malformedRequest, response.getReturnCode());
			assertEquals(request.getTransactionID(), response.getTransactionId());
			assertEquals(request.getSessionID(), response.getSessionId());
			assertEquals(1, subscriberA.getLanguageIDCurrent());
			assertEquals(balance, (long) subscriberA.getAccountValue1());
		}

		// Migrate French to French
		{
			MigrateRequest request = new MigrateRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(FRENCH);
			request.setNewServiceID(SERVICE_ID);
			request.setNewVariantID(FRENCH);
			request.setSubscriberNumber(new Number(A_NUMBER));
			MigrateResponse response = vasConnector.migrate(request);
			assertEquals(ReturnCodes.alreadySubscribed, response.getReturnCode());
			assertEquals(request.getTransactionID(), response.getTransactionId());
			assertEquals(request.getSessionID(), response.getSessionId());
			assertEquals(1, subscriberA.getLanguageIDCurrent());
			assertEquals(balance, (long) subscriberA.getAccountValue1());
		}

		// Migrate French to English
		{
			MigrateRequest request = new MigrateRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(FRENCH);
			request.setNewServiceID(SERVICE_ID);
			request.setNewVariantID(ENGLISH);
			request.setSubscriberNumber(new Number(A_NUMBER));
			MigrateResponse response = vasConnector.migrate(request);
			assertEquals(ReturnCodes.success, response.getReturnCode());
			assertEquals(request.getTransactionID(), response.getTransactionId());
			assertEquals(request.getSessionID(), response.getSessionId());
			assertEquals(2, subscriberA.getLanguageIDCurrent());
			balance -= CHARGE_MIG;
			assertEquals(balance, (long) subscriberA.getAccountValue1());
		}

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
		request.setLanguageID(1);
		request.setVersion("1");
		request.setMode(RequestModes.normal);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Teardown
	//
	// /////////////////////////////////

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		if (airSimulator != null)
			airSimulator.stop();
		esb.stop();
	}

}
