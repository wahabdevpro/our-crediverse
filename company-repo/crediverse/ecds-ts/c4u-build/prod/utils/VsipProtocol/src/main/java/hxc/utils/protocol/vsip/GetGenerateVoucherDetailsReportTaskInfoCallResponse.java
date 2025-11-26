package hxc.utils.protocol.vsip;

// The GetGenerateVoucherDetailsReportTaskInfo message is used to return
// information about a GenerateVoucherDetailsReport Task.

public class GetGenerateVoucherDetailsReportTaskInfoCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetGenerateVoucherDetailsReportTaskInfoResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetGenerateVoucherDetailsReportTaskInfoResponse getResponse()
	{
		return response;
	}

	public void setResponse(GetGenerateVoucherDetailsReportTaskInfoResponse response)
	{
		this.response = response;
	}
}
