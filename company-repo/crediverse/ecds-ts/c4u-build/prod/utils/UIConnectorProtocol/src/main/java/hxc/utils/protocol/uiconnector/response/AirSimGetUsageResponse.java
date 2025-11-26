package hxc.utils.protocol.uiconnector.response;

import hxc.utils.protocol.uiconnector.airsim.AirSimMSISDNUsage;

public class AirSimGetUsageResponse extends UiBaseResponse
{
	private static final long serialVersionUID = 4130240081747690438L;
	private AirSimMSISDNUsage [] airSimUsage = null;
	
	public AirSimGetUsageResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}
	
	public AirSimMSISDNUsage[] getAirSimUsage()
	{
		return airSimUsage;
	}
	
	public void setAirSimUsage(AirSimMSISDNUsage[] airSimUsage)
	{
		this.airSimUsage = airSimUsage;
	}
	
}
