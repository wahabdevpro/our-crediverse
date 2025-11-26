package hxc.utils.protocol.uiconnector.ctrl.response;

import hxc.connectors.ctrl.IServerRole;

public class ServerRole implements IServerRole
{

	private static final long serialVersionUID = 7910531043604655050L;
	private String serverRoleName;
	private boolean exclusive;
	private String owner;
	private String attachCommand;
	private String detachCommand;
	private int originalIndex = -1;

	public ServerRole()
	{
	}

	public ServerRole(String serverRoleName, boolean exclusive, String owner, String attachCommand, String detachCommand)
	{
		this.serverRoleName = serverRoleName;
		this.exclusive = exclusive;
		this.owner = owner;
		this.attachCommand = attachCommand;
		this.detachCommand = detachCommand;
	}

	public ServerRole(String serverRoleName, boolean exclusive, String attachCommand, String detachCommand)
	{
		this.serverRoleName = serverRoleName;
		this.exclusive = exclusive;
		this.attachCommand = attachCommand;
		this.detachCommand = detachCommand;
	}

	@Override
	public String getServerRoleName()
	{
		return serverRoleName;
	}

	@Override
	public boolean isExclusive()
	{
		return exclusive;
	}

	@Override
	public String getOwner()
	{
		return owner;
	}

	@Override
	public String getAttachCommand()
	{
		return attachCommand;
	}

	@Override
	public String getDetachCommand()
	{
		return detachCommand;
	}

	/**
	 * @param serverRoleName
	 *            the serverRoleName to set
	 */
	public void setServerRoleName(String serverRoleName)
	{
		this.serverRoleName = serverRoleName;
	}

	/**
	 * @param exclusive
	 *            the exclusive to set
	 */
	public void setExclusive(boolean exclusive)
	{
		this.exclusive = exclusive;
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	/**
	 * @param attachCommand
	 *            the attachCommand to set
	 */
	public void setAttachCommand(String attachCommand)
	{
		this.attachCommand = attachCommand;
	}

	/**
	 * @param detachCommand
	 *            the detachCommand to set
	 */
	public void setDetachCommand(String detachCommand)
	{
		this.detachCommand = detachCommand;
	}

	public int getOriginalIndex()
	{
		return originalIndex;
	}

	public void setOriginalIndex(int originalIndex)
	{
		this.originalIndex = originalIndex;
	}

}
