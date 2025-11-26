package hxc.connectors.ui;

import java.io.ByteArrayOutputStream;
import java.util.List;

import hxc.connectors.ui.sessionman.UiSessionManager;
import hxc.servicebus.IServiceBus;
import hxc.services.reporting.IReport;
import hxc.services.reporting.IReportingService;
import hxc.services.reporting.ReportParameters;
import hxc.services.security.IUser;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.reports.BinaryReportResponse;
import hxc.utils.protocol.uiconnector.reports.GenerateReportRequest;
import hxc.utils.protocol.uiconnector.reports.GetAvailableReports;
import hxc.utils.protocol.uiconnector.reports.GetAvailableReportsResponse;
import hxc.utils.protocol.uiconnector.reports.GetReportparametersRequest;
import hxc.utils.protocol.uiconnector.reports.GetReportparametersResponse;
import hxc.utils.protocol.uiconnector.reports.HtmlReportResponse;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;

public class UiReportsController
{
	private IServiceBus esb;
	
	public UiReportsController(IServiceBus esb, UiSessionManager sessionManager)
	{
		this.esb = esb;
	}

	private IReport[] extractAvailableReports()
	{
		IReportingService reportService = esb.getFirstService(IReportingService.class);
		return reportService.getReports();
	}

	private IReport findReport(String reportName)
	{

		for (IReport report : extractAvailableReports())
		{
			if (report.getName().equals(reportName))
			{
				return report;
			}
		}
		return null;
	}

	public GetAvailableReportsResponse getAvailableReports(GetAvailableReports request) throws Exception
	{
		GetAvailableReportsResponse result = new GetAvailableReportsResponse(request.getUserId(), request.getSessionId());
		IReport[] reports = extractAvailableReports();
		for (IReport rep : reports)
		{
			result.getAvailableReports().add(rep.getName());
		}

		return result;
	}

	public GetReportparametersResponse getReportParameters(GetReportparametersRequest request, IUser user) throws Exception
	{
		GetReportparametersResponse result = null;
		IReport report = findReport(request.getName());

		result = new GetReportparametersResponse(request.getUserId(), request.getSessionId());
		ConfigurableResponseParam[] cparms = (new UiUtils()).extractParameters(report.getDefaultParameters());
		result.setParams(cparms);

		return result;
	}

	private ReportParameters prepareReport(IReport report, List<IConfigurableParam> fields) throws Exception
	{
		ReportParameters reportParms = report.getDefaultParameters();
		UiUtils utils = new UiUtils();
		utils.populateObjectParameters(reportParms, fields);
		return reportParms;
	}

	public HtmlReportResponse generateHtmlReport(GenerateReportRequest request) throws Exception
	{
		IReport report = findReport(request.getReportName());
		ReportParameters reportParms = prepareReport(report, request.getFields());

		HtmlReportResponse response = new HtmlReportResponse(request.getUserId(), request.getSessionId());
		String html = report.getHtml(reportParms);
		if (html == null)
			html = generateNullHtmlReport();
		response.setHtml(html);

		return response;
	}

	public BinaryReportResponse generateBinaryReport(GenerateReportRequest request) throws Exception
	{
		IReport report = findReport(request.getReportName());
		ReportParameters reportParms = prepareReport(report, request.getFields());

		BinaryReportResponse response = new BinaryReportResponse(request.getUserId(), request.getSessionId());
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		switch (request.getReportType())
		{
			case PDF:
				report.downloadPdf(bos, reportParms);
				break;
			case EXCEL:
				report.downloadExcel(bos, reportParms);
				break;
			default:
				break;
		}

		byte[] byteArray = bos.toByteArray();
		response.setReportBytes(byteArray);
		return response;
	}

	private String generateNullHtmlReport()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("<b>No Report Data was returned</b>");

		return sb.toString();
	}
}
