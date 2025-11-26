package hxc.utils.protocol.vsip;

// The GenerateVoucherDistributionReport message is used to create a voucher
// distribution report either for a batch or for all vouchers in the database.
// For information about the report file, see PMS Voucher Distribution Report
// File, Reference [4].

public class GenerateVoucherDistributionReportCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GenerateVoucherDistributionReportResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GenerateVoucherDistributionReportResponse getResponse()
	{
		return response;
	}

	public void setResponse(GenerateVoucherDistributionReportResponse response)
	{
		this.response = response;
	}
}
