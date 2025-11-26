package hxc.utils.protocol.ucip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * GetAccumulatorsRequest
 * 
 * The message GetAccumulators is used to obtain accumulator values and (optional) start and end dates related to those accumulators. Note: If pre-activation is wanted then
 * messageCapabilityFlag.accountActivati onFlag should be included set to 1.
 */
@XmlRpcMethod(name = "GetAccumulators")
public class GetAccumulatorsRequest
{
	public GetAccumulatorsRequestMember member;

	public GetAccumulatorsRequest()
	{
		member = new GetAccumulatorsRequestMember();
	}
}
