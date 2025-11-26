package hxc.userinterfaces.gui.plugin;

import org.eclipse.jetty.server.Handler;

//import org.eclipse.jetty.server.Server;

public interface IPluginService
{
	// public void registerServerBasedService(Server server);
	public void registerContextBaseService(Handler contextHandler);
}
