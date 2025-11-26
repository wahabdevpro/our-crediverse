package hxc.services.caisim.engine.soap;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.utils.protocol.caisim.HlrSubscription;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.request.soap.SetHlrSubscriberNamRequest;
import hxc.utils.protocol.caisim.response.soap.SetHlrSubscriberNamResponse;

public class SetHlrSubscriberNamRequestImpl extends ImplBase<SetHlrSubscriberNamResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public SetHlrSubscriberNamRequestImpl(ICaiData caiData)
	{
		super(caiData);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public SetHlrSubscriberNamResponse execute(SetHlrSubscriberNamRequest request)
	{
		// Create the response
		SetHlrSubscriberNamResponse response = new SetHlrSubscriberNamResponse(request);

		synchronized (caiData.getLock())
		{
			// Get the subscriber
			Subscriber subscriber = caiData.getSubscriber(request.getMsisdn(), SubscriptionType.HLR);

			// Check if the subscriber exists
			if (subscriber == null)
				return exitWith(response, Protocol.RESPONSE_CODE_SUBSCRIBER_DATA_NOT_RECOGNIZED);
			
			// Set the NAM
			HlrSubscription hlrSub = subscriber.getHlrSubscription();
			hlrSub.setNam(request.getNam());
		}

		// Exit successfully
		return exitWith(response, Protocol.RESPONSE_CODE_SUCCESSFUL);
	}
}
