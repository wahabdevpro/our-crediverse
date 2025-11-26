package hxc.utils.protocol.vsip;

// The DeleteTask message is used to delete a task. There is one request for
// each type of task but the parameters and behavior are the same for all of them.

public class DeleteChangeVoucherStateTaskCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private DeleteChangeVoucherStateTaskResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public DeleteChangeVoucherStateTaskResponse getResponse()
	{
		return response;
	}

	public void setResponse(DeleteChangeVoucherStateTaskResponse response)
	{
		this.response = response;
	}
}
