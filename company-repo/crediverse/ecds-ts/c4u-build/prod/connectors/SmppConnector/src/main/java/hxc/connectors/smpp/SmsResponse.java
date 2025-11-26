package hxc.connectors.smpp;

import hxc.connectors.sms.ISmsResponse;

public class SmsResponse implements ISmsResponse
{

	public static final String WARN = "WARN";
	public static final String ERROR = "ERROR";
	public static final String ASYNCHRONOUS = "ASYNC";

	private String messageID;
	private String resultMessage;
	private int sequenceNumber;

	public SmsResponse()
	{
	}

	public SmsResponse(String messageID, String resultMessage, int sequenceNumber)
	{
		this.messageID = messageID;
		this.resultMessage = resultMessage;
		this.sequenceNumber = sequenceNumber;
	}

	@Override
	public void setResultMessage(String resultMessage)
	{
		this.resultMessage = resultMessage;
	}

	@Override
	public String getResultMessage()
	{
		return resultMessage;
	}

	@Override
	public void setMessageID(String messageID)
	{
		this.messageID = messageID;
	}

	@Override
	public String getMessageID()
	{
		return messageID;
	}

	@Override
	public void setSequenceNumber(int sequenceNumber)
	{
		this.sequenceNumber = sequenceNumber;
	}

	@Override
	public int getSequenceNumber()
	{
		return sequenceNumber;
	}

}
