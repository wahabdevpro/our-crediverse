package com.concurrent.hxc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.servicebus.ReturnCodes;
import hxc.services.transactions.ICdr;

public class ResponseHeader
{
	final static Logger logger = LoggerFactory.getLogger(ResponseHeader.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String transactionId;
	private String sessionId;
	private ReturnCodes returnCode;
	private String message;
	private long chargeLevied = 0;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getTransactionId()
	{
		return transactionId;
	}

	public void setTransactionId(String transactionId)
	{
		this.transactionId = transactionId;
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public void setSessionId(String sessionId)
	{
		this.sessionId = sessionId;
	}

	public ReturnCodes getReturnCode()
	{
		return returnCode;
	}

	public void setReturnCode(ReturnCodes returnCode)
	{
		this.returnCode = returnCode;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public boolean wasSuccess()
	{
		return returnCode == ReturnCodes.success || returnCode == ReturnCodes.successfulTest;
	}

	public long getChargeLevied()
	{
		return chargeLevied;
	}

	public void setChargeLevied(long chargeLevied)
	{
		this.chargeLevied = chargeLevied;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	/**
	 * Constructor from Request
	 */
	public ResponseHeader(RequestHeader request)
	{
		this.transactionId = request.getTransactionID();
		this.sessionId = request.getSessionID();
		this.returnCode = ReturnCodes.notSupported;
		this.message = null;
	}

	public ResponseHeader(String transactionId, String sessionID)
	{
		this.transactionId = transactionId;
		this.sessionId = sessionID;
		this.returnCode = ReturnCodes.notSupported;
		this.message = null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@SuppressWarnings("unchecked")
	public <T extends ResponseHeader> T exitWith(ICdr cdr, ReturnCodes returnCode, String message, Object... args)
	{
		this.returnCode = returnCode;
		if (message == null)
			message = "Error";
		this.message = String.format(message, args);
		if (returnCode == ReturnCodes.success || returnCode == ReturnCodes.successfulTest)
			logger.trace(this.message, args);
		else
			logger.error(this.message, args);

		if (cdr != null)
		{
			cdr.setReturnCode(returnCode);
			if (cdr.getAdditionalInformation() == null)
				cdr.setAdditionalInformation(this.message);
		}

		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public <T extends ResponseHeader> T exitWith(ICdr cdr, ReturnCodes returnCode, String message)
	{
		this.returnCode = returnCode;
		if (message == null)
			message = "Error";
		this.message = message;
		if (returnCode == ReturnCodes.success || returnCode == ReturnCodes.successfulTest)
			logger.debug(this.message.replaceAll("%", "%%"));
		else
			logger.info(this.message.replaceAll("%", "%%"));

		if (cdr != null)
		{
			cdr.setReturnCode(returnCode);
			if (cdr.getAdditionalInformation() == null)
				cdr.setAdditionalInformation(this.message);
		}

		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public <T extends ResponseHeader> T exitWith(ICdr cdr, ReturnCodes returnCode, Throwable e)
	{
		this.returnCode = returnCode;
		message = e.getMessage();
		if (message == null || message.length() == 0)
			message = e.toString();

		logger.error(message);

		if (cdr != null)
		{
			cdr.setReturnCode(returnCode);
			if (cdr.getAdditionalInformation() == null)
				cdr.setAdditionalInformation(this.message);
			else
				cdr.setAdditionalInformation(cdr.getAdditionalInformation() + " " + message);
		}

		return (T) this;

	}

}
