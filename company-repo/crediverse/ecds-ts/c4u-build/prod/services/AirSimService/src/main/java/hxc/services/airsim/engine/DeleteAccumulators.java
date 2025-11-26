package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.services.airsim.protocol.Accumulator;
import hxc.utils.protocol.acip.AccumulatorIdentifier;
import hxc.utils.protocol.acip.DeleteAccumulatorsRequest;
import hxc.utils.protocol.acip.DeleteAccumulatorsResponse;

public class DeleteAccumulators extends SupportedRequest<DeleteAccumulatorsRequest, DeleteAccumulatorsResponse>
{
	public DeleteAccumulators()
	{
		super(DeleteAccumulatorsRequest.class);
	}

	@Override
	protected DeleteAccumulatorsResponse execute(DeleteAccumulatorsRequest request, InjectedResponse injectedResponse)
	{
		// Supported Response Codes
		// 100 - Other Error
		// 102 - Subscriber not found
		// 127 - Accumulator not available
		// 207 - Invalid accumulator end date
		// 208 - Invalid accumulator service class
		// 260 - Capability not available
		// 999 - Other Error No Retry

		// Create Response
		DeleteAccumulatorsResponse response = new DeleteAccumulatorsResponse();
		if (!validate(request.member, response.member, injectedResponse))
			return response;

		// Get the Subscriber
		SubscriberEx subscriber = getSubscriber(request.member);
		if (subscriber == null)
		{
			response.member.setResponseCode(102);
			return response;
		}

		// Validate all UAs to delete first
		if (request.member.accumulatorIdentifier == null || request.member.accumulatorIdentifier.length == 0)
		{
			response.member.setResponseCode(100);
			return response;
		}

		for (AccumulatorIdentifier accumulatorIdentifier : request.member.accumulatorIdentifier)
		{
			Accumulator accumulator = subscriber.getAccumulators().get(accumulatorIdentifier.accumulatorID);
			if (accumulator == null)
			{
				response.member.setResponseCode(127);
				return response;
			}

			if (accumulator.getAccumulatorEndDate() != null && //
					accumulatorIdentifier.accumulatorEndDate != null && //
					!accumulatorIdentifier.accumulatorEndDate.equals(accumulator.getAccumulatorEndDate()))
			{
				response.member.setResponseCode(207);
				return response;
			}

		}

		// Delete UAs
		for (AccumulatorIdentifier accumulatorIdentifier : request.member.accumulatorIdentifier)
		{
			subscriber.getAccumulators().remove(accumulatorIdentifier.accumulatorID);
		}

		// Create Response
		response.member.setAccumulatorInformation(subscriber.getAcipAccumulatorInformation());

		return response;
	}
}
