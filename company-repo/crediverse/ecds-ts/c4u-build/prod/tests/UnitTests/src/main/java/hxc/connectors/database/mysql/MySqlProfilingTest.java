package hxc.connectors.database.mysql;

import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.connectors.database.IDatabaseConnection;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.logging.LoggerService;
import hxc.testsuite.RunAllTestsBase;

public class MySqlProfilingTest extends RunAllTestsBase
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private static LoggerService logger;
	private static MySqlConnector mysql;

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

	@Test
	public void testSQLExceptionRate()
	{

		try (IDatabaseConnection con = mysql.getConnection(null))
		{
			con.dropTable(TestClass.class);

			con.createTable(TestClass.class);

			for (int i = 0; i < 100; i++)
			{
				TestClass test = new TestClass();
				test.loadSampleData();
				test.pkInt = i;
				test.nonNullShort += i;
				test.nonNullChar = (char) ((int) test.nonNullChar + i);

				con.insert(test);
			}

			con.dropTable(TestClass.class);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}

	}

}
