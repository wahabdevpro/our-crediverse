package hxc.utils.protocol.uiconnector.response;

public class AirSimStopUsageResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -7829476080665664898L;
	public boolean usageStopped;
	
	public AirSimStopUsageResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public boolean isUsageStopped()
	{
		return usageStopped;
	}

	public void setUsageStopped(boolean usageStopped)
	{
		this.usageStopped = usageStopped;
	}
	
	
}
