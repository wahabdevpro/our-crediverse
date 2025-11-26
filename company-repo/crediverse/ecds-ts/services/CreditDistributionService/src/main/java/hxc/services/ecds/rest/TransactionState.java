package hxc.services.ecds.rest;

import static hxc.services.ecds.Session.CHANNEL_3PP;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.hlr.IHlrInformation;
import hxc.ecds.protocol.rest.TransactionRequest;
import hxc.ecds.protocol.rest.TransactionResponse;
import hxc.ecds.protocol.rest.config.AgentsConfig;
import hxc.ecds.protocol.rest.config.IConfiguration;
import hxc.ecds.protocol.rest.config.RewardsConfig;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.AgentUser;
import hxc.services.ecds.model.Area;
import hxc.services.ecds.model.Cell;
import hxc.services.ecds.model.IAgentUser;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransferRule;
import hxc.services.ecds.rewards.RewardAssessor;
import hxc.services.ecds.util.LastMismatch;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;
import hxc.utils.calendar.DateTime;

public class TransactionState<Treq extends TransactionRequest, Tresp extends TransactionResponse>
{
	protected static class CachedCell
	{
		private Cell cell;
		private Date expiry;

		public CachedCell(Cell cell, int expireAfterMinutes)
		{
			this.cell = cell;
			this.expiry = new DateTime(new Date()).addMinutes(expireAfterMinutes);
		}

		public Cell getCell()
		{
			return cell;
		}

		public boolean isExpired()
		{
			return expiry.compareTo(new Date()) <= 0;
		}
	}

	protected static class LocationCache extends HashMap<String, CachedCell>
	{
		@Override
		public synchronized	CachedCell put(String key, CachedCell value)
		{
			return (CachedCell)super.put(key, value);
		}

     	public synchronized Cell getCell(String key)
		{
			CachedCell cachedCell = get(key);
			if (cachedCell == null) return null;
			if (cachedCell.isExpired()) return null;
			return cachedCell.getCell();
		}

		public synchronized int clearExpired()
		{
			int count = 0;
			Iterator<Map.Entry<String, CachedCell>> i = entrySet().iterator();
            while(i.hasNext()) 
			{
				Map.Entry<String, CachedCell> entry = i.next();
				if (entry.getValue().isExpired())
				{
					i.remove();
					count++;
				}	
			}
			return count;
		}
	}

