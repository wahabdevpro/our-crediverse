package hxc.userinterfaces.gui.structs;

import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.jetty.CmdArgs;
import hxc.userinterfaces.gui.thymeleaf.ThymeleafEngine;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.context.WebContext;

//@RunAs("special")
@SuppressWarnings("serial")
public abstract class BaseServlet extends HttpServlet
{

	public void sendResponse(HttpServletResponse response, String htmlString) throws IOException
	{
		response.setContentType("text/json;charset=UTF-8");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setStatus(HttpServletResponse.SC_OK);

		PrintWriter out = response.getWriter();
		out.print(htmlString);
		out.flush();
	}

	public void sendTemplateResponse(HttpServletResponse response, WebContext ctx, String page) throws IOException
	{
		response.setContentType("text/html;charset=UTF-8");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setStatus(HttpServletResponse.SC_OK);
		ThymeleafEngine.getTemplateEngine().process(page, ctx, response.getWriter());
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

	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String action = (String) request.getParameter("act");
		String methodToCall = (action == null) ? "defaultHandler" : action + "Handler";

		HttpSession session = request.getSession(false);
		User user = null;
		if (session != null)
		{
			user = (User) session.getAttribute("ccuser");
		}
		else
		{
			session = request.getSession(true);
			session.setMaxInactiveInterval(CmdArgs.sessionTimeout);
		}

		Method method;
		try
		{
			method = this.getClass().getMethod(methodToCall, HttpServletRequest.class, HttpServletResponse.class, HttpSession.class, User.class, WebContext.class);
			WebContext ctx = new WebContext(request, response, request.getServletContext(), request.getLocale());
			// if (user != null)
			// {
			// ctx.setLocale(user.getLanguage());
			// }
			method.invoke(this, request, response, session, user, ctx);
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
		}
	}

	public abstract void defaultHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException;
}
