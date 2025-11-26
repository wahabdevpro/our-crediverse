package hxc.utils.protocol.vsip;

// The message ChangeVoucherState message is used to schedule a task to
// change the state of vouchers.
// Caution!
// When using alphanumeric serial numbers (PC), range based operations are not
// recommended since it is likely to affect more vouchers than intended.

public class ChangeVoucherStateCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ChangeVoucherStateResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public ChangeVoucherStateResponse getResponse()
	{
		return response;
	}

	public void setResponse(ChangeVoucherStateResponse response)
	{
		this.response = response;
	}
}
