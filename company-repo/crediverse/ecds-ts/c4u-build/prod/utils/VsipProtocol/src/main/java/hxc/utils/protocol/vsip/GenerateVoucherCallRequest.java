package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The message GenerateVoucher is used to schedule a generate voucher task.
// The GenerateVoucher message will be added to the VS Task Manager for
// immediate or scheduled execution.

@XmlRpcMethod(name = "GenerateVoucher")
public class GenerateVoucherCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GenerateVoucherRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GenerateVoucherRequest getRequest()
	{
		return request;
	}

	public void setRequest(GenerateVoucherRequest request)
	{
		this.request = request;
	}
}
