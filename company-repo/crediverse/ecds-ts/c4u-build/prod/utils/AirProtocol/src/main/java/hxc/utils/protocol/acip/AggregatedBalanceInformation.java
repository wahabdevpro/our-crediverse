package hxc.utils.protocol.acip;

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
	 * The totalBalance1 and totalBalance2 parameters contain the aggregated balance. totalBalance1 indicates an aggregated balance in the first currency and totalBalance2 the aggregated balance in
	 * the second currency. If the unit type is other than Money the totalBalance1 contain the sum of the valid units and the totalBalance2 is omitted. When dedicatedAccountUnitType is of the unit
	 * type Money the parameter can contain both an integer part and a decimal part. There is no decimal separator, the decimal part is given directly to the right of the integer part. The number of
	 * digits in the decimal part is configured in the currency configuration. The integer part range is: 0 - 9 223 372 036 854. The decimal part can consist of 0 to 6 digits, which maximum value is
	 * 999999. If the maximum value of the integer part is reached, the maximum decimal part is 775807
	 */
	@Air(PC = "PC:10803", Range = "0:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public Long totalBalance1;

	/*
	 * The totalBalance1 and totalBalance2 parameters contain the aggregated balance. totalBalance1 indicates an aggregated balance in the first currency and totalBalance2 the aggregated balance in
	 * the second currency. If the unit type is other than Money the totalBalance1 contain the sum of the valid units and the totalBalance2 is omitted. When dedicatedAccountUnitType is of the unit
	 * type Money the parameter can contain both an integer part and a decimal part. There is no decimal separator, the decimal part is given directly to the right of the integer part. The number of
	 * digits in the decimal part is configured in the currency configuration. The integer part range is: 0 - 9 223 372 036 854. The decimal part can consist of 0 to 6 digits, which maximum value is
	 * 999999. If the maximum value of the integer part is reached, the maximum decimal part is 775807
	 */
	@Air(PC = "PC:10803", Range = "0:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public Long totalBalance2;

	/*
	 * The totalActiveBalance1 and totalActiveBalance2 parameters contain the aggregated balance that can be consumed right now. totalActiveBalance1 indicates an aggregated balance in the first
	 * currency and totalActiveBalance2 the aggregated balance in the second currency. If the unit type is other than Money the totalActiveBalance1 contain the sum of the valid units and the
	 * totalActiveBalance2 is omitted. When dedicatedAccountUnitType is of the unit type Money the parameter can contain both an integer part and a decimal part. There is no decimal separator, the
	 * decimal part is given directly to the right of the integer part. The number of digits in the decimal part is configured in the currency configuration. The integer part range is: 0 - 9 223 372
	 * 036 854. The decimal part can consist of 0 to 6 digits, which maximum value is 999999. If the maximum value of the integer part is reached, the maximum decimal part is 775807
	 */
	@Air(PC = "PC:10803", Range = "0:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public Long totalActiveBalance1;

	/*
	 * The totalActiveBalance1 and totalActiveBalance2 parameters contain the aggregated balance that can be consumed right now. totalActiveBalance1 indicates an aggregated balance in the first
	 * currency and totalActiveBalance2 the aggregated balance in the second currency. If the unit type is other than Money the totalActiveBalance1 contain the sum of the valid units and the
	 * totalActiveBalance2 is omitted. When dedicatedAccountUnitType is of the unit type Money the parameter can contain both an integer part and a decimal part. There is no decimal separator, the
	 * decimal part is given directly to the right of the integer part. The number of digits in the decimal part is configured in the currency configuration. The integer part range is: 0 - 9 223 372
	 * 036 854. The decimal part can consist of 0 to 6 digits, which maximum value is 999999. If the maximum value of the integer part is reached, the maximum decimal part is 775807
	 */
	@Air(PC = "PC:10803", Range = "0:9223372036854775807", Format = "Unit")
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

}
