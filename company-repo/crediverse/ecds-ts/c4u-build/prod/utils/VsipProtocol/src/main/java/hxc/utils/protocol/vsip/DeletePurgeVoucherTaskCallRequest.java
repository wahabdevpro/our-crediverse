package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The DeleteTask message is used to delete a task. There is one request for
// each type of task but the parameters and behavior are the same for all of them.

@XmlRpcMethod(name = "DeletePurgeVoucherTask")
public class DeletePurgeVoucherTaskCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private DeletePurgeVoucherTaskRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public DeletePurgeVoucherTaskRequest getRequest()
	{
		return request;
	}

	public void setRequest(DeletePurgeVoucherTaskRequest request)
	{
		this.request = request;
	}
}
