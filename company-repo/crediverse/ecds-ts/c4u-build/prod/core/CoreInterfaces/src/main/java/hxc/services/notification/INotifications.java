package hxc.services.notification;

import java.sql.SQLException;
import java.util.Map;

import hxc.connectors.database.IDatabaseConnection;
import hxc.servicebus.ILocale;

public interface INotifications
{
	public abstract int[] getNotificationIds();

	public abstract INotification getNotification(int notificationId);

	public abstract INotificationText get(int notifcationId, String languageCode, ILocale locale, Object properties);

	public abstract String get(String languageCode, String text, ILocale locale, Object properties);

	public abstract String[] getVariables();

	public abstract void save(IDatabaseConnection connection, long serialVersionUID) throws SQLException;

	public abstract void load(IDatabaseConnection connection, long serialVersionUID) throws SQLException;

	public abstract Map<String, String> getVariableDetails();
}
