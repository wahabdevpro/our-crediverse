package hxc.utils.protocol.uiconnector.bam;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class GetAvailablePluginMetricsResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -3982561948797378526L;
	private MetricPlugin metricPlugins[];

	public GetAvailablePluginMetricsResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public void setMetricPlugins(MetricPlugin metricPlugins[])
	{
		this.metricPlugins = metricPlugins;
	}

	public MetricPlugin[] getMetricPlugins()
	{
		return metricPlugins;
	}
}
