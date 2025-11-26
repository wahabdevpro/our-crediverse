package hxc.utils.protocol.ucip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * GeneralUpdateRequest
 * 
 * The message GeneralUpdate is used by external system to adjust offers, account balances, accumulators, service class and more in a single transaction. On the main account it is possible to adjust
 * the balance and expiry dates both negative and positive (relative) direction and it is also possible to adjust the expiry dates with absolute dates. The dedicated accounts balances, start dates and
 * expiry dates could be adjusted in negative and positive direction or with absolute values. Note: * It is not possible to do both a relative and an absolute balance or date set for the same data
 * type (example: it is possible to either set an absolute OR a relative adjustment to the service fee expiry date). * It is only allowed to do unified actions to multiple accumulators. This means
 * that absolute and relative adjustments has to be ordered in separate requests. When using relative adjustment, negative or positive adjustments of accumulator values has to be ordered in separate
 * requests. It is not allowed to combine any of these types of actions in the same request. * The complete list of community numbers must be given when changing communities. For a community ID that
 * is not used, a "filler" community e.g. 9999999 needs to be given. Example: The subscriber has communities 3,10,5. Now 10 is removed and 5 changed to 7. The array below would look like:
 * communityInformationCurrent: 3,10,5; communityInformationNew: 3,9999999,7 (9999999 = "filler"). * It is not possible to do both a relative and an absolute balance or date set for the same data type
 * (example: it is possible to either set an absolute OR a relative adjustment to the service fee expiry date). * With this message Sub-DA:s can be created but not updated. * If pre-activation is
 * wanted then messageCapabilityFlag.accountA ctivationFlag should be included set to 1.
 */
@XmlRpcMethod(name = "GeneralUpdate")
public class GeneralUpdateRequest
{
	public GeneralUpdateRequestMember member;

	public GeneralUpdateRequest()
	{
		member = new GeneralUpdateRequestMember();
	}
}
