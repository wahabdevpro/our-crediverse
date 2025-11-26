package hxc.services.vssim.engine;

import java.util.List;

import hxc.services.vssim.IVoucherInfo;
import hxc.services.vssim.model.ScheduledTask;
import hxc.utils.protocol.vsip.GenerateVoucherDistributionReportRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherDistributionReportTaskInfoCallRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherDistributionReportTaskInfoCallResponse;
import hxc.utils.protocol.vsip.GetGenerateVoucherDistributionReportTaskInfoRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherDistributionReportTaskInfoResponse;
import hxc.utils.protocol.vsip.GetGenerateVoucherDistributionReportTaskInfoTaskData;
import hxc.utils.protocol.vsip.GetGenerateVoucherDistributionReportTaskInfoTasks;
import hxc.utils.protocol.vsip.Protocol;

public class GetGenerateVoucherDistributionReportTaskInfo extends VoucherCallBase<GetGenerateVoucherDistributionReportTaskInfoResponse, GetGenerateVoucherDistributionReportTaskInfoRequest>
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
	public GetGenerateVoucherDistributionReportTaskInfo(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "GetGenerateVoucherDistributionReportTaskInfo");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static GetGenerateVoucherDistributionReportTaskInfoCallResponse call(IVoucherInfo voucherInfo, GetGenerateVoucherDistributionReportTaskInfoCallRequest call, String operatorID)
	{
		GetGenerateVoucherDistributionReportTaskInfoCallResponse response = new GetGenerateVoucherDistributionReportTaskInfoCallResponse();
		response.setResponse(new GetGenerateVoucherDistributionReportTaskInfo(voucherInfo).execute(new GetGenerateVoucherDistributionReportTaskInfoResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public GetGenerateVoucherDistributionReportTaskInfoResponse exec(GetGenerateVoucherDistributionReportTaskInfoRequest request, String operatorID)
	{
		// Create Response
		GetGenerateVoucherDistributionReportTaskInfoResponse response = new GetGenerateVoucherDistributionReportTaskInfoResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_REQUEST);

		// Simulate
		try
		{
			response.setResponseCode(Protocol.RESPONSECODE_SUCCESS);
			List<ScheduledTask<GenerateVoucherDistributionReportRequest>> tasks = voucherInfo.getTaskInfo(request.getTaskId(), GenerateVoucherDistributionReportRequest.class);
			GetGenerateVoucherDistributionReportTaskInfoTasks[] replies = new GetGenerateVoucherDistributionReportTaskInfoTasks[tasks.size()];
			response.setTasks(replies);
			int index = 0;
			for (ScheduledTask<GenerateVoucherDistributionReportRequest> task : tasks)
			{
				GetGenerateVoucherDistributionReportTaskInfoTasks reply = new GetGenerateVoucherDistributionReportTaskInfoTasks();
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

				GetGenerateVoucherDistributionReportTaskInfoTaskData data = new GetGenerateVoucherDistributionReportTaskInfoTaskData();
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
