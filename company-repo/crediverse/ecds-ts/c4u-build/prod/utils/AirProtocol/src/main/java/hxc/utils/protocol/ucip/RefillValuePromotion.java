package hxc.utils.protocol.ucip;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * RefillValuePromotion
 * 
 * The refillValueTotal and refillValuePromotion are enclosed in a <struct> of their own. It contains the resulting values that will be applied to the account, both for the added amounts and dates.
 * The refill is divided on main and dedicated accounts. For refillValueTotal the refill values are the total values, including market segmentation and promotions. For refillValuePromotion the refill
 * values are only the resulting promotion values that will be applied to the account (these are included in the "Total").
 */
public class RefillValuePromotion
{
	/*
	 * The refillAmount1 and refillAmount2 parameters contains refill value towards the main account. refillAmount1 indicates a refill amount in the first currency to be announced and refillAmount2 a
	 * refill amount in the second one. If the unit type is other than money the refillAmount1 contains the amount of the valid units and refillAmount2 is omitted.
	 */
	@Air(Mandatory = true, Range = "0:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public long refillAmount1;

	/*
	 * The refillAmount1 and refillAmount2 parameters contains refill value towards the main account. refillAmount1 indicates a refill amount in the first currency to be announced and refillAmount2 a
	 * refill amount in the second one. If the unit type is other than money the refillAmount1 contains the amount of the valid units and refillAmount2 is omitted.
	 */
	@Air(Range = "0:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public Long refillAmount2;

	/*
	 * The supervisionDaysExt parameter contains number of days the supervision period has been extended as a result of the refill. The part is given in number of days.
	 */
	@Air(Range = "0:2147483647")
	public Integer supervisionDaysExtended;

	/*
	 * The serviceFeeDaysExtended parameter gives the number of days the service fee period has been extended as a result of the refill. The part is given in number of days.
	 */
	@Air(Range = "0:2147483647")
	public Integer serviceFeeDaysExtended;

	public DedicatedAccountRefillInformation[] dedicatedAccountRefillInformation;

	public UsageAccumulatorInformation[] usageAccumulatorInformation;

}
