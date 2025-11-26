package hxc.services.transactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.logging.LoggerService;

public class RollbackTest
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private int result = 0;
	private static IServiceBus esb;
	private static LoggerService logger;
	private static TransactionService tservice;

	public int getResult()
	{
		return result;
	}

	public void setResult(int result) throws Exception
	{
		if (result == 2)
			throw new Exception("Not allowed to be 2");
		this.result = result;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Setup
	//
	// /////////////////////////////////
	@BeforeClass
	public static void setup()
	{
		// Create Transaction Service
		esb = ServiceBus.getInstance();
		esb.stop();
		tservice = new TransactionService();
		logger = new LoggerService();
		esb.registerService(logger);
		esb.registerService(tservice);
		esb.start(null);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Test Rollback
	//
	// /////////////////////////////////
	@Test
	public void simpleRollbackTest()
	{

		CsvCdr cdr = new CsvCdr();
		try (Transaction<?> transaction = tservice.create(cdr, null))
		{
			assertEquals(0, getResult());

			// First Step
			final int was1 = getResult();
			setResult(1);
			transaction.addReversal(new Reversal()
			{
				@Override
				public void reverse() throws Exception
				{
					setResult(was1);
				}
			});
			assertEquals(1, getResult());

			// Last Step
			final int was2 = getResult();
			setResult(2);
			transaction.addReversal(new Reversal()
			{
				@Override
				public void reverse() throws Exception
				{
					setResult(was2);
				}
			});

			// Indicate successful completion
			transaction.complete();

		}
		catch (Exception ex)
		{
			assertEquals("Not allowed to be 2", ex.getMessage());
		}

		assertEquals(0, getResult());

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Test Forward
	//
	// /////////////////////////////////
	@Test
	public void simpleForwardTest()
	{

		CsvCdr cdr = new CsvCdr();
		try (Transaction<?> transaction = tservice.create(cdr, null))
		{
			assertEquals(0, getResult());

			// First Step
			final int was1 = getResult();
			setResult(1);
			transaction.addReversal(new Reversal()
			{
				@Override
				public void reverse() throws Exception
				{
					setResult(was1);
				}
			});
			assertEquals(1, getResult());

			// Last Step
			final int was2 = getResult();
			setResult(3);
			transaction.addReversal(new Reversal()
			{
				@Override
				public void reverse() throws Exception
				{
					setResult(was2);
				}
			});

			// Indicate successful completion
			transaction.complete();

		}
		catch (Exception ex)
		{
			fail("No Exeption Expected");
		}

		assertEquals(3, getResult());

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Teardown
	//
	// /////////////////////////////////
	@AfterClass
	public static void teardown()
	{
		esb.stop();
	}

}
