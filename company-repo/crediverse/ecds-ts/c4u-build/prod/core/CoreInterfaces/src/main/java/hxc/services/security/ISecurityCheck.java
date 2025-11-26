package hxc.services.security;

public interface ISecurityCheck
{
	public abstract void check(Object object, String permissionId) throws SecurityException;

	public abstract void check(Class<?> type, String permissionId) throws SecurityException;
}
