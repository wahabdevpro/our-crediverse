package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The GenerateVoucherDetailsReport message is used to schedule a report
// of all vouchers in a specified batch.
// For information of the report file, see Protocol Message Specification Voucher
// Details Report File, Reference [3].

@XmlRpcMethod(name = "GenerateVoucherDetailsReport")
public class GenerateVoucherDetailsReportCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GenerateVoucherDetailsReportRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GenerateVoucherDetailsReportRequest getRequest()
	{
		return request;
	}

	public void setRequest(GenerateVoucherDetailsReportRequest request)
	{
		this.request = request;
	}
}
