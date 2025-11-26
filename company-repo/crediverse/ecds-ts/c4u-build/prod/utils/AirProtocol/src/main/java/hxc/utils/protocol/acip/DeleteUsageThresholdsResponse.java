package hxc.utils.protocol.acip;

/**
 * DeleteUsageThresholdsResponse
 * 
 * The message DeleteUsageThresholds removes a personal or common usage threshold from a subscriber.
 */
public class DeleteUsageThresholdsResponse
{
	public DeleteUsageThresholdsResponseMember member;

	public DeleteUsageThresholdsResponse()
	{
		member = new DeleteUsageThresholdsResponseMember();
	}
}
