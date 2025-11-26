package hxc.services.ecds.model;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.RestParams;
import hxc.services.ecds.rest.batch.IBatchEnabled;
import hxc.services.ecds.util.QueryBuilder;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

// //////////////////////////////////////////////////////////////////////////////////////
//
// Transfer Rule Table 
//
// /////////////////////////////////

@Table(name = "et_rule", uniqueConstraints = { //
		@UniqueConstraint(name = "et_rule_name", columnNames = { "company_id", "name" }) })
@Entity
@NamedQueries({ //
		@NamedQuery(name = "TransferRule.findByName", query = "SELECT p FROM TransferRule p where name = :name and companyID = :companyID"), //
		@NamedQuery(name = "TransferRule.findByID", query = "SELECT p FROM TransferRule p where id = :id and companyID = :companyID"), //
		@NamedQuery(name = "TransferRule.findSourceTierID", query = "SELECT p FROM TransferRule p where sourceTierID = :sourceTierID and companyID = :companyID order by targetTierID"), //

		@NamedQuery(name = "TransferRule.findTierIDs",
				query = "SELECT p FROM TransferRule p" +
						" where sourceTierID = :sourceTierID and targetTierID = :targetTierID and companyID = :companyID" +
						" ORDER BY group NULLS LAST, targetGroup NULLS LAST"),

		@NamedQuery(name = "TransferRule.referenceServiceClass", query = "SELECT p FROM TransferRule p where serviceClass.id = :serviceClassID or targetServiceClass.id = :serviceClassID"), //
		@NamedQuery(name = "TransferRule.referenceGroup", query = "SELECT p FROM TransferRule p where group.id = :groupID or targetGroup.id = :groupID"), //
		@NamedQuery(name = "TransferRule.referenceTier", query = "SELECT p FROM TransferRule p where sourceTier.id = :tierID or targetTier.id = :tierID") //
})

