package hxc.utils.protocol.uiconnector.request;

public class SendSMSRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 4876231992278218361L;
	private String toMSISDN;
	private String fromMSISDN;
	private String message;

	public SendSMSRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public void setToMSISDN(String toMSISDN)
	{
		this.toMSISDN = toMSISDN;
	}

	public String getToMSISDN()
	{
		return toMSISDN;
	}

	public void setFromMSISDN(String fromMSISDN)
	{
		this.fromMSISDN = fromMSISDN;
	}

	public String getFromMSISDN()
	{
		return fromMSISDN;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getMessage()
	{
		return message;
	}
}
