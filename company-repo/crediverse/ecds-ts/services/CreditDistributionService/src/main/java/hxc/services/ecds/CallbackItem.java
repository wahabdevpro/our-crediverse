package hxc.services.ecds;

public class CallbackItem  implements ICallbackItem{
	private String sessionID; //unique identifier
	private String baseUri;
	private String callbackUriPath;
	private String tokenUriPath;
	private int agentID;
	private String transactionNo;
	private int offset;
	private int limit;
	
	public CallbackItem()
	{
	}
	
	public CallbackItem(String sessionID)
	{
		this.sessionID = sessionID;
	}

	public String getBaseUri() {
		return baseUri;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public String getSessionID() 
	{
		return sessionID;
	}
	
	public void setSessionID(String sessionID) 
	{
		this.sessionID = sessionID;
	}
	
	public String getCallbackUriPath() 
	{
		return callbackUriPath;
	}
	
	public void setCallbackUriPath(String callbackUriPath) 
	{
		this.callbackUriPath = callbackUriPath;
	}
	
	public int getAgentID() 
	{
		return agentID;
	}
	
	public void setAgentID(int agentID) 
	{
		this.agentID = agentID;
	}
	
	public String getTokenUriPath() 
	{
		return tokenUriPath;
	}

	public void setTokenUriPath(String tokenUriPath) 
	{
		this.tokenUriPath = tokenUriPath;
	}

	public String getTransactionNo()
	{
		return transactionNo;
	}

	public void setTransactionNo(String transactionNo)
	{
		this.transactionNo = transactionNo;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	@Override
	public int hashCode()
	{
		int hash;
		hash = this.sessionID.hashCode();
		return hash;
	}
	
	public boolean equals(Object obj)
	{
		if (obj instanceof CallbackItem) {
			CallbackItem callbackItem = (CallbackItem) obj;
			return callbackItem.getSessionID().equals(this.sessionID); 
		} else {
			return false;
		}
    }
}
