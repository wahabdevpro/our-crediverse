package hxc.utils.protocol.uiconnector.response;

import java.util.Collections;
import java.util.List;

import hxc.utils.protocol.uiconnector.common.Configurable;

public class GetAllConfigurablesResponse extends UiBaseResponse
{

	private static final long serialVersionUID = 803995177058287307L;

	private List<Configurable> configs;

	public GetAllConfigurablesResponse()
	{
		setResponseCode(UiResponseCode.GET_CONFIGURABLES);
	}

	/**
	 * @return the configs
	 */
	public List<Configurable> getConfigs()
	{
		return configs;
	}

	/**
	 * @param configs
	 *            the configs to set
	 */
	public void setConfigs(List<Configurable> configs)
	{
		this.configs = configs;
	}

	public void sort()
	{
		Collections.sort(configs, Configurable.ConfigNameComparator);
	}
}
