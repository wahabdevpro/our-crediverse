package hxc.userinterfaces.gui.data;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileConnectorConfiguration
{
	private int sequence; // Unique per item
	private String fileType;
	private String serverRole;

	private String filenameFilter;
	private String fileProcessorType;

	private String inputDirectory;
	private String outputDirectory;

	private String copyCommand = "scp root@%1$s:%2$s/%3$s %2$s"; // Note that this comes from the ConfigRecord Class / Default

	private String processStartTimeOfDay; // In the form HH:mm
	private String processEndTimeOfDay; // In the form HH:mm

	private boolean strictlySequential;

	public FileConnectorConfiguration()
	{
	}

	// public static SimpleDateFormat fromDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	public static SimpleDateFormat convertDateFormat = new SimpleDateFormat("HH:mm");

	public void importField(String fieldName, Object value, boolean fromBrowser)
	{
		String name = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);

		try
		{
			if (name.equals("strictlySequential"))
				try
				{
					strictlySequential = (fromBrowser) ? true : Boolean.valueOf(value.toString());
				}
				catch (Exception e)
				{

				}
			else
			{
				Field field = this.getClass().getDeclaredField(name);
				String sValue = (value == null) ? "" : value.toString();
				if (value != null)
				{
					if (value.getClass().getName().equals(Date.class.getName()))
					{
						sValue = convertDateFormat.format(value);
					}
				}

				field.set(this, sValue);
			}
		}
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
		{
		}
	}

	public String getFileType()
	{
		return fileType;
	}

	public void setFileType(String fileType)
	{
		this.fileType = fileType;
	}

	public String getServerRole()
	{
		return serverRole;
	}

	public void setServerRole(String serverRole)
	{
		this.serverRole = serverRole;
	}

	public String getFilenameFilter()
	{
		return filenameFilter;
	}

	public void setFilenameFilter(String filenameFilter)
	{
		this.filenameFilter = filenameFilter;
	}

	public String getFileProcessorType()
	{
		return fileProcessorType;
	}

	public void setFileProcessorType(String fileProcessorType)
	{
		this.fileProcessorType = fileProcessorType;
	}

	public String getInputDirectory()
	{
		return inputDirectory;
	}

	public void setInputDirectory(String inputDirectory)
	{
		this.inputDirectory = inputDirectory;
	}

	public String getOutputDirectory()
	{
		return outputDirectory;
	}

	public void setOutputDirectory(String outputDirectory)
	{
		this.outputDirectory = outputDirectory;
	}

	public String getCopyCommand()
	{
		return copyCommand;
	}

	public void setCopyCommand(String copyCommand)
	{
		this.copyCommand = copyCommand;
	}

	public String getProcessStartTimeOfDay()
	{
		return processStartTimeOfDay;
	}

	public void setProcessStartTimeOfDay(String processStartTimeOfDay)
	{
		this.processStartTimeOfDay = processStartTimeOfDay;
	}

	public String getProcessEndTimeOfDay()
	{
		return processEndTimeOfDay;
	}

	public void setProcessEndTimeOfDay(String processEndTimeOfDay)
	{
		this.processEndTimeOfDay = processEndTimeOfDay;
	}

	public boolean isStrictlySequential()
	{
		return strictlySequential;
	}

	public void setStrictlySequential(boolean strictlySequential)
	{
		this.strictlySequential = strictlySequential;
	}

	public int getSequence()
	{
		return sequence;
	}

	public void setSequence(int sequence)
	{
		this.sequence = sequence;
	}

}
