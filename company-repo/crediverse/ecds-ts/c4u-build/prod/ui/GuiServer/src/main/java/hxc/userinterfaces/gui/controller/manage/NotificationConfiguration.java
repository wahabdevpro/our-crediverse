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
import hxc.utils.protocol.uiconnector.common.Configurable;

public class NotificationConfiguration implements IThymeleafController
{
	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{
		// request.setCharacterEncoding("ISO-8859-1");

        // not sure if this has side effects ... leaving the instantiation
		new WebContext(request, response, servletContext, request.getLocale());
		HttpSession session = request.getSession(false);
		if (session == null)
		{
			session = request.getSession(true);
		}

		User user = (User) session.getAttribute("user");

		boolean inValidateSession = (user == null);

		if (inValidateSession)
		{
			session.setAttribute("user", null);
			response.sendRedirect("/");
			return;
		}

		// Check that the notificaitons are being updated
		String component = request.getParameter("config_comp");
		String act = request.getParameter("config_act");
		String uid = request.getParameter("config_uid");
		String version = request.getParameter("config_ver");

		Configurable updated = null;

		if (((component != null && act != null) && (component.equalsIgnoreCase("msg") && act.equals("upd"))))
		{
			long luid = Long.parseLong(uid);
			int iversion = Integer.parseInt(version);
			// Update notifications
			try
			{
				updated = UiConnectionClient.getInstance().saveNotifications(user, luid, iversion, request.getParameterMap());
				sendResponse(response, createSuccessfuleUpdateJSON(luid, updated.getVersion()));
			}
			catch (Exception e)
			{
				// Error caused notifications not to be saved
				sendResponse(response, createFailedUpdateJSON(luid, e.getMessage()));
				// sendResponse(response, e.getMessage());
			}
		}

	}

	private String createSuccessfuleUpdateJSON(long uid, long version)
	{
		JsonObject job = new JsonObject();
		job.add("update", new JsonPrimitive("success"));
		job.add("uid", new JsonPrimitive(uid));
		job.add("version", new JsonPrimitive(version));
		return job.toString();
	}

	private String createFailedUpdateJSON(long uid, String message)
	{
		JsonObject job = new JsonObject();
		job.add("update", new JsonPrimitive("fail"));
		job.add("uid", new JsonPrimitive(uid));
		job.add("error", new JsonPrimitive(message));
		return job.toString();
	}

	public void sendResponse(HttpServletResponse response, String htmlString) throws IOException
	{
		PrintWriter out = response.getWriter();
		out.print(htmlString);
		out.flush();
	}
}
