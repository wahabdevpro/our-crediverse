package hxc.utils.protocol.acip;

/**
 * UpdateAccountManagementCountersResponse
 * 
 * The message UpdateAccountManagementCounters will modify account management counters.
 */
public class UpdateAccountManagementCountersResponse
{
	public UpdateAccountManagementCountersResponseMember member;

	public UpdateAccountManagementCountersResponse()
	{
		member = new UpdateAccountManagementCountersResponseMember();
	}
}
