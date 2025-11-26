package hxc.utils.protocol.uiconnector.airsim;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class AirSimUssdResponse extends UiBaseResponse
{
	private static final long serialVersionUID = -2955211162105332856L;
	private String text;
	private boolean last;
	
	public AirSimUssdResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public boolean isLast()
	{
		return last;
	}

	public void setLast(boolean last)
	{
		this.last = last;
	}

}
