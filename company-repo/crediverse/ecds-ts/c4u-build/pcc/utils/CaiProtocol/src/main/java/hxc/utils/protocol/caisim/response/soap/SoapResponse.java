package hxc.utils.protocol.caisim.response.soap;

import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.request.soap.SoapRequest;

public class SoapResponse
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	private Integer responseCode = Protocol.RESPONSE_CODE_INVALID_COMMAND;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	protected SoapResponse(SoapRequest request)
	{
	
	}

	protected SoapResponse()
	{

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Methods
	//
	// /////////////////////////////////

	public Integer getResponseCode()
	{
		return responseCode;
	}

	public void setResponseCode(Integer responseCode)
	{
		this.responseCode = responseCode;
	}
}
