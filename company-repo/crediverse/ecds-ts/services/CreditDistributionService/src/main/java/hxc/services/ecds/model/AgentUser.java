package hxc.services.ecds.model;
import java.io.Serializable;
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
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import hxc.ecds.protocol.rest.config.AgentsConfig;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.AgentUsers;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.rest.RestParams;
import hxc.services.ecds.util.AuthenticationHelper;
import hxc.services.ecds.util.PredicateExtender;
import hxc.services.ecds.util.QueryBuilder;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;

////////////////////////////////////////////////////////////////////////////////////////
//
// AgentUser Table - Used for AgentUsers
//
///////////////////////////////////

@Table(name = "ea_user", uniqueConstraints = { //
		@UniqueConstraint(name = "ea_user_acc_no", columnNames = { "comp_id", "acc_no" }), //
		@UniqueConstraint(name = "ea_user_msisdn", columnNames = { "msisdn" }), //
		@UniqueConstraint(name = "ea_user_domain_account", columnNames = { "comp_id", "domain_account" }), //
})
@Entity
@NamedQueries({ //
		@NamedQuery(name = "AgentUser.findByID", query = "SELECT p FROM AgentUser p where id = :id and companyID = :companyID"), //
		@NamedQuery(name = "AgentUser.findByMSISDN", query = "SELECT p FROM AgentUser p where mobileNumber = :msisdn and companyID = :companyID"), //
		@NamedQuery(name = "AgentUser.findByMSISDNWithAgent", query = "SELECT p FROM AgentUser p LEFT OUTER JOIN FETCH p.agent where p.mobileNumber = :msisdn and p.companyID = :companyID"), //
		@NamedQuery(name = "AgentUser.findByAccountNo", query = "SELECT p FROM AgentUser p where accountNumber = :accountNumber and companyID = :companyID"), //
		@NamedQuery(name = "AgentUser.findByDomainAccountName", query = "SELECT p FROM AgentUser p where companyID = :companyID and domainAccountName = :domainAccountName"), //
		@NamedQuery(name = "AgentUser.findByDomainAccountNameWithAgent", query = "SELECT p FROM AgentUser p LEFT OUTER JOIN FETCH p.agent where p.companyID = :companyID and p.domainAccountName = :domainAccountName"), //
		@NamedQuery(name = "AgentUser.referenceAgent", query = "SELECT p FROM AgentUser p where agentID.id = :agentID"), //
		@NamedQuery(name = "AgentUser.findAgentUsers", query = "SELECT p FROM AgentUser p where agentID = :agentID and authenticationMethod != '" + AgentUser.AUTHENTICATE_PASSWORD_2FACTOR + "' and companyID = :companyID"), //
		@NamedQuery(name = "AgentUser.findApiUsers", query = "SELECT p FROM AgentUser p where agentID = :agentID and authenticationMethod = '" + AgentUser.AUTHENTICATE_PASSWORD_2FACTOR + "' and companyID = :companyID"), //
})
@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
public class AgentUser extends hxc.ecds.protocol.rest.AgentUser //
		implements Serializable, ICompanyData<AgentUser>, IAgentUser, IAuthenticatable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = -6959451753603217601L;

	public static final String NEXT_USER_AGENT_ACC_NO = "NextAgentUserAccNo";

	public static final Permission MAY_ADD = new Permission(false, false, Permission.GROUP_AGENT_USERS, Permission.PERM_ADD, "May Add Agent or API Users");
	public static final Permission MAY_UPDATE = new Permission(false, false, Permission.GROUP_AGENT_USERS, Permission.PERM_UPDATE, "May Update Agent or API Users");
	public static final Permission MAY_DELETE = new Permission(false, false, Permission.GROUP_AGENT_USERS, Permission.PERM_DELETE, "May Delete Agent or API Users");
	public static final Permission MAY_RESET_IMSI_LOCK = new Permission(false, false, Permission.GROUP_AGENT_USERS, Permission.PERM_RESET_IMSI, "May reset IMSI Lockout");
	public static final Permission MAY_RESET_PIN = new Permission(false, false, Permission.GROUP_AGENT_USERS, Permission.PERM_RESET_PIN, "May reset PIN");
	public static final Permission MAY_VIEW = new Permission(false, false, Permission.GROUP_AGENT_USERS, Permission.PERM_VIEW, "May View Agent or API Users");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Additional Fields
	//
	// /////////////////////////////////

	@JsonIgnore
	protected byte[] key1;
	@JsonIgnore
	protected byte[] key2;
	@JsonIgnore
	protected byte[] key3;
	@JsonIgnore
	protected byte[] key4;
	@JsonIgnore
	protected Agent agent;
	@JsonIgnore
	protected Role role;
	@JsonIgnore
	protected int lastUserID;
	@JsonIgnore
	protected Date lastTime;
	@JsonIgnore
	protected Date lastImsiChange;
	@JsonIgnore
	protected Date lastImeiUpdate;

	@JsonIgnore
	protected Integer lastCellID;
	@JsonIgnore
	protected Cell lastCell;
	@JsonIgnore
	protected Date lastCellExpiryTime;

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
	public AgentUser setId(int id)
	{
		this.id = id;
		return this;
	}

	@Override
	@Column(name = "comp_id", nullable = false)
	public int getCompanyID()
	{
		return companyID;
	}

	@Override
	public AgentUser setCompanyID(int companyID)
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
	public AgentUser setVersion(int version)
	{
		this.version = version;
		return this;
	}

	@Override
	@Column(name = "acc_no", nullable = true, length = ACCOUNT_NO_MAX_LENGTH)
	public String getAccountNumber()
	{
		return accountNumber;
	}

	@Override
	public AgentUser setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
		return this;
	}

	@Override
	@Column(name = "msisdn", nullable = false, length = PHONE_NUMBER_MAX_LENGTH)
	public String getMobileNumber()
	{
		return mobileNumber;
	}

	@Override
	public AgentUser setMobileNumber(String mobileNumber)
	{
		this.mobileNumber = mobileNumber;
		return this;
	}

	@Override
	@Column(name = "imei", nullable = true, length = IMEI_MAX_LENGTH)
	public String getImei()
	{
		return imei;
	}

	@Override
	public AgentUser setImei(String imei)
	{
		this.imei = imei;
		return this;
	}

	@Override
	@Column(name = "imsi", nullable = true, length = IMSI_MAX_LENGTH)
	public String getImsi()
	{
		return imsi;
	}

	@Override
	public AgentUser setImsi(String imsi)
	{
		this.imsi = imsi;
		return this;
	}

	@Override
	@Column(name = "title", nullable = true, length = TITLE_MAX_LENGTH)
	public String getTitle()
	{
		return title;
	}

	@Override
	public AgentUser setTitle(String title)
	{
		this.title = title;
		return this;
	}

	@Override
	@Column(name = "first_name", nullable = false, length = FIRST_NAME_MAX_LENGTH)
	public String getFirstName()
	{
		return firstName;
	}

	@Override
	public AgentUser setFirstName(String firstName)
	{
		this.firstName = firstName;
		return this;
	}

	@Override
	@Column(name = "intitials", nullable = true, length = INITIALS_MAX_LENGTH)
	public String getInitials()
	{
		return initials;
	}

	@Override
	public AgentUser setInitials(String initials)
	{
		this.initials = initials;
		return this;
	}

	@Override
	@Column(name = "surname", nullable = false, length = LAST_NAME_MAX_LENGTH)
	public String getSurname()
	{
		return surname;
	}

	@Override
	public AgentUser setSurname(String surname)
	{
		this.surname = surname;
		return this;
	}

	@Override
	@Column(name = "language", nullable = false, length = LANGUAGE_MAX_LENGTH)
	public String getLanguage()
	{
		return language;
	}

	@Override
	public AgentUser setLanguage(String language)
	{
		this.language = language;
		return this;
	}

	@Override
	@Column(name = "domain_account", nullable = true, length = DOMAIN_NAME_MAX_LENGTH)
	public String getDomainAccountName()
	{
		return domainAccountName;
	}

	@Override
	public AgentUser setDomainAccountName(String domainAccountName)
	{
		this.domainAccountName = domainAccountName;
		return this;
	}

	@Override
	@Column(name = "state", nullable = false, length = 1)
	public String getState()
	{
		return state;
	}

	@Override
	public AgentUser setState(String state)
	{
		this.state = state;
		return this;
	}

	@Override
	@Column(name = "channel_type", nullable = true, length = 2)
	public String getChannelType()
	{
		return channelType;
	}

	@Override
	public AgentUser setChannelType(String channelType)
	{
		this.channelType = channelType;
		return this;
	}

	@Override
	@Column(name = "a_date", nullable = true)
	public Date getActivationDate()
	{
		return activationDate;
	}

	@Override
	public AgentUser setActivationDate(Date activationDate)
	{
		this.activationDate = activationDate;
		return this;
	}

	@Override
	@Column(name = "d_date", nullable = true)
	public Date getDeactivationDate()
	{
		return deactivationDate;
	}

	@Override
	public AgentUser setDeactivationDate(Date deactivationDate)
	{
		this.deactivationDate = deactivationDate;
		return this;
	}

	@Override
	@Column(name = "e_date", nullable = true)
	public Date getExpirationDate()
	{
		return expirationDate;
	}

	@Override
	public AgentUser setExpirationDate(Date expirationDate)
	{
		this.expirationDate = expirationDate;
		return this;
	}

	@Override
	@Column(name = "channels", nullable = false)
	public int getAllowedChannels()
	{
		return allowedChannels;
	}

	@Override
	public AgentUser setAllowedChannels(int allowedChannels)
	{
		this.allowedChannels = allowedChannels;
		return this;
	}

	@Override
	@Column(name = "temp_pin", nullable = false)
	public boolean isTemporaryPin()
	{
		return temporaryPin;
	}

	@Override
	public AgentUser setTemporaryPin(boolean temporaryPin)
	{
		this.temporaryPin = temporaryPin;
		return this;
	}
	
	@Override
	@Column(name = "auth_method", nullable = false, unique = false, length = 1)
	public String getAuthenticationMethod()
	{
		return authenticationMethod;
	}

	@Override
	public AgentUser setAuthenticationMethod(String authenticationMethod)
	{
		this.authenticationMethod = authenticationMethod;
		return this;
	}

	@Column(name = "key1", nullable = true, unique = false, length = KEY_MAX_LENGTH)
	public byte[] getKey1()
	{
		return key1;
	}

	public AgentUser setKey1(byte[] key1)
	{
		this.key1 = key1;
		return this;
	}

	@Column(name = "key2", nullable = true, unique = false, length = KEY_MAX_LENGTH)
	public byte[] getKey2()
	{
		return key2;
	}

	public AgentUser setKey2(byte[] key2)
	{
		this.key2 = key2;
		return this;
	}

	@Column(name = "key3", nullable = true, unique = false, length = KEY_MAX_LENGTH)
	public byte[] getKey3()
	{
		return key3;
	}

	public AgentUser setKey3(byte[] key3)
	{
		this.key3 = key3;
		return this;
	}

	@Column(name = "key4", nullable = true, unique = false, length = KEY_MAX_LENGTH)
	public byte[] getKey4()
	{
		return key4;
	}

	public AgentUser setKey4(byte[] key4)
	{
		this.key4 = key4;
		return this;
	}

	@Column(name = "attempts", nullable = true)
	public Integer getConsecutiveAuthFailures()
	{
		return consecutiveAuthFailures;
	}

	public AgentUser setConsecutiveAuthFailures(Integer consecutiveAuthFailures)
	{
		this.consecutiveAuthFailures = consecutiveAuthFailures;
		return this;
	}

	@Override
	@Column(name = "dept", nullable = true, length = DEPARTMENT_MAX_LENGTH)
	public String getDepartment()
	{
		return department;
	}

	@Override
	public AgentUser setDepartment(String department)
	{
		this.department = department;
		return this;
	}

	@Override
	@Column(name = "email", nullable = true, length = EMAIL_MAX_LENGTH)
	public String getEmail()
	{
		return email;
	}

	@Override
	public AgentUser setEmail(String email)
	{
		this.email = email;
		return this;
	}

	@Override
	@Column(name = "agent_id", nullable = false, insertable = false, updatable = false)
	public int getAgentID()
	{
		return agentID;
	}

	@Override
	public AgentUser setAgentID(int agentID)
	{
		this.agentID = agentID;
		return this;
	}

	@Override
	@Column(name = "role_id", nullable = false, insertable = false, updatable = false)
	public int getRoleID()
	{
		return roleID;
	}

	@Override
	public AgentUser setRoleID(int roleID)
	{
		this.roleID = roleID;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public AgentUser setLastUserID(int lastUserID)
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
	public AgentUser setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	@Override
	@Column(name = "last_imsi", nullable = true)
	public Date getLastImsiChange()
	{
		return lastImsiChange;
	}

	public AgentUser setLastImsiChange(Date lastImsiChange)
	{
		this.lastImsiChange = lastImsiChange;
		return this;
	}

	@Override
	@Transient
	public boolean isImsiLockedOut()
	{
		return imsiLockedOut;
	}

	@Override
	public AgentUser setImsiLockedOut(boolean imsiLockedOut)
	{
		this.imsiLockedOut = imsiLockedOut;
		return this;
	}

	@Override
	@Transient
	public boolean isPinLockedOut()
	{
		return consecutiveAuthFailures != null && consecutiveAuthFailures < 0;
	}

	@Override
	public AgentUser setPinLockedOut(boolean pinLockedOut)
	{
		this.pinLockedOut = isPinLockedOut();
		return this;
	}

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "agent_id", foreignKey = @ForeignKey(name = "FK_AgentUser_Agent"))
	public Agent getAgent()
	{
		return agent;
	}

	public AgentUser setAgent(Agent agent)
	{
		this.agent = agent;
		return this;
	}

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "FK_AgentUser_Role"))
	public Role getRole()
	{
		return role;
	}

	public AgentUser setRole(Role role)
	{
		this.role = role;
		return this;
	}

	@Override
	@Column(name = "pin_version", nullable = false)
	public int getPinVersion()
	{
		return pinVersion;
	}

	@Override
	public AgentUser setPinVersion(int pinVersion)
	{
		this.pinVersion = pinVersion;
		return this;
	}

	@Column(name = "last_imei_update", nullable = true)
	public Date getLastImeiUpdate()
	{
		return lastImeiUpdate;
	}

	public AgentUser setLastImeiUpdate(Date lastImeiUpdate)
	{
		this.lastImeiUpdate = lastImeiUpdate;
		return this;
	}

	public boolean isImeiRecent(long refreshIntervalMinutes)
	{
		boolean result = false;
		Date now = new Date();
		long refreshIntervalSeconds = refreshIntervalMinutes * 60000L;
		result = (lastImeiUpdate != null && (now.getTime() - lastImeiUpdate.getTime()) <= refreshIntervalSeconds);
		return result;
	}

	// Location Caching
	@Override
	@Column(name = "last_cell_id", nullable = true, insertable = false, updatable = false)
	public Integer getLastCellID()
	{
		return lastCellID;
	}

	@Override
	public AgentUser setLastCellID(Integer lastCellID)
	{
		this.lastCellID = lastCellID;
		return this;
	}

	@Override
	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "last_cell_id", foreignKey = @ForeignKey(name = "FK_AgentUser_Cell"))
	public Cell getLastCell()
	{
		return lastCell;		
	}

	@Override
	public AgentUser setLastCell(Cell lastCell)
	{
		this.lastCell = lastCell;
		return this;
	}

	@Override
	@Column(name = "last_cell_expires", nullable = true)
	public Date getLastCellExpiryTime()
	{
		return lastCellExpiryTime;
	}

	@Override
	public AgentUser setLastCellExpiryTime(Date lastCellExpiryTime)
	{
		this.lastCellExpiryTime = lastCellExpiryTime;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public AgentUser()
	{

	}

	public AgentUser(AgentUser agent)
	{
		this.lastUserID = agent.lastUserID;
		this.lastTime = agent.lastTime;
		this.pinVersion = agent.pinVersion;
		amend(agent);
	}

	public void amend(hxc.ecds.protocol.rest.AgentUser agent)
	{
		this.id = agent.getId();
		this.companyID = agent.getCompanyID();
		this.version = agent.getVersion();
		this.accountNumber = agent.getAccountNumber();
		this.mobileNumber = agent.getMobileNumber();
		this.title = agent.getTitle();
		this.firstName = agent.getFirstName();
		this.initials = agent.getInitials();
		this.surname = agent.getSurname();
		this.language = agent.getLanguage();
		this.domainAccountName = agent.getDomainAccountName();
		this.state = agent.getState();
		this.activationDate = agent.getActivationDate();
		this.deactivationDate = agent.getDeactivationDate();
		this.expirationDate = agent.getExpirationDate();
		this.allowedChannels = agent.getAllowedChannels();
		this.temporaryPin = agent.isTemporaryPin();
		this.imei = agent.getImei();
		this.imsi = agent.getImsi();
		this.department = agent.getDepartment();
		this.email = agent.getEmail();
		this.agentID = agent.getAgentID();
		this.roleID = agent.getRoleID();
		this.imsiLockedOut = agent.isImsiLockedOut();
		this.pinLockedOut = agent.isPinLockedOut();
		this.authenticationMethod = agent.getAuthenticationMethod();
		this.consecutiveAuthFailures = agent.getConsecutiveAuthFailures();
		this.channelType = agent.getChannelType();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Minimum Required Data
	//
	// /////////////////////////////////
	public static void loadMRD(EntityManager em, int companyID, Session session) throws RuleCheckException
	{
		Permission.loadMRD(em, MAY_UPDATE, session);
		Permission.loadMRD(em, MAY_ADD, session);
		Permission.loadMRD(em, MAY_DELETE, session);
		Permission.loadMRD(em, MAY_VIEW, session);
		Permission.loadMRD(em, MAY_RESET_IMSI_LOCK, session);
		Permission.loadMRD(em, MAY_RESET_PIN, session);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Finders
	//
	// /////////////////////////////////
	public static AgentUser findByID(EntityManager em, int agentUserID, int companyID)
	{
		TypedQuery<AgentUser> query = em.createNamedQuery("AgentUser.findByID", AgentUser.class);
		query.setParameter("id", agentUserID);
		query.setParameter("companyID", companyID);
		List<AgentUser> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static AgentUser findByAccountNumber(EntityManager em, String accountNumber, int companyID)
	{
		TypedQuery<AgentUser> query = em.createNamedQuery("AgentUser.findByAccountNo", AgentUser.class);
		query.setParameter("accountNumber", accountNumber);
		query.setParameter("companyID", companyID);
		List<AgentUser> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static AgentUser findByMSISDN(EntityManager em, String msisdn, int companyID)
	{
		TypedQuery<AgentUser> query = em.createNamedQuery("AgentUser.findByMSISDN", AgentUser.class);
		query.setParameter("msisdn", msisdn);
		query.setParameter("companyID", companyID);
		List<AgentUser> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static AgentUser findByMSISDNWithAgent(EntityManager em, String msisdn, int companyID)
	{
		TypedQuery<AgentUser> query = em.createNamedQuery("AgentUser.findByMSISDNWithAgent", AgentUser.class);
		query.setParameter("msisdn", msisdn);
		query.setParameter("companyID", companyID);
		List<AgentUser> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static AgentUser findByDomainAccountName(EntityManager em, int companyID, String domainAccountName)
	{
		TypedQuery<AgentUser> query = em.createNamedQuery("AgentUser.findByDomainAccountName", AgentUser.class);
		query.setParameter("companyID", companyID);
		query.setParameter("domainAccountName", domainAccountName);
		List<AgentUser> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static AgentUser findByDomainAccountNameWithAgent(EntityManager em, int companyID, String domainAccountName)
	{
		TypedQuery<AgentUser> query = em.createNamedQuery("AgentUser.findByDomainAccountNameWithAgent", AgentUser.class);
		query.setParameter("companyID", companyID);
		query.setParameter("domainAccountName", domainAccountName);
		List<AgentUser> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<AgentUser> findAll(EntityManager em, RestParams params, int companyID)
	{
		return QueryBuilder.getQueryResultList(em, AgentUser.class, params, companyID, "accountNumber", "mobileNumber", "imsi", "firstName", "surname", "domainAccountName");
	}
	///---
	public static List<AgentUser> findAgentUsers(EntityManager em, RestParams params, int companyID, int agentId)
	{
		TypedQuery<AgentUser> query = em.createNamedQuery("AgentUser.findAgentUsers", AgentUser.class);
		query.setParameter("companyID", companyID);
		query.setParameter("agentID", agentId);
		
		List<AgentUser> results = query.getResultList();
		return results;
	}
	
	public static List<AgentUser> findApiUsers(EntityManager em, RestParams params, int companyID, int agentId)
	{
		TypedQuery<AgentUser> query = em.createNamedQuery("AgentUser.findApiUsers", AgentUser.class);
		query.setParameter("companyID", companyID);
		query.setParameter("agentID", agentId);
		List<AgentUser> results = query.getResultList();
		return results;
	}
	///---
	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, AgentUser.class, params, companyID, "accountNumber", "mobileNumber", "imsi", "firstName", "surname", "domainAccountName");
		return query.getSingleResult();
	}

	public static List<AgentUser> findMine(EntityManager em, RestParams params, int companyID, int myID)
	{
		AgentExtender px = new AgentUser.AgentExtender(myID);
		return QueryBuilder.getQueryResultList(em, AgentUser.class, params, companyID, px, "accountNumber", "mobileNumber", "imsi", "firstName", "surname", "domainAccountName");
	}

	public static Long findMyCount(EntityManager em, RestParams params, int companyID, int myID)
	{
		AgentExtender px = new AgentUser.AgentExtender(myID);
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, AgentUser.class, params, companyID, px, "accountNumber", "mobileNumber", "imsi", "firstName", "surname", "domainAccountName");
		return query.getSingleResult();
	}

	public static AgentUser findRoot(EntityManager em, int companyID)
	{
		TypedQuery<AgentUser> query = em.createNamedQuery("AgentUser.findRoot", AgentUser.class);
		query.setParameter("companyID", companyID);
		List<AgentUser> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static boolean referencesServiceClass(EntityManager em, int serviceClassID)
	{
		TypedQuery<AgentUser> query = em.createNamedQuery("AgentUser.referenceServiceClass", AgentUser.class);
		query.setParameter("serviceClassID", serviceClassID);
		query.setMaxResults(1);
		List<AgentUser> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesGroup(EntityManager em, int groupID)
	{
		TypedQuery<AgentUser> query = em.createNamedQuery("AgentUser.referenceGroup", AgentUser.class);
		query.setParameter("groupID", groupID);
		query.setMaxResults(1);
		List<AgentUser> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesTier(EntityManager em, int tierID)
	{
		TypedQuery<AgentUser> query = em.createNamedQuery("AgentUser.referenceTier", AgentUser.class);
		query.setParameter("tierID", tierID);
		query.setMaxResults(1);
		List<AgentUser> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesAgent(EntityManager em, int agentID)
	{
		TypedQuery<AgentUser> query = em.createNamedQuery("AgentUser.referenceAgent", AgentUser.class);
		query.setParameter("agentID", agentID);
		query.setMaxResults(1);
		List<AgentUser> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	// Add Default Account Number
	public void autoNumber(EntityManager em, int companyID)
	{
		if (accountNumber == null || accountNumber.isEmpty() || accountNumber.equals(AgentUser.AUTO_NUMBER))
		{
			while (true)
			{
				String nextAccountNumber = String.format("D%d", State.getNextNumber(em, companyID, AgentUser.NEXT_USER_AGENT_ACC_NO, 1));
				AgentUser agent = findByAccountNumber(em, nextAccountNumber, companyID);
				if (agent != null)
					continue;
				accountNumber = nextAccountNumber;
				break;
			}
		}
	}





	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ICompanyData
	//
	// /////////////////////////////////
	@Override
	public void persist(EntityManager em, AgentUser oldValue, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		// Set (De)Activation Date
		if (STATE_ACTIVE.equals(state) && (oldValue == null || !STATE_ACTIVE.equals(oldValue.state)))
			activationDate = new Date();
		if (STATE_DEACTIVATED.equals(state) && (oldValue == null || !STATE_DEACTIVATED.equals(oldValue.state)))
			deactivationDate = new Date();

		validate(oldValue);
		boolean isNew = id == 0;
		if (!isNew)
		{
			QueryBuilder.merge(em, oldValue, this, session, AuditEntry.TYPE_AGENT_USER, auditEntryContext);
			return;
		}
		else
		{
			try (RequiresTransaction transaction = new RequiresTransaction(em))
			{
				// Persist AgentUser
				QueryBuilder.persist(em, oldValue, this, session, AuditEntry.TYPE_AGENT_USER, auditEntryContext);
				transaction.commit();

			}
		}
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		try (RequiresTransaction transaction = new RequiresTransaction(em))
		{
			QueryBuilder.remove(em, this, session, AuditEntry.TYPE_AGENT_USER, auditEntryContext);

			transaction.commit();
		}
	}

	@Override
	public void validate(AgentUser oldValue) throws RuleCheckException
	{
		RuleCheck.validate(this);

		RuleCheck.notNull("roleID", role);
		RuleCheck.equals("roleID", roleID, role.getId());
		RuleCheck.notNull("agentID", agent);
		RuleCheck.equals("agentID", agentID, agent.getId());

		//The following code causes a hibernate lazy initialization error.
		//This seems like a pointless validation check anyway, An AgentUser type is passed in, so we test if its data attribute is equal to Agent?
		//I don't see how we would get in here if oldValue wasn't an AgentUser. This role check seems redundant.
		/*if (!hxc.ecds.protocol.rest.Role.TYPE_AGENT.equals(role.getType()))
			throw new RuleCheckException(StatusCode.INVALID_VALUE, "roleID", "Invalid Role Type %s", role.getType());*/

		RuleCheck.notLonger("key1", key1, KEY_MAX_LENGTH);
		RuleCheck.notLonger("key2", key2, KEY_MAX_LENGTH);
		RuleCheck.notLonger("key3", key3, KEY_MAX_LENGTH);
		RuleCheck.notLonger("key4", key4, KEY_MAX_LENGTH);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Classes
	//
	// /////////////////////////////////
	static class AgentExtender extends PredicateExtender<AgentUser>
	{
		private int myID;

		public AgentExtender(int myID)
		{
			this.myID = myID;
		}

		@Override
		public String getName()
		{
			return "MyUsers";
		}

		@Override
		public List<Predicate> extend(CriteriaBuilder cb, Root<AgentUser> root, CriteriaQuery<?> query, List<Predicate> predicates)
		{
			Predicate p1 = cb.equal(col(root, "id"), cb.parameter(Integer.class, "myID"));
			Predicate p2 = cb.isNotNull(col(root, "agentID"));
			Predicate p3 = cb.equal(col(root, "agentID"), cb.parameter(Integer.class, "myID"));

			predicates.add(cb.or(p1, cb.and(p2, p3)));

			return predicates;
		}

		@Override
		public void addParameters(TypedQuery<?> query)
		{
			query.setParameter("myID", myID);
		}

	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IAuthenticable
	//
	// /////////////////////////////////
	@Override
	public byte[] validateNewPin(EntityManager em, CompanyInfo company, String newPin) throws RuleCheckException
	{
		return AgentUsers.validateNewPin(em, company, this, newPin);
	}
	
	@Override
	public boolean testIfSamePin(String pin)
	{
		return AuthenticationHelper.testIfSamePin(this, pin);
	}
	
	@Override
	public String offerPIN(EntityManager em, Session session, CompanyInfo company, String pin) throws RuleCheckException
	{
		int attempts = getConsecutiveAuthFailures() == null ? 0 : getConsecutiveAuthFailures();
		if (attempts < 0)
			return TransactionsConfig.ERR_PIN_LOCKOUT;

		// Test if Same
		boolean same = testIfSamePin(pin);
		if (same && attempts == 0)
			return null;

		// Update AgentUser
		AgentUser original = new AgentUser(this);
		if (same)
			setConsecutiveAuthFailures(0);
		else
		{
			attempts++;
			AgentsConfig config = company.getConfiguration(em, AgentsConfig.class);
			if (attempts >= config.getMaxPinRetriesBeforeLockout())
				attempts = -1;
			setConsecutiveAuthFailures(attempts);
		}
		AuditEntryContext auditEntryContext = new AuditEntryContext("AGENTUSER_AUTH", this.getId());
		persist(em, original, session, auditEntryContext);

		// Return result
		if (attempts < 0)
			return TransactionsConfig.ERR_PIN_LOCKOUT;
		else if (!same)
			return TransactionsConfig.ERR_INVALID_PIN;
		else
			return null;
	}
	
	@Override
	public void updatePin(EntityManager em, byte[] key, Session session) throws RuleCheckException
	{
		AgentUser oldAgentUser = new AgentUser(this);
		AuthenticationHelper.updatePin(this, em, key, session);
		AuditEntryContext auditEntryContext = new AuditEntryContext("AGENTUSER_PIN_CHANGE", this.getId());
		persist(em, oldAgentUser, session, auditEntryContext);
	}
		

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IAgentUser
	//
	// /////////////////////////////////
	@Override
	public void validateAgentImsi(ICreditDistribution context, EntityManager em, TransactionsConfig transactionsConfig, Session session) throws RuleCheckException
	{
		AgentUsers.validateAgentUserImsi(context, em, transactionsConfig, session, this);
	}

	@Override
	public void updateAgentImei(ICreditDistribution context, EntityManager em, TransactionsConfig transactionsConfig, Session session) throws RuleCheckException
	{
		AgentUsers.updateAgentUserImei(context, em, transactionsConfig, session, this);
	}

}
