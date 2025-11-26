package hxc.connectors.tampercheck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IConnector;
import hxc.connectors.ecds.tampercheck.ITamperCheckConnector;
import hxc.connectors.ecds.tampercheck.ITamperedAccount;
import hxc.connectors.ecds.tampercheck.ITamperedAgent;
import hxc.connectors.ecds.tampercheck.ITamperedAuditEntry;
import hxc.connectors.ecds.tampercheck.ITamperedBatch;
import hxc.servicebus.IServiceBus;
import hxc.services.ecds.CreditDistribution;
import hxc.services.ecds.CreditDistribution.CreditDistributionConfig;
import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.AuditEntry;
import hxc.services.ecds.model.Batch;
import hxc.services.ecds.rest.RestParams;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.notification.INotifications;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.security.SupplierOnly;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.protocol.uiconnector.ecds.tampercheck.TamperedAccount;
import hxc.utils.protocol.uiconnector.ecds.tampercheck.TamperedAgent;
import hxc.utils.protocol.uiconnector.ecds.tampercheck.TamperedAuditEntry;
import hxc.utils.protocol.uiconnector.ecds.tampercheck.TamperedBatch;

public class TamperCheckConnector implements IConnector, ITamperCheckConnector
{
	final static Logger logger = LoggerFactory.getLogger(TamperCheckConnector.class);

	private IServiceBus esb;
	CreditDistribution creditDistribution;
	
	@Override
	public void initialise(IServiceBus esb) 
	{
		this.esb = esb;
		
	}

	public boolean start(String[] args) 
	{
		logger.info("Starting Tamper Check Service.");

		creditDistribution = esb.getFirstService(CreditDistribution.class);
		if(creditDistribution == null)
			return false;
		
		return true;
	}

	@Override
	public void stop()
	{
		logger.info("Stopping Tamper Check Service...");
		logger.info("Stopped Tamper Check Service.");
	}

	@Override
	public IConfiguration getConfiguration() 
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException 
	{
		this.config = (TamperCheckConfiguration) config;
	}

	@Override
	public boolean canAssume(String serverRole) 
	{
		return false;
	}

