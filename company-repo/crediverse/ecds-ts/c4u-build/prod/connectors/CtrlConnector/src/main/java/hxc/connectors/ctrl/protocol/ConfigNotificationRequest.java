package hxc.connectors.ctrl.protocol;

public class ConfigNotificationRequest extends ServerRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 5039493645772393486L;
	private long configSerialVersionUID;
	private String requestingHost;
	private int hopsToLive = 1000;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public long getConfigSerialVersionUID()
	{
		return configSerialVersionUID;
	}

	public void setConfigSerialVersionUID(long configSerialVersionUID)
	{
		this.configSerialVersionUID = configSerialVersionUID;
	}

	public String getRequestingHost()
	{
		return requestingHost;
	}

	public void setRequestingHost(String requestingHost)
	{
		this.requestingHost = requestingHost;
	}

	public int getHopsToLive()
	{
		return hopsToLive;
	}

	public void setHopsToLive(int hopsToLive)
	{
		this.hopsToLive = hopsToLive;
	}

}
