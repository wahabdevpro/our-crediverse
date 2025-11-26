package hxc.connectors.zte.proxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hxc.connectors.zte.ZTEException;
import hxc.connectors.zte.IZTEConnection;
import hxc.connectors.zte.IZTEConnector;
import hxc.connectors.zte.ZTEConnection;
//import hxc.connectors.zte.IResponseHeader;
import hxc.connectors.zte.ZTEConnector.ZTEConnectorConfig;
//import hxc.connectors.zte.IRequestHeader;
//import hxc.connectors.zte.IRequestHeader2;
import hxc.connectors.soap.ISubscriber;
import hxc.servicebus.HostInfo;
import hxc.servicebus.IPlugin;
import hxc.services.logging.ILogger;
import hxc.services.notification.IPhrase;
import hxc.services.transactions.ITransaction;
import hxc.services.transactions.Reversal;
import hxc.utils.protocol.zte.IResponseHeader;
import hxc.utils.protocol.zte.OfferInformation;
import hxc.utils.protocol.zte.basewebservice.*;
import hxc.utils.protocol.zte.econetwebservice.*;
import hxc.utils.protocol.zte.basewebservice.TQryProdStateReq;
import hxc.utils.protocol.zte.basewebservice.TQryProdStateRes;
import hxc.utils.protocol.zte.basewebservice.TQueryUserProfileReq;
import hxc.utils.protocol.zte.basewebservice.TQueryUserProfileRsp;
import hxc.utils.protocol.zte.basewebservice.TResponseBO;
import hxc.utils.protocol.zte.basewebservice.TServiceAttrDto;
import hxc.utils.protocol.zte.basewebservice.TServiceDto;
import hxc.utils.protocol.zte.basewebservice.TServiceDto1;
import hxc.utils.protocol.zte.basewebservice.TServiceDto2;
import hxc.utils.protocol.zte.basewebservice.TServiceDto3;
import hxc.utils.protocol.zte.basewebservice.TSetServiceReq;
import hxc.utils.protocol.zte.subsinformation.*;
import hxc.utils.protocol.zte.FafInformation;

// This Class performs lazy loading of subscriber information from ZTE
public class Subscriber implements ISubscriber
{
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

	// Reference to the ZTEConnection
	protected IZTEConnection zteConnection;
	protected String zteAddress = null;

	// Reference to the ZTEServer
	protected IZTEConnector zteConnector;

	// Reference to the Logger
	protected ILogger logger;

	// Current Transaction
	protected ITransaction transaction;

	// Maximum Retries
	private int maxRetries = 2;

	// List of ZTE Response codes which may be retried
	private int[] retryableResponseCodes = new int[] { ZTEException.TIMEOUT, 100 };

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////

	@Override
	public String getInternationalNumber()
	{
		if (internationalNumber == null)
			internationalNumber = zteConnector.getNumberPlan().getInternationalFormat(msisdn);
		return internationalNumber;
	}

	@Override
	public String getNationalNumber()
	{
		if (nationalNumber == null)
			nationalNumber = zteConnector.getNumberPlan().getNationalFormat(msisdn);
		return nationalNumber;
	}

	public String getTransactionID()
	{
		if (transaction != null)
			return transaction.getTransactionID();
		else
			return zteConnector.getNextTransactionID(TRANSACTION_ID_LENGTH);
	}

	public ITransaction getTransaction()
	{
		return transaction;
	}

	public void setTransaction(ITransaction transaction)
	{
		this.transaction = transaction;
	}

	public String getAddress()
	{
		if (zteAddress != null)
			return zteAddress;
		else if (zteConnection == null)
			return "";
		else
		{
			zteAddress = String.format("ZTE %s: ", zteConnection.getAddress());
			return zteAddress;
		}

	}

	public String getEndpoint()
	{
		if (zteConnection == null)
			return "";
		else
		{
			String endPoint = String.format("ZTE %s:%s: ", zteConnection.getAddress(), zteConnection.getPort());
			return endPoint;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ZTE Default Properties
	//
	// /////////////////////////////////
	private String originHostName = null;
	private Date originTimeStamp = new Date();
	private int subscriberNumberNAI = 1;

	public String getOriginHostName()
	{
		if (originHostName == null)
		{
			originHostName = HostInfo.getNameOrElseHxC();
		}
		return originHostName;
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
	private int serviceClassCurrent = 0;
	private int languageIDCurrent = 1;
	private boolean postPaidFlag = false;
	private String currency1;
	private String currency2;
	private long accountValue1 = 0L;
	private long accountValue2;
	private long aggregatedBalance1;
	private long aggregatedBalance2;
	private Date supervisionExpiryDate;
	private Date serviceFeeExpiryDate;
	private Date creditClearanceDate;
	private Date serviceRemovalDate;
	private Boolean temporaryBlockedFlag;
	private Long accountPrepaidEmptyLimit1;
	private Long accountPrepaidEmptyLimit2;
	public Map<Integer, OfferInformation> offerMap;


	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Cached GetBalanceAndDate / GetAccountDetails Properties
	//
	// /////////////////////////////////
	public int getServiceClassCurrent() throws ZTEException
	{
		if (!hasQUP && !hasQAB)
			getBalanceAndDateOrAccountDetails();
		return serviceClassCurrent;
	}

	public int getLanguageIDCurrent() throws ZTEException
	{
		if (!hasQUP && !hasQAB)
			getBalanceAndDateOrAccountDetails();
		return languageIDCurrent;
	}

	public boolean getPostPaidFlag() throws ZTEException
	{
		if (!hasQUP && !hasQAB)
			getBalanceAndDateOrAccountDetails();
		return postPaidFlag;
	}

	@Override
	public int getServiceClass()
	{
		try
		{
			return getServiceClassCurrent();
		}
		catch (ZTEException e)
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
			return result < 1 || result > IPhrase.MAX_LANGUAGES ? IPhrase.DEFAULT_LANGUAGE_IDx : result;
		}
		catch (ZTEException e)
		{
			return IPhrase.DEFAULT_LANGUAGE_IDx;
		}
	}

	public String getCurrency1() throws ZTEException
	{
		return currency1;
	}

	public String getCurrency2() throws ZTEException
	{
		return currency2;
	}

	public Long getAccountValue1() throws ZTEException
	{
		if (!hasQUP && !hasQAB)
			getBalanceAndDateOrAccountDetails();
		return accountValue1;
	}

	public Long getAccountValue2() throws ZTEException
	{
		if (!hasQUP && !hasQAB)
			getBalanceAndDateOrAccountDetails();
		return accountValue2;
	}

	public Long getAggregatedBalance1() throws ZTEException
	{
		if (!hasQUP && !hasQAB)
			getBalanceAndDateOrAccountDetails();
		return aggregatedBalance1;
	}

	public Long getAggregatedBalance2() throws ZTEException
	{
		if (!hasQUP && !hasQAB)
			getBalanceAndDateOrAccountDetails();
		return aggregatedBalance2;
	}

	public Date getSupervisionExpiryDate() throws ZTEException
	{
		if (!hasQUP && !hasQAB)
			getBalanceAndDateOrAccountDetails();
		return supervisionExpiryDate;
	}

	public Date getServiceFeeExpiryDate() throws ZTEException
	{
		if (!hasQUP && !hasQAB)
			getBalanceAndDateOrAccountDetails();
		return serviceFeeExpiryDate;
	}

	public Date getCreditClearanceDate() throws ZTEException
	{
		if (!hasQUP && !hasQAB)
			getBalanceAndDateOrAccountDetails();
		return creditClearanceDate;
	}

	public Date getServiceRemovalDate() throws ZTEException
	{
		if (!hasQUP && !hasQAB)
			getBalanceAndDateOrAccountDetails();
		return serviceRemovalDate;
	}

	public Boolean getTemporaryBlockedFlag() throws ZTEException
	{
		if (!hasQUP && !hasQAB)
			getBalanceAndDateOrAccountDetails();
		return temporaryBlockedFlag;
	}

	public Long getAccountPrepaidEmptyLimit1() throws ZTEException
	{
		if (!hasQUP && !hasQAB)
			getBalanceAndDateOrAccountDetails();
		return accountPrepaidEmptyLimit1;
	}

	public Long getAccountPrepaidEmptyLimit2() throws ZTEException
	{
		if (!hasQUP && !hasQAB)
			getBalanceAndDateOrAccountDetails();
		return accountPrepaidEmptyLimit2;
	}

	public boolean isActive() throws ZTEException
	{
		if (!hasQUP && !hasQAB)
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
	private boolean hasQUP = false; // QueryUserProfile
	private boolean hasQAB = false; // Query Account Balance

	// private Map<Integer, UsageCounterUsageThresholdInformation> ucMap = new HashMap<Integer, UsageCounterUsageThresholdInformation>();
	// private Map<Integer, UsageThresholdInformation> utMap = new HashMap<Integer, UsageThresholdInformation>();

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
	private TServiceDto[] serviceOfferings;
	// private CommunityInformationCurrent[] communityInformationCurrent;
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
	// private PamInformationList[] pamInformationList;
	private Integer maxServiceFeePeriod;
	private Integer maxSupervisionPeriod;
	private Date negativeBalanceBarringDate;
	private String accountTimeZone;
	private String cellIdentifier;
	private String locationNumber;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Cached GetAccountDetails Properties
	//
	// /////////////////////////////////
	public Boolean getFirstIVRCallFlag() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return firstIVRCallFlag;
	}

	public Integer getServiceClassOriginal() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return serviceClassOriginal;
	}

	public Date getServiceClassTemporaryExpiryDate() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return serviceClassTemporaryExpiryDate;
	}

