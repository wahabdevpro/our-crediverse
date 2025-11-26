package hxc.userinterfaces.gui.controller.service;

import hxc.userinterfaces.gui.controller.templates.ComplexConfiguration;

public class CreditTransferService extends ComplexConfiguration
{
	/**
	 * General Configuration ReturnCodesTexts AllowedRecipientServiceClasses Variants
	 * 
	 * IConfigurableParam[] > Values ConfigurableResponseParam[] > Structure
	 */

	@Override
	public void handle(String component, String action)
	{
	}

	@Override
	public String getComponentPage(String component)
	{
		if (component.equalsIgnoreCase("Variants"))
			return "credittransfer/credittransfer";
		else
			return null;
	}

}
