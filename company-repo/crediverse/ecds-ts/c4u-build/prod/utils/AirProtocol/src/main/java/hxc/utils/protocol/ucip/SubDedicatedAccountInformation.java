package hxc.utils.protocol.ucip;

import java.util.Date;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * SubDedicatedAccountInformation
 * 
 * The struct subDedicatedAccountInformation contains balances and dates for sub dedicated accounts. It is enclosed in a <struct> of its own. Structs are placed in an <array>.
 */
public class SubDedicatedAccountInformation
{

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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public SubDedicatedAccountInformation()
	{

	}

	public SubDedicatedAccountInformation(SubDedicatedAccountInformation subDedicatedAccountInformation)
	{
		this.dedicatedAccountValue1 = subDedicatedAccountInformation.dedicatedAccountValue1;
		this.dedicatedAccountValue2 = subDedicatedAccountInformation.dedicatedAccountValue2;
		this.startDate = subDedicatedAccountInformation.startDate;
		this.expiryDate = subDedicatedAccountInformation.expiryDate;
	}

}
