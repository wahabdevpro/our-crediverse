package hxc.utils.protocol.uiconnector.alarms;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class GetAlarmDataRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 1437642253339859244L;

	public GetAlarmDataRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

}
