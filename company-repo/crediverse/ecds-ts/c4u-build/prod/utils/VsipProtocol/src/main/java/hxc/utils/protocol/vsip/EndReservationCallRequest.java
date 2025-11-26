package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// This message is sent after a refill has been carried out. A successful refill
// will result in the transaction committing, and the voucher state is updated to
// "used". An unsuccessful refill will cause the refill transaction to be rolled back.
// The voucher status will then be reset to "available".

@XmlRpcMethod(name = "EndReservation")
public class EndReservationCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private EndReservationRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public EndReservationRequest getRequest()
	{
		return request;
	}

	public void setRequest(EndReservationRequest request)
	{
		this.request = request;
	}
}
