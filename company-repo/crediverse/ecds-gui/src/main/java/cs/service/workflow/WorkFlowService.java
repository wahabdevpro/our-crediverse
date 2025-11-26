package cs.service.workflow;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import javax.annotation.PostConstruct;

import hxc.ecds.protocol.rest.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.config.RestServerConfiguration;
import cs.constants.ApplicationEnum;
import cs.constants.ApplicationEnum.BatchStatusEnum;
import cs.dto.GuiAdjudicateRequest;
import cs.dto.GuiAdjustmentRequest;
import cs.dto.GuiBatchUploadRequest;
import cs.dto.GuiReplenishRequest;
import cs.dto.GuiReversalCoAuthRequest;
import cs.dto.GuiTransferRequest;
import cs.dto.GuiWebUser;
import cs.dto.GuiWorkItem.WorkItemAction;
import cs.dto.GuiWorkflowRequest;
import cs.dto.GuiWorkflowRequest.WorkflowRequestType;
import cs.dto.security.LoginSessionData;
import cs.service.AgentService;
import cs.service.PermissionService;
import cs.service.SessionService;
import cs.service.TdrService;
import cs.service.TransactionService;
import cs.service.WebUserService;
import cs.service.batch.BatchImportService;
import cs.template.CsRestTemplate;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.config.WorkflowConfig;

@Service
public class WorkFlowService
{
	public static final String CONST_INVALID_REPLENISH_TYPE = "INVALID_REPLENISH_TYPE";
	public static final String CONST_INVALID_ADJUSTMENT_TYPE = "INVALID_ADJUSTMENT_TYPE";
	public static final String CONST_INVALID_TRANSFER_TYPE = "INVALID_TRANSFER_TYPE";
	public static final String CONST_INVALID_REVERSAL_TYPE = "INVALID_REVERSAL_TYPE";
	public static final String CONST_INVALID_BATCH_TYPE = "INVALID_BATCH_TYPE";

	private static final Logger logger = LoggerFactory.getLogger(WorkFlowService.class);

	@Autowired
	private CsRestTemplate restTemplate;

	@Autowired
	private RestServerConfiguration restServerConfig;

	@Autowired
	private WebUserService webUserService;

	@Autowired
	private AgentService agentService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private TdrService tdrService;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private BatchImportService batchImportService;

	private String restServerUrl;

	@PostConstruct
	public void configure()
	{
		this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getWorkflowurl();
	}

	public WorkItem getWorkItem(int id) throws Exception
	{
		return getWorkItem(String.valueOf(id));
	}

	public WorkItem getWorkItem(String id) throws Exception
	{
		WorkItem response = null;
		response = restTemplate.execute(restServerUrl+"/uuid/"+id, HttpMethod.GET, WorkItem.class);
		return response;
	}

	public WorkItem[] getWorkItemList() throws Exception
	{
		return restTemplate.execute(restServerUrl, HttpMethod.GET, WorkItem[].class);
	}

	public void create(WorkItem newWorkItem) throws Exception
	{
		newWorkItem.setSmsOnChange(true);
		restTemplate.execute(restServerUrl, HttpMethod.PUT, newWorkItem, Void.class);
	}

	public void update(WorkItem updatedItem) throws Exception
	{
		updatedItem.setSmsOnChange(true);
		restTemplate.execute(restServerUrl, HttpMethod.PUT, updatedItem, Void.class);
	}

