package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.utils.protocol.ucip.GetAccumulatorsRequest;
import hxc.utils.protocol.ucip.GetAccumulatorsResponse;

public class GetAccumulators extends SupportedRequest<GetAccumulatorsRequest, GetAccumulatorsResponse>
{
	public GetAccumulators()
	{
		super(GetAccumulatorsRequest.class);
	}

	@Override
	protected GetAccumulatorsResponse execute(GetAccumulatorsRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		GetAccumulatorsResponse response = new GetAccumulatorsResponse();
		if (!validate(request.member, response.member, injectedResponse))
			return response;

		// Get the Subscriber
		SubscriberEx subscriber = getSubscriber(request.member);
		if (subscriber == null)
		{
			response.member.setResponseCode(102);
			return response;
		}

		// Create Response
		response.member.setLanguageIDCurrent(subscriber.getLanguageIDCurrent());
		response.member.setServiceClassCurrent(subscriber.getServiceClassCurrent());
		response.member.setAccumulatorInformation(subscriber.getAccumulatorInformation());
		response.member.setTemporaryBlockedFlag(subscriber.getTemporaryBlockedFlag());
		// response.member.setChargingResultInformation(subscriber.getChargingResultInformation());
		// response.member.setAccountFlagsAfter(subscriber.getAccountFlagsAfter());
		// response.member.setAccountFlagsBefore(subscriber.getAccountFlagsBefore());

		return response;
	}

}
