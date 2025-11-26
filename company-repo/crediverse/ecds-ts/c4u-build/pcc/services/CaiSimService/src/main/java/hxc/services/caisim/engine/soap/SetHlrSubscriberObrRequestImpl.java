package hxc.services.caisim.engine.soap;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.utils.protocol.caisim.HlrSubscription;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.request.soap.SetHlrSubscriberObrRequest;
import hxc.utils.protocol.caisim.response.soap.SetHlrSubscriberObrResponse;

public class SetHlrSubscriberObrRequestImpl extends ImplBase<SetHlrSubscriberObrResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public SetHlrSubscriberObrRequestImpl(ICaiData caiData)
	{
		super(caiData);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public SetHlrSubscriberObrResponse execute(SetHlrSubscriberObrRequest request)
	{
		// Create the response
		SetHlrSubscriberObrResponse response = new SetHlrSubscriberObrResponse(request);

		synchronized (caiData.getLock())
		{
			// Get the subscriber
			Subscriber subscriber = caiData.getSubscriber(request.getMsisdn(), SubscriptionType.HLR);

			// Check if the subscriber exists
			if (subscriber == null)
				return exitWith(response, Protocol.RESPONSE_CODE_SUBSCRIBER_DATA_NOT_RECOGNIZED);
			
			// Set the OBR
			HlrSubscription hlrSub = subscriber.getHlrSubscription();
			hlrSub.setObr(request.getObr());
		}

		// Exit successfully
		return exitWith(response, Protocol.RESPONSE_CODE_SUCCESSFUL);
	}
}
