package hxc.utils.protocol.uiconnector.ctrl.response;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class CtrlConfigServerRoleResponse extends UiBaseResponse
{

	private static final long serialVersionUID = 7960388499319154625L;
	private ServerRole[] serverRoleList;
	private int versionNumber = 0;

	public CtrlConfigServerRoleResponse()
	{
	}

	public CtrlConfigServerRoleResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public ServerRole[] getServerRoleList()
	{
		return serverRoleList;
	}

	public void setServerRoleList(ServerRole[] serverRoleList)
	{
		this.serverRoleList = serverRoleList;
	}

	public int getVersionNumber()
	{
		return versionNumber;
	}

	public void setVersionNumber(int versionNumber)
	{
		this.versionNumber = versionNumber;
	}

}
