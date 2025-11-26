package hxc.connectors;

import java.io.IOException;

import hxc.servicebus.IPlugin;

public interface IConnector extends IPlugin
{
	public abstract IConnection getConnection(String optionalConnectionString) throws IOException;

}
