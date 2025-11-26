package hxc.services.caisim.engine.soap;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.utils.protocol.caisim.HlrSubscription;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.request.soap.SetHlrSubscriberRsaRequest;
import hxc.utils.protocol.caisim.response.soap.SetHlrSubscriberRsaResponse;

public class SetHlrSubscriberRsaRequestImpl extends ImplBase<SetHlrSubscriberRsaResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public SetHlrSubscriberRsaRequestImpl(ICaiData caiData)
	{
		super(caiData);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public SetHlrSubscriberRsaResponse execute(SetHlrSubscriberRsaRequest request)
	{
		// Create the response
		SetHlrSubscriberRsaResponse response = new SetHlrSubscriberRsaResponse(request);

		synchronized (caiData.getLock())
		{
			// Get the subscriber
			Subscriber subscriber = caiData.getSubscriber(request.getMsisdn(), SubscriptionType.HLR);

			// Check if the subscriber exists
			if (subscriber == null)
				return exitWith(response, Protocol.RESPONSE_CODE_SUBSCRIBER_DATA_NOT_RECOGNIZED);
			
			// Set the RSA
			HlrSubscription hlrSub = subscriber.getHlrSubscription();
			hlrSub.setRsa(request.getRsa());
		}

		// Exit successfully
		return exitWith(response, Protocol.RESPONSE_CODE_SUCCESSFUL);
	}
}
