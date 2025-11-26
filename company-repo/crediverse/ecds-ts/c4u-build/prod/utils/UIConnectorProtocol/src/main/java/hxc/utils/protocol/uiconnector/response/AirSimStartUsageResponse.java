package hxc.utils.protocol.uiconnector.response;

public class AirSimStartUsageResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -8753848620673040936L;
	private boolean started = false;
	
	public AirSimStartUsageResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public boolean isStarted()
	{
		return started;
	}

	public void setStarted(boolean started)
	{
		this.started = started;
	}
	
}
