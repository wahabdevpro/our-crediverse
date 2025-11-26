package com.concurrent.soap;

import com.concurrent.util.ICallable;
import com.concurrent.util.ISerialiser;

public class RemoveCreditTransfersRequest extends RequestHeader implements ICallable<RemoveCreditTransfersResponse>
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
	public RemoveCreditTransfersRequest()
	{

	}

	// Copy Constructor
	public RemoveCreditTransfersRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(RemoveCreditTransfersRequest request)
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
	// ICallable
	//
	// /////////////////////////////////
	@Override
	public void serialise(ISerialiser serialiser)
	{
		super.serialise(serialiser);
		serialiser.add("serviceID", serviceID);
		serialiser.add("variantID", variantID);
		serialiser.add("transferMode", transferMode);
		serialiser.add("subscriberNumber", subscriberNumber);
		serialiser.add("memberNumber", memberNumber);
	}

	@Override
	public RemoveCreditTransfersResponse deserialiseResponse(ISerialiser serialiser)
	{
		RemoveCreditTransfersResponse response = new RemoveCreditTransfersResponse(this);
		response.deserialise(serialiser);
		return response;
	}

	@Override
	public String getMethodID()
	{
		return "removeCreditTransfers";
	}

}