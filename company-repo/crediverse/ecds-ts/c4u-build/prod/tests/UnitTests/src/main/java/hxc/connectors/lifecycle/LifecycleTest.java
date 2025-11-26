package hxc.connectors.lifecycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.reporting.ReportingService;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.calendar.DateTime;

public class LifecycleTest extends RunAllTestsBase
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private String A_NUMBER = "08244526545";
	private String B_NUMBER1 = "0823751482";
	private String B_NUMBER2 = "0823751483";
	private String SERVICE_ID = "CrShr";
	private String MONTHLY = "MONTHLY";
	private String WEEKLY = "WEEKLY";
	private String DAILY = "DAILY";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Setup
	//
	// /////////////////////////////////
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		// Create Transaction Service
		esb = ServiceBus.getInstance();
		esb.stop();
		configureLogging(esb);
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerConnector(new LifecycleConnector());
		esb.registerConnector(new CtrlConnector());
		esb.registerService(new ReportingService());
		boolean started = esb.start(null);
		assert (started);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	// /////////////////////////////////
	@Test
	public void testLifecycle() throws Exception
	{
		// Get Plug-ins
		IDatabase db = esb.getFirstConnector(IDatabase.class);
		ILifecycle lifecycle = esb.getFirstConnector(ILifecycle.class);

		// Using a database connection
		try (IDatabaseConnection database = db.getConnection(null))
		{
			Party aParty = new Party(A_NUMBER, 1);
			Party bParty1 = new Party(B_NUMBER1, 1);
			Party bParty2 = new Party(B_NUMBER2, 1);

			// Test of 'isAMember' to see if it works without table being there
			lifecycle.isMember(database, aParty, SERVICE_ID, bParty2);

			// Clear store
			lifecycle.removeSubscriptions(database, aParty, SERVICE_ID);

			// Add two Subscriptions
			boolean result = lifecycle.addSubscription(database, aParty, SERVICE_ID, DAILY, 0, dt(2014, 12, 24), dt(2014, 12, 25), dt(2014, 12, 26));
			assertFalse(result);
			result = lifecycle.addSubscription(database, aParty, SERVICE_ID, DAILY, 0, dt(2014, 12, 24), dt(2014, 12, 25), dt(2014, 12, 26));
			assertTrue(result);
			result = lifecycle.addSubscription(database, aParty, SERVICE_ID, MONTHLY, 1, dt(2015, 12, 24), dt(2015, 12, 25), dt(2015, 12, 26));
			assertFalse(result);

			// Get Subscription 1
			ISubscription lcyl = lifecycle.getSubscription(database, aParty, SERVICE_ID, WEEKLY);
			assertNull(lcyl);
			lcyl = lifecycle.getSubscription(database, bParty1, SERVICE_ID, DAILY);
			assertNull(lcyl);
			lcyl = lifecycle.getSubscription(database, aParty, SERVICE_ID, DAILY);
			assertNotNull(lcyl);
			assertEquals(A_NUMBER, lcyl.getMsisdn());
			assertEquals(SERVICE_ID, lcyl.getServiceID());
			assertEquals(DAILY, lcyl.getVariantID());
			assertEquals(0, lcyl.getState());
			assertEquals(dt(2014, 12, 24), lcyl.getNextDateTime());
			assertEquals(dt(2014, 12, 25), lcyl.getDateTime1());
			assertEquals(dt(2014, 12, 26), lcyl.getDateTime2());
			assertNull(lcyl.getDateTime3());
			assertNull(lcyl.getDateTime4());

			// Get Subscription 2
			lcyl = lifecycle.getSubscription(database, aParty, SERVICE_ID, MONTHLY);
			assertNotNull(lcyl);
			assertEquals(A_NUMBER, lcyl.getMsisdn());
			assertEquals(SERVICE_ID, lcyl.getServiceID());
			assertEquals(MONTHLY, lcyl.getVariantID());
			assertEquals(1, lcyl.getState());
			assertEquals(dt(2015, 12, 24), lcyl.getNextDateTime());
			assertEquals(dt(2015, 12, 25), lcyl.getDateTime1());
			assertEquals(dt(2015, 12, 26), lcyl.getDateTime2());
			assertNull(lcyl.getDateTime3());
			assertNull(lcyl.getDateTime4());

			// Get all SUbscriptions
			ISubscription[] lcyls = lifecycle.getSubscriptions(database, aParty, SERVICE_ID);
			assertNotNull(lcyls);
			assertEquals(2, lcyls.length);

			assertEquals(DAILY, lcyls[0].getVariantID());
			assertEquals(0, lcyls[0].getState());
			assertEquals(dt(2014, 12, 24), lcyls[0].getNextDateTime());
			assertEquals(dt(2014, 12, 25), lcyls[0].getDateTime1());
			assertEquals(dt(2014, 12, 26), lcyls[0].getDateTime2());
			assertNull(lcyls[0].getDateTime3());
			assertNull(lcyls[0].getDateTime4());

			assertEquals(MONTHLY, lcyls[1].getVariantID());
			assertEquals(1, lcyls[1].getState());
			assertEquals(dt(2015, 12, 24), lcyls[1].getNextDateTime());
			assertEquals(dt(2015, 12, 25), lcyls[1].getDateTime1());
			assertEquals(dt(2015, 12, 26), lcyls[1].getDateTime2());
			assertNull(lcyls[0].getDateTime3());
			assertNull(lcyls[0].getDateTime4());

			lcyls = lifecycle.getSubscriptions(database, aParty);
			assertNotNull(lcyls);
			assertEquals(2, lcyls.length);

			// Test isSubscribed
			assertTrue(lifecycle.isSubscribed(database, aParty, SERVICE_ID));
			assertTrue(lifecycle.isSubscribed(database, aParty, SERVICE_ID, DAILY));
			assertFalse(lifecycle.isSubscribed(database, aParty, SERVICE_ID, WEEKLY));
			assertTrue(lifecycle.isSubscribed(database, aParty, SERVICE_ID, MONTHLY));

			// Update Subscription
			lcyl = lifecycle.getSubscription(database, aParty, SERVICE_ID, MONTHLY);

			lcyl.setState(7);
			lcyl.setNextDateTime(dt(2016, 12, 24));
			lcyl.setDateTime1(dt(2016, 12, 25));
			lcyl.setDateTime2(dt(2016, 12, 26));
			lcyl.setDateTime3(dt(2016, 12, 27));
			lcyl.setDateTime4(dt(2016, 12, 28));
			lifecycle.updateSubscription(database, lcyl);

			lcyl = lifecycle.getSubscription(database, aParty, SERVICE_ID, MONTHLY);
			assertNotNull(lcyl);
			assertEquals(A_NUMBER, lcyl.getMsisdn());
			assertEquals(SERVICE_ID, lcyl.getServiceID());
			assertEquals(MONTHLY, lcyl.getVariantID());
			assertEquals(7, lcyl.getState());
			assertEquals(dt(2016, 12, 24), lcyl.getNextDateTime());
			assertEquals(dt(2016, 12, 25), lcyl.getDateTime1());
			assertEquals(dt(2016, 12, 26), lcyl.getDateTime2());
			assertEquals(dt(2016, 12, 27), lcyl.getDateTime3());
			assertEquals(dt(2016, 12, 28), lcyl.getDateTime4());

			// Remove
			assertTrue(lifecycle.removeSubscription(database, aParty, SERVICE_ID, DAILY));
			assertFalse(lifecycle.removeSubscription(database, aParty, SERVICE_ID, DAILY));
			assertTrue(lifecycle.isSubscribed(database, aParty, SERVICE_ID));
			assertFalse(lifecycle.isSubscribed(database, aParty, SERVICE_ID, DAILY));
			assertFalse(lifecycle.isSubscribed(database, aParty, SERVICE_ID, WEEKLY));
			assertTrue(lifecycle.isSubscribed(database, aParty, SERVICE_ID, MONTHLY));

			// Remove All
			assertTrue(lifecycle.removeSubscriptions(database, aParty, SERVICE_ID));
			assertFalse(lifecycle.removeSubscriptions(database, aParty, SERVICE_ID));
			assertFalse(lifecycle.isSubscribed(database, aParty, SERVICE_ID));
			assertFalse(lifecycle.isSubscribed(database, aParty, SERVICE_ID, DAILY));
			assertFalse(lifecycle.isSubscribed(database, aParty, SERVICE_ID, WEEKLY));
			assertFalse(lifecycle.isSubscribed(database, aParty, SERVICE_ID, MONTHLY));

		}
		catch (Exception e)
		{
			throw e;
		}

	}

	//
	// public abstract boolean removeMember(IDatabaseConnection database, String ownerMSISDN, String serviceID, String variantID, String memberMSISDN) throws SQLException;
	//
	// public abstract boolean removeMembers(IDatabaseConnection database, String ownerMSISDN, String serviceID, String variantID) throws SQLException;

	@Test
	public void testMembership() throws Exception
	{
		Party aParty = new Party(A_NUMBER, 1);
		Party bParty1 = new Party(B_NUMBER1, 1);
		Party bParty2 = new Party(B_NUMBER2, 1);

		// Get Plug-ins
		IDatabase db = esb.getFirstConnector(IDatabase.class);
		ILifecycle lifecycle = esb.getFirstConnector(ILifecycle.class);

		// Using a database connection
		try (IDatabaseConnection database = db.getConnection(null))
		{
			// Test of 'removeMembers' to see if it works without table being there
			lifecycle.removeMembers(database, aParty, SERVICE_ID, DAILY);

			// Clear store
			lifecycle.removeMembers(database, aParty, SERVICE_ID, DAILY);
			lifecycle.removeMembers(database, aParty, SERVICE_ID, WEEKLY);
			lifecycle.removeMembers(database, aParty, SERVICE_ID, MONTHLY);

			// Has Members
			assertFalse(lifecycle.hasMembers(database, aParty, SERVICE_ID));
			assertFalse(lifecycle.hasMembers(database, aParty, SERVICE_ID, DAILY));
			assertFalse(lifecycle.hasMembers(database, aParty, SERVICE_ID, WEEKLY));
			assertFalse(lifecycle.hasMembers(database, aParty, SERVICE_ID, MONTHLY));

			// Add 3 Members
			assertTrue(lifecycle.addMember(database, aParty, SERVICE_ID, DAILY, bParty1));
			assertFalse(lifecycle.addMember(database, aParty, SERVICE_ID, DAILY, bParty1));
			assertTrue(lifecycle.addMember(database, aParty, SERVICE_ID, DAILY, bParty2));
			assertTrue(lifecycle.addMember(database, aParty, SERVICE_ID, MONTHLY, bParty2));

			// Has Members
			assertTrue(lifecycle.hasMembers(database, aParty, SERVICE_ID));
			assertTrue(lifecycle.hasMembers(database, aParty, SERVICE_ID, DAILY));
			assertFalse(lifecycle.hasMembers(database, aParty, SERVICE_ID, WEEKLY));
			assertTrue(lifecycle.hasMembers(database, aParty, SERVICE_ID, MONTHLY));

			// Test Membership
			assertTrue(lifecycle.isMember(database, aParty, SERVICE_ID, bParty1));
			assertTrue(lifecycle.isMember(database, aParty, SERVICE_ID, DAILY, bParty1));
			assertFalse(lifecycle.isMember(database, aParty, SERVICE_ID, WEEKLY, bParty1));
			assertFalse(lifecycle.isMember(database, aParty, SERVICE_ID, MONTHLY, bParty1));
			assertTrue(lifecycle.isMember(database, aParty, SERVICE_ID, bParty2));
			assertTrue(lifecycle.isMember(database, aParty, SERVICE_ID, DAILY, bParty2));
			assertFalse(lifecycle.isMember(database, aParty, SERVICE_ID, WEEKLY, bParty2));
			assertTrue(lifecycle.isMember(database, aParty, SERVICE_ID, MONTHLY, bParty2));

			// Get Members
			String[] members = lifecycle.getMembers(database, aParty, SERVICE_ID, DAILY);
			assertNotNull(members);
			assertEquals(2, members.length);
			assertEquals(B_NUMBER1, members[0]);
			assertEquals(B_NUMBER2, members[1]);
			members = lifecycle.getMembers(database, aParty, SERVICE_ID, WEEKLY);
			assertNotNull(members);
			assertEquals(0, members.length);
			members = lifecycle.getMembers(database, aParty, SERVICE_ID, MONTHLY);
			assertNotNull(members);
			assertEquals(1, members.length);
			assertEquals(B_NUMBER2, members[0]);

			// Remove Member
			assertTrue(lifecycle.removeMember(database, aParty, SERVICE_ID, MONTHLY, bParty2));
			assertFalse(lifecycle.removeMember(database, aParty, SERVICE_ID, MONTHLY, bParty2));
			assertTrue(lifecycle.isMember(database, aParty, SERVICE_ID, bParty1));
			assertTrue(lifecycle.isMember(database, aParty, SERVICE_ID, DAILY, bParty1));
			assertFalse(lifecycle.isMember(database, aParty, SERVICE_ID, WEEKLY, bParty1));
			assertFalse(lifecycle.isMember(database, aParty, SERVICE_ID, MONTHLY, bParty1));
			assertTrue(lifecycle.isMember(database, aParty, SERVICE_ID, bParty2));
			assertTrue(lifecycle.isMember(database, aParty, SERVICE_ID, DAILY, bParty2));
			assertFalse(lifecycle.isMember(database, aParty, SERVICE_ID, WEEKLY, bParty2));
			assertFalse(lifecycle.isMember(database, aParty, SERVICE_ID, MONTHLY, bParty2));

			// Remove Members
			assertTrue(lifecycle.removeMembers(database, aParty, SERVICE_ID, DAILY));
			assertFalse(lifecycle.removeMembers(database, aParty, SERVICE_ID, DAILY));
			assertFalse(lifecycle.isMember(database, aParty, SERVICE_ID, bParty1));
			assertFalse(lifecycle.isMember(database, aParty, SERVICE_ID, DAILY, bParty1));
			assertFalse(lifecycle.isMember(database, aParty, SERVICE_ID, WEEKLY, bParty1));
			assertFalse(lifecycle.isMember(database, aParty, SERVICE_ID, MONTHLY, bParty1));
			assertFalse(lifecycle.isMember(database, aParty, SERVICE_ID, bParty2));
			assertFalse(lifecycle.isMember(database, aParty, SERVICE_ID, DAILY, bParty2));
			assertFalse(lifecycle.isMember(database, aParty, SERVICE_ID, WEEKLY, bParty2));
			assertFalse(lifecycle.isMember(database, aParty, SERVICE_ID, MONTHLY, bParty2));

			// Has Members
			assertFalse(lifecycle.hasMembers(database, aParty, SERVICE_ID));
			assertFalse(lifecycle.hasMembers(database, aParty, SERVICE_ID, DAILY));
			assertFalse(lifecycle.hasMembers(database, aParty, SERVICE_ID, WEEKLY));
			assertFalse(lifecycle.hasMembers(database, aParty, SERVICE_ID, MONTHLY));

		}
		catch (Exception e)
		{
			throw e;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	private Date dt(int year, int month, int day)
	{
		return new DateTime(year, month, day, 0, 0, 0, 0);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Teardown
	//
	// /////////////////////////////////
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		esb.stop();
	}
}
