package com.concurrent.soap;

import java.util.Date;

public class ProcessLifecycleEventRequest extends RequestHeader
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

	public String getMsisdn()
	{
		return msisdn;
	}

	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
	}

	public int getServiceClass()
	{
		return serviceClass;
	}

	public void setServiceClass(int serviceClass)
	{
		this.serviceClass = serviceClass;
	}

	public Date getNextDateTime()
	{
		return nextDateTime;
	}

	public void setNextDateTime(Date nextDateTime)
	{
		this.nextDateTime = nextDateTime;
	}

	public boolean isBeingProcessed()
	{
		return beingProcessed;
	}

	public void setBeingProcessed(boolean beingProcessed)
	{
		this.beingProcessed = beingProcessed;
	}

	public int getState()
	{
		return state;
	}

	public void setState(int state)
	{
		this.state = state;
	}

	public Date getDateTime1()
	{
		return dateTime1;
	}

	public void setDateTime1(Date dateTime1)
	{
		this.dateTime1 = dateTime1;
	}

	public Date getDateTime2()
	{
		return dateTime2;
	}

	public void setDateTime2(Date dateTime2)
	{
		this.dateTime2 = dateTime2;
	}

	public Date getDateTime3()
	{
		return dateTime3;
	}

	public void setDateTime3(Date dateTime3)
	{
		this.dateTime3 = dateTime3;
	}

	public Date getDateTime4()
	{
		return dateTime4;
	}

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
