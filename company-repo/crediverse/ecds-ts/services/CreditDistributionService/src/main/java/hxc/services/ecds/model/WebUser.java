package hxc.services.ecds.model;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.ecds.protocol.rest.config.WebUsersConfig;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.RestParams;
import hxc.services.ecds.rest.WebUsers;
import hxc.services.ecds.rest.batch.IBatchEnabled;
import hxc.services.ecds.util.AuthenticationHelper;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.QueryBuilder;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

////////////////////////////////////////////////////////////////////////////////////////
//
// WebUser Table - Contains Web User Details
//
///////////////////////////////////

@Entity
@javax.persistence.Access(javax.persistence.AccessType.PROPERTY)
@Table(name = "es_webuser", uniqueConstraints = { //
		@UniqueConstraint(name = "es_webuser_acc_no", columnNames = { "comp_id", "acc_no" }), //
		@UniqueConstraint(name = "es_webuser_domain_name", columnNames = { "comp_id", "domain_name" }) })
@NamedQueries({ //
		@NamedQuery(name = "WebUser.findByID", query = "SELECT u FROM WebUser u where id = :id and companyID = :companyID "),
		@NamedQuery(name = "WebUser.findByAccountNo", query = "SELECT u FROM WebUser u where companyID = :companyID and accountNumber = :accountNumber"),
		@NamedQuery(name = "WebUser.findByDomainAccountName", query = "SELECT u FROM WebUser u where companyID = :companyID and domainAccountName = :domainAccountName"), //
		@NamedQuery(name = "WebUser.findWithPermission", query = "SELECT u FROM WebUser u join u.roles r join r.permissions p where u.companyID = :companyID and p.id = :permissionID and u.state != '"
				+ hxc.ecds.protocol.rest.WebUser.STATE_PERMANENT + "'"), //
		@NamedQuery(name = "WebUser.referenceDepartment", query = "SELECT p FROM WebUser p where department.id = :departmentID"), //
		@NamedQuery(name = "WebUser.findAllNonServiceUsers", query = "SELECT p FROM WebUser p where companyID = :companyID " +
				" AND (serviceUser IS NULL OR serviceUser!=1)"),
		@NamedQuery(name = "WebUser.findNonServiceUsersCount", query = "SELECT COUNT(p) FROM WebUser p where companyID = :companyID " +
				" AND (serviceUser IS NULL OR serviceUser!=1)"),

})
public class WebUser extends hxc.ecds.protocol.rest.WebUser implements Serializable, ICompanyData<WebUser>, IBatchEnabled<WebUser>, IAuthenticatable
{
	final static Logger logger = LoggerFactory.getLogger(WebUser.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	public static final String LANGUAGE_ENGLISH = "en";
	public static final String LANGUAGE_FRENCH = "fr";

	public static final String ACCOUNT_NO_ADMINISTRATOR = "Admin";
	public static final String NAME_ADMINISTRATOR = "ecds.admin";

	public static final String ACCOUNT_NO_SUPPLIER = "HxC";
	public static final String NAME_SUPPLIER = "Supplier";

	private static final long serialVersionUID = 3153139683521085905L;

	public static final Permission MAY_ADD = new Permission(false, false, Permission.GROUP_WEBUSERS, Permission.PERM_ADD, "May Add Web-Users");
	public static final Permission MAY_UPDATE = new Permission(false, false, Permission.GROUP_WEBUSERS, Permission.PERM_UPDATE, "May Update Web-Users");
	public static final Permission MAY_DELETE = new Permission(false, false, Permission.GROUP_WEBUSERS, Permission.PERM_DELETE, "May Delete Web-Users");
	public static final Permission MAY_CONFIGURE = new Permission(false, false, Permission.GROUP_WEBUSERS, Permission.PERM_CONFIGURE, "May Configure Web-Users");
	public static final Permission MAY_VIEW = new Permission(false, false, Permission.GROUP_WEBUSERS, Permission.PERM_VIEW, "May View Web-Users");
	public static final Permission MAY_UPDATE_OWN = new Permission(false, true, Permission.GROUP_WEBUSERS, Permission.PERM_UPDATE_OWN, "May Update Own Profile");
	public static final Permission MAY_RESET_PASSWORDS = new Permission(false, true, Permission.GROUP_WEBUSERS, Permission.PERM_RESET_PASSWORDS, "May Reset Web-User's Passwords");

	private static final String INITIAL_DEPARTMENT = "-";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	@JsonIgnore
	protected int lastUserID;

	@JsonIgnore
	protected Date lastTime;

	@JsonIgnore
	protected Department department;

	@JsonIgnore
	protected byte[] key3;

	@JsonIgnore
	protected byte[] key4;

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
	public WebUser setId(int id)
	{
		super.id = id;
		return this;
	}

	@Override
	@Column(name = "comp_id", nullable = false, unique = false)
	public int getCompanyID()
	{
		return companyID;
	}

	@Override
	public WebUser setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Override
	@Column(name = "title", nullable = false, unique = false, length = TITLE_MAX_LENGTH)
	public String getTitle()
	{
		return title;
	}

	@Override
	public WebUser setTitle(String title)
	{
		this.title = title;
		return this;
	}

	@Override
	@Column(name = "first_name", nullable = false, unique = false, length = FIRST_NAME_MAX_LENGTH)
	public String getFirstName()
	{
		return firstName;
	}

	@Override
	public WebUser setFirstName(String firstName)
	{
		this.firstName = firstName;
		return this;
	}

	@Override
	@Column(name = "last_name", nullable = false, unique = false, length = LAST_NAME_MAX_LENGTH)
	public String getSurname()
	{
		return surname;
	}

	@Override
	public WebUser setSurname(String surname)
	{
		this.surname = surname;
		return this;
	}

	@Override
	@Column(name = "initials", nullable = false, unique = false, length = INITIALS_MAX_LENGTH)
	public String getInitials()
	{
		return initials;
	}

	@Override
	public WebUser setInitials(String initials)
	{
		this.initials = initials;
		return this;
	}

	@Override
	@Column(name = "msisdn", nullable = false, unique = false, length = MOBILE_NUMBER_MAX_LENGTH)
	public String getMobileNumber()
	{
		return mobileNumber;
	}

	@Override
	public WebUser setMobileNumber(String mobileNumber)
	{
		this.mobileNumber = mobileNumber;
		return this;
	}

	@Override
	@Column(name = "email", nullable = true, unique = false, length = EMAIL_MAX_LENGTH)
	public String getEmail()
	{
		return email;
	}

	@Override
	public WebUser setEmail(String email)
	{
		this.email = email;
		return this;
	}

	@Override
	@Column(name = "acc_no", nullable = true, length = ACCOUNT_NO_MAX_LENGTH)
	public String getAccountNumber()
	{
		return accountNumber;
	}

	@Override
	public WebUser setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
		return this;
	}

