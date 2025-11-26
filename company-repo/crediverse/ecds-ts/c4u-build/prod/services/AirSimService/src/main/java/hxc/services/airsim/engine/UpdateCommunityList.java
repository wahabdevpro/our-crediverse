package hxc.services.airsim.engine;

import java.util.List;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.utils.protocol.ucip.CommunityInformationCurrent;
import hxc.utils.protocol.ucip.CommunityInformationNew;
import hxc.utils.protocol.ucip.UpdateCommunityListRequest;
import hxc.utils.protocol.ucip.UpdateCommunityListRequestMember;
import hxc.utils.protocol.ucip.UpdateCommunityListResponse;

public class UpdateCommunityList extends SupportedRequest<UpdateCommunityListRequest, UpdateCommunityListResponse>
{

	public UpdateCommunityList()
	{
		super(UpdateCommunityListRequest.class);
	}

	@Override
	protected UpdateCommunityListResponse execute(UpdateCommunityListRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		UpdateCommunityListResponse response = new UpdateCommunityListResponse();
		if (!validate(request.member, response.member, injectedResponse))
			return response;

		// Get the Subscriber
		SubscriberEx subscriber = getSubscriber(request.member);
		if (subscriber == null)
		{
			response.member.setResponseCode(102);
			return response;
		}

		// Errors
		// 0 - Success
		// 100 - Other Error
		// 102 - Subscriber not found
		// 104 - Temporary blocked
		// 147 - Invalid old community list
		// 148 - Invalid new community list
		// 260 - Capability not available
		// 999 - Other Error No Retry

		// Get Current List (may be null but longer than 3)
		UpdateCommunityListRequestMember req = request.member;
		CommunityInformationCurrent[] currentOnes = req.getCommunityInformationCurrent();
		if (currentOnes != null && currentOnes.length > 3)
			exitWith(response, response.member, 147);

		// New Info Cannot be null or longer than 3
		CommunityInformationNew[] newOnes = req.getCommunityInformationNew();
		if (newOnes == null || newOnes.length > 3)
			exitWith(response, response.member, 148);

		// Get the Actual IDs
		List<Integer> actualList = subscriber.getCommunityIDs();
		Integer[] actualOnes = actualList.toArray(new Integer[actualList.size()]);

		// Compare Current with Actual
		if (!isConsistent(currentOnes, actualOnes))
			exitWith(response, response.member, 147);

		// Update Actual
		actualList.clear();
		for (int index = 0; index < newOnes.length; index++)
		{
			int communityID = newOnes[index].communityID;
			if (index < actualOnes.length && actualOnes[index] != communityID && currentOnes == null)
				exitWith(response, response.member, 147);
			actualList.add(communityID);
		}
		subscriber.setCommunityIDs(actualList);

		return response;
	}

	private boolean isConsistent(CommunityInformationCurrent[] currentOnes, Integer[] actualOnes)
	{
		if (currentOnes == null)
			return true;

		if (currentOnes.length != actualOnes.length)
			return false;

		for (int index = 0; index < actualOnes.length; index++)
		{
			if (currentOnes[index].communityID != actualOnes[index])
				return false;
		}

		return true;
	}

}
