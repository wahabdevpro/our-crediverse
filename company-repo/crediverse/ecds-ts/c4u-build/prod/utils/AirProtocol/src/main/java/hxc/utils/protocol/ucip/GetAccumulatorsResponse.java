package hxc.utils.protocol.ucip;

/**
 * GetAccumulatorsResponse
 * 
 * The message GetAccumulators is used to obtain accumulator values and (optional) start and end dates related to those accumulators. Note: If pre-activation is wanted then
 * messageCapabilityFlag.accountActivati onFlag should be included set to 1.
 */
public class GetAccumulatorsResponse
{
	public GetAccumulatorsResponseMember member;

	public GetAccumulatorsResponse()
	{
		member = new GetAccumulatorsResponseMember();
	}
}
