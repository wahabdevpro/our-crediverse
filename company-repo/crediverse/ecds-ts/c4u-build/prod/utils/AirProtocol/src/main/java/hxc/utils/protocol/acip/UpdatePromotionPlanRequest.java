package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * UpdatePromotionPlanRequest
 * 
 * The message UpdatePromotionPlan can Add, Set or Delete a promotion plan allocation to an account. The promotion plan ID has to be defined already in the business configuration in AIR, where the
 * actual execution of promotions is done. Two promotion plans can be allocated to account, but it is only possible to address one promotion plan at the time in the request. The validity periods of
 * two promotion plans for a single account are not allowed to overlap The promotion plan can not be set if it ends in the past. Note: The Promotion plan configurations are done through the ERE trees
 * and the IDs allowed in the "Update Promotion Plan" needs to be added to the Promotion Plan window. For more information see, AIR User's Guide Service Configuration Administration, Reference [3].
 */
@XmlRpcMethod(name = "UpdatePromotionPlan")
public class UpdatePromotionPlanRequest
{
	public UpdatePromotionPlanRequestMember member;

	public UpdatePromotionPlanRequest()
	{
		member = new UpdatePromotionPlanRequestMember();
	}
}
