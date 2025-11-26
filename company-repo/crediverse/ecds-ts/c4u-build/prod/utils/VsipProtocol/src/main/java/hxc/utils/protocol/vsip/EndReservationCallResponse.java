package hxc.utils.protocol.vsip;

// This message is sent after a refill has been carried out. A successful refill
// will result in the transaction committing, and the voucher state is updated to
// "used". An unsuccessful refill will cause the refill transaction to be rolled back.
// The voucher status will then be reset to "available".

public class EndReservationCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private EndReservationResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public EndReservationResponse getResponse()
	{
		return response;
	}

	public void setResponse(EndReservationResponse response)
	{
		this.response = response;
	}
}
