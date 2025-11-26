package hxc.utils.protocol.uiconnector.response;

import java.util.ArrayList;
import java.util.List;

public class AuthenticateResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -1312584276741607109L;
	private String name;
	private List<String> permissionIds = null;

	public AuthenticateResponse(String userId)
	{
		super(userId);
		setResponseCode(UiResponseCode.AUTHENTICATE);
		permissionIds = new ArrayList<>();
	}

	/**
	 * @return the permissionIds
	 */
	public List<String> getPermissionIds()
	{
		return permissionIds;
	}

	/**
	 * @param permissionIds
	 *            the permissionIds to set
	 */
	public void setPermissionIds(List<String> permissionIds)
	{
		this.permissionIds = permissionIds;
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

}