	@Override
	@Column(name = "domain_name", nullable = false, length = DOMAIN_NAME_MAX_LENGTH)
	public String getDomainAccountName()
	{
		return domainAccountName;
	}

	@Override
	public WebUser setDomainAccountName(String domainAccountName)
	{
		this.domainAccountName = domainAccountName;
		return this;
	}

	@Override
	@Column(name = "lang", nullable = false, unique = false, length = LANGUAGE_MAX_LENGTH)
	public String getLanguage()
	{
		return language;
	}

	@Override
	public WebUser setLanguage(String language)
	{
		this.language = language;
		return this;
	}

	@Override
	@Column(name = "dept_id", nullable = false, insertable = false, updatable = false)
	public int getDepartmentID()
	{
		return departmentID;
	}

	@Override
	public WebUser setDepartmentID(int departmentID)
	{
		this.departmentID = departmentID;
		return this;
	}

	@Override
	@Column(name = "state", nullable = false, unique = false, length = 1)
	public String getState()
	{
		return state;
	}

	@Override
	public WebUser setState(String state)
	{
		this.state = state;
		return this;
	}

	@Override
	@Column(name = "a_date", nullable = true, unique = false)
	@Temporal(TemporalType.DATE)
	public Date getActivationDate()
	{
		return activationDate;
	}

	@Override
	public WebUser setActivationDate(Date activationDate)
	{
		this.activationDate = activationDate;
		return this;
	}

	@Override
	@Column(name = "d_date", nullable = true, unique = false)
	@Temporal(TemporalType.DATE)
	public Date getDeactivationDate()
	{
		return deactivationDate;
	}

	@Override
	public WebUser setDeactivationDate(Date deactivationDate)
	{
		this.deactivationDate = deactivationDate;
		return this;
	}

	@Override
	@Column(name = "e_date", nullable = true, unique = false)
	@Temporal(TemporalType.DATE)
	public Date getExpirationDate()
	{
		return expirationDate;
	}

