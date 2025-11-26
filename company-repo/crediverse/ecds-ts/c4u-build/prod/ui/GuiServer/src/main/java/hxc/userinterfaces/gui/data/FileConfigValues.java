package hxc.userinterfaces.gui.data;

public class FileConfigValues
{
	private String[] fileTypes;
	private String[] fileProcessorTypes;

	public void importType(String fieldName, String[] posibleValues)
	{
		try
		{
			if (fieldName.equalsIgnoreCase("fileType"))
			{
				fileTypes = new String[posibleValues.length];
				System.arraycopy(posibleValues, 0, fileTypes, 0, posibleValues.length);
			}
			else if (fieldName.equalsIgnoreCase("fileProcessorType"))
			{
				fileProcessorTypes = new String[posibleValues.length];
				System.arraycopy(posibleValues, 0, fileProcessorTypes, 0, posibleValues.length);
			}
		}
		catch (Exception e)
		{
		}
	}

	public String[] getFileTypes()
	{
		return fileTypes;
	}

	public void setFileTypes(String[] fileTypes)
	{
		this.fileTypes = fileTypes;
	}

	public String[] getFileProcessorTypes()
	{
		return fileProcessorTypes;
	}

	public void setFileProcessorTypes(String[] fileProcessorTypes)
	{
		this.fileProcessorTypes = fileProcessorTypes;
	}

}
