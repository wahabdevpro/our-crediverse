package hxc.utils.protocol.uiconnector.response;

import java.io.Serializable;

public class ConfigurableMethod implements Serializable
{

	private static final long serialVersionUID = 384824975626184191L;
	private String methodName;
	private String description;

	public ConfigurableMethod()
	{
	}

	public ConfigurableMethod(String methodName, String description)
	{
		this.methodName = methodName;
		this.description = description;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName()
	{
		return methodName;
	}

	/**
	 * @param methodName
	 *            the methodName to set
	 */
	public void setMethodName(String methodName)
	{
		this.methodName = methodName;
	}

	/**
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

}
