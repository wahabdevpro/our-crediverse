package hxc.services.airsim.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import hxc.services.airsim.engine.GetUsageThresholdsAndCounters;
import hxc.services.airsim.protocol.Accumulator;
import hxc.services.airsim.protocol.DedicatedAccount;
import hxc.services.airsim.protocol.Subscriber;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.services.airsim.protocol.TnpThreshold;
import hxc.services.airsim.protocol.TnpThreshold.TnpTriggerTypes;
import hxc.services.airsim.protocol.UsageCounter;
import hxc.services.airsim.protocol.UsageThreshold;
import hxc.services.airsim.protocol.UsageTimer;
import hxc.utils.calendar.DateTime;
import hxc.utils.protocol.acip.PamInformation;
import hxc.utils.protocol.ucip.AccountFlags;
import hxc.utils.protocol.ucip.AccountFlagsAfter;
import hxc.utils.protocol.ucip.AccountFlagsBefore;
import hxc.utils.protocol.ucip.AccumulatorInformation;
import hxc.utils.protocol.ucip.CommunityIdList;
import hxc.utils.protocol.ucip.DedicatedAccountInformation;
import hxc.utils.protocol.ucip.FafInformation;
import hxc.utils.protocol.ucip.OfferInformation;
import hxc.utils.protocol.ucip.OfferInformationList;
import hxc.utils.protocol.ucip.PamInformationList;
import hxc.utils.protocol.ucip.ServiceOfferings;
import hxc.utils.protocol.ucip.UsageAccumulatorInformation;

