package hxc.services.vssim.engine;

import java.util.List;

import hxc.services.vssim.IVoucherInfo;
import hxc.services.vssim.model.ScheduledTask;
import hxc.utils.protocol.vsip.GetLoadVoucherBatchFileTaskInfoCallRequest;
import hxc.utils.protocol.vsip.GetLoadVoucherBatchFileTaskInfoCallResponse;
import hxc.utils.protocol.vsip.GetLoadVoucherBatchFileTaskInfoRequest;
import hxc.utils.protocol.vsip.GetLoadVoucherBatchFileTaskInfoResponse;
import hxc.utils.protocol.vsip.GetLoadVoucherBatchFileTaskInfoTaskData;
import hxc.utils.protocol.vsip.GetLoadVoucherBatchFileTaskInfoTasks;
import hxc.utils.protocol.vsip.LoadVoucherBatchFileRequest;
import hxc.utils.protocol.vsip.Protocol;

public class GetLoadVoucherBatchFileTaskInfo extends VoucherCallBase<GetLoadVoucherBatchFileTaskInfoResponse, GetLoadVoucherBatchFileTaskInfoRequest>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public GetLoadVoucherBatchFileTaskInfo(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "GetLoadVoucherBatchFileTaskInfo");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static GetLoadVoucherBatchFileTaskInfoCallResponse call(IVoucherInfo voucherInfo, GetLoadVoucherBatchFileTaskInfoCallRequest call, String operatorID)
	{
		GetLoadVoucherBatchFileTaskInfoCallResponse response = new GetLoadVoucherBatchFileTaskInfoCallResponse();
		response.setResponse(new GetLoadVoucherBatchFileTaskInfo(voucherInfo).execute(new GetLoadVoucherBatchFileTaskInfoResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public GetLoadVoucherBatchFileTaskInfoResponse exec(GetLoadVoucherBatchFileTaskInfoRequest request, String operatorID)
	{
		// Create Response
		GetLoadVoucherBatchFileTaskInfoResponse response = new GetLoadVoucherBatchFileTaskInfoResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_REQUEST);

		// Simulate
		try
		{
			response.setResponseCode(Protocol.RESPONSECODE_SUCCESS);
			List<ScheduledTask<LoadVoucherBatchFileRequest>> tasks = voucherInfo.getTaskInfo(request.getTaskId(), LoadVoucherBatchFileRequest.class);
			GetLoadVoucherBatchFileTaskInfoTasks[] replies = new GetLoadVoucherBatchFileTaskInfoTasks[tasks.size()];
			response.setTasks(replies);
			int index = 0;
			for (ScheduledTask<LoadVoucherBatchFileRequest> task : tasks)
			{
				GetLoadVoucherBatchFileTaskInfoTasks reply = new GetLoadVoucherBatchFileTaskInfoTasks();
				replies[index++] = reply;
				reply.setTaskId(task.getTaskId());
				reply.setTaskStatus(task.getTaskStatus());
				reply.setExecutionTime(task.getExecutionTime());
				reply.setOperatorId(task.getOperatorId());
				reply.setFailReason(task.getFailReason());
				reply.setAdditionalInfo(task.getAdditionalInfo());

				GetLoadVoucherBatchFileTaskInfoTaskData data = new GetLoadVoucherBatchFileTaskInfoTaskData();
				reply.setTaskData(new GetLoadVoucherBatchFileTaskInfoTaskData[] { data });
				data.setFilename(task.getRequest().getFilename());
				data.setBatchId(task.getRequest().getBatchId());
				data.setNewState(task.getRequest().getNewState());
				data.setInitialVoucherState(Protocol.STATE_AVAILABLE); // TODO Not well understood
			}
		}
		catch (Exception e)
		{
			return exitWith(response, Protocol.RESPONSECODE_DATABASE_ERROR);
		}

		// Validate Response
		if (response.getResponseCode() != Protocol.RESPONSECODE_SUCCESS)
			return response;
		else if (!response.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_RESPONSE);

		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
}
