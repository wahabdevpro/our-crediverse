package hxc.connectors.database.mysql;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.connectors.database.IDatabaseConnection;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.logging.LoggerService;
import hxc.testsuite.RunAllTestsBase;

public class PoolingTest extends RunAllTestsBase implements Runnable
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private static LoggerService logger;
	private static MySqlConnector mysql;

	private static final int THREAD_COUNT = 10;
	private static final int MILLI_SECONDS = 4000;

	private volatile boolean mustStop;
	private volatile boolean failed;
	private volatile int count;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Setup and tear down
	//
	// /////////////////////////////////

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		esb = ServiceBus.getInstance();
		esb.stop();
		logger = new LoggerService();
		esb.registerService(logger);
		
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		mysql = new MySqlConnector();
		esb.registerConnector(mysql);
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
	public void testConnectionPooling() throws Exception
	{
		try (IDatabaseConnection db = mysql.getConnection(null))
		{
			TestClass testClass = new TestClass();
			testClass.loadSampleData();
			db.upsert(testClass);
		}

		long stopAt = System.currentTimeMillis() + MILLI_SECONDS;
		mustStop = false;
		failed = false;
		count = 0;
		for (int index = 0; index < THREAD_COUNT; index++)
		{
			Thread thread = new Thread(this, String.format("Test Thread %d", index));
			thread.start();
		}

		Thread.sleep(stopAt - System.currentTimeMillis());
		mustStop = true;
		Thread.sleep(200);
		assertFalse(failed);
		count = count;

	}

	@Override
	public void run()
	{
		while (!mustStop)
		{
			try (IDatabaseConnection db = mysql.getConnection(null))
			{
				TestClass testClass = db.select(TestClass.class, "limit 1;");
				assertNotNull(testClass);
				testClass.nonNullInt = count++;
				db.update(testClass);
			}
			catch (Exception e)
			{
				failed = true;
				return;
			}

		}

	}

}
