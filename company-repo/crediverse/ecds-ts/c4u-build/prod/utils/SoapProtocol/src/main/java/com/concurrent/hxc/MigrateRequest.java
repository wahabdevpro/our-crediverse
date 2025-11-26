package com.concurrent.hxc;

public class MigrateRequest extends RequestHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private String variantID;
	private String newServiceID;
	private String newVariantID;
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

	public String getNewServiceID()
	{
		return newServiceID;
	}

	public void setNewServiceID(String newServiceID)
	{
		this.newServiceID = newServiceID;
	}

	public String getNewVariantID()
	{
		return newVariantID;
	}

	public void setNewVariantID(String newVariantID)
	{
		this.newVariantID = newVariantID;
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
	public MigrateRequest()
	{

	}

	/**
	 * Copy Constructor
	 */
	public MigrateRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(MigrateRequest request)
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
