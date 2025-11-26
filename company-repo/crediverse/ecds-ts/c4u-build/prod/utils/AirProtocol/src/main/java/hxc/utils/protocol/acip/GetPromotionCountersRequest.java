package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * GetPromotionCountersRequest
 * 
 * The message GetPromotionCounters will return the current accumulated values used as base for the calculation of when to give a promotion and when to progress a promotion plan.
 */
@XmlRpcMethod(name = "GetPromotionCounters")
public class GetPromotionCountersRequest
{
	public GetPromotionCountersRequestMember member;

	public GetPromotionCountersRequest()
	{
		member = new GetPromotionCountersRequestMember();
	}
}
