package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The message LoadVoucherCheck is used to check if the vouchers in a serial
// number range are loaded into the database. The number of voucher found
// within the range are returned. If any voucher is missing, this is indicated
// in the response code, which is 10 (Voucher Does not exist), in that case.
// The requested serial number range is pointed out by serialNumberFirst and
// serialNumberLast.
// Note: When using alphanumeric serial numbers (PC) the numberOfVouchers
// parameter will not always be correct. The use of message
// LoadVoucherCheck is not recommended for alphanumeric serial
// numbers (PC).

@XmlRpcMethod(name = "LoadVoucherCheck")
public class LoadVoucherCheckCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private LoadVoucherCheckRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public LoadVoucherCheckRequest getRequest()
	{
		return request;
	}

	public void setRequest(LoadVoucherCheckRequest request)
	{
		this.request = request;
	}
}
