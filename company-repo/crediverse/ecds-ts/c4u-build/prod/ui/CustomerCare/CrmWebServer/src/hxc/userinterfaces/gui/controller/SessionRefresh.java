package hxc.userinterfaces.gui.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.context.WebContext;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.jetty.CmdArgs;
import hxc.userinterfaces.gui.structs.BaseServlet;

@WebServlet(urlPatterns = { "/session" }, name = "SessionRefreshHandler", asyncSupported = true)
@SuppressWarnings("serial")
public class SessionRefresh extends BaseServlet
{

	@Override
	public void defaultHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException
	{
		JsonObject job = new JsonObject();
		boolean valid = ((user != null) && (user.getSessionId() != null));
		job.add("valid", new JsonPrimitive(valid));
		job.add("to", new JsonPrimitive(CmdArgs.sessionTimeout));
		sendResponse(response, job.toString());
	}

}
