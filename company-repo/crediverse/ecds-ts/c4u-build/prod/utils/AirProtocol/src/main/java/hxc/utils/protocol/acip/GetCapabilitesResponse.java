package hxc.utils.protocol.acip;

/**
 * GetCapabilitesResponse
 * 
 * The message GetCapabilities is used to fetch available capabilities.
 */
public class GetCapabilitesResponse
{
	public GetCapabilitesResponseMember member;

	public GetCapabilitesResponse()
	{
		member = new GetCapabilitesResponseMember();
	}
}
