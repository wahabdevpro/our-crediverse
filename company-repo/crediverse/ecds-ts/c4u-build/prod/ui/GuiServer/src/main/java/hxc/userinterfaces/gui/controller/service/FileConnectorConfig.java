package hxc.userinterfaces.gui.controller.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import hxc.configuration.ValidationException;
import hxc.userinterfaces.gui.data.FileConnectorConfiguration;
import hxc.userinterfaces.gui.data.GuiUpdateResponse;
import hxc.userinterfaces.gui.data.GuiUpdateResponse.OperationStatus;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;

public class FileConnectorConfig implements IThymeleafController
{
	public static final String CONFIG_RECORD_VARIABLE = "configrecords";

	public static final String CONFIG_RECORD_OPTIONS = "fileTypes";
	// public static final String CONFIG_FileType_OPTIONS = "fileConfigValues";

	public static final String CONFIG_RECORD_STRUCTURE_VARIABLE = "configstruct";
	public static final String CONFIG_RECORD_FIELD = "configfield";

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{
		HttpSession session = request.getSession(true);
		GuiUpdateResponse guiResponse = null;
		String page = null;

		WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

		// Dialog parameters
		String action = (String) request.getParameter("act");

		if (action != null)
		{
			String ind = (String) request.getParameter("index");
			int index = (ind != null) ? Integer.parseInt(ind) : -1;

			FileConnectorConfiguration fileConfig = null;

			if (action.equals("add") || action.equals("upd"))
			{
				fileConfig = new FileConnectorConfiguration();
				fileConfig.setStrictlySequential(false); // Parameter from web is false when true
				for (String parm : request.getParameterMap().keySet())
				{
					fileConfig.importField(parm, request.getParameter(parm), true);
				}
			}
			try
			{
				switch (action)
				{
					case "struct":
						String structs = retrieveFileConfigDefaults(session);
						sendResponse(response, structs);
						return;
					case "add":
						addFileConfig(session, fileConfig);
						break;
					case "upd":
						updateFileConfigs(session, index, fileConfig);
						break;
					case "del":
						deleteFileConfig(session, index);
						break;
					case "data":
						// Data request
						String json = retrieveFileConfigs(session, index);
						sendResponse(response, json);
						return;
					case "refresh":
						ctx.setVariable(CONFIG_RECORD_VARIABLE, session.getAttribute(CONFIG_RECORD_VARIABLE));
						page = "fileconnector/configrecords";
						break;
				}
				guiResponse = new GuiUpdateResponse(OperationStatus.pass, "Operation successful");
			}
			catch (ValidationException ve)
			{
				guiResponse = new GuiUpdateResponse(OperationStatus.fail, ve.getMessage());
			}
			catch (Exception e)
			{
				guiResponse = new GuiUpdateResponse(OperationStatus.fail, String.format("General backend exception thrown: %d", e.getMessage()));
			}
		}

		if (page == null)
			sendResponse(response, guiResponse.toString());
		else
			templateEngine.process(page, ctx, response.getWriter());
	}

	private void addFileConfig(HttpSession session, FileConnectorConfiguration fileConfig) throws ValidationException
	{
		List<FileConnectorConfiguration> fileConfigs = retrieveFileConfigs(session);
		fileConfigs.add(fileConfig);
		persistFileConfigsLocally(session, fileConfigs);
	}

	private void updateFileConfigs(HttpSession session, int index, FileConnectorConfiguration fileConfig) throws ValidationException
	{
		List<FileConnectorConfiguration> fileConfigs = retrieveFileConfigs(session);
		// checkRepeat(serverRoles, serverRole, index);
		fileConfigs.set(index, fileConfig);
		persistFileConfigsLocally(session, fileConfigs);
	}

	private void deleteFileConfig(HttpSession session, int index) throws ValidationException
	{
		List<FileConnectorConfiguration> fileConfigs = retrieveFileConfigs(session);
		// if (fileConfigs.get(index).getServerRoleName().equals(serverRoleName))
		// {
		fileConfigs.remove(index);
		persistFileConfigsLocally(session, fileConfigs);
		// }
		// else
		// throw new ValidationException("Server Role Name could not be validated");
	}

	private String retrieveFileConfigs(HttpSession session, int index)
	{
		FileConnectorConfiguration[] fileConfig = (FileConnectorConfiguration[]) session.getAttribute(CONFIG_RECORD_VARIABLE);
		String result = null;
		if (fileConfig != null && index < fileConfig.length)
		{
			Gson gson = new Gson();
			result = gson.toJson(fileConfig[index]);
		}
		else
		{
			result = "{}";
		}
		return result;
	}

	public void sendResponse(HttpServletResponse response, String htmlString) throws IOException
	{
		PrintWriter out = response.getWriter();
		out.print(htmlString);
		out.flush();
	}

	// ---------------------------- COMMON STUFF -------------------------------------
	private List<FileConnectorConfiguration> retrieveFileConfigs(HttpSession session)
	{
		FileConnectorConfiguration[] fileConfig = (FileConnectorConfiguration[]) session.getAttribute(CONFIG_RECORD_VARIABLE);
		List<FileConnectorConfiguration> serverList = ((fileConfig == null) ? (new ArrayList<FileConnectorConfiguration>()) : (new ArrayList<>(Arrays.asList(fileConfig))));
		return serverList;
	}

	private void persistFileConfigsLocally(HttpSession session, List<FileConnectorConfiguration> fileConfigs)
	{
		FileConnectorConfiguration[] configs = fileConfigs.toArray(new FileConnectorConfiguration[fileConfigs.size()]);
		// sortRoles(roles);
		session.setAttribute(CONFIG_RECORD_VARIABLE, configs);
	}

	private String revertNulls(String value)
	{
		return (value == null) ? "" : value;
	}

	private String retrieveFileConfigDefaults(HttpSession session)
	{
		ConfigurableResponseParam[] parms = (ConfigurableResponseParam[]) session.getAttribute(CONFIG_RECORD_STRUCTURE_VARIABLE);

		JsonObject main = new JsonObject();
		JsonArray jarr = new JsonArray();
		if (parms != null)
		{
			for (ConfigurableResponseParam cp : parms)
			{
				String fieldName = cp.getFieldName().substring(0, 1).toLowerCase() + cp.getFieldName().substring(1);

				JsonObject job = new JsonObject();
				job.add("field", new JsonPrimitive(fieldName));
				job.add("description", new JsonPrimitive(revertNulls(cp.getDescription())));
				job.add("defaultValue", new JsonPrimitive(revertNulls(cp.getDefaultValue())));
				jarr.add(job);
			}
		}
		main.add("struct", jarr);
		return main.toString();
	}
}
