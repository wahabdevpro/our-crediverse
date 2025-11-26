package hxc.services.vssim.engine;

import hxc.services.vssim.IVoucherInfo;
import hxc.utils.protocol.vsip.DeletePurgeVoucherTaskCallRequest;
import hxc.utils.protocol.vsip.DeletePurgeVoucherTaskCallResponse;
import hxc.utils.protocol.vsip.DeletePurgeVoucherTaskRequest;
import hxc.utils.protocol.vsip.DeletePurgeVoucherTaskResponse;
import hxc.utils.protocol.vsip.Protocol;
import hxc.utils.protocol.vsip.PurgeVouchersRequest;

public class DeletePurgeVoucherTask extends VoucherCallBase<DeletePurgeVoucherTaskResponse, DeletePurgeVoucherTaskRequest>
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
	public DeletePurgeVoucherTask(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "DeletePurgeVoucherTask");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static DeletePurgeVoucherTaskCallResponse call(IVoucherInfo voucherInfo, DeletePurgeVoucherTaskCallRequest call, String operatorID)
	{
		DeletePurgeVoucherTaskCallResponse response = new DeletePurgeVoucherTaskCallResponse();
		response.setResponse(new DeletePurgeVoucherTask(voucherInfo).execute(new DeletePurgeVoucherTaskResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public DeletePurgeVoucherTaskResponse exec(DeletePurgeVoucherTaskRequest request, String operatorID)
	{
		// Create Response
		DeletePurgeVoucherTaskResponse response = new DeletePurgeVoucherTaskResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_REQUEST);

		// Simulate
		try
		{
			response.setResponseCode(voucherInfo.deleteTask(request.getTaskId(), request.getNetworkOperatorId(), PurgeVouchersRequest.class));
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
