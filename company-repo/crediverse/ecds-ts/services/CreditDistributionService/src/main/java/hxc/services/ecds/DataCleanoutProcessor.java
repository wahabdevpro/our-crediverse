package hxc.services.ecds;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.config.BatchConfig;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.ecds.protocol.rest.config.WebUIConfig;
import hxc.ecds.protocol.rest.config.WebUsersConfig;
import hxc.ecds.protocol.rest.config.WorkflowConfig;
import hxc.services.ecds.model.AuditEntry;
import hxc.services.ecds.model.Batch;
import hxc.services.ecds.model.ClientState;
import hxc.services.ecds.model.Stage;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.WorkItem;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.rest.TransactionState;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.utils.calendar.DateTime;

public class DataCleanoutProcessor implements Runnable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final int POLL_INTERVAL_MINUTES = 60;
	
	final static Logger logger = LoggerFactory.getLogger(DataCleanoutProcessor.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ICreditDistribution context;
	private CompanyInfo company;
	private String directory;
	private ScheduledFuture<?> future;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public DataCleanoutProcessor(ICreditDistribution context, CompanyInfo company)
	{
		this.context = context;
		this.company = company;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public void start(ScheduledThreadPoolExecutor scheduledThreadPool)
	{
		future = scheduledThreadPool.scheduleAtFixedRate(this, POLL_INTERVAL_MINUTES, POLL_INTERVAL_MINUTES, TimeUnit.MINUTES);
	}

	public void stop()
	{
		if (future != null)
		{
			future.cancel(true);
			future = null;
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Runnable
	//
	// /////////////////////////////////
	@Override
	public void run()
	{
		int companyID = company.getCompany().getId();
		logger.info("Starting Data Cleanout for Company {} to {}", companyID, directory);

		// Get Database
		try (EntityManagerEx em = context.getEntityManager())
		{
			// Clean-out OLTP Transactions
			DateTime now = DateTime.getNow();
			TransactionsConfig transactionsConfig = company.getConfiguration(em, TransactionsConfig.class);
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
			WebUsersConfig webUsersConfig = company.getConfiguration(em, WebUsersConfig.class);
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
			BatchConfig batchConfig = company.getConfiguration(em, BatchConfig.class);
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
			WorkflowConfig workflowConfig = company.getConfiguration(em, WorkflowConfig.class);
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
			WebUIConfig webUiConfig = company.getConfiguration(em, WebUIConfig.class);
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
		catch (Throwable tr)
		{
			logger.error("Data cleanout error", tr);
			return;
		}

		logger.info("Completed Data Cleanout for Company {}", companyID);

	}

}
