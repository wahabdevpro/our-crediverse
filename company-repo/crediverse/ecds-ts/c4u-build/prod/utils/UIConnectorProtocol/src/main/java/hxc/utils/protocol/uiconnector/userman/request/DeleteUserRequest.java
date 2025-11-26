package hxc.utils.protocol.uiconnector.userman.request;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class DeleteUserRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 5964143267097410068L;
	private String[] usersToDeleteIds;

	public DeleteUserRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
		// setRequestCode(UiRequestCode.UPDATE_SPECIFIC_CONFIGURATION);
	}

	/**
	 * @return the usersToDeleteIds
	 */
	public String[] getUsersToDeleteIds()
	{
		return usersToDeleteIds;
	}

	/**
	 * @param usersToDeleteIds
	 *            the usersToDeleteIds to set
	 */
	public void setUsersToDeleteIds(String[] usersToDeleteIds)
	{
		this.usersToDeleteIds = usersToDeleteIds;
	}

	public void addSingleUserToDelete(String userToDeleteId)
	{
		this.usersToDeleteIds = new String[1];
		this.usersToDeleteIds[0] = userToDeleteId;
	}

}
