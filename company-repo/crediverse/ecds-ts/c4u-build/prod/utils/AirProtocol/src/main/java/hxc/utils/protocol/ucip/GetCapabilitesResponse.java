package hxc.utils.protocol.ucip;

/**
 * GetCapabilitesResponse
 * 
 * The message GetCapabilities is used to fetch available capabilities. See Section 9 on page 213 for available capabilities
 */
public class GetCapabilitesResponse
{
	public GetCapabilitesResponseMember member;

	public GetCapabilitesResponse()
	{
		member = new GetCapabilitesResponseMember();
	}
}
