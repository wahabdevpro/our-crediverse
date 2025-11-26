package hxc.utils.uiconnector.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UIClient implements AutoCloseable
{
	final static Logger logger = LoggerFactory.getLogger(UIClient.class);
	
	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream iis;
	private static final int TIMEOUT = 5000;

	public void connect(String host, int port) throws UnknownHostException, IOException
	{
		socket = new Socket();
		socket.connect(new InetSocketAddress(host, port), TIMEOUT);
		oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
	}

	@Override
	public void close()
	{
		try
		{
			oos.close();
		}
		catch (IOException e)
		{
		}
		try
		{
			iis.close();
		}
		catch (IOException e)
		{
		}
		try
		{
			socket.close();
		}
		catch (Exception e)
		{
		}

		iis = null;
		oos = null;
		socket = null;
	}

	@SuppressWarnings("unchecked")
	public <T, TReturn> TReturn call(T request, Class<TReturn> returnType) throws IOException, ClassNotFoundException
	{
		TReturn result = null;
		try
		{
			oos.writeObject(request);
			oos.flush();
			if (iis == null)
			{
				iis = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
			}
			result = (TReturn) iis.readObject();
		}
		catch (EOFException eof)
		{
			logger.error(eof.getMessage(), eof);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
			try
			{
				throw e.getCause();
			}
			catch (Throwable e1)
			{
				throw e;
			}
		}

		catch (ClassNotFoundException e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
		return (TReturn) result;
	}

}
