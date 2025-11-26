package hxc.utils.protocol.uiconnector.bam;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class GetMetricsRequest extends UiBaseRequest
{

	private static final long serialVersionUID = -933784498191291246L;
	private String uids[];
	private String metricNames[];

	public GetMetricsRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public void setUids(String uids[])
	{
		this.uids = uids;
	}

	public String[] getUids()
	{
		return uids;
	}

	public void setMetricNames(String metricNames[])
	{
		this.metricNames = metricNames;
	}

	public String[] getMetricNames()
	{
		return metricNames;
	}
}
