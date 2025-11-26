package hxc.utils.protocol.uiconnector.response;

public class RevertServiceResponse extends UiBaseResponse
{

	private static final long serialVersionUID = 7657918848831248068L;
	private String response;

	public RevertServiceResponse(String userId, String sessionId, String response)
	{
		super(userId, sessionId);
		this.response = response;
	}

	public void setResponse(String response)
	{
		this.response = response;
	}

	public String getResponse()
	{
		return response;
	}
}
