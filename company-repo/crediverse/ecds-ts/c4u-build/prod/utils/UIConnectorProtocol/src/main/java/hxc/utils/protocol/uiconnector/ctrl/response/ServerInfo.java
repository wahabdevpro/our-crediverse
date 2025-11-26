package hxc.utils.protocol.uiconnector.ctrl.response;

import hxc.connectors.ctrl.IServerInfo;

public class ServerInfo implements IServerInfo
{

	private static final long serialVersionUID = 4660330394144395407L;
	private String serverHost;
	private String peerHost;
	private String transactionNumberPrefix;

	public ServerInfo()
	{
	}

	public ServerInfo(String serverHost, String peerHost, String transactionNumberPrefix)
	{
		this.serverHost = serverHost;
		this.peerHost = peerHost;
		this.transactionNumberPrefix = transactionNumberPrefix;
	}

	@Override
	public String getServerHost()
	{
		return serverHost;
	}

	@Override
	public String getPeerHost()
	{
		return peerHost;
	}

	/**
	 * @param serverHost
	 *            the serverHost to set
	 */
	public void setServerHost(String serverHost)
	{
		this.serverHost = serverHost;
	}

	/**
	 * @param peerHost
	 *            the peerHost to set
	 */
	public void setPeerHost(String peerHost)
	{
		this.peerHost = peerHost;
	}

	@Override
	public String getTransactionNumberPrefix()
	{
		return transactionNumberPrefix;
	}

	public void setTransactionNumberPrefix(String transactionNumberPrefix)
	{
		this.transactionNumberPrefix = transactionNumberPrefix;
	}

}
