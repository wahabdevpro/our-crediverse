package com.concurrent.hxc;

public class AddMemberRequest extends RequestHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private String variantID;
	private Number subscriberNumber;
	private Number memberNumber;
	private String memberType;

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

	public Number getMemberNumber()
	{
		return memberNumber;
	}

	public void setMemberNumber(Number memberNumber)
	{
		this.memberNumber = memberNumber;
	}

	public String getMemberType()
	{
		return memberType;
	}

	public void setMemberType(String memberType)
	{
		this.memberType = memberType;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	/**
	 * Default Constructor
	 */
	public AddMemberRequest()
	{

	}

	/**
	 * Copy Constructor
	 */
	public AddMemberRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(AddMemberRequest request)
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

		problem = Number.validate(request.memberNumber);
		if (problem != null)
			return problem;

		return null;
	}

}
