package hxc.utils.protocol.acip;

import java.util.Date;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * SubDedicatedAccountUpdateInformation
 * 
 * The struct subDedicatedAccountUpdateInformation contains information for updating balances and expiry date for sub dedicated accounts. Clearing the value of a sub dedicated account, and thereby
 * deleting it, is done by sending the value zero on subDedicatedAccountValueAbsolute. It is enclosed in a <struct> of its own. Structs are placed in an <array>.
 */
public class SubDedicatedAccountUpdateInformation
{
	/*
	 * The dedicatedAccountID parameter contains the identity of the dedicated account in order to be able to distinguish between the various dedicated accounts in an array of dedicated accounts.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	public int dedicatedAccountID;

	@Air(Mandatory = true)
	public SubDedicatedAccountIdentifier subDedicatedAccountIdentifier;

	/*
	 * The adjustmentAmountRelative parameter contains the amount of the adjustment (positive or negative) to be applied to the account. It can be applied to both dedicated accounts and main account,
	 * but only unit type of money can be applied to main account. If the amount is to be applied to a dedicated account, then if the unit type is Money, this parameter can contain both an integer
	 * part and a decimal part. There is no decimal separator, the decimal part is given directly to the right of the integer part. The number of digits in the decimal part is configured in the
	 * currency configuration. The integer part range is: 0-9223372036854. The decimal part can consist of 0 to 6 digits, that is, maximum value is 999999. If the maximum value of the integer part is
	 * reached, the maximum decimal part is 775807.
	 */
	@Air(Range = "-999999999999:999999999999", Format = "Unit")
	@XmlRpcAsString
	public Long adjustmentAmountRelative;

	/*
	 * The subDedicatedAccountValueAbsolute parameter contains units to assign to a sub dedicated account. This is not taking in consideration any ongoing chargeable events.
	 */
	@Air(Range = "0:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public Long subDedicatedAccountValueAbsolute;

	/*
	 * The adjustmentDateRelative parameter is used to make a relative adjustment to the current expiry date. The adjustment can be positive or negative. It is expressed in number of days.
	 */
	@Air(Range = "-32767:-1,1:32767")
	public Integer adjustmentDateRelative;

	/*
	 * The expiryDate parameter contains the expiry date for a dedicated account.
	 */
	@Air(Range = "DateMin:DateMax,DateInfinite")
	public Date expiryDate;

	/*
	 * The expiryPamPeriodIndicator parameter indicates the Periodic Account Management period when the offer or DA becomes invalid. (PC:09847 start) When the offer has the capability of storing date
	 * and time the time stamp will be set to 00:00:00 or 23:59:59 depending on system configuration. (PC:09847 end)
	 */
	@Air(Range = "1:100,2147483647")
	public Integer expiryPamPeriodIndicator;

	/*
	 * The startDate parameter contains the date when a dedicated account, FaF entry or offer will be considered as active. The parameter may also be used to define start date for other entities
	 * depending on the context where it is used.
	 */
	@Air(Range = "DateMin:DateMax,DateBeginningOfTime")
	public Date startDate;

	/*
	 * The adjustmentStartDateRelative parameter is used to make a relative adjustment to the current start date. The adjustment can be positive or negative. It is expressed in number of days.
	 */
	@Air(Range = "-999:-1,1:999")
	public Integer adjustmentStartDateRelative;

	/*
	 * The startPamPeriodIndicator parameter indicates the Periodic Account Management period when the offer and DA becomes valid. (PC:09847 start) If the offer has the capability of date and time the
	 * time stamp will default to 00:00:00.(PC:09847 end) (PC:09847 start) Update of the PAM period indicator on an existing offer will keep the current time stamp unchanged. Time stamp can however be
	 * changed by including currentTimeOffset (see Section 7.53 on page 108) (PC:09847 end)
	 */
	@Air(Range = "0:99")
	public Integer startPamPeriodIndicator;

	/*
	 * The pamServiceID parameter specifies the id of the periodic account management service.
	 */
	@Air(Range = "0:99")
	public Integer pamServiceID;

	/*
	 * The expiryDateCurrent parameter contains the current expiry date for a dedicated account. The parameter may also be used to define expiry date for other entities depending on the context where
	 * it is used. Used for validation. No validation is performed if omitted.
	 */
	@Air(Range = "DateMin:DateMax,DateInfinite")
	public Date expiryDateCurrent;

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
