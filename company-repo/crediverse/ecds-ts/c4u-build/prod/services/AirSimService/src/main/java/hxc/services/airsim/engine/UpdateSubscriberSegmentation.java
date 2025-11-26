package hxc.services.airsim.engine;

import java.util.Map;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.utils.protocol.ucip.ServiceOfferings;
import hxc.utils.protocol.ucip.ServiceOfferingsResult;
import hxc.utils.protocol.ucip.UpdateSubscriberSegmentationRequest;
import hxc.utils.protocol.ucip.UpdateSubscriberSegmentationResponse;

public class UpdateSubscriberSegmentation extends SupportedRequest<UpdateSubscriberSegmentationRequest, UpdateSubscriberSegmentationResponse>
{
	public UpdateSubscriberSegmentation()
	{
		super(UpdateSubscriberSegmentationRequest.class);
	}

	@Override
	protected UpdateSubscriberSegmentationResponse execute(UpdateSubscriberSegmentationRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		UpdateSubscriberSegmentationResponse response = new UpdateSubscriberSegmentationResponse();
		if (!validate(request.member, response.member, injectedResponse))
			return response;

		// Get the Subscriber
		SubscriberEx subscriber = getSubscriber(request.member);
		if (subscriber == null)
		{
			response.member.setResponseCode(102);
			return response;
		}

		// Update the Subscriber Segmentation
		Map<Integer, ServiceOfferings> serviceOfferings = subscriber.getServiceOfferings();
		subscriber.setAccountGroupID(request.member.getAccountGroupID());
		ServiceOfferingsResult[] serviceOfferingsResult = null;
		if (request.member.getServiceOfferings() != null)
		{
			serviceOfferings.clear();
			serviceOfferingsResult = new ServiceOfferingsResult[request.member.getServiceOfferings().length];
			int index = 0;
			for (ServiceOfferings serviceOffering : request.member.getServiceOfferings())
			{
				serviceOfferings.put(serviceOffering.serviceOfferingID, serviceOffering);
				ServiceOfferingsResult so = new ServiceOfferingsResult();
				so.serviceOfferingID = serviceOffering.serviceOfferingID;
				serviceOfferingsResult[index++] = so;
			}
		}

		// Create Response
		response.member.setServiceOfferingsResult(serviceOfferingsResult);
		response.member.setCurrency1(subscriber.getCurrency1());
		response.member.setCurrency2(subscriber.getCurrency2());
		response.member.setServiceFeeInformationList(null);

		return response;
	}

}
