package hxc.services.vssim.engine;

import java.util.List;

import hxc.services.vssim.IVoucherInfo;
import hxc.services.vssim.model.ScheduledTask;
import hxc.utils.protocol.vsip.GenerateVoucherUsageReportRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherUsageReportTaskInfoCallRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherUsageReportTaskInfoCallResponse;
import hxc.utils.protocol.vsip.GetGenerateVoucherUsageReportTaskInfoRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherUsageReportTaskInfoResponse;
import hxc.utils.protocol.vsip.GetGenerateVoucherUsageReportTaskInfoTaskData;
import hxc.utils.protocol.vsip.GetGenerateVoucherUsageReportTaskInfoTasks;
import hxc.utils.protocol.vsip.Protocol;

public class GetGenerateVoucherUsageReportTaskInfo extends VoucherCallBase<GetGenerateVoucherUsageReportTaskInfoResponse, GetGenerateVoucherUsageReportTaskInfoRequest>
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
	public GetGenerateVoucherUsageReportTaskInfo(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "GetGenerateVoucherUsageReportTaskInfo");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static GetGenerateVoucherUsageReportTaskInfoCallResponse call(IVoucherInfo voucherInfo, GetGenerateVoucherUsageReportTaskInfoCallRequest call, String operatorID)
	{
		GetGenerateVoucherUsageReportTaskInfoCallResponse response = new GetGenerateVoucherUsageReportTaskInfoCallResponse();
		response.setResponse(new GetGenerateVoucherUsageReportTaskInfo(voucherInfo).execute(new GetGenerateVoucherUsageReportTaskInfoResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public GetGenerateVoucherUsageReportTaskInfoResponse exec(GetGenerateVoucherUsageReportTaskInfoRequest request, String operatorID)
	{
		// Create Response
		GetGenerateVoucherUsageReportTaskInfoResponse response = new GetGenerateVoucherUsageReportTaskInfoResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_REQUEST);

		// Simulate
		try
		{
			response.setResponseCode(Protocol.RESPONSECODE_SUCCESS);
			List<ScheduledTask<GenerateVoucherUsageReportRequest>> tasks = voucherInfo.getTaskInfo(request.getTaskId(), GenerateVoucherUsageReportRequest.class);
			GetGenerateVoucherUsageReportTaskInfoTasks[] replies = new GetGenerateVoucherUsageReportTaskInfoTasks[tasks.size()];
			response.setTasks(replies);
			int index = 0;
			for (ScheduledTask<GenerateVoucherUsageReportRequest> task : tasks)
			{
				GetGenerateVoucherUsageReportTaskInfoTasks reply = new GetGenerateVoucherUsageReportTaskInfoTasks();
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

				GetGenerateVoucherUsageReportTaskInfoTaskData data = new GetGenerateVoucherUsageReportTaskInfoTaskData();
				reply.setTaskData(data);
				data.setFromTime(task.getRequest().getFromTime());
				data.setToTime(task.getRequest().getToTime());

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
