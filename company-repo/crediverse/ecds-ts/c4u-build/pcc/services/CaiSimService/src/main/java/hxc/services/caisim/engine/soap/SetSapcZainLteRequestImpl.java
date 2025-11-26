package hxc.services.caisim.engine.soap;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.SapcGroup;
import hxc.utils.protocol.caisim.SapcGroupId;
import hxc.utils.protocol.caisim.SapcSubscription;
import hxc.utils.protocol.caisim.request.soap.SetSapcZainLteRequest;
import hxc.utils.protocol.caisim.response.soap.SetSapcZainLteResponse;

public class SetSapcZainLteRequestImpl extends ImplBase<SetSapcZainLteResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public SetSapcZainLteRequestImpl(ICaiData caiData)
	{
		super(caiData);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public SetSapcZainLteResponse execute(SetSapcZainLteRequest request)
	{
		// Create the response
		SetSapcZainLteResponse response = new SetSapcZainLteResponse(request);

		synchronized (caiData.getLock())
		{
			// Get the subscriber
			Subscriber subscriber = caiData.getSubscriber(request.getMsisdn(), SubscriptionType.SAPC);

			// Check if the subscriber exists
			if (subscriber == null)
				return exitWith(response, Protocol.RESPONSE_CODE_SUBSCRIBER_DATA_NOT_RECOGNIZED);

			// Set the quota to the subscriber's group
			SapcSubscription sapc = subscriber.getSapcSubscription();
			SapcGroup sapcGroup = sapc.getGroup(new SapcGroupId(request.getGroup().getId()));
			if (sapcGroup == null)
				return exitWith(response, Protocol.RESPONSE_CODE_NO_SUCH_GROUP_ID);
			sapcGroup.setZainLte(request.getGroup().getZainLte());
		}

		// Exit successfully
		return exitWith(response, Protocol.RESPONSE_CODE_SUCCESSFUL);
	}
}
