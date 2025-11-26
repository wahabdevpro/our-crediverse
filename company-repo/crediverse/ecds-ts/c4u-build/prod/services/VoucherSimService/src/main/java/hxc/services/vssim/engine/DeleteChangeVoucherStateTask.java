package hxc.services.vssim.engine;

import hxc.services.vssim.IVoucherInfo;
import hxc.utils.protocol.vsip.ChangeVoucherStateRequest;
import hxc.utils.protocol.vsip.DeleteChangeVoucherStateTaskCallRequest;
import hxc.utils.protocol.vsip.DeleteChangeVoucherStateTaskCallResponse;
import hxc.utils.protocol.vsip.DeleteChangeVoucherStateTaskRequest;
import hxc.utils.protocol.vsip.DeleteChangeVoucherStateTaskResponse;
import hxc.utils.protocol.vsip.Protocol;

public class DeleteChangeVoucherStateTask extends VoucherCallBase<DeleteChangeVoucherStateTaskResponse, DeleteChangeVoucherStateTaskRequest>
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
	public DeleteChangeVoucherStateTask(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "DeleteChangeVoucherStateTask");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static DeleteChangeVoucherStateTaskCallResponse call(IVoucherInfo voucherInfo, DeleteChangeVoucherStateTaskCallRequest call, String operatorID)
	{
		DeleteChangeVoucherStateTaskCallResponse response = new DeleteChangeVoucherStateTaskCallResponse();
		response.setResponse(new DeleteChangeVoucherStateTask(voucherInfo).execute(new DeleteChangeVoucherStateTaskResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public DeleteChangeVoucherStateTaskResponse exec(DeleteChangeVoucherStateTaskRequest request, String operatorID)
	{
		// Create Response
		DeleteChangeVoucherStateTaskResponse response = new DeleteChangeVoucherStateTaskResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_REQUEST);

		// Simulate
		try
		{
			response.setResponseCode(voucherInfo.deleteTask(request.getTaskId(), request.getNetworkOperatorId(), ChangeVoucherStateRequest.class));
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
