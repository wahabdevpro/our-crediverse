package hxc.services.vssim.engine;

import hxc.services.vssim.IVoucherInfo;
import hxc.utils.protocol.vsip.DeleteVoucherDistributionReportTaskCallRequest;
import hxc.utils.protocol.vsip.DeleteVoucherDistributionReportTaskCallResponse;
import hxc.utils.protocol.vsip.DeleteVoucherDistributionReportTaskRequest;
import hxc.utils.protocol.vsip.DeleteVoucherDistributionReportTaskResponse;
import hxc.utils.protocol.vsip.GenerateVoucherDistributionReportRequest;
import hxc.utils.protocol.vsip.Protocol;

public class DeleteVoucherDistributionReportTask extends VoucherCallBase<DeleteVoucherDistributionReportTaskResponse, DeleteVoucherDistributionReportTaskRequest>
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
	public DeleteVoucherDistributionReportTask(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "DeleteVoucherDistributionReportTask");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static DeleteVoucherDistributionReportTaskCallResponse call(IVoucherInfo voucherInfo, DeleteVoucherDistributionReportTaskCallRequest call, String operatorID)
	{
		DeleteVoucherDistributionReportTaskCallResponse response = new DeleteVoucherDistributionReportTaskCallResponse();
		response.setResponse(new DeleteVoucherDistributionReportTask(voucherInfo).execute(new DeleteVoucherDistributionReportTaskResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public DeleteVoucherDistributionReportTaskResponse exec(DeleteVoucherDistributionReportTaskRequest request, String operatorID)
	{
		// Create Response
		DeleteVoucherDistributionReportTaskResponse response = new DeleteVoucherDistributionReportTaskResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_REQUEST);

		// Simulate
		try
		{
			response.setResponseCode(voucherInfo.deleteTask(request.getTaskId(), request.getNetworkOperatorId(), GenerateVoucherDistributionReportRequest.class));
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
