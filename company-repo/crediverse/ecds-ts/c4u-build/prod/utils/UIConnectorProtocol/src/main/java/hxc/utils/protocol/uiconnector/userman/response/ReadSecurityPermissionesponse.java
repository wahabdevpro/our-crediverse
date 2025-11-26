package hxc.utils.protocol.uiconnector.userman.response;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.protocol.uiconnector.userman.common.SecurityPermissionInfo;

public class ReadSecurityPermissionesponse extends UiBaseResponse
{

	private static final long serialVersionUID = 2427839694987352440L;
	private SecurityPermissionInfo[] securityPermissionInfo;

	public ReadSecurityPermissionesponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	/**
	 * @return the securityPermissionInfo
	 */
	public SecurityPermissionInfo[] getSecurityPermissionInfo()
	{
		return securityPermissionInfo;
	}

	/**
	 * @param securityPermissionInfo
	 *            the securityPermissionInfo to set
	 */
	public void setSecurityPermissionInfo(SecurityPermissionInfo[] securityPermissionInfo)
	{
		this.securityPermissionInfo = securityPermissionInfo;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid()
	{
		return serialVersionUID;
	}

}
