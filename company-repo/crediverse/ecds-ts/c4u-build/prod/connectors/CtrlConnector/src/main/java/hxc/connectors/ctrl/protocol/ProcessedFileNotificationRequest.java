package hxc.connectors.ctrl.protocol;

import java.io.File;

public class ProcessedFileNotificationRequest extends ServerRequest
{

	private static final long serialVersionUID = -8989395385416923154L;
	private File file;
	private String filename;
	private String outputDir;
	private String requestingHost;
	private int hopsToLive = 100;

	public void setFile(File file)
	{
		this.file = file;
		this.filename = file.getName();
	}

	public File getFile()
	{
		return file;
	}

	public String getFilename()
	{
		return filename;
	}

	public void setOutputDir(String outputDir)
	{
		this.outputDir = outputDir;
	}

	public String getOutputDir()
	{
		return outputDir;
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
