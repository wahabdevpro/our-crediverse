package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * UpdatePeriodicAccountManagementDataRequest
 * 
 * The message UpdatePeriodicAccountManagementData changes periodic account management data for a subscriber.
 */
@XmlRpcMethod(name = "UpdatePeriodicAccountManagementData")
public class UpdatePeriodicAccountManagementDataRequest
{
	public UpdatePeriodicAccountManagementDataRequestMember member;

	public UpdatePeriodicAccountManagementDataRequest()
	{
		member = new UpdatePeriodicAccountManagementDataRequestMember();
	}
}
