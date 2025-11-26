package hxc.userinterfaces.gui.jetty;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;

import hxc.userinterfaces.gui.data.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequenceHandler extends HandlerCollection
{
	final static Logger logger = LoggerFactory.getLogger(SequenceHandler.class);
	private Handler authHandler;
	private Map<String, Handler> pluginHandlers;
	private Map<String, String> aliasMap;

	public void setAuthHandler(Handler authHandler)
	{
		this.authHandler = authHandler;
		super.addHandler(authHandler);
	}

	public void setMaxInactiveInterval(int timeout)
	{
		if (isStarted())
		{
			for (Handler handler : getHandlers())
			{
				if (handler instanceof WebAppContext)
				{
					WebAppContext rctx = (WebAppContext) handler;
					rctx.getSessionHandler().getSessionManager().setMaxInactiveInterval(timeout);
				}
			}
		}
	}

	public void setPluginHandler(String pathPrefix, Handler handler)
	{
		if (pluginHandlers == null)
		{
			pluginHandlers = new HashMap<String, Handler>();
		}
		pluginHandlers.put(pathPrefix, handler);
		super.addHandler(handler);
	}

	/* ------------------------------------------------------------ */
	/**
	 * @see Handler#handle(String, Request, HttpServletRequest, HttpServletResponse)
	 * 
	 *      This custom handler first passes the request to the authentication handler. If the request authenticates a user (or has a valid session) then the request is passed on to the correct
	 *      handler for this plugin.
	 */
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		Handler[] handlers = getHandlers();

		if (handlers != null && isStarted())
		{
			// Pass to Tapestry for authentication and other tapestry related handling
			authHandler.handle(target, baseRequest, request, response);

			// Check session for attribute indicating this is a request for another plugin.
			HttpServletRequest req = (HttpServletRequest) request;
			String state = (String) req.getAttribute("plugin");
			if (state != null && state.equals("true"))
			{
				if (pluginHandlers != null)
				{
					String className = User.class.getName();
					HttpSession sess = req.getSession(false);
					if (sess != null)
					{
						User currentUser = (User) sess.getAttribute("sso:" + className);
						if (currentUser != null)
						{
							baseRequest.setAttribute("authenticatedUser", currentUser);
							baseRequest.removeAttribute("plugin");
							baseRequest.setHandled(false);

							/*
							 * If we get here, we have an authenticated user, now we just need to find the correct handler for the request.
							 * 
							 * First we get the prefix for this URL, which is a max of 2 sections.
							 */
							// String url = StringUtil.getUrlParts(req.getRequestURI(), 2);

							String url = req.getRequestURI();

							Handler reqHandler = null;
							String prefix = null;
							// Then find the correct handler for this URL and handle the request.
							Iterator<String> pluginIt = pluginHandlers.keySet().iterator();
							while (pluginIt.hasNext())
							{
								prefix = pluginIt.next();
								if (url.startsWith(prefix))
								{
									reqHandler = pluginHandlers.get(prefix);
									break;
								}
							}

							// Handler reqHandler = pluginHandlers.get(url);
							if (reqHandler != null)
							{
								/*
								 * if (prefix != null) { baseRequest.setContextPath("/admingui"); }
								 */
								// public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
								String targetPrefix = "";
								if (aliasMap != null && aliasMap.containsKey(prefix))
								{
									targetPrefix = aliasMap.get(prefix);
								}
								reqHandler.handle(targetPrefix + target, baseRequest, request, response);
							}
							else
							{

								logger.error("Unable to locate handler for prefix " + url);
							}
						}
						else
						{
							logger.error("Unable to authenticate user for url " + req.getRequestURI());
						}
					}
					else
					{
						logger.error("Unable to obtain session for url " + req.getRequestURI());
					}
				}
				else
				{
					logger.error("No plugin handlers configured " + req.getRequestURI());
				}
			}
		}
	}

	public Handler getHandler(String url)
	{
		Handler reqHandler = null;
		// Then find the correct handler for this URL and handle the request.
		Iterator<String> pluginIt = pluginHandlers.keySet().iterator();
		while (pluginIt.hasNext())
		{
			String prefix = pluginIt.next();
			if (url.startsWith(prefix))
			{
				reqHandler = pluginHandlers.get(prefix);
				break;
			}
		}
		return reqHandler;
	}

	public void registerAliasHandler(String originalContext, String alias)
	{
		Handler reqHandler = getHandler(originalContext);
		if (reqHandler != null)
		{
			pluginHandlers.put(alias, reqHandler);
			// setPluginHandler(alias, reqHandler);
			if (aliasMap == null)
			{
				aliasMap = new HashMap<String, String>();
			}
			aliasMap.put(alias, originalContext);
		}
	}

	public boolean hasHandler(String path)
	{
		boolean result = false;
		if (pluginHandlers != null)
		{
			Iterator<String> pluginIt = pluginHandlers.keySet().iterator();
			while (pluginIt.hasNext())
			{
				String prefix = pluginIt.next();
				if (path.startsWith(prefix))
				{
					result = true;
					break;
				}
			}
		}
		return result;
	}
}
