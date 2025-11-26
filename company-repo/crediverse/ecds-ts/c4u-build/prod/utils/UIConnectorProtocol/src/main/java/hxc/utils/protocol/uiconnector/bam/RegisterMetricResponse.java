package hxc.utils.protocol.uiconnector.bam;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class RegisterMetricResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -2001270613570126037L;
	private boolean registered;

	public RegisterMetricResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public void setRegistered(boolean registered)
	{
		this.registered = registered;
	}

	public boolean isRegistered()
	{
		return registered;
	}
}
