package hxc.services.airsim.protocol;

import java.util.Date;

import com.google.gson.Gson;

public class CallHistory
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String method;
	private Date timeStamp;
	private String request;
	private String response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public String getMethod()
	{
		return method;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}

	public Date getTimeStamp()
	{
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp)
	{
		this.timeStamp = timeStamp;
	}

	public String getRequest()
	{
		return request;
	}

	public void setRequest(String request)
	{
		this.request = request;
	}

	public String getResponse()
	{
		return response;
	}

	public void setResponse(String response)
	{
		this.response = response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////
	public CallHistory()
	{

	}

	public CallHistory(Gson gson, String method, Object request, Object response)
	{
		this.timeStamp = new Date();
		this.method = method;
		this.request = toJson(gson, request);
		this.response = toJson(gson, response);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	private String toJson(Gson gson, Object aucip)
	{
		if (aucip == null)
			return "";
		String result = gson.toJson(aucip);

		if (result.startsWith("{\"member\":"))
		{
			result = result.substring(10, result.length() - 1);
		}

		return result.replace("\"", "");
	}

	@Override
	public String toString()
	{
		return method;
	}

}
