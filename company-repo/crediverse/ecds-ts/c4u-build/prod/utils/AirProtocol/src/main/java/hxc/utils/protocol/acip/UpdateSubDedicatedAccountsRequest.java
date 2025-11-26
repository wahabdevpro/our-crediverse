package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * UpdateSubDedicatedAccountsRequest
 * 
 * The message UpdateSubDedicatedAccounts is used by external system to adjust balances, start dates and expiry dates on the sub dedicated accounts. While it is possible to update several sub
 * dedicated accounts belonging to different composite dedicated accounts in one request, it is not possible to update several sub dedicated accounts that belong to the same composite dedicated
 * account in the same request.
 */
@XmlRpcMethod(name = "UpdateSubDedicatedAccounts")
public class UpdateSubDedicatedAccountsRequest
{
	public UpdateSubDedicatedAccountsRequestMember member;

	public UpdateSubDedicatedAccountsRequest()
	{
		member = new UpdateSubDedicatedAccountsRequestMember();
	}
}
