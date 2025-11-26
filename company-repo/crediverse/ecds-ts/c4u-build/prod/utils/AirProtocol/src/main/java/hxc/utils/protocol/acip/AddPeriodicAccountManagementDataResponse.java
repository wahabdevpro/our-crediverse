package hxc.utils.protocol.acip;

/**
 * AddPeriodicAccountManagementDataResponse
 * 
 * The message AddPeriodicAccountManagementData adds periodic account management data to a subscriber.
 */
public class AddPeriodicAccountManagementDataResponse
{
	public AddPeriodicAccountManagementDataResponseMember member;

	public AddPeriodicAccountManagementDataResponse()
	{
		member = new AddPeriodicAccountManagementDataResponseMember();
	}
}
