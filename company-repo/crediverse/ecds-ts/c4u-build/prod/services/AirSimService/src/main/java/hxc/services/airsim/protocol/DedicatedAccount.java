package hxc.services.airsim.protocol;

import java.util.Date;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

public class DedicatedAccount
{
	/*
	 * The dedicatedAccountID parameter contains the identity of the dedicated account in order to be able to distinguish between the various dedicated accounts in an array of dedicated accounts.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	protected int dedicatedAccountID;

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
	protected Long dedicatedAccountValue1;

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
	protected Long dedicatedAccountValue2;

	/*
	 * The expiryDate parameter contains the expiry date for a dedicated account.
	 */
	@Air(Range = "DateMin:DateMax,DateInfinite")
	protected Date expiryDate;

	/*
	 * The startDate parameter contains the date when a dedicated account, FaF entry or offer will be considered as active. The parameter may also be used to define start date for other entities
	 * depending on the context where it is used.
	 */
	@Air(Range = "DateMin:DateMax,DateBeginningOfTime")
	protected Date startDate;

	/*
	 * The pamServiceID parameter specifies the id of the periodic account management service.
	 */
	@Air(Range = "0:99")
	protected Integer pamServiceID;

	/*
	 * The offerID parameter contains the identity of an offer.
	 */
	@Air(Range = "1:2147483647")
	protected Integer offerID;

	/*
	 * The productID parameter contains the identity of a product.
	 */
	@Air(PC = "PC:09847", Range = "0:2147483647")
	protected Integer productID;

	/*
	 * If the dedicated account is used to hold money received from various promotions or bonuses, the dedicatedAccountRealMoneyFlag will be set to false. If the dedicated account is used to hold
	 * money that the subscriber have received through the purchase of, for instance, a voucher then the dedicatedAccountRealMoneyFlag will be set to true. Dedicated accounts which have the
	 * dedicatedAccountRealMoneyFlag set to true, will be summarized in the aggregatedBalance parameter. The dedicatedAccountRealMoneyFlag is only valid in dedicated accounts where the
	 * dedicatedAccountUnitTypeparameter has the value Money.
	 */
	@Air(PC = "PC:05225")
	protected Boolean dedicatedAccountRealMoneyFlag;

	/*
	 * The closestExpiryDate parameter contains the date when the next sub dedicated account(s) will expire, this include both active and inactive sub dedicated accounts. This parameter is only valid
	 * for composite dedicated accounts.
	 */
	@Air(Range = "DateToday:DateMax")
	protected Date closestExpiryDate;

