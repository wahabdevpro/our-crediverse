package hxc.utils.protocol.uiconnector.userman.response;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class DeleteUserResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -2217941856856093445L;
	private String[] deletedUserIds;
	private String[] notRemovedUserIds;

	public DeleteUserResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
		// setRequestCode(UiRequestCode.UPDATE_SPECIFIC_CONFIGURATION);
	}

	/**
	 * @return the deletedUserIds
	 */
	public String[] getDeletedUserIds()
	{
		return deletedUserIds;
	}

	/**
	 * @param deletedUserIds
	 *            the deletedUserIds to set
	 */
	public void setDeletedUserIds(String[] deletedUserIds)
	{
		this.deletedUserIds = deletedUserIds;
	}

	/**
	 * @return the notRemovedUserIds
	 */
	public String[] getNotRemovedUserIds()
	{
		return notRemovedUserIds;
	}

	/**
	 * @param notRemovedUserIds
	 *            the notRemovedUserIds to set
	 */
	public void setNotRemovedUserIds(String[] notRemovedUserIds)
	{
		this.notRemovedUserIds = notRemovedUserIds;
	}

}
