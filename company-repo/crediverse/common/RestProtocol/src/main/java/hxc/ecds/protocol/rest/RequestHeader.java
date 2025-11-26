package hxc.ecds.protocol.rest;

public abstract class RequestHeader implements IValidatable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static String MODE_NORMAL = "N";

	public static final String VERSION_CURRENT = "1";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String sessionID;
	private String inboundTransactionID;
	private String inboundSessionID;
	private String version = VERSION_CURRENT;
	private String mode = MODE_NORMAL;
	private String requestOriginInterface = "";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public String getSessionID()
	{
		return sessionID;
	}

	public RequestHeader setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
		return this;
	}

	public String getInboundTransactionID()
	{
		return inboundTransactionID;
	}

	public RequestHeader setInboundTransactionID(String inboundTransactionID)
	{
		this.inboundTransactionID = inboundTransactionID;
		return this;
	}

	public String getInboundSessionID()
	{
		return inboundSessionID;
	}

	public RequestHeader setInboundSessionID(String inboundSessionID)
	{
		this.inboundSessionID = inboundSessionID;
		return this;
	}

	public String getVersion()
	{
		return version;
	}

	public RequestHeader setVersion(String version)
	{
		this.version = version;
		return this;
	}

	public String getMode()
	{
		return mode;
	}

	public RequestHeader setMode(String mode)
	{
		this.mode = mode;
		return this;
	}
	public String getRequestOriginInterface()
	{
		return requestOriginInterface;
	}

	public RequestHeader setRequestOriginInterface(String requestOriginInterface)
	{
		this.requestOriginInterface = requestOriginInterface;
		return this;
	}
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public abstract <T extends ResponseHeader> T createResponse();


	public String describe(String extra)
	{
		return String.format("%s@%s(sessionID = %s, inboundTransactionID = %s, inboundSessionID = %s, version = %s, mode = %s%s%s, requestOriginInterface = %s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			sessionID, inboundTransactionID, inboundSessionID, version, mode,requestOriginInterface,
			(extra.isEmpty() ? "" : ", "), extra);
	}

	public String describe()
	{
		return this.describe("");
	}

	public String toString()
	{
		return this.describe();
	}

}
