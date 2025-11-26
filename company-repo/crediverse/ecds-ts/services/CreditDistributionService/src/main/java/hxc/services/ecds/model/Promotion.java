package hxc.services.ecds.model;

import java.io.Serializable;
import java.math.BigDecimal;
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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
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

////////////////////////////////////////////////////////////////////////////////////////
//
// Promotion Table - Used for reward Transactions
//
///////////////////////////////////

@Table(name = "ep_promo", uniqueConstraints = { //
		@UniqueConstraint(name = "ep_promo_name", columnNames = { "company_id", "name" }) })
@Entity
@NamedQueries({ //
		@NamedQuery(name = "Promotion.findByName", query = "SELECT p FROM Promotion p where name = :name and companyID = :companyID"), //
		@NamedQuery(name = "Promotion.findByID", query = "SELECT p FROM Promotion p where id = :id and companyID = :companyID"), //
		@NamedQuery(name = "Promotion.findAllActive", query = "SELECT p FROM Promotion p where state = '" + hxc.ecds.protocol.rest.Promotion.STATE_ACTIVE + "' and companyID = :companyID"), //
		@NamedQuery(name = "Promotion.findAllLocationBased", query = "SELECT p FROM Promotion p where areaID is not null and state = '" + hxc.ecds.protocol.rest.Promotion.STATE_ACTIVE + "' and companyID = :companyID"), //
		@NamedQuery(name = "Promotion.referenceServiceClass", query = "SELECT p FROM Promotion p where serviceClass.id = :serviceClassID"), //
		@NamedQuery(name = "Promotion.referenceArea", query = "SELECT p FROM Promotion p where area.id = :areaID"), //
		@NamedQuery(name = "Promotion.referenceTransferRule", query = "SELECT p FROM Promotion p where transferRule.id = :transferRuleID"), //
})

