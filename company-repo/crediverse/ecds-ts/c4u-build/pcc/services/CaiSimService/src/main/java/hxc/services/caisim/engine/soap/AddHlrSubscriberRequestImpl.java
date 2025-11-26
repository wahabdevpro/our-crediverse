package hxc.services.caisim.engine.soap;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.utils.protocol.caisim.HlrSubscription;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.request.soap.AddHlrSubscriberRequest;
import hxc.utils.protocol.caisim.response.soap.AddHlrSubscriberResponse;

public class AddHlrSubscriberRequestImpl extends ImplBase<AddHlrSubscriberResponse>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public AddHlrSubscriberRequestImpl(ICaiData caiData)
	{
		super(caiData);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public AddHlrSubscriberResponse execute(AddHlrSubscriberRequest request)
	{
		// Create the response
		AddHlrSubscriberResponse response = new AddHlrSubscriberResponse(request);

		synchronized (caiData.getLock())
		{
			int count = request.getCount();
			for(int i = 0; i < count; i++)
			{
				String msisdn;
				String imei;
				try
				{
					msisdn = Long.toString((Long.parseLong(request.getMsisdn()) + i));
				} catch(NumberFormatException nfe)
				{						
					return exitWith(response, Protocol.RESPONSE_CODE_MSISDN_IS_NOT_A_NUMBER);
				}
				try
				{
					imei = Long.toString((Long.parseLong(request.getImei()) + i));
				} catch(NumberFormatException nfe)
				{						
					return exitWith(response, Protocol.RESPONSE_CODE_IMEI_IS_NOT_A_NUMBER);
				}
				// Get the subscriber
				Subscriber subscriber = caiData.getSubscriber(msisdn, SubscriptionType.ANY);
	
				// Check if the subscriber exists
				if (subscriber == null)
				{					
					subscriber = caiData.createSubscriber(msisdn);
				}
	
				// Create a new HLR subscription object
				HlrSubscription hlrSub = new HlrSubscription();
				
				// Copy parameters from the request to the HLR subscription
				hlrSub.setObr(request.getObr());
				hlrSub.setRsa(request.getRsa());
				hlrSub.setCfnrc(request.getCfnrc());
				hlrSub.setNam(request.getNam());
				hlrSub.setPdpCp(request.getPdpCp());
				hlrSub.setPdpContexts(request.getPdpContexts().getPdpContext());
				// IMEI uses the synthesized value based on count
				hlrSub.setImei(imei);
	
				// Set the CAI subscription to the subscriber
				subscriber.setHlrSubscription(hlrSub);
			}
		}

		// Exit successfully
		return exitWith(response, Protocol.RESPONSE_CODE_SUCCESSFUL);
	}
}
