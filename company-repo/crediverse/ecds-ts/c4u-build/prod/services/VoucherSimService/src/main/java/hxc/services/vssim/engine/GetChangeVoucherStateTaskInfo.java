package hxc.services.vssim.engine;

import java.util.List;

import hxc.services.vssim.IVoucherInfo;
import hxc.services.vssim.model.ScheduledTask;
import hxc.utils.protocol.vsip.ChangeVoucherStateRequest;
import hxc.utils.protocol.vsip.GetChangeVoucherStateTaskInfoCallRequest;
import hxc.utils.protocol.vsip.GetChangeVoucherStateTaskInfoCallResponse;
import hxc.utils.protocol.vsip.GetChangeVoucherStateTaskInfoRequest;
import hxc.utils.protocol.vsip.GetChangeVoucherStateTaskInfoResponse;
import hxc.utils.protocol.vsip.GetChangeVoucherStateTaskInfoTaskData;
import hxc.utils.protocol.vsip.GetChangeVoucherStateTaskInfoTasks;
import hxc.utils.protocol.vsip.Protocol;

public class GetChangeVoucherStateTaskInfo extends VoucherCallBase<GetChangeVoucherStateTaskInfoResponse, GetChangeVoucherStateTaskInfoRequest>
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
	public GetChangeVoucherStateTaskInfo(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "GetChangeVoucherStateTaskInfo");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static GetChangeVoucherStateTaskInfoCallResponse call(IVoucherInfo voucherInfo, GetChangeVoucherStateTaskInfoCallRequest call, String operatorID)
	{
		GetChangeVoucherStateTaskInfoCallResponse response = new GetChangeVoucherStateTaskInfoCallResponse();
		response.setResponse(new GetChangeVoucherStateTaskInfo(voucherInfo).execute(new GetChangeVoucherStateTaskInfoResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public GetChangeVoucherStateTaskInfoResponse exec(GetChangeVoucherStateTaskInfoRequest request, String operatorID)
	{
		// Create Response
		GetChangeVoucherStateTaskInfoResponse response = new GetChangeVoucherStateTaskInfoResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
		{
			response.setResponseCode(Protocol.RESPONSECODE_MALFORMED_REQUEST);
			return response;
		}

		// Simulate
		try
		{
			// Set response code
			response.setResponseCode(Protocol.RESPONSECODE_SUCCESS);

			// Get the tasks
			List<ScheduledTask<ChangeVoucherStateRequest>> tasks = super.voucherInfo.getTaskInfo(request.getTaskId(), ChangeVoucherStateRequest.class);
			GetChangeVoucherStateTaskInfoTasks replies[] = new GetChangeVoucherStateTaskInfoTasks[tasks.size()];

			response.setTasks(replies);
			int index = 0;
			for (ScheduledTask<ChangeVoucherStateRequest> task : tasks)
			{
				GetChangeVoucherStateTaskInfoTasks reply = new GetChangeVoucherStateTaskInfoTasks();
				replies[index++] = reply;

				// Set task info
				reply.setTaskId(task.getTaskId());
				reply.setTaskStatus(task.getTaskStatus());
				reply.setExecutionTime(task.getExecutionTime());
				reply.setOperatorId(task.getOperatorId());
				reply.setFilename(task.getFilename());
				reply.setFailReason(task.getFailReason());
				reply.setAdditionalInfo(task.getAdditionalInfo());

				// Set task info data
				GetChangeVoucherStateTaskInfoTaskData data = new GetChangeVoucherStateTaskInfoTaskData();
				reply.setTaskData(data);
				ChangeVoucherStateRequest changeVoucherStateRequest = task.getRequest();
				data.setBatchId(changeVoucherStateRequest.getBatchId());
				data.setSerialNumber(changeVoucherStateRequest.getSerialNumber());
				data.setSerialNumberFirst(changeVoucherStateRequest.getSerialNumberFirst());
				data.setSerialNumberLast(changeVoucherStateRequest.getSerialNumberLast());
				data.setActivationCode(changeVoucherStateRequest.getActivationCode());
				data.setNewState(changeVoucherStateRequest.getNewState());
				data.setOldState(changeVoucherStateRequest.getOldState());
				data.setReportFormat(changeVoucherStateRequest.getReportFormat());
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
		{
			response.setResponseCode(Protocol.RESPONSECODE_MALFORMED_RESPONSE);
			return response;
		}

		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

}
