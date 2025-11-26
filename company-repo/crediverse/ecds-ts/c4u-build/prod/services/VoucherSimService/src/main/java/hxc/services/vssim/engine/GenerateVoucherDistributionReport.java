package hxc.services.vssim.engine;

import hxc.services.vssim.IVoucherInfo;
import hxc.utils.protocol.vsip.GenerateVoucherDistributionReportCallRequest;
import hxc.utils.protocol.vsip.GenerateVoucherDistributionReportCallResponse;
import hxc.utils.protocol.vsip.GenerateVoucherDistributionReportRequest;
import hxc.utils.protocol.vsip.GenerateVoucherDistributionReportResponse;
import hxc.utils.protocol.vsip.Protocol;

public class GenerateVoucherDistributionReport extends VoucherCallBase<GenerateVoucherDistributionReportResponse, GenerateVoucherDistributionReportRequest>
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
	public GenerateVoucherDistributionReport(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "GenerateVoucherDistributionReport");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static GenerateVoucherDistributionReportCallResponse call(IVoucherInfo voucherInfo, GenerateVoucherDistributionReportCallRequest call, String operatorID)
	{
		GenerateVoucherDistributionReportCallResponse response = new GenerateVoucherDistributionReportCallResponse();
		response.setResponse(new GenerateVoucherDistributionReport(voucherInfo).execute(new GenerateVoucherDistributionReportResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public GenerateVoucherDistributionReportResponse exec(GenerateVoucherDistributionReportRequest request, String operatorID)
	{
		// Create Response
		GenerateVoucherDistributionReportResponse response = new GenerateVoucherDistributionReportResponse();

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