public class SubscriberEx extends Subscriber implements IUsageHandler
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Map<Integer, OfferEx> offers = new HashMap<Integer, OfferEx>();
	private Map<Integer, DedicatedAccount> dedicatedAccounts = new HashMap<Integer, DedicatedAccount>();
	private Map<Integer, UsageCounter> usageCounters = new HashMap<Integer, UsageCounter>();
	private Map<Integer, UsageThreshold> usageThresholds = new HashMap<Integer, UsageThreshold>();
	private Map<Integer, Accumulator> accumulators = new HashMap<Integer, Accumulator>();
	private Map<String, FafInformation> fafEntries = new HashMap<String, FafInformation>();
	private Map<Integer, ServiceOfferings> serviceOfferings = new HashMap<Integer, ServiceOfferings>();
	private Map<Long, TnpThreshold> tnpThresholds;
	private Map<Integer, PamInformationList> pamEntries = new HashMap<Integer, PamInformationList>();
	private Map<Integer, UsageTimer> usageTimers = new HashMap<Integer, UsageTimer>();
	private List<Integer> communityIDs = new ArrayList<Integer>();
	private transient Random random = new Random();

	// HLR /Map
	private int stateId = 4; // Default: Attached, reachable for paging
	private int domain = 0; // Default: Circuit Switched
	private int mnpStatusId = 0; // Default: Not known to be ported
	private int mobileCountryCode = 655; // RSA
	private int mobileNetworkCode = 1; // Vodacom
	private int locationAreaCode = 161; // Sandton Morningside
	private int cellIdentity = 13352; // -26077654S, 28.067789E
	private Date lastFix = new Date();
	private String imsi = null;
	private String imei = null;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public Map<Integer, OfferEx> getOffers()
	{
		return offers;
	}

	public Map<Integer, DedicatedAccount> getDedicatedAccounts()
	{
		return dedicatedAccounts;
	}

	public Map<Integer, UsageCounter> getUsageCounters()
	{
		return usageCounters;
	}

	public Map<Integer, UsageThreshold> getUsageThresholds()
	{
		return usageThresholds;
	}

	public Map<Integer, Accumulator> getAccumulators()
	{
		return accumulators;
	}

	public Map<String, FafInformation> getFafEntries()
	{
		return fafEntries;
	}

	public Map<Integer, PamInformationList> getPamEntries()
	{
		return pamEntries;
	}

	public Map<Integer, ServiceOfferings> getServiceOfferings()
	{
		return serviceOfferings;
	}

	public int getStateId()
	{
		return stateId;
	}

	public void setStateId(int stateId)
	{
		this.stateId = stateId;
	}

	public int getDomain()
	{
		return domain;
	}

	public void setDomain(int domain)
	{
		this.domain = domain;
	}

	public int getMnpStatusId()
	{
		return mnpStatusId;
	}

	public void setMnpStatusId(int mnpStatusId)
	{
		this.mnpStatusId = mnpStatusId;
	}

	public int getMobileCountryCode()
	{
		return mobileCountryCode;
	}

	public void setMobileCountryCode(int mobileCountryCode)
	{
		this.mobileCountryCode = mobileCountryCode;
		this.lastFix = new Date();
	}

	public int getMobileNetworkCode()
	{
		return mobileNetworkCode;
	}

	public void setMobileNetworkCode(int mobileNetworkCode)
	{
		this.mobileNetworkCode = mobileNetworkCode;
		this.lastFix = new Date();
	}

	public int getLocationAreaCode()
	{
		return locationAreaCode;
	}

	public void setLocationAreaCode(int locationAreaCode)
	{
		this.locationAreaCode = locationAreaCode;
		this.lastFix = new Date();
	}

	public int getCellIdentity()
	{
		return cellIdentity;
	}

	public void setCellIdentity(int cellIdentity)
	{
		this.cellIdentity = cellIdentity;
		this.lastFix = new Date();
	}

	public long getFixAgeInSeconds()
	{
		return (new Date().getTime() - lastFix.getTime()) / 1000;
	}

	public List<Integer> getCommunityIDs()
	{
		return communityIDs;
	}

	public void setCommunityIDs(List<Integer> communityIDs)
	{
		this.communityIDs = communityIDs;
	}

	public String getImsi()
	{
		return imsi;
	}

	public void setImsi(String imsi)
	{
		this.imsi = imsi;
	}

	public String getImei()
	{
		return imei;
	}

	public void setImei(String imei)
	{
		this.imei = imei;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public SubscriberEx()
	{

	}

	public SubscriberEx(String currency, String nationalNumber, String internationalNumber, int languageID, int serviceClass, long accountValue, SubscriberState state,
			Map<Long, TnpThreshold> tnpThresholds)
	{
		this.currency1 = this.currency2 = currency;
		this.nationalNumber = nationalNumber;
		this.internationalNumber = internationalNumber;
		this.setMasterAccountNumber(internationalNumber);
		this.languageIDCurrent = languageID;
		this.serviceClassCurrent = this.serviceClassOriginal = serviceClass;
		this.accountValue1 = this.accountValue2 = accountValue;
		this.tnpThresholds = tnpThresholds;

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long start = cal.getTime().getTime();

		final long oneDay = 24 * 60 * 60 * 1000L;

		switch (state)
		{
			// < Activition Date
			case inActive:
				start += 3 * oneDay;
				break;

			// < Supervision Expiry Date
			case active:
				start += oneDay;
				break;

			// < Service Fee Expiry Date
			case passive:
				start -= oneDay;
				break;

			// < Service Credit Clearance Date
			case grace:
				start -= 3 * oneDay;
				break;

			// < Service Removal Date
			case pool:
				start -= 5 * oneDay;
				break;

			// > Service Removal Date
			case disconnect:
				start -= 7 * oneDay;
				break;
		}

		setActivationDate(new Date(start - 2 * oneDay));
		setSupervisionExpiryDate(new Date(start));
		setServiceFeeExpiryDate(new Date(start + 2 * oneDay));
		setCreditClearanceDate(new Date(start + 4 * oneDay));
		setServiceRemovalDate(new Date(start + 6 * oneDay));
	}

	public SubscriberEx(String nationalNumber, String internationalNumber, SubscriberEx subscriber, Map<Long, TnpThreshold> tnpThresholds)
	{
		super(nationalNumber, internationalNumber, subscriber);
		this.tnpThresholds = tnpThresholds;

		for (OfferEx offer : subscriber.offers.values())
		{
			OfferEx newOffer = new OfferEx(offer);
			offers.put(newOffer.getOfferID(), newOffer);
		}

		for (DedicatedAccount dedicatedAccount : subscriber.dedicatedAccounts.values())
		{
			DedicatedAccount newDedicatedAccount = new DedicatedAccount(dedicatedAccount);
			dedicatedAccounts.put(newDedicatedAccount.getDedicatedAccountID(), newDedicatedAccount);
		}

		for (UsageCounter usageCounter : subscriber.usageCounters.values())
		{
			UsageCounter newUsageCounter = new UsageCounter(usageCounter);
			usageCounters.put(newUsageCounter.getUsageCounterID(), newUsageCounter);
		}

		for (UsageThreshold usageThreshold : subscriber.usageThresholds.values())
		{
			UsageThreshold newUsageThreshold = new UsageThreshold(usageThreshold);
			usageThresholds.put(newUsageThreshold.getUsageThresholdID(), newUsageThreshold);
		}

		for (Accumulator accumulator : subscriber.accumulators.values())
		{
			Accumulator newAccumulator = new Accumulator(accumulator);
			accumulators.put(newAccumulator.getAccumulatorID(), newAccumulator);
		}

		for (FafInformation fafEntry : subscriber.fafEntries.values())
		{
			FafInformation newFafEntry = new FafInformation(fafEntry);
			fafEntries.put(newFafEntry.fafNumber, newFafEntry);
		}

		for (ServiceOfferings serviceOffering : subscriber.serviceOfferings.values())
		{
			ServiceOfferings newServiceOffering = new ServiceOfferings(serviceOffering);
			serviceOfferings.put(newServiceOffering.serviceOfferingID, newServiceOffering);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public boolean hasOffer(int offerID)
	{
		return offers != null && offers.containsKey(offerID);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public OfferInformation[] getOfferInformation(SubscriberEx subscriber)
	{
		OfferInformation[] result = new OfferInformation[offers.size()];

		int index = 0;
		for (OfferEx offer : offers.values())
		{
			OfferInformation item = new OfferInformation();

			item.offerID = offer.getOfferID();
			item.startDate = offer.getStartDate();
			item.expiryDate = offer.getExpiryDate();
			item.startDateTime = offer.getStartDateTime();
			item.expiryDateTime = offer.getExpiryDateTime();
			item.pamServiceID = offer.getPamServiceID();
			item.offerType = offer.getOfferType();
			item.offerState = offer.getOfferState();
			item.offerProviderID = offer.getOfferProviderID();
			item.productID = offer.getProductID();
			item.usageCounterUsageThresholdInformation = GetUsageThresholdsAndCounters.getInformation(subscriber); // offer.getUsageCounterUsageThresholdInformation();
			item.dedicatedAccountInformation = offer.getDedicatedAccountInformation();
			item.attributeInformationList = offer.getAttributeInformationList();

			result[index++] = item;
		}
		return result;
	}

	public OfferInformationList[] getOfferInformationList(SubscriberEx subscriber)
	{
		OfferInformationList[] result = new OfferInformationList[offers.size()];

		int index = 0;
		for (OfferEx offer : offers.values())
		{
			OfferInformationList item = new OfferInformationList();

			item.offerID = offer.getOfferID();
			item.startDate = offer.getStartDate();
			item.expiryDate = offer.getExpiryDate();
			item.startDateTime = offer.getStartDateTime();
			item.expiryDateTime = offer.getExpiryDateTime();
			item.pamServiceID = offer.getPamServiceID();
			item.offerType = offer.getOfferType();
			item.offerState = offer.getOfferState();
			item.offerProviderID = offer.getOfferProviderID();
			item.productID = offer.getProductID();
			item.usageCounterUsageThresholdInformation = GetUsageThresholdsAndCounters.getInformation(subscriber);
			item.dedicatedAccountInformation = offer.getDedicatedAccountInformation();
			item.attributeInformationList = offer.getAttributeInformationList();

			result[index++] = item;
		}
		return result;
	}

	public PamInformationList[] getPamInformationList()
	{
		PamInformationList[] result = new PamInformationList[pamEntries.size()];

		int index = 0;
		for (PamInformationList pam : pamEntries.values())
		{
			PamInformationList item = new PamInformationList();

			item.pamServiceID = pam.pamServiceID;
			item.pamClassID = pam.pamClassID;
			item.scheduleID = pam.scheduleID;
			item.currentPamPeriod = pam.currentPamPeriod;
			item.deferredToDate = pam.deferredToDate;
			item.lastEvaluationDate = pam.lastEvaluationDate;
			item.pamServicePriority = pam.pamServicePriority;
			result[index++] = item;
		}

		return result;
	}

	public hxc.utils.protocol.acip.PamInformationList[] getPamInformationListA()
	{
		hxc.utils.protocol.acip.PamInformationList[] result = new hxc.utils.protocol.acip.PamInformationList[pamEntries.size()];

		int index = 0;
		for (PamInformationList pam : pamEntries.values())
		{
			hxc.utils.protocol.acip.PamInformationList item = new hxc.utils.protocol.acip.PamInformationList();

			item.pamServiceID = pam.pamServiceID;
			item.pamClassID = pam.pamClassID;
			item.scheduleID = pam.scheduleID;
			item.currentPamPeriod = pam.currentPamPeriod;
			item.deferredToDate = pam.deferredToDate;
			item.lastEvaluationDate = pam.lastEvaluationDate;
			item.pamServicePriority = pam.pamServicePriority;
			result[index++] = item;
		}

		return result;
	}

	public PamInformation getPamInformationA(PamInformationList pam)
	{
		PamInformation result = new PamInformation();

		result.pamServiceID = pam.pamServiceID;
		result.pamClassID = pam.pamClassID;
		result.scheduleID = pam.scheduleID;
		result.currentPamPeriod = pam.currentPamPeriod;
		result.deferredToDate = pam.deferredToDate;
		result.lastEvaluationDate = pam.lastEvaluationDate;
		result.pamServicePriority = pam.pamServicePriority;

		return result;
	}

	public void update(Subscriber subscriber) throws Exception
	{
		try
		{
			super.setAccountValue1(subscriber.getAccountValue1());
		}
		catch (Exception e)
		{
			throw (e);
		}
		super.setCurrency1(subscriber.getCurrency1());
		super.setCreditClearanceDate(subscriber.getCreditClearanceDate());
		super.setCurrency2(subscriber.getCurrency2());
		super.setPinCode(subscriber.getPinCode());
		super.setNegotiatedCapabilities(subscriber.getNegotiatedCapabilities());
		super.setServiceClassCurrent(subscriber.getServiceClassCurrent());
		super.setAggregatedBalance1(subscriber.getAggregatedBalance1());
		super.setAccountValue2(subscriber.getAccountValue2());
		super.setAggregatedBalance2(subscriber.getAggregatedBalance2());
		super.setSupervisionExpiryDate(subscriber.getSupervisionExpiryDate());
		super.setServiceFeeExpiryDate(subscriber.getServiceFeeExpiryDate());
		super.setServiceRemovalDate(subscriber.getServiceRemovalDate());
		super.setLanguageIDCurrent(subscriber.getLanguageIDCurrent());
		super.setTemporaryBlockedFlag(subscriber.getTemporaryBlockedFlag());
		super.setAccountPrepaidEmptyLimit1(subscriber.getAccountPrepaidEmptyLimit1());
		super.setAccountPrepaidEmptyLimit2(subscriber.getAccountPrepaidEmptyLimit2());
		super.setCreditClearancePeriod(subscriber.getCreditClearancePeriod());
		super.setServiceRemovalPeriod(subscriber.getServiceRemovalPeriod());
		super.setCellIdentifier(subscriber.getCellIdentifier());
		super.setNegativeBalanceBarringDate(subscriber.getNegativeBalanceBarringDate());
		super.setFirstIVRCallFlag(subscriber.getFirstIVRCallFlag());
		super.setServiceClassOriginal(subscriber.getServiceClassOriginal());
		super.setAccountGroupID(subscriber.getAccountGroupID());
		super.setAccountActivatedFlag(subscriber.getAccountActivatedFlag());
		super.setActivationDate(subscriber.getActivationDate());
		super.setMasterSubscriberFlag(subscriber.getMasterSubscriberFlag());
		super.setMasterAccountNumber(subscriber.getMasterAccountNumber());
		super.setRefillUnbarDateTime(subscriber.getRefillUnbarDateTime());
		super.setPromotionAnnouncementCode(subscriber.getPromotionAnnouncementCode());
		super.setPromotionPlanID(subscriber.getPromotionPlanID());
		super.setPromotionStartDate(subscriber.getPromotionStartDate());
		super.setPromotionEndDate(subscriber.getPromotionEndDate());
		super.setServiceFeePeriod(subscriber.getServiceFeePeriod());
		super.setSupervisionPeriod(subscriber.getSupervisionPeriod());
		super.setAccountHomeRegion(subscriber.getAccountHomeRegion());
		super.setMaxServiceFeePeriod(subscriber.getMaxServiceFeePeriod());
		super.setMaxSupervisionPeriod(subscriber.getMaxSupervisionPeriod());
		super.setAccountTimeZone(subscriber.getAccountTimeZone());
		super.setLocationNumber(subscriber.getLocationNumber());
		super.setServiceClassTemporaryExpiryDate(subscriber.getServiceClassTemporaryExpiryDate());
		super.setUssdEndOfCallNotificationID(subscriber.getUssdEndOfCallNotificationID());
		super.setServiceClassChangeUnbarDate(subscriber.getServiceClassChangeUnbarDate());
		super.setAvailableServerCapabilities(subscriber.getAvailableServerCapabilities());

	}

	public AccumulatorInformation[] getAccumulatorInformation()
	{
		AccumulatorInformation[] result = new AccumulatorInformation[accumulators.size()];
		int index = 0;
		for (Accumulator accumulator : accumulators.values())
		{
			AccumulatorInformation ua = new AccumulatorInformation();
			ua.setAccumulatorID(accumulator.getAccumulatorID());
			ua.setAccumulatorStartDate(accumulator.getAccumulatorStartDate());
			ua.setAccumulatorEndDate(accumulator.getAccumulatorEndDate());
			ua.setAccumulatorValue(accumulator.getAccumulatorValue());
			result[index++] = ua;
		}

		return result;
	}

	public hxc.utils.protocol.acip.AccumulatorInformation[] getAcipAccumulatorInformation()
	{
		hxc.utils.protocol.acip.AccumulatorInformation[] result = new hxc.utils.protocol.acip.AccumulatorInformation[accumulators.size()];
		int index = 0;
		for (Accumulator accumulator : accumulators.values())
		{
			hxc.utils.protocol.acip.AccumulatorInformation ua = new hxc.utils.protocol.acip.AccumulatorInformation();
			ua.setAccumulatorID(accumulator.getAccumulatorID());
			ua.setAccumulatorStartDate(accumulator.getAccumulatorStartDate());
			ua.setAccumulatorEndDate(accumulator.getAccumulatorEndDate());
			ua.setAccumulatorValue(accumulator.getAccumulatorValue());
			result[index++] = ua;
		}

		return result;
	}

	public AccountFlags getAccountFlags()
	{
		DateTime now = DateTime.getNow();
		AccountFlags flags = new AccountFlags();
		flags.activationStatusFlag = accountActivatedFlag;
		flags.negativeBarringStatusFlag = accountValue1 != null && accountValue1 < 0;
		flags.supervisionPeriodWarningActiveFlag = supervisionExpiryDate != null && now.addDays(1).after(supervisionExpiryDate);
		flags.serviceFeePeriodWarningActiveFlag = serviceFeeExpiryDate != null && now.after(serviceFeeExpiryDate);
		flags.supervisionPeriodExpiryFlag = supervisionExpiryDate != null && now.after(supervisionExpiryDate);
		flags.serviceFeePeriodExpiryFlag = serviceFeeExpiryDate != null && now.after(serviceFeeExpiryDate);
		flags.twoStepActivationFlag = null;
		return flags;
	}

	public AccountFlagsBefore getAccountFlagsBefore()
	{
		DateTime now = DateTime.getNow();
		AccountFlagsBefore flags = new AccountFlagsBefore();
		flags.activationStatusFlag = accountActivatedFlag;
		flags.negativeBarringStatusFlag = accountValue1 != null && accountValue1 < 0;
		flags.supervisionPeriodWarningActiveFlag = supervisionExpiryDate != null && now.addDays(1).after(supervisionExpiryDate);
		flags.serviceFeePeriodWarningActiveFlag = serviceFeeExpiryDate != null && now.after(serviceFeeExpiryDate);
		flags.supervisionPeriodExpiryFlag = supervisionExpiryDate != null && now.after(supervisionExpiryDate);
		flags.serviceFeePeriodExpiryFlag = serviceFeeExpiryDate != null && now.after(serviceFeeExpiryDate);
		return flags;
	}

	public AccountFlagsAfter getAccountFlagsAfter()
	{
		DateTime now = DateTime.getNow();
		AccountFlagsAfter flags = new AccountFlagsAfter();
		flags.activationStatusFlag = accountActivatedFlag;
		flags.negativeBarringStatusFlag = accountValue1 != null && accountValue1 < 0;
		flags.supervisionPeriodWarningActiveFlag = supervisionExpiryDate != null && now.addDays(1).after(supervisionExpiryDate);
		flags.serviceFeePeriodWarningActiveFlag = serviceFeeExpiryDate != null && now.after(serviceFeeExpiryDate);
		flags.supervisionPeriodExpiryFlag = supervisionExpiryDate != null && now.after(supervisionExpiryDate);
		flags.serviceFeePeriodExpiryFlag = serviceFeeExpiryDate != null && now.after(serviceFeeExpiryDate);
		return flags;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// File Generation
	//
	// /////////////////////////////////
	@Override
	public void triggerValueChange(int accountID, Long oldValue, Long newValue) throws Exception
	{
		// Coerce oldValue
		if (oldValue == null)
			oldValue = 0L;

		// Coerce newValue
		if (newValue == null)
			newValue = 0L;

		// Exit if not thresholds defined
		if (tnpThresholds == null || tnpThresholds.size() == 0)
			return;

		// Loop through all thresholds
		for (TnpThreshold tnpThreshold : tnpThresholds.values())
		{
			// Continue if AccountID Mismatches
			if (tnpThreshold.getAccountID() != accountID)
				continue;

			// Continue if Service Class Mismatch
			if (tnpThreshold.getServiceClass() != serviceClassCurrent)
				continue;

			// Continue if not Up
			double level = tnpThreshold.getLevel();
			if (tnpThreshold.isUpwards() && !(oldValue < level && newValue >= level))
				continue;

			// Continue if not Down
			if (!tnpThreshold.isUpwards() && !(oldValue > level && newValue <= level))
				continue;

			try
			{
				// Create folder if it doesn't exist
				File theDir = new File(tnpThreshold.getDirectory());
				if (!theDir.exists())
				{
					theDir.mkdirs();
				}

				// Create filename
				DateTime now = new DateTime();
				String timePart = now.toString("yyyyMMddHHmmss");
				String filename = String.format("%s%s%s_v%s.TNP", timePart, tnpThreshold.getReceiverID(), tnpThreshold.getSenderID(), tnpThreshold.getVersion());
				String tempFilename = filename + ".tmp";

				// Open / Append the File
				File theFile = new File(theDir, tempFilename);

				boolean append = theFile.exists();

				try (FileWriter fw = new FileWriter(theFile, append))
				{

					if (tnpThreshold.getVersion().equals("2.0"))
					{
						String datePart = now.toString("yyMMdd");
						timePart = now.toString("HHmmss");

						String record = String.format("%s,%d,%s,%s,%d,%f,%s,%s,%d\n", //
								internationalNumber, //
								serviceClassCurrent, //
								tnpThreshold.isUpwards() ? "u" : "d", //
								tnpThreshold.getTriggerType().toString(), //
								tnpThreshold.getThresholdID(), //
								tnpThreshold.getLevel(), //
								datePart, //
								timePart, //
								tnpThreshold.getAccountGroupID());

						fw.write(record);
					}

					if (tnpThreshold.getVersion().equals("3.0"))
					{
						String datePart = now.toString("yyyyMMdd");
						timePart = now.toString("HHmmss");

						int operation = 1; // Traffical
						if (tnpThreshold.getTriggerType() == TnpTriggerTypes.ADMIN)
							operation = 2;
						else if (tnpThreshold.getTriggerType() == TnpTriggerTypes.BATCH)
							operation = 3;

						String record = String.format("Version: %s\n", tnpThreshold.getVersion());
						fw.write(record);

						record = String.format("%d,%s,%d,%s,%s,%d,%d,%d,%s,%d,%s,%f,%d,%d", //

								tnpThreshold.getAccountID() == 0 ? 1 : 2, // record_type
								internationalNumber, // sub_id
								serviceClassCurrent, // sc
								datePart, // date
								timePart, // time
								languageIDCurrent, // sub_language
								6, // ?? reason
								operation, // operation
								tnpThreshold.isUpwards() ? "u" : "d", // threshold_direction
								tnpThreshold.getThresholdID(), // threshold_id
								"Y", // ?? threshold_passing_last
								tnpThreshold.getLevel(), // threshold_limit
								0, // ?? tele_service_code
								tnpThreshold.getAccountGroupID() // account_group_id
						);

						if (tnpThreshold.getAccountID() == 0)
						{
							record += "\n";
						}
						else
						{
							// 32,20090101,20090531,27
							DedicatedAccount da = dedicatedAccounts.get(tnpThreshold.getAccountID());
							if (da == null)
							{
								throw new Exception(String.format("Subscriber [%s] not provisioned with DA [%d]", internationalNumber, tnpThreshold.getAccountID()));
							}

							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
							String startDate = "";
							if (da.getStartDate() != null)
								startDate = sdf.format(da.getStartDate());
							String endDate = "";
							if (da.getExpiryDate() != null)
								endDate = sdf.format(da.getExpiryDate());
							String offerID = da.getOfferID() == null ? "" : da.getOfferID().toString();
							record = String.format("%s,%d,%s,%s,%s\n", //
									record, // Common
									da.getDedicatedAccountID(), // da_id
									startDate, // da_start_date
									endDate, // da_expiry_date
									offerID// offer_id;
							);
						}

						fw.write(record);

						fw.write("==========\n");
					}

				}
				catch (IOException e)
				{
					continue;
				}

				// Rename the file
				theFile.renameTo(new File(theDir, filename));

			}
			catch (SecurityException se)
			{
				continue;
			}

		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Usage Timers
	//
	// /////////////////////////////////
	private transient ScheduledThreadPoolExecutor scheduledThreadPool;

	public boolean startUsage(UsageTimer timer, ScheduledThreadPoolExecutor scheduledThreadPool)
	{
		usageTimers.put(timer.getAccount(), timer);
		this.scheduledThreadPool = scheduledThreadPool;
		timer.schedule(scheduledThreadPool, this);

		return true;
	}

	public boolean stopUsage()
	{
		usageTimers.clear();

		return true;
	}

	public List<UsageTimer> getUsage()
	{
		List<UsageTimer> usageTimers = new ArrayList<UsageTimer>();
		usageTimers.addAll(this.usageTimers.values());

		// TODO Auto-generated method stub
		return usageTimers;
	}

	@Override
	public void onUsage(UsageTimer usageTimer)
	{
		// Exit if scheduler is not available or subscriber doesn't have the Usage Timer anymore
		if (scheduledThreadPool == null || scheduledThreadPool.isTerminated() || !usageTimers.containsValue(usageTimer))
			return;

		// Get the Current balance
		Long balance = this.accountValue1;
		DedicatedAccount da = null;
		int accountID = usageTimer.getAccount();
		if (accountID != 0)
		{
			da = dedicatedAccounts.get(accountID);
			if (da == null)
				return;
			balance = da.getDedicatedAccountValue1();
		}

		if (balance == null)
			balance = 0L;

		// Randomise the Decrement
		long decrement = usageTimer.getAmount();
		if (usageTimer.getStandardDeviation() != 0.0)
		{
			decrement += Math.round(usageTimer.getStandardDeviation() * random.nextGaussian());
		}

		// Decrement the Balance
		balance -= decrement;
		if (balance <= 0L)
		{
			Long topUp = usageTimer.getTopupValue();
			if (topUp == null)
				balance = 0L;
			else
				balance = topUp;
		}

		// Update the balance
		try
		{
			if (accountID == 0)
				setAccountValue1(balance);
			else
				da.setDedicatedAccountValue1(balance);
		}
		catch (Exception e)
		{
		}

		// Schedule the next Update
		usageTimer.schedule(scheduledThreadPool, this);

	}

	public void scheduleUsage(ScheduledThreadPoolExecutor scheduledThreadPool)
	{
		this.scheduledThreadPool = scheduledThreadPool;
		for (UsageTimer timer : usageTimers.values())
		{
			timer.schedule(scheduledThreadPool, this);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	public DedicatedAccountInformation[] getDedicatedAccountInformation()
	{
		DedicatedAccountInformation[] result = new DedicatedAccountInformation[dedicatedAccounts.size()];

		int index = 0;
		for (DedicatedAccount da : dedicatedAccounts.values())
		{
			DedicatedAccountInformation info = new DedicatedAccountInformation();
			info.dedicatedAccountID = da.getDedicatedAccountID();
			info.dedicatedAccountValue1 = da.getDedicatedAccountValue1();
			info.dedicatedAccountValue2 = da.getDedicatedAccountValue2();
			info.expiryDate = da.getExpiryDate();
			info.startDate = da.getStartDate();
			info.pamServiceID = da.getPamServiceID();
			info.offerID = da.getOfferID();
			info.productID = da.getProductID();
			info.dedicatedAccountRealMoneyFlag = da.getDedicatedAccountRealMoneyFlag();
			info.closestExpiryDate = da.getClosestExpiryDate();
			info.closestExpiryValue1 = da.getClosestExpiryValue1();
			info.closestExpiryValue2 = da.getClosestExpiryValue2();
			info.closestAccessibleDate = da.getClosestAccessibleDate();
			info.closestAccessibleValue1 = da.getClosestAccessibleValue1();
			info.closestAccessibleValue2 = da.getClosestAccessibleValue2();
			// TODO info.subDedicatedAccountInformation = da.getSubDedicatedAccountInformation();
			info.dedicatedAccountActiveValue1 = da.getDedicatedAccountActiveValue1();
			info.dedicatedAccountActiveValue2 = da.getDedicatedAccountActiveValue2();
			info.dedicatedAccountUnitType = da.getDedicatedAccountUnitType();
			info.compositeDedicatedAccountFlag = da.getCompositeDedicatedAccountFlag();

			result[index++] = info;
		}

		return result;
	}

	public UsageAccumulatorInformation[] getUsageAccumulatorInformation()
	{
		UsageAccumulatorInformation[] result = new UsageAccumulatorInformation[accumulators.size()];

		int index = 0;
		for (Accumulator ua : accumulators.values())
		{
			UsageAccumulatorInformation info = new UsageAccumulatorInformation();
			info.accumulatorID = ua.getAccumulatorID();
			info.accumulatorValue = ua.getAccumulatorValue();

			result[index++] = info;
		}

		return result;
	}

	public ServiceOfferings[] getServiceOfferings2()
	{
		return serviceOfferings.values().toArray(new ServiceOfferings[serviceOfferings.size()]);
	}

	public CommunityIdList[] getCommunityIdList()
	{
		CommunityIdList[] result = new CommunityIdList[communityIDs.size()];

		int index = 0;
		for (Integer id : communityIDs)
		{
			CommunityIdList info = new CommunityIdList();
			info.communityID = id;
			result[index++] = info;
		}

		return result;
	}

	public OfferInformationList[] getOfferInformationList()
	{
		OfferInformationList[] result = new OfferInformationList[offers.size()];

		int index = 0;
		for (OfferEx offer : offers.values())
		{
			OfferInformationList info = new OfferInformationList();

			info.offerID = offer.getOfferID();
			info.startDate = offer.getStartDate();
			info.expiryDate = offer.getExpiryDate();
			info.startDateTime = offer.getStartDateTime();
			info.expiryDateTime = offer.getExpiryDateTime();
			info.pamServiceID = offer.getPamServiceID();
			info.offerType = offer.getOfferType();
			info.offerState = offer.getOfferState();
			info.offerProviderID = offer.getOfferProviderID();
			info.productID = offer.getProductID();
			// TODO info. usageCounterUsageThresholdInformation= offer.getUsageCounterUsageThresholdInformation();
			// TODO info. dedicatedAccountChangeInformation= offer.getDedicatedAccountChangeInformation();
			info.dedicatedAccountInformation = offer.getDedicatedAccountInformation();
			info.attributeInformationList = offer.getAttributeInformationList();

			result[index++] = info;
		}

		return result;
	}

}
