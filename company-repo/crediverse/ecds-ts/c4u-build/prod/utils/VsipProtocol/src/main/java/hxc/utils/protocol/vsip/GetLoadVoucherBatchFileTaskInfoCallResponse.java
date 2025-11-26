package hxc.utils.protocol.vsip;

// The message GetLoadVoucherBatchFileTaskInfo is used to retrieve information
// about a LoadVoucherBatchFile.

public class GetLoadVoucherBatchFileTaskInfoCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetLoadVoucherBatchFileTaskInfoResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetLoadVoucherBatchFileTaskInfoResponse getResponse()
	{
		return response;
	}

	public void setResponse(GetLoadVoucherBatchFileTaskInfoResponse response)
	{
		this.response = response;
	}
}
