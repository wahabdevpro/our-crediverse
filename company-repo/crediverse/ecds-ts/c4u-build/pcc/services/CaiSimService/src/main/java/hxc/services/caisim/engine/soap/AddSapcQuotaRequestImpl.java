package hxc.services.caisim.engine.soap;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.SapcGroup;
import hxc.utils.protocol.caisim.SapcGroupId;
import hxc.utils.protocol.caisim.SapcSubscription;
import hxc.utils.protocol.caisim.request.soap.AddSapcQuotaRequest;
import hxc.utils.protocol.caisim.response.soap.AddSapcQuotaResponse;

public class AddSapcQuotaRequestImpl extends ImplBase<AddSapcQuotaResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public AddSapcQuotaRequestImpl(ICaiData caiData)
	{
		super(caiData);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public AddSapcQuotaResponse execute(AddSapcQuotaRequest request)
	{
		// Create the response
		AddSapcQuotaResponse response = new AddSapcQuotaResponse(request);

		synchronized (caiData.getLock())
		{
			// Get the subscriber
			Subscriber subscriber = caiData.getSubscriber(request.getMsisdn(), SubscriptionType.SAPC);

			// Check if the subscriber exists
			if (subscriber == null)
				return exitWith(response, Protocol.RESPONSE_CODE_SUBSCRIBER_DATA_NOT_RECOGNIZED);

			// Add the quota to the subscriber's group
			SapcSubscription sapc = subscriber.getSapcSubscription();
			SapcGroup sapcGroup = sapc.getGroup(new SapcGroupId(request.getGroup().getId()));
			if (sapcGroup == null)
				return exitWith(response, Protocol.RESPONSE_CODE_NO_SUCH_GROUP_ID);
			sapcGroup.setQuota(sapcGroup.getQuota() + request.getGroup().getQuota());
		}

		// Exit successfully
		return exitWith(response, Protocol.RESPONSE_CODE_SUCCESSFUL);
	}
}
