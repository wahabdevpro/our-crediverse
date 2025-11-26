package hxc.utils.protocol.acip;

import java.util.Date;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * DedicatedAccountDeleteInformation
 * 
 * The struct dedicatedAccountDeleteInformation contains balances and dates for dedicated accounts. A dedicatedAccountDeleteInformation struct with no value or date parameters constitutes a composite
 * dedicated account with no assigned sub dedicated account. It contains the balances and dates before the dedicated account was deleted. It is enclosed in a <struct> of its own. Structs are placed in
 * an <array>. If the dedicatedAccountDeleteInformation contains a productID, which means that the DA is a private (instantiated) DA, the following parameters will be excluded from the structure:
 * expiryDate, startDate, pamServiceID, offerID, subDedicatedAccountInformation.
 */
public class DedicatedAccountDeleteInformation
{
	/*
	 * The dedicatedAccountID parameter contains the identity of the dedicated account in order to be able to distinguish between the various dedicated accounts in an array of dedicated accounts.
	 */
	@Air(Range = "1:2147483647")
	public Integer dedicatedAccountID;

	/*
	 * The productID parameter contains the identity of a product.
	 */
	@Air(PC = "PC:09847", CAP = "CAP:6")
	public Integer productID;

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
	 * If the dedicated account is used to hold money received from various promotions or bonuses, the dedicatedAccountRealMoneyFlag will be set to false. If the dedicated account is used to hold
	 * money that the subscriber have received through the purchase of, for instance, a voucher then the dedicatedAccountRealMoneyFlag will be set to true. Dedicated accounts which have the
	 * dedicatedAccountRealMoneyFlag set to true, will be summarized in the aggregatedBalance parameter. The dedicatedAccountRealMoneyFlag is only valid in dedicated accounts where the
	 * dedicatedAccountUnitTypeparameter has the value Money.
	 */
	@Air(PC = "PC:05225")
	public Boolean dedicatedAccountRealMoneyFlag;

	public SubDedicatedAccountInformation[] subDedicatedAccountInformation;

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
