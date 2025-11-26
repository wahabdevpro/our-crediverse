package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The GenerateVoucherDistributionReport message is used to create a voucher
// distribution report either for a batch or for all vouchers in the database.
// For information about the report file, see PMS Voucher Distribution Report
// File, Reference [4].

@XmlRpcMethod(name = "GenerateVoucherDistributionReport")
public class GenerateVoucherDistributionReportCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GenerateVoucherDistributionReportRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GenerateVoucherDistributionReportRequest getRequest()
	{
		return request;
	}

	public void setRequest(GenerateVoucherDistributionReportRequest request)
	{
		this.request = request;
	}
}
