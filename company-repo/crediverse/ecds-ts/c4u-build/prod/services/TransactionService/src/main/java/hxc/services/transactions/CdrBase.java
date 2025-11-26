package hxc.services.transactions;

import java.util.Date;

import com.concurrent.hxc.RequestHeader;

import hxc.connectors.Channels;
import hxc.connectors.IInteraction;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;

public abstract class CdrBase implements ICdr
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// CDR Fields
	//
	// /////////////////////////////////
	private String hostName;
	private String callerID;
	private String a_MSISDN;
	private String b_MSISDN;
	private Date startTime = new Date();
	private String inboundTransactionID;
	private String inboundSessionID;
	private Channels channel;
	private RequestModes requestMode;
	private String transactionID;
	private String serviceID;
	private String variantID;
	private String processID;
	private String lastActionID;
	private int lastExternalResultCode = 0;
	private int chargeLevied;
	private ReturnCodes returnCode = ReturnCodes.incomplete;
	private boolean rolledBack = false;
	private boolean followUp = false;
	private String param1;
	private String param2;
	private String additionalInformation;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@Override
	public String getHostName()
	{
		return hostName;
	}

	@Override
	public void setHostName(String hostName)
	{
		this.hostName = hostName;
	}

	@Override
	public String getCallerID()
	{
		return callerID;
	}

	@Override
	public void setCallerID(String callerID)
	{
		this.callerID = callerID;
	}

	@Override
	public String getA_MSISDN()
	{
		return a_MSISDN;
	}

	@Override
	public void setA_MSISDN(String a_MSISDN)
	{
		this.a_MSISDN = a_MSISDN;
	}

	@Override
	public String getB_MSISDN()
	{
		return b_MSISDN;
	}

	@Override
	public void setB_MSISDN(String b_MSISDN)
	{
		this.b_MSISDN = b_MSISDN;
	}

	@Override
	public Date getStartTime()
	{
		return startTime;
	}

	@Override
	public void setStartTime(Date startTime)
	{
		this.startTime = startTime;
	}

	@Override
	public String getInboundTransactionID()
	{
		return inboundTransactionID;
	}

	@Override
	public void setInboundTransactionID(String inboundTransactionID)
	{
		this.inboundTransactionID = inboundTransactionID;
	}

	@Override
	public String getInboundSessionID()
	{
		return inboundSessionID;
	}

	@Override
	public void setInboundSessionID(String inboundSessionID)
	{
		this.inboundSessionID = inboundSessionID;
	}

	@Override
	public Channels getChannel()
	{
		return channel;
	}

	@Override
	public void setChannel(Channels channel)
	{
		this.channel = channel;
	}

	@Override
	public RequestModes getRequestMode()
	{
		return requestMode;
	}

	@Override
	public void setRequestMode(RequestModes requestMode)
	{
		this.requestMode = requestMode;
	}

	@Override
	public String getTransactionID()
	{
		return transactionID;
	}

	@Override
	public void setTransactionID(String transactionID)
	{
		this.transactionID = transactionID;
	}

	@Override
	public String getServiceID()
	{
		return serviceID;
	}

	@Override
	public void setServiceID(String serviceID)
	{
		this.serviceID = serviceID;
	}

	@Override
	public String getVariantID()
	{
		return variantID;
	}

	@Override
	public void setVariantID(String variantID)
	{
		this.variantID = variantID;
	}

	@Override
	public String getProcessID()
	{
		return processID;
	}

	@Override
	public void setProcessID(String processID)
	{
		this.processID = processID;
	}

	@Override
	public String getLastActionID()
	{
		return lastActionID;
	}

	@Override
	public void setLastActionID(String lastActionID)
	{
		this.lastActionID = lastActionID;
	}

	@Override
	public int getLastExternalResultCode()
	{
		return lastExternalResultCode;
	}

	@Override
	public void setLastExternalResultCode(int lastExternalResultCode)
	{
		this.lastExternalResultCode = lastExternalResultCode;
	}

	@Override
	public int getChargeLevied()
	{
		return chargeLevied;
	}

	@Override
	public void setChargeLevied(int chargeLevied)
	{
		this.chargeLevied = chargeLevied;
	}

	@Override
	public ReturnCodes getReturnCode()
	{
		return returnCode;
	}

	@Override
	public void setReturnCode(ReturnCodes returnCode)
	{
		this.returnCode = returnCode;
	}

	@Override
	public boolean isRolledBack()
	{
		return rolledBack;
	}

	@Override
	public void setRolledBack(boolean rolledBack)
	{
		this.rolledBack = rolledBack;
	}

	@Override
	public boolean isFollowUp()
	{
		return followUp;
	}

	@Override
	public void setFollowUp(boolean followUp)
	{
		this.followUp = followUp;
	}

	@Override
	public String getParam1()
	{
		return param1;
	}

	@Override
	public void setParam1(String param1)
	{
		this.param1 = param1;
	}

	@Override
	public String getParam2()
	{
		return param2;
	}

	@Override
	public void setParam2(String param2)
	{
		this.param2 = param2;
	}

	@Override
	public String getAdditionalInformation()
	{
		return additionalInformation;
	}

	@Override
	public void setAdditionalInformation(String additionalInformation)
	{
		this.additionalInformation = additionalInformation;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public CdrBase()
	{

	}

	public CdrBase(RequestHeader request, String transactionNumber)
	{
		this.hostName = request.getHostName();
		this.callerID = request.getCallerID();
		this.inboundTransactionID = request.getTransactionID();
		this.inboundSessionID = request.getSessionID();
		this.channel = request.getChannel();
		this.requestMode = request.getMode();
		this.transactionID = transactionNumber;
	}

	public CdrBase(IInteraction request, String transactionNumber)
	{
		this.hostName = "SGW";
		this.callerID = request.getMSISDN();
		this.inboundTransactionID = request.getInboundTransactionID();
		this.inboundSessionID = "0";
		this.channel = request.getChannel();
		this.requestMode = RequestModes.normal;
		this.transactionID = transactionNumber;
		this.a_MSISDN = request.getMSISDN();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Abstract Methods
	//
	// /////////////////////////////////

	/**
	 * Write the CDR out to a FileOutputStream
	 * 
	 * @param stream
	 *            the Stream to write to (can be null)
	 * @param logger
	 *            the Logger to log information to (can be null)
	 */
	// public abstract void Write(FileOutputStream stream, ILogger logger);

}
