package hxc.utils.protocol.uiconnector.bam;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class GetMetricRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 2767318643743015756L;
	private String uid;
	private String metricName;
	private boolean force;

	public GetMetricRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public void setUid(String uid)
	{
		this.uid = uid;
	}

	public String getUid()
	{
		return uid;
	}

	public void setMetricName(String metricName)
	{
		this.metricName = metricName;
	}

	public String getMetricName()
	{
		return metricName;
	}

	public void setForce(boolean force)
	{
		this.force = force;
	}

	public boolean isForce()
	{
		return force;
	}
}
