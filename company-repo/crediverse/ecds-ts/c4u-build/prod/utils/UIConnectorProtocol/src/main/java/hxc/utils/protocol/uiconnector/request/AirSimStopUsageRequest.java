package hxc.utils.protocol.uiconnector.request;

public class AirSimStopUsageRequest extends UiBaseRequest
{
	
	private static final long serialVersionUID = -3782334302741235114L;
	private String msisdn = null;
	
	public AirSimStopUsageRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public String getMsisdn()
	{
		return msisdn;
	}

	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
	}
	
}
