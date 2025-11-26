package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The message PurgeVouchers is used to schedule a purge voucher task. The
// purge voucher task purges all voucher that match the specified criteria.

@XmlRpcMethod(name = "PurgeVouchers")
public class PurgeVouchersCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private PurgeVouchersRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public PurgeVouchersRequest getRequest()
	{
		return request;
	}

	public void setRequest(PurgeVouchersRequest request)
	{
		this.request = request;
	}
}
