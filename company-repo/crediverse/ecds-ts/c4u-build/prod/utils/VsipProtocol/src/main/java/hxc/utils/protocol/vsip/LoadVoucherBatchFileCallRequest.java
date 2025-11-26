package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The message LoadVoucherBatchFile is used to schedule the loading of a
// batch file. This message will be added to the task manager for immediate or
// scheduled execution.

@XmlRpcMethod(name = "LoadVoucherBatchFile")
public class LoadVoucherBatchFileCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private LoadVoucherBatchFileRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public LoadVoucherBatchFileRequest getRequest()
	{
		return request;
	}

	public void setRequest(LoadVoucherBatchFileRequest request)
	{
		this.request = request;
	}
}
