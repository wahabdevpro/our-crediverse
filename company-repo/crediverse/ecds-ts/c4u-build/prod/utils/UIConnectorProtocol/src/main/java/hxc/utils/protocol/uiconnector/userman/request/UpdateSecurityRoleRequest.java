package hxc.utils.protocol.uiconnector.userman.request;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;
import hxc.utils.protocol.uiconnector.userman.common.SecurityRole;

public class UpdateSecurityRoleRequest extends UiBaseRequest
{

	private static final long serialVersionUID = -2546804595250045831L;
	private SecurityRole[] securityRoles;

	public UpdateSecurityRoleRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
		// setRequestCode(UiRequestCode.UPDATE_SPECIFIC_CONFIGURATION);
	}

	/**
	 * @return the securityRoles
	 */
	public SecurityRole[] getSecurityRoles()
	{
		return securityRoles;
	}

	/**
	 * @param securityRoles
	 *            the securityRoles to set
	 */
	public void setSecurityRoles(SecurityRole[] securityRoles)
	{
		this.securityRoles = securityRoles;
	}

	public void addSingleSecurityRole(SecurityRole role)
	{
		this.securityRoles = new SecurityRole[1];
		this.securityRoles[0] = role;
	}
}
