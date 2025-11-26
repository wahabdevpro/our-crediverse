package hxc.services.transactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.logging.LoggerService;

public class NumberingTest
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private static LoggerService logger;
	private static TransactionService tservice;

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
	// Test Numbers
	//
	// /////////////////////////////////
	@Test
	public void numberingTest() throws InterruptedException
	{
		String number1 = esb.getNextTransactionNumber(20);
		String number2 = esb.getNextTransactionNumber(20);
		assertEquals(number1.length(), 20);
		assertEquals(number2.length(), 20);
		assertNotEquals(number1, number2);
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
