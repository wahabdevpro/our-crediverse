package cs.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import cs.dto.*;
import hxc.ecds.protocol.rest.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cs.config.RestServerConfiguration;
import cs.constants.ApplicationEnum.BatchStatusEnum;
import cs.dto.batch.BatchImport;
import cs.dto.data.BaseResponse;
import cs.dto.security.LoginSessionData;
import cs.service.batch.BatchImportService;
import cs.template.CsRestTemplate;
import cs.utility.BatchUtility.BatchFileSubType;

@Service
public class TransactionService {
	private static Logger logger = LoggerFactory.getLogger(TransactionService.class);

	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private CsRestTemplate restTemplate;

	@Autowired
	private RestServerConfiguration restServerConfig;

	@Autowired
	private TypeConvertorService typeConvertorService;

	@Autowired
	@Qualifier("transactionStore")
	private Map<String, Object> transactionStore;

	@Autowired
	private BatchImportService batchImportService;

	@Autowired
	private ObjectMapper mapper;

	private boolean configured = false;
	private String restTransactionsServerUrl;

	@PostConstruct
	public void configure()
	{
		if (!configured)
		{
			this.restTransactionsServerUrl = restServerConfig.getRestServer() + restServerConfig.getTransactionsUrl();
			configured = true;
		}
	}

	private String getTransactionRESTUrl(String path)
	{
		return String.format("%s/%s", restTransactionsServerUrl, path);
	}


	private <T>void addTransaction(String id, T transaction)
	{
		if (transactionStore == null) transactionStore = new ConcurrentHashMap<String, Object>();
			transactionStore.put(id, transaction);
	}

	private <T> T getTransaction(String id)
	{
		if (transactionStore != null)
		{
			return (T)transactionStore.get(id);
		}
		return null;
	}

	private <T> void processCosignableRequest(T request, String id) throws Exception
	{
		if (request instanceof ICoSignable)
		{
			ICoSignable cosignableRequest = (ICoSignable)request;


			cosignableRequest.setCoSignatoryTransactionID(id);
		}
	}

	public <T> UUID createTransaction(T request) throws Exception
	{
		return createTransaction(request, null);
	}

	public BaseResponse processTransaction(GuiReversalRequest request) throws Exception {
		UUID id = UUID.randomUUID();
		String stringId = id.toString();
		restTemplate.setValue(request, "uuid", stringId, false);
		restTemplate.setValue(request, "sessionID", sessionData.getServerSessionID(), false);
		return processRequest(request);
	}

	public <T> UUID createTransaction(T request, String myUuid) throws Exception
	{
		UUID id = null;
		String stringId = null;
		if (myUuid != null)
		{
			id = UUID.fromString(myUuid);
			stringId = id.toString();
		}
		else
		{
			id = UUID.randomUUID();
			stringId = id.toString();
		}

		restTemplate.setValue(request, "uuid", stringId, false);
		addTransaction(stringId, request);
		processCosignableRequest(request, stringId);
		return id;
	}

	public <T> UUID createOtpTransaction(T request, String myUuid) throws Exception
	{
		UUID id = null;
		String stringId = null;
		if (myUuid != null)
		{
			id = UUID.fromString(myUuid);
			stringId = id.toString();
		}
		else
		{
			id = UUID.randomUUID();
			stringId = id.toString();
		}

		restTemplate.setValue(request, "uuid", stringId, false);
		addTransaction(stringId, request);
		processCosignableRequest(request, stringId);
		return id;
	}

	private String getRestUrl(String path)
	{
		String url = restServerConfig.getRestServer() + path;
		return url;
	}

