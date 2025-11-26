package hxc.utils.protocol.uiconnector.metrics;

import java.util.ArrayList;
import java.util.List;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class MetricsDataResponse extends UiBaseResponse
{

	private static final long serialVersionUID = 1830970968570731502L;
	private List<MetricData> metricData;

	public MetricsDataResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
		metricData = new ArrayList<>();
	}

	/**
	 * @return the metricData
	 */
	public List<MetricData> getMetricData()
	{
		return metricData;
	}

	/**
	 * @param metricData
	 *            the metricData to set
	 */
	public void setMetricData(List<MetricData> metricData)
	{
		this.metricData = metricData;
	}

}
