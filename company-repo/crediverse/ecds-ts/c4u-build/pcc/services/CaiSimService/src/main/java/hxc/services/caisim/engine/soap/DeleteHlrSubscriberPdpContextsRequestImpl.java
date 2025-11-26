package hxc.services.caisim.engine.soap;

import java.net.InetAddress;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.utils.protocol.caisim.DeletedPdpContext;
import hxc.utils.protocol.caisim.HlrSubscription;
import hxc.utils.protocol.caisim.PdpContext;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.request.soap.DeleteHlrSubscriberPdpContextsRequest;
import hxc.utils.protocol.caisim.response.soap.DeleteHlrSubscriberPdpContextsResponse;

public class DeleteHlrSubscriberPdpContextsRequestImpl extends ImplBase<DeleteHlrSubscriberPdpContextsResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public DeleteHlrSubscriberPdpContextsRequestImpl(ICaiData caiData)
	{
		super(caiData);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public DeleteHlrSubscriberPdpContextsResponse execute(DeleteHlrSubscriberPdpContextsRequest request)
	{
		// Create the response
		DeleteHlrSubscriberPdpContextsResponse response = new DeleteHlrSubscriberPdpContextsResponse(request);

		synchronized (caiData.getLock())
		{
			// Get the subscriber
			Subscriber subscriber = caiData.getSubscriber(request.getMsisdn(), SubscriptionType.HLR);

			// Check if the subscriber exists
			if (subscriber == null)
				return exitWith(response, Protocol.RESPONSE_CODE_SUBSCRIBER_DATA_NOT_RECOGNIZED);
			
			// Delete the PDP Contexts
			HlrSubscription hlrSub = subscriber.getHlrSubscription();
			for (DeletedPdpContext deletedPdp : request.getPdpContexts().getPdpContext())
			{
				try
				{
					if (deletedPdp.getPdpId() != PdpContext.INVALID_PDP_ID)
						hlrSub.deletePdpContextByPdpId(deletedPdp.getPdpId());
					else if (deletedPdp.getApnId() >= 0)
					{
						if (deletedPdp.getPdpAddress().isEmpty())
							hlrSub.deletePdpContextsByApnId(deletedPdp.getApnId());
						else
							hlrSub.deletePdpContext(deletedPdp.getApnId(), InetAddress.getByName(deletedPdp.getPdpAddress()));
					}
				}
				catch(Exception e)
				{
					
				}
			}
		}

		// Exit successfully
		return exitWith(response, Protocol.RESPONSE_CODE_SUCCESSFUL);
	}
}
