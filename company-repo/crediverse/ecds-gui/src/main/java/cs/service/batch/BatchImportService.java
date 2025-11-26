package cs.service.batch;

import java.io.BufferedReader;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import cs.config.RestServerConfiguration;
import cs.constants.ApplicationEnum.BatchStatusEnum;
import cs.dto.GuiBatchUploadRequest;
import cs.dto.batch.BatchImport;
import cs.dto.data.BaseResponse;
import cs.dto.error.GuiGeneralException;
import cs.dto.security.LoginSessionData;
import cs.service.AgentService;
import cs.service.AreaService;
import cs.service.CellGroupService;
import cs.service.CellService;
import cs.service.CorrelationIdService;
import cs.service.DepartmentService;
import cs.service.GroupService;
import cs.service.PromotionsService;
import cs.service.ServiceClassService;
import cs.service.TierService;
import cs.service.TransactionService;
import cs.service.TransferRuleService;
import cs.service.WebUserService;
import cs.service.workflow.WorkFlowService;
import cs.template.CsRestTemplate;
import cs.utility.BatchUtility;
import cs.utility.BatchUtility.BatchFileSubType;
import cs.utility.BatchUtility.BatchFileType;
import hxc.ecds.protocol.rest.Batch;
import hxc.ecds.protocol.rest.BatchIssue;
import hxc.ecds.protocol.rest.BatchUploadRequest;
import hxc.ecds.protocol.rest.BatchUploadResponse;
import hxc.ecds.protocol.rest.config.TransactionsConfig;

