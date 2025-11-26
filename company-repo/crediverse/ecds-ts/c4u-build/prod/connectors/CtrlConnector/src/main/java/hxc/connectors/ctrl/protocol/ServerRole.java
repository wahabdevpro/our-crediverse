package hxc.connectors.ctrl.protocol;

import hxc.configuration.ValidationException;
import hxc.connectors.ctrl.IServerRole;
import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

@Table(name = "ct_role")
public class ServerRole implements IServerRole
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private static final long serialVersionUID = -4697736688628145818L;

	@Column(primaryKey = true, maxLength = 32)
	private String serverRoleName;

	private boolean exclusive = false;

	@Column(maxLength = 255, nullable = true)
	private String attachCommand;

	@Column(maxLength = 255, nullable = true)
	private String detachCommand;

	@Column(maxLength = 64, nullable = true)
	private String owner;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public String getServerRoleName()
	{
		return serverRoleName;
	}

	public ServerRole setServerRoleName(String serverRoleName) throws ValidationException
	{
		if (serverRoleName == null || serverRoleName.length() == 0 || serverRoleName.length() > 32)
			throw new ValidationException("Invalid Server Role Name : %s", serverRoleName);
		this.serverRoleName = serverRoleName;
		return this;
	}

	@Override
	public boolean isExclusive()
	{
		return exclusive;
	}

	public ServerRole setExclusive(boolean exclusive)
	{
		this.exclusive = exclusive;
		if (!exclusive)
			owner = null;
		return this;
	}

	@Override
	public String getOwner()
	{
		return exclusive ? owner : null;
	}

	public ServerRole setOwner(String owner) throws ValidationException
	{
		if (owner != null && owner.length() > 64)
			throw new ValidationException("Invalid Role Owner : %s", owner);
		this.owner = owner;
		return this;
	}

	@Override
	public String getAttachCommand()
	{
		return attachCommand;
	}

	public ServerRole setAttachCommand(String attachCommand) throws ValidationException
	{
		if (attachCommand != null && attachCommand.length() > 50)
			throw new ValidationException("Invalid Attach Command : %s", attachCommand);
		this.attachCommand = attachCommand;
		return this;
	}

	@Override
	public String getDetachCommand()
	{
		return detachCommand;
	}

	public ServerRole setDetachCommand(String detachCommand) throws ValidationException
	{
		if (detachCommand != null && detachCommand.length() > 50)
			throw new ValidationException("Invalid Detach Command : %s", detachCommand);
		this.detachCommand = detachCommand;
		return this;
	}

	public ServerRole()
	{
	}

	public ServerRole(String serverRoleName, boolean exclusive, String attachCommand, String detachCommand, String owner) throws ValidationException
	{
		this.setServerRoleName(serverRoleName);
		this.setExclusive(exclusive);
		this.setAttachCommand(attachCommand);
		this.setDetachCommand(detachCommand);
		this.setOwner(owner);
	}

	public static ServerRole create(String serverRoleName, boolean exclusive, String attachCommand, String detachCommand, String owner) throws ValidationException
	{
		return new ServerRole(serverRoleName, exclusive, attachCommand, detachCommand, owner);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public String toString()
	{
		return String.format("%s", serverRoleName);
	}

}
