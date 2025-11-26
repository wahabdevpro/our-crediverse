package hxc.servicebus;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.connectors.IConnection;

public class ThrottlingTest
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
	public void testThrottling() throws InterruptedException
	{
		int transactionsToTest = 100;
		int tpsToTestAt = 10;

		final Object stopEvent = new Object();

		// Create ESB
		esb = ServiceBus.getInstance(true);
		esb.stop();
		esb.setMaxTPS(tpsToTestAt);
		esb.setMaxPeakTPS(tpsToTestAt);
		esb.setThreadQueueCapacity(2);
		
		// Test Sequential Transactions - Z being the last one
		esb.setMaxThreadPoolSize(1);
		
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

			@Override
			public boolean isLowPriority(String message)
			{
				return message.equals("Z");
			}

		});

		// Execute transactionsToTest transactions
		long now = System.currentTimeMillis();

		for (int i = 1; i <= 20; i++)
		{
			esb.dispatch("A", null);
		}

		esb.dispatch("Z", null);

		for (int i = 22; i <= transactionsToTest; i++)
		{
			esb.dispatch("A", null);
		}

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

		// Test Duration
		long minDuration = (transactionsToTest * 1000) / tpsToTestAt - 1500;
		long maxDuration = (transactionsToTest * 1000) / tpsToTestAt + 1500;
		System.err.printf( "minDuration = %s maxDuration = %s duration = %s\n", minDuration, maxDuration, duration );
		if (duration < minDuration)
			assertTrue("Too fast", duration >= minDuration);
		assertTrue("Too slow", duration <= maxDuration);

		System.out.println(String.format("duration=%dms, measuredTps=%d", duration, measuredTps));
	}
}
