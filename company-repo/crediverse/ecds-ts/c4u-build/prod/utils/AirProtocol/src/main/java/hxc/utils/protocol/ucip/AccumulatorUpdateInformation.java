package hxc.utils.protocol.ucip;

import java.util.Date;

import hxc.connectors.air.Air;

/**
 * AccumulatorUpdateInformation
 * 
 * The AccumulatorUpdateInformation is enclosed in a <struct> of its own. Structs are placed in an <array>
 */
public class AccumulatorUpdateInformation
{
	/*
	 * The accumulatorID parameter contains the accumulator identity.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	public int accumulatorID;

	/*
	 * The accumulatorValueRelative parameter contains an accumulator value used for a relative update.
	 */
	@Air(Range = "-2147483648:2147483647")
	public Integer accumulatorValueRelative;

	/*
	 * The accumulatorValueAbsolute parameter contains an accumulator value used for an absolute update.
	 */
	@Air(Range = "-2147483648:2147483647")
	public Integer accumulatorValueAbsolute;

	/*
	 * The accumulatorStartDate parameter indicates the date on which the accumulator was last reset.
	 */
	@Air(Range = "DateMin:DateToday")
	public Date accumulatorStartDate;

}
