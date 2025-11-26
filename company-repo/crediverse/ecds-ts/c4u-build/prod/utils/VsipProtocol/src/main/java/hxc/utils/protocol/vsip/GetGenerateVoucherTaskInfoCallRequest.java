package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The message GetGenerateVoucherTaskInfo is used to retrieve information
// about a GenerateVoucher task.

@XmlRpcMethod(name = "GetGenerateVoucherTaskInfo")
public class GetGenerateVoucherTaskInfoCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetGenerateVoucherTaskInfoRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetGenerateVoucherTaskInfoRequest getRequest()
	{
		return request;
	}

	public void setRequest(GetGenerateVoucherTaskInfoRequest request)
	{
		this.request = request;
	}
}
