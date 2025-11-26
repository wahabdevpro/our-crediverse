package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.services.airsim.protocol.DedicatedAccount;
import hxc.utils.protocol.ucip.DedicatedAccountInformation;
import hxc.utils.protocol.ucip.GetBalanceAndDateRequest;
import hxc.utils.protocol.ucip.GetBalanceAndDateResponse;
import hxc.utils.protocol.ucip.SubDedicatedAccountInformation;

public class GetBalanceAndDate extends SupportedRequest<GetBalanceAndDateRequest, GetBalanceAndDateResponse>
{
	public GetBalanceAndDate()
	{
		super(GetBalanceAndDateRequest.class);
	}

	@Override
	protected GetBalanceAndDateResponse execute(GetBalanceAndDateRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		GetBalanceAndDateResponse response = new GetBalanceAndDateResponse();
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
		response.member.setServiceClassCurrent(subscriber.getServiceClassCurrent());
		response.member.setCurrency1(subscriber.getCurrency1());
		response.member.setAccountValue1(subscriber.getAccountValue1());
		response.member.setAggregatedBalance1(subscriber.getAggregatedBalance1());
		response.member.setCurrency2(subscriber.getCurrency2());
		response.member.setAccountValue2(subscriber.getAccountValue2());
		response.member.setAggregatedBalance2(subscriber.getAggregatedBalance2());
		DedicatedAccountInformation[] daInfos = null;
		if (subscriber.getDedicatedAccounts().size() > 0)
		{
			daInfos = new DedicatedAccountInformation[subscriber.getDedicatedAccounts().size()];
			int index = 0;
			for (DedicatedAccount da : subscriber.getDedicatedAccounts().values())
			{
				DedicatedAccountInformation daInfo = new DedicatedAccountInformation();
				daInfo.dedicatedAccountID = da.getDedicatedAccountID();
				daInfo.dedicatedAccountValue1 = da.getDedicatedAccountValue1();
				daInfo.dedicatedAccountValue2 = da.getDedicatedAccountValue2();
				daInfo.expiryDate = da.getExpiryDate();
				daInfo.startDate = da.getStartDate();
				daInfo.pamServiceID = da.getPamServiceID();
				daInfo.offerID = da.getOfferID();
				daInfo.productID = da.getProductID();
				daInfo.dedicatedAccountRealMoneyFlag = da.getDedicatedAccountRealMoneyFlag();
				daInfo.closestExpiryDate = da.getClosestExpiryDate();
				daInfo.closestExpiryValue1 = da.getClosestExpiryValue1();
				daInfo.closestExpiryValue2 = da.getClosestExpiryValue2();
				daInfo.closestAccessibleDate = da.getClosestAccessibleDate();
				daInfo.closestAccessibleValue1 = da.getClosestAccessibleValue1();
				daInfo.closestAccessibleValue2 = da.getClosestAccessibleValue2();
				daInfo.dedicatedAccountActiveValue1 = da.getDedicatedAccountActiveValue1();
				daInfo.dedicatedAccountActiveValue2 = da.getDedicatedAccountActiveValue2();
				daInfo.dedicatedAccountUnitType = da.getDedicatedAccountUnitType();

				if (request.member.requestSubDedicatedAccountDetailsFlag != null && request.member.requestSubDedicatedAccountDetailsFlag)
				{
					SubDedicatedAccountInformation[] subDaInfos = null;
					if (da.getSubDedicatedAccountInformation() != null && da.getSubDedicatedAccountInformation().length > 0)
					{

						subDaInfos = new SubDedicatedAccountInformation[da.getSubDedicatedAccountInformation().length];
						int index2 = 0;
						for (hxc.services.airsim.protocol.SubDedicatedAccountInformation subDaInfo : da.getSubDedicatedAccountInformation())
						{

							SubDedicatedAccountInformation subDa = new SubDedicatedAccountInformation();
							subDa.dedicatedAccountValue1 = subDaInfo.getDedicatedAccountValue1();
							subDa.dedicatedAccountValue2 = subDaInfo.getDedicatedAccountValue2();
							subDa.startDate = subDaInfo.getStartDate();
							subDa.expiryDate = subDaInfo.getExpiryDate();
							subDaInfos[index2++] = subDa;

						}

					}

					daInfo.subDedicatedAccountInformation = subDaInfos;
					daInfo.compositeDedicatedAccountFlag = true;
				}

				daInfos[index++] = daInfo;
			}
		}

		response.member.setDedicatedAccountInformation(daInfos);
		response.member.setSupervisionExpiryDate(subscriber.getSupervisionExpiryDate());
		response.member.setServiceFeeExpiryDate(subscriber.getServiceFeeExpiryDate());
		response.member.setCreditClearanceDate(subscriber.getCreditClearanceDate());
		response.member.setServiceRemovalDate(subscriber.getServiceRemovalDate());
		response.member.setLanguageIDCurrent(subscriber.getLanguageIDCurrent());
		response.member.setTemporaryBlockedFlag(subscriber.getTemporaryBlockedFlag());
		// response.member.setChargingResultInformation(subscriber.getChargingResultInformation());
		response.member.setAccountFlagsAfter(subscriber.getAccountFlagsAfter());
		response.member.setAccountFlagsBefore(subscriber.getAccountFlagsBefore());
		response.member.setOfferInformationList(subscriber.getOfferInformationList(subscriber));
		response.member.setAccountPrepaidEmptyLimit1(subscriber.getAccountPrepaidEmptyLimit1());
		response.member.setAccountPrepaidEmptyLimit2(subscriber.getAccountPrepaidEmptyLimit2());
		// response.member.setAggregatedBalanceInformation(subscriber.getAggregatedBalanceInformation());

		return response;

	}

}
