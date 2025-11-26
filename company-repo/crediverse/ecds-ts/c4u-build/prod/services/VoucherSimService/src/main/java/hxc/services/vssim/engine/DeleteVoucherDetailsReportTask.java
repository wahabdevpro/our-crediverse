package hxc.services.vssim.engine;

import hxc.services.vssim.IVoucherInfo;
import hxc.utils.protocol.vsip.DeleteVoucherDetailsReportTaskCallRequest;
import hxc.utils.protocol.vsip.DeleteVoucherDetailsReportTaskCallResponse;
import hxc.utils.protocol.vsip.DeleteVoucherDetailsReportTaskRequest;
import hxc.utils.protocol.vsip.DeleteVoucherDetailsReportTaskResponse;
import hxc.utils.protocol.vsip.GenerateVoucherDetailsReportRequest;
import hxc.utils.protocol.vsip.Protocol;

public class DeleteVoucherDetailsReportTask extends VoucherCallBase<DeleteVoucherDetailsReportTaskResponse, DeleteVoucherDetailsReportTaskRequest>
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
	public DeleteVoucherDetailsReportTask(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "DeleteVoucherDetailsReportTask");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static DeleteVoucherDetailsReportTaskCallResponse call(IVoucherInfo voucherInfo, DeleteVoucherDetailsReportTaskCallRequest call, String operatorID)
	{
		DeleteVoucherDetailsReportTaskCallResponse response = new DeleteVoucherDetailsReportTaskCallResponse();
		response.setResponse(new DeleteVoucherDetailsReportTask(voucherInfo).execute(new DeleteVoucherDetailsReportTaskResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public DeleteVoucherDetailsReportTaskResponse exec(DeleteVoucherDetailsReportTaskRequest request, String operatorID)
	{
		// Create Response
		DeleteVoucherDetailsReportTaskResponse response = new DeleteVoucherDetailsReportTaskResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_REQUEST);

		// Simulate
		try
		{
			response.setResponseCode(voucherInfo.deleteTask(request.getTaskId(), request.getNetworkOperatorId(), GenerateVoucherDetailsReportRequest.class));
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