	/*
	 * These parameters states the balance of the sub dedicated account(s) with the closest expiry date, this include both active and in active sub dedicated accounts. This parameter is only valid for
	 * composite dedicated accounts. 1 indicates the balance of the first currency to be announced and 2 the balance of the second one. If the unit type is other than money the closestExpiryValue1
	 * contains the amount of the valid units and closestExpiryValue2 is omitted.
	 */
	@Air(Range = "-9223372036854775807:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	protected Long closestExpiryValue1;

	/*
	 * These parameters states the balance of the sub dedicated account(s) with the closest expiry date, this include both active and in active sub dedicated accounts. This parameter is only valid for
	 * composite dedicated accounts. 1 indicates the balance of the first currency to be announced and 2 the balance of the second one. If the unit type is other than money the closestExpiryValue1
	 * contains the amount of the valid units and closestExpiryValue2 is omitted.
	 */
	@Air(Range = "-9223372036854775807:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	protected Long closestExpiryValue2;

	/*
	 * The closestAccessibleDate parameter contains the date when the next sub dedicated account(s) will be accessible. This parameter is only valid for composite dedicated accounts.
	 */
	@Air(Range = "DateToday:DateMax")
	protected Date closestAccessibleDate;

	/*
	 * These parameters states the balance of the sub dedicated account(s) with the closest start date. This parameter is only valid for composite dedicated accounts. 1 indicates the balance of the
	 * first currency to be announced and 2 the balance of the second one. If the unit type is other than money the closestAccessibleValue1 contains the sum of the valid units and
	 * closestAccessibleValue2 is omitted.
	 */
	@Air(Range = "-9223372036854775807:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	protected Long closestAccessibleValue1;

	/*
	 * These parameters states the balance of the sub dedicated account(s) with the closest start date. This parameter is only valid for composite dedicated accounts. 1 indicates the balance of the
	 * first currency to be announced and 2 the balance of the second one. If the unit type is other than money the closestAccessibleValue1 contains the sum of the valid units and
	 * closestAccessibleValue2 is omitted.
	 */
	@Air(Range = "-9223372036854775807:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	protected Long closestAccessibleValue2;

	protected SubDedicatedAccountInformation[] subDedicatedAccountInformation;

	/*
	 * The dedicatedAccountActiveValue1 and dedicatedAccountAc tiveValue2 parameters contains a dedicated account balance that can be consumed right now. This is not taking in consideration any
	 * ongoing chargeable events. The active value is only valid for composite dedicated account as they can have resources that becomes accessible later. dedicatedAccountActiveValue1 indicates that
	 * the balance is in the first currency to be announced and dedicatedAccountActiveValue2 indicated that the balance is in the second one. If the unit type is other than money the
	 * dedicatedAccountActiveValue1 c ontains the sum of the valid units and dedicatedAccountActiveValue2 is omitted.
	 */
	@Air(Range = "0:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	protected Long dedicatedAccountActiveValue1;

	/*
	 * The dedicatedAccountActiveValue1 and dedicatedAccountAc tiveValue2 parameters contains a dedicated account balance that can be consumed right now. This is not taking in consideration any
	 * ongoing chargeable events. The active value is only valid for composite dedicated account as they can have resources that becomes accessible later. dedicatedAccountActiveValue1 indicates that
	 * the balance is in the first currency to be announced and dedicatedAccountActiveValue2 indicated that the balance is in the second one. If the unit type is other than money the
	 * dedicatedAccountActiveValue1 c ontains the sum of the valid units and dedicatedAccountActiveValue2 is omitted.
	 */
	@Air(Range = "0:9223372036854775807", Format = "Unit")
	@XmlRpcAsString
	protected Long dedicatedAccountActiveValue2;

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
	protected Integer dedicatedAccountUnitType;

	/*
	 * This flag indicates that a dedicated account is a composite dedicated account. It is used to identify a composite dedicated account that does not have any sub dedicated accounts
	 */
	protected Boolean compositeDedicatedAccountFlag;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public int getDedicatedAccountID()
	{
		return dedicatedAccountID;
	}

	public void setDedicatedAccountID(int dedicatedAccountID)
	{
		this.dedicatedAccountID = dedicatedAccountID;
	}

	public Long getDedicatedAccountValue1()
	{
		return dedicatedAccountValue1;
	}

	public void setDedicatedAccountValue1(Long dedicatedAccountValue1)
	{
		this.dedicatedAccountValue1 = dedicatedAccountValue1;
	}

	public Long getDedicatedAccountValue2()
	{
		return dedicatedAccountValue2;
	}

	public void setDedicatedAccountValue2(Long dedicatedAccountValue2)
	{
		this.dedicatedAccountValue2 = dedicatedAccountValue2;
	}

	public Date getExpiryDate()
	{
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate)
	{
		this.expiryDate = expiryDate;
	}

	public Date getStartDate()
	{
		return startDate;
	}

	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}

	public Integer getPamServiceID()
	{
		return pamServiceID;
	}

	public void setPamServiceID(Integer pamServiceID)
	{
		this.pamServiceID = pamServiceID;
	}

	public Integer getOfferID()
	{
		return offerID;
	}

	public void setOfferID(Integer offerID)
	{
		this.offerID = offerID;
	}

	public Integer getProductID()
	{
		return productID;
	}

	public void setProductID(Integer productID)
	{
		this.productID = productID;
	}

	public Boolean getDedicatedAccountRealMoneyFlag()
	{
		return dedicatedAccountRealMoneyFlag;
	}

	public void setDedicatedAccountRealMoneyFlag(Boolean dedicatedAccountRealMoneyFlag)
	{
		this.dedicatedAccountRealMoneyFlag = dedicatedAccountRealMoneyFlag;
	}

	public Date getClosestExpiryDate()
	{
		return closestExpiryDate;
	}

	public void setClosestExpiryDate(Date closestExpiryDate)
	{
		this.closestExpiryDate = closestExpiryDate;
	}

	public Long getClosestExpiryValue1()
	{
		return closestExpiryValue1;
	}

	public void setClosestExpiryValue1(Long closestExpiryValue1)
	{
		this.closestExpiryValue1 = closestExpiryValue1;
	}

	public Long getClosestExpiryValue2()
	{
		return closestExpiryValue2;
	}

	public void setClosestExpiryValue2(Long closestExpiryValue2)
	{
		this.closestExpiryValue2 = closestExpiryValue2;
	}

	public Date getClosestAccessibleDate()
	{
		return closestAccessibleDate;
	}

	public void setClosestAccessibleDate(Date closestAccessibleDate)
	{
		this.closestAccessibleDate = closestAccessibleDate;
	}

	public Long getClosestAccessibleValue1()
	{
		return closestAccessibleValue1;
	}

	public void setClosestAccessibleValue1(Long closestAccessibleValue1)
	{
		this.closestAccessibleValue1 = closestAccessibleValue1;
	}

	public Long getClosestAccessibleValue2()
	{
		return closestAccessibleValue2;
	}

	public void setClosestAccessibleValue2(Long closestAccessibleValue2)
	{
		this.closestAccessibleValue2 = closestAccessibleValue2;
	}

	public Long getDedicatedAccountActiveValue1()
	{
		return dedicatedAccountActiveValue1;
	}

	public void setDedicatedAccountActiveValue1(Long dedicatedAccountActiveValue1)
	{
		this.dedicatedAccountActiveValue1 = dedicatedAccountActiveValue1;
	}

	public Long getDedicatedAccountActiveValue2()
	{
		return dedicatedAccountActiveValue2;
	}

	public void setDedicatedAccountActiveValue2(Long dedicatedAccountActiveValue2)
	{
		this.dedicatedAccountActiveValue2 = dedicatedAccountActiveValue2;
	}

	public Integer getDedicatedAccountUnitType()
	{
		return dedicatedAccountUnitType;
	}

	public void setDedicatedAccountUnitType(Integer dedicatedAccountUnitType)
	{
		this.dedicatedAccountUnitType = dedicatedAccountUnitType;
	}

	public Boolean getCompositeDedicatedAccountFlag()
	{
		return compositeDedicatedAccountFlag;
	}

	public void setCompositeDedicatedAccountFlag(Boolean compositeDedicatedAccountFlag)
	{
		this.compositeDedicatedAccountFlag = compositeDedicatedAccountFlag;
	}

	public SubDedicatedAccountInformation[] getSubDedicatedAccountInformation()
	{
		return subDedicatedAccountInformation;
	}

	public void setSubDedicatedAccountInformation(SubDedicatedAccountInformation[] subDedicatedAccountInformation)
	{
		this.subDedicatedAccountInformation = subDedicatedAccountInformation;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public DedicatedAccount()
	{

	}

	public DedicatedAccount(DedicatedAccount dedicatedAccount)
	{
		this.dedicatedAccountID = dedicatedAccount.dedicatedAccountID;
		this.dedicatedAccountValue1 = dedicatedAccount.dedicatedAccountValue1;
		this.dedicatedAccountValue2 = dedicatedAccount.dedicatedAccountValue2;
		this.expiryDate = dedicatedAccount.expiryDate;
		this.startDate = dedicatedAccount.startDate;
		this.pamServiceID = dedicatedAccount.pamServiceID;
		this.offerID = dedicatedAccount.offerID;
		this.productID = dedicatedAccount.productID;
		this.dedicatedAccountRealMoneyFlag = dedicatedAccount.dedicatedAccountRealMoneyFlag;
		this.closestExpiryDate = dedicatedAccount.closestExpiryDate;
		this.closestExpiryValue1 = dedicatedAccount.closestExpiryValue1;
		this.closestExpiryValue2 = dedicatedAccount.closestExpiryValue2;
		this.closestAccessibleDate = dedicatedAccount.closestAccessibleDate;
		this.closestAccessibleValue1 = dedicatedAccount.closestAccessibleValue1;
		this.closestAccessibleValue2 = dedicatedAccount.closestAccessibleValue2;
		this.dedicatedAccountActiveValue1 = dedicatedAccount.dedicatedAccountActiveValue1;
		this.dedicatedAccountActiveValue2 = dedicatedAccount.dedicatedAccountActiveValue2;
		this.dedicatedAccountUnitType = dedicatedAccount.dedicatedAccountUnitType;
		this.compositeDedicatedAccountFlag = dedicatedAccount.compositeDedicatedAccountFlag;
		this.subDedicatedAccountInformation = dedicatedAccount.subDedicatedAccountInformation;
	}

}
