package com.concurrent.soap;

import com.concurrent.util.ICallable;
import com.concurrent.util.ISerialiser;

public class GetQuotasRequest extends RequestHeader implements ICallable<GetQuotasResponse>
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
	private String quotaID;
	private String service;
	private String destination;
	private String timeOfDay;
	private String daysOfWeek;
	private boolean activeOnly;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getQuotaID()
	{
		return quotaID;
	}

	public void setQuotaID(String quotaID)
	{
		this.quotaID = quotaID;
	}

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

	public String getService()
	{
		return service;
	}

	public void setService(String service)
	{
		this.service = service;
	}

	public String getDestination()
	{
		return destination;
	}

	public void setDestination(String destination)
	{
		this.destination = destination;
	}

	public String getTimeOfDay()
	{
		return timeOfDay;
	}

	public void setTimeOfDay(String timeOfDay)
	{
		this.timeOfDay = timeOfDay;
	}

	public String getDaysOfWeek()
	{
		return daysOfWeek;
	}

	public void setDaysOfWeek(String daysOfWeek)
	{
		this.daysOfWeek = daysOfWeek;
	}

	public boolean isActiveOnly()
	{
		return activeOnly;
	}

	public void setActiveOnly(boolean activeOnly)
	{
		this.activeOnly = activeOnly;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	/**
	 * Default Constructor
	 */
	public GetQuotasRequest()
	{

	}

	/**
	 * Copy Constructor
	 */
	public GetQuotasRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(GetQuotasRequest request)
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

		if (request.activeOnly)
		{
			problem = Number.validate(request.memberNumber);
			if (problem != null)
				return problem;
		}

		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ICallable
	//
	// /////////////////////////////////

	@Override
	public void serialise(ISerialiser serialiser)
	{
		super.serialise(serialiser);
		serialiser.add("serviceID", serviceID);
		serialiser.add("variantID", variantID);
		serialiser.add("subscriberNumber", subscriberNumber);
		serialiser.add("memberNumber", memberNumber);
		serialiser.add("quotaID", quotaID);
		serialiser.add("service", service);
		serialiser.add("destination", destination);
		serialiser.add("timeOfDay", timeOfDay);
		serialiser.add("daysOfWeek", daysOfWeek);
		serialiser.add("activeOnly", activeOnly);
	}

	@Override
	public GetQuotasResponse deserialiseResponse(ISerialiser serialiser)
	{
		GetQuotasResponse response = new GetQuotasResponse(this);
		response.deserialise(serialiser);
		return response;
	}

	@Override
	public String getMethodID()
	{
		return "getQuotas";
	}

}
