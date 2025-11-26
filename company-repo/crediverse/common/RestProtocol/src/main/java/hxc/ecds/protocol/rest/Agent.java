package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Agent implements IValidatable, IAuthenticatable
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
	public static final int PHONE_NUMBER_MAX_LENGTH = 30;
	public static final int ACCOUNT_NO_MAX_LENGTH = 20;
	public static final int DOMAIN_NAME_MAX_LENGTH = 40;
	public static final int LANGUAGE_MAX_LENGTH = 2;
	public static final int KEY_MAX_LENGTH = 100;
	public static final int IMSI_MAX_LENGTH = 15;
	public static final int IMEI_MAX_LENGTH = 16;
	public static final int ADDRESS_LINE_MAX_LENGTH = 50;
	public static final int ZIP_MAX_LENGTH = 10;
	public static final int PLACE_MAX_LENGTH = 30;
	public static final int EMAIL_MAX_LENGTH = 50;

	public static final int ALLOWED_USSD = 1;
	public static final int ALLOWED_SMS = 2;
	public static final int ALLOWED_APP = 4;
	public static final int ALLOWED_API = 8;
	public static final int ALLOWED_WUI = 16;
	public static final int ALLOWED_BATCH = 32;
	public static final int ALLOWED_ALL = 63;

	public static final String GENDER_MALE = "M";
	public static final String GENDER_FEMALE = "F";
	public static final String GENDER_OTHER = "O";

	public static final String AUTO_NUMBER = "<Auto>";

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
	/*
	 *	Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
	 */
	//protected boolean msisdnRecycled;
	protected String imei;
	protected Date lastImeiUpdate;
	protected String imsi;
	protected Date lastImsiChange;
	protected String title;
	protected String firstName;
	protected String initials;
	protected String surname;
	protected String language;
	protected String domainAccountName;
	protected String gender;
	protected Date dateOfBirth;
	protected String streetAddressLine1;
	protected String streetAddressLine2;
	protected String streetAddressSuburb;
	protected String streetAddressCity;
	protected String streetAddressZip;
	protected String postalAddressLine1;
	protected String postalAddressLine2;
	protected String postalAddressSuburb;
	protected String postalAddressCity;
	protected String postalAddressZip;
	protected String altPhoneNumber;
	protected String email;
	protected int tierID;
	protected Integer groupID;
	protected Integer areaID;
	protected Integer serviceClassID;
	protected int roleID;
	protected String state;
	protected Date activationDate;
	protected Date deactivationDate;
	protected Date expirationDate;
	protected Integer supplierAgentID;
	protected Integer ownerAgentID;
	protected int allowedChannels;
	protected BigDecimal warningThreshold;


	// AML Limits
	protected BigDecimal maxTransactionAmount;
	protected Integer maxDailyCount;
	protected BigDecimal maxDailyAmount;
	protected Integer maxMonthlyCount;
	protected BigDecimal maxMonthlyAmount;
	
	// Reporting Limits
	protected Integer reportCountLimit;
	protected Integer reportDailyScheduleLimit;

	// Security
	protected boolean temporaryPin = true;
	protected long signature;
	protected boolean tamperedWith = false;
	protected boolean imsiLockedOut = false;
	protected boolean pinLockedOut = false;
	protected int pinVersion = 0;
	protected boolean confirmUssd = true;
	protected String authenticationMethod = AUTHENTICATE_PIN_2FACTOR;
	protected Integer consecutiveAuthFailures = 0;
	protected boolean sendBundleCommissionReport = true;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public int getId()
	{
		return id;
	}

	public Agent setId(int id)
	{
		this.id = id;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public Agent setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public Agent setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getAccountNumber()
	{
		return accountNumber;
	}

	public Agent setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
		return this;
	}

	public String getMobileNumber()
	{
		return mobileNumber;
	}

	public Agent setMobileNumber(String mobileNumber)
	{
		this.mobileNumber = mobileNumber;
		return this;
	}

	public String getImei()
	{
		return imei;
	}

	public Agent setImei(String imei)
	{
		this.imei = imei;
		return this;
	}
	
	public Date getLastImeiUpdate()
	{
		return lastImeiUpdate;
	}

	public Agent setLastImeiUpdate(Date lastImeiUpdate)
	{
		this.lastImeiUpdate = lastImeiUpdate;
		return this;
	}
	
	public boolean isImeiRecent(long refreshIntervalMinutes)
	{
		boolean result = false;
		Date now = new Date();		
		long refreshIntervalMilliSeconds = refreshIntervalMinutes * 60 * 1000;
		result = (lastImeiUpdate != null && (now.getTime() - lastImeiUpdate.getTime()) <= refreshIntervalMilliSeconds);
		return result;
	}
	
	public String getImsi()
	{
		return imsi;
	}

	public Agent setImsi(String imsi)
	{
		this.imsi = imsi;
		return this;
	}
	
	public Date getLastImsiChange()
	{
		return lastImsiChange;
	}

	public Agent setLastImsiChange(Date lastImsiChange)
	{
		this.lastImsiChange = lastImsiChange;
		return this;
	}

	public String getTitle()
	{
		return title;
	}

	public Agent setTitle(String title)
	{
		this.title = title;
		return this;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public Agent setFirstName(String firstName)
	{
		this.firstName = firstName;
		return this;
	}

	public String getInitials()
	{
		return initials;
	}

	public Agent setInitials(String initials)
	{
		this.initials = initials;
		return this;
	}

	public String getSurname()
	{
		return surname;
	}

	public Agent setSurname(String surname)
	{
		this.surname = surname;
		return this;
	}

	public String getLanguage()
	{
		return language;
	}

	public Agent setLanguage(String language)
	{
		this.language = language;
		return this;
	}

	public String getDomainAccountName()
	{
		return domainAccountName;
	}

	public Agent setDomainAccountName(String domainAccountName)
	{
		this.domainAccountName = domainAccountName;
		return this;
	}

	public String getGender()
	{
		return gender;
	}

	public Agent setGender(String gender)
	{
		this.gender = gender;
		return this;
	}

	public Date getDateOfBirth()
	{
		return dateOfBirth;
	}

	public Agent setDateOfBirth(Date dateOfBirth)
	{
		this.dateOfBirth = dateOfBirth;
		return this;
	}

	public String getStreetAddressLine1()
	{
		return streetAddressLine1;
	}

	public Agent setStreetAddressLine1(String streetAddressLine1)
	{
		this.streetAddressLine1 = streetAddressLine1;
		return this;
	}

	public String getStreetAddressLine2()
	{
		return streetAddressLine2;
	}

	public Agent setStreetAddressLine2(String streetAddressLine2)
	{
		this.streetAddressLine2 = streetAddressLine2;
		return this;
	}

	public String getStreetAddressSuburb()
	{
		return streetAddressSuburb;
	}

	public Agent setStreetAddressSuburb(String streetAddressSuburb)
	{
		this.streetAddressSuburb = streetAddressSuburb;
		return this;
	}

	public String getStreetAddressCity()
	{
		return streetAddressCity;
	}

	public Agent setStreetAddressCity(String streetAddressCity)
	{
		this.streetAddressCity = streetAddressCity;
		return this;
	}

	public String getStreetAddressZip()
	{
		return streetAddressZip;
	}

	public Agent setStreetAddressZip(String streetAddressZip)
	{
		this.streetAddressZip = streetAddressZip;
		return this;
	}

	public String getPostalAddressLine1()
	{
		return postalAddressLine1;
	}

	public Agent setPostalAddressLine1(String postalAddressLine1)
	{
		this.postalAddressLine1 = postalAddressLine1;
		return this;
	}

	public String getPostalAddressLine2()
	{
		return postalAddressLine2;
	}

	public Agent setPostalAddressLine2(String postalAddressLine2)
	{
		this.postalAddressLine2 = postalAddressLine2;
		return this;
	}

	public String getPostalAddressSuburb()
	{
		return postalAddressSuburb;
	}

	public Agent setPostalAddressSuburb(String postalAddressSuburb)
	{
		this.postalAddressSuburb = postalAddressSuburb;
		return this;
	}

	public String getPostalAddressCity()
	{
		return postalAddressCity;
	}

	public Agent setPostalAddressCity(String postalAddressCity)
	{
		this.postalAddressCity = postalAddressCity;
		return this;
	}

	public String getPostalAddressZip()
	{
		return postalAddressZip;
	}

	public Agent setPostalAddressZip(String postalAddressZip)
	{
		this.postalAddressZip = postalAddressZip;
		return this;
	}

	public String getAltPhoneNumber()
	{
		return altPhoneNumber;
	}

	public Agent setAltPhoneNumber(String altPhoneNumber)
	{
		this.altPhoneNumber = altPhoneNumber;
		return this;
	}	

	public String getEmail()
	{
		return email;
	}

	public Agent setEmail(String email)
	{
		this.email = email;
		return this;
	}

	public int getTierID()
	{
		return tierID;
	}

	public Agent setTierID(int tierID)
	{
		this.tierID = tierID;
		return this;
	}

	public Integer getGroupID()
	{
		return groupID;
	}

	public Agent setGroupID(Integer groupID)
	{
		this.groupID = groupID;
		return this;
	}

	public Integer getAreaID()
	{
		return areaID;
	}

	public Agent setAreaID(Integer areaID)
	{
		this.areaID = areaID;
		return this;
	}

	public Integer getServiceClassID()
	{
		return serviceClassID;
	}

	public Agent setServiceClassID(Integer serviceClassID)
	{
		this.serviceClassID = serviceClassID;
		return this;
	}	

	public int getRoleID()
	{
		return roleID;
	}

	public Agent setRoleID(int roleID)
	{
		this.roleID = roleID;
		return this;
	}

	public String getState()
	{
		return state;
	}

	public Agent setState(String state)
	{
		this.state = state;
		return this;
	}

	public Date getActivationDate()
	{
		return activationDate;
	}

	public Agent setActivationDate(Date activationDate)
	{
		this.activationDate = activationDate;
		return this;
	}

	public Date getDeactivationDate()
	{
		return deactivationDate;
	}

	public Agent setDeactivationDate(Date deactivationDate)
	{
		this.deactivationDate = deactivationDate;
		return this;
	}

	public Date getExpirationDate()
	{
		return expirationDate;
	}

	public Agent setExpirationDate(Date expirationDate)
	{
		this.expirationDate = expirationDate;
		return this;
	}

	public Integer getSupplierAgentID()
	{
		return supplierAgentID;
	}

	public Agent setSupplierAgentID(Integer supplierAgentID)
	{
		this.supplierAgentID = supplierAgentID;
		return this;
	}

	public Integer getOwnerAgentID()
	{
		return ownerAgentID;
	}

	public Agent setOwnerAgentID(Integer ownerAgentID)
	{
		this.ownerAgentID = ownerAgentID;
		return this;
	}

	public int getAllowedChannels()
	{
		return allowedChannels;
	}

	public Agent setAllowedChannels(int allowedChannels)
	{
		this.allowedChannels = allowedChannels;
		return this;
	}

	public BigDecimal getWarningThreshold()
	{
		return warningThreshold;
	}

	public Agent setWarningThreshold(BigDecimal warningThreshold)
	{
		this.warningThreshold = warningThreshold;
		return this;
	}

	public BigDecimal getMaxTransactionAmount()
	{
		return maxTransactionAmount;
	}

	public Agent setMaxTransactionAmount(BigDecimal maxTransactionAmount)
	{
		this.maxTransactionAmount = maxTransactionAmount;
		return this;
	}

	public Integer getMaxDailyCount()
	{
		return maxDailyCount;
	}

	public Agent setMaxDailyCount(Integer maxDailyCount)
	{
		this.maxDailyCount = maxDailyCount;
		return this;
	}

	public BigDecimal getMaxDailyAmount()
	{
		return maxDailyAmount;
	}

	public Agent setMaxDailyAmount(BigDecimal maxDailyAmount)
	{
		this.maxDailyAmount = maxDailyAmount;
		return this;
	}

	public Integer getMaxMonthlyCount()
	{
		return maxMonthlyCount;
	}

	public Agent setMaxMonthlyCount(Integer maxMonthlyCount)
	{
		this.maxMonthlyCount = maxMonthlyCount;
		return this;
	}

	public BigDecimal getMaxMonthlyAmount()
	{
		return maxMonthlyAmount;
	}

	public Agent setMaxMonthlyAmount(BigDecimal maxMonthlyAmount)
	{
		this.maxMonthlyAmount = maxMonthlyAmount;
		return this;
	}
	
	public Integer getReportCountLimit()
	{
		return this.reportCountLimit;
	}
	public Agent setReportCountLimit( Integer reportCountLimit )
	{
		this.reportCountLimit = reportCountLimit;
		return this;
	}

	public Integer getReportDailyScheduleLimit()
	{
		return this.reportDailyScheduleLimit;
	}
	public Agent setReportDailyScheduleLimit( Integer reportDailyScheduleLimit )
	{
		this.reportDailyScheduleLimit = reportDailyScheduleLimit;
		return this;
	}

	public boolean isTemporaryPin()
	{
		return temporaryPin;
	}

	public Agent setTemporaryPin(boolean temporaryPin)
	{
		this.temporaryPin = temporaryPin;
		return this;
	}

	public long getSignature()
	{
		return signature;
	}

	public Agent setSignature(long signature)
	{
		this.signature = signature;
		return this;
	}

	public boolean isTamperedWith()
	{
		return tamperedWith;
	}

	public Agent setTamperedWith(boolean tamperedWith)
	{
		this.tamperedWith = tamperedWith;
		return this;
	}

	public boolean isImsiLockedOut()
	{
		return imsiLockedOut;
	}

	public Agent setImsiLockedOut(boolean imsiLockedOut)
	{
		this.imsiLockedOut = imsiLockedOut;
		return this;
	}

	public boolean isPinLockedOut()
	{
		return pinLockedOut;
	}

	public Agent setPinLockedOut(boolean pinLockedOut)
	{
		this.pinLockedOut = pinLockedOut;
		return this;
	}	

	public int getPinVersion()
	{
		return pinVersion;
	}

	public Agent setPinVersion(int pinVersion)
	{
		this.pinVersion = pinVersion;
		return this;
	}		

	public String getAuthenticationMethod() 
	{
		return authenticationMethod;
	}

	public Agent setAuthenticationMethod(String authenticationMethod) 
	{
		this.authenticationMethod = authenticationMethod;
		return this;
	}

	public boolean isConfirmUssd()
	{
		return confirmUssd;
	}

	public Agent setConfirmUssd(boolean confirmUssd)
	{
		this.confirmUssd = confirmUssd;
		return this;
	}

	public Integer getConsecutiveAuthFailures()
	{
		return consecutiveAuthFailures;
	}

	public Agent setConsecutiveAuthFailures(Integer consecutiveAuthFailures)
	{
		this.consecutiveAuthFailures = consecutiveAuthFailures;
		return this;
	}
	public boolean isSendBundleCommissionReport()
	{
		return sendBundleCommissionReport;
	}

	public Agent setSendBundleCommissionReport(boolean sendBundleCommissionReport)
	{
		this.sendBundleCommissionReport = sendBundleCommissionReport;
		return this;
	}
	/*
	 * Functionality on hold MSISDN-RECYCLING - uncommment when functionality re-instated
	 */
	/*public boolean isMsisdnRecycled() {
		return msisdnRecycled;
	}

	public void setMsisdnRecycled(boolean msisdnRecycled) {
		this.msisdnRecycled = msisdnRecycled;
	}*/

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Agent()
	{

	}

	public Agent(Agent agent)
	{
		this.id = agent.id;
		this.companyID = agent.companyID;
		this.version = agent.version;
		this.accountNumber = agent.accountNumber;
		this.mobileNumber = agent.mobileNumber;
		this.imei = agent.imei;
		this.lastImeiUpdate = agent.lastImeiUpdate;
		this.imsi = agent.imsi;
		this.lastImsiChange = agent.lastImsiChange;
		this.title = agent.title;
		this.firstName = agent.firstName;
		this.initials = agent.initials;
		this.surname = agent.surname;
		this.language = agent.language;
		this.domainAccountName = agent.domainAccountName;
		this.gender = agent.gender;
		this.dateOfBirth = agent.dateOfBirth;
		this.streetAddressLine1 = agent.streetAddressLine1;
		this.streetAddressLine2 = agent.streetAddressLine2;
		this.streetAddressSuburb = agent.streetAddressSuburb;
		this.streetAddressCity = agent.streetAddressCity;
		this.streetAddressZip = agent.streetAddressZip;
		this.postalAddressLine1 = agent.postalAddressLine1;
		this.postalAddressLine2 = agent.postalAddressLine2;
		this.postalAddressSuburb = agent.postalAddressSuburb;
		this.postalAddressCity = agent.postalAddressCity;
		this.postalAddressZip = agent.postalAddressZip;
		this.altPhoneNumber = agent.altPhoneNumber;
		this.email = agent.email;
		this.tierID = agent.tierID;
		this.groupID = agent.groupID;
		this.areaID = agent.areaID;
		this.serviceClassID = agent.serviceClassID;
		this.roleID = agent.roleID;
		this.state = agent.state;
		this.activationDate = agent.activationDate;
		this.deactivationDate = agent.deactivationDate;
		this.expirationDate = agent.expirationDate;
		this.supplierAgentID = agent.supplierAgentID;
		this.ownerAgentID = agent.ownerAgentID;
		this.allowedChannels = agent.allowedChannels;
		this.warningThreshold = agent.warningThreshold;
		this.maxTransactionAmount = agent.maxTransactionAmount;
		this.maxDailyCount = agent.maxDailyCount;
		this.maxDailyAmount = agent.maxDailyAmount;
		this.maxMonthlyCount = agent.maxMonthlyCount;
		this.maxMonthlyAmount = agent.maxMonthlyAmount;
		this.reportCountLimit = agent.reportCountLimit;
		this.reportDailyScheduleLimit = agent.reportDailyScheduleLimit;
		this.temporaryPin = agent.temporaryPin;
		this.signature = agent.signature;
		this.tamperedWith = agent.tamperedWith;
		this.imsiLockedOut = agent.imsiLockedOut;
		this.pinLockedOut = agent.pinLockedOut;
		this.confirmUssd = agent.confirmUssd;
		this.authenticationMethod = agent.authenticationMethod;
		this.sendBundleCommissionReport = agent.sendBundleCommissionReport;
		/*
		 *	Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
		 */
		//this.msisdnRecycled = agent.msisdnRecycled;
		// pinVersion not copied
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
				.notEmpty("title", title, TITLE_MAX_LENGTH) //
				.notEmpty("firstName", firstName, FIRST_NAME_MAX_LENGTH) //
				.notLonger("initials", initials, INITIALS_MAX_LENGTH) //
				.notEmpty("surname", surname, LAST_NAME_MAX_LENGTH) //
				.notEmpty("language", language, LANGUAGE_MAX_LENGTH) //
				.notLonger("domainAccountName", domainAccountName, DOMAIN_NAME_MAX_LENGTH) //

				.notLonger("streetAddressLine1", streetAddressLine1, ADDRESS_LINE_MAX_LENGTH) //
				.notLonger("streetAddressLine2", streetAddressLine2, ADDRESS_LINE_MAX_LENGTH) //
				.notLonger("streetAddressSuburb", streetAddressSuburb, PLACE_MAX_LENGTH) //
				.notLonger("streetAddressCity", streetAddressCity, PLACE_MAX_LENGTH) //
				.notLonger("streetAddressZip", streetAddressZip, ZIP_MAX_LENGTH) //
				.notLonger("postalAddressLine1", postalAddressLine1, ADDRESS_LINE_MAX_LENGTH) //
				.notLonger("postalAddressLine2", postalAddressLine2, ADDRESS_LINE_MAX_LENGTH) //
				.notLonger("postalAddressSuburb", postalAddressSuburb, PLACE_MAX_LENGTH) //
				.notLonger("postalAddressCity", postalAddressCity, PLACE_MAX_LENGTH) //
				.notLonger("postalAddressZip", postalAddressZip, ZIP_MAX_LENGTH) //
				.notLonger("altPhoneNumber", altPhoneNumber, PHONE_NUMBER_MAX_LENGTH) //
				.notLonger("email", email, EMAIL_MAX_LENGTH) //
				.oneOf("state", state, STATE_ACTIVE, STATE_SUSPENDED, STATE_DEACTIVATED, STATE_PERMANENT) //

				.notNull("allowedChannels", allowedChannels) //
				.notLess("warningThreshold", warningThreshold, BigDecimal.ZERO) //
				.notLess("serviceClassID", serviceClassID, 1) //
				.notLess("roleID", roleID, 1) //
				.oneOf("authenticationMethod", authenticationMethod, AUTHENTICATE_PIN_2FACTOR, AUTHENTICATE_EXTERNAL_2FACTOR, AUTHENTICATE_PASSWORD_2FACTOR) //
				.notLess("maxTransactionAmount", maxTransactionAmount, BigDecimal.ZERO) //
				.notLess("maxDailyCount", maxDailyCount, 0) //
				.notLess("maxDailyAmount", maxDailyAmount, maxTransactionAmount) //
				.notLess("maxMonthlyCount", maxMonthlyCount, 0) //
				.notLess("maxMonthlyAmount", maxMonthlyAmount, BigDecimal.ZERO) //
				.notLess("maxMonthlyCount", maxMonthlyCount, maxDailyCount) //
				.notLess("maxMonthlyAmount", maxMonthlyAmount, maxDailyAmount) //
				.isMoney("maxDailyAmount", maxDailyAmount) //
				.isMoney("maxMonthlyAmount", maxMonthlyAmount) //
				.notLess("reportCountLimit", reportCountLimit, 0) //
				.notLess("reportDailyScheduleLimit", reportDailyScheduleLimit, 0) //
		;

		if (gender != null)
			validator.oneOf("gender", gender, GENDER_MALE, GENDER_FEMALE, GENDER_OTHER);

		if (supplierAgentID != null && supplierAgentID == id)
			validator.append(Violation.RECURSIVE, "supplierAgentID", null, "Cannot be own supplier");

		return validator.toList();

	}

}