	final static Logger logger = LoggerFactory.getLogger(TransactionState.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Treq request;
	private Tresp response;
	private Transaction transaction;
	private Session session;
	private Map<String, Object> values = new HashMap<String, Object>();
	private IConfiguration config = null;
	private ICreditDistribution context;
	private BigDecimal ulp = null;
	private int step = 0;
	private boolean cannotLocateAgentA = false;
	private static LocationCache locationCache = new LocationCache();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public Treq getRequest()
	{
		return request;
	}

	public TransactionState<Treq, Tresp> setRequest(Treq request)
	{
		this.request = request;
		return this;
	}

	public Tresp getResponse()
	{
		return response;
	}

	public TransactionState<Treq, Tresp> setResponse(Tresp response)
	{
		this.response = response;
		return this;
	}

	public Transaction getTransaction()
	{
		return transaction;
	}

	public TransactionState<Treq, Tresp> setTransaction(Transaction transaction)
	{
		this.transaction = transaction;
		return this;
	}

	public Session getSession()
	{
		return session;
	}

	public TransactionState<Treq, Tresp> setSession(Session session)
	{
		this.session = session;
		return this;
	}

	public ICreditDistribution getContext()
	{
		return this.context;
	}

	public void setContext(ICreditDistribution context)
	{
		this.context = context;
	}

	public int getStep()
	{
		return step;
	}

	public void setStep(int step)
	{
		this.step = step;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String key)
	{
		return (T) values.get(key);
	}

	public TransactionState<Treq, Tresp> set(String key, Object value)
	{
		if (value == null)
			values.remove(key);
		else
			values.put(key, value);
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	private static synchronized LocationCache getLocationCache()
	{
		if (locationCache == null)
			locationCache = new LocationCache();
		return locationCache;
	}

	public TransactionState<Treq, Tresp> setRequestInfo(String a_msisdn, String b_msisdn, BigDecimal amount)
	{
		transaction //
				.setA_MSISDN(a_msisdn) //
				.setB_MSISDN(b_msisdn) //
				.setAmount(amount); //
		return this;
	}

	public TransactionState<Treq, Tresp> setAgentA(Agent agentA)
	{
		transaction //
				.setA_AgentID(agentA.getId()) //
				.setA_Agent(agentA) //
				.setA_MSISDN(agentA.getMobileNumber()) //
				.setA_TierID(agentA.getTierID()) //
				.setA_Tier(agentA.getTier()) //
				.setA_ServiceClassID(agentA.getServiceClassID()) //
				.setA_ServiceClass(agentA.getServiceClass()) //
				.setA_GroupID(agentA.getGroupID()) //
				.setA_Group(agentA.getGroup()) //
				.setA_OwnerAgentID(agentA.getOwnerAgentID()) //
				.setA_Owner(agentA.getOwner()) //
				.setA_AreaID(agentA.getAreaID()) //
				.setA_Area(agentA.getArea()) //
				.setA_IMSI(agentA.getImsi()) //
				.setA_IMEI(agentA.getImei()); //
		return this;
	}

	public Agent getAgentA()
	{
		return transaction.getA_Agent();
	}

	public TransactionState<Treq, Tresp> setBeforeA(Account accountA)
	{
		if (accountA != null)
		{
			transaction //
					.setA_BalanceBefore(accountA.getBalance()) //
					.setA_BonusBalanceBefore(accountA.getBonusBalance()) //
					.setA_OnHoldBalanceBefore(accountA.getOnHoldBalance()) //
					.setA_BalanceAfter(accountA.getBalance()) //
					.setA_BonusBalanceAfter(accountA.getBonusBalance()) //
					.setA_OnHoldBalanceAfter(accountA.getOnHoldBalance());

		}
		return this;
	}

	public TransactionState<Treq, Tresp> setAfterA(Account accountA)
	{
		if (accountA != null)
		{
			transaction //
					.setA_BalanceAfter(accountA.getBalance()) //
					.setA_BonusBalanceAfter(accountA.getBonusBalance()) //
					.setA_OnHoldBalanceAfter(accountA.getOnHoldBalance());
		}
		return this;
	}

	public TransactionState<Treq, Tresp> setAgentB(Agent agentB)
	{
		transaction //
				.setB_AgentID(agentB.getId()) //
				.setB_Agent(agentB) //
				.setB_MSISDN(agentB.getMobileNumber()) //
				.setB_TierID(agentB.getTierID()) //
				.setB_Tier(agentB.getTier()) //
				.setB_ServiceClassID(agentB.getServiceClassID()) //
				.setB_ServiceClass(agentB.getServiceClass()) //
				.setB_GroupID(agentB.getGroupID()) //
				.setB_Group(agentB.getGroup()) //
				.setB_OwnerAgentID(agentB.getOwnerAgentID()) //
				.setB_Owner(agentB.getOwner()) //
				.setB_AreaID(agentB.getAreaID()) //
				.setB_Area(agentB.getArea()) //
				.setB_IMSI(agentB.getImsi()) //
				.setB_IMEI(agentB.getImei()); //
		return this;
	}

	public Agent getAgentB()
	{
		return transaction.getB_Agent();
	}

	public TransactionState<Treq, Tresp> setBeforeB(Account accountB)
	{
		if (accountB != null)
		{
			transaction //
					.setB_BalanceBefore(accountB.getBalance()) //
					.setB_BonusBalanceBefore(accountB.getBonusBalance()) //
					.setB_BalanceAfter(accountB.getBalance()) //
					.setB_BonusBalanceAfter(accountB.getBonusBalance());
		}
		return this;
	}

	public TransactionState<Treq, Tresp> setAfterB(Account accountB)
	{
		if (accountB != null)
		{
			transaction //
					.setB_BalanceAfter(accountB.getBalance()) //
					.setB_BonusBalanceAfter(accountB.getBonusBalance());
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T extends IConfiguration> T getConfig(EntityManager em, Class<T> cls)
	{
		if (config != null && config.getClass().equals(cls))
			return (T) config;

		CompanyInfo company = context.findCompanyInfoByID(session.getCompanyID());
		if (company == null)
			return null;

		config = company.getConfiguration(em, cls);

		return (T) config;
	}

	public Locale getLocale(String languageID)
	{
		return session.getLocale(languageID);
	}

	public Locale getLocale()
	{
		return session.getLocale();
	}

	public void exitWith(String returnCode, String additionalInformation, Object... args)
	{
		response.exitWith(returnCode, additionalInformation, args);
	}

	public Agent getSessionAgent(EntityManager em, boolean reload)
	{
		Integer agentID = session.getAgentID();
		if (agentID == null)
			return null;
		Agent agent = session.getAgent();
		boolean foundInSession = false;
		if (reload || agent == null)
			agent = Agent.findByID(em, agentID, session.getCompanyID());
		else if (!em.contains(agent))
			agent = Agent.findByID(em, agentID, session.getCompanyID());
		else
			foundInSession = true;
		if (!foundInSession && agent != null)
			session.setAgent(agent);
		return agent;
	}

	public IAgentUser getSessionUser(EntityManager em)
	{
		IAgentUser user = session.getAgentUser();
		if (user == null)
			return null;
		if (em.contains(user))
			return user;
		boolean isAgent = user instanceof Agent;
		if (!isAgent)
			user = AgentUser.findByID(em, user.getId(), user.getCompanyID());
		else
			user = getSessionAgent(em, false);

		session.setAgentUser(user);
		return user;
	}

	public Session getSession(String coSignatorySessionID) throws RuleCheckException
	{
		if (coSignatorySessionID == null || coSignatorySessionID.isEmpty())
			return null;

		return context.getSession(coSignatorySessionID);
	}

	public void testAmountDecimalDigits(BigDecimal amount) throws RuleCheckException
	{
		if (ulp == null)
			ulp = BigDecimal.ONE.movePointLeft(context.getMoneyScale());

		if (amount == null || amount.signum() == 0 || amount.stripTrailingZeros().ulp().compareTo(ulp) >= 0)
			return;
		throw new RuleCheckException(TransactionsConfig.ERR_INVALID_AMOUNT, null, "Invalid Number of decimal Digits");
	}

	public String getRequesterType()
	{
		return getRequesterType(session);
	}

	public static String getRequesterType(Session session)
	{
		IAgentUser agentUser = session.getAgentUser();
		if (agentUser == null) {
			if (CHANNEL_3PP.equals(session.getChannel())) {
				return Transaction.REQUESTER_TYPE_SERVICE_USER;
			} else {
				return Transaction.REQUESTER_TYPE_WEB_USER;
			}
		} else if (agentUser instanceof Agent)
			return Transaction.REQUESTER_TYPE_AGENT;
		else
			return Transaction.REQUESTER_TYPE_AGENT_USER;
	}

	public void getCachedLocation(EntityManager em, IAgentUser aAgent, boolean force) throws RuleCheckException
	{
		// Get Cached Location regardless
		Cell cell = getCachedCell(aAgent);
		if (cell != null)
		{
			transaction.setA_CellID(cell.getId());
			transaction.setA_Cell(cell);
			return;
		}

		if (!force || aAgent == null || Agent.STATE_PERMANENT.equals(aAgent.getState()))
			return;

		// Get Location
		if (transaction.getA_Cell() == null) {
			// In getALocation we have: transaction.setA_Cell(cell);
			getALocation(em, aAgent, "Forced Location", false);
		}
	}

	public Cell getLocation(EntityManager em, String msisdn, String reason, boolean force, boolean throwOnFailure, boolean cachingEnabled, int cacheExpiryMin) throws RuleCheckException
	{
		Cell cell = null;

		if (cachingEnabled)
		{
			logger.trace("Caching enabled, checking cache for {} [{}]", msisdn, reason);
			// Try to get it from Cache
			cell = getLocationCache().getCell(msisdn);
			if (cell != null)
			{
				logger.trace("Returning cached location for {} [{}]", msisdn, reason);
				return cell;
			}
		}	

		logger.trace("Requesting location for {} [{}]", msisdn, reason);
			
		IHlrInformation location = context.getHlrInformation(msisdn, true, false, false);
		if (!Cell.isEmpty(location))
		{
			logger.trace("{} Located in Cell {},{},{},{}", msisdn, location.getMobileCountryCode(), location.getMobileNetworkCode(), //
					location.getLocationAreaCode(), location.getCellIdentity());
			cell = Cell.find(em, location.getMobileCountryCode(), location.getMobileNetworkCode(), //
					location.getLocationAreaCode(), location.getCellIdentity(), transaction.getCompanyID());

			// Auto Create Cell if it doesn't exist
			if (cell == null)
			{
				cell = new Cell() //
						.setCompanyID(transaction.getCompanyID()) //
						.setMobileCountryCode(location.getMobileCountryCode()) //
						.setMobileNetworkCode(location.getMobileNetworkCode()) //
						.setLocalAreaCode(location.getLocationAreaCode()) //
						.setCellID(location.getCellIdentity());
				AuditEntryContext auditContext = new AuditEntryContext("CELL_AUTO_CREATE", cell.getCellGlobalIdentity());
				cell.persist(em, null, session, auditContext);
			}

			if (cachingEnabled)
			{
				logger.trace("Caching location for {} [{}]", msisdn, reason);
				getLocationCache().put(msisdn, new CachedCell(cell, cacheExpiryMin));
			}	

			return cell;
		}
		else
		{
			logger.trace("No location available for [{}]", msisdn);
			if (cachingEnabled)
			{
				logger.trace("Caching location for {} [{}]", msisdn, reason);
				getLocationCache().put(msisdn, new CachedCell(null, cacheExpiryMin));
			}	
			if (throwOnFailure)
				throw new RuleCheckException(TransactionsConfig.ERR_NO_LOCATION, "CellID", "Location Unavailable");
		}

		return cell;
	}

	public Cell getALocation(EntityManager em, IAgentUser aAgentUser, String reason, boolean throwOnFailure) throws RuleCheckException
	{
		// Try to get it from Cache
		Cell cell = getCachedCell(aAgentUser);
		if (cell != null)
		{
			transaction.setA_CellID(cell.getId());
			transaction.setA_Cell(cell);
			return cell;
		}

		String a_msisdn = aAgentUser.getMobileNumber();
		logger.trace("Requesting location for {} to {}", a_msisdn, reason);

		IHlrInformation location = cannotLocateAgentA ? null : context.getHlrInformation(a_msisdn, true, false, false);
		if (!Cell.isEmpty(location))
		{
			logger.trace("{} Located in Cell {},{},{},{}", a_msisdn, location.getMobileCountryCode(), location.getMobileNetworkCode(), //
					location.getLocationAreaCode(), location.getCellIdentity());
			cell = Cell.find(em, location.getMobileCountryCode(), location.getMobileNetworkCode(), //
					location.getLocationAreaCode(), location.getCellIdentity(), transaction.getCompanyID());

			// Auto Create Cell if it doesn't exist
			if (cell == null)
			{
				cell = new Cell() //
						.setCompanyID(transaction.getCompanyID()) //
						.setMobileCountryCode(location.getMobileCountryCode()) //
						.setMobileNetworkCode(location.getMobileNetworkCode()) //
						.setLocalAreaCode(location.getLocationAreaCode()) //
						.setCellID(location.getCellIdentity());
				AuditEntryContext auditContext = new AuditEntryContext("CELL_AUTO_CREATE", cell.getCellGlobalIdentity());
				cell.persist(em, null, session, auditContext);
			}

			// Store location in transaction
			transaction.setA_CellID(cell.getId());
			transaction.setA_Cell(cell);

			// Cache Location
			aAgentUser.setLastCell(cell);
			aAgentUser.setLastCellID(cell.getId());
			AgentsConfig agentConfig = getConfig(em, AgentsConfig.class);
			Date lastCellExpiryTime = new DateTime(transaction.getStartTime()).addMinutes(agentConfig.getLocationCachingExpiryMinutes());
			aAgentUser.setLastCellExpiryTime(lastCellExpiryTime);
			em.persist(aAgentUser);

			return cell;

		}
		else
		{
			logger.trace("No location available for {}", a_msisdn);
			cannotLocateAgentA = true;
			if (throwOnFailure)
				throw new RuleCheckException(TransactionsConfig.ERR_NO_LOCATION, "a_CellID", "Location Unavailable");
		}

		return cell;
	}

	private Cell getCachedCell(IAgentUser agentUser)
	{
		Cell cell = agentUser.getLastCell();
		Integer cellID = agentUser.getLastCellID();
		Date expires = agentUser.getLastCellExpiryTime();
		if (cellID != null && cell != null && expires != null && transaction.getStartTime().before(expires))
		{
			logger.trace("Using cached location {},{},{},{} for {}", cell.getMobileCountryCode(), cell.getMobileNetworkCode(), //
					cell.getLocalAreaCode(), cell.getCellID(), agentUser.getMobileNumber());
			return cell;
		}

		return null;
	}

	public TransferRule findTransferRule(EntityManager em, BigDecimal amount, Date time, IAgentUser aAgentUser, //
			Agent aAgent, Agent bAgent, int bTierID) throws RuleCheckException
	{
		// Test without location
		List<TransferRule> transferRules = new ArrayList<TransferRule>();
		TransferRule transferRule = findTransferRule(em, //
				transferRules, //
				amount, //
				time, //
				aAgent, //
				bAgent, //
				bTierID, //
				null);

		// Test with location
		if (transferRule == null)
		{
			Cell cell = getALocation(em, aAgentUser, "Evaluate Transfer Rules", true);
			transferRule = findTransferRule(em, //
					transferRules, //
					amount, //
					time, //
					aAgent, //
					bAgent, //
					bTierID, //
					cell);
		}

		return transferRule;
	}

	private TransferRule findTransferRule(EntityManager em, List<TransferRule> transferRules, BigDecimal amount, Date time, Agent aAgent, Agent bAgent, //
			int bTierID, Cell cell) throws RuleCheckException
	{
		boolean tryWithLocation = false;

		if (transferRules.size() == 0)
		{
			transferRules.addAll(TransferRule.findByTierIDs(em, aAgent.getTierID(), bTierID, session.getCompanyID()));
		}
		TransferRule result = null;

		long secsSinceMidnight = TransferRule.secondsSinceMidnight(time, false);
		int dayMask = 1 << TransferRule.dayOfWeek(time);

		LastMismatch lastMismatch = new LastMismatch();
		for (TransferRule rule : transferRules)
		{
			// Test Status
			if (!TransferRule.STATE_ACTIVE.equals(rule.getState()))
			{
				lastMismatch.fail(1, rule, "Active State");
				continue;
			}

			// Test for Min Amounts
			if (rule.getMinimumAmount() != null && rule.getMinimumAmount().compareTo(amount) > 0)
			{
				lastMismatch.fail(2, rule, "Min Amount");
				continue;
			}

			// Test for Max Amount
			if (rule.getMaximumAmount() != null && rule.getMaximumAmount().compareTo(amount) < 0)
			{
				lastMismatch.fail(3, rule, "Max Amount");
				continue;
			}

			// Test day of week
			if (rule.getDaysOfWeek() != null && (rule.getDaysOfWeek() & dayMask) == 0)
			{
				lastMismatch.fail(4, rule, "Day of Week");
				continue;
			}

			// Test Start Time of Day
			if (secsSinceMidnight < TransferRule.secondsSinceMidnight(rule.getStartTimeOfDay(), false))
			{
				lastMismatch.fail(5, rule, "Start Time of Day");
				continue;
			}

			// Test End Time of Day
			if (secsSinceMidnight >= TransferRule.secondsSinceMidnight(rule.getEndTimeOfDay(), true))
			{
				lastMismatch.fail(6, rule, "End Time of Day");
				continue;
			}

			// Test Group
			if (rule.getGroupID() != null && !rule.getGroupID().equals(aAgent.getGroupID()))
			{
				lastMismatch.fail(7, rule, "Group");
				continue;
			}

			// Test Service Class
			if (rule.getServiceClassID() != null && !rule.getServiceClassID().equals(aAgent.getServiceClassID()))
			{
				lastMismatch.fail(8, rule, "Service Class");
				continue;
			}

			// Test Target Group
			if (bAgent != null && rule.getTargetGroupID() != null && !rule.getTargetGroupID().equals(bAgent.getGroupID()))
			{
				lastMismatch.fail(9, rule, "Target Group");
				continue;
			}

			// Test Target Service Class
			if (bAgent != null && rule.getTargetServiceClassID() != null && !rule.getTargetServiceClassID().equals(bAgent.getServiceClassID()))
			{
				lastMismatch.fail(10, rule, "Target Service Class");
				continue;
			}

			// Test Area
			Area area = rule.getArea();
			if (area != null)
			{
				if (cell == null)
				{
					tryWithLocation = true;
					continue;
				}
				else
				{
					if (!cell.containedWithin(area))
					{
						lastMismatch.fail(11, rule, "Area");
						continue;
					}
				}
			}

			// Strict Supplier
			Integer bAgentSupplierID = bAgent == null ? null : bAgent.getSupplierAgentID();
			if (rule.isStrictSupplier() && bAgentSupplierID != null && bAgentSupplierID != aAgent.getId())
			{
				lastMismatch.fail(12, rule, "Supplier");
				continue;
			}

			if (result != null) {
				if (sameGroups(result, rule)) {
					throw new RuleCheckException(StatusCode.AMBIGUOUS, "transferRuleID", "Ambiguous Transfer Rules");
				} else if (isAAgentWithGroupButRuleWithoutSourceGroup(aAgent, rule)
						|| isBAgentWithGroupButRuleWithoutTargetGroup(bAgent, rule)) {
					// In this case, regarding the groups, it is OK to have more than one matching rule.
					// In the query returning rules we have "ORDER BY group NULLS LAST, targetGroup NULLS LAST" which means that
					// already chosen rule should be more specific, so we are just skipping the current (the more general) one.
				} else {
					throw new RuleCheckException(StatusCode.AMBIGUOUS, "transferRuleID", "Ambiguous Transfer Rules");
				}
			} else {
				result = rule;
			}
		}

		if (!tryWithLocation && result == null)
		{
			if (lastMismatch.getLevel() > 0)
				logger.trace("Last failure to find tranferRule on {}/{}", lastMismatch.getRule(), lastMismatch.getTest());
			throw new RuleCheckException(TransactionsConfig.ERR_NO_TRANSFER_RULE, "transferRuleID", "");
		}

		return result;
	}

	private boolean sameGroups(TransferRule alreadyChosenRule, TransferRule currentRule) {
		return equalsInteger(currentRule.getGroupID(), alreadyChosenRule.getGroupID())
				&& equalsInteger(currentRule.getTargetGroupID(), alreadyChosenRule.getTargetGroupID());
	}

	private boolean isAAgentWithGroupButRuleWithoutSourceGroup(Agent aAgent, TransferRule rule) {
		return aAgent.getGroup() != null && rule.getGroup() == null; 
	}

	private boolean isBAgentWithGroupButRuleWithoutTargetGroup(Agent bAgent, TransferRule rule) {
		return bAgent != null && bAgent.getGroup() != null && rule.getTargetGroup() == null;
	}

	public static boolean equalsInteger(Integer id1, Integer id2) {
		if (id1 == null) {
			return id2 == null;
		}
		return id1.equals(id2);
	}

	public void enforceAStrictArea(EntityManager em, IAgentUser aAgentUser, Agent aAgent) throws RuleCheckException
	{
		// Exit if not strict or aAgent not restricted to an area
		TransferRule transferRule = transaction.getTransferRule();
		if (transferRule == null || !transferRule.isStrictArea() || aAgent.getArea() == null)
			return;

		// Get Location
		Cell cell = transaction.getA_Cell();
		if (cell == null)
		{
			AgentsConfig agentsConfig = getConfig(em, AgentsConfig.class);
			cell = getALocation(em, aAgentUser, "Enforce Strict Area", agentsConfig.isFailStrictAreaUponFailureToLocate());
		}

		// Fail if not within Area
		if (cell != null && !cell.containedWithin(aAgent.getArea()))
		{
			throw new RuleCheckException(TransactionsConfig.ERR_WRONG_LOCATION, "a_CellID", //
					"Not allowed to trade in Cell %d,%d,%d,%d", cell.getMobileCountryCode(), cell.getMobileNetworkCode(), //
					cell.getLocalAreaCode(), cell.getCellID());
		}

	}

	public void obtainALocationForPromotions(EntityManager em, IAgentUser user) throws RuleCheckException
	{
		// Don't get it again
		Cell cell = transaction.getA_Cell();
		if (cell != null)
			return;

		// Get Location Specific Active Promotions
		List<hxc.services.ecds.model.Promotion> promotions = hxc.services.ecds.model.Promotion.findAllActiveLocationBased(em, transaction.getCompanyID());

		// Test for consistent Promotions
		if (RewardAssessor.consistentWithAny(promotions, transaction, true))
		{
			RewardsConfig rewardsConfig = getConfig(em, RewardsConfig.class);
			getALocation(em, user, "Evaluate Promotions", rewardsConfig.isFailTransactionsUponFailureToLocate());
		}

	}

	static public int cleanupLocationCache()
	{
		return getLocationCache().clearExpired();
	}
}
