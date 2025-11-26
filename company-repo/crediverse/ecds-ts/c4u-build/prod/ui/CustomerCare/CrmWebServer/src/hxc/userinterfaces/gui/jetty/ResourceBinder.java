package hxc.userinterfaces.gui.jetty;

import javax.naming.NamingException;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.plus.jndi.EnvEntry;

public class ResourceBinder
{

	public static void bindResources(Handler contextHandler) throws NamingException
	{
		// JVM Scope
		// new EnvEntry(null, "someValue", Double.valueOf(123), true);

		// Context Scope
		// new EnvEntry(contextHandler, "maxAmount", Double.valueOf(100), true);
		// new Resource(contextHandler, "loginService", new LoginService());

	}

	public static void bindServerScopeResources(Server jettyServer) throws NamingException
	{
		new EnvEntry(jettyServer, "maxAmount", Double.valueOf(100), true);
	}
}
