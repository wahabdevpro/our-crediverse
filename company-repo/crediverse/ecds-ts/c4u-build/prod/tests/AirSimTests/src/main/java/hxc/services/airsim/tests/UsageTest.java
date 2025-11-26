package hxc.services.airsim.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.ValidationException;
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.airsim.AirSim;
import hxc.services.airsim.protocol.DedicatedAccount;
import hxc.services.airsim.protocol.GetUsageResponse;
import hxc.services.airsim.protocol.IAirSim;
import hxc.services.airsim.protocol.StartUsageRequest;
import hxc.services.airsim.protocol.Subscriber;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.services.airsim.protocol.UsageTimer;
import hxc.services.logging.LoggerService;
import hxc.services.numberplan.NumberPlanService;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.calendar.TimeUnits;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UsageTest extends RunAllTestsBase
{
	final static Logger logger = LoggerFactory.getLogger(UsageTest.class);
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
	/**
	 * This test is designed to run before other tests. 
	 * The AIM is to validate that when a usage timer is required for more subscribers than are on the system, 
	 * the usage timers for valid subscribers are created and FALSE is returned.
	 */
	public void test1() throws Exception
	{
		// Some Test parameters
		int FIRST_SUB_MSISDN = 3223801;
		int LAST_SUB_MSISDN =  3223900;
		int LAST_TIMER_MSISDN = 3223904;
		
		int TEST_ACCOUNT = 0;	// Main account
		int TEST_AMOUNT = 100;
		int TEST_INTERVAL = 10;
		int TEST_DEVIATION = 10;
		
		// Reset Simulator
		airSimulator.reset();
		
		// Configure MSISDNs (FIRST_SUB_MSISDN to LAST_SUB_MSISDN)
		for(int msisdn = FIRST_SUB_MSISDN; msisdn <= LAST_SUB_MSISDN; msisdn++)		
		{
			airSimulator.addSubscriber(String.valueOf(msisdn), 1, 12, 5000, SubscriberState.active);
		}
		
		// Create Usage Timers (More than subscribers created)
		int count = LAST_TIMER_MSISDN - FIRST_SUB_MSISDN + 1;
		UsageTimer []  usageTimers = new UsageTimer[count];
		for(int msisdn = FIRST_SUB_MSISDN; msisdn <= LAST_TIMER_MSISDN; msisdn++)
		{
			int index = msisdn - FIRST_SUB_MSISDN;
			usageTimers[index] =  new UsageTimer();
			usageTimers[index].setMsisdn( String.valueOf(msisdn) );
			usageTimers[index].setAccount( TEST_ACCOUNT );
			usageTimers[index].setAmount( TEST_AMOUNT );
			usageTimers[index].setInterval( TEST_INTERVAL );
			usageTimers[index].setTimeUnit( TimeUnits.Seconds );
			usageTimers[index].setStandardDeviation( TEST_DEVIATION );
		}
		
		// Start Usage timers ad verify
		StartUsageRequest startRequest = new StartUsageRequest();
		startRequest.setUsageTimers(usageTimers);
		boolean usageTimersStarted = airSimulator.startUsageTimers(startRequest);		
		assertFalse( String.format("Last subscriber: %d and last usage timer:%d (not all usage timers should have be created)",  LAST_SUB_MSISDN, LAST_TIMER_MSISDN), usageTimersStarted);
		
		// Validate usage Creation
		GetUsageResponse response = airSimulator.getUsageTimers(null);
		int usageTimersValidationCount = (LAST_SUB_MSISDN-FIRST_SUB_MSISDN+1);
		int actualUsageTimerCount = response.getUsageTimers().length;
		assertTrue(String.format("Was expecting %d Timers created, Only %d were created", usageTimersValidationCount, actualUsageTimerCount), usageTimersValidationCount == actualUsageTimerCount);		
	}
	
	@Test
	public void testUsage() throws Exception
	{
		// Reset Simulator
		airSimulator.reset();

		// Create Subscriber
		Subscriber subscriber = airSimulator.addSubscriber(MSISDN, 1, 12, 5000, SubscriberState.active);

		// Add a dedicated account for it
		DedicatedAccount dedicatedAccount = new DedicatedAccount();
		dedicatedAccount.setDedicatedAccountID(12);
		dedicatedAccount.setDedicatedAccountUnitType(2);
		dedicatedAccount.setDedicatedAccountValue1(300L);
		airSimulator.updateDedicatedAccount(MSISDN, dedicatedAccount);

		// Create a UsageTimer for DA
		UsageTimer timer = new UsageTimer();
		timer.setMsisdn(MSISDN);
		timer.setAmount(100);
		timer.setAccount(12);
		timer.setInterval(1);
		timer.setTimeUnit(TimeUnits.Seconds);
		timer.setStandardDeviation(10);
		StartUsageRequest request = new StartUsageRequest();
		request.setUsageTimers(new UsageTimer[] { timer });
		boolean ok = airSimulator.startUsageTimers(request);
		assertTrue(ok);

		// Persist State (Exception will be thrown if there is a problem)
		ok = airSimulator.saveState();
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

		// Wait for it to deplete
		for (int i = 0; i <= 5; i++)
		{
			dedicatedAccount = airSimulator.getDedicatedAccount(MSISDN, 12);
			System.out.println(dedicatedAccount.getDedicatedAccountValue1());
			Thread.sleep(1000);
		}
		Long balance = dedicatedAccount.getDedicatedAccountValue1();
		assertEquals(0L, (long) balance);

		// Get Usage Timers
		GetUsageResponse response = airSimulator.getUsageTimers(MSISDN);
		UsageTimer[] timers = response.getUsageTimers();
		assertEquals(1, timers.length);
		timer = timers[0];
		assertEquals(MSISDN, timer.getMsisdn());
		assertEquals(100, timer.getAmount());
		assertEquals(12, timer.getAccount());
		assertEquals(1, timer.getInterval());
		assertEquals(TimeUnits.Seconds, timer.getTimeUnit());
		assertEquals(10, timer.getStandardDeviation());

		// Stop Usage
		ok = airSimulator.stopUsageTimers(MSISDN);
		assertTrue(ok);

		// Get Usage Timers
		response = airSimulator.getUsageTimers(null);
		timers = response.getUsageTimers();
		assertEquals(0, timers.length);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////

}
