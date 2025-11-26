package hxc.userinterfaces.gui.controller;

import java.io.IOException;

import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.structs.BaseServlet;
import hxc.userinterfaces.gui.thymeleaf.ThymeleafEngine;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.context.WebContext;

@WebServlet(urlPatterns = { "/test" }, name = "TestPage", asyncSupported = true)
@SuppressWarnings("serial")
public class Test extends BaseServlet
{
	@Override
	public void defaultHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException
	{
		ThymeleafEngine.getTemplateEngine().process("test", ctx, response.getWriter());
	}
}
