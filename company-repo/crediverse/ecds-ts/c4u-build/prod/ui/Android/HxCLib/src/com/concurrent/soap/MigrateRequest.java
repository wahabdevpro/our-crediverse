package com.concurrent.soap;

import com.concurrent.util.ICallable;
import com.concurrent.util.ISerialiser;

public class MigrateRequest extends RequestHeader implements
		ICallable<MigrateResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private String variantID;
	private String newServiceID;
	private String newVariantID;
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

	public String getNewServiceID()
	{
		return newServiceID;
	}

	public void setNewServiceID(String newServiceID)
	{
		this.newServiceID = newServiceID;
	}

	public String getNewVariantID()
	{
		return newVariantID;
	}

	public void setNewVariantID(String newVariantID)
	{
		this.newVariantID = newVariantID;
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
	public MigrateRequest()
	{

	}

	/**
	 * Copy Constructor
	 */
	public MigrateRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(MigrateRequest request)
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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ICallable Interface
	//
	// /////////////////////////////////

	@Override
	public void serialise(ISerialiser serialiser)
	{
		super.serialise(serialiser);

		serialiser.add("serviceID", serviceID);
		serialiser.add("variantID", variantID);
		serialiser.add("newServiceID", newServiceID);
		serialiser.add("newVariantID", newVariantID);
		serialiser.add("subscriberNumber", subscriberNumber);
	}

	@Override
	public MigrateResponse deserialiseResponse(ISerialiser serialiser)
	{
		MigrateResponse response = new MigrateResponse(this);
		response.deserialise(serialiser);
		return response;
	}

	@Override
	public String getMethodID()
	{
		return "migrate";
	}
}
