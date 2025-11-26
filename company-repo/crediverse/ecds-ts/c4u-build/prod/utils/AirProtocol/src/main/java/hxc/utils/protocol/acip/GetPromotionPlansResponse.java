package hxc.utils.protocol.acip;

/**
 * GetPromotionPlansResponse
 * 
 * The message GetPromotionPlans will return the promotion plans allocated to the subscribers account.
 */
public class GetPromotionPlansResponse
{
	public GetPromotionPlansResponseMember member;

	public GetPromotionPlansResponse()
	{
		member = new GetPromotionPlansResponseMember();
	}
}
