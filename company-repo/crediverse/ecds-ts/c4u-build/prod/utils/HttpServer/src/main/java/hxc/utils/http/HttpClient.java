package hxc.utils.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

//import com.sun.org.apache.xml.internal.utils.URI;
//import com.sun.org.apache.xml.internal.utils.URI.MalformedURIException;

public class HttpClient
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private static final int DEFAULT_TIMEOUT_MS = 60000; // 1 Minute
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private String url;
	private URL urlObj;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public HttpClient(String url)
	{
		this.url = url;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	protected HttpURLConnection getHttpURLConnection() throws IOException
	{
		return getHttpURLConnection(DEFAULT_TIMEOUT_MS, DEFAULT_TIMEOUT_MS);
	}

	protected HttpURLConnection getHttpURLConnection(int connectTimeout_ms, int readTimout_ms) throws IOException
	{
		synchronized (this)
		{
			if (urlObj == null)
				urlObj = new URL(url);
		}

		HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setConnectTimeout(connectTimeout_ms);
		connection.setReadTimeout(readTimout_ms);
		return connection;
	}

	public HttpConnection getConnection() throws IOException
	{
		return new HttpConnection(getHttpURLConnection());
	}
	
	public String getHost()
	{
		if (url == null || url.isEmpty())
			return null;
		try
		{
			return new URI(url).getHost();
		}
		catch (URISyntaxException e)
		{
			return null;
		}
	}

}
