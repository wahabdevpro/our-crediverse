package hxc.utils.protocol.uiconnector.ctrl.request;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class CtrlConfigurationRequest extends UiBaseRequest
{

	private static final long serialVersionUID = -8046376087526144233L;

	public CtrlConfigurationRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

}
