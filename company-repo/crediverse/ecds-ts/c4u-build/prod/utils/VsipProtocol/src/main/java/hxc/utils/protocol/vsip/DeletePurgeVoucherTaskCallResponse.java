package hxc.utils.protocol.vsip;

// The DeleteTask message is used to delete a task. There is one request for
// each type of task but the parameters and behavior are the same for all of them.

public class DeletePurgeVoucherTaskCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private DeletePurgeVoucherTaskResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public DeletePurgeVoucherTaskResponse getResponse()
	{
		return response;
	}

	public void setResponse(DeletePurgeVoucherTaskResponse response)
	{
		this.response = response;
	}
}
