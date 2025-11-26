package com.concurrent.soap;

public class ChangePINRequest extends RequestHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private String variantID;
	private Number subscriberNumber;

	private String oldPIN;
	private String newPIN;

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

	// oldPIN
	public String getOldPIN()
	{
		return oldPIN;
	}

	public void setOldPIN(String oldPIN)
	{
		this.oldPIN = oldPIN;
	}

	// newPIN
	public String getNewPIN()
	{
		return newPIN;
	}

	public void setNewPIN(String newPIN)
	{
		this.newPIN = newPIN;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	/**
	 * Default Constructor
	 */
	public ChangePINRequest()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * Copy Constructor
	 */
	public ChangePINRequest(RequestHeader request)
	{
		super(request);
		// TODO Auto-generated constructor stub
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(ChangePINRequest request)
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

		// current PIN be non-empty
		if ((request.oldPIN == null) || (request.oldPIN.length() == 0))
			return "OldPIN is Empty";

		// new PIN mustbe non-empty
		if ((request.newPIN == null) || (request.newPIN.length() == 0))
			return "NewPIN is Empty";

		return null;
	}
}
