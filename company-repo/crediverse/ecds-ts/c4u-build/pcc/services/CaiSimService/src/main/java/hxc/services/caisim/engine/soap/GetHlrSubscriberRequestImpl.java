package hxc.services.caisim.engine.soap;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.utils.protocol.caisim.HlrSubscription;
import hxc.utils.protocol.caisim.PdpContexts;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.request.soap.GetHlrSubscriberRequest;
import hxc.utils.protocol.caisim.response.soap.GetHlrSubscriberResponse;

public class GetHlrSubscriberRequestImpl extends ImplBase<GetHlrSubscriberResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public GetHlrSubscriberRequestImpl(ICaiData caiData)
	{
		super(caiData);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public GetHlrSubscriberResponse execute(GetHlrSubscriberRequest request)
	{
		// Create the response
		GetHlrSubscriberResponse response = new GetHlrSubscriberResponse(request);

		// Set the MSISDN for the subscriber
		response.setMsisdn(request.getMsisdn());
		
		synchronized (caiData.getLock())
		{
			// Get the subscriber
			Subscriber subscriber = caiData.getSubscriber(request.getMsisdn(), SubscriptionType.HLR);

			// Check if the subscriber exists
			if (subscriber == null)
				return exitWith(response, Protocol.RESPONSE_CODE_SUBSCRIBER_DATA_NOT_RECOGNIZED);
			
			HlrSubscription hlrsUb = subscriber.getHlrSubscription();
			response.setObr(hlrsUb.getObr());
			response.setRsa(hlrsUb.getRsa());
			response.setCfnrc(hlrsUb.getCfnrc());
			response.setNam(hlrsUb.getNam());
			response.setPdpCp(hlrsUb.getPdpCp());
			response.setPdpContexts(new PdpContexts(hlrsUb.getPdpContexts()));
			response.setImei(hlrsUb.getImei());

			// Extract data from the HLR subscription and set into the response
		}

		// Exit successfully
		return exitWith(response, Protocol.RESPONSE_CODE_SUCCESSFUL);
	}
}
