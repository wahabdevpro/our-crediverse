package hxc.utils.protocol.uiconnector.userman.request;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class ReadSecurityRoleRequest extends UiBaseRequest
{
	private static final long serialVersionUID = -8981794870787586518L;
	private int roleId = -1;

	public ReadSecurityRoleRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
		// setRequestCode(UiRequestCode.UPDATE_SPECIFIC_CONFIGURATION);
	}

	/**
	 * @return the roleId
	 */
	public int getRoleId()
	{
		return roleId;
	}

	/**
	 * @param roleId
	 *            the roleId to set
	 */
	public void setRoleId(int roleId)
	{
		this.roleId = roleId;
	}

}
