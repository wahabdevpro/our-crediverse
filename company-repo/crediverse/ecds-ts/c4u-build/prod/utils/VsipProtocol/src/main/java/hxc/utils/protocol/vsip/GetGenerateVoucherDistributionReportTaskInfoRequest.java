package hxc.utils.protocol.vsip;

// The GetGenerateVoucherDistributionReportTaskInfo message is used to return
// information about a GenerateVoucherDistributionReport task.

public class GetGenerateVoucherDistributionReportTaskInfoRequest implements IVsipRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Integer taskId;
	private String networkOperatorId;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// The taskId parameter is used to state the unique Id that identifies a task in
	// the VS Task Manager.
	//
	// If omitted all GenerateVoucherDistributionReport tasks are returned.
	//
	// Optional

	public Integer getTaskId()
	{
		return taskId;
	}

	public void setTaskId(Integer taskId)
	{
		this.taskId = taskId;
	}

	// The networkOperatorId parameter is used to reference a Mobile Virtual
	// Network Operator. The VS system is capable of administering and managing
	// multiple operators simultaneously. Each Mobile Virtual Network Operator has
	// its own database schema, in which this operator's own vouchers are stored.
	// The parameter is bound to the Mobile Virtual Network Operator functionality,
	// which must be explicitly configured. If not activated, the parameter is not
	// mandatory, in which case all requests are targeted to the default database
	// schema of the VS system.
	//
	// This element is mandatory if Mobile Virtual Network Operator functionality is activated;
	// otherwise, the element is optional.
	//
	// Optional

	public String getNetworkOperatorId()
	{
		return networkOperatorId;
	}

	public void setNetworkOperatorId(String networkOperatorId)
	{
		this.networkOperatorId = networkOperatorId;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	@Override
	public boolean validate(IValidationContext context)
	{
		return Protocol.validateTaskId(context, false, taskId) && //
				Protocol.validateNetworkOperatorId(context, networkOperatorId);
	}

}
