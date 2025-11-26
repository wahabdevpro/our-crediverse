package hxc.utils.protocol.ucip;

import hxc.connectors.air.Air;

/**
 * UsageAccumulatorInformation
 * 
 * The usageAccumulatorInformation is enclosed in a <struct> of its own. Structs are placed in an <array>.
 */
public class UsageAccumulatorInformation
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

}
