package hxc.utils.protocol.vsip;

// The message GetPurgeVouchersTaskInfo message is used to return
// information about a PurgeVoucherTask.

public class GetPurgeVouchersTaskInfoCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetPurgeVouchersTaskInfoResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetPurgeVouchersTaskInfoResponse getResponse()
	{
		return response;
	}

	public void setResponse(GetPurgeVouchersTaskInfoResponse response)
	{
		this.response = response;
	}
}
