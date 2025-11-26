package hxc.userinterfaces.gui.controller.service;

import hxc.userinterfaces.gui.controller.templates.ComplexConfiguration;

public class GenericServiceConfig extends ComplexConfiguration
{

	@Override
	public void handle(String component, String action)
	{
		// Defaults handled by ComplexConfiguration
	}

	@Override
	public String getComponentPage(String component)
	{
		return null;
	}

}
