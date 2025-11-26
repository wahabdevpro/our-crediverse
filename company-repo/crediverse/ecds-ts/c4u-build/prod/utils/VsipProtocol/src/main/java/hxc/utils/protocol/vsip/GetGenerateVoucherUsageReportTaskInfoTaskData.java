package hxc.utils.protocol.vsip;

import java.util.Date;

public class GetGenerateVoucherUsageReportTaskInfoTaskData
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Date fromTime;
	private Date toTime;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// The fromTime parameter together with toTime parameter is used to create
	// a time frame, which can be used to define, for example a matching criteria.
	// The fromTime parameter is used to determine which vouchers was used in
	// a specific time frame.
	//
	// Optional

	public Date getFromTime()
	{
		return fromTime;
	}

	public void setFromTime(Date fromTime)
	{
		this.fromTime = fromTime;
	}

	// The toTime parameter together with the fromTime parameter is used to
	// create an interval and within this time frame is the matching criteria. The
	// toTime parameter is used to determine which vouchers was used in a specific
	// time frame.
	//
	// Optional

	public Date getToTime()
	{
		return toTime;
	}

	public void setToTime(Date toTime)
	{
		this.toTime = toTime;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////

	public boolean validate(IValidationContext context)
	{
		return Protocol.validateFromTime(context, false, fromTime) && //
				Protocol.validateToTime(context, false, toTime);
	}

}
