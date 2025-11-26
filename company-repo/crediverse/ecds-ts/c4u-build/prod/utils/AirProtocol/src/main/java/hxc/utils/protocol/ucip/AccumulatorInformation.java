package hxc.utils.protocol.ucip;

import java.util.Date;

import hxc.connectors.air.Air;

/**
 * AccumulatorInformation
 * 
 * The accumulatorInformation is enclosed in a <struct> of its own. Structs are placed in an <array>
 */
public class AccumulatorInformation
{
	/*
	 * The accumulatorID parameter contains the accumulator identity.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	public int accumulatorID;

	/*
	 * The accumulatorValue parameter contains an accumulator value.
	 */
	@Air(Mandatory = true, Range = "-2147483648:2147483647")
	public int accumulatorValue;

	/*
	 * The accumulatorStartDate parameter indicates the date on which the accumulator was last reset.
	 */
	@Air(Range = "DateMin:DateToday")
	public Date accumulatorStartDate;

	/*
	 * The accumulatorEndDate parameter indicates the date on which the accumulator will be reset to the initial value again.
	 */
	@Air(Range = "DateToday:DateMax,DateInfinite")
	public Date accumulatorEndDate;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public int getAccumulatorID()
	{
		return accumulatorID;
	}

	public void setAccumulatorID(int accumulatorID)
	{
		this.accumulatorID = accumulatorID;
	}

	public int getAccumulatorValue()
	{
		return accumulatorValue;
	}

	public void setAccumulatorValue(int accumulatorValue)
	{
		this.accumulatorValue = accumulatorValue;
	}

	public Date getAccumulatorStartDate()
	{
		return accumulatorStartDate;
	}

	public void setAccumulatorStartDate(Date accumulatorStartDate)
	{
		this.accumulatorStartDate = accumulatorStartDate;
	}

	public Date getAccumulatorEndDate()
	{
		return accumulatorEndDate;
	}

	public void setAccumulatorEndDate(Date accumulatorEndDate)
	{
		this.accumulatorEndDate = accumulatorEndDate;
	}

}
