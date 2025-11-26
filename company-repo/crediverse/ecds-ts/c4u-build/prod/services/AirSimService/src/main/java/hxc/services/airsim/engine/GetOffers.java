package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.utils.protocol.ucip.GetOffersRequest;
import hxc.utils.protocol.ucip.GetOffersResponse;
import hxc.utils.protocol.ucip.OfferInformation;

public class GetOffers extends SupportedRequest<GetOffersRequest, GetOffersResponse>
{

	public GetOffers()
	{
		super(GetOffersRequest.class);
	}

	@Override
	protected GetOffersResponse execute(GetOffersRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		GetOffersResponse response = new GetOffersResponse();
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
		OfferInformation[] result = subscriber.getOfferInformation(subscriber).clone();
		for (OfferInformation entry : result)
		{
			entry.offerProviderID = getNaiNumber(entry.offerProviderID, request.member.getSubscriberNumberNAI());
		}
		response.member.setOfferInformation(result);
		response.member.setCurrency1(subscriber.getCurrency1());
		response.member.setCurrency2(subscriber.getCurrency2());

		return response;
	}

}
