package hxc.ecds.protocol.rest;

public interface ICoSignFor
{
	public String getCoSignForSessionID();
	public ICoSignFor setCoSignForSessionID(String coSignForSessionID);

	public String getCoSignatoryTransactionID();
	public ICoSignFor setCoSignatoryTransactionID(String coSignatoryTransactionID);
}
