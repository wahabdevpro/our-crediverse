package hxc.utils.protocol.ucip;

import hxc.connectors.air.Air;

/**
 * AggregatedOfferInformation
 * 
 * The aggregatedOfferInformation contains aggregated information of product resources connected to offer instances.
 */
@Air(PC = "PC:10803")
public class AggregatedOfferInformation
{

	@Air(PC = "PC:10803", CAP = "CAP:15")
	public AggregatedBalanceInformation aggregatedBalanceInformation;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public AggregatedOfferInformation()
	{

	}

	public AggregatedOfferInformation(AggregatedOfferInformation aggregatedOfferInformation)
	{
		this.aggregatedBalanceInformation = new AggregatedBalanceInformation(aggregatedBalanceInformation);
	}

}
