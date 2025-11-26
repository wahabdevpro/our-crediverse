package hxc.connectors.ctrl.protocol;

import hxc.configuration.ValidationException;
import hxc.connectors.ctrl.IServerInfo;
import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

@Table(name = "ct_server")
public class ServerInfo implements IServerInfo
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 856600798515605447L;

	@Column(primaryKey = true, maxLength = 64)
	private String serverHost;

	@Column(maxLength = 64, nullable = true)
	private String peerHost;

	private String transactionNumberPrefix = "99";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@Override
	public String getServerHost()
	{
		return serverHost;
	}

	public ServerInfo setServerHost(String serverHost) throws ValidationException
	{
		if (serverHost == null || serverHost.length() == 0 || serverHost.length() > 64)
			throw new ValidationException("Invalid Server Computer : %s", serverHost);
		this.serverHost = serverHost;
		return this;
	}

	@Override
	public String getPeerHost()
	{
		return peerHost;
	}

	public ServerInfo setPeerHost(String peerHost) throws ValidationException
	{
		if (peerHost == null || peerHost.length() == 0 || peerHost.length() > 64)
			throw new ValidationException("Invalid Peer Computer : %s", peerHost);
		this.peerHost = peerHost;
		return this;
	}

	@Override
	public String getTransactionNumberPrefix()
	{
		return transactionNumberPrefix;
	}

	public ServerInfo setTransactionNumberPrefix(String transactionNumberPrefix)
	{
		this.transactionNumberPrefix = transactionNumberPrefix;
		return this;
	}

	public ServerInfo()
	{
	}

	public ServerInfo(String serverHost, String peerHost, String transactionNumberPrefix) throws ValidationException
	{
		this.setServerHost(serverHost);
		this.setPeerHost(peerHost);
		this.setTransactionNumberPrefix(transactionNumberPrefix);
	}

	public static ServerInfo create(String serverHost, String peerHost, String transactionNumberPrefix) throws ValidationException
	{
		return new ServerInfo(serverHost, peerHost, transactionNumberPrefix);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@Override
	public String toString()
	{
		return String.format("%s->%s", peerHost, serverHost);
	}

}
