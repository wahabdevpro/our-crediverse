package hxc.userinterfaces.gui.controller.manage;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;

public class SessionKeepAlive implements IThymeleafController
{

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{
		// This is just to provide a keep alive signal
		WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

		// Check for authentication session
		HttpSession session = request.getSession(true);

		User user = (User) session.getAttribute("user");

		boolean valid = false;
		try
		{
			valid = UiConnectionClient.getInstance().validateSession(user);
		}
		catch (Exception e)
		{
		}

		JsonObject job = new JsonObject();
		job.add("valid", new JsonPrimitive(valid));
		sendResponse(response, job.toString());
	}

	public void sendResponse(HttpServletResponse response, String message) throws IOException
	{
		PrintWriter out = response.getWriter();
		out.print(message);
		out.flush();
	}
}
