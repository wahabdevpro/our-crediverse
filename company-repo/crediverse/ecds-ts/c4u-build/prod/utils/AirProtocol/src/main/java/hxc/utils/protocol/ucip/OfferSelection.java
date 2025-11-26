package hxc.utils.protocol.ucip;

import hxc.connectors.air.Air;

/**
 * OfferSelection
 * 
 * The struct offerSelection is used to define offer identifiers to retrieve. It is enclosed in a <struct> of its own. Structs are placed in an <array>. If no offer IDs are specified in the request
 * all installed active offers are returned. The request contains first and last identities for a sequence of Offers. If a single offer shall be returned, offerIDFirst could be used alone, or the same
 * identity could be used for both offerIDFirst and offerIDLast. Overlapping sequences is allowed, the response will only contain one instance. Note: Explicit requests use offerIDFirst alone and if
 * the requested offer does not exist response code 165 will be returned. For an implicit request which uses a range of offers between offerIDFirst and offerIDLast the response code 165 will not be
 * returned if no offers are found in the range (even if the same identity is used for both offerIDFirst and offerIDLast). An explicit request overrides an implicit request. Example: 1, 2 and 3 are
 * offer IDs in the example. For the three first examples two structs are used in the array, the first an explicit request with offerIDFirst = 1 and the second an implicit request with offerIDFirst =
 * 2 and offerIDLast = 3. In the last example one struct is used with an implicit request with offerIDFirst = 1 and offerIDLast = 3. 1,2-3: 1 does not exist -> not ok, response code 165 and no offers
 * are returned 1,2-3: 3 does not exist -> ok, offer 1 and 2 are returned 1,2-3: 2-3 do not exist -> ok, offer 1 is returned 1-3: 1-3 do not exist -> ok, no offers are returned
 */
public class OfferSelection
{
	/*
	 * The offerIDFirst parameter contains the identity of the first offer in a sequence of offers, or the only offer identity if a single offer shall be obtained.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	public int offerIDFirst;

	/*
	 * The offerIDLast parameter contains the identity of the last offer in a sequence of offers, or the only offer identity if a single offer shall be obtained.
	 */
	@Air(Range = "1:2147483647")
	public Integer offerIDLast;

}
