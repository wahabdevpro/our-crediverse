package hxc.connectors.database.mysql;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.testsuite.RunAllTestsBase;

public class MySqlTest extends RunAllTestsBase
{
	final static Logger logger = LoggerFactory.getLogger(MySqlTest.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
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
		configureLogging(esb);
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
	public void testMySqlConnector() throws Exception
	{
		IDatabase db = esb.getFirstConnector(IDatabase.class);
		assertNotNull(db);
		try (IDatabaseConnection connection = db.getConnection(null))
		{

			// Drop table if it exists
			connection.dropTable(TestClass.class);

			// Create Table
			connection.createTable(TestClass.class);

			// Insert Data
			TestClass testClass = new TestClass();
			testClass.loadSampleData();
			connection.insert(testClass);

			// Select Data
			TestClass readData = connection.select(TestClass.class, "where pkInt = %s and pkString = %s", testClass.pkInt, testClass.pkString);
			assertNotNull(readData);
			assertEquals(testClass.pkInt, readData.pkInt);
			assertEquals(testClass.pkString, readData.pkString);
			assertEquals(testClass.primitiveInt, readData.primitiveInt);
			assertEquals(testClass.nullInt, readData.nullInt);
			assertEquals(testClass.nonNullInt, readData.nonNullInt);
			assertEquals("defaultTest", readData.readOnlyString);
			assertEquals(testClass.nullString, readData.nullString);
			assertEquals(testClass.nonNullString, readData.nonNullString);
			assertEquals(testClass.nullBigDecimal, readData.nullBigDecimal);
			assertEquals(testClass.nonNullBigDecimal, readData.nonNullBigDecimal);
			assertEquals(testClass.nullDate, readData.nullDate);
			assertEquals(testClass.nonNullDate, readData.nonNullDate);
			assertEquals(testClass.nonNullBoolean, readData.nonNullBoolean);
			assertEquals(testClass.nullBoolean, readData.nullBoolean);
			assertEquals(testClass.nonNullByte, readData.nonNullByte);
			assertEquals(testClass.nullByte, readData.nullByte);
			assertEquals(testClass.nonNullChar, readData.nonNullChar);
			assertEquals(testClass.nullChar, readData.nullChar);
			assertEquals(testClass.nonNullLong, readData.nonNullLong);
			assertEquals(testClass.nullLong, readData.nullLong);
			assertEquals(testClass.nonNullDouble, readData.nonNullDouble, 1e-8);
			assertEquals(testClass.nullDouble, readData.nullDouble);
			assertEquals(testClass.nonNullFloat, readData.nonNullFloat, 1e-3f);
			assertEquals(testClass.nullFloat, readData.nullFloat);
			assertEquals(testClass.nonNullShort, readData.nonNullShort);
			assertArrayEquals(testClass.byteArray, readData.byteArray);
			assertEquals(testClass.nonNullEnum, readData.nonNullEnum);
			assertEquals(testClass.nullGuid, readData.nullGuid);
			assertEquals(testClass.nonNullGuid, readData.nonNullGuid);

			// Select Scalar
			long nonNullLong = connection.selectScalar(long.class, "select nonNullLong from my_test");
			assertEquals(nonNullLong, 124L);

			// Select Vector
			TestClass testClass2 = new TestClass();
			testClass2.loadSampleData();
			testClass2.pkInt = 13;
			testClass2.nonNullShort = 4321;
			connection.insert(testClass2);
			List<Integer> list = connection.selectVector(Integer.class, "select pkInt from my_test order by pkInt desc");
			assertNotNull(list);
			assertEquals(2, list.size());
			assertEquals(13, (int) list.get(0));
			assertEquals(12, (int) list.get(1));

			// Update
			testClass2.nonNullShort = 1234;
			connection.update(testClass2);
			TestClass updated = connection.select(TestClass.class, "where pkInt = 13");
			assertEquals(updated.nonNullShort, 1234);

			// Delete
			connection.delete(testClass2);
			list = connection.selectVector(Integer.class, "select pkInt from my_test order by pkInt desc");
			assertNotNull(list);
			assertEquals(1, list.size());
			assertEquals(12, (int) list.get(0));

		}
		catch (Exception ex)
		{
			logger.error(ex.getMessage(), ex);
			throw ex;
		}

	}

}
