package hxc.userinterfaces.gui.controller.monitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import hxc.userinterfaces.gui.data.GuiUpdateResponse;
import hxc.userinterfaces.gui.data.GuiUpdateResponse.OperationStatus;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.utils.protocol.uiconnector.logtailer.LogDTO;
import hxc.utils.protocol.uiconnector.logtailer.LogFileResponse;
import hxc.utils.protocol.uiconnector.logtailer.LogFilterOptions;

public class LogFileViewer implements IThymeleafController
{
	public static final boolean LOG_AVAILABLE = true;
	public static final String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss"; // 07/10/2014 [09:05]

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{
		WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		HttpSession session = request.getSession(false);
		if (session == null)
		{
			session = request.getSession(true);
		}

		User user = (User) session.getAttribute("user");

		boolean inValidateSession = (user == null);

		if (inValidateSession)
		{
			session.setAttribute("user", null);
			response.sendRedirect("/");
			return;
		}

		String page = null;
		String json = null;

		String action = request.getParameter("act");
		if (action != null)
		{
			if (action.equals("ret"))
			{

				String host = request.getParameter("host");
				String startDate = request.getParameter("startDate");
				String endDate = request.getParameter("endDate");
				String searchText = request.getParameter("search");
				String tinfo = request.getParameter("tinfo"); // tail info (format host:position,host:postion), e.g. hxc6:1502
				boolean tailSelected = extractBoolean(request, "tail");

				List<LoggingLevels> llevels = new ArrayList<>();
				for (LoggingLevels level : LoggingLevels.values())
				{
					String varString = "is" + level.name().substring(0, 1).toUpperCase() + level.name().substring(1).toLowerCase();
					String value = request.getParameter(varString);
					if (value != null)
					{
						boolean bValue = false;
						try
						{
							bValue = Boolean.parseBoolean(value);
							if (bValue)
							{
								llevels.add(level);
							}
						}
						catch (Exception e)
						{
						}
					}
				}

				LoggingLevels[] severityLevels = llevels.toArray(new LoggingLevels[llevels.size()]);

				boolean tail = false;
				Map<String, Integer> hostReadPostions = null;
				if (tinfo != null)
				{
					tail = true;
					hostReadPostions = new HashMap<>();
					String[] tpos = tinfo.split(",");
					for (String pos : tpos)
					{
						String[] harr = pos.split(":");
						if (harr.length == 2)
						{
							try
							{
								int rpos = Integer.parseInt(harr[1]);
								hostReadPostions.put(harr[0], rpos);
							}
							catch (Exception e)
							{
							}
						}

					}
				}

				List<String> hosts = new ArrayList<>();
				if (host.equals("all"))
				{
					hosts = UiConnectionClient.getInstance().extractServerList(user);
					if (hosts.size() == 0)
					{
						InetAddress ip = InetAddress.getLocalHost();
						hosts.add(ip.getHostName());
					}

				}
				else
				{
					hosts.add(host);
				}

				try
				{
					List<LogDTO> data = new ArrayList<LogDTO>();
					Map<String, Integer> hostLastReads = new HashMap<>();

					// Common information
					SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
					Date fromDate = (startDate == null || startDate.length() == 0) ? null : sdf.parse(startDate);
					Date toDate = null;

					if (!tailSelected)
						toDate = (endDate == null || endDate.length() == 0) ? null : sdf.parse(endDate);

					// Configure Filter
					LogFilterOptions filter = new LogFilterOptions();
					filter.setOnlyNonBlankTID(extractBoolean(request, "onlyNonBlankTID"));
					filter.setStartDate(fromDate);
					filter.setEndDate(toDate);
					filter.setLoggingLevels(severityLevels);
					filter.setText(searchText);

					for (String hostServer : hosts)
					{
						LogFileResponse logResponse = null;
						try
						{
							if (hostReadPostions != null)
							{
								int position = hostReadPostions.containsKey(hostServer) ? hostReadPostions.get(hostServer) : 0;
								logResponse = UiConnectionClient.getInstance().retrieveLogFileInfo(user, hostServer, filter, position);
							}
							else
							{
								logResponse = UiConnectionClient.getInstance().retrieveLogFileInfo(user, hostServer, filter);
							}
						}
						catch (Exception e)
						{
							UiConnectionClient.getInstance().log(LoggingLevels.ERROR, String.format("Could not retrieve logs from %s, error: %s", hostServer, e.getMessage()));
						}

						if (logResponse != null)
						{
							data.addAll(logResponse.getLogRecords());
							hostLastReads.put(hostServer, logResponse.getLastPosition());
						}
					}

					// latestAtTop
					final boolean latestAtTop = extractBoolean(request, "latestAtTop");

					// Now put everything together
					Collections.sort(data, new Comparator<LogDTO>()
					{

						@Override
						public int compare(LogDTO o1, LogDTO o2)
						{
							if (latestAtTop)
								return o2.recordTime.compareTo(o1.recordTime);
							else
								return o1.recordTime.compareTo(o2.recordTime);
						}

					});

					json = extractLogJson(data, hostLastReads);
				}
				catch (Exception e)
				{
					GuiUpdateResponse resp = new GuiUpdateResponse(OperationStatus.fail, String.format("Extracting data failed: %s", e.getMessage()));
					json = resp.toString();
				}
			}
		}

		if (page != null)
			templateEngine.process(page, ctx, response.getWriter());
		else
			sendResponse(response, json);
	}

	private boolean extractBoolean(HttpServletRequest request, String field)
	{
		boolean result = false;
		String value = request.getParameter(field);
		if (value != null)
		{
			try
			{
				result = Boolean.parseBoolean(value);
			}
			catch (Exception e)
			{
			}
		}

		return result;
	}

	public void sendResponse(HttpServletResponse response, String htmlString) throws IOException
	{
		PrintWriter out = response.getWriter();
		out.print(htmlString);
		out.flush();
	}

	private String extractLogJson(List<LogDTO> records, Map<String, Integer> lastReadPositions)
	{
		JsonObject joList = new JsonObject();

		// Part 1: Data
		JsonArray jarr = new JsonArray();
		for (LogDTO log : records)
		{
			try
			{
				JsonArray jar = new JsonArray();
				jar.add(new JsonPrimitive(log.host));
				jar.add(new JsonPrimitive(log.recordTime));
				jar.add(new JsonPrimitive(log.severity));
				jar.add(new JsonPrimitive(log.transactionID));
				jar.add(new JsonPrimitive(log.component));
				jar.add(new JsonPrimitive(log.operation));
				jar.add(new JsonPrimitive(log.returnCode));
				jar.add(new JsonPrimitive(log.text));
				jarr.add(jar);
			}
			catch (Exception e)
			{
			}
		}
		joList.add("data", jarr);

		// Part 2: last positions
		JsonObject hosts = new JsonObject();
		if (lastReadPositions != null)
		{
			for (String host : lastReadPositions.keySet())
			{
				hosts.add(host, new JsonPrimitive(lastReadPositions.get(host)));
			}
			joList.add("hosts", hosts);
		}
		return joList.toString();
	}

}