	public void update(WorkItemAction action, WorkItem updatedItem, String enteredPin) throws Exception
	{
		String uuid = updatedItem.getUuid().toString();
		WorkItem realItem = getWorkItem(uuid);
		BeanUtils.copyProperties(updatedItem, realItem, "request", "response", "id", "uuid", "uri");
		Session sess = null;
		GuiWorkflowRequest currentWorkflowRequest = null;
		WorkflowRequestType newType = null;
		switch(action)
		{
			case LOCK:
				realItem.setOwnerSession(sessionData.getServerSessionID());
				break;
			case STATUS: //
				break;
			case APPROVE:
				currentWorkflowRequest = mapper.readValue(realItem.getRequest(), GuiWorkflowRequest.class);
				//sess = sessionService.getWorkItemSession(updatedItem.getUuid());// requester
				//currentWorkflowRequest.setSessionID(sess.getSessionID());
				currentWorkflowRequest.setCoSignatoryOTP(enteredPin);
				currentWorkflowRequest.setCoSignatorySessionID(sessionData.getServerSessionID());
				transactionService.createOtpTransaction(currentWorkflowRequest, uuid);
				switch (currentWorkflowRequest.getRequestType())
				{
					case BATCHUPLOAD:
						if (currentWorkflowRequest != null && currentWorkflowRequest.getRequestType() == WorkflowRequestType.BATCHUPLOAD)
						{
							GuiBatchUploadRequest batchUploadRequest = currentWorkflowRequest.getBatchUpload();
							batchUploadRequest.setWorkItemId(updatedItem.getUuid());
						}
						else
						{
							throw new Exception(CONST_INVALID_BATCH_TYPE);
						}
					case ADJUSTMENT:
					case ADJUDICATION:
					case REPLENISH:
					case REVERSAL:
					case PARTIALREVERSAL:
					case TRANSFER:
						UUID transactionUuid = realItem.getUuid();
						String sessionID = sessionData.getServerSessionID();
						transactionService.processWorkflowTransaction(transactionUuid, sessionID);
						break;
					case WORKFLOWREQUEST:
						break;
					default:
						break;

				}

				realItem.setState(WorkItem.STATE_COMPLETED);
				realItem.setStatus(ApplicationEnum.BatchStatusEnum.COMPLETE.getTsState());
				updateRequestStatus(realItem, ApplicationEnum.BatchStatusEnum.COMPLETE);
				break;
			case DECLINE:
				realItem.setState(WorkItem.STATE_DECLINED);
				realItem.setStatus(ApplicationEnum.BatchStatusEnum.DECLINED.getTsState());
				updateRequestStatus(realItem, ApplicationEnum.BatchStatusEnum.DECLINED);
				break;
			case HOLD:
				realItem.setState(WorkItem.STATE_ON_HOLD);
				realItem.setStatus(ApplicationEnum.BatchStatusEnum.ONHOLD.getTsState());
				updateRequestStatus(realItem, ApplicationEnum.BatchStatusEnum.ONHOLD);
				break;
			case UNHOLD:
				realItem.setState(WorkItem.STATE_IN_PROGRESS);
				realItem.setStatus(ApplicationEnum.BatchStatusEnum.PENDING_AUTHORIZATION.getTsState());
				updateRequestStatus(realItem, ApplicationEnum.BatchStatusEnum.PENDING_AUTHORIZATION);
				break;
			case UPDATE:
				break;
			case CANCELLED:
				realItem.setState(WorkItem.STATE_CANCELLED);
				realItem.setStatus(ApplicationEnum.BatchStatusEnum.CANCELLED.getTsState());
				updateRequestStatus(realItem, ApplicationEnum.BatchStatusEnum.CANCELLED);
				break;
			case EXECUTEWORKFLOW:
				switch (realItem.getState())
				{
					case WorkItem.STATE_NEW:
						newType = WorkflowRequestType.valueOf(realItem.getUri().trim().toUpperCase());
						switch (newType)
						{
							case ADJUSTMENT:
								GuiAdjustmentRequest adjustmentRequest = mapper.readValue(realItem.getRequest(), GuiAdjustmentRequest.class);
								adjustmentRequest.setSessionID(sessionData.getServerSessionID()); // Set session ID to ID of user creating the actual request.
								adjustmentRequest.setAgentId(sessionData.getAgentId());
								adjustmentRequest.setWebUserId(sessionData.getWebUserId());
								realItem.setRequest(mapper.writeValueAsString(adjustmentRequest));
								realItem.setState(WorkItem.STATE_IN_PROGRESS);
								realItem.setStatus(ApplicationEnum.BatchStatusEnum.PROCESSING.getTsState());
								break;
							case ADJUDICATION:
								GuiAdjudicateRequest adjudicationRequest = mapper.readValue(realItem.getRequest(), GuiAdjudicateRequest.class);
								adjudicationRequest.setSessionID(sessionData.getServerSessionID()); // Set session ID to ID of user creating the actual request.
								adjudicationRequest.setAgentId(sessionData.getAgentId());
								adjudicationRequest.setWebUserId(sessionData.getWebUserId());
								realItem.setRequest(mapper.writeValueAsString(adjudicationRequest));
								realItem.setState(WorkItem.STATE_IN_PROGRESS);
								realItem.setStatus(ApplicationEnum.BatchStatusEnum.PROCESSING.getTsState());
								break;
							case PARTIALREVERSAL:
								break;
							case REPLENISH:
								break;
							case REVERSAL:
								break;
							case TRANSFER:
								GuiTransferRequest transferRequest = mapper.readValue(realItem.getRequest(), GuiTransferRequest.class);
								transferRequest.setSessionID(sessionData.getServerSessionID()); // Set session ID to ID of user creating the actual request.
								transferRequest.setAgentId(sessionData.getAgentId());
								transferRequest.setWebUserId(sessionData.getWebUserId());
								realItem.setRequest(mapper.writeValueAsString(transferRequest));
								realItem.setState(WorkItem.STATE_IN_PROGRESS);
								realItem.setStatus(ApplicationEnum.BatchStatusEnum.PROCESSING.getTsState());
								break;
							case WORKFLOWREQUEST:
								break;
							default:
								break;

						}
						break;
					case WorkItem.STATE_IN_PROGRESS:
						UUID transactionUuid = null;
						WorkflowRequestType progressType = WorkflowRequestType.valueOf(realItem.getUri().trim().toUpperCase());
						switch (progressType)
						{
							case ADJUSTMENT:
								GuiAdjustmentRequest adjustmentRequest = mapper.readValue(realItem.getRequest(), GuiAdjustmentRequest.class);
								sess = sessionService.getWorkItemSession(updatedItem.getUuid());
								adjustmentRequest.setCoSignatorySessionID(sess.getSessionID());
								transactionUuid = transactionService.createTransaction(adjustmentRequest);
								transactionService.processWorkflowTransaction(transactionUuid, sess.getSessionID());
								realItem.setState(WorkItem.STATE_COMPLETED);
								realItem.setStatus(ApplicationEnum.BatchStatusEnum.COMPLETE.getTsState());
								break;
							case ADJUDICATION:
								GuiAdjudicateRequest adjudicationRequest = mapper.readValue(realItem.getRequest(), GuiAdjudicateRequest.class);
								sess = sessionService.getWorkItemSession(updatedItem.getUuid());
								adjudicationRequest.setCoSignatorySessionID(sess.getSessionID());
								transactionUuid = transactionService.createTransaction(adjudicationRequest);
								transactionService.processWorkflowTransaction(transactionUuid, sess.getSessionID());
								realItem.setState(WorkItem.STATE_COMPLETED);
								realItem.setStatus(ApplicationEnum.BatchStatusEnum.COMPLETE.getTsState());
								break;
							case PARTIALREVERSAL:
								break;
							case REPLENISH:
								break;
							case REVERSAL:
								break;
							case TRANSFER:
								GuiTransferRequest transferRequest = mapper.readValue(realItem.getRequest(), GuiTransferRequest.class);
								sess = sessionService.getWorkItemSession(updatedItem.getUuid());
								transferRequest.setCoSignatorySessionID(sess.getSessionID());
								transactionUuid = transactionService.createTransaction(transferRequest);
								transactionService.processWorkflowTransaction(transactionUuid, sess.getSessionID());
								realItem.setState(WorkItem.STATE_COMPLETED);
								realItem.setStatus(ApplicationEnum.BatchStatusEnum.COMPLETE.getTsState());
								break;
							case WORKFLOWREQUEST:
								break;
							default:
								break;

						}
						break;
						default:
							break;
				}
				break;
			default:
				break;
		}
		realItem.setSmsOnChange(true);
		restTemplate.execute(restServerUrl, HttpMethod.PUT, realItem, Void.class);
	}

