package hxc.utils.protocol.ucip;

import hxc.connectors.air.Air;
import hxc.connectors.air.IResponseHeader2;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * RefillResponseMember
 * 
 */
public class RefillResponseMember implements IResponseHeader2
{
	/*-
	 * The responseCode parameter is sent back after a message has been
	 * processed and indicates success or failure of the message.
	 *
	 * Possible Values:
	 * ----------------
	 * 0:	Successful
	 * 1:	Ok but supervision period exceeded
	 * 2:	Ok but service fee period exceeded
	 * 100:	 Other Error
	 * 101:	Not used
	 * 102:	Subscriber not found
	 * 103:	Account barred from refill
	 * 104:	Temporary blocked
	 * 105:	Dedicated account not allowed
	 * 106:	Dedicated account negative
	 * 107:	Voucher status used by same
	 * 108:	Voucher status used by different
	 * 109:	Voucher status unavailable
	 * 110:	Voucher status expired
	 * 111:	Voucher status stolen or missing
	 * 112:	Voucher status damaged
	 * 113:	Voucher status pending
	 * 114:	Voucher type not accepted
	 * 115:	Refill not accepted
	 * 117:	Service class change not allowed
	 * 119:	Invalid voucher activation code
	 * 120:	Invalid refill profile
	 * 121:	Supervision period too long
	 * 122:	Service fee period too long
	 * 123:	Max credit limit exceeded
	 * 124:	Below minimum balance
	 * 126:	Account not active
	 * 127:	Accumulator not available
	 * 128:	Invalid PIN code
	 * 129:	Faf number does not exist
	 * 130:	Faf number not allowed
	 * 133:	Service class list empty
	 * 134:	Accumulator overflow
	 * 135:	Accumulator underflow
	 * 136:	Date adjustment error
	 * 137:	Get balance and date not allowed
	 * 138:	No PIN code registered
	 * 139:	Dedicated account not defined
	 * 140:	Invalid old Service Class
	 * 141:	Invalid language
	 * 142:	Subscriber already installed
	 * 143:	Invalid master subscriber
	 * 144:	Subscriber already activated
	 * 145:	Already linked subordinate
	 * 146:	Already linked as master
	 * 147:	Invalid old community list
	 * 148:	Invalid new community list
	 * 149:	Invalid promotion plan end date
	 * 150:	Invalid promotion plan. The promotion plan allocation was invalid.
	 * 151:	Promotion plan not found
	 * 152:	Deblocking of expired account
	 * 153:	Dedicated account max credit limit exceeded
	 * 154:	Invalid old SC date
	 * 155:	Invalid new service class
	 * 156:	Delete subscriber failed due to for example references to consumers exist at the provider.
	 * 157:	Invalid account home region
	 * 158:	Not used
	 * 159:	Charged FaF not active for service class
	 * 160:	Operation not allowed from current location
	 * 161:	Failed to get location information
	 * 163:	Invalid dedicated account period
	 * 164:	Invalid dedicated account start date
	 * 165:	Offer not found
	 * 166:	Not used
	 * 167:	Invalid unit type
	 * 168:	Refill denied, First IVR call not made
	 * 177:	Refill denied, Account not active
	 * 178:	Refill denied, Service fee period expired
	 * 179:	Refill denied, Supervision period expired
	 * 190:	The PAM service id provided in the request already exist
	 * 191:	The PAM service id provided in the request was out of range, or did not exist
	 * 192:	The old PAM class id provided in the request was incorrect or did not match the existing PAM class id for the account
	 * 193:	The PAM class id or new PAM class id provided in the request was incorrect
	 * 194:	The old schedule id provided in the request was incorrect or did not match the existing schedule id for the account
	 * 195:	The schedule id or new schedule id provided in the request was incorrect
	 * 196:	Invalid deferred to date
	 * 197:	Periodic account management evaluation failed
	 * 198:	Too many PAM services given in the sequence or the number of services on the account would be exceeded
	 * 199:	The PAM period, provided or calculated, could not be found in the schedule
	 * 200:	The PAM class id or new PAM class id provided in the request does not exist
	 * 201:	The schedule id or new schedule id provided in the request did not exist or no valid period found
	 * 202:	Invalid PAM indicator
	 * 203:	Subscriber installed but marked for deletion
	 * 204:	Inconsistency between given current value and Account Database state
	 * 205:	Max number of FaF indicators exceeded
	 * 206:	FaF indicator already exists
	 * 207:	Invalid accumulator end date
	 * 208:	Invalid accumulator service class
	 * 209:	Invalid dedicated account expiry date
	 * 210:	Invalid dedicated account service class
	 * 211:	Delete dedicated account failed
	 * 212:	Crop of composite dedicated account not allowed
	 * 213:	Sub dedicated account not defined
	 * 214:	One or several of the provided offers are not defined or there is a mismatch between provided offer type and the offer type definition. Also used in case the offer type is not supported for the requested update.
	 * 215:	Too many offers of the type Multi User Identification given in the sequence or the Multi User Identification offer is already activated for the subscriber.
	 * 216:	Usage threshold not found in definition
	 * 217:	Usage counter not found in definition
	 * 218:	The usage threshold does not exist on the account
	 * 219:	Usage counter value out of bounds
	 * 220:	The supplied value type does not match the definition
	 * 221:	No subordinate subscribers connected to the account
	 * 222:	Dedicated account can not be deleted because of it is in use
	 * 223:	Service failed because new offer date provided in the request was incorrect.(PC:08204)
	 * 224:	The old offer date provided in the request did not match the current date.(PC:08204)
	 * 225:	The offer start date can not be changed because the offer is already active.(PC:08204)
	 * 226:	Invalid PAM Period Relative Dates Start PAM Period Indicator
	 * 227:	Invalid PAM Period Relative Dates Expiry PAM Period Indicator
	 * 230:	Not allowed to convert to other type of lifetime(1)
	 * 232:	Not allowed to delete PAM service ID in use. The PAM Service ID is used by an Offer, DA, or sub-DA's Relative Dates.
	 * 233:	Invalid PAM Service Priority
	 * 234:	Old PAM service priority provided in the request was incorrect or did not match the existing PAM service priority for the account.
	 * 235:	Not allowed to connect the PAM Class to the PAM Service. The account already has a bill cycle service. Only one bill cycle per account is allowed.
	 * 236:	PAM Service Priority is already used for some other PAM service.
	 * 237:	Not allowed to add a Provider ID to another offer type than provider account offer.
	 * 238:	Not allowed to create a provider account offer without providing a Provider ID.
	 * 239:	The request failed because the given time restriction does not exist.
	 * 240:	The request failed because the end time was before the start time.
	 * 241:	The timezone could not be found in the timezone mapping table.
	 * 242:	Discount not defined
	 * 243:	Missing associated party ID for provider owned personal usage counter.
	 * 244:	Associated party ID not allowed for provider owned common usage counter.
	 * 245:	Provider owned common usage counter can not have personal usage threshold.
	 * 246:	The common usage threshold does not exist on the account.
	 * 247:	Product not found
	 * 248:	Shared account offer is not allowed on a subordinate subscriber.
	 * 249:	The product id specified was invalid, or a product id was expected but not supplied.
	 * 250:	Communication ID change failed. Insufficient funds.
	 * 251:	Communication ID change failed. AIA/MSISDN mismatch.
	 * 252:	Communication ID change failed. Invalid combination of values.
	 * 253:	Communication ID change failed. Previous MSISDN Change pending.
	 * 254:	Communication ID change failed. New MSISDN not available.
	 * 255:	Operation not allowed on subordinate subscriber
	 * 256:	Attribute name does not exist
	 * 257:	Operation not allowed since End of Provisioning is set
	 * 258:	Attribute value does not exist
	 * 259:	Attribute value already exists
	 * 260:	Capability not available
	 * 261:	Invalid Capability combination
	 * 999:	Other Error No Retry
	 */
	@Air(Mandatory = true)
	public int responseCode;