public class TransferRule extends hxc.ecds.protocol.rest.TransferRule //
		implements ICompanyData<TransferRule>, IBatchEnabled<TransferRule>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	@Transient
	public static final Permission MAY_ADD = new Permission(false, false, Permission.GROUP_TRANSFERRULES, Permission.PERM_ADD, "May Add Transfer Rules");
	@Transient
	public static final Permission MAY_UPDATE = new Permission(false, false, Permission.GROUP_TRANSFERRULES, Permission.PERM_UPDATE, "May Update Transfer Rules");
	@Transient
	public static final Permission MAY_DELETE = new Permission(false, false, Permission.GROUP_TRANSFERRULES, Permission.PERM_DELETE, "May Delete Transfer Rules");
	@Transient
	public static final Permission MAY_VIEW = new Permission(false, false, Permission.GROUP_TRANSFERRULES, Permission.PERM_VIEW, "May View Transfer Rules");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Additional Fields
	//
	// /////////////////////////////////
	@JsonIgnore
	protected int lastUserID;
	@JsonIgnore
	protected Date lastTime;
	@JsonIgnore
	protected Tier sourceTier;
	@JsonIgnore
	protected Tier targetTier;
	@JsonIgnore
	protected Group group;
	@JsonIgnore
	protected ServiceClass serviceClass;
	@JsonIgnore
	protected Group targetGroup;
	@JsonIgnore
	protected ServiceClass targetServiceClass;
	@JsonIgnore
	protected Area area;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getId()
	{
		return id;
	}

	@Override
	public TransferRule setId(int id)
	{
		this.id = id;
		return this;
	}

	@Override
	@Column(name = "company_id", nullable = false)
	public int getCompanyID()
	{
		return companyID;
	}

	@Override
	public TransferRule setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Override
	@Version
	public int getVersion()
	{
		return version;
	}

	@Override
	public TransferRule setVersion(int version)
	{
		this.version = version;
		return this;
	}

	@Override
	@Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
	public String getName()
	{
		return name;
	}

	@Override
	public TransferRule setName(String name)
	{
		this.name = name;
		return this;
	}

	@Override
	@Column(name = "s_tier_id", nullable = false, insertable = false, updatable = false)
	public int getSourceTierID()
	{
		return sourceTierID;
	}

	@Override
	public TransferRule setSourceTierID(int sourceTierID)
	{
		this.sourceTierID = sourceTierID;
		return this;
	}

	@Override
	@Column(name = "t_tier_id", nullable = false, insertable = false, updatable = false)
	public int getTargetTierID()
	{
		return targetTierID;
	}

	@Override
	public TransferRule setTargetTierID(int targetTierID)
	{
		this.targetTierID = targetTierID;
		return this;
	}
	
	@Override
	@Column(name = "t_bonus_pct", nullable = true, scale = ICompanyData.FINE_MONEY_SCALE, precision = ICompanyData.FINE_MONEY_PRECISSION)
	public BigDecimal getTargetBonusPercentage()
	{
		return targetBonusPercentage;
	}

	@Override
	public TransferRule setTargetBonusPercentage(BigDecimal targetBonusPercentage)
	{
		this.targetBonusPercentage = targetBonusPercentage;
		return this;
	}
	
	@Override
	@Column(name = "t_bonus_profile", nullable = true, length = BONUS_PROFILE_MAX_LENGTH)
	public String getTargetBonusProfile()
	{
		return targetBonusProfile;
	}

	@Override
	public TransferRule setTargetBonusProfile(String targetBonusProfile)
	{
		this.targetBonusProfile = targetBonusProfile;
		return this;
	}

	@Override
	@Column(name = "bonus_pct", nullable = false, scale = ICompanyData.FINE_MONEY_SCALE, precision = ICompanyData.FINE_MONEY_PRECISSION)
	public BigDecimal getBuyerTradeBonusPercentage()
	{
		return buyerTradeBonusPercentage;
	}

	@Override
	public TransferRule setBuyerTradeBonusPercentage(BigDecimal buyerTradeBonusPercentage)
	{
		this.buyerTradeBonusPercentage = buyerTradeBonusPercentage;
		return this;
	}

	@Override
	@Column(name = "area_id", nullable = true, insertable = false, updatable = false)
	public Integer getAreaID()
	{
		return areaID;
	}

	@Override
	public TransferRule setAreaID(Integer areaID)
	{
		this.areaID = areaID;
		return this;
	}

	@Override
	@Column(name = "group_id", nullable = true, insertable = false, updatable = false)
	public Integer getGroupID()
	{
		return groupID;
	}

	@Override
	public TransferRule setGroupID(Integer groupID)
	{
		this.groupID = groupID;
		return this;
	}

	@Override
	@Column(name = "sc_id", nullable = true, insertable = false, updatable = false)
	public Integer getServiceClassID()
	{
		return serviceClassID;
	}

	@Override
	public TransferRule setServiceClassID(Integer serviceClassID)
	{
		this.serviceClassID = serviceClassID;
		return this;
	}

	@Override
	@Column(name = "t_group_id", nullable = true, insertable = false, updatable = false)
	public Integer getTargetGroupID()
	{
		return targetGroupID;
	}

	@Override
	public TransferRule setTargetGroupID(Integer targetGroupID)
	{
		this.targetGroupID = targetGroupID;
		return this;
	}

	@Override
	@Column(name = "t_sc_id", nullable = true, insertable = false, updatable = false)
	public Integer getTargetServiceClassID()
	{
		return targetServiceClassID;
	}

	@Override
	public TransferRule setTargetServiceClassID(Integer targetServiceClassID)
	{
		this.targetServiceClassID = targetServiceClassID;
		return this;
	}

	@Override
	@Column(name = "min_amount", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getMinimumAmount()
	{
		return minimumAmount;
	}

	@Override
	public TransferRule setMinimumAmount(BigDecimal minimumAmount)
	{
		this.minimumAmount = minimumAmount;
		return this;
	}

	@Override
	@Column(name = "max_amount", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getMaximumAmount()
	{
		return maximumAmount;
	}

	@Override
	public TransferRule setMaximumAmount(BigDecimal maximumAmount)
	{
		this.maximumAmount = maximumAmount;
		return this;
	}

	@Override
	@Column(name = "dow", nullable = true)
	public Integer getDaysOfWeek()
	{
		return daysOfWeek;
	}

	@Override
	public TransferRule setDaysOfWeek(Integer daysOfWeek)
	{
		this.daysOfWeek = daysOfWeek;
		return this;
	}

	@Override
	@Column(name = "start_tod", nullable = true)
	public Date getStartTimeOfDay()
	{
		return startTimeOfDay;
	}

	@Override
	public TransferRule setStartTimeOfDay(Date startTimeOfDay)
	{
		this.startTimeOfDay = startTimeOfDay;
		return this;
	}

	@Override
	@Column(name = "end_tod", nullable = true)
	public Date getEndTimeOfDay()
	{
		return endTimeOfDay;
	}

	@Override
	public TransferRule setEndTimeOfDay(Date endTimeOfDay)
	{
		this.endTimeOfDay = endTimeOfDay;
		return this;
	}

	@Override
	@Column(name = "strict_supplier", nullable = false)
	public boolean isStrictSupplier()
	{
		return strictSupplier;
	}

	@Override
	public TransferRule setStrictSupplier(boolean strictSupplier)
	{
		this.strictSupplier = strictSupplier;
		return this;
	}

	@Override
	@Column(name = "strict_area", nullable = false)
	public boolean isStrictArea()
	{
		return strictArea;
	}

	@Override
	public TransferRule setStrictArea(boolean strictArea)
	{
		this.strictArea = strictArea;
		return this;
	}

	@Override
	@Column(name = "state", nullable = false, unique = false, length = 1)
	public String getState()
	{
		return state;
	}

	@Override
	public TransferRule setState(String state)
	{
		this.state = state;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public TransferRule setLastUserID(int lastUserID)
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
	public TransferRule setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "s_tier_id", foreignKey = @ForeignKey(name = "FK_Rule_Source"))
	public Tier getSourceTier()
	{
		return sourceTier;
	}

	public TransferRule setSourceTier(Tier sourceTier)
	{
		this.sourceTier = sourceTier;
		return this;
	}

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "t_tier_id", foreignKey = @ForeignKey(name = "FK_Rule_Target"))
	public Tier getTargetTier()
	{
		return targetTier;
	}

	public TransferRule setTargetTier(Tier targetTier)
	{
		this.targetTier = targetTier;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id", foreignKey = @ForeignKey(name = "FK_Rule_Group"))
	public Group getGroup()
	{
		return group;
	}

	public TransferRule setGroup(Group group)
	{
		this.group = group;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "sc_id", foreignKey = @ForeignKey(name = "FK_Rule_SClass"))
	public ServiceClass getServiceClass()
	{
		return serviceClass;
	}

	public TransferRule setServiceClass(ServiceClass serviceClass)
	{
		this.serviceClass = serviceClass;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "t_group_id", foreignKey = @ForeignKey(name = "FK_Rule_TGroup"))
	public Group getTargetGroup()
	{
		return targetGroup;
	}

	public TransferRule setTargetGroup(Group targetGroup)
	{
		this.targetGroup = targetGroup;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "t_sc_id", foreignKey = @ForeignKey(name = "FK_Rule_TSClass"))
	public ServiceClass getTargetServiceClass()
	{
		return targetServiceClass;
	}

	public TransferRule setTargetServiceClass(ServiceClass targetServiceClass)
	{
		this.targetServiceClass = targetServiceClass;
		return this;
	}
	
	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "area_id", foreignKey = @ForeignKey(name = "FK_Rule_Area")) 
	public Area getArea()
	{
		return area;
	}

	public TransferRule setArea(Area area)
	{
		this.area = area;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public TransferRule()
	{

	}

	public TransferRule(TransferRule transferRule)
	{
		this.lastUserID = transferRule.lastUserID;
		this.lastTime = transferRule.lastTime;
		amend(transferRule);
	}

	public void amend(hxc.ecds.protocol.rest.TransferRule transferRule)
	{
		this.id = transferRule.getId();
		this.companyID = transferRule.getCompanyID();
		this.version = transferRule.getVersion();
		this.name = transferRule.getName();
		this.sourceTierID = transferRule.getSourceTierID();
		this.targetTierID = transferRule.getTargetTierID();
		this.buyerTradeBonusPercentage = transferRule.getBuyerTradeBonusPercentage();
		this.targetBonusPercentage = transferRule.getTargetBonusPercentage();
		this.targetBonusProfile = transferRule.getTargetBonusProfile();
		this.areaID = transferRule.getAreaID();
		this.groupID = transferRule.getGroupID();
		this.serviceClassID = transferRule.getServiceClassID();
		this.targetGroupID = transferRule.getTargetGroupID();
		this.targetServiceClassID = transferRule.getTargetServiceClassID();
		this.minimumAmount = transferRule.getMinimumAmount();
		this.maximumAmount = transferRule.getMaximumAmount();
		this.daysOfWeek = transferRule.getDaysOfWeek();
		this.startTimeOfDay = transferRule.getStartTimeOfDay();
		this.endTimeOfDay = transferRule.getEndTimeOfDay();
		this.strictSupplier = transferRule.isStrictSupplier();
		this.strictArea = transferRule.isStrictArea();
		this.state = transferRule.getState();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// MRD
	//
	// /////////////////////////////////
	public static void loadMRD(EntityManager em, int companyID, Session session) throws RuleCheckException
	{
		Permission.loadMRD(em, MAY_ADD, session);
		Permission.loadMRD(em, MAY_UPDATE, session);
		Permission.loadMRD(em, MAY_DELETE, session);
		Permission.loadMRD(em, MAY_VIEW, session);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Finders
	//
	// /////////////////////////////////

	public static TransferRule findByName(EntityManager em, int companyID, String name)
	{
		TypedQuery<TransferRule> query = em.createNamedQuery("TransferRule.findByName", TransferRule.class);
		query.setParameter("companyID", companyID);
		query.setParameter("name", name);
		List<TransferRule> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static TransferRule findByID(EntityManager em, int id, int companyID)
	{
		TypedQuery<TransferRule> query = em.createNamedQuery("TransferRule.findByID", TransferRule.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<TransferRule> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<TransferRule> findByTierIDs(EntityManager em, int sourceTierID, int targetTierID, int companyID)
	{
		TypedQuery<TransferRule> query = em.createNamedQuery("TransferRule.findTierIDs", TransferRule.class);
		query.setParameter("sourceTierID", sourceTierID);
		query.setParameter("targetTierID", targetTierID);
		query.setParameter("companyID", companyID);
		return query.getResultList();
	}

	public static List<TransferRule> findAll(EntityManager em, RestParams params, int companyID)
	{
		return QueryBuilder.getQueryResultList(em, TransferRule.class, params, companyID, "name");
	}

	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, TransferRule.class, params, companyID, "name");
		return query.getSingleResult();
	}

	public static boolean referencesServiceClass(EntityManager em, int serviceClassID)
	{
		TypedQuery<TransferRule> query = em.createNamedQuery("TransferRule.referenceServiceClass", TransferRule.class);
		query.setParameter("serviceClassID", serviceClassID);
		query.setMaxResults(1);
		List<TransferRule> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesGroup(EntityManager em, int groupID)
	{
		TypedQuery<TransferRule> query = em.createNamedQuery("TransferRule.referenceGroup", TransferRule.class);
		query.setParameter("groupID", groupID);
		query.setMaxResults(1);
		List<TransferRule> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesTier(EntityManager em, int tierID)
	{
		TypedQuery<TransferRule> query = em.createNamedQuery("TransferRule.referenceTier", TransferRule.class);
		query.setParameter("tierID", tierID);
		query.setMaxResults(1);
		List<TransferRule> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ICompanyData
	//
	// /////////////////////////////////

	@Override
	public void persist(EntityManager em, TransferRule previous, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(previous);
		QueryBuilder.persist(em, previous, this, session, AuditEntry.TYPE_TRANSFER_RULE, auditEntryContext);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		QueryBuilder.remove(em, this, session, AuditEntry.TYPE_TRANSFER_RULE, auditEntryContext);
	}

	@Override
	public void validate(TransferRule previous) throws RuleCheckException
	{
		RuleCheck.validate(this);

		// Source Tier must exist
		RuleCheck.notNull("sourceTier", sourceTier);
		RuleCheck.equals("sourceTierID", sourceTier.getId(), sourceTierID);
		sourceTier.validate(null);

		// Target Tier must exist
		RuleCheck.notNull("targetTier", targetTier);
		RuleCheck.equals("targetTierID", targetTier.getId(), targetTierID);
		targetTier.validate(null);

		// Cannot transfer to Root
		String from = sourceTier.getType();
		String to = targetTier.getType();
		if (to.equals(Tier.TYPE_ROOT))
			throw new RuleCheckException(StatusCode.INVALID_TO_ROOT, "targetTier.type", "Cannot transfer to Root Tier");

		// Cannot transfer from Subscriber
		if (from.equals(Tier.TYPE_SUBSCRIBER))
			throw new RuleCheckException(StatusCode.INVALID_FROM_SUBSCRIBER, "sourceTier.type", "Cannot transfer from Subscriber Tier");

		// Wholesalers cannot transfer to Store
		if (from.equals(Tier.TYPE_WHOLESALER) && to.equals(Tier.TYPE_STORE))
			throw new RuleCheckException(StatusCode.INVALID_TIER_TO_STORE, "sourceTier.type", "Wholesalers cannot transfer to strores");

		// Retailers cannot transfer to wholesaler
		if (from.equals(Tier.TYPE_RETAILER) && to.equals(Tier.TYPE_WHOLESALER))
			throw new RuleCheckException(StatusCode.INVALID_TIER_TO_WHOLESALER, "sourceTier.type", "Retailers cannot transfer to wholesalers");

		// Retailers cannot transfer to Store
		if (from.equals(Tier.TYPE_RETAILER) && to.equals(Tier.TYPE_STORE))
			throw new RuleCheckException(StatusCode.INVALID_TIER_TO_STORE, "sourceTier.type", "Retailers cannot transfer to strores");

		// Only a Retailer can sell to and only to a Subscriber
		if (!from.equals(Tier.TYPE_RETAILER) && to.equals(Tier.TYPE_SUBSCRIBER))
			throw new RuleCheckException(StatusCode.INVALID_TIER_TO_SUBSCRIBER, "sourceTier.type", "Only a Retailer can sell to a subscriber");

		// Only Some tiers may receive a bonus
		if (buyerTradeBonusPercentage != null && buyerTradeBonusPercentage.signum() != 0 && !mayReceiveBuyerTradeBonus(targetTier))
			throw new RuleCheckException(StatusCode.CANNOT_HAVE_VALUE, "buyerTradeBonusPercentage", "Subscribers Cannot Receive Bonus");

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public boolean overlapsWith(TransferRule other)
	{
		if (other == null)
			return false;

		// Same ID
		if (id == other.getId())
			return false;

		// Source Tier
		if (sourceTierID != other.getSourceTierID())
			return false;

		// Target Tier
		if (targetTierID != other.getTargetTierID())
			return false;

		// Disabled
		if (TransferRule.STATE_INACTIVE.equals(getState()) || TransferRule.STATE_INACTIVE.equals(other.getState()))
			return false;

		// Time of Day
		long start = secondsSinceMidnight(startTimeOfDay, false);
		long end = secondsSinceMidnight(endTimeOfDay, true);
		long otherStart = secondsSinceMidnight(other.getStartTimeOfDay(), false);
		long otherEnd = secondsSinceMidnight(other.getEndTimeOfDay(), true);
		if (end < otherStart)
			return false;
		if (otherEnd < start)
			return false;

		// Min/Max Amounts
		BigDecimal min = isNull(minimumAmount, 0);
		BigDecimal max = isNull(maximumAmount, Integer.MAX_VALUE);
		BigDecimal otherMin = isNull(other.getMinimumAmount(), 0);
		BigDecimal otherMax = isNull(other.getMaximumAmount(), Integer.MAX_VALUE);
		if (max.compareTo(otherMin) < 0)
			return false;
		if (otherMax.compareTo(min) < 0)
			return false;

		// Day of Week
		int days = daysOfWeek == null ? DOW_ALL : daysOfWeek;
		int otherDays = other.getDaysOfWeek() == null ? DOW_ALL : other.getDaysOfWeek();
		if ((days & otherDays) == 0)
			return false;

		// Groups
		if (differentGroup(groupID, other.getGroupID()))
			return false;

		if (differentGroup(targetGroupID, other.getTargetGroupID()))
			return false;

		// Service Classes
		if (different(serviceClassID, other.getServiceClassID()))
			return false;

		if (different(targetServiceClassID, other.getTargetServiceClassID()))
			return false;

		// TODO Areas

		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	private BigDecimal isNull(BigDecimal value, int defaultValue)
	{
		return value == null ? new BigDecimal(defaultValue) : value;
	}

	public static long secondsSinceMidnight(Date time, boolean end)
	{
		if (time == null)
			return end ? 86400 : 0;
		Calendar c = Calendar.getInstance();
		c.setTime(time);
		long now = time.getTime();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long passed = now - c.getTimeInMillis();
		return passed / 1000;
	}

	public static int dayOfWeek(Date time)
	{
		if (time == null)
			return 7;
		Calendar c = Calendar.getInstance();
		c.setTime(time);
		return c.get(Calendar.DAY_OF_WEEK) - 1;
	}

	private boolean different(Integer id1, Integer id2)
	{
		if (id1 == null || id2 == null)
			return false;
		return !id1.equals(id2);
	}

	private boolean differentGroup(Integer id1, Integer id2) {
		if (id1 == null) {
			return id2 != null;
		}
		return !id1.equals(id2);
	}

	@Override
	public String toString()
	{
		return String.format("%s-%s", sourceTier == null ? "?" : sourceTier.getName(), targetTier == null ? "?" : targetTier.getName());
	}

}
