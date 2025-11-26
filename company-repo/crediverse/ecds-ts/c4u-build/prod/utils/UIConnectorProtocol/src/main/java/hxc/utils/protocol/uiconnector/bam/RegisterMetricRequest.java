package hxc.utils.protocol.uiconnector.bam;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class RegisterMetricRequest extends UiBaseRequest
{

	private static final long serialVersionUID = -6764295251395277094L;
	private String uid;
	private String metricName;

	public RegisterMetricRequest(String userId, String sessionId)
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
}
