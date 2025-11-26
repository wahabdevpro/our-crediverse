package hxc.utils.protocol.uiconnector.request;

public class ESBShutdownRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 7218582026600980615L;

	public ESBShutdownRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}
}
