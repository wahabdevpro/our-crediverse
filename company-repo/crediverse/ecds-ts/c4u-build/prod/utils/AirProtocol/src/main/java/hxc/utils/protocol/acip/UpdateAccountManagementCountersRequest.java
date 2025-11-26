package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * UpdateAccountManagementCountersRequest
 * 
 * The message UpdateAccountManagementCounters will modify account management counters.
 */
@XmlRpcMethod(name = "UpdateAccountManagementCounters")
public class UpdateAccountManagementCountersRequest
{
	public UpdateAccountManagementCountersRequestMember member;

	public UpdateAccountManagementCountersRequest()
	{
		member = new UpdateAccountManagementCountersRequestMember();
	}
}
