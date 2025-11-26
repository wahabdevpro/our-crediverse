package hxc.utils.xmlrpc;

import java.io.IOException;

import hxc.utils.http.HttpClient;

public class XmlRpcClient extends HttpClient
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private int connectTimeout = 0;
	private int readTimeout = 0;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public XmlRpcClient(String url)
	{
		super(url);

		// For SGW
		// A User-Agent and Host must be specified.
		// The Content-Type is text/xml.
		// The Content-Length must be specified and must be correct.

	}

	public XmlRpcClient(String url, int connectTimeout, int readTimeout)
	{
		super(url);
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public XmlRpcConnection getConnection() throws IOException
	{
		if (connectTimeout != 0 || readTimeout != 0)
			return new XmlRpcConnection(super.getHttpURLConnection(connectTimeout, readTimeout));
		else
			return new XmlRpcConnection(super.getHttpURLConnection());
	}

}
