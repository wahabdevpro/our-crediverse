package hxc.services.numberplan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.configuration.ValidationException;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.logging.LoggerService;
import hxc.services.numberplan.NumberPlanService.NumberPlanChangeConfig;
import hxc.services.security.SecurityService;
import hxc.testsuite.RunAllTestsBase;

public class NumberPlanTest extends RunAllTestsBase
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private static LoggerService logger;

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
		logger = new LoggerService();
		esb.registerService(logger);
		esb.registerService(new NumberPlanService());
		
		 MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerService(new SecurityService());
		boolean started = esb.start(null);
		assert (started);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Test Number Plan
	//
	// /////////////////////////////////
	@Test
	public void testNumberPlan() throws ValidationException
	{
		String migratingNumbers = "67xxxxxxx=7xxxxxxx,65xxxxxxx=5xxxxxxx";

		INumberPlan np = esb.getFirstService(INumberPlan.class);
		assertNotNull(np);
		NumberPlanService nps = (NumberPlanService) np;
		NumberPlanChangeConfig config = (NumberPlanChangeConfig) nps.getConfiguration();

		config.setFixedLineNumbers("2xxxxxxx,3xxxxxxx");
		config.setOnNetNumbers("7xxxxxxx,67xxxxxxx");
		config.setOtherMobileNumbers("8xxxxxxx,9xxxxxxx");
		config.setSpecialNumbers("1xx");
		config.setNationalDailingCode("237");
		config.setInternationalAccessCodes("+,00");
		config.setMigratingOnnetNumbers(migratingNumbers);

		// Simple On-net
		String number = "671234567";
		assertTrue(np.isValid(number));
		assertTrue(np.isOnnet(number));
		assertTrue(np.isMobile(number));
		assertTrue(np.isNational(number));
		assertTrue(!np.isFixed(number));
		assertTrue(!np.isSpecial(number));
		assertEquals(config.getNationalDailingCode() + number, np.getInternationalFormat(number));
		assertEquals(np.getNationalFormat(number), number);

		// Legacy On-net
		number = "71234567";
		assertTrue(np.isValid(number));
		assertTrue(np.isOnnet(number));
		assertTrue(np.isMobile(number));
		assertTrue(np.isNational(number));
		assertTrue(!np.isFixed(number));
		assertTrue(!np.isSpecial(number));
		assertEquals(config.getNationalDailingCode() + "6" + number, np.getInternationalFormat(number));
		assertEquals("6" + number, np.getNationalFormat(number));

		// Legacy On-net
		number = config.getNationalDailingCode() + "71234567";
		assertTrue(np.isValid(number));
		assertTrue(np.isOnnet(number));
		assertTrue(np.isMobile(number));
		assertTrue(np.isNational(number));
		assertTrue(!np.isFixed(number));
		assertTrue(!np.isSpecial(number));
		assertEquals("237671234567", np.getInternationalFormat(number));
		assertEquals("671234567", np.getNationalFormat(number));

		// International On-net
		number = config.getNationalDailingCode() + "671237567";
		assertTrue(np.isValid(number));
		assertTrue(np.isOnnet(number));
		assertTrue(np.isMobile(number));
		assertTrue(np.isNational(number));
		assertTrue(!np.isFixed(number));
		assertTrue(!np.isSpecial(number));
		assertEquals(number, np.getInternationalFormat(number));
		assertEquals("671237567", np.getNationalFormat(number));

		// International On-net
		number = "00" + config.getNationalDailingCode() + "671237567";
		assertTrue(np.isValid(number));
		assertTrue(np.isOnnet(number));
		assertTrue(np.isMobile(number));
		assertTrue(np.isNational(number));
		assertTrue(!np.isFixed(number));
		assertTrue(!np.isSpecial(number));
		assertEquals(config.getNationalDailingCode() + "671237567", np.getInternationalFormat(number));
		assertEquals("671237567", np.getNationalFormat(number));

		// International On-net
		number = "+" + config.getNationalDailingCode() + "671237567";
		assertTrue(np.isValid(number));
		assertTrue(np.isOnnet(number));
		assertTrue(np.isMobile(number));
		assertTrue(np.isNational(number));
		assertTrue(!np.isFixed(number));
		assertTrue(!np.isSpecial(number));
		assertEquals(config.getNationalDailingCode() + "671237567", np.getInternationalFormat(number));
		assertEquals("671237567", np.getNationalFormat(number));

		// Simple Mobile off-net
		number = "81234567";
		assertTrue(np.isValid(number));
		assertTrue(!np.isOnnet(number));
		assertTrue(np.isMobile(number));
		assertTrue(np.isNational(number));
		assertTrue(!np.isFixed(number));
		assertTrue(!np.isSpecial(number));
		assertEquals(config.getNationalDailingCode() + number, np.getInternationalFormat(number));
		assertEquals(number, np.getNationalFormat(number));

		// Simple Mobile off-net
		number = "91234567";
		assertTrue(np.isValid(number));
		assertTrue(!np.isOnnet(number));
		assertTrue(np.isMobile(number));
		assertTrue(np.isNational(number));
		assertTrue(!np.isFixed(number));
		assertTrue(!np.isSpecial(number));
		assertEquals(config.getNationalDailingCode() + number, np.getInternationalFormat(number));
		assertEquals(number, np.getNationalFormat(number));

		// Simple Fixed
		number = "21234567";
		assertTrue(np.isValid(number));
		assertTrue(!np.isOnnet(number));
		assertTrue(!np.isMobile(number));
		assertTrue(np.isNational(number));
		assertTrue(np.isFixed(number));
		assertTrue(!np.isSpecial(number));
		assertEquals(config.getNationalDailingCode() + number, np.getInternationalFormat(number));
		assertEquals(number, np.getNationalFormat(number));

		// Simple Special
		number = "123";
		assertTrue(np.isValid(number));
		assertTrue(!np.isOnnet(number));
		assertTrue(!np.isMobile(number));
		assertTrue(np.isNational(number));
		assertTrue(!np.isFixed(number));
		assertTrue(np.isSpecial(number));
		assertEquals(config.getNationalDailingCode() + number, np.getInternationalFormat(number));
		assertEquals(number, np.getNationalFormat(number));

		// Not National
		number = "1234";
		assertTrue(np.isValid(number));
		assertTrue(!np.isOnnet(number));
		assertTrue(!np.isMobile(number));
		assertTrue(!np.isNational(number));
		assertTrue(!np.isFixed(number));
		assertTrue(!np.isSpecial(number));
		assertEquals(number, np.getInternationalFormat(number));
		assertEquals(number, np.getNationalFormat(number));

		// Invalid
		number = "123O4";
		assertTrue(!np.isValid(number));
		assertTrue(!np.isOnnet(number));
		assertTrue(!np.isMobile(number));
		assertTrue(!np.isNational(number));
		assertTrue(!np.isFixed(number));
		assertTrue(!np.isSpecial(number));
		assertEquals(number, np.getInternationalFormat(number));
		assertEquals(number, np.getNationalFormat(number));

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

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
