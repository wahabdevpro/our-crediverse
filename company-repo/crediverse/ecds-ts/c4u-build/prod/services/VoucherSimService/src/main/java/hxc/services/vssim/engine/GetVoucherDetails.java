package hxc.services.vssim.engine;

import hxc.services.vssim.IVoucherInfo;
import hxc.services.vssim.model.Voucher;
import hxc.utils.protocol.vsip.GetVoucherDetailsCallRequest;
import hxc.utils.protocol.vsip.GetVoucherDetailsCallResponse;
import hxc.utils.protocol.vsip.GetVoucherDetailsRequest;
import hxc.utils.protocol.vsip.GetVoucherDetailsResponse;
import hxc.utils.protocol.vsip.GetVoucherHistoryTransactionRecords;
import hxc.utils.protocol.vsip.Protocol;

public class GetVoucherDetails extends VoucherCallBase<GetVoucherDetailsResponse, GetVoucherDetailsRequest>
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
	public GetVoucherDetails(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "GetVoucherDetails");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static GetVoucherDetailsCallResponse call(IVoucherInfo voucherInfo, GetVoucherDetailsCallRequest call, String operatorID)
	{
		GetVoucherDetailsCallResponse response = new GetVoucherDetailsCallResponse();
		response.setResponse(new GetVoucherDetails(voucherInfo).execute(new GetVoucherDetailsResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public GetVoucherDetailsResponse exec(GetVoucherDetailsRequest request, String operatorID)
	{
		// Create Response
		GetVoucherDetailsResponse response = new GetVoucherDetailsResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
			return exitWith(response, Protocol.RESPONSECODE_MALFORMED_REQUEST);

		// Simulate
		try
		{
			Voucher voucher = super.voucherInfo.getVoucher(request.getSerialNumber(), request.getActivationCode(), request.getNetworkOperatorId());
			if (voucher == null)
				return exitWith(response, Protocol.RESPONSECODE_VOUCHER_DOESNT_EXIST);
			response.setActivationCode(voucher.getActivationCode());
			response.setAgent(voucher.getAgent());
			response.setBatchId(voucher.getBatchId());
			response.setCurrency(voucher.getCurrency());
			response.setExpiryDate(voucher.getExpiryDate());
			response.setExtensionText1(voucher.getExtensionText1());
			response.setExtensionText2(voucher.getExtensionText2());
			response.setExtensionText3(voucher.getExtensionText3());
			GetVoucherHistoryTransactionRecords lastTransaction = voucher.getLastTransaction();
			if (lastTransaction != null)
			{
				response.setOperatorId(lastTransaction.getOperatorId());
				response.setSubscriberId(lastTransaction.getSubscriberId());
				response.setTimestamp(lastTransaction.getTimestamp());
				response.setState(lastTransaction.getNewState());
			}
			response.setValue(voucher.getValue());
			response.setVoucherGroup(voucher.getVoucherGroup());
			response.setSerialNumber(voucher.getSerialNumber());
			response.setVoucherExpired(voucher.getVoucherExpired());
			response.setSupplierId(voucher.getSupplierId());
			response.setResponseCode(Protocol.RESPONSECODE_SUCCESS);
		}
		catch (Exception e)
		{
			logger.error("", e);
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
