package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The message GetVoucherDetails is used in order to obtain detailed information
// on an individual voucher.

@XmlRpcMethod(name = "GetVoucherDetails")
public class GetVoucherDetailsCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetVoucherDetailsRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetVoucherDetailsRequest getRequest()
	{
		return request;
	}

	public void setRequest(GetVoucherDetailsRequest request)
	{
		this.request = request;
	}
}
