package hxc.userinterfaces.gui.controller.monitor;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import hxc.services.logging.LoggingLevels;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.utils.protocol.uiconnector.bam.Dimension;
import hxc.utils.protocol.uiconnector.bam.GetAvailablePluginMetricsResponse;
import hxc.utils.protocol.uiconnector.bam.GetMetricResponse;
import hxc.utils.protocol.uiconnector.bam.GetMetricsResponse;
import hxc.utils.protocol.uiconnector.bam.GetUserLayoutResponse;
import hxc.utils.protocol.uiconnector.bam.Metric;
import hxc.utils.protocol.uiconnector.bam.MetricPlugin;
import hxc.utils.protocol.uiconnector.bam.RegisterMetricResponse;
import hxc.utils.protocol.uiconnector.bam.UnregisterMetricResponse;

public class BamViewer implements IThymeleafController
{

	public static boolean BAMVIEWER_AVAILABLE = false;

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{
		// Create the variable
		String page = null;
		String json = null;
		WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		HttpSession session = request.getSession(true);
		User user = (User) session.getAttribute("user");

		// Check if the bam view is allowed
		if (!BAMVIEWER_AVAILABLE)
		{
			// If not return with an error message
			sendResponse(response, "<p>This feature is not availabe with current license.</p>");
			return;
		}

		// Get the current action for the page
		String action = request.getParameter("act");

		// Ensure it is not null
		if (action != null)
		{

			// Check if the action is to check the available plugins that have metrics
			if (action.equalsIgnoreCase("available"))
			{
				// Create the response
				GetAvailablePluginMetricsResponse availablePlugins = null;

				try
				{
					// Get the metrics
					availablePlugins = UiConnectionClient.getInstance().getAvailablePluginMetrics(user);
				}
				catch (Exception e)
				{
					UiConnectionClient.getInstance().log(LoggingLevels.ERROR, e.toString());
				}

				// Ensure the response is not null
				if (availablePlugins != null)
				{
					// Create a JSON object
					JsonObject jsonObj = new JsonObject();

					// Iterate through the plugins and add it to the array
					JsonArray jsonArr = new JsonArray();
					for (MetricPlugin plug : availablePlugins.getMetricPlugins())
					{
						jsonArr.add(new JsonPrimitive(plug.getName()));
					}

					// Add the array of plugins to the json object
					jsonObj.add("components", jsonArr);

					// Convert the json object to string
					json = jsonObj.toString();
				}
			}
			// Else check if the action is to get the available metrics from the plugin
			else if (action.equalsIgnoreCase("getmetric"))
			{

				// Get the plugin to check
				String component = request.getParameter("comp");

				// Create the response
				GetAvailablePluginMetricsResponse availablePlugins = null;

				try
				{
					// Get the available plugins
					availablePlugins = UiConnectionClient.getInstance().getAvailablePluginMetrics(user);
				}
				catch (Exception e)
				{
					UiConnectionClient.getInstance().log(LoggingLevels.ERROR, e.toString());
				}

				// Ensure the plugins is not null
				if (availablePlugins != null)
				{
					// Create the json object
					JsonObject jsonObj = new JsonObject();

					// Iterate through the plugins to find the one specified
					JsonArray jsonArr = new JsonArray();
					for (MetricPlugin plug : availablePlugins.getMetricPlugins())
					{
						// Compare the plugins
						if (plug.getName().equalsIgnoreCase(component))
						{
							// Iterate through the metrics the plugin offers
							for (String metric : plug.getMetrics())
							{
								// Add the plugin to the json array
								jsonArr.add(new JsonPrimitive(metric));
							}

							// Add the plugin uid to the json object
							jsonObj.add("uid", new JsonPrimitive(plug.getUid()));
							break;
						}
					}

					// Add the metrics array ot the json object
					jsonObj.add("metrics", jsonArr);

					// Convert the json object to string
					json = jsonObj.toString();
				}
			}
			// Else check if the action is to register the metric
			else if (action.equalsIgnoreCase("register"))
			{
				// Get the plugin identifier
				String uid = request.getParameter("uid");

				// Get the metric
				String metric = request.getParameter("met");

				// Ensure neither are null
				if (uid == null || uid.equalsIgnoreCase("null") || metric == null || metric.equalsIgnoreCase("null"))
				{
					return;
				}

				// Create the response
				RegisterMetricResponse registered = null;

				try
				{
					// Register the metric
					registered = UiConnectionClient.getInstance().registerMetric(user, uid, metric);
				}
				catch (Exception e)
				{
					UiConnectionClient.getInstance().log(LoggingLevels.ERROR, e.toString());
				}

				// Ensure the response is not null
				if (registered != null)
				{
					// Create a json object
					JsonObject jsonObj = new JsonObject();

					// Add the registered flag
					jsonObj.add("registered", new JsonPrimitive(registered.isRegistered()));

					// Convert the json object to string
					json = jsonObj.toString();
				}
			}
			// Else check if the action is to unregister the metric
			else if (action.equalsIgnoreCase("unregister"))
			{
				// Get the plugin identifier
				String uid = request.getParameter("uid");

				// Get the metric
				String metric = request.getParameter("met");

				// Ensure neither are null
				if (uid == null || uid.equalsIgnoreCase("null") || metric == null || metric.equalsIgnoreCase("null"))
				{
					return;
				}

				// Create the response
				UnregisterMetricResponse unregistered = null;

				try
				{
					// Unregister the metric
					unregistered = UiConnectionClient.getInstance().unregisterMetric(user, uid, metric);
				}
				catch (Exception e)
				{
					UiConnectionClient.getInstance().log(LoggingLevels.ERROR, e.toString());
				}

				// Ensure the response is not null
				if (unregistered != null)
				{
					// Create the json object
					JsonObject jsonObj = new JsonObject();

					// Add the unregistered flag
					jsonObj.add("unregistered", new JsonPrimitive(unregistered.isUnregistered()));

					// Convert the json object to string
					json = jsonObj.toString();
				}
			}
			// Else check if the action is to get the metric data
			else if (action.equalsIgnoreCase("metricdata"))
			{
				// Get the plugin identifier
				String uid = request.getParameter("uid");

				// Get the metric
				String met = request.getParameter("met");

				// Get the force flag
				boolean force = Boolean.parseBoolean(request.getParameter("force"));

				// Ensure neither are null
				if (uid == null || uid.equalsIgnoreCase("null") || met == null || met.equalsIgnoreCase("null"))
				{
					return;
				}

				// Create the response
				GetMetricResponse metric = null;

				try
				{
					// Get the metric data
					metric = UiConnectionClient.getInstance().getMetric(user, uid, met, force);
				}
				catch (Exception e)
				{
					UiConnectionClient.getInstance().log(LoggingLevels.ERROR, e.toString());
				}

				// Ensure the metric response is valid
				if (metric != null && metric.getMetric() != null)
				{
					// Create the json object
					JsonObject jsonObj = new JsonObject();

					// Iterate through the dimensions of the metric
					JsonArray jsonArr = new JsonArray();
					for (Dimension dimension : metric.getMetric().getDimensions())
					{
						// Ensure it is not null
						if (dimension == null)
							continue;

						// Add the dimension data
						JsonArray jsonArr2 = new JsonArray();
						jsonArr2.add(new JsonPrimitive(dimension.getName()));
						jsonArr2.add(new JsonPrimitive(dimension.getValue() != null ? dimension.getValue().toString() : "0"));
						jsonArr2.add(new JsonPrimitive(dimension.getUnits()));
						jsonArr.add(jsonArr2);
					}

					// Add the json array to the json object
					jsonObj.add("dimensions", jsonArr);

					// Convert the json object to string
					json = jsonObj.toString();
				}
			}
			// Else check if the action is to get the metrics data
			else if (action.equalsIgnoreCase("metricsdata"))
			{
				// Get the plugin identifiers
				String uids = request.getParameter("uids");

				// Get the metrics
				String mets = request.getParameter("mets");

				// Ensure neither are null
				if (uids == null || uids.equalsIgnoreCase("null") || mets == null || mets.equalsIgnoreCase("null"))
				{
					return;
				}

				// Create the response
				GetMetricsResponse metrics = null;

				try
				{
					// Get the metrics data
					metrics = UiConnectionClient.getInstance().getMetrics(user, uids.split(","), mets.split(","));
				}
				catch (Exception e)
				{
					UiConnectionClient.getInstance().log(LoggingLevels.ERROR, e.toString());
				}

				// Ensure the metrics response is not null
				if (metrics != null && metrics.getMetrics() != null && metrics.getMetrics().length > 0)
				{
					// Create the json object
					JsonObject jsonObj = new JsonObject();

					// Iterate through the metrics
					JsonArray jsonArr = new JsonArray();
					for (Metric metric : metrics.getMetrics())
					{
						JsonArray jsonArr2 = new JsonArray();

						// Iterate through the dimensions of the metric
						for (Dimension dimension : metric.getDimensions())
						{
							// Ensure dimension is not null
							if (dimension == null)
								continue;

							// Add the dimension data for the metric
							JsonArray jsonArr3 = new JsonArray();
							jsonArr3.add(new JsonPrimitive(dimension.getName()));
							jsonArr3.add(new JsonPrimitive(dimension.getValue() != null ? dimension.getValue().toString() : "0"));
							jsonArr3.add(new JsonPrimitive(dimension.getUnits()));

							// Add the dimension data to the array
							jsonArr2.add(jsonArr3);
						}

						// Add the metric to the json array
						jsonArr.add(jsonArr2);
					}

					// Add the array to the json object
					jsonObj.add("metrics", jsonArr);

					// Convert the json object to string
					json = jsonObj.toString();
				}
			}
			// Else check if the action is to load the layout
			else if (action.equalsIgnoreCase("loadlayout"))
			{
				// Create the response
				GetUserLayoutResponse userLayout = null;

				try
				{
					// Get the user layout
					userLayout = UiConnectionClient.getInstance().getUserLayout(user);
				}
				catch (Exception e)
				{
					UiConnectionClient.getInstance().log(LoggingLevels.ERROR, e.toString());
				}

				// Ensure the response is not null
				if (userLayout != null)
				{
					// Create the json object
					JsonObject jsonObj = new JsonObject();

					// Add the layout to the json object
					jsonObj.add("layout", new JsonPrimitive(userLayout.getLayout()));

					// Convert the json object to string
					json = jsonObj.toString();
				}
			}
			// Else check if the action is to save the layout
			else if (action.equalsIgnoreCase("savelayout"))
			{
				// Get the layout
				String layout = request.getParameter("layout");

				// Check that it is not null
				if (layout == null || layout.equalsIgnoreCase("null"))
				{
					return;
				}

				try
				{
					// Save the user layout
					UiConnectionClient.getInstance().setUserLayout(user, layout);
				}
				catch (Exception e)
				{
					UiConnectionClient.getInstance().log(LoggingLevels.ERROR, e.toString());
				}
			}
		}
		else
		{
			page = "bamview";
		}

		// Check if need to load the page
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
		out.println(htmlString);
		out.flush();
	}
}
