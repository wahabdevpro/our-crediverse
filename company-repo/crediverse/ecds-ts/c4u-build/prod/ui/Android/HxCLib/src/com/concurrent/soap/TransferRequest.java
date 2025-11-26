package com.concurrent.soap;

import com.concurrent.util.ICallable;
import com.concurrent.util.ISerialiser;

public class TransferRequest extends RequestHeader implements ICallable<TransferResponse>
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private String variantID;
	private Number subscriberNumber;
	private Number recipientNumber;
	private long amount;
	private String pin;
	private String transferModeID;
	
	private static final long serialVersionUID = -6401528676872929363L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getServiceID()
	{
		return serviceID;
	}

	public String getVariantID()
	{
		return variantID;
	}

	public void setServiceID(String serviceID)
	{
		this.serviceID = serviceID;
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

	public Number getRecipientNumber()
	{
		return recipientNumber;
	}

	public void setRecipientNumber(Number recipientNumber)
	{
		this.recipientNumber = recipientNumber;
	}

	public long getAmount()
	{
		return amount;
	}

	public void setAmount(long amount)
	{
		this.amount = amount;
	}

	public String getPin()
	{
		return pin;
	}

	public void setPin(String pin)
	{
		this.pin = pin;
	}

	public String getTransferModeID()
	{
		return transferModeID;
	}

	public void setTransferModeID(String transferModeID)
	{
		this.transferModeID = transferModeID;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	/**
	 * Default Constructor
	 */
	public TransferRequest()
	{

	}

	/**
	 * Copy Constructor
	 */
	public TransferRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(TransferRequest request)
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

		problem = Number.validate(request.recipientNumber);
		if (problem != null)
			return problem;

		if (request.amount <= 0)
			return "Invalid Amount";

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
		serialiser.add("recipientNumber", recipientNumber);
		serialiser.add("amount", amount);
		serialiser.add("pin", pin);
		serialiser.add("transferModeID", transferModeID);

	}

	@Override
	public TransferResponse deserialiseResponse(ISerialiser serialiser)
	{
		TransferResponse response = new TransferResponse(this);
		response.deserialise(serialiser);
		return response;
	}

	@Override
	public String getMethodID()
	{
		return "transfer";
	}

}
