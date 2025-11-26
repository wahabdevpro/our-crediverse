package com.concurrent.hxc;

public class GetLocationRequest extends RequestHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private String variantID;
	private Number subscriberNumber;
	private Number subjectNumber;
	protected boolean requestCoordinates = false;
	protected boolean requestAddress = false;

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

	public String getVariantID()
	{
		return variantID;
	}

	public void setVariantID(String variantID)
	{
		this.variantID = variantID;
	}

	public Number getSubscriberNumber()
	{
		return subscriberNumber;
	}

	public void setSubscriberNumber(Number subscriberNumber)
	{
		this.subscriberNumber = subscriberNumber;
	}

	public Number getSubjectNumber()
	{
		return subjectNumber;
	}

	public void setSubjectNumber(Number subjectNumber)
	{
		this.subjectNumber = subjectNumber;
	}

	public boolean getRequestCoordinates()
	{
		return requestCoordinates;
	}

	public void setRequestCoordinates(boolean requestCoordinates)
	{
		this.requestCoordinates = requestCoordinates;
	}

	public boolean getRequestAddress()
	{
		return requestAddress;
	}

	public void setRequestAddress(boolean requestAddress)
	{
		this.requestAddress = requestAddress;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	// Default Constructor
	public GetLocationRequest()
	{

	}

	// Copy Constructor
	public GetLocationRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(GetLocationRequest request)
	{
		// Validate Header
		String problem = RequestHeader.validate(request);
		if (problem != null)
			return problem;

		if (request.serviceID == null || request.serviceID.length() == 0)
			return "No ServiceID";

		problem = Number.validate(request.subscriberNumber);
		if (problem != null)
			return problem;

		return null;
	}

}
