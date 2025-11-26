package hxc.services.caisim.engine.soap;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.SapcGroup;
import hxc.utils.protocol.caisim.SapcSubscription;
import hxc.utils.protocol.caisim.request.soap.GetSapcGroupRequest;
import hxc.utils.protocol.caisim.response.soap.GetSapcGroupResponse;

public class GetSapcGroupRequestImpl extends ImplBase<GetSapcGroupResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public GetSapcGroupRequestImpl(ICaiData caiData)
	{
		super(caiData);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public GetSapcGroupResponse execute(GetSapcGroupRequest request)
	{
		// Create the response
		GetSapcGroupResponse response = new GetSapcGroupResponse(request);

		synchronized (caiData.getLock())
		{
			// Get the subscriber
			Subscriber subscriber = caiData.getSubscriber(request.getMsisdn(), SubscriptionType.SAPC);

			// Set the MSISDN for the subscriber
			response.setMsisdn(request.getMsisdn());

			// Check if the subscriber exists
			if (subscriber == null)
				return exitWith(response, Protocol.RESPONSE_CODE_SUBSCRIBER_DATA_NOT_RECOGNIZED);

			// Set the SAPC Group from the subscriber
			SapcSubscription sapc = subscriber.getSapcSubscription();
			SapcGroup sapcGroup = sapc.getGroup(request.getGroup());
			if (sapcGroup == null)
				return exitWith(response, Protocol.RESPONSE_CODE_NO_SUCH_GROUP_ID);
			response.setGroup(sapcGroup);
		}

		// Exit successfully
		return exitWith(response, Protocol.RESPONSE_CODE_SUCCESSFUL);
	}
}
