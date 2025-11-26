package hxc.userinterfaces.gui.controller;

import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.jetty.CmdArgs;
import hxc.userinterfaces.gui.json.GuiUpdateResponse;
import hxc.userinterfaces.gui.json.GuiUpdateResponse.OperationStatus;
import hxc.userinterfaces.gui.services.ILoginService;
import hxc.userinterfaces.gui.structs.BaseServlet;
import hxc.userinterfaces.gui.thymeleaf.ThymeleafEngine;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.context.WebContext;

@WebServlet(urlPatterns = { "/login" }, name = "Login", asyncSupported = true)
@SuppressWarnings("serial")
public class Login extends BaseServlet
{

	// @PostConstruct @PreDestroy (If needed)

	@Resource(name = "loginService")
	private ILoginService loginService;

	@Override
	public void defaultHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException
	{
		// This will be called if there is a clean call to this url (i.e. no details supplied or submitted)
		// WebContext ctx = new WebContext(request, response, request.getServletContext(), request.getLocale());
		// ctx.setVariable("VERSION", JettyMain.getVersion());
		ctx.setVariable("VERSION", loginService.getVersion());
		// ctx.setLocale(Locale.FRANCE);

		// TODO: store last login language as cookie
		ThymeleafEngine.getTemplateEngine().process("login", ctx, response.getWriter());
	}

	// (act=login)
	public void loginHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException, NoSuchAlgorithmException
	{
		String userName = request.getParameter("user");
		String pass = request.getParameter("pass");
		GuiUpdateResponse jsonResponse = null;

		if (userName != null && pass != null)
		{
			if (loginService == null)
			{
				// System.err??.println("No login service Detected!");
			}
			user = loginService.login(userName, pass);
			session.setAttribute("ccuser", user);

			if (user != null && user.getLastLoginError() == null)
			{
				session.setMaxInactiveInterval(CmdArgs.sessionTimeout);
				jsonResponse = new GuiUpdateResponse(OperationStatus.pass, "");
			}
			else if (user == null)
			{
				user = new User();
				user.setLastLoginError("The user or password is incorrect!");
			}
		}

		if (jsonResponse == null)
			jsonResponse = new GuiUpdateResponse(OperationStatus.fail, user.getLastLoginError());

		sendResponse(response, jsonResponse.toString());
		return;
	}

	/**
	 * Extract Json of all property file messages
	 */
	public void localizeHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String lang = request.getParameter("lang");
		if (lang == null)
		{
			lang = "en";
		}
		Locale loc = Locale.forLanguageTag(lang);
		if (loc != null)
		{
			ctx.setVariable("jsonview", true);
			ctx.setLocale(loc);
			ThymeleafEngine.getTemplateEngine().process("login", ctx, response.getWriter());
		}
	}

}
