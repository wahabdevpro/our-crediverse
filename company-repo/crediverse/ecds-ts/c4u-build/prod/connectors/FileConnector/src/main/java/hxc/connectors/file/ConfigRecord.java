package hxc.connectors.file;

import java.util.Date;

import hxc.configuration.Config;
import hxc.configuration.ValidationException;
import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

////////////////////////////////////////////////////////////////////////////////////////
//
// Persistence Class
//
///////////////////////////////////

@Table(name = "FC_Config")
public class ConfigRecord
{
	@Column(primaryKey = true)
	public long SerialVersionUID = 4513114146612190707L;

	@Column(primaryKey = true)
	public int sequence;

	@Column(maxLength = 64)
	private String filenameFilter = "*type*.csv";

	@Column(maxLength = 255)
	private String inputDirectory = "/var/opt/cs/c4u/input";

	@Column(maxLength = 255)
	private String outputDirectory = "/var/opt/cs/c4u/output";

	@Column(maxLength = 255, defaultValue = "scp root@%1$s:%2$s/%3$s %2$s")
	private String copyCommand = "scp root@%1$s:%2$s/%3$s %2$s"; /* Available Params %1$s: srcComp, %2$s: inDir, %3$s: filename, %4$s: tgtComp */

	private FileProcessorType fileProcessorType;

	@Column(maxLength = 64)
	private FileType fileType; /* Full Class Name, derives from FileDTO */

	@Column(maxLength = 32)
	private String serverRole = "hostname";

	@Column(nullable = true)
	private Date processStartTimeOfDay;

	@Column(nullable = true)
	private Date processEndTimeOfDay;

	private boolean strictlySequential;

	public ConfigRecord()
	{
	}

	public ConfigRecord(int sequence, String filenameFilter, String inputDirectory, String outputDirectory, FileProcessorType fileProcessorType, FileType fileType, String serverRole,
			boolean strictlySequential)
	{
		this.sequence = sequence;
		this.filenameFilter = filenameFilter;
		this.inputDirectory = inputDirectory;
		this.outputDirectory = outputDirectory;
		this.fileProcessorType = fileProcessorType;
		this.fileType = fileType;
		this.serverRole = serverRole;
		this.strictlySequential = strictlySequential;
	}

	/**
	 * @return the sequence
	 */
	public int getSequence()
	{
		return sequence;
	}

	/**
	 * @param sequence
	 *            the sequence to set
	 */
	public void setSequence(int sequence)
	{
		this.sequence = sequence;
	}

	/**
	 * @return the filenameFilter
	 */
	@Config(description = "File Name Filter", defaultValue = "*type*.csv", link = "FileType")
	public String getFilenameFilter()
	{
		return filenameFilter;
	}

	/**
	 * @param filenameFilter
	 *            the filenameFilter to set
	 * @throws ValidationException
	 */
	public void setFilenameFilter(String filenameFilter) throws ValidationException
	{
		this.filenameFilter = filenameFilter;
	}

	/**
	 * @return the inputDirectory
	 */
	@Config(description = "Input Directory", defaultValue = "/var/opt/cs/c4u/input", link = "FileType")
	public String getInputDirectory()
	{
		return inputDirectory;
	}

	/**
	 * @param inputDirectory
	 *            the inputDirectory to set
	 * @throws ValidationException
	 */
	public void setInputDirectory(String inputDirectory) throws ValidationException
	{
		this.inputDirectory = inputDirectory;
	}

	/**
	 * @return the outputDirectory
	 */
	@Config(description = "Output Directory", defaultValue = "/var/opt/cs/c4u/output", link = "FileType")
	public String getOutputDirectory()
	{
		return outputDirectory;
	}

	/**
	 * @param outputDirectory
	 *            the outputDirectory to set
	 * @throws ValidationException
	 */
	public void setOutputDirectory(String outputDirectory) throws ValidationException
	{
		this.outputDirectory = outputDirectory;
	}

