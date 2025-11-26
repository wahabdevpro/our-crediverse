package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.utils.protocol.ucip.UpdateAccountDetailsRequest;
import hxc.utils.protocol.ucip.UpdateAccountDetailsResponse;

public class UpdateAccountDetails extends SupportedRequest<UpdateAccountDetailsRequest, UpdateAccountDetailsResponse>
{
	public UpdateAccountDetails()
	{
		super(UpdateAccountDetailsRequest.class);
	}

	@Override
	protected UpdateAccountDetailsResponse execute(UpdateAccountDetailsRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		UpdateAccountDetailsResponse response = new UpdateAccountDetailsResponse();
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

		// Error 204
		if (request.member.getLanguageIDCurrent() != null && request.member.getLanguageIDCurrent() != subscriber.getLanguageIDCurrent())
		{
			response.member.setResponseCode(204);
			return response;
		}

		// Apply Changes
		if (request.member.getLanguageIDNew() != null)
		{
			int id = request.member.getLanguageIDNew();
			if (id < 1 || id > 4)
			{
				response.member.setResponseCode(141);
				return response;
			}
			subscriber.setLanguageIDCurrent(request.member.getLanguageIDNew());
		}

		if (request.member.getFirstIVRCallDoneFlag() != null)
			subscriber.setFirstIVRCallFlag((boolean) request.member.getFirstIVRCallDoneFlag());
		if (request.member.getAccountHomeRegion() != null)
			subscriber.setAccountHomeRegion(request.member.getAccountHomeRegion());
		if (request.member.getAccountTimeZone() != null)
			subscriber.setAccountTimeZone(request.member.getAccountTimeZone());
		if (request.member.getPinCode() != null)
			subscriber.setPinCode(request.member.getPinCode());
		if (request.member.getUssdEndOfCallNotificationID() != null)
			subscriber.setUssdEndOfCallNotificationID(request.member.getUssdEndOfCallNotificationID());

		// Make Result
		response.member.setServiceClassCurrent(subscriber.getServiceClassCurrent());
		response.member.setAccountFlagsAfter(null);
		response.member.setAccountFlagsBefore(null);
		// response.member.setNegotiatedCapabilities(subscriber.getNegotiatedCapabilities());
		// response.member.setAvailableServerCapabilities(subscriber.getAvailableServerCapabilities());
		response.member.setCurrency1(subscriber.getCurrency1());
		response.member.setCurrency2(subscriber.getCurrency2());
		response.member.setAccountPrepaidEmptyLimit1(subscriber.getAccountPrepaidEmptyLimit1());
		response.member.setAccountPrepaidEmptyLimit2(subscriber.getAccountPrepaidEmptyLimit2());

		return response;
	}

}
