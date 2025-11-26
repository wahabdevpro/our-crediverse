package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.services.airsim.protocol.UsageCounter;
import hxc.utils.protocol.acip.DeleteUsageThresholdsRequest;
import hxc.utils.protocol.acip.DeleteUsageThresholdsResponse;
import hxc.utils.protocol.acip.UsageThresholdInformation;
import hxc.utils.protocol.acip.UsageThresholds;

public class DeleteUsageThresholds extends SupportedRequest<DeleteUsageThresholdsRequest, DeleteUsageThresholdsResponse>
{
	public DeleteUsageThresholds()
	{
		super(DeleteUsageThresholdsRequest.class);
	}

	@Override
	protected DeleteUsageThresholdsResponse execute(DeleteUsageThresholdsRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		DeleteUsageThresholdsResponse response = new DeleteUsageThresholdsResponse();
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

		// Process Request
		if (request.member.usageThresholds != null)
		{
			for (UsageThresholds ut : request.member.usageThresholds)
			{
				subscriber.getUsageCounters().remove(ut.usageThresholdID);
			}
		}

		// Update the Response
		response.member.currency1 = subscriber.getCurrency1();
		response.member.currency2 = subscriber.getCurrency2();
		response.member.usageThresholdInformation = new UsageThresholdInformation[subscriber.getUsageCounters().size()];
		int index = 0;
		for (UsageCounter ut : subscriber.getUsageCounters().values())
		{
			UsageThresholdInformation at = new UsageThresholdInformation();
			at.usageThresholdID = ut.getUsageCounterID();
			at.usageThresholdValue = ut.getUsageCounterValue();
			at.usageThresholdMonetaryValue1 = ut.getUsageCounterMonetaryValue1();
			at.usageThresholdMonetaryValue2 = ut.getUsageCounterMonetaryValue2();
			// at.usageThresholdSource =ut.;
			at.associatedPartyID = ut.getAssociatedPartyID();

			response.member.usageThresholdInformation[index++] = at;
		}

		return response;
	}

}
