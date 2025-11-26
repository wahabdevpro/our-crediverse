package hxc.utils.protocol.uiconnector.request;

public class GetAirsimHistoryRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 6710035553378870147L;

	public GetAirsimHistoryRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}
}
