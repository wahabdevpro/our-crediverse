package hxc.connectors.air.proxy;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.air.AirConnector.AirConnectorConfig;
import hxc.connectors.air.AirException;
import hxc.connectors.air.IAirConnection;
import hxc.connectors.air.IAirConnector;
import hxc.connectors.air.IRequestHeader;
import hxc.connectors.air.IRequestHeader2;
import hxc.connectors.soap.ISubscriber;
import hxc.servicebus.HostInfo;
import hxc.servicebus.IPlugin;
import hxc.services.notification.IPhrase;
import hxc.services.transactions.ITransaction;
import hxc.services.transactions.Reversal;
import hxc.utils.protocol.acip.DeleteOfferRequest;
import hxc.utils.protocol.acip.DeleteOfferResponse;
import hxc.utils.protocol.acip.DeleteUsageThresholdsRequest;
import hxc.utils.protocol.acip.DeleteUsageThresholdsResponse;
import hxc.utils.protocol.acip.InstallSubscriberRequest;
import hxc.utils.protocol.acip.InstallSubscriberResponse;
import hxc.utils.protocol.acip.UsageThresholds;
import hxc.utils.protocol.ucip.AccountFlagsAfter;
import hxc.utils.protocol.ucip.AccountFlagsBefore;
import hxc.utils.protocol.ucip.CommunityInformationCurrent;
import hxc.utils.protocol.ucip.DedicatedAccountChangeInformation;
import hxc.utils.protocol.ucip.DedicatedAccountInformation;
import hxc.utils.protocol.ucip.DedicatedAccountUpdateInformation;
import hxc.utils.protocol.ucip.FafInformation;
import hxc.utils.protocol.ucip.GetAccountDetailsRequest;
import hxc.utils.protocol.ucip.GetAccountDetailsResponse;
import hxc.utils.protocol.ucip.GetAccountDetailsResponseMember;
import hxc.utils.protocol.ucip.GetBalanceAndDateRequest;
import hxc.utils.protocol.ucip.GetBalanceAndDateResponse;
import hxc.utils.protocol.ucip.GetBalanceAndDateResponseMember;
import hxc.utils.protocol.ucip.GetFaFListRequest;
import hxc.utils.protocol.ucip.GetFaFListResponse;
import hxc.utils.protocol.ucip.GetOffersRequest;
import hxc.utils.protocol.ucip.GetOffersResponse;
import hxc.utils.protocol.ucip.GetUsageThresholdsAndCountersRequest;
import hxc.utils.protocol.ucip.GetUsageThresholdsAndCountersResponse;
import hxc.utils.protocol.ucip.MessageCapabilityFlag;
import hxc.utils.protocol.ucip.OfferInformation;
import hxc.utils.protocol.ucip.OfferInformationList;
import hxc.utils.protocol.ucip.PamInformationList;
import hxc.utils.protocol.ucip.RefillRequest;
import hxc.utils.protocol.ucip.RefillResponse;
import hxc.utils.protocol.ucip.ServiceOfferings;
import hxc.utils.protocol.ucip.UpdateAccountDetailsRequest;
import hxc.utils.protocol.ucip.UpdateAccountDetailsResponse;
import hxc.utils.protocol.ucip.UpdateBalanceAndDateRequest;
import hxc.utils.protocol.ucip.UpdateBalanceAndDateResponse;
import hxc.utils.protocol.ucip.UpdateBalanceAndDateResponseMember;
import hxc.utils.protocol.ucip.UpdateFaFListRequest;
import hxc.utils.protocol.ucip.UpdateFaFListRequestMember.FafActions;
import hxc.utils.protocol.ucip.UpdateFaFListResponse;
import hxc.utils.protocol.ucip.UpdateOfferRequest;
import hxc.utils.protocol.ucip.UpdateOfferResponse;
import hxc.utils.protocol.ucip.UpdateUsageThresholdsAndCountersRequest;
import hxc.utils.protocol.ucip.UpdateUsageThresholdsAndCountersResponse;
import hxc.utils.protocol.ucip.UsageCounterUpdateInformation;
import hxc.utils.protocol.ucip.UsageCounterUsageThresholdInformation;
import hxc.utils.protocol.ucip.UsageThresholdInformation;
import hxc.utils.protocol.ucip.UsageThresholdUpdateInformation;

