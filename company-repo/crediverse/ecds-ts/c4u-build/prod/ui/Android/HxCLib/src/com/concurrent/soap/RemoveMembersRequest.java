package com.concurrent.soap;

public class RemoveMembersRequest extends RequestHeader
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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	/**
	 * Default Constructor
	 */
	public RemoveMembersRequest()
	{

	}

	/**
	 * Copy Constructor
	 */
	public RemoveMembersRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(RemoveMembersRequest request)
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
