package hxc.connectors.ctrl.protocol;

public class ElectionResultRequest extends ServerRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 5045995296329497739L;
	private String requestingHost;
	private String serverRole;
	private String electedHost = null;
	private int hopsToLive = 1000;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getRequestingHost()
	{
		return requestingHost;
	}

	public void setRequestingHost(String requestingHost)
	{
		this.requestingHost = requestingHost;
	}

	public String getServerRole()
	{
		return serverRole;
	}

	public void setServerRole(String serverRole)
	{
		this.serverRole = serverRole;
	}

	public String getElectedHost()
	{
		return electedHost;
	}

	public void setElectedHost(String electedHost)
	{
		this.electedHost = electedHost;
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
