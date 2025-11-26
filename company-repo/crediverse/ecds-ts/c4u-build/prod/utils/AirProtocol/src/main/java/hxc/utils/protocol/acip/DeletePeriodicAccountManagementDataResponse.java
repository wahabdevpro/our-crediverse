package hxc.utils.protocol.acip;

/**
 * DeletePeriodicAccountManagementDataResponse
 * 
 * The message DeletePeriodicAccountManagementData deletes periodic account management evaluation data for a subscriber.
 */
public class DeletePeriodicAccountManagementDataResponse
{
	public DeletePeriodicAccountManagementDataResponseMember member;

	public DeletePeriodicAccountManagementDataResponse()
	{
		member = new DeletePeriodicAccountManagementDataResponseMember();
	}
}
