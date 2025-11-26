package hxc.userinterfaces.gui.controller.reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import hxc.userinterfaces.gui.data.GuiUpdateResponse;
import hxc.userinterfaces.gui.data.GuiUpdateResponse.OperationStatus;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.userinterfaces.gui.utils.GuiUtils;
import hxc.utils.calendar.DateRange;
import hxc.utils.protocol.uiconnector.reports.BinaryReportResponse;
import hxc.utils.protocol.uiconnector.reports.GenerateReportRequest.ReportType;
import hxc.utils.protocol.uiconnector.reports.GetAvailableReportsResponse;
import hxc.utils.protocol.uiconnector.reports.GetReportparametersResponse;
import hxc.utils.protocol.uiconnector.reports.HtmlReportResponse;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;

//reporting
public class ReportingController implements IThymeleafController
{

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{
		String page = null;
		String content = null;
		WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		HttpSession session = request.getSession(true);
		User user = (User) session.getAttribute("user");

		String action = request.getParameter("act");

		if (action != null)
		{
			if (action.equalsIgnoreCase("avail"))
			{
				try
				{
					GetAvailableReportsResponse availReports = UiConnectionClient.getInstance().getAvailableReports(user);
					content = createReportsListJson(availReports);
				}
				catch (Exception e)
				{
					content = (new GuiUpdateResponse(OperationStatus.fail, "Failed to retrieve available reports with error: " + e.getMessage())).toString();
				}
			}
			else if (action.equalsIgnoreCase("parms"))
			{
				String reportName = request.getParameter("name");
				GetReportparametersResponse repParmsResponse = UiConnectionClient.getInstance().getReportParameters(user, reportName);
				content = createReportParametersJson(repParmsResponse);
			}
			else if (action.equalsIgnoreCase("rep"))
			{
				response.setHeader("Cache-Control", "no-store");
				response.setHeader("Pragma", "private");

				String reportName = request.getParameter("name");
				String reportType = request.getParameter("type");
				Map<String, String> fields = new HashMap<>();

				for (String prm : request.getParameterMap().keySet())
				{
					if (!(prm.equals("act") || prm.equals("type") || prm.equals("name")))
					{
						fields.put(prm, request.getParameterMap().get(prm)[0]);
					}
				}
				if (reportType.equalsIgnoreCase("html"))
				{
					response.setContentType("text/html");
					HtmlReportResponse htmlResponse = UiConnectionClient.getInstance().generateHtmlReport(user, reportName, fields);
					content = htmlResponse.getHtml();
				}
				else
				{
					SimpleDateFormat sdf = new SimpleDateFormat("dd MMM YYYY");
					String fileName = reportName + " created on " + sdf.format(new Date());
					fileName = fileName.replace(' ', '_');

					ReportType rt = reportType.equalsIgnoreCase("pdf") ? ReportType.PDF : ReportType.EXCEL;
					BinaryReportResponse binReport = UiConnectionClient.getInstance().generateBinaryReport(user, reportName, fields, rt);
					ServletOutputStream output = response.getOutputStream();
					response.setContentType("application/octet-stream");

					switch (rt)
					{
						case PDF:
							response.setHeader("Content-Disposition", "Attachment; Filename=" + fileName + ".pdf;");
							// response.setContentType("application/pdf");
							break;
						case EXCEL:
							response.setHeader("Content-disposition", "Attachment; Filename=" + fileName + ".xls;");
							// response.setContentType("application/xls");
							break;
					}
					output.write(binReport.getReportBytes(), 0, binReport.getReportBytes().length);
					output.close();
					output.flush();
				}

			}
		}

		if (content != null)
			sendTextResponse(response, content);

	}

	private void sendTextResponse(HttpServletResponse response, String htmlString) throws IOException
	{
		PrintWriter out = response.getWriter();
		out.print(htmlString);
		out.flush();
	}

	/**
	 * Json available reports list
	 * 
	 * @param availReports
	 * @return
	 */
	private String createReportsListJson(GetAvailableReportsResponse availReports)
	{
		JsonObject job = new JsonObject();
		JsonArray jarr = new JsonArray();
		for (String name : availReports.availableReports)
		{
			jarr.add(new JsonPrimitive(name));
		}
		job.add("reports", jarr);
		return job.toString();
	}

	/**
	 * Parameters for Report Modal
	 * 
	 * @param repParmsResponse
	 * @return
	 */
	private String createReportParametersJson(GetReportparametersResponse repParmsResponse)
	{
		JsonObject job = new JsonObject();
		JsonArray jarr = new JsonArray();
		if (repParmsResponse.getParams() != null && repParmsResponse.getParams().length > 0)
		{
			for (ConfigurableResponseParam param : repParmsResponse.getParams())
			{
				JsonObject jo = new JsonObject();
				jo.add("field", new JsonPrimitive(param.getFieldName()));
				jo.add("type", new JsonPrimitive(param.getValueType()));

				if (param.getDescription() == null)
					jo.add("desc", new JsonPrimitive(GuiUtils.splitCamelCaseString(param.getFieldName())));
				else
					jo.add("desc", new JsonPrimitive(param.getDescription()));

				if (param.getValueType().startsWith("DateRange"))
				{
					JsonArray ja = new JsonArray();
					for (DateRange dateRange : DateRange.GetAllRanges())
					{
						String str = String.format("%s:%s", dateRange.getPeriod(), dateRange.getName());
						ja.add(new JsonPrimitive(str));
					}
					jo.add("sel", ja);
					jo.add("dateFormat", new JsonPrimitive(GuiUtils.extractLocaleDateFormat()));
				}
				else
				{
					if (param.getPossibleValues() != null)
					{
						Arrays.sort(param.getPossibleValues());
						JsonArray ja = new JsonArray();
						for (String val : param.getPossibleValues())
						{
							ja.add(new JsonPrimitive(val));
						}

						jo.add("sel", ja);
					}
				}

				jarr.add(jo);
			}
		}
		job.add("fields", jarr);

		return job.toString();
	}

}