	private void updateRequestStatus(WorkItem realItem, BatchStatusEnum newState)
	{
		String requestString = realItem.getRequest();
		if (requestString != null)
		{
			try
			{
				String reason = null;
				GuiWorkflowRequest request = mapper.readValue(requestString, GuiWorkflowRequest.class);
				switch (request.getRequestType())
				{
					case ADJUSTMENT:
						GuiAdjustmentRequest adj = request.getAdjustment();
						reason = adj.getReason();
						break;
					case BATCHUPLOAD:
						GuiBatchUploadRequest upload = request.getBatchUpload();
						reason = upload.getReason();
						int batchId = upload.getBatchID();
						batchImportService.updateState(batchId, newState);
						break;
					case PARTIALREVERSAL:
						reason = ((PartialReversalRequest) request.getReversal()).getReason();
						break;
					case REVERSAL:
						reason = request.getReversal().getReason();
						break;
					case ADJUDICATION:
						request.getAdjudication();
						logger.debug("updateRequestStatus() called, retrieved Adjudication, but ignored the result");
						//reason = reversal.getReason();
						break;
					case REPLENISH:
						break;
					case TRANSFER:
						break;
					case WORKFLOWREQUEST:
						break;
					default:
						break;
				}
				if (newState == BatchStatusEnum.PENDING_AUTHORIZATION)
				{
					realItem.setReason(reason);
				}
			}
			catch(Exception ex)
			{
				logger.error("", ex);
			}
		}
	}

	public void delete(String id) throws Exception
	{
		restTemplate.execute(restServerUrl+"/"+id, HttpMethod.DELETE, Void.class);
	}

	private WorkItem workItemFromRequest(GuiWebUser webUser, GuiReplenishRequest request) throws Exception
	{
		WorkItem workItem = new WorkItem();
		UUID uuid = UUID.randomUUID();
		workItem.setUuid(uuid);

		workItem.setState(WorkItem.STATE_NEW);
		workItem.setStatus(ApplicationEnum.BatchStatusEnum.NEW.getTsState());

		workItem.setType(WorkItem.TYPE_AUTHENTICATION_REQUEST);
		workItem.setCreationTime(new Date());

		workItem.setCreatedByWebUserID(webUser.getId());


		workItem.setUri("transaction/replenish");

		try
		{
			int userId = Integer.parseInt(request.getAuthorizedBy());
			workItem.setCreatedForWebUserID(userId);
		}
		catch(Exception ex)
		{
			workItem.setCreatedForPermissionID(permissionService.getPermissionIdByName(Permission.GROUP_TRANSACTIONS, Permission.PERM_AUTHORISE_REPLENISH));
		}

		if (request.getLanguage().equals("en"))
		{
			WorkFlowMessageUtility.addEnglishDescription(workItem, webUser, request, WorkflowRequestType.REPLENISH);
		}
		else
		{
			WorkFlowMessageUtility.addFrenchDescription(workItem, webUser, request, WorkflowRequestType.REPLENISH);
		}

		return workItem;
	}

