package hxc.userinterfaces.gui.controller;

import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.jetty.CmdArgs;
import hxc.userinterfaces.gui.thymeleaf.ThymeleafEngine;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.annotation.WebInitParam;

import org.thymeleaf.context.WebContext;

@WebServlet(urlPatterns = { "/*" }, name = "AnnotationTest", asyncSupported = true, initParams = { @WebInitParam(name = "fromAnnotation", value = "xyz") })
public class MainServlet extends HttpServlet
{

	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/html;charset=UTF-8");
		// response.setHeader("Pragma", "no-cache");
		// response.setHeader("Cache-Control", "no-cache");
		// response.setDateHeader("Expires", 0);
		response.setStatus(HttpServletResponse.SC_OK);

		HttpSession session = request.getSession(false);

		User user = null;
		if (session != null)
		{
			int value = session.getMaxInactiveInterval();
			user = (User) session.getAttribute("ccuser");
		}
		else
		{
			session = request.getSession(true);
			session.setAttribute("org.mortbay.jetty.servlet.SessionCookie", "CCJSESSIONID");
			session.setMaxInactiveInterval(CmdArgs.sessionTimeout);
		}
		String uri = request.getRequestURI();

		if ((session == null) || session.isNew() || user == null || user.getSessionId() == null)
		{
			// User not logged in all requests go through login controller
			request.getRequestDispatcher("/login").forward(request, response);
		}
		else if (session != null && request.getRequestURI().endsWith("/logout"))
		{
			session.setAttribute("ccuser", null);
			// session.invalidate();
			// request.getRequestDispatcher("/login").forward(request,response);
			response.sendRedirect("/");
		}
		else
		{
			WebContext ctx = new WebContext(request, response, request.getServletContext(), request.getLocale());
			ctx.setVariable("ccuser", user);
			ctx.setLocale(user.getLanguage());

			// Page content?
			String content = request.getParameter("content");
			ctx.setVariable("main_content", (content != null) ? content : "");

			ThymeleafEngine.getTemplateEngine().process("home", ctx, response.getWriter());
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		processRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		processRequest(request, response);
	}
}