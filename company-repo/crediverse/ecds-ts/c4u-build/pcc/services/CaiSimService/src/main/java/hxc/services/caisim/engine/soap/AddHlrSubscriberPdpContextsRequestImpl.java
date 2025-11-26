package hxc.services.caisim.engine.soap;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.utils.protocol.caisim.HlrSubscription;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.request.soap.AddHlrSubscriberPdpContextsRequest;
import hxc.utils.protocol.caisim.response.soap.AddHlrSubscriberPdpContextsResponse;

public class AddHlrSubscriberPdpContextsRequestImpl extends ImplBase<AddHlrSubscriberPdpContextsResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public AddHlrSubscriberPdpContextsRequestImpl(ICaiData caiData)
	{
		super(caiData);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public AddHlrSubscriberPdpContextsResponse execute(AddHlrSubscriberPdpContextsRequest request)
	{
		// Create the response
		AddHlrSubscriberPdpContextsResponse response = new AddHlrSubscriberPdpContextsResponse(request);

		synchronized (caiData.getLock())
		{
			// Get the subscriber
			Subscriber subscriber = caiData.getSubscriber(request.getMsisdn(), SubscriptionType.HLR);

			// Check if the subscriber exists
			if (subscriber == null)
				return exitWith(response, Protocol.RESPONSE_CODE_SUBSCRIBER_DATA_NOT_RECOGNIZED);
			
			// Add the PDP Contexts
			HlrSubscription hlrSub = subscriber.getHlrSubscription();
			hlrSub.addPdpContexts(request.getPdpContexts().getPdpContext());
		}

		// Exit successfully
		return exitWith(response, Protocol.RESPONSE_CODE_SUCCESSFUL);
	}
}
