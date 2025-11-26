package hxc.utils.protocol.vsip;

// The message GetGenerateVoucherTaskInfo is used to retrieve information
// about a GenerateVoucher task.

public class GetGenerateVoucherTaskInfoCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetGenerateVoucherTaskInfoResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetGenerateVoucherTaskInfoResponse getResponse()
	{
		return response;
	}

	public void setResponse(GetGenerateVoucherTaskInfoResponse response)
	{
		this.response = response;
	}
}
