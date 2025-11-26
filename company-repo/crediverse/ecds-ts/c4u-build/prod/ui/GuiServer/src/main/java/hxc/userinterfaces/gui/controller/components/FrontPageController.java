package hxc.userinterfaces.gui.controller.components;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import hxc.userinterfaces.gui.controller.monitor.AlarmViewer;
import hxc.userinterfaces.gui.controller.monitor.LogFileViewer;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.utils.registration.IFacilityIDs;

public class FrontPageController implements IThymeleafController
{

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{
		WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		HttpSession session = request.getSession(true);
		User user = (User) session.getAttribute("user");

		// ctx.setVariable("isbam", BusinessActivityMonitoring.BAM_AVAILABLE);
		ctx.setVariable("islog", LogFileViewer.LOG_AVAILABLE);
		ctx.setVariable("isalarm", AlarmViewer.ALARMVIEWER_AVAILABLE);
		ctx.setVariable("isbam", UiConnectionClient.getInstance().hasFacility(user, IFacilityIDs.BAM_FACILITY_ID));
		ctx.setVariable("isreports", user.hasPermission("ViewReportingService"));;
		
		ctx.setVariable("user", user);
		templateEngine.process("firstPage", ctx, response.getWriter());
	}

}
