package hxc.services.ecds.olapmodel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.Group;
import hxc.services.ecds.model.ICompanyData;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.util.RequiresTransaction;

@Table(name = "ap_agent_account",
	indexes = {
		@Index(name = "ap_agent_account_tier_name", columnList = "tier_name"),
		@Index(name = "ap_agent_account_group_name", columnList = "group_name"),
		@Index(name = "ap_agent_account_owner_id", columnList = "owner_id"),
		@Index(name = "ap_agent_account_owner_id", columnList = "owner_id")
	}
)
@Entity
//@NamedQueries({ @NamedQuery(name = "OlapAgentAccount.replace", query = "REPLACE INTO OlapAgentAccount (id, comp_id, msisdn, agent_name, balance, bonus_balance, hold_balance, tier_name, group_name) VALUES(:id, :comp_id, :msisdn, :agent_name, :balanc, :bonus_balance, :hold_balance, :tier_name, :group_name)") })
public class OlapAgentAccount implements Serializable
{
	private static final long serialVersionUID = -8199837621132702237L;

	protected int id;
	protected int companyID;
	protected String msisdn;
	/*
	 *	Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
	 */
	//protected boolean msisdnRecycled;
	protected String agentName;
	protected BigDecimal balance;
	protected BigDecimal bonusBalance;
	protected BigDecimal holdBalance;
	protected String tierName;
	protected String groupName;
	protected Integer ownerID;
	protected String state;

	@Id
	public int getId()
	{
		return this.id;
	}

	public OlapAgentAccount setId(int id)
	{
		this.id = id;
		return this;
	}

	@Column(name = "comp_id", nullable = false)
	public int getCompanyID()
	{
		return this.companyID;
	}

	public OlapAgentAccount setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Column(name = "msisdn", nullable = false, length = Transaction.MSISDN_MAX_LENGTH)
	public String getMsisdn()
	{
		return this.msisdn;
	}

