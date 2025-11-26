package hxc.services.security;

import java.util.List;

public interface IUser
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties Fields
	//
	// /////////////////////////////////

	public abstract String getUserId();

	public abstract void setUserId(String userId);

	public abstract String getName();

	public abstract void setName(String name);

	public abstract String getMobileNumber();

	public abstract void setMobileNumber(String mobileNumber);

	public abstract void setPassword(byte[] password);

	public abstract byte[] getPublicKey();

	public abstract boolean isBuiltIn();

	public abstract boolean isEnabled();

	public abstract void setEnabled(boolean enabled);

	public abstract long getRoles();

	public abstract long getUserRolesForLogin();

	public abstract boolean addRole(int roleId);

	public abstract boolean removeRole(int roleId);

	public abstract List<String> getPermissionIdList();

	public abstract boolean isSupplier();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public abstract boolean hasPermission(Class<?> type, String permissionId);

	public abstract boolean hasPermission(Object object, String permissionId);

	public abstract boolean hasRole(IRole role);

	public abstract boolean update();

	public abstract boolean delete();
	
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internalizations for UICOnnector
	//
	// /////////////////////////////////

	public String getInternalMobileNumber();
	
	public void setInternalMobileNumber(String mobileNumber);
	
	public String getInternalName();
	
	public void setInternalName(String name);
	
	// Note that this is required to allow a user to change their own password
	public void setInternalPassword(byte[] password);
	public String getInternalUserId();
}
