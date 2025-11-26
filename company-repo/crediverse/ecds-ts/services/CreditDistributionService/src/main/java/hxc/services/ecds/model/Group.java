package hxc.services.ecds.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.RestParams;
import hxc.services.ecds.rest.batch.IBatchEnabled;
import hxc.services.ecds.util.QueryBuilder;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;

////////////////////////////////////////////////////////////////////////////////////////
//
// Group Table - Used for Agent Segmentation
//
///////////////////////////////////

@Table(name = "et_group", uniqueConstraints = { //
		@UniqueConstraint(name = "et_group_name", columnNames = { "company_id", "name" }) })
@Entity
@NamedQueries({ //
		@NamedQuery(name = "Group.findByName", query = "SELECT p FROM Group p where name = :name and companyID = :companyID"), //
		@NamedQuery(name = "Group.findByID", query = "SELECT p FROM Group p where id = :id and companyID = :companyID"), //
		@NamedQuery(name = "Group.findByTierID", query = "SELECT p FROM Group p where tier.id = :tierID and companyID = :companyID"), //
		@NamedQuery(name = "Group.referenceTier", query = "SELECT p FROM Group p where tier.id = :tierID"), //
})

public class Group extends hxc.ecds.protocol.rest.Group implements Serializable, ICompanyData<Group>, IAntiLaunder<Group>, IBatchEnabled<Group>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = -3689259540440166627L;

	public static final Permission MAY_ADD = new Permission(false, false, Permission.GROUP_GROUPS, Permission.PERM_ADD, "May Add Groups");
	public static final Permission MAY_UPDATE = new Permission(false, false, Permission.GROUP_GROUPS, Permission.PERM_UPDATE, "May Update Groups");
	public static final Permission MAY_DELETE = new Permission(false, false, Permission.GROUP_GROUPS, Permission.PERM_DELETE, "May Delete Groups");
	public static final Permission MAY_VIEW = new Permission(false, false, Permission.GROUP_GROUPS, Permission.PERM_VIEW, "May View Groups");

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
	protected Tier tier;

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
	public Group setId(int id)
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
	public Group setCompanyID(int companyID)
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
	public Group setName(String name)
	{
		this.name = name;
		return this;
	}

	@Override
	@Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LENGTH)
	public String getDescription()
	{
		return description;
	}

	@Override
	public Group setDescription(String description)
	{
		this.description = description;
		return this;
	}

	@Override
	@Column(name = "state", nullable = false, length = 1)
	public String getState()
	{
		return state;
	}

	@Override
	public Group setState(String state)
	{
		this.state = state;
		return this;
	}

	@Override
	@Column(name = "max_amount", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getMaxTransactionAmount()
	{
		return maxTransactionAmount;
	}

	@Override
	public Group setMaxTransactionAmount(BigDecimal maxTransactionAmount)
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
	public Group setMaxDailyCount(Integer maxDailyCount)
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
	public Group setMaxDailyAmount(BigDecimal maxDailyAmount)
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
	public Group setMaxMonthlyCount(Integer maxMonthlyCount)
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
	public Group setMaxMonthlyAmount(BigDecimal maxMonthlyAmount)
	{
		this.maxMonthlyAmount = maxMonthlyAmount;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public Group setLastUserID(int lastUserID)
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
	public Group setVersion(int version)
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
	public Group setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	@Override
	@Column(name = "tier_id", nullable = false, insertable = false, updatable = false)
	public int getTierID()
	{
		return tierID;
	}

	@Override
	public Group setTierID(int tierID)
	{
		this.tierID = tierID;
		return this;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "tier_id", foreignKey = @ForeignKey(name = "FK_Group_Tier"))
	public Tier getTier()
	{
		return tier;
	}

	public Group setTier(Tier tier)
	{
		this.tier = tier;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public Group()
	{

	}

	public Group(Group group)
	{
		this.lastUserID = group.lastUserID;
		this.lastTime = group.lastTime;
		amend(group);
	}

	public void amend(hxc.ecds.protocol.rest.Group group)
	{
		this.id = group.getId();
		this.companyID = group.getCompanyID();
		this.version = group.getVersion();
		this.name = group.getName();
		this.description = group.getDescription();
		this.tierID = group.getTierID();
		this.maxTransactionAmount = group.getMaxTransactionAmount();
		this.maxDailyCount = group.getMaxDailyCount();
		this.maxDailyAmount = group.getMaxDailyAmount();
		this.maxMonthlyCount = group.getMaxMonthlyCount();
		this.maxMonthlyAmount = group.getMaxMonthlyAmount();
		this.state = group.getState();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Finders
	//
	// /////////////////////////////////

	public static Group findByName(EntityManager em, int companyID, String name)
	{
		TypedQuery<Group> query = em.createNamedQuery("Group.findByName", Group.class);
		query.setParameter("companyID", companyID);
		query.setParameter("name", name);
		List<Group> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<Group> findByTierID(EntityManager em, int companyID, int tierID)
	{
		TypedQuery<Group> query = em.createNamedQuery("Group.findByTierID", Group.class);
		query.setParameter("companyID", companyID);
		query.setParameter("tierID", tierID);
		List<Group> results = query.getResultList();
		return results;
	}

	public static Group findByID(EntityManager em, int id, int companyID)
	{
		TypedQuery<Group> query = em.createNamedQuery("Group.findByID", Group.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<Group> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static boolean referencesTier(EntityManager em, int tierID)
	{
		TypedQuery<Group> query = em.createNamedQuery("Group.referenceTier", Group.class);
		query.setParameter("tierID", tierID);
		query.setMaxResults(1);
		List<Group> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public static List<Group> findAll(EntityManager em, RestParams params, int companyID)
	{
		return QueryBuilder.getQueryResultList(em, Group.class, params, companyID, "name", "description");
	}

	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, Group.class, params, companyID, "name", "description");
		return query.getSingleResult();
	}

	public static void loadMRD(EntityManager em, int companyID, Session session) throws RuleCheckException
	{
		Permission.loadMRD(em, MAY_ADD, session);
		Permission.loadMRD(em, MAY_UPDATE, session);
		Permission.loadMRD(em, MAY_DELETE, session);
		Permission.loadMRD(em, MAY_VIEW, session);
	}

	@Override
	public void persist(EntityManager em, Group existing, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(existing);
		QueryBuilder.persist(em, existing, this, session, AuditEntry.TYPE_GROUP, auditEntryContext);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		QueryBuilder.remove(em, this, session, AuditEntry.TYPE_GROUP, auditEntryContext);
	}

	@Override
	public void validate(Group previous) throws RuleCheckException
	{
		RuleCheck.validate(this);

		if (previous != null)
		{
			RuleCheck.noChange("id", id, previous.id);
			RuleCheck.noChange("companyID", companyID, previous.companyID);
		}

	}

	//Need to put this somewhere common.
	private static <T> List<Order> getSortOrderList(CriteriaBuilder cb, Root<T> root, String sortString)
	{
		List<Order> orderList = new ArrayList<Order>();		
		int index;
		while(sortString.length() > 0)
		{		
			index = sortString.indexOf("+", 0) > 0 ? sortString.indexOf("+", 0) : sortString.indexOf("-", 0);
			int mode = sortString.charAt(index) == '+' ? 1 : -1;			
			String column = sortString.substring(0, index).substring(sortString.indexOf(".") + 1);
			sortString = sortString.substring(index + 1);
			if(mode > 0)
				orderList.add(cb.asc(root.get(column)));
			else 
				orderList.add(cb.desc(root.get(column)));
		}
		return orderList;
	}

}
