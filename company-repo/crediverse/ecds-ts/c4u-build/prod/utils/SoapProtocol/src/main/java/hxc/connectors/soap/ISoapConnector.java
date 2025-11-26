package hxc.connectors.soap;

import com.concurrent.hxc.IHxC;

public interface ISoapConnector
{
	public abstract IHxC getVasInterface();

	public abstract String getInternalCredentials();

	public abstract String getServiceName(String serviceID);
}
