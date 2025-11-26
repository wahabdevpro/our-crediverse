package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * GetPromotionPlansRequest
 * 
 * The message GetPromotionPlans will return the promotion plans allocated to the subscribers account.
 */
@XmlRpcMethod(name = "GetPromotionPlans")
public class GetPromotionPlansRequest
{
	public GetPromotionPlansRequestMember member;

	public GetPromotionPlansRequest()
	{
		member = new GetPromotionPlansRequestMember();
	}
}
