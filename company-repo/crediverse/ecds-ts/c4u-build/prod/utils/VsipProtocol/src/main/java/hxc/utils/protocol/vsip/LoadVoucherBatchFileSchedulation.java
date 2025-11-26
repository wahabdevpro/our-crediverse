package hxc.utils.protocol.vsip;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class LoadVoucherBatchFileSchedulation
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	@XmlElement(required = true)
	private Date executionTime;

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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public boolean validate(IValidationContext context)
	{
		return Protocol.validateExecutionTime(context, true, executionTime);
	}

}
