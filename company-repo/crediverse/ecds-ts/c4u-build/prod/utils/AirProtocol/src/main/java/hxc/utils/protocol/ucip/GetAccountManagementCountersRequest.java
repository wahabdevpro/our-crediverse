package hxc.utils.protocol.ucip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * GetAccountManagementCountersRequest
 * 
 * The message GetAccountManagementCounters will return account management counters.
 */
@XmlRpcMethod(name = "GetAccountManagementCounters")
public class GetAccountManagementCountersRequest
{
	public GetAccountManagementCountersRequestMember member;

	public GetAccountManagementCountersRequest()
	{
		member = new GetAccountManagementCountersRequestMember();
	}
}
