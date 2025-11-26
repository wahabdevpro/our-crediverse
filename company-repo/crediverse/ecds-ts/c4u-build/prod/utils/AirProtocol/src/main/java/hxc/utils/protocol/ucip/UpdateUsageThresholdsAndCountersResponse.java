package hxc.utils.protocol.ucip;

/**
 * UpdateUsageThresholdsAndCountersResponse
 * 
 * The message UpdateUsageThresholdsAndCounters is used to personalize a usage threshold for a subscriber by setting a value other than the default value, either an individual value for a subscriber
 * or an individual value for a provider shared by all consumers. The other main usage is to reset a usage counter. A counter can also be changed to any value, either by specifying a new counter value
 * or by adding or subtracting a specified value to the current counter value. When the parameter updateUsageCounterForMultiUser is included in the message the usage counters specified in
 * usageCounterUpdateInformation will be reset for all subscribers connected to the account or for the provider and all consumers. In this case the usageCounterUsageThresholdInformation in the
 * response will only contain information about the subscriber or associatedPartyID the request was directed to.
 */
public class UpdateUsageThresholdsAndCountersResponse
{
	public UpdateUsageThresholdsAndCountersResponseMember member;

	public UpdateUsageThresholdsAndCountersResponse()
	{
		member = new UpdateUsageThresholdsAndCountersResponseMember();
	}
}
