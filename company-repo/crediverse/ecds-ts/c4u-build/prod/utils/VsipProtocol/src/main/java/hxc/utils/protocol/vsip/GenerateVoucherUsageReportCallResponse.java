package hxc.utils.protocol.vsip;

// The GenerateVoucherUsageReport message is used to schedule a report of all
// vouchers that was used within a specified time frame.

public class GenerateVoucherUsageReportCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GenerateVoucherUsageReportResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GenerateVoucherUsageReportResponse getResponse()
	{
		return response;
	}

	public void setResponse(GenerateVoucherUsageReportResponse response)
	{
		this.response = response;
	}
}
