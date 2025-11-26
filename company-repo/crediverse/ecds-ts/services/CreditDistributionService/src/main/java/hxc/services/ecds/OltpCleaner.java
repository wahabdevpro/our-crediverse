package hxc.services.ecds;

import java.time.*;
import java.util.concurrent.*;

import org.slf4j.*;

import hxc.connectors.snmp.*;
import hxc.ecds.protocol.rest.config.*;
import hxc.servicebus.*;
import hxc.services.*;
import hxc.services.ecds.model.*;
//import hxc.services.ecds.rest.*;
import hxc.services.ecds.util.*;
import hxc.services.notification.*;
import hxc.utils.general.*;
import hxc.utils.calendar.*;

import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.rest.TransactionState;

public class OltpCleaner implements RunCounted.Forwarding, IConfigurationChange, AutoCloseable, ScheduledRunnable.Forwarding
{
	private final static Logger logger = LoggerFactory.getLogger(OltpCleaner.class);

    private final IServiceBus esb;
    private final ICreditDistribution context;
    private final IService service;
    private final ISnmpConnector snmp;

    private final CompanyInfo companyInfo;

	private final Runnable logic;
	private final RunCounted.Runnable.Default runCountedLogic;
	private final ScheduledRunnable scheduledRunnable;

    private int retentionDays;
    private LocalTime timeOfDay;

	public OltpCleaner(IServiceBus esb, ICreditDistribution context, IService service, CompanyInfo companyInfo)
	{
        this.esb = esb;
        this.context = context;
        this.service = service;
        this.snmp = esb.getFirstConnector(ISnmpConnector.class);

        this.companyInfo = companyInfo;
        this.companyInfo.registerForConfigurationChangeNotifications(this);

		this.logic = new OltpCleaner.Runnable();
		this.runCountedLogic = new RunCounted.Runnable.Default(logger, this.logic);
		this.scheduledRunnable = new ScheduledRunnable(logger, this.esb.getScheduledThreadPool());
	}

	private class Runnable implements java.lang.Runnable
	{
		public void run()
		{
			logger.info("run: starting ...");
			try
			{
				if ( OltpCleaner.this.context.isMasterServer() == false )
				{
					logger.info("Not master server ... ignoring ...");
					return;
				}
				int companyID = companyInfo.getCompany().getId();
				logger.info("Starting Data Cleanout for Company {}", companyID);
				try (EntityManagerEx em = context.getEntityManager())
				{
					// Clean-out OLTP Transactions
					DateTime now = DateTime.getNow();
					TransactionsConfig transactionsConfig = companyInfo.getConfiguration(em, TransactionsConfig.class);
					DateTime before = now.addDays(-transactionsConfig.getOltpTransactionRetentionDays());
					before = new DateTime(before.getYear(), before.getMonth(), before.getDay());
					try (RequiresTransaction scope = new RequiresTransaction(em))
					{
						int count = Transaction.cleanout(em, before, companyID);
						if (count > 0)
							logger.info("Purged {} old OLTP Transactions for Company {}", count, companyID);
						scope.commit();
					}

					// Cleanup Audit Entries
					WebUsersConfig webUsersConfig = companyInfo.getConfiguration(em, WebUsersConfig.class);
					before = now.addDays(-webUsersConfig.getAuditEntriesRetentionDays());
					before = new DateTime(before.getYear(), before.getMonth(), before.getDay());
					try (RequiresTransaction scope = new RequiresTransaction(em))
					{
						int count = AuditEntry.cleanout(em, before, companyID);
						if (count > 0)
							logger.info("Purged {} old Audit Entries for Company {}", count, companyID);
						scope.commit();
					}

					// Clean-out Batch History
					BatchConfig batchConfig = companyInfo.getConfiguration(em, BatchConfig.class);
					before = now.addDays(-batchConfig.getBatchEntriesRetentionDays());
					before = new DateTime(before.getYear(), before.getMonth(), before.getDay());
					try (RequiresTransaction scope = new RequiresTransaction(em))
					{
						int count = Batch.cleanout(em, before, companyID);
						if (count > 0)
							logger.info("Purged {} old Batch Entries for Company {}", count, companyID);
						scope.commit();
					}

					// Clean-out Batch Staging Entries
					try (RequiresTransaction scope = new RequiresTransaction(em))
					{
						int count = Stage.cleanout(em, companyID);
						if (count > 0)
							logger.info("Purged {} old Batch Staging Entries for Company {}", count, companyID);
						scope.commit();
					}

					// Cleanup Workflow Entries
					WorkflowConfig workflowConfig = companyInfo.getConfiguration(em, WorkflowConfig.class);
					before = now.addDays(-workflowConfig.getWorkItemRetentionDays());
					before = new DateTime(before.getYear(), before.getMonth(), before.getDay());
					try (RequiresTransaction scope = new RequiresTransaction(em))
					{
						int count = WorkItem.cleanout(em, before, companyID);
						if (count > 0)
							logger.info("Purged {} old Work Items for Company {}", count, companyID);
						scope.commit();
					}

					// Cleanup Client State
					WebUIConfig webUiConfig = companyInfo.getConfiguration(em, WebUIConfig.class);
					before = now.addDays(-webUiConfig.getClientStateRetentionDays());
					before = new DateTime(before.getYear(), before.getMonth(), before.getDay());
					try (RequiresTransaction scope = new RequiresTransaction(em))
					{
						int count = ClientState.cleanout(em, before, companyID);
						if (count > 0)
							logger.info("Purged {} old Client States for Company {}", count, companyID);
						scope.commit();
					}

					// Cleanup Location Cache
					int removedCachedEntries = TransactionState.cleanupLocationCache();
					logger.info("Purged {} expired location entries Company {}", removedCachedEntries, companyID);
				}
			}
			catch (Throwable throwable)
			{
				String msg = String.format("%s: run failed with: %s", this, throwable);
				logger.error(msg, throwable);
				snmp.jobFailed(service.getConfiguration().getName(IPhrase.ENG), IncidentSeverity.CRITICAL, msg);
			}
			finally
			{
				logger.info("run: completed ...");
			}
		}
	}

