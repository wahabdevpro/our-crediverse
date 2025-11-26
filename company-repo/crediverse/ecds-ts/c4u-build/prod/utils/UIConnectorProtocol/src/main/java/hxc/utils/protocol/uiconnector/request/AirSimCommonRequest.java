package hxc.utils.protocol.uiconnector.request;

public class AirSimCommonRequest	extends UiBaseRequest
{
	private static final long serialVersionUID = 9120104549019041071L;

	public static enum AirSimRequestType 
	{
		ClearHistory,
		ClearEmailHistory;
	}
	
	private String [] parameters = null;
	private AirSimRequestType airSimRequestType = AirSimRequestType.ClearHistory;
	
	public AirSimCommonRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}
	
	public AirSimRequestType getAirSimRequestType()
	{
		return this.airSimRequestType;
	}

	public void setAirSimRequestType(AirSimRequestType airSimRequestType)
	{
		this.airSimRequestType = airSimRequestType;
	}

	public String [] getParameters()
	{
		return this.parameters;
	}
	
	public void setParameters(String [] parameters)
	{
		this.parameters = parameters;
	}
	
}
