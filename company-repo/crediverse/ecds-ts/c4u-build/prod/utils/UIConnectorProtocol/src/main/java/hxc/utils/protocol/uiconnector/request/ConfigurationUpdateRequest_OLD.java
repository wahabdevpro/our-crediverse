package hxc.utils.protocol.uiconnector.request;

import hxc.utils.protocol.uiconnector.common.Configurable;

public class ConfigurationUpdateRequest_OLD extends UiBaseRequest
{

	private static final long serialVersionUID = 991816404796512289L;
	private Configurable config;

	public ConfigurationUpdateRequest_OLD(String userId, String sessionId)
	{
		super(userId, sessionId);
		setRequestCode(UiRequestCode.UPDATE_SPECIFIC_CONFIGURATION);
	}

	/**
	 * @return the config
	 */
	public Configurable getConfig()
	{
		return config;
	}

	/**
	 * @param config
	 *            the config to set
	 */
	public void setConfig(Configurable config)
	{
		this.config = config;
	}

}
