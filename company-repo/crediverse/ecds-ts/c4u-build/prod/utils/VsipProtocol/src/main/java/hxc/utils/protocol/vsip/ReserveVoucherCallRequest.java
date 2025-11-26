package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// This message is used to reserve a voucher. The message represents the start
// of a refill transaction.
// The additionalAction parameter can be used to request different reservation
// mechanisms, and it controls the need of issuing specific commit or rollback
// messages.

@XmlRpcMethod(name = "ReserveVoucher")
public class ReserveVoucherCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ReserveVoucherRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public ReserveVoucherRequest getRequest()
	{
		return request;
	}

	public void setRequest(ReserveVoucherRequest request)
	{
		this.request = request;
	}
}
