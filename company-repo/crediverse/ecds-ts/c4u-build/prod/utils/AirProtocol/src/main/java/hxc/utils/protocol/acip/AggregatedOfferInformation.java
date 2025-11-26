package hxc.utils.protocol.acip;

import hxc.connectors.air.Air;

/**
 * AggregatedOfferInformation
 * 
 */
@Air(PC = "PC:10803")
public class AggregatedOfferInformation
{
	@Air(PC = "PC:10803", CAP = "CAP:15")
	public AggregatedBalanceInformation aggregatedBalanceInformation;

}
