package com.concurrent.soap;

public class SendSMSRequest extends RequestHeader
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Number subscriberNumber;
	private String sourceAddress;
	private String message;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public Number getSubscriberNumber()
	{
		return subscriberNumber;
	}

	public void setSubscriberNumber(Number subscriberNumber)
	{
		this.subscriberNumber = subscriberNumber;
	}

	public String getSourceAddress()
	{
		return sourceAddress;
	}

	public void setSourceAddress(String source)
	{
		this.sourceAddress = source;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	/**
	 * Default Constructor
	 */
	public SendSMSRequest()
	{

	}

	/**
	 * Copy Constructor
	 */
	public SendSMSRequest(RequestHeader request)
	{
		super(request);
	}

}
