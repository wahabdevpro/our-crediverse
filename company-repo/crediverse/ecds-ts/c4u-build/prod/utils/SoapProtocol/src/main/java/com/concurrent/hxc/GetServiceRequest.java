package com.concurrent.hxc;

public class GetServiceRequest extends RequestHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private String variantID;
	private Number subscriberNumber;
	private boolean activeOnly;
	private boolean suggested;

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

	public boolean isActiveOnly()
	{
		return activeOnly;
	}

	public void setActiveOnly(boolean activeOnly)
	{
		this.activeOnly = activeOnly;
	}

	public boolean isSuggested()
	{
		return suggested;
	}

	public void setSuggested(boolean suggested)
	{
		this.suggested = suggested;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	/**
	 * Default Constructor
	 */
	public GetServiceRequest()
	{

	}

	/**
	 * Copy Constructor
	 */
	public GetServiceRequest(RequestHeader request)
	{
		super(request);
	}
}
