package hxc.services.ecds.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
//import org.codehaus.jackson.annotate.JsonIgnore;

import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.RestParams;
import hxc.services.ecds.util.QueryBuilder;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

////////////////////////////////////////////////////////////////////////////////////////
//
// Role Table - Used for Security checks
//
///////////////////////////////////

@Table(name = "es_role", uniqueConstraints = { //
		@UniqueConstraint(name = "es_role_name", columnNames = { "company_id", "name" }) })
@Entity
@NamedQueries({ //
		@NamedQuery(name = "Role.findByName", query = "SELECT p FROM Role p where name = :name and companyID = :companyID"), //
		@NamedQuery(name = "Role.findByID", query = "SELECT p FROM Role p where id = :id and companyID = :companyID") })

public class Role extends hxc.ecds.protocol.rest.Role implements Serializable, ICompanyData<Role>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = -8068142878261672278L;

	public static final String ROLE_ADMINISTRATOR_NAME = "Administration";
	public static final String ROLE_SUPPLIER_NAME = "Supplier";
	public static final String ROLE_AGENT_ALL_NAME = "AgentAll";

	public static final Permission MAY_ADD = new Permission(false, false, Permission.GROUP_ROLES, Permission.PERM_ADD, "May Add Roles");
	public static final Permission MAY_UPDATE = new Permission(false, false, Permission.GROUP_ROLES, Permission.PERM_UPDATE, "May Update Roles");
	public static final Permission MAY_DELETE = new Permission(false, false, Permission.GROUP_ROLES, Permission.PERM_DELETE, "May Delete Roles");
	public static final Permission MAY_VIEW = new Permission(false, false, Permission.GROUP_ROLES, Permission.PERM_VIEW, "May View Roles");
	public static final Permission MAY_DOWNLOAD = new Permission(false, false, Permission.GROUP_ROLES, Permission.PERM_DOWNLOAD, "May Download Roles");

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
	public Role setId(int id)
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
	public void setCompanyID(int companyID)
	{
		this.companyID = companyID;
	}

	@Override
	@Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
	public String getName()
	{
		return name;
	}

	@Override
	public Role setName(String name)
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
	public Role setDescription(String description)
	{
		this.description = description;
		return this;
	}
	
	@Override
	@Column(name = "type", nullable = false, length = 1)	
	public String getType()
	{
		return type;
	}

	@Override
	public Role setType(String type)
	{
		this.type = type;
		return this;
	}

	@Override
	@Column(name = "permanent", nullable = false)
	public boolean isPermanent()
	{
		return permanent;
	}

	@Override
	public Role setPermanent(boolean permanent)
	{
		this.permanent = permanent;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public Role setLastUserID(int lastUserID)
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
	@Version
	public Role setVersion(int version)
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
	public Role setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	@Override
	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
	@JoinTable(name = "es_role_permission", joinColumns = { @JoinColumn(name = "role_id") }, inverseJoinColumns = { @JoinColumn(name = "permission_id") })
	@SuppressWarnings({"unchecked"})
	public List<Permission> getPermissions()
	{
		return (List<Permission>) permissions;
	}

	@Override
	public void setPermissions(List<? extends hxc.ecds.protocol.rest.Permission> permissions)
	{
		super.setPermissions(permissions);
	}

	@Transient
	private boolean hasPermission(int id)
	{
		List<Permission> permissions = getPermissions();
		if (permissions == null)
			return false;
		for (Permission permission : permissions)
		{
			if (permission.getId() == id)
				return true;
		}

		return false;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public Role()
	{

	}

	public Role(Role role) {
		super(role);
		this.lastUserID = role.lastUserID;
		this.lastTime = role.lastTime;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Cloning
	//
	// /////////////////////////////////

	// Make deep copy
	public Role copy(EntityManager em) 
	{
		Role copy = new Role();
		copy.lastUserID = this.lastUserID;
		copy.lastTime = this.lastTime;
		copy.amend(em, this);
		return copy;
	}

	// Amend selected fields
	@SuppressWarnings({"unchecked"})
	public void amend(EntityManager em, hxc.ecds.protocol.rest.Role role) 
	{
		this.id = role.getId();
		this.companyID = role.getCompanyID();
		this.version = role.getVersion();
		this.name = role.getName();
		this.description = role.getDescription();
		this.type = role.getType();
		this.permanent = role.isPermanent();

		// Add new Permissions
		List<hxc.ecds.protocol.rest.Permission> newPermissions = (List<hxc.ecds.protocol.rest.Permission>) role.getPermissions();
		List<Permission> existingPermissions = getPermissions();
		if (newPermissions != null)
		{
			for (hxc.ecds.protocol.rest.Permission newPermission : newPermissions)
			{
				if (!contains(existingPermissions, newPermission))
				{
					existingPermissions.add(Permission.findByID(em, newPermission.getId()));
				}
			}

			// Remove unused Permissions
			int index = 0;
			while (index < existingPermissions.size())
			{
				if (!contains(newPermissions, existingPermissions.get(index)))
					existingPermissions.remove(index);
				else
					index++;
			}

		}
		
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public static Role findByName(EntityManager em, int companyID, String name)
	{
		TypedQuery<Role> query = em.createNamedQuery("Role.findByName", Role.class);
		query.setParameter("companyID", companyID);
		query.setParameter("name", name);
		List<Role> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}
	
	public static Role findAgentAll(EntityManager em, int companyID)
	{
		return findByName(em, companyID, ROLE_AGENT_ALL_NAME);
	}

	public static Role findByID(EntityManager em, int id, int companyID)
	{
		TypedQuery<Role> query = em.createNamedQuery("Role.findByID", Role.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<Role> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<Role> findAll(EntityManager em, RestParams params, int companyID)
	{
		return QueryBuilder.getQueryResultList(em, Role.class, params, companyID, "name");
	}

	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, Role.class, params, companyID, "name");
		return query.getSingleResult();
	}

	public static void loadMRD(EntityManager em, int companyID, Session session) throws RuleCheckException
	{
		Permission.loadMRD(em, MAY_ADD, session);
		Permission.loadMRD(em, MAY_UPDATE, session);
		Permission.loadMRD(em, MAY_DELETE, session);
		Permission.loadMRD(em, MAY_VIEW, session);
		Permission.loadMRD(em, MAY_DOWNLOAD, session);

		Role.loadMRD(em, session, companyID, true, false, ROLE_SUPPLIER_NAME, "Built-in Supplier Role");
		Role.loadMRD(em, session, companyID, false, false, ROLE_ADMINISTRATOR_NAME, "Built-in Administration Role");
		Role.loadMRD(em, session, companyID, false, true, ROLE_AGENT_ALL_NAME, "Built-in Agent Role");

	}

	private static void loadMRD(EntityManager em, Session session, int companyID, boolean supplier, boolean agent, String name, String description) throws RuleCheckException
	{
		boolean changed = false;
		Role role = findByName(em, companyID, name);
		Role oldRole = role == null ? null : new Role(role);
		if (role == null)
		{
			role = new Role();
			role.setCompanyID(companyID);
			role.setName(name);
			role.setDescription(description);
			role.setType(agent ? TYPE_AGENT : TYPE_WEB_USER);
			role.setPermanent(true);
			AuditEntryContext auditContext = new AuditEntryContext("LOADED_MRD_ROLE", role.getName(), role.getDescription());
			role.persist(em, null, session, auditContext);
			changed = true;
		}
		
		String filter = supplier ? null : "supplierOnly='0'";
		if (agent)
			filter = "agentAllowed='true'"; 
		
		RestParams params = new RestParams(null, 0, -1, "group+name+", null, filter);
		List<Permission> perms = Permission.findAll(em, params, companyID);
		List<Permission> permissions = role.getPermissions();

		for (Permission perm : perms)
		{
			if (!role.hasPermission(perm.getId()))
			{
				permissions.add(perm);
				changed = true;
			}
		}

		if (changed)
		{
			AuditEntryContext auditContext = new AuditEntryContext("MRD_ROLE_UPDATED", role.getName(), role.getId());
			role.persist(em, oldRole, session, auditContext);
		}

	}

	@Override
	public void persist(EntityManager em, Role previous, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(previous);
		QueryBuilder.persist(em, previous, this, session, AuditEntry.TYPE_ROLE, auditEntryContext);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		RuleCheck.isFalse(null, permanent, "Cannot delete Permanent Role");
		QueryBuilder.remove(em, this, session, AuditEntry.TYPE_ROLE, auditEntryContext);
	}

	@Override
	public void validate(Role previous) throws RuleCheckException
	{
		RuleCheck.validate(this);

		if (previous != null && permanent ^ previous.isPermanent())
			throw new RuleCheckException(StatusCode.INVALID_VALUE, "permanent", "Cannot change permanency");
		
//		if (Role.TYPE_AGENT.equals(this.type))
//		{
//			for (hxc.ecds.protocol.rest.Permission permission:permissions)
//			{
//				if (!permission.isAgentAllowed())
//					throw new RuleCheckException(StatusCode.INVALID_VALUE, "type", "Invalid Permission for Role Type"); 
//			}
//		}
		
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	private boolean contains(List<? extends hxc.ecds.protocol.rest.Permission> permissions, hxc.ecds.protocol.rest.Permission permission)
	{
		for (hxc.ecds.protocol.rest.Permission perm : permissions)
		{
			if (perm.getId() == permission.getId())
				return true;
		}
		return false;
	}



}
