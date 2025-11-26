package hxc.utils.xmlrpc;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

import hxc.utils.http.HttpConnection;

public class XmlRpcConnection extends HttpConnection
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	// private XmlRpcSerializer serializer = new XmlRpcSerializer();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public XmlRpcConnection(HttpURLConnection httpURLConnection)
	{
		super(httpURLConnection);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public <T, TReturn> TReturn call(T request, Class<TReturn> returnType) throws XmlRpcException
	{
		Object result = null;

		try
		{
			if (!super.isConnected())
			{
				try
				{
					super.setRequestProperty("Content-Type", "text/xml");
					super.setRequestProperty("Accept", "text/xml");
					super.connect();
				}	
				catch (IOException e)
				{
					super.disconnect();
					throw new XmlRpcException(e.getMessage(), -1, e, XmlRpcException.Context.Connect);
				}
			}

			try
			{
				// Serialise the request
				XmlRpcSerializer serializer = new XmlRpcSerializer();
				String xml = serializer.serialize(request);
				OutputStreamWriter writer = new OutputStreamWriter(super.getOutputStream());
				writer.write(xml);
				writer.close();
	
				// DeSerialise the Result
				result = serializer.deSerialize(super.getInputStream(), returnType);
				if (result instanceof XmlRpcException)
					throw (XmlRpcException) result;
				else if (result instanceof Throwable)
				{
					Throwable throwable = (Throwable) result;
					throw new XmlRpcException(throwable.getMessage(), -1, throwable);
				}
			}
			catch (XmlRpcException e)
			{
				super.disconnect();
				throw e;
			}
			catch (IOException e)
			{
				super.disconnect();
				throw new XmlRpcException(e.getMessage(), -1, e);
			}
		}
		finally
		{
			super.disconnect();
		}

		return (TReturn) result;
	}

}
