package hxc.services.airsim.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.ValidationException;
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.airsim.AirSim;
import hxc.services.airsim.protocol.DedicatedAccount;
import hxc.services.airsim.protocol.IAirSim;
import hxc.services.airsim.protocol.Offer;
import hxc.services.airsim.protocol.Subscriber;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.services.logging.LoggerService;
import hxc.services.numberplan.NumberPlanService;
import hxc.testsuite.RunAllTestsBase;

public class PersistanceTest extends RunAllTestsBase
{
	final static Logger logger = LoggerFactory.getLogger(PersistanceTest.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private static IAirSim airSimulator = null;

	private static final String MSISDN = "0824452655";
	private static final String saveFilename = "/tmp/c4u/airsim/air_sim_state.json";

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
		esb.registerConnector(new CtrlConnector());

		NumberPlanService numberPlan = new NumberPlanService();
		esb.registerService(numberPlan);

		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		airSimulator = new AirSim(esb, 10011, "/Air", numberPlan, "CFR", saveFilename);

		boolean started = esb.start(null);
		assertTrue(started);
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
	public void testPersistance() throws Exception
	{
		// Reset Simulator
		airSimulator.reset();

		// Create Subscriber
		Subscriber subscriber = airSimulator.addSubscriber(MSISDN, 1, 12, 5000, SubscriberState.active);
		
		// Add a dedicated account for it
		DedicatedAccount dedicatedAccount = new DedicatedAccount();
		dedicatedAccount.setDedicatedAccountID(12);
		dedicatedAccount.setDedicatedAccountUnitType(2);
		dedicatedAccount.setDedicatedAccountValue1(20L);
		airSimulator.updateDedicatedAccount(MSISDN, dedicatedAccount);

		// Set an Offer
		Offer offer = new Offer();
		offer.setOfferID(1010);
		offer.setOfferType(2);
		airSimulator.updateOffer(MSISDN, offer);
		
		// Persist State (Exception will be thrown if there is a problem)
		boolean ok = airSimulator.saveState();
		assertTrue(ok);

		// Delete Subscriber
		airSimulator.deleteSubscriber(MSISDN);
		subscriber = airSimulator.getSubscriber(MSISDN);
		assertNull(subscriber);

		// Restore State (Exception will be thrown if there is a problem)
		ok = airSimulator.restoreState();
		assertTrue(ok);

		// Test if Subscriber is back
		subscriber = airSimulator.getSubscriber(MSISDN);
		assertNotNull(subscriber);
		
		// Test if DA is back
		dedicatedAccount = airSimulator.getDedicatedAccount(MSISDN, 12);
		assertEquals(20L, (long)dedicatedAccount.getDedicatedAccountValue1());

		// Test if Offer is back
		offer = airSimulator.getOffer(MSISDN, 1010);
		assertEquals(2, (int)offer.getOfferType());
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////


}