	private WorkItem workItemFromRequest(GuiWebUser webUser, GuiTransferRequest request) throws Exception
	{
		WorkItem workItem = new WorkItem();
		UUID uuid = UUID.randomUUID();
		workItem.setUuid(uuid);

		workItem.setState(WorkItem.STATE_NEW);
		workItem.setStatus(ApplicationEnum.BatchStatusEnum.NEW.getTsState());
		workItem.setType(WorkItem.TYPE_AUTHENTICATION_REQUEST);
		workItem.setCreationTime(new Date());

		workItem.setCreatedByWebUserID(webUser.getId());


		workItem.setUri("transaction/transfer");

		try
		{
			int userId = Integer.parseInt(request.getAuthorizedBy());
			workItem.setCreatedForWebUserID(userId);
		}
		catch(Exception ex)
		{
			workItem.setCreatedForPermissionID(permissionService.getPermissionIdByName(Permission.GROUP_TRANSACTIONS, Permission.PERM_AUTHORISE_TRANSFER_FROM_ROOT_ACCOUNT));
		}

		if (request.getLanguage().equals("en"))
		{
			WorkFlowMessageUtility.addEnglishDescription(workItem, webUser, request, WorkflowRequestType.TRANSFER);
		}
		else
		{
			WorkFlowMessageUtility.addFrenchDescription(workItem, webUser, request, WorkflowRequestType.TRANSFER);
		}

		return workItem;
	}

	private WorkItem workItemFromRequest(GuiWebUser webUser, GuiAdjustmentRequest request) throws Exception
	{
		WorkItem workItem = new WorkItem();
		UUID uuid = UUID.randomUUID();
		workItem.setUuid(uuid);


		workItem.setState(WorkItem.STATE_NEW);
		workItem.setStatus(ApplicationEnum.BatchStatusEnum.NEW.getTsState());
		workItem.setType(WorkItem.TYPE_AUTHENTICATION_REQUEST);
		workItem.setCreationTime(new Date());
		workItem.setReason(request.getReason());

		workItem.setCreatedByWebUserID(webUser.getId());

		workItem.setUri("transaction/adjust");

		try
		{
			int userId = Integer.parseInt(request.getAuthorizedBy());
			workItem.setCreatedForWebUserID(userId);
		}
		catch(Exception ex)
		{
			workItem.setCreatedForPermissionID(permissionService.getPermissionIdByName(Permission.GROUP_TRANSACTIONS, Permission.PERM_AUTHORISE_ADJUST));
		}

		if (request.getLanguage().equals("en"))
		{
			WorkFlowMessageUtility.addEnglishDescription(workItem, webUser, request, agentService.getAgent(request.getAgentID()), WorkflowRequestType.ADJUSTMENT);
		}
		else
		{
			WorkFlowMessageUtility.addFrenchDescription(workItem, webUser, request, agentService.getAgent(request.getAgentID()), WorkflowRequestType.ADJUSTMENT);
		}

		return workItem;
	}

	private WorkItem workItemFromRequest(GuiWebUser webUser, GuiAdjudicateRequest request) throws Exception {
		WorkItem workItem = new WorkItem();
		UUID uuid = UUID.randomUUID();
		workItem.setUuid(uuid);

		TransactionEx tdr = tdrService.getTransactionExFromNo(request.getTransactionNumber());
		request.setAmount(tdr.getAmount());

		workItem.setWorkType(request.getTransactionNumber());
		workItem.setState(WorkItem.STATE_NEW);
		workItem.setStatus(ApplicationEnum.BatchStatusEnum.NEW.getTsState());
		workItem.setType(WorkItem.TYPE_AUTHENTICATION_REQUEST);
		workItem.setCreationTime(new Date());

		//workItem.setReason(request.getReason());

		workItem.setCreatedByWebUserID(webUser.getId());

		workItem.setUri("transaction/adjudicate");

		try
		{
			int userId = Integer.parseInt(request.getAuthorizedBy());
			workItem.setCreatedForWebUserID(userId);
		}
		catch(Exception ex)
		{
			workItem.setCreatedForPermissionID(permissionService.getPermissionIdByName(Permission.GROUP_TRANSACTIONS, Permission.PERM_AUTHORISE_ADJUDICATE));
		}

		if (request.getLanguage().equals("en"))
		{
			WorkFlowMessageUtility.addEnglishDescription(workItem, webUser, request, tdr, WorkflowRequestType.ADJUDICATION);
		}
		else
		{
			WorkFlowMessageUtility.addFrenchDescription(workItem, webUser, request, tdr, WorkflowRequestType.ADJUDICATION);
		}

		return workItem;
	}

