package hxc.utils.protocol.uiconnector.request;

public class GetEcdsCheckTamperedAgentRequest extends UiBaseRequest
{
	private static final long serialVersionUID = 6473128274548162378L;

	private String msisdn;
	
	public String getMsisdn()
	{
		return msisdn;
	}
	
	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
	}
	
	public GetEcdsCheckTamperedAgentRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}
}
