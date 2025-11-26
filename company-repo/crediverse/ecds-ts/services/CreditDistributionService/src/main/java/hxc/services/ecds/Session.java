package hxc.services.ecds;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hxc.ecds.protocol.rest.ICoSignable;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.AgentUser;
import hxc.services.ecds.model.IAgentUser;
import hxc.services.ecds.model.Permission;
import hxc.services.ecds.model.Role;
import hxc.services.ecds.model.WebUser;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

public class Session extends hxc.ecds.protocol.rest.Session
{
	private final static Logger logger = LoggerFactory.getLogger(Session.class);
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static enum State
	{
		UNAUTHENTICATED, // 0
		IMSI, // ??
		USERNAME, // STEP1
		PASSWORD, // STEP2
		OTP, // STEP3
		AUTHENTICATED; // 10
	}; 

	public static final String CHANNEL_USSD = "U";
	public static final String CHANNEL_SMS = "S";
	public static final String CHANNEL_3PP = "A";
	public static final String CHANNEL_SMART_DEVICE = "P";
	public static final String CHANNEL_WUI = "W";
	public static final String CHANNEL_BATCH = "B";

	public static final String TEST_MODE = "TEST_MODE";
	public static final int SUPER_SESSION_ID = 0;

	public static enum UserType
	{
		WEBUSER, AGENT, AGENTUSER, SERVICE_USER
	}

	private static final String CO_SIGNATORY_OTP_SALT = "u=CQ,4^ke`omYq,y";
	private static final SecureRandom RANDOM = new SecureRandom();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	@JsonIgnore
	private int[] roles;

	@JsonIgnore
	private State state = State.UNAUTHENTICATED;

	@JsonIgnore
	private ConcurrentMap<String, Object> valueMap = new ConcurrentHashMap<String, Object>();

	@JsonIgnore
	private ConcurrentMap<Integer, Boolean> permissionCache = new ConcurrentHashMap<Integer, Boolean>();

	@JsonIgnore
	private CompanyInfo companyInfo;

	@JsonIgnore
	private String ipAddress;

	@JsonIgnore
	private String macAddress;

	@JsonIgnore
	private String machineName;

	@JsonIgnore
	private String channel;

	@JsonIgnore
	private String channelType;

	@JsonIgnore
	private Locale locale = null;

	@JsonIgnore
	private NumberFormat currencyFormat = null;

	@JsonIgnore
	private String mobileNumber;

	@JsonIgnore
	private boolean builtIn = false;

	@JsonIgnore
	private Agent agent;

	@JsonIgnore 
	private IAgentUser agentUser;
	
	@JsonIgnore
	private byte[] encOldPassw = null;

	@JsonIgnore
	private String oldPasswd = null;

	@JsonIgnore
	private boolean passwdExpired = false;

	@JsonIgnore
	private byte[] tempPIN = null;

	@JsonIgnore
	private Long tempPINExpiry = null;

	@JsonIgnore
	private UserType userType = null;

	@JsonIgnore
	private String imsi = null;

	@JsonIgnore
	private boolean coSignOnly = false;

	@JsonIgnore
	protected String coSignForSessionID;

	@JsonIgnore
	protected String coSignatoryTransactionID;

	@JsonIgnore
	protected byte[] coSignatoryOTPHash;

	@JsonIgnore
	protected Long coSignatoryOTPExpiryTimestamp;

	@JsonIgnore
	protected Date coSignatoryOTPExpiryDate;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@Override
	public String getSessionID()
	{
		return sessionID;
	}