	private WorkItem workItemFromRequest(GuiWebUser webUser, GuiReversalCoAuthRequest request) throws Exception
	{
		WorkItem workItem = new WorkItem();
		UUID uuid = UUID.randomUUID();
		workItem.setUuid(uuid);


		workItem.setState(WorkItem.STATE_NEW);
		workItem.setStatus(ApplicationEnum.BatchStatusEnum.NEW.getTsState());
		workItem.setType(WorkItem.TYPE_AUTHENTICATION_REQUEST);
		workItem.setCreationTime(new Date());
		workItem.setReason(request.getReason());

		workItem.setCreatedByWebUserID(webUser.getId());

		workItem.setUri((request.getType() == GuiReversalCoAuthRequest.ReversalType.FULL)?"transaction/reversal":"transaction/partialreversal");

		try
		{
			int userId = Integer.parseInt(request.getAuthorizedBy());
			workItem.setCreatedForWebUserID(userId);
		}
		catch(Exception ex)
		{
			workItem.setCreatedForPermissionID(permissionService.getPermissionIdByName(Permission.GROUP_TRANSACTIONS, Permission.PERM_AUTHORISE_REVERSE));
		}

		if (request.getLanguage().equals("en"))
		{
			WorkFlowMessageUtility.addEnglishDescription(workItem, webUser, request, request.getTransactionNumber(), WorkflowRequestType.ADJUSTMENT);
		}
		else
		{
			WorkFlowMessageUtility.addFrenchDescription(workItem, webUser, request, request.getTransactionNumber(), WorkflowRequestType.ADJUSTMENT);
		}

		return workItem;
	}

	private WorkItem workItemFromRequest(GuiWebUser webUser, GuiBatchUploadRequest request) throws Exception
	{
		WorkItem workItem = new WorkItem();
		UUID uuid = UUID.randomUUID();
		workItem.setUuid(uuid);


		workItem.setState(WorkItem.STATE_NEW);
		workItem.setStatus(ApplicationEnum.BatchStatusEnum.NEW.getTsState());
		workItem.setType(WorkItem.TYPE_AUTHENTICATION_REQUEST);
		workItem.setCreationTime(new Date());
		workItem.setReason(request.getReason());

		workItem.setCreatedByWebUserID(webUser.getId());


		workItem.setUri("transaction/batchupload");

		try
		{
			int userId = Integer.parseInt(request.getAuthorizedBy());
			workItem.setCreatedForWebUserID(userId);
		}
		catch(Exception ex)
		{
			Integer permissionId = permissionService.getPermissionIdByName(Permission.GROUP_TRANSACTIONS, Permission.PERM_AUTHORISE_ADJUST);
			workItem.setCreatedForPermissionID(permissionId);
		}

		if (request.getLanguage().equals("en"))
		{
			WorkFlowMessageUtility.addEnglishDescription(workItem, webUser, request, WorkflowRequestType.BATCHUPLOAD);
		}
		else
		{
			WorkFlowMessageUtility.addFrenchDescription(workItem, webUser, request, WorkflowRequestType.BATCHUPLOAD);
		}

		return workItem;
	}

	private WorkItem workItemFromRequest(GuiWebUser webUser, GuiWorkflowRequest request) throws Exception
	{
		WorkItem workItem = new WorkItem();
		UUID uuid = UUID.randomUUID();
		workItem.setUuid(uuid);

		workItem.setState(WorkItem.STATE_NEW);
		workItem.setStatus(ApplicationEnum.BatchStatusEnum.NEW.getTsState());
		workItem.setType(WorkItem.TYPE_EXECUTE);
		workItem.setCreationTime(new Date());
		workItem.setReason(request.getReason());

		workItem.setCreatedByWebUserID(webUser.getId());

		workItem.setUri("transaction/"+request.getTaskType());
		switch (request.getTaskType())
		{
			case "replenish":
				workItem.setCreatedForPermissionID(permissionService.getPermissionIdByName(Permission.GROUP_TRANSACTIONS, Permission.PERM_AUTHORISE_REPLENISH));
				break;
			case "transfer":
				workItem.setCreatedForPermissionID(permissionService.getPermissionIdByName(Permission.GROUP_TRANSACTIONS, Permission.PERM_AUTHORISE_TRANSFER_FROM_ROOT_ACCOUNT));
				break;
			case "adjust":
				workItem.setCreatedForPermissionID(permissionService.getPermissionIdByName(Permission.GROUP_TRANSACTIONS, Permission.PERM_AUTHORISE_ADJUST));
				break;
			case "reverse":
			case "partialreverse":
				workItem.setCreatedForPermissionID(permissionService.getPermissionIdByName(Permission.GROUP_TRANSACTIONS, Permission.PERM_AUTHORISE_REVERSE));
				break;
			case "adjudicate":
				workItem.setCreatedForPermissionID(permissionService.getPermissionIdByName(Permission.GROUP_TRANSACTIONS, Permission.PERM_ADJUDICATE));
				break;
			default:
				workItem.setCreatedForPermissionID(permissionService.getPermissionIdByName(Permission.GROUP_TRANSACTIONS, Permission.PERM_AUTHORISE_REPLENISH));
				break;
		}

		String destinationAccount = request.getDestination();
		try
		{
			GuiWebUser destinationWebUser = webUserService.getGuiWebUser(destinationAccount);
			if (request.getLanguage().equals("en"))
			{
				WorkFlowMessageUtility.addEnglishDescription(workItem, webUser, destinationWebUser, request, WorkflowRequestType.WORKFLOWREQUEST);
			}
			else
			{
				WorkFlowMessageUtility.addFrenchDescription(workItem, webUser, destinationWebUser, request, WorkflowRequestType.WORKFLOWREQUEST);
			}
		}
		catch (Exception ex) {};


		return workItem;
	}

