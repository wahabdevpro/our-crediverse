package hxc.utils.protocol.vsip;

// The message UpdateVoucherState is used to update the voucher state.
// The requested state change pointed out by the “newState” parameter must
// follow the state model with allowed state transitions defined for the voucher
// server.

public class UpdateVoucherStateCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private UpdateVoucherStateResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public UpdateVoucherStateResponse getResponse()
	{
		return response;
	}

	public void setResponse(UpdateVoucherStateResponse response)
	{
		this.response = response;
	}
}
