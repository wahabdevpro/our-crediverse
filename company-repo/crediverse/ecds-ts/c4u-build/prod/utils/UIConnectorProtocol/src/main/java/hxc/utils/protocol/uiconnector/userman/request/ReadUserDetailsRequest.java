package hxc.utils.protocol.uiconnector.userman.request;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class ReadUserDetailsRequest extends UiBaseRequest
{

	private static final long serialVersionUID = -673097391242803196L;

	private String userToReadId = null; // Leave Null to display all details

	public ReadUserDetailsRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
		// setRequestCode(UiRequestCode.UPDATE_SPECIFIC_CONFIGURATION);
	}

	/**
	 * @return the userToReadId
	 */
	public String getUserToReadId()
	{
		return userToReadId;
	}

	/**
	 * @param userToReadId
	 *            the userToReadId to set
	 */
	public void setUserToReadId(String userToReadId)
	{
		this.userToReadId = userToReadId;
	}

}
