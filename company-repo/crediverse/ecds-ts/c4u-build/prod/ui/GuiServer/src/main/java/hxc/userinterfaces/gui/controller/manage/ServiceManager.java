package hxc.userinterfaces.gui.controller.manage;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import com.google.gson.Gson;

import hxc.configuration.ValidationException;
import hxc.userinterfaces.gui.controller.service.ControlService;
import hxc.userinterfaces.gui.data.GuiUpdateResponse;
import hxc.userinterfaces.gui.data.GuiUpdateResponse.OperationStatus;
import hxc.userinterfaces.gui.data.PermissionCategory;
import hxc.userinterfaces.gui.data.PermissionView;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.jetty.JettyMain;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.userinterfaces.gui.utils.GuiUtils;
import hxc.utils.protocol.uiconnector.ctrl.response.CtrlConfigServerRoleResponse;
import hxc.utils.protocol.uiconnector.ctrl.response.CtrlConfigurationResponse;
import hxc.utils.protocol.uiconnector.ctrl.response.FitnessResponse;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerInfo;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerRole;
import hxc.utils.protocol.uiconnector.userman.common.SecurityRole;
import hxc.utils.protocol.uiconnector.userman.common.UserDetails;

public class ServiceManager implements IThymeleafController
{

	@Override
	/**
	 * 2 Types of requests processed: Page Requests (referrer parameter) / Ajax requests (referrer parameter NULL)
	 */
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{
		// Get Session and context details
		WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		HttpSession session = request.getSession(true);

		// Validate Session
		User user = (User) session.getAttribute("user");
		boolean inValidateSession = (user == null);

		if (inValidateSession)
		{
			session.setAttribute("user", null);
			response.sendRedirect("/");
			return;
		}
		
		String callReference = (String) request.getParameter("referrer");
		if (callReference != null)
		{
			processPageRequest(ctx, response, templateEngine, callReference, user);
		}
		else
		{
			processAjaxRequest(session, request, response, user);
		}
	}
	
