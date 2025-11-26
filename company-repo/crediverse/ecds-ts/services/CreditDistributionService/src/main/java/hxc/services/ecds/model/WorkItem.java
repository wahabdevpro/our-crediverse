package hxc.services.ecds.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
import javax.persistence.Query;
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

////////////////////////////////////////////////////////////////////////////////////////
//
// WorkItem Table - User for Workflow
//
///////////////////////////////////

@Table(name = "ew_item", uniqueConstraints = { //
		@UniqueConstraint(name = "ew_item_uuid", columnNames = { "uuid" }) })
@Entity
@NamedQueries({ //
		@NamedQuery(name = "WorkItem.findByUUID", query = "SELECT p FROM WorkItem p where uuid = :uuid and companyID = :companyID"), //
		@NamedQuery(name = "WorkItem.findByID", query = "SELECT p FROM WorkItem p where id = :id and companyID = :companyID"), //
		@NamedQuery(name = "WorkItem.referenceWebUser", query = "SELECT p FROM WorkItem p where createdByWebUserID = :webUserID or createdForWebUserID = :webUserID"), //
		@NamedQuery(name = "WorkItem.referenceAgent", query = "SELECT p FROM WorkItem p where createdByAgentID = :agentID"), //
		@NamedQuery(name = "WorkItem.cleanout", query = "delete from WorkItem p where p.creationTime < :before and companyID = :companyID and " //
				+ "state in " + hxc.ecds.protocol.rest.WorkItem.INACTIVE_FILTER), //
		@NamedQuery(name = "WorkItem.findForMe", query = "select w from WorkItem w " //
				+ " where w.companyID = :companyID " //
				+ " and w.state in " + hxc.ecds.protocol.rest.WorkItem.ACTIVE_FILTER //
				+ " and (w.createdForWebUserID = :createdForWebUserID or w.createdForPermissionID in (" //
				+ "select p.id from WebUser u join u.roles r join r.permissions p where u.id = :createdForWebUserID" //
				+ ")) order by w.lastTime desc"), //

		@NamedQuery(name = "WorkItem.findForMeCount", query = "select count(w) from WorkItem w " //
				+ " where w.companyID = :companyID " //
				+ " and w.state in " + hxc.ecds.protocol.rest.WorkItem.ACTIVE_FILTER //
				+ " and (w.createdForWebUserID = :createdForWebUserID or w.createdForPermissionID in (" //
				+ "select p.id from WebUser u join u.roles r join r.permissions p where u.id = :createdForWebUserID" //
				+ "))"), //

		@NamedQuery(name = "WorkItem.findMyHistory", query = "select w from WorkItem w " //
				+ " where w.companyID = :companyID " //
				+ " and w.state in " + hxc.ecds.protocol.rest.WorkItem.INACTIVE_FILTER //
				+ " and (w.createdForWebUserID = :createdForWebUserID or w.createdForPermissionID in (" //
				+ "select p.id from WebUser u join u.roles r join r.permissions p where u.id = :createdForWebUserID" //
				+ ")) order by w.lastTime desc"), //

		@NamedQuery(name = "WorkItem.findMyHistoryCount", query = "select count(w) from WorkItem w " //
				+ " where w.companyID = :companyID " //
				+ " and w.state in " + hxc.ecds.protocol.rest.WorkItem.INACTIVE_FILTER //
				+ " and (w.createdForWebUserID = :createdForWebUserID or w.createdForPermissionID in (" //
				+ "select p.id from WebUser u join u.roles r join r.permissions p where u.id = :createdForWebUserID" //
				+ "))"), //

})
//
//
public class WorkItem extends hxc.ecds.protocol.rest.WorkItem implements Serializable, ICompanyData<WorkItem> //

{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 9222052197463039172L;

