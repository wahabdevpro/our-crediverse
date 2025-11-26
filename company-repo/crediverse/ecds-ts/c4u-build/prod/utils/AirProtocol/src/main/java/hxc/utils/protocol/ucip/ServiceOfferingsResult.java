package hxc.utils.protocol.ucip;

import hxc.connectors.air.Air;

/**
 * ServiceOfferingsResult
 * 
 * The serviceOfferingsResult parameter contains a list of updated serviceOfferingIDs. The serviceOfferingsResult is a list of <struct> placed in an <array>.
 */
@Air(PC = "PC:06214")
public class ServiceOfferingsResult
{
	/*
	 * The serviceOfferingID parameter contains the identity of a current service offering defined on an account.
	 */
	@Air(Mandatory = true, Range = "1:31")
	public int serviceOfferingID;

}
