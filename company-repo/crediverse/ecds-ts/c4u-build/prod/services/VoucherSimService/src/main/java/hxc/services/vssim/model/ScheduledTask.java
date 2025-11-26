package hxc.services.vssim.model;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import hxc.utils.protocol.vsip.IVsipRequest;
import hxc.utils.protocol.vsip.Protocol;
import hxc.utils.protocol.vsip.Recurrence;

public abstract class ScheduledTask<TReq extends IVsipRequest> implements Runnable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int taskId;
	protected String networkOperatorId;
	protected String taskStatus;

	protected String operatorId;
	protected String filename;
	protected String failReason;
	protected String additionalInfo;
	protected Recurrence recurrence;
	protected Integer recurrenceValue;
	protected ScheduledFuture<?> future;
	protected TReq request;
	protected Date executionTime;

	// The taskId parameter is used to state the unique Id that identifies a task in
	// the VS Task Manager.
	//

	public int getTaskId()
	{
		return taskId;
	}

	public void setTaskId(int taskId)
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
	// Size: 0-20 Allowed: A-Z,a-z,0-9
	public void setNetworkOperatorId(String networkOperatorId)
	{
		this.networkOperatorId = networkOperatorId;
	}

	public String getNetworkOperatorId()
	{
		return networkOperatorId;
	}

	// The taskStatus parameter is used to indicate in what state a task is in.
	//

	public void setTaskStatus(String taskStatus)
	{
		this.taskStatus = taskStatus;
	}

	public String getTaskStatus()
	{
		return taskStatus;
	}

	// The operatorId parameter is used to define the name of the operator who
	// carried out the operation.
	// When used in a response message it represents the operator that did the
	// change on the voucher.
	//

	public String getOperatorId()
	{
		return operatorId;
	}

	public void setOperatorId(String operatorId)
	{
		this.operatorId = operatorId;
	}

	// The filename parameter is the filename generated as output from the specific
	// operation. Note that the filename is not a complete filename. The full path of
	// the file is not included and the suffix of the file may be excluded (for report
	// files for example).
	//
	// Will only be included if taskStatus is completed.
	//

	public String getFilename()
	{
		return filename;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	// The failReason parameter is used to describe the cause of a failed task
	// in execution.
	//
	// failReason will be included if taskStatus is failed.
	//

	public String getFailReason()
	{
		return failReason;
	}

	public void setFailReason(String failReason)
	{
		this.failReason = failReason;
	}

	// The additionalInfo parameter is used to hold additional information such
	// as how many vouchers of a voucher batch file that was successfully loaded.
	//

	public String getAdditionalInfo()
	{
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo)
	{
		this.additionalInfo = additionalInfo;
	}

	// The recurrence parameter is, in combination with the recurrenceValue
	// parameter, used to define how often a scheduled task should be executed. This
	// parameter indicates whether the recurrence is described in terms of days,
	// weeks or months.
	// Range: daily,weekly,monthly
	public void setRecurrence(Recurrence recurrence)
	{
		this.recurrence = recurrence;
	}

	public Integer getRecurrenceValue()
	{
		return recurrenceValue;
	}

	// The recurrenceValue parameter is, in combination with the
	// recurrence parameter, used to define how often a scheduled task should be
	// executed. This parameter defines the interval of the recurrence.
	// Range: 1:99999
	public void setRecurrenceValue(Integer recurrenceValue)
	{
		this.recurrenceValue = recurrenceValue;
	}

	public Recurrence getRecurrence()
	{
		return recurrence;
	}

	// Java Future for executing this task
	public ScheduledFuture<?> getFuture()
	{
		return future;
	}

	public void setFuture(ScheduledFuture<?> future)
	{
		this.future = future;
	}

	public TReq getRequest()
	{
		return request;
	}

	public void setRequest(TReq request)
	{
		this.request = request;
	}

	// The executionTime parameter is used to define the time when a task was
	// run or should be run.
	//
	// Mandatory

	public Date getExecutionTime()
	{
		return executionTime;
	}

	public void setExecutionTime(Date executionTime)
	{
		this.executionTime = executionTime;
	}

	public boolean isOfType(Class<? extends IVsipRequest> cls)
	{
		return request != null && cls.isInstance(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public ScheduledTask(TReq request, String networkOperatorId, String operatorId)
	{
		this.request = request;
		this.taskStatus = Protocol.TASKSTATUS_ORDERED;
		this.networkOperatorId = networkOperatorId;
		this.operatorId = operatorId;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	// /////////////////////////////////
	@Override
	public void run()
	{
		try
		{
			executionTime = new Date();
			taskStatus = Protocol.TASKSTATUS_RUNNING;
			taskStatus = execute() ? Protocol.TASKSTATUS_COMPLETED : Protocol.TASKSTATUS_FAILED;
		}
		catch (Exception ex)
		{
			taskStatus = Protocol.TASKSTATUS_FAILED;
			additionalInfo = ex.getMessage();
		}
	}

	public abstract boolean execute() throws Exception;

}
