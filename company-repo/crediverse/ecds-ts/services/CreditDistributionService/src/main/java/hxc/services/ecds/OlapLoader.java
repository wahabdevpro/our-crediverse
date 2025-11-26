package hxc.services.ecds;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.snmp.ISnmpConnector;
import hxc.connectors.snmp.IncidentSeverity;
import hxc.ecds.protocol.rest.Transaction;
import hxc.servicebus.IServiceBus;
import hxc.services.IService;
import hxc.services.ecds.olapmodel.OlapAgentAccount;
import hxc.services.ecds.olapmodel.OlapGroup;
import hxc.services.ecds.olapmodel.OlapTransaction;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.notification.IPhrase;

public class OlapLoader implements Runnable, AutoCloseable
{

	private Boolean closed = false;
	private final Object closedMonitor = new Object();
	private final static Logger logger = LoggerFactory.getLogger(OlapLoader.class);

	public Boolean isClosed()
	{
		synchronized (this.closedMonitor)
		{
			return Boolean.valueOf(this.closed);
		}
	}

	private IServiceBus esb;
	private ICreditDistribution context;
	private IService service;
	private ISnmpConnector snmp;

	private final Integer chunkSize;
	private final Integer checkClosedInterval;

	public OlapLoader(IServiceBus esb, ICreditDistribution context, IService service)
	{
		this(esb, context, service, 2500, 250);
	}

	public OlapLoader(IServiceBus esb, ICreditDistribution context, IService service, Integer chunkSize, Integer checkClosedInterval) throws RuntimeException
	{
		this.esb = esb;
		this.context = context;
		this.service = service;
		this.chunkSize = chunkSize;
		this.checkClosedInterval = checkClosedInterval;
		this.snmp = esb.getFirstConnector(ISnmpConnector.class);
	}

	// return false if process should end ...
	private boolean persistEntries(EntityManager apEm, List<OlapTransaction> olapTransactions, Date maxEnded)
	{
		logger.info(String.format("Persisting %1$d entries earlier than %2$tFT%2$tT", olapTransactions.size(), maxEnded));
		int runCounter = 0;
		try (RequiresTransaction scope = new RequiresTransaction(apEm))
		{
			try
			{
				for (OlapTransaction olapTransaction : olapTransactions)
				{
					if ((runCounter++ % this.checkClosedInterval) == 0 && this.isClosed())
					{
						logger.info("is closed, stopping ...");
						return false;
					}
					if (maxEnded != null && maxEnded.compareTo(olapTransaction.getEndTime()) < 0)
					{
						logger.info(String.format("Got transaction newer than maxEnded: %1$tFT%1$tT < %2$tFT%2$tT", maxEnded, olapTransaction.getEndTime()));
						return false;
					}
					logger.trace("Persisting transacton [{}]", olapTransaction.getId());
					apEm.persist(olapTransaction);

					// do not further examine non-successful transactions
					if ( (olapTransaction.getSuccess() == null) || (olapTransaction.getSuccess() == false) )
						continue;

					if( olapTransaction.getType().equals(Transaction.Type.Code.ADJUDICATE) )
					{
						Long relatedID = olapTransaction.getRelatedID();
						OlapTransaction relatedTransaction = apEm.find(OlapTransaction.class, relatedID);
						if (relatedTransaction != null)
							relatedTransaction.setFollowUp(OlapTransaction.FollowUp.ADJUDICATED);
						else
							logger.warn("Related 'follow-up' transaction not found, cannot update status to ADJUDICATED for Adjudication ID [{}] related ID [{}]", olapTransaction.getId(), relatedID);
					}
				
					if (olapTransaction.getA_AgentID() != null)
					{
						if ( (olapTransaction.getA_BalanceAfter() != null) && (olapTransaction.getA_BonusBalanceAfter() != null) )
						{
							OlapAgentAccount olapAgentAccount = new OlapAgentAccount();
							olapAgentAccount.setId(olapTransaction.getA_AgentID());
							olapAgentAccount.setCompanyID(olapTransaction.getCompanyID());
							olapAgentAccount.setMsisdn(olapTransaction.getA_MSISDN());
							olapAgentAccount.setName(olapTransaction.getA_AgentName());
							olapAgentAccount.setBalance(olapTransaction.getA_BalanceAfter());
							olapAgentAccount.setBonusBalance(olapTransaction.getA_BonusBalanceAfter());
							olapAgentAccount.setHoldBalance(olapTransaction.getA_OnHoldBalanceAfter());
							olapAgentAccount.setTierName(olapTransaction.getA_TierName());
							olapAgentAccount.setGroupName(olapTransaction.getA_GroupName());
							olapAgentAccount.setOwnerID(olapTransaction.getA_OwnerID());
							apEm.merge(olapAgentAccount);
						
							if (olapTransaction.getA_GroupID() != null)
							{
								OlapGroup olapGroup = new OlapGroup();
								olapGroup.setId(olapTransaction.getA_GroupID());
								olapGroup.setCompanyID(olapTransaction.getCompanyID());
								olapGroup.setName(olapTransaction.getA_GroupName());
								apEm.merge(olapGroup);
							}	
						}
						else
						{
							logger.warn("Transaction [{}] A [{}/{}] has null balance and/or bonus balance, not updating Agent/Group data", olapTransaction.getId(), olapTransaction.getA_AgentID(), olapTransaction.getA_MSISDN());
						}
					}	
					
					if ((olapTransaction.getB_AgentID() != null) && (olapTransaction.getA_AgentID() != olapTransaction.getB_AgentID()))
					{
						if ( (olapTransaction.getB_BalanceAfter() != null) && (olapTransaction.getB_BonusBalanceAfter() != null) )
						{
							OlapAgentAccount olapAgentAccount = new OlapAgentAccount();
							olapAgentAccount.setId(olapTransaction.getB_AgentID());
							olapAgentAccount.setCompanyID(olapTransaction.getCompanyID());
							olapAgentAccount.setMsisdn(olapTransaction.getB_MSISDN());
							olapAgentAccount.setName(olapTransaction.getB_AgentName());
							olapAgentAccount.setBalance(olapTransaction.getB_BalanceAfter());
							olapAgentAccount.setBonusBalance(olapTransaction.getB_BonusBalanceAfter());
							olapAgentAccount.setTierName(olapTransaction.getB_TierName());
							olapAgentAccount.setGroupName(olapTransaction.getB_GroupName());
							olapAgentAccount.setOwnerID(olapTransaction.getB_OwnerID());
							apEm.merge(olapAgentAccount);
						
							if (olapTransaction.getB_GroupID() != null)
							{
								OlapGroup olapGroup = new OlapGroup();
								olapGroup.setId(olapTransaction.getB_GroupID());
								olapGroup.setCompanyID(olapTransaction.getCompanyID());
								olapGroup.setName(olapTransaction.getB_GroupName());
								apEm.merge(olapGroup);
							}	
						}
						else
						{
							logger.warn("Transaction [{}] B [{}/{}] has null balance and/or bonus balance, not updating Agent/Group data", olapTransaction.getId(), olapTransaction.getB_AgentID(), olapTransaction.getB_MSISDN());
						}
					}	
				}
			}
			catch (Throwable throwable)
			{
				logger.error("Failed to persist", throwable);
				throw throwable;
			}
			finally
			{
				scope.commit();
			}
		}
		return true;
	}

