package hxc.utils.xmlrpc;

import java.io.IOException;
import java.io.OutputStream;

import hxc.utils.http.HttpRequest;

public class XmlRpcRequest
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private Object methodCall;
	private HttpRequest httpRequest;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public XmlRpcRequest(Object methodCall, HttpRequest httpRequest)
	{
		this.methodCall = methodCall;
		this.httpRequest = httpRequest;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public <T> T getMethodCall()
	{
		return (T) methodCall;
	}

	public <T> void respond(T methodResponse) throws IOException
	{
		httpRequest.addHeader("Content-Type", "text/xml");
		XmlRpcSerializer serializer = new XmlRpcSerializer();
		String xml = serializer.serialize(methodResponse);
		byte[] xmlBytes = xml.getBytes("UTF-8");
		httpRequest.sendResponseHeaders(200, xmlBytes.length);
		OutputStream os = httpRequest.getResponseBody();
		os.write(xmlBytes);
		os.close();
	}

	public boolean isTypeOf(Class<?> cls)
	{
		return cls.isInstance(methodCall);
	}

	public String getTypeName()
	{
		return methodCall.getClass().getSimpleName();
	}

}
