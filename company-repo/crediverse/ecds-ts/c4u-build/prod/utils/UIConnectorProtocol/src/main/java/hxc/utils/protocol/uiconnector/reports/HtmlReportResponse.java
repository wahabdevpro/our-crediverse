package hxc.utils.protocol.uiconnector.reports;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class HtmlReportResponse extends UiBaseResponse
{
	private static final long serialVersionUID = 8176959826949921202L;
	public String reportName;
	public String html;

	public HtmlReportResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public String getReportName()
	{
		return reportName;
	}

	public void setReportName(String reportName)
	{
		this.reportName = reportName;
	}

	public String getHtml()
	{
		return html;
	}

	public void setHtml(String html)
	{
		this.html = html;
	}

}
