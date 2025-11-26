package hxc.ecds.protocol.rest;

import java.util.List;

// REST End-Point: ~//authentication/authenticate
public class AuthenticationRequest extends RequestHeader implements ICoSignFor
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static String CHANNEL_USSD = "U";
	public static String CHANNEL_SMS = "S";
	public static String CHANNEL_3PP = "A";
	public static String CHANNEL_SMART_DEVICE = "P";
	public static String CHANNEL_WUI = "W";

	public static enum UserType
	{
		WEBUSER, AGENT;
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int companyID;
	private String channel;
	private String hostName;
	protected String macAddress;
	protected String ipAddress;
	protected byte[] data;
	protected String username;
	protected String password;
	protected String oneTimePin;
	protected UserType userType;
	protected String coSignForSessionID;
	protected String coSignatoryTransactionID;
	protected String customPinChangeMessage;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public String getCustomPinChangeMessage()
	{
		return customPinChangeMessage;
	}

	public void setCustomPinChangeMessage(String customPinChangeMessage)
	{
		this.customPinChangeMessage = customPinChangeMessage;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public void setCompanyID(int companyID)
	{
		this.companyID = companyID;
	}

	public String getChannel()
	{
		return channel;
	}

	public void setChannel(String channel)
	{
		this.channel = channel;
	}

	public String getHostName()
	{
		return hostName;
	}

	public void setHostName(String hostName)
	{
		this.hostName = hostName;
	}

	public String getMacAddress()
	{
		return macAddress;
	}

	public void setMacAddress(String macAddress)
	{
		this.macAddress = macAddress;
	}

	public String getIpAddress()
	{
		return ipAddress;
	}

	public void setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}

	public byte[] getData()
	{
		return data;
	}

	public void setData(byte[] data)
	{
		this.data = data;
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public void setUsername(String username)
	{
		this.username = username;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public String getOneTimePin()
	{
		return oneTimePin; 
	}
	
	public void setOneTimePin(String oneTimePin)
	{
		this.oneTimePin = oneTimePin;
	}

	public UserType getUserType()
	{
		return this.userType;
	}

	public void setUserType(UserType userType)
	{
		this.userType = userType;
	}

	@Override
	public String getCoSignForSessionID()
	{
		return this.coSignForSessionID;
	}

	@Override
	public AuthenticationRequest setCoSignForSessionID(String coSignForSessionID)
	{
		this.coSignForSessionID = coSignForSessionID;
		return this;
	}

	@Override
	public String getCoSignatoryTransactionID()
	{
		return this.coSignatoryTransactionID;
	}

	@Override
	public AuthenticationRequest setCoSignatoryTransactionID(String coSignatoryTransactionID)
	{
		this.coSignatoryTransactionID = coSignatoryTransactionID;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public AuthenticationResponse createResponse()
	{
		return new AuthenticationResponse(this);
	}

	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator() //
				.notNull("companyID", companyID) //
				;
		validator = CoSignableUtils.validate(validator, this, false);
		return validator.toList();
	}

}
