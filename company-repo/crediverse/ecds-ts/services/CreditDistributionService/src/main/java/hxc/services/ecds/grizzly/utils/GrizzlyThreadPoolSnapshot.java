package hxc.services.ecds.grizzly.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class GrizzlyThreadPoolSnapshot {

	private final AtomicInteger allocatedThreads = new AtomicInteger(0);
	private final AtomicInteger maxAllocatedThreads = new AtomicInteger(0);
	private final AtomicInteger minAllocatedThreads = new AtomicInteger(Integer.MAX_VALUE);
	
	private final AtomicInteger taskQueuedCounter = new AtomicInteger(0);
	private final AtomicInteger taskDequeuedCounter = new AtomicInteger(0);
	private final AtomicInteger taskCancelledCounter = new AtomicInteger(0);
	private final AtomicInteger taskCompletedCounter = new AtomicInteger(0);
	private final AtomicInteger taskQueueOverflowedCounter = new AtomicInteger(0);
	
	private final AtomicInteger queuedThreads = new AtomicInteger(0);
	private final AtomicInteger busyThreadsCount = new AtomicInteger(0);
	
	public void reset()
	{
		maxAllocatedThreads.set(0);
		minAllocatedThreads.set(Integer.MAX_VALUE);
		taskQueuedCounter.set(0);
		taskDequeuedCounter.set(0);
		taskCancelledCounter.set(0);
		taskCompletedCounter.set(0);
		taskQueueOverflowedCounter.set(0);
	}
	
	public void incrementAllocatedThreads()
	{
		allocatedThreads.incrementAndGet();
		updateBoundaryCounters();
	}

	public void decrementAllocatedThreads()
	{
		allocatedThreads.decrementAndGet();
		updateBoundaryCounters();
		
	}
	
	private void updateBoundaryCounters()
	{
		int value = allocatedThreads.get();
		if(value > maxAllocatedThreads.get())
			maxAllocatedThreads.set(value);
		if(value < minAllocatedThreads.get())
			minAllocatedThreads.set(value);
	}
	
	public int getAllocatedThreads()
	{
		return allocatedThreads.get();
	}
	
	public int getMaxAllocatedThreads()
	{
		int max = maxAllocatedThreads.get();
		int current = allocatedThreads.get();
		return max < current ? current : max;
	}
	
	public int getMinAllocatedThreads()
	{
		int min = minAllocatedThreads.get();
		int current = allocatedThreads.get();
		return min > current ? current : min;
	}
	
	public int getTaskQueued()
	{
		return taskQueuedCounter.get();
	}
	
	public void incrementTaskQueued()
	{
		taskQueuedCounter.incrementAndGet();
	}
	
	public int getTaskDequeued()
	{
		return taskDequeuedCounter.get();
	}
	
	public void incrementTaskDequeued()
	{
		taskDequeuedCounter.incrementAndGet();
	}
	
	public int getTaskCancelled()
	{
		return taskCancelledCounter.get();
	}
	
	public void incrementTaskCancelled()
	{
		taskCancelledCounter.incrementAndGet();
	}
	
	public int getTaskCompleted()
	{
		return taskCompletedCounter.get();
	}
	
	public void incrementTaskCompleted()
	{
		taskCompletedCounter.incrementAndGet();
	}
	
	public int getTaskQueueOverflowed()
	{
		return taskQueueOverflowedCounter.get();
	}
	
	public void incrementTaskQueueOverflowed()
	{
		taskQueueOverflowedCounter.incrementAndGet();
	}
	
	public void incrementQueuedThreadCount()
	{
		queuedThreads.incrementAndGet();
	}
	
	public void decrementQueuedThreadCount()
	{
		final int val = queuedThreads.decrementAndGet();
		if (val < 0) { 
			// it may happen if monitoring probe was added at the time, when 
			// the task had beed dequeued. 
			// So we have to re-balance the counter, sooner or later it will 
			// get the proper value. 
			queuedThreads.compareAndSet(val, 0); 
		}
	}
	
	public int getQueuedThreadCount()
	{
		return this.queuedThreads.get();
	}

	public void incrementBusyThreadCount()
	{
		busyThreadsCount.incrementAndGet();
	}
	
	public void decrementBusyThreadCount()
	{
		final int val = busyThreadsCount.decrementAndGet();
		if (val < 0) { 
			// it may happen if monitoring probe was added at the time, when 
			// the task had beed dequeued. 
			// So we have to re-balance the counter, sooner or later it will 
			// get the proper value. 
			busyThreadsCount.compareAndSet(val, 0); 
		}
	}
	
	public int getBusyThreadCount()
	{
		return this.busyThreadsCount.get();
	}
}
