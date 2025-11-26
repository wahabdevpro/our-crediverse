package hxc.services.ecds.grizzly.utils;

import org.glassfish.grizzly.threadpool.AbstractThreadPool;
import org.glassfish.grizzly.threadpool.ThreadPoolProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HuxThreadPoolProbe implements ThreadPoolProbe {

	final static Logger logger = LoggerFactory.getLogger(HuxThreadPoolProbe.class);
	private GrizzlyThreadPoolSnapshot threadPoolSnapshot;
	
	public HuxThreadPoolProbe(GrizzlyThreadPoolSnapshot threadPoolSnapshot)
	{
		this.threadPoolSnapshot = threadPoolSnapshot; 
	}
	
	@Override
	public void onThreadPoolStartEvent(AbstractThreadPool threadPool) {}

	@Override
	public void onThreadPoolStopEvent(AbstractThreadPool threadPool) {}

	@Override
	public void onThreadAllocateEvent(AbstractThreadPool threadPool, Thread thread)
	{
		logger.trace("New thread allocated in thread pool {}, thread {}", threadPool.getConfig().getPoolName(), thread.getName());
		threadPoolSnapshot.incrementAllocatedThreads();
	}

	@Override
	public void onThreadReleaseEvent(AbstractThreadPool threadPool, Thread thread) 
	{
		threadPoolSnapshot.decrementAllocatedThreads();
	}

	@Override
	public void onMaxNumberOfThreadsEvent(AbstractThreadPool threadPool, int maxNumberOfThreads) {
		logger.warn("Maximum Number of Threads reached in thread pool {}, number of threads {}", threadPool.getConfig().getPoolName(), maxNumberOfThreads);
	}

	@Override
	public void onTaskQueueEvent(AbstractThreadPool threadPool, Runnable task)
	{
		threadPoolSnapshot.incrementTaskQueued();
		threadPoolSnapshot.incrementQueuedThreadCount();
	}

	@Override
	public void onTaskDequeueEvent(AbstractThreadPool threadPool, Runnable task)
	{
		threadPoolSnapshot.incrementTaskDequeued();
		threadPoolSnapshot.decrementQueuedThreadCount();
		threadPoolSnapshot.incrementBusyThreadCount();
	}

	@Override
	public void onTaskCancelEvent(AbstractThreadPool threadPool, Runnable task)
	{
		threadPoolSnapshot.incrementTaskCancelled();
		threadPoolSnapshot.decrementBusyThreadCount();
	}

	@Override
	public void onTaskCompleteEvent(AbstractThreadPool threadPool, Runnable task)
	{
		threadPoolSnapshot.incrementTaskCompleted();
		threadPoolSnapshot.decrementBusyThreadCount();
	}

	@Override
	public void onTaskQueueOverflowEvent(AbstractThreadPool threadPool) {
		logger.warn("Task Queue Overflow Event occurred in thread pool {}", threadPool.getConfig().getPoolName());
		threadPoolSnapshot.incrementTaskQueueOverflowed();
	}
}
