package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * UpdatePromotionCountersRequest
 * 
 * The message UpdatePromotionCounters give access to modify the counters used in the calculation when to give a promotion or promotion plan progression. It is possible to modify the accumulated value
 * or the accumulated counter used in these calculations.
 */
@XmlRpcMethod(name = "UpdatePromotionCounters")
public class UpdatePromotionCountersRequest
{
	public UpdatePromotionCountersRequestMember member;

	public UpdatePromotionCountersRequest()
	{
		member = new UpdatePromotionCountersRequestMember();
	}
}
