package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The message GetPurgeVouchersTaskInfo message is used to return
// information about a PurgeVoucherTask.

@XmlRpcMethod(name = "GetPurgeVouchersTaskInfo")
public class GetPurgeVouchersTaskInfoCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetPurgeVouchersTaskInfoRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetPurgeVouchersTaskInfoRequest getRequest()
	{
		return request;
	}

	public void setRequest(GetPurgeVouchersTaskInfoRequest request)
	{
		this.request = request;
	}
}
