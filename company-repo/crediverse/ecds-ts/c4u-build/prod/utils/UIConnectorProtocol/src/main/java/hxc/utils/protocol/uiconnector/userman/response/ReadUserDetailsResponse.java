package hxc.utils.protocol.uiconnector.userman.response;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.protocol.uiconnector.userman.common.UserDetails;

public class ReadUserDetailsResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -5591972501137709939L;
	private UserDetails[] userDetails = null;

	public ReadUserDetailsResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	/**
	 * @return the userDetails
	 */
	public UserDetails[] getUserDetails()
	{
		return userDetails;
	}

	/**
	 * @param userDetails
	 *            the userDetails to set
	 */
	public void setUserDetails(UserDetails[] userDetails)
	{
		this.userDetails = userDetails;
	}

}
