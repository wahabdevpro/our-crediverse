package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * DeleteUsageThresholdsRequest
 * 
 * The message DeleteUsageThresholds removes a personal or common usage threshold from a subscriber.
 */
@XmlRpcMethod(name = "DeleteUsageThresholds")
public class DeleteUsageThresholdsRequest
{
	public DeleteUsageThresholdsRequestMember member;

	public DeleteUsageThresholdsRequest()
	{
		member = new DeleteUsageThresholdsRequestMember();
	}
}
