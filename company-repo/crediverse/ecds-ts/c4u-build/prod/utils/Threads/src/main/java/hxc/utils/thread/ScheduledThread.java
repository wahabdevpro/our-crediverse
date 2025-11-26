package hxc.utils.thread;

import java.util.Calendar;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.snmp.ISnmpConnector;
import hxc.connectors.snmp.IncidentSeverity;

public class ScheduledThread implements Runnable, AutoCloseable
{
	final static Logger logger = LoggerFactory.getLogger(ScheduledThread.class);
	private ScheduledExecutorService scheduledExecutorService;
	private ISnmpConnector snmpConnector;

	private ScheduledFuture<?> future = null;
	private final Object futureMonitor = new Object();

	private int runStartCounter = 0;
	private int runCompleteCounter = 0;
	private int minIntialDelay = 5 * 60;
	private final Object runCounterMonitor = new Object();

	private String serviceName;
	private Long timeOfDay;
	private long period;
	
	public ScheduledThread(String serviceName, ScheduledExecutorService scheduledExecutorService, ISnmpConnector snmpConnector)
	{
		this.serviceName = serviceName;
		this.scheduledExecutorService =	scheduledExecutorService;
		this.snmpConnector = snmpConnector;
	}

	public boolean isRunning()
	{
		synchronized (this.futureMonitor)
		{
			return this.future != null;
		}
	}

	public boolean isStopped()
	{
		synchronized (this.futureMonitor)
		{
			return !(this.future != null);
		}
	}

	public boolean waitForRunStartCount(int count, Long timeout) throws Exception
	{
		Long deadline = null;
		if (timeout != null)
		{
			if (timeout < 0)
				throw new IllegalArgumentException("timeout may not be negative");
			long start = System.nanoTime() / 1000 / 1000;
			deadline = start + timeout;
		}
		synchronized (this.runCounterMonitor)
		{
			while (this.runStartCounter < count)
			{
				if (deadline != null)
				{
					long now = (System.nanoTime() / 1000 / 1000);
					if (deadline <= now)
						break;
					long useTimeout = deadline - now;
					logger.trace("Waiting up to {} milliseconds for runCounterMonitor ...", useTimeout);
					this.runCounterMonitor.wait(useTimeout);
				}
				else
				{
					this.runCounterMonitor.wait();
				}
			}
			logger.trace("Returning !( {} < {} )", this.runStartCounter, count);
			return !(this.runStartCounter < count);
		}
	}

	public boolean waitForRunCompleteCount(int count, Long timeout) throws Exception
	{
		Long deadline = null;
		if (timeout != null)
		{
			if (timeout < 0)
				throw new IllegalArgumentException("timeout may not be negative");
			long start = System.nanoTime() / 1000 / 1000;
			deadline = start + timeout;
		}
		synchronized (this.runCounterMonitor)
		{
			while (this.runCompleteCounter < count)
			{
				if (deadline != null)
				{
					long now = (System.nanoTime() / 1000 / 1000);
					if (deadline <= now)
						break;
					long useTimeout = deadline - now;
					logger.trace("Waiting up to {} milliseconds for runCounterMonitor ...", useTimeout);
					this.runCounterMonitor.wait(useTimeout);
				}
				else
				{
					this.runCounterMonitor.wait();
				}
			}
			logger.trace("Returning !( {} < {} )", this.runCompleteCounter, count);
			return !(this.runCompleteCounter < count);
		}
	}

	public void restart(Long timeOfDay, long period, int minIntialDelay)
	{
		this.timeOfDay = timeOfDay;
		this.period = period;
		this.minIntialDelay = minIntialDelay;
		this.restart();
	}

	public void restart()
	{
		logger.info("Restarting ...");
		synchronized (this.futureMonitor)
		{
			if (this.future != null)
			{
				this.future.cancel(true);
				this.future = null;
			}
			synchronized (this.runCounterMonitor)
			{
				this.runStartCounter = 0;
				this.runCompleteCounter = 0;
				this.runCounterMonitor.notify();
			}
			this.future = this.schedule();
		}
	}

	public boolean start(Long timeOfDay, long period, int minIntialDelay)
	{
		this.timeOfDay = timeOfDay;
		this.period = period;
		this.minIntialDelay = minIntialDelay;
		return this.start();
	}

	public boolean start()
	{
		logger.info("Starting ...");
		synchronized (this.futureMonitor)
		{
			if (this.future == null)
			{
				synchronized (this.runCounterMonitor)
				{
					this.runStartCounter = 0;
					this.runCompleteCounter = 0;
					this.runCounterMonitor.notify();
				}
				this.future = this.schedule();
				this.futureMonitor.notifyAll();
				return true;
			}
			else
			{
				return false;
			}
		}
	}

	public boolean stop()
	{
		logger.info("Stopping ...");
		synchronized (this.futureMonitor)
		{
			if (this.future != null)
			{
				this.future.cancel(true);
				this.future = null;
				this.futureMonitor.notifyAll();
				return true;
			}
			else
			{
				return false;
			}
		}
	}

	@Override
	public void close()
	{
		this.stop();
	}

	public void runActual() throws Throwable
	{

	}

	// Runnable
	@Override
	public final void run()
	{
		// TODO Ensure no concurrent runs ...
		logger.info("starting run ...");
		try
		{
			synchronized (this.runCounterMonitor)
			{
				this.runStartCounter++;
				this.runCounterMonitor.notify();
			}
			try
			{
				this.runActual();
			}
			finally
			{
				synchronized (this.runCounterMonitor)
				{
					this.runCompleteCounter = this.runStartCounter;
					this.runCounterMonitor.notify();
				}
			}
		}
		catch (Throwable throwable)
		{
			logger.error("run failed", throwable);
			this.snmpConnector.jobFailed(this.serviceName, IncidentSeverity.CRITICAL, String.format("%s: run failed with: %s", throwable));
		}
		finally
		{
			logger.info("run ended");
		}
	}

	private static long calculateInitialDelay(int timeOfDay)
	{
		int hourOfDay = (timeOfDay / (60 * 60));
		int minute = (timeOfDay % (60 * 60)) / 60;
		int second = (timeOfDay % (60 * 60)) % 60;
		
		Calendar now = Calendar.getInstance();
		now.set(Calendar.MILLISECOND, 0);
		
		Calendar startTime = (Calendar) now.clone();
		startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
		startTime.set(Calendar.MINUTE, minute);
		startTime.set(Calendar.SECOND, second);
		startTime.set(Calendar.MILLISECOND, 0);
		
		if (startTime.compareTo(now) < 0)
		{   
			startTime.add(Calendar.HOUR, 24);
		}
		return ((startTime.getTime().getTime() - now.getTime().getTime()) / 1000L);
	}

	private long calculateInitialDelay()
	{
		if ( this.timeOfDay == null ) return 0;
		return calculateInitialDelay(this.timeOfDay.intValue());
	}

	private ScheduledFuture<?> schedule()
	{
		long initialDelay = this.calculateInitialDelay();
		// Minimum initial delay as setConfigruation gets called 2x when config is updated via gui ;(
		if ( initialDelay < minIntialDelay )
		{
			if ( this.timeOfDay != null ) initialDelay += ( 24 * 60 * 60 );
			else initialDelay = minIntialDelay;
		}
		TimeUnit unit = TimeUnit.SECONDS;
		logger.info("Scheduling with initialDelay = {}, period = {}, unit = {}", initialDelay, this.period, unit);
		return this.scheduledExecutorService.scheduleAtFixedRate(this, initialDelay, this.period, unit);
	}
}
