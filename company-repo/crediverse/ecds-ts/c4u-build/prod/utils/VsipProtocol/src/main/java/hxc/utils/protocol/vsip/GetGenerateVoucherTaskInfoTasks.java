package hxc.utils.protocol.vsip;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class GetGenerateVoucherTaskInfoTasks
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	@XmlElement(required = true)
	private int taskId;
	@XmlElement(required = true)
	private String taskStatus;
	@XmlElement(required = true)
	private Date executionTime;
	@XmlElement(required = true)
	private String operatorId;
	private String filename;
	private String failReason;
	private String additionalInfo;
	private GetGenerateVoucherTaskInfoTaskData taskData;

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

	// The taskStatus parameter is used to indicate in what state a task is in.
	//
	// Mandatory

	public String getTaskStatus()
	{
		return taskStatus;
	}

	public void setTaskStatus(String taskStatus)
	{
		this.taskStatus = taskStatus;
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

	// The operatorId parameter is used to define the name of the operator who
	// carried out the operation.
	// When used in a response message it represents the operator that did the
	// change on the voucher.
	//
	// Mandatory

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
	// Optional

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
	// Optional

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
	// Optional

	public String getAdditionalInfo()
	{
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo)
	{
		this.additionalInfo = additionalInfo;
	}

	// Mandatory

	public GetGenerateVoucherTaskInfoTaskData getTaskData()
	{
		return taskData;
	}

	public void setTaskData(GetGenerateVoucherTaskInfoTaskData taskData)
	{
		this.taskData = taskData;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public boolean validate(IValidationContext context)
	{
		return Protocol.validateTaskId(context, true, taskId) && //
				Protocol.validateTaskStatus(context, true, taskStatus) && //
				Protocol.validateExecutionTime(context, true, executionTime) && //
				Protocol.validateOperatorId(context, true, operatorId) && //
				Protocol.validateFilename(context, false, filename) && //
				Protocol.validateFailReason(context, false, failReason) && //
				Protocol.validateAdditionalInfo(context, false, additionalInfo) && //
				(taskData != null && taskData.validate(context));
	}

}
