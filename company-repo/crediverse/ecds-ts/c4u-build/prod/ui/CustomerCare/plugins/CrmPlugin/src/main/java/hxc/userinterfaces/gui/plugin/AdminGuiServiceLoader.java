package hxc.userinterfaces.gui.plugin;

import java.util.Map;

import javax.naming.NamingException;

import hxc.userinterfaces.gui.services.ActService;
import hxc.userinterfaces.gui.services.GlobalVasService;
import hxc.userinterfaces.gui.services.IServiceHandlerMappings;
import hxc.userinterfaces.gui.services.LoginService;
import hxc.userinterfaces.gui.services.ServiceHandlerMappings;
import hxc.userinterfaces.gui.services.CreditSharingService;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.plus.jndi.Resource;

@ResourceLocation(virtual = "cc", actual = "res")
public class AdminGuiServiceLoader implements IPluginService
{

	public static Map<String, String> serviceControllers;

	@Override
	public void registerContextBaseService(Handler contextHandler)
	{
		System.out.println("Registering Customer Care Services");
		try
		{
			new Resource(contextHandler, "loginService", new LoginService());

			// Register Controllers agains Services
			IServiceHandlerMappings mappings = new ServiceHandlerMappings();
			mappings.registerServiceController("AutoXfr", "autoxfr");
			mappings.registerServiceController("GSA", "gsa");
			new Resource(contextHandler, "serviceMappings", mappings);

			// General Subscription services (For getting services)
			new Resource(contextHandler, "globalVasService", new GlobalVasService());

			// Service Specific services
			new Resource(contextHandler, "creditSharingService", new CreditSharingService());
			new Resource(contextHandler, "actService", new ActService());

		}
		catch (NamingException e)
		{
			e.printStackTrace();
		}
	}

}
