package hxc.configuration;

import java.lang.reflect.Method;
import java.util.Collection;

import hxc.connectors.ctrl.ICtrlConnector;
import hxc.connectors.database.IDatabaseConnection;
import hxc.services.notification.INotifications;
import hxc.utils.reflection.IPropertyInfo;

public interface IConfiguration
{
	public abstract int getVersion();

	public abstract String getName(String languageCode);

	public abstract String getPath(String languageCode);

	public abstract Collection<IConfiguration> getConfigurations();

	public abstract void validate() throws ValidationException;

	public abstract boolean save(IDatabaseConnection databaseConnection, ICtrlConnector control);

	public abstract boolean load(IDatabaseConnection databaseConnection);

	public abstract INotifications getNotifications();

	public abstract void updateNotification(int notificationId, int languageId, String text);

	public abstract void performGetNotificationSecurityCheck();

	public abstract long getSerialVersionUID();

	public abstract Method[] getMethods();

	public abstract IPropertyInfo[] getProperties();
}