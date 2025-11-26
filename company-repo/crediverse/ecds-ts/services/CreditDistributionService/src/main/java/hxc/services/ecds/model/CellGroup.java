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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
// CellGroup Table - Used for WebUser Segmentation
//
///////////////////////////////////

@Table(name = "el_cell_group", uniqueConstraints = { //
		@UniqueConstraint(name = "el_cell_group_name", columnNames = { "company_id", "name" }), 
		@UniqueConstraint(name = "el_cell_group_code", columnNames = { "company_id", "code" }) })
@Entity
@NamedQueries({ //
		@NamedQuery(name = "CellGroup.findByName", query = "SELECT p FROM CellGroup p where name = :name and companyID = :companyID"), //
		@NamedQuery(name = "CellGroup.findByCode", query = "SELECT p FROM CellGroup p where code = :code and companyID = :companyID"), //
		@NamedQuery(name = "CellGroup.findByID", query = "SELECT p FROM CellGroup p where id = :id and companyID = :companyID") })

public class CellGroup extends hxc.ecds.protocol.rest.CellGroup implements Serializable, ICompanyData<CellGroup>, IBatchEnabled<CellGroup>
{
	final static Logger logger = LoggerFactory.getLogger(CellGroup.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 8759485090571123887L;

	public static final Permission MAY_ADD = new Permission(false, false, Permission.GROUP_CELLGROUPS, Permission.PERM_ADD, "May Add Cell Groups");
	public static final Permission MAY_UPDATE = new Permission(false, false, Permission.GROUP_CELLGROUPS, Permission.PERM_UPDATE, "May Update Cell Groups");
	public static final Permission MAY_DELETE = new Permission(false, false, Permission.GROUP_CELLGROUPS, Permission.PERM_DELETE, "May Delete Cell Groups");
	public static final Permission MAY_VIEW = new Permission(false, false, Permission.GROUP_CELLGROUPS, Permission.PERM_VIEW, "May View Cell Groups");

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
	public CellGroup setId(int id)
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
	public CellGroup setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Override
	@Column(name = "code", nullable = false, length = 5)
	public String getCode()
	{
		return code;
	}

	@Override
	public CellGroup setCode(String code)
	{
		this.code = code;
		return this;
	}

	@Override
	@Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
	public String getName()
	{
		return name;
	}

	@Override
	public CellGroup setName(String name)
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
	public CellGroup setLastUserID(int lastUserID)
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
	public CellGroup setVersion(int version)
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
	public CellGroup setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public CellGroup()
	{

	}

	public CellGroup(CellGroup cellGroup)
	{
		this.lastUserID = cellGroup.lastUserID;
		this.lastTime = cellGroup.lastTime;
		amend(cellGroup);
	}

	public void amend(hxc.ecds.protocol.rest.CellGroup cellGroup)
	{
		this.id = cellGroup.getId();
		this.companyID = cellGroup.getCompanyID();
		this.version = cellGroup.getVersion();
		this.code = cellGroup.getCode();
		this.name = cellGroup.getName();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public static CellGroup findByName(EntityManager em, int companyID, String name)
	{
		TypedQuery<CellGroup> query = em.createNamedQuery("CellGroup.findByName", CellGroup.class);
		query.setParameter("companyID", companyID);
		query.setParameter("name", name);
		List<CellGroup> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static CellGroup findByCode(EntityManager em, int companyID, String code)
	{
		TypedQuery<CellGroup> query = em.createNamedQuery("CellGroup.findByCode", CellGroup.class);
		query.setParameter("companyID", companyID);
		query.setParameter("code", code);
		List<CellGroup> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static CellGroup findByID(EntityManager em, int id, int companyID)
	{
		TypedQuery<CellGroup> query = em.createNamedQuery("CellGroup.findByID", CellGroup.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<CellGroup> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<CellGroup> findAll(EntityManager em, RestParams params, int companyID)
	{
		return QueryBuilder.getQueryResultList(em, CellGroup.class, params, companyID, "name");
	}

	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, CellGroup.class, params, companyID, "name");
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
	public void persist(EntityManager em, CellGroup existing, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(existing);
		QueryBuilder.persist(em, existing, this, session, AuditEntry.TYPE_CELL_GROUP, auditEntryContext);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		QueryBuilder.remove(em, this, session, AuditEntry.TYPE_CELL_GROUP, auditEntryContext);
	}

	@Override
	public void validate(CellGroup previous) throws RuleCheckException
	{
		logger.debug( "**** VALIDATING CELL GROUP MODEL: {}/{}", code, name );
		RuleCheck.validate(this);

		if (previous != null)
		{
			RuleCheck.noChange("id", id, previous.id);
			RuleCheck.noChange("companyID", companyID, previous.companyID);
		}

	}

}
