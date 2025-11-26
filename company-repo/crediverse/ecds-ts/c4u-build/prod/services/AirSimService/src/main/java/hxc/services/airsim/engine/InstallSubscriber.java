package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.protocol.Subscriber;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.utils.protocol.acip.InstallSubscriberRequest;
import hxc.utils.protocol.acip.InstallSubscriberRequestMember;
import hxc.utils.protocol.acip.InstallSubscriberResponse;

// 0   Successful
// 100 Other Error
// 141 Invalid language
// 142 Subscriber already installed
// 155 Deblocking of expired account
// 157 Invalid account home region
// 190 The PAM service id provided in the request already exist
// 191 The PAM service id provided in the request was out of range, or did not exist
// 193 The PAM class id or new PAM class id provided in the request was incorrect
// 195 The schedule id or new schedule id provided in the request was incorrect
// 196 Invalid deferred to date
// 198 Too many PAM services given in the sequence or the number of services on the account would be exceeded
// 199 The PAM period, provided or calculated, could not be found in the schedule
// 200 The PAM class id or new PAM class id provided in the request does not exist
// 201 The schedule id or new schedule id provided in the request did not exist or no valid period found
// 203 Subscriber installed but marked for deletion
// 233 Service failed because new offer date provided in the request was incorrect.(PC:08204)
// 235 Not allowed to connect the PAM Class to the PAM Service. The account already has a bill cycle service. Only one bill cycle per account is allowed.
// 236 PAM Service Priority is already used for some other PAM service.
// 240 The request failed because the end time was before the start time.
// 257 Operation not allowed since End of Provisioning is set
// 260 Capability not available
// 999 Other Error No Retry

public class InstallSubscriber extends SupportedRequest<InstallSubscriberRequest, InstallSubscriberResponse>
{
	public InstallSubscriber()
	{
		super(InstallSubscriberRequest.class);
	}

	@Override
	protected InstallSubscriberResponse execute(InstallSubscriberRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		InstallSubscriberResponse response = new InstallSubscriberResponse();
		if (!validate(request.member, response.member, injectedResponse))
			return response;

		// Get the Subscriber
		Subscriber subscriber = getSubscriber(request.member);
		if (subscriber != null)
		{
			response.member.setResponseCode(142);
			return response;
		}

		// Create the Subscriber
		InstallSubscriberRequestMember member = request.member;
		subscriber = addSubscriber(member.subscriberNumber, member.languageIDNew == null ? 1 : member.languageIDNew, member.serviceClassNew, 0, SubscriberState.active);
		if (subscriber == null)
			return exitWith(response, response.member, 100);

		// Create Response
		response.member.responseCode = 0;

		return response;
	}

}
