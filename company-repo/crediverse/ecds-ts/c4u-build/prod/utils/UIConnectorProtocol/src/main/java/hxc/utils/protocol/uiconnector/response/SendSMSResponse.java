package hxc.utils.protocol.uiconnector.response;

public class SendSMSResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -5943118313064833755L;
	private String response;

	public SendSMSResponse(String userId, String sessionId, String response)
	{
		super(userId, sessionId);
		this.response = response;
	}

	public String getResponse()
	{
		return response;
	}
}
