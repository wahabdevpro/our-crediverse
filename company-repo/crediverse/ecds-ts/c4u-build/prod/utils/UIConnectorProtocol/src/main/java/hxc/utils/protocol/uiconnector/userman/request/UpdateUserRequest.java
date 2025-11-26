package hxc.utils.protocol.uiconnector.userman.request;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;
import hxc.utils.protocol.uiconnector.userman.common.UserDetails;

/**
 * Use to create / update user details If userid exists, call will be an update else create user
 * 
 * @author johne
 * 
 */
public class UpdateUserRequest extends UiBaseRequest
{

	private static final long serialVersionUID = -1027523357321507680L;
	private UserDetails[] userDetails; // User details to update / add

	public UpdateUserRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
		// setRequestCode(UiRequestCode.UPDATE_SPECIFIC_CONFIGURATION);
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

	public void addSingleUserDetail(UserDetails userDetail)
	{
		this.userDetails = new UserDetails[1];
		this.userDetails[0] = userDetail;
	}

}
