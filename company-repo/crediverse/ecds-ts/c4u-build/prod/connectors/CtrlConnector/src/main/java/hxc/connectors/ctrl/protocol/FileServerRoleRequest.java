package hxc.connectors.ctrl.protocol;

import hxc.connectors.file.FileType;

public class FileServerRoleRequest extends ServerRequest
{

	private static final long serialVersionUID = 1781393380363588485L;
	private String inputDirectory;
	private FileType fileType;
	private boolean hasIncumbent;
	private String requestingHost;
	private int hopsToLive = 100;

	public void setInputDirectory(String inputDirectory)
	{
		this.inputDirectory = inputDirectory;
	}

	public String getInputDirectory()
	{
		return inputDirectory;
	}

	public void setFileType(FileType fileType)
	{
		this.fileType = fileType;
	}

	public FileType getFileType()
	{
		return fileType;
	}

	public void setHasIncumbent(boolean hasIncumbent)
	{
		this.hasIncumbent = hasIncumbent;
	}

	public boolean hasIncumbent()
	{
		return hasIncumbent;
	}

	public void setRequestingHost(String requestingHost)
	{
		this.requestingHost = requestingHost;
	}

	public String getRequestingHost()
	{
		return requestingHost;
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