	private class ScheduledRunnable extends hxc.utils.general.ScheduledRunnable.Default
	{
		public ScheduledRunnable(Logger logger, ScheduledExecutorService scheduledExecutorService)
		{
			super(logger, scheduledExecutorService);
		}

		@Override
        public void startLogic()
		{
			runCountedLogic.resetRunCounters();
			try (EntityManagerEx entityManager = OltpCleaner.this.context.getEntityManager())
			{
				TransactionsConfig configuration = companyInfo.getConfiguration(entityManager, TransactionsConfig.class);
				OltpCleaner.this.processConfiguration(configuration);
			}
		}

		@Override
        public void stopLogic() {}

		@Override
        public ScheduledFuture<?> schedule(ScheduledExecutorService scheduledExecutorService)
		{
			LocalTime now = LocalTime.now();
			LocalTime startTime = OltpCleaner.this.timeOfDay;
			//if ( startTime.compareTo(now) < 0 ) { startTime = startTime.plusHours(24); }
			long initialDelay = startTime.toSecondOfDay() - now.toSecondOfDay();
			if ( initialDelay < 0 ) initialDelay += 24L * 60L * 60L;
			long period = 24L * 60L * 60L;
			TimeUnit unit = TimeUnit.SECONDS;
			logger.info("schedule: startTime = {}, now = {} -> initialDelay = {}, period = {}, unit = {}", startTime, now, initialDelay, period, unit);
			ScheduledFuture<?> result = scheduledExecutorService.scheduleAtFixedRate(OltpCleaner.this.runCountedLogic, initialDelay, period, unit);
			logger.info("schedule: result.getDelay({}) = {}", TimeUnit.NANOSECONDS, result.getDelay(TimeUnit.NANOSECONDS));
			return result;
		}
	}

	@Override
	public RunCounted delegateRunCounted() {
		return this.runCountedLogic;
	}

	@Override
	public ScheduledRunnable delegateScheduledRunnable() {
		return this.scheduledRunnable;
	}

    @Override
    public void onConfigurationChanged(IConfiguration configuration)
    {
		logger.info("onConfigurationChanged: ...");
		if (!(configuration instanceof TransactionsConfig))
		{
			logger.trace("ignoring configuration change notification for {}", configuration);
			return;
		}
		this.processConfiguration((TransactionsConfig)configuration);
		this.restart();
	}

	public void processConfiguration(TransactionsConfig configuration)
	{
		this.timeOfDay = configuration.getOltpTransactionCleanupTimeOfDay();
		this.retentionDays = configuration.getOltpTransactionRetentionDays();
	}

	@Override
	public void close()
	{
		this.scheduledRunnable.stop();
	}
}
