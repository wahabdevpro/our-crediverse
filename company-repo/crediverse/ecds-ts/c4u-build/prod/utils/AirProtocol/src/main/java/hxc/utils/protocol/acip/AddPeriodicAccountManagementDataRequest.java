package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * AddPeriodicAccountManagementDataRequest
 * 
 * The message AddPeriodicAccountManagementData adds periodic account management data to a subscriber.
 */
@XmlRpcMethod(name = "AddPeriodicAccountManagementData")
public class AddPeriodicAccountManagementDataRequest
{
	public AddPeriodicAccountManagementDataRequestMember member;

	public AddPeriodicAccountManagementDataRequest()
	{
		member = new AddPeriodicAccountManagementDataRequestMember();
	}
}
