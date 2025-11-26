package hxc.utils.protocol.vsip;

// The message GetChangeVoucherStateTaskInfo message is used to retrieve
// information about a ChangeVoucherState task.

public class GetChangeVoucherStateTaskInfoCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetChangeVoucherStateTaskInfoResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetChangeVoucherStateTaskInfoResponse getResponse()
	{
		return response;
	}

	public void setResponse(GetChangeVoucherStateTaskInfoResponse response)
	{
		this.response = response;
	}
}
