package hxc.servicebus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.configuration.IConfiguration;
import hxc.services.IService;
import hxc.services.logging.LoggerService;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.reflection.IPropertyInfo;

public class ConfigurationTest extends RunAllTestsBase
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Setup / Teardown
	//
	// /////////////////////////////////
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		esb = ServiceBus.getInstance();
		esb.stop();
		configureLogging(esb);
		esb.start(null);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		esb.stop();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	// /////////////////////////////////

	@Test
	public void testConfiguration()
	{
		// Test getMethods
		LoggerService logger = new LoggerService();
		assertNotNull(logger);
		IService loggerService = (IService) logger;
		assertNotNull(loggerService);
		IConfiguration config = loggerService.getConfiguration();
		Method[] methods = config.getMethods();
		assertNotNull(methods);
		assertEquals(1, methods.length);
		assertEquals("Rotate", methods[0].getName());
		methods = config.getMethods();
		assertEquals("Rotate", methods[0].getName());

		// Test getProperties
		IPropertyInfo[] properties = config.getProperties();
		assertNotNull(properties);
		assertTrue(properties.length > 5);
		properties = config.getProperties();
		assertTrue(properties.length > 5);

	}

}