	// return false (no more data): if there is no chunk size, or if less than chunk size was read.
	public boolean copyEntries(EntityManagerEx em, EntityManager apEm, long minId, Date maxEnded)
	{
		logger.info("Processing from id {}", minId);
		List<OlapTransaction> olapTransactions = OlapTransaction.createFromTransactions(em, minId, this.chunkSize);
		if (this.persistEntries(apEm, olapTransactions, maxEnded) == false)
		{
			return false;
		}
		if (this.chunkSize == null || (this.chunkSize > 0 && olapTransactions.size() < this.chunkSize))
		{
			logger.info("No more enties ... stopping");
			return false;
		}
		return true;
	}

	public void runActual() throws Exception
	{
		if ( this.context.isMasterServer() == false )
		{
			logger.info("Not master server ... ignoring ...");
			return;
		}
		try (EntityManagerEx em = this.context.getEntityManager(); EntityManagerEx apEm = this.context.getApEntityManager();)
		{
			Date maxEnded = new Date();
			while (true)
			{
				if (this.isClosed())
				{
					logger.info("is closed: stopping ...");
					break;
				}
				long minId = OlapTransaction.getNextId(apEm);
				if (this.copyEntries(em, apEm, minId, maxEnded) == false)
				{
					logger.info("stopping based on copy outcome ...");
					break;
				}
				logger.info("Transferring more transactions ...");
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
			String msg = String.format("OlapLoader: run failed with: %s", throwable);
			logger.error(msg, throwable);
			this.snmp.jobFailed(this.service.getConfiguration().getName(IPhrase.ENG), IncidentSeverity.CRITICAL, msg);
		}
		finally
		{
			logger.info("run ended");
		}

	}

	// AutoCloseable
	@Override
	public void close()
	{
		logger.info("closing ...");
		try
		{
			synchronized (this.closedMonitor)
			{
				this.closed = true;
			}
		}
		catch (Throwable throwable)
		{
			String msg = String.format("OlapLoader: close failed with: %s", throwable);
			logger.error(msg, throwable);
			this.snmp.jobFailed(this.service.getConfiguration().getName(IPhrase.ENG), IncidentSeverity.CRITICAL, msg);
		}
		finally
		{
			logger.info("closed");
		}
	}
}
