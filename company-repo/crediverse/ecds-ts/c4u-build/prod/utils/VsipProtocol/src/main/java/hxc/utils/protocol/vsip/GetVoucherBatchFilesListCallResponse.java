package hxc.utils.protocol.vsip;

// The message GetVoucherBatchFilesList is used to get a list of all generated
// batch files. The filenames returned in the response are the names of the
// batchfiles that are used for loading vouchers in the LoadVoucherBatchFile
// tasks.

public class GetVoucherBatchFilesListCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private GetVoucherBatchFilesListResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public GetVoucherBatchFilesListResponse getResponse()
	{
		return response;
	}

	public void setResponse(GetVoucherBatchFilesListResponse response)
	{
		this.response = response;
	}
}
