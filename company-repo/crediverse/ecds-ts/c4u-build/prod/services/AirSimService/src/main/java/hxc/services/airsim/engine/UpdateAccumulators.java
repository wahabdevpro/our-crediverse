package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.services.airsim.protocol.Accumulator;
import hxc.utils.protocol.acip.AccumulatorUpdateInformation;
import hxc.utils.protocol.acip.UpdateAccumulatorsRequest;
import hxc.utils.protocol.acip.UpdateAccumulatorsResponse;

public class UpdateAccumulators extends SupportedRequest<UpdateAccumulatorsRequest, UpdateAccumulatorsResponse>
{
	public UpdateAccumulators()
	{
		super(UpdateAccumulatorsRequest.class);
	}

	@Override
	protected UpdateAccumulatorsResponse execute(UpdateAccumulatorsRequest request, InjectedResponse injectedResponse)
	{
		// Response Codes
		// 100 - Other Error
		// 102 - Subscriber not found
		// 104 - Temporary blocked
		// 127 - Accumulator not available
		// 134 - Accumulator overflow
		// 135 - Accumulator underflow
		// 260 - Capability not available
		// 999 - Other Error No Retry

		// Create Response
		UpdateAccumulatorsResponse response = new UpdateAccumulatorsResponse();
		if (!validate(request.member, response.member, injectedResponse))
			return response;

		// Get the Subscriber
		SubscriberEx subscriber = getSubscriber(request.member);
		if (subscriber == null)
			return exitWith(response, response.member, 102);
		if (subscriber.getTemporaryBlockedFlag() != null && subscriber.getTemporaryBlockedFlag())
			return exitWith(response, response.member, 104);

		// Validate request
		boolean commit = false;
		while (true)
		{
			for (AccumulatorUpdateInformation aui : request.member.getAccumulatorUpdateInformation())
			{
				Accumulator ua = subscriber.getAccumulators().get(aui.accumulatorID);
				if (ua == null)
					return exitWith(response, response.member, 127);

				int rule = aui.accumulatorValueAbsolute != null ? 1 : 0;
				rule += aui.accumulatorValueRelative != null ? 2 : 0;

				if (rule == 3)
					return exitWith(response, response.member, 999);
				else if (rule != 0)
				{
					long newValue = rule == 1 ? aui.accumulatorValueAbsolute : ua.getAccumulatorValue() + aui.accumulatorValueRelative;
					if (newValue < -2147483648L)
						return exitWith(response, response.member, 135);
					else if (newValue > 2147483647L)
						return exitWith(response, response.member, 134);
					if (commit)
					{
						ua.setAccumulatorValue((int) newValue);
					}
				}

				if (commit)
					ua.setAccumulatorStartDate(aui.accumulatorStartDate);
			}

			if (commit)
				return response;
			else
				commit = true;

		}

	}

}
