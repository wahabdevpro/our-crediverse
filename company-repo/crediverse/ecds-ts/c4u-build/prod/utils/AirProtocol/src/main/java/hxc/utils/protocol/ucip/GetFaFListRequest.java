package hxc.utils.protocol.ucip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * GetFaFListRequest
 * 
 * The GetFaFList message is used to fetch the list of Family and Friends numbers with attached FaF indicators.
 */
@XmlRpcMethod(name = "GetFaFList")
public class GetFaFListRequest
{
	public GetFaFListRequestMember member;

	public GetFaFListRequest()
	{
		member = new GetFaFListRequestMember();
	}
}
