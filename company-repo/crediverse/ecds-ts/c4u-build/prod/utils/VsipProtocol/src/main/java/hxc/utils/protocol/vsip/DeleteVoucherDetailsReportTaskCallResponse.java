package hxc.utils.protocol.vsip;

// The DeleteTask message is used to delete a task. There is one request for
// each type of task but the parameters and behavior are the same for all of them.

public class DeleteVoucherDetailsReportTaskCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private DeleteVoucherDetailsReportTaskResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public DeleteVoucherDetailsReportTaskResponse getResponse()
	{
		return response;
	}

	public void setResponse(DeleteVoucherDetailsReportTaskResponse response)
	{
		this.response = response;
	}
}
