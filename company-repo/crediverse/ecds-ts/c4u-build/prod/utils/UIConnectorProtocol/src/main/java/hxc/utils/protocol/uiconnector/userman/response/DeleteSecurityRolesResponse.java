package hxc.utils.protocol.uiconnector.userman.response;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class DeleteSecurityRolesResponse extends UiBaseResponse
{

	private static final long serialVersionUID = 8545712741553816886L;
	private Integer[] deletedRoleIds;
	private Integer[] notRemovedRoleIds;

	public DeleteSecurityRolesResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	/**
	 * @return the deletedRoleIds
	 */
	public Integer[] getDeletedRoleIds()
	{
		return deletedRoleIds;
	}

	/**
	 * @param deletedRoleIds
	 *            the deletedRoleIds to set
	 */
	public void setDeletedRoleIds(Integer[] deletedRoleIds)
	{
		this.deletedRoleIds = deletedRoleIds;
	}

	/**
	 * @return the notRemovedRoleIds
	 */
	public Integer[] getNotRemovedRoleIds()
	{
		return notRemovedRoleIds;
	}

	/**
	 * @param notRemovedRoleIds
	 *            the notRemovedRoleIds to set
	 */
	public void setNotRemovedRoleIds(Integer[] notRemovedRoleIds)
	{
		this.notRemovedRoleIds = notRemovedRoleIds;
	}

}
