package hxc.utils.protocol.vsip;

// The DeleteTask message is used to delete a task. There is one request for
// each type of task but the parameters and behavior are the same for all of them.

public class DeleteVoucherUsageReportTaskCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private DeleteVoucherUsageReportTaskResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public DeleteVoucherUsageReportTaskResponse getResponse()
	{
		return response;
	}

	public void setResponse(DeleteVoucherUsageReportTaskResponse response)
	{
		this.response = response;
	}
}
