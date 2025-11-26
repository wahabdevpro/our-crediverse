package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The message GetChangeVoucherStateTaskInfo message is used to retrieve
// information about a ChangeVoucherState task.

@XmlRpcMethod(name = "GetChangeVoucherStateTaskInfo")
public class GetChangeVoucherStateTaskInfoCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetChangeVoucherStateTaskInfoRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetChangeVoucherStateTaskInfoRequest getRequest()
	{
		return request;
	}

	public void setRequest(GetChangeVoucherStateTaskInfoRequest request)
	{
		this.request = request;
	}
}
