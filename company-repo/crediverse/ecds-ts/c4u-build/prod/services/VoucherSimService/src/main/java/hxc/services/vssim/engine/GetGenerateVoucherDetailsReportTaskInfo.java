package hxc.services.vssim.engine;

import java.util.List;

import hxc.services.vssim.IVoucherInfo;
import hxc.services.vssim.model.ScheduledTask;
import hxc.utils.protocol.vsip.GenerateVoucherDetailsReportRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherDetailsReportTaskInfoCallRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherDetailsReportTaskInfoCallResponse;
import hxc.utils.protocol.vsip.GetGenerateVoucherDetailsReportTaskInfoRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherDetailsReportTaskInfoResponse;
import hxc.utils.protocol.vsip.GetGenerateVoucherDetailsReportTaskInfoTaskData;
import hxc.utils.protocol.vsip.GetGenerateVoucherDetailsReportTaskInfoTasks;
import hxc.utils.protocol.vsip.Protocol;

public class GetGenerateVoucherDetailsReportTaskInfo extends VoucherCallBase<GetGenerateVoucherDetailsReportTaskInfoResponse, GetGenerateVoucherDetailsReportTaskInfoRequest>
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
	public GetGenerateVoucherDetailsReportTaskInfo(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "GetGenerateVoucherDetailsReportTaskInfo");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static GetGenerateVoucherDetailsReportTaskInfoCallResponse call(IVoucherInfo voucherInfo, GetGenerateVoucherDetailsReportTaskInfoCallRequest call, String operatorID)
	{
		GetGenerateVoucherDetailsReportTaskInfoCallResponse response = new GetGenerateVoucherDetailsReportTaskInfoCallResponse();
		response.setResponse(new GetGenerateVoucherDetailsReportTaskInfo(voucherInfo).execute(new GetGenerateVoucherDetailsReportTaskInfoResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public GetGenerateVoucherDetailsReportTaskInfoResponse exec(GetGenerateVoucherDetailsReportTaskInfoRequest request, String operatorID)
	{
		// Create Response
		GetGenerateVoucherDetailsReportTaskInfoResponse response = new GetGenerateVoucherDetailsReportTaskInfoResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_REQUEST);

		// Simulate
		try
		{
			response.setResponseCode(Protocol.RESPONSECODE_SUCCESS);
			List<ScheduledTask<GenerateVoucherDetailsReportRequest>> tasks = voucherInfo.getTaskInfo(request.getTaskId(), GenerateVoucherDetailsReportRequest.class);
			GetGenerateVoucherDetailsReportTaskInfoTasks[] replies = new GetGenerateVoucherDetailsReportTaskInfoTasks[tasks.size()];
			response.setTasks(replies);
			int index = 0;
			for (ScheduledTask<GenerateVoucherDetailsReportRequest> task : tasks)
			{
				GetGenerateVoucherDetailsReportTaskInfoTasks reply = new GetGenerateVoucherDetailsReportTaskInfoTasks();
				replies[index++] = reply;

				reply.setTaskId(task.getTaskId());
				reply.setTaskStatus(task.getTaskStatus());
				reply.setExecutionTime(task.getExecutionTime());
				reply.setOperatorId(task.getOperatorId());
				reply.setFilename(task.getFilename());
				reply.setFailReason(task.getFailReason());
				reply.setAdditionalInfo(task.getAdditionalInfo());
				reply.setRecurrence(task.getRecurrence());
				reply.setRecurrenceValue(task.getRecurrenceValue());

				GetGenerateVoucherDetailsReportTaskInfoTaskData data = new GetGenerateVoucherDetailsReportTaskInfoTaskData();
				reply.setTaskData(data);
				data.setBatchId(task.getRequest().getBatchId());
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
