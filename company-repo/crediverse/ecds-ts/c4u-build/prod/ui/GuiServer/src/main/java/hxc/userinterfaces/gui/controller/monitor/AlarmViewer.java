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

import hxc.connectors.snmp.IAlarm;
import hxc.services.logging.LoggingLevels;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.utils.protocol.uiconnector.alarms.GetAlarmDataResponse;

public class AlarmViewer implements IThymeleafController
{

	public static boolean ALARMVIEWER_AVAILABLE = true;

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{
		// Create the variables
		String page = null;
		String json = null;
		WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		HttpSession session = request.getSession(true);
		User user = (User) session.getAttribute("user");

		// Get the action
		String action = request.getParameter("act");

		// Check if there is action
		if (action != null)
		{
			// Check if it is a poll action to get the alarm information
			if (action.equalsIgnoreCase("poll"))
			{
				// Get the host to check
				String host = request.getParameter("host");

				// Check if host is equal to null, then set host to null
				if (host != null && host.equalsIgnoreCase("null"))
					host = null;

				// Create the response
				GetAlarmDataResponse alarmData = null;

				try
				{
					// Get the alarm data
					alarmData = UiConnectionClient.getInstance().getLatestAlarms(user, host);
				}
				catch (Exception e)
				{
					if (! "undefined".equalsIgnoreCase(host))
						UiConnectionClient.getInstance().log(LoggingLevels.ERROR, "Could not retrieve alarms. " + e.toString());
				}

				// Ensure the alarm data is not null
				if (alarmData != null)
				{
					// Create the json object
					JsonObject jsonObj = new JsonObject();

					// Iterate through each alarm
					JsonArray jsonArr = new JsonArray();
					for (IAlarm alarm : alarmData.getIndicationAlarms())
					{
						// Add the alarm data to a json array
						JsonArray jsonArr2 = new JsonArray();
						jsonArr2.add(new JsonPrimitive(alarm.getName()));
						jsonArr2.add(new JsonPrimitive(alarm.getDescription()));
						jsonArr2.add(new JsonPrimitive(alarm.getSeverity().toString()));
						jsonArr2.add(new JsonPrimitive(alarm.getState().toString()));
						jsonArr2.add(new JsonPrimitive(alarm.getTimestamp().toString()));

						// Add the array to the alarms array
						jsonArr.add(jsonArr2);
					}

					// Add the alarms array to the json object
					jsonObj.add("indications", jsonArr);

					// If the host is null set it to the current host (Though this is invariably wrong!)
//					if (host == null)
//						host = HostInfo.getName();

					JsonArray jsonArr3 = new JsonArray();

					// Add the host to the json array
					if (host != null)
						jsonArr3.add(new JsonPrimitive(host));

					// Get the other available hosts
					for (String host2 : alarmData.getHosts())
						if (host == null || !host.equalsIgnoreCase(host2))
							jsonArr3.add(new JsonPrimitive(host2));

					// Add the available hosts to the json object
					jsonObj.add("hosts", jsonArr3);

					// Convert the json object to string
					json = jsonObj.toString();
				}
			}
		}
		else
		{
			page = "alarm";
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