	@Override
	public WebUser setExpirationDate(Date expirationDate)
	{
		this.expirationDate = expirationDate;
		return this;
	}

	@Override
	@Column(name = "temp_pin", nullable = false)
	public boolean isTemporaryPin()
	{
		return temporaryPin;
	}

	@Override
	public WebUser setTemporaryPin(boolean temporaryPin)
	{
		this.temporaryPin = temporaryPin;
		return this;
	}

	@Column(name = "service_user")
	public Boolean getServiceUser() {
		return serviceUser;
	}

	@Override
	@Transient
	public boolean isPinLockedOut()
	{
		return consecutiveAuthFailures != null && consecutiveAuthFailures < 0;
	}

	@Override
	public WebUser setPinLockedOut(boolean pinLockedOut)
	{
		this.pinLockedOut = isPinLockedOut();
		return this;
	}

	@Override
	@Column(name = "pin_version", nullable = false)
	public int getPinVersion()
	{
		return pinVersion;
	}

	@Override
	public WebUser setPinVersion(int pinVersion)
	{
		this.pinVersion = pinVersion;
		return this;
	}

	@Override
	@Column(name = "auth_method", nullable = false, unique = false, length = 1)
	public String getAuthenticationMethod()
	{
		return authenticationMethod;
	}

	@Override
	public WebUser setAuthenticationMethod(String authenticationMethod)
	{
		this.authenticationMethod = authenticationMethod;
		return this;
	}

	@Column(name = "attempts", nullable = true)
	public Integer getConsecutiveAuthFailures()
	{
		return consecutiveAuthFailures;
	}

	public WebUser setConsecutiveAuthFailures(Integer consecutiveAuthFailures)
	{
		this.consecutiveAuthFailures = consecutiveAuthFailures;
		return this;
	}

	@Override
	@Column(name = "key1", nullable = true, unique = false, length = KEY_MAX_LENGTH)
	public byte[] getKey1()
	{
		return key1;
	}

	@Override
	public WebUser setKey1(byte[] key1)
	{
		this.key1 = key1;
		return this;
	}

	@Override
	@Column(name = "key2", nullable = true, unique = false, length = KEY_MAX_LENGTH)
	public byte[] getKey2()
	{
		return key2;
	}

	@Override
	public WebUser setKey2(byte[] key2)
	{
		this.key2 = key2;
		return this;
	}

	@Column(name = "key3", nullable = true, unique = false, length = KEY_MAX_LENGTH)
	public byte[] getKey3()
	{
		return key3;
	}

	public WebUser setKey3(byte[] key3)
	{
		this.key3 = key3;
		return this;
	}

	@Column(name = "key4", nullable = true, unique = false, length = KEY_MAX_LENGTH)
	public byte[] getKey4()
	{
		return key4;
	}

	public WebUser setKey4(byte[] key4)
	{
		this.key4 = key4;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public WebUser setLastUserID(int lastUserID)
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
	public WebUser setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	@Override
	@Version
	public int getVersion()
	{
		return version;
	}

	@Override
	public WebUser setVersion(int version)
	{
		super.version = version;
		return this;
	}

	@Override
	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
	@JoinTable(name = "es_webuser_role", joinColumns = { @JoinColumn(name = "webuser_id") }, inverseJoinColumns = { @JoinColumn(name = "role_id") })
	@SuppressWarnings({ "unchecked" })
	public List<Role> getRoles()
	{
		return (List<Role>) roles;
	}

	@Override
	public void setRoles(List<? extends hxc.ecds.protocol.rest.Role> roles)
	{
		super.setRoles(roles);
	}

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "dept_id", foreignKey = @ForeignKey(name = "FK_User_Dept"))
	public Department getDepartment()
	{
		return department;
	}

