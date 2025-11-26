package hxc.utils.protocol.uiconnector.metrics;

import java.io.Serializable;

import hxc.utils.instrumentation.IMetric;

@SuppressWarnings("serial")
public class PluginMetrics implements Serializable
{
	private String pluginName;
	private long pluginUID;
	private IMetric[] metrics;

	/**
	 * @return the pluginName
	 */
	public String getPluginName()
	{
		return pluginName;
	}

	/**
	 * @param pluginName
	 *            the pluginName to set
	 */
	public void setPluginName(String pluginName)
	{
		this.pluginName = pluginName;
	}

	/**
	 * @return the pluginUID
	 */
	public long getPluginUID()
	{
		return pluginUID;
	}

	/**
	 * @param pluginUID
	 *            the pluginUID to set
	 */
	public void setPluginUID(long pluginUID)
	{
		this.pluginUID = pluginUID;
	}

	/**
	 * @return the metrics
	 */
	public IMetric[] getMetrics()
	{
		return metrics;
	}

	/**
	 * @param metrics
	 *            the metrics to set
	 */
	public void setMetrics(IMetric[] metrics)
	{
		this.metrics = metrics;
	}

}
