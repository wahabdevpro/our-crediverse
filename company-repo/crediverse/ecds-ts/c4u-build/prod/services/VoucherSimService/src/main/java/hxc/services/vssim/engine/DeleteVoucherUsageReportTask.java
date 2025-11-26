package hxc.services.vssim.engine;

import hxc.services.vssim.IVoucherInfo;
import hxc.utils.protocol.vsip.DeleteVoucherUsageReportTaskCallRequest;
import hxc.utils.protocol.vsip.DeleteVoucherUsageReportTaskCallResponse;
import hxc.utils.protocol.vsip.DeleteVoucherUsageReportTaskRequest;
import hxc.utils.protocol.vsip.DeleteVoucherUsageReportTaskResponse;
import hxc.utils.protocol.vsip.GenerateVoucherUsageReportRequest;
import hxc.utils.protocol.vsip.Protocol;

public class DeleteVoucherUsageReportTask extends VoucherCallBase<DeleteVoucherUsageReportTaskResponse, DeleteVoucherUsageReportTaskRequest>
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
	public DeleteVoucherUsageReportTask(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "DeleteVoucherUsageReportTask");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static DeleteVoucherUsageReportTaskCallResponse call(IVoucherInfo voucherInfo, DeleteVoucherUsageReportTaskCallRequest call, String operatorID)
	{
		DeleteVoucherUsageReportTaskCallResponse response = new DeleteVoucherUsageReportTaskCallResponse();
		response.setResponse(new DeleteVoucherUsageReportTask(voucherInfo).execute(new DeleteVoucherUsageReportTaskResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public DeleteVoucherUsageReportTaskResponse exec(DeleteVoucherUsageReportTaskRequest request, String operatorID)
	{
		// Create Response
		DeleteVoucherUsageReportTaskResponse response = new DeleteVoucherUsageReportTaskResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_REQUEST);

		// Simulate
		try
		{
			response.setResponseCode(voucherInfo.deleteTask(request.getTaskId(), request.getNetworkOperatorId(), GenerateVoucherUsageReportRequest.class));
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
