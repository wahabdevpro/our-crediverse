package hxc.utils.protocol.uiconnector.bam;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class GetUserLayoutResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -91241925981742574L;
	private String layout;

	public GetUserLayoutResponse(String userId, String sessionId)
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
