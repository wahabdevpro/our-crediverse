package hxc.utils.protocol.uiconnector.bam;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class GetUserLayoutRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 182530613300459051L;

	public GetUserLayoutRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}
}
