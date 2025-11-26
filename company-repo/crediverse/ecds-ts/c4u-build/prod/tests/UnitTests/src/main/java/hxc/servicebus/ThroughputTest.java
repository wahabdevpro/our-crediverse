package hxc.servicebus;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.connectors.IConnection;

public class ThroughputTest
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private boolean mustStop = false;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	// /////////////////////////////////
	@Test
	public void testThroughput()
	{
		int transactionsToTest = 15000;

		final Object stopEvent = new Object();
		final int tpsToTestAt = 1000;

		// Create ESB
		esb = ServiceBus.getInstance(true);
		esb.stop();
		esb.setMaxTPS(tpsToTestAt);
		esb.setMaxPeakTPS(transactionsToTest);
		esb.setThreadQueueCapacity(50);

		// Start the ESB
		esb.start(null);
		mustStop = false;

		// Create a trigger
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
				if (message.equals("Z"))
				{
					synchronized (stopEvent)
					{
						mustStop = true;
						stopEvent.notify();
					}
				}
			}
		});

		// Execute transactionsToTest transactions
		long now = System.currentTimeMillis();
		for (int i = 1; i <= transactionsToTest; i++)
		{
			if (i % 1000 == 0)
				System.out.printf("%d ", i);
			esb.dispatch(i >= transactionsToTest ? "Z" : "A", null);
		}
		System.out.print("\n");
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
		long duration = System.currentTimeMillis() - now;

		// Test TPS
		int measuredTps = esb.getCurrentTPS();
		assertTrue("TPS too low", measuredTps >= tpsToTestAt - tpsToTestAt / 10);
		assertTrue("TPS too high", measuredTps <= tpsToTestAt + tpsToTestAt / 10);

		System.out.println(String.format("duration=%dms, measuredTps=%d", duration, measuredTps));

	}

}
