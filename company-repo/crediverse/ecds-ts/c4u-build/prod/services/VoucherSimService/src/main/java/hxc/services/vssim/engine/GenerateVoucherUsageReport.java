package hxc.services.vssim.engine;

import hxc.services.vssim.IVoucherInfo;
import hxc.utils.protocol.vsip.GenerateVoucherUsageReportCallRequest;
import hxc.utils.protocol.vsip.GenerateVoucherUsageReportCallResponse;
import hxc.utils.protocol.vsip.GenerateVoucherUsageReportRequest;
import hxc.utils.protocol.vsip.GenerateVoucherUsageReportResponse;
import hxc.utils.protocol.vsip.Protocol;

public class GenerateVoucherUsageReport extends VoucherCallBase<GenerateVoucherUsageReportResponse, GenerateVoucherUsageReportRequest>
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
	public GenerateVoucherUsageReport(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "GenerateVoucherUsageReport");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static GenerateVoucherUsageReportCallResponse call(IVoucherInfo voucherInfo, GenerateVoucherUsageReportCallRequest call, String operatorID)
	{
		GenerateVoucherUsageReportCallResponse response = new GenerateVoucherUsageReportCallResponse();
		response.setResponse(new GenerateVoucherUsageReport(voucherInfo).execute(new GenerateVoucherUsageReportResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public GenerateVoucherUsageReportResponse exec(GenerateVoucherUsageReportRequest request, String operatorID)
	{
		// Create Response
		GenerateVoucherUsageReportResponse response = new GenerateVoucherUsageReportResponse();

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
