package hxc.utils.protocol.uiconnector.userman.response;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.protocol.uiconnector.userman.common.SecurityRole;

public class ReadSecurityRolesResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -8355471991852394829L;
	private SecurityRole[] securityRole;

	public ReadSecurityRolesResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid()
	{
		return serialVersionUID;
	}

	/**
	 * @return the securityRole
	 */
	public SecurityRole[] getSecurityRole()
	{
		return securityRole;
	}

	/**
	 * @param securityRole
	 *            the securityRole to set
	 */
	public void setSecurityRole(SecurityRole[] securityRole)
	{
		this.securityRole = securityRole;
	}

}
