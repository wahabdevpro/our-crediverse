package hxc.userinterfaces.gui.controller.components;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import hxc.userinterfaces.gui.controller.manage.ServiceConfigurationLoader;
import hxc.userinterfaces.gui.controller.templates.ComplexConfiguration;
import hxc.userinterfaces.gui.data.GuiUpdateResponse;
import hxc.userinterfaces.gui.data.GuiUpdateResponse.OperationStatus;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.userinterfaces.gui.utils.GuiUtils;
import hxc.utils.protocol.uiconnector.response.BasicConfigurableParm;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;
import hxc.utils.protocol.uiconnector.vas.VasCommandsResponse;

public class VasCommandController implements IThymeleafController
{

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{
		HttpSession session = request.getSession(true);

		String content = null;
		try
		{
			String action = request.getParameter("act");
			String field = request.getParameter("field");

			switch (action)
			{
				case "table":
					content = createTable(session, field);
					break;
				case "process":
					content = allProcesses(session, field);
					break;
				case "cmds":
					long configUID = (long) session.getAttribute(ServiceConfigurationLoader.CURRENT_UID_VARIABLE);
					User user = (User) session.getAttribute("user");
					content = extractCommands(user, configUID);
					break;
				default:
					content = (new GuiUpdateResponse(OperationStatus.fail, String.format("Operation %s not supported", action))).toString();
			}

		}
		catch (Exception e)
		{
		}

		if (content == null)
		{
			content = (new GuiUpdateResponse(OperationStatus.fail, "Failed to retrieve vas commands")).toString();
		}

		GuiUtils.sendResponse(response, content);

	}

	private List<BasicConfigurableParm[]> extractConfig(HttpSession session, String fieldName)
	{
		Object obj = session.getAttribute(fieldName);
		if (obj != null)
			return ((List<BasicConfigurableParm[]>) obj);
		else
			return null;
	}

	private Object findFieldValue(BasicConfigurableParm[] parms, String fieldName)
	{
		for (BasicConfigurableParm bp : parms)
		{
			if (bp.getFieldName().equalsIgnoreCase(fieldName))
				return bp.getValue();
		}
		return null;
	}

	private String allProcesses(HttpSession session, String component)
	{
		JsonObject job = new JsonObject();
		ConfigurableResponseParam[] parms = (ConfigurableResponseParam[]) session.getAttribute(component + ComplexConfiguration.STRUCTURE_SUFFIX);
		for (ConfigurableResponseParam cp : parms)
		{
			if (cp.getFieldName().equalsIgnoreCase("Process"))
			{
				for (String value : cp.getPossibleValues())
				{
					job.add(value, new JsonPrimitive(GuiUtils.splitCamelCaseString(value)));
				}
			}
		}
		return job.toString();
	}

	/**
	 * 
	 * @param action
	 *            "edit", "view", "del"
	 * @param index
	 * @return
	 */
	private String createAction(String action, int index)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<a id='vas").append(action).append("_").append(index).append("' class='vas");
		sb.append(action).append("'>");
		sb.append("<span class='glyphicon ");
		if (action.equalsIgnoreCase("view"))
			sb.append("glyphicon-eye-open viewbutton vasviewcmd' title='view'></span></a>");
		else if (action.equalsIgnoreCase("edit"))
			sb.append("glyphicon-pencil editbutton vaseditcmd' title='edit'></span></a>");
		else if (action.equalsIgnoreCase("del"))
			sb.append("glyphicon-trash deletebutton vasdeletecmd' title='delete'></span></a>");

		return sb.toString();
	}

	private String createTable(HttpSession session, String fieldName)
	{
		StringBuilder sb = new StringBuilder();

		List<BasicConfigurableParm[]> parms = extractConfig(session, fieldName);
		if (parms == null)
		{
			sb.append("No configuration could be loaded");
		}
		else if (parms.size() == 0)
		{
			sb.append("<p>No VAS Commands (Input Sequences) currently configured. Click on + to add</p>");
		}
		else
		{
			// create table
			sb.append("<table id='vascommandtable' class='table infotable table-hover table-bordered table-striped' style='width: 100%; table-layout: fixed;'>");
			sb.append("<col width='20%' />");
			sb.append("<col width='70%' />");
			sb.append("<col width='120px' />");
			sb.append("<thead><tr>");
			sb.append("<th>Process</th>");
			sb.append("<th>Input Sequence</th>");
			sb.append("<th>Action</th>");
			sb.append("</tr></thread>");

			sb.append("<tbody>");

			for (int i = 0; i < parms.size(); i++)
			{
				String process = GuiUtils.splitCamelCaseString(findFieldValue(parms.get(i), "Process").toString());
				String command = (String) findFieldValue(parms.get(i), "Command");

				sb.append("<tr>");
				sb.append("<td>").append(process).append("</td>");
				sb.append("<td>").append(command).append("</td>");
				sb.append("<td>");

				// Actions
				sb.append(createAction("view", i));
				sb.append("<span>|</span>");
				sb.append(createAction("edit", i));
				sb.append("<span>|</span>");
				sb.append(createAction("del", i));

				sb.append("</td>");
				sb.append("</tr>");

			}

			sb.append("</tbody>");
		}

		return sb.toString();
	}

	private String extractCommands(User user, long configUID)
	{
		try
		{
			VasCommandsResponse vcr = UiConnectionClient.getInstance().extractVasCommands(user, configUID);

			JsonArray jarr = new JsonArray();
			if (vcr.getCommandVariables() != null)
			{
				for (String cmd : vcr.getCommandVariables())
				{
					jarr.add(new JsonPrimitive(cmd));
				}
			}
			JsonObject job = new JsonObject();
			job.add("cmds", jarr);
			return job.toString();
		}
		catch (Exception e)
		{
			GuiUpdateResponse resp = new GuiUpdateResponse(OperationStatus.fail, String.format("VAS retrival failed: %s", e.getMessage()));
			return resp.toString();
		}
	}
}
