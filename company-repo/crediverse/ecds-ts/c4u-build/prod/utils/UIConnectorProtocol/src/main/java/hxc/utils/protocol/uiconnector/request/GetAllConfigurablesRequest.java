package hxc.utils.protocol.uiconnector.request;

public class GetAllConfigurablesRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 4665516770848941376L;

	public GetAllConfigurablesRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
		setRequestCode(UiRequestCode.GET_ALL_CONFIGURATION);
	}

}
