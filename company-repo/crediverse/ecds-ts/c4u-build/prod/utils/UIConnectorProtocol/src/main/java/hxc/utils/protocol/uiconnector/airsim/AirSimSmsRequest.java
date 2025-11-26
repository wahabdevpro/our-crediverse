package hxc.utils.protocol.uiconnector.airsim;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class AirSimSmsRequest extends UiBaseRequest
{
	private static final long serialVersionUID = -8721704518026459428L;

	private String from;
	private String to;
	private String text;
	
	public AirSimSmsRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public String getFrom()
	{
		return from;
	}

	public void setFrom(String from)
	{
		this.from = from;
	}

	public String getTo()
	{
		return to;
	}

	public void setTo(String to)
	{
		this.to = to;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

}
