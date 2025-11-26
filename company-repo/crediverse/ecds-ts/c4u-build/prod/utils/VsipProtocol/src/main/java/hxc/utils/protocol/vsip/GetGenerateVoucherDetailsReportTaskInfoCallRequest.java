package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The GetGenerateVoucherDetailsReportTaskInfo message is used to return
// information about a GenerateVoucherDetailsReport Task.

@XmlRpcMethod(name = "GetGenerateVoucherDetailsReportTaskInfo")
public class GetGenerateVoucherDetailsReportTaskInfoCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetGenerateVoucherDetailsReportTaskInfoRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetGenerateVoucherDetailsReportTaskInfoRequest getRequest()
	{
		return request;
	}

	public void setRequest(GetGenerateVoucherDetailsReportTaskInfoRequest request)
	{
		this.request = request;
	}
}
