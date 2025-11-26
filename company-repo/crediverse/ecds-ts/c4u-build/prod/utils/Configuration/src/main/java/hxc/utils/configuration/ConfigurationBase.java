package hxc.utils.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.Configurable;
import hxc.configuration.IConfigurable;
import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.ctrl.ICtrlConnector;
import hxc.connectors.database.IDatabaseConnection;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.INotifications;
import hxc.services.security.ISecurityCheck;
import hxc.utils.reflection.ClassInfo;
import hxc.utils.reflection.FieldInfo;
import hxc.utils.reflection.FieldInfo.FieldTypes;
import hxc.utils.reflection.IPropertyInfo;
import hxc.utils.reflection.NonReflective;
import hxc.utils.reflection.PropertyInfo;
import hxc.utils.reflection.ReflectionHelper;

@Configurable
public abstract class ConfigurationBase implements IConfiguration
{
	final static Logger logger = LoggerFactory.getLogger(ConfigurationBase.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private int version;

	@NonReflective
	private Method[] methods = null;

	@NonReflective
	private IPropertyInfo[] properties = null;

	@NonReflective
	private ISecurityCheck security = null;

	@Override
	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	@Override
	public abstract String getName(String languageCode);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@Override
	public Collection<IConfiguration> getConfigurations()
	{
		return null;
	}

	@Override
	public abstract void validate() throws ValidationException;

	@Override
	public boolean save(IDatabaseConnection database, ICtrlConnector control)
	{
		logger.trace("save: {}", getSerialVersionUID());
		// Validate
		try
		{
			validate();
		}
		catch (ValidationException e1)
		{
			logger.warn("Validation failure", e1);
			return false;
		}

		// Delete old parameters
		try
		{
			database.delete(ConfigRecord.class, "where SerialVersionUID = %s ", getSerialVersionUID());
		}
		catch (SQLException e)
		{
			logger.warn("Failed to delete old parameters", e);
		}

		// Save this config to Database
		int sequence = 1;
		sequence = saveObject("", this, sequence, database);

		// Get fields
		ClassInfo classInfo = ReflectionHelper.getClassInfo(this.getClass());
		Collection<FieldInfo> values = classInfo.getFields().values();

		// Save notifications too
		INotifications notifications = getNotifications();
		if (notifications != null)
		{
			try
			{
				notifications.save(database, getSerialVersionUID());
			}
			catch (SQLException e)
			{
				logger.error("Failed to save notification", e);
				return false;
			}
		}

		// Save Children too
		Collection<IConfiguration> children = getConfigurations();
		if (children != null)
		{
			for (IConfiguration child : children)
			{
				if (!child.save(database, control))
					return false;
			}
		}

		// Notify other computers too
		if (control != null)
			control.notifyConfigChange(getSerialVersionUID());

		return true;
	}

	private int saveArray(String prefix, Object instance, FieldInfo arrayField, int sequence, IDatabaseConnection database)
	{
		Object array;
		try
		{
			array = arrayField.get(instance);
		}
		catch (IllegalArgumentException | IllegalAccessException e1)
		{
			logger.error("Invalid array", e1);
			return sequence;
		}

		if (array == null)
			return sequence;

		// Save Array Length
		int length = Array.getLength(array);
		String fieldName = String.format("%s.ArrayLength", prefix);
		byte[] fieldBytes = ByteBuffer.allocate(4).putInt((int) length).array();
		sequence = saveField(sequence, database, fieldName, fieldBytes);

		for (int index = 0; index < length; index++)
		{
			Object element = Array.get(array, index);
			String indexPrefix = String.format("%s[%d]", prefix, index);

			if (element != null
					&& (element instanceof Integer || element instanceof String || element instanceof Integer || element instanceof Long || element instanceof Float || element instanceof Double))
			{
				try
				{
					if (element instanceof String)
						fieldBytes = ((String) element).getBytes("UTF-8");
					else if (element instanceof Integer)
						fieldBytes = ByteBuffer.allocate(4).putInt((int) element).array();
					else if (element instanceof Long)
						fieldBytes = ByteBuffer.allocate(8).putLong((long) element).array();
					else if (element instanceof Float)
						fieldBytes = ByteBuffer.allocate(4).putFloat((float) element).array();
					else if (element instanceof Double)
						fieldBytes = ByteBuffer.allocate(8).putDouble((double) element).array();

					sequence = saveField(sequence, database, indexPrefix, fieldBytes);
				}
				catch (UnsupportedEncodingException e)
				{
					logger.error("Encoding error", e);
				}

			}
			else
				sequence = saveObject(indexPrefix, element, sequence, database);
		}

		return sequence;
	}

	private int saveObject(String prefix, Object object, int sequence, IDatabaseConnection database)
	{
		long startTime = System.nanoTime();
		// Exit if null
		if (object == null)
			return sequence;

		// Prefix delimiter
		if (prefix != null && prefix.length() != 0)
			prefix = prefix + ".";

		// Exit if this class is not @Configurable
		Class<?> type = object.getClass();
		if (!type.isAnnotationPresent(Configurable.class) && !ConfigurationBase.class.isAssignableFrom(type))
			return sequence;

		// Get fields
		ClassInfo classInfo = ReflectionHelper.getClassInfo(type);
		Collection<FieldInfo> values = classInfo.getFields().values();

		// For each Field
		for (FieldInfo field : values)
		{
			// Compile Field Name
			String fieldName = prefix + field.getName();

			if (IConfigurable.class.isAssignableFrom(field.getType()))
			{
				IConfigurable param;
				try
				{
					param = (IConfigurable) field.get(object);
				}
				catch (IllegalArgumentException | IllegalAccessException e)
				{
					logger.error("Invalid field", e);
					continue;
				}
				try
				{
					if (param != null)
						param.save(database, getSerialVersionUID());
				}
				catch (SQLException e)
				{
					logger.error("Failed to save", e);
				}
				continue;
			}

			if (field.getFieldType() == FieldTypes.Other)
			{
				Class<?> fieldType = field.getField().getType();
				if (fieldType.isArray() && fieldType.getComponentType().isAnnotationPresent(Configurable.class))
				{
					sequence = saveArray(fieldName, object, field, sequence, database);
				}
				else if (fieldType.isAnnotationPresent(Configurable.class))
				{
					try
					{
						sequence = saveObject(fieldName, field.get(object), sequence, database);
					}
					catch (IllegalArgumentException | IllegalAccessException e)
					{
						logger.error("Invalid field", e);
					}
				}
				else if (fieldType.isArray() && (fieldType.getComponentType().equals(String.class) || fieldType.getComponentType().equals(Integer.class)))
				{
					sequence = saveArray(fieldName, object, field, sequence, database);
				}
				continue;
			}

			try
			{
				sequence = saveField(sequence, database, fieldName, field.getBytes(object));
			}
			catch (Throwable e)
			{
				logger.error("Exception thrown saving field:{} details:{}", fieldName, (object!=null)? object.toString() : "NULL");
			}

		}
		long endTime = System.nanoTime();
		logger.debug("Configuration for Object: {} Saved Time taken: {} ms", prefix == null ? "Unknown" : prefix, ((endTime - startTime) / 1000000.0));

		return sequence;

	}

	private int saveField(int sequence, IDatabaseConnection database, String fieldName, byte[] fieldBytes)
	{
		ConfigRecord record = new ConfigRecord();
		record.SerialVersionUID = getSerialVersionUID();
		record.name = fieldName;
		record.sequence = sequence++;
		try
		{
			record.value = fieldBytes;
			database.insert(record);
		}
		catch (SQLException | IllegalArgumentException e)
		{
			logger.error(String.format("Failed to save field {}", fieldName), e);
		}
		return sequence;
	}

	// Convert a Comma separated list to a pipe delimited list
	public static String toPipeDelimitedList(String commaSeparatedList)
	{
		// Defensive
		if (commaSeparatedList == null)
			return null;

		StringBuilder sb = new StringBuilder(commaSeparatedList.length() + 2);
		String[] parts = commaSeparatedList.split("\\,");
		for (String part : parts)
		{
			sb.append('|');
			sb.append(part);
		}
		sb.append('|');

		return sb.toString();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IConfigurable Interface
	//
	// /////////////////////////////////
	@Override
	public Method[] getMethods()
	{
		if (this.methods == null)
		{
			List<Method> result = new ArrayList<Method>();
			Method[] methods = this.getClass().getDeclaredMethods();
			for (Method method : methods)
			{
				String name = method.getName();
				if (name.startsWith("get") || name.startsWith("set") || name.startsWith("is"))
					continue;
				if (name.equals("validate") || name.equals("save"))
					continue;
				if (method.getParameterTypes().length > 0)
					continue;
				if (method.getReturnType() != String.class && method.getReturnType() != Void.class)
					continue;
				result.add(method);
			}

			this.methods = result.toArray(new Method[0]);
		}

		return this.methods;
	}

	@Override
	public IPropertyInfo[] getProperties()
	{
		if (this.properties == null)
		{

			List<IPropertyInfo> result = new ArrayList<IPropertyInfo>();
			Collection<PropertyInfo> properties = ReflectionHelper.getClassInfo(this.getClass()).getProperties().values();
			for (PropertyInfo property : properties)
			{
				String name = property.getName();
				if (name.equals("Version") || name.equals("Name") || name.equals("Configurations") || name.equals("Methods") || name.equals("Properties") || name.equals("Path")
						|| name.equals("Notifications") || name.equals("SerialVersionUID"))
					continue;
				result.add(property);
			}

			this.properties = result.toArray(new IPropertyInfo[0]);
		}

		return this.properties;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Protected Methods
	//
	// /////////////////////////////////
	protected void check(IServiceBus esb, String permissionId)
	{
		// Get and Cache Security Service
		if (security == null && esb != null)
			security = esb.getFirstService(ISecurityCheck.class);
		if (security != null)
			security.check(this, permissionId);
	}

	@Override
	public void updateNotification(int notificationId, int languageId, String text)
	{
		performUpdateNotificationSecurityCheck();
		if (getNotifications() != null && getNotifications().getNotification(notificationId) != null)
			getNotifications().getNotification(notificationId).setText(languageId, text);
	}

	public void performUpdateNotificationSecurityCheck()
	{
		// Placeholder for Update Notification Check
	}

	@Override
	public void performGetNotificationSecurityCheck()
	{
		// Placeholder for Get Notification Check
	}

	@Override
	public boolean load(IDatabaseConnection databaseConnection)
	{

		// Load Config from Database
		Map<String, ConfigRecord> records = new HashMap<String, ConfigRecord>();
		try
		{

			List<ConfigRecord> recordList = databaseConnection.selectList(ConfigRecord.class, "where SerialVersionUID = %s order by sequence", getSerialVersionUID());
			if (recordList.size() == 0)
				return true;
			for (ConfigRecord record : recordList)
			{
				records.put(record.name, record);
			}
		}
		catch (SQLException e)
		{
			logger.error("Failed to load data", e);
			return false;
		}

		// Load Hierarchy
		loadObject("", this, records, databaseConnection);

		// Load Notifications too
		try
		{
			INotifications notifications = getNotifications();
			if (notifications != null)
				notifications.load(databaseConnection, getSerialVersionUID());
		}
		catch (SQLException e)
		{
			logger.error("Failed to load notifications", e);
		}

		// Load Children too
		Collection<IConfiguration> children = getConfigurations();
		if (children != null)
		{
			for (IConfiguration child : children)
			{
				if (!child.load(databaseConnection))
					return false;
			}
		}

		// Validate
		try
		{
			validate();
		}
		catch (Throwable e)
		{
			logger.error("Validation error", e);
			return false;
		}

		return true;
	}

	private void loadArray(String prefix, Object instance, FieldInfo arrayField, Map<String, ConfigRecord> records, IDatabaseConnection databaseConnection)
	{
		// Get the Array Length
		String fieldName = String.format("%s%s.ArrayLength", prefix, arrayField.getName());
		ConfigRecord record = records.get(fieldName);
		if (record == null)
			return;
		int arrayLength = ByteBuffer.wrap(record.value).getInt();

		// Create the Array
		Class<?> componentType = arrayField.getType().getComponentType();
		// boolean isString = componentType.equals(String.class);
		Object array = Array.newInstance(componentType, arrayLength);
		try
		{
			arrayField.set(instance, array);
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			logger.error("Failed to set field");
			return;
		}

		// Add Each Element
		for (int index = 0; index < arrayLength; index++)
		{
			try
			{
				String indexPrefix = String.format("%s%s[%d]", prefix, arrayField.getName(), index);
				Object element = null;

				record = records.get(indexPrefix);
				if (componentType.equals(String.class) && record != null)
					element = new String(record.value, "UTF-8");
				else if (componentType.equals(Integer.class) && record != null)
					element = ByteBuffer.wrap(record.value).getInt();
				else if (componentType.equals(Long.class) && record != null)
					element = ByteBuffer.wrap(record.value).getLong();
				else if (componentType.equals(Float.class) && record != null)
					element = ByteBuffer.wrap(record.value).getFloat();
				else if (componentType.equals(Double.class) && record != null)
					element = ByteBuffer.wrap(record.value).getDouble();
				else
				{
					element = componentType.newInstance();
					loadObject(indexPrefix, element, records, databaseConnection);
				}

				Array.set(array, index, element);
			}
			catch (InstantiationException | IllegalAccessException | UnsupportedEncodingException e)
			{
				logger.error("Failed to load object", e);
			}
		}

		return;

	}

	private void loadObject(String prefix, Object instance, Map<String, ConfigRecord> records, IDatabaseConnection databaseConnection)
	{
		// Delimit Prefix
		if (prefix != null && prefix.length() > 0)
			prefix = prefix + ".";

		// Get fields
		ClassInfo classInfo = ReflectionHelper.getClassInfo(instance.getClass());
		Map<String, FieldInfo> fields = classInfo.getFields();

		// Update Fields
		for (FieldInfo field : fields.values())
		{
			Class<?> type = field.getType();
			if (IConfigurable.class.isAssignableFrom(type))
			{
				IConfigurable param;
				try
				{
					param = (IConfigurable) field.get(this);
					Object newValue = param.load(databaseConnection, getSerialVersionUID());
					if (newValue != null)
						field.set(this, newValue);
				}
				catch (IllegalArgumentException | IllegalAccessException | SQLException e)
				{
					logger.error("Invalid field", e);
					continue;
				}
				continue;
			}
			else if (type.isArray())
			{
				if (type.getComponentType().isAnnotationPresent(Configurable.class) || type.getComponentType().equals(String.class) || type.getComponentType().equals(Integer.class))
					loadArray(prefix, instance, field, records, databaseConnection);
				continue;
			}
			else if (type.isAnnotationPresent(Configurable.class))
			{
				try
				{
					Object value = type.newInstance();
					loadObject(prefix + field.getName(), value, records, databaseConnection);
					field.set(instance, value);
				}
				catch (InstantiationException | IllegalAccessException e)
				{
					logger.error("Invalid annotation", e);
				}
				continue;
			}

			try
			{
				ConfigRecord record = records.get(prefix + field.getName());
				if (record != null)
					field.setBytes(instance, record.value);
			}
			catch (IllegalArgumentException | IllegalAccessException | UnsupportedEncodingException e)
			{
				logger.error("Invalid config record", e);
			}

		}
	}

	/**
	 * Return Help Text for help messages stored in XML file under class source path: help/tooltips.xml Format of help file: <helpcontent> <hints> <hint id="143">Helpful text explaining field</hint>
	 * </hints> </helpcontent>
	 */
	public String extractHelpHintText(int helpId)
	{
		String searchId = String.valueOf(helpId);
		ClassLoader CLDR = this.getClass().getClassLoader();
		try (InputStream is = CLDR.getResourceAsStream("help/tooltips.xml"))
		{
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader reader = factory.createXMLStreamReader(is);

			while (reader.hasNext())
			{
				switch (reader.next())
				{
					case XMLStreamConstants.START_ELEMENT:
						if ("hint".equals(reader.getLocalName()))
						{
							if (searchId.equals(reader.getAttributeValue("", "id")))
							{
								return reader.getElementText();
							}
						}
						break;
				}
			}
		}
		catch (IOException | XMLStreamException ex)
		{
		}
		return null;
	}

}
