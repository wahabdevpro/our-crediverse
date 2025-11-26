package hxc.services.ecds;

public interface ICallbackItem 
{
	public abstract String getSessionID(); 
	public abstract void setSessionID(String sessionID); 
	public abstract String getBaseUri();
	public abstract void setBaseUri(String baseUri);
	public abstract String getCallbackUriPath();
	public abstract void setCallbackUriPath(String callbackUriPath);
	public abstract String getTokenUriPath();
	public abstract void setTokenUriPath(String tokenUriPath);
	public abstract int getAgentID();
	public abstract void setAgentID(int agentID);
	public abstract String getTransactionNo();
	public abstract void setTransactionNo(String transactionNo);
	public abstract int getOffset();
	public abstract void setOffset(int offset);
	public abstract int getLimit();
	public abstract void setLimit(int limit);
	@Override
	public abstract int hashCode();
	@Override
	public abstract boolean equals(Object obj);
	
}
