package hxc.services.caisim.engine.soap;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.SapcGroups;
import hxc.utils.protocol.caisim.request.soap.GetSapcGroupsRequest;
import hxc.utils.protocol.caisim.response.soap.GetSapcGroupsResponse;

public class GetSapcGroupsRequestImpl extends ImplBase<GetSapcGroupsResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public GetSapcGroupsRequestImpl(ICaiData caiData)
	{
		super(caiData);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public GetSapcGroupsResponse execute(GetSapcGroupsRequest request)
	{
		// Create the response
		GetSapcGroupsResponse response = new GetSapcGroupsResponse(request);

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
			response.setGroups(new SapcGroups(subscriber.getSapcSubscription().getGroups()));
		}

		// Exit successfully
		return exitWith(response, Protocol.RESPONSE_CODE_SUCCESSFUL);
	}
}