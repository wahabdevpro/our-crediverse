package com.concurrent.hxc;

public class GetServicesRequest extends RequestHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Number subscriberNumber;
	private boolean activeOnly;
	private boolean suggested;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
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
	public GetServicesRequest()
	{

	}

	/**
	 * Copy Constructor
	 */
	public GetServicesRequest(RequestHeader request)
	{
		super(request);
	}
}
