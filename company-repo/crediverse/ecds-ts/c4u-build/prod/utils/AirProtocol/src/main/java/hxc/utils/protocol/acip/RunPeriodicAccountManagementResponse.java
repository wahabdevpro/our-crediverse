package hxc.utils.protocol.acip;

/**
 * RunPeriodicAccountManagementResponse
 * 
 * The message RunPeriodicAccountManagement executes an on demand periodic account management evaluation.
 */
public class RunPeriodicAccountManagementResponse
{
	public RunPeriodicAccountManagementResponseMember member;

	public RunPeriodicAccountManagementResponse()
	{
		member = new RunPeriodicAccountManagementResponseMember();
	}
}
