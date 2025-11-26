package hxc.services.security;

import java.util.ArrayList;
import java.util.List;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;
import hxc.utils.reflection.NonReflective;

@Table(name = "se_user")
@Perms(perms = { @Perm(name = "ChangeUsers", category = "User Configuration", description = "Change Users", implies = "ViewUsers"),
		@Perm(name = "ViewUsers", category = "User Configuration", description = "View Users") })
public class User implements IUser
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Private fields
	//
	// /////////////////////////////////
	@NonReflective
	private SecurityService parent;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Persistable Fields
	//
	// /////////////////////////////////

	@Column(primaryKey = true, maxLength = 15)
	private String userId;

	@Column(maxLength = 60, nullable = false)
	private String name;

	@Column(maxLength = 15, nullable = true)
	private String mobileNumber;

	@Column(nullable = false, maxLength = 50)
	private byte[] password;

	@Column(nullable = false, maxLength = 50)
	private byte[] publicKey;

	private boolean builtIn;

	private boolean enabled;

	@Column(defaultValue = "3")
	private long roles;

	private int control;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@Override
	public String getUserId()
	{
		parent.check(this, "ViewUsers");
		return userId;
	}

	public String getInternalUserId()
	{
		return userId;
	}

	@Override
	public void setUserId(String userId)
	{
		parent.check(this, "ChangeUsers");
		this.userId = userId;
	}

	@Override
	public String getName()
	{
		parent.check(this, "ViewUsers");
		return name;
	}

	public String getInternalName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
		parent.check(this, "ChangeUsers");
		this.name = name;
	}

	public void setInternalName(String name)
	{
		this.name = name;
	}

	@Override
	public String getMobileNumber()
	{
		parent.check(this, "ViewUsers");
		return mobileNumber;
	}

	public String getInternalMobileNumber()
	{
		return mobileNumber;
	}

	@Override
	public void setMobileNumber(String mobileNumber)
	{
		parent.check(this, "ChangeUsers");
		this.mobileNumber = mobileNumber;
	}

	public void setInternalMobileNumber(String mobileNumber)
	{
		this.mobileNumber = mobileNumber;
	}

	public byte[] getPassword()
	{
		parent.check(this, "ViewUsers");
		return password;
	}

	public byte[] getInternalPassword()
	{
		return password;
	}

	@Override
	public void setPassword(byte[] password)
	{
		parent.check(this, "ChangeUsers");
		this.password = password;
	}

	public void setInternalPassword(byte[] password)
	{
		this.password = password;
	}

	@Override
	public byte[] getPublicKey()
	{
		return publicKey;
	}

	public void setPublicKey(byte[] publicKey)
	{
		parent.check(this, "ChangeUsers");
		this.publicKey = publicKey;
	}

	@Override
	public boolean isBuiltIn()
	{
		parent.check(this, "ViewUsers");
		return builtIn;
	}

	public void setBuiltIn(boolean builtIn)
	{
		parent.check(this, "ChangeUsers");
		this.builtIn = builtIn;
	}

	@Override
	public boolean isEnabled()
	{
		parent.check(this, "ViewUsers");
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		parent.check(this, "ChangeUsers");
		this.enabled = enabled;
	}

	@Override
	public long getRoles()
	{
		parent.check(this, "ViewUsers");
		return roles;
	}

	public long getInternalRoles()
	{
		return roles;
	}

	@Override
	public boolean addRole(int roleId)
	{
		parent.check(this, "ChangeUsers");
		roles |= (1L << (roleId - 1));
		return true;
	}

	@Override
	public boolean removeRole(int roleId)
	{
		parent.check(this, "ChangeUsers");
		roles &= ~(1L << (roleId - 1));
		return true;
	}

	public int getControl()
	{
		parent.check(this, "ViewUsers");
		return control;
	}

	public void setControl(int control)
	{
		parent.check(this, "ChangeUsers");
		this.control = control;
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
	public boolean hasPermission(Class<?> type, String permissionId)
	{

		return parent.hasPermission(type, permissionId, roles);
	}

	@Override
	public boolean hasPermission(Object object, String permissionId)
	{
		parent.check(this, "ViewUsers");

		// Defensive
		if (object == null || permissionId == null)
			return false;

		return parent.hasPermission(object.getClass(), permissionId, roles);
	}

	@Override
	public boolean hasRole(IRole role)
	{
		if (role == null)
			return false;
		long mask = 1L << (role.getRoleId() - 1);
		return (roles & mask) != 0;
	}

	@Override
	public boolean update()
	{
		parent.check(this, "ChangeUsers");
		return parent.updateUser(this);
	}

	@Override
	public boolean delete()
	{
		parent.check(this, "ChangeUsers");
		return parent.deleteUser(this);
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public List<String> getPermissionIdList()
	{
		List<String> result = new ArrayList<String>();

		return result;
	}

	@Override
	public long getUserRolesForLogin()
	{
		return roles;
	}

	@Override
	public boolean isSupplier()
	{
		return ((roles & SecurityService.SUPPLIER_ROLE) > 0);
	}

}
