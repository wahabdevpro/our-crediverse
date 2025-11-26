package hxc.connectors.ui.utils;

import java.util.LinkedList;
import java.util.List;

import hxc.configuration.IConfiguration;
import hxc.servicebus.IPlugin;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.Phrase;

public abstract class ConfigurablesIterator
{
	private IServiceBus esb;

	public ConfigurablesIterator(IServiceBus esb, Object... refs)
	{
		this.esb = esb;
		this.refs = refs;
	}

	Object[] refs;

	/**
	 * Iterate through all the plugins and their configuration
	 * 
	 * @param recurse
	 */
	public void iterateThroughPlughinConfigurables(boolean recurse)
	{
		List<IPlugin> configList = new LinkedList<IPlugin>();
		configList.addAll(esb.getServices(IPlugin.class));
		configList.addAll(esb.getConnectors(IPlugin.class));
		configList.add((IPlugin) esb);

		for (IPlugin iplugin : configList)
		{
			if (iplugin != null)
			{
				IConfiguration iconfig = iplugin.getConfiguration();
				if (iconfig != null)
				{
					processConfiguration(iplugin, iconfig, "", this.refs);
					if (recurse)
					{
						recursiveGetConfigurable(iplugin, iconfig, "");
					}
				}
			}
		}
	}

	private void recursiveGetConfigurable(IPlugin iplugin, IConfiguration iconfig, String parentPath)
	{
		if (iconfig.getConfigurations() != null)
		{
			String thisPath = parentPath + ((parentPath.length() > 0) ? "." : "") + iconfig.getPath(Phrase.ENG) + ((iconfig.getPath(Phrase.ENG).length() > 0) ? "." : "") + iconfig.getName(Phrase.ENG);
			for (IConfiguration innerConfig : iconfig.getConfigurations())
			{
				processConfiguration(iplugin, innerConfig, thisPath, this.refs);
				recursiveGetConfigurable(iplugin, innerConfig, thisPath);
			}
		}
	}

	public abstract void processConfiguration(IPlugin iplugin, IConfiguration iconfig, String parentPath, Object... refs);
}