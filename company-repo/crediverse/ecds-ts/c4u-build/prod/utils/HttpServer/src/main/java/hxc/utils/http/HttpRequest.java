package hxc.utils.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sun.net.httpserver.Headers;

public class HttpRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Private Properties
	//
	// /////////////////////////////////
	private com.sun.net.httpserver.HttpExchange exchange;
	private InputStream inputStream = null;
	private OutputStream outputStream = null;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public HttpRequest(com.sun.net.httpserver.HttpExchange exchange)
	{
		this.exchange = exchange;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public InputStream getRequestBody()
	{
		if (inputStream == null)
			inputStream = exchange.getRequestBody();
		return inputStream;
	}

	public void addHeader(String name, String value)
	{
		Headers h = exchange.getResponseHeaders();
		h.add("Content-Type", "application/xml");

	}

	public void sendResponseHeaders(int arg0, long arg1) throws IOException
	{
		exchange.sendResponseHeaders(arg0, arg1);

	}

	public OutputStream getResponseBody()
	{
		if (outputStream == null)
			outputStream = exchange.getResponseBody();
		return outputStream;
	}

}
