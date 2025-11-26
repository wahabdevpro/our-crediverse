package hxc.utils.protocol.ucip;

/**
 * GetUsageThresholdsAndCountersResponse
 * 
 * The message GetUsageThresholdsAndCounters is used to fetch the active usage counters and thresholds for a subscriber.
 */
public class GetUsageThresholdsAndCountersResponse
{
	public GetUsageThresholdsAndCountersResponseMember member;

	public GetUsageThresholdsAndCountersResponse()
	{
		member = new GetUsageThresholdsAndCountersResponseMember();
	}
}
