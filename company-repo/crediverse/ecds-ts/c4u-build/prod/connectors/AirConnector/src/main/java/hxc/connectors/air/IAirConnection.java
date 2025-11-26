package hxc.connectors.air;

import hxc.connectors.IConnection;
import hxc.utils.protocol.acip.AddPeriodicAccountManagementDataRequest;
import hxc.utils.protocol.acip.AddPeriodicAccountManagementDataResponse;
import hxc.utils.protocol.acip.DeleteAccumulatorsRequest;
import hxc.utils.protocol.acip.DeleteAccumulatorsResponse;
import hxc.utils.protocol.acip.DeleteDedicatedAccountsRequest;
import hxc.utils.protocol.acip.DeleteDedicatedAccountsResponse;
import hxc.utils.protocol.acip.DeleteOfferRequest;
import hxc.utils.protocol.acip.DeleteOfferResponse;
import hxc.utils.protocol.acip.DeletePeriodicAccountManagementDataRequest;
import hxc.utils.protocol.acip.DeletePeriodicAccountManagementDataResponse;
import hxc.utils.protocol.acip.DeleteSubscriberRequest;
import hxc.utils.protocol.acip.DeleteSubscriberResponse;
import hxc.utils.protocol.acip.DeleteTimeRestrictionRequest;
import hxc.utils.protocol.acip.DeleteTimeRestrictionResponse;
import hxc.utils.protocol.acip.DeleteUsageThresholdsRequest;
import hxc.utils.protocol.acip.DeleteUsageThresholdsResponse;
import hxc.utils.protocol.acip.GetPromotionCountersRequest;
import hxc.utils.protocol.acip.GetPromotionCountersResponse;
import hxc.utils.protocol.acip.GetPromotionPlansRequest;
import hxc.utils.protocol.acip.GetPromotionPlansResponse;
import hxc.utils.protocol.acip.GetTimeRestrictionRequest;
import hxc.utils.protocol.acip.GetTimeRestrictionResponse;
import hxc.utils.protocol.acip.InstallSubscriberRequest;
import hxc.utils.protocol.acip.InstallSubscriberResponse;
import hxc.utils.protocol.acip.LinkSubordinateSubscriberRequest;
import hxc.utils.protocol.acip.LinkSubordinateSubscriberResponse;
import hxc.utils.protocol.acip.RunPeriodicAccountManagementRequest;
import hxc.utils.protocol.acip.RunPeriodicAccountManagementResponse;
import hxc.utils.protocol.acip.UpdateAccountManagementCountersRequest;
import hxc.utils.protocol.acip.UpdateAccountManagementCountersResponse;
import hxc.utils.protocol.acip.UpdateAccumulatorsRequest;
import hxc.utils.protocol.acip.UpdateAccumulatorsResponse;
import hxc.utils.protocol.acip.UpdateCommunicationIDRequest;
import hxc.utils.protocol.acip.UpdateCommunicationIDResponse;
import hxc.utils.protocol.acip.UpdatePeriodicAccountManagementDataRequest;
import hxc.utils.protocol.acip.UpdatePeriodicAccountManagementDataResponse;
import hxc.utils.protocol.acip.UpdatePromotionCountersRequest;
import hxc.utils.protocol.acip.UpdatePromotionCountersResponse;
import hxc.utils.protocol.acip.UpdatePromotionPlanRequest;
import hxc.utils.protocol.acip.UpdatePromotionPlanResponse;
import hxc.utils.protocol.acip.UpdateRefillBarringRequest;
import hxc.utils.protocol.acip.UpdateRefillBarringResponse;
import hxc.utils.protocol.acip.UpdateSubDedicatedAccountsRequest;
import hxc.utils.protocol.acip.UpdateSubDedicatedAccountsResponse;
import hxc.utils.protocol.acip.UpdateTemporaryBlockedRequest;
import hxc.utils.protocol.acip.UpdateTemporaryBlockedResponse;
import hxc.utils.protocol.acip.UpdateTimeRestrictionRequest;
import hxc.utils.protocol.acip.UpdateTimeRestrictionResponse;
import hxc.utils.protocol.ucip.GeneralUpdateRequest;
import hxc.utils.protocol.ucip.GeneralUpdateResponse;
import hxc.utils.protocol.ucip.GetAccountDetailsRequest;
import hxc.utils.protocol.ucip.GetAccountDetailsResponse;
import hxc.utils.protocol.ucip.GetAccountManagementCountersRequest;
import hxc.utils.protocol.ucip.GetAccountManagementCountersResponse;
import hxc.utils.protocol.ucip.GetAccountServiceFeeDataRequest;
import hxc.utils.protocol.ucip.GetAccountServiceFeeDataResponse;
import hxc.utils.protocol.ucip.GetAccumulatorsRequest;
import hxc.utils.protocol.ucip.GetAccumulatorsResponse;
import hxc.utils.protocol.ucip.GetAllowedServiceClassesRequest;
import hxc.utils.protocol.ucip.GetAllowedServiceClassesResponse;
import hxc.utils.protocol.ucip.GetBalanceAndDateRequest;
import hxc.utils.protocol.ucip.GetBalanceAndDateResponse;
import hxc.utils.protocol.ucip.GetDiscountInformationRequest;
import hxc.utils.protocol.ucip.GetDiscountInformationResponse;
import hxc.utils.protocol.ucip.GetFaFListRequest;
import hxc.utils.protocol.ucip.GetFaFListResponse;
import hxc.utils.protocol.ucip.GetOffersRequest;
import hxc.utils.protocol.ucip.GetOffersResponse;
import hxc.utils.protocol.ucip.GetRefillOptionsRequest;
import hxc.utils.protocol.ucip.GetRefillOptionsResponse;
import hxc.utils.protocol.ucip.GetUsageThresholdsAndCountersRequest;
import hxc.utils.protocol.ucip.GetUsageThresholdsAndCountersResponse;
import hxc.utils.protocol.ucip.RefillRequest;
import hxc.utils.protocol.ucip.RefillResponse;
import hxc.utils.protocol.ucip.UpdateAccountDetailsRequest;
import hxc.utils.protocol.ucip.UpdateAccountDetailsResponse;
import hxc.utils.protocol.ucip.UpdateBalanceAndDateRequest;
import hxc.utils.protocol.ucip.UpdateBalanceAndDateResponse;
import hxc.utils.protocol.ucip.UpdateCommunityListRequest;
import hxc.utils.protocol.ucip.UpdateCommunityListResponse;
import hxc.utils.protocol.ucip.UpdateFaFListRequest;
import hxc.utils.protocol.ucip.UpdateFaFListResponse;
import hxc.utils.protocol.ucip.UpdateOfferRequest;
import hxc.utils.protocol.ucip.UpdateOfferResponse;
import hxc.utils.protocol.ucip.UpdateServiceClassRequest;
import hxc.utils.protocol.ucip.UpdateServiceClassResponse;
import hxc.utils.protocol.ucip.UpdateSubscriberSegmentationRequest;
import hxc.utils.protocol.ucip.UpdateSubscriberSegmentationResponse;
import hxc.utils.protocol.ucip.UpdateUsageThresholdsAndCountersRequest;
import hxc.utils.protocol.ucip.UpdateUsageThresholdsAndCountersResponse;

