package hxc.userinterfaces.gui.controller.manage;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import hxc.userinterfaces.gui.controller.monitor.AlarmViewer;
import hxc.userinterfaces.gui.controller.monitor.BamViewer;
import hxc.userinterfaces.gui.controller.monitor.LogFileViewer;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.jetty.JettyMain;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.utils.registration.IFacilityIDs;

public class Home implements IThymeleafController
{

	public Home()
	{
		super();
	}

	public User loginUserIn(String userName, String password)
	{

		return null;
	}

	@Override
	public void process(final HttpServletRequest request, final HttpServletResponse response, final ServletContext servletContext, final TemplateEngine templateEngine) throws Exception
	{

		String page = "";

		WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("loginfailed", false);
		ctx.setVariable("password", "");

		// Check for authentication session
		HttpSession session = request.getSession(true);

		User user = (User) session.getAttribute("user");
		boolean inValidateSession = false;

		if (request.getPathInfo().equals("/logout") || inValidateSession)
		{
			session.setAttribute("user", null);
			response.sendRedirect("/");
			return;
		}

		if (user == null)
		{

			page = "login";

			String userName = request.getParameter("user");
			String pass = request.getParameter("pass");

			if (userName != null && pass != null)
			{
				// Try and login in
				user = UiConnectionClient.getInstance().login(userName, pass);
				if (user != null)
				{
					session.setAttribute("user", user);
					user = (User) session.getAttribute("user");
				}
				else
				{
					ctx.setVariable("loginfailed", true);
				}
			}
		}
		else if (request.getPathInfo().equals("/system"))
		{
			String system = (String) request.getParameter("system");
			PrintWriter out = response.getWriter();
			out.println("<!DOCTYPE html>");
			String pageData = UiConnectionClient.getInstance().buildContent(system, user);
			out.println(pageData);
			return;
		}
		else if (request.getPathInfo().equals("/loadmenu"))
		{
			// Load menu items
			// PrintWriter out = response.getWriter();
			// String menu = UiConnectionClient.getInstance().buidMenu(user);
			String menu = UiConnectionClient.getInstance().getServiceMenuItems(user);
			// String test = "<li><a href=\"#\" onclick=\"loadSystem('test')\">hello there...</a></li>";
			sendResponse(response, menu);
			return;
		}
		else if (request.getPathInfo().equals("/login") && (user != null))
		{
			response.sendRedirect("/");
			return;
		}
		else if (user != null)
		{
			String pathinfo = request.getPathInfo().substring(1);
			String[] parts = pathinfo.split("/");

			// String menu = (parts.length>0)? parts[0] : "config";
			// String system = (parts.length>1)? parts[1] : "serviceconfigdefault";

			String menu = (parts.length > 0) ? parts[0] : "";
			String system = (parts.length > 1) ? parts[1] : "";

			ctx.setVariable("menu", menu);
			ctx.setVariable("system", system);
			ctx.setVariable("user", user);

			// Permissions?

		}

		if (user != null)
		{
			ctx.setVariable("islog", LogFileViewer.LOG_AVAILABLE);
			ctx.setVariable("isalarm", AlarmViewer.ALARMVIEWER_AVAILABLE);
			ctx.setVariable("isbam", (BamViewer.BAMVIEWER_AVAILABLE = UiConnectionClient.getInstance().hasFacility(user, IFacilityIDs.BAM_FACILITY_ID)));
			ctx.setVariable("isdebug", JettyMain.isRunningInDebugMode());
			ctx.setVariable("isreports", user.hasPermission("ViewReportingService"));;
			
			page = "home";
		}

		try
		{
			templateEngine.process(page, ctx, response.getWriter());
		}
		catch (Exception e)
		{
			throw e;
		}

	}

	private void sendResponse(HttpServletResponse response, String htmlString) throws IOException
	{
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.print(htmlString);
		out.flush();
	}

}