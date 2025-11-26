package hxc.utils.protocol.ucip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * GetAccountServiceFeeDataRequest
 * 
 * The GetAccountServiceFeeData message is used to fetch service fee data tied to an account.
 */
@XmlRpcMethod(name = "GetAccountServiceFeeData")
public class GetAccountServiceFeeDataRequest
{
	public GetAccountServiceFeeDataRequestMember member;

	public GetAccountServiceFeeDataRequest()
	{
		member = new GetAccountServiceFeeDataRequestMember();
	}
}
