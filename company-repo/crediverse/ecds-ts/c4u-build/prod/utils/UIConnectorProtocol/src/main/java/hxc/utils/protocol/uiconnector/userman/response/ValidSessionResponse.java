package hxc.utils.protocol.uiconnector.userman.response;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class ValidSessionResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -4585179512910540L;

	public ValidSessionResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

}
