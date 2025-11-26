package hxc.userinterfaces.gui.data;

import java.util.List;

public class PermissionView
{
	private String permId;
	private String description;
	private String implies;
	private List<String> impliedBy;
	private boolean assignable;

	public PermissionView()
	{
	}

	public PermissionView(String permId, String description, String implies, boolean assignable)
	{
		this.permId = permId;
		this.description = description;
		this.implies = implies;
		this.assignable = assignable;
	}

	/**
	 * @return the permId
	 */
	public String getPermId()
	{
		return permId;
	}

	/**
	 * @param permId
	 *            the permId to set
	 */
	public void setPermId(String permId)
	{
		this.permId = permId;
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
	 * @return the impliedBy
	 */
	public List<String> getImpliedBy()
	{
		return impliedBy;
	}

	/**
	 * @param impliedBy
	 *            the impliedBy to set
	 */
	public void setImpliedBy(List<String> impliedBy)
	{
		this.impliedBy = impliedBy;
	}

	public String impliedByJSArray()
	{
		StringBuilder sb = new StringBuilder("[");
		try
		{
			if (impliedBy != null)
			{
				for (int i = 0; i < impliedBy.size(); i++)
				{
					sb.append((i > 0) ? ",\"" : "\"");
					sb.append(impliedBy.get(i));
					sb.append("\"");
				}
			}

		}
		catch (Exception e)
		{
		}

		sb.append("]");
		return sb.toString();
	}
}
