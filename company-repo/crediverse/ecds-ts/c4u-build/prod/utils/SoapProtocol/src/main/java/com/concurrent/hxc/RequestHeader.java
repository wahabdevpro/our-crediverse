package com.concurrent.hxc;

import hxc.connectors.Channels;
import hxc.servicebus.HostInfo;
import hxc.servicebus.RequestModes;
import hxc.services.notification.IPhrase;

public class RequestHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String callerID;
	private Channels channel;
	private String hostName;
	private String transactionID;
	private String sessionID;
	private String version;
	private RequestModes mode;
	private Integer languageID;

	public static final String CURRENT_VERSION = "1";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getCallerID()
	{
		return callerID;
	}

	public void setCallerID(String callerID)
	{
		this.callerID = callerID;
	}

	public Channels getChannel()
	{
		return channel;
	}

	public void setChannel(Channels channel)
	{
		this.channel = channel;
	}

	public String getHostName()
	{
		return hostName;
	}

	public void setHostName(String hostName)
	{
		this.hostName = hostName;
	}

	public String getTransactionID()
	{
		return transactionID;
	}

	public void setTransactionID(String transactionID)
	{
		this.transactionID = transactionID;
	}

	public String getSessionID()
	{
		return sessionID;
	}

	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public RequestModes getMode()
	{
		return mode;
	}

	public void setMode(RequestModes mode)
	{
		this.mode = mode;
	}

	public boolean isTestOnly()
	{
		return this.mode == RequestModes.testOnly;
	}

	public boolean isForced()
	{
		return this.mode == RequestModes.force;
	}

	public Integer getLanguageID()
	{
		return languageID;
	}

	public void setLanguageID(Integer languageID)
	{
		this.languageID = languageID;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	/**
	 * Default Constructor
	 */
	public RequestHeader()
	{
		this.hostName = HostInfo.getNameOrElseHxC();
		this.version = CURRENT_VERSION;
		this.mode = RequestModes.normal;
	}

	/**
	 * Copy Constructor
	 */
	public RequestHeader(RequestHeader request)
	{
		this.callerID = request.callerID;
		this.channel = request.channel;
		this.hostName = request.hostName;
		this.transactionID = request.transactionID;
		this.sessionID = request.sessionID;
		this.version = request.version;
		this.mode = request.mode;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	protected static String validate(RequestHeader request)
	{
		if (request == null)
			return "Null Request";

		if (request.callerID == null || request.callerID.length() == 0)
			return "No CallerID";

		if (request.hostName == null || request.hostName.length() == 0)
			return "No HostName";

		if (request.transactionID == null || request.transactionID.length() == 0)
			return "No TransactionID";

		if (request.sessionID == null || request.sessionID.length() == 0)
			return "No SessionID";

		if (request.channel == null)
			return "No Channel";

		if (request.mode == null)
			return "No Mode";

		if (request.languageID != null)
		{
			if (request.languageID < 1 || request.languageID > IPhrase.MAX_LANGUAGES)
				return "Bad Language ID";
		}

		if (!CURRENT_VERSION.equals(request.version))
			return "Unsupported Version";

		return null;
	}
}
