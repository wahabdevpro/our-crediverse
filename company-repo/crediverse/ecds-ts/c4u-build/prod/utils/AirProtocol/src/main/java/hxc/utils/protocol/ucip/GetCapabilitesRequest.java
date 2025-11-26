package hxc.utils.protocol.ucip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * GetCapabilitesRequest
 * 
 * The message GetCapabilities is used to fetch available capabilities. See Section 9 on page 213 for available capabilities
 */
@XmlRpcMethod(name = "GetCapabilites")
public class GetCapabilitesRequest
{
	public GetCapabilitesRequestMember member;

	public GetCapabilitesRequest()
	{
		member = new GetCapabilitesRequestMember();
	}
}
