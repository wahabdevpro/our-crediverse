package com.concurrent.hxc;

public class ValidatePINRequest extends RequestHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private String variantID;
	private Number subscriberNumber;
	private String pin;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties (getters and setters)
	//
	// /////////////////////////////////

	// serviceID
	public String getServiceID()
	{
		return serviceID;
	}

	public void setServiceID(String serviceID)
	{
		this.serviceID = serviceID;
	}

	// variantID
	public String getVariantID()
	{
		return variantID;
	}

	public void setVariantID(String variantID)
	{
		this.variantID = variantID;
	}

	// subscriberNumber (MSISDN)
	public Number getSubscriberNumber()
	{
		return subscriberNumber;
	}

	public void setSubscriberNumber(Number subscriberNumber)
	{
		this.subscriberNumber = subscriberNumber;
	}

	// PIN
	public String getPIN()
	{
		return pin;
	}

	public void setPIN(String pin)
	{
		this.pin = pin;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	// Default Constructor
	public ValidatePINRequest()
	{
	}

	// Copy Constructor
	public ValidatePINRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(ValidatePINRequest request)
	{
		// Validate Header
		String problem = RequestHeader.validate(request);
		if (problem != null)
			return problem;

		// request has to have a serviceID
		if (request.serviceID == null || request.serviceID.length() == 0)
			return "No ServiceID";

		// MSISDN_A validation
		problem = Number.validate(request.subscriberNumber);
		if (problem != null)
			return problem;

		// current PIN must be non-empty
		if ((request.pin == null) || (request.pin.length() == 0))
			return "PIN must be non-empty";

		return null;
	}
}
