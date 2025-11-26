package com.concurrent.soap;

import com.concurrent.util.ICallable;
import com.concurrent.util.ISerialiser;

public class UnsubscribeRequest extends RequestHeader implements ICallable<UnsubscribeResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private String variantID;
	private Number subscriberNumber;

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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	/**
	 * Default Constructor
	 */
	public UnsubscribeRequest()
	{

	}

	/**
	 * Copy Constructor
	 */
	public UnsubscribeRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(UnsubscribeRequest request)
	{

		// Validate Header
		String problem = RequestHeader.validate(request);
		if (problem != null)
			return problem;

		if (request.serviceID == null || request.serviceID.length() == 0)
			return "No ServiceID";

		if (request.variantID == null || request.variantID.length() == 0)
			return "No VariantID";

		problem = Number.validate(request.subscriberNumber);
		if (problem != null)
			return problem;

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
	}

	@Override
	public UnsubscribeResponse deserialiseResponse(ISerialiser serialiser)
	{
		UnsubscribeResponse response = new UnsubscribeResponse(this);
		response.deserialise(serialiser);
		return response;
	}

	@Override
	public String getMethodID()
	{
		return "unsubscribe";
	}
}
