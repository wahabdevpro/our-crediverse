package hxc.services.security;

import hxc.configuration.ValidationException;

public interface IPermission
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getPermissionId();

	public String getPath();

	public void setPath(String path);

	public String getDescription();

	public void setDescription(String description);

	public String getCategory();

	public void setCategory(String category) throws ValidationException;

	public String getImplies();

	public void setImplies(String implies);

	public long getRoles();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public abstract boolean update();

	public abstract boolean delete();

	public abstract boolean addRole(int roleId);

	public abstract boolean removeRole(int roleId);

}