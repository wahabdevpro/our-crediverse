package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The DeleteTask message is used to delete a task. There is one request for
// each type of task but the parameters and behavior are the same for all of them.

@XmlRpcMethod(name = "DeleteChangeVoucherStateTask")
public class DeleteChangeVoucherStateTaskCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private DeleteChangeVoucherStateTaskRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public DeleteChangeVoucherStateTaskRequest getRequest()
	{
		return request;
	}

	public void setRequest(DeleteChangeVoucherStateTaskRequest request)
	{
		this.request = request;
	}
}
