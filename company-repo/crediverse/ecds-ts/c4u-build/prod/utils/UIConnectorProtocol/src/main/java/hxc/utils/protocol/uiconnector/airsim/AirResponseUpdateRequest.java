package hxc.utils.protocol.uiconnector.airsim;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class AirResponseUpdateRequest extends UiBaseRequest
{
	private static final long serialVersionUID = -2421705519021450317L;

	private String airCall;
	private String responseCode;
	private String delay;
	
	public AirResponseUpdateRequest(String userId, String sessionId)
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

	public String getResponseCode() 
	{
		return responseCode;
	}

	public void setResponseCode(String responseCode) 
	{
		this.responseCode = responseCode;
	}

	public String getDelay() 
	{
		return delay;
	}

	public void setDelay(String delay) 
	{
		this.delay = delay;
	}
}
