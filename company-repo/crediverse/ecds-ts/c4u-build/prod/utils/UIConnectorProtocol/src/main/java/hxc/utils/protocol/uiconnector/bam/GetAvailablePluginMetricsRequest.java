package hxc.utils.protocol.uiconnector.bam;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class GetAvailablePluginMetricsRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 5278695975029399704L;

	public GetAvailablePluginMetricsRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}
}
