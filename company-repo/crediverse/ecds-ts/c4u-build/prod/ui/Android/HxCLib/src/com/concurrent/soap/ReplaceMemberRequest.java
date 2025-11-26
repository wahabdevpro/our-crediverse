package com.concurrent.soap;

public class ReplaceMemberRequest extends RequestHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private String variantID;
	private Number subscriberNumber;
	private Number oldMemberNumber;
	private Number newMemberNumber;

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

	public Number getOldMemberNumber()
	{
		return oldMemberNumber;
	}

	public void setOldMemberNumber(Number oldMemberNumber)
	{
		this.oldMemberNumber = oldMemberNumber;
	}

	public Number getNewMemberNumber()
	{
		return newMemberNumber;
	}

	public void setNewMemberNumber(Number newMemberNumber)
	{
		this.newMemberNumber = newMemberNumber;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	/**
	 * Default Constructor
	 */
	public ReplaceMemberRequest()
	{

	}

	/**
	 * Copy Constructor
	 */
	public ReplaceMemberRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(ReplaceMemberRequest request)
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

		problem = Number.validate(request.newMemberNumber);
		if (problem != null)
			return problem;

		problem = Number.validate(request.oldMemberNumber);
		if (problem != null)
			return problem;

		return null;
	}

}
