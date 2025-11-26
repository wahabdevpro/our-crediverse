package hxc.services.ecds.model;

import java.io.Serializable;
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
// Department Table - Used for WebUser Segmentation
//
///////////////////////////////////

@Table(name = "es_dept", uniqueConstraints = { //
		@UniqueConstraint(name = "es_dept_name", columnNames = { "company_id", "name" }) })
@Entity
@NamedQueries({ //
		@NamedQuery(name = "Department.findByName", query = "SELECT p FROM Department p where name = :name and companyID = :companyID"), //
		@NamedQuery(name = "Department.findByID", query = "SELECT p FROM Department p where id = :id and companyID = :companyID") })

public class Department extends hxc.ecds.protocol.rest.Department implements Serializable, ICompanyData<Department>, IBatchEnabled<Department>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 6399446402028599195L;

	public static final Permission MAY_ADD = new Permission(false, false, Permission.GROUP_DEPARTMENTS, Permission.PERM_ADD, "May Add Departments");
	public static final Permission MAY_UPDATE = new Permission(false, false, Permission.GROUP_DEPARTMENTS, Permission.PERM_UPDATE, "May Update Departments");
	public static final Permission MAY_DELETE = new Permission(false, false, Permission.GROUP_DEPARTMENTS, Permission.PERM_DELETE, "May Delete Departments");
	public static final Permission MAY_VIEW = new Permission(false, false, Permission.GROUP_DEPARTMENTS, Permission.PERM_VIEW, "May View Departments");

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
	public Department setId(int id)
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
	public Department setCompanyID(int companyID)
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
	public Department setName(String name)
	{
		this.name = name;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public Department setLastUserID(int lastUserID)
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
	public Department setVersion(int version)
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
	public Department setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public Department()
	{

	}

	public Department(Department department)
	{
		this.lastUserID = department.lastUserID;
		this.lastTime = department.lastTime;
		amend(department);
	}

	public void amend(hxc.ecds.protocol.rest.Department department)
	{
		this.id = department.getId();
		this.companyID = department.getCompanyID();
		this.version = department.getVersion();
		this.name = department.getName();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public static Department findByName(EntityManager em, int companyID, String name)
	{
		TypedQuery<Department> query = em.createNamedQuery("Department.findByName", Department.class);
		query.setParameter("companyID", companyID);
		query.setParameter("name", name);
		List<Department> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static Department findByID(EntityManager em, int id, int companyID)
	{
		TypedQuery<Department> query = em.createNamedQuery("Department.findByID", Department.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<Department> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<Department> findAll(EntityManager em, RestParams params, int companyID)
	{
		return QueryBuilder.getQueryResultList(em, Department.class, params, companyID, "name");
	}

	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, Department.class, params, companyID, "name");
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
	public void persist(EntityManager em, Department existing, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(existing);
		QueryBuilder.persist(em, existing, this, session, AuditEntry.TYPE_DEPT, auditEntryContext);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		QueryBuilder.remove(em, this, session, AuditEntry.TYPE_DEPT, auditEntryContext);
	}

	@Override
	public void validate(Department previous) throws RuleCheckException
	{
		RuleCheck.validate(this);

		if (previous != null)
		{
			RuleCheck.noChange("id", id, previous.id);
			RuleCheck.noChange("companyID", companyID, previous.companyID);
		}

	}

}
