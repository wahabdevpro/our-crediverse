package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The GetGenerateVoucherUsageReportTaskInfo message is used to return
// information about a specific or all GenerateVoucherUsageReport tasks.

@XmlRpcMethod(name = "GetGenerateVoucherUsageReportTaskInfo")
public class GetGenerateVoucherUsageReportTaskInfoCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetGenerateVoucherUsageReportTaskInfoRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetGenerateVoucherUsageReportTaskInfoRequest getRequest()
	{
		return request;
	}

	public void setRequest(GetGenerateVoucherUsageReportTaskInfoRequest request)
	{
		this.request = request;
	}
}
