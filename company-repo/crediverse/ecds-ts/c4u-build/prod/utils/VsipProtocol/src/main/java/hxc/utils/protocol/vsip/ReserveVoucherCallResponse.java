package hxc.utils.protocol.vsip;

// This message is used to reserve a voucher. The message represents the start
// of a refill transaction.
// The additionalAction parameter can be used to request different reservation
// mechanisms, and it controls the need of issuing specific commit or rollback
// messages.

public class ReserveVoucherCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ReserveVoucherResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public ReserveVoucherResponse getResponse()
	{
		return response;
	}

	public void setResponse(ReserveVoucherResponse response)
	{
		this.response = response;
	}
}
