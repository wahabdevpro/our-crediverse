package hxc.userinterfaces.gui.controller;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;

public class TestPage implements IThymeleafController
{
	//TODO: Remove!
	public static final boolean LOG_AVAILABLE = true;

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{
		WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		templateEngine.process("testpage", ctx, response.getWriter());
	}

}
