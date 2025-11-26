package hxc.utils.protocol.acip;

/**
 * UpdatePromotionCountersResponse
 * 
 * The message UpdatePromotionCounters give access to modify the counters used in the calculation when to give a promotion or promotion plan progression. It is possible to modify the accumulated value
 * or the accumulated counter used in these calculations.
 */
public class UpdatePromotionCountersResponse
{
	public UpdatePromotionCountersResponseMember member;

	public UpdatePromotionCountersResponse()
	{
		member = new UpdatePromotionCountersResponseMember();
	}
}
