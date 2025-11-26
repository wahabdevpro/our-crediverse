package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The DeleteTask message is used to delete a task. There is one request for
// each type of task but the parameters and behavior are the same for all of them.

@XmlRpcMethod(name = "DeleteLoadVoucherBatchTask")
public class DeleteLoadVoucherBatchTaskCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private DeleteLoadVoucherBatchTaskRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public DeleteLoadVoucherBatchTaskRequest getRequest()
	{
		return request;
	}

	public void setRequest(DeleteLoadVoucherBatchTaskRequest request)
	{
		this.request = request;
	}
}
