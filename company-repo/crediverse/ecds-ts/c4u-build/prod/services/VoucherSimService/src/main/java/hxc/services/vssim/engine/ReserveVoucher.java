package hxc.services.vssim.engine;

import hxc.services.vssim.IVoucherInfo;
import hxc.services.vssim.model.Voucher;
import hxc.utils.protocol.vsip.Protocol;
import hxc.utils.protocol.vsip.ReserveVoucherCallRequest;
import hxc.utils.protocol.vsip.ReserveVoucherCallResponse;
import hxc.utils.protocol.vsip.ReserveVoucherRequest;
import hxc.utils.protocol.vsip.ReserveVoucherResponse;

public class ReserveVoucher extends VoucherCallBase<ReserveVoucherResponse, ReserveVoucherRequest>
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
	public ReserveVoucher(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "ReserveVoucher");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static ReserveVoucherCallResponse call(IVoucherInfo voucherInfo, ReserveVoucherCallRequest call, String operatorID)
	{
		ReserveVoucherCallResponse response = new ReserveVoucherCallResponse();
		response.setResponse(new ReserveVoucher(voucherInfo).execute(new ReserveVoucherResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public ReserveVoucherResponse exec(ReserveVoucherRequest request, String operatorID)
	{
		// Create Response
		ReserveVoucherResponse response = new ReserveVoucherResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_REQUEST);

		// Possible Errors
		// 10 Voucher does not exist /
		// 11 Voucher already used by other subscriber
		// 12 Voucher missing/stolen
		// 13 Voucher unavailable
		// 100 Voucher already used by same subscriber
		// 102 Voucher expired
		// 107 Voucher damaged
		// 108 Voucher reserved by other subscriber
		// 109 Database error /

		try
		{
			// Get Voucher
			Voucher voucher = super.voucherInfo.getVoucher(null, request.getActivationCode(), request.getNetworkOperatorId());
			if (voucher == null)
				return exitWith(response, Protocol.RESPONSECODE_TASK_DOESNT_EXIST);

			response.setResponseCode(voucher.reserve(request));
			response.setAgent(voucher.getAgent());
			response.setCurrency(voucher.getCurrency());
			response.setExtensionText1(voucher.getExtensionText1());
			response.setExtensionText2(voucher.getExtensionText2());
			response.setExtensionText3(voucher.getExtensionText3());
			response.setSerialNumber(voucher.getSerialNumber());
			response.setVoucherGroup(voucher.getVoucherGroup());
			response.setValue(voucher.getValue());
			response.setSupplierId(voucher.getSupplierId());

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
