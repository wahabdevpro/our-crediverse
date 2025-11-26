package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.utils.protocol.acip.DeletePeriodicAccountManagementDataRequest;
import hxc.utils.protocol.acip.DeletePeriodicAccountManagementDataResponse;
import hxc.utils.protocol.acip.PamInformationList;

public class DeletePeriodicAccountManagementData extends SupportedRequest<DeletePeriodicAccountManagementDataRequest, DeletePeriodicAccountManagementDataResponse>
{
	public DeletePeriodicAccountManagementData()
	{
		super(DeletePeriodicAccountManagementDataRequest.class);
	}

	@Override
	protected DeletePeriodicAccountManagementDataResponse execute(DeletePeriodicAccountManagementDataRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		DeletePeriodicAccountManagementDataResponse response = new DeletePeriodicAccountManagementDataResponse();
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

		// Delete the PAMs
		for (PamInformationList pam : request.member.pamInformationList)
		{
			subscriber.getPamEntries().remove(pam.pamServiceID);
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