// This Class performs lazy loading of subscriber information from Air
public class Subscriber implements ISubscriber
{
	final static Logger logger = LoggerFactory.getLogger(Subscriber.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final int OFFERTYPE_ACCOUNT = 0;
	private static final int OFFERTYPE_MUI = 1;
	private static final int OFFERTYPE_TIMER = 2;
	private static final int OFFERTYPE_PROVIDER = 3;
	private static final int OFFERTYPE_SHARED = 4;

	public static final int DATYPE_TIME = 0;
	public static final int DATYPE_MONEY = 1;
	public static final int DATYPE_TOTAL_OCTETS = 2;
	public static final int DATYPE_IN_OCTETS = 3;
	public static final int DATYPE_OUT_OCTETS = 4;

	public static final int DATYPE_UNITS = 5;
	public static final int DATYPE_VOLUME = 6;

	private static final int UCIP_MAX_LANGUAGES = 4;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Fields
	//
	// /////////////////////////////////
	public static final int TRANSACTION_ID_LENGTH = 20;

	// MSISDN of the Subscriber
	protected String msisdn;
	protected String internationalNumber;
	protected String nationalNumber;

	// Reference to the AirConnection
	protected IAirConnection airConnection;
	protected String airAddress = null;

	// Reference to the AirServer
	protected IAirConnector airConnector;

	// Current Transaction
	protected ITransaction transaction;

	// Maximum Retries
	private int maxRetries = 2;

	// List of Air Response codes which may be retried
	private int[] retryableResponseCodes = new int[] { AirException.TIMEOUT, 100 };

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////

	@Override
	public String getInternationalNumber()
	{
		if (internationalNumber == null)
			internationalNumber = airConnector.getNumberPlan().getInternationalFormat(msisdn);
		return internationalNumber;
	}

	@Override
	public String getNationalNumber()
	{
		if (nationalNumber == null)
			nationalNumber = airConnector.getNumberPlan().getNationalFormat(msisdn);
		return nationalNumber;
	}

	public String getTransactionID()
	{
		if (transaction != null)
			return transaction.getTransactionID();
		else
			return airConnector.getNextTransactionID(TRANSACTION_ID_LENGTH);
	}

	public ITransaction getTransaction()
	{
		return transaction;
	}

	public void setTransaction(ITransaction transaction)
	{
		this.transaction = transaction;
	}

	protected String getAirAddress()
	{
		if (airAddress != null)
			return airAddress;
		else if (airConnection == null)
			return "";
		else
		{
			airAddress = String.format("Air %s: ", airConnection.getAddress());
			return airAddress;
		}

	}

	protected String getAirEndpoint()
	{
		if (airConnection == null)
			return "";
		else
		{
			String endPoint = String.format("Air %s:%s: ", airConnection.getAddress(), airConnection.getPort());
			return endPoint;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Air Default Properties
	//
	// /////////////////////////////////
	private String originHostName = null;
	private String originNodeType = "EXT";
	private String originOperatorID = "HXC";
	private Date originTimeStamp = new Date();
	private int subscriberNumberNAI = 1;

	public String getOriginHostName()
	{
		if (originHostName == null)
		{
			originHostName = toAlphaNumeric(HostInfo.getNameOrElseHxC());
		}
		return originHostName;
	}

	public String getOriginNodeType()
	{
		return originNodeType;
	}

	public String getOriginOperatorID()
	{
		return originOperatorID;
	}

	public Date getOriginTimeStamp()
	{
		return originTimeStamp;
	}

	public void setOriginTimeStamp(Date originTimeStamp)
	{
		this.originTimeStamp = originTimeStamp;
	}

	public int getSubscriberNumberNAI()
	{
		return subscriberNumberNAI;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Cached GetBalanceAndDate / GetAccountDetails Fields
	//
	// /////////////////////////////////
	private int serviceClassCurrent;
	private int languageIDCurrent;
	private String currency1;
	private String currency2;
	private Long accountValue1;
	private Long accountValue2;
	private Long aggregatedBalance1;
	private Long aggregatedBalance2;
	private Date supervisionExpiryDate;
	private Date serviceFeeExpiryDate;
	private Date creditClearanceDate;
	private Date serviceRemovalDate;
	private Boolean temporaryBlockedFlag;
	// private OfferInformationList[] offerInformationList;
	private Long accountPrepaidEmptyLimit1;
	private Long accountPrepaidEmptyLimit2;
	private AccountFlagsAfter accountFlagsAfter;
	private AccountFlagsBefore accountFlagsBefore;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Cached GetBalanceAndDate / GetAccountDetails Properties
	//
	// /////////////////////////////////
	public int getServiceClassCurrent() throws AirException
	{
		if (!hasGBD && !hasGAD)
			getBalanceAndDateOrAccountDetails();
		return serviceClassCurrent;
	}

	public int getLanguageIDCurrent() throws AirException
	{
		if (!hasGBD && !hasGAD)
			getBalanceAndDateOrAccountDetails();
		return languageIDCurrent;
	}

	@Override
	public int getServiceClass()
	{
		try
		{
			return getServiceClassCurrent();
		}
		catch (AirException e)
		{
			return 0;
		}
	}

	@Override
	public int getLanguageID()
	{
		try
		{
			int result = getLanguageIDCurrent();
			return result < 1 || result > UCIP_MAX_LANGUAGES ? IPhrase.DEFAULT_LANGUAGE_ID : result;
		}
		catch (AirException e)
		{
			return IPhrase.DEFAULT_LANGUAGE_ID;
		}
	}

	public String getLanguageCode2()
	{
		if (languageIDCurrent < 1 || languageIDCurrent > UCIP_MAX_LANGUAGES)
			return null;
		return airConnector.getLanguageCode2(languageIDCurrent);
	}

	public String getCurrency1() throws AirException
	{
		return currency1;
	}

	public String getCurrency2() throws AirException
	{
		return currency2;
	}

	public Long getAccountValue1() throws AirException
	{
		if (!hasGBD && !hasGAD)
			getBalanceAndDateOrAccountDetails();
		return accountValue1;
	}

	public Long getAccountValue2() throws AirException
	{
		if (!hasGBD && !hasGAD)
			getBalanceAndDateOrAccountDetails();
		return accountValue2;
	}

	public Long getAggregatedBalance1() throws AirException
	{
		if (!hasGBD && !hasGAD)
			getBalanceAndDateOrAccountDetails();
		return aggregatedBalance1;
	}

	public Long getAggregatedBalance2() throws AirException
	{
		if (!hasGBD && !hasGAD)
			getBalanceAndDateOrAccountDetails();
		return aggregatedBalance2;
	}

	public Date getSupervisionExpiryDate() throws AirException
	{
		if (!hasGBD && !hasGAD)
			getBalanceAndDateOrAccountDetails();
		return supervisionExpiryDate;
	}

	public Date getServiceFeeExpiryDate() throws AirException
	{
		if (!hasGBD && !hasGAD)
			getBalanceAndDateOrAccountDetails();
		return serviceFeeExpiryDate;
	}

	public Date getCreditClearanceDate() throws AirException
	{
		if (!hasGBD && !hasGAD)
			getBalanceAndDateOrAccountDetails();
		return creditClearanceDate;
	}

	public Date getServiceRemovalDate() throws AirException
	{
		if (!hasGBD && !hasGAD)
			getBalanceAndDateOrAccountDetails();
		return serviceRemovalDate;
	}

	public Boolean getTemporaryBlockedFlag() throws AirException
	{
		if (!hasGBD && !hasGAD)
			getBalanceAndDateOrAccountDetails();
		return temporaryBlockedFlag;
	}

	public Long getAccountPrepaidEmptyLimit1() throws AirException
	{
		if (!hasGBD && !hasGAD)
			getBalanceAndDateOrAccountDetails();
		return accountPrepaidEmptyLimit1;
	}

	public Long getAccountPrepaidEmptyLimit2() throws AirException
	{
		if (!hasGBD && !hasGAD)
			getBalanceAndDateOrAccountDetails();
		return accountPrepaidEmptyLimit2;
	}

	public AccountFlagsAfter getAccountFlagsAfter() throws AirException
	{
		if (!hasGBD && !hasGAD)
			getBalanceAndDateOrAccountDetails();
		return accountFlagsAfter;
	}

	public AccountFlagsBefore getAccountFlagsBefore() throws AirException
	{
		if (!hasGBD && !hasGAD)
			getBalanceAndDateOrAccountDetails();
		return accountFlagsBefore;
	}

	public boolean isActive() throws AirException
	{
		if (!hasGBD && !hasGAD)
			getBalanceAndDateOrAccountDetails();

		Date now = new Date();

		if (supervisionExpiryDate != null && supervisionExpiryDate.before(now))
			return false;

		if (serviceFeeExpiryDate != null && serviceFeeExpiryDate.before(now))
			return false;

		if (creditClearanceDate != null && creditClearanceDate.before(now))
			return false;

		if (serviceRemovalDate != null && serviceRemovalDate.before(now))
			return false;

		if (temporaryBlockedFlag != null && temporaryBlockedFlag)
			return false;

		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Cached GetBalanceAndDate Fields
	//
	// /////////////////////////////////
	private boolean hasGBD = false; // Get Balance and Date
	private boolean hasGAD = false; // Get Account Details
	private boolean hasUCT = false; // Usage Counters and Thresholds

	private boolean hasAccountOffers = false;
	private boolean hasMuiOffers = false;
	private boolean hasTimerOffers = false;
	private boolean hasProviderOffers = false;
	private boolean hasSharedOffers = false;

	private Map<Integer, UsageCounterUsageThresholdInformation> ucMap = new HashMap<Integer, UsageCounterUsageThresholdInformation>();
	private Map<Integer, UsageThresholdInformation> utMap = new HashMap<Integer, UsageThresholdInformation>();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Cached GetBalanceAndDate Properties
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Cached GetAccountDetails Fields
	//
	// /////////////////////////////////

	private Boolean firstIVRCallFlag;
	private Integer serviceClassOriginal;
	private Date serviceClassTemporaryExpiryDate;
	private Integer ussdEndOfCallNotificationID;
	private Integer accountGroupID;
	private ServiceOfferings[] serviceOfferings;
	private CommunityInformationCurrent[] communityInformationCurrent;
	private Boolean accountActivatedFlag;
	private Date activationDate;
	private Boolean masterSubscriberFlag;
	private String masterAccountNumber;
	private Date refillUnbarDateTime;
	private Integer promotionAnnouncementCode;
	private String promotionPlanID;
	private Date promotionStartDate;
	private Date promotionEndDate;
	private Date serviceClassChangeUnbarDate;
	private Integer serviceFeePeriod;
	private Integer supervisionPeriod;
	private Integer serviceRemovalPeriod;
	private Integer creditClearancePeriod;
	private Integer accountHomeRegion;
	private String pinCode;
	private PamInformationList[] pamInformationList;
	private Integer maxServiceFeePeriod;
	private Integer maxSupervisionPeriod;
	private Date negativeBalanceBarringDate;
	private String accountTimeZone;
	private String cellIdentifier;
	private String locationNumber;
	private Boolean inactive = Boolean.FALSE;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Cached GetAccountDetails Properties
	//
	// /////////////////////////////////
	public Boolean isInactive() {
		return this.inactive;
	}

	public Boolean getFirstIVRCallFlag() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return firstIVRCallFlag;
	}

	public Integer getServiceClassOriginal() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return serviceClassOriginal;
	}

	public Date getServiceClassTemporaryExpiryDate() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return serviceClassTemporaryExpiryDate;
	}

	public Integer getUssdEndOfCallNotificationID() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return ussdEndOfCallNotificationID;
	}

	public Integer getAccountGroupID() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return accountGroupID;
	}

