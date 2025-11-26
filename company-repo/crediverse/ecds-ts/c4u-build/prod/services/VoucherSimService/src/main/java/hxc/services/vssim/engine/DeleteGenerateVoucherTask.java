package hxc.services.vssim.engine;

import hxc.services.vssim.IVoucherInfo;
import hxc.utils.protocol.vsip.DeleteGenerateVoucherTaskCallRequest;
import hxc.utils.protocol.vsip.DeleteGenerateVoucherTaskCallResponse;
import hxc.utils.protocol.vsip.DeleteGenerateVoucherTaskRequest;
import hxc.utils.protocol.vsip.DeleteGenerateVoucherTaskResponse;
import hxc.utils.protocol.vsip.GenerateVoucherRequest;
import hxc.utils.protocol.vsip.Protocol;

public class DeleteGenerateVoucherTask extends VoucherCallBase<DeleteGenerateVoucherTaskResponse, DeleteGenerateVoucherTaskRequest>
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
	public DeleteGenerateVoucherTask(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "DeleteGenerateVoucherTask");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static DeleteGenerateVoucherTaskCallResponse call(IVoucherInfo voucherInfo, DeleteGenerateVoucherTaskCallRequest call, String operatorID)
	{
		DeleteGenerateVoucherTaskCallResponse response = new DeleteGenerateVoucherTaskCallResponse();
		response.setResponse(new DeleteGenerateVoucherTask(voucherInfo).execute(new DeleteGenerateVoucherTaskResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public DeleteGenerateVoucherTaskResponse exec(DeleteGenerateVoucherTaskRequest request, String operatorID)
	{
		// Create Response
		DeleteGenerateVoucherTaskResponse response = new DeleteGenerateVoucherTaskResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_REQUEST);

		// Simulate
		try
		{
			response.setResponseCode(voucherInfo.deleteTask(request.getTaskId(), request.getNetworkOperatorId(), GenerateVoucherRequest.class));
		}
		catch (Exception ex)
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
