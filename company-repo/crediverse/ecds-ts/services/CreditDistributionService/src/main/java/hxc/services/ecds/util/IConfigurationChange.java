package hxc.services.ecds.util;

import hxc.ecds.protocol.rest.config.IConfiguration;

public interface IConfigurationChange
{
	public abstract void onConfigurationChanged(IConfiguration configuration);
}
