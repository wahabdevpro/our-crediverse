package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.utils.protocol.ucip.FafInformation;
import hxc.utils.protocol.ucip.GetFaFListRequest;
import hxc.utils.protocol.ucip.GetFaFListResponse;

public class GetFaFList extends SupportedRequest<GetFaFListRequest, GetFaFListResponse>
{

	public GetFaFList()
	{
		super(GetFaFListRequest.class);
	}

	@Override
	protected GetFaFListResponse execute(GetFaFListRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		GetFaFListResponse response = new GetFaFListResponse();
		if (!validate(request.member, response.member, injectedResponse))
			return response;

		// Get the Subscriber
		SubscriberEx subscriber = getSubscriber(request.member);
		if (subscriber == null)
		{
			response.member.setResponseCode(102);
			return response;
		}

		// Create Response
		// response.member.setFafChangeUnbarDate(fafChangeUnbarDate);
		// response.member.setFafChargingNotAllowedFlag(fafChargingNotAllowedFlag);
		FafInformation[] results = subscriber.getFafEntries().values().toArray(new FafInformation[subscriber.getFafEntries().size()]);
		for (FafInformation entry : results)
		{
			entry.fafNumber = super.getNaiNumber(entry.fafNumber, request.member.getSubscriberNumberNAI());
		}

		response.member.setFafInformationList(results);
		// response.member.setFafMaxAllowedNumbersReachedFlag(fafMaxAllowedNumbersReachedFlag);

		return response;
	}

}
