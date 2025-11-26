package hxc.utils.protocol.vsip;

// The message ChangeVoucherState message is used to schedule a task to
// change the state of vouchers.
// Caution!
// When using alphanumeric serial numbers (PC), range based operations are not
// recommended since it is likely to affect more vouchers than intended.

public class ChangeVoucherStateResponse implements IVsipResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private int taskId;
	private int responseCode;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// The taskId parameter is used to state the unique Id that identifies a task in
	// the VS Task Manager.
	//
	// Mandatory

	public int getTaskId()
	{
		return taskId;
	}

	public void setTaskId(int taskId)
	{
		this.taskId = taskId;
	}

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

		return Protocol.validateTaskId(context, true, taskId) && //
				Protocol.validateResponseCode(context, true, responseCode);
	}

}
