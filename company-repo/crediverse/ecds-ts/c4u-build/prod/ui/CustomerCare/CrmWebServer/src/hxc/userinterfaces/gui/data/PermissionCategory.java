package hxc.userinterfaces.gui.data;

import java.util.ArrayList;
import java.util.List;

public class PermissionCategory
{
	private String description;
	private List<PermissionView> permissions;

	public PermissionCategory(String description)
	{
		this.description = description;
		this.permissions = new ArrayList<>();
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
	 * @return the permissions
	 */
	public List<PermissionView> getPermissions()
	{
		return permissions;
	}

	/**
	 * @param permissions
	 *            the permissions to set
	 */
	public void setPermissions(List<PermissionView> permissions)
	{
		this.permissions = permissions;
	}

}
