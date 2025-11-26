package hxc.utils.protocol.ucip;

import hxc.connectors.air.Air;

/**
 * ProductSelection
 * 
 * The struct productSelection is used to define product identifiers to retrieve for a given offer. It is enclosed in a <struct> of its own. Structs are placed in an <array>. If no product ID is
 * specified in the request all installed products for the given offer is returned. If a single product shall be returned, the offerID for the offer is specified together with the product identity in
 * productID. Note: Explicit requests use offerID and productID, if the requested offer does not exist response code 165 will be returned. If the requested offer exists but the requested product do
 * not exists response code 247 will be returned.
 */
public class ProductSelection
{
	/*
	 * The offerID parameter contains the identity of an offer.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	public int offerID;

	/*
	 * The productID parameter contains the identity of a product.
	 */
	@Air(Range = "0:2147483647")
	public Integer productID;

}
