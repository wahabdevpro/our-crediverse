package hxc.utils.protocol.vsip;

// The message GetChangeVoucherStateTaskInfo message is used to retrieve
// information about a ChangeVoucherState task.

public class GetChangeVoucherStateTaskInfoResponse implements IVsipResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private int responseCode;
	private GetChangeVoucherStateTaskInfoTasks[] tasks;

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

	// The task record is enclosed in a <struct>. Structs are placed in an <array>, see Table 38.
	//
	// Optional

	public GetChangeVoucherStateTaskInfoTasks[] getTasks()
	{
		return tasks;
	}

	public void setTasks(GetChangeVoucherStateTaskInfoTasks[] tasks)
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

	private boolean validateTasks(IValidationContext context, boolean required, GetChangeVoucherStateTaskInfoTasks[] tasks)
	{
		if (tasks == null || tasks.length == 0)
			return !required;

		for (GetChangeVoucherStateTaskInfoTasks task : tasks)
		{
			if (!task.validate(context))
				return false;
		}

		return true;
	}

}