	public BaseResponse processRequest(Object incoming) throws Exception
	{
		BaseResponse response = null;
		Object request = null;
		if (incoming instanceof GuiReplenishRequest)
		{
			request = typeConvertorService.getReplenishRequestFromGuiReplenishRequest((GuiReplenishRequest)incoming);
			restTemplate.postForObject(getRestUrl(restServerConfig.getReplenishurl()), request, ReplenishResponse.class);
		}
		else if (incoming instanceof GuiTransferRequest)
		{
			request = typeConvertorService.getTransferRequestFromGuiTransferRequest((GuiTransferRequest)incoming);
			String restUrl = getRestUrl(restServerConfig.getTransferurl());
			restTemplate.postForObject(restUrl, request, TransferResponse.class);
		}
		else if (incoming instanceof GuiAdjudicateRequest)
		{
			request = typeConvertorService.getAdjudicateRequestFromGuiAdjudicateRequest((GuiAdjudicateRequest)incoming);
			String restUrl = getRestUrl(restServerConfig.getAdjudicateurl());
			restTemplate.postForObject(restUrl, request, AdjudicateResponse.class);
		}
		else if (incoming instanceof GuiAdjustmentRequest)
		{
			AdjustmentRequest currentRequest = typeConvertorService.getAdjustmentRequestFromGuiAdjustmentRequest((GuiAdjustmentRequest)incoming);
			restTemplate.postForObject(getRestUrl(restServerConfig.getAdjustmenturl()), currentRequest, AdjustmentResponse.class);
		}
		else if (incoming instanceof GuiReversalCoAuthRequest)
		{
			GuiReversalCoAuthRequest reversal = (GuiReversalCoAuthRequest)incoming;
			//throw new Exception("Test");
			switch (reversal.getType())
			{
				case FULL:
					request = typeConvertorService.getReversalRequestWithCoAuthFromGuiReversal(reversal);
					restTemplate.postForObject(getRestUrl(restServerConfig.getReversalurl()), request, PartialReversalResponse.class);
					break;
				case PARTIAL:
					request = typeConvertorService.getPartialReversalRequestWithCoAuthFromGuiReversal(reversal);
					restTemplate.postForObject(getRestUrl(restServerConfig.getPartialreversalurl()), request, ReversalResponse.class);
					break;
			}
		} else if (incoming instanceof GuiReversalRequest) {
			GuiReversalRequest reversal = (GuiReversalRequest) incoming;
			switch (reversal.getType()) {
				case FULL:
					request = typeConvertorService.getReversalRequestFromGuiReversal(reversal);
					restTemplate.postForObject(getRestUrl(restServerConfig.getReversalWithoutCoAuthUrl()), request, PartialReversalResponse.class);
					break;
				case PARTIAL:
					request = typeConvertorService.getPartialReversalRequestFromGuiReversal(reversal);
					restTemplate.postForObject(getRestUrl(restServerConfig.getPartialReversalWithoutCoAuthUrl()), request, ReversalResponse.class);
					break;
			}
		} else if (incoming instanceof GuiBatchUploadRequest) {
			batchImportService.processQueued((GuiBatchUploadRequest)incoming);
		}
		else if (incoming instanceof BatchImport)
		{
			BatchImport batchImport = (BatchImport)incoming;
			switch (batchImport.getStatus())
			{
				case QUEUED:
					response = batchImportService.queueBatchUpload(batchImport);
					break;
				case PENDING_AUTHORIZATION:
				case READY_FOR_PROCESSING:
					batchImport.setStatus(BatchStatusEnum.READY_FOR_PROCESSING);
					batchImportService.updateState(batchImport);
					// Fall through to verified.
				case VERIFIED:
					response = batchImportService.processUploaded(batchImport);
					break;
				default:
					break;
			}
		}
		else if (incoming instanceof GuiWorkflowRequest)
		{
			GuiWorkflowRequest workflowRequest = (GuiWorkflowRequest)incoming;
			processWorkflowTransaction(workflowRequest);
		}
		else
		{
			throw new UnsupportedOperationException();
		}
		try
		{
			logger.info("Request "+mapper.writeValueAsString(request));
			logger.info("Response "+mapper.writeValueAsString(response));
		}
		catch (JsonProcessingException e)
		{
			logger.error("", e);
		}
		return response;
	}