public interface IAirConnection extends IConnection
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Housekeeping
	//
	// /////////////////////////////////

	public String getAddress();

	public Integer getPort();

	Integer ACCOUNT_NOT_ACTIVE_RESPONSE_CODE = 126;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// UCIP Protocol
	//
	// /////////////////////////////////
	/**
	 * generalUpdate
	 * 
	 * The message GeneralUpdate is used by external system to adjust offers, account balances, accumulators, service class and more in a single transaction. On the main account it is possible to
	 * adjust the balance and expiry dates both negative and positive (relative) direction and it is also possible to adjust the expiry dates with absolute dates. The dedicated accounts balances,
	 * start dates and expiry dates could be adjusted in negative and positive direction or with absolute values. Note: * It is not possible to do both a relative and an absolute balance or date set
	 * for the same data type (example: it is possible to either set an absolute OR a relative adjustment to the service fee expiry date). * It is only allowed to do unified actions to multiple
	 * accumulators. This means that absolute and relative adjustments has to be ordered in separate requests. When using relative adjustment, negative or positive adjustments of accumulator values
	 * has to be ordered in separate requests. It is not allowed to combine any of these types of actions in the same request. * The complete list of community numbers must be given when changing
	 * communities. For a community ID that is not used, a "filler" community e.g. 9999999 needs to be given. Example: The subscriber has communities 3,10,5. Now 10 is removed and 5 changed to 7. The
	 * array below would look like: communityInformationCurrent: 3,10,5; communityInformationNew: 3,9999999,7 (9999999 = "filler"). * It is not possible to do both a relative and an absolute balance
	 * or date set for the same data type (example: it is possible to either set an absolute OR a relative adjustment to the service fee expiry date). * With this message Sub-DA:s can be created but
	 * not updated. * If pre-activation is wanted then messageCapabilityFlag.accountA ctivationFlag should be included set to 1.
	 * 
	 * @param GeneralUpdateRequest
	 *            request
	 * @return GeneralUpdateResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 105, 106, 117, 123, 124, 126, 139, 140, 147, 148, 153, 154, 155, 163, 164, 165, 167, 204, 209, 212, 214, 215, 223, 224, 225, 226, 227,
	 *             230, 237, 238, 247, 248, 249, 257, 260, 999
	 */
	public GeneralUpdateResponse generalUpdate(GeneralUpdateRequest request) throws AirException;

	/**
	 * getAccountDetails
	 * 
	 * The GetAccountDetails message is used to obtain account information in order to validate and tailor the user communication. Information on subscriber and account level is returned in the
	 * message. Information is only returned in case it has previously been set on the account. Example, serviceFeeExpiryDate is only returned if the account has been activated (and thus has been
	 * assigned an end date for service fee). Note: If pre-activation is wanted then messageCapabilityFlag.accountActivationFlag should be included set to 1. Note: If the locationNumber is not found,
	 * the Visitor Location Register (VLR) is returned.
	 * 
	 * @param GetAccountDetailsRequest
	 *            request
	 * @return GetAccountDetailsResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 1, 2, 100, 102, 197, 260, 999
	 */
	public GetAccountDetailsResponse getAccountDetails(GetAccountDetailsRequest request) throws AirException;

	/**
	 * getAccountManagementCounters
	 * 
	 * The message GetAccountManagementCounters will return account management counters.
	 * 
	 * @param GetAccountManagementCountersRequest
	 *            request
	 * @return GetAccountManagementCountersResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 260, 999
	 */
	public GetAccountManagementCountersResponse getAccountManagementCounters(GetAccountManagementCountersRequest request) throws AirException;

	/**
	 * getAccountServiceFeeData
	 * 
	 * The GetAccountServiceFeeData message is used to fetch service fee data tied to an account.
	 * 
	 * @param GetAccountServiceFeeDataRequest
	 *            request
	 * @return GetAccountServiceFeeDataResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 260
	 */
	public GetAccountServiceFeeDataResponse getAccountServiceFeeData(GetAccountServiceFeeDataRequest request) throws AirException;

	/**
	 * getAccumulators
	 * 
	 * The message GetAccumulators is used to obtain accumulator values and (optional) start and end dates related to those accumulators. Note: If pre-activation is wanted then
	 * messageCapabilityFlag.accountActivati onFlag should be included set to 1.
	 * 
	 * @param GetAccumulatorsRequest
	 *            request
	 * @return GetAccumulatorsResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 123, 124, 126, 127, 260, 999
	 */
	public GetAccumulatorsResponse getAccumulators(GetAccumulatorsRequest request) throws AirException;

	/**
	 * getAllowedServiceClasses
	 * 
	 * The GetAllowedServiceClasses message is used to fetch a list of service classes the subscriber is allowed to change to.
	 * 
	 * @param GetAllowedServiceClassesRequest
	 *            request
	 * @return GetAllowedServiceClassesResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 133, 260, 999
	 */
	public GetAllowedServiceClassesResponse getAllowedServiceClasses(GetAllowedServiceClassesRequest request) throws AirException;

	/**
	 * getBalanceAndDate
	 * 
	 * The message GetBalanceAndDate is used to perform a balance enquiry on the account associated with a specific subscriber identity. Also lifecycle dates are presented. Information is given on
	 * both main and dedicated accounts. Note: If pre-activation is wanted then messageCapabilityFlag.accountActivati onFlag should be included set to 1. For a product private (instantiated) DA, the
	 * GetBalanceAndDate request should be used to only get their instance ID (productID). To get the capabilities which the DA share with the Offer, use the GetOffers request.
	 * 
	 * @param GetBalanceAndDateRequest
	 *            request
	 * @return GetBalanceAndDateResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 123, 124, 126, 137, 139, 197, 260, 999
	 */
	public GetBalanceAndDateResponse getBalanceAndDate(GetBalanceAndDateRequest request) throws AirException;

	/**
	 * getCapabilites
	 * 
	 * The message GetCapabilities is used to fetch available capabilities. See Section 9 on page 213 for available capabilities
	 * 
	 * @param GetCapabilitesRequest
	 *            request
	 * @return GetCapabilitesResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100
	 */
	public hxc.utils.protocol.ucip.GetCapabilitesResponse getCapabilites(hxc.utils.protocol.ucip.GetCapabilitesRequest request) throws AirException;

	/**
	 * getDiscountInformation
	 * 
	 * The message GetDiscountInformation retrieves discounts. Any number of discount IDs can be specified for retrieval. If no IDs are requested all the discounts will be returned.
	 * 
	 * @param GetDiscountInformationRequest
	 *            request
	 * @return GetDiscountInformationResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 242, 999
	 */
	public GetDiscountInformationResponse getDiscountInformation(GetDiscountInformationRequest request) throws AirException;

	/**
	 * getFaFList
	 * 
	 * The GetFaFList message is used to fetch the list of Family and Friends numbers with attached FaF indicators.
	 * 
	 * @param GetFaFListRequest
	 *            request
	 * @return GetFaFListResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 126, 260, 999
	 */
	public GetFaFListResponse getFaFList(GetFaFListRequest request) throws AirException;

	/**
	 * getOffers
	 * 
	 * The message GetOffers will return a list of offers currently assigned to an account. The detail level of the returned list can be specified in the request using various flags. To get
	 * subDedicatedAccounts, both requestSubDedicatedAccountDetailsFlag and requestDedicatedAccountDetailsFlag must be set to "1". For product private (instantiated) DA:s, the GetOffers request should
	 * be used to get the capabilities which the DA share with the Offer. Such data are start and expiry date, dateTime, state, offer type, PAM service and offerProviderID.
	 * 
	 * @param GetOffersRequest
	 *            request
	 * @return GetOffersResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 165, 214, 247, 260
	 */
	public GetOffersResponse getOffers(GetOffersRequest request) throws AirException;

	/**
	 * getRefillOptions
	 * 
	 * This message GetRefillOptions is used to fetch the refill options. Note: In case Service Class is given it takes precedence before subscriber number. It is thus possible to request refill
	 * options for a Service Class which is not yet active for the given subscriber number. Note: If pre-activation is wanted then messageCapabilityFlag.accountActivati onFlag should be included set
	 * to 1.
	 * 
	 * @param GetRefillOptionsRequest
	 *            request
	 * @return GetRefillOptionsResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 103, 107, 108, 109, 110, 111, 112, 113, 115, 119, 126, 260, 999
	 */
	public GetRefillOptionsResponse getRefillOptions(GetRefillOptionsRequest request) throws AirException;

	/**
	 * getUsageThresholdsAndCounters
	 * 
	 * The message GetUsageThresholdsAndCounters is used to fetch the active usage counters and thresholds for a subscriber.
	 * 
	 * @param GetUsageThresholdsAndCountersRequest
	 *            request
	 * @return GetUsageThresholdsAndCountersResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 260
	 */
	public GetUsageThresholdsAndCountersResponse getUsageThresholdsAndCounters(GetUsageThresholdsAndCountersRequest request) throws AirException;

	/**
	 * refill
	 * 
	 * The message Refill is used to apply a refill from an administrative system to a prepaid account associated with a specific subscriber identity. It can be a voucherless refill where an amount is
	 * added to account, according to the refill profile rules. It can also be a voucher refill made for an example by customer care on request from the subscriber. The
	 * requestSubDedicatedAccountDetailsFlag parameter will only affect whether sub dedicated account details are included in the accountBeforeRefill and accountAfterRefill structs. The
	 * refillInformation struct is not affected by requestSubDedicatedAccountDetailsFlag and will always contain details on affected sub dedicated accounts. Note: In order to differentiate a
	 * voucherless refill from a voucher refill, it is not allowed to send the N/A-marked parameters in the different refills. The different types of refill are mutual exclusive. Example: It is not
	 * allowed to give transactionAmount in a voucher refill. Note: If pre-activation is wanted then messageCapabilityFlag.accountActivati onFlag should be included set to 1.
	 * 
	 * @param RefillRequest
	 *            request
	 * @return RefillResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 1, 2, 100, 102, 103, 104, 105, 107, 108, 109, 110, 111, 112, 113, 114, 115, 117, 119, 120, 121, 122, 123, 126, 136, 153, 160, 161, 165, 167, 176, 177,
	 *             178, 179, 214, 225, 248, 260, 999
	 */
	public RefillResponse refill(RefillRequest request) throws AirException;

	/**
	 * updateAccountDetails
	 * 
	 * The message UpdateAccountDetails is used to update the account information. Note: If pre-activation is wanted then messageCapabilityFlag.accountActivati onFlag should be included set to 1.
	 * 
	 * @param UpdateAccountDetailsRequest
	 *            request
	 * @return UpdateAccountDetailsResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 126, 128, 138, 141, 157, 204, 241, 260, 999
	 */
	public UpdateAccountDetailsResponse updateAccountDetails(UpdateAccountDetailsRequest request) throws AirException;

	/**
	 * updateBalanceAndDate
	 * 
	 * The message UpdateBalanceAndDate is used by external system to adjust balances, start dates and expiry dates on the main account and the dedicated accounts. On the main account it is possible
	 * to adjust the balance and expiry dates both negative and positive (relative) direction and it is also possible to adjust the expiry dates with absolute dates. The dedicated accounts balances,
	 * start dates and expiry dates could be adjusted in negative and positive direction or with absolute values. Note: It is not possible to do both a relative and an absolute balance or date set for
	 * the same data type (example: it is possible to either set an absolute OR a relative adjustment to the service fee expiry date). It is also possible to set the Service removal and Credit
	 * clearance periods on account. Note: If pre-activation is wanted then messageCapabilityFlag.accountActivati onFlag should be included set to 1.
	 * 
	 * @param UpdateBalanceAndDateRequest
	 *            request
	 * @return UpdateBalanceAndDateResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 105, 106, 121, 122, 123, 124, 126, 136, 139, 153, 163, 164, 167, 204, 212, 226, 227, 230, 247, 249, 257, 260, 999
	 */
	public UpdateBalanceAndDateResponse updateBalanceAndDate(UpdateBalanceAndDateRequest request) throws AirException;

	/**
	 * updateCommunityList
	 * 
	 * The message UpdateCommunityList set or updates the list of communities which the account belong to. The complete list of community numbers must be given when changing communities. Example: The
	 * subscriber has communities 3,10,5. Now 10 is removed and 5 changed to 7. The array below would look like: communityInformationCurrent: 3,10,5; communityInformationNew: 3,7.
	 * 
	 * @param UpdateCommunityListRequest
	 *            request
	 * @return UpdateCommunityListResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 147, 148, 260, 999
	 */
	public UpdateCommunityListResponse updateCommunityList(UpdateCommunityListRequest request) throws AirException;

	/**
	 * updateFaFList
	 * 
	 * The message UpdateFaFList is used to update the Family and Friends list for either the account or subscriber. Note: Charged FaF number change is not supported on account level. It is only
	 * supported on subscription level. The field fafIndicator in fafInformation is mandatory for non-charging operations, and it is optional for charged operations.
	 * 
	 * @param UpdateFaFListRequest
	 *            request
	 * @return UpdateFaFListResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 123, 124, 126, 127, 129, 130, 134, 135, 159, 205, 206, 260, 999
	 */
	public UpdateFaFListResponse updateFaFList(UpdateFaFListRequest request) throws AirException;

	/**
	 * updateOffer
	 * 
	 * The UpdateOffer message will assign a new offer or update an existing offer to an account. If the UpdateOffer request is sent and the offerID is not found for the account, then the update
	 * request is considered to be an assignment request. If the offer is configured to allow multiple products a new product for the specified offer will be assigned. The following principles apply
	 * when assigning a new offer: * It is not allowed to have a start date (and time) beyond the expiry date (and time). * It is not allowed to have an expiry date (and time) set to an earlier date
	 * (and time) than the current date (and time). * If no absolute or relative start date (and time) is provided, then no date (and time) will be assigned as offer start date (and time). * If no
	 * expiry date (or expiry date and time) is provided, then an infinite expiry date is used which means that the offer never expires. The following principles apply when updating an offer: * An
	 * offer (except type Timer) will be active if the start date has been reached and the expiry date is still in the future. * An offer of type Timer will only become active through triggering by a
	 * traffic event. A Timer offer is always installed in a disabled state. * An offer will expire if the expiry date (or expiry date and time) is before the current date (and time). * It is not
	 * allowed to modify the start date (or start date and time) of an active or enabled (in the case of type Timer) offer. * It is not allowed to modify the start date and time of an offer of type
	 * Timer if the start date and time has already passed. * It is not allowed to modify the expiry date (or expiry date and time) to an earlier date (or date and time) than the current date (or date
	 * and time). * It is not allowed to modify the expiry date (or expiry date and time) of an expired offer * It is not allowed to modify the start date (or start date and time) beyond the expiry
	 * date (or expiry date and time). When doing an update, if a date (or date and time) is given in relative days (or days and time expressed in seconds), then the new date (or date and time) will
	 * be the current defined date (or date and time) plus the relative days (or days and time expressed in seconds). This applies to both start date (or date and time) and expiry date (or date and
	 * time). The parameter offerProviderID states the needed provider ID when creating a provider account offer. The parameter offerProviderID states the new provider ID when updating a provider
	 * account offer. Note: OfferType it is mandatory for Timer Offer
	 * 
	 * @param UpdateOfferRequest
	 *            request
	 * @return UpdateOfferResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 136, 165, 214, 215, 223, 224, 225, 226, 227, 230, 237, 238, 247, 248, 256, 257, 258, 259, 260
	 */
	public UpdateOfferResponse updateOffer(UpdateOfferRequest request) throws AirException;

	/**
	 * updateServiceClass
	 * 
	 * This message UpdateServiceClass is used to update the service class (SC) for the subscriber. It is also possible to set a temporary SC with an expiry date. When temporary Service Class date is
	 * expired the Account will fallback to the original Service Class defined for the account.
	 * 
	 * @param UpdateServiceClassRequest
	 *            request
	 * @return UpdateServiceClassResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 117, 123, 124, 126, 127, 134, 135, 140, 154, 155, 257, 260, 999
	 */
	public UpdateServiceClassResponse updateServiceClass(UpdateServiceClassRequest request) throws AirException;

	/**
	 * updateSubscriberSegmentation
	 * 
	 * The message UpdateSubscriberSegmentation is used in order set or update the accountGroupID and serviceOffering parameters which are used for subscriber segmentation. ServiceFee information is
	 * included in the response as (PC:06214).
	 * 
	 * @param UpdateSubscriberSegmentationRequest
	 *            request
	 * @return UpdateSubscriberSegmentationResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 124, 260, 999
	 */
	public UpdateSubscriberSegmentationResponse updateSubscriberSegmentation(UpdateSubscriberSegmentationRequest request) throws AirException;

	/**
	 * updateUsageThresholdsAndCounters
	 * 
	 * The message UpdateUsageThresholdsAndCounters is used to personalize a usage threshold for a subscriber by setting a value other than the default value, either an individual value for a
	 * subscriber or an individual value for a provider shared by all consumers. The other main usage is to reset a usage counter. A counter can also be changed to any value, either by specifying a
	 * new counter value or by adding or subtracting a specified value to the current counter value. When the parameter updateUsageCounterForMultiUser is included in the message the usage counters
	 * specified in usageCounterUpdateInformation will be reset for all subscribers connected to the account or for the provider and all consumers. In this case the
	 * usageCounterUsageThresholdInformation in the response will only contain information about the subscriber or associatedPartyID the request was directed to.
	 * 
	 * @param UpdateUsageThresholdsAndCountersRequest
	 *            request
	 * @return UpdateUsageThresholdsAndCountersResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 216, 217, 219, 220, 221, 243, 244, 245, 247, 260
	 */
	public UpdateUsageThresholdsAndCountersResponse updateUsageThresholdsAndCounters(UpdateUsageThresholdsAndCountersRequest request) throws AirException;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ACIP Protocol
	//
	// /////////////////////////////////
	/**
	 * addPeriodicAccountManagementData
	 * 
	 * The message AddPeriodicAccountManagementData adds periodic account management data to a subscriber.
	 * 
	 * @param AddPeriodicAccountManagementDataRequest
	 *            request
	 * @return AddPeriodicAccountManagementDataResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 190, 191, 193, 195, 196, 197, 198, 199, 200, 201, 233, 235, 236, 255, 257, 260, 999
	 */
	public AddPeriodicAccountManagementDataResponse addPeriodicAccountManagementData(AddPeriodicAccountManagementDataRequest request) throws AirException;

	/**
	 * deleteAccumulators
	 * 
	 * This message is intended to remove one or more accumulators identified by their accumulatorID. If additional conditions need to be processed, the message offers the possibility to use optional
	 * input parameters to be verified with the subscriber (serviceClassCurrent) and accumulator (accumulatorEndDate) configurations.
	 * 
	 * @param DeleteAccumulatorsRequest
	 *            request
	 * @return DeleteAccumulatorsResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 127, 207, 208, 260, 999
	 */
	public DeleteAccumulatorsResponse deleteAccumulators(DeleteAccumulatorsRequest request) throws AirException;

	/**
	 * deleteDedicatedAccounts
	 * 
	 * This message is intended to remove one or more dedicated accounts identified by their dedicatedAccountID. If additional conditions need to be processed, the message offers the possibility to
	 * use optional input parameters to be verified with the dedicated account (expiryDate) configuration. Note that for product private (instantiated) DA:s, that the DeleteDedicatedAcc ounts response
	 * will not contain any of the capabilities which the DA share with the Offer.
	 * 
	 * @param DeleteDedicatedAccountsRequest
	 *            request
	 * @return DeleteDedicatedAccountsResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 139, 209, 210, 211, 222, 247, 249, 260, 999
	 */
	public DeleteDedicatedAccountsResponse deleteDedicatedAccounts(DeleteDedicatedAccountsRequest request) throws AirException;

	/**
	 * deleteOffer
	 * 
	 * The message DeleteOffer is used to disconnect an offer assigned to an account.
	 * 
	 * @param DeleteOfferRequest
	 *            request
	 * @return DeleteOfferResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 165, 247, 260
	 */
	public DeleteOfferResponse deleteOffer(DeleteOfferRequest request) throws AirException;

	/**
	 * deletePeriodicAccountManagementData
	 * 
	 * The message DeletePeriodicAccountManagementData deletes periodic account management evaluation data for a subscriber.
	 * 
	 * @param DeletePeriodicAccountManagementDataRequest
	 *            request
	 * @return DeletePeriodicAccountManagementDataResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 191, 197, 198, 201, 232, 255, 260, 999
	 */
	public DeletePeriodicAccountManagementDataResponse deletePeriodicAccountManagementData(DeletePeriodicAccountManagementDataRequest request) throws AirException;

	/**
	 * deleteSubscriber
	 * 
	 * The message DeleteSubscriber performs a deletion of subscriber and account. Details of the subscriber and account are reported in the response. If a master subscriber with subordinates is to be
	 * deleted all subordinates must have been deleted first (for clean-up and account history purposes). When a single/master subscriber is deleted, its account and all the data connected to the
	 * account (dedicated accounts, accumulators, and so on) are deleted. For a subordinate subscriber, only the subscriber part and its related data are deleted. If it is of interest to see the
	 * relations between instansiated resources (UA/DA) and an instantiated Offer when deleting a subscriber, the recommendation would be to first perform a DeleteOffers operation (to get the
	 * capabilities which the DA share with the Offer), and, after that the DeleteSubscriber operation.
	 * 
	 * @param DeleteSubscriberRequest
	 *            request
	 * @return DeleteSubscriberResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 156, 260, 999
	 */
	public DeleteSubscriberResponse deleteSubscriber(DeleteSubscriberRequest request) throws AirException;

	/**
	 * deleteTimeRestriction
	 * 
	 * This message removes any number of time restrictions. If no identifier is given all existing restrictions will be deleted.
	 * 
	 * @param DeleteTimeRestrictionRequest
	 *            request
	 * @return DeleteTimeRestrictionResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 239, 260
	 */
	public DeleteTimeRestrictionResponse deleteTimeRestriction(DeleteTimeRestrictionRequest request) throws AirException;

	/**
	 * deleteUsageThresholds
	 * 
	 * The message DeleteUsageThresholds removes a personal or common usage threshold from a subscriber.
	 * 
	 * @param DeleteUsageThresholdsRequest
	 *            request
	 * @return DeleteUsageThresholdsResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 216, 218, 246, 260
	 */
	public DeleteUsageThresholdsResponse deleteUsageThresholds(DeleteUsageThresholdsRequest request) throws AirException;

	/**
	 * getCapabilites
	 * 
	 * The message GetCapabilities is used to fetch available capabilities.
	 * 
	 * @param GetCapabilitesRequest
	 *            request
	 * @return GetCapabilitesResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100
	 */
	public hxc.utils.protocol.acip.GetCapabilitesResponse getCapabilites(hxc.utils.protocol.acip.GetCapabilitesRequest request) throws AirException;

	/**
	 * getPromotionCounters
	 * 
	 * The message GetPromotionCounters will return the current accumulated values used as base for the calculation of when to give a promotion and when to progress a promotion plan.
	 * 
	 * @param GetPromotionCountersRequest
	 *            request
	 * @return GetPromotionCountersResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 260, 999
	 */
	public GetPromotionCountersResponse getPromotionCounters(GetPromotionCountersRequest request) throws AirException;

	/**
	 * getPromotionPlans
	 * 
	 * The message GetPromotionPlans will return the promotion plans allocated to the subscribers account.
	 * 
	 * @param GetPromotionPlansRequest
	 *            request
	 * @return GetPromotionPlansResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 260, 999
	 */
	public GetPromotionPlansResponse getPromotionPlans(GetPromotionPlansRequest request) throws AirException;

	/**
	 * getTimeRestriction
	 * 
	 * This message retrieves time restrictions. Any number of time restriction IDs can be specified for retrieval. If no IDs are requested all the time restriction will be returned.
	 * 
	 * @param GetTimeRestrictionRequest
	 *            request
	 * @return GetTimeRestrictionResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 239, 260
	 */
	public GetTimeRestrictionResponse getTimeRestriction(GetTimeRestrictionRequest request) throws AirException;

	/**
	 * installSubscriber
	 * 
	 * The message InstallSubscriber performs an installation of a subscriber with relevant account and subscriber data. A master subscription is created in an account database predefined in the
	 * system. The master subscription can be changed to a subordinate subscription by using the LinkSubordinateSubscriber message.
	 * 
	 * @param InstallSubscriberRequest
	 *            request
	 * @return InstallSubscriberResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 141, 142, 155, 157, 190, 191, 193, 195, 196, 198, 199, 200, 201, 203, 233, 235, 236, 240, 257, 260, 999
	 */
	public InstallSubscriberResponse installSubscriber(InstallSubscriberRequest request) throws AirException;

	/**
	 * linkSubordinateSubscriber
	 * 
	 * The message LinkSubordinateSubscriber will link a previously installed subscriber to another subscriber's account.
	 * 
	 * @param LinkSubordinateSubscriberRequest
	 *            request
	 * @return LinkSubordinateSubscriberResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 143, 144, 145, 146, 260, 999
	 */
	public LinkSubordinateSubscriberResponse linkSubordinateSubscriber(LinkSubordinateSubscriberRequest request) throws AirException;

	/**
	 * runPeriodicAccountManagement
	 * 
	 * The message RunPeriodicAccountManagement executes an on demand periodic account management evaluation.
	 * 
	 * @param RunPeriodicAccountManagementRequest
	 *            request
	 * @return RunPeriodicAccountManagementResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 126, 191, 197, 199, 201, 202, 255, 260, 999
	 */
	public RunPeriodicAccountManagementResponse runPeriodicAccountManagement(RunPeriodicAccountManagementRequest request) throws AirException;

	/**
	 * updateAccountManagementCounters
	 * 
	 * The message UpdateAccountManagementCounters will modify account management counters.
	 * 
	 * @param UpdateAccountManagementCountersRequest
	 *            request
	 * @return UpdateAccountManagementCountersResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 127, 134, 135, 260, 999
	 */
	public UpdateAccountManagementCountersResponse updateAccountManagementCounters(UpdateAccountManagementCountersRequest request) throws AirException;

	/**
	 * updateAccumulators
	 * 
	 * The message UpdateAccumulators performs an adjustment to the counter values of the chosen accumulators. It is possible to do a relative adjustment or an absolute adjustment of an accumulator
	 * value. Relative adjustment of the accumulator value is possible to do both in positive and negative direction. The accumulator is cleared by setting the absolute accumulator value to 0. Note:
	 * It is only allowed to do unified actions to multiple accumulators. This means that absolute and relative adjustments has to be ordered in separate requests. When using relative adjustment,
	 * negative or positive adjustments of accumulator values has to be ordered in separate requests. It is not allowed to combine any of these types of actions in the same request. If additional
	 * conditions need to be processed, the message offers the possibility to use an optional input parameter to be verified with the subscriber (serviceClassCurrent) configuration.
	 * 
	 * @param UpdateAccumulatorsRequest
	 *            request
	 * @return UpdateAccumulatorsResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 127, 134, 135, 260, 999
	 */
	public UpdateAccumulatorsResponse updateAccumulators(UpdateAccumulatorsRequest request) throws AirException;

	/**
	 * updateCommunicationID
	 * 
	 * The communication ID change operation changes the Communication ID. A Communication ID can be one or several out of MSISDN, NAI, IMSI, SIP-URI and PRIVATE. In order to change an identifier,
	 * both the old and new identifier needs to be included. Example: imsiCurrentimsiNew. For simplicity NAI, IMSI, SIP-URI and PRIVATE are referred to as a group, extended address. Valid for all
	 * combinations is that, in order for the operation to be successful; the new MSISDN and/or the new extended address cannot be occupied by another subscriber already. During a Communication ID
	 * change, where the MSISDN is changed from current to new, only extended addresses included in the operation, will be connected to the new MSISDN by the operation. It is possible to have this
	 * change done afterwards by running an offline job. It is possible to differentiate charging using the parameter chargingInformation, and to indicate that charging is done outside Charging
	 * Compound using externalContract parameter.
	 * 
	 * @param UpdateCommunicationIDRequest
	 *            request
	 * @return UpdateCommunicationIDResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 126, 142, 250, 251, 252, 253, 254, 260, 999
	 */
	public UpdateCommunicationIDResponse updateCommunicationID(UpdateCommunicationIDRequest request) throws AirException;

	/**
	 * updatePeriodicAccountManagementData
	 * 
	 * The message UpdatePeriodicAccountManagementData changes periodic account management data for a subscriber.
	 * 
	 * @param UpdatePeriodicAccountManagementDataRequest
	 *            request
	 * @return UpdatePeriodicAccountManagementDataResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 233, 234, 235, 236, 255, 257, 260, 999
	 */
	public UpdatePeriodicAccountManagementDataResponse updatePeriodicAccountManagementData(UpdatePeriodicAccountManagementDataRequest request) throws AirException;

	/**
	 * updatePromotionCounters
	 * 
	 * The message UpdatePromotionCounters give access to modify the counters used in the calculation when to give a promotion or promotion plan progression. It is possible to modify the accumulated
	 * value or the accumulated counter used in these calculations.
	 * 
	 * @param UpdatePromotionCountersRequest
	 *            request
	 * @return UpdatePromotionCountersResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 126, 260, 999
	 */
	public UpdatePromotionCountersResponse updatePromotionCounters(UpdatePromotionCountersRequest request) throws AirException;

	/**
	 * updatePromotionPlan
	 * 
	 * The message UpdatePromotionPlan can Add, Set or Delete a promotion plan allocation to an account. The promotion plan ID has to be defined already in the business configuration in AIR, where the
	 * actual execution of promotions is done. Two promotion plans can be allocated to account, but it is only possible to address one promotion plan at the time in the request. The validity periods
	 * of two promotion plans for a single account are not allowed to overlap The promotion plan can not be set if it ends in the past. Note: The Promotion plan configurations are done through the ERE
	 * trees and the IDs allowed in the "Update Promotion Plan" needs to be added to the Promotion Plan window. For more information see, AIR User's Guide Service Configuration Administration,
	 * Reference [3].
	 * 
	 * @param UpdatePromotionPlanRequest
	 *            request
	 * @return UpdatePromotionPlanResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 149, 150, 151, 204, 260, 999
	 */
	public UpdatePromotionPlanResponse updatePromotionPlan(UpdatePromotionPlanRequest request) throws AirException;

	/**
	 * updateRefillBarring
	 * 
	 * The message UpdateRefillBarring either bar or clear the subscriber when attempting refills. It is done by either increasing the subscriber's barring counter (making it more likely the
	 * subscriber will be barred) or moving the unbar-date forward, or clearing the counter and resetting the barring date. When a subscriber's barring counter exceeds a configured value, when it is
	 * increased, the subscriber will be barred.
	 * 
	 * @param UpdateRefillBarringRequest
	 *            request
	 * @return UpdateRefillBarringResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 126, 260, 999
	 */
	public UpdateRefillBarringResponse updateRefillBarring(UpdateRefillBarringRequest request) throws AirException;

	/**
	 * updateSubDedicatedAccounts
	 * 
	 * The message UpdateSubDedicatedAccounts is used by external system to adjust balances, start dates and expiry dates on the sub dedicated accounts. While it is possible to update several sub
	 * dedicated accounts belonging to different composite dedicated accounts in one request, it is not possible to update several sub dedicated accounts that belong to the same composite dedicated
	 * account in the same request.
	 * 
	 * @param UpdateSubDedicatedAccountsRequest
	 *            request
	 * @return UpdateSubDedicatedAccountsResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 104, 105, 106, 123, 124, 126, 136, 139, 153, 163, 164, 167, 213, 226, 227, 230, 257, 260, 999
	 */
	public UpdateSubDedicatedAccountsResponse updateSubDedicatedAccounts(UpdateSubDedicatedAccountsRequest request) throws AirException;

	/**
	 * updateTemporaryBlocked
	 * 
	 * The message UpdateTemporaryBlocked set or clear the temporary blocked status on a subscriber. When temporary block status is set for a subscriber, all updates of the account through that
	 * particular subscriber is prevented. This means that temporary blocking one subscriber (independent if it is the master or subordinate) does not prevent the other subscribers to access and
	 * update the account if they belong to the same account.
	 * 
	 * @param UpdateTemporaryBlockedRequest
	 *            request
	 * @return UpdateTemporaryBlockedResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 152, 260, 999
	 */
	public UpdateTemporaryBlockedResponse updateTemporaryBlocked(UpdateTemporaryBlockedRequest request) throws AirException;

	/**
	 * updateTimeRestriction
	 * 
	 * This message handles both creation and updates to time restrictions. If a restriction id is given that does not exist the restriction will be created. If the corresponding restriction exists it
	 * will be updated instead.
	 * 
	 * @param UpdateTimeRestrictionRequest
	 *            request
	 * @return UpdateTimeRestrictionResponse response
	 * @throws AirException
	 *             if AIR returns any one of: 0, 100, 102, 240, 260
	 */
	public UpdateTimeRestrictionResponse updateTimeRestriction(UpdateTimeRestrictionRequest request) throws AirException;

	public abstract String getHost();
	
}
