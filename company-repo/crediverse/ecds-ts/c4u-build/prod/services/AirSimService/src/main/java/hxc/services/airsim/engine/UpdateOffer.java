package hxc.services.airsim.engine;

import java.util.Random;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.OfferEx;
import hxc.services.airsim.model.SubscriberEx;
import hxc.utils.protocol.ucip.UpdateOfferRequest;
import hxc.utils.protocol.ucip.UpdateOfferResponse;

public class UpdateOffer extends SupportedRequest<UpdateOfferRequest, UpdateOfferResponse>
{
	public UpdateOffer()
	{
		super(UpdateOfferRequest.class);
	}

	@Override
	protected UpdateOfferResponse execute(UpdateOfferRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		UpdateOfferResponse response = new UpdateOfferResponse();
		if (!validate(request.member, response.member, injectedResponse))
			return response;

		// Get the Subscriber
		SubscriberEx subscriber = getSubscriber(request.member);
		if (subscriber == null)
		{
			response.member.setResponseCode(102);
			return response;
		}

		// Update the Offer
		OfferEx offer = subscriber.getOffers().get(request.member.getOfferID());
		if (offer == null)
		{
			offer = new OfferEx();
			offer.setOfferID(request.member.getOfferID());
			subscriber.getOffers().put(offer.getOfferID(), offer);
		}

		offer.setOfferID(request.member.getOfferID());
		if (request.member.getStartDate() != null)
			offer.setStartDate(request.member.getStartDate());
		offer.setStartDate(addDays(offer.getStartDate(), request.member.getStartDateRelative()));
		// offer.startPamPeriodIndicator = request.member.getStartPamPeriodIndicator();
		// offer.currentTimeOffset = request.member.getCurrentTimeOffset();
		if (request.member.getExpiryDate() != null)
			offer.setExpiryDate(pseudoNull(request.member.getExpiryDate()));
		offer.setExpiryDate(addDays(offer.getExpiryDate(), request.member.getExpiryDateRelative()));
		// offer.expiryPamPeriodIndicator = request.member.getExpiryPamPeriodIndicator();
		if (request.member.getStartDateTime() != null)
			offer.setStartDateTime(request.member.getStartDateTime());
		offer.setStartDateTime(addDays(offer.getStartDateTime(), request.member.getStartDateTimeRelative()));
		if (request.member.getExpiryDateTime() != null)
			offer.setExpiryDateTime(pseudoNull(request.member.getExpiryDateTime()));
		offer.setExpiryDateTime(addDays(offer.getExpiryDateTime(), request.member.getExpiryDateTimeRelative()));
		offer.setPamServiceID(request.member.getPamServiceID());
		offer.setOfferType(request.member.getOfferType());
		offer.setOfferProviderID(request.member.getOfferProviderID());
		// offer.dedicatedAccountUpdateInformation = request.member.getDedicatedAccountUpdateInformation();
		// offer.attributeUpdateInformationList = request.member.getAttributeUpdateInformationList();
		// offer.updateAction=request.member.getUpdateAction();
		offer.setProductID(request.member.getProductID());

		// Create Response
		response.member.setCurrency1(subscriber.getCurrency1());
		response.member.setCurrency2(subscriber.getCurrency2());
		response.member.setOfferID(request.member.getOfferID());
		response.member.setStartDate(request.member.getStartDate());
		response.member.setExpiryDate(request.member.getExpiryDate());
		response.member.setStartDateTime(request.member.getStartDateTime());
		response.member.setExpiryDateTime(request.member.getExpiryDateTime());
		response.member.setPamServiceID(request.member.getPamServiceID());
		response.member.setOfferType(request.member.getOfferType());
		response.member.setOfferState(0); // request.member.getOfferState());
		response.member.setOfferProviderID(request.member.getOfferProviderID());
		if( request.member.getProductID() != null )
			response.member.setProductID(request.member.getProductID());
		else
		{
			Random r = new Random();
			int randomProductID = r.nextInt((10 - 1) + 1) + 1;
			response.member.setProductID(randomProductID);
		}
		// response.member.setDedicatedAccountChangeInformation(request.member.getDedicatedAccountChangeInformation());

		return response;
	}

}
