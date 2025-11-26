package hxc.services.caisim.engine.soap;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.SapcGroup;
import hxc.utils.protocol.caisim.SapcGroupId;
import hxc.utils.protocol.caisim.SapcSubscription;
import hxc.utils.protocol.caisim.request.soap.SetSapcQuotaRequest;
import hxc.utils.protocol.caisim.response.soap.SetSapcQuotaResponse;

public class SetSapcQuotaRequestImpl extends ImplBase<SetSapcQuotaResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public SetSapcQuotaRequestImpl(ICaiData caiData)
	{
		super(caiData);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public SetSapcQuotaResponse execute(SetSapcQuotaRequest request)
	{
		// Create the response
		SetSapcQuotaResponse response = new SetSapcQuotaResponse(request);

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
			sapcGroup.setQuota(request.getGroup().getQuota());
		}

		// Exit successfully
		return exitWith(response, Protocol.RESPONSE_CODE_SUCCESSFUL);
	}
}
