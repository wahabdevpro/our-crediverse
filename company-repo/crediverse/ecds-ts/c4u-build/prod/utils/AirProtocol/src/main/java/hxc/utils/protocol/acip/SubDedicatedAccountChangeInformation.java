package hxc.utils.protocol.acip;

import java.util.Date;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * SubDedicatedAccountChangeInformation
 * 
 * The struct subDedicatedAccountChangeInformation contains information about the changes made to a sub dedicated account. It is enclosed in a <struct> of its own. Structs are placed in an <array>.
 */
public class SubDedicatedAccountChangeInformation
{
	/*
	 * The changedAmount1 and changedAmount2 parameters define changed values on a main or dedicated account. changedAmount1 indicates a changed amount in the first currency to be announced and
	 * changedAmount2 a changed amount in the second currency. If the unit type is other than money the changedAmount1 contains the amount of the valid units and changedAmount2 is omitted.
	 */
	@Air(Range = "-9223372036854775807:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public Long changedAmount1;

	/*
	 * The changedAmount1 and changedAmount2 parameters define changed values on a main or dedicated account. changedAmount1 indicates a changed amount in the first currency to be announced and
	 * changedAmount2 a changed amount in the second currency. If the unit type is other than money the changedAmount1 contains the amount of the valid units and changedAmount2 is omitted.
	 */
	@Air(Range = "-9223372036854775807:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	public Long changedAmount2;

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
	 * The changedExpiryDate parameter contains the number of days the expiry date for a dedicated account has been changed as a result of the operation. The value is given in number of days.
	 */
	@Air(Range = "-65535:65535")
	public Integer changedExpiryDate;

	/*
	 * The newExpiryDate parameter contains the new expiry date for a dedicated account.
	 */
	@Air(Range = "DateMin:DateMax,DateInfinite")
	public Date newExpiryDate;

	/*
	 * The clearedExpiryDate parameter contains the previous expiry date for a cleared dedicated account.
	 */
	@Air(Range = "DateMin:DateMax,DateInfinite")
	public Date clearedExpiryDate;

	/*
	 * The changeStartDate parameter contains the number of days the start date for a dedicated account has been changed as a result of the operation. The value is given in number of days.
	 */
	@Air(Range = "-65535:65535")
	public Integer changedStartDate;

	/*
	 * The newStartDate parameter contains the new start date for a dedicated account.
	 */
	@Air(Range = "DateMin:DateMax,DateBeginningOfTime")
	public Date newStartDate;

	/*
	 * The clearedStartDate parameter contains the previous start date for a cleared dedicated account.
	 */
	@Air(Range = "DateMin:DateMax,DateBeginningOfTime")
	public Date clearedStartDate;

}
