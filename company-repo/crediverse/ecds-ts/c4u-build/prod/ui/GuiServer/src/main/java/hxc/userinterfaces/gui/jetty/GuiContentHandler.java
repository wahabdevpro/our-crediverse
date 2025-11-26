package hxc.userinterfaces.gui.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.thymeleaf.TemplateEngine;

import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.thymeleaf.ThymeleafEngine;

public class GuiContentHandler extends AbstractHandler
{

	private void validateLogin()
	{

	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		try
		{
			HttpServletRequest req = (HttpServletRequest) request;
			HttpServletResponse resp = (HttpServletResponse) response;

			String path = req.getServletContext().getRealPath("/");
			String contextPath = baseRequest.getContextPath();

			/*
			 * Write the response headers
			 */
			response.setContentType("text/html;charset=UTF-8");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);

			IThymeleafController controller = ThymeleafEngine.resolveControllerForRequest(request);
			if (controller == null)
			{
				response.getWriter().println("<h1>Things did not go right - Controller NULL</h1>");
			}
			else
			{
				TemplateEngine templateEngine = ThymeleafEngine.getTemplateEngine();
				controller.process(request, response, req.getServletContext(), templateEngine);
			}
		}
		catch (Exception e)
		{
		}

	}

}
