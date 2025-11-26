package hxc.utils.protocol.uiconnector.request;

public class GetFacilityRequest extends UiBaseRequest
{

	private static final long serialVersionUID = -8393703540829764906L;
	private String facilityID;

	public GetFacilityRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public void setFacilityID(String facilityID)
	{
		this.facilityID = facilityID;
	}

	public String getFacilityID()
	{
		return facilityID;
	}
}
