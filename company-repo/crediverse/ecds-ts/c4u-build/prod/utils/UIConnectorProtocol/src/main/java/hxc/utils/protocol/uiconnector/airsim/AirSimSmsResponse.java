package hxc.utils.protocol.uiconnector.airsim;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class AirSimSmsResponse extends UiBaseResponse
{
	private static final long serialVersionUID = 6887371472320322736L;
	
	public AirSimSmsResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

}
