package hxc.services.vssim.engine;

import hxc.services.vssim.IVoucherInfo;
import hxc.utils.protocol.vsip.GenerateVoucherDetailsReportCallRequest;
import hxc.utils.protocol.vsip.GenerateVoucherDetailsReportCallResponse;
import hxc.utils.protocol.vsip.GenerateVoucherDetailsReportRequest;
import hxc.utils.protocol.vsip.GenerateVoucherDetailsReportResponse;
import hxc.utils.protocol.vsip.Protocol;

public class GenerateVoucherDetailsReport extends VoucherCallBase<GenerateVoucherDetailsReportResponse, GenerateVoucherDetailsReportRequest>
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
	public GenerateVoucherDetailsReport(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "GenerateVoucherDetailsReport");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static GenerateVoucherDetailsReportCallResponse call(IVoucherInfo voucherInfo, GenerateVoucherDetailsReportCallRequest call, String operatorID)
	{
		GenerateVoucherDetailsReportCallResponse response = new GenerateVoucherDetailsReportCallResponse();
		response.setResponse(new GenerateVoucherDetailsReport(voucherInfo).execute(new GenerateVoucherDetailsReportResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public GenerateVoucherDetailsReportResponse exec(GenerateVoucherDetailsReportRequest request, String operatorID)
	{
		// Create Response
		GenerateVoucherDetailsReportResponse response = new GenerateVoucherDetailsReportResponse();

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
