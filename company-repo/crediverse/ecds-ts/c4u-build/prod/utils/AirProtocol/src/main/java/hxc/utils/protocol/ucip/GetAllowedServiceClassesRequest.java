package hxc.utils.protocol.ucip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * GetAllowedServiceClassesRequest
 * 
 * The GetAllowedServiceClasses message is used to fetch a list of service classes the subscriber is allowed to change to.
 */
@XmlRpcMethod(name = "GetAllowedServiceClasses")
public class GetAllowedServiceClassesRequest
{
	public GetAllowedServiceClassesRequestMember member;

	public GetAllowedServiceClassesRequest()
	{
		member = new GetAllowedServiceClassesRequestMember();
	}
}
