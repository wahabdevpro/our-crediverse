package cs.service.batch;

import java.io.BufferedReader;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cs.config.RestServerConfiguration;
import cs.constants.ApplicationEnum.BatchStatusEnum;
import cs.dto.batch.BatchImport;
import cs.dto.security.LoginSessionData;
import cs.template.BatchThreadWorker;
import cs.template.CsRestTemplate;
import cs.utility.BatchUtility;
import hxc.ecds.protocol.rest.BatchUploadRequest;
import hxc.ecds.protocol.rest.BatchUploadResponse;


public class BatchUploadWorker extends BatchThreadWorker
{
	private static Logger logger = LoggerFactory.getLogger(BatchUploadWorker.class);
	private BatchImport task;
	private String restServerUrl;

	@Autowired
	private CsRestTemplate restTemplate;

	@Autowired
	private RestServerConfiguration restServerConfig;

	@Autowired
	private BatchImportService batchImportService;

	@Autowired
	private LoginSessionData sessionData;

	@PostConstruct
	public void configure()
	{
		this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getBatchuploadurl();
	}

	protected void setTask(BatchImport task)
	{
		this.task = task;
	}

	/*
	 * public static final String TYPE_USER = "user";
	public static final String TYPE_CELL = "cell";
	public static final String TYPE_AREA = "area";
	public static final String TYPE_TIER = "tier";
	public static final String TYPE_SC = "sc";
	public static final String TYPE_AGENT = "account";
	public static final String TYPE_GROUP = "group";
	public static final String TYPE_RULE = "rule";
	public static final String TYPE_PROM = "prom";
	public static final String TYPE_ADJUST = "adjust";

	public static final int SESSION_ID_MAX_LENGTH = 50;
	public static final int FILENAME_MAX_LENGTH = 100;
	public static final long SEGMENT_MAXLENGTH = 1 * 1024 * 1024;

	public static final String VERB_ADD = "add";
	public static final String VERB_UPDATE = "update";
	public static final String VERB_DELETE = "delete";
	public static final String VERB_VERIFY = "verify";
	public static final String VERB_UPSERT = "upsert";
	 */

	@Override
	public void onRun()
	{
		// TODO Code to upload and process individual file
		if (task == null) throw new IllegalArgumentException("Cannot run without a task to perform");

		BatchUploadRequest currentChunk = new BatchUploadRequest();
		try
		{
			currentChunk.setType(BatchUtility.getFileTypeString(task.getLocalFilename()));
			//currentChunk.setFileLength(task.getSize()); // FIXME set size of file so remote system has a clue what to expect (requires TS changes).
			currentChunk.setFilename(task.getLocalFilename());
			currentChunk.setSessionID(this.sessionData.getServerSessionID());

			boolean sendSuccess = false;
			while(!sendSuccess)
			{
				try
				{
					sendWholeFile(currentChunk);
					sendSuccess = true;
				}
				catch(Throwable ex)
				{
					logger.error("", ex);
					/*if (retryCount >= ApplicationConstants.CONST_BATCH_MAX_RETRY)
					{
						logger.info("Failed to send chunk at character offset "+currentChunk.getCharacterOffset());
						logger.error("", ex);
						task.setStatus(BatchStatusType.ERROR);
						throw ex;
					}*/
					logger.info("Failed to send chunk at character offset "+currentChunk.getCharacterOffset());
					logger.error("", ex);
					task.setError(ex);
					throw ex;
				}
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
			task.setError(e);
		}
	}

	private boolean sendWholeFile(BatchUploadRequest currentChunk) throws Exception
	{
		boolean success = false;
		long currentCharOffset = 0L;
		BufferedReader inputStream = null;
		try
		{
			currentCharOffset = task.getCharacterOffset();
			currentChunk.setCharacterOffset(currentCharOffset);
			inputStream = task.getDataInputStream();
			task.setStatus(BatchStatusEnum.VERIFYING);
			String line;
			StringBuilder textBuffer = new StringBuilder();
			long startTime = System.nanoTime();
			long nextExpectedOffset = currentCharOffset;
			while((line = inputStream.readLine()) != null)
			{
				if (line.trim().length() == 0) continue;
				if (((textBuffer.length() + line.length()) >= BatchUploadRequest.SEGMENT_MAX_LENGTH))
				{
					if (textBuffer.length() > 0)
					{
						textBuffer.append("\n");
					}
					String text = textBuffer.toString();
					currentChunk.setContent(text);
					currentChunk.setCharacterOffset(currentCharOffset);
					currentChunk.setCoSignatorySessionID(task.getCoSignatorySessionID());
					//logger.info(currentChunk.getContent());
					BatchUploadResponse chunkResponse = sendChunk(currentChunk, nextExpectedOffset);
					nextExpectedOffset = chunkResponse.getNextExpectedCharacterOffset();
					task.updateIssues(currentChunk, chunkResponse, BatchStatusEnum.VERIFYING);
					task.updateProgress(text.getBytes().length);
					currentCharOffset += textBuffer.length();
					task.setCharacterOffset(currentCharOffset);
					textBuffer.setLength(0);
					//logger.error("ChunkLines was "+String.valueOf(chunklines));
					textBuffer.append(line);
				}
				else
				{
					if (textBuffer.length() > 0)
					{
						textBuffer.append("\n");
					}
					textBuffer.append(line);
				}
			}
			if (textBuffer.length() > 0)
			{
				textBuffer.append("\n");
			}
			String text = textBuffer.toString();
			//textBuffer.append("\n");
			currentChunk.setCharacterOffset(currentCharOffset);
			currentChunk.setCoSignatorySessionID(task.getCoSignatorySessionID());
			currentChunk.setContent(text);
			currentChunk.setLast(false);
			//logger.info(currentChunk.getContent());
			task.updateIssues(currentChunk, sendChunk(currentChunk, nextExpectedOffset), BatchStatusEnum.VERIFIED);
			currentCharOffset += text.length();
			task.setCharacterOffset(currentCharOffset);
			task.updateProgress(text.getBytes().length);
			success = true;
			long endTime = System.nanoTime();
			long duration = (endTime - startTime);
			logger.error("Duration was "+String.valueOf(duration/1000000));
		}
		finally
		{
			batchImportService.updateState(task);
		}
		return success;
	}

	private BatchUploadResponse sendChunk(BatchUploadRequest currentChunk, long nextExpectedOffset) throws Exception
	{
		BatchUploadResponse response = null;
		try
		{
			response = restTemplate.postForObject(restServerUrl, currentChunk, BatchUploadResponse.class);
		}
		catch (Exception ex)
		{
			ex.fillInStackTrace();
			task.setError(ex);
			throw ex;
		}
		if (currentChunk.getCharacterOffset() != nextExpectedOffset)
		{
			throw new Exception("Offset mismatch");
		}
		return response;
	}
}
