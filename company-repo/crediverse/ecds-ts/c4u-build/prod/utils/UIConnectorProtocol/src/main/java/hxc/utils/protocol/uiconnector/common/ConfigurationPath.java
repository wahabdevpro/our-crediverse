package hxc.utils.protocol.uiconnector.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationPath implements Serializable
{
	private static final long serialVersionUID = 457489917122411809L;

	private String name;
	private long configSerialVersionUID = 0;

	private int parametersCount;
	private int methodsCount;
	private int notificationsCount;

	private List<ConfigurationPath> children = null;

	public ConfigurationPath()
	{
	}

	public ConfigurationPath(String name, long uid)
	{
		this.name = name;
		this.configSerialVersionUID = uid;
	}

	public void copyAllButChildren(ConfigurationPath cpOriginal)
	{
		this.name = cpOriginal.getName();
		this.configSerialVersionUID = cpOriginal.getConfigSerialVersionUID();
		this.parametersCount = cpOriginal.getParametersCount();
		this.methodsCount = cpOriginal.getMethodsCount();
		this.notificationsCount = cpOriginal.getNotificationsCount();
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
	 * @return the children
	 */
	public List<ConfigurationPath> getChildren()
	{
		return children;
	}

	/**
	 * @param children
	 *            the children to set
	 */
	public void setChildren(List<ConfigurationPath> children)
	{
		this.children = children;
	}

	public void addChild(ConfigurationPath path)
	{
		if (this.children == null)
		{
			children = new ArrayList<ConfigurationPath>();
		}
		children.add(path);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Name:%s P:%d M:%d N:%d\n", name, getParametersCount(), getMethodsCount(), getNotificationsCount()));

		if (children != null && children.size() > 0)
		{
			sb.append("Children:\n");
			for (ConfigurationPath cp : children)
			{
				sb.append("\t").append(cp.toString());
			}
		}

		return sb.toString();
	}

	/**
	 * @return the parametersCount
	 */
	public int getParametersCount()
	{
		return parametersCount;
	}

	/**
	 * @param parametersCount
	 *            the parametersCount to set
	 */
	public void setParametersCount(int parametersCount)
	{
		this.parametersCount = parametersCount;
	}

	/**
	 * @return the methodsCount
	 */
	public int getMethodsCount()
	{
		return methodsCount;
	}

	/**
	 * @param methodsCount
	 *            the methodsCount to set
	 */
	public void setMethodsCount(int methodsCount)
	{
		this.methodsCount = methodsCount;
	}

	/**
	 * @return the notificationsCount
	 */
	public int getNotificationsCount()
	{
		return notificationsCount;
	}

	/**
	 * @param notificationsCount
	 *            the notificationsCount to set
	 */
	public void setNotificationsCount(int notificationsCount)
	{
		this.notificationsCount = notificationsCount;
	}
}
