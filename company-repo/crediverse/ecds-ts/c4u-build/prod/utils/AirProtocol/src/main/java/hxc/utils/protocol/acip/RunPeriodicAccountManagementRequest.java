package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * RunPeriodicAccountManagementRequest
 * 
 * The message RunPeriodicAccountManagement executes an on demand periodic account management evaluation.
 */
@XmlRpcMethod(name = "RunPeriodicAccountManagement")
public class RunPeriodicAccountManagementRequest
{
	public RunPeriodicAccountManagementRequestMember member;

	public RunPeriodicAccountManagementRequest()
	{
		member = new RunPeriodicAccountManagementRequestMember();
	}
}
