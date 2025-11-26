package hxc.utils.protocol.vsip;

// The message GetVoucherHistory is used to get historical information for a
// voucher including information about voucher state changes performed for a
// specific voucher.

public class GetVoucherHistoryCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetVoucherHistoryResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetVoucherHistoryResponse getResponse()
	{
		return response;
	}

	public void setResponse(GetVoucherHistoryResponse response)
	{
		this.response = response;
	}
}