	@Override
	public Session setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
		return this;
	}

	@Override
	public int getCompanyID()
	{
		return companyID;
	}

	@Override
	public Session setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Override
	public Integer getWebUserID()
	{
		return webUserID;
	}

	@Override
	public Session setWebUserID(Integer webUserID)
	{
		this.webUserID = webUserID;
		return this;
	}

	@Override
	public Integer getAgentID()
	{
		return agentID;
	}

	@Override
	public Session setAgentID(Integer agentID)
	{
		this.agentID = agentID;
		return this;
	}

	public Agent getAgent()
	{
		return agent;
	}

	public Session setAgent(Agent agent)
	{
		this.agent = agent;
		return this;
	}

	@Override
	public String getDomainAccountName()
	{
		return domainAccountName;
	}

	@Override
	public Session setDomainAccountName(String domainAccountName)
	{
		this.domainAccountName = domainAccountName;
		return this;
	}

	public int[] getRoles()
	{
		return roles;
	}

	public Session setRoles(int[] roles)
	{
		this.roles = roles;
		return this;
	}

	@Override
	public Date getExpiryTime()
	{
		return expiryTime;
	}

	@Override
	public Session setExpiryTime(Date expiryTime)
	{
		this.expiryTime = expiryTime;
		return this;
	}

	@JsonIgnore
	public State getState()
	{
		return state;
	}

	@JsonIgnore
	public Session setState(State state)
	{
		this.state = state;
		return this;
	}

	@JsonIgnore
	public CompanyInfo getCompanyInfo()
	{
		return this.companyInfo;
	}
	
	@JsonIgnore
	public boolean isUserIDValid()
	{
		return webUserID != null && agentID != null;
	}

	@JsonIgnore
	public int getUserID()
	{
		return webUserID == null ? -agentID : webUserID;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String key)
	{
		return (T) valueMap.get(key);
	}

	public <T> Session set(String key, T value)
	{
		if (value == null)
			valueMap.remove(key);
		else
			valueMap.put(key, value);
		return this;
	}

	public String getIpAddress()
	{
		return ipAddress;
	}

	public Session setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
		return this;
	}

	public String getMacAddress()
	{
		return macAddress;
	}

	public Session setMacAddress(String macAddress)
	{
		this.macAddress = macAddress;
		return this;
	}

	public String getMachineName()
	{
		return machineName;
	}

	public Session setMachineName(String machineName)
	{
		this.machineName = machineName;
		return this;
	}

	public String getChannel()
	{
		return channel;
	}

	public Session setChannel(String channel)
	{
		this.channel = channel;
		return this;
	}

	public String getChannelType()
	{
		return channelType;
	}

	public Session setChannelType(String channelType)
	{
		this.channelType = channelType;
		return this;
	}

	@Override
	public String getLanguageID()
	{
		return languageID;
	}

	@Override
	public Session setLanguageID(String languageID)
	{
		this.languageID = languageID;
		return this;
	}

	@Override
	public String getCountryID()
	{
		return countryID;
	}

	@Override
	public Session setCountryID(String countryID)
	{
		this.countryID = countryID;
		return this;
	}

	@Override
	public String getCompanyPrefix()
	{
		return companyPrefix;
	}

	@Override
	public Session setCompanyPrefix(String companyPrefix)
	{
		this.companyPrefix = companyPrefix;
		return this;
	}

	public Locale getLocale()
	{
		if (locale == null)
			locale = getLocale(languageID);
		return locale;
	}

	@JsonIgnore
	public Locale getLocale(String languageID)
	{
		return new Locale(languageID, countryID);
	}

	public NumberFormat getCurrencyFormat()
	{
		if (currencyFormat == null)
			currencyFormat = NumberFormat.getCurrencyInstance(getLocale());
		return currencyFormat;
	}

	public String getMobileNumber()
	{
		return mobileNumber;
	}

	public Session setMobileNumber(String mobileNumber)
	{
		this.mobileNumber = mobileNumber;
		return this;
	}

	@Override
	public Integer getOwnerAgentID()
	{
		return ownerAgentID;
	}

	@Override
	public Session setOwnerAgentID(Integer ownerAgentID)
	{
		this.ownerAgentID = ownerAgentID;
		return this;
	}

	@JsonIgnore
	public byte[] getTempPIN()
	{
		return tempPIN;
	}

	@JsonIgnore
	public void setTempPIN(byte[] codePIN)
	{
		tempPIN = codePIN;
	}

	@JsonIgnore
	public Long getTempPINExpiry()
	{
		return this.tempPINExpiry;
	}

	@JsonIgnore
	public void setTempPINExpiry(Long tempPINExpiry)
	{
		this.tempPINExpiry = tempPINExpiry;
	}

	@JsonIgnore
	public UserType getUserType()
	{
		return this.userType;
	}

	@JsonIgnore
	public Session setUserType(UserType userType)
	{
		this.userType = userType;
		return this;
	}

	@JsonIgnore
	public String getImsi()
	{
		return this.imsi;
	}

	@JsonIgnore
	public Session setImsi(String imsi)
	{
		this.imsi = imsi;
		return this;
	}

	@JsonIgnore
	public boolean isCoSignOnly()
	{
		return this.coSignOnly;
	}

	@JsonIgnore
	public boolean getCoSignOnly()
	{
		return this.coSignOnly;
	}

	@JsonIgnore
	public Session setCoSignOnly(boolean coSignOnly)
	{
		this.coSignOnly = coSignOnly;
		return this;
	}

	@JsonIgnore
	public String getCoSignForSessionID()
	{
		return this.coSignForSessionID;
	}

	@JsonIgnore
	public Session setCoSignForSessionID(String coSignForSessionID)
	{
		this.coSignForSessionID = coSignForSessionID;
		return this;
	}

	@JsonIgnore
	public String getCoSignatoryTransactionID()
	{
		return this.coSignatoryTransactionID;
	}

	@JsonIgnore
	public Session setCoSignatoryTransactionID(String coSignatoryTransactionID)
	{
		this.coSignatoryTransactionID = coSignatoryTransactionID;
		return this;
	}

	@JsonIgnore
	public byte[] getCoSignatoryOTPHash()
	{
		return this.coSignatoryOTPHash;
	}

	@JsonIgnore
	public Session setCoSignatoryOTPHash(byte[] coSignatoryOTPHash)
	{
		this.coSignatoryOTPHash = coSignatoryOTPHash;
		return this;
	}

	@JsonIgnore
	public Long getCoSignatoryOTPExpiryTimestamp()
	{
		return this.coSignatoryOTPExpiryTimestamp;
	}

	@JsonIgnore
	public Session setCoSignatoryOTPExpiryTimestamp(Long coSignatoryOTPExpiryTimestamp)
	{
		this.coSignatoryOTPExpiryTimestamp = coSignatoryOTPExpiryTimestamp;
		return this;
	}

	@JsonIgnore
	public Date getCoSignatoryOTPExpiryDate()
	{
		return this.coSignatoryOTPExpiryDate;
	}

	@JsonIgnore
	public Session setCoSignatoryOTPExpiryDate(Date coSignatoryOTPExpiryDate)
	{
		this.coSignatoryOTPExpiryDate = coSignatoryOTPExpiryDate;
		return this;
	}
	
	@JsonIgnore
	public IAgentUser getAgentUser()
	{
		return agentUser;
	}

	@JsonIgnore
	public Session setAgentUser(IAgentUser agentUser)
	{
		this.agentUser = agentUser;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Session()
	{

	}

	public Session(CompanyInfo companyInfo)
	{
		this.companyInfo = companyInfo;
		this.companyID = companyInfo.getCompany().getId();
		this.countryID = companyInfo.getCompany().getCountry();
		this.companyPrefix = companyInfo.getCompany().getPrefix();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public void check(EntityManager em, Permission permission) throws RuleCheckException
	{
		check(em, permission, false);
	}

	public void check(EntityManager em, Permission permission, boolean forTrading) throws RuleCheckException
	{
		if (forTrading && builtIn)
		{
			Boolean testMode = get(Session.TEST_MODE);
			if (testMode == null || !testMode)
				throw new RuleCheckException(StatusCode.FORBIDDEN, null, "Permanent Web-Users may not Trade", permission);
		}
		check(em, permission, "WebUser/Agent doesn't have the '%s' permission", permission);
	}

	public void check(EntityManager em, Permission permission, String message, Object... args) throws RuleCheckException
	{
		// Load Permission if it hasn't been loaded before
		int permissionID = permission.getId();
		if (permissionID <= 0)
		{
			Permission perm = Permission.findByName(em, permission.getGroup(), permission.getName());
			if (perm == null)
				throw new RuleCheckException(StatusCode.FORBIDDEN, null, "WebUser/Agent doesn't have the '%s' permission", permission);
			permission.setId(permissionID = perm.getId());
		}

		// Attempt to get from cache
		Boolean allowed = permissionCache.get(permissionID);

		if (allowed == null)
		{
			// Add to Cache
			allowed = companyID == SUPER_SESSION_ID || companyInfo.check(em, permissionID, roles);
			//permissionCache.put(permissionID, allowed);
		}

		// Throw if not allowed
		if (!allowed)
			throw new RuleCheckException(StatusCode.FORBIDDEN, null, message, args);
	}

	public void agentOrCheck(EntityManager em, Permission permission, boolean forTrading) throws RuleCheckException
	{
		if (agent != null)
			return;
		check(em, permission, forTrading);
	}

	public boolean hasPermission(EntityManager em, Permission permission)
	{
		return hasPermission(em, permission, false);
	}

	public boolean hasPermission(EntityManager em, Permission permission, boolean forTrading)
	{
		// Test if Built-In Web User wants to Trade
		if (forTrading && builtIn)
		{
			Boolean testMode = get(Session.TEST_MODE);
			if (testMode == null || !testMode)
				return false;
		}

		// Load Permission if it hasn't been loaded before
		int permissionID = permission.getId();
		if (permissionID <= 0)
		{
			Permission perm = Permission.findByName(em, permission.getGroup(), permission.getName());
			if (perm == null)
				return false;
			permission.setId(permissionID = perm.getId());
		}

		// Attempt to get from cache
		Boolean allowed = permissionCache.get(permissionID);

		if (allowed == null)
		{
			// Add to Cache
			allowed = companyID == SUPER_SESSION_ID || companyInfo.check(em, permissionID, roles);
			//permissionCache.put(permissionID, allowed);
		}

		return allowed;
	}

	// Extends Session time-out
	public void extend(int minutes)
	{
		Date now = new Date();
		expiryTime = new Date(now.getTime() + minutes * 60000);
	}

	// Cache Role IDs for the Session's WebUser
	public void setRoles(List<Role> roles)
	{
		if (roles == null)
			return;
		int[] roleArray = new int[roles.size()];
		int index = 0;
		for (Role role : roles)
		{
			roleArray[index++] = role.getId();
		}
		this.roles = roleArray;
	}

	public boolean expired()
	{
		Date now = new Date();
		return now.after(expiryTime);
	}

	public String offerPIN(EntityManager em, Session session, boolean required, String pin) throws RuleCheckException
	{
		// Must be an Agent Session
		if (agent == null)
			return TransactionsConfig.ERR_INVALID_PIN;

		// Empty PIN
		if (pin == null || pin.isEmpty())
			return required ? TransactionsConfig.ERR_INVALID_PIN : null;

		return agentUser.offerPIN(em, session, companyInfo, pin);
	}

	@JsonIgnore
	public void setWebUser(WebUser webUser)
	{
		Objects.requireNonNull(webUser, "webUser may not be null");
		setWebUserID(webUser.getId());
		setRoles(webUser.getRoles());
		setChannel(Session.CHANNEL_WUI);
		setDomainAccountName(webUser.getDomainAccountName());
		setMobileNumber(webUser.getMobileNumber());
		setLanguageID(webUser.getLanguage());
		setUserType(UserType.WEBUSER);
		builtIn = WebUser.STATE_PERMANENT.equals(webUser.getState());
	}

	@JsonIgnore
	public void setServiceUser(WebUser webUser, String channel) {
		setWebUser(webUser);
		setChannel(channel);
		setUserType(UserType.SERVICE_USER);
	}

	@JsonIgnore
	public Session withAgent(Agent agent, AgentUser user)
	{
		setAgentUser(user == null ? agent : user);
		setAgentUserID(user == null ? null : user.getId());
		setChannelType(user == null ? null : user.getChannelType());
		setAgent(agent);
		setAgentID(agent.getId());
		setDomainAccountName(agentUser.getDomainAccountName() != null ? agentUser.getDomainAccountName() : agentUser.getMobileNumber());
		setOwnerAgentID(agent.getOwnerAgentID());
		setMobileNumber(agentUser.getMobileNumber());
		setLanguageID(agentUser.getLanguage());
		setUserType(UserType.AGENT);
		builtIn = Agent.STATE_PERMANENT.equals(agent.getState());
		setRoles(new int[] { agentUser.getRoleID() });
		return this;
	}

	@JsonIgnore
	public static byte[] hashOTP(String OTPString) throws UnsupportedEncodingException, NoSuchAlgorithmException
	{
		MessageDigest crypt = MessageDigest.getInstance("SHA-1");
		crypt.reset();
		String OTPSaltedString = CO_SIGNATORY_OTP_SALT + OTPString;
		crypt.update(OTPSaltedString.getBytes("UTF-8"));
		return crypt.digest();
	}

	@JsonIgnore
	public String generateCoSignatoryOTP(String coSignForSessionID, String coSignatoryTransactionID, int validity) throws UnsupportedEncodingException, NoSuchAlgorithmException
	{
		int OTP = RANDOM.nextInt(90000) + 10000;
		String OTPString = String.format("%05d", OTP);
		byte[] OTPHash = hashOTP(OTPString);

		Calendar OTPExpiryCalendar = Calendar.getInstance();
		OTPExpiryCalendar.add(Calendar.SECOND, validity);
		long OTPExpiryTimestamp = System.nanoTime() + ((long) validity * 1000L * 1000L * 1000L);

		this.setCoSignForSessionID(coSignForSessionID);
		this.setCoSignatoryTransactionID(coSignatoryTransactionID);
		this.setCoSignatoryOTPHash(OTPHash);
		this.setCoSignatoryOTPExpiryTimestamp(OTPExpiryTimestamp);
		this.setCoSignatoryOTPExpiryDate(OTPExpiryCalendar.getTime());

		return OTPString;
	}

	@JsonIgnore
	public void validateCoSignable(ICreditDistribution context, ICoSignable coSignable, String coSignedForSessionID) throws RuleCheckException
	{
		try
		{
			if (this.getCoSignForSessionID() == null)
				throw new RuleCheckException(TransactionsConfig.ERR_CO_AUTHORIZE, "coSignForSessionID", "coSignForSessionID is not set");

			if (Objects.equals(this.getCoSignForSessionID(), coSignedForSessionID) == false)
				throw new RuleCheckException(TransactionsConfig.ERR_CO_AUTHORIZE, "coSignForSessionID", "coSignForSessionID does not match");

			if (this.getCoSignatoryTransactionID() == null)
				throw new RuleCheckException(TransactionsConfig.ERR_CO_AUTHORIZE, "coSignatoryTransactionID", "coSignatoryTransactionID is not set");

			if (Objects.equals(this.getCoSignatoryTransactionID(), coSignable.getCoSignatoryTransactionID()) == false)
				throw new RuleCheckException(TransactionsConfig.ERR_CO_AUTHORIZE, "coSignatoryTransactionID", "coSignatoryTransactionID does not match");

			if (this.coSignOnly)
			{
				logger.trace("CoSign only session ... not checking OTP");
				return;
			}
			else
			{
				if (this.getCoSignatoryOTPHash() == null)
					throw new RuleCheckException(TransactionsConfig.ERR_CO_AUTHORIZE, "coSignatoryOTP", "session.coSignatoryOTP is null ...");

				if (coSignable.getCoSignatoryOTP() == null)
					throw new RuleCheckException(TransactionsConfig.ERR_CO_AUTHORIZE, "coSignatoryOTP", "coSignatoryOTP is null ...");

				Long now = System.nanoTime();
				Long OTPExpiryTimestamp = this.getCoSignatoryOTPExpiryTimestamp();
				if (OTPExpiryTimestamp != null && now > OTPExpiryTimestamp)
				{
					logger.info("coSignatoryOTP expired {} > {} -> true", now, OTPExpiryTimestamp);
					throw new RuleCheckException(TransactionsConfig.ERR_CO_AUTHORIZE, "coSignatoryOTP", "coSignatoryOTP is expired ...");
				}

				if (!Arrays.equals(this.getCoSignatoryOTPHash(), hashOTP(coSignable.getCoSignatoryOTP())))
				{
					throw new RuleCheckException(TransactionsConfig.ERR_CO_AUTHORIZE, "coSignatoryOTP", "coSignatoryOTP is invalid ...");
				}
				this.setCoSignForSessionID(null);
				this.setCoSignatoryTransactionID(null);
				this.setCoSignatoryOTPHash(null);
				this.setCoSignatoryOTPExpiryTimestamp(null);
				this.setCoSignatoryOTPExpiryDate(null);
			}
		}
		catch (UnsupportedEncodingException | NoSuchAlgorithmException exception)
		{
			String msg = String.format("validateCoSignable: %s", exception.getMessage());
			logger.error(msg, exception);
			throw new RuleCheckException(StatusCode.TECHNICAL_PROBLEM, null, msg);
		}
	}

	@JsonIgnore
	public static Boolean checkAgentAllowedChannels(String sessionChannel, int agentAllowedChannels)
	{
		Objects.requireNonNull(sessionChannel, "sessionChannel may not be null");
		Boolean allowed = false;
		switch (sessionChannel)
		{
			case Session.CHANNEL_USSD:
				allowed = (agentAllowedChannels & Agent.ALLOWED_USSD) != 0;
				break;

			case Session.CHANNEL_SMS:
				allowed = (agentAllowedChannels & Agent.ALLOWED_SMS) != 0;
				break;

			case Session.CHANNEL_3PP:
				allowed = (agentAllowedChannels & Agent.ALLOWED_API) != 0;
				break;

			case Session.CHANNEL_SMART_DEVICE:
				allowed = (agentAllowedChannels & Agent.ALLOWED_APP) != 0;
				break;

			case Session.CHANNEL_WUI:
				allowed = (agentAllowedChannels & Agent.ALLOWED_WUI) != 0;
				break;

			case Session.CHANNEL_BATCH:
				allowed = (agentAllowedChannels & Agent.ALLOWED_BATCH) != 0;
				break;

			default:
				allowed = null;
				break;
		}
		return allowed;
	}
}
