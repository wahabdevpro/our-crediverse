package hxc.utils.protocol.zte;

public interface IResponseHeader
{

	public abstract int getResponseCode();

	public abstract void setResponseCode(int responseCode);

	public abstract String getOriginTransactionID();

	public abstract void setOriginTransactionID(String originTransactionID);

	public abstract Integer[] getNegotiatedCapabilities();

	public abstract void setNegotiatedCapabilities(Integer[] negotiatedCapabilities);

	public abstract Integer[] getAvailableServerCapabilities();

	public abstract void setAvailableServerCapabilities(Integer[] availableServerCapabilities);

}