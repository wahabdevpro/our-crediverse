package hxc.services.ecds.grizzly.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class GrizzlyConnectionSnapshot {
	AtomicInteger bindEventCount = new AtomicInteger(0);
	AtomicInteger acceptEventCount = new AtomicInteger(0);
	AtomicInteger connectEventCount = new AtomicInteger(0);
	AtomicInteger closeEventCount = new AtomicInteger(0);
	AtomicInteger errorEventCount = new AtomicInteger(0);
	AtomicInteger openConnectionCount = new AtomicInteger(0);
	
	public void reset()
	{
		bindEventCount.set(0);
		acceptEventCount.set(0);
		connectEventCount.set(0);
		closeEventCount.set(0);
		errorEventCount.set(0);
	}
	
	public int getBindEventCount()
	{
		return bindEventCount.get();
	}
	
	public void incrementBindEventCount()
	{
		bindEventCount.incrementAndGet();
	}

	public int getAcceptEventCount()
	{
		return acceptEventCount.get();
	}

	public void incrementAcceptEventCount()
	{
		acceptEventCount.incrementAndGet();
	}

	public int getConnectEventCount()
	{
		return connectEventCount.get();
	}

	public void incrementConnectEventCount()
	{
		connectEventCount.incrementAndGet();
	}
	
	public int getCloseEventCount()
	{
		return closeEventCount.get();
	}

	public void incrementCloseEventCount()
	{
		closeEventCount.incrementAndGet();
	}

	public int getErrorEventCount()
	{
		return errorEventCount.get();
	}

	public void incrementErrorEventCount()
	{
		errorEventCount.incrementAndGet();
	}

	public int getOpenConnectionCount()
	{
		return openConnectionCount.get();
	}
	
	public void incrementOpenConnectionCount()
	{
		openConnectionCount.incrementAndGet();
	}
	
	public void decrementOpenConnectionCount()
	{
		int val = openConnectionCount.decrementAndGet();
		if(val < 0)
		{
			openConnectionCount.compareAndSet(val, 0);
		}
	}
}
