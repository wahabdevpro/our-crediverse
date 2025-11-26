package hxc.services.vssim.engine;

import hxc.services.vssim.IVoucherInfo;
import hxc.utils.protocol.vsip.DeleteLoadVoucherBatchTaskCallRequest;
import hxc.utils.protocol.vsip.DeleteLoadVoucherBatchTaskCallResponse;
import hxc.utils.protocol.vsip.DeleteLoadVoucherBatchTaskRequest;
import hxc.utils.protocol.vsip.DeleteLoadVoucherBatchTaskResponse;
import hxc.utils.protocol.vsip.LoadVoucherBatchFileRequest;
import hxc.utils.protocol.vsip.Protocol;

public class DeleteLoadVoucherBatchTask extends VoucherCallBase<DeleteLoadVoucherBatchTaskResponse, DeleteLoadVoucherBatchTaskRequest>
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
	public DeleteLoadVoucherBatchTask(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "DeleteLoadVoucherBatchTask");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static DeleteLoadVoucherBatchTaskCallResponse call(IVoucherInfo voucherInfo, DeleteLoadVoucherBatchTaskCallRequest call, String operatorID)
	{
		DeleteLoadVoucherBatchTaskCallResponse response = new DeleteLoadVoucherBatchTaskCallResponse();
		response.setResponse(new DeleteLoadVoucherBatchTask(voucherInfo).execute(new DeleteLoadVoucherBatchTaskResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public DeleteLoadVoucherBatchTaskResponse exec(DeleteLoadVoucherBatchTaskRequest request, String operatorID)
	{
		// Create Response
		DeleteLoadVoucherBatchTaskResponse response = new DeleteLoadVoucherBatchTaskResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_REQUEST);

		// Simulate
		try
		{
			response.setResponseCode(voucherInfo.deleteTask(request.getTaskId(), request.getNetworkOperatorId(), LoadVoucherBatchFileRequest.class));
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
