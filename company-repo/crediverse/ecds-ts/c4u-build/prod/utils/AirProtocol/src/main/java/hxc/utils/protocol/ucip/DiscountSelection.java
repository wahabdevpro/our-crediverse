package hxc.utils.protocol.ucip;

import hxc.connectors.air.Air;

/**
 * DiscountSelection
 * 
 * The discountSelection parameter contains the identifiers for the services the discounts are requested for. It is enclosed in a struct of its own. Structs are placed in an array. If no discountIDs
 * are requested in the request message, information about all the discount IDs will be returned in response.
 */
public class DiscountSelection
{
	/*
	 * The discountID contains the identifier of the service to be returned in the response.
	 */
	@Air(CAP = "CAP:14", Range = "1:65535")
	public Integer discountID;

}
