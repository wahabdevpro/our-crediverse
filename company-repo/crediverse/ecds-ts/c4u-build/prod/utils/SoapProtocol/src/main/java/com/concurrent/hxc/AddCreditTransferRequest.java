package com.concurrent.hxc;

import java.util.Date;

public class AddCreditTransferRequest extends RequestHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private Number subscriberNumber;
	private Number memberNumber;
	private String transferMode;
	private long amount;
	private Date nextTransferDate;
	private Long transferLimit;
	private Long transferThreshold;
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

	public String getTransferMode()
	{
		return transferMode;
	}

	public void setTransferMode(String transferMode)
	{
		this.transferMode = transferMode;
	}

	public long getAmount()
	{
		return amount;
	}

	public void setAmount(long amount)
	{
		this.amount = amount;
	}

	public Date getNextTransferDate()
	{
		return nextTransferDate;
	}

	public void setNextTransferDate(Date nextTransferDate)
	{
		this.nextTransferDate = nextTransferDate;
	}

	public Long getTransferLimit()
	{
		return transferLimit;
	}

	public void setTransferLimit(Long transferLimit)
	{
		this.transferLimit = transferLimit;
	}

	public Long getTransferThreshold()
	{
		return transferThreshold;
	}

	public void setTransferThreshold(Long transferThreshold)
	{
		this.transferThreshold = transferThreshold;
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
	public AddCreditTransferRequest()
	{

	}

	// Copy Constructor
	public AddCreditTransferRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(AddCreditTransferRequest request)
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

}
