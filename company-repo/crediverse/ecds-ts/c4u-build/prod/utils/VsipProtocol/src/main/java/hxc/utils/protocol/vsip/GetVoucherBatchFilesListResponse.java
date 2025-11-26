package hxc.utils.protocol.vsip;

// The message GetVoucherBatchFilesList is used to get a list of all generated
// batch files. The filenames returned in the response are the names of the
// batchfiles that are used for loading vouchers in the LoadVoucherBatchFile
// tasks.

public class GetVoucherBatchFilesListResponse implements IVsipResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private int responseCode;
	private GetVoucherBatchFilesListFilenames[] filenames;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// The responseCode parameter is sent back after a message has been
	// processed and indicates success or failure of the message.
	//
	// Mandatory

	@Override
	public int getResponseCode()
	{
		return responseCode;
	}

	@Override
	public void setResponseCode(int responseCode)
	{
		this.responseCode = responseCode;
	}

	// Optional

	public GetVoucherBatchFilesListFilenames[] getFilenames()
	{
		return filenames;
	}

	public void setFilenames(GetVoucherBatchFilesListFilenames[] filenames)
	{
		this.filenames = filenames;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	@Override
	public boolean validate(IValidationContext context)
	{
		if (responseCode != Protocol.RESPONSECODE_SUCCESS)
			return Protocol.validateResponseCode(context, true, responseCode);

		return Protocol.validateResponseCode(context, true, responseCode) && //
				validateFilenames(context, false, filenames);
	}

	private boolean validateFilenames(IValidationContext context, boolean required, GetVoucherBatchFilesListFilenames[] filenames)
	{
		if (filenames == null || filenames.length == 0)
			return !required;

		for (GetVoucherBatchFilesListFilenames filename : filenames)
		{
			if (!filename.validate(context))
				return false;
		}

		return true;
	}

}
