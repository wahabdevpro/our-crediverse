package hxc.utils.protocol.uiconnector.common;

import java.io.Serializable;

public class ConfigurationInfo implements Serializable
{

	private static final long serialVersionUID = 3587055206175016387L;
	private int parameters;
	private int methods;
	private int notifications;

	public ConfigurationInfo()
	{
	}

	public ConfigurationInfo(int parameters, int methods, int notifications)
	{
		this.parameters = parameters;
		this.methods = methods;
		this.notifications = notifications;
	}

	/**
	 * @return the parameters
	 */
	public int getParameters()
	{
		return parameters;
	}

	/**
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(int parameters)
	{
		this.parameters = parameters;
	}

	/**
	 * @return the methods
	 */
	public int getMethods()
	{
		return methods;
	}

	/**
	 * @param methods
	 *            the methods to set
	 */
	public void setMethods(int methods)
	{
		this.methods = methods;
	}

	/**
	 * @return the notifications
	 */
	public int getNotifications()
	{
		return notifications;
	}

	/**
	 * @param notifications
	 *            the notifications to set
	 */
	public void setNotifications(int notifications)
	{
		this.notifications = notifications;
	}

}
