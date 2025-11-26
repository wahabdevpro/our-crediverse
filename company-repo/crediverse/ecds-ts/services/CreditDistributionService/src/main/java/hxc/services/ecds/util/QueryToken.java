package hxc.services.ecds.util;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.services.ecds.rest.ICreditDistribution;

public class QueryToken implements AutoCloseable
{
	final static Logger logger = LoggerFactory.getLogger(QueryToken.class);
	
	// TODO Delete when slow queries have been properly fixed!

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Semaphore semaphore;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	private QueryToken(Semaphore semaphore)
	{		
		this.semaphore = semaphore;
	}
	
	public static QueryToken aquire(ICreditDistribution context, Semaphore semaphore)
	{
		boolean aquired = false;
		try
		{
			aquired = semaphore.tryAcquire(10, TimeUnit.SECONDS);
		}
		catch (Throwable tr)
		{
			logger.warn("error aquiring semaphore", tr);
		}
		
		if (!aquired)
		{
			logger.warn("Waiting for query token...");
			semaphore.acquireUninterruptibly();
			logger.warn("Aquired query token");
		}
		
		return new QueryToken(semaphore);
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// AutoCloseable
	//
	// /////////////////////////////////
	@Override
	public void close() throws Exception
	{
		if (semaphore != null)
			semaphore.release();
	}
}
