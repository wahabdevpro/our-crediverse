package hxc.utils.protocol.uiconnector.airsim;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class AirResponseResetRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 8609477620661529076L;
	
	private String airCall;
	
	public AirResponseResetRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public String getAirCall() 
	{
		return airCall;
	}

	public void setAirCall(String airCall) 
	{
		this.airCall = airCall;
	}
}
