package hxc.ecds.protocol.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WebUser implements IValidatable, IAuthenticatable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String STATE_ACTIVE = "A";
	public static final String STATE_SUSPENDED = "S";
	public static final String STATE_DEACTIVATED = "D";
	public static final String STATE_PERMANENT = "P";

	public static final int TITLE_MAX_LENGTH = 20;
	public static final int FIRST_NAME_MAX_LENGTH = 30;
	public static final int LAST_NAME_MAX_LENGTH = 30;
	public static final int INITIALS_MAX_LENGTH = 10;
	public static final int MOBILE_NUMBER_MAX_LENGTH = 30;
	public static final int EMAIL_MAX_LENGTH = 50;
	public static final int ACCOUNT_NO_MAX_LENGTH = 20;
	public static final int DOMAIN_NAME_MAX_LENGTH = 40;
	public static final int LANGUAGE_MAX_LENGTH = 2;
	public static final int KEY_MAX_LENGTH = 100;

	public static final String AUTO_NUMBER = "<Auto>";

	public static final String NEXT_USER_ACC_NO = "NextUserAccNo";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int id;
	protected int companyID;
	protected int version;
	protected String title;
	protected String firstName;
	protected String surname;
	protected String initials;
	protected String mobileNumber;
	protected String email;
	protected String accountNumber = null;
	protected String domainAccountName;
	protected String language;
	protected int departmentID;
	protected String state;
	protected Date activationDate;
	protected Date deactivationDate;
	protected Date expirationDate;
	
	protected byte[] key1;
	protected byte[] key2;
	protected boolean temporaryPin = false;
	protected boolean pinLockedOut = false;
	protected int pinVersion = 0;
	protected String authenticationMethod = AUTHENTICATE_EXTERNAL_2FACTOR;
	protected Integer consecutiveAuthFailures = 0;
	
	protected Boolean serviceUser;

	protected List<? extends Role> roles = new ArrayList<Role>();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public int getId()
	{
		return id;
	}

	public WebUser setId(int id)
	{
		this.id = id;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public WebUser setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public WebUser setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getTitle()
	{
		return title;
	}

	public WebUser setTitle(String title)
	{
		this.title = title;
		return this;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public WebUser setFirstName(String firstName)
	{
		this.firstName = firstName;
		return this;
	}

	public String getSurname()
	{
		return surname;
	}

	public WebUser setSurname(String surname)
	{
		this.surname = surname;
		return this;
	}

	public String getInitials()
	{
		return initials;
	}

	public WebUser setInitials(String initials)
	{
		this.initials = initials;
		return this;
	}

	public String getMobileNumber()
	{
		return mobileNumber;
	}

	public WebUser setMobileNumber(String mobileNumber)
	{
		this.mobileNumber = mobileNumber;
		return this;
	}

	public String getEmail()
	{
		return email;
	}

	public WebUser setEmail(String email)
	{
		this.email = email;
		return this;
	}

	public String getAccountNumber()
	{
		return accountNumber;
	}

	public WebUser setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
		return this;
	}

	public String getDomainAccountName()
	{
		return domainAccountName;
	}

	public WebUser setDomainAccountName(String domainAccountName)
	{
		this.domainAccountName = domainAccountName;
		return this;
	}

	public String getLanguage()
	{
		return language;
	}

	public WebUser setLanguage(String language)
	{
		this.language = language;
		return this;
	}

	public int getDepartmentID()
	{
		return departmentID;
	}

	public WebUser setDepartmentID(int departmentID)
	{
		this.departmentID = departmentID;
		return this;
	}

	public String getState()
	{
		return state;
	}

	public WebUser setState(String state)
	{
		this.state = state;
		return this;
	}

	public Date getActivationDate()
	{
		return activationDate;
	}

	public WebUser setActivationDate(Date activationDate)
	{
		this.activationDate = activationDate;
		return this;
	}

	public Date getDeactivationDate()
	{
		return deactivationDate;
	}

	public WebUser setDeactivationDate(Date deactivationDate)
	{
		this.deactivationDate = deactivationDate;
		return this;
	}

	public Date getExpirationDate()
	{
		return expirationDate;
	}

	public WebUser setExpirationDate(Date expirationDate)
	{
		this.expirationDate = expirationDate;
		return this;
	}

	public byte[] getKey1()
	{
		return key1;
	}

	public WebUser setKey1(byte[] key1)
	{
		this.key1 = key1;
		return this;
	}

	public byte[] getKey2()
	{
		return key2;
	}

	public WebUser setKey2(byte[] key2)
	{
		this.key2 = key2;
		return this;
	}

	public void setServiceUser(Boolean serviceUser) {
		this.serviceUser = serviceUser;
	}

	public Boolean getServiceUser() {
		return serviceUser;
	}
		

	@Override
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

	@Override
	public boolean isPinLockedOut() 
	{
		return pinLockedOut;
	}

	@Override
	public WebUser setPinLockedOut(boolean pinLockedOut) 
	{
		this.pinLockedOut = pinLockedOut;
		return this;
	}

	@Override
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
	
	
	public Integer getConsecutiveAuthFailures()
	{
		return consecutiveAuthFailures;
	}

	public WebUser setConsecutiveAuthFailures(Integer consecutiveAuthFailures)
	{
		this.consecutiveAuthFailures = consecutiveAuthFailures;
		return this;
	}

	public List<? extends Role> getRoles()
	{
		return roles;
	}

	public void setRoles(List<? extends Role> roles)
	{
		this.roles = roles;
	}

	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator() //
				.notLess("companyID", companyID, 1) //
				.notEmpty("surname", surname, LAST_NAME_MAX_LENGTH) //
				.notLonger("email", email, EMAIL_MAX_LENGTH) //
				.notLonger("accountNumber", accountNumber, ACCOUNT_NO_MAX_LENGTH) //
				.notEmpty("domainAccountName", domainAccountName, DOMAIN_NAME_MAX_LENGTH) //
				.notEmpty("language", language, LANGUAGE_MAX_LENGTH) //
				.oneOf("state", state, STATE_ACTIVE, STATE_SUSPENDED, STATE_DEACTIVATED, STATE_PERMANENT) //
				.notLonger("key1", key1, KEY_MAX_LENGTH) //
				.oneOf("authenticationMethod", authenticationMethod, AUTHENTICATE_EXTERNAL_2FACTOR, AUTHENTICATE_PASSWORD_2FACTOR) //
				.notLonger("key2", key2, KEY_MAX_LENGTH);

		if (STATE_PERMANENT.equals(state))
		{
			validator //
					.notNull("title", title, TITLE_MAX_LENGTH) //
					.notNull("firstName", firstName, FIRST_NAME_MAX_LENGTH) //
					.notNull("initials", initials, INITIALS_MAX_LENGTH) //
					.notNull("mobileNumber", mobileNumber, MOBILE_NUMBER_MAX_LENGTH);
		}
		else
		{
			validator //
					.notEmpty("title", title, TITLE_MAX_LENGTH) //
					.notEmpty("firstName", firstName, FIRST_NAME_MAX_LENGTH) //
					.notEmpty("initials", initials, INITIALS_MAX_LENGTH) //
					.notEmpty("mobileNumber", mobileNumber, MOBILE_NUMBER_MAX_LENGTH);
		}

		return validator.toList();
	}

}
