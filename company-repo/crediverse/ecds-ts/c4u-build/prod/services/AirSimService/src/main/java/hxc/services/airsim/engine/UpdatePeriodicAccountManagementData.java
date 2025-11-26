package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.utils.protocol.acip.PamUpdateInformationList;
import hxc.utils.protocol.acip.UpdatePeriodicAccountManagementDataRequest;
import hxc.utils.protocol.acip.UpdatePeriodicAccountManagementDataResponse;

public class UpdatePeriodicAccountManagementData extends SupportedRequest<UpdatePeriodicAccountManagementDataRequest, UpdatePeriodicAccountManagementDataResponse>
{
	public UpdatePeriodicAccountManagementData()
	{
		super(UpdatePeriodicAccountManagementDataRequest.class);
	}

	@Override
	protected UpdatePeriodicAccountManagementDataResponse execute(UpdatePeriodicAccountManagementDataRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		UpdatePeriodicAccountManagementDataResponse response = new UpdatePeriodicAccountManagementDataResponse();
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

		// Update the PAMs
		for (PamUpdateInformationList pam : request.member.pamUpdateInformationList)
		{
			hxc.utils.protocol.ucip.PamInformationList upam = subscriber.getPamEntries().get(pam.pamServiceID);
			if (upam != null)
			{
				upam.pamClassID = pam.pamClassIDNew;
				upam.scheduleID = pam.scheduleIDNew;
				upam.currentPamPeriod = pam.currentPamPeriod;
				upam.deferredToDate = pam.deferredToDate;
				upam.pamServicePriority = pam.pamServicePriorityNew;
			}
		}

		// Create response
		response.member.originTransactionID = request.member.originTransactionID;
		response.member.originOperatorID = request.member.originOperatorID;
		response.member.pamInformationList = subscriber.getPamInformationListA();
		response.member.negotiatedCapabilities = request.member.negotiatedCapabilities;
		// response.member.availableServerCapabilities = request.member.availableServerCapabilities;

		return response;
	}

}
