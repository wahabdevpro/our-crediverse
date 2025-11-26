package hxc.utils.protocol.uiconnector.userman.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserDetails implements Serializable
{

	private static final long serialVersionUID = -339827244914278940L;
	private boolean isNewUser = false;
	private String userId;
	private String name;
	private String mobileNumber;
	private boolean enabled = true;
	private List<Integer> roleIds = null;

	public UserDetails()
	{
	}

	public UserDetails(String userId, String name, String mobileNumber)
	{
		this.userId = userId;
		this.name = name;
		this.mobileNumber = mobileNumber;
	}

	/**
	 * @return the userId
	 */
	public String getUserId()
	{
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the mobileNumber
	 */
	public String getMobileNumber()
	{
		return mobileNumber;
	}

	/**
	 * @param mobileNumber
	 *            the mobileNumber to set
	 */
	public void setMobileNumber(String mobileNumber)
	{
		this.mobileNumber = mobileNumber;
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * @param enabled
	 *            the enabled to set
	 */
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public void addSecurityRole(int roleId)
	{
		if (roleIds == null)
		{
			roleIds = new ArrayList<>();
		}
		roleIds.add(roleId);
	}

	public List<Integer> getRoleIds()
	{
		return roleIds;
	}

	public void setRoleIds(List<Integer> roleIds)
	{
		this.roleIds = roleIds;
	}

	/**
	 * @return the isNewUser
	 */
	public boolean isNewUser()
	{
		return isNewUser;
	}

	/**
	 * @param isNewUser
	 *            the isNewUser to set
	 */
	public void setNewUser(boolean isNewUser)
	{
		this.isNewUser = isNewUser;
	}

}
