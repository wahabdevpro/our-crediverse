package hxc.utils.protocol.uiconnector.vas;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class VasCommandsRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 4382915817575189476L;
	private long configurationUID;

	public VasCommandsRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public long getConfigurationUID()
	{
		return configurationUID;
	}

	public void setConfigurationUID(long configurationUID)
	{
		this.configurationUID = configurationUID;
	}

}
