package hxc.services.caisim.engine.soap;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.SapcGroups;
import hxc.utils.protocol.caisim.SapcSubscription;
import hxc.utils.protocol.caisim.request.soap.GetSapcSubscriberRequest;
import hxc.utils.protocol.caisim.response.soap.GetSapcSubscriberResponse;

public class GetSapcSubscriberRequestImpl extends ImplBase<GetSapcSubscriberResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public GetSapcSubscriberRequestImpl(ICaiData caiData)
	{
		super(caiData);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////
	
	public GetSapcSubscriberResponse execute(GetSapcSubscriberRequest request)
	{
		// Create the response
		GetSapcSubscriberResponse response = new GetSapcSubscriberResponse(request);

		// Set the MSISDN for the subscriber
		response.setMsisdn(request.getMsisdn());
		
		synchronized (caiData.getLock())
		{
			// Get the subscriber
			Subscriber subscriber = caiData.getSubscriber(request.getMsisdn(), SubscriptionType.SAPC);

			// Check if the subscriber exists
			if (subscriber == null)
				return exitWith(response, Protocol.RESPONSE_CODE_SUBSCRIBER_DATA_NOT_RECOGNIZED);

			// Set the SAPC Groups from the subscriber
			SapcSubscription sapcSub = subscriber.getSapcSubscription();
			response.setGroups(new SapcGroups(sapcSub.getGroups()));			
		}

		// Exit successfully
		return exitWith(response, Protocol.RESPONSE_CODE_SUCCESSFUL);
	}
}
