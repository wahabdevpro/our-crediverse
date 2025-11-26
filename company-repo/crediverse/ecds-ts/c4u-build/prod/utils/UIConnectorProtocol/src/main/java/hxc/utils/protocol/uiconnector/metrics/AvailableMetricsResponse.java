package hxc.utils.protocol.uiconnector.metrics;

import java.util.ArrayList;
import java.util.List;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class AvailableMetricsResponse extends UiBaseResponse
{

	private static final long serialVersionUID = 3517399449693839649L;
	private List<PluginMetrics> pluginMetrics;

	public AvailableMetricsResponse()
	{
		init();
	}

	public AvailableMetricsResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
		init();
	}

	private void init()
	{
		pluginMetrics = new ArrayList<>();
	}

	/**
	 * @return the pluginMetrics
	 */
	public List<PluginMetrics> getPluginMetrics()
	{
		return pluginMetrics;
	}

	/**
	 * @param pluginMetrics
	 *            the pluginMetrics to set
	 */
	public void setPluginMetrics(List<PluginMetrics> pluginMetrics)
	{
		this.pluginMetrics = pluginMetrics;
	}

}
