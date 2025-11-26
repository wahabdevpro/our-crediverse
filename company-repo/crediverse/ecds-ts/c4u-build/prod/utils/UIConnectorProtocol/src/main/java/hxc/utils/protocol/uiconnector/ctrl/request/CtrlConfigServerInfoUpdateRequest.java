package hxc.utils.protocol.uiconnector.ctrl.request;

import hxc.utils.protocol.uiconnector.ctrl.response.ServerInfo;
import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class CtrlConfigServerInfoUpdateRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 1291831263943302692L;
	private ServerInfo[] serverInfoList;
	private boolean persistToDatabase;
	private int versionNumber;

	public CtrlConfigServerInfoUpdateRequest()
	{
	}

	public CtrlConfigServerInfoUpdateRequest(String userId, String sessionId)
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
