package hxc.services.ecds.grizzly.utils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Ideally we should be able to query this type of stuff via JMX with Java VisualVM...
public class GrizzlyMonitorLogger implements Runnable 
{
	final static Logger logger = LoggerFactory.getLogger(GrizzlyMonitorLogger.class);
	final private GrizzlyThreadPoolSnapshot threadPoolSnapshot;
	final private GrizzlyConnectionSnapshot connectionSnapshot;
	private ScheduledFuture<?> future;
	private ScheduledThreadPoolExecutor scheduledThreadPool;
	
	public GrizzlyMonitorLogger(GrizzlyThreadPoolSnapshot threadPoolSnapshot, GrizzlyConnectionSnapshot connectionSnapshot)
	{
		this.threadPoolSnapshot = threadPoolSnapshot;
		this.connectionSnapshot = connectionSnapshot;
	}

	@Override
	public void run() {
		//Thread Pool Counts
		int currentAllocatedThreads = threadPoolSnapshot.getAllocatedThreads();
		int maxAllocatedThreads = threadPoolSnapshot.getMaxAllocatedThreads();
		int minAllocatedThreads = threadPoolSnapshot.getMinAllocatedThreads();
		int taskQueued = threadPoolSnapshot.getTaskQueued();
		int taskCancelled = threadPoolSnapshot.getTaskCancelled();
		int taskCompleted = threadPoolSnapshot.getTaskCompleted();
		int taskDequeued = threadPoolSnapshot.getTaskDequeued();
		int taskQueueOverflowed = threadPoolSnapshot.getTaskQueueOverflowed();
		int queuedThreadCount = threadPoolSnapshot.getQueuedThreadCount();
		int busyThreadCount = threadPoolSnapshot.getBusyThreadCount();
		//Connection Counts
		int acceptEventCount = connectionSnapshot.getAcceptEventCount();
		int bindEventCount = connectionSnapshot.getBindEventCount();
		int closeEventCount = connectionSnapshot.getCloseEventCount();
		int connectEventCount = connectionSnapshot.getConnectEventCount();
		int errorEventCount = connectionSnapshot.getErrorEventCount();
		int openConnectionCount = connectionSnapshot.getOpenConnectionCount();
		
		logger.info("Grizzly HuX HTTP Server Thread Pool Status: Current Allocated Threads {}, Max Threads {}, Min Threads {}, Tasks Queued {}, Tasks Dequeued {}, Tasks Cancelled {}, Tasks Completed {}, Task Queue Overflowed {}, Current Tasks Queued {}, Current Tasks In Progress {}",
				currentAllocatedThreads, maxAllocatedThreads, minAllocatedThreads, taskQueued, taskDequeued, taskCancelled, taskCompleted, taskQueueOverflowed, queuedThreadCount, busyThreadCount);
		logger.info("Grizzly HuX HTTP Server Connection Status: acceptEventCount {}, Bind Event Count {}, Close Event Count {}, Connect Event Count {}, Error Event Count {}, Open Connection Count {}.", acceptEventCount, bindEventCount, closeEventCount, connectEventCount, errorEventCount, openConnectionCount);
		threadPoolSnapshot.reset();
		connectionSnapshot.reset();
		
	}
	
	public void start(ScheduledThreadPoolExecutor scheduledThreadPool)
	{
		this.scheduledThreadPool = scheduledThreadPool;
		this.future = this.scheduledThreadPool.scheduleAtFixedRate(this, 1, 20, TimeUnit.SECONDS);
	}
	
	public void stop()
	{
		if (future != null)
		{
			future.cancel(true);
			future = null;
		}
	}

}
