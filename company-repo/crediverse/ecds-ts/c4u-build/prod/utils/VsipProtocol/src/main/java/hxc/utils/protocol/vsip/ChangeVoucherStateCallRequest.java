package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The message ChangeVoucherState message is used to schedule a task to
// change the state of vouchers.
// Caution!
// When using alphanumeric serial numbers (PC), range based operations are not
// recommended since it is likely to affect more vouchers than intended.

@XmlRpcMethod(name = "ChangeVoucherState")
public class ChangeVoucherStateCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ChangeVoucherStateRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public ChangeVoucherStateRequest getRequest()
	{
		return request;
	}

	public void setRequest(ChangeVoucherStateRequest request)
	{
		this.request = request;
	}
}
