package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.utils.protocol.acip.AddPeriodicAccountManagementDataRequest;
import hxc.utils.protocol.acip.AddPeriodicAccountManagementDataResponse;

public class AddPeriodicAccountManagementData extends SupportedRequest<AddPeriodicAccountManagementDataRequest, AddPeriodicAccountManagementDataResponse>
{

	public AddPeriodicAccountManagementData()
	{
		super(AddPeriodicAccountManagementDataRequest.class);
	}

	@Override
	protected AddPeriodicAccountManagementDataResponse execute(AddPeriodicAccountManagementDataRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		AddPeriodicAccountManagementDataResponse response = new AddPeriodicAccountManagementDataResponse();
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

		// Update Subscriber
		for (hxc.utils.protocol.acip.PamInformationList pam : request.member.getPamInformationList())
		{
			hxc.utils.protocol.ucip.PamInformationList upam = new hxc.utils.protocol.ucip.PamInformationList();
			upam.pamServiceID = pam.pamServiceID;
			upam.pamClassID = pam.pamClassID;
			upam.scheduleID = pam.scheduleID;
			upam.currentPamPeriod = pam.currentPamPeriod;
			upam.deferredToDate = pam.deferredToDate;
			upam.lastEvaluationDate = pam.lastEvaluationDate;
			upam.pamServicePriority = pam.pamServicePriority;
			subscriber.getPamEntries().put(pam.pamServiceID, upam);
		}

		// Create Response
		response.member.setPamInformationList(request.member.getPamInformationList());

		return response;
	}

}
