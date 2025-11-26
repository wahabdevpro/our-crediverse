package hxc.utils.protocol.vsip;

// The DeleteTask message is used to delete a task. There is one request for
// each type of task but the parameters and behavior are the same for all of them.

public class DeleteVoucherDistributionReportTaskCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private DeleteVoucherDistributionReportTaskResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public DeleteVoucherDistributionReportTaskResponse getResponse()
	{
		return response;
	}

	public void setResponse(DeleteVoucherDistributionReportTaskResponse response)
	{
		this.response = response;
	}
}
