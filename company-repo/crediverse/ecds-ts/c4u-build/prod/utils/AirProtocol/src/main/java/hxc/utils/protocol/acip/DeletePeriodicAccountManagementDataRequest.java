package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * DeletePeriodicAccountManagementDataRequest
 * 
 * The message DeletePeriodicAccountManagementData deletes periodic account management evaluation data for a subscriber.
 */
@XmlRpcMethod(name = "DeletePeriodicAccountManagementData")
public class DeletePeriodicAccountManagementDataRequest
{
	public DeletePeriodicAccountManagementDataRequestMember member;

	public DeletePeriodicAccountManagementDataRequest()
	{
		member = new DeletePeriodicAccountManagementDataRequestMember();
	}
}
