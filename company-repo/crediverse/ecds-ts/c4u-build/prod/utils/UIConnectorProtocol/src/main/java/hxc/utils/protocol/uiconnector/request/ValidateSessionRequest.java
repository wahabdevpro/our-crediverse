package hxc.utils.protocol.uiconnector.request;

public class ValidateSessionRequest extends UiBaseRequest
{

	private static final long serialVersionUID = -3748717763309696300L;

	public ValidateSessionRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

}
