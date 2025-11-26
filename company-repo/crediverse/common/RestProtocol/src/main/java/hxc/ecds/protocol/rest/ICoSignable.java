package hxc.ecds.protocol.rest;

public interface ICoSignable
{
	public String getCoSignatorySessionID();
	public ICoSignable setCoSignatorySessionID(String coSignatorySessionID);

	public String getCoSignatoryTransactionID();
	public ICoSignable setCoSignatoryTransactionID(String coSignatoryTransactionID);

	public String getCoSignatoryOTP();
	public ICoSignable setCoSignatoryOTP(String coSignatoryOTP);
}
