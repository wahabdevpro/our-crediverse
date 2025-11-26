package hxc.utils.protocol.uiconnector.request;

public class GetLicenseDetailsRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 5749730631562482630L;

	public GetLicenseDetailsRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}
}
