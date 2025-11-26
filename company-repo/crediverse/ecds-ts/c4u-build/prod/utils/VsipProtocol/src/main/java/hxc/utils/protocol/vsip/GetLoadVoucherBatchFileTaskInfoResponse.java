package hxc.utils.protocol.vsip;

// The message GetLoadVoucherBatchFileTaskInfo is used to retrieve information
// about a LoadVoucherBatchFile.

public class GetLoadVoucherBatchFileTaskInfoResponse implements IVsipResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private int responseCode;
	private GetLoadVoucherBatchFileTaskInfoTasks[] tasks;

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

	// Each task record is enclosed in a <struct> of its own. Structs are placed in an <array>.
	//
	// Optional

	public GetLoadVoucherBatchFileTaskInfoTasks[] getTasks()
	{
		return tasks;
	}

	public void setTasks(GetLoadVoucherBatchFileTaskInfoTasks[] tasks)
	{
		this.tasks = tasks;
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
				validateTasks(context, false, tasks);
	}

	private boolean validateTasks(IValidationContext context, boolean required, GetLoadVoucherBatchFileTaskInfoTasks[] tasks)
	{
		if (tasks == null || tasks.length == 0)
			return !required;

		for (GetLoadVoucherBatchFileTaskInfoTasks task : tasks)
		{
			if (!task.validate(context))
				return false;
		}

		return true;
	}

}
