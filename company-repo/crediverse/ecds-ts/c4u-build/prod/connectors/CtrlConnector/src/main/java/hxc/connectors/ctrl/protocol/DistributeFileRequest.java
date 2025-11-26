package hxc.connectors.ctrl.protocol;

public class DistributeFileRequest extends ServerRequest
{

	private static final long serialVersionUID = 4831924178508008538L;
	private String directory;
	private String filename;
	private String requestingHost;
	private int port;
	private int hopsToLive;

	public void setDirectory(String directory)
	{
		this.directory = directory;
	}

	public String getDirectory()
	{
		return directory;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public String getFilename()
	{
		return filename;
	}

	public void setRequestingHost(String requestingHost)
	{
		this.requestingHost = requestingHost;
	}

	public String getRequestingHost()
	{
		return requestingHost;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public int getPort()
	{
		return port;
	}

	public void setHopsToLive(int hopsToLive)
	{
		this.hopsToLive = hopsToLive;
	}

	public int getHopsToLive()
	{
		return hopsToLive;
	}

}
