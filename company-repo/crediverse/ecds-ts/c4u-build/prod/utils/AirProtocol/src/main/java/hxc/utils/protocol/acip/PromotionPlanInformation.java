package hxc.utils.protocol.acip;

import java.util.Date;

import hxc.connectors.air.Air;

/**
 * PromotionPlanInformation
 * 
 * The message promotionPlanInformation is enclosed in a <struct> of its own. Structs are placed in an <array>
 */
public class PromotionPlanInformation
{
	/*
	 * The promotionPlanID parameter contains the identity of one of the current promotion plans of a subscriber.
	 */
	@Air(Mandatory = true, Length = "1:4", Format = "Alphanumeric")
	public String promotionPlanID;

	/*
	 * The promotionStartDate parameter specifies the start date of the associated promotion plan.
	 */
	@Air(Mandatory = true)
	public Date promotionStartDate;

	/*
	 * The promotionEndDate parameter specifies the end date of the associated promotion plan.
	 */
	@Air(Mandatory = true)
	public Date promotionEndDate;

}
