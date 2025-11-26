package hxc.utils.protocol.vsip;

// The message LoadVoucherCheck is used to check if the vouchers in a serial
// number range are loaded into the database. The number of voucher found
// within the range are returned. If any voucher is missing, this is indicated
// in the response code, which is 10 (Voucher Does not exist), in that case.
// The requested serial number range is pointed out by serialNumberFirst and
// serialNumberLast.
// Note: When using alphanumeric serial numbers (PC) the numberOfVouchers
// parameter will not always be correct. The use of message
// LoadVoucherCheck is not recommended for alphanumeric serial
// numbers (PC).

public class LoadVoucherCheckResponse implements IVsipResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private int numberOfVouchers;
	private int responseCode;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// The numberOfVouchers parameter is used to define the number of vouchers
	// in a batch or a serial number range.
	//
	// Mandatory

	public int getNumberOfVouchers()
	{
		return numberOfVouchers;
	}

	public void setNumberOfVouchers(int numberOfVouchers)
	{
		this.numberOfVouchers = numberOfVouchers;
	}

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
		if (responseCode != Protocol.RESPONSECODE_SUCCESS)
			return Protocol.validateResponseCode(context, true, responseCode);

		return Protocol.validateNumberOfVouchers(context, true, numberOfVouchers) && //
				Protocol.validateResponseCode(context, true, responseCode);
	}

}
