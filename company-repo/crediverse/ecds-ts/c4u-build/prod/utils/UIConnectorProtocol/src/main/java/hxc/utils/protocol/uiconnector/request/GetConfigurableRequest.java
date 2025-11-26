package hxc.utils.protocol.uiconnector.request;

public class GetConfigurableRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 6098899500788889384L;

	private long configurableSerialVersionID;
	private String name;
	private String path;

	public GetConfigurableRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public void setConfigurableSerialVersionID(long configurableSerialVersionID)
	{
		this.configurableSerialVersionID = configurableSerialVersionID;
	}

	public long getConfigurableSerialVersionID()
	{
		return this.configurableSerialVersionID;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getPath()
	{
		return this.path;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}
}
