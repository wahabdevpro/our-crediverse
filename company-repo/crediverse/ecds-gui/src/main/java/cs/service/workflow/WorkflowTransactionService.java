package cs.service.workflow;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import cs.config.RestServerConfiguration;
import cs.dto.GuiAdjustmentRequest;
import cs.dto.GuiWorkflowRequest;
import cs.dto.GuiWorkflowRequest.WorkflowRequestType;
import cs.service.SessionService;
import cs.service.TransactionService;
import cs.template.CsRestTemplate;
import hxc.ecds.protocol.rest.Session;
import hxc.ecds.protocol.rest.WorkItem;

@Service
public class WorkflowTransactionService
{
	@Autowired
	private CsRestTemplate restTemplate;

	@Autowired
	private RestServerConfiguration restServerConfig;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private ObjectMapper mapper;

	private String workflowServerUrl;

	@PostConstruct
	public void configure()
	{
		this.workflowServerUrl = restServerConfig.getRestServer() + restServerConfig.getWorkflowurl();
	}

	/*
	 * Obtain updated work item from TS, don't overwrite keys or request and response.
	 */
	private WorkItem getWorkItem(WorkItem updatedItem) throws Exception
	{
		WorkItem response = null;
		response = restTemplate.execute(workflowServerUrl+"/uuid/"+String.valueOf(updatedItem.getId()), HttpMethod.GET, WorkItem.class);
		BeanUtils.copyProperties(updatedItem, response, "request", "response", "id", "uuid", "uri");
		return response;
	}

	public void setWorkItemSession(Session session, WorkItem updatedItem, String error) throws Exception
	{
		GuiWorkflowRequest currentWorkflow = null;
		WorkItem realItem = getWorkItem(updatedItem);
		session = sessionService.getWorkItemSession(updatedItem.getUuid());
		if (session != null)
		{
			currentWorkflow = mapper.readValue(realItem.getRequest(), GuiWorkflowRequest.class);
			if (currentWorkflow != null && currentWorkflow.getRequestType() == WorkflowRequestType.ADJUSTMENT)
			{
				GuiAdjustmentRequest adjustmentRequest = currentWorkflow.getAdjustment();
				adjustmentRequest.setCoSignatorySessionID(session.getSessionID());


				UUID transactionUuid = transactionService.createTransaction(adjustmentRequest);
				transactionService.processWorkflowTransaction(transactionUuid, session.getSessionID());
			}
			else
			{
				throw new Exception(error);//CONST_INVALID_ADJUSTMENT_TYPE);
			}
		}
	}
}
