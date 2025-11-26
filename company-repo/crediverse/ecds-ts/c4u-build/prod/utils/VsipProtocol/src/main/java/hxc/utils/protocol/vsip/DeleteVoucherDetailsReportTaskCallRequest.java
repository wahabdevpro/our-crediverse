package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The DeleteTask message is used to delete a task. There is one request for
// each type of task but the parameters and behavior are the same for all of them.

@XmlRpcMethod(name = "DeleteVoucherDetailsReportTask")
public class DeleteVoucherDetailsReportTaskCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private DeleteVoucherDetailsReportTaskRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public DeleteVoucherDetailsReportTaskRequest getRequest()
	{
		return request;
	}

	public void setRequest(DeleteVoucherDetailsReportTaskRequest request)
	{
		this.request = request;
	}
}
