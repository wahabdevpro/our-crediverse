package hxc.userinterfaces.gui.controller.service;

import hxc.userinterfaces.gui.controller.templates.ComplexConfiguration;

public class PinService extends ComplexConfiguration
{

	@Override
	public void handle(String component, String action)
	{

	}

	@Override
	public String getComponentPage(String component)
	{
		if (component.equalsIgnoreCase("Variants"))
			return "pinservice/variants";
		else
			return null;
	}

}
