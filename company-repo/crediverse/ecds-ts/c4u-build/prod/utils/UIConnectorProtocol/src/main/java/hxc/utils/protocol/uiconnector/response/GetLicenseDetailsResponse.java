package hxc.utils.protocol.uiconnector.response;

import hxc.utils.protocol.uiconnector.registration.Registration;

public class GetLicenseDetailsResponse extends UiBaseResponse
{

	private static final long serialVersionUID = 3758424547646074335L;
	private Registration licenseDetails;

	public GetLicenseDetailsResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public void setLicenseDetails(Registration licenseDetails)
	{
		this.licenseDetails = licenseDetails;
	}

	public Registration getLicenseDetails()
	{
		return licenseDetails;
	}
}
