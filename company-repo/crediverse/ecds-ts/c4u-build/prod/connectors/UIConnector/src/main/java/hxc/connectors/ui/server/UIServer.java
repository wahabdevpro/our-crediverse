package hxc.connectors.ui.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.servicebus.IServiceBus;
import hxc.utils.protocol.uiconnector.request.UiBaseRequest;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public abstract class UIServer implements Runnable
{
	final static Logger logger = LoggerFactory.getLogger(UIServer.class);
	
	private Thread thread;
	private ServerSocket ss;
	private boolean run;
	IServiceBus esb;

	private ExecutorService threadPool = Executors.newCachedThreadPool(new ThreadFactory()
	{

		private int num = 0;

		@Override
		public Thread newThread(Runnable r)
		{
			return new Thread(r, "UiConnectorThreadPool-" + num++);
		}

	});

	public UIServer(IServiceBus esb)
	{
		this.esb = esb;
	}

	public void start(int serverPort) throws IOException, InterruptedException
	{		
		try
		{
			ss = new ServerSocket(serverPort);
			run = true;
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
		thread = new Thread(this, "UiServer Thread");
		thread.start();
	}

	public void stop()
	{
		run = false;
		this.threadPool.shutdown();
		thread = null;
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
		ss = null;

	}

	@Override
	public void run()
	{
		try {
			esb.waitForRunning();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		
		try
		{
			while (run)
			{
				Socket sock = ss.accept();
				this.threadPool.execute(new UIClientExchange(this, sock));
				// UIClientExchange uic = new UIClientExchange(this, sock, logger);
				// (new Thread(uic)).start();
			}
		}
		catch (IOException ex)
		{
			if (run)
			{
				logger.error(ex.getMessage(), ex);
			}
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Callback
	//
	// /////////////////////////////////
	protected abstract UiBaseResponse handleUiRequest(UiBaseRequest request) throws IOException;

}
