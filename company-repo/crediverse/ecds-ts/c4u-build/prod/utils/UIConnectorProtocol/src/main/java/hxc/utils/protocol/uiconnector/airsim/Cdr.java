package hxc.utils.protocol.uiconnector.airsim;

import java.io.Serializable;
import java.util.Date;

import hxc.connectors.Channels;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.services.airsim.protocol.ICdr;

public class Cdr implements ICdr, Serializable
{
	private static final long serialVersionUID = 1751604239121217652L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
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
	private String[] additionalInformation;

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

	public void setHostName(String hostName)
	{
		this.hostName = hostName;
	}

	@Override
	public String getCallerID()
	{
		return callerID;
	}

	public void setCallerID(String callerID)
	{
		this.callerID = callerID;
	}

	@Override
	public String getA_MSISDN()
	{
		return a_MSISDN;
	}

	public void setA_MSISDN(String a_MSISDN)
	{
		this.a_MSISDN = a_MSISDN;
	}

	@Override
	public String getB_MSISDN()
	{
		return b_MSISDN;
	}

	public void setB_MSISDN(String b_MSISDN)
	{
		this.b_MSISDN = b_MSISDN;
	}

	@Override
	public Date getStartTime()
	{
		return startTime;
	}

	public void setStartTime(Date startTime)
	{
		this.startTime = startTime;
	}

	@Override
	public String getInboundTransactionID()
	{
		return inboundTransactionID;
	}

	public void setInboundTransactionID(String inboundTransactionID)
	{
		this.inboundTransactionID = inboundTransactionID;
	}

	@Override
	public String getInboundSessionID()
	{
		return inboundSessionID;
	}

	public void setInboundSessionID(String inboundSessionID)
	{
		this.inboundSessionID = inboundSessionID;
	}

	@Override
	public Channels getChannel()
	{
		return channel;
	}

	public void setChannel(Channels channel)
	{
		this.channel = channel;
	}

	@Override
	public RequestModes getRequestMode()
	{
		return requestMode;
	}

	public void setRequestMode(RequestModes requestMode)
	{
		this.requestMode = requestMode;
	}

	@Override
	public String getTransactionID()
	{
		return transactionID;
	}

	public void setTransactionID(String transactionID)
	{
		this.transactionID = transactionID;
	}

	@Override
	public String getServiceID()
	{
		return serviceID;
	}

	public void setServiceID(String serviceID)
	{
		this.serviceID = serviceID;
	}

	@Override
	public String getVariantID()
	{
		return variantID;
	}

	public void setVariantID(String variantID)
	{
		this.variantID = variantID;
	}

	@Override
	public String getProcessID()
	{
		return processID;
	}

	public void setProcessID(String processID)
	{
		this.processID = processID;
	}

	@Override
	public String getLastActionID()
	{
		return lastActionID;
	}

	public void setLastActionID(String lastActionID)
	{
		this.lastActionID = lastActionID;
	}

	@Override
	public int getLastExternalResultCode()
	{
		return lastExternalResultCode;
	}

	public void setLastExternalResultCode(int lastExternalResultCode)
	{
		this.lastExternalResultCode = lastExternalResultCode;
	}

	@Override
	public int getChargeLevied()
	{
		return chargeLevied;
	}

	public void setChargeLevied(int chargeLevied)
	{
		this.chargeLevied = chargeLevied;
	}

	@Override
	public ReturnCodes getReturnCode()
	{
		return returnCode;
	}

	public void setReturnCode(ReturnCodes returnCode)
	{
		this.returnCode = returnCode;
	}

	@Override
	public boolean isRolledBack()
	{
		return rolledBack;
	}

	public void setRolledBack(boolean rolledBack)
	{
		this.rolledBack = rolledBack;
	}

	@Override
	public boolean isFollowUp()
	{
		return followUp;
	}

	public void setFollowUp(boolean followUp)
	{
		this.followUp = followUp;
	}

	@Override
	public String getParam1()
	{
		return param1;
	}

	public void setParam1(String param1)
	{
		this.param1 = param1;
	}

	@Override
	public String getParam2()
	{
		return param2;
	}

	public void setParam2(String param2)
	{
		this.param2 = param2;
	}

	@Override
	public String[] getAdditionalInformation()
	{
		return additionalInformation;
	}

	public void setAdditionalInformation(String[] additionalInformation)
	{
		this.additionalInformation = additionalInformation;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Cdr()
	{

	}

	public Cdr(ICdr cdr)
	{
		this.hostName = cdr.getHostName();
		this.callerID = cdr.getCallerID();
		this.a_MSISDN = cdr.getA_MSISDN();
		this.b_MSISDN = cdr.getB_MSISDN();
		this.startTime = cdr.getStartTime();
		this.inboundTransactionID = cdr.getInboundTransactionID();
		this.inboundSessionID = cdr.getInboundSessionID();
		this.channel = cdr.getChannel();
		this.requestMode = cdr.getRequestMode();
		this.transactionID = cdr.getTransactionID();
		this.serviceID = cdr.getServiceID();
		this.variantID = cdr.getVariantID();
		this.processID = cdr.getProcessID();
		this.lastActionID = cdr.getLastActionID();
		this.lastExternalResultCode = cdr.getLastExternalResultCode();
		this.chargeLevied = cdr.getChargeLevied();
		this.returnCode = cdr.getReturnCode();
		this.rolledBack = cdr.isRolledBack();
		this.followUp = cdr.isFollowUp();
		this.param1 = cdr.getParam1();
		this.param2 = cdr.getParam2();
		this.additionalInformation = cdr.getAdditionalInformation();

	}

}
