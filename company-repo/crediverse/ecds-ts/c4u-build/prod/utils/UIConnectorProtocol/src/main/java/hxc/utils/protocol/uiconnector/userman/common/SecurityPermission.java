package hxc.utils.protocol.uiconnector.userman.common;

import java.io.Serializable;

public class SecurityPermission implements Serializable
{

	private static final long serialVersionUID = 2831148624191621353L;
	private String permId;
	private String category;

	public SecurityPermission()
	{
	}

	public SecurityPermission(String permId, String category)
	{
		this.permId = permId;
		this.category = category;
	}

	public String getPermId()
	{
		return permId;
	}

	public String getCategory()
	{
		return category;
	}

	public void setPermId(String permId)
	{
		this.permId = permId;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}

}