public class Promotion extends hxc.ecds.protocol.rest.Promotion implements Serializable, ICompanyData<Promotion>, //
		IBatchEnabled<Promotion>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = -6699638028038893221L;

	public static final Permission MAY_ADD = new Permission(false, false, Permission.GROUP_PROMOTIONS, Permission.PERM_ADD, "May Add Promotions");
	public static final Permission MAY_UPDATE = new Permission(false, false, Permission.GROUP_PROMOTIONS, Permission.PERM_UPDATE, "May Update Promotions");
	public static final Permission MAY_DELETE = new Permission(false, false, Permission.GROUP_PROMOTIONS, Permission.PERM_DELETE, "May Delete Promotions");
	public static final Permission MAY_VIEW = new Permission(false, false, Permission.GROUP_PROMOTIONS, Permission.PERM_VIEW, "May View Promotions");

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
	protected ServiceClass serviceClass;
	@JsonIgnore
	protected Area area;
	@JsonIgnore
	protected Bundle bundle;
	@JsonIgnore
	protected TransferRule transferRule;

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
	public Promotion setId(int id)
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
	public Promotion setCompanyID(int companyID)
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
	public Promotion setName(String name)
	{
		this.name = name;
		return this;
	}

	@Override
	@Column(name = "state", nullable = false, unique = false, length = 1)
	public String getState()
	{
		return state;
	}

	@Override
	public Promotion setState(String state)
	{
		this.state = state;
		return this;
	}

	@Override
	@Column(name = "start_time", nullable = false)
	public Date getStartTime()
	{
		return startTime;
	}

	@Override
	public Promotion setStartTime(Date startTime)
	{
		this.startTime = startTime;
		return this;
	}

	@Override
	@Column(name = "end_time", nullable = false)
	public Date getEndTime()
	{
		return endTime;
	}

	@Override
	public Promotion setEndTime(Date endTime)
	{
		this.endTime = endTime;
		return this;
	}

	@Override
	@Column(name = "rule_id", nullable = true, insertable = false, updatable = false)
	public Integer getTransferRuleID()
	{
		return transferRuleID;
	}

	@Override
	public Promotion setTransferRuleID(Integer transferRuleID)
	{
		this.transferRuleID = transferRuleID;
		return this;
	}

	@Override
	@Column(name = "area_id", nullable = true, insertable = false, updatable = false)
	public Integer getAreaID()
	{
		return areaID;
	}

	@Override
	public Promotion setAreaID(Integer areaID)
	{
		this.areaID = areaID;
		return this;
	}

	@Override
	@Column(name = "sc_id", nullable = true, insertable = false, updatable = false)
	public Integer getServiceClassID()
	{
		return serviceClassID;
	}

	@Override
	public Promotion setServiceClassID(Integer serviceClassID)
	{
		this.serviceClassID = serviceClassID;
		return this;
	}

	@Override
	@Column(name = "bundle_id", nullable = true, insertable = false, updatable = false)
	public Integer getBundleID()
	{
		return bundleID;
	}

	@Override
	public Promotion setBundleID(Integer bundleID)
	{
		this.bundleID = bundleID;
		return this;
	}

	@Override
	@Column(name = "tgt_amount", nullable = false, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getTargetAmount()
	{
		return targetAmount;
	}

	@Override
	public Promotion setTargetAmount(BigDecimal targetAmount)
	{
		this.targetAmount = targetAmount;
		return this;
	}

	@Override
	@Column(name = "tgt_period", nullable = false)
	public int getTargetPeriod()
	{
		return targetPeriod;
	}

	@Override
	public Promotion setTargetPeriod(int targetPeriod)
	{
		this.targetPeriod = targetPeriod;
		return this;
	}

	@Override
	@Column(name = "reward_pct", nullable = false, scale = ICompanyData.FINE_MONEY_SCALE, precision = ICompanyData.FINE_MONEY_PRECISSION)
	public BigDecimal getRewardPercentage()
	{
		return rewardPercentage;
	}

	@Override
	public Promotion setRewardPercentage(BigDecimal rewardPercentage)
	{
		this.rewardPercentage = rewardPercentage;
		return this;
	}

	@Override
	@Column(name = "reward_amt", nullable = false, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getRewardAmount()
	{
		return rewardAmount;
	}

	@Override
	public Promotion setRewardAmount(BigDecimal rewardAmount)
	{
		this.rewardAmount = rewardAmount;
		return this;
	}
	
	@Override
	@Column(name = "retriggers", nullable = false)
	public boolean isRetriggerable()
	{
		return retriggerable;
	}

	@Override
	public Promotion setRetriggerable(boolean retriggerable)
	{
		this.retriggerable = retriggerable;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public Promotion setLastUserID(int lastUserID)
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
	public Promotion setVersion(int version)
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
	public Promotion setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "sc_id", foreignKey = @ForeignKey(name = "FK_Promo_SC"))
	public ServiceClass getServiceClass()
	{
		return serviceClass;
	}

	public void setServiceClass(ServiceClass serviceClass)
	{
		this.serviceClass = serviceClass;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "area_id", foreignKey = @ForeignKey(name = "FK_Promo_Area"))
	public Area getArea()
	{
		return area;
	}

	public void setArea(Area area)
	{
		this.area = area;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "bundle_id", foreignKey = @ForeignKey(name = "FK_Promo_Bundle"))
	public Bundle getBundle()
	{
		return bundle;
	}

	public void setBundle(Bundle bundle)
	{
		this.bundle = bundle;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "rule_id", foreignKey = @ForeignKey(name = "FK_Promo_Rule"))
	public TransferRule getTransferRule()
	{
		return transferRule;
	}

	public void setTransferRule(TransferRule transferRule)
	{
		this.transferRule = transferRule;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public Promotion()
	{

	}

	public Promotion(Promotion promotion)
	{
		this.lastUserID = promotion.lastUserID;
		this.lastTime = promotion.lastTime;
		amend(promotion);
	}

	public void amend(hxc.ecds.protocol.rest.Promotion promotion)
	{
		this.id = promotion.getId();
		this.companyID = promotion.getCompanyID();
		this.version = promotion.getVersion();
		this.name = promotion.getName();
		this.state = promotion.getState();
		this.startTime = promotion.getStartTime();
		this.endTime = promotion.getEndTime();
		this.transferRuleID = promotion.getTransferRuleID();
		this.areaID = promotion.getAreaID();
		this.serviceClassID = promotion.getServiceClassID();
		this.bundleID = promotion.getBundleID();
		this.targetAmount = promotion.getTargetAmount();
		this.targetPeriod = promotion.getTargetPeriod();
		this.rewardPercentage = promotion.getRewardPercentage();
		this.rewardAmount = promotion.getRewardAmount();
		this.retriggerable = promotion.isRetriggerable();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public static Promotion findByName(EntityManager em, int companyID, String name)
	{
		TypedQuery<Promotion> query = em.createNamedQuery("Promotion.findByName", Promotion.class);
		query.setParameter("companyID", companyID);
		query.setParameter("name", name);
		List<Promotion> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static Promotion findByID(EntityManager em, int id, int companyID)
	{
		TypedQuery<Promotion> query = em.createNamedQuery("Promotion.findByID", Promotion.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<Promotion> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<Promotion> findAll(EntityManager em, RestParams params, int companyID)
	{
		return QueryBuilder.getQueryResultList(em, Promotion.class, params, companyID, "name");
	}

	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, Promotion.class, params, companyID, "name");
		return query.getSingleResult();
	}

	public static List<Promotion> findAllActive(EntityManager em, int companyID)
	{
		TypedQuery<Promotion> query = em.createNamedQuery("Promotion.findAllActive", Promotion.class);
		query.setParameter("companyID", companyID);
		return query.getResultList();
	}
	
	public static List<Promotion> findAllActiveLocationBased(EntityManager em, int companyID)
	{
		TypedQuery<Promotion> query = em.createNamedQuery("Promotion.findAllLocationBased", Promotion.class);
		query.setParameter("companyID", companyID);
		return query.getResultList();
	}

	public static boolean referencesServiceClass(EntityManager em, int serviceClassID)
	{
		TypedQuery<Promotion> query = em.createNamedQuery("Promotion.referenceServiceClass", Promotion.class);
		query.setParameter("serviceClassID", serviceClassID);
		query.setMaxResults(1);
		List<Promotion> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesArea(EntityManager em, int areaID)
	{
		TypedQuery<Promotion> query = em.createNamedQuery("Promotion.referenceArea", Promotion.class);
		query.setParameter("areaID", areaID);
		query.setMaxResults(1);
		List<Promotion> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesTransferRule(EntityManager em, int transferRuleID)
	{
		TypedQuery<Promotion> query = em.createNamedQuery("Promotion.referenceTransferRule", Promotion.class);
		query.setParameter("transferRuleID", transferRuleID);
		query.setMaxResults(1);
		List<Promotion> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static void loadMRD(EntityManager em, int companyID, Session session) throws RuleCheckException
	{
		Permission.loadMRD(em, MAY_ADD, session);
		Permission.loadMRD(em, MAY_UPDATE, session);
		Permission.loadMRD(em, MAY_DELETE, session);
		Permission.loadMRD(em, MAY_VIEW, session);
	}

	@Override
	public void persist(EntityManager em, Promotion existing, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(existing);
		QueryBuilder.persist(em, existing, this, session, AuditEntry.TYPE_PROMOTION, auditEntryContext);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		QueryBuilder.remove(em, this, session, AuditEntry.TYPE_PROMOTION, auditEntryContext);
	}

	@Override
	public void validate(Promotion previous) throws RuleCheckException
	{
		RuleCheck.validate(this);

		if (previous != null)
		{
			RuleCheck.noChange("id", id, previous.id);
			RuleCheck.noChange("companyID", companyID, previous.companyID);
			RuleCheck.notLess("startTime", startTime, previous.startTime);
		}
	}

	public boolean overlaps(Promotion that)
	{
		// Not so if same Promotion
		if (that == null || that.getId() == this.getId())
			return false;

		// Not so if either is inactive
		if (!STATE_ACTIVE.equals(this.state) || !STATE_ACTIVE.equals(that.state))
			return false;

		// Not so if dates are not overlapping
		if (that.endTime.before(this.startTime) || that.startTime.after(this.endTime))
			return false;

		// Not so if Transfer Rules are not overlapping
		if (this.transferRuleID != null && that.transferRuleID != null && this.transferRuleID != that.transferRuleID)
			return false;

		// Not so if Service Classes are not overlapping
		if (this.serviceClassID != null && that.serviceClassID != null && this.serviceClassID != that.serviceClassID)
			return false;

		// Not so if Bundles are different
		if (this.bundleID != null && that.bundleID != null && this.bundleID != that.bundleID)
			return false;

		// Not so if Areas are different
		if (this.areaID != null && that.areaID != null)
		{
			if (this.areaID == that.areaID)
				return true;
			if (this.area.getSubAreas().contains(that.area) || //
					that.area.getSubAreas().contains(this.area))
				return true;
			return false;
		}

		return true;
	}



}
