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

import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.RestParams;
import hxc.services.ecds.util.QueryBuilder;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

// //////////////////////////////////////////////////////////////////////////////////////
//
// Permission Table - Used for Security checks
//
// /////////////////////////////////

@Table(name = "es_permission", uniqueConstraints = { //
		@UniqueConstraint(name = "es_permission_group_name", columnNames = { "grp", "name" }) })
@Entity
@NamedQueries({ //
		@NamedQuery(name = "Permission.findByName", query = "SELECT p FROM Permission p where group = :group and name = :name"), //
		@NamedQuery(name = "Permission.findByID", query = "SELECT p FROM Permission p where id = :id") })

public class Permission extends hxc.ecds.protocol.rest.Permission implements Serializable, IMasterData<Permission>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 5319545875948442306L;

	public static final Permission MAY_UPDATE = new Permission(false, false, Permission.GROUP_PERMISSIONS, Permission.PERM_UPDATE, "May Update Permissions");
	public static final Permission MAY_ESCALATE_AGENT = new Permission(false, false, Permission.GROUP_PERMISSIONS, Permission.PERM_ESCALATE_AGENT, "May Escalate Agent Permissions");
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
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
	@Column(name = "grp", nullable = false, length = GROUP_MAX_LENGTH)
	public String getGroup()
	{
		return group;
	}

	@Override
	public Permission setGroup(String group)
	{
		this.group = group;
		return this;
	}

	@Override
	@Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
	public String getName()
	{
		return name;
	}

	@Override
	public Permission setName(String name)
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
	public Permission setDescription(String description)
	{
		this.description = description;
		return this;
	}

	@Override
	@Column(name = "supplier_only", nullable = false)
	public boolean isSupplierOnly()
	{
		return supplierOnly;
	}

	@Override
	public Permission setSupplierOnly(boolean supplierOnly)
	{
		this.supplierOnly = supplierOnly;
		return this;
	}

	@Override
	@Column(name = "agent_allowed", nullable = false)
	public boolean isAgentAllowed()
	{
		return agentAllowed;
	}

	@Override
	public Permission setAgentAllowed(boolean agentAllowed)
	{
		this.agentAllowed = agentAllowed;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public Permission setLastUserID(int lastUserID)
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
	@Column(name = "lm_time", nullable = false)
	public Date getLastTime()
	{
		return lastTime;
	}

	@Override
	public Permission setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public Permission()
	{

	}

	public Permission(boolean supplierOnly, boolean agentAllowed, String group, String name, String description)
	{
		this.supplierOnly = supplierOnly;
		this.agentAllowed = agentAllowed;
		this.group = group;
		this.name = name;
		this.description = description;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Cloning
	//
	// ////////////////////////////////

	// Make deep copy
	public Permission copy()
	{
		Permission copy = new Permission();
		copy.lastUserID = this.lastUserID;
		copy.lastTime = this.lastTime;
		copy.amend(this);
		return copy;
	}

	// Amend selected fields
	public void amend(hxc.ecds.protocol.rest.Permission permission)
	{
		this.id = permission.getId();
		this.version = permission.getVersion();
		this.group = permission.getGroup();
		this.name = permission.getName();
		this.description = permission.getDescription();
		this.supplierOnly = permission.isSupplierOnly();
		this.agentAllowed = permission.isAgentAllowed();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@Override
	public String toString()
	{
		return String.format("%s.%s", group, name);
	}

	public static Permission findByName(EntityManager em, String group, String name)
	{
		TypedQuery<Permission> query = em.createNamedQuery("Permission.findByName", Permission.class);
		query.setParameter("group", group);
		query.setParameter("name", name);
		List<Permission> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static Permission findByID(EntityManager em, int id)
	{
		TypedQuery<Permission> query = em.createNamedQuery("Permission.findByID", Permission.class);
		query.setParameter("id", id);
		List<Permission> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<Permission> findAll(EntityManager em, RestParams params, int companyID)
	{
		return QueryBuilder.getQueryResultList(em, Permission.class, params, companyID, "group", "name");
	}

	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, Permission.class, params, companyID, "group", "name");
		return query.getSingleResult();
	}

	public static void loadMRD(EntityManager em, Session session) throws RuleCheckException
	{
		Permission.loadMRD(em, MAY_UPDATE, session);
		Permission.loadMRD(em, MAY_ESCALATE_AGENT, session);
	}

	public static void loadMRD(EntityManager em, Permission prototype, Session session) throws RuleCheckException
	{
		Permission permission = findByName(em, prototype.getGroup(), prototype.getName());
		if (permission == null)
		{
			permission = new Permission();
			permission.setGroup(prototype.getGroup());
			permission.setName(prototype.getName());
			permission.setDescription(prototype.getDescription());
			permission.setSupplierOnly(prototype.isSupplierOnly());
			permission.setAgentAllowed(prototype.isAgentAllowed());
			AuditEntryContext auditContext = new AuditEntryContext("LOADED_MRD_PERMISSIONS", permission.getName(), permission.getDescription(), permission.getGroup());
			permission.persist(em, null, session, auditContext);
		}
		else if ((permission.isAgentAllowed() ^ prototype.isAgentAllowed()) //
				|| (permission.isSupplierOnly() ^ prototype.isSupplierOnly()))
		{
			permission.setSupplierOnly(prototype.isSupplierOnly());
			permission.setAgentAllowed(prototype.isAgentAllowed());
			em.persist(permission);
		}
	}

	@Override
	public void persist(EntityManager em, Permission existing, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(existing);
		QueryBuilder.persist(em, existing, this, session, AuditEntry.TYPE_PERMISSION, auditEntryContext);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		throw new RuleCheckException(StatusCode.CANNOT_DELETE, null, "Cannot delete Permissions");
	}

	@Override
	public void validate(Permission existing) throws RuleCheckException
	{
		RuleCheck.validate(this);
	}

}
