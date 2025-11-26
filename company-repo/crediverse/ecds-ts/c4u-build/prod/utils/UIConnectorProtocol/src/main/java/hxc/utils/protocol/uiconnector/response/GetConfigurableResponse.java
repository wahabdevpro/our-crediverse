package hxc.utils.protocol.uiconnector.response;

import hxc.utils.protocol.uiconnector.common.Configurable;

public class GetConfigurableResponse extends UiBaseResponse
{

	private static final long serialVersionUID = 8181482140226158543L;

	private Configurable config;

	public GetConfigurableResponse()
	{
	}

	public void setConfig(Configurable config)
	{
		this.config = config;
	}

	public Configurable getConfig()
	{
		return this.config;
	}

	public void sort()
	{
		config.sortAll();
	}
}