	/**
	 *	Process "referrer" requests. These are requests to specific pages from Page Navigation perspective 
	 */
	protected void processPageRequest(WebContext ctx, HttpServletResponse response, TemplateEngine templateEngine, String callReference, User user) throws ServletException, IOException
	{
		// Page Template to invoke
		String pageTemplate = callReference;
		
		if (callReference.equalsIgnoreCase("manusers"))
		{
			// User Management (will need list of users and
			UserDetails[] details = UiConnectionClient.getInstance().getUserList(user);
			List<UserDetails> userDetails = Arrays.asList(details);
			ctx.setVariable("userDetails", userDetails);

			// All roles required for roles list
			SecurityRole[] roles = UiConnectionClient.getInstance().getSecurityList(user);
			List<SecurityRole> roleDetails = null;
			if (roles != null)
			{
				roleDetails = Arrays.asList(roles);
			}
			else
			{
				roleDetails = new ArrayList<>();
			}
			ctx.setVariable("roleDetails", roleDetails);
			ctx.setVariable("can_update", user.isCanChangeUsers());
			String permLevel = user.isCanViewRoles() ? (user.isCanChangePermissions() ? "all" : "view") : "noview";
			ctx.setVariable("roleLevel", permLevel);
		}
		else if (callReference.equalsIgnoreCase("manroles"))
		{
			// Role Management
			SecurityRole[] details = UiConnectionClient.getInstance().getSecurityList(user);
			List<SecurityRole> roleDetails = Arrays.asList(details);
			// sorting roles by id
			Collections.sort(roleDetails, new Comparator<SecurityRole>()
			{
				@Override
				public int compare(SecurityRole o1, SecurityRole o2)
				{
					if (o1.getRoleId() == o2.getRoleId())
						return 0;
					return o1.getRoleId() < o2.getRoleId() ? -1 : 1;
				}
			});
			ctx.setVariable("roleDetails", roleDetails);

			// Get list of security permissions
			// SecurityPermissionInfo[] perms = UiConnectionClient.getInstance().getSecurityPermissionList(user);
			List<PermissionCategory> permCats = UiConnectionClient.getInstance().getSecurityPermissions(user);
			permCats = filterRolePermissions(permCats);
			ctx.setVariable("permCats", permCats);

			boolean canUpdate = user.isCanChangeRoles();

			ctx.setVariable("can_update", canUpdate);
			ctx.setVariable("user", user);

			if (!user.isCanViewPermissions())
			{
				String permLevel = user.isCanViewPermissions() ? (user.isCanChangePermissions() ? "all" : "view") : "noview";
				ctx.setVariable("permLevel", permLevel);
			}
		}
		else if (callReference.equalsIgnoreCase("mydetails"))
		{
			// Load Current logged on user details
			UserDetails myDetails = UiConnectionClient.getInstance().getUser(user, user.getUserId());
			ctx.setVariable("VERSION", JettyMain.getVersion());
			ctx.setVariable("mydetails", myDetails);
		}
		else if (callReference.equalsIgnoreCase("licensing"))
		{
			ctx.setVariable("license", UiConnectionClient.getInstance().getLicenseDetails(user));
			pageTemplate = "licensing";
		}
		else if (callReference.equalsIgnoreCase("fitness"))
		{
			loadFitness(ctx, user);
			pageTemplate = "fitness/fitness";
		}
		else if (callReference.equalsIgnoreCase("logview"))
		{
			List<String> hosts = UiConnectionClient.getInstance().extractServerList(user);

			ctx.setVariable("hosts", hosts);
			pageTemplate = "logviewer/logviewer";
		}
		else if (callReference.equalsIgnoreCase("alarmview"))
		{
			pageTemplate = "alarmviewer/alarmviewer";
		}
		else if (callReference.equalsIgnoreCase("reportview"))
		{
			@SuppressWarnings("unused")
			List<String> hosts = UiConnectionClient.getInstance().extractServerList(user);

			// ctx.setVariable("hosts", hosts);
			pageTemplate = "reportviewer";
		}
		else if (callReference.equalsIgnoreCase("bamview"))
		{
			pageTemplate = "bamviewer/bamviewer";
		}
		else if (callReference.equalsIgnoreCase("bammon"))
		{
			pageTemplate = "bam";
		}
		else
		{
			// serviceConfigHeading
			String heading = callReference.replaceAll("_", " ");
			ctx.setVariable("serviceConfigHeading", heading);
			ctx.setVariable("serviceConfigMenu", callReference);

			// System Configuration
			pageTemplate = "serviceconfig";
		}
		
		// Process template
		try
		{
			templateEngine.process(pageTemplate, ctx, response.getWriter());
		}
		catch (Exception e)
		{
			PrintWriter out = response.getWriter();
			out.println("Failure to communicate! Error: " + e.getLocalizedMessage());
			throw e;
		}
	}