	/*
	 * The originTransactionID parameter reference to a single operation, generated by the system it was initiated from. Note: Each operation must have a unique value The value in the
	 * originTransactionID parameter must be unique per operation and can be a sequence number. An operation in this case is for example a refill.
	 */
	@Air(Range = "0:99999999999999999999", Format = "Numeric")
	public String originTransactionID;

	/*
	 * The originOperatorID parameter is the identity of the system user or the session from where the operation was initiated. It might be used for security management or logging purposes for an
	 * example.
	 */
	@Air(Length = "1:255", Format = "Alphanumeric")
	public String originOperatorID;

	/*
	 * The masterAccountNumber parameter contains the subscriber identity of the master subscriber in a multi user account. The format of the parameter is the same numbering format as used by the
	 * account database, this also includes
	 */
	@Air(Length = "1:28", Format = "Numeric")
	public String masterAccountNumber;

	/*-
	 * The languageIDCurrent parameter contains the subscriber's preferred
	 * language.
	 *
	 * Possible Values:
	 * ----------------
	 * 1:	Operator specific language 1
	 * 2:	Operator specific language 2
	 * 3:	Operator specific language 3
	 * 4:	Operator specific language 4
	 */
	public Integer languageIDCurrent;

	/*
	 * The promotionAnnouncementCode parameter identifies the promotional code for the announcement to be played. The message in question is applicable for refill promotions.
	 */
	@Air(Range = "0:99")
	public Integer promotionAnnouncementCode;

