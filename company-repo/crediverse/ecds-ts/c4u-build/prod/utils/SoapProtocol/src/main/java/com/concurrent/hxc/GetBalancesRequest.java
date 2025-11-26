package com.concurrent.hxc;

public class GetBalancesRequest extends RequestHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private String variantID;
	private Number subscriberNumber;
	private boolean requestSMS = false;
	private String serialNumber;

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

	public boolean getRequestSMS()
	{
		return requestSMS;
	}

	public void setRequestSMS(boolean requestSMS)
	{
		this.requestSMS = requestSMS;
	}

	public String getSerialNumber()
	{
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber)
	{
		this.serialNumber = serialNumber;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	/**
	 * Default Constructor
	 */
	public GetBalancesRequest()
	{

	}

	/**
	 * Copy Constructor
	 */
	public GetBalancesRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(GetBalancesRequest request)
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
