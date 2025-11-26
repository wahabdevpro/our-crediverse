package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.utils.protocol.ucip.UpdateServiceClassRequest;
import hxc.utils.protocol.ucip.UpdateServiceClassResponse;

public class UpdateServiceClass extends SupportedRequest<UpdateServiceClassRequest, UpdateServiceClassResponse>
{
	public UpdateServiceClass()
	{
		super(UpdateServiceClassRequest.class);
	}

	@Override
	protected UpdateServiceClassResponse execute(UpdateServiceClassRequest request, InjectedResponse injectedResponse)
	{
		// 100 - Other Error
		// 102 - Subscriber not found
		// 104 - Temporary blocked
		// 117 - Service class change not allowed
		// 123 - Max credit limit exceeded
		// 124 - Below minimum balance
		// 126 - Account not active
		// 134 - Accumulator overflow
		// 135 - Accumulator underflow
		// 140 - Invalid old Service Class
		// 154 - Invalid old SC date
		// 155 - Invalid new service class
		// 257 - Operation not allowed since End of Provisioning is set
		// 260 - Capability not available
		// 999 - Other Error No Retry

		// Create Response
		UpdateServiceClassResponse response = new UpdateServiceClassResponse();
		if (!validate(request.member, response.member, injectedResponse))
			return response;

		// Get the Subscriber
		SubscriberEx subscriber = getSubscriber(request.member);
		if (subscriber == null)
		{
			response.member.setResponseCode(102);
			return response;
		}
		if (subscriber.getTemporaryBlockedFlag() != null && subscriber.getTemporaryBlockedFlag())
			return exitWith(response, response.member, 104);

		// Current SC must Match
		if (request.member.getServiceClassCurrent() != null && request.member.getServiceClassCurrent() != subscriber.getServiceClassCurrent())
			return exitWith(response, response.member, 140);

		// Update Service Class
		subscriber.setServiceClassCurrent(request.member.getServiceClassNew());

		// Create Response
		response.member.setNotAllowedReason(null);
		response.member.setChargingResultInformation(null);
		response.member.setAccountFlagsAfter(null);
		response.member.setAccountFlagsBefore(null);
		response.member.setNegotiatedCapabilities(null);

		return response;
	}

}
