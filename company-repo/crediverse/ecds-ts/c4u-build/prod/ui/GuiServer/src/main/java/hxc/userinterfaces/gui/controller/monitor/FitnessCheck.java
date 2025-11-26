package hxc.userinterfaces.gui.controller.monitor;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import hxc.userinterfaces.gui.data.GuiUpdateResponse;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.userinterfaces.gui.utils.GuiUtils;
import hxc.utils.protocol.uiconnector.ctrl.response.CtrlConfigServerRoleResponse;
import hxc.utils.protocol.uiconnector.ctrl.response.FitnessResponse;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerRole;

public class FitnessCheck implements IThymeleafController
{

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{
		HttpSession session = request.getSession(true);
		GuiUpdateResponse guiResponse = null;
		String page = null;
		WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

		String action = (String) request.getParameter("act");
		User user = (User) session.getAttribute("user");
		if (action != null)
		{
			switch (action)
			{
				case "fit":
					String server = (String) request.getParameter("server");
					loadFitness(ctx, server, user);
					loadServerRoles(ctx, user);
					page = "fitness/serverfitness";
					break;
				default:
					break;
			}
		}
		if (page != null)
			templateEngine.process(page, ctx, response.getWriter());
	}

	private void loadFitness(WebContext ctx, String server, User user) throws Exception
	{
		ctx.setVariable("serverconnection", server);
		try
		{
			FitnessResponse fitness = UiConnectionClient.getInstance().getServerFitnessLevels(server, user);
			ctx.setVariable("fitness", fitness);
			ctx.setVariable("connection", "pass");
		}
		catch (Exception e)
		{
			ctx.setVariable("fitness", null);
			ctx.setVariable("connection", "fail");

		}
	}

	private void loadServerRoles(WebContext ctx, User user)
	{
		// Load role information
		CtrlConfigServerRoleResponse roleConfig = UiConnectionClient.getInstance().getControlServiceRoleConfiguration(user);
		if (roleConfig != null)
		{
			ServerRole[] sroles = roleConfig.getServerRoleList();
			GuiUtils.sortServerRoles(sroles);
			ctx.setVariable("serverroles", sroles);
		}
		else
		{
			ctx.setVariable("serverroles", null);
		}
	}
}
