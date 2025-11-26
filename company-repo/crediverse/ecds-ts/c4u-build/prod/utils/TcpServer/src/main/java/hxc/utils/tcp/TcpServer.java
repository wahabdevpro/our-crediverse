package hxc.utils.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TcpServer
{
	final static Logger logger = LoggerFactory.getLogger(TcpServer.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	private Thread acceptorThread;
	private ExecutorService executor;
	private ServerSocket server;

	private String prompt;
	private String lineSeparator;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public TcpServer(int threadPoolSize)
	{
		// Create the thread pool executor
		this.executor = Executors.newFixedThreadPool(threadPoolSize);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public void start(final int port) throws IOException
	{
		// Stop the server
		stop();

		try
		{
			// Create the server socket
			server = new ServerSocket(port);
		}
		catch (IOException e)
		{
			logger.error("Failed to start", e);
		}

		// Create the acceptor thread
		acceptorThread = new Thread()
		{

			@Override
			public void run()
			{
				// Start the infinite loop
				while (server != null)
				{
					try
					{
						// Wait for sockets to connect
						Socket socket = server.accept();
						
						socket.setTcpNoDelay(true);						

						// Create the TCP request
						TcpRequest request = new TcpRequest(TcpServer.this, socket);

						// Execute the connection recieved method
						connectionRecieved(request);

						// Execute the request
						executor.execute(request);
					}
					catch (IOException e)
					{
						logger.error("Failed to run", e);
					}
				}

			}

		};

		acceptorThread.start();
	}

	public void stop() throws IOException
	{
		// Ensure the acceptor thread is still alive
		if (server != null)
		{
			// Shutdown the executor
			executor.shutdownNow();

			// Stop the server
			server.close();
			server = null;

			// Interrupt the thread and wait for the thread to end
			acceptorThread.interrupt();

			try
			{
				acceptorThread.join();
			}
			catch (InterruptedException e)
			{

			}
		}

	}

	public String getPrompt()
	{
		return prompt;
	}

	public void setPrompt(String prompt)
	{
		this.prompt = prompt;
	}

	public String getLineSeparator()
	{
		return lineSeparator;
	}

	public void setLineSeparator(String lineSeparator)
	{
		this.lineSeparator = lineSeparator;
	}

	// Override to recieve a connection from the client
	public void connectionRecieved(TcpRequest request)
	{
	}

	// Override to close the connection
	public void connectionClosed(TcpRequest request)
	{
	}

	// Gets called when a request comes through
	public abstract void uponTcpRequest(TcpRequest request);
}
