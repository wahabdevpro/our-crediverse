package hxc.ecds.protocol.rest.config;

import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class BatchConfig implements IConfiguration
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 3580923677429449168L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int version;
	protected String batchArchiveFolder = "/var/opt/cs/ecds/batch";
	private int batchEntriesRetentionDays = 366;
	private int batchDownloadChunkSize = 100000;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@Override
	public long uid()
	{
		return serialVersionUID;
	}

	public String getBatchArchiveFolder()
	{
		return batchArchiveFolder;
	}

	public BatchConfig setBatchArchiveFolder(String batchArchiveFolder)
	{
		this.batchArchiveFolder = batchArchiveFolder;
		return this;
	}

	public int getBatchEntriesRetentionDays()
	{
		return batchEntriesRetentionDays;
	}

	public BatchConfig setBatchEntriesRetentionDays(int batchEntriesRetentionDays)
	{
		this.batchEntriesRetentionDays = batchEntriesRetentionDays;
		return this;
	}

	public int getBatchDownloadChunkSize()
	{
		return batchDownloadChunkSize;
	}

	public BatchConfig setBatchDownloadChunkSize(int batchDownloadChunkSize)
	{
		this.batchDownloadChunkSize = batchDownloadChunkSize;
		return this;
	}

	@Override
	public int getVersion()
	{
		return version;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Post-Load fix up
	//
	// /////////////////////////////////
	@Override
	public void onPostLoad()
	{
		BatchConfig template = new BatchConfig();

		if (batchEntriesRetentionDays == 0)
			batchEntriesRetentionDays = template.batchEntriesRetentionDays;

		if (batchDownloadChunkSize == 0)
			batchDownloadChunkSize = template.batchDownloadChunkSize;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator() //
				.notLess("batchEntriesRetentionDays", batchEntriesRetentionDays, 30) //
				.notLess("batchDownloadChunkSize", batchDownloadChunkSize, 1000) //
		;

		return validator.toList();
	}

}
