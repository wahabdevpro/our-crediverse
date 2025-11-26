package hxc.services.vssim.engine;

import hxc.services.vssim.IVoucherInfo;
import hxc.services.vssim.model.Voucher;
import hxc.utils.protocol.vsip.GetVoucherHistoryCallRequest;
import hxc.utils.protocol.vsip.GetVoucherHistoryCallResponse;
import hxc.utils.protocol.vsip.GetVoucherHistoryRequest;
import hxc.utils.protocol.vsip.GetVoucherHistoryResponse;
import hxc.utils.protocol.vsip.GetVoucherHistoryTransactionRecords;
import hxc.utils.protocol.vsip.Protocol;

public class GetVoucherHistory extends VoucherCallBase<GetVoucherHistoryResponse, GetVoucherHistoryRequest>
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
	public GetVoucherHistory(IVoucherInfo voucherInfo)
	{
		super(voucherInfo, "GetVoucherHistory");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call
	//
	// /////////////////////////////////
	public static GetVoucherHistoryCallResponse call(IVoucherInfo voucherInfo, GetVoucherHistoryCallRequest call, String operatorID)
	{
		GetVoucherHistoryCallResponse response = new GetVoucherHistoryCallResponse();
		response.setResponse(new GetVoucherHistory(voucherInfo).execute(new GetVoucherHistoryResponse(), call.getRequest(), operatorID));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	// /////////////////////////////////
	@Override
	public GetVoucherHistoryResponse exec(GetVoucherHistoryRequest request, String operatorID)
	{
		// Create Response
		GetVoucherHistoryResponse response = new GetVoucherHistoryResponse();

		// Validate Request
		if (!request.validate(voucherInfo))
			exitWith(response, Protocol.RESPONSECODE_MALFORMED_REQUEST);

		// Simulate Response
		try
		{
			Voucher voucher = super.voucherInfo.getVoucher(request.getSerialNumber(), request.getActivationCode(), request.getNetworkOperatorId());
			if (voucher == null)
				return exitWith(response, Protocol.RESPONSECODE_VOUCHER_DOESNT_EXIST);

			response.setAgent(voucher.getAgent());
			response.setBatchId(voucher.getBatchId());
			response.setCurrency(voucher.getCurrency());
			response.setExpiryDate(voucher.getExpiryDate());
			response.setExtensionText1(voucher.getExtensionText1());
			response.setExtensionText2(voucher.getExtensionText2());
			response.setExtensionText3(voucher.getExtensionText3());
			response.setValue(voucher.getValue());
			response.setVoucherGroup(voucher.getVoucherGroup());
			response.setTransactionRecords(voucher.getTransactionRecords());
			response.setVoucherExpired(voucher.getVoucherExpired());
			response.setSupplierId(voucher.getSupplierId());

			GetVoucherHistoryTransactionRecords lastTransaction = voucher.getLastTransaction();
			if (lastTransaction != null)
			{
				response.setState(lastTransaction.getNewState());
			}
			response.setResponseCode(Protocol.RESPONSECODE_SUCCESS);
		}
		catch (Exception e)
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
