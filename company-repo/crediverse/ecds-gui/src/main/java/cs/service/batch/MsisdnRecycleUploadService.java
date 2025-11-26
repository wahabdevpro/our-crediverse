package cs.service.batch;
/*
import cs.config.RestServerConfiguration;
import cs.constants.ApplicationEnum.BatchStatusEnum;
import cs.dto.batch.BatchImport;
import cs.dto.batch.BatchMsisdnRecycleSubmit;
import cs.dto.error.GuiGeneralException;
import cs.dto.msisdnrecycle.MsisdnRecycleUpload;
import cs.dto.security.LoginSessionData;
import cs.service.*;
import cs.service.workflow.WorkFlowService;
import cs.template.CsRestTemplate;
import cs.utility.BatchUtility;
import cs.utility.BatchUtility.BatchFileType;
import hxc.ecds.protocol.rest.*;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;*/


/*
Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
 */
//@Service
public class MsisdnRecycleUploadService
{
	/*private static Logger logger = LoggerFactory.getLogger(MsisdnRecycleUploadService.class);
	private ConcurrentHashMap<String, BatchImport> importsInProgress;
	private String restStatusUrl;
	private String restResultsUrl;
	private String restSubmitUrl;

	@Autowired
	@Qualifier("batchExecutor")
	private ThreadPoolTaskExecutor batchExecutor;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private CsRestTemplate restTemplate;

	private RestServerConfiguration restServerConfig;

	@Autowired
	private LoginSessionData sessionData;
	private String restServerUrl;

	@Autowired
	private CorrelationIdService correlationIdService;


	// Autowired specifically for exporting headers (on Import Page)
	@Autowired
	private WebUserService webUserService;

	@Autowired
	private AgentService agentService;

	@Autowired
	private TierService tierService;

	@Autowired
	private TransferRuleService transferRuleService;

	@Autowired
	private GroupService groupService;

	@Autowired
	private ServiceClassService serviceClassService;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private WorkFlowService workFlowService;


	@Autowired
	private AreaService areaService;

	@Autowired
	private CellService cellService;

	@Autowired
	private CellGroupService cellGroupService;

	@Autowired
	private PromotionsService promotionService;

	@PostConstruct
	public void configure()
	{
		this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getMsisdnRecycleUploadUrl();
	}

	public MsisdnRecycleUploadService(@Autowired RestServerConfiguration restServerConfig)
	{
		this.restServerConfig = restServerConfig;
		importsInProgress = new ConcurrentHashMap<String, BatchImport>();
		this.restStatusUrl = restServerConfig.getRestServer() + restServerConfig.getMsisdnRecycleStatusUrl();
		this.restResultsUrl = restServerConfig.getRestServer() + restServerConfig.getMsisdnRecycleResultsUrl();
		this.restSubmitUrl = restServerConfig.getRestServer() + restServerConfig.getMsisdnRecycleSubmitUrl();
	}

	public BatchImport getBatchImportStatus(String uuid, Optional<Integer> offset, Optional<Integer> limit) throws Exception
	{
		BatchImport response = null;
		if (!importsInProgress.containsKey(uuid))
		{
			throw new Exception("No such job in queue "+uuid);
		}
		BatchImport task = importsInProgress.get(uuid);
		response = task.getIssues(offset, limit);
		switch (task.getStatus())
		{
			case QUEUED:
				MsisdnRecycleUploadWorker worker = applicationContext.getBean(MsisdnRecycleUploadWorker.class);
				worker.setTask(task);
				//task.setStatus(BatchStatusType.VERIFYING);
				batchExecutor.submit(worker);
				break;
			case COMPLETE:
				//importsInProgress.remove(uuid);
				break;
			case VERIFIED:
				task.setStatus(BatchStatusEnum.VERIFIED);
				
				break;
			case ERROR:
				List<BatchIssue> issues = response.getIssues();
				if ((task.getResponseCode() != null && task.getResponseCode().equals(TransactionsConfig.ERR_FORBIDDEN)) || (issues != null && issues.isEmpty()))
				{
					GuiGeneralException ex = new GuiGeneralException(task.getResponseCode());

					ex.setCorrelationId(correlationIdService.getUniqueId());
					ex.fillInStackTrace();
					if (task.getResponseCode().equals(TransactionsConfig.ERR_FORBIDDEN))
						ex.setErrorCode(HttpStatus.FORBIDDEN);
					else
						ex.setErrorCode(HttpStatus.BAD_REQUEST);
					ex.setServerCode(task.getResponseCode());
					throw ex;
				}

				// FIXME added to work around TS bug.  Remove when TX is fixed.
				task.setStatus(BatchStatusEnum.ERROR);

				this.updateState(task);
			default:
				break;
		}
		if (response != null && (task.getStatus() != BatchStatusEnum.COMPLETE || task.getStatus() != BatchStatusEnum.VERIFIED) )
		{
			int batchid = response.getBatchID();
			try
			{
				Batch batchStatus = restTemplate.execute(restStatusUrl+"/"+String.valueOf(batchid), HttpMethod.GET, Batch.class);
				response.setBatchStatus(batchStatus);
			}
			catch (Exception ex)
			{
				ex.fillInStackTrace();
			}
		}
		return response;
	}

	public MsisdnRecycleResponse getBatchImportResults(String uuid) throws Exception
	{
		MsisdnRecycleResponse msisdnRecycleResponse = null;
		if (!importsInProgress.containsKey(uuid))
		{
			throw new Exception("No such job in queue "+uuid);
		}
		BatchImport batchImport = importsInProgress.get(uuid);
		
		if (batchImport != null)
		{
			int batchid = batchImport.getBatchID();
			try
			{
				msisdnRecycleResponse = restTemplate.execute(restResultsUrl+"/"+String.valueOf(batchid), HttpMethod.GET, MsisdnRecycleResponse.class);
				
			}
			catch (Exception ex)
			{
				//issues are returned in the response for the front end to handle
				ex.fillInStackTrace();
			}
		}
		return msisdnRecycleResponse;
	}


	public MsisdnRecycleUpload queueBatchUpload(MsisdnRecycleUpload task) throws Exception
	{
		String uuid = task.getUuid();
		importsInProgress.put(uuid, task);
		task.setStatus(BatchStatusEnum.QUEUED);
		BatchUploadRequest currentChunk = new BatchUploadRequest();
		currentChunk.setType(BatchUtility.getFileTypeString(task.getLocalFilename()));
		currentChunk.setFilename(task.getLocalFilename());
		currentChunk.setSessionID(this.sessionData.getServerSessionID());
		long currentCharOffset = 0L;
		currentChunk.setCharacterOffset(currentCharOffset);
		task.setCharacterOffset(currentCharOffset);
		BufferedReader inputStream = null;
		BatchUploadResponse response = null;
		try
		{

			if (currentChunk.getType() != null && BatchFileType.valueOf(currentChunk.getType().toUpperCase()) == BatchFileType.ADJUST)
			{
				transactionService.createTransaction(task, task.getUuid());
			}
			inputStream = task.getDataInputStream();
			//task.setStatus(BatchStatusType.VERIFYING);

			String line;
			StringBuilder textBuffer = new StringBuilder();
			while((line = inputStream.readLine()) != null)
			{
				if (line.trim().length() == 0) continue; // Skips empty lines.
				break;
			}
			textBuffer.append(line);
			textBuffer.append("\n");

			String text = textBuffer.toString();
			currentChunk.setContent(text.toLowerCase()); // This line is the headings only, so lowercase it.
			currentChunk.setCharacterOffset(currentCharOffset);
			currentChunk.setCoSignatorySessionID(task.getCoSignatorySessionID());
			//logger.info(currentChunk.getContent());

			response = restTemplate.postForObject(restServerUrl, currentChunk, BatchUploadResponse.class);
			task.updateIssues(currentChunk, response, BatchStatusEnum.QUEUED);
			task.updateProgress(text.getBytes().length);
			currentCharOffset += textBuffer.length();
			task.setCharacterOffset(currentCharOffset);
			//task.setMsisdnRecycleResponse(response);
		}
		catch(Exception ex)
		{
			task.setError(ex);
			throw ex;
		}
		return task;
	}


	public MsisdnSubmitRecycleResponse submitAgentsForRecyclingMsisdn(BatchMsisdnRecycleSubmit batchMsisdnRecycleSubmit) throws Exception
	{

		MsisdnSubmitRecycleRequest msisdnSubmitRecycleRequest = new MsisdnSubmitRecycleRequest();
		msisdnSubmitRecycleRequest.setAgentIdsToRecycle(batchMsisdnRecycleSubmit.getAgentIds());
		msisdnSubmitRecycleRequest.setSessionID(this.sessionData.getServerSessionID());
		
		transactionService.createTransaction(batchMsisdnRecycleSubmit, null);
		msisdnSubmitRecycleRequest.setCoSignatorySessionID(batchMsisdnRecycleSubmit.getUuid());
		MsisdnSubmitRecycleResponse response = restTemplate.postForObject(restSubmitUrl, msisdnSubmitRecycleRequest, MsisdnSubmitRecycleResponse.class);
		return response;
	}



	private BatchUploadRequest getBatchStateChangeRequestFromBatchImport(BatchImport task, BatchStatusEnum newState) throws Exception
	{
		BatchUploadRequest stateChangeRequest = new BatchUploadRequest();
		stateChangeRequest.setType(task.getType());
		stateChangeRequest.setFilename(task.getLocalFilename());
		stateChangeRequest.setSessionID(this.sessionData.getServerSessionID());
		stateChangeRequest.setCharacterOffset(task.getCharacterOffset());
		stateChangeRequest.setState(newState.getTsState());
		stateChangeRequest.setLast(true);
		return stateChangeRequest;
	}

	private BatchUploadRequest getBatchStateChangeRequestFromBatch(Batch batch, BatchStatusEnum newState) throws Exception
	{
		BatchUploadRequest stateChangeRequest = new BatchUploadRequest();
		stateChangeRequest.setType(batch.getType());
		stateChangeRequest.setFilename(batch.getFilename());
		stateChangeRequest.setSessionID(this.sessionData.getServerSessionID());
		//stateChangeRequest.setCharacterOffset(batch.getCharacterOffset());
		stateChangeRequest.setState(newState.getTsState());
		stateChangeRequest.setLast(false);
		return stateChangeRequest;
	}


	public BatchUploadResponse updateState(BatchImport batchImport, BatchStatusEnum newState) throws Exception
	{
		BatchUploadRequest stateChangeRequest = getBatchStateChangeRequestFromBatchImport(batchImport, newState);
		return restTemplate.postForObject(restServerUrl, stateChangeRequest, BatchUploadResponse.class);
	}


	public BatchUploadResponse updateState(BatchImport batchImport) throws Exception
	{
		return updateState(batchImport, batchImport.getStatus());
	}

	public BatchUploadResponse updateState(int id, BatchStatusEnum newState) throws Exception
	{
		BatchUploadRequest stateChangeRequest = getBatchStateChangeRequestFromBatch(getBatch(id), newState);
		return restTemplate.postForObject(restServerUrl, stateChangeRequest, BatchUploadResponse.class);
	}
	
	public Batch getBatch(int batchId) throws Exception
	{
		return getBatch(String.valueOf(batchId));
	}

	public Batch getBatch(String batchId) throws Exception
	{
		Batch response = null;
		response = restTemplate.execute(restStatusUrl+"/"+batchId, HttpMethod.GET, Batch.class);

		return response;
	}
*/
	
}