	private GuiReplenishRequest generateSingle(GuiWebUser webUser, GuiReplenishRequest request) throws Exception
	{
		 WorkItem workItem = workItemFromRequest(webUser, request);

		GuiWorkflowRequest workflowRequest = new GuiWorkflowRequest();
		workflowRequest.setRequestType(WorkflowRequestType.REPLENISH);
		workflowRequest.setCoSignForSessionID(this.sessionData.getServerSessionID());
		workflowRequest.setReplenish(request);
		workItem.setRequest(mapper.writeValueAsString(workflowRequest));

		create(workItem);
		request.setUuid(workItem.getUuid().toString());

		return request;
	}

	private GuiTransferRequest generateSingle(GuiWebUser webUser, GuiTransferRequest request) throws Exception
	{
		 WorkItem workItem = workItemFromRequest(webUser, request);

		GuiWorkflowRequest workflowRequest = new GuiWorkflowRequest();
		workflowRequest.setRequestType(WorkflowRequestType.TRANSFER);
		workflowRequest.setCoSignForSessionID(this.sessionData.getServerSessionID());
		workflowRequest.setTransfer(request);
		workItem.setRequest(mapper.writeValueAsString(workflowRequest));

		create(workItem);
		request.setUuid(workItem.getUuid().toString());

		return request;
	}

	private GuiAdjustmentRequest generateSingle(GuiWebUser webUser, GuiAdjustmentRequest request) throws Exception
	{
		WorkItem workItem = workItemFromRequest(webUser, request);

		GuiWorkflowRequest workflowRequest = new GuiWorkflowRequest();
		workflowRequest.setRequestType(WorkflowRequestType.ADJUSTMENT);
		workflowRequest.setCoSignForSessionID(sessionData.getServerSessionID());
		workflowRequest.setAdjustment(request);
		workItem.setRequest(mapper.writeValueAsString(workflowRequest));
		workItem.setOwnerSession(sessionData.getServerSessionID());

		create(workItem);
		request.setUuid(workItem.getUuid().toString());

		return request;
	}

	private GuiAdjudicateRequest generateSingle(GuiWebUser webUser, GuiAdjudicateRequest request) throws Exception {
		WorkItem workItem = workItemFromRequest(webUser, request);

		GuiWorkflowRequest workflowRequest = new GuiWorkflowRequest();
		workflowRequest.setRequestType(WorkflowRequestType.ADJUDICATION);
		workflowRequest.setCoSignForSessionID(sessionData.getServerSessionID());
		workflowRequest.setAdjudication(request);
		workItem.setRequest(mapper.writeValueAsString(workflowRequest));
		workItem.setOwnerSession(sessionData.getServerSessionID());

		create(workItem);
		request.setUuid(workItem.getUuid().toString());

		return request;
	}

	private GuiReversalCoAuthRequest generateSingle(GuiWebUser webUser, GuiReversalCoAuthRequest request) throws Exception
	{
		WorkItem workItem = workItemFromRequest(webUser, request);

		GuiWorkflowRequest workflowRequest = new GuiWorkflowRequest();
		workflowRequest.setRequestType((request.getType() == GuiReversalCoAuthRequest.ReversalType.FULL)?WorkflowRequestType.REVERSAL:WorkflowRequestType.PARTIALREVERSAL);
		workflowRequest.setCoSignForSessionID(this.sessionData.getServerSessionID());
		workflowRequest.setReversal(request);
		workItem.setRequest(mapper.writeValueAsString(workflowRequest));
		workItem.setOwnerSession(sessionData.getServerSessionID());

		create(workItem);
		request.setUuid(workItem.getUuid().toString());
		return request;
	}

	private GuiBatchUploadRequest generateSingle(GuiWebUser webUser, GuiBatchUploadRequest request) throws Exception
	{
		WorkItem workItem = workItemFromRequest(webUser, request);

		GuiWorkflowRequest workflowRequest = new GuiWorkflowRequest();
		workflowRequest.setRequestType(WorkflowRequestType.BATCHUPLOAD);
		workflowRequest.setCoSignForSessionID(this.sessionData.getServerSessionID());
		workflowRequest.setBatchUpload(request);
		workItem.setRequest(mapper.writeValueAsString(workflowRequest));

		create(workItem);
		request.setUuid(workItem.getUuid().toString());

		return request;
	}

