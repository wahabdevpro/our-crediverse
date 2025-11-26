package com.concurrent.soap;

import com.concurrent.util.ICallable;
import com.concurrent.util.ISerialiser;

public class GetServicesRequest extends RequestHeader implements
		ICallable<GetServicesResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Number subscriberNumber;
	private boolean activeOnly;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public Number getSubscriberNumber()
	{
		return subscriberNumber;
	}

	public void setSubscriberNumber(Number subscriberNumber)
	{
		this.subscriberNumber = subscriberNumber;
	}

	public boolean isActiveOnly()
	{
		return activeOnly;
	}

	public void setActiveOnly(boolean activeOnly)
	{
		this.activeOnly = activeOnly;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	/**
	 * Default Constructor
	 */
	public GetServicesRequest()
	{

	}

	/**
	 * Copy Constructor
	 */
	public GetServicesRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ICallable Implementation
	//
	// /////////////////////////////////

	@Override
	public void serialise(ISerialiser serialiser)
	{
		super.serialise(serialiser);

		serialiser.add("subscriberNumber", subscriberNumber);
		serialiser.add("activeOnly", activeOnly);
	}

	@Override
	public GetServicesResponse deserialiseResponse(ISerialiser serialiser)
	{
		GetServicesResponse response = new GetServicesResponse(); 
		response.deserialise(serialiser);
		return response;
	}

	@Override
	public String getMethodID()
	{
		return "getServices";
	}
}
