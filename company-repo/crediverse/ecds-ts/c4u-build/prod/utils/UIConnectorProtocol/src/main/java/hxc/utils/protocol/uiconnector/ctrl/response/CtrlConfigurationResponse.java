package hxc.utils.protocol.uiconnector.ctrl.response;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class CtrlConfigurationResponse extends UiBaseResponse
{

	private static final long serialVersionUID = 2216232063255011232L;
	private ServerInfo[] serverList;
	private ServerRole[] serverRoleList;
	private int versionNumber = 0;

	public CtrlConfigurationResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	/**
	 * @return the serverList
	 */
	public ServerInfo[] getServerList()
	{
		return serverList;
	}

	/**
	 * @param serverList
	 *            the serverList to set
	 */
	public void setServerList(ServerInfo[] serverList)
	{
		this.serverList = serverList;
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
