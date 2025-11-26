package hxc.utils.protocol.hsx0;

import java.util.Date;

import hxc.utils.xmlrpc.XmlRpcFormat;

public class ResponseHeader
{
	public String originTransactionId;
	public String originOperatorId;
	public int responseCode;
	public String responseMessage;
	@XmlRpcFormat(format = "yyyyMMdd'T'HH:mm:ss")
	public Date originTimeStamp;

	// completionStatus
	public final static int COMPLETE_SUCCESS = 0;
	public final static int COMPLETE_MAYRETRY = 1;
	public final static int COMPLETE_NORETRY = 2;
	public final static int COMPLETE_REJECT = 3;

	// systemComponentId
	public final static int COMPONENT_BASE = 0;
	public final static int COMPONENT_SGW = 100;

	// reason
	public final static int REASON_SUCCESS = 0;
	public final static int REASON_INTERNAL = 0;

	public void setResponseCode(int completionStatus, int systemComponentId, int reason)
	{
		responseCode = (completionStatus * 1000000) + (systemComponentId * 1000) + reason;
	}

}
