package hxc.connectors.ui.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UIClientExchange implements Runnable
{
	final static Logger logger = LoggerFactory.getLogger(UIClientExchange.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private Socket socket;
	private boolean run = true;
	private UIServer server;

	public UIClientExchange(UIServer server, Socket socket)
	{
		this.server = server;
		this.socket = socket;
	}

	@Override
	public void run()
	{
		ObjectOutputStream out = null;
		ObjectInputStream in = null;

		try
		{
			in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

			while (run)
			{
				// Incoming Request
				Object ro = in.readObject();
				UiBaseRequest request = (UiBaseRequest) ro;
				UiBaseResponse response = server.handleUiRequest(request);

				// Handle response
				if (response != null)
				{
					if (out == null)
					{
						out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
					}
					out.writeObject(response);
					out.flush();
				}
				else
				{
					break;
				}
			}
		}
		catch (IOException | ClassNotFoundException e1)
		{
			if (out != null)
			{
				try
				{
					out.flush();
				}
				catch (Exception e)
				{
				}
			}

		}
		finally
		{
			try
			{
				if (in != null)
					in.close();

				if (out != null)
					out.close();

				if (socket != null)
					socket.close();
			}
			catch (IOException e)
			{
				logger.info(e.getMessage());
			}
		}
	}
}
