package com.concurrent.hxc;

import java.util.Date;

public class SendSMSResponse extends ResponseHeader
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Number subscriberNumber;
	private String sourceAddress;
	private String message;
	private Date scheduleTime;
	private Date expiryTime;

	private String messageID;
	private String resultMessage;
	private Integer sequenceNumber;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public void setSourceAddress(String sourceAddress)
	{
		this.sourceAddress = sourceAddress;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	/**
	 * Constructor from Request
	 */
	public SendSMSResponse(SendSMSRequest request)
	{
		super(request);
	}

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

	public void setSource(String source)
	{
		this.sourceAddress = source;
	}

	@Override
	public String getMessage()
	{
		return message;
	}

	@Override
	public void setMessage(String message)
	{
		this.message = message;
	}

	public Date getScheduleTime()
	{
		return scheduleTime;
	}

	public void setScheduleTime(Date scheduleTime)
	{
		this.scheduleTime = scheduleTime;
	}

	public Date getExpiryTime()
	{
		return expiryTime;
	}

	public void setExpiryTime(Date expiryTime)
	{
		this.expiryTime = expiryTime;
	}

	public String getMessageID()
	{
		return messageID;
	}

	public void setMessageID(String messageID)
	{
		this.messageID = messageID;
	}

	public String getResultMessage()
	{
		return resultMessage;
	}

	public void setResultMessage(String resultMessage)
	{
		this.resultMessage = resultMessage;
	}

	public Integer getSequenceNumber()
	{
		return sequenceNumber;
	}

	public void setSequenceNumber(Integer sequenceNumber)
	{
		this.sequenceNumber = sequenceNumber;
	}

}
