package hxc.utils.protocol.uiconnector.ctrl.request;

import hxc.utils.protocol.uiconnector.ctrl.response.ServerInfo;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerRole;
import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class CtrlConfigurationUpdateRequest extends UiBaseRequest
{

	private static final long serialVersionUID = -702448093713278403L;
	private ServerInfo[] serverInfoList;
	private ServerRole[] serverRoleList;
	private boolean persistToDatabase;
	private int versionNumber;

	public CtrlConfigurationUpdateRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	/**
	 * @return the serverInfoList
	 */
	public ServerInfo[] getServerInfoList()
	{
		return serverInfoList;
	}

	/**
	 * @param serverInfoList
	 *            the serverInfoList to set
	 */
	public void setServerInfoList(ServerInfo[] serverInfoList)
	{
		this.serverInfoList = serverInfoList;
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