	private GuiWorkflowRequest generateSingle(GuiWebUser webUser, GuiWorkflowRequest request) throws Exception
	{
		WorkItem workItem = workItemFromRequest(webUser, request);

		request.setRequestType(WorkflowRequestType.WORKFLOWREQUEST);

		GuiWebUser destinationWebUser = null;
		String destinationAccount = request.getDestination();
		if (destinationAccount != null && destinationAccount.trim().length() > 0)
		{
			destinationWebUser = webUserService.getGuiWebUser(destinationAccount);
		}

		workItem.setUri(request.getTaskType());
		switch(request.getTaskType())
		{
			case "replenish":
				GuiReplenishRequest replenish = new GuiReplenishRequest();
				replenish.setAmount(request.getAmount());
				replenish.setBonusProvision(new BigDecimal(request.getBonus()));
				replenish.setLanguage(request.getLanguage());
				replenish.setSeperators(request.getSeperators());
				replenish.setSessionID(sessionData.getServerSessionID());
				workItem.setRequest(mapper.writeValueAsString(replenish));
				break;
			case "transfer":
				GuiTransferRequest transfer = new GuiTransferRequest();
				transfer.setAmount(request.getAmount());
				transfer.setLanguage(request.getLanguage());
				transfer.setSeperators(request.getSeperators());
				transfer.setSessionID(sessionData.getServerSessionID());
				transfer.setTargetMSISDN(destinationWebUser.getMobileNumber());
				workItem.setRequest(mapper.writeValueAsString(transfer));
				break;
			case "adjust":
				GuiAdjustmentRequest adjustment = new GuiAdjustmentRequest();
				adjustment.setAmount(request.getAmount());
				adjustment.setLanguage(request.getLanguage());
				adjustment.setSeperators(request.getSeperators());
				adjustment.setSessionID(sessionData.getServerSessionID());
				workItem.setRequest(mapper.writeValueAsString(adjustment));
				break;
			case "adjudicate":
				GuiAdjudicateRequest adjudication = new GuiAdjudicateRequest();
				//adjudication.setAmount(request.getAmount());
				adjudication.setTransactionNumber(request.getTransactionNumber());
				adjudication.setLanguage(request.getLanguage());
				adjudication.setSeperators(request.getSeperators());
				adjudication.setSessionID(sessionData.getServerSessionID());
				workItem.setRequest(mapper.writeValueAsString(adjudication));
				break;
			case "reverse":
			case "partialreverse":
			 	logger.warn("Performing Task Type: '{}' - but not handling it?", request.getTaskType());
				break;
			default:
				break;
		}

		create(workItem);
		request.setUuid(workItem.getUuid().toString());
		return request;
	}

	public GuiReplenishRequest createRequest(String username, GuiReplenishRequest request) throws Exception
	{
		GuiWebUser webUser = webUserService.getGuiWebUserByUsername(username);
		return generateSingle(webUser, request);
	}

	public GuiTransferRequest createRequest(String username, GuiTransferRequest request) throws Exception
	{
		GuiWebUser webUser = webUserService.getGuiWebUserByUsername(username);
		return generateSingle(webUser, request);
	}

	public WorkItem status(String transId) throws Exception
	{
		WorkItem response = null;
		response = restTemplate.execute(restServerUrl+"/uuid/"+transId, HttpMethod.GET, WorkItem.class);
		return response;
	}

