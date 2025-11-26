package hxc.services.ecds.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
// ServiceClass Table - Used for Agent Segmentation
//
///////////////////////////////////

@Table(name = "et_sclass", uniqueConstraints = { //
		@UniqueConstraint(name = "et_sclass_name", columnNames = { "company_id", "name" }) })
@Entity
@NamedQueries({ //
		@NamedQuery(name = "ServiceClass.findByName", query = "SELECT p FROM ServiceClass p where name = :name and companyID = :companyID"), //
		@NamedQuery(name = "ServiceClass.findByID", query = "SELECT p FROM ServiceClass p where id = :id and companyID = :companyID") })

public class ServiceClass extends hxc.ecds.protocol.rest.ServiceClass implements Serializable, ICompanyData<ServiceClass>, //
		IAntiLaunder<ServiceClass>, IBatchEnabled<ServiceClass>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 6399446402028599195L;

	public static final Permission MAY_ADD = new Permission(false, false, Permission.GROUP_SERVICECLASSES, Permission.PERM_ADD, "May Add ServiceClasses");
	public static final Permission MAY_UPDATE = new Permission(false, false, Permission.GROUP_SERVICECLASSES, Permission.PERM_UPDATE, "May Update ServiceClasses");
	public static final Permission MAY_DELETE = new Permission(false, false, Permission.GROUP_SERVICECLASSES, Permission.PERM_DELETE, "May Delete ServiceClasses");
	public static final Permission MAY_VIEW = new Permission(false, false, Permission.GROUP_SERVICECLASSES, Permission.PERM_VIEW, "May View ServiceClasses");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Additional Fields
	//
	// /////////////////////////////////
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
	public int getId()
	{
		return id;
	}

	@Override
	public ServiceClass setId(int id)
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
	public ServiceClass setCompanyID(int companyID)
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
	public ServiceClass setName(String name)
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
	public ServiceClass setDescription(String description)
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
	public ServiceClass setState(String state)
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
	public ServiceClass setMaxTransactionAmount(BigDecimal maxTransactionAmount)
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
	public ServiceClass setMaxDailyCount(Integer maxDailyCount)
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
	public ServiceClass setMaxDailyAmount(BigDecimal maxDailyAmount)
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
	public ServiceClass setMaxMonthlyCount(Integer maxMonthlyCount)
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
	public ServiceClass setMaxMonthlyAmount(BigDecimal maxMonthlyAmount)
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
	public ServiceClass setLastUserID(int lastUserID)
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
	public ServiceClass setVersion(int version)
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
	public ServiceClass setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public ServiceClass()
	{

	}

	public ServiceClass(ServiceClass serviceClass)
	{
		this.lastUserID = serviceClass.lastUserID;
		this.lastTime = serviceClass.lastTime;
		amend(serviceClass);
	}

	public void amend(hxc.ecds.protocol.rest.ServiceClass serviceClass)
	{
		this.id = serviceClass.getId();
		this.companyID = serviceClass.getCompanyID();
		this.version = serviceClass.getVersion();
		this.name = serviceClass.getName();
		this.description = serviceClass.getDescription();
		this.maxTransactionAmount = serviceClass.getMaxTransactionAmount();
		this.maxDailyCount = serviceClass.getMaxDailyCount();
		this.maxDailyAmount = serviceClass.getMaxDailyAmount();
		this.maxMonthlyCount = serviceClass.getMaxMonthlyCount();
		this.maxMonthlyAmount = serviceClass.getMaxMonthlyAmount();
		this.state = serviceClass.getState();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public static ServiceClass findByName(EntityManager em, int companyID, String name)
	{
		TypedQuery<ServiceClass> query = em.createNamedQuery("ServiceClass.findByName", ServiceClass.class);
		query.setParameter("companyID", companyID);
		query.setParameter("name", name);
		List<ServiceClass> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static ServiceClass findByID(EntityManager em, int id, int companyID)
	{
		TypedQuery<ServiceClass> query = em.createNamedQuery("ServiceClass.findByID", ServiceClass.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<ServiceClass> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<ServiceClass> findAll(EntityManager em, RestParams params, int companyID)
	{
		return QueryBuilder.getQueryResultList(em, ServiceClass.class, params, companyID, "name", "description");
	}

	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, ServiceClass.class, params, companyID, "name", "description");
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
	public void persist(EntityManager em, ServiceClass existing, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(existing);
		QueryBuilder.persist(em, existing, this, session, AuditEntry.TYPE_SCLASS, auditEntryContext);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		QueryBuilder.remove(em, this, session, AuditEntry.TYPE_SCLASS, auditEntryContext);
	}

	@Override
	public void validate(ServiceClass previous) throws RuleCheckException
	{
		RuleCheck.validate(this);

		if (previous != null)
		{
			RuleCheck.noChange("id", id, previous.id);
			RuleCheck.noChange("companyID", companyID, previous.companyID);
		}

	}

}
