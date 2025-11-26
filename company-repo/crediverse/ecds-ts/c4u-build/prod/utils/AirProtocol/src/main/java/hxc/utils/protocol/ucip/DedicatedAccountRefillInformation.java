package hxc.utils.protocol.ucip;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * DedicatedAccountRefillInformation
 * 
 * The dedicatedAccountRefillInformation is enclosed in a <struct> of its own. Structs are placed in an <array>.
 */
public class DedicatedAccountRefillInformation
{
	/*
	 * The dedicatedAccountID parameter contains the identity of the dedicated account in order to be able to distinguish between the various dedicated accounts in an array of dedicated accounts.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	public int dedicatedAccountID;

	/*
	 * The refillAmount1 and refillAmount2 parameters contains refill value towards the main account. refillAmount1 indicates a refill amount in the first currency to be announced and refillAmount2 a
	 * refill amount in the second one. If the unit type is other than money the refillAmount1 contains the amount of the valid units and refillAmount2 is omitted.
	 */
	@Air(Range = "0:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public Long refillAmount1;

	/*
	 * The refillAmount1 and refillAmount2 parameters contains refill value towards the main account. refillAmount1 indicates a refill amount in the first currency to be announced and refillAmount2 a
	 * refill amount in the second one. If the unit type is other than money the refillAmount1 contains the amount of the valid units and refillAmount2 is omitted.
	 */
	@Air(Range = "0:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public Long refillAmount2;

	/*
	 * The expiryDateExtended parameter contains number of days the expiry date for a dedicated account has been extended as a result of the refillValueTotal and refillValuePromotion. The part is
	 * given in number of days.
	 */
	@Air(Range = "0:65535")
	public Integer expiryDateExtended;

	/*
	 * The clearedValue1 and clearedValue2 parameters contains units cleared for the subscriber's dedicated account when the dedicated account is removed. A dedicated account might for an example be
	 * removed at a service class change, or when the account is deleted. 1 indicates that the units cleared is in the first currency to be announced and 2 that the units cleared is in the second one.
	 * If the unit type is other than money the clearedValue1 contains the amount of the valid units and clearedValue2 is omitted.
	 */
	@Air(Range = "-9223372036854775807:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public Long clearedValue1;

	/*
	 * The clearedValue1 and clearedValue2 parameters contains units cleared for the subscriber's dedicated account when the dedicated account is removed. A dedicated account might for an example be
	 * removed at a service class change, or when the account is deleted. 1 indicates that the units cleared is in the first currency to be announced and 2 that the units cleared is in the second one.
	 * If the unit type is other than money the clearedValue1 contains the amount of the valid units and clearedValue2 is omitted.
	 */
	@Air(Range = "-9223372036854775807:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public Long clearedValue2;

	/*
	 * The offerID parameter contains the identity of an offer.
	 */
	@Air(Range = "1:2147483647")
	public Integer offerId;

	public SubDedicatedAccountRefillInformation[] subDedicatedAccountRefillInformation;

	/*-
	 * The dedicatedAccountUnitType parameter contains the unit of the
	 * dedicated account values and is mandatory if the function "multi unit" is active, in other case it is optional.
	 *
	 * Possible Values:
	 * ----------------
	 * 0:	The account contains time.
	 * 1:	The account contains money.
	 * 2:	The account contains total octets.
	 * 3:	The account contains input octets.
	 * 4:	The account contains output octets.
	 * 5:	The account contains service specific units.
	 * 6:	The account contains volume.
	 */
	public Integer dedicatedAccountUnitType;

}
