package hxc.userinterfaces.gui.controller.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import com.google.gson.Gson;

import hxc.configuration.ValidationException;
import hxc.userinterfaces.gui.data.GuiUpdateResponse;
import hxc.userinterfaces.gui.data.GuiUpdateResponse.OperationStatus;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerInfo;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerRole;

public class ControlService implements IThymeleafController
{

	public static final String SERVER_INFO_VARIABLE = "servers";
	public static final String SERVER_ROLES_VARIABLE = "serverroles";
	public static final String SERVER_ROLES_UPDATED_VARIABLE = "serverrolesupdated";
	public static final String SERVER_INFO_UPDATED_VARIABLE = "serverrolesupdated";

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{
		HttpSession session = request.getSession(true);
		GuiUpdateResponse guiResponse = null;
		String page = null;

		WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		// User user = (User) session.getAttribute("user");

		// Dialog parameters
		String component = (String) request.getParameter("comp");
		String action = (String) request.getParameter("act");

		if (component != null && action != null)
		{
			String ind = (String) request.getParameter("index");
			int index = (ind != null) ? Integer.parseInt(ind) : -1;

			try
			{
				if (component.equalsIgnoreCase("host"))
				{
					// Parameters from call
					String serverhost = (String) request.getParameter("serverhost");
					String peerhost = (String) request.getParameter("peerhost");
					String transactionNumberPrefix = (String) request.getParameter("transactionNumberPrefix");
					switch (action)
					{
						case "add":
							addServerHost(session, serverhost, peerhost, transactionNumberPrefix);
							break;
						case "upd":
							updateServerHost(session, index, serverhost, peerhost, transactionNumberPrefix);
							break;
						case "del":
							deleteServerHost(session, index, serverhost, peerhost);
							break;
						case "data":
							// Data request
							String json = retrieveServerHostData(session, index);
							sendResponse(response, json);
							return;
						case "refresh":
							ctx.setVariable(ControlService.SERVER_INFO_VARIABLE, session.getAttribute(ControlService.SERVER_INFO_VARIABLE));
							page = "controlservice/serverinfo";
							break;
					}
				}
				else if (component.equalsIgnoreCase("role"))
				{
					String roleName = (String) request.getParameter("roleName");
					boolean isExclusive = (request.getParameter("exclusive") != null);
					String attachCommand = (String) request.getParameter("attachCommand");
					String detachCommand = (String) request.getParameter("detachCommand");
					ServerRole serverRole = null;

					if (action.equalsIgnoreCase("add") || action.equalsIgnoreCase("upd"))
					{
						serverRole = new ServerRole(roleName, isExclusive, attachCommand, detachCommand);
					}

					switch (action)
					{
						case "add":
							addServerRole(session, serverRole);
							break;
						case "upd":
							updateServerRole(session, index, serverRole);
							break;
						case "del":
							deleteServerRole(session, index, roleName);
							break;
						case "data":
							String json = retrieveServerRolesData(session, index);
							sendResponse(response, json);
							return;
						case "refresh":
							ctx.setVariable(ControlService.SERVER_ROLES_VARIABLE, session.getAttribute(ControlService.SERVER_ROLES_VARIABLE));
							page = "controlservice/serverroles";
							break;
					}
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
		else
		{
			guiResponse = new GuiUpdateResponse(OperationStatus.fail, "Nothing ventured, nothing gained");
		}

		if (page == null)
			sendResponse(response, guiResponse.toString());
		else
			templateEngine.process(page, ctx, response.getWriter());
	}

	public void sendResponse(HttpServletResponse response, String htmlString) throws IOException
	{
		PrintWriter out = response.getWriter();
		out.print(htmlString);
		out.flush();
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// ServerInfo
	// ////////////////////////////////////////////////////////////////////////////////////
	private void addServerHost(HttpSession session, String serverHost, String peerHost, String transactionNumberPrefix) throws ValidationException
	{
		List<ServerInfo> serverList = retrieveServerHosts(session);
		ServerInfo si = new ServerInfo(serverHost, peerHost, transactionNumberPrefix);
		checkRepeat(serverList, si, -1);
		serverList.add(si);
		persistServerHostsLocally(session, serverList);
		session.setAttribute(SERVER_INFO_UPDATED_VARIABLE, true);
	}

	private void updateServerHost(HttpSession session, int index, String serverHost, String peerHost, String transactionNumberPrefix) throws ValidationException
	{
		List<ServerInfo> serverList = retrieveServerHosts(session);
		ServerInfo si = new ServerInfo(serverHost, peerHost, transactionNumberPrefix);
		checkRepeat(serverList, si, index);
		serverList.get(index).setPeerHost(peerHost);
		serverList.get(index).setServerHost(serverHost);
		serverList.get(index).setTransactionNumberPrefix(transactionNumberPrefix);

		persistServerHostsLocally(session, serverList);
		session.setAttribute(SERVER_INFO_UPDATED_VARIABLE, true);
	}

	private void deleteServerHost(HttpSession session, int index, String serverhost, String peerhost) throws ValidationException
	{
		List<ServerInfo> serverList = retrieveServerHosts(session);
		if (serverList.get(index).getPeerHost().equals(peerhost) && serverList.get(index).getServerHost().equals(serverhost))
		{
			serverList.remove(index);
			persistServerHostsLocally(session, serverList);
		}
		else
			throw new ValidationException("Host combination to be remove could not be validated");
		session.setAttribute(SERVER_INFO_UPDATED_VARIABLE, true);
	}

	private List<ServerInfo> retrieveServerHosts(HttpSession session)
	{
		ServerInfo[] servers = (ServerInfo[]) session.getAttribute(SERVER_INFO_VARIABLE);
		List<ServerInfo> serverList = ((servers == null) ? (new ArrayList<ServerInfo>()) : (new ArrayList<>(Arrays.asList(servers))));
		return serverList;
	}

	private void checkRepeat(List<ServerInfo> serverList, ServerInfo si, int repeateAllowedAtIndex) throws ValidationException
	{
		for (int i = 0; i < serverList.size(); i++)
		{
			if (serverList.get(i).getPeerHost().equalsIgnoreCase(si.getPeerHost()) && serverList.get(i).getServerHost().equalsIgnoreCase(si.getServerHost()) && i != repeateAllowedAtIndex)
			{
				throw new ValidationException("Combination of peer/host already exists");
			}
		}
	}

	private void persistServerHostsLocally(HttpSession session, List<ServerInfo> serverList)
	{
		ServerInfo[] servers = serverList.toArray(new ServerInfo[serverList.size()]);
		sortInfo(servers);
		session.setAttribute(SERVER_INFO_VARIABLE, servers);
	}

	private String retrieveServerHostData(HttpSession session, int index)
	{
		ServerInfo[] servers = (ServerInfo[]) session.getAttribute(SERVER_INFO_VARIABLE);
		String result = null;
		if (servers != null && index < servers.length)
		{
			Gson gson = new Gson();
			result = gson.toJson(servers[index]);
		}
		else
		{
			result = "{}";
		}
		return result;
	}

	private void sortInfo(ServerInfo[] servers)
	{
		if (servers != null && servers.length > 0)
		{
			Arrays.sort(servers, new Comparator<ServerInfo>()
			{

				@Override
				public int compare(ServerInfo s1, ServerInfo s2)
				{
					return s1.getServerHost().compareTo(s2.getServerHost());
				}
			});
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// ServerRole
	// ////////////////////////////////////////////////////////////////////////////////////

	private void addServerRole(HttpSession session, ServerRole serverRole) throws ValidationException
	{
		List<ServerRole> serverRoles = retrieveServerRoles(session);
		checkRepeat(serverRoles, serverRole, -1);
		serverRoles.add(serverRole);
		persistServerRolesLocally(session, serverRoles);
		session.setAttribute(SERVER_ROLES_UPDATED_VARIABLE, true);
	}

	private void updateServerRole(HttpSession session, int index, ServerRole serverRole) throws ValidationException
	{
		List<ServerRole> serverRoles = retrieveServerRoles(session);
		checkRepeat(serverRoles, serverRole, index);
		serverRoles.set(index, serverRole);
		persistServerRolesLocally(session, serverRoles);
		session.setAttribute(SERVER_ROLES_UPDATED_VARIABLE, true);
	}

	private void deleteServerRole(HttpSession session, int index, String serverRoleName) throws ValidationException
	{
		List<ServerRole> serverRoles = retrieveServerRoles(session);
		if (serverRoles.get(index).getServerRoleName().equals(serverRoleName))
		{
			serverRoles.remove(index);
			persistServerRolesLocally(session, serverRoles);
		}
		else
			throw new ValidationException("Server Role Name could not be validated");
		session.setAttribute(SERVER_ROLES_UPDATED_VARIABLE, true);
	}

	private List<ServerRole> retrieveServerRoles(HttpSession session)
	{
		ServerRole[] roles = (ServerRole[]) session.getAttribute(SERVER_ROLES_VARIABLE);
		List<ServerRole> serverRoles = ((roles == null) ? (new ArrayList<ServerRole>()) : (new ArrayList<ServerRole>(Arrays.asList(roles))));
		return serverRoles;
	}

	private void checkRepeat(List<ServerRole> serverList, ServerRole sr, int repeateAllowedAtIndex) throws ValidationException
	{
		for (int i = 0; i < serverList.size(); i++)
		{
			if (serverList.get(i).getServerRoleName().equals(sr.getServerRoleName()) && repeateAllowedAtIndex != i)
			{
				throw new ValidationException("Role with that name already exists");
			}
		}
	}

	private void persistServerRolesLocally(HttpSession session, List<ServerRole> serverRoles)
	{
		ServerRole[] roles = serverRoles.toArray(new ServerRole[serverRoles.size()]);
		sortRoles(roles);
		session.setAttribute(SERVER_ROLES_VARIABLE, roles);
	}

	private String retrieveServerRolesData(HttpSession session, int index)
	{
		ServerRole[] roles = (ServerRole[]) session.getAttribute(SERVER_ROLES_VARIABLE);
		String result = null;
		if (roles != null && index < roles.length)
		{
			Gson gson = new Gson();
			result = gson.toJson(roles[index]);
		}
		else
		{
			result = "{}";
		}
		return result;
	}

	private void sortRoles(ServerRole[] roles)
	{
		if (roles != null && roles.length > 0)
		{
			Arrays.sort(roles, new Comparator<ServerRole>()
			{

				@Override
				public int compare(ServerRole s1, ServerRole s2)
				{
					return s1.getServerRoleName().compareTo(s2.getServerRoleName());
				}
			});
		}
	}
}
