package hxc.services.security;

import java.util.List;

public interface ISecurity extends ISecurityCheck
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Authentication
	//
	// /////////////////////////////////
	public abstract byte[] getPublicKey(String userId);

	public abstract IUser authenticate(String userId, byte[] credentials);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// User Management
	//
	// /////////////////////////////////
	public abstract List<IUser> getUsers();

	public abstract IUser getUser(String userId);

	public abstract IUser createUser(String userId);

	public abstract List<String> getUserPermissionIds(IUser user);

	public abstract List<IPermission> getUserPermissionDetails(IUser user);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Role Management
	//
	// /////////////////////////////////
	public abstract List<IRole> getRoles();

	public abstract IRole getRole(int roleId);

	public abstract IRole createRole(int roleId);

	public abstract List<String> getRolePermissionIds(IRole role);

	public abstract List<IPermission> getRolePermissionDetails(IRole role);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Permission Management
	//
	// /////////////////////////////////
	public abstract IPermission getPermission(String permissionId);

	public abstract IPermission getPermission(String permissionId, String category);

	public abstract List<IPermission> getPermissions();

	public abstract boolean hasPermission(Object object, String permissionId);

	public abstract boolean hasPermission(Class<?> type, String permissionId);

	public abstract boolean isSupplier(IUser user);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internalization for UIConnector
	//
	// /////////////////////////////////
	public boolean updateInternalUserDetails(IUser user);
}