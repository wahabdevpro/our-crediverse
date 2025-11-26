package hxc.connectors.ctrl.protocol;

public class ElectionRequest extends ServerRequest
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 1675606916609648708L;
	private String serverRole;
	private String requestingHost;
	private String candidateHost = null;
	private int candidateRating = 0;
	private int hopsToLive = 1000;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getServerRole()
	{
		return serverRole;
	}

	public void setServerRole(String serverRole)
	{
		this.serverRole = serverRole;
	}

	public String getRequestingHost()
	{
		return requestingHost;
	}

	public void setRequestingHost(String requestingHost)
	{
		this.requestingHost = requestingHost;
	}

	public String getCandidateHost()
	{
		return candidateHost;
	}

	public void setCandidateHost(String canditateHost)
	{
		this.candidateHost = canditateHost;
	}

	public int getCandidateRating()
	{
		return candidateRating;
	}

	public void setCandidateRating(int candidateRating)
	{
		this.candidateRating = candidateRating;
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
