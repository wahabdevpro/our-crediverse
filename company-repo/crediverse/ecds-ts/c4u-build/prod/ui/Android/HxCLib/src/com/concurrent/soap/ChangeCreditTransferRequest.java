package com.concurrent.soap;

import com.concurrent.util.ICallable;
import com.concurrent.util.ISerialiser;

public class ChangeCreditTransferRequest extends RequestHeader 
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private Number donorNumber;
	private Number recipientNumber;
	private String pin;

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

	public Number getDonorNumber()
	{
		return donorNumber;
	}

	public void setDonorNumber(Number donorNumber)
	{
		this.donorNumber = donorNumber;
	}

	public Number getRecipientNumber()
	{
		return recipientNumber;
	}

	public void setRecipientNumber(Number recipientNumber)
	{
		this.recipientNumber = recipientNumber;
	}

	public String getPin()
	{
		return pin;
	}

	public void setPin(String pin)
	{
		this.pin = pin;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	// Default Constructor
	public ChangeCreditTransferRequest()
	{

	}

	// Copy Constructor
	public ChangeCreditTransferRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(ChangeCreditTransferRequest request)
	{
		// Validate Header
		String problem = RequestHeader.validate(request);
		if (problem != null)
			return problem;

		if (request.serviceID == null || request.serviceID.length() == 0)
			return "No ServiceID";

		problem = Number.validate(request.donorNumber);
		if (problem != null)
			return problem;

		return null;
	}
	


}