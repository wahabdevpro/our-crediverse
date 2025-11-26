package hxc.servicebus;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.connectors.IConnection;

public class TPSLimitTest
{

	private static IServiceBus esb;
	private static boolean mustStop = false;
	private static final Object stopEvent = new Object();

	private static int transactionsToTest = 50;
	private static int maxTPS = 2;
	private final int TPSLimit = 1;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		esb = ServiceBus.getInstance(true);
		esb.setThreadQueueCapacity(transactionsToTest);
		// Add the trigger
		esb.addTrigger(new Trigger<String>(String.class)
		{
			@Override
			public boolean testCondition(String message)
			{
				return true;
			}

			@Override
			public void action(String message, IConnection connection)
			{
				if (message.equals("No Message"))
				{
					synchronized (stopEvent)
					{
						mustStop = true;
						stopEvent.notify();
					}
				}
			}
		});
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		esb.stop();
		esb = null;
	}

	@Test
	public void testTPSLimit()
	{

		// Setup ESB
		esb.setMaxTPS(maxTPS);
		esb.setMaxPeakTPS(maxTPS);
		esb.setMaxThreadPoolSize(1); // // Test Sequential Transactions

		// Start the ESB
		esb.start(null);
		mustStop = false;

		// Dispatch transactions and wait until all are finished
		dispatchTransactions(transactionsToTest);
		waitUntilFinished();

		// Check that the limit is greater than the TPSLimit
		assertTrue(esb.getConsecutiveLimiting() >= TPSLimit);

		// Setup ESB
		maxTPS = 50;
		esb.setMaxTPS(maxTPS);
		esb.setMaxPeakTPS(maxTPS);
		esb.setMaxThreadPoolSize(1); // // Test Sequential Transactions

		mustStop = false;

		// Dispatch the same amount of transactions as before
		dispatchTransactions(transactionsToTest);
		waitUntilFinished();

		// Check the that the limit is under the TPSLimit
		assertTrue(esb.getConsecutiveLimiting() <= TPSLimit);
	}

	private void dispatchTransactions(int numTransactions)
	{
		for (int i = 0; i < numTransactions; i++)
		{
			esb.dispatch(i == numTransactions - 1 ? "No Message" : "Message", null);
		}
	}

	private void waitUntilFinished()
	{
		try
		{
			synchronized (stopEvent)
			{
				if (!mustStop)
					stopEvent.wait();
			}
		}
		catch (InterruptedException e)
		{
			fail("Interupted");
		}
		return;
	}

}
