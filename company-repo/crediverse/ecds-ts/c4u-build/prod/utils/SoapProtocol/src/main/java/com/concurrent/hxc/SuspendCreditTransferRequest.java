package com.concurrent.hxc;

public class SuspendCreditTransferRequest extends RequestHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private String variantID;
	private String transferMode;
	private Number subscriberNumber;
	private Number memberNumber;

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

	public String getTransferMode()
	{
		return transferMode;
	}

	public void setTransferMode(String transferMode)
	{
		this.transferMode = transferMode;
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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	// Default Constructor
	public SuspendCreditTransferRequest()
	{

	}

	// Copy Constructor
	public SuspendCreditTransferRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(SuspendCreditTransferRequest request)
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