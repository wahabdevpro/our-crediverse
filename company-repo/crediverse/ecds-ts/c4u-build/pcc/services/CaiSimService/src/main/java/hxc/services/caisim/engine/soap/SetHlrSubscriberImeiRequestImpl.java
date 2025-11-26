package hxc.services.caisim.engine.soap;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.utils.protocol.caisim.HlrSubscription;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.request.soap.SetHlrSubscriberImeiRequest;
import hxc.utils.protocol.caisim.response.soap.SetHlrSubscriberImeiResponse;

public class SetHlrSubscriberImeiRequestImpl extends ImplBase<SetHlrSubscriberImeiResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public SetHlrSubscriberImeiRequestImpl(ICaiData caiData)
	{
		super(caiData);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public SetHlrSubscriberImeiResponse execute(SetHlrSubscriberImeiRequest request)
	{
		// Create the response
		SetHlrSubscriberImeiResponse response = new SetHlrSubscriberImeiResponse(request);

		synchronized (caiData.getLock())
		{
			// Get the subscriber
			Subscriber subscriber = caiData.getSubscriber(request.getMsisdn(), SubscriptionType.HLR);

			// Check if the subscriber exists
			if (subscriber == null)
				return exitWith(response, Protocol.RESPONSE_CODE_SUBSCRIBER_DATA_NOT_RECOGNIZED);
			
			// Set the IMEI
			HlrSubscription hlrSub = subscriber.getHlrSubscription();
			hlrSub.setImei(request.getImei());
		}

		// Exit successfully
		return exitWith(response, Protocol.RESPONSE_CODE_SUCCESSFUL);
	}
}
