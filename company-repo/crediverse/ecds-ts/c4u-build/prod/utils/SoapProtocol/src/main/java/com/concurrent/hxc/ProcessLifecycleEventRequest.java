package com.concurrent.hxc;

import java.util.Date;

import hxc.connectors.lifecycle.ISubscription;

public class ProcessLifecycleEventRequest extends RequestHeader implements ISubscription
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private String variantID;
	private String msisdn;
	private Date nextDateTime;
	private int serviceClass;
	private boolean beingProcessed;
	private int state;
	private Date dateTime1;
	private Date dateTime2;
	private Date dateTime3;
	private Date dateTime4;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@Override
	public String getServiceID()
	{
		return serviceID;
	}

	public void setServiceID(String serviceID)
	{
		this.serviceID = serviceID;
	}

	@Override
	public String getVariantID()
	{
		return variantID;
	}

	public void setVariantID(String variantID)
	{
		this.variantID = variantID;
	}

	@Override
	public String getMsisdn()
	{
		return msisdn;
	}

	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
	}

	@Override
	public int getServiceClass()
	{
		return serviceClass;
	}

	@Override
	public void setServiceClass(int serviceClass)
	{
		this.serviceClass = serviceClass;
	}

	@Override
	public Date getNextDateTime()
	{
		return nextDateTime;
	}

	@Override
	public void setNextDateTime(Date nextDateTime)
	{
		this.nextDateTime = nextDateTime;
	}

	@Override
	public boolean isBeingProcessed()
	{
		return beingProcessed;
	}

	@Override
	public void setBeingProcessed(boolean beingProcessed)
	{
		this.beingProcessed = beingProcessed;
	}

	@Override
	public int getState()
	{
		return state;
	}

	@Override
	public void setState(int state)
	{
		this.state = state;
	}

	@Override
	public Date getDateTime1()
	{
		return dateTime1;
	}

	@Override
	public void setDateTime1(Date dateTime1)
	{
		this.dateTime1 = dateTime1;
	}

	@Override
	public Date getDateTime2()
	{
		return dateTime2;
	}

	@Override
	public void setDateTime2(Date dateTime2)
	{
		this.dateTime2 = dateTime2;
	}

	@Override
	public Date getDateTime3()
	{
		return dateTime3;
	}

	@Override
	public void setDateTime3(Date dateTime3)
	{
		this.dateTime3 = dateTime3;
	}

	@Override
	public Date getDateTime4()
	{
		return dateTime4;
	}

	@Override
	public void setDateTime4(Date dateTime4)
	{
		this.dateTime4 = dateTime4;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	/**
	 * Default Constructor
	 */
	public ProcessLifecycleEventRequest()
	{

	}

	public ProcessLifecycleEventRequest(ISubscription subscription)
	{
		this.setMsisdn(subscription.getMsisdn());
		this.setServiceID(subscription.getServiceID());
		this.setVariantID(subscription.getVariantID());
		this.setState(subscription.getState());
		this.setServiceClass(subscription.getServiceClass());
		this.setNextDateTime(subscription.getNextDateTime());
		this.setBeingProcessed(subscription.isBeingProcessed());
		this.setDateTime1(subscription.getDateTime1());
		this.setDateTime2(subscription.getDateTime2());
		this.setDateTime3(subscription.getDateTime3());
		this.setDateTime4(subscription.getDateTime4());
	}

	/**
	 * Copy Constructor
	 */
	public ProcessLifecycleEventRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(ProcessLifecycleEventRequest request)
	{
		// Validate Header
		String problem = RequestHeader.validate(request);
		if (problem != null)
			return problem;

		if (request.serviceID == null || request.serviceID.length() == 0)
			return "No ServiceID";

		problem = Number.validate(new Number(request.msisdn));
		if (problem != null)
			return problem;

		return null;
	}
}
