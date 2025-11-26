package hxc.utils.protocol.uiconnector.request;

import hxc.utils.protocol.uiconnector.common.ConfigNotification;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;

public class ConfigurationUpdateRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 991816404796512289L;

	// IConfigurable Data
	private String path; // Beginning is important
	private String name; // This is the name of the configuration being updated
	private int version;
	private long configurableSerialVersionUID; // Very important

	// Configurable params
	private IConfigurableParam[] params; // This depends on request / response

	// Configurable norifications
	private ConfigNotification[] notifications;
	private boolean saveToDB = true; // By default save data to database (if false hold back on the save)

	public ConfigurationUpdateRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
		setRequestCode(UiRequestCode.UPDATE_SPECIFIC_CONFIGURATION);
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
	 * @return the configurableSerialVersionUID
	 */
	public long getConfigurableSerialVersionUID()
	{
		return configurableSerialVersionUID;
	}

	/**
	 * @param configurableSerialVersionUID
	 *            the configurableSerialVersionUID to set
	 */
	public void setConfigurableSerialVersionUID(long configurableSerialVersionUID)
	{
		this.configurableSerialVersionUID = configurableSerialVersionUID;
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
	 * @return the notifications
	 */
	public ConfigNotification[] getNotifications()
	{
		return notifications;
	}

	/**
	 * @param notifications
	 *            the notifications to set
	 */
	public void setNotifications(ConfigNotification[] notifications)
	{
		this.notifications = notifications;
	}

	/**
	 * @return the saveToDB
	 */
	public boolean isSaveToDB()
	{
		return saveToDB;
	}

	/**
	 * @param saveToDB
	 *            the saveToDB to set
	 */
	public void setSaveToDB(boolean saveToDB)
	{
		this.saveToDB = saveToDB;
	}

}
