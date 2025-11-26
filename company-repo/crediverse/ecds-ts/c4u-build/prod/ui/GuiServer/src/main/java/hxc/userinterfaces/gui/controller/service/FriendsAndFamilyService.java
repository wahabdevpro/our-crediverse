package hxc.userinterfaces.gui.controller.service;

import hxc.userinterfaces.gui.controller.templates.ComplexConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FriendsAndFamilyService extends ComplexConfiguration
{
	final static Logger logger = LoggerFactory.getLogger(FriendsAndFamilyService.class);

	@Override
	public void handle(String component, String action)
	{

	}

	@Override
	public String getComponentPage(String component)
	{
		logger.info("Calling getComponentPage:" + component);		
		if (component.equalsIgnoreCase("Variants"))
			return "friendsandfamily/variants";
		else if (component.equalsIgnoreCase("ServiceClasses"))
			return "friendsandfamily/serviceclasses";		
		else
			return "unknown Page: " + component;
	}

}
