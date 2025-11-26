package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The GenerateVoucherUsageReport message is used to schedule a report of all
// vouchers that was used within a specified time frame.

@XmlRpcMethod(name = "GenerateVoucherUsageReport")
public class GenerateVoucherUsageReportCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GenerateVoucherUsageReportRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GenerateVoucherUsageReportRequest getRequest()
	{
		return request;
	}

	public void setRequest(GenerateVoucherUsageReportRequest request)
	{
		this.request = request;
	}
}
