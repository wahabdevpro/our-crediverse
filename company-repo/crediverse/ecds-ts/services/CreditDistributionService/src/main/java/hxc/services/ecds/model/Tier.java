package hxc.services.ecds.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
//import org.codehaus.jackson.annotate.JsonIgnore;

import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.RestParams;
import hxc.services.ecds.rest.batch.IBatchEnabled;
import hxc.services.ecds.util.QueryBuilder;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

////////////////////////////////////////////////////////////////////////////////////////
//
// Tier Table - Used for Security checks
//
///////////////////////////////////

@Table(name = "et_tier", uniqueConstraints = { //
		@UniqueConstraint(name = "et_tier_name", columnNames = { "company_id", "name" }) })
@Entity
@NamedQueries({ //
		@NamedQuery(name = "Tier.findByName", query = "SELECT p FROM Tier p where name = :name and companyID = :companyID"), //
		@NamedQuery(name = "Tier.findByType", query = "SELECT p FROM Tier p where type = :type and companyID = :companyID"), //
		@NamedQuery(name = "Tier.findByID", query = "SELECT p FROM Tier p where id = :id and companyID = :companyID") })

public class Tier extends hxc.ecds.protocol.rest.Tier implements Serializable, ICompanyData<Tier>, IAntiLaunder<Tier>, IBatchEnabled<Tier>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	private static final long serialVersionUID = 7461809680309401927L;

	public static final String TIER_ROOT_NAME = "Root";
	public static final String TIER_SUBSCRIBER_NAME = "Subscriber";

	public static final Permission MAY_ADD = new Permission(false, false, Permission.GROUP_TIERS, Permission.PERM_ADD, "May Add Tiers");
	public static final Permission MAY_UPDATE = new Permission(false, false, Permission.GROUP_TIERS, Permission.PERM_UPDATE, "May Update Tiers");
	public static final Permission MAY_DELETE = new Permission(false, false, Permission.GROUP_TIERS, Permission.PERM_DELETE, "May Delete Tiers");
	public static final Permission MAY_VIEW = new Permission(false, false, Permission.GROUP_TIERS, Permission.PERM_VIEW, "May View Tiers");

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
	protected Set<Group> groups;

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
	public Tier setId(int id)
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
	public Tier setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Override
	@Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
	public String getName()
	{
		return name;
	}

	@Override
	public Tier setName(String name)
	{
		this.name = name;
		return this;
	}

	@Override
	@Column(name = "type", nullable = false, length = TYPE_MAX_LENGTH)
	public String getType()
	{
		return type;
	}

	@Override
	public Tier setType(String type)
	{
		this.type = type;
		return this;
	}

	@Override
	@Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LENGTH)
	public String getDescription()
	{
		return description;
	}

	@Override
	public Tier setDescription(String description)
	{
		this.description = description;
		return this;
	}

	@Override
	@Column(name = "permanent", nullable = false)
	public boolean isPermanent()
	{
		return permanent;
	}

	@Override
	public Tier setPermanent(boolean permanent)
	{
		this.permanent = permanent;
		return this;
	}

	@Override
	@Column(name = "max_amount", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getMaxTransactionAmount()
	{
		return maxTransactionAmount;
	}

	@Override
	public Tier setMaxTransactionAmount(BigDecimal maxTransactionAmount)
	{
		this.maxTransactionAmount = maxTransactionAmount;
		return this;
	}

	@Override
	@Column(name = "max_daily_count", nullable = true)
	public Integer getMaxDailyCount()
	{
		return maxDailyCount;
	}

	@Override
	public Tier setMaxDailyCount(Integer maxDailyCount)
	{
		this.maxDailyCount = maxDailyCount;
		return this;
	}

	@Override
	@Column(name = "max_daily_amount", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getMaxDailyAmount()
	{
		return maxDailyAmount;
	}

	@Override
	public Tier setMaxDailyAmount(BigDecimal maxDailyAmount)
	{
		this.maxDailyAmount = maxDailyAmount;
		return this;
	}

	@Override
	@Column(name = "max_monthly_count", nullable = true)
	public Integer getMaxMonthlyCount()
	{
		return maxMonthlyCount;
	}

	@Override
	public Tier setMaxMonthlyCount(Integer maxMonthlyCount)
	{
		this.maxMonthlyCount = maxMonthlyCount;
		return this;
	}

	@Override
	@Column(name = "max_monthly_amount", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getMaxMonthlyAmount()
	{
		return maxMonthlyAmount;
	}

	@Override
	public Tier setMaxMonthlyAmount(BigDecimal maxMonthlyAmount)
	{
		this.maxMonthlyAmount = maxMonthlyAmount;
		return this;
	}

	@Override
	@Column(name = "state", nullable = false, unique = false, length = 1)
	public String getState()
	{
		return state;
	}

	@Override
	public Tier setState(String state)
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
	@Column(name = "down_pct", nullable = true, scale = ICompanyData.FINE_MONEY_SCALE, precision = ICompanyData.FINE_MONEY_PRECISSION)
	public BigDecimal getDownStreamPercentage()
	{
		return downStreamPercentage;
	}

	@Override
	public Tier setDownStreamPercentage(BigDecimal downStreamPercentage)
	{
		this.downStreamPercentage = downStreamPercentage;
		return this;
	}

	@Override
	public Tier setLastUserID(int lastUserID)
	{
		this.lastUserID = lastUserID;
		return this;
	}

	@Override
	@Version
	public int getVersion()
	{
		return version;
	}

	@Override
	public Tier setVersion(int version)
	{
		this.version = version;
		return this;
	}

	@Override
	@Column(name = "lm_time", nullable = false)
	public Date getLastTime()
	{
		return lastTime;
	}

	@Override
	public Tier setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "tier_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public Set<Group> getGroups()
	{
		return this.groups;
	}

	public Tier setGroups(Set<Group> groups)
	{
		this.groups = groups;
		return this;
	}

	@Override
	@Column(name = "allow_intratier_transfer", nullable = false)
	public boolean isAllowIntraTierTransfer()
	{
		return allowIntraTierTransfer;
	}

	@Override
	public Tier setAllowIntraTierTransfer(boolean allowIntraTierTransfer)
	{
		this.allowIntraTierTransfer = allowIntraTierTransfer;
		return this;
	}
	
	@Override
	@Column(name = "default_bonus_pct", nullable = true, scale = ICompanyData.FINE_MONEY_SCALE, precision = ICompanyData.FINE_MONEY_PRECISSION)
	public BigDecimal getBuyerDefaultTradeBonusPercentage()
	{
		return buyerDefaultTradeBonusPercentage;
	}

	@Override
	public Tier setBuyerDefaultTradeBonusPercentage(BigDecimal buyerDefaultTradeBonusPercentage)
	{
		this.buyerDefaultTradeBonusPercentage = buyerDefaultTradeBonusPercentage;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public Tier()
	{

	}

	public Tier(Tier tier)
	{
		this.lastUserID = tier.lastUserID;
		this.lastTime = tier.lastTime;
		amend(tier);
	}

	public void amend(hxc.ecds.protocol.rest.Tier tier)
	{
		this.id = tier.getId();
		this.companyID = tier.getCompanyID();
		this.version = tier.getVersion();
		this.name = tier.getName();
		this.type = tier.getType();
		this.description = tier.getDescription();
		this.permanent = tier.isPermanent();
		this.maxTransactionAmount = tier.getMaxTransactionAmount();
		this.maxDailyCount = tier.getMaxDailyCount();
		this.maxDailyAmount = tier.getMaxDailyAmount();
		this.maxMonthlyCount = tier.getMaxMonthlyCount();
		this.maxMonthlyAmount = tier.getMaxMonthlyAmount();
		this.state = tier.getState();
		this.allowIntraTierTransfer = tier.isAllowIntraTierTransfer();
		this.buyerDefaultTradeBonusPercentage = tier.getBuyerDefaultTradeBonusPercentage();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Finders
	//
	// /////////////////////////////////

	public static Tier findByName(EntityManager em, int companyID, String name)
	{
		TypedQuery<Tier> query = em.createNamedQuery("Tier.findByName", Tier.class);
		query.setParameter("companyID", companyID);
		query.setParameter("name", name);
		List<Tier> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<Tier> findByType(EntityManager em, int companyID, String type)
	{
		TypedQuery<Tier> query = em.createNamedQuery("Tier.findByType", Tier.class);
		query.setParameter("companyID", companyID);
		query.setParameter("type", type);
		return query.getResultList();
	}

	public static Tier findByID(EntityManager em, int id, int companyID)
	{
		TypedQuery<Tier> query = em.createNamedQuery("Tier.findByID", Tier.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<Tier> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<Tier> findAll(EntityManager em, RestParams params, int companyID)
	{
		return QueryBuilder.getQueryResultList(em, Tier.class, params, companyID, "name", "description");
	}

	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, Tier.class, params, companyID, "name", "description");
		return query.getSingleResult();
	}

	public static Tier findRoot(EntityManager em, int companyID)
	{
		return findByName(em, companyID, TIER_ROOT_NAME);
	}

	public static Tier findSubscriber(EntityManager em, int companyID)
	{
		return findByName(em, companyID, TIER_SUBSCRIBER_NAME);
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

		Tier.loadMRD(em, session, companyID, TIER_ROOT_NAME, TYPE_ROOT);
		Tier.loadMRD(em, session, companyID, TIER_SUBSCRIBER_NAME, TYPE_SUBSCRIBER);
	}

	private static void loadMRD(EntityManager em, Session session, int companyID, String name, String type) throws RuleCheckException
	{
		Tier tier = findByName(em, companyID, name);
		if (tier == null)
		{
			tier = new Tier() //
					.setName(name) //
					.setDescription(String.format("Built-in %s Tier", name)) //
					.setCompanyID(companyID) //
					.setType(type) //
					.setPermanent(true)
					.setAllowIntraTierTransfer(false);
			AuditEntryContext auditContext = new AuditEntryContext("LOADED_MRD_TIER", tier.getName());
			tier.persist(em, null, session, auditContext);
		}
	}

	@Override
	public void persist(EntityManager em, Tier existing, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(existing);
		QueryBuilder.persist(em, existing, this, session, AuditEntry.TYPE_TIER, auditEntryContext);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		RuleCheck.isFalse(null, permanent, "Cannot delete Permanent Tier");
		QueryBuilder.remove(em, this, session, AuditEntry.TYPE_TIER, auditEntryContext);
	}

	@Override
	public void validate(Tier previous) throws RuleCheckException
	{
		RuleCheck.validate(this);

		boolean isPermanentType = TYPE_ROOT.equals(type) || TYPE_SUBSCRIBER.equals(type);

		if (isPermanentType && !permanent)
			throw new RuleCheckException(StatusCode.CANT_SET_AS_PERMANENT, "type", "May not set Permanent Type");
		else if (!isPermanentType && permanent)
			throw new RuleCheckException(StatusCode.CANT_SET_AS_PERMANENT, "type", "May not set Permanent Type");

		if (previous != null)
		{
			RuleCheck.noChange("id", id, previous.id);
			RuleCheck.noChange("companyID", companyID, previous.companyID);
			RuleCheck.noChange("permanent", permanent, previous.permanent);

			RuleCheck.noChange("name", name, previous.name, previous.permanent);
			RuleCheck.noChange("type", type, previous.type, previous.permanent);

			RuleCheck.notLess("downStreamPercentage", downStreamPercentage, BigDecimal.ZERO);
			RuleCheck.notMore("downStreamPercentage", downStreamPercentage, BigDecimal.ONE);

			if (!type.equals(previous.getType()))
				RuleCheck.oneOf("type", type, TYPE_STORE, TYPE_WHOLESALER, TYPE_RETAILER);
		}

	}

}
