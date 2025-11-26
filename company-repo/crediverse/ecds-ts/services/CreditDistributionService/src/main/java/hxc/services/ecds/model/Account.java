package hxc.services.ecds.model;

import static hxc.ecds.protocol.rest.Agent.STATE_ACTIVE;
import static hxc.ecds.protocol.rest.Agent.STATE_PERMANENT;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.LockModeType;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.RestParams;
import hxc.services.ecds.util.PredicateExtender;
import hxc.services.ecds.util.QueryBuilder;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

////////////////////////////////////////////////////////////////////////////////////////
//
// Account Table - Used for Agent Balances
//
///////////////////////////////////

@Table(name = "ea_account")
@Entity
@NamedQueries({ //
		@NamedQuery(name = "Account.findByAgentID", query = "SELECT p FROM Account p where agentID = :agentID"), //
		@NamedQuery(name = "Account.findForDump", query = "SELECT p FROM Account p, Agent a where a.id = p.agentID and a.state IN :states and p.agentID > :lastAgentID and a.companyID = :companyID order by p.agentID"), //
		@NamedQuery(name = "Account.findForDumpWithDeleted", query = "SELECT p FROM Account p, Agent a where a.id = p.agentID and p.agentID > :lastAgentID and a.companyID = :companyID order by p.agentID"), //
		
		@NamedQuery(name = "Account.findForDumpWithLimit", query = "SELECT p FROM Account p, Agent a where a.id = p.agentID and a.state IN :states and p.agentID > :lastAgentID and a.companyID = :companyID and exists(select 1 from Transaction t where t.a_AgentID = p.agentID or t.b_AgentID = p.agentID and started between :startDate and :endDate) order by p.agentID "), //
		@NamedQuery(name = "Account.findForDumpWithDeletedAndLimit", query = "SELECT p FROM Account p, Agent a where a.id = p.agentID and p.agentID > :lastAgentID and a.companyID = :companyID and exists(select 1 from Transaction t where t.a_AgentID = p.agentID or t.b_AgentID = p.agentID and started between :startDate and :endDate) order by p.agentID") //
		
		
})
public class Account extends hxc.ecds.protocol.rest.Account //
		implements Serializable, IMasterData<Account>, ISecured<Account>
{
	final static Logger logger = LoggerFactory.getLogger(Account.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 6382006856122120202L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Additional Fields
	//
	// /////////////////////////////////
	@JsonIgnore
	protected Agent agent;
	@JsonIgnore
	protected int lastUserID;
	@JsonIgnore
	protected Date lastTime;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	@Id	
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer getID()
	{
		return id;
	}

	@Override
	public Account setID(Integer id)
	{
		this.id = id;
		return this;
	}
	
	
	@Override	
	@Column(name = "agent_id", nullable = false, insertable = false, updatable = false)
	public Integer getAgentID()
	{
		return agent.getId();
	}

	@Override
	public Account setAgentID(Integer agentID)
	{
		this.agentID = agentID;
		return this;
	}

	@Override
	@Version
	public int getVersion()
	{
		return version;
	}

	@Override
	public Account setVersion(int version)
	{
		this.version = version;
		return this;
	}

	@Override
	@Column(name = "balance", nullable = false, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getBalance()
	{
		return balance;
	}

	@Override
	public Account setBalance(BigDecimal balance)
	{
		this.balance = balance;
		return this;
	}

	@Override
	@Column(name = "bonus", nullable = false, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getBonusBalance()
	{
		return bonusBalance;
	}

	@Override
	public Account setBonusBalance(BigDecimal bonusBalance)
	{
		this.bonusBalance = bonusBalance;
		return this;
	}	
	
	@Override
	@Column(name = "on_hold", nullable = false, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getOnHoldBalance()
	{
		return onHoldBalance;
	}

	@Override
	public Account setOnHoldBalance(BigDecimal onHoldBalance)
	{
		this.onHoldBalance = onHoldBalance;
		return this;
	}

	@Override
	@Column(name = "signature", nullable = false)
	public long getSignature()
	{
		return signature;
	}

	@Override
	public Account setSignature(long signature)
	{
		this.signature = signature;
		return this;
	}

	@Transient
	@Override
	public boolean isTamperedWith()
	{
		return tamperedWith;
	}

	@Override
	public Account setTamperedWith(boolean tamperedWith)
	{
		this.tamperedWith = tamperedWith;
		return this;
	}

	@Override
	@Column(name = "day", nullable = false)
	public Date getDay()
	{
		return day;
	}

	@Override
	public Account setDay(Date day)
	{
		this.day = day;
		return this;
	}

	@Override
	@Column(name = "day_count", nullable = false)
	public int getDayCount()
	{
		return dayCount;
	}

	@Override
	public Account setDayCount(int dayCount)
	{
		this.dayCount = dayCount;
		return this;
	}

	@Override
	@Column(name = "day_total", nullable = false, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getDayTotal()
	{
		return dayTotal;
	}

	@Override
	public Account setDayTotal(BigDecimal dayTotal)
	{
		this.dayTotal = dayTotal;
		return this;
	}

	@Override
	@Column(name = "month_count", nullable = false)
	public int getMonthCount()
	{
		return monthCount;
	}

	@Override
	public Account setMonthCount(int monthCount)
	{
		this.monthCount = monthCount;
		return this;
	}

	@Override
	@Column(name = "month_total", nullable = false, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getMonthTotal()
	{
		return monthTotal;
	}

	@Override
	public Account setMonthTotal(BigDecimal monthTotal)
	{
		this.monthTotal = monthTotal;
		return this;
	}

	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "agent_id", foreignKey = @ForeignKey(name = "FK_Account_Agent"))
	@MapsId()
	public Agent getAgent()
	{
		return agent;
	}

	public Account setAgent(Agent agent)
	{
		this.agentID = agent.getId();
		this.agent = agent;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public Account setLastUserID(int lastUserID)
	{
		this.lastUserID = lastUserID;
		return this;
	}

	@Override
	@Column(name = "lm_time", nullable = false)
	public Date getLastTime()
	{
		return lastTime;
	}

	@Override
	public Account setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Finders
	//
	// /////////////////////////////////
	public static Account findByAgentID(EntityManager em, int agentID, boolean forUpdate)
	{
		TypedQuery<Account> query = em.createNamedQuery("Account.findByAgentID", Account.class);
		query.setParameter("agentID", agentID);
		query.setMaxResults(1);
		if (forUpdate)
		{
			query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
		}
		List<Account> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}
	
	public static List<Account> findAll(EntityManager em, RestParams params, int companyID)
	{
		addCompanyFilter(params, companyID);
		return QueryBuilder.getQueryResultList(em, Account.class, params, companyID, //
				"agent.accountNumber", "agent.mobileNumber", "agent.imsi", "agent.firstName", "agent.surname", "agent.domainAccountName");
	}
	
	public static List<Account> findMine(EntityManager em, RestParams params, int companyID, int myID)
	{
		addCompanyFilter(params, companyID);
		AccountExtender px = new Account.AccountExtender(myID);
		return QueryBuilder.getQueryResultList(em, Account.class, params, companyID, px, //
				"agent.accountNumber", "agent.mobileNumber", "agent.imsi", "agent.firstName", "agent.surname", "agent.domainAccountName");
	}
	
	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{
		addCompanyFilter(params, companyID);
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, Account.class, params, companyID, //
				"agent.accountNumber", "agent.mobileNumber", "agent.imsi", "agent.firstName", "agent.surname", "agent.domainAccountName");
		return query.getSingleResult();
	}

	public static Long findMyCount(EntityManager em, RestParams params, int companyID, int myID)
	{
		addCompanyFilter(params, companyID);
		AccountExtender px = new Account.AccountExtender(myID);
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, Account.class, params, companyID, px, //
				"agent.accountNumber", "agent.mobileNumber", "agent.imsi", "agent.firstName", "agent.surname", "agent.domainAccountName");
		return query.getSingleResult();
	}

	public static List<Account> findForDump(EntityManager em, int companyID, int lastAgentID, int batchSize, boolean includeDeleted, long activityValue)
	{
		List<Account> results = null;
		if (activityValue < 0) {
			if (includeDeleted) {
				TypedQuery<Account> query = em.createNamedQuery("Account.findForDumpWithDeleted", Account.class);
				query.setParameter("companyID", companyID);
				query.setParameter("lastAgentID", lastAgentID);
				query.setMaxResults(batchSize);
				results = query.getResultList();
			}
			else {
				TypedQuery<Account> query = em.createNamedQuery("Account.findForDump", Account.class);
				query.setParameter("companyID", companyID);
				query.setParameter("lastAgentID", lastAgentID);
				query.setParameter("states", Arrays.asList(STATE_ACTIVE, STATE_PERMANENT));
				query.setMaxResults(batchSize);
				results = query.getResultList();
			}
		}
		else {
			Calendar now = Calendar.getInstance();
			Date endDate = now.getTime();
			//now.add(Calendar.DATE, -10);
			Long offsetDays = Long.valueOf(activityValue);
			offsetDays = -offsetDays;
			now.add(Calendar.DATE, offsetDays.intValue());
			Date startDate = now.getTime();
			if (includeDeleted) {
				TypedQuery<Account> query = em.createNamedQuery("Account.findForDumpWithLimit", Account.class);
				query.setParameter("companyID", companyID);
				query.setParameter("lastAgentID", lastAgentID);
				query.setParameter("states", Arrays.asList(STATE_ACTIVE, STATE_PERMANENT));
				query.setParameter("startDate", startDate);
				query.setParameter("endDate", endDate);
				query.setMaxResults(batchSize);
				results = query.getResultList();
			}
			else {
				TypedQuery<Account> query = em.createNamedQuery("Account.findForDumpWithDeletedAndLimit", Account.class);
				query.setParameter("companyID", companyID);
				query.setParameter("lastAgentID", lastAgentID);
				query.setParameter("startDate", startDate);
				query.setParameter("endDate", endDate);
				query.setMaxResults(batchSize);
				results = query.getResultList();
			}
		}
		
		
		
		/*TypedQuery<Account> query = em.createNamedQuery("Account.findForDump", Account.class);
		
		//Account.findForDumpWithDeleted
		
		
		query.setParameter("companyID", companyID);
		query.setParameter("lastAgentID", lastAgentID);
		query.setParameter("agentState", Agent.STATE_ACTIVE);
		query.setMaxResults(batchSize);
		List<Account> results = query.getResultList();*/
		return results;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IMasterData
	//
	// /////////////////////////////////

	@Override
	public void persist(EntityManager em, Account oldValue, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(oldValue);
		lastUserID = session.getUserID();
		lastTime = new Date();
		em.persist(this);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		// Not Allowed if Balances are non-Zero
		if (balance != null && balance.signum() != 0)
			throw new RuleCheckException(StatusCode.NON_ZERO_BALANCE, "balance", "Balance not Zero");

		if (bonusBalance != null && bonusBalance.signum() != 0)
			throw new RuleCheckException(StatusCode.NON_ZERO_BALANCE, "bonusBalance", "Bonus Balance not Zero");

		if (onHoldBalance != null && onHoldBalance.signum() != 0)
			throw new RuleCheckException(StatusCode.NON_ZERO_BALANCE, "onHoldBalance", "On Hold Balance not Zero");
		
		// Not Allowed for Permanent
		RuleCheck.isFalse(null, STATE_PERMANENT.equals(agent.getState()), "Cannot delete Permanent Account");

		em.remove(this);
	}

	@Override
	public void validate(Account oldValue) throws RuleCheckException
	{
		RuleCheck.validate(this);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ISecured
	//
	// /////////////////////////////////

	// Update Security Signature
	@Override
	public long calcSecuritySignature()
	{
		return 0;
//		SignCheck signCheck = new SignCheck() //
//				.add("agentID", agentID)
//				.add("balance", balance) //
//				.add("bonusBalance", bonusBalance)
//				.add("day", day) //
//				.add("dayCount", dayCount) //
//				.add("dayTotal", dayTotal) //
//				.add("monthCount", monthCount) //
//				.add("monthTotal", monthTotal); //
//		long signature = signCheck.signature();
//		return signature;
	}

	@PreUpdate
	@PrePersist
	@Override
	public void onPrePersist()
	{
//		signature = calcSecuritySignature();
//		tamperedWith = false;
	}

	@PostLoad
	@Override
	public void onPostLoad()
	{
//		tamperedWith = calcSecuritySignature() != signature;
//		if (tamperedWith)
//			logger.warn("Tampering Detected on Account! AccountID=[{}]; AgentID = [{}]", id, getAgentID());
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	// Adjust balances up/down
	public void adjust(BigDecimal deltaBalance, BigDecimal deltaBonus, boolean root) throws RuleCheckException
	{
		BigDecimal newBalance = balance;
		BigDecimal newBonusBalance = bonusBalance;

		// if (isTamperedWith())
		// throw new RuleCheckException(TransactionsConfig.ERR_TECHNICAL_PROBLEM, null, "Account Tampered with!");

		if (deltaBalance != null)
		{
			newBalance = balance.add(deltaBalance);
			if (newBalance.signum() < 0)
				throw new RuleCheckException(TransactionsConfig.ERR_INSUFFICIENT_FUNDS, "amount", "Insufficient funds");
		}
		if (deltaBonus != null)
		{
			newBonusBalance = bonusBalance.add(deltaBonus);
			if (root && newBonusBalance.signum() < 0)
				throw new RuleCheckException(TransactionsConfig.ERR_INSUFFICIENT_PROVISION, "amount", "Insufficient bonus provision");
		}

		balance = newBalance;
		bonusBalance = newBonusBalance;
	}

	// Transact by decrementing balances and updating AML statistics
	public void transact(Date time, BigDecimal amount, BigDecimal bonusAmount, BigDecimal bonusProvision, boolean fromRoot) throws RuleCheckException
	{
		// Coerce
		if (amount == null)
			amount = BigDecimal.ZERO;
		if (bonusAmount == null)
			bonusAmount = BigDecimal.ZERO;
		if (bonusProvision == null)
			bonusProvision = BigDecimal.ZERO;

		// Decrement Balance
		if (amount.compareTo(balance) > 0)
			throw new RuleCheckException(TransactionsConfig.ERR_INSUFFICIENT_FUNDS, "amount", "Insufficient funds");
		balance = balance.subtract(amount);


		// Decrement Bonus Balance
		BigDecimal totalBonusAmount = bonusAmount.add(bonusProvision);

		logger.info("totalBonusAmount = {}", totalBonusAmount );
		logger.info("bonusBalance= {}" , bonusBalance);
		logger.info("fromRoot= {}" , fromRoot);

		if (fromRoot && totalBonusAmount.compareTo(bonusBalance) > 0)
			throw new RuleCheckException(TransactionsConfig.ERR_INSUFFICIENT_PROVISION, "amount", "Insufficient funds");
		bonusBalance = bonusBalance.subtract(totalBonusAmount);

		// Reset Totals upon day/month change
		Calendar now = Calendar.getInstance();
		now.setTime(time);
		Calendar then = Calendar.getInstance();
		then.setTime(day == null ? new Date(0) : day);
		boolean monthChanged = now.get(Calendar.YEAR) != then.get(Calendar.YEAR) //
				|| now.get(Calendar.MONTH) != then.get(Calendar.MONTH);
		if (monthChanged || now.get(Calendar.DAY_OF_MONTH) != then.get(Calendar.DAY_OF_MONTH))
		{
			day = time;
			dayCount = 0;
			dayTotal = BigDecimal.ZERO;
			if (monthChanged)
			{
				monthCount = 0;
				monthTotal = BigDecimal.ZERO;
			}
		}

		// Increment Totals
		dayCount++;
		dayTotal = dayTotal.add(amount);
		monthCount++;
		monthTotal = monthTotal.add(amount);

	}

	public void reverse(Date time, BigDecimal amount, BigDecimal bonusAmount, BigDecimal bonusProvision, boolean partialOnly) throws RuleCheckException
	{
		// Coerce
		if (amount == null)
			amount = BigDecimal.ZERO;
		if (bonusAmount == null)
			bonusAmount = BigDecimal.ZERO;
		if (bonusProvision == null)
			bonusProvision = BigDecimal.ZERO;

		// Increment Balance
		balance = balance.add(amount);

		// Decrement Bonus Balance
		bonusBalance = bonusBalance.subtract(bonusProvision).add(bonusAmount); 

		// Reset Totals upon day/month change
		Calendar now = Calendar.getInstance();
		now.setTime(time);
		Calendar then = Calendar.getInstance();
		then.setTime(day == null ? new Date(0) : day);
		boolean sameMonth = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) && now.get(Calendar.MONTH) == then.get(Calendar.MONTH);
		if (sameMonth)
		{
			monthTotal = monthTotal.subtract(amount);
			if (!partialOnly)
				monthCount--;
			boolean sameDay = sameMonth && now.get(Calendar.DAY_OF_MONTH) == then.get(Calendar.DAY_OF_MONTH);
			if (sameDay)
			{
				dayTotal = dayTotal.subtract(amount);
				if (!partialOnly)
					dayCount--;
			}
		}

	}

	public void testAmlLimits(IAntiLaunder<?> limits, BigDecimal amount) throws RuleCheckException
	{
		if (limits == null)
			return;

		if (limits.getMaxTransactionAmount() != null && amount != null && amount.compareTo(limits.getMaxTransactionAmount()) > 0)
			throw new RuleCheckException(TransactionsConfig.ERR_MAX_AMOUNT_LIMIT, "amount", "Maximum Amount Exceeded");

		if (limits.getMaxDailyCount() != null && dayCount > limits.getMaxDailyCount())
			throw new RuleCheckException(TransactionsConfig.ERR_DAY_COUNT_LIMIT, "amount", "Daily Count Exceeded");

		if (limits.getMaxDailyAmount() != null && dayTotal.compareTo(limits.getMaxDailyAmount()) > 0)
			throw new RuleCheckException(TransactionsConfig.ERR_DAY_AMOUNT_LIMIT, "amount", "Daily Amount Exceeded");

		if (limits.getMaxMonthlyCount() != null && monthCount > limits.getMaxMonthlyCount())
			throw new RuleCheckException(TransactionsConfig.ERR_MONTH_COUNT_LIMIT, "amount", "Monthly Count Exceeded");

		if (limits.getMaxMonthlyAmount() != null && monthTotal.compareTo(limits.getMaxMonthlyAmount()) > 0)
			throw new RuleCheckException(TransactionsConfig.ERR_MONTH_AMOUNT_LIMIT, "amount", "Monthly Amount Exceeded");
	}

	private static void addCompanyFilter(RestParams params, int companyID)
	{
		if (params == null)
			return;
		String companyFilter = String.format("agent.companyID='%d'", companyID);
		String filter = params.getFilter();
		if (filter == null || filter.isEmpty())
			filter = companyFilter;
		else
			filter += "+" + companyFilter;
		params.setFilter(filter);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Classes
	//
	// /////////////////////////////////
	static class AccountExtender extends PredicateExtender<Account>
	{
		private int myID;

		public AccountExtender(int myID)
		{
			this.myID = myID;
		}

		@Override
		public String getName()
		{
			return "MyAgentAccounts";
		}

		@Override
		public List<Predicate> extend(CriteriaBuilder cb, Root<Account> root, CriteriaQuery<?> query, List<Predicate> predicates)
		{
			Predicate p1 = cb.equal(col(root, "agentID"), cb.parameter(Integer.class, "myID"));
			Predicate p2 = cb.isNotNull(col(root, "agent.ownerAgentID"));
			Predicate p3 = cb.equal(col(root, "agent.ownerAgentID"), cb.parameter(Integer.class, "myID"));

			predicates.add(cb.or(p1, cb.and(p2, p3)));

			return predicates;
		}

		@Override
		public void addParameters(TypedQuery<?> query)
		{
			query.setParameter("myID", myID);
		}

	};

}
