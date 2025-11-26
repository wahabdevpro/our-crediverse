package hxc.services.airsim.engine;

import java.util.List;
import java.util.Map;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.utils.protocol.ucip.CommunityInformationCurrent;
import hxc.utils.protocol.ucip.GetAccountDetailsRequest;
import hxc.utils.protocol.ucip.GetAccountDetailsResponse;
import hxc.utils.protocol.ucip.ServiceOfferings;

public class GetAccountDetails extends SupportedRequest<GetAccountDetailsRequest, GetAccountDetailsResponse>
{

	public GetAccountDetails()
	{
		super(GetAccountDetailsRequest.class);
	}

	@Override
	protected GetAccountDetailsResponse execute(GetAccountDetailsRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		GetAccountDetailsResponse response = new GetAccountDetailsResponse();
		if (!validate(request.member, response.member, injectedResponse))
			return response;

		// Get the Subscriber
		SubscriberEx subscriber = getSubscriber(request.member);
		if (subscriber == null)
		{
			response.member.setResponseCode(102);
			return response;
		}

		// Create Response
		response.member.setFirstIVRCallFlag(subscriber.getFirstIVRCallFlag());
		response.member.setLanguageIDCurrent(subscriber.getLanguageIDCurrent());
		response.member.setServiceClassCurrent(subscriber.getServiceClassCurrent());
		response.member.setServiceClassOriginal(subscriber.getServiceClassOriginal());
		response.member.setServiceClassTemporaryExpiryDate(subscriber.getServiceClassTemporaryExpiryDate());
		response.member.setUssdEndOfCallNotificationID(subscriber.getUssdEndOfCallNotificationID());
		response.member.setAccountGroupID(subscriber.getAccountGroupID());
		Map<Integer, ServiceOfferings> serviceOfferings = subscriber.getServiceOfferings();
		response.member.setServiceOfferings(serviceOfferings.values().toArray(new ServiceOfferings[serviceOfferings.size()]));

		List<Integer> cids = subscriber.getCommunityIDs();
		if (cids != null && cids.size() > 0)
		{
			CommunityInformationCurrent[] current = new CommunityInformationCurrent[cids.size()];
			for (int index = 0; index < cids.size(); index++)
			{
				current[index] = new CommunityInformationCurrent();
				current[index].communityID = cids.get(index);
			}
			response.member.setCommunityInformationCurrent(current);
		}
		response.member.setTemporaryBlockedFlag(subscriber.getTemporaryBlockedFlag());
		response.member.setAccountActivatedFlag(subscriber.getAccountActivatedFlag());
		response.member.setActivationDate(subscriber.getActivationDate());
		response.member.setAccountFlags(subscriber.getAccountFlags());
		response.member.setMasterSubscriberFlag(subscriber.getMasterSubscriberFlag());
		response.member.setMasterAccountNumber(subscriber.getMasterAccountNumber());
		response.member.setRefillUnbarDateTime(subscriber.getRefillUnbarDateTime());
		response.member.setPromotionAnnouncementCode(subscriber.getPromotionAnnouncementCode());
		response.member.setPromotionPlanID(subscriber.getPromotionPlanID());
		response.member.setPromotionStartDate(subscriber.getPromotionStartDate());
		response.member.setPromotionEndDate(subscriber.getPromotionEndDate());
		response.member.setSupervisionExpiryDate(subscriber.getSupervisionExpiryDate());
		response.member.setCreditClearanceDate(subscriber.getCreditClearanceDate());
		response.member.setServiceRemovalDate(subscriber.getServiceRemovalDate());
		response.member.setServiceFeeExpiryDate(subscriber.getServiceFeeExpiryDate());
		response.member.setServiceClassChangeUnbarDate(subscriber.getServiceClassChangeUnbarDate());
		response.member.setServiceFeePeriod(subscriber.getServiceFeePeriod());
		response.member.setSupervisionPeriod(subscriber.getSupervisionPeriod());
		response.member.setServiceRemovalPeriod(subscriber.getServiceRemovalPeriod());
		response.member.setCreditClearancePeriod(subscriber.getCreditClearancePeriod());
		response.member.setCurrency1(subscriber.getCurrency1());
		response.member.setAccountValue1(subscriber.getAccountValue1());
		response.member.setCurrency2(subscriber.getCurrency2());
		response.member.setAccountValue2(subscriber.getAccountValue2());
		response.member.setAccountHomeRegion(subscriber.getAccountHomeRegion());
		response.member.setPinCode(subscriber.getPinCode());
		response.member.setAggregatedBalance1(subscriber.getAggregatedBalance1());
		response.member.setAggregatedBalance2(subscriber.getAggregatedBalance2());
		if (super.isSet(request.member.getRequestPamInformationFlag()))
			response.member.setPamInformationList(subscriber.getPamInformationList());
		response.member.setMaxServiceFeePeriod(subscriber.getMaxServiceFeePeriod());
		response.member.setMaxSupervisionPeriod(subscriber.getMaxSupervisionPeriod());
		response.member.setNegativeBalanceBarringDate(subscriber.getNegativeBalanceBarringDate());
		response.member.setAccountFlagsBefore(subscriber.getAccountFlagsBefore());
		response.member.setOfferInformationList(subscriber.getOfferInformationList(subscriber));
		response.member.setAccountTimeZone(subscriber.getAccountTimeZone());
		// response.member.setNegotiatedCapabilities(subscriber.getNegotiatedCapabilities());
		// response.member.setAvailableServerCapabilities(subscriber.getAvailableServerCapabilities());
		response.member.setCellIdentifier(subscriber.getCellIdentifier());
		response.member.setLocationNumber(subscriber.getLocationNumber());
		response.member.setAccountPrepaidEmptyLimit1(subscriber.getAccountPrepaidEmptyLimit1());
		response.member.setAccountPrepaidEmptyLimit2(subscriber.getAccountPrepaidEmptyLimit2());

		return response;
	}

}