	public static final Permission MAY_ADD = new Permission(false, false, Permission.GROUP_WORKITEMS, Permission.PERM_ADD, "May Add Workflow Items");
	public static final Permission MAY_UPDATE = new Permission(false, false, Permission.GROUP_WORKITEMS, Permission.PERM_UPDATE, "May Update Workflow Items");
	public static final Permission MAY_DELETE = new Permission(false, false, Permission.GROUP_WORKITEMS, Permission.PERM_DELETE, "May Delete Workflow Items");
	public static final Permission MAY_CONFIGURE = new Permission(false, false, Permission.GROUP_WORKITEMS, Permission.PERM_CONFIGURE, "May Configure Workflow");
	public static final Permission MAY_VIEW = new Permission(false, false, Permission.GROUP_WORKITEMS, Permission.PERM_VIEW, "May View Workflow");

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
	protected WebUser createdByWebUser;
	@JsonIgnore
	protected Agent createdByAgent;
	@JsonIgnore
	protected WebUser createdForWebUser;
	@JsonIgnore
	protected Permission createdForPermission;

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
	public WorkItem setId(int id)
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
	public WorkItem setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Override
	@Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LENGTH)
	public String getDescription()
	{
		return description;
	}

	@Override
	public WorkItem setDescription(String description)
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
	public WorkItem setState(String state)
	{
		this.state = state;
		return this;
	}

	// @Type(type="uuid-char"), columnDefinition="uuid-char"
	@Override
	@Column(name = "uuid", nullable = false, columnDefinition = "BINARY(16)")
	public UUID getUuid()
	{
		return uuid;
	}

	@Override
	public WorkItem setUuid(UUID uuid)
	{
		this.uuid = uuid;
		return this;
	}

	@Override
	@Column(name = "type", nullable = false, length = 1)
	public String getType()
	{
		return type;
	}

	@Override
	public WorkItem setType(String type)
	{
		this.type = type;
		return this;
	}

	@Override
	@Column(name = "created", nullable = false)
	public Date getCreationTime()
	{
		return creationTime;
	}

	@Override
	public WorkItem setCreationTime(Date creationTime)
	{
		this.creationTime = creationTime;
		return this;
	}

	@Override
	@Column(name = "completed", nullable = true)
	public Date getCompletionTime()
	{
		return completionTime;
	}

	@Override
	public WorkItem setCompletionTime(Date completionTime)
	{
		this.completionTime = completionTime;
		return this;
	}

	@Override
	@Column(name = "by_user_id", nullable = true, insertable = false, updatable = false)
	public Integer getCreatedByWebUserID()
	{
		return createdByWebUserID;
	}

	@Override
	@Column(name = "send_sms", nullable = false)
	public boolean isSmsOnChange()
	{
		return smsOnChange;
	}

	@Override
	public WorkItem setSmsOnChange(boolean smsOnChange)
	{
		this.smsOnChange = smsOnChange;
		return this;
	}

	@Override
	@Column(name = "reason", nullable = true, length = REASON_MAX_LENGTH)
	public String getReason()
	{
		return reason;
	}

	@Override
	public WorkItem setReason(String reason)
	{
		this.reason = reason;
		return this;
	}

	@Override
	public WorkItem setCreatedByWebUserID(Integer createdByWebUserID)
	{
		this.createdByWebUserID = createdByWebUserID;
		return this;
	}

	@Override
	@Column(name = "by_agent_id", nullable = true, insertable = false, updatable = false)
	public Integer getCreatedByAgentID()
	{
		return createdByAgentID;
	}

	@Override
	public WorkItem setCreatedByAgentID(Integer createdByAgentID)
	{
		this.createdByAgentID = createdByAgentID;
		return this;
	}

	@Override
	@Column(name = "for_user_id", nullable = true, insertable = false, updatable = false)
	public Integer getCreatedForWebUserID()
	{
		return createdForWebUserID;
	}

	@Override
	public WorkItem setCreatedForWebUserID(Integer createdForWebUserID)
	{
		this.createdForWebUserID = createdForWebUserID;
		return this;
	}

	@Override
	@Column(name = "for_perm_id", nullable = true, insertable = false, updatable = false)
	public Integer getCreatedForPermissionID()
	{
		return createdForPermissionID;
	}

	@Override
	public WorkItem setCreatedForPermissionID(Integer createdForPermissionID)
	{
		this.createdForPermissionID = createdForPermissionID;
		return this;
	}

	@Override
	@Column(name = "uri", nullable = false, length = URI_MAX_LENGTH)
	public String getUri()
	{
		return uri;
	}

	@Override
	public WorkItem setUri(String uri)
	{
		this.uri = uri;
		return this;
	}

	@Override
	@Column(name = "request", nullable = true, columnDefinition = "TEXT")
	public String getRequest()
	{
		return request;
	}

	@Override
	public WorkItem setRequest(String request)
	{
		this.request = request;
		return this;
	}

	@Override
	@Column(name = "response", nullable = true, columnDefinition = "TEXT")
	public String getResponse()
	{
		return response;
	}

	@Override
	public WorkItem setResponse(String response)
	{
		this.response = response;
		return this;
	}

	@Override
	@Column(name = "work_type", nullable = true, length = WORK_TYPE_MAX_LENGTH)
	public String getWorkType()
	{
		return workType;
	}

	@Override
	public WorkItem setWorkType(String workType)
	{
		this.workType = workType;
		return this;
	}

	@Override
	@Column(name = "status", nullable = true, length = STATUS_MAX_LENGTH)
	public String getStatus()
	{
		return status;
	}

	@Override
	public WorkItem setStatus(String status)
	{
		this.status = status;
		return this;
	}

	@Override
	@Column(name = "owner_session", nullable = true, length = SESSION_MAX_LENGTH)
	public String getOwnerSession()
	{
		return ownerSession;
	}

	@Override
	public WorkItem setOwnerSession(String ownerSession)
	{
		this.ownerSession = ownerSession;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public WorkItem setLastUserID(int lastUserID)
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
	public WorkItem setVersion(int version)
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
	public WorkItem setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "by_user_id", foreignKey = @ForeignKey(name = "FK_Work_By"))
	public WebUser getCreatedByWebUser()
	{
		return createdByWebUser;
	}

	public WorkItem setCreatedByWebUser(WebUser createdByWebUser)
	{
		this.createdByWebUser = createdByWebUser;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "by_agent_id", foreignKey = @ForeignKey(name = "FK_Work_Agent"))
	public Agent getCreatedByAgent()
	{
		return createdByAgent;
	}

	public WorkItem setCreatedByAgent(Agent createdByAgent)
	{
		this.createdByAgent = createdByAgent;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "for_user_id", foreignKey = @ForeignKey(name = "FK_Work_For"))
	public WebUser getCreatedForWebUser()
	{
		return createdForWebUser;
	}

	public WorkItem setCreatedForWebUser(WebUser createdForWebUser)
	{
		this.createdForWebUser = createdForWebUser;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "for_perm_id", foreignKey = @ForeignKey(name = "FK_Work_Perm"))
	public Permission getCreatedForPermission()
	{
		return createdForPermission;
	}

	public WorkItem setCreatedForPermission(Permission createdForPermission)
	{
		this.createdForPermission = createdForPermission;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public WorkItem()
	{

	}

	public WorkItem(WorkItem workItem)
	{
		this.lastUserID = workItem.lastUserID;
		this.lastTime = workItem.lastTime;
		amend(workItem);
	}

	public void amend(hxc.ecds.protocol.rest.WorkItem workItem)
	{
		this.id = workItem.getId();
		this.companyID = workItem.getCompanyID();
		this.version = workItem.getVersion();
		this.description = workItem.getDescription();
		this.uuid = workItem.getUuid();
		this.state = workItem.getState();
		this.type = workItem.getType();
		this.creationTime = workItem.getCreationTime();
		this.completionTime = workItem.getCompletionTime();
		this.smsOnChange = workItem.isSmsOnChange();
		this.reason = workItem.getReason();
		this.createdByWebUserID = workItem.getCreatedByWebUserID();
		this.createdByAgentID = workItem.getCreatedByAgentID();
		this.createdForWebUserID = workItem.getCreatedForWebUserID();
		this.createdForPermissionID = workItem.getCreatedForPermissionID();
		this.uri = workItem.getUri();
		this.request = workItem.getRequest();
		this.status = workItem.getStatus();
		this.workType = workItem.getWorkType();
		this.ownerSession = workItem.getOwnerSession();
		this.response = workItem.getResponse();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public static WorkItem findByID(EntityManager em, int id, int companyID)
	{
		TypedQuery<WorkItem> query = em.createNamedQuery("WorkItem.findByID", WorkItem.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<WorkItem> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static WorkItem findByUUID(EntityManager em, UUID uuid, int companyID)
	{
		TypedQuery<WorkItem> query = em.createNamedQuery("WorkItem.findByUUID", WorkItem.class);
		query.setParameter("uuid", uuid);
		query.setParameter("companyID", companyID);
		List<WorkItem> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<WorkItem> findAll(EntityManager em, RestParams params, int companyID) throws RuleCheckException
	{
		return QueryBuilder.getQueryResultList(em, WorkItem.class, params, companyID, "name", "description");
	}

	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, WorkItem.class, params, companyID, "name", "description");
		return query.getSingleResult();
	}

	public static List<WorkItem> findForMe(EntityManager em, RestParams params, Session session) 
	{
		TypedQuery<WorkItem> query = em.createNamedQuery("WorkItem.findForMe", WorkItem.class);
		query.setParameter("companyID", session.getCompanyID());
		query.setParameter("createdForWebUserID", session.getWebUserID());
		if (params.getFirst() > 0)
			query.setFirstResult(params.getFirst());
		query.setMaxResults(params.getMax() >= 0 ? params.getMax() : RestParams.DEFAULT_MAX_RESULTS);
		List<WorkItem> results = query.getResultList();
		return results;
	}

	public static Long findForMeCount(EntityManager em, RestParams params, Session session)
	{
		TypedQuery<Long> query = em.createNamedQuery("WorkItem.findForMeCount", Long.class);
		query.setParameter("companyID", session.getCompanyID());
		query.setParameter("createdForWebUserID", session.getWebUserID());
		return query.getSingleResult();
	}

	public static List<WorkItem> findMyHistory(EntityManager em, RestParams params, Session session) throws RuleCheckException
	{
		TypedQuery<WorkItem> query = em.createNamedQuery("WorkItem.findMyHistory", WorkItem.class);
		query.setParameter("companyID", session.getCompanyID());
		query.setParameter("createdForWebUserID", session.getWebUserID());
		if (params.getFirst() > 0)
			query.setFirstResult(params.getFirst());
		query.setMaxResults(params.getMax() >= 0 ? params.getMax() : RestParams.DEFAULT_MAX_RESULTS);
		List<WorkItem> results = query.getResultList();
		return results;
	}

	public static Long findMyHistoryCount(EntityManager em, RestParams params, Session session)
	{
		TypedQuery<Long> query = em.createNamedQuery("WorkItem.findMyHistoryCount", Long.class);
		query.setParameter("companyID", session.getCompanyID());
		query.setParameter("createdForWebUserID", session.getWebUserID());
		return query.getSingleResult();
	}

	public static boolean referencesWebUser(EntityManager em, int webUserID)
	{
		TypedQuery<WorkItem> query = em.createNamedQuery("WorkItem.referenceWebUser", WorkItem.class);
		query.setParameter("webUserID", webUserID);
		query.setMaxResults(1);
		List<WorkItem> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesAgent(EntityManager em, int agentID)
	{
		TypedQuery<WorkItem> query = em.createNamedQuery("WorkItem.referenceAgent", WorkItem.class);
		query.setParameter("agentID", agentID);
		query.setMaxResults(1);
		List<WorkItem> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	// Clean-out Work Item Entries
	public static int cleanout(EntityManager em, Date before, int companyID)
	{
		Query query = em.createNamedQuery("WorkItem.cleanout");
		query.setParameter("before", before);
		query.setParameter("companyID", companyID);
		return query.executeUpdate();
	}

	public static void loadMRD(EntityManager em, int companyID, Session session) throws RuleCheckException
	{
		Permission.loadMRD(em, MAY_ADD, session);
		Permission.loadMRD(em, MAY_UPDATE, session);
		Permission.loadMRD(em, MAY_DELETE, session);
		Permission.loadMRD(em, MAY_CONFIGURE, session);
		Permission.loadMRD(em, MAY_VIEW, session);
	}

	@Override
	public void persist(EntityManager em, WorkItem existing, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(existing);
		QueryBuilder.persist(em, existing, this, session, AuditEntry.TYPE_WORK_ITEM, auditEntryContext);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		QueryBuilder.remove(em, this, session, AuditEntry.TYPE_WORK_ITEM, auditEntryContext);
	}

	@Override
	public void validate(WorkItem previous) throws RuleCheckException
	{
		RuleCheck.validate(this);

		if (previous != null)
		{
			RuleCheck.noChange("id", id, previous.id);
			RuleCheck.noChange("companyID", companyID, previous.companyID);
		}

	}

}
