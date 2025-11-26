package hxc.utils.protocol.vsip;

// The DeleteTask message is used to delete a task. There is one request for
// each type of task but the parameters and behavior are the same for all of them.

public class DeleteGenerateVoucherTaskCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private DeleteGenerateVoucherTaskResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public DeleteGenerateVoucherTaskResponse getResponse()
	{
		return response;
	}

	public void setResponse(DeleteGenerateVoucherTaskResponse response)
	{
		this.response = response;
	}
}
