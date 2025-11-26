package hxc.userinterfaces.gui.controller.manage;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;

import hxc.userinterfaces.gui.data.GuiUpdateResponse;
import hxc.userinterfaces.gui.data.GuiUpdateResponse.OperationStatus;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;

public class SessionTimeoutInfo implements IThymeleafController
{
	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{
		GuiUpdateResponse guiResponse = null;
		HttpSession session = request.getSession(true);
		User user = (User) session.getAttribute("user");
		try
		{
			int timeout = UiConnectionClient.getInstance().getSessionTimeoutMinutes(user);
			guiResponse = new GuiUpdateResponse(OperationStatus.pass, String.valueOf(timeout));
		}
		catch (Exception e)
		{
			guiResponse = new GuiUpdateResponse(OperationStatus.fail, "Could not get session settings");
		}
		sendResponse(response, guiResponse.toString());
	}

	public void sendResponse(HttpServletResponse response, String htmlString) throws IOException
	{
		PrintWriter out = response.getWriter();
		out.print(htmlString);
		out.flush();
	}
}