	public ServiceOfferings[] getServiceOfferings() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return serviceOfferings;
	}

	public CommunityInformationCurrent[] getCommunityInformationCurrent() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return communityInformationCurrent;
	}

	public Boolean getAccountActivatedFlag() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return accountActivatedFlag;
	}

	public Date getActivationDate() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return activationDate;
	}

	public Boolean getMasterSubscriberFlag() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return masterSubscriberFlag;
	}

	public String getMasterAccountNumber() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return masterAccountNumber;
	}

	public Date getRefillUnbarDateTime() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return refillUnbarDateTime;
	}

	public Integer getPromotionAnnouncementCode() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return promotionAnnouncementCode;
	}

	public String getPromotionPlanID() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return promotionPlanID;
	}

	public Date getPromotionStartDate() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return promotionStartDate;
	}

	public Date getPromotionEndDate() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return promotionEndDate;
	}

	public Date getServiceClassChangeUnbarDate() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return serviceClassChangeUnbarDate;
	}

	public Integer getServiceFeePeriod() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return serviceFeePeriod;
	}

	public Integer getSupervisionPeriod() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return supervisionPeriod;
	}

	public Integer getServiceRemovalPeriod() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return serviceRemovalPeriod;
	}

	public Integer getCreditClearancePeriod() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return creditClearancePeriod;
	}

	public Integer getAccountHomeRegion() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return accountHomeRegion;
	}

	public String getPinCode() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return pinCode;
	}

	public PamInformationList[] getPamInformationList() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return pamInformationList;
	}

	public Integer getMaxServiceFeePeriod() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return maxServiceFeePeriod;
	}

	public Integer getMaxSupervisionPeriod() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return maxSupervisionPeriod;
	}

	public Date getNegativeBalanceBarringDate() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return negativeBalanceBarringDate;
	}

	public String getAccountTimeZone() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return accountTimeZone;
	}

	public String getCellIdentifier() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return cellIdentifier;
	}

	public String getLocationNumber() throws AirException
	{
		if (!hasGAD)
			getAccountDetails();
		return locationNumber;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Subscriber(String msisdn, IAirConnector airConnector, ITransaction transaction)
	{
		this.msisdn = msisdn;
		this.airConnector = airConnector;
		this.airConnection = airConnector.getConnection(null);
		this.transaction = transaction;

		AirConnectorConfig config = (AirConnectorConfig) ((IPlugin) airConnector).getConfiguration();
		this.currency1 = config.getDefaultCurrency();
		this.originHostName = toAlphaNumeric(HostInfo.getNameOrElseHxC());
		this.originNodeType = config.getDefaultOriginNodeType();
		this.originOperatorID = config.getDefaultOriginOperatorID();
		this.subscriberNumberNAI = config.getDefaultSubscriberNumberNAI();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Account Details
	//
	// /////////////////////////////////

	public void getAccountDetails() throws AirException
	{
		if (hasGAD)
			return;

		track(this, "Get Account Details for {}", getInternationalNumber());

		for (int retry = 1; retry <= maxRetries; retry++)
		{
			try
			{

				// Perform UCIP Call
				GetAccountDetailsRequest request = new GetAccountDetailsRequest();
				updateHeader(request.member);
				GetAccountDetailsResponse result = airConnection.getAccountDetails(request);
				GetAccountDetailsResponseMember member = result.member;
				checkResponse(member.responseCode, "GetAccountDetails", airConnection.getHost());

				// Cache Results
				serviceClassCurrent = member.serviceClassCurrent;
				languageIDCurrent = member.languageIDCurrent;
				currency1 = ifNotEmptyElse(member.currency1, currency1);
				currency2 = ifNotEmptyElse(member.currency2, currency2);
				accountValue1 = member.accountValue1;
				accountValue2 = member.accountValue2;
				aggregatedBalance1 = member.aggregatedBalance1;
				aggregatedBalance2 = member.aggregatedBalance2;
				supervisionExpiryDate = member.supervisionExpiryDate;
				serviceFeeExpiryDate = member.serviceFeeExpiryDate;
				creditClearanceDate = member.creditClearanceDate;
				serviceRemovalDate = member.serviceRemovalDate;
				temporaryBlockedFlag = member.temporaryBlockedFlag;
				setOffers(member.offerInformationList);
				accountPrepaidEmptyLimit1 = member.accountPrepaidEmptyLimit1;
				accountPrepaidEmptyLimit2 = member.accountPrepaidEmptyLimit2;
				// ?? accountFlagsAfter = member.accountFlags;
				accountFlagsBefore = member.accountFlagsBefore;

				firstIVRCallFlag = member.firstIVRCallFlag;
				serviceClassOriginal = member.serviceClassOriginal;
				serviceClassTemporaryExpiryDate = member.serviceClassTemporaryExpiryDate;
				ussdEndOfCallNotificationID = member.ussdEndOfCallNotificationID;
				accountGroupID = member.accountGroupID;
				serviceOfferings = member.serviceOfferings;
				communityInformationCurrent = member.communityInformationCurrent;
				accountActivatedFlag = member.accountActivatedFlag;
				activationDate = member.activationDate;
				masterSubscriberFlag = member.masterSubscriberFlag;
				masterAccountNumber = member.masterAccountNumber;
				refillUnbarDateTime = member.refillUnbarDateTime;
				promotionAnnouncementCode = member.promotionAnnouncementCode;
				promotionPlanID = member.promotionPlanID;
				promotionStartDate = member.promotionStartDate;
				promotionEndDate = member.promotionEndDate;
				serviceClassChangeUnbarDate = member.serviceClassChangeUnbarDate;
				serviceFeePeriod = member.serviceFeePeriod;
				supervisionPeriod = member.supervisionPeriod;
				serviceRemovalPeriod = member.serviceRemovalPeriod;
				creditClearancePeriod = member.creditClearancePeriod;
				accountHomeRegion = member.accountHomeRegion;
				pinCode = member.pinCode;
				pamInformationList = member.pamInformationList;
				maxServiceFeePeriod = member.maxServiceFeePeriod;
				maxSupervisionPeriod = member.maxSupervisionPeriod;
				negativeBalanceBarringDate = member.negativeBalanceBarringDate;
				accountTimeZone = member.accountTimeZone;
				cellIdentifier = member.cellIdentifier;
				locationNumber = member.locationNumber;

				this.hasGAD = true;
				return;
			}
			catch (AirException ex)
			{
				fatal(this, ex);
				testCanBeRetried(retry, ex);
			}
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Balance and Date
	//
	// /////////////////////////////////
	public void getBalanceAndDate() throws AirException
	{
		if (hasGBD)
			return;

		track(this, "Get Balance and Date for {}", getInternationalNumber());

		for (int retry = 1; retry <= maxRetries; retry++)
		{
			try
			{
				track(this, "Executing GetBalanceAndDate for {}", getInternationalNumber());
				GetBalanceAndDateRequest request = new GetBalanceAndDateRequest();
				updateHeader(request.member);
				GetBalanceAndDateResponse result = airConnection.getBalanceAndDate(request);
				GetBalanceAndDateResponseMember member = result.member;
				checkResponse(result.member.responseCode, "GetBalanceAndDate", airConnection.getHost());

				// Cache Results
				serviceClassCurrent = member.serviceClassCurrent;
				languageIDCurrent = member.languageIDCurrent;
				currency1 = ifNotEmptyElse(member.currency1, currency1);
				currency2 = ifNotEmptyElse(member.currency2, currency2);
				accountValue1 = member.accountValue1;
				accountValue2 = member.accountValue2;
				aggregatedBalance1 = member.aggregatedBalance1;
				aggregatedBalance2 = member.aggregatedBalance2;
				supervisionExpiryDate = member.supervisionExpiryDate;
				serviceFeeExpiryDate = member.serviceFeeExpiryDate;
				creditClearanceDate = member.creditClearanceDate;
				serviceRemovalDate = member.serviceRemovalDate;
				temporaryBlockedFlag = member.temporaryBlockedFlag;
				setOffers(member.offerInformationList);
				accountPrepaidEmptyLimit1 = member.accountPrepaidEmptyLimit1;
				accountPrepaidEmptyLimit2 = member.accountPrepaidEmptyLimit2;
				accountFlagsAfter = member.accountFlagsAfter;
				accountFlagsBefore = member.accountFlagsBefore;

				setDedicatedAccounts(member.dedicatedAccountInformation);

				this.hasGBD = true;
				return;
			}
			catch (AirException ex)
			{
				fatal(this, ex);
				testCanBeRetried(retry, ex);
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Dedicated Accounts
	//
	// /////////////////////////////////
	private Map<Integer, DedicatedAccountInformation> daMap;

	private void setDedicatedAccounts(DedicatedAccountInformation[] dedicatedAccountInformation)
	{
		if (daMap == null)
			daMap = new HashMap<Integer, DedicatedAccountInformation>();

		if (dedicatedAccountInformation == null)
			return;

		for (DedicatedAccountInformation da : dedicatedAccountInformation)
		{
			daMap.put(da.dedicatedAccountID, da);
		}
	}

	private void setDedicatedAccounts(DedicatedAccountChangeInformation[] dedicatedAccountChangeInformation)
	{
		if (daMap == null)
			daMap = new HashMap<Integer, DedicatedAccountInformation>();

		if (dedicatedAccountChangeInformation == null)
			return;

		for (DedicatedAccountChangeInformation daChange : dedicatedAccountChangeInformation)
		{
			DedicatedAccountInformation da = daMap.get(daChange.dedicatedAccountID);
			if (da == null)
				da = new DedicatedAccountInformation();
			da.dedicatedAccountID = daChange.dedicatedAccountID;
			da.dedicatedAccountValue1 = daChange.dedicatedAccountValue1;
			da.dedicatedAccountValue2 = daChange.dedicatedAccountValue2;
			da.expiryDate = daChange.expiryDate;
			da.startDate = daChange.startDate;
			da.pamServiceID = daChange.pamServiceID;
			da.offerID = daChange.offerID;
			da.productID = daChange.productID;
			da.dedicatedAccountRealMoneyFlag = daChange.dedicatedAccountRealMoneyFlag;
			da.closestExpiryDate = daChange.closestExpiryDate;
			da.closestExpiryValue1 = daChange.closestExpiryValue1;
			da.closestExpiryValue2 = daChange.closestExpiryValue2;
			da.closestAccessibleDate = daChange.closestAccessibleDate;
			da.closestAccessibleValue1 = daChange.closestAccessibleValue1;
			da.closestAccessibleValue2 = daChange.closestAccessibleValue2;
			da.dedicatedAccountActiveValue1 = daChange.dedicatedAccountActiveValue1;
			da.dedicatedAccountActiveValue2 = daChange.dedicatedAccountActiveValue2;
			da.dedicatedAccountUnitType = daChange.dedicatedAccountUnitType;
			daMap.put(da.dedicatedAccountID, da);
		}
	}

	public DedicatedAccountInformation getDedicatedAccount(int dedicatedAccountID) throws AirException
	{
		if (!hasGBD)
			getBalanceAndDate();
		return daMap.get(dedicatedAccountID);
	}

	public boolean hasDecicatedAccount(int dedicatedAccountID) throws AirException
	{
		if (!hasGBD)
			getBalanceAndDate();
		return daMap.containsKey(dedicatedAccountID);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Offers
	//
	// /////////////////////////////////

	private Map<Integer, OfferInformation> offerMap;

	public OfferInformation getAccountOffer(int offerID) throws AirException
	{
		return getOffer(offerID, OFFERTYPE_ACCOUNT);
	}

	public OfferInformation getSharedOffer(int offerID) throws AirException
	{
		return getOffer(offerID, OFFERTYPE_SHARED);
	}

	public OfferInformation getProviderOffer(int offerID) throws AirException
	{
		return getOffer(offerID, OFFERTYPE_PROVIDER);
	}

	private OfferInformation getOffer(int offerID, int offerType) throws AirException
	{
		getOffers(offerType == OFFERTYPE_ACCOUNT, offerType == OFFERTYPE_MUI, offerType == OFFERTYPE_TIMER, //
				offerType == OFFERTYPE_PROVIDER, offerType == OFFERTYPE_SHARED);
		return offerMap == null ? null : offerMap.get(offerID);
	}

	public boolean hasAccountOffer(int offerID) throws AirException
	{
		return hasOffer(offerID, OFFERTYPE_ACCOUNT);
	}

	public boolean hasSharedOffer(int offerID) throws AirException
	{
		return hasOffer(offerID, OFFERTYPE_SHARED);
	}

	public boolean hasProviderOffer(int offerID) throws AirException
	{
		return hasOffer(offerID, OFFERTYPE_PROVIDER);
	}

	private boolean hasOffer(int offerID, int offerType) throws AirException
	{
		OfferInformation offer = getOffer(offerID, offerType);
		return offer != null;
	}

	public OfferInformation[] getOffers( //
			boolean accountOffers, boolean muiOffers, boolean timerOffers, boolean providerOffers, boolean sharedOffers) throws AirException
	{
		if ((!accountOffers || hasAccountOffers) //
				&& (!muiOffers || hasMuiOffers) //
				&& (!timerOffers || hasTimerOffers) //
				&& (!providerOffers || hasProviderOffers) //
				&& (!sharedOffers || hasSharedOffers))
			return null;

		track(this, "Get Offers for {}", getInternationalNumber());

		OfferInformation[] offers = new OfferInformation[0];

		for (int retry = 1; retry <= maxRetries; retry++)
		{
			try
			{
				track(this, "Executing GetOffersRequest for {}", getInternationalNumber());
				GetOffersRequest request = new GetOffersRequest();
				updateHeader(request.member);

				char digit1 = accountOffers ? '1' : '0';
				char digit2 = muiOffers ? '1' : '0';
				char digit3 = timerOffers ? '1' : '0';
				char digit4 = providerOffers ? '1' : '0';
				char digit5 = sharedOffers ? '1' : '0';
				request.member.offerRequestedTypeFlag = new String(new char[] { digit1, digit2, digit3, digit4, digit5, '0', '0', '0' });

				GetOffersResponse result = airConnection.getOffers(request);

				if (result != null && result.member != null)
				{
					checkResponse(result.member.responseCode, "GetOffersRequest", airConnection.getHost());
					if (result.member.offerInformation != null)
					{
						offers = result.member.offerInformation;
					}
				}

				setOffers(offers);

				hasAccountOffers = accountOffers ? true : hasAccountOffers;
				hasMuiOffers = muiOffers ? true : hasMuiOffers;
				hasTimerOffers = timerOffers ? true : hasTimerOffers;
				hasProviderOffers = providerOffers ? true : hasProviderOffers;
				hasSharedOffers = sharedOffers ? true : hasSharedOffers;

				return offers;
			}
			catch (AirException ex)
			{
				fatal(this, ex);
				testCanBeRetried(retry, ex);
			}

		}

		return offers;

	}

	private void setOffers(OfferInformation[] offers)
	{
		this.offerMap = new HashMap<Integer, OfferInformation>();
		for (OfferInformation offer : offers)
		{
			this.offerMap.put(offer.offerID, offer);
		}
	}

	private void setOffers(OfferInformationList[] offers)
	{
		this.offerMap = new HashMap<Integer, OfferInformation>();

		if (offers == null)
			return;

		for (OfferInformationList offer : offers)
		{
			OfferInformation info = new OfferInformation();
			info.offerID = offer.offerID;
			info.startDate = offer.startDate;
			info.expiryDate = offer.expiryDate;
			info.startDateTime = offer.startDateTime;
			info.expiryDateTime = offer.expiryDateTime;
			info.pamServiceID = offer.pamServiceID;
			info.dedicatedAccountInformation = offer.dedicatedAccountInformation;
			info.offerType = offer.offerType;
			info.offerState = offer.offerState;
			info.offerProviderID = offer.offerProviderID;
			info.productID = offer.productID;
			info.usageCounterUsageThresholdInformation = offer.usageCounterUsageThresholdInformation;
			info.attributeInformationList = offer.attributeInformationList;

			this.offerMap.put(offer.offerID, info);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Update Accounts
	//
	// /////////////////////////////////
	public UpdateBalanceAndDateResponseMember updateAccounts(Long mainValueRelative, AccountUpdate... dedicated) throws AirException
	{
		return updateAccounts((UpdateParameters) null, mainValueRelative, dedicated);
	}

	public UpdateBalanceAndDateResponseMember updateAccounts(Long mainValueRelative, List<String> externalData, AccountUpdate... dedicated) throws AirException
	{
		return updateAccounts((UpdateParameters) null, mainValueRelative, externalData, dedicated);
	}

	public UpdateBalanceAndDateResponseMember updateAccounts(UpdateParameters updateParameters, Long mainValueRelative, AccountUpdate... dedicated) throws AirException
	{
		return updateAccounts(updateParameters, mainValueRelative, null, dedicated);
	}

	public UpdateBalanceAndDateResponseMember updateAccounts(UpdateParameters updateParameters, Long mainValueRelative, List<String> externalData, AccountUpdate... dedicated) throws AirException
	{
		track(this, "Update Accounts for {}", getInternationalNumber());

		if (mainValueRelative == null && (dedicated == null || dedicated.length == 0))
			return null;

		// Prepare the call
		final UpdateBalanceAndDateRequest request = new UpdateBalanceAndDateRequest();
		updateHeader(request.member);
		request.member.transactionCurrency = getCurrency1();
		request.member.adjustmentAmountRelative = mainValueRelative;
		if ( externalData != null )
		{
			if (externalData.size() >= 2) request.member.externalData2 = externalData.get(2-1);
			if (externalData.size() >= 1) request.member.externalData1 = externalData.get(1-1);
		}

		// Add DAs
		if (dedicated != null && dedicated.length > 0)
		{
			request.member.dedicatedAccountUpdateInformation = new DedicatedAccountUpdateInformation[dedicated.length];

			int index = 0;
			for (AccountUpdate da : dedicated)
			{
				DedicatedAccountUpdateInformation daUpdate = new DedicatedAccountUpdateInformation();
				daUpdate.dedicatedAccountID = da.id;
				daUpdate.adjustmentAmountRelative = da.valueRelative;
				daUpdate.dedicatedAccountValueNew = da.valueNew;
				daUpdate.adjustmentDateRelative = da.expiryRelative;
				daUpdate.expiryDate = da.expiryNew;
				daUpdate.startDate = da.startNew;
				daUpdate.adjustmentStartDateRelative = da.startRelative;
				daUpdate.dedicatedAccountUnitType = da.unitType;
				request.member.dedicatedAccountUpdateInformation[index++] = daUpdate;
			}
		}

		// Update Parameters
		if (updateParameters != null)
			updateParameters.update(request.member);

		// Execute the call
		final UpdateBalanceAndDateResponse response = airConnection.updateBalanceAndDate(request);

		// Test the response
		checkResponse(response.member.responseCode, "UpdateBalanceAndDate", airConnection.getHost());

		// Update Cached information
		this.accountValue1 = response.member.accountValue1;
		this.accountValue2 = response.member.accountValue2;

		// Update Cached DA Information
		setDedicatedAccounts(response.member.dedicatedAccountChangeInformation);

		// Create Reversal
		Reversal reversal = new Reversal()
		{
			@Override
			public void reverse() throws Exception
			{
				warn(this, "Reversing UpdateBalanceAndDate for {}", request.member.subscriberNumber);

				if (request.member.adjustmentAmountRelative != null)
					request.member.adjustmentAmountRelative = -request.member.adjustmentAmountRelative;

				DedicatedAccountUpdateInformation[] daUpdates = request.member.dedicatedAccountUpdateInformation;
				if (daUpdates != null)
				{
					for (DedicatedAccountUpdateInformation daUpdate : daUpdates)
					{
						if (daUpdate.adjustmentAmountRelative != null)
							daUpdate.adjustmentAmountRelative = -daUpdate.adjustmentAmountRelative;
						if (daUpdate.dedicatedAccountValueNew != null)
							throw new AirException(999, null);
					}
				}

				// Execute the Call
				UpdateBalanceAndDateResponse response2 = airConnection.updateBalanceAndDate(request);

				// Test the response
				checkResponse(response2.member.responseCode, "UpdateBalanceAndDateResponse", airConnection.getHost());

				// Update Cached information
				Subscriber.this.accountValue1 = response2.member.accountValue1;
				Subscriber.this.accountValue2 = response2.member.accountValue2;

				// Update Cached DA Information
				setDedicatedAccounts(response.member.dedicatedAccountChangeInformation);

			}

		};
		transaction.addReversal(reversal);

		return response.member;

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Update Offer
	//
	// /////////////////////////////////
	public void updateAccountOffer(int offerID, Date startDateTime, Integer startDateTimeRelative, //
			Date expiryDateTime, Integer expiryDateTimeRelative) throws AirException
	{
		updateOffer(offerID, OFFERTYPE_ACCOUNT, startDateTime, startDateTimeRelative, //
				expiryDateTime, expiryDateTimeRelative, null);
	}

	public void updateSharedOffer(int offerID, Date startDateTime, Integer startDateTimeRelative, //
			Date expiryDateTime, Integer expiryDateTimeRelative) throws AirException
	{
		updateOffer(offerID, OFFERTYPE_SHARED, startDateTime, startDateTimeRelative, //
				expiryDateTime, expiryDateTimeRelative, null);
	}

	public void updateProviderOffer(int offerID, Date startDateTime, Integer startDateTimeRelative, //
			Date expiryDateTime, Integer expiryDateTimeRelative, Subscriber provider) throws AirException
	{
		updateOffer(offerID, OFFERTYPE_PROVIDER, startDateTime, startDateTimeRelative, //
				expiryDateTime, expiryDateTimeRelative, provider);
	}

	private void updateOffer(int offerID, int offerType, Date startDateTime, Integer startDateTimeRelative, //
			Date expiryDateTime, Integer expiryDateTimeRelative, Subscriber provider) throws AirException
	{
		track(this, "Update Offer {} for {}", offerID, getInternationalNumber());

		final OfferInformation origionalOffer = getOffer(offerID, offerType);

		final UpdateOfferRequest request = new UpdateOfferRequest();
		updateHeader(request.member);
		request.member.setOfferID(offerID);
		request.member.setOfferType(offerType);
		if (provider != null)
			request.member.setOfferProviderID(provider.getNaiNumber());

		// If not Timer Offer
		if (offerType != 2)
		{
			request.member.setStartDate(datePart(startDateTime));
			request.member.setStartDateRelative(startDateTimeRelative);
			request.member.setExpiryDate(datePart(expiryDateTime));
			request.member.setExpiryDateRelative(expiryDateTimeRelative);
		}

		// else if Timer Offer
		else
		{
			request.member.setStartDateTime(startDateTime);
			request.member.setStartDateTimeRelative(startDateTimeRelative);
			request.member.setExpiryDateTime(expiryDateTime);
			request.member.setExpiryDateTimeRelative(expiryDateTimeRelative);
		}

		UpdateOfferResponse response = airConnection.updateOffer(request);

		// Test the response
		checkResponse(response.member.responseCode, "UpdateOffer", airConnection.getHost());

		// Update cached information
		if (offerMap != null)
		{
			OfferInformation offer = new OfferInformation();
			offer.offerID = response.member.offerID;
			offer.startDate = response.member.startDate;
			offer.expiryDate = response.member.expiryDate;
			offer.startDateTime = response.member.startDateTime;
			offer.expiryDateTime = response.member.expiryDateTime;
			offer.pamServiceID = response.member.pamServiceID;
			offer.offerType = response.member.offerType;
			offer.offerState = response.member.offerState;
			offer.offerProviderID = response.member.offerProviderID;
			offer.productID = response.member.productID;
			offerMap.put(offer.offerID, offer);
		}

		// Create Reversal
		Reversal reversal = new Reversal()
		{
			@Override
			public void reverse() throws Exception
			{
				if (origionalOffer == null)
				{
					warn(this, "Reversing UpdateOffer for {} with offerID = {}", request.member.subscriberNumber, request.member.offerID);

					final DeleteOfferRequest delete = new DeleteOfferRequest();
					updateHeader(delete.member);
					delete.member.setOfferID(request.member.offerID);
					DeleteOfferResponse response = airConnection.deleteOffer(delete);

					// Test the response
					checkResponse(response.member.responseCode, "DeleteOffer", airConnection.getHost());

					// Update cached information
					if (offerMap != null)
					{
						offerMap.remove(delete.member.offerID);
					}
				}
			}
		};
		transaction.addReversal(reversal);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Delete Offer
	//
	// /////////////////////////////////

	public void deleteAccountOffer(int offerID) throws AirException
	{
		deleteOffer(offerID, OFFERTYPE_ACCOUNT);
	}

	public void deleteSharedOffer(int offerID) throws AirException
	{
		deleteOffer(offerID, OFFERTYPE_SHARED);
	}

	public void deleteProviderOffer(int offerID) throws AirException
	{
		deleteOffer(offerID, OFFERTYPE_PROVIDER);
	}

	private void deleteOffer(int offerID, int offerType) throws AirException
	{
		track(this, "Delete Offer {} for {}", offerID, getInternationalNumber());

		final OfferInformation origionalOffer = getOffer(offerID, offerType);
		if (origionalOffer == null)
		{
			warn(this, "DeleteOffer for non-existing Offer {} for {}. Ignored", offerID, msisdn);
			return;
		}

		final DeleteOfferRequest request = new DeleteOfferRequest();
		updateHeader(request.member);
		request.member.setOfferID(offerID);

		final DeleteOfferResponse response = airConnection.deleteOffer(request);

		// Test the response
		checkResponse(response.member.responseCode, "DeleteOffer", airConnection.getHost());

		// Update cached information
		if (offerMap != null)
		{
			offerMap.remove(request.member.getOfferID());
		}

		// Create Reversal
		Reversal reversal = new Reversal()
		{
			@Override
			public void reverse() throws Exception
			{
				warn(this, "Reversing DeleteOffer for {} with offerID = {}", request.member.subscriberNumber, request.member.offerID);

				final UpdateOfferRequest recreate = new UpdateOfferRequest();
				updateHeader(recreate.member);
				recreate.member.setOfferID(response.member.getOfferID());
				recreate.member.setOfferType(response.member.getOfferType());
				recreate.member.setExpiryDate(response.member.getExpiryDate());
				recreate.member.setExpiryDateTime(response.member.getExpiryDateTime());
				recreate.member.setOfferProviderID(response.member.getOfferProviderID());
				recreate.member.setPamServiceID(response.member.getPamServiceID());
				recreate.member.setProductID(origionalOffer.productID);
				UpdateOfferResponse response2 = airConnection.updateOffer(recreate);

				// Test the response
				checkResponse(response2.member.responseCode, "UpdateOffer", airConnection.getHost());

				// Update cached information
				if (offerMap != null)
				{
					OfferInformation newOffer = new OfferInformation();
					newOffer.offerID = response2.member.offerID;
					newOffer.startDate = response2.member.startDate;
					newOffer.expiryDate = response2.member.expiryDate;
					newOffer.startDateTime = response2.member.startDateTime;
					newOffer.expiryDateTime = response2.member.expiryDateTime;
					newOffer.pamServiceID = response2.member.pamServiceID;
					newOffer.offerType = response2.member.offerType;
					newOffer.offerState = response2.member.offerState;
					newOffer.offerProviderID = response2.member.offerProviderID;
					newOffer.productID = response2.member.productID;
					offerMap.put(newOffer.offerID, newOffer);
				}

			}
		};
		transaction.addReversal(reversal);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Update Account Details
	//
	// /////////////////////////////////

	public void updateAccountDetails(Integer languageID, Object spare1, Object spare2, Object spare3, Object spare4, Object spare5, Object spare6) throws AirException
	{
		track(this, "Update Account Details for {}", getInternationalNumber());

		final int oldLanguageID = getLanguageID();
		final UpdateAccountDetailsRequest request = new UpdateAccountDetailsRequest();
		updateHeader(request.member);
		request.member.setLanguageIDNew(languageID);

		UpdateAccountDetailsResponse response = airConnection.updateAccountDetails(request);

		// Test the response
		checkResponse(response.member.getResponseCode(), " UpdateAccountDetails", airConnection.getHost());

		// Update cached information
		this.languageIDCurrent = languageID;

		// Create Reversal
		Reversal reversal = new Reversal()
		{
			@Override
			public void reverse() throws Exception
			{
				warn(this, "Reversing LanguageID Change for {}", request.member.getSubscriberNumber());

				request.member.setLanguageIDCurrent(oldLanguageID);
				request.member.setLanguageIDNew(oldLanguageID);
				UpdateAccountDetailsResponse response = airConnection.updateAccountDetails(request);

				// Test the response
				checkResponse(response.member.getResponseCode(), "UpdateAccountDetails", airConnection.getHost());

				// Update cached information
				Subscriber.this.languageIDCurrent = oldLanguageID;
			}
		};
		transaction.addReversal(reversal);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Refill
	//
	// /////////////////////////////////
	public RefillResponse refillAccount(String refillProfileID, long transactionAmount) throws AirException
	{
		return refillAccount(refillProfileID, transactionAmount, false);
	}

	public RefillResponse refillAccount(String refillProfileID, long transactionAmount, boolean activateAccount) throws AirException
	{
		return this.refillAccount(refillProfileID, transactionAmount, activateAccount, null, false);
	}

	public RefillResponse refillAccount(String refillProfileID, long transactionAmount, boolean activateAccount,
                                        List<String> externalData, boolean enableDedicatedAccountReversal) throws AirException
	{
		track(this, "Execute Refill for {}", getInternationalNumber());

		final RefillRequest request = new RefillRequest();
		updateHeader(request.member);
		request.member.transactionAmount = transactionAmount;
		request.member.transactionCurrency = getCurrency1();
		request.member.refillProfileID = refillProfileID;
		request.member.requestRefillAccountBeforeFlag = true;
		request.member.requestRefillAccountAfterFlag = true;

		if (enableDedicatedAccountReversal) {
			request.member.requestSubDedicatedAccountDetailsFlag = true;
			request.member.requestRefillDetailsFlag = true;
        }

		if ( externalData != null )
		{
			if (externalData.size() >= 4) request.member.externalData4 = externalData.get(4-1);
			if (externalData.size() >= 3) request.member.externalData3 = externalData.get(3-1);
			if (externalData.size() >= 2) request.member.externalData2 = externalData.get(2-1);
			if (externalData.size() >= 1) request.member.externalData1 = externalData.get(1-1);
		}
		if (activateAccount)
		{
			request.member.messageCapabilityFlag = new MessageCapabilityFlag();
			request.member.messageCapabilityFlag.accountActivationFlag = true;
		}

		RefillResponse response = airConnection.refill(request);

		// Update Language ID
		Integer languageID = response.member.languageIDCurrent;
		if (languageID != null)
			this.languageIDCurrent = languageID;

		// Test the response
		checkResponse(response.member.getResponseCode(), " Refill", airConnection.getHost());

		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Set Usage Threshold
	//
	// /////////////////////////////////
	public void setUsageThreshold(final int thresholdID, Long newValue, Long newMonetartValue, final Subscriber associatedParty) throws AirException
	{
		track(this, "Set UT {} for {}", thresholdID, getInternationalNumber());

		// Get UsageCounters and Thresholds to be able to rollback
		if (!hasUCT)
			getUsageThresholdsAndCounters();

		// Get original UT
		UsageThresholdInformation ut = utMap.get(thresholdID);
		final boolean existed = ut != null;
		final Long oldUsageThresholdValue = ut == null ? null : ut.usageThresholdValue;
		final Long oldUsageThresholdMonetaryValue1 = ut == null ? null : ut.usageThresholdMonetaryValue1;
		final String oldAssociatedPartyID = ut == null ? null : ut.associatedPartyID;

		// Create Request
		final UpdateUsageThresholdsAndCountersRequest request = new UpdateUsageThresholdsAndCountersRequest();
		updateHeader(request.member);
		request.member.setTransactionCurrency(getCurrency1());
		UsageThresholdUpdateInformation usageThresholdUpdateInformation = new UsageThresholdUpdateInformation();
		usageThresholdUpdateInformation.usageThresholdID = thresholdID;
		usageThresholdUpdateInformation.usageThresholdValueNew = newValue;
		usageThresholdUpdateInformation.usageThresholdMonetaryValueNew = newMonetartValue;
		usageThresholdUpdateInformation.associatedPartyID = associatedParty == null ? null : associatedParty.getNaiNumber();
		request.member.usageThresholdUpdateInformation = new UsageThresholdUpdateInformation[] { usageThresholdUpdateInformation };

		// Execute the Request
		UpdateUsageThresholdsAndCountersResponse response = airConnection.updateUsageThresholdsAndCounters(request);

		// Test the response
		checkResponse(response.member.getResponseCode(), "UpdateUsageThresholdsAndCountersResponse", airConnection.getHost());

		// Cache the Result
		cacheUsageCounterUsageThresholdInformation(response.member.usageCounterUsageThresholdInformation);

		// Create Reversal
		Reversal reversal = new Reversal()
		{
			@Override
			public void reverse() throws Exception
			{
				warn(this, "Reversing UpdateUsageThresholdsAndCounters for {}", request.member.getSubscriberNumber());

				if (!existed)
				{
					// Delete the Threshold
					final DeleteUsageThresholdsRequest request = new DeleteUsageThresholdsRequest();
					updateHeader(request.member);
					UsageThresholds usageThreshold = new UsageThresholds();
					usageThreshold.usageThresholdID = thresholdID;
					usageThreshold.associatedPartyID = associatedParty.getNaiNumber();
					request.member.usageThresholds = new UsageThresholds[] { usageThreshold };
					DeleteUsageThresholdsResponse response = airConnection.deleteUsageThresholds(request);
					checkResponse(response.member.getResponseCode(), "DeleteUsageThresholds", airConnection.getHost());
					cacheUsageThresholdInformation(response.member.usageThresholdInformation);
					utMap.remove(thresholdID);
				}
				else
				{
					final UpdateUsageThresholdsAndCountersRequest request = new UpdateUsageThresholdsAndCountersRequest();
					updateHeader(request.member);
					request.member.transactionCurrency = Subscriber.this.getCurrency1();
					UsageThresholdUpdateInformation usageThresholdUpdateInformation = new UsageThresholdUpdateInformation();
					usageThresholdUpdateInformation.usageThresholdID = thresholdID;
					usageThresholdUpdateInformation.usageThresholdValueNew = oldUsageThresholdValue;
					usageThresholdUpdateInformation.usageThresholdMonetaryValueNew = oldUsageThresholdMonetaryValue1;
					usageThresholdUpdateInformation.associatedPartyID = oldAssociatedPartyID;
					request.member.usageThresholdUpdateInformation = new UsageThresholdUpdateInformation[] { usageThresholdUpdateInformation };
					UpdateUsageThresholdsAndCountersResponse response = airConnection.updateUsageThresholdsAndCounters(request);
					checkResponse(response.member.getResponseCode(), "UpdateUsageThresholdsAndCounters", airConnection.getHost());
					cacheUsageCounterUsageThresholdInformation(response.member.usageCounterUsageThresholdInformation);
				}
			}
		};
		transaction.addReversal(reversal);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Clear Usage Counters
	//
	// /////////////////////////////////
	public void clearUsageCounters(Subscriber associatedParty, Integer... countersToClear) throws AirException
	{
		track(this, "Clear Usage Counters for {}", getInternationalNumber());

		// Get UsageCounters and Thresholds to be able to rollback
		if (!hasUCT)
			getUsageThresholdsAndCounters();

		final UpdateUsageThresholdsAndCountersRequest request = new UpdateUsageThresholdsAndCountersRequest();
		updateHeader(request.member);
		request.member.setTransactionCurrency(getCurrency1());

		// Add Parameters
		request.member.transactionCurrency = this.getCurrency1();
		int count = countersToClear == null ? 0 : countersToClear.length;
		request.member.usageCounterUpdateInformation = new UsageCounterUpdateInformation[count];
		for (int index = 0; index < count; index++)
		{
			UsageCounterUpdateInformation usageCounterUpdateInformation = new UsageCounterUpdateInformation();
			usageCounterUpdateInformation.usageCounterID = countersToClear[index];
			usageCounterUpdateInformation.usageCounterValueNew = 0L;
			usageCounterUpdateInformation.adjustmentUsageCounterValueRelative = null;
			usageCounterUpdateInformation.usageCounterMonetaryValueNew = null;
			usageCounterUpdateInformation.adjustmentUsageCounterMonetaryValueRelative = null;
			usageCounterUpdateInformation.associatedPartyID = associatedParty == null ? null : associatedParty.getNaiNumber();
			request.member.usageCounterUpdateInformation[index] = usageCounterUpdateInformation;
		}

		// Create Reversal Request
		final UpdateUsageThresholdsAndCountersRequest reverseRequest = new UpdateUsageThresholdsAndCountersRequest();
		updateHeader(reverseRequest.member);
		reverseRequest.member.setTransactionCurrency(getCurrency1());
		count = 0;
		UsageCounterUpdateInformation[] uci = new UsageCounterUpdateInformation[countersToClear != null ? countersToClear.length : 0];
		for (int index = 0; index < (countersToClear != null ? countersToClear.length : 0); index++)
		{
			UsageCounterUsageThresholdInformation uc = ucMap.get(countersToClear[index]);
			if (uc == null)
				continue;
			UsageCounterUpdateInformation ucu = new UsageCounterUpdateInformation();
			ucu.usageCounterID = uc.usageCounterID;
			ucu.usageCounterValueNew = uc.usageCounterValue;
			ucu.usageCounterMonetaryValueNew = uc.usageCounterMonetaryValue1;
			ucu.associatedPartyID = uc.associatedPartyID;
			ucu.productID = uc.productID;
			uci[count++] = ucu;
		}
		reverseRequest.member.setUsageCounterUpdateInformation(Arrays.copyOf(uci, count));

		// Execute Request
		UpdateUsageThresholdsAndCountersResponse response = airConnection.updateUsageThresholdsAndCounters(request);

		// Test the response
		checkResponse(response.member.getResponseCode(), "UpdateUsageThresholdsAndCountersResponse", airConnection.getHost());

		// Cache the Results
		cacheUsageCounterUsageThresholdInformation(response.member.usageCounterUsageThresholdInformation);

		// Create Reversal
		Reversal reversal = new Reversal()
		{
			@Override
			public void reverse() throws Exception
			{
				warn(this, "Reversing UpdateUsageThresholdsAndCounters for {}", request.member.getSubscriberNumber());
				UpdateUsageThresholdsAndCountersResponse response = airConnection.updateUsageThresholdsAndCounters(reverseRequest);
				checkResponse(response.member.responseCode, "UpdateUsageThresholdsAndCounters", airConnection.getHost());
				cacheUsageCounterUsageThresholdInformation(response.member.usageCounterUsageThresholdInformation);
			}
		};
		transaction.addReversal(reversal);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Query Usage Threshold and Counters
	//
	// /////////////////////////////////
	public UsageCounterUsageThresholdInformation[] getUsageThresholdsAndCounters() throws AirException
	{
		track(this, "Query UC/UT for {}", getInternationalNumber());

		if (hasUCT)
			return ucMap.values().toArray(new UsageCounterUsageThresholdInformation[ucMap.size()]);

		for (int retry = 1; retry <= maxRetries; retry++)
		{
			try
			{
				track(this, "Executing GetUsageThresholdsAndCounters for {}", getInternationalNumber());
				GetUsageThresholdsAndCountersRequest request = new GetUsageThresholdsAndCountersRequest();
				updateHeader(request.member);
				GetUsageThresholdsAndCountersResponse result = airConnection.getUsageThresholdsAndCounters(request);
				checkResponse(result.member.responseCode, "GetUsageThresholdsAndCounters", airConnection.getHost());

				// Update Cached Information
				cacheUsageCounterUsageThresholdInformation(result.member.usageCounterUsageThresholdInformation);
				hasUCT = true;

				return result.member.usageCounterUsageThresholdInformation;

			}
			catch (AirException ex)
			{
				fatal(this, ex);
				testCanBeRetried(retry, ex);
			}

		}

		return null;
	}

	private void cacheUsageCounterUsageThresholdInformation(UsageCounterUsageThresholdInformation[] usageCounterUsageThresholdInformation)
	{
		if (usageCounterUsageThresholdInformation == null)
			return;

		for (UsageCounterUsageThresholdInformation uc : usageCounterUsageThresholdInformation)
		{
			ucMap.put(uc.usageCounterID, uc);
			cacheUsageThresholdInformation(uc.usageThresholdInformation);

		}

	}

	private void cacheUsageThresholdInformation(UsageThresholdInformation[] usageThresholdInformation)
	{
		if (usageThresholdInformation == null)
			return;

		for (UsageThresholdInformation ut : usageThresholdInformation)
		{
			utMap.put(ut.usageThresholdID, ut);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Delete Usage Threshold
	//
	// /////////////////////////////////
	public void deleteUsageThresholds(Subscriber associatedParty, Integer... thresholdIDs) throws AirException
	{
		track(this, "Delete UTs for {}", getInternationalNumber());

		// Get UsageCounters and Thresholds to be able to rollback
		if (!hasUCT)
			getUsageThresholdsAndCounters();

		final DeleteUsageThresholdsRequest request = new DeleteUsageThresholdsRequest();
		updateHeader(request.member);

		// Add Parameters
		int count = thresholdIDs == null ? 0 : thresholdIDs.length;
		request.member.usageThresholds = new UsageThresholds[count];
		for (int index = 0; index < count; index++)
		{
			UsageThresholds usageThreshold = new UsageThresholds();
			usageThreshold.usageThresholdID = thresholdIDs[index];
			usageThreshold.associatedPartyID = associatedParty == null ? null : associatedParty.getNaiNumber();
			request.member.usageThresholds[index] = usageThreshold;
		}

		// Create Reversal Request
		final UpdateUsageThresholdsAndCountersRequest reverseRequest = new UpdateUsageThresholdsAndCountersRequest();
		updateHeader(reverseRequest.member);
		reverseRequest.member.setTransactionCurrency(getCurrency1());
		count = 0;
		UsageThresholdUpdateInformation[] uti = new UsageThresholdUpdateInformation[thresholdIDs != null ? thresholdIDs.length : 0];
		for (int index = 0; index < (thresholdIDs != null ? thresholdIDs.length : 0); index++)
		{
			UsageThresholdInformation ut = utMap.get(thresholdIDs[index]);
			if (ut == null)
				continue;
			UsageThresholdUpdateInformation utu = new UsageThresholdUpdateInformation();
			utu.usageThresholdID = ut.usageThresholdID;
			utu.usageThresholdValueNew = ut.usageThresholdValue;
			utu.usageThresholdMonetaryValueNew = ut.usageThresholdMonetaryValue1;
			utu.associatedPartyID = ut.associatedPartyID;
			uti[count++] = utu;
		}
		reverseRequest.member.setUsageThresholdUpdateInformation(Arrays.copyOf(uti, count));

		// Execute the call
		DeleteUsageThresholdsResponse response = airConnection.deleteUsageThresholds(request);

		// Test the response
		checkResponse(response.member.getResponseCode(), "DeleteUsageThresholds", airConnection.getHost());

		// Update Cache
		for (int index = 0; index < (thresholdIDs != null ? thresholdIDs.length : 0); index++)
		{
			utMap.remove(thresholdIDs[index]);
		}
		cacheUsageThresholdInformation(response.member.getUsageThresholdInformation());

		// Create Reversal
		Reversal reversal = new Reversal()
		{
			@Override
			public void reverse() throws Exception
			{
				warn(this, "Reversing DeleteUsageThresholds for {}", request.member.getSubscriberNumber());
				UpdateUsageThresholdsAndCountersResponse response = airConnection.updateUsageThresholdsAndCounters(reverseRequest);
				checkResponse(response.member.getResponseCode(), "UpdateUsageThresholdsAndCounters", airConnection.getHost());
				cacheUsageCounterUsageThresholdInformation(response.member.usageCounterUsageThresholdInformation);
			}

		};
		transaction.addReversal(reversal);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Install Subscriber
	//
	// /////////////////////////////////
	public void install(int serviceClassID) throws AirException
	{
		track(this, "Install Subscriber {}", getInternationalNumber());

		final InstallSubscriberRequest request = new InstallSubscriberRequest();
		updateHeader(request.member);

		// Add Parameters
		request.member.setServiceClassNew(serviceClassID);

		// Create Reversal Request
		// Cannot be reversed

		// Execute the call
		InstallSubscriberResponse response = airConnection.installSubscriber(request);

		// Test the response
		checkResponse(response.member.getResponseCode(), "InstallSubscriber", airConnection.getHost());

		// Update Cache
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Usage Threshold
	//
	// /////////////////////////////////
	public UsageThresholdInformation getUsageThreshold(int usageThresholdID) throws AirException
	{
		if (!hasUCT)
			getUsageThresholdsAndCounters();
		return utMap.get(usageThresholdID);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Usage Counter
	//
	// /////////////////////////////////
	public UsageCounterUsageThresholdInformation getUsageCounter(int usageCounterID) throws AirException
	{
		if (!hasUCT)
			getUsageThresholdsAndCounters();
		return ucMap.get(usageCounterID);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Query FAF List
	//
	// /////////////////////////////////
	public FafInformation[] getFafList() throws AirException
	{
		track(this, "getFafList for {}", getInternationalNumber());

		FafInformation[] fafList = new FafInformation[0];

		for (int retry = 1; retry <= maxRetries; retry++)
		{
			try
			{
				track(this, "Executing GetOffersRequest for {}", getInternationalNumber());
				GetFaFListRequest request = new GetFaFListRequest();
				updateHeader(request.member);

				GetFaFListResponse result = airConnection.getFaFList(request);

				if (result != null && result.member != null)
				{
					checkResponse(result.member.responseCode, "GetFaFListRequest", airConnection.getHost());
					if (result.member.fafInformationList != null)
					{
						fafList = result.member.fafInformationList;
					}
				}
				return fafList;
			}
			catch (AirException ex)
			{
				fatal(this, ex);
				testCanBeRetried(retry, ex);
			}
		}
		return fafList;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ADD FAF List
	//
	// /////////////////////////////////
	public boolean addFafList(FafInformation fafInfo) throws AirException
	{
		track(this, "addFafList for {}", getInternationalNumber());

		try
		{
			track(this, "Executing GetOffersRequest for {}", getInternationalNumber());
			UpdateFaFListRequest request = new UpdateFaFListRequest();
			updateHeader(request.member);
			request.member.setFafInformation(fafInfo);
			request.member.setFafAction(FafActions.ADD);
			UpdateFaFListResponse result = airConnection.updateFaFList(request);

			if (result != null && result.member != null)
			{
				checkResponse(result.member.responseCode, "GetFaFListRequest", airConnection.getHost());
				if (result.member == null)
				{
					return false;
				}
			}
			return true;
		}
		catch (AirException ex)
		{
			fatal(this, ex);
		}
		return false;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// DEL FAF List
	//
	// /////////////////////////////////
	public boolean delFafList(FafInformation fafInfo) throws AirException
	{
		track(this, "Get Offers for {}", getInternationalNumber());

		try
		{
			track(this, "Executing GetOffersRequest for {}", getInternationalNumber());
			UpdateFaFListRequest request = new UpdateFaFListRequest();
			updateHeader(request.member);
			request.member.setFafInformation(fafInfo);
			request.member.setFafAction(FafActions.DELETE);
			UpdateFaFListResponse result = airConnection.updateFaFList(request);

			if (result != null && result.member != null)
			{
				checkResponse(result.member.responseCode, "GetFaFListRequest", airConnection.getHost());
				if (result.member == null)
				{
					return false;
				}
			}
			return true;
		}
		catch (AirException ex)
		{
			fatal(this, ex);
		}
		return false;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// REPLACE FAF List
	//
	// /////////////////////////////////
	public boolean replaceFafList(FafInformation fafOldInfo, FafInformation fafNewInfo) throws AirException
	{
		track(this, "Get Offers for {}", getInternationalNumber());

		try
		{
			track(this, "Executing GetOffersRequest for {}", getInternationalNumber());
			UpdateFaFListRequest request = new UpdateFaFListRequest();
			updateHeader(request.member);
			request.member.setFafInformation(fafOldInfo);
			request.member.setFafAction(FafActions.DELETE);
			UpdateFaFListResponse result = airConnection.updateFaFList(request);

			if (result != null && result.member != null)
			{
				checkResponse(result.member.responseCode, "GetFaFListRequest", airConnection.getHost());
				if (result.member == null)
				{
					return false;
				}
			}

			request.member.setFafInformation(fafNewInfo);
			request.member.setFafAction(FafActions.ADD);
			result = airConnection.updateFaFList(request);

			if (result != null && result.member != null)
			{
				checkResponse(result.member.responseCode, "GetFaFListRequest", airConnection.getHost());
				if (result.member == null)
				{
					return false;
				}
			}

			return true;
		}
		catch (AirException ex)
		{
			fatal(this, ex);
		}
		return false;
	}
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	@Override
	public String toString()
	{
		return msisdn;
	}

	private void updateHeader(IRequestHeader member)
	{
		member.setOriginNodeType(getOriginNodeType());
		member.setOriginHostName(getOriginHostName());
		member.setOriginTransactionID(getTransactionID());
		member.setOriginTimeStamp(getOriginTimeStamp());
		member.setSubscriberNumberNAI(getSubscriberNumberNAI());
		member.setSubscriberNumber(getNaiNumber());
		member.setNegotiatedCapabilities(null);
	}

	public String getNaiNumber()
	{
		return getSubscriberNumberNAI() == 1 ? getInternationalNumber() : getNationalNumber();
	}

	private void updateHeader(IRequestHeader2 member)
	{
		updateHeader((IRequestHeader) member);
		member.setOriginOperatorID(getOriginOperatorID());
	}

	private void getBalanceAndDateOrAccountDetails() throws AirException
	{
		getBalanceAndDate();
	}

	// Tests if an Air Exception contains an response code which can be retried
	protected boolean canBeRetried(AirException ex)
	{
		int responseCode = ex.getResponseCode();
		for (int retryableResponseCode : retryableResponseCodes)
		{
			if (responseCode == retryableResponseCode)
				return true;
		}

		return false;
	}

	// Test if an operation which threw an AirException, can be retried
	protected void testCanBeRetried(int retry, AirException ex) throws AirException
	{
		if (retry >= maxRetries || !canBeRetried(ex))
			throw ex;
	}

	protected void checkResponse(int responseCode, String method, String server) throws AirException
	{
		if (responseCode != 0 && responseCode != 1 && responseCode != 2)
		{
			warn(this, "{} call returned responseCode {}", method, responseCode);
			if (transaction != null)
				transaction.setResultCode(responseCode);
			throw new AirException(responseCode, server);
		}
	}

	private Date datePart(Date dateTime)
	{
		if (dateTime == null)
			return dateTime;

		Calendar cal = Calendar.getInstance();
		cal.setTime(dateTime);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public boolean isInServiceClass(int[] allowedServiceClasses) throws AirException
	{
		if (allowedServiceClasses == null)
			return false;
		int sc = getServiceClassCurrent();
		for (int allowedServiceClass : allowedServiceClasses)
		{
			if (sc == allowedServiceClass)
				return true;
		}
		return false;
	}

	@Override
	public boolean isSameNumber(String msisdn)
	{
		if (msisdn == null || msisdn.length() == 0)
			return false;
		if (msisdn.equalsIgnoreCase(this.msisdn))
			return true;

		return getInternationalNumber().equalsIgnoreCase(airConnector.getNumberPlan().getInternationalFormat(msisdn));
	}

	private void track(Object origin, String message, Object... args)
	{
		logger.debug(getAirEndpoint() + message, args);
	}

	private void warn(Object origin, String message, Object... args)
	{
		logger.warn(getAirEndpoint() + message, args);
	}

	private void fatal(Object origin, Exception ex)
	{
		logger.error(getAirEndpoint() + ex.getMessage());
	}

	private String ifNotEmptyElse(String text, String defaultText)
	{
		return text != null && text.length() > 0 ? text : defaultText;
	}

	public Long getAccountBalance(int accountID) throws AirException
	{
		if (accountID == 0)
			return getAccountValue1();

		DedicatedAccountInformation da = getDedicatedAccount(accountID);
		if (da == null)
			return null;

		return da.dedicatedAccountValue1;

	}

	public boolean hasPSO(int psoNumber) throws AirException
	{
		ServiceOfferings[] psoList = getServiceOfferings();
		for (ServiceOfferings pso : psoList)
		{
			if (pso.serviceOfferingActiveFlag && pso.serviceOfferingID == psoNumber)
				return true;
		}
		return false;
	}

	private String toAlphaNumeric(String text)
	{
		if (text == null || text.isEmpty())
			return text;
		return text.replaceAll("[^a-zA-Z0-9]", "");
	}

}