	public OlapAgentAccount setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
		return this;
	}

	/*
	 *	Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
	 */
	/*@Column(name = "msisdn_recycled", nullable = false, length = 1)
	public boolean getMsisdnRecycled() {
		return msisdnRecycled;
	}

	public void setMsisdnRecycled(boolean msisdnRecycled) {
		this.msisdnRecycled = msisdnRecycled;
	}*/

	@Column(name = "agent_name", nullable = false, length = Agent.TITLE_MAX_LENGTH + Agent.FIRST_NAME_MAX_LENGTH + Agent.INITIALS_MAX_LENGTH + Agent.LAST_NAME_MAX_LENGTH + 3)
	public String getName()
	{
		return this.agentName;
	}

	public OlapAgentAccount setName(String agentName)
	{
		this.agentName = agentName;
		return this;
	}
	
	@Column(name = "balance", nullable = false, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getBalance()
	{
		return this.balance;
	}

	public OlapAgentAccount setBalance(BigDecimal balance)
	{
		this.balance = balance;
		return this;
	}

	@Column(name = "bonus_balance", nullable = false, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getBonusBalance()
	{
		return this.bonusBalance;
	}

	public OlapAgentAccount setBonusBalance(BigDecimal bonusBalance)
	{
		this.bonusBalance = bonusBalance;
		return this;
	}

	@Column(name = "hold_balance", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getHoldBalance()
	{
		return this.holdBalance;
	}

	public OlapAgentAccount setHoldBalance(BigDecimal holdBalance)
	{
		this.holdBalance = holdBalance;
		return this;
	}

	@Column(name = "tier_name", nullable = true, length = Tier.NAME_MAX_LENGTH)
	public String getTierName()
	{
		return this.tierName;
	}

	public OlapAgentAccount setTierName(String tierName)
	{
		this.tierName = tierName;
		return this;
	}

	@Column(name = "group_name", nullable = true, length = Group.NAME_MAX_LENGTH)
	public String getGroupName()
	{
		return this.groupName;
	}

	public OlapAgentAccount setGroupName(String groupName)
	{
		this.groupName = groupName;
		return this;
	}

	@Column(name = "owner_id", nullable = true)
	public Integer getOwnerID()
	{
		return this.ownerID;
	}

	public OlapAgentAccount setOwnerID(Integer ownerID)
	{
		this.ownerID = ownerID;
		return this;
	}

		// ---- Theoretically this should not be nullable.... however it is a COPY of the LIVE agents table.
		//		As such, when this is created for the first time, the associated migration script
		//			will line it up with the LIVE table.
		//		Thereafter the data will be a replica of live (which cannot be nulled) so can be ignored in this olap table
		//
	@Column(name = "state", nullable = true)
	public String getState()
	{
		return this.state;
	}

	public OlapAgentAccount setState(String state)
	{
		this.state = state;
		return this;
	}

	// Constructors

	public OlapAgentAccount()
	{
	}
	
	public OlapAgentAccount( int id, int companyID, String msisdn, String agentName, BigDecimal balance, BigDecimal bonusBalance, BigDecimal holdBalance, String tierName, String groupName, Integer ownerID, String state)
	{
		this.id = id;
		this.companyID = companyID;
		this.msisdn = msisdn;
		this.agentName = agentName;
		this.balance = balance;
		this.bonusBalance = bonusBalance;
		this.holdBalance = holdBalance;
		this.tierName = tierName;
		this.groupName = groupName;
		this.ownerID = ownerID;
		this.state = state;
	}

	public String describe(String extra)
	{
		return String.format("%s@%s("
			+ "id = '%s', companyID = '%s', msisdn = '%', agentName = '%s', balance = '%s', bonusBalance = '%s', holdBalance = '%s', tierName = %s, groupName = '%s', ownerID = '%s'"
			+ "%s%s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			id, companyID, msisdn, agentName, balance, bonusBalance, holdBalance, tierName, groupName, ownerID,
			(extra.isEmpty() ? "" : ", "), extra);
	}

	public String describe()
	{
		return this.describe("");
	}

	public String toString()
	{
		return this.describe();
	}
	
	public static int synchronizeState(EntityManager oltpEm, EntityManager apEm, int accountId)
	{
		List<Account> accounts = (List<Account>)oltpEm.createQuery("SELECT a FROM Account a WHERE id = :accountId")
													.setParameter("accountId", accountId)
													.setFirstResult(0)
													.setMaxResults(1)
													.getResultList();
		if ( accounts.iterator().hasNext() ) {
			Account account;
		
			try (RequiresTransaction scope = new RequiresTransaction(apEm))
			{
				try
				{
					account = accounts.iterator().next();	// select the ONLY record with hasNext()
					Agent agent = account.getAgent();
			
					OlapAgentAccount olapAgentAccount = new OlapAgentAccount();
					olapAgentAccount.setId(agent.getId());
					olapAgentAccount.setCompanyID(agent.getCompanyID());
					olapAgentAccount.setName(agent.getTitle() + " " + agent.getFirstName() + " " + agent.getSurname());
					olapAgentAccount.setMsisdn(agent.getMobileNumber());
					/*
					 *	Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
					 */
					//olapAgentAccount.setMsisdnRecycled(agent.getMsisdnRecycled());
					olapAgentAccount.setBalance(account.getBalance());
					olapAgentAccount.setBonusBalance(account.getBonusBalance());
					olapAgentAccount.setHoldBalance(account.getOnHoldBalance());
					olapAgentAccount.setTierName(agent.getTier() != null ? agent.getTier().getName() : null);
					olapAgentAccount.setGroupName(agent.getGroup() != null ? agent.getGroup().getName() : null);
					olapAgentAccount.setOwnerID(agent.getOwnerAgentID());
					olapAgentAccount.setState(agent.getState());
					apEm.merge(olapAgentAccount);
				}	
				finally
				{
					scope.commit();
				}
			}	
		}

		return 0;
	}
	
	// Statics

	public static int synchronize(EntityManager oltpEm, EntityManager apEm)
	{
		int count = 0;
		int start = 0;
		int limit = 10000;
		do
		{
			List<Account> accounts = (List<Account>)oltpEm.createQuery("SELECT a FROM Account a").setFirstResult(start).setMaxResults(limit).getResultList(); 
			count = accounts.size();
			Iterator i = accounts.iterator();
			Account account;
		
			try (RequiresTransaction scope = new RequiresTransaction(apEm))
			{
				try
				{
					while(i.hasNext())
					{
						account = (Account)i.next();
						Agent agent = account.getAgent();
			
						OlapAgentAccount olapAgentAccount = new OlapAgentAccount();
						olapAgentAccount.setId(agent.getId());
						olapAgentAccount.setCompanyID(agent.getCompanyID());
						olapAgentAccount.setName(agent.getTitle() + " " + agent.getFirstName() + " " + agent.getSurname());
						olapAgentAccount.setMsisdn(agent.getMobileNumber());
						/*
						 *	Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
						 */
						//olapAgentAccount.setMsisdnRecycled(agent.getMsisdnRecycled());
						olapAgentAccount.setBalance(account.getBalance());
						olapAgentAccount.setBonusBalance(account.getBonusBalance());
						olapAgentAccount.setHoldBalance(account.getOnHoldBalance());
						olapAgentAccount.setTierName(agent.getTier() != null ? agent.getTier().getName() : null);
						olapAgentAccount.setGroupName(agent.getGroup() != null ? agent.getGroup().getName() : null);
						olapAgentAccount.setOwnerID(agent.getOwnerAgentID());
						apEm.merge(olapAgentAccount);
					}
				}	
				finally
				{
					scope.commit();
				}
			}	

			start += count;
		}
		while(count == limit);

		return start;
	}
}
