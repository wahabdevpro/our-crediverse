package hxc.services.vssim.engine;

import java.util.List;

import hxc.services.vssim.IVoucherInfo;
import hxc.services.vssim.model.ScheduledTask;
import hxc.utils.protocol.vsip.GenerateVoucherRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherTaskInfoCallRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherTaskInfoCallResponse;
import hxc.utils.protocol.vsip.GetGenerateVoucherTaskInfoRequest;
import hxc.utils.protocol.vsip.GetGenerateVoucherTaskInfoResponse;
import hxc.utils.protocol.vsip.GetGenerateVoucherTaskInfoTaskData;
import hxc.utils.protocol.vsip.GetGenerateVoucherTaskInfoTasks;
import hxc.utils.protocol.vsip.Protocol;

public class GetGenerateVoucherTaskInfo extends VoucherCallBase<GetGenerateVoucherTaskInfoResponse, GetGenerateVoucherTaskInfoRequest>
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
	public GetGenerateVoucherTaskInfo(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "GetGenerateVoucherTaskInfo");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static GetGenerateVoucherTaskInfoCallResponse call(IVoucherInfo voucherInfo, GetGenerateVoucherTaskInfoCallRequest call, String operatorID)
	{
		GetGenerateVoucherTaskInfoCallResponse response = new GetGenerateVoucherTaskInfoCallResponse();
		response.setResponse(new GetGenerateVoucherTaskInfo(voucherInfo).execute(new GetGenerateVoucherTaskInfoResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public GetGenerateVoucherTaskInfoResponse exec(GetGenerateVoucherTaskInfoRequest request, String operatorID)
	{
		// Create Response
		GetGenerateVoucherTaskInfoResponse response = new GetGenerateVoucherTaskInfoResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_REQUEST);

		// Simulate
		try
		{
			response.setResponseCode(Protocol.RESPONSECODE_SUCCESS);
			List<ScheduledTask<GenerateVoucherRequest>> tasks = voucherInfo.getTaskInfo(request.getTaskId(), GenerateVoucherRequest.class);
			GetGenerateVoucherTaskInfoTasks[] replies = new GetGenerateVoucherTaskInfoTasks[tasks.size()];
			response.setTasks(replies);
			int index = 0;
			for (ScheduledTask<GenerateVoucherRequest> task : tasks)
			{
				GetGenerateVoucherTaskInfoTasks reply = new GetGenerateVoucherTaskInfoTasks();
				replies[index++] = reply;

				reply.setTaskId(task.getTaskId());
				reply.setTaskStatus(task.getTaskStatus());
				reply.setExecutionTime(task.getExecutionTime());
				reply.setOperatorId(task.getOperatorId());
				reply.setFilename(task.getFilename());
				reply.setFailReason(task.getFailReason());
				reply.setAdditionalInfo(task.getAdditionalInfo());

				GetGenerateVoucherTaskInfoTaskData data = new GetGenerateVoucherTaskInfoTaskData();
				reply.setTaskData(data);
				data.setNumberOfVouchers(task.getRequest().getNumberOfVouchers());
				data.setActivationCodeLength(task.getRequest().getActivationCodeLength());
				data.setCurrency(task.getRequest().getCurrency());
				data.setSerialNumber(task.getRequest().getSerialNumber());
				data.setValue(task.getRequest().getValue());
				data.setVoucherGroup(task.getRequest().getVoucherGroup());
				data.setExpiryDate(task.getRequest().getExpiryDate());
				data.setAgent(task.getRequest().getAgent());
				data.setExtensionText1(task.getRequest().getExtensionText1());
				data.setExtensionText2(task.getRequest().getExtensionText2());
				data.setExtensionText3(task.getRequest().getExtensionText3());
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
