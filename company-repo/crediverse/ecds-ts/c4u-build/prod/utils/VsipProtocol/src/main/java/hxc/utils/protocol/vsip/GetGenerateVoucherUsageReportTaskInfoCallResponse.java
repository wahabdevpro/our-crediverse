package hxc.utils.protocol.vsip;

// The GetGenerateVoucherUsageReportTaskInfo message is used to return
// information about a specific or all GenerateVoucherUsageReport tasks.

public class GetGenerateVoucherUsageReportTaskInfoCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetGenerateVoucherUsageReportTaskInfoResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetGenerateVoucherUsageReportTaskInfoResponse getResponse()
	{
		return response;
	}

	public void setResponse(GetGenerateVoucherUsageReportTaskInfoResponse response)
	{
		this.response = response;
	}
}
