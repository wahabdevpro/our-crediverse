package hxc.connectors.ctrl.protocol;

import java.io.File;

public class DistributeFileNotificationRequest extends ServerRequest
{

	private static final long serialVersionUID = 7819958067016207197L;
	private File file;
	private String filename;
	private String inDir;
	private String requestingHost;
	private String copyCommand;
	private int hopsToLive = 100;

	public void setFile(File file)
	{
		this.file = file;
		this.filename = file.getName();
		this.inDir = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf('/') + 1);
	}

	public File getFile()
	{
		return file;
	}

	public String getFilename()
	{
		return filename;
	}

	public String getInDir()
	{
		return inDir;
	}

	public void setRequestingHost(String requestingHost)
	{
		this.requestingHost = requestingHost;
	}

	public String getRequestingHost()
	{
		return requestingHost;
	}

	public void setCopyCommand(String copyCommand)
	{
		this.copyCommand = String.format(copyCommand, requestingHost, inDir, filename);
	}

	public String getCopyCommand()
	{
		return copyCommand;
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
