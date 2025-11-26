package hxc.services.vssim.engine;

import hxc.services.vssim.IVoucherInfo;
import hxc.utils.protocol.vsip.LoadVoucherBatchFileCallRequest;
import hxc.utils.protocol.vsip.LoadVoucherBatchFileCallResponse;
import hxc.utils.protocol.vsip.LoadVoucherBatchFileRequest;
import hxc.utils.protocol.vsip.LoadVoucherBatchFileResponse;
import hxc.utils.protocol.vsip.Protocol;

public class LoadVoucherBatchFile extends VoucherCallBase<LoadVoucherBatchFileResponse, LoadVoucherBatchFileRequest>
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
	public LoadVoucherBatchFile(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "LoadVoucherBatchFile");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static LoadVoucherBatchFileCallResponse call(IVoucherInfo voucherInfo, LoadVoucherBatchFileCallRequest call, String operatorID)
	{
		LoadVoucherBatchFileCallResponse response = new LoadVoucherBatchFileCallResponse();
		response.setResponse(new LoadVoucherBatchFile(voucherInfo).execute(new LoadVoucherBatchFileResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public LoadVoucherBatchFileResponse exec(LoadVoucherBatchFileRequest request, String operatorID)
	{
		// Create Response
		LoadVoucherBatchFileResponse response = new LoadVoucherBatchFileResponse();

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
