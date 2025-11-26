package hxc.ecds.protocol.rest;

public abstract class ResponseHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	public static final String RETURN_CODE_UNKNOWN = "ERR_UNKNOWN";
	public static final String RETURN_CODE_SUCCESS = "SUCCESS"; 

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected String transactionNumber;
	protected String inboundTransactionID;
	protected String inboundSessionID;
	protected String returnCode = RETURN_CODE_UNKNOWN;
	protected String additionalInformation;
	protected boolean followUp;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public String getTransactionNumber()
	{
		return transactionNumber;
	}

	public ResponseHeader setTransactionNumber(String transactionNumber)
	{
		this.transactionNumber = transactionNumber;
		return this;
	}

	public String getInboundTransactionID()
	{
		return inboundTransactionID;
	}

	public ResponseHeader setInboundTransactionID(String inboundTransactionID)
	{
		this.inboundTransactionID = inboundTransactionID;
		return this;
	}

	public String getInboundSessionID()
	{
		return inboundSessionID;
	}

	public ResponseHeader setInboundSessionID(String inboundSessionID)
	{
		this.inboundSessionID = inboundSessionID;
		return this;
	}

	public String getReturnCode()
	{
		return returnCode;
	}

	public ResponseHeader setReturnCode(String returnCode)
	{
		this.returnCode = returnCode;
		return this;
	}

	public String getAdditionalInformation()
	{
		return additionalInformation;
	}

	public ResponseHeader setAdditionalInformation(String additionalInformation)
	{
		this.additionalInformation = additionalInformation;
		return this;
	}

	public boolean getFollowUp()
	{
		return followUp;
	}

	public ResponseHeader setFollowUp(boolean followUp)
	{
		this.followUp = followUp;
		return this;
	}

	public boolean wasSuccessful()
	{
		return RETURN_CODE_SUCCESS.equals(returnCode);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public ResponseHeader()
	{
	}

	public ResponseHeader(RequestHeader request)
	{
		this.inboundTransactionID = request.getInboundTransactionID();
		this.inboundSessionID = request.getInboundSessionID();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public boolean exitWith(String returnCode, String additionalInformation, Object... args)
	{
		this.returnCode = returnCode;
		this.additionalInformation = String.format(additionalInformation, args);
		return false;
	}

	public String describe(String extra)
	{
		return String.format("%s@%s(transactionNumber = %s, inboundTransactionID = %s, inboundSessionID = %s, returnCode = %s, additionalInformation = %s, followUp = %s%s%s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			transactionNumber, inboundTransactionID, inboundSessionID, returnCode, additionalInformation,followUp,
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
