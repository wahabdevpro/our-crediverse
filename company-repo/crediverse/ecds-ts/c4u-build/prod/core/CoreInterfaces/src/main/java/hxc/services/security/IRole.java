package hxc.services.security;

public interface IRole
{
	public abstract int getRoleId();

	public abstract String getName();

	public abstract void setName(String name);

	public abstract String getDescription();

	public abstract void setDescription(String description);

	public abstract boolean isBuiltIn();

	public abstract boolean update();

	public abstract boolean delete();
}