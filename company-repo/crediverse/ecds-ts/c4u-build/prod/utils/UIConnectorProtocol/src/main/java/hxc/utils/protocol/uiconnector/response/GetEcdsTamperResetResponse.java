package hxc.utils.protocol.uiconnector.response;

public class GetEcdsTamperResetResponse extends UiBaseResponse
{
	private static final long serialVersionUID = 3556723351973876518L;

	Boolean result;
	
	public Boolean getResult()
	{
		return result;
	}
	
	public void setResult(Boolean result)
	{
		this.result = result;
	}
	
	public GetEcdsTamperResetResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}
}
