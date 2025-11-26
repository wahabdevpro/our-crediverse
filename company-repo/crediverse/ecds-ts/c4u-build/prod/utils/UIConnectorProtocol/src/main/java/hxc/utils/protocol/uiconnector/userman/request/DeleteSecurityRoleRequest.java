package hxc.utils.protocol.uiconnector.userman.request;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class DeleteSecurityRoleRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 59266365858218387L;
	private int[] rolesIdToDelete;

	public DeleteSecurityRoleRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
		// setRequestCode(UiRequestCode.UPDATE_SPECIFIC_CONFIGURATION);
	}

	/**
	 * @return the rolesIdToDelete
	 */
	public int[] getRolesIdToDelete()
	{
		return rolesIdToDelete;
	}

	/**
	 * @param rolesIdToDelete
	 *            the rolesIdToDelete to set
	 */
	public void setRolesIdToDelete(int[] rolesIdToDelete)
	{
		this.rolesIdToDelete = rolesIdToDelete;
	}

	public void assSingleRoleToDelete(int roleId)
	{
		this.rolesIdToDelete = new int[1];
		this.rolesIdToDelete[0] = roleId;
	}
}
