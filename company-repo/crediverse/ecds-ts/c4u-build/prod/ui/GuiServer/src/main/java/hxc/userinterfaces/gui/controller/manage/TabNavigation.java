package hxc.userinterfaces.gui.controller.manage;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;

public class TabNavigation implements IThymeleafController
{

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{
		HttpSession session = request.getSession(true);
		String navTab = request.getParameter("nav"); // Toptab
		String configTab = request.getParameter("con"); // Toptab
		String retieve = request.getParameter("ret");
		String clr = request.getParameter("clr");

		if (clr != null)
		{
			session.removeAttribute("navTab");
			session.removeAttribute("configTab");
		}
		else
		{
			if (navTab != null)
				session.setAttribute("navTab", navTab);
			if (configTab != null)
				session.setAttribute("configTab", configTab);

		}

		String json = "";
		if (retieve != null)
		{
			JsonObject job = new JsonObject();
			String tab = (String) session.getAttribute("navTab");
			if (tab != null)
				job.add("nav", new JsonPrimitive(tab));

			tab = (String) session.getAttribute("configTab");
			if (tab != null)
				job.add("con", new JsonPrimitive(tab));

			json = job.toString();
		}
		sendResponse(response, json);
	}

	public void sendResponse(HttpServletResponse response, String htmlString) throws IOException
	{
		PrintWriter out = response.getWriter();
		out.print(htmlString);
		out.flush();
	}
}
