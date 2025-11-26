package hxc.utils.protocol.acip;

/**
 * GetTimeRestrictionResponse
 * 
 * This message retrieves time restrictions. Any number of time restriction IDs can be specified for retrieval. If no IDs are requested all the time restriction will be returned.
 */
public class GetTimeRestrictionResponse
{
	public GetTimeRestrictionResponseMember member;

	public GetTimeRestrictionResponse()
	{
		member = new GetTimeRestrictionResponseMember();
	}
}
