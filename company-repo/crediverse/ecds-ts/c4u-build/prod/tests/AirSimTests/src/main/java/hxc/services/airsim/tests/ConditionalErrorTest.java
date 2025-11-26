package hxc.services.airsim.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.ValidationException;
import hxc.connectors.air.AirConnector;
import hxc.connectors.air.AirConnector.AirConnectionConfig;
import hxc.connectors.air.AirConnector.AirConnectorConfig;
import hxc.connectors.air.AirException;
import hxc.connectors.air.IAirConnection;
import hxc.connectors.air.IRequestHeader;
import hxc.connectors.air.IResponseHeader;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.airsim.AirSim;
import hxc.services.airsim.protocol.AirCalls;
import hxc.services.airsim.protocol.IAirSim;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.services.logging.LoggerService;
import hxc.services.numberplan.NumberPlanService;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.protocol.ucip.GetBalanceAndDateRequest;
import hxc.utils.protocol.ucip.GetBalanceAndDateRequestMember;
import hxc.utils.protocol.ucip.GetBalanceAndDateResponse;

public class ConditionalErrorTest extends RunAllTestsBase
{
	final static Logger logger = LoggerFactory.getLogger(ConditionalErrorTest.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private static IAirSim airSimulator = null;
	private static AirConnector air;

	private static final int LANGUAGE_ID = 1;
	private static final String MSISDN_A = "0824452655";

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
		esb.registerService(new LoggerService());
		air = new AirConnector();

		AirConnectorConfig airConfig = (AirConnectorConfig) air.getConfiguration();
		AirConnectionConfig conConfig = (AirConnectionConfig) airConfig.getConfigurations().iterator().next();
		conConfig.setUri("http://127.0.0.1:10011/Air");

		NumberPlanService numberPlan = new NumberPlanService();
		esb.registerService(numberPlan);

		esb.registerConnector(air);
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		airSimulator = new AirSim(esb, 10011, "/Air", numberPlan, "CFR");

		boolean started = esb.start(null);
		assert (started);
		airSimulator.start();

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Tear Down
	//
	// /////////////////////////////////
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		airSimulator.stop();
		esb.stop();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	// /////////////////////////////////

	@Test
	public void testConditionalError()
	{
		airSimulator.reset();
		airSimulator.addSubscribers("07245566612", 2, 1, 12, 5000, SubscriberState.active);

		IAirConnection airConn = air.getConnection(null);

		airSimulator.addSubscriber(MSISDN_A, LANGUAGE_ID, 1, 1000, SubscriberState.active);

		try
		{

			// GBD
			{
				GetBalanceAndDateRequest request = new GetBalanceAndDateRequest();
				request.member = new GetBalanceAndDateRequestMember();
				initialize(request.member);
				GetBalanceAndDateResponse response = airConn.getBalanceAndDate(request);
				validate(request.member, response.member);
			}

			airSimulator.injectSelectiveResponse(AirCalls.GetBalanceAndDate, 101, 1000, 1, 1);

			// GBD
			{
				GetBalanceAndDateRequest request = new GetBalanceAndDateRequest();
				request.member = new GetBalanceAndDateRequestMember();
				initialize(request.member);
				GetBalanceAndDateResponse response = airConn.getBalanceAndDate(request);
				validate(request.member, response.member);
			}

			// GBD
			{
				GetBalanceAndDateRequest request = new GetBalanceAndDateRequest();
				request.member = new GetBalanceAndDateRequestMember();
				initialize(request.member);
				GetBalanceAndDateResponse response = airConn.getBalanceAndDate(request);
				assertEquals(101, response.member.getResponseCode());
			}

			// GBD
			{
				GetBalanceAndDateRequest request = new GetBalanceAndDateRequest();
				request.member = new GetBalanceAndDateRequestMember();
				initialize(request.member);
				GetBalanceAndDateResponse response = airConn.getBalanceAndDate(request);
				validate(request.member, response.member);
			}

			logger.info("OK");

		}
		catch (AirException e)
		{
			logger.info(e.getMessage(), e);
			fail(e.getMessage());
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	private void initialize(IRequestHeader request)
	{
		request.setOriginHostName("AndriesHP");
		request.setOriginNodeType("HxC");
		request.setOriginTimeStamp(new Date());
		request.setOriginTransactionID("123");
		request.setSubscriberNumber(MSISDN_A);
		request.setSubscriberNumberNAI(2);
	}

	private void validate(IRequestHeader request, IResponseHeader response)
	{
		assertEquals(0, response.getResponseCode());
	}

}
