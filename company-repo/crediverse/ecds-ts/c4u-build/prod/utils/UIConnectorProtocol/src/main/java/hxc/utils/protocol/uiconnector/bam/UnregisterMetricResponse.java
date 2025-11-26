package hxc.utils.protocol.uiconnector.bam;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class UnregisterMetricResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -852278769401631479L;
	private boolean unregistered;

	public UnregisterMetricResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public void setUnregistered(boolean unregistered)
	{
		this.unregistered = unregistered;
	}

	public boolean isUnregistered()
	{
		return unregistered;
	}
}
