package hxc.services.caisim.engine.soap;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.utils.protocol.caisim.HlrSubscription;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.request.soap.UpdateHlrSubscriberPdpContextsRequest;
import hxc.utils.protocol.caisim.response.soap.UpdateHlrSubscriberPdpContextsResponse;

public class UpdateHlrSubscriberPdpContextsRequestImpl extends ImplBase<UpdateHlrSubscriberPdpContextsResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public UpdateHlrSubscriberPdpContextsRequestImpl(ICaiData caiData)
	{
		super(caiData);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public UpdateHlrSubscriberPdpContextsResponse execute(UpdateHlrSubscriberPdpContextsRequest request)
	{
		// Create the response
		UpdateHlrSubscriberPdpContextsResponse response = new UpdateHlrSubscriberPdpContextsResponse(request);

		synchronized (caiData.getLock())
		{
			// Get the subscriber
			Subscriber subscriber = caiData.getSubscriber(request.getMsisdn(), SubscriptionType.HLR);

			// Check if the subscriber exists
			if (subscriber == null)
				return exitWith(response, Protocol.RESPONSE_CODE_SUBSCRIBER_DATA_NOT_RECOGNIZED);
			
			// Set the PDP Contexts
			HlrSubscription hlrSub = subscriber.getHlrSubscription();
			hlrSub.setPdpContexts(request.getPdpContexts().getPdpContext());
		}

		// Exit successfully
		return exitWith(response, Protocol.RESPONSE_CODE_SUCCESSFUL);
	}
}
