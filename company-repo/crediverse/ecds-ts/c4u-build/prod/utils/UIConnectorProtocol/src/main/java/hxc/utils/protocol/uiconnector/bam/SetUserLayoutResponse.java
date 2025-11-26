package hxc.utils.protocol.uiconnector.bam;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class SetUserLayoutResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -7862271870334172499L;

	public SetUserLayoutResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}
}
