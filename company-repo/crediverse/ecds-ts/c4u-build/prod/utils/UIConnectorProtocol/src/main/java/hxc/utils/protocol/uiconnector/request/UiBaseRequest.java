package hxc.utils.protocol.uiconnector.request;

import java.io.Serializable;

public abstract class UiBaseRequest implements Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = 8278760415090020002L;

	public enum UiRequestCode
	{
		NONE, // No idea yet
		PUBLIC_KEY, AUTHENTICATE, GET_ALL_CONFIGURATION, GET_SPECIFIC_CONFIGURATION, UPDATE_SPECIFIC_CONFIGURATION, USER_MANAGEMENT, CALL_METHOD_REQUEST
	}

	public UiBaseRequest()
	{
	}

	public UiBaseRequest(String userId)
	{
		this.userId = userId;
	}

	public UiBaseRequest(String userId, String sessionId)
	{
		this.userId = userId;
		this.sessionId = sessionId;
	}

	private UiRequestCode requestCode;
	private String userId;
	private String sessionId;

	/**
	 * @return the requestCode
	 */
	public UiRequestCode getRequestCode()
	{
		return requestCode;
	}

	/**
	 * @param requestCode
	 *            the requestCode to set
	 */
	public void setRequestCode(UiRequestCode requestCode)
	{
		this.requestCode = requestCode;
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