@Service
public class BatchImportService
{
	private ConcurrentHashMap<String, BatchImport> importsInProgress;
	private String restStatusUrl;

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
		this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getBatchuploadurl();
	}

	public BatchImportService(@Autowired RestServerConfiguration restServerConfig)
	{
		this.restServerConfig = restServerConfig;
		importsInProgress = new ConcurrentHashMap<String, BatchImport>();
		this.restStatusUrl = restServerConfig.getRestServer() + restServerConfig.getBatchstatusurl();
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
				BatchUploadWorker worker = applicationContext.getBean(BatchUploadWorker.class);
				worker.setTask(task);
				//task.setStatus(BatchStatusType.VERIFYING);
				batchExecutor.submit(worker);
				break;
			case COMPLETE:
				//importsInProgress.remove(uuid);
				break;
			case VERIFIED:
				task.setStatus(BatchStatusEnum.VERIFIED);
				this.updateState(task);
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
		if (response != null)
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

	public BatchImport queueBatchUpload(BatchImport task) throws Exception
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
		}
		catch(Exception ex)
		{
			task.setError(ex);
			throw ex;
		}
		return task;
	}

	public BatchImport queueUnauthorized(GuiBatchUploadRequest job) throws Exception
	{
		BatchImport task = (BatchImport)transactionService.getTransactionById(job.getUuid());

		if (task == null)
		{
			task = importsInProgress.get(job.getUuid());
			if (task == null) throw new Exception("No such job in queue "+job.getUuid());
		}
		else {
			importsInProgress.put(job.getUuid(), task);
		}
		job.setType(BatchUtility.getFileTypeString(task.getLocalFilename()));
		job.setFilename(task.getLocalFilename());
		job.setSessionID(this.sessionData.getServerSessionID());
		job.setCharacterOffset(task.getCharacterOffset());
		job.setBatchID(task.getBatchID());
		job.setLast(false);

		workFlowService.createRequest(sessionData.getUsername(), job);

		task.setStatus(BatchStatusEnum.QUEUED);
		//task.updateIssues(job, response, BatchStatusType.QUEUED);
		return task;
	}

	private BatchUploadRequest getBatchUploadRequestFromBatchImport(BatchImport task) throws Exception
	{
		BatchUploadRequest currentChunk = new BatchUploadRequest();
		currentChunk.setType(task.getType());
		currentChunk.setFilename(task.getLocalFilename());
		currentChunk.setSessionID(this.sessionData.getServerSessionID());
		currentChunk.setCoSignatoryTransactionID(task.getUuid());

		currentChunk.setCharacterOffset(task.getCharacterOffset());
		currentChunk.setCoSignatorySessionID(task.getCoSignatorySessionID());
		currentChunk.setLast(true);
		return currentChunk;
	}

	public Object processUploaded(String uuid) throws Exception
	{
		if (!importsInProgress.containsKey(uuid))
		{
			throw new Exception("No such job in queue "+uuid);
		}
		BatchImport task = importsInProgress.get(uuid);
		return processUploaded(task);
		/*BatchUploadRequest currentChunk = getBatchUploadRequestFromBatchImport(task);
		BatchUploadResponse response = restTemplate.postForObject(restServerUrl, currentChunk, BatchUploadResponse.class);
		task.updateIssues(currentChunk, response, BatchStatusEnum.COMPLETE);
		updateState(task);
		return task;*/
	}

	public BaseResponse processUploaded(BatchImport batchImport) throws Exception
	{
		BatchUploadRequest currentChunk = getBatchUploadRequestFromBatchImport(batchImport);
		BatchUploadResponse response = restTemplate.postForObject(restServerUrl, currentChunk, BatchUploadResponse.class);
		batchImport.updateIssues(currentChunk, response, BatchStatusEnum.COMPLETE);
		updateState(batchImport);
		return batchImport;
	}

	private BatchUploadRequest getBatchStateChangeRequestFromBatchImport(BatchImport task, BatchStatusEnum newState) throws Exception
	{
		BatchUploadRequest stateChangeRequest = new BatchUploadRequest();
		stateChangeRequest.setType(task.getType());
		stateChangeRequest.setFilename(task.getLocalFilename());
		stateChangeRequest.setSessionID(this.sessionData.getServerSessionID());
		stateChangeRequest.setCharacterOffset(task.getCharacterOffset());
		stateChangeRequest.setState(newState.getTsState());
		stateChangeRequest.setLast(false);
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

	public String getExportHeadings(HttpServletResponse response, BatchFileType batchType) throws Exception
	{
		String result = "";
		String filter = "id='0'";

		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), batchType, ".csv"));

		switch(batchType) {
			case USER:
				result = webUserService.listAsCsv(filter, null, 0, 0, null);
				break;

			case ACCOUNT:
				result = agentService.listAsCsv(filter, null, 0, 0, null);
				break;

			case ADJUST:
				result = transactionService.getAdjustmentsTemplate();
				break;

			case RULE:
				result = transferRuleService.listAsCsv(filter, null, 0, 0, null);
				break;

			case TIER:
				result = tierService.listAsCsv(filter, null, 0, 0, null);
				break;

			case GROUP:
				result = groupService.listAsCsv(filter, null, 0, 0, null);
				break;

			case SC:
				result = serviceClassService.listAsCsv(filter, null, 0, 0, null);
				break;

			case DEPT:
				result = departmentService.listAsCsv(filter, null, 0, 0, null);
				break;

			case AREA:
				result = areaService.listAsCsv(filter, null, 0, 0, null);
				break;

			case CELL:
				result = cellService.listAsCsv(filter, null, 0, 0, null);
				break;

			case CELLGROUP:
				result = cellGroupService.listAsCsv(filter, null, 0, 0, null);
				break;

			case PROM:
				result = promotionService.listAsCsv(filter, null, 0, 0, null);
				break;

			default:
				break;
		}


		return result;
	}

	public void processQueued(GuiBatchUploadRequest incoming) throws Exception
	{
		BatchProcessingWorker worker = applicationContext.getBean(BatchProcessingWorker.class);
		incoming.setLast(true);
		worker.setRequest(incoming);
		batchExecutor.submit(worker);
	}

	public String getExportHeadings(HttpServletResponse response, BatchFileType batchType, BatchFileSubType subType) throws Exception
	{
		String result = "";

		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), batchType, ".csv"));
		switch(batchType)
		{
			case ADJUST:
				result = transactionService.getAdjustmentsTemplate(subType);
				break;
			default:
				break;
		}
		return result;
	}
}
