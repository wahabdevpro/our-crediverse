package hxc.utils.protocol.ucip;

/**
 * GetRefillOptionsResponse
 * 
 * This message GetRefillOptions is used to fetch the refill options. Note: In case Service Class is given it takes precedence before subscriber number. It is thus possible to request refill options
 * for a Service Class which is not yet active for the given subscriber number. Note: If pre-activation is wanted then messageCapabilityFlag.accountActivati onFlag should be included set to 1.
 */
public class GetRefillOptionsResponse
{
	public GetRefillOptionsResponseMember member;

	public GetRefillOptionsResponse()
	{
		member = new GetRefillOptionsResponseMember();
	}
}
