package hxc.utils.protocol.uiconnector.request;

public class RevertServiceRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 9001147280831813601L;
	private String serviceId;

	public RevertServiceRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public void setServiceID(String serviceId)
	{
		this.serviceId = serviceId;
	}

	public String getServiceID()
	{
		return serviceId;
	}
}
