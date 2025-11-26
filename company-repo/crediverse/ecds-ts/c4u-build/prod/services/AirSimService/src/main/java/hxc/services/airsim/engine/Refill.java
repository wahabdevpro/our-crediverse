package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.utils.protocol.ucip.AccountAfterRefill;
import hxc.utils.protocol.ucip.AccountBeforeRefill;
import hxc.utils.protocol.ucip.RefillRequest;
import hxc.utils.protocol.ucip.RefillResponse;

public class Refill extends SupportedRequest<RefillRequest, RefillResponse>
{

	public Refill()
	{
		super(RefillRequest.class);
	}

	@Override
	protected RefillResponse execute(RefillRequest request, InjectedResponse injectedResponse) throws Exception
	{
		// Create Response
		RefillResponse response = new RefillResponse();
		if (!validate(request.member, response.member, injectedResponse))
			return response;
		
		// Remember the RefillProfileID
		super.simulationData.setLastRefillProfileID(request.member.refillProfileID);

		// Get the Subscriber
		SubscriberEx subscriber = getSubscriber(request.member);
		if (subscriber == null)
		{
			response.member.setResponseCode(102);
			return response;
		}
		if (subscriber.getTemporaryBlockedFlag() != null && subscriber.getTemporaryBlockedFlag())
			return exitWith(response, response.member, 104);

		// Get the Before Accounts
		response.member.setAccountBeforeRefill(getAccountBeforeRefill(request.member.getRequestRefillAccountBeforeFlag(), subscriber));

		// Perform the Refill
		if ("INCM".equals(request.member.refillProfileID))
		{
			Long balance = subscriber.getAccountValue1();
			balance = balance == null ? 0L : balance;
			subscriber.setAccountValue1(request.member.transactionAmount + balance);
		}
		else if ("DECM".equals(request.member.refillProfileID))
		{
			Long balance = subscriber.getAccountValue1();
			balance = balance == null ? 0L : balance;
			subscriber.setAccountValue1(balance - request.member.transactionAmount);
		}
		else if ("ERR".equals(request.member.refillProfileID))
		{
			response.member.setResponseCode(115);
			return response;
		}

		// Create Response
		response.member.setMasterAccountNumber(super.getNaiNumber(subscriber.getInternationalNumber(), request.member.getSubscriberNumberNAI()));
		response.member.setLanguageIDCurrent(subscriber.getLanguageIDCurrent());
		// response.member.setPromotionAnnouncementCode(request.get);
		// response.member.setVoucherAgent(request.get);
		// response.member.setVoucherSerialNumber(request.get);
		// response.member.setVoucherGroup(request.get);
		response.member.setTransactionCurrency(request.member.getTransactionCurrency());
		response.member.setTransactionAmount(request.member.getTransactionAmount());
		response.member.setTransactionAmountConverted(request.member.getTransactionAmount());
		response.member.setCurrency1(subscriber.getCurrency1());
		response.member.setCurrency2(subscriber.getCurrency2());
		// response.member.setRefillInformation(request.get);
		response.member.setAccountAfterRefill(getAccountAfterRefill(request.member.getRequestRefillAccountAfterFlag(), subscriber));
		// response.member.setRefillFraudCount(request.get);
		response.member.setSelectedOption(request.member.getSelectedOption());
		// response.member.setSegmentationID(request.member.);
		response.member.setRefillType(request.member.getRefillType());
		response.member.setNegotiatedCapabilities(request.member.getNegotiatedCapabilities());
		// response.member.setAvailableServerCapabilities(request.member.get);
		// response.member.setAccountPrepaidEmptyLimit1(request.member.get);
		// response.member.setAccountPrepaidEmptyLimit2(request.get);
		// response.member.setTreeDefinedField(request.get);

		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	private AccountBeforeRefill getAccountBeforeRefill(Boolean requestRefillAccountBeforeFlag, SubscriberEx subscriber)
	{
		// Exit if not required
		if (requestRefillAccountBeforeFlag == null || !requestRefillAccountBeforeFlag)
			return null;

		AccountBeforeRefill result = new AccountBeforeRefill();
		result.serviceClassTemporaryExpiryDate = subscriber.getServiceClassTemporaryExpiryDate();
		result.serviceClassOriginal = subscriber.getServiceClassOriginal();
		result.serviceClassCurrent = subscriber.getServiceClassCurrent();
		result.accountFlags = subscriber.getAccountFlags();
		result.promotionPlanID = subscriber.getPromotionPlanID();
		result.serviceFeeExpiryDate = subscriber.getServiceFeeExpiryDate();
		result.supervisionExpiryDate = subscriber.getSupervisionExpiryDate();
		result.creditClearanceDate = subscriber.getCreditClearanceDate();
		result.serviceRemovalDate = subscriber.getServiceRemovalDate();
		result.accountValue1 = subscriber.getAccountValue1();
		result.accountValue2 = subscriber.getAccountValue2();
		result.dedicatedAccountInformation = subscriber.getDedicatedAccountInformation();
		result.usageAccumulatorInformation = subscriber.getUsageAccumulatorInformation();
		result.serviceOfferings = subscriber.getServiceOfferings2();
		result.communityIdList = subscriber.getCommunityIdList();
		result.offerInformationList = subscriber.getOfferInformationList();

		return result;
	}

	private AccountAfterRefill getAccountAfterRefill(Boolean requestRefillAccountAfterFlag, SubscriberEx subscriber)
	{
		// Exit if not required
		if (requestRefillAccountAfterFlag == null || !requestRefillAccountAfterFlag)
			return null;

		AccountAfterRefill result = new AccountAfterRefill();

		result.serviceClassTemporaryExpiryDate = subscriber.getServiceClassTemporaryExpiryDate();
		result.serviceClassOriginal = subscriber.getServiceClassOriginal();
		result.serviceClassCurrent = subscriber.getServiceClassCurrent();
		result.accountFlags = subscriber.getAccountFlags();
		result.promotionPlanID = subscriber.getPromotionPlanID();
		result.serviceFeeExpiryDate = subscriber.getServiceFeeExpiryDate();
		result.supervisionExpiryDate = subscriber.getSupervisionExpiryDate();
		result.creditClearanceDate = subscriber.getCreditClearanceDate();
		result.serviceRemovalDate = subscriber.getServiceRemovalDate();
		result.accountValue1 = subscriber.getAccountValue1();
		result.accountValue2 = subscriber.getAccountValue2();
		result.dedicatedAccountInformation = subscriber.getDedicatedAccountInformation();
		result.usageAccumulatorInformation = subscriber.getUsageAccumulatorInformation();
		result.serviceOfferings = subscriber.getServiceOfferings2();
		result.communityIdList = subscriber.getCommunityIdList();
		result.offerInformationList = subscriber.getOfferInformationList();

		return result;
	}

}
