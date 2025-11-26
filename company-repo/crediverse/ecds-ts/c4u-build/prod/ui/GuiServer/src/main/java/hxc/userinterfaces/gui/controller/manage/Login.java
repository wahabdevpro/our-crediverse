package hxc.userinterfaces.gui.controller.manage;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.jetty.JettyMain;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;

public class Login implements IThymeleafController
{

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{

		// validate login?
		WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("loginfailed", false);
		ctx.setVariable("password", "");
		ctx.setVariable("VERSION", JettyMain.getVersion());
		ctx.setVariable("isdebug", JettyMain.isRunningInDebugMode());

		String userName = request.getParameter("user");
		String pass = request.getParameter("pass");
		HttpSession session = request.getSession(false);
		if (session == null)
		{
			session = request.getSession(true);
		}

		User user = (User) session.getAttribute("user");

		if (userName != null && pass != null)
		{
			// Try and login in
			user = UiConnectionClient.getInstance().login(userName, pass);

			if (user == null || user.getLastLoginError() != null)
			{
				// Login failed
				ctx.setVariable("loginfailed", true);
			}
			else
			{
				// First save user session details
				session.setAttribute("user", user);
				user = (User) session.getAttribute("user");

				// UiConnectionClient.getInstance().loadUserBamMetrics(user, session);

				// Login passed time to move on (to home?)
				response.sendRedirect("/");
				return;
			}
		}

		try
		{
			templateEngine.process("login", ctx, response.getWriter());
		}
		catch (Exception e)
		{
			throw e;
		}
	}

}
