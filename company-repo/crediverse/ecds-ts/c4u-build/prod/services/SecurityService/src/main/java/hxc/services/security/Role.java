package hxc.services.security;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;
import hxc.utils.reflection.NonReflective;

@Table(name = "se_role")
@Perms(perms = { @Perm(name = "ViewRoles", category = "User Roles", description = "View Roles"),
		@Perm(name = "ChangeRoles", category = "User Roles", description = "Change Roles", implies = "ViewRoles") })
public class Role implements IRole
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Private Fields
	//
	// /////////////////////////////////

	@NonReflective
	SecurityService parent;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Persistable Fields
	//
	// /////////////////////////////////

	@Column(primaryKey = true)
	private int roleId;

	@Column(maxLength = 60, nullable = false)
	private String name;

	@Column(maxLength = 100, nullable = false)
	private String description;

	private boolean builtIn;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@Override
	public int getRoleId()
	{
		return roleId;
	}

	public void setRoleId(int roleId)
	{
		parent.check(this, "ChangeRoles");
		this.roleId = roleId;
	}

	@Override
	public String getName()
	{
		parent.check(this, "ViewRoles");
		return name;
	}

	@Override
	public void setName(String name)
	{
		parent.check(this, "ChangeRoles");
		this.name = name;
	}

	@Override
	public String getDescription()
	{
		parent.check(this, "ViewRoles");
		return description;
	}

	@Override
	public void setDescription(String description)
	{
		parent.check(this, "ChangeRoles");
		this.description = description;
	}

	@Override
	public boolean isBuiltIn()
	{
		parent.check(this, "ViewRoles");
		return builtIn;
	}

	public void setBuiltIn(boolean builtIn)
	{
		parent.check(this, "ChangeRoles");
		this.builtIn = builtIn;
	}

	public void setParent(SecurityService parent)
	{
		this.parent = parent;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@Override
	public boolean update()
	{
		parent.check(this, "ChangeRoles");
		return parent.updateRole(this);
	}

	@Override
	public boolean delete()
	{
		parent.check(this, "ChangeRoles");
		return parent.deleteRole(this);
	}

	@Override
	public String toString()
	{
		return name;
	}

}
