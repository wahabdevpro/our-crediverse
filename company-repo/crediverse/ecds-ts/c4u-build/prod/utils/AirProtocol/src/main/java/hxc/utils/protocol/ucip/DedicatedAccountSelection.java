package hxc.utils.protocol.ucip;

import hxc.connectors.air.Air;

/**
 * DedicatedAccountSelection
 * 
 * The dedicatedAccountSelection parameter is used to select which dedicated accounts that will be returned. If no dedicated account IDs are specified in the request all installed dedicated accounts
 * are returned. The request contains first and last identities for a sequence of dedicated accounts. If a single dedicated account shall be returned, dedicatedAccountIDFirst could be used alone, or
 * the same identity could be used for both dedicatedAccountIDFirst and dedicatedAccountIDLast. Overlapping sequences is allowed, the response will only contain one instance per dedicated account.
 * Structs are placed in an <array> with maximum 255 entries. Note: Explicit requests use dedicatedAccountIDFirst alone and if the requested dedicated account does not exist response code 139 will be
 * returned. For an implicit request which uses a range of dedicated accounts between dedicatedAccountIDFirst and dedicatedAccountIDLast the response code 139 will not be returned if no dedicated
 * accounts are found in the range (even if the same identity is used for both dedicatedAccountIDFirst and dedicatedAccountIDLast). An explicit request overrides an implicit request. Example: 1, 2 and
 * 3 are dedicated account IDs in the example. For the three first examples two structs are used in the array, the first an explicit request with dedicatedAccountIDFirst = 1 and the second an implicit
 * request with dedicatedAccountIDFirst = 2 and dedicatedAccountIDLast = 3. In the last example one struct is used with an implicit request with dedicatedAccountIDFirst = 1 and dedicatedAccountIDLast
 * = 3. 1,2-3: 1 does not exist -> not ok, response code 139 and no dedicated accounts are returned 1,2-3: 3 does not exist -> ok, dedicated account 1 and 2 are returned 1,2-3: 2-3 do not exist -> ok,
 * dedicated account 1 is returned 1-3: 1-3 do not exist -> ok, no dedicated accounts are returned
 */
public class DedicatedAccountSelection
{
	/*
	 * The dedicatedAccountIDFirst parameter contains the identity of the first dedicated account in a sequence of dedicated accounts, or the only dedicated account identity if a single dedicated
	 * account shall be obtained.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	public int dedicatedAccountIDFirst;

	/*
	 * The dedicatedAccountIDLast parameter contains the identity of the last dedicated account in a sequence of dedicated accounts.
	 */
	@Air(Range = "1:2147483647")
	public Integer dedicatedAccountIDLast;

}
