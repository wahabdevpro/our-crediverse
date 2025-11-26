package hxc.utils.tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpRequest implements Runnable
{
	final static Logger logger = LoggerFactory.getLogger(TcpRequest.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	private TcpServer server;
	private Socket socket;

	private String request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public TcpRequest(TcpServer server, Socket socket)
	{
		this.server = server;
		this.socket = socket;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Method
	//
	// /////////////////////////////////

	public String getRequest()
	{
		return request;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Runnable Implementation
	//
	// /////////////////////////////////

	@Override
	public void run()
	{
		// Open the input stream for requests
		try(InputStreamReader ireader = new InputStreamReader(socket.getInputStream()))
		{
			try (BufferedReader reader = new BufferedReader(ireader))
			{
				try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())))
				{
					// Reply to the user
					reply(writer, server.getPrompt());
					boolean quitting = false;
					// Read from the stream					
					while (!quitting) //&& !request.equalsIgnoreCase("quit")
					{
						request = reader.readLine();
						logger.info("CaiSim received request: " + request);
						quitting = request == null || request.equalsIgnoreCase("quit");
						if(!quitting)
						{
							// Execute the request method
							server.uponTcpRequest(this);
	
							// Reply to the user
							logger.info("CaiSim sending prompt.");
							reply(writer, server.getPrompt());
							logger.info("CaiSim sent prompt.");
						} else {
							break;
						}
					}
				}
			}			
		} catch (Exception e)
		{
			logger.error("CaiSim failed to run.");
			logger.info(e.toString());
		}
		finally
		{
			try
			{
				// Finally close the socket
				socket.close();
			}
			catch (IOException e)
			{

			}

			// Close the connection
			server.connectionClosed(this);
		}
	}

	// Responds to the client
	public void respond(TcpResponse response) throws IOException
	{
		// Open the output stream to write the response
		PrintWriter printer = new PrintWriter(socket.getOutputStream());

		// Write the response
		if (server.getLineSeparator() != null)
			printer.print(String.format("%s%s", response.getResponse(), server.getLineSeparator()));
		else
			printer.println(response.getResponse());

		// Flush the content
		printer.flush();
	}

	// Replies to the client
	public void reply(BufferedWriter writer, String message) throws IOException
	{
		// Ensure the writers are not null
		if (writer != null && message != null)
		{
			// Write the prompt
			writer.append(server.getPrompt());

			// Flush the buffer
			writer.flush();
		}
	}
	
	/**
	 * @return Current socket to allow for checking of current socket clients connections
	 */
	public Socket getSocket()
	{
		return socket;
	}
}
