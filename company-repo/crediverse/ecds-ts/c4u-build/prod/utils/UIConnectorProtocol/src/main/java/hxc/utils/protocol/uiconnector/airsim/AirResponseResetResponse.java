package hxc.utils.protocol.uiconnector.airsim;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class AirResponseResetResponse extends UiBaseResponse
{
	private static final long serialVersionUID = -1796103398569325129L;

	public AirResponseResetResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

}