	@Override
	public IMetric[] getMetrics()
	{
		return null;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////
	@Perms(perms = { @Perm(name = "ViewEcdsTamperCheckParameters", description = "View ECDS Tamper Check Parameters", category = "C4U System Under Test", supplier = true),
			@Perm(name = "ChangeEcdsTamperCheckParameters", implies = "ViewEcdsTamperCheckParameters", description = "Change ECDS Tamper Check Parameters", category = "C4U System Under Test", supplier = true) })
	public class TamperCheckConfiguration extends ConfigurationBase
	{
		//private String huxUrl = "http://localhost:14000/RPC2";
		private String huxUrl = "http://localhost:14400/ecds/ussd";
		
		public void setHuxUrl(String huxUrl)
		{
			check(esb, "ChangeEcdsTamperCheckParameters");
			this.huxUrl = huxUrl;
		}
		
		@SupplierOnly
		public String getHuxUrl()
		{
			check(esb, "ViewEcdsTamperCheckParameters");
			return huxUrl;
		}

		@Override
		@SupplierOnly
		public String getPath(String languageCode)
		{
			return "Testing";
		}

		@Override
		@SupplierOnly		
		public INotifications getNotifications()
		{
			return null;
		}

		@Override
		public long getSerialVersionUID()
		{
			return 1253827320892618922L;
		}

		@Override
		@SupplierOnly		
		public String getName(String languageCode)
		{
			return "ECDS Tamper Check Connector";
		}

		@Override
		public void validate() throws ValidationException
		{

		}
	}

	private TamperCheckConfiguration config = new TamperCheckConfiguration();

	@Override
	public boolean isFit()
	{
		return true;
	}

	@Override
	public IConnection getConnection(String optionalConnectionString) throws IOException 
	{
		return null;
	}

	@Override
	public ITamperedAccount[] getTamperedAccounts()
	{
		if(creditDistribution != null)
		{
			CreditDistributionConfig configCreditDistribution = (CreditDistributionConfig)creditDistribution.getConfiguration();
			String connectionString = configCreditDistribution.getDatabaseConnectionString();
			logger.info("Tamper Check using connection string [{}]", connectionString);
			try(EntityManagerEx em = creditDistribution.getEntityManager())
			{
				RestParams params = new RestParams("");
				List<Account> ecdsAccountList = hxc.services.ecds.model.Account.findAll(em, params, 2);
				List<ITamperedAccount> uiAccountList = new ArrayList<hxc.connectors.ecds.tampercheck.ITamperedAccount>();
				for(Account account: ecdsAccountList)
				{
					if(account.isTamperedWith())
					{
						Agent agent = Agent.findByID(em, account.getAgentID(), 2);
						TamperedAccount uiAccount = new TamperedAccount();
						uiAccount.setAgentId(account.getAgentID());
						uiAccount.setAgentMsisdn(agent.getMobileNumber());
						uiAccount.setBalance(account.getBalance());
						uiAccount.setBonusBalance(account.getBonusBalance());
						uiAccount.setSignature(account.getSignature());
						uiAccount.setDay(account.getDay());
						uiAccount.setDayCount(account.getDayCount());
						uiAccount.setDayTotal(account.getDayTotal());
						uiAccount.setMonthCount(account.getMonthCount());
						uiAccount.setMonthTotal(account.getMonthTotal());					
						uiAccountList.add(uiAccount);
						logger.info("Account [{}] is tampered with", account.getAgentID());
					}
				}
				hxc.connectors.ecds.tampercheck.ITamperedAccount[] result = new hxc.connectors.ecds.tampercheck.ITamperedAccount[uiAccountList.size()];
				uiAccountList.toArray(result);			
				return result;
			}
		}	
		return null;
	}
	
	@Override
	public boolean resetAccounts()
	{
		if(creditDistribution != null)
		{
			CreditDistributionConfig configCreditDistribution = (CreditDistributionConfig)creditDistribution.getConfiguration();
			String connectionString = configCreditDistribution.getDatabaseConnectionString();
			logger.info("Tamper Check using connection string [{}]", connectionString);
			
			try(EntityManagerEx em = creditDistribution.getEntityManager())
			{
				RestParams params = new RestParams("");
				
				List<Account> ecdsAccountList = hxc.services.ecds.model.Account.findAll(em, params, 2);			
				for(Account account: ecdsAccountList)
				{
					if(account.isTamperedWith())
					{
						long signature = account.calcSecuritySignature();
						account.setSignature(signature);
						try (RequiresTransaction transaction = new RequiresTransaction(em))
						{
							em.persist(account);
							transaction.commit();
						}
						logger.info("Account [{}] was tampered with, but has been reset.", account.getAgentID());
					}
				}
			}
			return true;
		}	
		return false;
	}
	
	@Override
	public boolean resetAccount(String msisdn)
	{
		if(creditDistribution != null)
		{
			CreditDistributionConfig configCreditDistribution = (CreditDistributionConfig)creditDistribution.getConfiguration();
			String connectionString = configCreditDistribution.getDatabaseConnectionString();
			logger.info("Tamper Check using connection string [{}]", connectionString);
			try(EntityManagerEx em = creditDistribution.getEntityManager())
			{
				try (RequiresTransaction transaction = new RequiresTransaction(em))
				{
					Agent agent = hxc.services.ecds.model.Agent.findByMSISDN(em, msisdn, 2);
					Account account = hxc.services.ecds.model.Account.findByAgentID(em, agent.getId(), true);			
					if(account.isTamperedWith())
					{
						long signature = account.calcSecuritySignature();
						account.setSignature(signature);				
						em.persist(account);
						transaction.commit();
					}				
					logger.info("Account [{}] was tampered with, but has been reset.", account.getAgentID());
				}
			}
			return true;
		}	
		return false;
	}
	
	
	@Override
	public ITamperedAgent[] getTamperedAgents()
	{
		if(creditDistribution != null)
		{
			CreditDistributionConfig configCreditDistribution = (CreditDistributionConfig)creditDistribution.getConfiguration();
			String connectionString = configCreditDistribution.getDatabaseConnectionString();
			logger.info("Tamper Check using connection string [{}]", connectionString);
			
			try(EntityManagerEx em = creditDistribution.getEntityManager())
			{
				RestParams params = new RestParams("");
				
				List<hxc.services.ecds.model.Agent> ecdsAgentList = hxc.services.ecds.model.Agent.findAll(em, params, 2);
				List<ITamperedAgent> uiAgentList = new ArrayList<ITamperedAgent>();
				for(Agent agent: ecdsAgentList)
				{
					if(agent.isTamperedWith())
					{
						TamperedAgent uiAgent = new TamperedAgent();
						uiAgent.setId(agent.getId());
						uiAgent.setAccountNumber(agent.getAccountNumber());
						uiAgent.setMobileNumber(agent.getMobileNumber());
						uiAgent.setImei(agent.getImei());
						uiAgent.setImsi(agent.getImsi());
						uiAgent.setTitle(agent.getTitle());
						uiAgent.setFirstName(agent.getFirstName());
						uiAgent.setInitials(agent.getInitials());
						uiAgent.setSurname(agent.getSurname());
						uiAgent.setLanguage(agent.getLanguage());
						uiAgent.setDomainAccountName(agent.getDomainAccountName());
						uiAgent.setTierID(agent.getTierID());
						uiAgent.setGroupID(agent.getGroupID());
						uiAgent.setAreaID(agent.getAreaID());
						uiAgent.setServiceClassID(agent.getServiceClassID());
						uiAgent.setState(agent.getState());
						uiAgent.setActivationDate(agent.getActivationDate());
						uiAgent.setDeactivationDate(agent.getDeactivationDate());
						uiAgent.setExpirationDate(agent.getExpirationDate());
						uiAgent.setSupplierAgentID(agent.getSupplierAgentID());
						uiAgent.setAllowedChannels(agent.getAllowedChannels());
						uiAgent.setWarningThreshold(agent.getWarningThreshold());
						uiAgent.setMaxTransactionAmount(agent.getMaxTransactionAmount());
						uiAgent.setMaxDailyCount(agent.getMaxDailyCount());
						uiAgent.setMaxDailyAmount(agent.getMaxDailyAmount());
						uiAgent.setMaxMonthlyCount(agent.getMaxMonthlyCount());
						uiAgent.setMaxMonthlyAmount(agent.getMaxMonthlyAmount());
						uiAgent.setTemporaryPin(agent.isTemporaryPin());
						uiAgent.setLastImsiChange(agent.getLastImsiChange());
						uiAgent.setKey3(agent.getKey3());
						uiAgent.setKey1(agent.getKey1());
						uiAgent.setKey2(agent.getKey2());
						uiAgentList.add(uiAgent);
						logger.info("Agent [{}] is tampered with", agent.getId());
					}
				}
				ITamperedAgent[] result = new ITamperedAgent[uiAgentList.size()];
				uiAgentList.toArray(result);			
				return result;
			}
		}	
		return null;
	}
	
	@Override
	public boolean resetAgents()
	{
		if(creditDistribution != null)
		{
			CreditDistributionConfig configCreditDistribution = (CreditDistributionConfig)creditDistribution.getConfiguration();
			String connectionString = configCreditDistribution.getDatabaseConnectionString();
			logger.info("Tamper Check using connection string [{}]", connectionString);
			
			try(EntityManagerEx em = creditDistribution.getEntityManager())
			{
				RestParams params = new RestParams("");
				
				List<hxc.services.ecds.model.Agent> ecdsAgentList = hxc.services.ecds.model.Agent.findAll(em, params, 2);			
				for(Agent agent: ecdsAgentList)
				{
					if(agent.isTamperedWith())
					{
						long signature = agent.calcSecuritySignature();
						agent.setSignature(signature);					
						try (RequiresTransaction transaction = new RequiresTransaction(em))
						{
							em.persist(agent);
							transaction.commit();
						}
						logger.info("Agent [{}] was tampered with, but has been reset.", agent.getId());
					}
				}
				return true;
			}
		}	
		return false;
	}
	
	@Override
	public boolean resetAgent(String msisdn)
	{
		if(creditDistribution != null)
		{
			CreditDistributionConfig configCreditDistribution = (CreditDistributionConfig)creditDistribution.getConfiguration();
			String connectionString = configCreditDistribution.getDatabaseConnectionString();
			logger.info("Tamper Check using connection string [{}]", connectionString);			
			try(EntityManagerEx em = creditDistribution.getEntityManager())
			{
				Agent agent = hxc.services.ecds.model.Agent.findByMSISDN(em, msisdn, 2);			
				if(agent.isTamperedWith())
				{
					long signature = agent.calcSecuritySignature();
					agent.setSignature(signature);					
					try (RequiresTransaction transaction = new RequiresTransaction(em))
					{
						em.persist(agent);
						transaction.commit();
					}
					logger.info("Agent [{}] was tampered with, but has been reset.", agent.getId());
				}			
				return true;
			}
		}	
		return false;
	}
	
	@Override
	public ITamperedAuditEntry[] getTamperedAuditEntries()
	{
		if(creditDistribution != null)
		{
			CreditDistributionConfig configCreditDistribution = (CreditDistributionConfig)creditDistribution.getConfiguration();
			String connectionString = configCreditDistribution.getDatabaseConnectionString();
			logger.info("Tamper Check using connection string [{}]", connectionString);		
			try(EntityManagerEx em = creditDistribution.getEntityManager())
			{
				List<AuditEntry> ecdsAuditEntryList = AuditEntry.findLatest(em, 100, 2, "en");
				List<ITamperedAuditEntry> uiAuditEntryList = new ArrayList<ITamperedAuditEntry>();
				for(AuditEntry audit: ecdsAuditEntryList)
				{
					TamperedAuditEntry uiAuditEntry = new TamperedAuditEntry();
					uiAuditEntry.setId(audit.getId());
					uiAuditEntry.setCompanyId(audit.getCompanyID());
					uiAuditEntry.setSequenceNo(audit.getSequenceNo());
					uiAuditEntry.setUserId(audit.getUserID());
					uiAuditEntry.setTimestamp(audit.getTimestamp());
					uiAuditEntry.setIpAddress(audit.getIpAddress());
					uiAuditEntry.setMacAddress(audit.getMacAddress());
					uiAuditEntry.setMachineName(audit.getMachineName());
					uiAuditEntry.setDomainName(audit.getDomainName());
					uiAuditEntry.setDataType(audit.getDataType());
					uiAuditEntry.setAction(audit.getAction());
					uiAuditEntry.setOldValue(audit.getOldValue());
					uiAuditEntry.setNewValue(audit.getNewValue());
					uiAuditEntry.setSignature(audit.getSignature());
					uiAuditEntry.setTampered(audit.isTamperedWith());
					uiAuditEntryList.add(uiAuditEntry);				
				}
				ITamperedAuditEntry[] result = new ITamperedAuditEntry[uiAuditEntryList.size()];
				uiAuditEntryList.toArray(result);
				return result;
			}
		}
		return null;
	}
	
	@Override
	public boolean resetAuditEntries()
	{
		if(creditDistribution != null)
		{
			CreditDistributionConfig configCreditDistribution = (CreditDistributionConfig)creditDistribution.getConfiguration();
			String connectionString = configCreditDistribution.getDatabaseConnectionString();
			logger.info("Tamper Check using connection string [{}]", connectionString);
			try(EntityManagerEx em = creditDistribution.getEntityManager())
			{
				List<AuditEntry> ecdsAuditEntryList = AuditEntry.findLatest(em, 100, 2, "en");
				for(AuditEntry audit: ecdsAuditEntryList)
				{
					if(audit.isTamperedWith())
					{
						long signature = audit.calcSecuritySignature();
						audit.setSignature(signature);
						try (RequiresTransaction transaction = new RequiresTransaction(em))
						{
							em.persist(audit);
							transaction.commit();
						}					
					}
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public ITamperedBatch[] getTamperedBatches()
	{
		if(creditDistribution != null)
		{
			CreditDistributionConfig configCreditDistribution = (CreditDistributionConfig)creditDistribution.getConfiguration();
			String connectionString = configCreditDistribution.getDatabaseConnectionString();
			logger.info("Tamper Check using connection string [{}]", connectionString);
			try(EntityManagerEx em = creditDistribution.getEntityManager())
			{
				List<Batch> ecdsBatchList = Batch.findLatest(em, 100, 2);
				List<ITamperedBatch> uiBatchList = new ArrayList<ITamperedBatch>();
				for(Batch batch: ecdsBatchList)
				{
					TamperedBatch uiBatch = new TamperedBatch();
					uiBatch.setId(batch.getId());
					uiBatch.setCompanyID(batch.getCompanyID());
					uiBatch.setFilename(batch.getFilename());
					uiBatch.setInsertCount(batch.getInsertCount());
					uiBatch.setUpdateCount(batch.getUpdateCount());
					uiBatch.setDeleteCount(batch.getDeleteCount());
					uiBatch.setFailureCount(batch.getFailureCount());
					uiBatch.setTotalValue(batch.getTotalValue());
					uiBatch.setType(batch.getType());
					uiBatch.setFileSize(batch.getFileSize());
					uiBatch.setTimestamp(batch.getTimestamp());
					uiBatch.setWebUserID(batch.getWebUserID());
					uiBatch.setIpAddress(batch.getIpAddress());
					uiBatch.setMacAddress(batch.getMacAddress());
					uiBatch.setMachineName(batch.getMachineName());
					uiBatch.setDomainName(batch.getDomainName());
					uiBatch.setSignature(batch.getSignature());
					uiBatch.setTampered(batch.isTamperedWith());
					uiBatchList.add(uiBatch);								
				}
				ITamperedBatch[] result = new ITamperedBatch[uiBatchList.size()];
				uiBatchList.toArray(result);
				return result;
			}
		}
		return null;
	}
	
	@Override
	public boolean resetBatches()
	{
		if(creditDistribution != null)
		{
			CreditDistributionConfig configCreditDistribution = (CreditDistributionConfig)creditDistribution.getConfiguration();
			String connectionString = configCreditDistribution.getDatabaseConnectionString();
			logger.info("Tamper Check using connection string [{}]", connectionString);
			
			try(EntityManagerEx em = creditDistribution.getEntityManager())
			{
				RestParams params = new RestParams("");
				
				List<Batch> ecdsBatchList = Batch.findAll(em, params, 2);
				for(Batch batch: ecdsBatchList)
				{
					if(batch.isTamperedWith())
					{
						
						long signature = batch.calcSecuritySignature();
						batch.setSignature(signature);
						batch.setTamperedWith(false);					
						try (RequiresTransaction transaction = new RequiresTransaction(em))
						{
							em.persist(batch);
							transaction.commit();
						}
					}
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int checkTamperedAgent(String msisdn)
	{
		int result = 0;
		if(creditDistribution != null)
		{
			CreditDistributionConfig configCreditDistribution = (CreditDistributionConfig)creditDistribution.getConfiguration();
			String connectionString = configCreditDistribution.getDatabaseConnectionString();
			logger.info("Tamper Check using connection string [{}]", connectionString);
			try(EntityManagerEx em = creditDistribution.getEntityManager())
			{
				hxc.services.ecds.model.Agent agent = hxc.services.ecds.model.Agent.findByMSISDN(em, msisdn, 2);
				hxc.services.ecds.model.Account account = hxc.services.ecds.model.Account.findByAgentID(em, agent.getId(), false);
				result = (agent.isTamperedWith()?1:0) | (account.isTamperedWith()?2:0); //Kind of hacky... bit 1 = agent tampered, bit 2 = account tampered
			}
		}
		return result;
	}
}