	/*
	 * The voucherAgent parameter contains the identity of the dealer who received the vouchers from the service provider (as defined when creating the vouchers).
	 */
	@Air(Length = "1:8", Format = "Alphanumeric")
	public String voucherAgent;

	/*
	 * The voucherSerialNumber parameter contains the vouchers serial number.
	 */
	@Air(Length = "8:20", Format = "Alphanumeric")
	public String voucherSerialNumber;

	/*
	 * The voucherGroup parameter contains the unique identity of the card group that is associated to the voucher.
	 */
	@Air(Length = "1:4", Format = "Alphanumeric")
	public String voucherGroup;

	/*
	 * The transactionCurrency parameter contains an ID to point out what currency is used for the transaction. A transaction parameter includes data regarding a requested change. transactionCurrency
	 * is mandatory if the account adjustment affects a dedicated account or an usageCounter with a monetary value, thus is of type Money. transactionCurrencyis also mandatory when a
	 * UpdateAccountDetails or InstallSubscriber request includes the parameter accountPrepaidEmptyLimit.
	 */
	@Air(Format = "Currency")
	public String transactionCurrency;

	/*
	 * The transactionAmount parameter specifies the nominal value of the refill. A transaction parameter includes data regarding a requested change.
	 */
	@Air(Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long transactionAmount;

	/*
	 * The transactionAmountConverted parameter is the 'Transaction Amount Refill' that has been converted into the currency of the account. No result of market segmentation data, division table data
	 * or refill promotions have been added to the parameter value.
	 */
	@Air(Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long transactionAmountConverted;

	/*
	 * The currency1 and currency2 parameters contains the currencies to be presented to the end user. currency1 indicates the first currency to be announced and currency2 the second one.
	 */
	@Air(Format = "Currency")
	public String currency1;

	/*
	 * The currency1 and currency2 parameters contains the currencies to be presented to the end user. currency1 indicates the first currency to be announced and currency2 the second one.
	 */
	@Air(Format = "Currency")
	public String currency2;

	public RefillInformation refillInformation;

	public AccountBeforeRefill accountBeforeRefill;

	public AccountAfterRefill accountAfterRefill;

	/*
	 * The refillFraudCount parameter contains number of fraudulent refill attempts left, before the account becomes barred from refill.
	 */
	@Air(Range = "0:255")
	public Integer refillFraudCount;

	/*
	 * This selectedOption parameter contains the value of the selected option. The values are configurable by the operator. The value is primarily used for Option Refill.
	 */
	@Air(Range = "1:999")
	public Integer selectedOption;

	/*
	 * The segmentationID parameter contains a value that was used for the branching in the refill trees.
	 */
	@Air(Length = "1:4", Format = "Alphanumeric")
	public String segmentationID;

	/*-
	 * The refillType parameter specifies the kind of refill, it is part of both the refill
	 * request and refill response.
	 * The refillType parameter in a request specifies the wanted type of refill; this
	 * parameter can be validated or ignored depending of the configuration.
	 *
	 * Possible Values:
	 * ----------------
	 * 0:	Undefined
	 * 1:	Value Refill
	 * 2:	Account Refill
	 * 3:	Premium Refill
	 * 100-999:	Operator defined
	 */
	public Integer refillType;

	/*
	 * The negotiatedCapabilities parameter is used to indicate the negotiated capabilities between the client and server node. The capabilities are presented as a series of elements, where each
	 * element contains an integer value between 0 and 2147483647. The value 0 indicates that none of the capabilities in the element are active, and the value 2147483647 indicates that all of the
	 * capabilities in the element are active. See Section 9 on page 213 for the supported capabilities.
	 */
	@Air(Range = "0:2147483647")
	public Integer[] negotiatedCapabilities;

	/*
	 * The availableServerCapablities parameter is used to indicate the available capabilities at the server node. The capabilities are presented as a series of elements, where each element contains
	 * an integer value between 0 and 2147483647. The value 0 indicates that none of the capabilities in the element are active, and the value 2147483647 indicates that all of the capabilities in the
	 * element are active. If only one element is present and is set to 0, no functions after release AIR-IP 5.0 are active, only legacy functionality can be used.
	 */
	@Air(Range = "0:2147483647")
	public Integer[] availableServerCapabilities;

	/*
	 * The accountPrepaidEmptyLimit1 and accountPrepaidEmptyLimit2 p arameters contain the lowest allowed balance on an account. 1 indicates an account balance in the first currency and 2 an account
	 * balance in the second one.
	 */
	@Air(CAP = "CAP:13", Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long accountPrepaidEmptyLimit1;

	/*
	 * The accountPrepaidEmptyLimit1 and accountPrepaidEmptyLimit2 p arameters contain the lowest allowed balance on an account. 1 indicates an account balance in the first currency and 2 an account
	 * balance in the second one.
	 */
	@Air(CAP = "CAP:13", Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long accountPrepaidEmptyLimit2;

	@Air(PC = "PC:10804", CAP = "CAP:7")
	public TreeDefinedField treeDefinedField;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@Override
	public int getResponseCode()
	{
		return responseCode;
	}

	@Override
	public void setResponseCode(int responseCode)
	{
		this.responseCode = responseCode;
	}

	@Override
	public String getOriginTransactionID()
	{
		return originTransactionID;
	}

	@Override
	public void setOriginTransactionID(String originTransactionID)
	{
		this.originTransactionID = originTransactionID;
	}

	@Override
	public String getOriginOperatorID()
	{
		return originOperatorID;
	}

	@Override
	public void setOriginOperatorID(String originOperatorID)
	{
		this.originOperatorID = originOperatorID;
	}

	public String getMasterAccountNumber()
	{
		return masterAccountNumber;
	}

	public void setMasterAccountNumber(String masterAccountNumber)
	{
		this.masterAccountNumber = masterAccountNumber;
	}

	public Integer getLanguageIDCurrent()
	{
		return languageIDCurrent;
	}

	public void setLanguageIDCurrent(Integer languageIDCurrent)
	{
		this.languageIDCurrent = languageIDCurrent;
	}

	public Integer getPromotionAnnouncementCode()
	{
		return promotionAnnouncementCode;
	}

	public void setPromotionAnnouncementCode(Integer promotionAnnouncementCode)
	{
		this.promotionAnnouncementCode = promotionAnnouncementCode;
	}

	public String getVoucherAgent()
	{
		return voucherAgent;
	}

	public void setVoucherAgent(String voucherAgent)
	{
		this.voucherAgent = voucherAgent;
	}

	public String getVoucherSerialNumber()
	{
		return voucherSerialNumber;
	}

	public void setVoucherSerialNumber(String voucherSerialNumber)
	{
		this.voucherSerialNumber = voucherSerialNumber;
	}

	public String getVoucherGroup()
	{
		return voucherGroup;
	}

	public void setVoucherGroup(String voucherGroup)
	{
		this.voucherGroup = voucherGroup;
	}

	public String getTransactionCurrency()
	{
		return transactionCurrency;
	}

	public void setTransactionCurrency(String transactionCurrency)
	{
		this.transactionCurrency = transactionCurrency;
	}

	public Long getTransactionAmount()
	{
		return transactionAmount;
	}

	public void setTransactionAmount(Long transactionAmount)
	{
		this.transactionAmount = transactionAmount;
	}

	public Long getTransactionAmountConverted()
	{
		return transactionAmountConverted;
	}

	public void setTransactionAmountConverted(Long transactionAmountConverted)
	{
		this.transactionAmountConverted = transactionAmountConverted;
	}

	public String getCurrency1()
	{
		return currency1;
	}

	public void setCurrency1(String currency1)
	{
		this.currency1 = currency1;
	}

	public String getCurrency2()
	{
		return currency2;
	}

	public void setCurrency2(String currency2)
	{
		this.currency2 = currency2;
	}

	public RefillInformation getRefillInformation()
	{
		return refillInformation;
	}

	public void setRefillInformation(RefillInformation refillInformation)
	{
		this.refillInformation = refillInformation;
	}

	public AccountBeforeRefill getAccountBeforeRefill()
	{
		return accountBeforeRefill;
	}

	public void setAccountBeforeRefill(AccountBeforeRefill accountBeforeRefill)
	{
		this.accountBeforeRefill = accountBeforeRefill;
	}

	public AccountAfterRefill getAccountAfterRefill()
	{
		return accountAfterRefill;
	}

	public void setAccountAfterRefill(AccountAfterRefill accountAfterRefill)
	{
		this.accountAfterRefill = accountAfterRefill;
	}

	public Integer getRefillFraudCount()
	{
		return refillFraudCount;
	}

	public void setRefillFraudCount(Integer refillFraudCount)
	{
		this.refillFraudCount = refillFraudCount;
	}

	public Integer getSelectedOption()
	{
		return selectedOption;
	}

	public void setSelectedOption(Integer selectedOption)
	{
		this.selectedOption = selectedOption;
	}

	public String getSegmentationID()
	{
		return segmentationID;
	}

	public void setSegmentationID(String segmentationID)
	{
		this.segmentationID = segmentationID;
	}

	public Integer getRefillType()
	{
		return refillType;
	}

	public void setRefillType(Integer refillType)
	{
		this.refillType = refillType;
	}

	@Override
	public Integer[] getNegotiatedCapabilities()
	{
		return negotiatedCapabilities;
	}

	@Override
	public void setNegotiatedCapabilities(Integer[] negotiatedCapabilities)
	{
		this.negotiatedCapabilities = negotiatedCapabilities;
	}

	@Override
	public Integer[] getAvailableServerCapabilities()
	{
		return availableServerCapabilities;
	}

	@Override
	public void setAvailableServerCapabilities(Integer[] availableServerCapabilities)
	{
		this.availableServerCapabilities = availableServerCapabilities;
	}

	public Long getAccountPrepaidEmptyLimit1()
	{
		return accountPrepaidEmptyLimit1;
	}

	public void setAccountPrepaidEmptyLimit1(Long accountPrepaidEmptyLimit1)
	{
		this.accountPrepaidEmptyLimit1 = accountPrepaidEmptyLimit1;
	}

	public Long getAccountPrepaidEmptyLimit2()
	{
		return accountPrepaidEmptyLimit2;
	}

	public void setAccountPrepaidEmptyLimit2(Long accountPrepaidEmptyLimit2)
	{
		this.accountPrepaidEmptyLimit2 = accountPrepaidEmptyLimit2;
	}

	public TreeDefinedField getTreeDefinedField()
	{
		return treeDefinedField;
	}

	public void setTreeDefinedField(TreeDefinedField treeDefinedField)
	{
		this.treeDefinedField = treeDefinedField;
	}

}
