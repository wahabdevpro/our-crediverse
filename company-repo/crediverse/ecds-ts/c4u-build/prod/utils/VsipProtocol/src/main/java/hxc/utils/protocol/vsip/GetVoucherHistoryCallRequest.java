package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The message GetVoucherHistory is used to get historical information for a
// voucher including information about voucher state changes performed for a
// specific voucher.

@XmlRpcMethod(name = "GetVoucherHistory")
public class GetVoucherHistoryCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetVoucherHistoryRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetVoucherHistoryRequest getRequest()
	{
		return request;
	}

	public void setRequest(GetVoucherHistoryRequest request)
	{
		this.request = request;
	}
}
