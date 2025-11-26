package hxc.utils.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

import javax.xml.bind.DatatypeConverter;

import hxc.utils.reflection.ClassInfo;
import hxc.utils.reflection.FieldInfo;
import hxc.utils.reflection.ReflectionHelper;

public class HttpConnection implements AutoCloseable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	protected HttpURLConnection httpURLConnection;
	private FieldInfo connectedField = null;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public HttpConnection(HttpURLConnection httpURLConnection)
	{
		this.httpURLConnection = httpURLConnection;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// AutoCloseable Implementation
	//
	// /////////////////////////////////
	@Override
	public void close() throws Exception
	{
		// TODO Auto-generated method stub

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public void setBasicAuthorization(String userName, String password)
	{
		String basicAuth = "Basic " + new String(DatatypeConverter.printBase64Binary((userName + ":" + password).getBytes()));
		httpURLConnection.setRequestProperty("Authorization", basicAuth);
	}

	public void setRequestProperty(String key, String value)
	{
		httpURLConnection.setRequestProperty(key, value);
	}

	public void setConnectionTimeout(int timeout)
	{
		httpURLConnection.setConnectTimeout(timeout);
	}

	public void setReadTimeout(int timeout)
	{
		httpURLConnection.setReadTimeout(timeout);
	}

	public void connect() throws IOException
	{
		httpURLConnection.connect();
	}

	public InputStream getInputStream() throws IOException
	{
		return httpURLConnection.getInputStream();
	}

	public OutputStream getOutputStream() throws IOException
	{
		return httpURLConnection.getOutputStream();
	}

	public void setRequestMethod(String method) throws ProtocolException
	{
		httpURLConnection.setRequestMethod(method);
	}

	public int getResponseCode() throws IOException
	{
		return httpURLConnection.getResponseCode();
	}

	public void disconnect()
	{
		httpURLConnection.disconnect();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	// ?? Have to use reflection for now
	public boolean isConnected()
	{
		if (httpURLConnection == null)
			return false;

		if (connectedField == null)
		{
			ClassInfo classInfo = ReflectionHelper.getClassInfo(httpURLConnection.getClass());
			connectedField = classInfo.getFields().get("connected");
		}

		if (connectedField == null)
			return false;

		try
		{
			return (boolean) connectedField.get(httpURLConnection);
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			return false;
		}

	}

}
