package com.concurrent.soap;

import com.concurrent.util.ICallable;
import com.concurrent.util.ISerialiser;

public class GetCreditTransfersRequest extends RequestHeader implements ICallable<GetCreditTransfersResponse>
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
	private String transferMode;
	private boolean activeOnly;
	
	private static final long serialVersionUID = 3868712161670623557L;
	
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

	public void setSubscriberNumber(Number donorNumber)
	{
		this.subscriberNumber = donorNumber;
	}

	public Number getMemberNumber()
	{
		return memberNumber;
	}

	public void setMemberNumber(Number recipientNumber)
	{
		this.memberNumber = recipientNumber;
	}

	public String getTransferMode()
	{
		return transferMode;
	}

	public void setTransferMode(String transferMode)
	{
		this.transferMode = transferMode;
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

	// Default Constructor
	public GetCreditTransfersRequest()
	{

	}

	// Copy Constructor
	public GetCreditTransfersRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(GetCreditTransfersRequest request)
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
		serialiser.add("subscriberNumber", subscriberNumber);
		serialiser.add("memberNumber", memberNumber);
		serialiser.add("transferMode", transferMode);
		serialiser.add("activeOnly", activeOnly);
	}

	@Override
	public GetCreditTransfersResponse deserialiseResponse(ISerialiser serialiser)
	{
		GetCreditTransfersResponse result = new GetCreditTransfersResponse(this);
		result.deserialise(serialiser);
		return result;
	}

	@Override
	public String getMethodID()
	{
		return "getCreditTransfers";
	}

}
