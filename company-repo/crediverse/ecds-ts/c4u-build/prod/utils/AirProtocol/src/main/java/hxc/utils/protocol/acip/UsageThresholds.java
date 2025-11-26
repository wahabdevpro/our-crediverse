package hxc.utils.protocol.acip;

import hxc.connectors.air.Air;

/**
 * UsageThresholds
 * 
 * The usageThresholds is used to specify which usage thresholds to perform an operation on. It is enclosed in a <struct> of its own. Structs are placed in an <array>.
 */
public class UsageThresholds
{
	/*
	 * The usageThresholdID parameter identifies the ID of a usage threshold.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	public int usageThresholdID;

	/*
	 * The associatedPartyID parameter contains the subscriber identity of the consumer or provider. The default format of the parameter is the same numbering format as used by the account database,
	 * this also includes support of leading zeroes.
	 */
	@Air(Length = "1:28", Format = "Numeric")
	public String associatedPartyID;

}
