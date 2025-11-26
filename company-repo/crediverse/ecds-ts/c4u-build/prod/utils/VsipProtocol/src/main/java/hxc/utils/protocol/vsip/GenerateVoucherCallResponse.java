package hxc.utils.protocol.vsip;

// The message GenerateVoucher is used to schedule a generate voucher task.
// The GenerateVoucher message will be added to the VS Task Manager for
// immediate or scheduled execution.

public class GenerateVoucherCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GenerateVoucherResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GenerateVoucherResponse getResponse()
	{
		return response;
	}

	public void setResponse(GenerateVoucherResponse response)
	{
		this.response = response;
	}
}
