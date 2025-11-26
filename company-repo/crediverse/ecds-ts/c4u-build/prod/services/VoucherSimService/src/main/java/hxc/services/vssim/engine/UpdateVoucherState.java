package hxc.services.vssim.engine;

import java.util.Date;

import hxc.services.vssim.IVoucherInfo;
import hxc.services.vssim.model.Voucher;
import hxc.utils.protocol.vsip.Protocol;
import hxc.utils.protocol.vsip.UpdateVoucherStateCallRequest;
import hxc.utils.protocol.vsip.UpdateVoucherStateCallResponse;
import hxc.utils.protocol.vsip.UpdateVoucherStateRequest;
import hxc.utils.protocol.vsip.UpdateVoucherStateResponse;

public class UpdateVoucherState extends VoucherCallBase<UpdateVoucherStateResponse, UpdateVoucherStateRequest>
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
	public UpdateVoucherState(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "UpdateVoucherState");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static UpdateVoucherStateCallResponse call(IVoucherInfo voucherInfo, UpdateVoucherStateCallRequest call, String operatorID)
	{
		UpdateVoucherStateCallResponse response = new UpdateVoucherStateCallResponse();
		response.setResponse(new UpdateVoucherState(voucherInfo).execute(new UpdateVoucherStateResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public UpdateVoucherStateResponse exec(UpdateVoucherStateRequest request, String operatorID)
	{
		// Create Response
		UpdateVoucherStateResponse response = new UpdateVoucherStateResponse();

		// Set Lower Case States
		if (request.getNewState() != null)
			request.setNewState(request.getNewState().toLowerCase());

		if (request.getOldState() != null)
			request.setOldState(request.getOldState().toLowerCase());

		// Validate Request
		if (!request.validate(voucherInfo))
		{
			response.setResponseCode(Protocol.RESPONSECODE_MALFORMED_REQUEST);
			return response;
		}

		// Validate the State Change
		if (request.getOldState() != null && request.getOldState().length() > 0 && !validateStateChange(request.getOldState(), request.getNewState()))
		{
			return exitWith(response, Protocol.RESPONSECODE_BAD_STATE_TRANSITION);
		}

		// Get the Voucher
		Voucher voucher = super.voucherInfo.getVoucher(request.getSerialNumber(), request.getActivationCode(), request.getNetworkOperatorId());
		if (voucher == null)
			return exitWith(response, Protocol.RESPONSECODE_VOUCHER_DOESNT_EXIST);

		// Change the Voucher State
		voucher.setNewState(request.getNewState(), operatorID, null, new Date(), null);

		response.setResponseCode(Protocol.RESPONSECODE_SUCCESS);

		// Validate Response
		if (response.getResponseCode() != Protocol.RESPONSECODE_SUCCESS)
			return response;
		else if (!response.validate(voucherInfo))
		{
			response.setResponseCode(Protocol.RESPONSECODE_MALFORMED_RESPONSE);
			return response;
		}

		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	private boolean validateStateChange(String oldState, String newState)
	{
		return ((oldState.equals(Protocol.STATE_UNAVAILABLE) && (newState.equals(Protocol.STATE_AVAILABLE) || newState.equals(Protocol.STATE_DAMAGED) || newState.equals(Protocol.STATE_STOLEN)))
				|| ((oldState.equals(Protocol.STATE_AVAILABLE) && (newState.equals(Protocol.STATE_UNAVAILABLE) || newState.equals(Protocol.STATE_DAMAGED) || newState.equals(Protocol.STATE_STOLEN))))
				|| ((oldState.equals(Protocol.STATE_PENDING) && (newState.equals(Protocol.STATE_AVAILABLE) || newState.equals(Protocol.STATE_USED)))) || ((oldState.equals(Protocol.STATE_STOLEN) && (newState
				.equals(Protocol.STATE_UNAVAILABLE) || newState.equals(Protocol.STATE_AVAILABLE)))));
	}
}
