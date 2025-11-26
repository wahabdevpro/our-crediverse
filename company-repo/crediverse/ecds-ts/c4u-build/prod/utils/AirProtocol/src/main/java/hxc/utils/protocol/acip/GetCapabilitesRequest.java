package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * GetCapabilitesRequest
 * 
 * The message GetCapabilities is used to fetch available capabilities.
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
