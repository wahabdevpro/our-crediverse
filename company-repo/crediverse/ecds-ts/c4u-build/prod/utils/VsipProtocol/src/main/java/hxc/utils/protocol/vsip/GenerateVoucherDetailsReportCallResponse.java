package hxc.utils.protocol.vsip;

// The GenerateVoucherDetailsReport message is used to schedule a report
// of all vouchers in a specified batch.
// For information of the report file, see Protocol Message Specification Voucher
// Details Report File, Reference [3].

public class GenerateVoucherDetailsReportCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GenerateVoucherDetailsReportResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GenerateVoucherDetailsReportResponse getResponse()
	{
		return response;
	}

	public void setResponse(GenerateVoucherDetailsReportResponse response)
	{
		this.response = response;
	}
}