	private void processWorkflowTransaction(GuiWorkflowRequest workflowRequest) throws Exception
	{
		switch(workflowRequest.getRequestType())
		{
			case ADJUSTMENT:
				GuiAdjustmentRequest adjustment = workflowRequest.getAdjustment();
				//adjustment.setCoSignatorySessionID(workflowRequest.getCoSignatorySessionID());
				adjustment.setCoSignatorySessionID(workflowRequest.getCoSignatorySessionID());
				adjustment.setCoSignatoryTransactionID(workflowRequest.getCoSignatoryTransactionID());
				adjustment.setCoSignatoryOTP(workflowRequest.getCoSignatoryOTP());
				adjustment.setSessionID(workflowRequest.getCoSignForSessionID());
				processRequest(adjustment);
				break;
			case ADJUDICATION:
				GuiAdjudicateRequest adjudication = workflowRequest.getAdjudication();
				//adjustment.setCoSignatorySessionID(workflowRequest.getCoSignatorySessionID());
				adjudication.setCoSignatorySessionID(workflowRequest.getCoSignatorySessionID());
				adjudication.setCoSignatoryTransactionID(workflowRequest.getCoSignatoryTransactionID());
				adjudication.setCoSignatoryOTP(workflowRequest.getCoSignatoryOTP());
				adjudication.setSessionID(workflowRequest.getCoSignForSessionID());
				processRequest(adjudication);
				break;
			case BATCHUPLOAD:
				GuiBatchUploadRequest upload = workflowRequest.getBatchUpload();
				upload.setCoSignatorySessionID(workflowRequest.getCoSignatorySessionID());
				upload.setCoSignatoryTransactionID(workflowRequest.getCoSignatoryTransactionID());
				upload.setCoSignatoryOTP(workflowRequest.getCoSignatoryOTP());
				upload.setSessionID(workflowRequest.getCoSignForSessionID());
				processRequest(upload);
				break;
			case REPLENISH:
				GuiReplenishRequest replenish = workflowRequest.getReplenish();
				replenish.setCoSignatorySessionID(workflowRequest.getCoSignatorySessionID());
				replenish.setCoSignatoryTransactionID(workflowRequest.getCoSignatoryTransactionID());
				replenish.setCoSignatoryOTP(workflowRequest.getCoSignatoryOTP());
				replenish.setSessionID(workflowRequest.getCoSignForSessionID());
				processRequest(replenish);
				break;
			case PARTIALREVERSAL:
			case REVERSAL:
				TransactionRequest reversal = workflowRequest.getReversal();
				if (reversal instanceof GuiReversalCoAuthRequest) {
					((GuiReversalCoAuthRequest)reversal).setCoSignatorySessionID(workflowRequest.getCoSignatorySessionID());
					((GuiReversalCoAuthRequest)reversal).setCoSignatoryTransactionID(workflowRequest.getCoSignatoryTransactionID());
					((GuiReversalCoAuthRequest)reversal).setCoSignatoryOTP(workflowRequest.getCoSignatoryOTP());
				}
				reversal.setSessionID(workflowRequest.getCoSignForSessionID());
				processRequest(reversal);
				break;
			case TRANSFER:
				GuiTransferRequest transfer = workflowRequest.getTransfer();
				transfer.setCoSignatorySessionID(workflowRequest.getCoSignatorySessionID());
				transfer.setCoSignatoryTransactionID(workflowRequest.getCoSignatoryTransactionID());
				transfer.setCoSignatoryOTP(workflowRequest.getCoSignatoryOTP());
				transfer.setSessionID(workflowRequest.getCoSignForSessionID());
				processRequest(transfer);
				break;
			default:
				break;

		}
	}

	public BaseResponse processTransaction(UUID uuid, String sessionId) throws Exception
	{
		return processTransaction(uuid.toString(), sessionId);
	}

	public BaseResponse processTransaction(String uuid, String sessionId) throws Exception
	{
		BaseResponse response = null;
		if (transactionStore != null && transactionStore.containsKey(uuid))
		{
			Object request = getTransaction(uuid);
			restTemplate.setValue(request, "sessionID", sessionData.getServerSessionID(), false);
			restTemplate.setValue(request, "coSignatorySessionID", sessionId, false);

			response = processRequest(request);
			//transactionStore.remove(uuid);
		}
		else
		{
            logger.error("\n\n\nUNHANDLED EXCEPTION: Invalid transaction, uuid:{}; sessionId:{}\n\n", uuid, sessionId);
            //throw new Exception(?????);
		}
		return response;
	}

	public BaseResponse processWorkflowTransaction(UUID uuid, String sessionId) throws Exception
	{
		return processWorkflowTransaction(uuid.toString(), sessionId);
	}

	public Object getTransactionById(String uuid)
	{
		if (transactionStore != null && transactionStore.containsKey(uuid))
		{
			return getTransaction(uuid);
		}
		return null;
	}

	public BaseResponse processWorkflowTransaction(String uuid, String sessionId) throws Exception
	{
		BaseResponse response = null;
		if (transactionStore != null && transactionStore.containsKey(uuid))
		{
			Object request = getTransaction(uuid);
			//restTemplate.setValue(request, "sessionID", sessionId, false);
			restTemplate.setValue(request, "coSignatorySessionID", sessionId, false);

			response = processRequest(request);
			//transactionStore.remove(uuid);
		}
		else
		{
            logger.error("\n\n\nUNHANDLED EXCEPTION: Invalid transaction, uuid:{}; sessionId:{}\n\n", uuid, sessionId);
            //throw new Exception(?????);
		}
		return response;
	}

	/**
	 * Create Transactionsacts
	 */

	public String getAdjustmentsTemplate() throws Exception
	{
		return restTemplate.execute(getTransactionRESTUrl("adjust/batch_template"), HttpMethod.GET, String.class);
	}

	public String getAdjustmentsTemplate(BatchFileSubType subType) throws Exception
	{
		StringBuilder templateUrl = new StringBuilder("adjust/");
		switch(subType)
		{
			case EXCLUSIVE:
				templateUrl.append("exclusive_batch_template");
				break;
			case INCLUSIVE:
				templateUrl.append("inclusive_batch_template");
				break;
			default:
				break;

		}
		return restTemplate.execute(getTransactionRESTUrl(templateUrl.toString()), HttpMethod.GET, String.class);
	}
}
