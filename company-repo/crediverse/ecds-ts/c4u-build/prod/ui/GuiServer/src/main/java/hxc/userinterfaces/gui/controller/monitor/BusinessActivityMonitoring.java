package hxc.userinterfaces.gui.controller.monitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import hxc.userinterfaces.gui.bam.BAMService;
import hxc.userinterfaces.gui.bam.BAMServices;
import hxc.userinterfaces.gui.data.GuiUpdateResponse;
import hxc.userinterfaces.gui.data.GuiUpdateResponse.OperationStatus;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.protocol.uiconnector.metrics.AvailableMetricsResponse;
import hxc.utils.protocol.uiconnector.metrics.MetricData;
import hxc.utils.protocol.uiconnector.metrics.MetricDataRecord;
import hxc.utils.protocol.uiconnector.metrics.MetricsDataResponse;
import hxc.utils.protocol.uiconnector.metrics.PluginMetrics;

public class BusinessActivityMonitoring implements IThymeleafController
{

	public static final boolean BAM_AVAILABLE = false;

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{
		String page = null;
		String json = null;
		WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		HttpSession session = request.getSession(true);
		User user = (User) session.getAttribute("user");

		String action = request.getParameter("act");

		if (action != null)
		{
			if (action.equalsIgnoreCase("reg"))
			{
				// Register metric to monitor
				String uid = request.getParameter("uid"); // Service ID
				String metricName = request.getParameter("metric"); // Metric Name
				long configurationUid = -1;
				try
				{
					configurationUid = Long.parseLong(uid);
				}
				catch (Exception e)
				{
				}
				GuiUpdateResponse resp = new GuiUpdateResponse();
				resp.setMessage("registered");
				if (configurationUid != 0)
				{
					boolean registered = UiConnectionClient.getInstance().registerBamMonitor(user, configurationUid, metricName);

					if (registered)
						resp.setStatus(OperationStatus.pass);
					else
						resp.setStatus(OperationStatus.fail);
				}
				else
				{
					resp.setStatus(OperationStatus.fail);
				}
				json = resp.toString();
			}
			else if (action.equalsIgnoreCase("avail"))
			{
				// Retrieve available metrics
				try
				{
					AvailableMetricsResponse availableMetrics = UiConnectionClient.getInstance().retrieveAvailableMetrics(user);
					if (availableMetrics.getPluginMetrics() == null || availableMetrics.getPluginMetrics().size() == 0)
						json = (new GuiUpdateResponse(OperationStatus.fail, "No Metrics availables")).toString();
					else
					{
						BAMServices services = new BAMServices();

						for (PluginMetrics pim : availableMetrics.getPluginMetrics())
						{
							for (IMetric metric : pim.getMetrics())
							{
								BAMService service = new BAMService(pim.getPluginName(), metric.getName(), pim.getPluginUID());
								service.setDimensions(metric.getDimensions());
								services.getServices().add(service);
							}
						}
						json = services.toString();
					}
				}
				catch (Exception e)
				{
					// Fail Message
					json = (new GuiUpdateResponse(OperationStatus.fail, "Unable to retrieve available metrics")).toString();
				}

			}
			else if (action.equals("poll"))
			{
				String lastms = request.getParameter("ms");
				long ms = 0;
				try
				{
					ms = Long.parseLong(lastms);
				}
				catch (Exception e)
				{
				}

				// Poll for latest data
				try
				{
					MetricsDataResponse metricData = UiConnectionClient.getInstance().getLatestMetricsData(user, ms);

					JsonArray jarr = new JsonArray();
					for (MetricData md : metricData.getMetricData())
					{
						JsonObject job = new JsonObject();
						job.add("uid", new JsonPrimitive(String.valueOf(md.getUid())));
						job.add("mtr", new JsonPrimitive(md.getName()));
						JsonArray mdrArr = new JsonArray();
						for (MetricDataRecord mdr : md.getRecords())
						{
							JsonObject jobMdr = new JsonObject();
							jobMdr.add("tm", new JsonPrimitive(String.valueOf(mdr.getTimeInMilliSecconds())));

							JsonArray jarrValues = new JsonArray();
							for (Object val : mdr.getValues())
							{
								if (val != null)
								{
									if (val.getClass().isArray())
									{
										JsonArray jDataArray = new JsonArray();
										for (int i = 0; i < Array.getLength(val); i++)
										{
											Object elementValue = Array.get(val, i);
											jDataArray.add(new JsonPrimitive(String.valueOf(elementValue)));
										}
										jarrValues.add(jDataArray);
									}
									else
									{
										jarrValues.add(new JsonPrimitive(val.toString()));
									}
								}
							}
							jobMdr.add("vals", jarrValues);
							mdrArr.add(jobMdr);
						}
						job.add("rec", mdrArr);
						jarr.add(job);
					}
					JsonObject jobMain = new JsonObject();
					jobMain.add("data", jarr);
					json = jobMain.toString();
				}
				catch (Exception e)
				{
					GuiUpdateResponse guiresp = new GuiUpdateResponse(OperationStatus.fail, "failed");
					json = guiresp.toString();
				}

			}
			else if (action.equals("data"))
			{
				// MetricsDataResponse metricData = UiConnectionClient.getInstance().getLatestMetrics(user);
				// JsonObject job = new JsonObject();
				// JsonArray jarr = new JsonArray();
				// // Random rnd = new Random(System.currentTimeMillis());
				//
				// for (MetricDataRecord md : metricData.getMetricData())
				// {
				// try
				// {
				// JsonArray jaDataPair = new JsonArray();
				// double tps = Double.valueOf(String.valueOf(md.getValues()[0]));
				// // tps = rnd.nextInt(100);
				// jaDataPair.add(new JsonPrimitive(md.getTimeInMilliSecconds()));
				// jaDataPair.add(new JsonPrimitive(tps));
				// jarr.add(jaDataPair);
				// }
				// catch (Exception e)
				// {
				// }
				// }
				// job.add("info", new JsonPrimitive("TPS"));
				// job.add("data", jarr);
				// json = job.toString();
			}
		}
		else
		{
			page = "bam";
		}

		if (page != null)
		{
			try
			{
				templateEngine.process(page, ctx, response.getWriter());
			}
			catch (Exception e)
			{
				throw e;
			}
		}
		else
		{
			sendResponse(response, json);
		}
	}

	private void sendResponse(HttpServletResponse response, String htmlString) throws IOException
	{
		PrintWriter out = response.getWriter();
		out.print(htmlString);
		out.flush();
	}
}
