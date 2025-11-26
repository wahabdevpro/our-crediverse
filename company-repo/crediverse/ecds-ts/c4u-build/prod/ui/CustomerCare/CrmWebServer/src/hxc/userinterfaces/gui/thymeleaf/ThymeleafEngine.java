package hxc.userinterfaces.gui.thymeleaf;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.messageresolver.StandardMessageResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

public class ThymeleafEngine
{
	// private static Map<String, IThymeleafController> controllersByURL;
	private static TemplateEngine templateEngine = new TemplateEngine();
	private static Map<String, IThymeleafController> controllersByURL = null;

	private static IThymeleafController defaultController = null;

	private ThymeleafEngine()
	{
		super();
	}

	/**
	 * 
	 * @param templateFolder
	 */
	public static void registerTemplateFolder(String templateFolder)
	{
		templateEngine.addMessageResolver(new StandardMessageResolver());

		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver();
		// XHTML is the default mode, but we will set it anyway for better understanding of code
		templateResolver.setTemplateMode("XHTML");
		// This will convert "home" to "/WEB-INF/templates/home.html"
		templateResolver.setPrefix(templateFolder);
		templateResolver.setSuffix(".html");
		// Set template cache TTL to 1 hour. If not set, entries would live in cache until expelled by LRU
		templateResolver.setCacheTTLMs(3600000L);
		// be automatically updated when modified.
		templateResolver.setCacheable(false);

		// Testing locale
		templateEngine.addTemplateResolver(templateResolver);
		templateEngine.initialize();
	}

	public static void registerController(String url, IThymeleafController controller)
	{
		if (controllersByURL == null)
		{
			controllersByURL = new HashMap<>();
		}
		controllersByURL.put(url, controller);
	}

	public static IThymeleafController resolveControllerForRequest(final HttpServletRequest request)
	{
		if (controllersByURL != null && controllersByURL.containsKey(request))
		{
			return controllersByURL.get(request);
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