	/**
	 * @return the copyCommand
	 */
	@Config(description = "Copy Command", defaultValue = "scp root@%1$s:%2$s/%3$s %2$s")
	public String getCopyCommand()
	{
		return copyCommand;
	}

	/**
	 * @param copyCommand
	 *            the copyCommand to set
	 */
	public void setCopyCommand(String copyCommand)
	{
		this.copyCommand = copyCommand;
	}

	/**
	 * @return the fileProcessorType
	 */
	public FileProcessorType getFileProcessorType()
	{
		return fileProcessorType;
	}

	/**
	 * @param fileProcessorType
	 *            the fileProcessorType to set
	 */
	public void setFileProcessorType(FileProcessorType fileProcessorType)
	{
		this.fileProcessorType = fileProcessorType;
	}

	/**
	 * @return the fileType
	 */
	public FileType getFileType()
	{
		return fileType;
	}

	/**
	 * @param fileType
	 *            the fileType to set
	 * @throws ValidationException
	 */
	public void setFileType(FileType fileType) throws ValidationException
	{
		this.fileType = fileType;
		// try
		// {
		// if (this.fileType != null)
		// {
		// String fType = this.fileType.toString().toLowerCase();
		// String name = fType.substring(0, fType.lastIndexOf("filev"));
		//
		// if (inputDirectory.equalsIgnoreCase("/var/opt/cs/c4u/input"))
		// setInputDirectory("/var/opt/cs/c4u/input" + "/" + name);
		//
		// if (outputDirectory.equalsIgnoreCase("/var/opt/cs/c4u/output"))
		// setOutputDirectory("/var/opt/cs/c4u/output" + "/" + name);
		//
		// if (filenameFilter.equalsIgnoreCase("*type*.csv"))
		// setFilenameFilter("*" + name + fType.substring(fType.lastIndexOf("v")) + "*.csv");
		//
		// }
		// }
		// catch (Exception e)
		// {
		// throw new ValidationException("FileType failed [%s]", e.getMessage());
		// }

	}

	/**
	 * @return the serverRole
	 */
	@Config(description = "Server Role", defaultValue = "hostname")
	public String getServerRole()
	{
		return serverRole;
	}

	/**
	 * @param serverRole
	 *            the serverRole to set
	 */
	public void setServerRole(String serverRole)
	{
		this.serverRole = serverRole;
	}

	/**
	 * @return the startTimeOfDay
	 */
	public Date getProcessStartTimeOfDay()
	{
		return processStartTimeOfDay;
	}

	/**
	 * @param startTimeOfDay
	 *            the startTimeOfDay to set
	 */
	public void setProcessStartTimeOfDay(Date processStartTimeOfDay)
	{
		this.processStartTimeOfDay = processStartTimeOfDay;
	}

	/**
	 * @return the endTimeOfDay
	 */
	public Date getProcessEndTimeOfDay()
	{
		return processEndTimeOfDay;
	}

	/**
	 * @param endTimeOfDay
	 *            the endTimeOfDay to set
	 */
	public void setProcessEndTimeOfDay(Date processEndTimeOfDay)
	{
		this.processEndTimeOfDay = processEndTimeOfDay;
	}

	/**
	 * @return the strictlySequential
	 */
	public boolean isStrictlySequential()
	{
		return strictlySequential;
	}

	/**
	 * @param strictlySequential
	 *            the strictlySequential to set
	 */
	public void setStrictlySequential(boolean strictlySequential)
	{
		this.strictlySequential = strictlySequential;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof ConfigRecord))
			return false;

		ConfigRecord record = (ConfigRecord) obj;
		if (this.SerialVersionUID != record.SerialVersionUID || this.fileType != record.fileType || !this.inputDirectory.equals(record.inputDirectory))
			return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		return (int) this.SerialVersionUID;
	}

	@Override
	public String toString()
	{
		return String.format("File Type: %s; Filename Filter: %s", fileType.toString(), filenameFilter);
	}
}
