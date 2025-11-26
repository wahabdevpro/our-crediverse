package hxc.userinterfaces.gui.thymeleaf;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import hxc.userinterfaces.gui.controller.TestPage;
import hxc.userinterfaces.gui.controller.components.FrontPageController;
import hxc.userinterfaces.gui.controller.components.ResultCodeController;
import hxc.userinterfaces.gui.controller.components.VasCommandController;
import hxc.userinterfaces.gui.controller.manage.Home;
import hxc.userinterfaces.gui.controller.manage.LicensingController;
import hxc.userinterfaces.gui.controller.manage.Login;
import hxc.userinterfaces.gui.controller.manage.NotificationConfiguration;
import hxc.userinterfaces.gui.controller.manage.ServiceConfigurationLoader;
import hxc.userinterfaces.gui.controller.manage.ServiceManager;
import hxc.userinterfaces.gui.controller.manage.SessionKeepAlive;
import hxc.userinterfaces.gui.controller.manage.SessionTimeoutInfo;
import hxc.userinterfaces.gui.controller.manage.TabNavigation;
import hxc.userinterfaces.gui.controller.monitor.AlarmViewer;
import hxc.userinterfaces.gui.controller.monitor.BamViewer;
import hxc.userinterfaces.gui.controller.monitor.FitnessCheck;
import hxc.userinterfaces.gui.controller.monitor.LogFileViewer;
import hxc.userinterfaces.gui.controller.reports.ReportingController;
import hxc.userinterfaces.gui.controller.service.AirSimController;
import hxc.userinterfaces.gui.controller.service.CallMeBackService;
import hxc.userinterfaces.gui.controller.service.ControlService;
import hxc.userinterfaces.gui.controller.service.CreditSharingService;
import hxc.userinterfaces.gui.controller.service.CreditTransferService;
import hxc.userinterfaces.gui.controller.service.EnhancedCreditTransferService;
import hxc.userinterfaces.gui.controller.service.FileConnectorConfig;
import hxc.userinterfaces.gui.controller.service.FriendsAndFamilyService;
import hxc.userinterfaces.gui.controller.service.GenericServiceConfig;
import hxc.userinterfaces.gui.controller.service.GroupSharedAccounts;
import hxc.userinterfaces.gui.controller.service.PinService;
import hxc.userinterfaces.gui.controller.service.TamperCheckController;

public class ThymeleafEngine
{
	private static Map<String, IThymeleafController> controllersByURL;
	private static TemplateEngine templateEngine;

	private static IThymeleafController defaultController = null;

	private ThymeleafEngine()
	{
		super();
	}

	public static void initialise()
	{
		initializeControllersByURL();
		initializeTemplateEngine();
	}

	private static void initializeTemplateEngine()
	{
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver();

		// XHTML is the default mode, but we will set it anyway for better understanding of code
		templateResolver.setTemplateMode("XHTML");
		templateResolver.setCharacterEncoding("UTF-8");
		// This will convert "home" to "/WEB-INF/templates/home.html"
		templateResolver.setPrefix("/templates/");
		templateResolver.setSuffix(".html");
		// Set template cache TTL to 1 hour. If not set, entries would live in cache until expelled by LRU
		templateResolver.setCacheTTLMs(Long.valueOf(3600000L));

		// Cache is set to true by default. Set to false if you want templates to
		// be automatically updated when modified.
		templateResolver.setCacheable(false);

		templateEngine = new TemplateEngine();
		templateEngine.setTemplateResolver(templateResolver);
	}

	private static Map<String, IThymeleafController> initializeControllersByURL()
	{

		controllersByURL = new HashMap<String, IThymeleafController>();

		// Add controllers here
		controllersByURL.put("/", new Home());
		controllersByURL.put("/login", new Login());
		controllersByURL.put("/service", new ServiceManager());
		controllersByURL.put("/sysconfigloader", new ServiceConfigurationLoader());
		controllersByURL.put("/msgpersist", new NotificationConfiguration());
		controllersByURL.put("/session", new SessionKeepAlive());

		controllersByURL.put("/ctrlservice", new ControlService());
		controllersByURL.put("/creditservice", new CreditSharingService());
		controllersByURL.put("/genshareacc", new GroupSharedAccounts());
		controllersByURL.put("/fitnesscheck", new FitnessCheck());
		controllersByURL.put("/fileconfig", new FileConnectorConfig());
		controllersByURL.put("/sessioninfo", new SessionTimeoutInfo());
		controllersByURL.put("/encreditservice", new EnhancedCreditTransferService());

		controllersByURL.put("/credittransfer", new CreditTransferService());

		controllersByURL.put("/pinservice", new PinService());
		controllersByURL.put("/callmebackservice", new CallMeBackService());
		controllersByURL.put("/friendsandfamilyservice", new FriendsAndFamilyService());
		controllersByURL.put("/fpc", new FrontPageController());
		controllersByURL.put("/logview", new LogFileViewer());
		controllersByURL.put("/alarmview", new AlarmViewer());
		controllersByURL.put("/bamview", new BamViewer());
		controllersByURL.put("/licensing", new LicensingController());

		controllersByURL.put("/genericconfig", new GenericServiceConfig());

		controllersByURL.put("/nav", new TabNavigation()); // For Tab navigation (stores last tab clicked)
		controllersByURL.put("/rct", new ResultCodeController()); // For Tab navigation (stores last tab clicked)
		controllersByURL.put("/testpage", new TestPage()); // For testing

		controllersByURL.put("/vct", new VasCommandController()); // For list of available Vascommand Text for process
		controllersByURL.put("/reports", new ReportingController()); // For testing
		
		controllersByURL.put("/airsimconfig", new AirSimController()); // For Air Sim (This needs to move into its own editor!)
		controllersByURL.put("/tampercheck", new TamperCheckController()); // For Tamper Check

		// Default Controller
		defaultController = new Home();
		return controllersByURL;
	}

	public static IThymeleafController resolveController(String requestPath)
	{
		if (controllersByURL.containsKey(requestPath))
		{
			return controllersByURL.get(requestPath);
		}
		else
		{
			return defaultController;
		}
	}

	public static IThymeleafController resolveControllerForRequest(final HttpServletRequest request)
	{
		final String path = getRequestPath(request);
		if (controllersByURL.containsKey(path))
		{
			return controllersByURL.get(path);
		}
		else
		{
			return defaultController;
		}
	}

	public static TemplateEngine getTemplateEngine()
	{
		return templateEngine;
	}

	private static String getRequestPath(final HttpServletRequest request)
	{

		String requestURI = request.getRequestURI();
		final String contextPath = request.getContextPath();

		final int fragmentIndex = requestURI.indexOf(';');
		if (fragmentIndex != -1)
		{
			requestURI = requestURI.substring(0, fragmentIndex);
		}

		if (requestURI.startsWith(contextPath))
		{
			return requestURI.substring(contextPath.length());
		}
		return requestURI;
	}

}
