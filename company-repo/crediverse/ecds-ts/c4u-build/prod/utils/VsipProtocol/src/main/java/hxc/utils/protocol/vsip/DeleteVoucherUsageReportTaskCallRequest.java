package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The DeleteTask message is used to delete a task. There is one request for
// each type of task but the parameters and behavior are the same for all of them.

@XmlRpcMethod(name = "DeleteVoucherUsageReportTask")
public class DeleteVoucherUsageReportTaskCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private DeleteVoucherUsageReportTaskRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public DeleteVoucherUsageReportTaskRequest getRequest()
	{
		return request;
	}

	public void setRequest(DeleteVoucherUsageReportTaskRequest request)
	{
		this.request = request;
	}
}
