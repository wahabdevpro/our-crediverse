package hxc.utils.protocol.uiconnector.request;

import hxc.utils.protocol.uiconnector.airsim.AirSimMSISDNUsage;

public class AirSimStartUsageRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 9086618712315192575L;
	private AirSimMSISDNUsage msisdnUsage;
	
	public AirSimStartUsageRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public AirSimMSISDNUsage getMsisdnUsage()
	{
		return msisdnUsage;
	}

	public void setMsisdnUsage(AirSimMSISDNUsage msisdnUsage)
	{
		this.msisdnUsage = msisdnUsage;
	}
	
}
