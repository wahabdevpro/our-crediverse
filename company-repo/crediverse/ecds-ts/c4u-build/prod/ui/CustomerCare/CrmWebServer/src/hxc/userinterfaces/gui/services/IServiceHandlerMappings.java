package hxc.userinterfaces.gui.services;

import java.util.Map;

public interface IServiceHandlerMappings
{
	public void registerServiceController(String serviceId, String controllerURL);

	public Map<String, String> getServiceControlers();
}
