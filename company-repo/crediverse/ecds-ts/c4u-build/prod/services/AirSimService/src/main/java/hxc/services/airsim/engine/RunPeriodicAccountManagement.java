package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.utils.protocol.acip.RunPeriodicAccountManagementRequest;
import hxc.utils.protocol.acip.RunPeriodicAccountManagementResponse;
import hxc.utils.protocol.ucip.PamInformationList;

// Errors:
//   0: Successful
// 100: Other Error
// 102: Subscriber not found
// 104: Temporary blocked
// 126: Account not active
// 191: The PAM service id provided in the request was out of range, or did not exist
// 197: Periodic account management evaluation failed
// 199: The PAM period, provided or calculated, could not be found in the schedule
// 201: The schedule id or new schedule id provided in the request did not exist or no valid period found
// 202: Invalid PAM indicator
// 255: The offer start date can not be changed because the offer is already active.(PC:08204)
// 260: Capability not available
// 999: Other Error No Retry

public class RunPeriodicAccountManagement extends SupportedRequest<RunPeriodicAccountManagementRequest, RunPeriodicAccountManagementResponse>
{
	public RunPeriodicAccountManagement()
	{
		super(RunPeriodicAccountManagementRequest.class);
	}

	@Override
	protected RunPeriodicAccountManagementResponse execute(RunPeriodicAccountManagementRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		RunPeriodicAccountManagementResponse response = new RunPeriodicAccountManagementResponse();
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

		// Get PAM
		PamInformationList pam = subscriber.getPamEntries().get(request.member.pamServiceID);
		if (pam == null)
		{
			response.member.setResponseCode(191);
			return response;
		}

		// Create response
		response.member.originTransactionID = request.member.originTransactionID;
		response.member.originOperatorID = request.member.originOperatorID;
		response.member.pamInformation = subscriber.getPamInformationA(pam);
		response.member.negotiatedCapabilities = request.member.negotiatedCapabilities;
		// response.member.availableServerCapabilities = request.member.availableServerCapabilities;

		return response;
	}

}
