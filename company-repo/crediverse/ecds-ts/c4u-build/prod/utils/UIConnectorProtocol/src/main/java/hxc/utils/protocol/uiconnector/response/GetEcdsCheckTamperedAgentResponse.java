package hxc.utils.protocol.uiconnector.response;

public class GetEcdsCheckTamperedAgentResponse extends UiBaseResponse
{
	private static final long serialVersionUID = 3556723341973826510L;

	Boolean result;
	Boolean agentTampered;
	Boolean accountTampered;
	
	public Boolean getResult()
	{
		return result;
	}
	
	public void setResult(Boolean result)
	{
		this.result = result;
	}
	
	public Boolean getAgentTampered() 
	{
		return agentTampered;
	}

	public void setAgentTampered(Boolean agentTampered) 
	{
		this.agentTampered = agentTampered;
	}

	public Boolean getAccountTampered() 
	{
		return accountTampered;
	}

	public void setAccountTampered(Boolean accountTampered) 
	{
		this.accountTampered = accountTampered;
	}

	public GetEcdsCheckTamperedAgentResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}
}
