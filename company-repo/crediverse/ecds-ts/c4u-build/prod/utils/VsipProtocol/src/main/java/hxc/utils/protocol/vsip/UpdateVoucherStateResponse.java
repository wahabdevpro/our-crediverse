package hxc.utils.protocol.vsip;

// The message UpdateVoucherState is used to update the voucher state.
// The requested state change pointed out by the “newState” parameter must
// follow the state model with allowed state transitions defined for the voucher
// server.

public class UpdateVoucherStateResponse implements IVsipResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private int responseCode;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// The responseCode parameter is sent back after a message has been
	// processed and indicates success or failure of the message.
	//
	// Mandatory

	@Override
	public int getResponseCode()
	{
		return responseCode;
	}

	@Override
	public void setResponseCode(int responseCode)
	{
		this.responseCode = responseCode;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	@Override
	public boolean validate(IValidationContext context)
	{
		return Protocol.validateResponseCode(context, true, responseCode);
	}

}
