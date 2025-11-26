package hxc.ecds.protocol.rest;

import java.util.Date;
import java.util.List;

//REST End-Point: ~/agent_users
public class AgentUser implements IValidatable, IAuthenticatable
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String STATE_ACTIVE = Agent.STATE_ACTIVE;
	public static final String STATE_DEACTIVATED = Agent.STATE_DEACTIVATED;

	public static final int TITLE_MAX_LENGTH = Agent.TITLE_MAX_LENGTH;
	public static final int FIRST_NAME_MAX_LENGTH = Agent.FIRST_NAME_MAX_LENGTH;
	public static final int LAST_NAME_MAX_LENGTH = Agent.LAST_NAME_MAX_LENGTH;
	public static final int INITIALS_MAX_LENGTH = Agent.INITIALS_MAX_LENGTH;
	public static final int PHONE_NUMBER_MAX_LENGTH = Agent.PHONE_NUMBER_MAX_LENGTH;
	public static final int ACCOUNT_NO_MAX_LENGTH = Agent.ACCOUNT_NO_MAX_LENGTH;
	public static final int DOMAIN_NAME_MAX_LENGTH = Agent.DOMAIN_NAME_MAX_LENGTH;
	public static final int LANGUAGE_MAX_LENGTH = Agent.LANGUAGE_MAX_LENGTH;
	public static final int KEY_MAX_LENGTH = Agent.KEY_MAX_LENGTH;
	public static final int IMSI_MAX_LENGTH = Agent.IMSI_MAX_LENGTH;
	public static final int IMEI_MAX_LENGTH = Agent.IMEI_MAX_LENGTH;
	public static final int DEPARTMENT_MAX_LENGTH = Department.NAME_MAX_LENGTH;
	public static final int EMAIL_MAX_LENGTH = Agent.EMAIL_MAX_LENGTH;

	public static final int ALLOWED_USSD = Agent.ALLOWED_USSD;
	public static final int ALLOWED_SMS = Agent.ALLOWED_SMS;
	public static final int ALLOWED_APP = Agent.ALLOWED_APP;
	public static final int ALLOWED_API = Agent.ALLOWED_API;
	public static final int ALLOWED_WUI = Agent.ALLOWED_WUI;
	public static final int ALLOWED_BATCH = Agent.ALLOWED_BATCH;
	public static final int ALLOWED_ALL = Agent.ALLOWED_ALL;

	public static final String AUTO_NUMBER = Agent.AUTO_NUMBER;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int id;
	protected int companyID;
	protected int version;

	protected String accountNumber = null;
	protected String mobileNumber;
	protected String imei;
	protected String imsi;
	protected String title;
	protected String firstName;
	protected String initials;
	protected String surname;
	protected String language;
	protected String domainAccountName;
	protected String state;
	protected Date activationDate;
	protected Date deactivationDate;
	protected Date expirationDate;
	protected int allowedChannels;
	protected String department;
	protected String email;
	protected int agentID;
	protected int roleID;
	protected String channelType;

	// Security
	protected boolean temporaryPin = true;
	protected boolean imsiLockedOut = false;
	protected boolean pinLockedOut = false;
	protected int pinVersion = 0;
	protected String authenticationMethod = AUTHENTICATE_PIN_2FACTOR;
	protected Integer consecutiveAuthFailures = 0;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public int getId()
	{
		return id;
	}

	public AgentUser setId(int id)
	{
		this.id = id;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public AgentUser setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public AgentUser setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getAccountNumber()
	{
		return accountNumber;
	}

	public AgentUser setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
		return this;
	}

	public String getMobileNumber()
	{
		return mobileNumber;
	}

	public AgentUser setMobileNumber(String mobileNumber)
	{
		this.mobileNumber = mobileNumber;
		return this;
	}

	public String getImei()
	{
		return imei;
	}

	public AgentUser setImei(String imei)
	{
		this.imei = imei;
		return this;
	}

	public String getImsi()
	{
		return imsi;
	}

	public AgentUser setImsi(String imsi)
	{
		this.imsi = imsi;
		return this;
	}

	public String getTitle()
	{
		return title;
	}

	public AgentUser setTitle(String title)
	{
		this.title = title;
		return this;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public AgentUser setFirstName(String firstName)
	{
		this.firstName = firstName;
		return this;
	}

	public String getInitials()
	{
		return initials;
	}

	public AgentUser setInitials(String initials)
	{
		this.initials = initials;
		return this;
	}

	public String getSurname()
	{
		return surname;
	}

	public AgentUser setSurname(String surname)
	{
		this.surname = surname;
		return this;
	}

	public String getLanguage()
	{
		return language;
	}

	public AgentUser setLanguage(String language)
	{
		this.language = language;
		return this;
	}

	public String getDomainAccountName()
	{
		return domainAccountName;
	}

	public AgentUser setDomainAccountName(String domainAccountName)
	{
		this.domainAccountName = domainAccountName;
		return this;
	}

	public String getState()
	{
		return state;
	}

	public AgentUser setState(String state)
	{
		this.state = state;
		return this;
	}

	public String getChannelType()
	{
		return channelType;
	}

	public AgentUser setChannelType(String channelType)
	{
		this.channelType = channelType;
		return this;
	}

	public Date getActivationDate()
	{
		return activationDate;
	}

	public AgentUser setActivationDate(Date activationDate)
	{
		this.activationDate = activationDate;
		return this;
	}

	public Date getDeactivationDate()
	{
		return deactivationDate;
	}

	public AgentUser setDeactivationDate(Date deactivationDate)
	{
		this.deactivationDate = deactivationDate;
		return this;
	}

	public Date getExpirationDate()
	{
		return expirationDate;
	}

	public AgentUser setExpirationDate(Date expirationDate)
	{
		this.expirationDate = expirationDate;
		return this;
	}

	public int getAllowedChannels()
	{
		return allowedChannels;
	}

	public AgentUser setAllowedChannels(int allowedChannels)
	{
		this.allowedChannels = allowedChannels;
		return this;
	}

	public boolean isTemporaryPin()
	{
		return temporaryPin;
	}

	public AgentUser setTemporaryPin(boolean temporaryPin)
	{
		this.temporaryPin = temporaryPin;
		return this;
	}

	public boolean isImsiLockedOut()
	{
		return imsiLockedOut;
	}

	public AgentUser setImsiLockedOut(boolean imsiLockedOut)
	{
		this.imsiLockedOut = imsiLockedOut;
		return this;
	}

	public boolean isPinLockedOut()
	{
		return pinLockedOut;
	}

	public AgentUser setPinLockedOut(boolean pinLockedOut)
	{
		this.pinLockedOut = pinLockedOut;
		return this;
	}

	public String getDepartment()
	{
		return department;
	}

	public AgentUser setDepartment(String department)
	{
		this.department = department;
		return this;
	}

	public String getEmail()
	{
		return email;
	}

	public AgentUser setEmail(String email)
	{
		this.email = email;
		return this;
	}

	public int getAgentID()
	{
		return agentID;
	}

	public AgentUser setAgentID(int agentID)
	{
		this.agentID = agentID;
		return this;
	}

	public int getRoleID()
	{
		return roleID;
	}

	public AgentUser setRoleID(int roleID)
	{
		this.roleID = roleID;
		return this;
	}	
	
	public int getPinVersion()
	{
		return pinVersion;
	}

	public AgentUser setPinVersion(int pinVersion)
	{
		this.pinVersion = pinVersion;
		return this;
	}	

	public String getAuthenticationMethod() 
	{
		return authenticationMethod;
	}

	public AgentUser setAuthenticationMethod(String authenticationMethod) 
	{
		this.authenticationMethod = authenticationMethod;
		return this;
	}

	public Integer getConsecutiveAuthFailures()
	{
		return consecutiveAuthFailures;
	}

	public AgentUser setConsecutiveAuthFailures(Integer consecutiveAuthFailures)
	{
		this.consecutiveAuthFailures = consecutiveAuthFailures;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public AgentUser()
	{

	}

	public AgentUser(AgentUser user)
	{
		this.id = user.id;
		this.companyID = user.companyID;
		this.version = user.version;
		this.accountNumber = user.accountNumber;
		this.mobileNumber = user.mobileNumber;
		this.imei = user.imei;
		this.imsi = user.imsi;
		this.title = user.title;
		this.firstName = user.firstName;
		this.initials = user.initials;
		this.surname = user.surname;
		this.language = user.language;
		this.domainAccountName = user.domainAccountName;
		this.state = user.state;
		this.activationDate = user.activationDate;
		this.deactivationDate = user.deactivationDate;
		this.expirationDate = user.expirationDate;
		this.allowedChannels = user.allowedChannels;
		this.temporaryPin = user.temporaryPin;
		this.imsiLockedOut = user.imsiLockedOut;
		this.pinLockedOut = user.pinLockedOut;
		this.email = user.email;
		this.department = user.department;
		this.roleID = user.roleID;
		this.agentID = user.agentID;
		this.channelType = user.channelType;
		// 	pinVersion left out
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Exported Business Rules
	//
	// /////////////////////////////////
	public static boolean mayAssignTo(Tier tier)
	{
		return tier != null && mayAssignTo(tier.getType());
	}

	public static boolean mayAssignTo(String tierType)
	{
		return Tier.TYPE_STORE.equals(tierType) || Tier.TYPE_WHOLESALER.equals(tierType) || Tier.TYPE_RETAILER.equals(tierType);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IValidatable
	//
	// /////////////////////////////////
	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator() //

				.notLess("companyID", companyID, 1) //
				.notEmpty("accountNumber", accountNumber, ACCOUNT_NO_MAX_LENGTH) //
				.notEmpty("mobileNumber", mobileNumber, PHONE_NUMBER_MAX_LENGTH) //
				.notLonger("imei", imei, IMEI_MAX_LENGTH) //
				.notLonger("imsi", imsi, IMSI_MAX_LENGTH) //
				.notLonger("title", title, TITLE_MAX_LENGTH) //
				.notEmpty("firstName", firstName, FIRST_NAME_MAX_LENGTH) //
				.notLonger("initials", initials, INITIALS_MAX_LENGTH) //
				.notEmpty("surname", surname, LAST_NAME_MAX_LENGTH) //
				.notEmpty("language", language, LANGUAGE_MAX_LENGTH) //
				.notLonger("domainAccountName", domainAccountName, DOMAIN_NAME_MAX_LENGTH) //
				.notLess("agentID", agentID, 1) //
				.notLess("roleID", roleID, 1) //
				.oneOf("authenticationMethod", authenticationMethod, AUTHENTICATE_PIN_2FACTOR, AUTHENTICATE_EXTERNAL_2FACTOR, AUTHENTICATE_PASSWORD_2FACTOR) //
				.notLonger("email", email, EMAIL_MAX_LENGTH) //
				.notLonger("department", department, DEPARTMENT_MAX_LENGTH) //
				.oneOf("state", state, STATE_ACTIVE, STATE_DEACTIVATED) //
				.notNull("allowedChannels", allowedChannels) //
		;

		return validator.toList();

	}

}
