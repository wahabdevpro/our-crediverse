package hxc.utils.xmlrpc;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.utils.http.HttpRequest;
import hxc.utils.http.HttpServer;

public abstract class XmlRpcServer extends HttpServer
{
	final static Logger logger = LoggerFactory.getLogger(XmlRpcServer.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private Class<?>[] methodCallsToExpect;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public XmlRpcServer(Class<?>... methodCallsToExpect)
	{
		super();
		for(Class<?>clazz :  methodCallsToExpect)
		{
			if (clazz == null)
			{
				String msg = new String("XmlRpcServer:: Constructor called with NULL in list of classes. Check constructor calls for invalid parameters such as a logger instance (that is no longer supported)");
				logger.error(msg);
				throw new IllegalArgumentException(msg);
			}
		}
		this.methodCallsToExpect = methodCallsToExpect;

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Handler
	//
	// /////////////////////////////////
	@Override
	public final void uponHttpRequest(HttpRequest request) throws IOException
	{
		InputStream inputStream = request.getRequestBody();

		Object object;
		try
		{
			XmlRpcSerializer serializer = new XmlRpcSerializer();
			object = serializer.deSerializeAny(inputStream, methodCallsToExpect);
		}
		catch (XmlRpcException e)
		{
			throw new IOException(e.getMessage(), e);
		}
		XmlRpcRequest xrequest = new XmlRpcRequest(object, request);
		uponXmlRpcRequest(xrequest);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public void start(int port, String url) throws IOException
	{
		super.start(port, url);
	}

	@Override
	public void stop()
	{
		super.stop();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Callback
	//
	// /////////////////////////////////
	protected abstract void uponXmlRpcRequest(XmlRpcRequest request);

}
