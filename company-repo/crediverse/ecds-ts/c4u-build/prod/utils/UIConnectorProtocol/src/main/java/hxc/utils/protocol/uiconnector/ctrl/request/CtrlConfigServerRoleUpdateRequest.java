package hxc.utils.protocol.uiconnector.ctrl.request;

import hxc.utils.protocol.uiconnector.ctrl.response.ServerRole;
import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class CtrlConfigServerRoleUpdateRequest extends UiBaseRequest
{
	private static final long serialVersionUID = -8852460515668504519L;
	private ServerRole[] serverRoleList;
	private boolean persistToDatabase = true;
	private int versionNumber;

	public CtrlConfigServerRoleUpdateRequest()
	{
	}

	public CtrlConfigServerRoleUpdateRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	/**
	 * @return the serverRoleList
	 */
	public ServerRole[] getServerRoleList()
	{
		return serverRoleList;
	}

	/**
	 * @param serverRoleList
	 *            the serverRoleList to set
	 */
	public void setServerRoleList(ServerRole[] serverRoleList)
	{
		this.serverRoleList = serverRoleList;
	}

	/**
	 * @return the persistToDatabase
	 */
	public boolean isPersistToDatabase()
	{
		return persistToDatabase;
	}

	/**
	 * @param persistToDatabase
	 *            the persistToDatabase to set
	 */
	public void setPersistToDatabase(boolean persistToDatabase)
	{
		this.persistToDatabase = persistToDatabase;
	}

	/**
	 * @return the versionNumber
	 */
	public int getVersionNumber()
	{
		return versionNumber;
	}

	/**
	 * @param versionNumber
	 *            the versionNumber to set
	 */
	public void setVersionNumber(int versionNumber)
	{
		this.versionNumber = versionNumber;
	}

}
