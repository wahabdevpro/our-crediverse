package hxc.ecds.protocol.rest;

public class AuthenticationResponse extends ResponseHeader
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String CODE_OK_AUTHENTICATED = "AUTHENTICATED";
	public static final String CODE_OK_NOW_REQUIRE_UTF8_IMSI = "REQUIRE_UTF8_IMSI";
	public static final String CODE_OK_NOW_REQUIRE_UTF8_USERNAME = "REQUIRE_UTF8_USERNAME";
	public static final String CODE_OK_NOW_REQUIRE_UTF8_OTP = "REQUIRE_UTF8_OTP";
	public static final String CODE_OK_NOW_REQUIRE_RSA_PASSWORD = "REQUIRE_RSA_PASSWORD";
	public static final String CODE_OK_NOW_REQUIRE_RSA_PIN = "REQUIRE_RSA_PIN";

	// == User Input Errors
	// Credentials is username and password (not OTP)
	// Invalid credentials means username or password is wrong.
	// This is used if its not found on either ECDS or Kerberos
	public static final String CODE_FAIL_CREDENTIALS_INVALID = "CREDENTIALS_INVALID";
	public static final String CODE_FAIL_OTP_INVALID = "OTP_INVALID";
	public static final String CODE_FAIL_OTP_EXPIRED = "OTP_EXPIRED";
	public static final String CODE_FAIL_CHANNEL_NOT_ALLOWED = "CHANNEL_NOT_ALLOWED";

	// == Non-user input errors:
	public static final String CODE_FAIL_SESSION_INVALID = "SESSION_INVALID";
	public static final String CODE_FAIL_INVALID_COMPANY = "INVALID_COMPANY";
	public static final String CODE_FAIL_ACCOUNT_NOT_ACTIVE = "ACCOUNT_NOT_ACTIVE";
	public static final String CODE_FAIL_PASSWORD_EXPIRED = "PASSWORD_EXPIRED";
	public static final String CODE_FAIL_IMSI_LOCKOUT = "IMSI_LOCKOUT";
	public static final String CODE_FAIL_PASSWORD_LOCKOUT = "PASSWORD_LOCKOUT";

	public static final String CODE_FAIL_OTHER_ERROR = "OTHER_ERROR";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected String sessionID;
	protected boolean moreInformationRequired;
	protected byte[] key1;
	protected byte[] key2;
	protected String value;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getSessionID()
	{
		return sessionID;
	}

	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}

	public boolean isMoreInformationRequired()
	{
		return moreInformationRequired;
	}

	public void setMoreInformationRequired(boolean moreInformationRequired)
	{
		this.moreInformationRequired = moreInformationRequired;
	}

	public byte[] getKey1()
	{
		return key1;
	}

	public void setKey1(byte[] key1)
	{
		this.key1 = key1;
	}

	public byte[] getKey2()
	{
		return key2;
	}

	public void setKey2(byte[] key2)
	{
		this.key2 = key2;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public AuthenticationResponse()
	{
	}

	public AuthenticationResponse(AuthenticationRequest request)
	{
		super(request);
	}
}