	public Integer getUssdEndOfCallNotificationID() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return ussdEndOfCallNotificationID;
	}

	public Integer getAccountGroupID() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return accountGroupID;
	}

	public TServiceDto[] getServiceOfferings() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return serviceOfferings;
	}

	public Boolean getAccountActivatedFlag() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return accountActivatedFlag;
	}

	public Date getActivationDate() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return activationDate;
	}

	public Boolean getMasterSubscriberFlag() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return masterSubscriberFlag;
	}

	public String getMasterAccountNumber() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return masterAccountNumber;
	}

	public Date getRefillUnbarDateTime() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return refillUnbarDateTime;
	}

	public Integer getPromotionAnnouncementCode() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return promotionAnnouncementCode;
	}

	public String getPromotionPlanID() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return promotionPlanID;
	}

	public Date getPromotionStartDate() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return promotionStartDate;
	}

	public Date getPromotionEndDate() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return promotionEndDate;
	}

	public Date getServiceClassChangeUnbarDate() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return serviceClassChangeUnbarDate;
	}

	public Integer getServiceFeePeriod() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return serviceFeePeriod;
	}

	public Integer getSupervisionPeriod() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return supervisionPeriod;
	}

	public Integer getServiceRemovalPeriod() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return serviceRemovalPeriod;
	}

	public Integer getCreditClearancePeriod() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return creditClearancePeriod;
	}

	public Integer getAccountHomeRegion() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return accountHomeRegion;
	}

	public String getPinCode() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return pinCode;
	}

	public Integer getMaxServiceFeePeriod() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return maxServiceFeePeriod;
	}

	public Integer getMaxSupervisionPeriod() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return maxSupervisionPeriod;
	}

	public Date getNegativeBalanceBarringDate() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return negativeBalanceBarringDate;
	}

	public String getAccountTimeZone() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return accountTimeZone;
	}

	public String getCellIdentifier() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return cellIdentifier;
	}

	public String getLocationNumber() throws ZTEException
	{
		if (!hasQAB)
			getAccountDetails();
		return locationNumber;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Subscriber(String msisdn, IZTEConnector zteConnector, ILogger logger, ITransaction transaction)
	{
		this.zteConnector = zteConnector;
		this.zteConnection = zteConnector.getConnection(null);
		this.logger = logger;
		this.transaction = transaction;

		ZTEConnectorConfig config = (ZTEConnectorConfig) ((IPlugin) zteConnector).getConfiguration();
		this.currency1 = config.getDefaultCurrency();
		this.originHostName = HostInfo.getNameOrElseHxC();
		this.subscriberNumberNAI = config.getDefaultSubscriberNumberNAI();
		this.msisdn = getIntNumber(msisdn);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Account Details
	//
	// /////////////////////////////////

	public void getAccountDetails() throws ZTEException
	{
		if (hasQUP)
			return;

		for (int retry = 1; retry <= maxRetries; retry++)
		{
			try
			{
				// Perform ZTE SOAP Call
				track(this, "Executing QueryUserProfile for %s", getInternationalNumber());
				TQueryUserProfileReq request = new TQueryUserProfileReq();
				request.setMSISDN(getInternationalNumber());
				TQueryUserProfileRsp result = zteConnection.getAccountDetails(request);
				TQueryUserProfileRsp member = result;
				checkResponse(member.hashCode(), "QueryUserProfile");

				if (result == null)
					throw new ZTEException(ZTEException.INTERNAL_SERVER_ERROR);
				checkResponse(result.hashCode(), "QueryUserProfile");

				accountValue1 = result.getBalance() * (-1L);

				postPaidFlag = result.getPostPaidFlag().compareTo("Y") == 0;

				hasQUP = true;
				return;
			}
			catch (ZTEException ex)
			{
				if (ex.getResponseCode() == ZTEException.SUBSCRIBER_FAILURE || ex.getResponseCode() == ZTEException.AUTHORIZATION_FAILURE || //
						ex.getResponseCode() == ZTEException.INTERNAL_SERVER_ERROR || ex.getResponseCode() == ZTEException.TIMEOUT)
				{
					hasQUP = true;
					throw (ex);
				}
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
	public void getBalanceAndDate() throws ZTEException
	{
		if (hasQAB)
			return;

		for (int retry = 1; retry <= maxRetries; retry++)
		{
			try
			{
				track(this, "Executing QueryAcctBalBO for %s", getInternationalNumber());
				TQueryAcctBalBO request = new TQueryAcctBalBO();
				request.setMSISDN(getInternationalNumber());
				TQueryAcctBalBOResponse result = zteConnection.getBalanceAndDate(request);
				if (result == null)
					throw new ZTEException(ZTEException.INTERNAL_SERVER_ERROR);
				checkResponse(result.hashCode(), "QueryAcctBalBO");

				setDedicatedAccounts(result.getBalDtoList());

				hasQAB = true;
				return;
			}
			catch (ZTEException ex)
			{
				if (ex.getResponseCode() == ZTEException.SUBSCRIBER_FAILURE || ex.getResponseCode() == ZTEException.AUTHORIZATION_FAILURE || //
						ex.getResponseCode() == ZTEException.INTERNAL_SERVER_ERROR || ex.getResponseCode() == ZTEException.TIMEOUT)
				{
					hasQAB = true;
					throw (ex);
				}
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
	private Map<Integer, TBalDto> daMap;

	private void setDedicatedAccounts(List<TArrayOfBalDtoQryAcctBal> dedicatedAccountList)
	{
		if (daMap == null)
			daMap = new HashMap<Integer, TBalDto>();

		if (dedicatedAccountList == null)
			return;

		int index = 0;
		int daNum = 1;
		for (index = 0; index < dedicatedAccountList.size();)
		{
			TBalDtoForQryAcctBal daTemp = dedicatedAccountList.get(index).getBalDto();
			if (daTemp.getAcctResID().compareTo("1") == 0)
				daNum = 0;

			TBalDto da = daMap.get(daNum);
			if (da == null)
				da = new TBalDto();
			da.setAcctResID(daTemp.getAcctResID());
			da.setAcctResName(daTemp.getAcctResName());
			da.setBalance(daTemp.getBalance());
			da.setBalID(daTemp.getBalID());
			da.setBalType(String.valueOf(daTemp.getBalType()));
			da.setEffDate(daTemp.getEffDate());
			da.setExpDate(daTemp.getExpDate());
			da.setUpdateDate(daTemp.getUpdateDate());
			daMap.put(daNum, da);
			daNum++;
			index++;

			if (daTemp.getAcctResID().equals("1"))
			{
				String accountBal= daTemp.getBalance();
				accountValue1 = Long.valueOf(accountBal) * (-1L);

				if (da.getExpDate()!= null && !da.getExpDate().equals(null))
				{
					GregorianCalendar gregorianCalendar = da.getExpDate().toGregorianCalendar();
					supervisionExpiryDate = gregorianCalendar.getTime();
					serviceFeeExpiryDate = gregorianCalendar.getTime();
					creditClearanceDate = gregorianCalendar.getTime();
					serviceRemovalDate = gregorianCalendar.getTime();
				}
				else 
				{
					Date tmpDate = new Date(0);
					supervisionExpiryDate = tmpDate;
					serviceFeeExpiryDate = tmpDate;
					creditClearanceDate = tmpDate;
					serviceRemovalDate = tmpDate;
				}
			}
		}

	}

	public TBalDto getDedicatedAccount(int dedicatedAccountID) throws ZTEException
	{
		if (!hasQUP)
			getBalanceAndDate();
		return daMap.get(dedicatedAccountID);
	}

	public boolean hasDecicatedAccount(int dedicatedAccountID) throws ZTEException
	{
		if (!hasQUP)
			getBalanceAndDate();
		return daMap.containsKey(dedicatedAccountID);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Offers
	//
	// /////////////////////////////////


	public OfferInformation getAccountOffer(int offerID) throws ZTEException
	{
		return getOffer(offerID, OFFERTYPE_ACCOUNT);
	}

	public OfferInformation getSharedOffer(int offerID) throws ZTEException
	{
		return getOffer(offerID, OFFERTYPE_SHARED);
	}

	public OfferInformation getProviderOffer(int offerID) throws ZTEException
	{
		return getOffer(offerID, OFFERTYPE_PROVIDER);
	}

	private OfferInformation getOffer(int offerID, int offerType) throws ZTEException
	{
		if (offerMap != null && offerMap.containsKey(offerID))
			return offerMap.get(offerID);
		
		getOffer(offerID);

		if (offerMap != null && offerMap.containsKey(offerID))
			return offerMap.get(offerID);
		else 
			return null;
	}

	public boolean hasAccountOffer(int offerID) throws ZTEException
	{
		return hasOffer(offerID, OFFERTYPE_ACCOUNT);
	}

	public boolean hasSharedOffer(int offerID) throws ZTEException
	{
		return hasOffer(offerID, OFFERTYPE_SHARED);
	}

	public boolean hasProviderOffer(int offerID) throws ZTEException
	{
		return hasOffer(offerID, OFFERTYPE_PROVIDER);
	}

	private boolean hasOffer(int offerID, int offerType) throws ZTEException
	{
		OfferInformation offer = getOffer(offerID, offerType);
		return offer.offerState == 1;
	}

	public OfferInformation getOffer(int offerID) throws ZTEException
	{
		if (offerMap != null && offerMap.containsKey(offerID))
			return offerMap.get(offerID);

		OfferInformation[] offers = new OfferInformation[1];

		for (int retry = 1; retry <= maxRetries; retry++)
		{
			try
			{
				track(this, "Executing QryProdStateReq for %s", getInternationalNumber());
				TQryProdStateReq request = new TQryProdStateReq();
				request.setMSISDN(this.getInternationalNumber());
				request.setUserPwd("");
				TServiceDto1 serviceDtoList = new TServiceDto1();
				serviceDtoList.setServiceCode("16");
				request.setServiceDtoList(serviceDtoList);

				TQryProdStateRes result = zteConnection.getOffers(request);

				if (result != null)
				{
					checkResponse(result.hashCode(), "QryProdStateReq");
					if (result.getServiceDtoList() != null)
					{
						String tmp = result.getServiceDtoList().getServiceCode();
						offers[0] = new OfferInformation();
						offers[0].offerID = Integer.parseInt(tmp);
						offers[0].offerState = (int) result.getServiceDtoList().getState();
						setOffers(offers);
					}
				}
				return offers[0];
			}
			catch (ZTEException ex)
			{
				fatal(this, ex);
				testCanBeRetried(retry, ex);
			}
		}
		return offers[0];
	}

	private void setOffers(OfferInformation[] offers)
	{
		if (offerMap == null)
			this.offerMap = new HashMap<Integer, OfferInformation>();
		for (OfferInformation offer : offers)
		{
			if (offerMap.containsKey(offer.offerID))
			{
				offerMap.remove(offer.offerID);
				offerMap.put(offer.offerID, offer);
			}
			else
				offerMap.put(offer.offerID, offer);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Update Accounts
	//
	// /////////////////////////////////
	public IResponseHeader updateAccounts(Long mainValueRelative, AccountUpdate... dedicated) throws ZTEException
	{
		track(this, "Executing DeductFeeRequest for %s", getInternationalNumber());

		if (mainValueRelative == null && (dedicated == null || dedicated.length == 0))
			return null;

		// Prepare the call
		final TDeductFeeRequest request = new TDeductFeeRequest();
		request.setMSISDN(getInternationalNumber());
		request.setCharge(String.valueOf(-mainValueRelative));

		TDeductFeeResponse result = zteConnection.updateBalanceAndDate(request);
		if (result == null)
			throw new ZTEException(ZTEException.INTERNAL_SERVER_ERROR);

		if (daMap == null)
			daMap = new HashMap<Integer, TBalDto>();
		
		TBalDto da = daMap.get(0);
		if (da == null)
		{
			da = new TBalDto();
			da.setBalance(result.getAfterBalance());
			da.setAcctResID("1");
			da.setExpDate(result.getExpireDate());
			daMap.put(0, da);
		}

		if (!daMap.equals(null) && da.getAcctResID().equals("1"))
		{
			da.setBalance(result.getAfterBalance());
		}

		// Test the response
		checkResponse(result.hashCode(), "DeductFeeRequest");

		// Update Cached information
		this.accountValue1 = Long.parseLong(result.getAfterBalance());

		// setDedicatedAccounts(response.member.dedicatedAccountChangeInformation);

		// No Reversal in ZTE
		/*
		 * Reversal reversal = new Reversal() {
		 * 
		 * @Override public void reverse() throws Exception { warn(this, "Reversing UpdateBalanceAndDate for %s", request.member.subscriberNumber);
		 * 
		 * if (request.member.adjustmentAmountRelative != null) request.member.adjustmentAmountRelative = -request.member.adjustmentAmountRelative;
		 * 
		 * DedicatedAccountUpdateInformation[] daUpdates = request.member.dedicatedAccountUpdateInformation; if (daUpdates != null) { for (DedicatedAccountUpdateInformation daUpdate : daUpdates) { if
		 * (daUpdate.adjustmentAmountRelative != null) daUpdate.adjustmentAmountRelative = -daUpdate.adjustmentAmountRelative; if (daUpdate.dedicatedAccountValueNew != null) throw new
		 * ZTEException(999); } }
		 * 
		 * // Execute the Call UpdateBalanceAndDateResponse response2 = zteConnection.updateBalanceAndDate(request);
		 * 
		 * // Test the response checkResponse(response2.member.responseCode, "UpdateBalanceAndDateResponse");
		 * 
		 * // Update Cached information Subscriber.this.accountValue1 = response2.member.accountValue1; Subscriber.this.accountValue2 = response2.member.accountValue2;
		 * 
		 * // Update Cached DA Information setDedicatedAccounts(response.member.dedicatedAccountChangeInformation);
		 * 
		 * }
		 * 
		 * }; transaction.addReversal(reversal);
		 */
		return null;

	}

	//////////////////////////////////////////////////////////////////////////////////////
	//
	// Query FAF List
	//
	// /////////////////////////////////
	public FafInformation[] getFafList() throws ZTEException
	{
		List<TFellowISDNLogDto> fellowISDNLog;
		FafInformation[] fafList;
		List<FafInformation> fafListArray = new ArrayList<FafInformation>();

		for (int retry = 1; retry <= maxRetries; retry++)
		{
			try
			{
				track(this, "Executing QueryFellowISDNLogBO for %s", getInternationalNumber());
				TQueryFellowISDNLogBO request = new TQueryFellowISDNLogBO();
				request.setMSISDN(getInternationalNumber());

				TQueryFellowISDNLogBOResponse result = zteConnection.getFaFList(request);

				if (result != null && result.getFellowISDNLogDtoList() != null)
				{
					fellowISDNLog = result.getFellowISDNLogDtoList();
					for (int i = 0; i < fellowISDNLog.size(); i++)
					{
						FafInformation fafItem = new FafInformation();
						fafItem.fafNumber = fellowISDNLog.get(i).getFellowISDN();
						fafItem.startDate = new Date(fellowISDNLog.get(i).getUpdateDate().toGregorianCalendar().getTimeInMillis());
						fafListArray.add(fafItem);
					}
				}
				fafList = fafListArray.toArray(new FafInformation[fafListArray.size()]);
				return fafList;
			}
			catch (ZTEException ex)
			{
				fatal(this, ex);
				testCanBeRetried(retry, ex);
			}
		}
		fafList = new FafInformation[0];
		return fafList;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ADD FAF List
	//
	// /////////////////////////////////
	public boolean addFafList(FafInformation fafInfo) throws ZTEException
	{

		for (int retry = 1; retry <= maxRetries; retry++)
		{
			try
			{
				track(this, "Executing AddFellowISDNBO for %s", getInternationalNumber());
				TAddFellowISDNBO request = new TAddFellowISDNBO();

				request.setMSISDN(this.getInternationalNumber());
				request.setUserPwd("");
				request.setFellowISDN(getIntNumber(fafInfo.fafNumber));
				request.setFellowType("16");
				request.setEffType(2l);
				request.setEffDate(null);
				request.setExpDate(null);

				TResponseBO result = zteConnection.AddFaFList(request);

				if (result != null && result.getResponseCode().compareTo("0") == 0)
					return true;
				else
					return false;
			}
			catch (ZTEException ex)
			{
				fatal(this, ex);
				testCanBeRetried(retry, ex);
			}
		}
		return false;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ADD FAF List
	//
	// /////////////////////////////////
	public boolean delFafList(FafInformation fafInfo) throws ZTEException
	{
		for (int retry = 1; retry <= maxRetries; retry++)
		{
			try
			{
				track(this, "Executing DelFellowISDNBO for %s", getInternationalNumber());
				TDelFellowISDNBO request = new TDelFellowISDNBO();

				request.setMSISDN(this.getInternationalNumber());
				request.setUserPwd("");
				request.setFellowISDN(fafInfo.fafNumber);
				request.setFellowType("16");

				TResponseBO result = zteConnection.deleteFaFList(request);

				if (result != null && result.getResponseCode().compareTo("0") == 0)
					return true;
				else
					return false;
			}
			catch (ZTEException ex)
			{
				fatal(this, ex);
				testCanBeRetried(retry, ex);
			}
		}
		return false;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// MOD FAF List
	//
	// /////////////////////////////////
	public boolean replaceFafList(FafInformation fafOldInfo, FafInformation fafNewInfo) throws ZTEException
	{
		for (int retry = 1; retry <= maxRetries; retry++)
		{
			try
			{
				track(this, "Executing ModFellowISDNBO for %s", getInternationalNumber());
				TModFellowISDNBO request = new TModFellowISDNBO();
				request.setMSISDN(this.getInternationalNumber());
				request.setFellowISDN(fafOldInfo.fafNumber);
				request.setNewFellowISDN(fafNewInfo.fafNumber);
				request.setFellowType("16");

				TResponseBO result = zteConnection.updateFaFList(request);

				if (result != null && result.getResponseCode().compareTo("0") == 0)
					return true;
				else
					return false;
			}
			catch (ZTEException ex)
			{
				fatal(this, ex);
				testCanBeRetried(retry, ex);
			}
		}
		return false;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Update Offer
	//
	// /////////////////////////////////
	public void updateAccountOffer(int offerID, Date startDateTime, Integer startDateTimeRelative, //
			Date expiryDateTime, Integer expiryDateTimeRelative) throws ZTEException
	{
		updateOffer(offerID, OFFERTYPE_ACCOUNT, startDateTime, startDateTimeRelative, //
				expiryDateTime, expiryDateTimeRelative, null);
	}

	/*
	 * public void updateSharedOffer(int offerID, Date startDateTime, Integer startDateTimeRelative, // Date expiryDateTime, Integer expiryDateTimeRelative) throws ZTEException { updateOffer(offerID,
	 * OFFERTYPE_SHARED, startDateTime, startDateTimeRelative, // expiryDateTime, expiryDateTimeRelative, null); }
	 * 
	 * public void updateProviderOffer(int offerID, Date startDateTime, Integer startDateTimeRelative, // Date expiryDateTime, Integer expiryDateTimeRelative, Subscriber provider) throws ZTEException
	 * { updateOffer(offerID, OFFERTYPE_PROVIDER, startDateTime, startDateTimeRelative, // expiryDateTime, expiryDateTimeRelative, provider); }
	 */
	private void updateOffer(int offerID, int offerType, Date startDateTime, Integer startDateTimeRelative, //
			Date expiryDateTime, Integer expiryDateTimeRelative, Subscriber provider) throws ZTEException
	{
		track(this, "Update Offer %d for %s", offerID, getInternationalNumber());

		final OfferInformation originalOffer = getOffer(offerID, offerType);
		final TSetServiceReq request = new TSetServiceReq();
		final TServiceDto3 serviceDtoList = new TServiceDto3();

		serviceDtoList.setServiceCode(Integer.toString(offerID));
		serviceDtoList.setAction(1L);
		request.setMSISDN(this.getInternationalNumber());
		request.setServiceDtoList(serviceDtoList);

		TResponseBO response = zteConnection.updateOffer(request);

		// Test the response
		checkResponse(response.hashCode(), "UpdateOffer");

		// Update cached information
		if (offerMap == null)
			offerMap = new HashMap<Integer, OfferInformation>();
			
		if (offerMap != null)
		{
			OfferInformation offer = new OfferInformation();
			offer.offerID = offerID;
			offer.expiryDate = new Date();
			offer.offerState = 1;
			offerMap.put(offerID, offer);
		}

//		// Create Reversal
//		Reversal reversal = new Reversal()
//		{
//			@Override
//			public void reverse() throws Exception
//			{
//				if (origionalOffer == null)
//				{
//					//warn(this, "Reversing UpdateOffer for %s with offerID = %s", request.getMSISDN(), serviceDtoList.getServiceCode());
//
//					// final DeleteOfferRequest delete = new DeleteOfferRequest();
//					// updateHeader(delete.member);
//					// delete.member.setOfferID(request.member.offerID);
//					// DeleteOfferResponse response = zteConnection.deleteOffer(delete);
//
//					// Test the response
//					//checkResponse(response.hashCode(), "DeleteOffer");
//
//					// Update cached information
//					if (offerMap != null)
//					{
//						offerMap.remove(offerID);
//					}
//				}
//			}
//		};
//		transaction.addReversal(reversal);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Delete Offer
	//
	// /////////////////////////////////
	/*
	 * public void deleteAccountOffer(int offerID) throws ZTEException { deleteOffer(offerID, OFFERTYPE_ACCOUNT); }
	 * 
	 * public void deleteSharedOffer(int offerID) throws ZTEException { deleteOffer(offerID, OFFERTYPE_SHARED); }
	 * 
	 * public void deleteProviderOffer(int offerID) throws ZTEException { deleteOffer(offerID, OFFERTYPE_PROVIDER); }
	 * 
	 * private void deleteOffer(int offerID, int offerType) throws ZTEException { track(this, "Delete Offer %d for %s", offerID, getInternationalNumber());
	 * 
	 * final OfferInformation origionalOffer = getOffer(offerID, offerType); if (origionalOffer == null) { warn(this, "DeleteOffer for non-existing Offer %d for %s. Ignored", offerID, msisdn); return;
	 * }
	 * 
	 * final DeleteOfferRequest request = new DeleteOfferRequest(); updateHeader(request.member); request.member.setOfferID(offerID);
	 * 
	 * final DeleteOfferResponse response = zteConnection.deleteOffer(request);
	 * 
	 * // Test the response checkResponse(response.member.responseCode, "DeleteOffer");
	 * 
	 * // Update cached information if (offerMap != null) { offerMap.remove(request.member.getOfferID()); }
	 * 
	 * // Create Reversal Reversal reversal = new Reversal() {
	 * 
	 * @Override public void reverse() throws Exception { warn(this, "Reversing DeleteOffer for %s with offerID = %d", request.member.subscriberNumber, request.member.offerID);
	 * 
	 * final UpdateOfferRequest recreate = new UpdateOfferRequest(); updateHeader(recreate.member); recreate.member.setOfferID(response.member.getOfferID());
	 * recreate.member.setOfferType(response.member.getOfferType()); recreate.member.setExpiryDate(response.member.getExpiryDate());
	 * recreate.member.setExpiryDateTime(response.member.getExpiryDateTime()); recreate.member.setOfferProviderID(response.member.getOfferProviderID());
	 * recreate.member.setPamServiceID(response.member.getPamServiceID()); recreate.member.setProductID(origionalOffer.productID); UpdateOfferResponse response2 = zteConnection.updateOffer(recreate);
	 * 
	 * // Test the response checkResponse(response2.member.responseCode, "UpdateOffer");
	 * 
	 * // Update cached information if (offerMap != null) { OfferInformation newOffer = new OfferInformation(); newOffer.offerID = response2.member.offerID; newOffer.startDate =
	 * response2.member.startDate; newOffer.expiryDate = response2.member.expiryDate; newOffer.startDateTime = response2.member.startDateTime; newOffer.expiryDateTime =
	 * response2.member.expiryDateTime; newOffer.pamServiceID = response2.member.pamServiceID; newOffer.offerType = response2.member.offerType; newOffer.offerState = response2.member.offerState;
	 * newOffer.offerProviderID = response2.member.offerProviderID; newOffer.productID = response2.member.productID; offerMap.put(newOffer.offerID, newOffer); }
	 * 
	 * } }; transaction.addReversal(reversal);
	 * 
	 * }
	 */
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Update Account Details
	//
	// /////////////////////////////////
	/*
	 * public void updateAccountDetails(Integer languageID, Object spare1, Object spare2, Object spare3, Object spare4, Object spare5, Object spare6) throws ZTEException { track(this,
	 * "Update Account Details for %s", getInternationalNumber());
	 * 
	 * final int oldLanguageID = getLanguageID(); final UpdateAccountDetailsRequest request = new UpdateAccountDetailsRequest(); updateHeader(request.member);
	 * request.member.setLanguageIDNew(languageID);
	 * 
	 * UpdateAccountDetailsResponse response = zteConnection.updateAccountDetails(request);
	 * 
	 * // Test the response checkResponse(response.member.getResponseCode(), " UpdateAccountDetails");
	 * 
	 * // Update cached information this.languageIDCurrent = languageID;
	 * 
	 * // Create Reversal Reversal reversal = new Reversal() {
	 * 
	 * @Override public void reverse() throws Exception { warn(this, "Reversing LanguageID Change for %s", request.member.getSubscriberNumber());
	 * 
	 * request.member.setLanguageIDCurrent(oldLanguageID); request.member.setLanguageIDNew(oldLanguageID); UpdateAccountDetailsResponse response = zteConnection.updateAccountDetails(request);
	 * 
	 * // Test the response checkResponse(response.member.getResponseCode(), "UpdateAccountDetails");
	 * 
	 * // Update cached information Subscriber.this.languageIDCurrent = oldLanguageID; } }; transaction.addReversal(reversal);
	 * 
	 * }
	 */
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Refill
	//
	// /////////////////////////////////
	/*
	 * public RefillResponse refillAccount(String refillProfileID, long transactionAmount) throws ZTEException { track(this, "Execute Refill for %s", getInternationalNumber());
	 * 
	 * final RefillRequest request = new RefillRequest(); updateHeader(request.member); request.member.transactionAmount = transactionAmount; request.member.transactionCurrency = getCurrency1();
	 * request.member.refillProfileID = refillProfileID; request.member.requestRefillAccountBeforeFlag = true; request.member.requestRefillAccountAfterFlag = true;
	 * 
	 * RefillResponse response = zteConnection.refill(request);
	 * 
	 * // Test the response checkResponse(response.member.getResponseCode(), " Refill");
	 * 
	 * return response; }
	 */
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Set Usage Threshold
	//
	// /////////////////////////////////
	/*
	 * public void setUsageThreshold(final int thresholdID, Long newValue, Long newMonetartValue, final Subscriber associatedParty) throws ZTEException { track(this, "Set UT %d for %s", thresholdID,
	 * getInternationalNumber());
	 * 
	 * // Get UsageCounters and Thresholds to be able to rollback if (!hasQS) getUsageThresholdsAndCounters();
	 * 
	 * // Get original UT UsageThresholdInformation ut = utMap.get(thresholdID); final boolean existed = ut != null; final Long oldUsageThresholdValue = ut == null ? null : ut.usageThresholdValue;
	 * final Long oldUsageThresholdMonetaryValue1 = ut == null ? null : ut.usageThresholdMonetaryValue1; final String oldAssociatedPartyID = ut == null ? null : ut.associatedPartyID;
	 * 
	 * // Create Request final UpdateUsageThresholdsAndCountersRequest request = new UpdateUsageThresholdsAndCountersRequest(); updateHeader(request.member);
	 * request.member.setTransactionCurrency(getCurrency1()); UsageThresholdUpdateInformation usageThresholdUpdateInformation = new UsageThresholdUpdateInformation();
	 * usageThresholdUpdateInformation.usageThresholdID = thresholdID; usageThresholdUpdateInformation.usageThresholdValueNew = newValue; usageThresholdUpdateInformation.usageThresholdMonetaryValueNew
	 * = newMonetartValue; usageThresholdUpdateInformation.associatedPartyID = associatedParty == null ? null : associatedParty.getNaiNumber(); request.member.usageThresholdUpdateInformation = new
	 * UsageThresholdUpdateInformation[] { usageThresholdUpdateInformation };
	 * 
	 * // Execute the Request UpdateUsageThresholdsAndCountersResponse response = zteConnection.updateUsageThresholdsAndCounters(request);
	 * 
	 * // Test the response checkResponse(response.member.getResponseCode(), "UpdateUsageThresholdsAndCountersResponse");
	 * 
	 * // Cache the Result cacheUsageCounterUsageThresholdInformation(response.member.usageCounterUsageThresholdInformation);
	 * 
	 * // Create Reversal Reversal reversal = new Reversal() {
	 * 
	 * @Override public void reverse() throws Exception { warn(this, "Reversing UpdateUsageThresholdsAndCounters for %s", request.member.getSubscriberNumber());
	 * 
	 * if (!existed) { // Delete the Threshold final DeleteUsageThresholdsRequest request = new DeleteUsageThresholdsRequest(); updateHeader(request.member); UsageThresholds usageThreshold = new
	 * UsageThresholds(); usageThreshold.usageThresholdID = thresholdID; usageThreshold.associatedPartyID = associatedParty.getNaiNumber(); request.member.usageThresholds = new UsageThresholds[] {
	 * usageThreshold }; DeleteUsageThresholdsResponse response = zteConnection.deleteUsageThresholds(request); checkResponse(response.member.getResponseCode(), "DeleteUsageThresholds");
	 * cacheUsageThresholdInformation(response.member.usageThresholdInformation); utMap.remove(thresholdID); } else { final UpdateUsageThresholdsAndCountersRequest request = new
	 * UpdateUsageThresholdsAndCountersRequest(); updateHeader(request.member); request.member.transactionCurrency = Subscriber.this.getCurrency1(); UsageThresholdUpdateInformation
	 * usageThresholdUpdateInformation = new UsageThresholdUpdateInformation(); usageThresholdUpdateInformation.usageThresholdID = thresholdID; usageThresholdUpdateInformation.usageThresholdValueNew =
	 * oldUsageThresholdValue; usageThresholdUpdateInformation.usageThresholdMonetaryValueNew = oldUsageThresholdMonetaryValue1; usageThresholdUpdateInformation.associatedPartyID =
	 * oldAssociatedPartyID; request.member.usageThresholdUpdateInformation = new UsageThresholdUpdateInformation[] { usageThresholdUpdateInformation }; UpdateUsageThresholdsAndCountersResponse
	 * response = zteConnection.updateUsageThresholdsAndCounters(request); checkResponse(response.member.getResponseCode(), "UpdateUsageThresholdsAndCounters");
	 * cacheUsageCounterUsageThresholdInformation(response.member.usageCounterUsageThresholdInformation); } } }; transaction.addReversal(reversal); }
	 */
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Clear Usage Counters
	//
	// /////////////////////////////////
	/*
	 * public void clearUsageCounters(Subscriber associatedParty, Integer... countersToClear) throws ZTEException { track(this, "Clear Usage Counters for %s", getInternationalNumber());
	 * 
	 * // Get UsageCounters and Thresholds to be able to rollback if (!hasQS) getUsageThresholdsAndCounters();
	 * 
	 * final UpdateUsageThresholdsAndCountersRequest request = new UpdateUsageThresholdsAndCountersRequest(); updateHeader(request.member); request.member.setTransactionCurrency(getCurrency1());
	 * 
	 * // Add Parameters request.member.transactionCurrency = this.getCurrency1(); int count = countersToClear == null ? 0 : countersToClear.length; request.member.usageCounterUpdateInformation = new
	 * UsageCounterUpdateInformation[count]; for (int index = 0; index < count; index++) { UsageCounterUpdateInformation usageCounterUpdateInformation = new UsageCounterUpdateInformation();
	 * usageCounterUpdateInformation.usageCounterID = countersToClear[index]; usageCounterUpdateInformation.usageCounterValueNew = 0L; usageCounterUpdateInformation.adjustmentUsageCounterValueRelative
	 * = null; usageCounterUpdateInformation.usageCounterMonetaryValueNew = null; usageCounterUpdateInformation.adjustmentUsageCounterMonetaryValueRelative = null;
	 * usageCounterUpdateInformation.associatedPartyID = associatedParty == null ? null : associatedParty.getNaiNumber(); request.member.usageCounterUpdateInformation[index] =
	 * usageCounterUpdateInformation; }
	 * 
	 * // Create Reversal Request final UpdateUsageThresholdsAndCountersRequest reverseRequest = new UpdateUsageThresholdsAndCountersRequest(); updateHeader(reverseRequest.member);
	 * reverseRequest.member.setTransactionCurrency(getCurrency1()); count = 0; UsageCounterUpdateInformation[] uci = new UsageCounterUpdateInformation[countersToClear != null ? countersToClear.length
	 * : 0]; for (int index = 0; index < (countersToClear != null ? countersToClear.length : 0); index++) { UsageCounterUsageThresholdInformation uc = ucMap.get(countersToClear[index]); if (uc ==
	 * null) continue; UsageCounterUpdateInformation ucu = new UsageCounterUpdateInformation(); ucu.usageCounterID = uc.usageCounterID; ucu.usageCounterValueNew = uc.usageCounterValue;
	 * ucu.usageCounterMonetaryValueNew = uc.usageCounterMonetaryValue1; ucu.associatedPartyID = uc.associatedPartyID; ucu.productID = uc.productID; uci[count++] = ucu; }
	 * reverseRequest.member.setUsageCounterUpdateInformation(Arrays.copyOf(uci, count));
	 * 
	 * // Execute Request UpdateUsageThresholdsAndCountersResponse response = zteConnection.updateUsageThresholdsAndCounters(request);
	 * 
	 * // Test the response checkResponse(response.member.getResponseCode(), "UpdateUsageThresholdsAndCountersResponse");
	 * 
	 * // Cache the Results cacheUsageCounterUsageThresholdInformation(response.member.usageCounterUsageThresholdInformation);
	 * 
	 * // Create Reversal Reversal reversal = new Reversal() {
	 * 
	 * @Override public void reverse() throws Exception { warn(this, "Reversing UpdateUsageThresholdsAndCounters for %s", request.member.getSubscriberNumber());
	 * UpdateUsageThresholdsAndCountersResponse response = zteConnection.updateUsageThresholdsAndCounters(reverseRequest); checkResponse(response.member.responseCode,
	 * "UpdateUsageThresholdsAndCounters"); cacheUsageCounterUsageThresholdInformation(response.member.usageCounterUsageThresholdInformation); } }; transaction.addReversal(reversal); }
	 */
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Query Usage Threshold and Counters
	//
	// /////////////////////////////////
	/*
	 * public UsageCounterUsageThresholdInformation[] getUsageThresholdsAndCounters() throws ZTEException { track(this, "Query UC/UT for %s", getInternationalNumber());
	 * 
	 * if (hasQS) return ucMap.values().toArray(new UsageCounterUsageThresholdInformation[ucMap.size()]);
	 * 
	 * for (int retry = 1; retry <= maxRetries; retry++) { try { track(this, "Executing GetUsageThresholdsAndCounters for %s", getInternationalNumber()); GetUsageThresholdsAndCountersRequest request =
	 * new GetUsageThresholdsAndCountersRequest(); updateHeader(request.member); GetUsageThresholdsAndCountersResponse result = zteConnection.getUsageThresholdsAndCounters(request);
	 * checkResponse(result.member.responseCode, "GetUsageThresholdsAndCounters");
	 * 
	 * // Update Cached Information cacheUsageCounterUsageThresholdInformation(result.member.usageCounterUsageThresholdInformation); hasQS = true;
	 * 
	 * return result.member.usageCounterUsageThresholdInformation;
	 * 
	 * } catch (ZTEException ex) { fatal(this, ex); testCanBeRetried(retry, ex); }
	 * 
	 * }
	 * 
	 * return null; }
	 * 
	 * private void cacheUsageCounterUsageThresholdInformation(UsageCounterUsageThresholdInformation[] usageCounterUsageThresholdInformation) { if (usageCounterUsageThresholdInformation == null)
	 * return;
	 * 
	 * for (UsageCounterUsageThresholdInformation uc : usageCounterUsageThresholdInformation) { ucMap.put(uc.usageCounterID, uc); cacheUsageThresholdInformation(uc.usageThresholdInformation);
	 * 
	 * }
	 * 
	 * }
	 * 
	 * private void cacheUsageThresholdInformation(UsageThresholdInformation[] usageThresholdInformation) { if (usageThresholdInformation == null) return;
	 * 
	 * for (UsageThresholdInformation ut : usageThresholdInformation) { utMap.put(ut.usageThresholdID, ut); } }
	 */
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Delete Usage Threshold
	//
	// /////////////////////////////////
	/*
	 * public void deleteUsageThresholds(Subscriber associatedParty, Integer... thresholdIDs) throws ZTEException { track(this, "Delete UTs for %s", getInternationalNumber());
	 * 
	 * // Get UsageCounters and Thresholds to be able to rollback if (!hasQS) getUsageThresholdsAndCounters();
	 * 
	 * final DeleteUsageThresholdsRequest request = new DeleteUsageThresholdsRequest(); updateHeader(request.member);
	 * 
	 * // Add Parameters int count = thresholdIDs == null ? 0 : thresholdIDs.length; request.member.usageThresholds = new UsageThresholds[count]; for (int index = 0; index < count; index++) {
	 * UsageThresholds usageThreshold = new UsageThresholds(); usageThreshold.usageThresholdID = thresholdIDs[index]; usageThreshold.associatedPartyID = associatedParty == null ? null :
	 * associatedParty.getNaiNumber(); request.member.usageThresholds[index] = usageThreshold; }
	 * 
	 * // Create Reversal Request final UpdateUsageThresholdsAndCountersRequest reverseRequest = new UpdateUsageThresholdsAndCountersRequest(); updateHeader(reverseRequest.member);
	 * reverseRequest.member.setTransactionCurrency(getCurrency1()); count = 0; UsageThresholdUpdateInformation[] uti = new UsageThresholdUpdateInformation[thresholdIDs != null ? thresholdIDs.length :
	 * 0]; for (int index = 0; index < (thresholdIDs != null ? thresholdIDs.length : 0); index++) { UsageThresholdInformation ut = utMap.get(thresholdIDs[index]); if (ut == null) continue;
	 * UsageThresholdUpdateInformation utu = new UsageThresholdUpdateInformation(); utu.usageThresholdID = ut.usageThresholdID; utu.usageThresholdValueNew = ut.usageThresholdValue;
	 * utu.usageThresholdMonetaryValueNew = ut.usageThresholdMonetaryValue1; utu.associatedPartyID = ut.associatedPartyID; uti[count++] = utu; }
	 * reverseRequest.member.setUsageThresholdUpdateInformation(Arrays.copyOf(uti, count));
	 * 
	 * // Execute the call DeleteUsageThresholdsResponse response = zteConnection.deleteUsageThresholds(request);
	 * 
	 * // Test the response checkResponse(response.member.getResponseCode(), "DeleteUsageThresholds");
	 * 
	 * // Update Cache for (int index = 0; index < (thresholdIDs != null ? thresholdIDs.length : 0); index++) { utMap.remove(thresholdIDs[index]); }
	 * cacheUsageThresholdInformation(response.member.getUsageThresholdInformation());
	 * 
	 * // Create Reversal Reversal reversal = new Reversal() {
	 * 
	 * @Override public void reverse() throws Exception { warn(this, "Reversing DeleteUsageThresholds for %s", request.member.getSubscriberNumber()); UpdateUsageThresholdsAndCountersResponse response
	 * = zteConnection.updateUsageThresholdsAndCounters(reverseRequest); checkResponse(response.member.getResponseCode(), "UpdateUsageThresholdsAndCounters");
	 * cacheUsageCounterUsageThresholdInformation(response.member.usageCounterUsageThresholdInformation); }
	 * 
	 * }; transaction.addReversal(reversal); }
	 * 
	 * // ////////////////////////////////////////////////////////////////////////////////////// // // Install Subscriber // // ///////////////////////////////// public void install(int
	 * serviceClassID) throws ZTEException { track(this, "Install Subscriber %s", getInternationalNumber());
	 * 
	 * final InstallSubscriberRequest request = new InstallSubscriberRequest(); updateHeader(request.member);
	 * 
	 * // Add Parameters request.member.setServiceClassNew(serviceClassID);
	 * 
	 * // Create Reversal Request // Cannot be reversed
	 * 
	 * // Execute the call InstallSubscriberResponse response = zteConnection.installSubscriber(request);
	 * 
	 * // Test the response checkResponse(response.member.getResponseCode(), "InstallSubscriber");
	 * 
	 * // Update Cache }
	 */
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Usage Threshold
	//
	// /////////////////////////////////
	/*
	 * public UsageThresholdInformation getUsageThreshold(int usageThresholdID) throws ZTEException { if (!hasQS) getUsageThresholdsAndCounters(); return utMap.get(usageThresholdID); }
	 */
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Usage Counter
	//
	// /////////////////////////////////
	/*
	 * public UsageCounterUsageThresholdInformation getUsageCounter(int usageCounterID) throws ZTEException { if (!hasQS) getUsageThresholdsAndCounters(); return ucMap.get(usageCounterID); }
	 */
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

	public String getNaiNumber()
	{
		return getSubscriberNumberNAI() == 1 ? getInternationalNumber() : getNationalNumber();
	}

	private void getBalanceAndDateOrAccountDetails() throws ZTEException
	{
		getAccountDetails(); //TODO getBalanceAndDate as backup getAccountDetails
	}

	// Tests if an ZTE Exception contains an response code which can be retried
	protected boolean canBeRetried(ZTEException ex)
	{
		int responseCode = ex.getResponseCode();
		for (int retryableResponseCode : retryableResponseCodes)
		{
			if (responseCode == retryableResponseCode)
				return true;
		}

		return false;
	}

	// Test if an operation which threw an ZTEException, can be retried
	protected void testCanBeRetried(int retry, ZTEException ex) throws ZTEException
	{
		if (retry >= maxRetries || !canBeRetried(ex))
			throw ex;
	}

	protected void checkResponse(int responseCode, String method) throws ZTEException
	{
		if (responseCode == 0)
		{
			warn(this, "%s call returned responseCode %d", method, responseCode);
			if (transaction != null)
				transaction.setResultCode(responseCode);
			throw new ZTEException(responseCode);
		}
	}

	public boolean isInServiceClass(int[] allowedServiceClasses) throws ZTEException
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

		return getInternationalNumber().equalsIgnoreCase(zteConnector.getNumberPlan().getInternationalFormat(msisdn));
	}
	
	private String getIntNumber(String msisdn)
	{
		//remove leading zeroes 
		while (msisdn.indexOf("0") == 0)
		{
			msisdn = msisdn.substring(1, msisdn.length());
		}
		msisdn = zteConnector.getNumberPlan().getInternationalFormat(msisdn);
		return msisdn;
	}

	private void track(Object origin, String message, Object... args)
	{
		if (logger != null)
			logger.debug(origin, getEndpoint() + message, args);
	}

	private void warn(Object origin, String message, Object... args)
	{
		if (logger != null)
			logger.warn(origin, getEndpoint() + message, args);
	}

	private void error(Object origin, String message, Object... args)
	{
		if (logger != null)
			logger.error(origin, getEndpoint() + message, args);
	}

	private void fatal(Object origin, Exception ex)
	{
		if (logger != null)
			logger.error(origin, getEndpoint() + ex.getMessage());
	}

	public Long getAccountBalance(int accountID)
	{
		if (accountID == 0)
			try
			{
				return getAccountValue1();
			}
			catch (ZTEException e)
			{
				e.printStackTrace();
			}
		return 0L;
		/*
		 * DedicatedAccountInformation da = getDedicatedAccount(accountID); if (da == null) return null;
		 * 
		 * return da.dedicatedAccountValue1;
		 */
	}

	public boolean hasPSO(int psoNumber) throws ZTEException
	{
		TServiceDto[] psoList = getServiceOfferings();
		for (TServiceDto pso : psoList)
		{
			if ((pso.getServiceAttrDtoList() != null) && (Integer.getInteger(pso.getServiceCode()) == psoNumber))
				return true;
		}
		return false;
	}

	public Object updateAccounts(Long mainValueRelative, Object... dedicated)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
