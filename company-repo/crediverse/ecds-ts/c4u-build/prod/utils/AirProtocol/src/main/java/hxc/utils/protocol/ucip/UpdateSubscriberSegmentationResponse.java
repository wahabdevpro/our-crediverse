package hxc.utils.protocol.ucip;

/**
 * UpdateSubscriberSegmentationResponse
 * 
 * The message UpdateSubscriberSegmentation is used in order set or update the accountGroupID and serviceOffering parameters which are used for subscriber segmentation. ServiceFee information is
 * included in the response as (PC:06214).
 */
public class UpdateSubscriberSegmentationResponse
{
	public UpdateSubscriberSegmentationResponseMember member;

	public UpdateSubscriberSegmentationResponse()
	{
		member = new UpdateSubscriberSegmentationResponseMember();
	}
}
