package hxc.utils.protocol.uiconnector.bam;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class GetMetricsResponse extends UiBaseResponse
{

	private static final long serialVersionUID = 8265606304151532580L;
	private Metric metrics[];

	public GetMetricsResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public void setMetrics(Metric metrics[])
	{
		this.metrics = metrics;
	}

	public Metric[] getMetrics()
	{
		return metrics;
	}
}
