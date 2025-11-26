package hxc.utils.protocol.ucip;

import java.util.Date;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * DedicatedAccountInformation
 * 
 * The struct dedicatedAccountInformation contains balances and dates for dedicated accounts. For get requests the subDedicatedAccountInfo rmation parameter will only be included if specifically
 * indicated with the requestSubDedicatedAccountDetailsFlag. A dedicatedAccountInformation struct with no value or date parameters constitutes a composite dedicated account with no assigned sub
 * dedicated account. It is enclosed in a <struct> of its own. Structs are placed in an <array>.
 */
public class DedicatedAccountInformation
{

	/*
	 * The dedicatedAccountID parameter contains the identity of the dedicated account in order to be able to distinguish between the various dedicated accounts in an array of dedicated accounts.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	public int dedicatedAccountID;

	/*
	 * The dedicatedAccountValue1 and dedicatedAccountValue2 para meters contain the total balance of the dedicated account, this includes all currently active and not yet active balances of the
	 * dedicated account if applicable. This is not taking in consideration any ongoing chargeable events. dedicatedAccountValue1 indicates that the balance is in the first currency to be announced
	 * and dedicatedAccountValue2 indicated that the balance is in the second one. If the unit type is not money the dedicatedAccountValue1 contains the sum of the valid units and
	 * dedicatedAccountValue2 is omitted. When dedicatedAccountUnitType is Money, the parameter can contain both an integer part and a decimal part. There is no decimal separator, the decimal part is
	 * given directly to the right of the integer part. The number of digits in the decimal part is configured in the currency configuration. The integer part range is: 0-9223372036854. The decimal
	 * part can consist of 0 to 6 digits, that is maximum value is 999999. If the maximum value of the integer part is reached the maximum decimal part is 775807.
	 */
	@Air(Range = "0:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public Long dedicatedAccountValue1;

	/*
	 * The dedicatedAccountValue1 and dedicatedAccountValue2 para meters contain the total balance of the dedicated account, this includes all currently active and not yet active balances of the
	 * dedicated account if applicable. This is not taking in consideration any ongoing chargeable events. dedicatedAccountValue1 indicates that the balance is in the first currency to be announced
	 * and dedicatedAccountValue2 indicated that the balance is in the second one. If the unit type is not money the dedicatedAccountValue1 contains the sum of the valid units and
	 * dedicatedAccountValue2 is omitted. When dedicatedAccountUnitType is Money, the parameter can contain both an integer part and a decimal part. There is no decimal separator, the decimal part is
	 * given directly to the right of the integer part. The number of digits in the decimal part is configured in the currency configuration. The integer part range is: 0-9223372036854. The decimal
	 * part can consist of 0 to 6 digits, that is maximum value is 999999. If the maximum value of the integer part is reached the maximum decimal part is 775807.
	 */
	@Air(Range = "0:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public Long dedicatedAccountValue2;

	/*
	 * The expiryDate parameter contains the expiry date for a dedicated account.
	 */
	@Air(Range = "DateMin:DateMax,DateInfinite")
	public Date expiryDate;

	/*
	 * The startDate parameter contains the date when a dedicated account, FaF entry or offer will be considered as active. The parameter may also be used to define start date for other entities
	 * depending on the context where it is used.
	 */
	@Air(Range = "DateMin:DateMax,DateBeginningOfTime")
	public Date startDate;

	/*
	 * The pamServiceID parameter specifies the id of the periodic account management service.
	 */
	@Air(Range = "0:99")
	public Integer pamServiceID;

	/*
	 * The offerID parameter contains the identity of an offer.
	 */
	@Air(Range = "1:2147483647")
	public Integer offerID;

	/*
	 * The productID parameter contains the identity of a product.
	 */
	@Air(PC = "PC:09847", Range = "0:2147483647")
	public Integer productID;

	/*
	 * If the dedicated account is used to hold money received from various promotions or bonuses, the dedicatedAccountRealMoneyFlag will be set to false. If the dedicated account is used to hold
	 * money that the subscriber have received through the purchase of, for instance, a voucher then the dedicatedAccountRealMoneyFlag will be set to true. Dedicated accounts which have the
	 * dedicatedAccountRealMoneyFlag set to true, will be summarized in the aggregatedBalance parameter. The dedicatedAccountRealMoneyFlag is only valid in dedicated accounts where the
	 * dedicatedAccountUnitTypeparameter has the value Money.
	 */
	@Air(PC = "PC:05225")
	public Boolean dedicatedAccountRealMoneyFlag;

	/*
	 * The closestExpiryDate parameter contains the date when the next sub dedicated account(s) will expire, this include both active and inactive sub dedicated accounts. This parameter is only valid
	 * for composite dedicated accounts.
	 */
	@Air(Range = "DateToday:DateMax")
	public Date closestExpiryDate;

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
	 * The closestAccessibleDate parameter contains the date when the next sub dedicated account(s) will be accessible. This parameter is only valid for composite dedicated accounts.
	 */
	@Air(Range = "DateToday:DateMax")
	public Date closestAccessibleDate;

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

	public SubDedicatedAccountInformation[] subDedicatedAccountInformation;

	/*
	 * The dedicatedAccountActiveValue1 and dedicatedAccountAc tiveValue2 parameters contains a dedicated account balance that can be consumed right now. This is not taking in consideration any
	 * ongoing chargeable events. The active value is only valid for composite dedicated account as they can have resources that becomes accessible later. dedicatedAccountActiveValue1 indicates that
	 * the balance is in the first currency to be announced and dedicatedAccountActiveValue2 indicated that the balance is in the second one. If the unit type is other than money the
	 * dedicatedAccountActiveValue1 c ontains the sum of the valid units and dedicatedAccountActiveValue2 is omitted.
	 */
	@Air(Range = "0:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public Long dedicatedAccountActiveValue1;

	/*
	 * The dedicatedAccountActiveValue1 and dedicatedAccountAc tiveValue2 parameters contains a dedicated account balance that can be consumed right now. This is not taking in consideration any
	 * ongoing chargeable events. The active value is only valid for composite dedicated account as they can have resources that becomes accessible later. dedicatedAccountActiveValue1 indicates that
	 * the balance is in the first currency to be announced and dedicatedAccountActiveValue2 indicated that the balance is in the second one. If the unit type is other than money the
	 * dedicatedAccountActiveValue1 c ontains the sum of the valid units and dedicatedAccountActiveValue2 is omitted.
	 */
	@Air(Range = "0:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public Long dedicatedAccountActiveValue2;

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
	 * This flag indicates that a dedicated account is a composite dedicated account. It is used to identify a composite dedicated account that does not have any sub dedicated accounts
	 */
	public Boolean compositeDedicatedAccountFlag;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public DedicatedAccountInformation()
	{

	}

	public DedicatedAccountInformation(DedicatedAccountInformation dedicatedAccountInformation)
	{
		this.dedicatedAccountID = dedicatedAccountInformation.dedicatedAccountID;
		this.dedicatedAccountValue1 = dedicatedAccountInformation.dedicatedAccountValue1;
		this.dedicatedAccountValue2 = dedicatedAccountInformation.dedicatedAccountValue2;
		this.expiryDate = dedicatedAccountInformation.expiryDate;
		this.startDate = dedicatedAccountInformation.startDate;
		this.pamServiceID = dedicatedAccountInformation.pamServiceID;
		this.offerID = dedicatedAccountInformation.offerID;
		this.productID = dedicatedAccountInformation.productID;
		this.dedicatedAccountRealMoneyFlag = dedicatedAccountInformation.dedicatedAccountRealMoneyFlag;
		this.closestExpiryDate = dedicatedAccountInformation.closestExpiryDate;
		this.closestExpiryValue1 = dedicatedAccountInformation.closestExpiryValue1;
		this.closestExpiryValue2 = dedicatedAccountInformation.closestExpiryValue2;
		this.closestAccessibleDate = dedicatedAccountInformation.closestAccessibleDate;
		this.closestAccessibleValue1 = dedicatedAccountInformation.closestAccessibleValue1;
		this.closestAccessibleValue2 = dedicatedAccountInformation.closestAccessibleValue2;

		if (dedicatedAccountInformation.subDedicatedAccountInformation != null)
		{
			this.subDedicatedAccountInformation = new SubDedicatedAccountInformation[dedicatedAccountInformation.subDedicatedAccountInformation.length];
			for (int index = 0; index < dedicatedAccountInformation.subDedicatedAccountInformation.length; index++)
			{
				this.subDedicatedAccountInformation[index] = new SubDedicatedAccountInformation(dedicatedAccountInformation.subDedicatedAccountInformation[index]);
			}
		}

		this.dedicatedAccountActiveValue1 = dedicatedAccountInformation.dedicatedAccountActiveValue1;
		this.dedicatedAccountActiveValue2 = dedicatedAccountInformation.dedicatedAccountActiveValue2;
		this.dedicatedAccountUnitType = dedicatedAccountInformation.dedicatedAccountUnitType;
		this.compositeDedicatedAccountFlag = dedicatedAccountInformation.compositeDedicatedAccountFlag;

	}

}
