package hxc.utils.protocol.uiconnector.reports;

import java.util.ArrayList;
import java.util.List;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class GetAvailableReportsResponse extends UiBaseResponse
{
	private static final long serialVersionUID = 6525897461697465519L;
	public List<String> availableReports = null;

	public GetAvailableReportsResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
		this.availableReports = new ArrayList<String>();
	}

	public List<String> getAvailableReports()
	{
		return availableReports;
	}

	public void setAvailableReports(List<String> availableReports)
	{
		this.availableReports = availableReports;
	}

}
