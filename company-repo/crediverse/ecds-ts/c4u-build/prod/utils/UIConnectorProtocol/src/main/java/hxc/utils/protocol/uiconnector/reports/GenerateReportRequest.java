package hxc.utils.protocol.uiconnector.reports;

import java.util.List;

import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class GenerateReportRequest extends UiBaseRequest
{
	public enum ReportType
	{
		EXCEL, HTML, PDF
	}

	private static final long serialVersionUID = 6599044328504794019L;

	private String reportName;
	private ReportType reportType;
	private List<IConfigurableParam> fields;

	public GenerateReportRequest(String userId, String sessionId)
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

	public ReportType getReportType()
	{
		return reportType;
	}

	public void setReportType(ReportType reportType)
	{
		this.reportType = reportType;
	}

	public List<IConfigurableParam> getFields()
	{
		return fields;
	}

	public void setFields(List<IConfigurableParam> fields)
	{
		this.fields = fields;
	}

}
