package hxc.utils.protocol.acip;

/**
 * UpdatePeriodicAccountManagementDataResponse
 * 
 * The message UpdatePeriodicAccountManagementData changes periodic account management data for a subscriber.
 */
public class UpdatePeriodicAccountManagementDataResponse
{
	public UpdatePeriodicAccountManagementDataResponseMember member;

	public UpdatePeriodicAccountManagementDataResponse()
	{
		member = new UpdatePeriodicAccountManagementDataResponseMember();
	}
}
