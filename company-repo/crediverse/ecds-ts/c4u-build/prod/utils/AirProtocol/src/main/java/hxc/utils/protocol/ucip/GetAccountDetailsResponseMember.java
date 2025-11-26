package hxc.utils.protocol.ucip;

import java.util.Date;

import hxc.connectors.air.Air;
import hxc.connectors.air.IResponseHeader;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * GetAccountDetailsResponseMember
 * 
 */
public class GetAccountDetailsResponseMember implements IResponseHeader
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
	 * The firstIVRCallFlag flag is sent to inform the client that the current IVR session is the first IVR call. The element is only included if it is set to 1 (true).
	 */
	public Boolean firstIVRCallFlag;

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
	@Air(Mandatory = true)
	public int languageIDCurrent;

	/*
	 * The serviceClassCurrent parameter contains the service class currently used by the subscriber. This might be a temporary Service Class, which is controlled by a temporary Service Class expiry
	 * date (separate parameter).
	 */
	@Air(Mandatory = true, Range = "0:9999")
	public int serviceClassCurrent;

	/*
	 * The serviceClassOriginal parameter contains the identity of the original service class when a temporary service class is active for an account. In case serviceClassOriginal is returned then the
	 * serviceClassCurrent will contain the temporary service class currently active for the account. When a temporary service class is active and a Return Service Class ID is specified, the
	 * serviceClassOriginal parameter will contain the identity of the return service class instead of the original service class. The account will then return to the specified Return Service Class ID
	 * when the temporary service class expires.
	 */
	@Air(Range = "0:9999")
	public Integer serviceClassOriginal;

	/*
	 * The serviceClassTemporaryExpiryDate parameter contains the expiry date of a temporary service class of an account. A temporary service class has precedence before the normally assigned service
	 * class, as long as the temporary service class date expiry date is not passed.
	 */
	public Date serviceClassTemporaryExpiryDate;

	/*-
	 * The ussdEndOfCallNotificationID parameter identifies which decision
	 * tree to use, when selecting the appropriate USSD text string for the End of Call
	 * Notification message to the subscriber.
	 *
	 * Possible Values:
	 * ----------------
	 * 0-199:	As defined in the tariff trees
	 * 200-254:	Reserved
	 * 255:	No ID assigned (clears previously assigned ID)
	 */
	public Integer ussdEndOfCallNotificationID;

	/*
	 * The accountGroupID parameter contains the Account Group identity for the account. An account can be placed in an Account Group which makes it possible to group subscribers together without
	 * considering their Service Classes.
	 */
	@Air(Range = "0:2147483647")
	public Integer accountGroupID;

	public ServiceOfferings[] serviceOfferings;

	public CommunityInformationCurrent[] communityInformationCurrent;

	/*
	 * The temporaryBlockedFlag parameter is a flag indicating whether the subscriber and operator has access to subscriber and account data. A temporary blocked subscriber does not have access to the
	 * account, and any messages requesting changes in the account or subscriber data are rejected. Therefore in case temporary blocked is set, a temporary blocked indication is set in the response
	 * code of the response message and the action is rejected. It is always possible to read data, but the only way to change data again is to unblock the subscriber. In case temporary blocked flag
	 * is not returned in the response messages, then the subscriber is not blocked.
	 */
	public Boolean temporaryBlockedFlag;

	/*
	 * The accountActivatedFlag flag is sent when the account got activated as a result of the User Communication session.
	 */
	public Boolean accountActivatedFlag;

	/*
	 * The activationDate parameter contains the activation date of an account. Subordinate subscribers will contain the activation date of the master account.
	 */
	@Air(Range = "DateMin:DateToday")
	public Date activationDate;

	public AccountFlags accountFlags;

	/*
	 * The masterSubscriberFlag flag is sent to indicate that the user is the master subscriber of the account.
	 */
	public Boolean masterSubscriberFlag;

	/*
	 * The masterAccountNumber parameter contains the subscriber identity of the master subscriber in a multi user account. The format of the parameter is the same numbering format as used by the
	 * account database, this also includes
	 */
	@Air(Length = "1:28", Format = "Numeric")
	public String masterAccountNumber;

	/*
	 * The refillUnbarDateTime parameter specifies the date and time when a refill barred subscriber is unbarred and is allowed to do a new refill.
	 */
	public Date refillUnbarDateTime;

	/*
	 * The promotionAnnouncementCode parameter identifies the promotional code for the announcement to be played. The message in question is applicable for refill promotions.
	 */
	@Air(Range = "0:99")
	public Integer promotionAnnouncementCode;

	/*
	 * The promotionPlanID parameter contains the identity of one of the current promotion plans of a subscriber.
	 */
	@Air(Length = "1:4", Format = "Alphanumeric")
	public String promotionPlanID;

	/*
	 * The promotionStartDate parameter specifies the start date of the associated promotion plan.
	 */
	public Date promotionStartDate;

	/*
	 * The promotionEndDate parameter specifies the end date of the associated promotion plan.
	 */
	public Date promotionEndDate;

	/*
	 * The supervisionExpiryDate parameter contains the expiry date of the supervision period.
	 */
	public Date supervisionExpiryDate;

	/*
	 * The creditClearanceDate parameter contains the date when the credit clearance period will expire.
	 */
	@Air(Range = "DateToday:DateMax")
	public Date creditClearanceDate;

	/*
	 * The serviceRemovalDate parameter contains the date when the account will be removed after the service removal period has expired.
	 */
	public Date serviceRemovalDate;

	/*
	 * The serviceFeeExpiryDate parameter contains the expiry date of the service fee period.
	 */
	public Date serviceFeeExpiryDate;

	/*
	 * The serviceClassChangeUnbarDate parameter contains the date when a charged service class change will be unbarred, that is allowed again. If sent, a charged service class change is not allowed.
	 * Today's date and dates older than today's date will not be sent.
	 */
	@Air(Range = "DateTomorrow:DateMax.")
	public Date serviceClassChangeUnbarDate;

	/*
	 * The serviceFeePeriod parameter contains the number of days until the service fee period expires.
	 */
	@Air(Range = "-32767:32767")
	public Integer serviceFeePeriod;

	/*
	 * The supervisionPeriod parameter contains the number of days until the supervision period expires.
	 */
	@Air(Range = "-32767:32767")
	public Integer supervisionPeriod;

	/*
	 * The serviceRemovalPeriod parameter contains the period until service removal.
	 */
	@Air(Range = "0:1023")
	public Integer serviceRemovalPeriod;

	/*
	 * The creditClearancePeriod parameter contains the period until credit clearance.
	 */
	@Air(Range = "0:1023")
	public Integer creditClearancePeriod;

	/*
	 * The currency1 and currency2 parameters contains the currencies to be presented to the end user. currency1 indicates the first currency to be announced and currency2 the second one.
	 */
	@Air(Format = "Currency")
	public String currency1;

	/*
	 * The accountvalue1 and accountValue2 parameters contains the account value for the subscriber's master account. This is not taking in consideration any ongoing chargeable events. 1 indicates an
	 * account value in the first currency to be announced and 2 an account value in the second one.
	 */
	@Air(Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long accountValue1;

	/*
	 * The aggregatedBalance1 and aggregatedBalance2 parameters contains the aggregated balance for the subscriber. This is not taking in consideration any ongoing chargeable events.
	 * aggregatedBalance1 indicates an aggregated balance in the first currency to be announced and aggregatedBalance2 the aggregated balance in the second currency. Aggregated balance is used to
	 * display the total balance of real money on the subscribers account. Real money can be seen as money added to the account by the subscriber and does not include various bonuses or promotions.
	 * Subscribers aggregated balance is the sum of main account value and the dedicated accounts marked with the dedicatedAccountRealMoneyFlag flag.
	 */
	// ??
	// @Air(PC="PC:05225",Range="-999999999999:999999999999",Format="Price")
	// @XmlRpcAsString
	// public Long aggregatedBalance1;

	/*
	 * The currency1 and currency2 parameters contains the currencies to be presented to the end user. currency1 indicates the first currency to be announced and currency2 the second one.
	 */
	@Air(Format = "Currency")
	public String currency2;

	/*
	 * The accountvalue1 and accountValue2 parameters contains the account value for the subscriber's master account. This is not taking in consideration any ongoing chargeable events. 1 indicates an
	 * account value in the first currency to be announced and 2 an account value in the second one.
	 */
	@Air(Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long accountValue2;

	/*
	 * The accountHomeRegion parameter contains the home region for the account.
	 */
	@Air(Range = "0:999")
	public Integer accountHomeRegion;

	/*
	 * The pinCode parameter contains the pin code for the subscriber.
	 */
	@Air(PC = "PC:01853", Length = "1:8", Format = "Numeric")
	public String pinCode;

	/*
	 * The aggregatedBalance1 and aggregatedBalance2 parameters contains the aggregated balance for the subscriber. This is not taking in consideration any ongoing chargeable events.
	 * aggregatedBalance1 indicates an aggregated balance in the first currency to be announced and aggregatedBalance2 the aggregated balance in the second currency. Aggregated balance is used to
	 * display the total balance of real money on the subscribers account. Real money can be seen as money added to the account by the subscriber and does not include various bonuses or promotions.
	 * Subscribers aggregated balance is the sum of main account value and the dedicated accounts marked with the dedicatedAccountRealMoneyFlag flag.
	 */
	@Air(PC = "PC:05225", Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long aggregatedBalance1;

	/*
	 * The aggregatedBalance1 and aggregatedBalance2 parameters contains the aggregated balance for the subscriber. This is not taking in consideration any ongoing chargeable events.
	 * aggregatedBalance1 indicates an aggregated balance in the first currency to be announced and aggregatedBalance2 the aggregated balance in the second currency. Aggregated balance is used to
	 * display the total balance of real money on the subscribers account. Real money can be seen as money added to the account by the subscriber and does not include various bonuses or promotions.
	 * Subscribers aggregated balance is the sum of main account value and the dedicated accounts marked with the dedicatedAccountRealMoneyFlag flag.
	 */
	@Air(PC = "PC:05225", Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long aggregatedBalance2;

	public PamInformationList[] pamInformationList;

	/*
	 * The maxServiceFeePeriod parameter contains the maximum allowed duration of the service fee period in number of days until the period expires.
	 */
	@Air(Range = "0:32767")
	public Integer maxServiceFeePeriod;

	/*
	 * The maxSupervisionPeriod parameter contains the maximum allowed duration of the supervision period in number of days until the period expires.
	 */
	@Air(Range = "0:32767")
	public Integer maxSupervisionPeriod;

	/*
	 * The negativeBalalanceBarringDate parameter contains the date when a subscriber is scheduled to be barred, or was barred, due to negative balance.
	 */
	@Air(Range = "DateMin:DateMax")
	public Date negativeBalanceBarringDate;

	public AccountFlagsBefore accountFlagsBefore;

	public OfferInformationList[] offerInformationList;

	/*
	 * This parameter contains the accountsTimeZone in the following formats, EST, EST5EDT or America/Whitehorse (Olson Database). The later of the three is required to get country specific daylight
	 * saving time start and stop dates. The supported time zones are the ones defined in the JavaTM framework (Olson Database). More information about the supported ways of defining TimeZones can be
	 * found in the JavaDoc.
	 */
	@Air(PC = "PC:07061")
	public String accountTimeZone;

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

	/*-
	 * The cellIdentifier parameter output the CellGlobal Identity (CGI) or
	 * Service Area Identity (SAI). The cellIdentifier can be sent as an input parameter
	 * through UCIP. If not sent as parameter, cellIdentifier is retrieved from the HLR
	 * using ATI request.
	 * The cellIdentifier parameter can also be a return parameter through
	 * UCIP.
	 *
	 * Possible Values:
	 * ----------------
	 * 1 to 19:	Numeric
	 * 10 to 19:	CGI/SAI
	 */
	@Air(CAP = "CAP:12")
	public String cellIdentifier;

	/*
	 * The locationNumber parameter contains the location of the subscriber. The default numbering format in the parameter is the same numbering format that is used for subscriber numbers in the
	 * Account Database. If an another format is used then it must be indicated by ''Location Number NAI''
	 */
	@Air(CAP = "CAP:12", Length = "1:20", Format = "Numeric")
	public String locationNumber;

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

	public Boolean getFirstIVRCallFlag()
	{
		return firstIVRCallFlag;
	}

	public void setFirstIVRCallFlag(Boolean firstIVRCallFlag)
	{
		this.firstIVRCallFlag = firstIVRCallFlag;
	}

	public int getLanguageIDCurrent()
	{
		return languageIDCurrent;
	}

	public void setLanguageIDCurrent(int languageIDCurrent)
	{
		this.languageIDCurrent = languageIDCurrent;
	}

	public int getServiceClassCurrent()
	{
		return serviceClassCurrent;
	}

	public void setServiceClassCurrent(int serviceClassCurrent)
	{
		this.serviceClassCurrent = serviceClassCurrent;
	}

	public Integer getServiceClassOriginal()
	{
		return serviceClassOriginal;
	}

	public void setServiceClassOriginal(Integer serviceClassOriginal)
	{
		this.serviceClassOriginal = serviceClassOriginal;
	}

	public Date getServiceClassTemporaryExpiryDate()
	{
		return serviceClassTemporaryExpiryDate;
	}

	public void setServiceClassTemporaryExpiryDate(Date serviceClassTemporaryExpiryDate)
	{
		this.serviceClassTemporaryExpiryDate = serviceClassTemporaryExpiryDate;
	}

	public Integer getUssdEndOfCallNotificationID()
	{
		return ussdEndOfCallNotificationID;
	}

	public void setUssdEndOfCallNotificationID(Integer ussdEndOfCallNotificationID)
	{
		this.ussdEndOfCallNotificationID = ussdEndOfCallNotificationID;
	}

	public Integer getAccountGroupID()
	{
		return accountGroupID;
	}

	public void setAccountGroupID(Integer accountGroupID)
	{
		this.accountGroupID = accountGroupID;
	}

	public ServiceOfferings[] getServiceOfferings()
	{
		return serviceOfferings;
	}

	public void setServiceOfferings(ServiceOfferings[] serviceOfferings)
	{
		this.serviceOfferings = serviceOfferings;
	}

	public CommunityInformationCurrent[] getCommunityInformationCurrent()
	{
		return communityInformationCurrent;
	}

	public void setCommunityInformationCurrent(CommunityInformationCurrent[] communityInformationCurrent)
	{
		this.communityInformationCurrent = communityInformationCurrent;
	}

	public Boolean getTemporaryBlockedFlag()
	{
		return temporaryBlockedFlag;
	}

	public void setTemporaryBlockedFlag(Boolean temporaryBlockedFlag)
	{
		this.temporaryBlockedFlag = temporaryBlockedFlag;
	}

	public Boolean getAccountActivatedFlag()
	{
		return accountActivatedFlag;
	}

	public void setAccountActivatedFlag(Boolean accountActivatedFlag)
	{
		this.accountActivatedFlag = accountActivatedFlag;
	}

	public Date getActivationDate()
	{
		return activationDate;
	}

	public void setActivationDate(Date activationDate)
	{
		this.activationDate = activationDate;
	}

	public AccountFlags getAccountFlags()
	{
		return accountFlags;
	}

	public void setAccountFlags(AccountFlags accountFlags)
	{
		this.accountFlags = accountFlags;
	}

	public Boolean getMasterSubscriberFlag()
	{
		return masterSubscriberFlag;
	}

	public void setMasterSubscriberFlag(Boolean masterSubscriberFlag)
	{
		this.masterSubscriberFlag = masterSubscriberFlag;
	}

	public String getMasterAccountNumber()
	{
		return masterAccountNumber;
	}

	public void setMasterAccountNumber(String masterAccountNumber)
	{
		this.masterAccountNumber = masterAccountNumber;
	}

	public Date getRefillUnbarDateTime()
	{
		return refillUnbarDateTime;
	}

	public void setRefillUnbarDateTime(Date refillUnbarDateTime)
	{
		this.refillUnbarDateTime = refillUnbarDateTime;
	}

	public Integer getPromotionAnnouncementCode()
	{
		return promotionAnnouncementCode;
	}

	public void setPromotionAnnouncementCode(Integer promotionAnnouncementCode)
	{
		this.promotionAnnouncementCode = promotionAnnouncementCode;
	}

	public String getPromotionPlanID()
	{
		return promotionPlanID;
	}

	public void setPromotionPlanID(String promotionPlanID)
	{
		this.promotionPlanID = promotionPlanID;
	}

	public Date getPromotionStartDate()
	{
		return promotionStartDate;
	}

	public void setPromotionStartDate(Date promotionStartDate)
	{
		this.promotionStartDate = promotionStartDate;
	}

	public Date getPromotionEndDate()
	{
		return promotionEndDate;
	}

	public void setPromotionEndDate(Date promotionEndDate)
	{
		this.promotionEndDate = promotionEndDate;
	}

	public Date getSupervisionExpiryDate()
	{
		return supervisionExpiryDate;
	}

	public void setSupervisionExpiryDate(Date supervisionExpiryDate)
	{
		this.supervisionExpiryDate = supervisionExpiryDate;
	}

	public Date getCreditClearanceDate()
	{
		return creditClearanceDate;
	}

	public void setCreditClearanceDate(Date creditClearanceDate)
	{
		this.creditClearanceDate = creditClearanceDate;
	}

	public Date getServiceRemovalDate()
	{
		return serviceRemovalDate;
	}

	public void setServiceRemovalDate(Date serviceRemovalDate)
	{
		this.serviceRemovalDate = serviceRemovalDate;
	}

	public Date getServiceFeeExpiryDate()
	{
		return serviceFeeExpiryDate;
	}

	public void setServiceFeeExpiryDate(Date serviceFeeExpiryDate)
	{
		this.serviceFeeExpiryDate = serviceFeeExpiryDate;
	}

	public Date getServiceClassChangeUnbarDate()
	{
		return serviceClassChangeUnbarDate;
	}

	public void setServiceClassChangeUnbarDate(Date serviceClassChangeUnbarDate)
	{
		this.serviceClassChangeUnbarDate = serviceClassChangeUnbarDate;
	}

	public Integer getServiceFeePeriod()
	{
		return serviceFeePeriod;
	}

	public void setServiceFeePeriod(Integer serviceFeePeriod)
	{
		this.serviceFeePeriod = serviceFeePeriod;
	}

	public Integer getSupervisionPeriod()
	{
		return supervisionPeriod;
	}

	public void setSupervisionPeriod(Integer supervisionPeriod)
	{
		this.supervisionPeriod = supervisionPeriod;
	}

	public Integer getServiceRemovalPeriod()
	{
		return serviceRemovalPeriod;
	}

	public void setServiceRemovalPeriod(Integer serviceRemovalPeriod)
	{
		this.serviceRemovalPeriod = serviceRemovalPeriod;
	}

	public Integer getCreditClearancePeriod()
	{
		return creditClearancePeriod;
	}

	public void setCreditClearancePeriod(Integer creditClearancePeriod)
	{
		this.creditClearancePeriod = creditClearancePeriod;
	}

	public String getCurrency1()
	{
		return currency1;
	}

	public void setCurrency1(String currency1)
	{
		this.currency1 = currency1;
	}

	public Long getAccountValue1()
	{
		return accountValue1;
	}

	public void setAccountValue1(Long accountValue1)
	{
		this.accountValue1 = accountValue1;
	}

	public String getCurrency2()
	{
		return currency2;
	}

	public void setCurrency2(String currency2)
	{
		this.currency2 = currency2;
	}

	public Long getAccountValue2()
	{
		return accountValue2;
	}

	public void setAccountValue2(Long accountValue2)
	{
		this.accountValue2 = accountValue2;
	}

	public Integer getAccountHomeRegion()
	{
		return accountHomeRegion;
	}

	public void setAccountHomeRegion(Integer accountHomeRegion)
	{
		this.accountHomeRegion = accountHomeRegion;
	}

	public String getPinCode()
	{
		return pinCode;
	}

	public void setPinCode(String pinCode)
	{
		this.pinCode = pinCode;
	}

	public Long getAggregatedBalance1()
	{
		return aggregatedBalance1;
	}

	public void setAggregatedBalance1(Long aggregatedBalance1)
	{
		this.aggregatedBalance1 = aggregatedBalance1;
	}

	public Long getAggregatedBalance2()
	{
		return aggregatedBalance2;
	}

	public void setAggregatedBalance2(Long aggregatedBalance2)
	{
		this.aggregatedBalance2 = aggregatedBalance2;
	}

	public PamInformationList[] getPamInformationList()
	{
		return pamInformationList;
	}

	public void setPamInformationList(PamInformationList[] pamInformationList)
	{
		this.pamInformationList = pamInformationList;
	}

	public Integer getMaxServiceFeePeriod()
	{
		return maxServiceFeePeriod;
	}

	public void setMaxServiceFeePeriod(Integer maxServiceFeePeriod)
	{
		this.maxServiceFeePeriod = maxServiceFeePeriod;
	}

	public Integer getMaxSupervisionPeriod()
	{
		return maxSupervisionPeriod;
	}

	public void setMaxSupervisionPeriod(Integer maxSupervisionPeriod)
	{
		this.maxSupervisionPeriod = maxSupervisionPeriod;
	}

	public Date getNegativeBalanceBarringDate()
	{
		return negativeBalanceBarringDate;
	}

	public void setNegativeBalanceBarringDate(Date negativeBalanceBarringDate)
	{
		this.negativeBalanceBarringDate = negativeBalanceBarringDate;
	}

	public AccountFlagsBefore getAccountFlagsBefore()
	{
		return accountFlagsBefore;
	}

	public void setAccountFlagsBefore(AccountFlagsBefore accountFlagsBefore)
	{
		this.accountFlagsBefore = accountFlagsBefore;
	}

	public OfferInformationList[] getOfferInformationList()
	{
		return offerInformationList;
	}

	public void setOfferInformationList(OfferInformationList[] offerInformationList)
	{
		this.offerInformationList = offerInformationList;
	}

	public String getAccountTimeZone()
	{
		return accountTimeZone;
	}

	public void setAccountTimeZone(String accountTimeZone)
	{
		this.accountTimeZone = accountTimeZone;
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

	public String getCellIdentifier()
	{
		return cellIdentifier;
	}

	public void setCellIdentifier(String cellIdentifier)
	{
		this.cellIdentifier = cellIdentifier;
	}

	public String getLocationNumber()
	{
		return locationNumber;
	}

	public void setLocationNumber(String locationNumber)
	{
		this.locationNumber = locationNumber;
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

}
