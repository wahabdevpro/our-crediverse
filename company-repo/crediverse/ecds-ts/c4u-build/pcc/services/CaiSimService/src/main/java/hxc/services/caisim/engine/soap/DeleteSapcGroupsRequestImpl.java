package hxc.services.caisim.engine.soap;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.SapcSubscription;
import hxc.utils.protocol.caisim.request.soap.DeleteSapcGroupsRequest;
import hxc.utils.protocol.caisim.response.soap.DeleteSapcGroupsResponse;

public class DeleteSapcGroupsRequestImpl extends ImplBase<DeleteSapcGroupsResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public DeleteSapcGroupsRequestImpl(ICaiData caiData)
	{
		super(caiData);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public DeleteSapcGroupsResponse execute(DeleteSapcGroupsRequest request)
	{
		// Create the response
		DeleteSapcGroupsResponse response = new DeleteSapcGroupsResponse(request);

		synchronized (caiData.getLock())
		{
			// Get the subscriber
			Subscriber subscriber = caiData.getSubscriber(request.getMsisdn(), SubscriptionType.SAPC);

			// Check if the subscriber exists
			if (subscriber == null)
				return exitWith(response, Protocol.RESPONSE_CODE_SUBSCRIBER_DATA_NOT_RECOGNIZED);

			// Delete the SAPC Groups
			SapcSubscription sapcSub = subscriber.getSapcSubscription();
			sapcSub.deleteGroups(request.getGroups());
		}

		// Exit successfully
		return exitWith(response, Protocol.RESPONSE_CODE_SUCCESSFUL);
	}
}