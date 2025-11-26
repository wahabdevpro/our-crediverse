package hxc.utils.protocol.vsip;

// The message LoadVoucherBatchFile is used to schedule the loading of a
// batch file. This message will be added to the task manager for immediate or
// scheduled execution.

public class LoadVoucherBatchFileCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private LoadVoucherBatchFileResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public LoadVoucherBatchFileResponse getResponse()
	{
		return response;
	}

	public void setResponse(LoadVoucherBatchFileResponse response)
	{
		this.response = response;
	}
}
