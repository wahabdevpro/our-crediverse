package hxc.connectors.ecdsapi;

import java.util.List;

import hxc.ecds.protocol.rest.Transaction;

public class NotificationEvent {
	private String sessionID;
	private String baseUri;
	private String tokenUriPath;
	private String callbackUriPath;
	private List<? extends Transaction> transactions;
	private static final String QUEUESTOP = "STOP";
	
	public String getSessionID() {
		return sessionID;
	}
	
	public void setSessionID(String sessionID) 
	{
		this.sessionID = sessionID;
	}
	
	public String getBaseUri() 
	{
		return baseUri;
	}
	
	public void setBaseUri(String baseUri) 
	{
		this.baseUri = baseUri;
	}
	
	public String getTokenUriPath() 
	{
		return tokenUriPath;
	}
	
	public void setTokenUriPath(String tokenUriPath) 
	{
		this.tokenUriPath = tokenUriPath;
	}
	
	public String getCallbackUriPath() 
	{
		return callbackUriPath;
	}
	
	public void setCallbackUriPath(String callbackUriPath) 
	{
		this.callbackUriPath = callbackUriPath;
	}
	
	public List<? extends Transaction> getTransactions() 
	{
		return transactions;
	}
	
	public void setTransactions(List<? extends Transaction> transactions) 
	{
		this.transactions = transactions;
	}
	
	public static NotificationEvent makePoisonPill()
	{
		NotificationEvent pill = new NotificationEvent();
		pill.setSessionID(QUEUESTOP);
		pill.setBaseUri(QUEUESTOP);
		pill.setTokenUriPath(QUEUESTOP);
		pill.setCallbackUriPath(QUEUESTOP);
		return pill;
	}
	
	public boolean isPoisonedPill()
	{
		return this.getSessionID().equals(QUEUESTOP);
	}
	
}
