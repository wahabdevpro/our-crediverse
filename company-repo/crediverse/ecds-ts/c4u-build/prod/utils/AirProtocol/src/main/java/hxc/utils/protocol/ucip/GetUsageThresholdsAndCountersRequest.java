package hxc.utils.protocol.ucip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * GetUsageThresholdsAndCountersRequest
 * 
 * The message GetUsageThresholdsAndCounters is used to fetch the active usage counters and thresholds for a subscriber.
 */
@XmlRpcMethod(name = "GetUsageThresholdsAndCounters")
public class GetUsageThresholdsAndCountersRequest
{
	public GetUsageThresholdsAndCountersRequestMember member;

	public GetUsageThresholdsAndCountersRequest()
	{
		member = new GetUsageThresholdsAndCountersRequestMember();
	}
}
