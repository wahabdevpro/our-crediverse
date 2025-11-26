package hxc.services.airsim.protocol;

import java.util.Date;

import hxc.connectors.air.Air;
import hxc.connectors.soap.ISubscriber;
import hxc.utils.xmlrpc.XmlRpcAsString;

public class Subscriber implements ISubscriber
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	protected String nationalNumber;
	protected String internationalNumber;

	// GBD

	/*
	 * The serviceClassCurrent parameter contains the service class currently used by the subscriber. This might be a temporary Service Class, which is controlled by a temporary Service Class expiry
	 * date (separate parameter).
	 */
	@Air(Mandatory = true, Range = "0:9999")
	protected int serviceClassCurrent;

	/*
	 * The currency1 and currency2 parameters contains the currencies to be presented to the end user. currency1 indicates the first currency to be announced and currency2 the second one.
	 */
	@Air(Format = "Currency")
	protected String currency1;

	/*
	 * The accountvalue1 and accountValue2 parameters contains the account value for the subscriber's master account. This is not taking in consideration any ongoing chargeable events. 1 indicates an
	 * account value in the first currency to be announced and 2 an account value in the second one.
	 */
	@Air(Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	protected Long accountValue1;

	/*
	 * The aggregatedBalance1 and aggregatedBalance2 parameters contains the aggregated balance for the subscriber. This is not taking in consideration any ongoing chargeable events.
	 * aggregatedBalance1 indicates an aggregated balance in the first currency to be announced and aggregatedBalance2 the aggregated balance in the second currency. Aggregated balance is used to
	 * display the total balance of real money on the subscribers account. Real money can be seen as money added to the account by the subscriber and does not include various bonuses or promotions.
	 * Subscribers aggregated balance is the sum of main account value and the dedicated accounts marked with the dedicatedAccountRealMoneyFlag flag.
	 */
	@Air(PC = "PC:05225", Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	protected Long aggregatedBalance1;

	/*
	 * The currency1 and currency2 parameters contains the currencies to be presented to the end user. currency1 indicates the first currency to be announced and currency2 the second one.
	 */
	@Air(Format = "Currency")
	protected String currency2;

	/*
	 * The accountvalue1 and accountValue2 parameters contains the account value for the subscriber's master account. This is not taking in consideration any ongoing chargeable events. 1 indicates an
	 * account value in the first currency to be announced and 2 an account value in the second one.
	 */
	@Air(Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	protected Long accountValue2;

	/*
	 * The aggregatedBalance1 and aggregatedBalance2 parameters contains the aggregated balance for the subscriber. This is not taking in consideration any ongoing chargeable events.
	 * aggregatedBalance1 indicates an aggregated balance in the first currency to be announced and aggregatedBalance2 the aggregated balance in the second currency. Aggregated balance is used to
	 * display the total balance of real money on the subscribers account. Real money can be seen as money added to the account by the subscriber and does not include various bonuses or promotions.
	 * Subscribers aggregated balance is the sum of main account value and the dedicated accounts marked with the dedicatedAccountRealMoneyFlag flag.
	 */
	@Air(PC = "PC:05225", Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	protected Long aggregatedBalance2;

	// private DedicatedAccountInformation[] dedicatedAccountInformation;

	/*
	 * The supervisionExpiryDate parameter contains the expiry date of the supervision period.
	 */
	protected Date supervisionExpiryDate;

	/*
	 * The serviceFeeExpiryDate parameter contains the expiry date of the service fee period.
	 */
	protected Date serviceFeeExpiryDate;

	/*
	 * The creditClearanceDate parameter contains the date when the credit clearance period will expire.
	 */
	@Air(Range = "DateToday:DateMax")
	protected Date creditClearanceDate;

	/*
	 * The serviceRemovalDate parameter contains the date when the account will be removed after the service removal period has expired.
	 */
	protected Date serviceRemovalDate;

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
	protected int languageIDCurrent;

	/*
	 * The temporaryBlockedFlag parameter is a flag indicating whether the subscriber and operator has access to subscriber and account data. A temporary blocked subscriber does not have access to the
	 * account, and any messages requesting changes in the account or subscriber data are rejected. Therefore in case temporary blocked is set, a temporary blocked indication is set in the response
	 * code of the response message and the action is rejected. It is always possible to read data, but the only way to change data again is to unblock the subscriber. In case temporary blocked flag
	 * is not returned in the response messages, then the subscriber is not blocked.
	 */
	protected Boolean temporaryBlockedFlag;

	/*
	 * The accountPrepaidEmptyLimit1 and accountPrepaidEmptyLimit2 parameters contain the lowest allowed balance on an account. 1 indicates an account balance in the first currency and 2 an account
	 * balance in the second one.
	 */
	@Air(CAP = "CAP:13", Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	protected Long accountPrepaidEmptyLimit1;

	/*
	 * The accountPrepaidEmptyLimit1 and accountPrepaidEmptyLimit2 p arameters contain the lowest allowed balance on an account. 1 indicates an account balance in the first currency and 2 an account
	 * balance in the second one.
	 */
	@Air(CAP = "CAP:13", Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	protected Long accountPrepaidEmptyLimit2;

	// @Air(PC = "PC:10803", CAP = "CAP:15")
	// public AggregatedBalanceInformation aggregatedBalanceInformation;

	// GAD

	/*
	 * The firstIVRCallFlag flag is sent to inform the client that the current IVR session is the first IVR call. The element is only included if it is set to 1 (true).
	 */
	protected Boolean firstIVRCallFlag;

	/*
	 * The serviceClassOriginal parameter contains the identity of the original service class when a temporary service class is active for an account. In case serviceClassOriginal is returned then the
	 * serviceClassCurrent will contain the temporary service class currently active for the account. When a temporary service class is active and a Return Service Class ID is specified, the
	 * serviceClassOriginal parameter will contain the identity of the return service class instead of the original service class. The account will then return to the specified Return Service Class ID
	 * when the temporary service class expires.
	 */
	@Air(Range = "0:9999")
	protected Integer serviceClassOriginal;

	/*
	 * The serviceClassTemporaryExpiryDate parameter contains the expiry date of a temporary service class of an account. A temporary service class has precedence before the normally assigned service
	 * class, as long as the temporary service class date expiry date is not passed.
	 */
	protected Date serviceClassTemporaryExpiryDate;

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
	protected Integer ussdEndOfCallNotificationID;

	/*
	 * The accountGroupID parameter contains the Account Group identity for the account. An account can be placed in an Account Group which makes it possible to group subscribers together without
	 * considering their Service Classes.
	 */
	@Air(Range = "0:2147483647")
	protected Integer accountGroupID;

	// private ServiceOfferings[] serviceOfferings;
	//
	// private CommunityInformationCurrent[] communityInformationCurrent;

	/*
	 * The accountActivatedFlag flag is sent when the account got activated as a result of the User Communication session.
	 */
	protected Boolean accountActivatedFlag;

	/*
	 * The activationDate parameter contains the activation date of an account. Subordinate subscribers will contain the activation date of the master account.
	 */
	@Air(Range = "DateMin:DateToday")
	protected Date activationDate;

	// private AccountFlags accountFlags;

	/*
	 * The masterSubscriberFlag flag is sent to indicate that the user is the master subscriber of the account.
	 */
	protected Boolean masterSubscriberFlag = true;

	/*
	 * The masterAccountNumber parameter contains the subscriber identity of the master subscriber in a multi user account. The format of the parameter is the same numbering format as used by the
	 * account database, this also includes
	 */
	@Air(Length = "1:28", Format = "Numeric")
	protected String masterAccountNumber;

	/*
	 * The refillUnbarDateTime parameter specifies the date and time when a refill barred subscriber is unbarred and is allowed to do a new refill.
	 */
	protected Date refillUnbarDateTime;

	/*
	 * The promotionAnnouncementCode parameter identifies the promotional code for the announcement to be played. The message in question is applicable for refill promotions.
	 */
	@Air(Range = "0:99")
	protected Integer promotionAnnouncementCode;

	/*
	 * The promotionPlanID parameter contains the identity of one of the current promotion plans of a subscriber.
	 */
	@Air(Length = "1:4", Format = "Alphanumeric")
	protected String promotionPlanID;

	/*
	 * The promotionStartDate parameter specifies the start date of the associated promotion plan.
	 */
	protected Date promotionStartDate;

	/*
	 * The promotionEndDate parameter specifies the end date of the associated promotion plan.
	 */
	protected Date promotionEndDate;

	/*
	 * The serviceClassChangeUnbarDate parameter contains the date when a charged service class change will be unbarred, that is allowed again. If sent, a charged service class change is not allowed.
	 * Today's date and dates older than today's date will not be sent.
	 */
	@Air(Range = "DateTomorrow:DateMax.")
	protected Date serviceClassChangeUnbarDate;

	/*
	 * The serviceFeePeriod parameter contains the number of days until the service fee period expires.
	 */
	@Air(Range = "-32767:32767")
	protected Integer serviceFeePeriod;

	/*
	 * The supervisionPeriod parameter contains the number of days until the supervision period expires.
	 */
	@Air(Range = "-32767:32767")
	protected Integer supervisionPeriod;

	/*
	 * The serviceRemovalPeriod parameter contains the period until service removal.
	 */
	@Air(Range = "0:1023")
	protected Integer serviceRemovalPeriod;

	/*
	 * The creditClearancePeriod parameter contains the period until credit clearance.
	 */
	@Air(Range = "0:1023")
	protected Integer creditClearancePeriod;

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
	 * The accountHomeRegion parameter contains the home region for the account.
	 */
	@Air(Range = "0:999")
	protected Integer accountHomeRegion;

	/*
	 * The pinCode parameter contains the pin code for the subscriber.
	 */
	@Air(PC = "PC:01853", Length = "1:8", Format = "Numeric")
	protected String pinCode;

	// private PamInformationList[] pamInformationList;

	/*
	 * The maxServiceFeePeriod parameter contains the maximum allowed duration of the service fee period in number of days until the period expires.
	 */
	@Air(Range = "0:32767")
	protected Integer maxServiceFeePeriod;

	/*
	 * The maxSupervisionPeriod parameter contains the maximum allowed duration of the supervision period in number of days until the period expires.
	 */
	@Air(Range = "0:32767")
	protected Integer maxSupervisionPeriod;

	/*
	 * The negativeBalalanceBarringDate parameter contains the date when a subscriber is scheduled to be barred, or was barred, due to negative balance.
	 */
	@Air(Range = "DateMin:DateMax")
	protected Date negativeBalanceBarringDate;

	/*
	 * This parameter contains the accountsTimeZone in the following formats, EST, EST5EDT or America/Whitehorse (Olson Database). The later of the three is required to get country specific daylight
	 * saving time start and stop dates. The supported time zones are the ones defined in the JavaTM framework (Olson Database). More information about the supported ways of defining TimeZones can be
	 * found in the JavaDoc.
	 */
	@Air(PC = "PC:07061")
	protected String accountTimeZone;

	/*
	 * The negotiatedCapabilities parameter is used to indicate the negotiated capabilities between the client and server node. The capabilities are presented as a series of elements, where each
	 * element contains an integer value between 0 and 2147483647. The value 0 indicates that none of the capabilities in the element are active, and the value 2147483647 indicates that all of the
	 * capabilities in the element are active. See Section 9 on page 213 for the supported capabilities.
	 */
	@Air(Range = "0:2147483647")
	protected Integer negotiatedCapabilities;

	/*
	 * The availableServerCapablities parameter is used to indicate the available capabilities at the server node. The capabilities are presented as a series of elements, where each element contains
	 * an integer value between 0 and 2147483647. The value 0 indicates that none of the capabilities in the element are active, and the value 2147483647 indicates that all of the capabilities in the
	 * element are active. If only one element is present and is set to 0, no functions after release AIR-IP 5.0 are active, only legacy functionality can be used.
	 */
	@Air(Range = "0:2147483647")
	protected Integer availableServerCapabilities;

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
	protected String cellIdentifier;

	/*
	 * The locationNumber parameter contains the location of the subscriber. The default numbering format in the parameter is the same numbering format that is used for subscriber numbers in the
	 * Account Database. If an another format is used then it must be indicated by ''Location Number NAI''
	 */
	@Air(CAP = "CAP:12", Length = "1:20", Format = "Numeric")
	protected String locationNumber;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@Override
	public String getNationalNumber()
	{
		return nationalNumber;
	}

	public void setNationalNumber(String nationalNumber)
	{
		this.nationalNumber = nationalNumber;
	}

	@Override
	public String getInternationalNumber()
	{
		return internationalNumber;
	}

	public void setInternationalNumber(String internationalNumber)
	{
		this.internationalNumber = internationalNumber;
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

	public int getServiceClassCurrent()
	{
		return serviceClassCurrent;
	}

	public void setServiceClassCurrent(int serviceClassCurrent)
	{
		this.serviceClassCurrent = serviceClassCurrent;
	}

	public Long getAccountValue1()
	{
		return accountValue1;
	}

	public void setAccountValue1(Long accountValue1) throws Exception
	{
		Long oldValue = this.accountValue1;
		this.accountValue1 = accountValue1;
		try
		{
			triggerValueChange(0, oldValue, accountValue1);
		}
		catch (Exception e)
		{
			throw (e);
		}
	}

	protected void triggerValueChange(int accountID, Long oldValue, Long newValue) throws Exception
	{
	}

	public Long getAggregatedBalance1()
	{
		return aggregatedBalance1;
	}

	public void setAggregatedBalance1(Long aggregatedBalance1)
	{
		this.aggregatedBalance1 = aggregatedBalance1;
	}

	public Long getAccountValue2()
	{
		return accountValue2;
	}

	public void setAccountValue2(Long accountValue2)
	{
		this.accountValue2 = accountValue2;
	}

	public Long getAggregatedBalance2()
	{
		return aggregatedBalance2;
	}

	public void setAggregatedBalance2(Long aggregatedBalance2)
	{
		this.aggregatedBalance2 = aggregatedBalance2;
	}

	public Date getSupervisionExpiryDate()
	{
		return supervisionExpiryDate;
	}

	public void setSupervisionExpiryDate(Date supervisionExpiryDate)
	{
		this.supervisionExpiryDate = supervisionExpiryDate;
	}

	public Date getServiceFeeExpiryDate()
	{
		return serviceFeeExpiryDate;
	}

	public void setServiceFeeExpiryDate(Date serviceFeeExpiryDate)
	{
		this.serviceFeeExpiryDate = serviceFeeExpiryDate;
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

	public int getLanguageIDCurrent()
	{
		return languageIDCurrent;
	}

	public void setLanguageIDCurrent(int languageIDCurrent)
	{
		this.languageIDCurrent = languageIDCurrent;
	}

	public Boolean getTemporaryBlockedFlag()
	{
		return temporaryBlockedFlag;
	}

	public void setTemporaryBlockedFlag(Boolean temporaryBlockedFlag)
	{
		this.temporaryBlockedFlag = temporaryBlockedFlag;
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

	public Boolean getFirstIVRCallFlag()
	{
		return firstIVRCallFlag;
	}

	public void setFirstIVRCallFlag(Boolean firstIVRCallFlag)
	{
		this.firstIVRCallFlag = firstIVRCallFlag;
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

	public Boolean getAccountActivatedFlag()
	{
		return this.accountActivatedFlag;
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
		this.accountActivatedFlag = activationDate != null && (new Date()).after(activationDate);

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
		this.masterSubscriberFlag = isSameNumber(masterAccountNumber);
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

	public String getAccountTimeZone()
	{
		return accountTimeZone;
	}

	public void setAccountTimeZone(String accountTimeZone)
	{
		this.accountTimeZone = accountTimeZone;
	}

	public Integer getNegotiatedCapabilities()
	{
		return negotiatedCapabilities;
	}

	public void setNegotiatedCapabilities(Integer negotiatedCapabilities)
	{
		this.negotiatedCapabilities = negotiatedCapabilities;
	}

	public Integer getAvailableServerCapabilities()
	{
		return availableServerCapabilities;
	}

	public void setAvailableServerCapabilities(Integer availableServerCapabilities)
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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Subscriber()
	{

	}

	public Subscriber(String nationalNumber, String internationalNumber, Subscriber subscriber)
	{

		this.nationalNumber = nationalNumber;
		this.internationalNumber = internationalNumber;
		this.serviceClassCurrent = subscriber.serviceClassCurrent;
		this.currency1 = subscriber.currency1;
		this.accountValue1 = subscriber.accountValue1;
		this.aggregatedBalance1 = subscriber.aggregatedBalance1;
		this.currency2 = subscriber.currency2;
		this.accountValue2 = subscriber.accountValue2;
		this.aggregatedBalance2 = subscriber.aggregatedBalance2;
		this.supervisionExpiryDate = subscriber.supervisionExpiryDate;
		this.serviceFeeExpiryDate = subscriber.serviceFeeExpiryDate;
		this.creditClearanceDate = subscriber.creditClearanceDate;
		this.serviceRemovalDate = subscriber.serviceRemovalDate;
		this.languageIDCurrent = subscriber.languageIDCurrent;
		this.temporaryBlockedFlag = subscriber.temporaryBlockedFlag;
		this.accountPrepaidEmptyLimit1 = subscriber.accountPrepaidEmptyLimit1;
		this.accountPrepaidEmptyLimit2 = subscriber.accountPrepaidEmptyLimit2;
		this.firstIVRCallFlag = subscriber.firstIVRCallFlag;
		this.serviceClassOriginal = subscriber.serviceClassOriginal;
		this.serviceClassTemporaryExpiryDate = subscriber.serviceClassTemporaryExpiryDate;
		this.ussdEndOfCallNotificationID = subscriber.ussdEndOfCallNotificationID;
		this.accountGroupID = subscriber.accountGroupID;
		this.accountActivatedFlag = subscriber.accountActivatedFlag;
		this.activationDate = subscriber.activationDate;
		this.masterSubscriberFlag = subscriber.masterSubscriberFlag;
		this.masterAccountNumber = internationalNumber;
		this.refillUnbarDateTime = subscriber.refillUnbarDateTime;
		this.promotionAnnouncementCode = subscriber.promotionAnnouncementCode;
		this.promotionPlanID = subscriber.promotionPlanID;
		this.promotionStartDate = subscriber.promotionStartDate;
		this.promotionEndDate = subscriber.promotionEndDate;
		this.serviceClassChangeUnbarDate = subscriber.serviceClassChangeUnbarDate;
		this.serviceFeePeriod = subscriber.serviceFeePeriod;
		this.supervisionPeriod = subscriber.supervisionPeriod;
		this.serviceRemovalPeriod = subscriber.serviceRemovalPeriod;
		this.creditClearancePeriod = subscriber.creditClearancePeriod;
		this.accountHomeRegion = subscriber.accountHomeRegion;
		this.pinCode = subscriber.pinCode;
		this.maxServiceFeePeriod = subscriber.maxServiceFeePeriod;
		this.maxSupervisionPeriod = subscriber.maxSupervisionPeriod;
		this.negativeBalanceBarringDate = subscriber.negativeBalanceBarringDate;
		this.accountTimeZone = subscriber.accountTimeZone;
		this.negotiatedCapabilities = subscriber.negotiatedCapabilities;
		this.availableServerCapabilities = subscriber.availableServerCapabilities;
		this.cellIdentifier = subscriber.cellIdentifier;
		this.locationNumber = subscriber.locationNumber;

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ISubscriber Implementatioon
	//
	// /////////////////////////////////
	@Override
	public int getLanguageID()
	{
		return languageIDCurrent;
	}

	@Override
	public boolean isSameNumber(String msisdn)
	{
		return this.internationalNumber.equals(msisdn) || this.nationalNumber.equals(msisdn);
	}

	@Override
	public int getServiceClass()
	{
		return serviceClassCurrent;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
}