	public Long listCount(int offset, int limit, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/*");
		RestRequestUtil.standardPaging(uri, offset, limit);
		if (sort != null && sort.length() > 0)RestRequestUtil.standardSorting(uri, sort);

		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
	}

	public WorkItem[] list(int offset, int limit, String sort) throws Exception
	{
		WorkItem[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardPaging(uri, offset, limit);
		RestRequestUtil.standardSorting(uri, sort);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, WorkItem[].class);
		return response;
	}

	public GuiWorkflowRequest createRequest(String username, GuiWorkflowRequest request) throws Exception
	{
		logger.info(mapper.writeValueAsString(request));
		GuiWebUser webUser = webUserService.getGuiWebUserByUsername(username);
		return generateSingle(webUser, request);
	}

	public Long countUserItems()  throws Exception
	{
		Long response = null;UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/*");

		StringBuilder filterString = new StringBuilder("createdByWebUserID='");
		filterString.append(String.valueOf(sessionData.getWebUserId()));
		filterString.append("'");

		RestRequestUtil.standardFilter(uri, filterString.toString());

		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
		return response;
	}

	public WorkItem[] listUserItems(Integer offset, Integer limit, String sort)  throws Exception
	{
		WorkItem[] response = null;UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);

		RestRequestUtil.standardPaging(uri, offset, limit);
		RestRequestUtil.standardSorting(uri, sort);

		StringBuilder filterString = new StringBuilder("createdByWebUserID='");
		filterString.append(String.valueOf(sessionData.getWebUserId()));
		filterString.append("'");

		RestRequestUtil.standardFilter(uri, filterString.toString());

		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, WorkItem[].class);
		return response;
	}

	public Long countInboxItems() throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/for_me/*");
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
	}

	public WorkItem[] listInboxItems(Integer offset, Integer limit, String sort)  throws Exception
	{
		WorkItem[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/for_me");

		RestRequestUtil.standardPaging(uri, offset, limit);

		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, WorkItem[].class);
		return response;
	}

	public Long countHistoryItems()  throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/my_history/*");
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
	}

	public WorkItem[] listHistoryItems(Integer offset, Integer limit, String sort)  throws Exception
	{
		WorkItem[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/my_history");

		RestRequestUtil.standardPaging(uri, offset, limit);

		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, WorkItem[].class);
		return response;
	}

	// ----- Workflow Configuration ---
	public WorkflowConfig getConfiguration() throws Exception
	{
		return restTemplate.execute(this.restServerUrl+ "/config", HttpMethod.GET, WorkflowConfig.class);
	}

	public void updateConfiguration(WorkflowConfig config) throws Exception
	{
		restTemplate.execute(this.restServerUrl+ "/config", HttpMethod.PUT, config, Void.class);
	}

	public GuiAdjustmentRequest createRequest(String username, GuiAdjustmentRequest request) throws Exception
	{
		GuiWebUser webUser = webUserService.getGuiWebUserByUsername(username);
		return generateSingle(webUser, request);
	}

	public GuiAdjudicateRequest createRequest(String username, GuiAdjudicateRequest request) throws Exception {
		GuiWebUser webUser = webUserService.getGuiWebUserByUsername(username);
		return generateSingle(webUser, request);
	}

	public GuiReversalCoAuthRequest createRequest(String username, GuiReversalCoAuthRequest request) throws Exception
	{
		GuiWebUser webUser = webUserService.getGuiWebUserByUsername(username);
		return generateSingle(webUser, request);
	}

	public GuiBatchUploadRequest createRequest(String username, GuiBatchUploadRequest request) throws Exception
	{
		GuiWebUser webUser = webUserService.getGuiWebUserByUsername(username);
		return generateSingle(webUser, request);
	}

	public ObjectNode sendOTPForItem(String username, String uuid) throws Exception
	{
		ObjectNode result = mapper.createObjectNode();
		WorkItem workItem = getWorkItem(uuid);
		if (workItem != null)
		{
			StringBuilder requestUri = new StringBuilder(this.restServerUrl);
			requestUri.append("/");
			requestUri.append(workItem.getId());
			requestUri.append("/generate_co_sign_otp");

			GenerateWorkItemCoSignOTPRequest otpRequest = new GenerateWorkItemCoSignOTPRequest();

			Session sess = sessionService.getWorkItemSession(uuid);// requester

			otpRequest.setCoSignForSessionID(sess.getSessionID());

			GenerateWorkItemCoSignOTPResponse response = restTemplate.postForObject(requestUri.toString(), otpRequest, GenerateWorkItemCoSignOTPResponse.class);
			if (response.getReturnCode().equals("SUCCESS"))
			{
				GuiWorkflowRequest workflowRequest = mapper.readValue(workItem.getRequest(), GuiWorkflowRequest.class);
				workflowRequest.setCoSignatoryTransactionID(response.getCoSignatoryTransactionID());
				workflowRequest.setCoSignForSessionID(otpRequest.getCoSignForSessionID());

				workItem.setRequest(mapper.writeValueAsString(workflowRequest));
				restTemplate.execute(restServerUrl, HttpMethod.PUT, workItem, Void.class);
				result.put("success", true);
				result.put("uuid", uuid);
			}
			else
			{
				result.put("success", false);
				result.put("uuid", uuid);
				result.put("error", "Unknown Error");
			}
		}
		return result;
	}

	private WorkItem findWorkItem(WorkItem[] items)
	{
		WorkItem result = null;
		if (items != null)
		{
			for (WorkItem item : items)
			{
				/*
				 * Purposely loop through all so as to return last item.  Should normally only be one item.
				 */
				String state = item.getState();
				if (state != null && !state.equals(WorkItem.STATE_COMPLETED) && !state.equals(WorkItem.STATE_CANCELLED))
				{
					result = item;
				}
			}
		}
		return result;
	}

	public WorkItem getWorkItemFromTransactionNumber(String number) throws Exception
	{
		WorkItem result = null;

		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		if (number != null && !number.isEmpty())
		{
			RestRequestUtil.standardFilter(uri, "workType='"+number+"'+uri='transaction/adjudicate'");
		}
		RestRequestUtil.withRecordCount(uri, false);

		result = findWorkItem(restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, WorkItem[].class));
		return result;
	}
}
