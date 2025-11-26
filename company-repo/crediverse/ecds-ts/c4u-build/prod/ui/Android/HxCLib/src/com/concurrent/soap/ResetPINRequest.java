package com.concurrent.soap;

public class ResetPINRequest extends RequestHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private String variantID;
	private Number subscriberNumber;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties (setters & getters)
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

	// A-party MSISDN
	public Number getSubscriberNumber()
	{
		return subscriberNumber;
	}

	public void setSubscriberNumber(Number subscriberNumber)
	{
		this.subscriberNumber = subscriberNumber;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	/**
	 * Default Constructor
	 */

	public ResetPINRequest()
	{

	}

	/**
	 * Copy Constructor
	 */
	public ResetPINRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(ResetPINRequest request)
	{
		// Validate Header
		String problem = RequestHeader.validate(request);
		if (problem != null)
		{
			return problem;
		}

		// serviceID
		if (request.serviceID == null || request.serviceID.length() == 0)
		{
			return "No ServiceID";
		}

		// A-party MSISDN
		problem = Number.validate(request.subscriberNumber);
		if (problem != null)
			return problem;

		return null;
	}
}
