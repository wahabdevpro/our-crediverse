package hxc.utils.protocol.uiconnector.response;

import java.io.Serializable;

public abstract class UiBaseResponse implements Serializable
{
	private static final long serialVersionUID = 7046654775297943685L;

	public enum UiResponseCode
	{
		ERROR, PUBLIC_KEY, AUTHENTICATE, GET_CONFIGURABLES, GET_SPECIFIC_CONFIGURATION, UPDATE_SPECIFIC_CONFIGURATION, CONFIRMATION, METHOD_RESPONSE
	}

	private UiResponseCode responseCode;
	private String userId;
	private String sessionId;

	public UiBaseResponse()
	{
	}

	public UiBaseResponse(String userId)
	{
		super();
		this.userId = userId;
	}

	public UiBaseResponse(String userId, String sessionId)
	{
		super();
		this.userId = userId;
		this.sessionId = sessionId;
	}

	/**
	 * @return the responseCode
	 */
	public UiResponseCode getResponseCode()
	{
		return responseCode;
	}

	/**
	 * @param responseCode
	 *            the responseCode to set
	 */
	public void setResponseCode(UiResponseCode responseCode)
	{
		this.responseCode = responseCode;
	}

	/**
	 * @return the userId
	 */
	public String getUserId()
	{
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId()
	{
		return sessionId;
	}

	/**
	 * @param sessionId
	 *            the sessionId to set
	 */
	public void setSessionId(String sessionId)
	{
		this.sessionId = sessionId;
	}

}