	public WebUser setDepartment(Department department)
	{
		this.department = department;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Cloning
	//
	// /////////////////////////////////

	// Make deep copy
	public WebUser copy(EntityManager em) throws RuleCheckException
	{
		WebUser copy = new WebUser();
		copy.lastUserID = this.lastUserID;
		copy.lastTime = this.lastTime;
		copy.amend(em, this);
		return copy;
	}

	// Amend selected fields
	@SuppressWarnings({ "unchecked" })
	public void amend(EntityManager em, hxc.ecds.protocol.rest.WebUser webUser) throws RuleCheckException
	{
		this.id = webUser.getId();
		this.companyID = webUser.getCompanyID();
		this.version = webUser.getVersion();
		this.title = webUser.getTitle();
		this.firstName = webUser.getFirstName();
		this.surname = webUser.getSurname();
		this.initials = webUser.getInitials();
		this.mobileNumber = webUser.getMobileNumber();
		this.email = webUser.getEmail();
		this.accountNumber = webUser.getAccountNumber();
		this.domainAccountName = webUser.getDomainAccountName();
		this.language = webUser.getLanguage();
		this.departmentID = webUser.getDepartmentID();
		this.state = webUser.getState();
		this.activationDate = webUser.getActivationDate();
		this.deactivationDate = webUser.getDeactivationDate();
		this.expirationDate = webUser.getExpirationDate();
		this.key1 = webUser.getKey1();
		this.key2 = webUser.getKey2();
		this.temporaryPin = webUser.isTemporaryPin();
		this.pinVersion = webUser.getPinVersion();
		this.authenticationMethod = webUser.getAuthenticationMethod();
		this.consecutiveAuthFailures = webUser.getConsecutiveAuthFailures();

		// Add new Roles
		List<hxc.ecds.protocol.rest.Role> newRoles = (List<hxc.ecds.protocol.rest.Role>) webUser.getRoles();
		List<Role> existingRoles = getRoles();
		if (newRoles != null)
		{
			for (hxc.ecds.protocol.rest.Role newRole : newRoles)
			{
				if (!contains(existingRoles, newRole))
				{
					Role role = Role.findByID(em, newRole.getId(), companyID);
					if (role == null)
						throw new RuleCheckException(StatusCode.INVALID_VALUE, "roles.id", "Invalid RoleID %d", newRole.getId());
					existingRoles.add(role);
				}
			}

			// Remove unused Roles
			int index = 0;
			while (index < existingRoles.size())
			{
				if (!contains(newRoles, existingRoles.get(index)))
					existingRoles.remove(index);
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

	@Override
	public String toString()
	{
		return String.format("%s %s", firstName, surname);
	}

	public static WebUser findByID(EntityManager em, int id, int companyID)
	{
		TypedQuery<WebUser> query = em.createNamedQuery("WebUser.findByID", WebUser.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<WebUser> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static WebUser findByAccountNo(EntityManager em, int companyID, String accountNumber)
	{
		TypedQuery<WebUser> query = em.createNamedQuery("WebUser.findByAccountNo", WebUser.class);
		query.setParameter("companyID", companyID);
		query.setParameter("accountNumber", accountNumber);
		List<WebUser> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static WebUser findByDomainAccountName(EntityManager em, int companyID, String domainAccountName)
	{
		TypedQuery<WebUser> query = em.createNamedQuery("WebUser.findByDomainAccountName", WebUser.class);
		query.setParameter("companyID", companyID);
		query.setParameter("domainAccountName", domainAccountName);
		List<WebUser> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<WebUser> findAll(EntityManager em, RestParams params, int companyID)
	{
		return QueryBuilder.getQueryResultList(em, WebUser.class, params, companyID, //
				"accountNumber", "domainAccountName", "firstName", "surname", "mobileNumber", "email");
	}

	public static List<WebUser> findAllNonServiceUsers(EntityManagerEx em, RestParams params, int companyID) {
		TypedQuery<WebUser> query = em.createNamedQuery("WebUser.findAllNonServiceUsers", WebUser.class);
		query.setParameter("companyID", companyID);
		if (params.getFirst() > 0)
			query.setFirstResult(params.getFirst());
		query.setMaxResults(params.getMax() >= 0 ? params.getMax() : RestParams.DEFAULT_MAX_RESULTS);
		return query.getResultList();
	}
	public static long findNonServiceUsersCount(EntityManagerEx em, int companyID) {
		TypedQuery<Long> query = em.createNamedQuery("WebUser.findNonServiceUsersCount", Long.class);
		query.setParameter("companyID", companyID);
		return query.getSingleResult();
	}

	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, WebUser.class, params, companyID, //
				"accountNumber", "domainAccountName", "firstName", "surname", "mobileNumber", "email");
		return query.getSingleResult();
	}

	public static List<WebUser> findWithPermission(EntityManager em, Integer permissionID, int companyID)
	{
		TypedQuery<WebUser> query = em.createNamedQuery("WebUser.findWithPermission", WebUser.class);
		query.setParameter("companyID", companyID);
		query.setParameter("permissionID", permissionID);
		List<WebUser> results = query.getResultList();
		return results;
	}

	public static boolean referencesDepartment(EntityManager em, int departmentID)
	{
		TypedQuery<WebUser> query = em.createNamedQuery("WebUser.referenceDepartment", WebUser.class);
		query.setParameter("departmentID", departmentID);
		query.setMaxResults(1);
		List<WebUser> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	// Add Default Account Number
	public void autoNumber(EntityManager em, int companyID)
	{
		if (accountNumber == null || accountNumber.isEmpty() || accountNumber.equals(Agent.AUTO_NUMBER))
		{
			while (true)
			{
				String nextAccountNumber = String.format("U%d", State.getNextNumber(em, companyID, WebUser.NEXT_USER_ACC_NO, 1));
				WebUser user = findByAccountNo(em, companyID, nextAccountNumber);
				if (user != null)
					continue;
				accountNumber = nextAccountNumber;
				break;
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// MRD
	//
	// /////////////////////////////////

	public static void loadMRD(EntityManager em, Session session) throws RuleCheckException
	{
		Permission.loadMRD(em, MAY_ADD, session);
		Permission.loadMRD(em, MAY_UPDATE, session);
		Permission.loadMRD(em, MAY_DELETE, session);
		Permission.loadMRD(em, MAY_CONFIGURE, session);
		Permission.loadMRD(em, MAY_VIEW, session);
		Permission.loadMRD(em, MAY_UPDATE_OWN, session);
		Permission.loadMRD(em, MAY_RESET_PASSWORDS, session);
	}

	public static void loadMRD(EntityManager em, int companyID, Session session) throws RuleCheckException
	{
		// Needs Roles to be Initialised
		Role.loadMRD(em, companyID, session);

		// Default Password
		Date now = new Date();
		byte[] key1 = null;
		try
		{
			key1 = " $$4U".getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			logger.error("", e);
		}

		// Needs Administrator
		WebUser webUser = findByAccountNo(em, companyID, ACCOUNT_NO_ADMINISTRATOR);
		if (webUser == null)
		{
			webUser = new WebUser() //
					.setState(STATE_PERMANENT) //
					.setActivationDate(now) //
					.setAccountNumber(ACCOUNT_NO_ADMINISTRATOR) //
					.setCompanyID(companyID) //
					.setDeactivationDate(null) //
					.setDepartment(findInitialDepartment(em, companyID, session)) //
					.setExpirationDate(null) //
					.setLanguage(LANGUAGE_ENGLISH) //
					.setKey1(key1) //
					.setKey2(null) //
					.setDomainAccountName(NAME_ADMINISTRATOR) //
					.setEmail("") //
					.setFirstName("") //
					.setSurname(NAME_ADMINISTRATOR) //
					.setInitials("") //
					.setMobileNumber("") //
					.setTitle("");

			webUser.getRoles().add(Role.findByName(em, companyID, Role.ROLE_ADMINISTRATOR_NAME));
			AuditEntryContext auditContext = new AuditEntryContext("WEBUSER_LOAD_MRD_ADMIN");
			webUser.persist(em, null, session, auditContext);
		}

		// Needs Supplier
		webUser = findByAccountNo(em, companyID, ACCOUNT_NO_SUPPLIER);
		if (webUser == null)
		{
			webUser = new WebUser() //
					.setState(STATE_PERMANENT) //
					.setActivationDate(now) //
					.setAccountNumber(ACCOUNT_NO_SUPPLIER) //
					.setCompanyID(companyID) //
					.setDeactivationDate(null) //
					.setDepartment(findInitialDepartment(em, companyID, session)) //
					.setExpirationDate(null) //
					.setLanguage(LANGUAGE_ENGLISH) //
					.setKey1(key1) //
					.setKey2(null) //
					.setDomainAccountName(NAME_SUPPLIER) //
					.setEmail("") //
					.setFirstName("") //
					.setSurname(NAME_SUPPLIER) //
					.setInitials("") //
					.setMobileNumber("") //
					.setTitle(""); //

			webUser.getRoles().add(Role.findByName(em, companyID, Role.ROLE_SUPPLIER_NAME));
			AuditEntryContext auditContext = new AuditEntryContext("WEBUSER_LOAD_MRD_SUPPLIER");
			webUser.persist(em, null, session, auditContext);
		}

		State.loadMRD(em, session, companyID, NEXT_USER_ACC_NO, 1000L);
	}

	private static Department findInitialDepartment(EntityManager em, int companyID, Session session) throws RuleCheckException
	{
		Department department = Department.findByName(em, companyID, INITIAL_DEPARTMENT);
		if (department != null)
			return department;
		department = new Department() //
				.setCompanyID(companyID) //
				.setName(INITIAL_DEPARTMENT);
		AuditEntryContext auditContext = new AuditEntryContext("DEPARTMENT_CREATE_MRD");
		department.persist(em, null, session, auditContext);
		return department;
	}

	// Persist to Database
	@Override
	public void persist(EntityManager em, WebUser previous, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		// Set (De)Activation Date
		if (STATE_ACTIVE.equals(state) && (previous == null || !STATE_ACTIVE.equals(previous.state)))
			activationDate = new Date();
		if (STATE_DEACTIVATED.equals(state) && (previous == null || !STATE_DEACTIVATED.equals(previous.state)))
			deactivationDate = new Date();

		validate(previous);
		if(this.id != 0)
		{
			QueryBuilder.merge(em, previous, this, session, AuditEntry.TYPE_WEB_USER, auditEntryContext);
		} else {
			QueryBuilder.persist(em, previous, this, session, AuditEntry.TYPE_WEB_USER, auditEntryContext);
		}
	}

	// Remove from Database
	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		RuleCheck.isFalse(null, STATE_PERMANENT.equals(state), "Cannot delete Permanent WebUser");
		QueryBuilder.remove(em, this, session, AuditEntry.TYPE_WEB_USER, auditEntryContext);
	}

	// Validation
	@Override
	public void validate(WebUser previous) throws RuleCheckException
	{
		RuleCheck.validate(this);

		if (previous != null)
		{
			if (previous.state.equals(STATE_PERMANENT) && !state.equals(STATE_PERMANENT) || !previous.state.equals(STATE_PERMANENT) && state.equals(STATE_PERMANENT))
				throw new RuleCheckException(StatusCode.INVALID_VALUE, "state", "Cannot change permanency");
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IAuthenticable
	//
	// /////////////////////////////////
	@Override
	public byte[] validateNewPin(EntityManager em, CompanyInfo company, String newPassword) throws RuleCheckException
	{
		return WebUsers.validateNewPin(em, company, this, newPassword);
	}

	@Override
	public String offerPIN(EntityManager em, Session session, CompanyInfo company, String password) throws RuleCheckException
	{
		int attempts = getConsecutiveAuthFailures() == null ? 0 : getConsecutiveAuthFailures();
		if (attempts < 0)
			return TransactionsConfig.ERR_PASSWORD_LOCKOUT;

		// Test if Same
		boolean same = AuthenticationHelper.testIfSamePin(this, password);
		if (same && attempts == 0)
			return null;

		// Update WebUser
		WebUser original = WebUser.findByID(em, getId(), session.getCompanyID());
		if (original == null || getCompanyID() != session.getCompanyID())
			throw new RuleCheckException(StatusCode.NOT_FOUND, null, "WebUser %d not found", getId());
		original = original.copy(em);
		if (same)
			setConsecutiveAuthFailures(0);
		else
		{
			attempts++;
			WebUsersConfig config = company.getConfiguration(em, WebUsersConfig.class);
			if (attempts >= config.getMaxPinRetriesBeforeLockout())
				attempts = -1;
			setConsecutiveAuthFailures(attempts);
		}
		AuditEntryContext auditContext = new AuditEntryContext("WEBUSER_AUTH", this.getId());
		persist(em, original, session, auditContext);

		// Return result
		if (attempts < 0)
			return TransactionsConfig.ERR_PASSWORD_LOCKOUT;
		else if (!same)
			return TransactionsConfig.ERR_INVALID_PASSWORD;
		else
			return null;
	}

	@Override
	public void updatePin(EntityManager em, byte[] key, Session session) throws RuleCheckException
	{
		WebUser oldWebUser = this.copy(em);
		AuthenticationHelper.updatePin(this, em, key, session);
		AuditEntryContext auditContext = new AuditEntryContext("WEBUSER_PIN_CHANGE", this.getId());
		persist(em, oldWebUser, session, auditContext);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	private boolean contains(List<? extends hxc.ecds.protocol.rest.Role> roles, hxc.ecds.protocol.rest.Role role)
	{
		for (hxc.ecds.protocol.rest.Role rol : roles)
		{
			if (rol.getId() == role.getId())
				return true;
		}
		return false;
	}
	
	public boolean testIfSamePin(String pin) 
	{
		return AuthenticationHelper.testIfSamePin(this, pin);
	}

}
