package hxc.utils.protocol.ucip;

import java.util.Date;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * AggregatedBalanceInformation
 * 
 * The struct aggregatedBalanceInformation contains aggregated information of product resources.
 */
@Air(PC = "PC:10803")
public class AggregatedBalanceInformation
{

	/*
	 * The dedicatedAccountID parameter contains the identity of the dedicated account in order to be able to distinguish between the various dedicated accounts in an array of dedicated accounts.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	public int dedicatedAccountID;

	/*
	 * aggregatedBalanceInformation??
	 */
	@Air(Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long totalBalance1;

	/*
	 * aggregatedBalanceInformation??
	 */
	@Air(Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long totalBalance2;

	/*
	 * aggregatedBalanceInformation??
	 */
	@Air(Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long totalActiveBalance1;

	/*
	 * aggregatedBalanceInformation??
	 */
	@Air(Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long totalActiveBalance2;

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

	/*
	 * The closestExpiryDateTime parameter contains the date and time for the product offer instance with the nearest expiry time at the moment.
	 */
	@Air(PC = "PC:10803", Range = "DateToday:DateMax,DateInfinite")
	public Date closestExpiryDateTime;

	/*
	 * These parameters states the balance of the sub dedicated account(s) with the closest expiry date, this include both active and in active sub dedicated accounts. This parameter is only valid for
	 * composite dedicated accounts. 1 indicates the balance of the first currency to be announced and 2 the balance of the second one. If the unit type is other than money the closestExpiryValue1
	 * contains the amount of the valid units and closestExpiryValue2 is omitted.
	 */
	@Air(Range = "-9223372036854775807:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public Long closestExpiryValue1;

	/*
	 * These parameters states the balance of the sub dedicated account(s) with the closest expiry date, this include both active and in active sub dedicated accounts. This parameter is only valid for
	 * composite dedicated accounts. 1 indicates the balance of the first currency to be announced and 2 the balance of the second one. If the unit type is other than money the closestExpiryValue1
	 * contains the amount of the valid units and closestExpiryValue2 is omitted.
	 */
	@Air(Range = "-9223372036854775807:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public Long closestExpiryValue2;

	/*
	 * The closestAccessibleDateTime parameter contains the date and time for the product offer instance with the nearest accessible time at the moment.
	 */
	@Air(PC = "PC:10803", Range = "DateToday:DateMax")
	public Date closestAccessibleDateTime;

	/*
	 * These parameters states the balance of the sub dedicated account(s) with the closest start date. This parameter is only valid for composite dedicated accounts. 1 indicates the balance of the
	 * first currency to be announced and 2 the balance of the second one. If the unit type is other than money the closestAccessibleValue1 contains the sum of the valid units and
	 * closestAccessibleValue2 is omitted.
	 */
	@Air(Range = "-9223372036854775807:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public Long closestAccessibleValue1;

	/*
	 * These parameters states the balance of the sub dedicated account(s) with the closest start date. This parameter is only valid for composite dedicated accounts. 1 indicates the balance of the
	 * first currency to be announced and 2 the balance of the second one. If the unit type is other than money the closestAccessibleValue1 contains the sum of the valid units and
	 * closestAccessibleValue2 is omitted.
	 */
	@Air(Range = "-9223372036854775807:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public Long closestAccessibleValue2;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public AggregatedBalanceInformation()
	{

	}

	public AggregatedBalanceInformation(AggregatedBalanceInformation aggregatedBalanceInformation)
	{
		this.dedicatedAccountID = aggregatedBalanceInformation.dedicatedAccountID;
		this.totalBalance1 = aggregatedBalanceInformation.totalBalance1;
		this.totalBalance2 = aggregatedBalanceInformation.totalBalance2;
		this.totalActiveBalance1 = aggregatedBalanceInformation.totalActiveBalance1;
		this.totalActiveBalance2 = aggregatedBalanceInformation.totalActiveBalance2;
		this.dedicatedAccountUnitType = aggregatedBalanceInformation.dedicatedAccountUnitType;
		this.closestExpiryDateTime = aggregatedBalanceInformation.closestExpiryDateTime;
		this.closestExpiryValue1 = aggregatedBalanceInformation.closestExpiryValue1;
		this.closestExpiryValue2 = aggregatedBalanceInformation.closestExpiryValue2;
		this.closestAccessibleDateTime = aggregatedBalanceInformation.closestAccessibleDateTime;
		this.closestAccessibleValue1 = aggregatedBalanceInformation.closestAccessibleValue1;
		this.closestAccessibleValue2 = aggregatedBalanceInformation.closestAccessibleValue2;
	}

}
