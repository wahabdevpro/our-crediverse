package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The message GetLoadVoucherBatchFileTaskInfo is used to retrieve information
// about a LoadVoucherBatchFile.

@XmlRpcMethod(name = "GetLoadVoucherBatchFileTaskInfo")
public class GetLoadVoucherBatchFileTaskInfoCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetLoadVoucherBatchFileTaskInfoRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetLoadVoucherBatchFileTaskInfoRequest getRequest()
	{
		return request;
	}

	public void setRequest(GetLoadVoucherBatchFileTaskInfoRequest request)
	{
		this.request = request;
	}
}
