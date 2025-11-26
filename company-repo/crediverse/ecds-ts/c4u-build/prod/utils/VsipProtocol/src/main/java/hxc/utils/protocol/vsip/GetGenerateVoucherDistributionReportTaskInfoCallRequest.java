package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The GetGenerateVoucherDistributionReportTaskInfo message is used to return
// information about a GenerateVoucherDistributionReport task.

@XmlRpcMethod(name = "GetGenerateVoucherDistributionReportTaskInfo")
public class GetGenerateVoucherDistributionReportTaskInfoCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetGenerateVoucherDistributionReportTaskInfoRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetGenerateVoucherDistributionReportTaskInfoRequest getRequest()
	{
		return request;
	}

	public void setRequest(GetGenerateVoucherDistributionReportTaskInfoRequest request)
	{
		this.request = request;
	}
}
