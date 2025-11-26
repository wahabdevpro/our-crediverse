package hxc.userinterfaces.gui.services;

import java.util.HashMap;
import java.util.Map;

public class ServiceHandlerMappings implements IServiceHandlerMappings
{

	private Map<String, String> mappings = null;

	@Override
	public void registerServiceController(String serviceId, String controllerURL)
	{
		if (mappings == null)
			mappings = new HashMap<String, String>();

		mappings.put(serviceId, controllerURL);
	}

	@Override
	public Map<String, String> getServiceControlers()
	{
		return mappings;
	}

}
