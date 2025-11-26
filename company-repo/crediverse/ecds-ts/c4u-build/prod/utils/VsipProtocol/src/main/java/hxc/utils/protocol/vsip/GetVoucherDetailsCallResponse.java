package hxc.utils.protocol.vsip;

// The message GetVoucherDetails is used in order to obtain detailed information
// on an individual voucher.

public class GetVoucherDetailsCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetVoucherDetailsResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetVoucherDetailsResponse getResponse()
	{
		return response;
	}

	public void setResponse(GetVoucherDetailsResponse response)
	{
		this.response = response;
	}
}
