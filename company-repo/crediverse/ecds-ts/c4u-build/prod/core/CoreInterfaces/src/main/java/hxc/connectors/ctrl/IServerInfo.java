package hxc.connectors.ctrl;

import java.io.Serializable;

public interface IServerInfo extends Serializable
{

	public abstract String getServerHost();

	public abstract String getPeerHost();

	public abstract String getTransactionNumberPrefix();

}