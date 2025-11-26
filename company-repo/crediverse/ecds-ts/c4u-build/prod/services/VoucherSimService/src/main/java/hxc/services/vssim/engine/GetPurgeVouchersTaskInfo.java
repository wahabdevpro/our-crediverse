package hxc.services.vssim.engine;

import java.util.List;

import hxc.services.vssim.IVoucherInfo;
import hxc.services.vssim.model.ScheduledTask;
import hxc.utils.protocol.vsip.GetPurgeVouchersTaskInfoCallRequest;
import hxc.utils.protocol.vsip.GetPurgeVouchersTaskInfoCallResponse;
import hxc.utils.protocol.vsip.GetPurgeVouchersTaskInfoRequest;
import hxc.utils.protocol.vsip.GetPurgeVouchersTaskInfoResponse;
import hxc.utils.protocol.vsip.GetPurgeVouchersTaskInfoTaskData;
import hxc.utils.protocol.vsip.GetPurgeVouchersTaskInfoTasks;
import hxc.utils.protocol.vsip.Protocol;
import hxc.utils.protocol.vsip.PurgeVouchersRequest;

public class GetPurgeVouchersTaskInfo extends VoucherCallBase<GetPurgeVouchersTaskInfoResponse, GetPurgeVouchersTaskInfoRequest>
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
	public GetPurgeVouchersTaskInfo(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "GetPurgeVouchersTaskInfo");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static GetPurgeVouchersTaskInfoCallResponse call(IVoucherInfo voucherInfo, GetPurgeVouchersTaskInfoCallRequest call, String operatorID)
	{
		GetPurgeVouchersTaskInfoCallResponse response = new GetPurgeVouchersTaskInfoCallResponse();
		response.setResponse(new GetPurgeVouchersTaskInfo(voucherInfo).execute(new GetPurgeVouchersTaskInfoResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public GetPurgeVouchersTaskInfoResponse exec(GetPurgeVouchersTaskInfoRequest request, String operatorID)
	{
		// Create Response
		GetPurgeVouchersTaskInfoResponse response = new GetPurgeVouchersTaskInfoResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_REQUEST);

		// Simulate
		try
		{
			response.setResponseCode(Protocol.RESPONSECODE_SUCCESS);
			List<ScheduledTask<PurgeVouchersRequest>> tasks = voucherInfo.getTaskInfo(request.getTaskId(), PurgeVouchersRequest.class);
			GetPurgeVouchersTaskInfoTasks[] replies = new GetPurgeVouchersTaskInfoTasks[tasks.size()];
			response.setTasks(replies);
			int index = 0;
			for (ScheduledTask<PurgeVouchersRequest> task : tasks)
			{
				GetPurgeVouchersTaskInfoTasks reply = new GetPurgeVouchersTaskInfoTasks();
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

				GetPurgeVouchersTaskInfoTaskData data = new GetPurgeVouchersTaskInfoTaskData();
				reply.setTaskData(data);

				data.setExpiryDate(task.getRequest().getExpiryDate());
				data.setOffset(task.getRequest().getOffset());
				data.setState(task.getRequest().getState());

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
