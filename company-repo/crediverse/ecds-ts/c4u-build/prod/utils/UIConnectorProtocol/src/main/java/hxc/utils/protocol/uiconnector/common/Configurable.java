package hxc.utils.protocol.uiconnector.common;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import hxc.utils.protocol.uiconnector.response.ConfigurableMethod;

public class Configurable implements Serializable, Comparable<Configurable>
{

	private static final long serialVersionUID = -308862395525993341L;

	// IConfigurable Data
	private String name;
	private String path;
	private int version;
	private long configSerialVersionUID;

	// Configurable params
	private IConfigurableParam[] params; // This depends on request / response
	private ConfigurableMethod[] methods;
	private List<Configurable> configurable;

	// New notification data
	private List<ConfigNotification> notifications;
	private List<NotificationVariable> variables;

	public Configurable()
	{
	}

	public Configurable(String name, String path)
	{
		this.name = name;
		this.path = path;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the path
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path)
	{
		this.path = path;
	}

	/**
	 * @return the version
	 */
	public int getVersion()
	{
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(int version)
	{
		this.version = version;
	}

	/**
	 * @return the params
	 */
	public IConfigurableParam[] getParams()
	{
		return params;
	}

	/**
	 * @param params
	 *            the params to set
	 */
	public void setParams(IConfigurableParam[] params)
	{
		this.params = params;
	}

	/**
	 * @return the configurable
	 */
	public List<Configurable> getConfigurable()
	{
		return configurable;
	}

	/**
	 * @param configurable
	 *            the configurable to set
	 */
	public void setConfigurable(List<Configurable> configurable)
	{
		this.configurable = configurable;
	}

	/**
	 * @return the methods
	 */
	public ConfigurableMethod[] getMethods()
	{
		return methods;
	}

	/**
	 * @param methods
	 *            the methods to set
	 */
	public void setMethods(ConfigurableMethod[] methods)
	{
		this.methods = methods;
	}

	/**
	 * @return the configSerialVersionUID
	 */
	public long getConfigSerialVersionUID()
	{
		return configSerialVersionUID;
	}

	/**
	 * @param configSerialVersionUID
	 *            the configSerialVersionUID to set
	 */
	public void setConfigSerialVersionUID(long configSerialVersionUID)
	{
		this.configSerialVersionUID = configSerialVersionUID;
	}

	/**
	 * @return the notifications
	 */
	public List<ConfigNotification> getNotifications()
	{
		return notifications;
	}

	/**
	 * @param notifications
	 *            the notifications to set
	 */
	public void setNotifications(List<ConfigNotification> notifications)
	{
		this.notifications = notifications;
	}

	/**
	 * @return the variables
	 */
	public List<NotificationVariable> getVariables()
	{
		return variables;
	}

	/**
	 * @param variables
	 *            the variables to set
	 */
	public void setVariables(List<NotificationVariable> variables)
	{
		this.variables = variables;
	}

	public void sortAll()
	{
		if (configurable != null)
		{
			Collections.sort(configurable);
		}
		if (params != null)
		{

			Arrays.sort(params);
		}
	}

	@Override
	public int compareTo(Configurable other)
	{
		return this.name.compareTo(other.name);
	}

	public static Comparator<Configurable> ConfigNameComparator = new Comparator<Configurable>()
	{

		@Override
		public int compare(Configurable o1, Configurable o2)
		{
			return o1.compareTo(o2);
		}
	};

}
