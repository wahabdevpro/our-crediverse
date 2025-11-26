package hxc.utils.http;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;

public abstract class HttpServer implements com.sun.net.httpserver.HttpHandler
{
	final static Logger logger = LoggerFactory.getLogger(HttpServer.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// private Properties
	//
	// /////////////////////////////////
	private com.sun.net.httpserver.HttpServer server;
	private ExecutorService threadPoolExecutor;
	private HttpContext context;

	private static final int maxTreadPool = 10;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Start / Stop
	//
	// /////////////////////////////////
	public void start(int port, final String url) throws IOException
	{
		try
		{
			logger.info("Starting Server {}:{}", url, port);
			server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(port), 0);
		}
		catch (IOException e)
		{
			if (e instanceof BindException)
				logger.error("Binding Error url:{} port:{}", url, port);
			else
				logger.error("None binding IO exception", e);
			throw e;
		}
		context = server.createContext(url, this);
		threadPoolExecutor = Executors.newFixedThreadPool(maxTreadPool, new ThreadFactory()
		{

			private int num = 0;

			@Override
			public Thread newThread(Runnable r)
			{
				return new Thread(r, url + "ThreadPool-" + num++);
			}

		});
		server.setExecutor(threadPoolExecutor);
		server.start();

	}

	public void stop()
	{
		if (server != null)
		{
			try
			{
				server.stop(1);
				threadPoolExecutor.shutdownNow();
			}
			catch (Exception e)
			{
				logger.error("Unable to stop HTTP server", e);
			}
			finally
			{
				threadPoolExecutor = null;
				server = null;
			}
		}
	}

	public void setAuthenticator(BasicAuthenticator authenticator)
	{
		context.setAuthenticator(authenticator);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// com.sun.net.httpserver.HttpHandler Implementation
	//
	// /////////////////////////////////
	@Override
	public final void handle(HttpExchange arg0) throws IOException
	{
		HttpRequest request = new HttpRequest(arg0);
		uponHttpRequest(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Callback
	//
	// /////////////////////////////////
	protected abstract void uponHttpRequest(HttpRequest request) throws IOException;

}
