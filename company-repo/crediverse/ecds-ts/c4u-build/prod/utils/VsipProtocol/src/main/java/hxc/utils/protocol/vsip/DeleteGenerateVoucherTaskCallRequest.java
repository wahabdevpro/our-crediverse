package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The DeleteTask message is used to delete a task. There is one request for
// each type of task but the parameters and behavior are the same for all of them.

@XmlRpcMethod(name = "DeleteGenerateVoucherTask")
public class DeleteGenerateVoucherTaskCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private DeleteGenerateVoucherTaskRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public DeleteGenerateVoucherTaskRequest getRequest()
	{
		return request;
	}

	public void setRequest(DeleteGenerateVoucherTaskRequest request)
	{
		this.request = request;
	}
}
