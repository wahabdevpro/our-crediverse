package hxc.utils.protocol.uiconnector.reports;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class GetReportparametersRequest extends UiBaseRequest
{

	private static final long serialVersionUID = -3621835302784353558L;
	private String name;

	public GetReportparametersRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

}
