package hxc.ecds.protocol.rest;

import java.util.List;

public class RegisterTransactionNotificationRequest extends RequestHeader 
{
	private String baseUri;
	private String callbackUriPath;
	private String tokenUriPath;
	private int agentID;
	private String transactionNo;
	private int offset;
	private int limit;
	
	public String getBaseUri() {
		return baseUri;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public String getCallbackUriPath() {
		return callbackUriPath;
	}

	public void setCallbackUriPath(String callbackUriPath) {
		this.callbackUriPath = callbackUriPath;
	}

	public int getAgentID() {
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
	public List<Violation> validate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends ResponseHeader> T createResponse() {
		// TODO Auto-generated method stub
		return null;
	}

}
