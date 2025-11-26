package hxc.utils.protocol.uiconnector.userman.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import hxc.services.security.IPermission;

public class SecurityRole implements Serializable
{

	private static final long serialVersionUID = -2039613588556171767L;
	private int roleId = -1;
	private String name;
	private String description;
	private List<String> permissionIds = null; // Please Note: This is the full permission (with path)
	private boolean assignable = false;

	public SecurityRole()
	{
	}

	public SecurityRole(int roleId, String name, String description)
	{
		this.roleId = roleId;
		this.name = name;
		this.description = description;
		this.permissionIds = new ArrayList<>();
	}

	/**
	 * Used when creating new role
	 * 
	 * @param name
	 * @param description
	 */
	public SecurityRole(String name, String description)
	{
		this.name = name;
		this.description = description;
	}

	/**
	 * @return the roleId
	 */
	public int getRoleId()
	{
		return roleId;
	}

	/**
	 * @param roleId
	 *            the roleId to set
	 */
	public void setRoleId(int roleId)
	{
		this.roleId = roleId;
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
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * @return the permIds
	 */
	public List<String> getPermissionIds()
	{
		return permissionIds;
	}

	/**
	 * @param permIds
	 *            the permIds to set
	 */
	public void setPermissions(List<String> permissionIds)
	{
		this.permissionIds = permissionIds;
	}

	/**
	 * @return the assignable
	 */
	public boolean isAssignable()
	{
		return assignable;
	}

	/**
	 * @param assignable
	 *            the assignable to set
	 */
	public void setAssignable(boolean assignable)
	{
		this.assignable = assignable;
	}

	public void addPermissions(List<IPermission> permissions)
	{
		for (IPermission perm : permissions)
		{
			String fullPath = ((perm.getPath() == null) || (perm.getPath().length() == 0)) ? perm.getPermissionId() : String.format("%s.%s", perm.getPath(), perm.getPermissionId());
			this.permissionIds.add(fullPath);
		}
	}
}
