package hxc.userinterfaces.gui.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.thymeleaf.TemplateEngine;

import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.thymeleaf.ThymeleafEngine;

public class MainServlet extends DefaultServlet
{
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		// String path = request.getServletContext().getRealPath("/");

		response.setContentType("text/html;charset=UTF-8");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setStatus(HttpServletResponse.SC_OK);

		// If this is a new session then go to login

		// Check if the user is logged in?
		HttpSession session = request.getSession(true);
		User user = (User) session.getAttribute("user");
		IThymeleafController controller = null;

		if (session.isNew() || user == null || user.getSessionId() == null)
		{
			// User not logged in all requests go through login controller
			controller = ThymeleafEngine.resolveController("/login");
		}
		else
		{
			// All other requests go through there
			controller = ThymeleafEngine.resolveControllerForRequest(request);
		}

		// Controller will handle page navigation
		if (controller == null)
		{
			response.getWriter().println("<h1>Things did not go right - Controller NULL</h1>");
		}
		else
		{
			TemplateEngine templateEngine = ThymeleafEngine.getTemplateEngine();
			try
			{
				controller.process(request, response, request.getServletContext(), templateEngine);
			}
			catch (Exception e)
			{
			}
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
