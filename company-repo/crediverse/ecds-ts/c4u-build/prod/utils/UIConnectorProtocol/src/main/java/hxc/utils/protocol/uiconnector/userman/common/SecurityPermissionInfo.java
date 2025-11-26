package hxc.utils.protocol.uiconnector.userman.common;

import java.io.Serializable;

public class SecurityPermissionInfo implements Serializable
{

	private static final long serialVersionUID = -5855271341621997249L;

	private String permId;
	private String path;
	private String description;
	private String category;
	private String implies;

	private boolean assignable = false; // Can be assigned by current user

	public SecurityPermissionInfo()
	{
	}

	public SecurityPermissionInfo(String permId, String path, String description, String category, String implies, boolean assignable)
	{
		this.permId = permId;
		this.path = path;
		this.description = description;
		this.category = category;
		this.implies = implies;
		this.assignable = assignable;
	}

	public String getPermId()
	{
		return permId;
	}

	public void setPermId(String permId)
	{
		this.permId = permId;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
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

	/**
	 * @return the category
	 */
	public String getCategory()
	{
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(String category)
	{
		this.category = category;
	}

	/**
	 * @return the implies
	 */
	public String getImplies()
	{
		return implies;
	}

	/**
	 * @param implies
	 *            the implies to set
	 */
	public void setImplies(String implies)
	{
		this.implies = implies;
	}

}
