package com.concurrent.soap;

import com.concurrent.util.ISerialiser;

import hxc.servicebus.ReturnCodes;

public abstract class ResponseHeader 
{
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
		return wasSuccess(returnCode);
	}
	
	public static boolean wasSuccess(ReturnCodes returnCode)
	{
		return returnCode == ReturnCodes.success
				|| returnCode == ReturnCodes.successfulTest;
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

	public ResponseHeader()
	{

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	protected void deserialise(ISerialiser serialiser)
	{
		transactionId = serialiser.getString("transactionId", null);
		sessionId = serialiser.getString("sessionId", null);
		returnCode = serialiser.getEnum("returnCode", ReturnCodes.class,
				ReturnCodes.technicalProblem);
		message = serialiser.getString("message", null);
		chargeLevied = serialiser.getLong("chargeLevied", 0L); 

	}

}
