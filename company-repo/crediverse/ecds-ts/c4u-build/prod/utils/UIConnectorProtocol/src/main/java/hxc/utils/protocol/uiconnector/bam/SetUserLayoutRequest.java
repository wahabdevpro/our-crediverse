package hxc.utils.protocol.uiconnector.bam;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class SetUserLayoutRequest extends UiBaseRequest
{

	private static final long serialVersionUID = -82678793756117564L;
	private String layout;

	public SetUserLayoutRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public void setLayout(String layout)
	{
		this.layout = layout;
	}

	public String getLayout()
	{
		return layout;
	}
}
