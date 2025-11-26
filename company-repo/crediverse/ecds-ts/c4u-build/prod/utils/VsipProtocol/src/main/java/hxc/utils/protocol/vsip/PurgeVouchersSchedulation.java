package hxc.utils.protocol.vsip;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import hxc.utils.xmlrpc.XmlRpcAsString;

@XmlAccessorType(XmlAccessType.FIELD)
public class PurgeVouchersSchedulation
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	@XmlElement(required = true)
	private Date executionTime;
	@XmlRpcAsString
	private Recurrence recurrence;
	private Integer recurrenceValue;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

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

	// The recurrence parameter is, in combination with the recurrenceValue
	// parameter, used to define how often a scheduled task should be executed. This
	// parameter indicates whether the recurrence is described in terms of days,
	// weeks or months.
	//
	// Optional

	public Recurrence getRecurrence()
	{
		return recurrence;
	}

	public void setRecurrence(Recurrence recurrence)
	{
		this.recurrence = recurrence;
	}

	// The recurrenceValue parameter is, in combination with the
	// recurrence parameter, used to define how often a scheduled task should be
	// executed. This parameter defines the interval of the recurrence.
	//
	// Optional

	public Integer getRecurrenceValue()
	{
		return recurrenceValue;
	}

	public void setRecurrenceValue(Integer recurrenceValue)
	{
		this.recurrenceValue = recurrenceValue;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public boolean validate(IValidationContext context)
	{
		return Protocol.validateExecutionTime(context, true, executionTime) && //
				Protocol.validateRecurrence(context, false, recurrence) && //
				Protocol.validateRecurrenceValue(context, false, recurrenceValue);
	}

}
