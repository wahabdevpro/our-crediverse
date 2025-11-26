package hxc.userinterfaces.gui.controller.service.confighandlers;

public class ServiceConfigHandlerResponse
{
	public enum ResponseType
	{
		Ajax,
		Page
	}
	
	private ResponseType responseType;
	private String response;

	public ServiceConfigHandlerResponse(){}
	
	public ServiceConfigHandlerResponse(ResponseType responseType, String response)
	{
		super();
		this.responseType = responseType;
		this.response = response;
	}

	public ResponseType getResponseType()
	{
		return responseType;
	}
	
	public void setResponseType(ResponseType responseType)
	{
		this.responseType = responseType;
	}
	
	public String getResponse()
	{
		return response;
	}
	
	public void setResponse(String response)
	{
		this.response = response;
	}
	
}
