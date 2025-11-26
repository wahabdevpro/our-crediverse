package hxc.utils.protocol.vsip;

// The GetGenerateVoucherDistributionReportTaskInfo message is used to return
// information about a GenerateVoucherDistributionReport task.

public class GetGenerateVoucherDistributionReportTaskInfoCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetGenerateVoucherDistributionReportTaskInfoResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetGenerateVoucherDistributionReportTaskInfoResponse getResponse()
	{
		return response;
	}

	public void setResponse(GetGenerateVoucherDistributionReportTaskInfoResponse response)
	{
		this.response = response;
	}
}
