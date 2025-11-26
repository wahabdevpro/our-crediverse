package hxc.services.security;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;
import hxc.utils.reflection.NonReflective;

@Table(name = "se_perm")
@Perms(perms = { @Perm(name = "ViewPermissions", category = "User Permissions", description = "View Permission"),
		@Perm(name = "ChangePermissions", category = "User Permissions", description = "Change Permissions", implies = "ViewPermissions") })
public class Permission implements IPermission
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Private Properties
	//
	// /////////////////////////////////
	@NonReflective
	private SecurityService parent;

	public void setParent(SecurityService parent)
	{
		this.parent = parent;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Persistable Fields
	//
	// /////////////////////////////////

	@Column(primaryKey = true, maxLength = 50, nullable = false)
	private String permissionId;

	@Column(primaryKey = true, maxLength = 50, nullable = false)
	private String category;

	@Column(maxLength = 250, nullable = false)
	private String path;

	@Column(maxLength = 80, nullable = false)
	private String description;

	@Column(maxLength = 50, nullable = false)
	private String implies;

	private long roles;

	private int control;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@Override
	public String getPermissionId()
	{
		return permissionId;
	}

	public void setPermissionId(String permissionId)
	{
		this.permissionId = permissionId;
	}

	@Override
	public String getPath()
	{
		return path;
	}

	@Override
	public void setPath(String path)
	{
		parent.check(this, "ChangePermissions");
		this.path = path;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public void setDescription(String description)
	{
		this.description = description;
	}

	@Override
	public String getCategory()
	{
		return category;
	}

	@Override
	public void setCategory(String category)
	{
		this.category = category;
	}

	@Override
	public String getImplies()
	{
		return implies;
	}

	@Override
	public void setImplies(String implies)
	{
		this.implies = implies;
	}

	@Override
	public long getRoles()
	{
		parent.check(this, "ViewPermissions");
		return roles;
	}

	/**
	 * Please note security is circumvented for this call
	 * 
	 * @return
	 */
	public long getRoleForValidation()
	{
		return roles;
	}

	public long getInternalRoles()
	{
		return roles;
	}

	public void setRoles(long roles)
	{
		parent.check(this, "ChangePermissions");
		this.roles = roles;
	}

	public int getControl()
	{
		return control;
	}

	public void setControl(int control)
	{
		this.control = control;
	}

	public SecurityService getParent()
	{
		return parent;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Permission()
	{
	}

	public Permission(SecurityService parent, String permissionId, String description, String category, String implies, String path, long roles)
	{
		this.parent = parent;
		this.permissionId = permissionId;
		this.path = path;
		this.description = description;
		this.category = category;
		this.implies = implies;
		this.roles = roles;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@Override
	public boolean update()
	{
		parent.check(this, "ChangePermissions");
		return parent.updatePermission(this);
	}

	@Override
	public boolean delete()
	{
		parent.check(this, "ChangePermissions");
		return parent.deletePermission(this);
	}

	@Override
	public boolean addRole(int roleId)
	{
		parent.check(this, "ChangePermissions");
		roles |= 1L << (roleId - 1);
		return true;
	}

	@Override
	public boolean removeRole(int roleId)
	{
		parent.check(this, "ChangePermissions");
		roles &= ~(1L << (roleId - 1));
		return true;
	}

	@Override
	public String toString()
	{
		return path + "." + permissionId;
	}

}
