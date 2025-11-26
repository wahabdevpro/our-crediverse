package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.utils.protocol.ucip.FafInformation;
import hxc.utils.protocol.ucip.UpdateFaFListRequest;
import hxc.utils.protocol.ucip.UpdateFaFListResponse;

public class UpdateFaFList extends SupportedRequest<UpdateFaFListRequest, UpdateFaFListResponse>
{

	public UpdateFaFList()
	{
		super(UpdateFaFListRequest.class);
	}

	@Override
	protected UpdateFaFListResponse execute(UpdateFaFListRequest request, InjectedResponse injectedResponse)
	{
		// 100 - Other Error
		// 102 - Subscriber not found
		// 104 - Temporary blocked
		// 123 - Max credit limit exceeded
		// 124 - Below minimum balance
		// 126 - Account not active
		// 127 - Accumulator not available
		// 129 - Faf number does not exist
		// 130 - Faf number not allowed
		// 134 - Accumulator overflow
		// 135 - Accumulator underflow
		// 159 - Charged FaF not active for service class
		// 205 - Max number of FaF indicators exceeded
		// 206 - FaF indicator already exists
		// 260 - Capability not available
		// 999 - Other Error No Retry

		// Create Response
		UpdateFaFListResponse response = new UpdateFaFListResponse();
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

		// Affect the Change
		switch (request.member.getFafAction())
		{
			case ADD:
				if (request.member.fafInformation != null)
					subscriber.getFafEntries().put(request.member.fafInformation.fafNumber, request.member.fafInformation);
				break;

			case DELETE:
				if (request.member.fafInformation != null)
					subscriber.getFafEntries().remove(request.member.fafInformation.fafNumber);
				break;

			case SET:
				if (request.member.fafInformationList != null)
				{
					subscriber.getFafEntries().clear();
					for (FafInformation entry : request.member.fafInformationList)
					{
						subscriber.getFafEntries().put(entry.fafNumber, entry);
					}
				}
				break;

			default:
				return exitWith(response, response.member, 999);
		}

		// Create Response
		// response.member.setAllowedOptions(allowedOptions);
		// response.member.setChargingResultInformation(chargingResultInformation);
		// response.member.setFafChangeUnbarDate(fafChangeUnbarDate);
		// response.member.setFafMaxAllowedNumbersReachedFlag(fafMaxAllowedNumbersReachedFlag);
		// response.member.setNotAllowedReason(notAllowedReason);

		return response;
	}

}
