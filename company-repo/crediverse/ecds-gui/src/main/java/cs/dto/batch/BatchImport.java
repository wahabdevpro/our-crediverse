package cs.dto.batch;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cs.constants.ApplicationEnum.BatchStatusEnum;
import cs.dto.data.BaseResponse;
import cs.dto.error.GuiGeneralException;
import cs.utility.BatchUtility;
import hxc.ecds.protocol.rest.Batch;
import hxc.ecds.protocol.rest.BatchIssue;
import hxc.ecds.protocol.rest.BatchUploadRequest;
import hxc.ecds.protocol.rest.BatchUploadResponse;
import hxc.ecds.protocol.rest.ResponseHeader;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Setter
@Getter
@ToString
public class BatchImport extends BaseResponse
{
	private static final Logger logger = LoggerFactory.getLogger(BatchImport.class);

	@JsonIgnore
	private BufferedReader dataInputStream;
	private int bufferSize;
	private boolean coauth = false;

	private AtomicLong progress;
	private String localFilename;
	private long size;
	private BatchStatusEnum status = BatchStatusEnum.NEW;
	private String errorText;
	private String additional;
	private String correlationId;
	private int batchID;
	private List<BatchIssue> issues;
	private Batch batchStatus;
	private String responseCode;
	private String fileType;

	private String sessionID; // Used when import needs to be coauthorised
	private String coSignatorySessionID; // Used when import needs to be coauthorised
	private long characterOffset;

	public void setStatus(BatchStatusEnum newStatus)
	{
		this.status = newStatus;
		getType();
		if (status == BatchStatusEnum.VERIFIED && coauth)
		{
			status = BatchStatusEnum.PENDING_AUTHORIZATION;
		}

		if (batchStatus != null)
		{
			batchStatus.setState(status.getTsState());
		}
	}

	public BatchImport()
	{
		super();
		super.setUuid(UUID.randomUUID().toString());
		progress = new AtomicLong();
		progress.set(0);
	}

	public void setDataInputStream(InputStream input) throws Exception
	{
		Reader reader = new InputStreamReader(input);
		this.dataInputStream = new BufferedReader(reader);
		this.setStatus(BatchStatusEnum.VERIFYING);
	}

	/*
	 * Copy constructor, used to allow returning subset of issues for paging purposes
	 */
	public BatchImport(BatchImport data)
	{
		super();
		super.setUuid(data.getUuid());
		progress = new AtomicLong();
		progress.set(data.getProgress().get());
		correlationId = data.getCorrelationId();
		batchID = data.getBatchID();
		status = data.getStatus();
		batchStatus = data.getBatchStatus();
		issues = null;
	}

	public void updateProgress(int sentBytes)
	{
		progress.addAndGet(sentBytes);
	}

	public void setSize(long size)
	{
		this.size = size;
	}

	/*
	 * Returns the fraction of the file that has currently been sent.  So for example,
	 * if full = 100, then the result is a percentage.  If full = 50, then the result
	 * is on a scale of 0-50 where 50 represents the whole file.
	 */
	public int progressfraction(int full)
	{
		long offset = progress.get();
		float singleChunk = ((float)this.size)/full;
		int total = (int)(offset/singleChunk);
		return total;
	}

	public void setCurrentProgress(int amount)
	{
		progress.set(amount);
	}

	public void setError(Throwable e)
	{
		this.errorText = "CHUNK_FAILED";
		status = BatchStatusEnum.ERROR;
		if (e instanceof GuiGeneralException)
		{
			GuiGeneralException ex = (GuiGeneralException)e;
			this.errorText = ex.getServerCode();
			this.additional = ex.getAdditional();
			this.correlationId = ex.getCorrelationId();
		}
	}

	public void updateIssues(BatchUploadRequest currentChunk, BatchUploadResponse response, BatchStatusEnum status)
	{
		this.batchID = response.getBatchID();
		BatchIssue[] issues = response.getIssues();
		if (issues != null)
		{
			if (this.issues == null)
			{
				this.issues = new ArrayList<BatchIssue>();
			}
			for (BatchIssue currentIssue : Arrays.asList(issues))
			{
				this.issues.add(currentIssue);
			}
		}
		this.responseCode = response.getReturnCode();
		if (response.getReturnCode().equals(ResponseHeader.RETURN_CODE_SUCCESS))
		{
			this.setStatus(status);
		}
		else
		{
			this.setStatus(BatchStatusEnum.ERROR);
		}
	}

	public BatchImport getIssues(Optional<Integer> offset, Optional<Integer> limit)
	{
		BatchImport result = new BatchImport(this);
		result.setCurrentProgress(this.progressfraction(100));
		if (offset.isPresent() && limit.isPresent())
		{
			result.issues = new ArrayList<BatchIssue>();
			int end = offset.get() + limit.get();
			result.issues.addAll(this.issues.subList(offset.get(), (end > this.issues.size())?this.issues.size():end));
		}
		else
		{
			result.issues = this.issues;
		}
		return result;
	}

	@JsonIgnore
	public String getType()
	{
		String result = null;
		if (localFilename != null)
		{
			try
			{
				result = BatchUtility.getFileTypeString(localFilename);
				if (result != null)
				{
					fileType = result;
					if (fileType.equals(BatchUploadRequest.TYPE_ADJUST))
					{
						this.coauth = true;
					}
				}
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}
		return result;
	}
}
