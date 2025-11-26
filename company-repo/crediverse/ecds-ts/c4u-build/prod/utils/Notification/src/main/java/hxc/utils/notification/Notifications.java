package hxc.utils.notification;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import hxc.configuration.Config;
import hxc.connectors.database.IDatabaseConnection;
import hxc.servicebus.ILocale;
import hxc.services.notification.INotification;
import hxc.services.notification.INotificationText;
import hxc.services.notification.INotifications;

public final class Notifications implements INotifications
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private Map<Integer, Notification> map = new LinkedHashMap<Integer, Notification>();
	private Map<String, VariableInfo> variablesMap = null;
	private Class<?> propertyClass;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Classes
	//
	// /////////////////////////////////
	private class VariableInfo
	{
		public String name;
		public String description;
		public boolean hasLanguageCode;
		public Method method;

		@Override
		public String toString()
		{
			return name;
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public Notifications(Class<?> propertyClass)
	{
		this.propertyClass = propertyClass;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public int add(String description, String... texts)
	{
		int notificationId = description.hashCode();
		while (map.containsKey(notificationId))
			notificationId++;
		Notification notification = new Notification(this);
		notification.setDescription(description);
		notification.setNotificationId(notificationId);
		setLanguages(notification, texts);
		map.put(notificationId, notification);
		return notificationId;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// INotifications implementation
	//
	// /////////////////////////////////
	@Override
	public final int[] getNotificationIds()
	{
		Set<Integer> set = map.keySet();
		int[] result = new int[set.size()];
		int index = 0;
		for (Integer element : set)
		{
			result[index++] = element;
		}
		return result;
	}

	@Override
	public final INotification getNotification(int notifcationId)
	{
		return map.get(notifcationId);
	}

	@Override
	public final INotificationText get(int notifcationId, String languageCode, ILocale locale, Object properties)
	{
		// Get the Notification
		Notification notification = map.get(notifcationId);
		if (notification == null)
			return null;

		// Get the text for the specific language
		String text = notification.getText(locale.getLanguageID(languageCode));
		if (text == null || text.length() == 0)
		{
			return null;
		}

		// Tokenize the text
		String[] parts = text.split("(?=\\{)|(?<=\\})");
		StringBuilder builder = new StringBuilder(text.length() << 2);
		for (String part : parts)
		{
			if (part.equalsIgnoreCase("{Currency}"))
				builder.append(locale.getCurrencyCode());
			else if (part.startsWith("{"))
			{
				try
				{
					builder.append(getValue(part, languageCode, properties));
				}
				catch (Exception e)
				{
					builder.append("??");
				}
			}
			else
				builder.append(part);
		}

		return new NotificationText(builder.toString(), languageCode);
	}

	@Override
	public String get(String languageCode, String text, ILocale locale, Object properties)
	{
		// Tokenize the text
		String[] parts = text.split("(?=\\{)|(?<=\\})");
		StringBuilder builder = new StringBuilder(text.length() << 2);
		for (String part : parts)
		{
			if (part.equalsIgnoreCase("{Currency}"))
				builder.append(locale.getCurrencyCode());
			else if (part.startsWith("{"))
			{
				try
				{
					builder.append(getValue(part, languageCode, properties));
				}
				catch (Exception e)
				{
					builder.append("??");
				}
			}
			else
				builder.append(part);
		}

		return builder.toString();

	}

	@Override
	public String[] getVariables()
	{
		Map<String, VariableInfo> map = getVariablesMap();
		String[] names = new String[map.size()];
		int index = 0;
		for (VariableInfo variable : map.values())
			names[index++] = variable.name;
		return names;
	}

	@Override
	public void save(IDatabaseConnection connection, long serialVersionUID) throws SQLException
	{
		for (Notification notification : map.values())
		{
			notification.setNotificationsId(serialVersionUID);
			connection.upsert(notification);
		}
	}

	@Override
	public void load(IDatabaseConnection connection, long serialVersionUID) throws SQLException
	{
		List<Notification> notifications = connection.selectList(Notification.class, "where notificationsID = %s", serialVersionUID);
		for (Notification notification : notifications)
		{
			notification.setParent(this);
			map.put(notification.getNotificationId(), notification);
		}

	}

	@Override
	public Map<String, String> getVariableDetails()
	{
		Map<String, String> variableDetails = new TreeMap<>();

		for (VariableInfo variable : getVariablesMap().values())
		{
			String name = variable.name.replaceAll("\\{", "");
			name = name.replaceAll("\\}", "");
			variableDetails.put(name, variable.description);
		}
		return variableDetails;
	}

	public Notification[] getNotifications()
	{
		return map.values().toArray(new Notification[map.size()]);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	private Map<String, VariableInfo> getVariablesMap()
	{
		if (variablesMap == null)
		{
			variablesMap = new LinkedHashMap<String, VariableInfo>();
			getVariablesMap(propertyClass);
		}

		return variablesMap;
	}

	private void getVariablesMap(Class<?> type)
	{
		if (type == null)
			return;

		Class<?> base = type.getSuperclass();
		if (base != null && base != Object.class)
			getVariablesMap(base);

		Method[] methods = type.getDeclaredMethods();
		for (Method method : methods)
		{
			method.setAccessible(true);
			if (!method.getName().startsWith("get") || method.getReturnType() != String.class)
				continue;
			VariableInfo variable = new VariableInfo();
			variable.name = "{" + method.getName().substring(3) + "}";

			// Descriptions are stored in annotations
			Annotation annotation = method.getAnnotation(Config.class);
			variable.description = (annotation != null) ? ((Config) annotation).description() : "";

			variable.method = method;
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length == 0)
				variable.hasLanguageCode = false;
			else if (parameterTypes.length > 1 || parameterTypes[0] != String.class)
				continue;
			else
				variable.hasLanguageCode = true;
			variablesMap.put(variable.name.toLowerCase(), variable);
		}

		// Add Currency (As default)
		VariableInfo variable = new VariableInfo();
		variable.name = "{Currency}";
		variablesMap.put(variable.name.toLowerCase(), variable);
	}

	private Object getValue(String variableName, String languageCode, Object properties) throws Exception
	{
		VariableInfo variable = getVariablesMap().get(variableName.toLowerCase());
		if (variable == null)
			throw new Exception("Invalid Variable: " + variableName);

		try
		{
			return variable.hasLanguageCode ? (String) variable.method.invoke(properties, languageCode) : (String) variable.method.invoke(properties);
		}
		catch (Exception e1)
		{
			String name = variable.method.getName();
			Method property = properties.getClass().getMethod(name);
			if (property == null)
				throw e1;
			property.setAccessible(true);
			return variable.hasLanguageCode ? (String) property.invoke(properties, languageCode) : (String) property.invoke(properties);
		}

	}

	public String getVariable(String variableName)
	{
		VariableInfo variable = getVariablesMap().get(variableName.toLowerCase());
		return variable == null ? null : variable.name;
	}

	private void setLanguages(Notification notification, String... texts)
	{
		int count = texts == null ? 0 : texts.length;
		if (count >= 1)
			notification.setLanguage1Text(texts[0]);
		if (count >= 2)
			notification.setLanguage2Text(texts[1]);
		if (count >= 3)
			notification.setLanguage3Text(texts[1]);
		if (count >= 4)
			notification.setLanguage4Text(texts[1]);
	}



}
