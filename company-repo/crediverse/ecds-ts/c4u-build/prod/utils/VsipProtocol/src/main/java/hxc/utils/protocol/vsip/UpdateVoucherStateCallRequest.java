package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The message UpdateVoucherState is used to update the voucher state.
// The requested state change pointed out by the “newState” parameter must
// follow the state model with allowed state transitions defined for the voucher
// server.

@XmlRpcMethod(name = "UpdateVoucherState")
public class UpdateVoucherStateCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private UpdateVoucherStateRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public UpdateVoucherStateRequest getRequest()
	{
		return request;
	}

	public void setRequest(UpdateVoucherStateRequest request)
	{
		this.request = request;
	}
}
