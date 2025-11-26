package hxc.services.airsim.protocol;

import java.util.Date;

import hxc.connectors.air.Air;

public class Accumulator
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	/*
	 * The accumulatorID parameter contains the accumulator identity.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	private int accumulatorID;

	/*
	 * The accumulatorValue parameter contains an accumulator value.
	 */
	@Air(Mandatory = true, Range = "-2147483648:2147483647")
	private int accumulatorValue;

	/*
	 * The accumulatorStartDate parameter indicates the date on which the accumulator was last reset.
	 */
	@Air(Range = "DateMin:DateToday")
	private Date accumulatorStartDate;

	/*
	 * The accumulatorEndDate parameter indicates the date on which the accumulator will be reset to the initial value again.
	 */
	@Air(Range = "DateToday:DateMax,DateInfinite")
	private Date accumulatorEndDate;

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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Accumulator()
	{
	}

	public Accumulator(Accumulator accumulator)
	{
		this.accumulatorID = accumulator.accumulatorID;
		this.accumulatorValue = accumulator.accumulatorValue;
		this.accumulatorStartDate = accumulator.accumulatorStartDate;
		this.accumulatorEndDate = accumulator.accumulatorEndDate;
	}

}
