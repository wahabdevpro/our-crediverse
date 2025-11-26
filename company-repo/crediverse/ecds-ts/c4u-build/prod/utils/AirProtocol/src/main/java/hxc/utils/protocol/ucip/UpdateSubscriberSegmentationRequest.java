package hxc.utils.protocol.ucip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * UpdateSubscriberSegmentationRequest
 * 
 * The message UpdateSubscriberSegmentation is used in order set or update the accountGroupID and serviceOffering parameters which are used for subscriber segmentation. ServiceFee information is
 * included in the response as (PC:06214).
 */
@XmlRpcMethod(name = "UpdateSubscriberSegmentation")
public class UpdateSubscriberSegmentationRequest
{
	public UpdateSubscriberSegmentationRequestMember member;

	public UpdateSubscriberSegmentationRequest()
	{
		member = new UpdateSubscriberSegmentationRequestMember();
	}
}
