package hxc.userinterfaces.gui.jetty;

import java.io.File;

import hxc.userinterfaces.gui.thymeleaf.ThymeleafEngine;
import hxc.userinterfaces.gui.utils.GuiUtils;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.TagLibConfiguration;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

public class JettyMain
{
	private static Logger logger = LoggerFactory.getLogger(JettyMain.class);
	private static String VERSION = null;
	private static Server server = null;
	private static WebAppContext webapp = null;

	private static final String[][] RESOURCES = { { "/css", "/webapp/web/css" }, { "/fonts", "/webapp/web/fonts" }, { "/js", "/webapp/web/js" }, { "/img", "/webapp/web/img" },
			{ "/sysconf/css", "/webapp/web/css" }, { "/images", "/webapp/web/images" }, { "/i18n", "/webapp/web/i18n" }, };

	public final static void main(String[] args) throws Exception
	{
		CmdArgs.parseArgs(args);
		reconfigureLogger();

		// Resource Binding
		String[] pluginPaths = discoverPlugins();

		// ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		// SessionManager sm = new HashSessionManager();
		// ((HashSessionManager)sm).setSessionCookie("XXJSESSIONID");
		// context.setSessionHandler(new SessionHandler(sm));

		ContextHandlerCollection contextHandler = createWebAppContextHandler(pluginPaths);

		// Time to organize Thymeleaf
		ThymeleafEngine.registerTemplateFolder("/web/");

		// Time to organize plugins
		// PluginLoader.loadPlugins();
		PluginLoader.registerAllPluginServices(webapp, contextHandler);

		startJetty(CmdArgs.port, contextHandler, CmdArgs.debugLevel);
	}

	private static String[] discoverPlugins()
	{
		String[] pluginPaths = null;
		// ResourceCollection resources = new ResourceCollection();
		if (CmdArgs.pluginPaths != null)
		{
			try
			{
				pluginPaths = CmdArgs.pluginPaths.split(":");
				for (String path : pluginPaths)
				{
					System.out.printf(String.format("Loading Plugin: %s%n", path));
					try
					{
						StringBuilder webappFolder = new StringBuilder(path);
						if (!path.endsWith(File.separator))
							webappFolder.append(File.separator);
						webappFolder.append("webapp");

						// Resource Collection used to register multiple paths
						// resources.addPath(webappFolder.toString());
						// result.add(webappFolder.toString());

						// Class and templates
						PluginLoader.loadPluginClassPath(webappFolder.toString());
						// PluginLoader.addPluginTemplateFolder(webappFolder.toString());

					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}
		return pluginPaths;
	}

	public void addWebAppContext()
	{

	}

	private static void reconfigureLogger()
	{
		// Set the GUICORE_LOG_DIR system property for use in the logback config file
		System.setProperty("GUICORE_LOG_DIR", CmdArgs.logDir);

		// assume SLF4J is bound to logback in the current environment
		try
		{
			LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			// Call context.reset() to clear any previous configuration, e.g. default
			// configuration. For multi-step configuration, omit calling context.reset().
			context.reset();
			configurator.doConfigure(CmdArgs.baseDir + "/etc/logback-config.xml");
			StatusPrinter.printInCaseOfErrorsOrWarnings(context);
			logger.info("Logging started");
		}
		catch (JoranException je)
		{
			je.printStackTrace();
			// StatusPrinter will handle this
		}

		// line below left for historic purposes. Can remove one day.
		// System.setProperty("log4j.configuration","file:"+CmdArgs.baseDir+"/etc/log4j.properties"); // Where to look for the log4j config file
	}

	private static ContextHandlerCollection createWebAppContextHandler(String[] pluginPaths) throws Exception
	// private static Handler createWebAppContextHandler(ResourceCollection pluginResources, Map<String, String>) throws Exception
	{
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		try
		{
			webapp = new WebAppContext();

			webapp.setContextPath("/");

			String[] resoucePaths = new String[pluginPaths.length + 1];
			resoucePaths[0] = "webapp";
			for (int i = 0; i < pluginPaths.length; i++)
			{
				StringBuilder path = new StringBuilder(pluginPaths[i]);
				if (!pluginPaths[i].endsWith(File.separator))
					path.append(File.separator);
				path.append("webapp");
				resoucePaths[i + 1] = path.toString();
			}

			ResourceCollection resources = new ResourceCollection(resoucePaths);
			webapp.setBaseResource(resources);
			webapp.setConfigurations(new Configuration[] { new WebInfConfiguration(), new WebXmlConfiguration(), new MetaInfConfiguration(), new FragmentConfiguration(), new EnvConfiguration(),
					new PlusConfiguration(), new AnnotationConfiguration(), new JettyWebXmlConfiguration(), new TagLibConfiguration() });
			webapp.setParentLoaderPriority(true);

			SessionManager sm = new HashSessionManager();
			((HashSessionManager) sm).setSessionCookie("XXJSESSIONID");
			sm.setMaxInactiveInterval(CmdArgs.sessionTimeout);
			webapp.getSessionHandler().setSessionManager(sm);

			// webapp.getSessionHandler().getSessionManager().setMaxInactiveInterval(CmdArgs.sessionTimeout);
			// webapp.getSessionHandler().getSessionManager().setSessionIdPathParameterName("CCJSESSIONID");

			// Testing bind
			// org.eclipse.jetty.plus.jndi.EnvEntry woggle = new org.eclipse.jetty.plus.jndi.EnvEntry(webapp, "woggle", Integer.valueOf(4000), true);

			Handler[] handers = new Handler[RESOURCES.length + 1]; // WAS 2
			handers[0] = webapp;
			for (int i = 0; i < RESOURCES.length; i++)
			{
				String folderPath = RESOURCES[i][1];
				handers[i + 1] = GuiUtils.createResourceHandler(RESOURCES[i][0], CmdArgs.baseDir, folderPath);
			}

			contexts.setHandlers(handers);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return contexts;
	}

	private static void startJetty(int port, Handler contextHandler, int debugLevel) throws Exception
	{
		if (GuiUtils.checkPortAvailable(CmdArgs.port))
		{
			server = new Server(CmdArgs.port);
		}
		else
		{
			String message = String.format("GUI cannot be started as port %d is already in use\n", CmdArgs.port);
			throw new Exception(message);
			// System.exit(1);
		}

		// Assign context handlers and start servers
		server.setHandler(contextHandler);

		// Session Handling
		// SessionHandler sessionHandler = new SessionHandler();
		// SessionManager sessionManager = new HashSessionManager();
		// sessionHandler.setSessionManager(sessionManager);
		// sessionManager.setMaxInactiveInterval(CmdArgs.sessionTimeout);

		// Time to bind...
		ResourceBinder.bindServerScopeResources(server);

		server.start();
		server.join();
	}

	public static void stop() throws Exception
	{
		if (server != null)
		{
			server.stop();
			server.join();
			server.destroy();
			server = null;
		}
	}

	public static String getVersion()
	{
		if (VERSION == null)
		{
			// VERSION = UiConnectionClient.getInstance().versionRequest();
		}
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
}
