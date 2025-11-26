package hxc.utils.protocol.uiconnector.common;

import java.io.Serializable;

public class ConfigNotification implements Serializable
{
	private static final long serialVersionUID = -5968523629784891248L;
	private int notificationId;
	private String description;
	private String[] text;

	public ConfigNotification()
	{
	}

	public ConfigNotification(int notificationId)
	{
		this.notificationId = notificationId;
	}

	public ConfigNotification(int notificationId, String[] text)
	{
		this.notificationId = notificationId;
		this.text = new String[text.length];
		System.arraycopy(text, 0, this.text, 0, text.length);
	}

	/**
	 * @return the notificationId
	 */
	public int getNotificationId()
	{
		return notificationId;
	}

	/**
	 * @param notificationId
	 *            the notificationId to set
	 */
	public void setNotificationId(int notificationId)
	{
		this.notificationId = notificationId;
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

	/**
	 * @return the text
	 */
	public String[] getText()
	{
		return text;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setText(String[] text)
	{
		this.text = text;
	}

}
