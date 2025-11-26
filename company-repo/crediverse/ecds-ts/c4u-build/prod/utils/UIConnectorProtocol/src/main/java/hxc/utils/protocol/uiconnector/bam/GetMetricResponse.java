package hxc.utils.protocol.uiconnector.bam;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class GetMetricResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -3524411334460197246L;
	private Metric metric;

	public GetMetricResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public void setMetric(Metric metric)
	{
		this.metric = metric;
	}

	public Metric getMetric()
	{
		return metric;
	}
}
