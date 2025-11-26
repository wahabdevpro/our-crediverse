package hxc.services.vssim.engine;

import hxc.services.vssim.IVoucherInfo;
import hxc.utils.protocol.vsip.LoadVoucherCheckCallRequest;
import hxc.utils.protocol.vsip.LoadVoucherCheckCallResponse;
import hxc.utils.protocol.vsip.LoadVoucherCheckRequest;
import hxc.utils.protocol.vsip.LoadVoucherCheckResponse;
import hxc.utils.protocol.vsip.Protocol;

public class LoadVoucherCheck extends VoucherCallBase<LoadVoucherCheckResponse, LoadVoucherCheckRequest>
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
	public LoadVoucherCheck(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "LoadVoucherCheck");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static LoadVoucherCheckCallResponse call(IVoucherInfo voucherInfo, LoadVoucherCheckCallRequest call, String operatorID)
	{
		LoadVoucherCheckCallResponse response = new LoadVoucherCheckCallResponse();
		response.setResponse(new LoadVoucherCheck(voucherInfo).execute(new LoadVoucherCheckResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public LoadVoucherCheckResponse exec(LoadVoucherCheckRequest request, String operatorID)
	{
		// Create Response
		LoadVoucherCheckResponse response = new LoadVoucherCheckResponse();

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
