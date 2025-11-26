package hxc.services.ecds;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.snmp.ISnmpConnector;
import hxc.connectors.snmp.IncidentSeverity;
import hxc.ecds.protocol.rest.config.IConfiguration;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.servicebus.IServiceBus;
import hxc.services.IService;
import hxc.services.ecds.olapmodel.OlapAgentAccount;
import hxc.services.ecds.olapmodel.OlapGroup;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.IConfigurationChange;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.notification.IPhrase;

public class OlapSync implements Runnable, AutoCloseable, IConfigurationChange
{
	private final static Logger logger = LoggerFactory.getLogger(OlapSync.class);

	private IServiceBus esb;
	private ICreditDistribution context;
	private IService service;
	private CompanyInfo companyInfo;

	private ISnmpConnector snmp;
	private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

	private ScheduledFuture<?> future = null;
	private final Object futureMonitor = new Object();

	private int runStartCounter = 0;
	private int runCompleteCounter = 0;
	private final Object runCounterMonitor = new Object();

	private int timeOfDay;

	public OlapSync(IServiceBus esb, ICreditDistribution context, IService service, CompanyInfo companyInfo) throws RuntimeException
	{
		this.esb = esb;
		this.context = context;
		this.service = service;
		this.snmp = esb.getFirstConnector(ISnmpConnector.class);
		this.companyInfo = companyInfo;
		this.companyInfo.registerForConfigurationChangeNotifications(this);
		this.scheduledThreadPoolExecutor = esb.getScheduledThreadPool();
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

	public void start()
	{
		try (EntityManagerEx entityManager = context.getEntityManager())
		{
			TransactionsConfig configuration = companyInfo.getConfiguration(entityManager, TransactionsConfig.class);
			onConfigurationChanged(configuration);
		}
	}

	private void restart()
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

	private boolean startActual()
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

	public void runActual() throws Exception
	{
		synchronized (this.runCounterMonitor)
		{
			this.runStartCounter++;
			this.runCounterMonitor.notify();
		}
		try (EntityManagerEx apEm = this.context.getApEntityManager(); 
			EntityManagerEx oltpEm = this.context.getEntityManager() )
		{
			if (this.future == null)
			{
				logger.info("is stopped: bailing ...");
				return;
			}

			{
				logger.info("synchronizing agent account entries");
				int updated = -1;
				try (RequiresTransaction scope = new RequiresTransaction(apEm))
				{
					updated = OlapAgentAccount.synchronize(oltpEm, apEm);
					scope.commit();
				}
				logger.info("updated {} agent account entries", updated);
			}	
			
			{
				logger.info("synchronizing group entries");
				int updated = -1;
				try (RequiresTransaction scope = new RequiresTransaction(apEm))
				{
					updated = OlapGroup.synchronize(oltpEm, apEm);
					scope.commit();
				}
				logger.info("updated {} group entries", updated);
			}	
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

	// Runnable
	@Override
	public void run()
	{
		// TODO Ensure no concurrent runs ...
		logger.info("starting run ...");
		try
		{
			this.runActual();
		}
		catch (Throwable throwable)
		{
			String msg = String.format("OlapSync: run failed with: %s", throwable);
			logger.error(msg, throwable);
			this.snmp.jobFailed(this.service.getConfiguration().getName(IPhrase.ENG), IncidentSeverity.CRITICAL, msg);
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
		return calculateInitialDelay(this.timeOfDay);
	}

	private ScheduledFuture<?> schedule()
	{
		long initialDelay = this.calculateInitialDelay();
		long period = 24L * 60L * 60L;
		//long initialDelay = 30;
		//long period = 60L;
		TimeUnit unit = TimeUnit.SECONDS;
		logger.info("Scheduling with initialDelay = {}, period = {}, unit = {}", initialDelay, period, unit);
		return this.scheduledThreadPoolExecutor.scheduleAtFixedRate(this, initialDelay, period, unit);
	}

	public int getTimeOfDay()
	{
		return this.timeOfDay;
	}

	@Override
	public void onConfigurationChanged(IConfiguration configuration)
	{
		logger.info("loading configuration ...");
		if (!(configuration instanceof TransactionsConfig))
		{
			logger.trace("ignoring configuration change notification for {}", configuration);
			return;
		}
		TransactionsConfig config = (TransactionsConfig) configuration;
		this.timeOfDay = config.getOlapSyncTimeOfDay();

		this.restart();
	}
}
