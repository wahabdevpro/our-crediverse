package hxc.utils.protocol.uiconnector.userman.request;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class ReadSecurityPermissionRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 5262551796623048160L;
	private int roleId = -1;

	public ReadSecurityPermissionRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
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

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid()
	{
		return serialVersionUID;
	}

}
