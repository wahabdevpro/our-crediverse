package hxc.utils.protocol.uiconnector.request;

public class SystemInfoRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 5862037653192712688L;
	
	public SystemInfoRequest()
	{
	}

	public SystemInfoRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

}
