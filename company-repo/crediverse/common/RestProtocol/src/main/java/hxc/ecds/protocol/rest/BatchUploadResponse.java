package hxc.ecds.protocol.rest;

public class BatchUploadResponse extends ResponseHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected long nextExpectedCharacterOffset;
	protected int batchID;
	protected BatchIssue[] issues;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public long getNextExpectedCharacterOffset()
	{
		return nextExpectedCharacterOffset;
	}

	public BatchUploadResponse setNextExpectedCharacterOffset(long nextExpectedCharacterOffset)
	{
		this.nextExpectedCharacterOffset = nextExpectedCharacterOffset;
		return this;
	}

	public int getBatchID()
	{
		return batchID;
	}

	public BatchUploadResponse setBatchID(int batchID)
	{
		this.batchID = batchID;
		return this;
	}

	public BatchIssue[] getIssues()
	{
		return issues;
	}

	public BatchUploadResponse setIssues(BatchIssue[] issues)
	{
		this.issues = issues;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public BatchUploadResponse()
	{

	}

	public BatchUploadResponse(BatchUploadRequest request)
	{
		super(request);
	}

}
