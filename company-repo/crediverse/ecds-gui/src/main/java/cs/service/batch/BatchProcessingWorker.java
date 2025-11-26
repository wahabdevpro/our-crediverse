package cs.service.batch;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.config.RestServerConfiguration;
import cs.dto.GuiBatchUploadRequest;
import cs.dto.GuiWorkItem.WorkItemStatus;
import cs.service.workflow.WorkFlowService;
import cs.template.BatchThreadWorker;
import cs.template.CsRestTemplate;
import hxc.ecds.protocol.rest.BatchUploadRequest;
import hxc.ecds.protocol.rest.BatchUploadResponse;
import hxc.ecds.protocol.rest.WorkItem;

public class BatchProcessingWorker extends BatchThreadWorker
{
	private static Logger logger = LoggerFactory.getLogger(BatchProcessingWorker.class);
	private GuiBatchUploadRequest request;
	private String restServerUrl;

	@Autowired
	private CsRestTemplate restTemplate;

	@Autowired
	private RestServerConfiguration restServerConfig;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private WorkFlowService workFlowService;

	@PostConstruct
	public void configure()
	{
		this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getBatchuploadurl();
	}

	protected void setRequest(GuiBatchUploadRequest request)
	{
		this.request = request;
	}

	private void updateWorkItem(Object response, String state)
	{
		try
		{
			WorkItem item = workFlowService.getWorkItem(request.getWorkItemId().toString());
			if (item != null)
			{
				item.setResponse(mapper.writeValueAsString(response));
				item.setState(state);
				workFlowService.update(item);
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
	}

	@Override
	public void onRun()
	{
		if (request == null) throw new IllegalArgumentException("Cannot run without a request to perform");
		BatchUploadRequest uploadRequest = new BatchUploadRequest();
		BeanUtils.copyProperties(request, uploadRequest);
		request.setLast(false);

		try
		{
			BatchUploadResponse response = restTemplate.postForObject(this.restServerUrl, uploadRequest, BatchUploadResponse.class);
			updateWorkItem(response, WorkItem.STATE_COMPLETED);
		}
		catch (Exception e)
		{
			ObjectNode error = mapper.createObjectNode();
			error.put("status",  WorkItemStatus.FAILED.toString());
			if (e.getMessage() != null) error.put("reason",  e.getMessage());
			if (e.getCause()   != null) error.put("cause",  e.getCause().getMessage());
			error.put("code",  411);
			updateWorkItem(e, WorkItem.STATE_FAILED);

			logger.error("", e);
		}
	}
}
