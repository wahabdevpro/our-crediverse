package hxc.utils.protocol.uiconnector.response;

import hxc.utils.registration.IFacilityRegistration;

public class GetFacilityResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -2021753325661737856L;
	private IFacilityRegistration facility;

	public GetFacilityResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public void setFacility(IFacilityRegistration facility)
	{
		this.facility = facility;
	}

	public IFacilityRegistration getFacility()
	{
		return facility;
	}
}
