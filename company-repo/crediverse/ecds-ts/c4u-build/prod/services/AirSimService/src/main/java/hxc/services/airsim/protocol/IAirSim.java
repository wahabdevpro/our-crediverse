package hxc.services.airsim.protocol;

import java.util.Date;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.xml.ws.BindingType;

@WebService
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL, parameterStyle = ParameterStyle.WRAPPED)
@BindingType(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
public interface IAirSim
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Ping
	//
	// /////////////////////////////////

	@WebMethod
	public abstract int ping(@WebParam(name = "seq") int seq);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Start/Stop/Reset
	//
	// /////////////////////////////////

	@WebMethod
	public abstract boolean start();

	@WebMethod
	public abstract void stop();

	@WebMethod
	public abstract void reset();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Subscriber
	//
	// /////////////////////////////////

	@WebMethod
	public abstract Subscriber addSubscriber( //
			@WebParam(name = "msisdn") String msisdn, //
			@WebParam(name = "languageID") int languageID, //
			@WebParam(name = "serviceClass") int serviceClass, //
			@WebParam(name = "accountValue") long accountValue, //
			@WebParam(name = "state") SubscriberState state //
	);

	@WebMethod
	public abstract Subscriber addSubscriber2( //
			@WebParam(name = "msisdn") String msisdn, //
			@WebParam(name = "languageID") int languageID, //
			@WebParam(name = "serviceClass") int serviceClass, //
			@WebParam(name = "accountValue") long accountValue, //
			@WebParam(name = "activationDate") Date activationDate, //
			@WebParam(name = "supervisionExpiryDate") Date supervisionExpiryDate, //
			@WebParam(name = "serviceFeeExpiryDate") Date serviceFeeExpiryDate, //
			@WebParam(name = "creditClearanceDate") Date creditClearanceDate, //
			@WebParam(name = "serviceRemovalDate") Date serviceRemovalDate //
	);

	@WebMethod
	public abstract void addSubscribers( //
			@WebParam(name = "msisdn") String msisdn, //
			@WebParam(name = "count") int count, //
			@WebParam(name = "languageID") int languageID, //
			@WebParam(name = "serviceClass") int serviceClass, //
			@WebParam(name = "accountValue") long accountValue, //
			@WebParam(name = "state") SubscriberState state //
	);

	@WebMethod
	public abstract void addSubscribers2( //
			@WebParam(name = "msisdn") String msisdn, //
			@WebParam(name = "count") int count, //
			@WebParam(name = "languageID") int languageID, //
			@WebParam(name = "serviceClass") int serviceClass, //
			@WebParam(name = "accountValue") long accountValue, //
			@WebParam(name = "activationDate") Date activationDate, //
			@WebParam(name = "supervisionExpiryDate") Date supervisionExpiryDate, //
			@WebParam(name = "serviceFeeExpiryDate") Date serviceFeeExpiryDate, //
			@WebParam(name = "creditClearanceDate") Date creditClearanceDate, //
			@WebParam(name = "serviceRemovalDate") Date serviceRemovalDate //
	);

	@WebMethod
	public abstract Subscriber cloneSubscriber( //
			@WebParam(name = "msisdn") String msisdn, //
			@WebParam(name = "newMsisdn") String newMsisdn //
	);

	@WebMethod
	public abstract boolean cloneSubscribers( //
			@WebParam(name = "msisdn") String msisdn, //
			@WebParam(name = "newMsisdn") String newMsisdn, //
			@WebParam(name = "count") int count //
	);

	@WebMethod
	public abstract Subscriber getSubscriber(@WebParam(name = "msisdn") String msisdn);

	@WebMethod
	public abstract boolean updateSubscriber(@WebParam(name = "subscriber") Subscriber subscriber);

	@WebMethod
	public abstract boolean adjustBalance(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "deltaAmount") long deltaAmount);

	@WebMethod
	public abstract boolean setBalance(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "newAmount") long newAmount);

	@WebMethod
	public abstract boolean deleteSubscriber(@WebParam(name = "msisdn") String msisdn);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Lifecycle
	//
	// /////////////////////////////////

	@WebMethod
	public abstract Lifecycle getLifecycle(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "serviceID") String serviceID, @WebParam(name = "variantID") String variantID);

	@WebMethod
	public abstract Lifecycle[] getLifecycles(@WebParam(name = "msisdn") String msisdn);

	@WebMethod
	public abstract boolean updateLifecycle(@WebParam(name = "lifecycle") Lifecycle lifecycle);

	@WebMethod
	public abstract boolean deleteLifecycle(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "serviceID") String serviceID, @WebParam(name = "variantID") String variantID);

	@WebMethod
	public abstract boolean adjustLifecycle(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "serviceID") String serviceID, @WebParam(name = "variantID") String variantID, @WebParam(name = "isBeingProcessed") Boolean isBeingProcessed, @WebParam(name = "timeStamp") Date timeStamp);

	@WebMethod
	public abstract boolean deleteLifecycles(@WebParam(name = "msisdn") String msisdn);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Membership
	//
	// /////////////////////////////////

	@WebMethod
	public abstract boolean hasMemberLifecycle(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "serviceID") String serviceID, @WebParam(name = "variantID") String variantID, @WebParam(name = "memberMsisdn") String memberMsisdn);

	@WebMethod
	public abstract String[] getMembersLifecycle(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "serviceID") String serviceID, @WebParam(name = "variantID") String variantID);

	@WebMethod
	public abstract boolean addMemberLifecycle(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "serviceID") String serviceID, @WebParam(name = "variantID") String variantID, @WebParam(name = "memberMsisdn") String memberMsisdn);

	@WebMethod
	public abstract boolean deleteMemberLifecycle(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "serviceID") String serviceID, @WebParam(name = "variantID") String variantID, @WebParam(name = "memberMsisdn") String memberMsisdn);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Temporal Triggers
	//
	// /////////////////////////////////

	@WebMethod
	public abstract TemporalTrigger[] getTemporalTriggers(@WebParam(name = "serviceID") String serviceID, @WebParam(name = "variantID") String variantID, @WebParam(name = "msisdnA") String msisdnA, @WebParam(name = "msisdnB") String msisdnB);

	@WebMethod
	public abstract boolean updateTemporalTrigger(@WebParam(name = "temporalTrigger") TemporalTrigger temporalTrigger);

	@WebMethod
	public abstract boolean deleteTemporalTrigger(@WebParam(name = "temporalTrigger") TemporalTrigger temporalTrigger);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Offers
	//
	// /////////////////////////////////

	@WebMethod
	public abstract Offer[] getOffers(@WebParam(name = "msisdn") String msisdn);

	@WebMethod
	public abstract Offer getOffer(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "offerID") int offerID);

	@WebMethod
	public abstract boolean hasOffer(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "offerID") int offerID);

	@WebMethod
	public abstract boolean updateOffer(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "offer") Offer offer);

	@WebMethod
	public abstract boolean deleteOffer(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "offerID") int offerID);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Dedicated Accounts
	//
	// /////////////////////////////////

	@WebMethod
	public abstract DedicatedAccount[] getDedicatedAccounts(@WebParam(name = "msisdn") String msisdn);

	@WebMethod
	public abstract DedicatedAccount getDedicatedAccount(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "dedicatedAccountID") int dedicatedAccountID);

	@WebMethod
	public abstract boolean hasDedicatedAccount(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "dedicatedAccountID") int dedicatedAccountID);

	@WebMethod
	public abstract boolean updateDedicatedAccount(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "dedicatedAccount") DedicatedAccount dedicatedAccount);

	@WebMethod
	public abstract boolean deleteDedicatedAccount(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "dedicatedAccountID") int dedicatedAccountID);

	@WebMethod
	public abstract boolean updateSubDedicatedAccounts(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "dedicatedAccountID") int dedicatedAccountID, @WebParam(name = "subDedicatedAccounts") SubDedicatedAccountInformation[] subDedicatedAccounts);

	@WebMethod
	public abstract boolean createDedicatedAccount(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "dedicatedAccountID") int dedicatedAccountID, //
			@WebParam(name = "unitType") int unitType, @WebParam(name = "value") Long value, //
			@WebParam(name = "startDate") Date startDate, @WebParam(name = "expiryDate") Date expiryDate);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Service Offerings
	//
	// /////////////////////////////////

	@WebMethod
	public abstract ServiceOffering[] getServiceOfferings(@WebParam(name = "msisdn") String msisdn);

	@WebMethod
	public abstract ServiceOffering getServiceOffering(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "serviceOfferingID") int serviceOfferingID);

	@WebMethod
	public abstract boolean hasServiceOffering(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "serviceOfferingID") int serviceOfferingID);

	@WebMethod
	public abstract boolean updateServiceOffering(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "serviceOffering") ServiceOffering serviceOffering);

	@WebMethod
	public abstract boolean deleteServiceOffering(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "serviceOfferingID") int serviceOfferingID);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Usage Counters
	//
	// /////////////////////////////////

	@WebMethod
	public abstract UsageCounter[] getUsageCounters(@WebParam(name = "msisdn") String msisdn);

	@WebMethod
	public abstract UsageCounter getUsageCounter(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "UsageCounterID") int usageCounterID);

	@WebMethod
	public abstract boolean hasUsageCounter(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "UsageCounterID") int usageCounterID);

	@WebMethod
	public abstract boolean updateUsageCounter(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "UsageCounter") UsageCounter usageCounter);

	@WebMethod
	public abstract boolean deleteUsageCounter(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "UsageCounterID") int usageCounterID);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Usage Thresholds
	//
	// /////////////////////////////////

	@WebMethod
	public abstract UsageThreshold[] getUsageThresholds(@WebParam(name = "msisdn") String msisdn);

	@WebMethod
	public abstract UsageThreshold getUsageThreshold(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "UsageThresholdID") int usageThresholdID);

	@WebMethod
	public abstract boolean hasUsageThreshold(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "UsageThresholdID") int usageThresholdID);

	@WebMethod
	public abstract boolean updateUsageThreshold(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "UsageThreshold") UsageThreshold usageThreshold);

	@WebMethod
	public abstract boolean deleteUsageThreshold(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "UsageThresholdID") int usageThresholdID);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accumulators
	//
	// /////////////////////////////////

	@WebMethod
	public abstract Accumulator[] getAccumulators(@WebParam(name = "msisdn") String msisdn);

	@WebMethod
	public abstract Accumulator getAccumulator(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "AccumulatorID") int accumulatorID);

	@WebMethod
	public abstract boolean hasAccumulator(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "AccumulatorID") int accumulatorID);

	@WebMethod
	public abstract boolean updateAccumulator(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "Accumulator") Accumulator accumulator);

	@WebMethod
	public abstract boolean deleteAccumulator(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "AccumulatorID") int accumulatorID);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Faf
	//
	// /////////////////////////////////

	@WebMethod
	public abstract FafEntry[] getFafEntries(@WebParam(name = "msisdn") String msisdn);

	@WebMethod
	public abstract boolean deleteFafEntry(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "fafNumber") String fafNumber);

	@WebMethod
	public abstract boolean deleteFafEntries(@WebParam(name = "msisdn") String msisdn);

	@WebMethod
	public abstract boolean updateFafEntry(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "fafEntry") FafEntry fafEntry);

	@WebMethod
	public abstract boolean addFafEntry(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "fafEntry") FafEntry fafEntry);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Community IDs
	//
	// /////////////////////////////////
	@WebMethod
	public abstract boolean updateCommunityIDs(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "communityIDs") int[] communityIDs);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Refill
	//
	// /////////////////////////////////
	@WebMethod
	public abstract String getLastRefillProfileID();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// PAM
	//
	// /////////////////////////////////
	@WebMethod
	public abstract boolean updatePAM( //
			@WebParam(name = "msisdn") String msisdn, //
			@WebParam(name = "pamServiceID") int pamServiceID, //
			@WebParam(name = "pamClassID") int pamClassID, //
			@WebParam(name = "scheduleID") int scheduleID, //
			@WebParam(name = "currentPamPeriod") String currentPamPeriod, //
			@WebParam(name = "deferredToDate") Date deferredToDate, //
			@WebParam(name = "lastEvaluationDate") Date lastEvaluationDate, //
			@WebParam(name = "pamServicePriority") Integer pamServicePriority);

	@WebMethod
	public abstract boolean deletePAM( //
			@WebParam(name = "msisdn") String msisdn, //
			@WebParam(name = "pamServiceID") int pamServiceID);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// HLR/Map
	//
	// /////////////////////////////////
	@WebMethod
	public abstract boolean setHlrData(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "stateId") Integer stateId, //
			@WebParam(name = "domain") Integer domain, @WebParam(name = "mnpStatusId") Integer mnpStatusId, //
			@WebParam(name = "mobileCountryCode") Integer mobileCountryCode, @WebParam(name = "mobileNetworkCode") Integer mobileNetworkCode, //
			@WebParam(name = "locationAreaCode") Integer locationAreaCode, @WebParam(name = "cellIdentity") Integer cellIdentity, //
			@WebParam(name = "imsi") String imsi, @WebParam(name = "imei") String imei);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// File Generation
	//
	// /////////////////////////////////
	@WebMethod
	public abstract boolean addTnpThreshold(@WebParam(name = "threshold") TnpThreshold threshold);

	@WebMethod
	public abstract void produceDedicatedAccountFile(@WebParam(name = "directory") String directory, @WebParam(name = "prefix") String prefix, @WebParam(name = "version") String version);

	@WebMethod
	public abstract void produceSubscriberFile(@WebParam(name = "directory") String directory, @WebParam(name = "prefix") String prefix, @WebParam(name = "version") String version);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call History
	//
	// /////////////////////////////////

	@WebMethod
	public abstract CallHistory[] getCallHistory();

	@WebMethod
	public abstract void clearCallHistory();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Persistance
	//
	// /////////////////////////////////

	@WebMethod
	public abstract boolean restoreState();

	@WebMethod
	public abstract boolean saveState();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Error Injection
	//
	// /////////////////////////////////
	@WebMethod
	public abstract void injectResponse(@WebParam(name = "airCall") AirCalls airCall, @WebParam(name = "responseCode") int responseCode, @WebParam(name = "delay_ms") int delay_ms);

	@WebMethod
	public abstract void injectSelectiveResponse(@WebParam(name = "airCall") AirCalls airCall, @WebParam(name = "responseCode") int responseCode, @WebParam(name = "delay_ms") int delay_ms, //
			@WebParam(name = "skipCount") int skipCount, @WebParam(name = "failCount") int failCount);

	@WebMethod
	public abstract void resetInjectedResponse(@WebParam(name = "airCall") AirCalls airCall);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Usage
	//
	// /////////////////////////////////
	@WebMethod
	public boolean startUsageTimers(@WebParam(name = "request") StartUsageRequest request);

	@WebMethod
	public boolean stopUsageTimers(@WebParam(name = "msisdn") String msisdn);

	@WebMethod
	public GetUsageResponse getUsageTimers(@WebParam(name = "msisdn") String msisdn);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Misc
	//
	// /////////////////////////////////
	@WebMethod
	public abstract String getNextMSISDN(@WebParam(name = "seed") String seed);

	@WebMethod
	public abstract boolean isCloseTo(@WebParam(name = "datetime1") Date datetime1, @WebParam(name = "datetime2") Date datetime2, @WebParam(name = "tollerance_s") int tollerance_s);

	@WebMethod
	public abstract Long parseTime(@WebParam(name = "time") String time);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ISystemUnderTest
	//
	// /////////////////////////////////

	@WebMethod
	public abstract void setup();

	@WebMethod
	public abstract SmsHistory[] getSmsHistory();

	@WebMethod
	public abstract void clearSmsHistory();

	@WebMethod
	public abstract boolean restoreBackup(@WebParam(name = "backupFileName") String backupFilename);

	@WebMethod
	public abstract void injectMOSms(@WebParam(name = "from") String from, @WebParam(name = "to") String to, @WebParam(name = "text") String text);

	@WebMethod
	public abstract UssdResponse injectMOUssd(@WebParam(name = "from") String from, @WebParam(name = "text") String text, @WebParam(name = "imsi") String imsi);

	@WebMethod
	public abstract String getUssdMenuLine(@WebParam(name = "lineNumber") int lineNumber);

	@WebMethod
	public Cdr getLastCdr();

	@WebMethod
	public Cdr[] getCdr(@WebParam(name = "filters") Filter filters[]);

	@WebMethod
	public Cdr[] getCdrHistory();

	@WebMethod
	public void clearCdrHistory();

	@WebMethod
	public Alarm getLastAlarm();

	@WebMethod
	public Alarm[] getAlarmHistory();

	@WebMethod
	public boolean restart(String optionalCommand);

	@WebMethod
	public String nonQuery(@WebParam(name = "command") String command);

	@WebMethod
	public abstract void tearDown();

}
