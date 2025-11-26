package hxc.utils.protocol.uiconnector.reports;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class GetAvailableReports extends UiBaseRequest
{

	private static final long serialVersionUID = -9026596209092737480L;

	public GetAvailableReports(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

}
