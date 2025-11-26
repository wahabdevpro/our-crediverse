package hxc.utils.protocol.uiconnector.reports;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class BinaryReportResponse extends UiBaseResponse
{

	private static final long serialVersionUID = 136714291866659968L;
	public String reportName;
	public byte[] reportBytes;

	public BinaryReportResponse(String userId, String sessionId)
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

	public byte[] getReportBytes()
	{
		return reportBytes;
	}

	public void setReportBytes(byte[] reportBytes)
	{
		this.reportBytes = reportBytes;
	}

}
