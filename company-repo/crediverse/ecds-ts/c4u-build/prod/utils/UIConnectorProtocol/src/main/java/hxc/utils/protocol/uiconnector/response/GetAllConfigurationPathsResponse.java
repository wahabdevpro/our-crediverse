package hxc.utils.protocol.uiconnector.response;

import hxc.utils.protocol.uiconnector.common.ConfigurationPath;

public class GetAllConfigurationPathsResponse extends UiBaseResponse
{
	private static final long serialVersionUID = -5369905969552001806L;
	private ConfigurationPath pathTree;

	public GetAllConfigurationPathsResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	/**
	 * @return the pathTree
	 */
	public ConfigurationPath getPathTree()
	{
		return pathTree;
	}

	/**
	 * @param pathTree
	 *            the pathTree to set
	 */
	public void setPathTree(ConfigurationPath pathTree)
	{
		this.pathTree = pathTree;
	}

}
