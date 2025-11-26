package com.concurrent.hxc;

public class GetDeviceInfoRequest extends RequestHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Number subscriberNumber;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public Number getSubscriberNumber()
	{
		return subscriberNumber;
	}

	public void setSubscriberNumber(Number donorNumber)
	{
		this.subscriberNumber = donorNumber;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	// Default Constructor
	public GetDeviceInfoRequest()
	{

	}

	// Copy Constructor
	public GetDeviceInfoRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(GetDeviceInfoRequest request)
	{
		// Validate Header
		String problem = RequestHeader.validate(request);
		if (problem != null)
			return problem;

		problem = Number.validate(request.subscriberNumber);
		if (problem != null)
			return problem;

		return null;
	}

}
