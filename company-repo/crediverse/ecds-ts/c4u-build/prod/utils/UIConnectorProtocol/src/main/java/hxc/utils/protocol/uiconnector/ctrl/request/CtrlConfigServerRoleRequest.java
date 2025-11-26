package hxc.utils.protocol.uiconnector.ctrl.request;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class CtrlConfigServerRoleRequest extends UiBaseRequest
{

	private static final long serialVersionUID = -1924991457678879220L;

	public CtrlConfigServerRoleRequest()
	{
	}

	public CtrlConfigServerRoleRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}
}
