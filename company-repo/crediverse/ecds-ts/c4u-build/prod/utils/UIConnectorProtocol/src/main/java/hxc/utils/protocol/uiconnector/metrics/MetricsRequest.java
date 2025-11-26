package hxc.utils.protocol.uiconnector.metrics;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class MetricsRequest extends UiBaseRequest
{
	public static enum MetricsRequestType
	{
		AvailableMetrics, RegisterPluginMetricsMonitor
	}

	private static final long serialVersionUID = -6986247609221779843L;
	private MetricsRequestType requestType;

	private long pluginUID;
	private String metricName;

	public MetricsRequest(String userId, String sessionId, MetricsRequestType requestType)
	{
		super(userId, sessionId);
		this.requestType = requestType;
	}

	/**
	 * @return the requestType
	 */
	public MetricsRequestType getRequestType()
	{
		return requestType;
	}

	/**
	 * @param requestType
	 *            the requestType to set
	 */
	public void setRequestType(MetricsRequestType requestType)
	{
		this.requestType = requestType;
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
	 * @return the metricName
	 */
	public String getMetricName()
	{
		return metricName;
	}

	/**
	 * @param metricName
	 *            the metricName to set
	 */
	public void setMetricName(String metricName)
	{
		this.metricName = metricName;
	}

}
