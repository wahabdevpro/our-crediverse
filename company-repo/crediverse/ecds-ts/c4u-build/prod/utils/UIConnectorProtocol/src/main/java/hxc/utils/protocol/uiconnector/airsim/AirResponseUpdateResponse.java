package hxc.utils.protocol.uiconnector.airsim;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class AirResponseUpdateResponse extends UiBaseResponse
{
	private static final long serialVersionUID = -3830108030032055178L;

	public AirResponseUpdateResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

}
