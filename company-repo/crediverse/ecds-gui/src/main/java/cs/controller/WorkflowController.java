package cs.controller;

import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.dto.GuiAdjudicateRequest;
import cs.dto.GuiAdjustmentRequest;
import cs.dto.GuiBatchUploadRequest;
import cs.dto.GuiDataTable;
import cs.dto.GuiDataTableRequest;
import cs.dto.GuiReplenishRequest;
import cs.dto.GuiReversalCoAuthRequest;
import cs.dto.GuiStatusResponse;
import cs.dto.GuiTransferRequest;
import cs.dto.GuiWorkItem;
import cs.dto.GuiWorkItem.WorkItemAction;
import cs.dto.GuiWorkflowRequest;
import cs.dto.batch.BatchImport;
import cs.dto.security.LoginSessionData;
import cs.service.TypeConvertorService;
import cs.service.batch.BatchImportService;
import cs.service.workflow.WorkFlowService;
import hxc.ecds.protocol.rest.WorkItem;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController
{
	@Autowired
	private WorkFlowService workflowService;

	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private TypeConvertorService typeConvertorService;

	@Autowired
	private BatchImportService batchImportService;

	//@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiWorkItem[] getWorkflowItems() throws Exception
	{
		GuiWorkItem[] workItemList = typeConvertorService.getGuiWorkItemFromWorkItem(workflowService.getWorkItemList());
		return workItemList;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable list(@RequestParam Map<String, String> params) throws Exception
	{
		GuiDataTable responseData = null;
		GuiDataTableRequest dtr = new GuiDataTableRequest( params );
		if ( dtr.getStart() != null && dtr.getLength() != null )
		{
			Long count = workflowService.listCount(dtr.getStart(), dtr.getLength(), "");
			GuiWorkItem[] workItemList = typeConvertorService.getGuiWorkItemFromWorkItem(workflowService.list(dtr.getStart(), dtr.getLength(), ""));

			responseData = new GuiDataTable(workItemList, count.intValue());
		}
		return responseData;
	}

	@RequestMapping(value="uuid/{uuid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiWorkItem getWorkItemFromUUID(@PathVariable("uuid") String uuid) throws Exception
	{
		return typeConvertorService.getGuiWorkItemFromWorkItem(workflowService.getWorkItem(uuid));
	}

	@RequestMapping(value="detail/{uuid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getWorkItemDetail(@PathVariable("uuid") String uuid) throws Exception
	{
		return workflowService.getWorkItem(uuid).getRequest();
	}

	/*
	 * Get tasks created by me
	 */
	@RequestMapping(value="taskList", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable myTaskList(@RequestParam Map<String, String> params) throws Exception
	{
		GuiDataTable responseData = null;
		GuiDataTableRequest dtr = new GuiDataTableRequest( params );
		if ( dtr.getStart() != null && dtr.getLength() != null )
		{
			Long count = workflowService.countUserItems();
			GuiWorkItem[] workItemList = typeConvertorService.getGuiWorkItemFromWorkItem(workflowService.listUserItems(dtr.getStart(), dtr.getLength(), "creationTime-"));
			responseData = new GuiDataTable(workItemList, count.intValue());
		}
		return responseData;
	}

	/*
	 * Get tasks not created by me but that I can authorize (Needs my permissions).
	 */
	@RequestMapping(value="inBox", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable myInboxList(@RequestParam Map<String, String> params) throws Exception
	{
		GuiDataTable responseData = null;
		GuiDataTableRequest dtr = new GuiDataTableRequest( params );
		if ( dtr.getStart() != null && dtr.getLength() != null )
		{
			Long count = workflowService.countInboxItems();
			GuiWorkItem[] workItemList = typeConvertorService.getGuiWorkItemFromWorkItem(workflowService.listInboxItems(dtr.getStart(), dtr.getLength(), ""));

			responseData = new GuiDataTable(workItemList, count.intValue());
		}
		return responseData;
	}

	@RequestMapping(value="history", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable myHistoryList(@RequestParam Map<String, String> params) throws Exception
	{
		GuiDataTable responseData = null;
		GuiDataTableRequest dtr = new GuiDataTableRequest( params );
		if ( dtr.getStart() != null && dtr.getLength() != null )
		{
			Long count = workflowService.countHistoryItems();
			GuiWorkItem[] workItemList = typeConvertorService.getGuiWorkItemFromWorkItem(workflowService.listHistoryItems(dtr.getStart(), dtr.getLength(), ""));

			responseData = new GuiDataTable(workItemList, count.intValue());
		}
		return responseData;
	}

	@RequestMapping(value="{id}", method = RequestMethod.DELETE)
	public String delete(@PathVariable("id") String id) throws Exception
	{
		workflowService.delete(id);
		return "{}";
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiWorkItem create(@RequestBody(required = true) GuiWorkItem guiWorkItem, Locale locale) throws Exception
	{
		WorkItem workItem = typeConvertorService.getWorkItemFromGuiWorkItem(guiWorkItem);
		workflowService.create(workItem);
		return guiWorkItem;
	}

	@RequestMapping(method = RequestMethod.PUT)
	public GuiWorkItem update(@RequestBody(required = true) GuiWorkItem guiWorkItem, Locale locale) throws Exception
	{
		WorkItemAction action = guiWorkItem.getAction();

		WorkItem workItem = typeConvertorService.getWorkItemFromGuiWorkItem(guiWorkItem);
		String enteredPin = guiWorkItem.getWorkItemOTP();
		workflowService.update(action, workItem, enteredPin);

		GuiStatusResponse.operationSuccessful();
		return typeConvertorService.getGuiWorkItemFromWorkItem(workItem);
	}

	@RequestMapping(value="replenish", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiReplenishRequest createReplenish(@RequestBody(required = true) GuiReplenishRequest request, Locale locale) throws Exception
	{
		return workflowService.createRequest(sessionData.getUsername(), request);
	}

	@RequestMapping(value="transfer", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiTransferRequest createtransfer(@RequestBody(required = true) GuiTransferRequest request, Locale locale) throws Exception
	{
		return workflowService.createRequest(sessionData.getUsername(), request);
	}

	@RequestMapping(value="adjust", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiAdjustmentRequest createadjustment(@RequestBody(required = true) GuiAdjustmentRequest request, Locale locale) throws Exception
	{
		String username = sessionData.getUsername();
		return workflowService.createRequest(username, request);
	}

	@RequestMapping(value="adjudicate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiAdjudicateRequest createadjudication(@RequestBody(required = true) GuiAdjudicateRequest request, Locale locale) throws Exception
	{
		String username = sessionData.getUsername();
		return workflowService.createRequest(username, request);
	}

	@RequestMapping(value="reversal", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiReversalCoAuthRequest createreversal(@RequestBody(required = true) GuiReversalCoAuthRequest request, Locale locale) throws Exception
	{
		return workflowService.createRequest(sessionData.getUsername(), request);
	}

	@RequestMapping(value="batch", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public BatchImport createbatch(@RequestBody(required = true) GuiBatchUploadRequest request, Locale locale) throws Exception
	{
		return batchImportService.queueUnauthorized(request);
	}

	@RequestMapping(value="status", method = RequestMethod.GET)
	public GuiWorkItem status(@PathVariable("transId") String transId) throws Exception
	{
		WorkItem workItem = workflowService.status(transId);
		return typeConvertorService.getGuiWorkItemFromWorkItem(workItem);
	}

	@RequestMapping(value="create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiWorkflowRequest createWorkItem(@RequestBody(required = true) GuiWorkflowRequest request, Locale locale) throws Exception
	{
		GuiWorkflowRequest workflowRequest = workflowService.createRequest(sessionData.getUsername(), request);
		return workflowRequest;
	}

	@RequestMapping(value="sendotp/{jobId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ObjectNode sendOTPForItem(@PathVariable("jobId") String jobId, Locale locale) throws Exception
	{
		ObjectNode response = workflowService.sendOTPForItem(sessionData.getUsername(), jobId);
		return response;
	}
}
