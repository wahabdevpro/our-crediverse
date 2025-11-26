package hxc.services.vssim.engine;

import hxc.services.vssim.IVoucherInfo;
import hxc.services.vssim.model.Voucher;
import hxc.utils.protocol.vsip.EndReservationCallRequest;
import hxc.utils.protocol.vsip.EndReservationCallResponse;
import hxc.utils.protocol.vsip.EndReservationRequest;
import hxc.utils.protocol.vsip.EndReservationResponse;
import hxc.utils.protocol.vsip.Protocol;

public class EndReservation extends VoucherCallBase<EndReservationResponse, EndReservationRequest>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public EndReservation(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "EndReservation");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static EndReservationCallResponse call(IVoucherInfo voucherInfo, EndReservationCallRequest call, String operatorID)
	{
		EndReservationCallResponse response = new EndReservationCallResponse();
		response.setResponse(new EndReservation(voucherInfo).execute(new EndReservationResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public EndReservationResponse exec(EndReservationRequest request, String operatorID)
	{
		// Create Response
		EndReservationResponse response = new EndReservationResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_REQUEST);

		// Can Return:
		// 0: Successful /
		// 10: Voucher does not exist /
		// 11: Voucher already used by other subscriber
		// 12: Voucher missing/stolen
		// 13: Voucher unavailable
		// 100: Voucher already used by same subscriber
		// 102: Voucher expired
		// 104: Subscriber Id mismatch between the reservation and the end of reservation
		// 105: Voucher not reserved
		// 106: Transaction Id mismatch between messages between reservation and the end of reservation
		// 107: Voucher damaged
		// 108: Voucher reserved by other subscriber
		// 109: Database error /

		// Simulation
		try
		{
			// Get the Voucher
			Voucher voucher = super.voucherInfo.getVoucher(null, request.getActivationCode(), request.getNetworkOperatorId());
			if (voucher == null)
				return exitWith(response, Protocol.RESPONSECODE_VOUCHER_DOESNT_EXIST);

			// End Reservation
			response.setResponseCode(voucher.endReserve(request));
		}
		catch (Exception ex)
		{
			return exitWith(response, Protocol.RESPONSECODE_DATABASE_ERROR);
		}

		// Validate Response
		if (response.getResponseCode() != Protocol.RESPONSECODE_SUCCESS)
			return response;
		else if (!response.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_RESPONSE);

		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
}
