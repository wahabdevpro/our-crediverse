package hxc.services.vssim.engine;

import hxc.services.vssim.IVoucherInfo;
import hxc.utils.protocol.vsip.GetVoucherBatchFilesListCallRequest;
import hxc.utils.protocol.vsip.GetVoucherBatchFilesListCallResponse;
import hxc.utils.protocol.vsip.GetVoucherBatchFilesListRequest;
import hxc.utils.protocol.vsip.GetVoucherBatchFilesListResponse;
import hxc.utils.protocol.vsip.Protocol;

public class GetVoucherBatchFilesList extends VoucherCallBase<GetVoucherBatchFilesListResponse, GetVoucherBatchFilesListRequest>
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
	public GetVoucherBatchFilesList(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "GetVoucherBatchFilesList");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static GetVoucherBatchFilesListCallResponse call(IVoucherInfo voucherInfo, GetVoucherBatchFilesListCallRequest call, String operatorID)
	{
		GetVoucherBatchFilesListCallResponse response = new GetVoucherBatchFilesListCallResponse();
		response.setResponse(new GetVoucherBatchFilesList(voucherInfo).execute(new GetVoucherBatchFilesListResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public GetVoucherBatchFilesListResponse exec(GetVoucherBatchFilesListRequest request, String operatorID)
	{
		// Create Response
		GetVoucherBatchFilesListResponse response = new GetVoucherBatchFilesListResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
		{
			response.setResponseCode(Protocol.RESPONSECODE_MALFORMED_REQUEST);
			return response;
		}

		// Do Simulation here...
		// TODO
		response.setResponseCode(Protocol.RESPONSECODE_NOT_IMPLEMENTED_YET);

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
