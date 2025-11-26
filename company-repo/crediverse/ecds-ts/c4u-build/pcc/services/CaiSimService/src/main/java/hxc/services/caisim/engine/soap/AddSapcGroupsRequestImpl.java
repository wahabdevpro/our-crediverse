package hxc.services.caisim.engine.soap;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.SapcSubscription;
import hxc.utils.protocol.caisim.request.soap.AddSapcGroupsRequest;
import hxc.utils.protocol.caisim.response.soap.AddSapcGroupsResponse;

public class AddSapcGroupsRequestImpl extends ImplBase<AddSapcGroupsResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public AddSapcGroupsRequestImpl(ICaiData caiData)
	{
		super(caiData);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public AddSapcGroupsResponse execute(AddSapcGroupsRequest request)
	{
		// Create the response
		AddSapcGroupsResponse response = new AddSapcGroupsResponse(request);

		synchronized (caiData.getLock())
		{
			// Get the subscriber
			Subscriber subscriber = caiData.getSubscriber(request.getMsisdn(), SubscriptionType.SAPC);

			// Check if the subscriber exists
			if (subscriber == null)
				return exitWith(response, Protocol.RESPONSE_CODE_SUBSCRIBER_DATA_NOT_RECOGNIZED);

			// Set the SAPC Groups to the subscriber
			SapcSubscription sapcSub = subscriber.getSapcSubscription();
			sapcSub.addGroups(request.getGroups().getGroup(), true);		
		}

		// Exit successfully
		return exitWith(response, Protocol.RESPONSE_CODE_SUCCESSFUL);
	}
}
