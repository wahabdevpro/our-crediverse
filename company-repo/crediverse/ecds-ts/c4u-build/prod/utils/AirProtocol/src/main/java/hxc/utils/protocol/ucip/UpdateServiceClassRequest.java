package hxc.utils.protocol.ucip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * UpdateServiceClassRequest
 * 
 * This message UpdateServiceClass is used to update the service class (SC) for the subscriber. It is also possible to set a temporary SC with an expiry date. When temporary Service Class date is
 * expired the Account will fallback to the original Service Class defined for the account.
 */
@XmlRpcMethod(name = "UpdateServiceClass")
public class UpdateServiceClassRequest
{
	public UpdateServiceClassRequestMember member;

	public UpdateServiceClassRequest()
	{
		member = new UpdateServiceClassRequestMember();
	}
}
