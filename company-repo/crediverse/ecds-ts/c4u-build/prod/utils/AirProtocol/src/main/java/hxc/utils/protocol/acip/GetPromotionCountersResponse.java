package hxc.utils.protocol.acip;

/**
 * GetPromotionCountersResponse
 * 
 * The message GetPromotionCounters will return the current accumulated values used as base for the calculation of when to give a promotion and when to progress a promotion plan.
 */
public class GetPromotionCountersResponse
{
	public GetPromotionCountersResponseMember member;

	public GetPromotionCountersResponse()
	{
		member = new GetPromotionCountersResponseMember();
	}
}
