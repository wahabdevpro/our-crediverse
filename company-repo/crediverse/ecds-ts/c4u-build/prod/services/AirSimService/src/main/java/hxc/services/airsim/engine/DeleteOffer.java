package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.OfferEx;
import hxc.services.airsim.model.SubscriberEx;
import hxc.utils.protocol.acip.DeleteOfferRequest;
import hxc.utils.protocol.acip.DeleteOfferResponse;

public class DeleteOffer extends SupportedRequest<DeleteOfferRequest, DeleteOfferResponse>
{

	public DeleteOffer()
	{
		super(DeleteOfferRequest.class);
	}

	@Override
	protected DeleteOfferResponse execute(DeleteOfferRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		DeleteOfferResponse response = new DeleteOfferResponse();
		if (!validate(request.member, response.member, injectedResponse))
			return response;

		// Get the Subscriber
		SubscriberEx subscriber = getSubscriber(request.member);
		if (subscriber == null)
		{
			response.member.setResponseCode(102);
			return response;
		}

		// Delete the Offer
		OfferEx offer = subscriber.getOffers().get(request.member.getOfferID());
		if (offer != null)
		{
			subscriber.getOffers().remove(request.member.getOfferID());
		}
		else
		{
			response.member.setResponseCode(165);
			return response;
		}

		// Create Response
		response.member.setOfferID(request.member.getOfferID());
		response.member.setCurrency1(subscriber.getCurrency1());
		response.member.setCurrency2(subscriber.getCurrency2());

		if (offer != null)
		{
			response.member.setStartDate(offer.getStartDate());
			response.member.setExpiryDate(offer.getExpiryDate());
			response.member.setStartDateTime(offer.getStartDateTime());
			response.member.setExpiryDateTime(offer.getExpiryDateTime());
			// response.member.setOfferInformationList(offer.offerInformationList);
			response.member.setPamServiceID(offer.getPamServiceID());
			// response.member.setDedicatedAccountDeleteInformation(offer.dedicatedAccountDeleteInformation);
			// response.member.setFafInformationList(offer.fafInformationList);
			response.member.setOfferType(offer.getOfferType());
			response.member.setOfferState(offer.getOfferState());
			response.member.setOfferProviderID(offer.getOfferProviderID());
			// response.member.setAttributeInformationList(offer.attributeInformationList);
		}

		return response;
	}

}