	/**
	 * Process AJAX data requests, this includes User Management, Role Management, Service Configuration requests 
	 * @throws ClassNotFoundException 
	 * @throws NoSuchAlgorithmException 
	 */
	protected void processAjaxRequest(HttpSession session, HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException, ClassNotFoundException, NoSuchAlgorithmException
	{
		// Ajax post
		String ajaxPage = request.getParameter("page");
		String ajaxAction = request.getParameter("act");

		if (ajaxPage.equals("manusers"))
		{
			manageUsersAjaxHandler(request, response, user, ajaxAction);
		}
		else if (ajaxPage.equals("manroles"))
		{
			manageRolesAjaxHandler(request, response, user, ajaxAction);
		}
		else if (ajaxPage.equals("mydetails"))
		{
			manageMyDetailsAjaxHandler(session, request, response, user, ajaxAction);
		}
	}

	private void manageMyDetailsAjaxHandler(HttpSession session, HttpServletRequest request, 
			HttpServletResponse response, User user, String ajaxAction) throws NoSuchAlgorithmException, IOException
	{
		if (ajaxAction.equals("pass"))
		{
			String oldPassword = request.getParameter("oldpassword");
			String newPassword = request.getParameter("password1");

			// Verify old password correct
			User myUser = UiConnectionClient.getInstance().login(user.getUserId(), oldPassword);
			if (myUser != null && (myUser.getLastLoginError() == null || myUser.getLastLoginError().length() == 0))
			{
				// Update User password
				boolean updated = UiConnectionClient.getInstance().updatePassword(user, user.getUserId(), newPassword);
				sendResponse(response, updated ? "success" : "fail");
			}
			else
			{
				sendResponse(response, "oldpass");
			}
			return;
		}
		// fullname
		if (ajaxAction.equals("upd"))
		{
			String fullname = request.getParameter("fullname");
			String mobile = request.getParameter("mobile");

			boolean result = UiConnectionClient.getInstance().updateMyDetails(user, fullname, mobile);
			if (result)
			{
				user.setName(fullname);
				session.setAttribute("user", user);
			}

			Map<String, String> respParms = new HashMap<>();
			respParms.put("result", result ? "success" : "fail");
			respParms.put("name", fullname);
			sendResponse(response, GuiUtils.buildJSON(respParms));
			return;
		}
	}

	private void manageRolesAjaxHandler(HttpServletRequest request,
			HttpServletResponse response, User user, String ajaxAction)
			throws ClassNotFoundException, IOException
	{
		String roleid = request.getParameter("roleid");
		String name = request.getParameter("name");
		String description = request.getParameter("description");

		// Permissions
		List<String> permIdList = new ArrayList<>();
		for (String param : request.getParameterMap().keySet())
		{
			if (param.startsWith("perm_"))
			{
				// String [] values = request.getParameterMap().get(param);
				String permId = param.split("_")[1];
				permIdList.add(permId);
			}
		}

		if (ajaxAction.equals("add") || ajaxAction.equals("upd"))
		{
			// Add or update
			SecurityRole role = new SecurityRole(name, description);

			role.setPermissions(permIdList);
			boolean result = false;
			if (roleid != null)
			{
				// Update
				int id = Integer.parseInt(roleid);
				role.setRoleId(id);
			}
			GuiUpdateResponse resp = null;
			try
			{
				result = UiConnectionClient.getInstance().addUpdateRole(user, role);
				if (result)
					resp = new GuiUpdateResponse(OperationStatus.pass);
				else
					resp = new GuiUpdateResponse(OperationStatus.fail, "Could not safe");
			}
			catch (ValidationException e)
			{
				resp = new GuiUpdateResponse(OperationStatus.fail, e.getMessage());
			}
			sendResponse(response, resp.toString());
		}
		else if (ajaxAction.equals("del"))
		{
			try
			{
				int id = Integer.parseInt(roleid);
				boolean result = UiConnectionClient.getInstance().deleteRole(user, id);
				sendResponse(response, (result ? "success" : "fail"));
			}
			catch (Exception e)
			{
				sendResponse(response, "fail");
			}
		}
		else if (ajaxAction.equals("ret"))
		{
			// Read lasest data for a service

			try
			{
				int id = Integer.parseInt(roleid);
				SecurityRole role = UiConnectionClient.getInstance().getRole(user, id);
				if (role == null)
				{
					sendResponse(response, "fail");
				}
				else
				{
					Gson gson = new Gson();
					String json = gson.toJson(role);
					sendResponse(response, json);
				}
			}
			catch (Exception e)
			{
				sendResponse(response, "fail");
			}

		}
	}

	private void manageUsersAjaxHandler(HttpServletRequest request,
			HttpServletResponse response, User user, String ajaxAction)
			throws IOException
	{
		// Basic details
		String userid = request.getParameter("userid");
		String fullname = request.getParameter("fullname");
		String mobile = request.getParameter("mobile");
		String password1 = request.getParameter("password1");

		// Roles
		List<Integer> roleIdList = new ArrayList<>();
		for (String param : request.getParameterMap().keySet())
		{
			if (param.startsWith("role_"))
			{
				// String [] values = request.getParameterMap().get(param);
				int roleId = Integer.parseInt(param.split("_")[1]);
				roleIdList.add(roleId);
			}
		}

		if (ajaxAction.equals("add") || ajaxAction.equals("upd"))
		{
			// Roles?
			UserDetails userDetails = new UserDetails();
			userDetails.setUserId(userid);
			userDetails.setName(fullname);
			userDetails.setMobileNumber(mobile);
			userDetails.setRoleIds(roleIdList);
			userDetails.setEnabled(true);
			userDetails.setNewUser(ajaxAction.equals("add"));

			try
			{
				boolean savedUserDSetails = UiConnectionClient.getInstance().updateUser(user, userDetails);
				if (savedUserDSetails)
				{
					// Check for password
					if (password1 != null && password1.length() > 0)
					{
						UiConnectionClient.getInstance().updatePassword(user, userid, password1);
					}
				}
				sendResponse(response, (savedUserDSetails ? "success" : "fail"));
			}
			catch (ValidationException ex)
			{
				sendResponse(response, "name_fail");
			}
		}
		else if (ajaxAction.equals("del"))
		{
			if (userid != null)
			{
				boolean result = UiConnectionClient.getInstance().deleteUser(user, userid);
				sendResponse(response, (user.getUserId().equals(userid)) ? "self" : (result ? "success" : "fail"));
			}
			else
			{
				sendResponse(response, (user.getUserId().equals(userid)) ? "self" : "fail");
			}
		}
		else if (ajaxAction.equals("ret"))
		{
			// Retrieve user Details
			try
			{
				UserDetails userDetails = UiConnectionClient.getInstance().getUser(user, userid);
				if (userDetails == null)
				{
					sendResponse(response, "fail");
				}
				else
				{
					Gson gson = new Gson();
					String json = gson.toJson(userDetails);
					sendResponse(response, json);
				}
			}
			catch (Exception e)
			{
				sendResponse(response, "fail");
			}
		}
	}
	
	public void sendResponse(HttpServletResponse response, String message) throws IOException
	{
		PrintWriter out = response.getWriter();
		out.print(message);
		out.flush();
	}

	/**
	 *	Load fitness data 
	 */
	private void loadFitness(WebContext ctx, User user)
	{
		String selectedServer = null;
		String localhost = GuiUtils.getLocalhost();
		if (localhost.indexOf('.') > 0)
		{
			localhost = localhost.substring(0, localhost.indexOf('.'));
		}

		CtrlConfigurationResponse ctrlConfig = UiConnectionClient.getInstance().getControlServiceConfiguration(user);
		List<String> servers = new ArrayList<>();
		if (ctrlConfig.getServerList() != null)
		{

			ServerInfo[] sinfos = ctrlConfig.getServerList();
			GuiUtils.sortServerHosts(sinfos);
			ctx.setVariable(ControlService.SERVER_INFO_VARIABLE, sinfos);

			for (ServerInfo server : sinfos)
			{
				servers.add(server.getServerHost());
				if (server.getServerHost().equalsIgnoreCase(localhost))
					selectedServer = server.getServerHost();
			}
		}

		if (selectedServer == null && servers.size() > 0)
			selectedServer = servers.get(0);

		ctx.setVariable("selectedServer", selectedServer);
		ctx.setVariable("servers", servers);
		ctx.setVariable("connection", (servers.size() == 0) ? "ignore" : "pass");

		try
		{
			if (selectedServer != null)
			{
				ctx.setVariable("serverconnection", selectedServer);
				FitnessResponse fitness = UiConnectionClient.getInstance().getServerFitnessLevels(selectedServer, user); // TODO: Possible hanging
				ctx.setVariable("fitness", fitness);
				ctx.setVariable("connection", "pass");
			}
		}
		catch (Exception e)
		{
			ctx.setVariable("fitness", null);
			ctx.setVariable("connection", "fail");
		}

		// Load role information
		CtrlConfigServerRoleResponse roleConfig = UiConnectionClient.getInstance().getControlServiceRoleConfiguration(user);
		if (roleConfig != null)
		{
			ServerRole[] sroles = roleConfig.getServerRoleList();
			GuiUtils.sortServerRoles(sroles);
			ctx.setVariable("serverroles", sroles);
		}
		else
		{
			ctx.setVariable("serverroles", null);
		}

	}

	private List<PermissionCategory> filterRolePermissions(List<PermissionCategory> permCats)
	{
		List<PermissionCategory> result = new ArrayList<>();
		for (PermissionCategory pc : permCats)
		{
			@SuppressWarnings("unused")
			boolean add = false;
			for (PermissionView pv : pc.getPermissions())
			{
				if (pv.isAssignable())
				{
					result.add(pc);
					break;
				}
			}
		}
		return result;
	}

}
