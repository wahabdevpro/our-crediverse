package hxc.utils.protocol.acip;

import java.util.Date;

import hxc.connectors.air.Air;

/**
 * AccumulatorIdentifier
 * 
 * The struct accumulatorIdentifier contains accumulator ID and end date for accumulators. It is enclosed in a <struct> of its own. Structs are placed in an <array>.
 */
public class AccumulatorIdentifier
{
	/*
	 * The accumulatorID parameter contains the accumulator identity.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	public int accumulatorID;

	/*
	 * The accumulatorEndDate parameter indicates the date on which the accumulator will be reset to the initial value again.
	 */
	@Air(Range = "DateToday:DateMax,DateInfinite")
	public Date accumulatorEndDate;

}
