package hxc.utils.protocol.ucip;

import java.util.Date;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * SubDedicatedAccountRefillInformation
 * 
 * The subDedicatedAccountRefillInformation is enclosed in a <struct> of its own. Structs are placed in an <array>.
 */
public class SubDedicatedAccountRefillInformation
{
	/*
	 * The startDate parameter contains the date when a dedicated account, FaF entry or offer will be considered as active. The parameter may also be used to define start date for other entities
	 * depending on the context where it is used.
	 */
	@Air(Range = "DateMin:DateMax,DateBeginningOfTime")
	public Date startDate;

	/*
	 * The expiryDate parameter contains the expiry date for a dedicated account.
	 */
	@Air(Range = "DateMin:DateMax,DateInfinite")
	public Date expiryDate;

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

}
