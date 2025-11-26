package com.concurrent.soap;

import com.concurrent.util.ICallable;
import com.concurrent.util.ISerialiser;

import hxc.servicebus.ReturnCodes;

public class GetReturnCodeTextRequest extends RequestHeader implements ICallable<GetReturnCodeTextResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private ReturnCodes returnCode;
	
	private static final long serialVersionUID = -4023235949907857528L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public String getServiceID()
	{
		return serviceID;
	}

	public void setServiceID(String serviceID)
	{
		this.serviceID = serviceID;
	}

	public ReturnCodes getReturnCode()
	{
		return returnCode;
	}

	public void setReturnCode(ReturnCodes returnCode)
	{
		this.returnCode = returnCode;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	/**
	 * Default Constructor
	 */
	public GetReturnCodeTextRequest()
	{
	}

	/**
	 * Copy Constructor
	 */
	public GetReturnCodeTextRequest(RequestHeader request)
	{
		super(request);
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	
	@Override
	public void serialise(ISerialiser serialiser)
	{
		super.serialise(serialiser);
		serialiser.add("serviceID", serviceID);
		serialiser.add("returnCode", returnCode);
	}
	

	@Override
	public GetReturnCodeTextResponse deserialiseResponse(ISerialiser serialiser)
	{
		GetReturnCodeTextResponse response = new GetReturnCodeTextResponse(this);
		response.deserialise(serialiser);
		return response;
	}

	@Override
	public String getMethodID()
	{
		return "getReturnCodeText";
	}

}
