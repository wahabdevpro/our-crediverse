package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcMethod;

// The message GetVoucherBatchFilesList is used to get a list of all generated
// batch files. The filenames returned in the response are the names of the
// batchfiles that are used for loading vouchers in the LoadVoucherBatchFile
// tasks.

@XmlRpcMethod(name = "GetVoucherBatchFilesList")
public class GetVoucherBatchFilesListCallRequest implements IVsipCallRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetVoucherBatchFilesListRequest request;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetVoucherBatchFilesListRequest getRequest()
	{
		return request;
	}

	public void setRequest(GetVoucherBatchFilesListRequest request)
	{
		this.request = request;
	}
}
