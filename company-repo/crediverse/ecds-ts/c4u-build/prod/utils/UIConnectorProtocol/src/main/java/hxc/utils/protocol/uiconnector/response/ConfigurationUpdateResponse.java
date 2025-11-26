package hxc.utils.protocol.uiconnector.response;

import hxc.utils.protocol.uiconnector.common.Configurable;

public class ConfigurationUpdateResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -2060315004532061911L;
	private Configurable config;

	public ConfigurationUpdateResponse()
	{
		setResponseCode(UiResponseCode.UPDATE_SPECIFIC_CONFIGURATION);
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
