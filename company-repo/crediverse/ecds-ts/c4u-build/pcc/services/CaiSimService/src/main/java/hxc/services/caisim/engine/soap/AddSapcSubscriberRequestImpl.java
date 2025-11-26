package hxc.services.caisim.engine.soap;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.SapcSubscription;
import hxc.utils.protocol.caisim.request.soap.AddSapcSubscriberRequest;
import hxc.utils.protocol.caisim.response.soap.AddSapcSubscriberResponse;

public class AddSapcSubscriberRequestImpl extends ImplBase<AddSapcSubscriberResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public AddSapcSubscriberRequestImpl(ICaiData caiData)
	{
		super(caiData);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public AddSapcSubscriberResponse execute(AddSapcSubscriberRequest request)
	{
		// Create the response
		AddSapcSubscriberResponse response = new AddSapcSubscriberResponse(request);

		synchronized (caiData.getLock())
		{
			// Get the subscriber
			Subscriber subscriber = caiData.getSubscriber(request.getMsisdn(), SubscriptionType.ANY);

			// Check if the subscriber exists
			if (subscriber == null)
				subscriber = caiData.createSubscriber(request.getMsisdn());

			// Set the CAI subscription to the subscriber
			SapcSubscription sapcSub = new SapcSubscription();
			sapcSub.setGroups(request.getGroups().getGroup());
			subscriber.setSapcSubscription(sapcSub);
		}

		// Exit successfully
		return exitWith(response, Protocol.RESPONSE_CODE_SUCCESSFUL);
	}
}