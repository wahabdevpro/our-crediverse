package hxc.userinterfaces.gui.jetty;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.IncludableGzipFilter;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.services.logging.LoggingLevels;
import hxc.supervisor.SupervisorConstants;
import hxc.userinterfaces.gui.servlets.MainServlet;
import hxc.userinterfaces.gui.thymeleaf.ThymeleafEngine;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.utils.protocol.uiconnector.response.SystemInfoResponse;

public class JettyMain
{
	private enum OperatingMode {
		UNKNOWN, NORMAL, DEBUG
	};
	
	private static String VERSION = null;
	private static OperatingMode OPERATING_MODE = OperatingMode.UNKNOWN;
	private static Logger logger = LoggerFactory.getLogger(JettyMain.class);

	/**
	 * Application Starting Point
	 */
	public static void main(String[] args) throws Exception
	{
		try
		{
			// Parse incoming arguments (including PORT settings)
			Set<String> pathSections = new HashSet<String>();
			CmdArgs.parseArgs(args);

			// Get Jetty up and running
			Server server = null;
			if (checkPortAvailable(CmdArgs.port))
			{
				server = new Server(CmdArgs.port);
			}
			else
			{
				 // There can only one instance of the GUI. Exist if GUI port not free 
				String message = String.format("GUI cannot be started as port %d is already in use\n", CmdArgs.port);
				try
				{
					UiConnectionClient.getInstance().log(LoggingLevels.FATAL, message);
				}
				catch (Exception e)
				{
				}

				logger.error(message);
				System.exit(SupervisorConstants.CANT_STARTUP_EXIT_CODE);
			}
			
			// Discover share path (holds resources and template files)
			File sharePath = new File(CmdArgs.baseDir + File.separator + "share");
			boolean shareInImmidiatePath = false;
			if (sharePath.exists())
			{
				shareInImmidiatePath = true;
			}
			logger.info("args = {}, CmdArgs.baseDir = {}, sharePath = {}, shareInImmidiatePath = {}", args, CmdArgs.baseDir, sharePath, shareInImmidiatePath);
			String basePath = Utility.expandPath(CmdArgs.baseDir + (shareInImmidiatePath ? "/share/" : "../../share"));

			// Default servlet handler (which deals with sessions)
			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setContextPath("/");
			
			context.addServlet(new ServletHolder(new MainServlet()), "/*");
			SessionManager sm = new HashSessionManager();
			((HashSessionManager) sm).setSessionCookie("C4USESSIONID");
			sm.setMaxInactiveInterval(CmdArgs.sessionTimeout);
			context.getSessionHandler().setSessionManager(sm);
			context.setResourceBase(basePath);
			context.setClassLoader(Thread.currentThread().getContextClassLoader());

			
			// Time to organize Thymeleaf
			ThymeleafEngine.initialise();

			// Resource Handling (i.e. JavaScript, CSS, images, etc...
			List<WebAppContext> resourceHandlers = new ArrayList<WebAppContext>();
			resourceHandlers.add(createResourceHandler("/img", (shareInImmidiatePath) ? "/share/resources/img" : "../../share/resources/img"));
			resourceHandlers.add(createResourceHandler("/images", (shareInImmidiatePath) ? "/share/resources/images" : "../../share/resources/images"));
			resourceHandlers.add(createResourceHandler("/css", (shareInImmidiatePath) ? "/share/resources/css" : "../../share/resources/css"));
			resourceHandlers.add(createResourceHandler("/js", (shareInImmidiatePath) ? "/share/resources/js" : "../../share/resources/js"));
			resourceHandlers.add(createResourceHandler("/sysconf/css", (shareInImmidiatePath) ? "/share/resources/css" : "../../share/resources/css"));
			resourceHandlers.add(createResourceHandler("/fonts", (shareInImmidiatePath) ? "/share/resources/fonts" : "../../share/resources/fonts"));

			// All context handlers
			Handler[] handers = new Handler[resourceHandlers.size() + 1];
			handers[0] = context;
			for (int i = 0; i < resourceHandlers.size(); i++)
			{
				handers[i + 1] = resourceHandlers.get(i);
			}
			ContextHandlerCollection contexts = new ContextHandlerCollection();
			contexts.setHandlers(handers);

			// Assign context handlers and start servers
			server.setHandler(contexts);
			server.start();
			server.join();
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
	}

	/**
	 *	Associate folders for resource handling 
	 */
	private static WebAppContext createResourceHandler(String contextPath, String path) throws IOException
	{
		// Configure resources handler for resources
		WebAppContext handler = new WebAppContext();
		handler.setContextPath(contextPath);
		ResourceCollection resource = new ResourceCollection(new String[] { Utility.expandPath(CmdArgs.baseDir + path) });
		handler.setBaseResource(resource);

		// Add gzip compression for resources
		EnumSet<DispatcherType> all = EnumSet.of(DispatcherType.ASYNC, DispatcherType.ERROR, DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.REQUEST);
		FilterHolder gzipFilter = new FilterHolder(new IncludableGzipFilter());
		gzipFilter.setInitParameter("mimeTypes", "text/html,text/plain,text/xml,application/xhtml+xml,text/css,application/javascript,application/json,image/svg+xml");
		handler.addFilter(gzipFilter, "/*", all);

		return handler;
	}
	
	private static void loadSystemInfo()
	{
		SystemInfoResponse sysInfo = UiConnectionClient.getInstance().systemInfoRequest();
		if (sysInfo != null)
		{
			VERSION = sysInfo.getVersion();
			if (sysInfo.isDebugMode())
				OPERATING_MODE = OperatingMode.DEBUG;
			else
				OPERATING_MODE = OperatingMode.NORMAL;
		}
	}
	
	public static boolean isRunningInDebugMode()
	{
		if (OPERATING_MODE == OperatingMode.DEBUG)
			loadSystemInfo();
		
		return (OPERATING_MODE == OperatingMode.DEBUG);
	}
	
	/**
	 * Handle GUI Versioning. Note that [REVISION] in ServiceBus ... Version.java file
	 * This needs to be replace on Jenkins Build
	 * @return
	 */
	public static String getVersion()
	{
		if (VERSION == null)
			loadSystemInfo();
			
		if (VERSION == null)
		{
			return "UNKNOWN";
		}
		else
		{
			if (VERSION.contains("[REVISION]"))
			{
				return "0.0.667";
			}
		}
		return VERSION;
	}

	/**
	 * Checks to see if a specific port is available.
	 * 
	 * @param port
	 *            the port to check for availability
	 */
	public static boolean checkPortAvailable(int port)
	{

		ServerSocket ss = null;
		DatagramSocket ds = null;
		try
		{
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		}
		catch (IOException e)
		{
		}
		finally
		{
			if (ds != null)
			{
				ds.close();
			}

			if (ss != null)
			{
				try
				{
					ss.close();
				}
				catch (IOException e)
				{
				}
			}
		}

		return false;
	}
}
